package me.gurinderhans.sfumaps.factory.classes;

import android.graphics.Point;
import android.graphics.PointF;
import android.util.Log;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import me.gurinderhans.sfumaps.BuildConfig;
import me.gurinderhans.sfumaps.app.Keys;
import me.gurinderhans.sfumaps.factory.classes.MapGrid.GridNode;
import me.gurinderhans.sfumaps.utils.MercatorProjection;

import static com.parse.ParseQuery.CachePolicy.CACHE_ELSE_NETWORK;
import static com.parse.ParseQuery.CachePolicy.NETWORK_ELSE_CACHE;

/**
 * Created by ghans on 15-08-17.
 */
public class PathSearch {

	public static final String TAG = PathSearch.class.getSimpleName();

	final GoogleMap mGoogleMap;
	final MapGrid mGrid;
	final Polyline mPathPolyline;

	// location points
	public Point mapPointFrom;
	public Point mapPointTo;

	Marker markerFrom;
	Marker markerTo;

	public PathSearch(GoogleMap googleMap, final MapGrid mapGrid) {
		this.mGoogleMap = googleMap;
		this.mGrid = mapGrid;

		mPathPolyline = mGoogleMap.addPolyline(new PolylineOptions().width(15).color(0xFF00AEEF).zIndex(10000));

		// load map path data
		ParseQuery<ParseObject> query = ParseQuery.getQuery(Keys.ParseMapGraph.CLASS);
		query.setCachePolicy(BuildConfig.DEBUG ? NETWORK_ELSE_CACHE : CACHE_ELSE_NETWORK);
		query.findInBackground(new FindCallback<ParseObject>() {
			@Override
			public void done(List<ParseObject> objects, ParseException e) {
				// There was an error or the network wasn't available.
				if (e != null)
					return;

				/*for (ParseObject obj : objects) {
					MapGraph mapPath = (MapGraph) obj;

					GroundOverlay groundOverlay = mGoogleMap.addGroundOverlay(new GroundOverlayOptions()
									.image(BitmapDescriptorFactory.fromResource(R.drawable.devtools_pathmaker_green_dot))
									.zIndex(10000)
									.transparency(0.2f)
									.position(MercatorProjection.fromPointToLatLng(mapGrid.getNode(mapPath.getPosition()).projCoords), 10000)
									.anchor(0, 0.5f)
									.visible(false)
					);


					PointF dims = PathMaker.getXYDist(
							MercatorProjection.fromPointToLatLng(mGrid.getNode(mapPath.getPosition()).projCoords),
							MercatorProjection.fromPointToLatLng(mGrid.getNode(mapPath.getEndPoint()).projCoords)
					);

					float pathSize = dims.x + dims.y;
					if (mapPath.getRotation() % 45 == 0)
						pathSize = (float) Math.sqrt(dims.x * dims.x + dims.y * dims.y);

					groundOverlay.setDimensions(pathSize, 10000);

					int rotation = (int) mapPath.getRotation();
					switch (rotation) {
						case 90:
						case 270:
							groundOverlay.setBearing(90);
							break;
						case 0:
						case 180:
							groundOverlay.setBearing(0);
							break;
						case -45:
							groundOverlay.setBearing(135);
							break;
						case 45:
							groundOverlay.setBearing(45);
						default:
							break;
					}

					mapPath.setMapEditOverlay(groundOverlay);
					MapGraph.mAllMapGraphs.add(mapPath);

					mGrid.createWalkableArea(mapPath.getPosition(), mapPath.getEndPoint(), mapPath.getRotation());
				}*/
			}
		});

	}

	private static List<GridNode> AStar(MapGrid grid, GridNode startNode, GridNode targetNode) {

		List<GridNode> openSet = new ArrayList<>();
		List<GridNode> closedSet = new ArrayList<>();

		openSet.add(startNode);

		while (openSet.size() > 0) {

			// get node with min fcost from openset
			GridNode currentNode = openSet.get(0);
			for (int i = 1; i < openSet.size(); i++) {
				if (openSet.get(i).getFCost() < currentNode.getFCost() || openSet.get(i).getFCost() == currentNode.getFCost() && openSet.get(i).hCost < currentNode.hCost) {
					currentNode = openSet.get(i);
				}
			}

			openSet.remove(currentNode);
			closedSet.add(currentNode);

			if (currentNode.gridX == targetNode.gridX && currentNode.gridY == targetNode.gridY) {
				// retrace path and return it
				List<GridNode> path = new ArrayList<>();
				GridNode thisNode = targetNode;
				while (thisNode != startNode) {
					path.add(thisNode);
					thisNode = thisNode.parentNode;
				}
				Collections.reverse(path);

				return path;
			}

			for (GridNode neighborNode : grid.getNeighbors(currentNode)) {

				if (!neighborNode.isWalkable() || closedSet.contains(neighborNode))
					continue;

				float newMovementCost = currentNode.gCost + dist(currentNode, neighborNode);
				if (newMovementCost < neighborNode.gCost || !openSet.contains(neighborNode)) {
					float gcost = newMovementCost;
					float hcost = dist(neighborNode, targetNode);

					// this is to avoid staircase effect at some diagonal turns
					// NOTE: not sure but this maaay have broken (A *)
					if (currentNode.gridX - neighborNode.gridX != 0) {
						gcost += 0.01;
						hcost += 0.01;
					}

					neighborNode.gCost = gcost;
					neighborNode.hCost = hcost;


					neighborNode.parentNode = currentNode;

					if (!openSet.contains(neighborNode))
						openSet.add(neighborNode);
				}
			}
		}

		return null;
	}

