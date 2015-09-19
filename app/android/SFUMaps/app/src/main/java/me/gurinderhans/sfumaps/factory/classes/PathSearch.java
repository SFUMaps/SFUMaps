package me.gurinderhans.sfumaps.factory.classes;

import android.graphics.PointF;
import android.util.Log;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.GroundOverlayOptions;
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
import java.util.PriorityQueue;

import me.gurinderhans.sfumaps.BuildConfig;
import me.gurinderhans.sfumaps.R;
import me.gurinderhans.sfumaps.factory.classes.mapgraph.MapGraph;
import me.gurinderhans.sfumaps.factory.classes.mapgraph.MapGraphEdge;
import me.gurinderhans.sfumaps.factory.classes.mapgraph.MapGraphNode;
import me.gurinderhans.sfumaps.utils.MercatorProjection;

import static com.parse.ParseQuery.CachePolicy.CACHE_ELSE_NETWORK;
import static com.parse.ParseQuery.CachePolicy.NETWORK_ELSE_CACHE;
import static me.gurinderhans.sfumaps.app.Keys.ParseMapGraphEdge.CLASS;
import static me.gurinderhans.sfumaps.app.Keys.ParseMapGraphEdge.NODE_A;
import static me.gurinderhans.sfumaps.app.Keys.ParseMapGraphEdge.NODE_B;
import static me.gurinderhans.sfumaps.devtools.PathMaker.EDGE_MAP_GIZMO_SIZE;
import static me.gurinderhans.sfumaps.devtools.PathMaker.NODE_MAP_GIZMO_SIZE;
import static me.gurinderhans.sfumaps.utils.MapTools.LatLngDistance;
import static me.gurinderhans.sfumaps.utils.MapTools.getXYDist;

/**
 * Created by ghans on 15-08-17.
 */
public class PathSearch {

	public static final String TAG = PathSearch.class.getSimpleName();

	private final GoogleMap mGoogleMap;
	private final MapGraph mapGraph = MapGraph.getInstance();
	private final Polyline mPathPolyline;

	Marker fromMarker, toMarker;

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

					PointF dims = getXYDist(edge.nodeA().getMapPosition(), edge.nodeB().getMapPosition());
					float pathSize = (float) Math.sqrt(dims.x * dims.x + dims.y * dims.y);

					edge.setMapGizmo(mGoogleMap.addGroundOverlay(
									new GroundOverlayOptions()
											.position(edge.nodeA().getMapPosition(), pathSize, EDGE_MAP_GIZMO_SIZE)
											.image(BitmapDescriptorFactory.fromResource(R.drawable.devtools_pathmaker_green_dot))
											.zIndex(10000)
											.anchor(0, 0.5f)
											.visible(false) //
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
												.zIndex(100001)
												.visible(false) //
												.transparency(0.2f))
						);
					}

					if (mapGraph.addNode(edge.nodeB())) {
						// set gizmo
						edge.nodeB().setMapGizmo(mGoogleMap.addGroundOverlay(
										new GroundOverlayOptions()
												.position(edge.nodeB().getMapPosition(), NODE_MAP_GIZMO_SIZE)
												.image(BitmapDescriptorFactory.fromResource(R.drawable.devtools_pathmaker_red_dot))
												.zIndex(100001)
												.visible(false) //
												.transparency(0.2f))
						);
					}
				}

			}
		});

	}

	public void newSearch(MapPlace from, MapPlace to) {
		try {

			// anode
			MapGraphNode anode = null;
			double minDist = Double.POSITIVE_INFINITY;
			for (MapGraphNode node : mapGraph.getNodes()) {
				LatLng fromLL = MercatorProjection.fromPointToLatLng(from.getPosition());
				LatLng nodeLL = node.getMapPosition();
				if (LatLngDistance(fromLL.latitude, fromLL.longitude, nodeLL.latitude, nodeLL.longitude) < minDist) {
					anode = node;
					minDist = LatLngDistance(fromLL.latitude, fromLL.longitude, nodeLL.latitude, nodeLL.longitude);
				}
			}

			// anode
			MapGraphNode bnode = null;
			double bminDist = Double.POSITIVE_INFINITY;
			for (MapGraphNode node : mapGraph.getNodes()) {
				LatLng fromLL = MercatorProjection.fromPointToLatLng(to.getPosition());
				LatLng nodeLL = node.getMapPosition();
				if (LatLngDistance(fromLL.latitude, fromLL.longitude, nodeLL.latitude, nodeLL.longitude) < bminDist) {
					bnode = node;
					bminDist = LatLngDistance(fromLL.latitude, fromLL.longitude, nodeLL.latitude, nodeLL.longitude);
				}
			}

			Log.i(TAG, "anode: " + anode);
			Log.i(TAG, "bnode: " + bnode);

			fromMarker = mGoogleMap.addMarker(new MarkerOptions().position(anode.getMapPosition()));
			toMarker = mGoogleMap.addMarker(new MarkerOptions().position(bnode.getMapPosition()));

			Dijkstra(mapGraph, anode);

			List<LatLng> path = getShortestPathTo(bnode);
			mPathPolyline.setPoints(path);

		} catch (Exception ex) {
			// TODO: 15-09-19 Toast error
			ex.printStackTrace();
		}
	}

	// TODO: 15-09-16 Switch to AStar, Dijkstra is too slow!!
	public static void Dijkstra(MapGraph graph, MapGraphNode source) {

		// reset graph
		for (MapGraphNode node : graph.getNodes()) {
			node.setDist(Double.POSITIVE_INFINITY);
			node.setParent(null);
		}

		source.setDist(0d);
		PriorityQueue<MapGraphNode> vertexQueue = new PriorityQueue<>();
		vertexQueue.add(source);

		while (!vertexQueue.isEmpty()) {
			MapGraphNode u = vertexQueue.poll();

			// Visit each edge exiting u
			for (MapGraphEdge e : graph.getNodeEdges(u)) {
				MapGraphNode v = getTrueNodeB(u, e);

				PointF point = getXYDist(u.getMapPosition(), v.getMapPosition());
				double weight = Math.sqrt(point.x * point.x + point.y * point.y);
				double distanceThroughU = u.getDist() + weight;

				// TODO: 15-09-16 create a method to get the node at exact position

				if (distanceThroughU < graph.getNodeAt(v.getMapPosition(), 0.5).getDist()) {

					graph.getNodeAt(v.getMapPosition(), 0.5).setDist(distanceThroughU);
					graph.getNodeAt(v.getMapPosition(), 0.5).setParent(u);

					vertexQueue.add(graph.getNodeAt(v.getMapPosition(), 0.5));
				}
			}
		}

	}

	public static List<LatLng> getShortestPathTo(MapGraphNode target) {
		List<LatLng> path = new ArrayList<>();

		for (MapGraphNode vertex = target; vertex != null; vertex = vertex.getParent())
			path.add(vertex.getMapPosition());

		Collections.reverse(path);
		return path;
	}

	public static MapGraphNode getTrueNodeB(MapGraphNode trueNodeA, MapGraphEdge edge) {

		if (edge.nodeB().equals(trueNodeA))
			return edge.nodeA();

		return edge.nodeB();
	}

	public void clearPaths() {
		mPathPolyline.setPoints(new ArrayList<LatLng>());
		if (fromMarker != null) {
			fromMarker.remove();
			fromMarker = null;
		}

		if (toMarker != null) {
			toMarker.remove();
			toMarker = null;
		}

	}
}