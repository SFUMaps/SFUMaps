package me.gurinderhans.sfumaps.factory.classes;

import android.graphics.Point;
import android.graphics.PointF;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import me.gurinderhans.sfumaps.R;
import me.gurinderhans.sfumaps.utils.MercatorProjection;

/**
 * Created by ghans on 15-08-17.
 */
public class PathSearch {

	public static final String TAG = PathSearch.class.getSimpleName();

	final GoogleMap mGoogleMap;
	final MapGrid mGrid;
	final Polyline mPathPolyline;

	// Location from
	public Point mapPointFrom;
	// Location to
	public Point mapPointTo;
	Marker markerFrom;
	Marker markerTo;

	public PathSearch(GoogleMap googleMap, MapGrid mapGrid) {
		this.mGoogleMap = googleMap;
		this.mGrid = mapGrid;

		mPathPolyline = mGoogleMap.addPolyline(new PolylineOptions().width(15).color(0xFF00AEEF).zIndex(10000));
	}

	public static List<GridNode> AStar(MapGrid grid, Point from, Point to) {
		GridNode startNode = grid.getNode(from);
		GridNode targetNode = grid.getNode(to);

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
					neighborNode.gCost = newMovementCost;
					neighborNode.hCost = dist(neighborNode, targetNode);
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

	public void recordPoint(LatLng tappedPoint) {

		Point tappedNodePoint = getGridIndices(tappedPoint);
		if (mapPointFrom == null) {
			mapPointFrom = tappedNodePoint;
			markerFrom = mGoogleMap.addMarker(new MarkerOptions()
					.position(MercatorProjection.fromPointToLatLng(
							mGrid.getNode(tappedNodePoint).projCoords))
					.icon(BitmapDescriptorFactory.fromResource(R.drawable.a)));
		} else {
			mapPointTo = tappedNodePoint;
			GridNode nodeFrom = mGrid.getNode(tappedNodePoint);
			GridNode nodeTo = mGrid.getNode(tappedNodePoint);

			if (markerTo != null)
				markerTo.remove();

			markerTo = mGoogleMap.addMarker(new MarkerOptions()
					.position(MercatorProjection.fromPointToLatLng(nodeTo.projCoords))
					.icon(BitmapDescriptorFactory.fromResource(R.drawable.b)));


			List<GridNode> path = AStar(
					mGrid, new Point(nodeFrom.gridX, nodeFrom.gridY),
					new Point(nodeTo.gridX, nodeTo.gridY));

			if (path != null) {
				List<LatLng> pathPoints = new ArrayList<>();

				for (GridNode node : path)
					pathPoints.add(MercatorProjection.fromPointToLatLng(node.projCoords));

				if (pathPoints.size() - 1 >= 0)
					pathPoints.remove(pathPoints.size() - 1);

				mPathPolyline.setPoints(pathPoints);
			}
		}
	}

	public Point getGridIndices(LatLng latLng) {

		PointF mapPoint = MercatorProjection.fromLatLngToPoint(latLng);
		PointF gridFirstPoint = mGrid.getNode(0, 0).projCoords;

		// convert dist to grid index and return the position of the node at that index
		return new Point((int) ((mapPoint.x - gridFirstPoint.x) / MapGrid.EACH_POINT_DIST), (int) ((mapPoint.y - gridFirstPoint.y) / MapGrid.EACH_POINT_DIST));
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