	public static float dist(GridNode a, GridNode b) {
		float dstX = Math.abs(a.gridX - b.gridX);
		float dstY = Math.abs(a.gridY - b.gridY);

		if (dstX > dstY)
			return 1.4f * dstY + (dstX - dstY);

		return 1.4f * dstX + (dstY - dstX);
	}

	public void drawPath(MapPlace placeFrom, MapPlace placeTo) {

		Log.i(TAG, "finding for place: " + placeFrom.getTitle());

		GridNode from = findClosestWalkablePathPoint(placeFrom.getPosition(), placeTo.getPosition());
		GridNode to = findClosestWalkablePathPoint(placeTo.getPosition(), from.projCoords);

		List<GridNode> path = AStar(mGrid, from, to);

		if (path != null) {

			Log.i(TAG, "path size: " + path.size());

			List<LatLng> pathPoints = new ArrayList<>();

			for (GridNode node : path)
				pathPoints.add(MercatorProjection.fromPointToLatLng(node.projCoords));

			if (pathPoints.size() - 1 >= 0)
				pathPoints.remove(pathPoints.size() - 1);

			mPathPolyline.setPoints(pathPoints);
		}

		mGoogleMap.addMarker(new MarkerOptions().position(MercatorProjection.fromPointToLatLng(from.projCoords)));
		mGoogleMap.addMarker(new MarkerOptions().position(MercatorProjection.fromPointToLatLng(to.projCoords)));
	}

	private GridNode findClosestWalkablePathPoint(PointF placePos, PointF compareTo) {
		Point gridNodeIndices = getGridIndices(placePos);
		Point compareToGridNode = getGridIndices(compareTo);


		List<GridNode> possibleWalkableNodes = new ArrayList<>();

		int expander = 0;
		while (possibleWalkableNodes.isEmpty()) {
			for (int x = -2 - expander; x <= 2 + expander; x++)
				for (int y = -2 - expander; y <= 2 + expander; y++) {
					if (x == 0 && y == 0)
						continue;

					int nX = gridNodeIndices.x + x;
					int nY = gridNodeIndices.y + y;

					GridNode checkNode = mGrid.getNode(nX, nY);
					if (checkNode.isWalkable())
						if (checkNode.gridX == gridNodeIndices.x || checkNode.gridY == gridNodeIndices.y
//								|| (Math.abs(nX - gridNodeIndices.x) == Math.abs(nY - gridNodeIndices.y))
								)
							possibleWalkableNodes.add(checkNode);
				}

			expander += 2;
		}

		// filter the point closest to placeFrom AND placeTo
		int lowestLength = Integer.MAX_VALUE;
		GridNode filteredNode = null;
		for (GridNode node : possibleWalkableNodes) {
			int fromX = Math.abs(node.gridX - gridNodeIndices.x),
					fromY = Math.abs(node.gridY - gridNodeIndices.y),
					toX = Math.abs(node.gridX - compareToGridNode.x),
					toY = Math.abs(node.gridY - compareToGridNode.y);

			int path_length = fromX + fromY + toX + toY;
			if (path_length < lowestLength) {
				lowestLength = path_length;
				filteredNode = node;
			}
		}

		return filteredNode;
	}

	private Point getGridIndices(PointF placePos) {
		PointF gridFirstPoint = mGrid.getNode(0, 0).projCoords;
		// convert dist to grid index and return the position of the node at that index
		return new Point((int) ((placePos.x - gridFirstPoint.x) / MapGrid.EACH_POINT_DIST), (int) ((placePos.y - gridFirstPoint.y) / MapGrid.EACH_POINT_DIST));
	}

	public void clearPath() {

		mPathPolyline.setPoints(new ArrayList<LatLng>());

		if (markerFrom != null)
			markerFrom.remove();
		if (markerTo != null)
			markerTo.remove();

		mapPointFrom = mapPointTo = null;
	}

}