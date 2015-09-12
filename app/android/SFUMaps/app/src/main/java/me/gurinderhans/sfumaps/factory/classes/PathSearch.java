package me.gurinderhans.sfumaps.factory.classes;

import android.graphics.Point;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import me.gurinderhans.sfumaps.factory.classes.mapgraph.MapGraph;
import me.gurinderhans.sfumaps.factory.classes.mapgraph.MapGraphEdge;
import me.gurinderhans.sfumaps.factory.classes.mapgraph.MapGraphNode;

/**
 * Created by ghans on 15-08-17.
 */
public class PathSearch {

	public static final String TAG = PathSearch.class.getSimpleName();

	final GoogleMap mGoogleMap;
	final MapGraph mapGraph;
	final Polyline mPathPolyline;

	// location points
	public Point mapPointFrom;
	public Point mapPointTo;

	Marker markerFrom;
	Marker markerTo;

	public PathSearch(GoogleMap googleMap, final MapGraph graph) {
		this.mGoogleMap = googleMap;
		this.mapGraph = graph;

		mPathPolyline = mGoogleMap.addPolyline(new PolylineOptions().width(15).color(0xFF00AEEF).zIndex(10000));

	}

	private static List<MapGraphNode> AStar(MapGraph graph, MapGraphNode startNode, MapGraphNode targetNode) {

		List<MapGraphNode> openSet = new ArrayList<>();
		List<MapGraphNode> closedSet = new ArrayList<>();

		openSet.add(startNode);

		while (openSet.size() > 0) {

			// get node with min fcost from openset
			MapGraphNode currentNode = openSet.get(0);
			for (int i = 1; i < openSet.size(); i++) {
				if (openSet.get(i).getFCost() < currentNode.getFCost() || openSet.get(i).getFCost() == currentNode.getFCost() && openSet.get(i).hCost < currentNode.hCost) {
					currentNode = openSet.get(i);
				}
			}

			openSet.remove(currentNode);
			closedSet.add(currentNode);

			if (currentNode.getMapPoint().x == targetNode.getMapPoint().x && currentNode.getMapPoint().y == targetNode.getMapPoint().y) {
				// retrace path and return it
				List<MapGraphNode> path = new ArrayList<>();
				MapGraphNode thisNode = targetNode;
				while (!thisNode.equals(startNode)) {
					path.add(thisNode);
					thisNode = thisNode.parentNode;
				}
				Collections.reverse(path);

				return path;
			}

			for (MapGraphEdge edge : graph.getNodeEdges(currentNode)) {

				MapGraphNode nodeB = edge.nodeB(); // question is will the nodeB in list change too ?

				if (closedSet.contains(nodeB))
					continue;

				float newMovementCost = currentNode.gCost + dist(currentNode, nodeB);
				if (newMovementCost < nodeB.gCost || !openSet.contains(nodeB)) {

					nodeB.gCost = newMovementCost;
					nodeB.hCost = dist(nodeB, targetNode);
					nodeB.parentNode = currentNode;

					if (!openSet.contains(nodeB))
						openSet.add(nodeB);
				}
			}
		}

		return null;
	}

	public static float dist(MapGraphNode a, MapGraphNode b) {
		float dstX = Math.abs(a.getMapPoint().x - b.getMapPoint().x);
		float dstY = Math.abs(a.getMapPoint().y - b.getMapPoint().y);

		if (dstX > dstY)
			return 1.4f * dstY + (dstX - dstY);

		return 1.4f * dstX + (dstY - dstX);
	}

	/*public void drawPath(MapPlace placeFrom, MapPlace placeTo) {

		Log.i(TAG, "finding for place: " + placeFrom.getTitle());

		GridNode from = findClosestWalkablePathPoint(placeFrom.getPosition(), placeTo.getPosition());
		GridNode to = findClosestWalkablePathPoint(placeTo.getPosition(), from.projCoords);

		List<MapGraphNode> path = AStar(mapGraph, from, to);

		*//*if (path != null) {

			Log.i(TAG, "path size: " + path.size());

			List<LatLng> pathPoints = new ArrayList<>();

			for (GridNode node : path)
				pathPoints.add(MercatorProjection.fromPointToLatLng(node.projCoords));

			if (pathPoints.size() - 1 >= 0)
				pathPoints.remove(pathPoints.size() - 1);

			mPathPolyline.setPoints(pathPoints);
		}*//*

		mGoogleMap.addMarker(new MarkerOptions().position(MercatorProjection.fromPointToLatLng(from.projCoords)));
		mGoogleMap.addMarker(new MarkerOptions().position(MercatorProjection.fromPointToLatLng(to.projCoords)));
	}*/

	/*private GridNode findClosestWalkablePathPoint(PointF placePos, PointF compareTo) {
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

					GridNode checkNode = mapGraph.getNode(nX, nY);
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
	}*/

	/*private Point getGridIndices(PointF placePos) {
		PointF gridFirstPoint = mapGraph.getNode(0, 0).projCoords;
		// convert dist to grid index and return the position of the node at that index
		return new Point((int) ((placePos.x - gridFirstPoint.x) / MapGrid.EACH_POINT_DIST), (int) ((placePos.y - gridFirstPoint.y) / MapGrid.EACH_POINT_DIST));
	}*/

	public void clearPath() {

		mPathPolyline.setPoints(new ArrayList<LatLng>());

		if (markerFrom != null)
			markerFrom.remove();
		if (markerTo != null)
			markerTo.remove();

		mapPointFrom = mapPointTo = null;
	}

}