package me.gurinderhans.sfumaps.factory.classes;

import android.graphics.PointF;
import android.util.Log;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.GroundOverlayOptions;
import com.google.android.gms.maps.model.LatLng;
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
import me.gurinderhans.sfumaps.R;
import me.gurinderhans.sfumaps.factory.classes.mapgraph.MapGraph;
import me.gurinderhans.sfumaps.factory.classes.mapgraph.MapGraphEdge;
import me.gurinderhans.sfumaps.factory.classes.mapgraph.MapGraphNode;
import me.gurinderhans.sfumaps.utils.MapTools;

import static com.parse.ParseQuery.CachePolicy.CACHE_ELSE_NETWORK;
import static com.parse.ParseQuery.CachePolicy.NETWORK_ELSE_CACHE;
import static me.gurinderhans.sfumaps.app.Keys.ParseMapGraphEdge.CLASS;
import static me.gurinderhans.sfumaps.app.Keys.ParseMapGraphEdge.NODE_A;
import static me.gurinderhans.sfumaps.app.Keys.ParseMapGraphEdge.NODE_B;
import static me.gurinderhans.sfumaps.devtools.PathMaker.EDGE_MAP_GIZMO_SIZE;
import static me.gurinderhans.sfumaps.devtools.PathMaker.NODE_MAP_GIZMO_SIZE;

/**
 * Created by ghans on 15-08-17.
 */
public class PathSearch {

	public static final String TAG = PathSearch.class.getSimpleName();

	private final GoogleMap mGoogleMap;
	private final MapGraph mapGraph = MapGraph.getInstance();
	private final Polyline mPathPolyline;

	public PathSearch(GoogleMap googleMap) {
		this.mGoogleMap = googleMap;

		mPathPolyline = mGoogleMap.addPolyline(new PolylineOptions().width(15).color(0xFF00AEEF).zIndex(10000));

		ParseQuery<ParseObject> query = ParseQuery.getQuery(CLASS);
		query.include(NODE_A);
		query.include(NODE_B);
		query.setCachePolicy(BuildConfig.DEBUG ? NETWORK_ELSE_CACHE : CACHE_ELSE_NETWORK);
		query.findInBackground(new FindCallback<ParseObject>() {
			@Override
			public void done(List<ParseObject> objects, ParseException e) {
				for (ParseObject obj : objects) {
					MapGraphEdge edge = (MapGraphEdge) obj;

					PointF dims = MapTools.getXYDist(edge.nodeA().getMapPosition(), edge.nodeB().getMapPosition());
					float pathSize = (float) Math.sqrt(dims.x * dims.x + dims.y * dims.y);

					edge.setMapGizmo(mGoogleMap.addGroundOverlay(
									new GroundOverlayOptions()
											.position(edge.nodeA().getMapPosition(), pathSize, EDGE_MAP_GIZMO_SIZE)
											.image(BitmapDescriptorFactory.fromResource(R.drawable.devtools_pathmaker_green_dot))
											.zIndex(10000)
											.anchor(0, 0.5f)
											.transparency(0.2f)
											.bearing(edge.getRotation())
							)
					);

					mapGraph.addEdge(edge);

					if (mapGraph.addNode(edge.nodeA())) {
						// set gizmo
						edge.nodeA().setMapGizmo(mGoogleMap.addGroundOverlay(
										new GroundOverlayOptions()
												.position(edge.nodeA().getMapPosition(), NODE_MAP_GIZMO_SIZE)
												.image(BitmapDescriptorFactory.fromResource(R.drawable.devtools_pathmaker_red_dot))
												.zIndex(10000)
												.transparency(0.2f))
						);
					}

					if (mapGraph.addNode(edge.nodeB())) {
						// set gizmo
						edge.nodeB().setMapGizmo(mGoogleMap.addGroundOverlay(
										new GroundOverlayOptions()
												.position(edge.nodeB().getMapPosition(), NODE_MAP_GIZMO_SIZE)
												.image(BitmapDescriptorFactory.fromResource(R.drawable.devtools_pathmaker_red_dot))
												.zIndex(10000)
												.transparency(0.2f))
						);
					}
				}


				Log.i(TAG, "[graph stats] -> #nodes: " + mapGraph.getNodes().size() + ", #edges: " + mapGraph.getEdges().size());

				MapGraphNode anode = mapGraph.getNodes().get(0);
				MapGraphNode bnode = mapGraph.getNodes().get(4);

				mGoogleMap.addMarker(new MarkerOptions().position(anode.getMapPosition()));
				mGoogleMap.addMarker(new MarkerOptions().position(bnode.getMapPosition()));

				List<MapGraphNode> path = AStar(mapGraph, anode, bnode);
				if (path != null) {

					Log.i(TAG, "path size: " + path.size());

					List<LatLng> pathPoints = new ArrayList<>();

					for (MapGraphNode node : path)
						pathPoints.add(node.getMapPosition());

					if (pathPoints.size() - 1 >= 0)
						pathPoints.remove(pathPoints.size() - 1);

					mPathPolyline.setPoints(pathPoints);
				}
			}
		});

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

}