package me.gurinderhans.sfumaps.factory.classes;

import android.content.Context;
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
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.PriorityQueue;

import me.gurinderhans.sfumaps.R;
import me.gurinderhans.sfumaps.app.Keys.ParseMapGraphEdge;
import me.gurinderhans.sfumaps.devtools.PathMaker;
import me.gurinderhans.sfumaps.factory.classes.mapgraph.MapGraph;
import me.gurinderhans.sfumaps.factory.classes.mapgraph.MapGraphEdge;
import me.gurinderhans.sfumaps.factory.classes.mapgraph.MapGraphNode;
import me.gurinderhans.sfumaps.utils.MercatorProjection;
import me.gurinderhans.sfumaps.utils.Tools.DataUtils;

import static me.gurinderhans.sfumaps.utils.Tools.LocationUtils.LatLngDistance;
import static me.gurinderhans.sfumaps.utils.Tools.LocationUtils.getXYDist;

/**
 * Created by ghans on 15-08-17.
 */
public class PathSearch {

	public static final String TAG = PathSearch.class.getSimpleName();

	private final GoogleMap mGoogleMap;
	private final MapGraph mapGraph = MapGraph.getInstance();
	private final Polyline mPathPolyline;

	Marker fromMarker, toMarker;

	public PathSearch(Context c, GoogleMap googleMap) {
		this.mGoogleMap = googleMap;

		mPathPolyline = mGoogleMap.addPolyline(new PolylineOptions().width(15).color(0xFF00AEEF).zIndex(10000));

		// fetch graph paths
		ParseQuery<ParseObject> query = ParseQuery.getQuery(ParseMapGraphEdge.CLASS);
		DataUtils.parseFetchClass(c, query, new ArrayList<String>(0), new DataUtils.FetchResultsCallback() {
			@Override
			public void onResults(List<?> objects) {
				Log.i(TAG, "edges: " + objects.size());
				for (Object obj : objects) {
					MapGraphEdge edge = (MapGraphEdge) obj;
					PointF dims = getXYDist(edge.nodeA().getMapPosition(), edge.nodeB().getMapPosition());
					float pathSize = (float) Math.sqrt(dims.x * dims.x + dims.y * dims.y);

					edge.setMapGizmo(mGoogleMap.addGroundOverlay(
									new GroundOverlayOptions()
											.position(edge.nodeA().getMapPosition(), pathSize, PathMaker.EDGE_MAP_GIZMO_SIZE)
											.image(BitmapDescriptorFactory.fromResource(R.drawable.devtools_pathmaker_green_dot))
											.zIndex(10000)
											.anchor(0, 0.5f)
											.visible(false)
											.transparency(0.2f)
											.bearing(edge.getRotation())
							)
					);

					mapGraph.addEdge(edge);

					if (mapGraph.addNode(edge.nodeA()))
						edge.nodeA().setMapGizmo(mGoogleMap.addGroundOverlay(
										new GroundOverlayOptions()
												.position(edge.nodeA().getMapPosition(), PathMaker.NODE_MAP_GIZMO_SIZE)
												.image(BitmapDescriptorFactory.fromResource(R.drawable.devtools_pathmaker_red_dot))
												.zIndex(100001)
												.visible(false)
												.transparency(0.2f))
						);

					if (mapGraph.addNode(edge.nodeB()))
						edge.nodeB().setMapGizmo(mGoogleMap.addGroundOverlay(
										new GroundOverlayOptions()
												.position(edge.nodeB().getMapPosition(), PathMaker.NODE_MAP_GIZMO_SIZE)
												.image(BitmapDescriptorFactory.fromResource(R.drawable.devtools_pathmaker_red_dot))
												.zIndex(100001)
												.visible(false)
												.transparency(0.2f))
						);
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

			fromMarker = mGoogleMap.addMarker(new MarkerOptions()
							.position(anode.getMapPosition())
							.anchor(0.5f, 0.5f)
							.icon(BitmapDescriptorFactory.fromResource(R.drawable.directions_from))
			);
			toMarker = mGoogleMap.addMarker(new MarkerOptions()
							.position(bnode.getMapPosition())
							.anchor(0.5f, 0.5f)
							.icon(BitmapDescriptorFactory.fromResource(R.drawable.directions_to))
			);

			Dijkstra(mapGraph, anode);

			List<LatLng> path = getShortestPathTo(bnode);
			mPathPolyline.setPoints(path);

		} catch (Exception ex) {
			// TODO: 15-09-19 Toast error
			ex.printStackTrace();
		}
	}

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

		// remove first element
//		if (path.size() > 0)
//			path.remove(0);

		// remove last element
//		if (path.size() > 1)
//			path.remove(path.size() - 1);


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