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
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;

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
												.zIndex(100001)
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
												.transparency(0.2f))
						);
					}
				}


				Log.i(TAG, "[graph stats] -> #nodes: " + mapGraph.getNodes().size() + ", #edges: " + mapGraph.getEdges().size());

				// test search

				try {
					MapGraphNode anode = mapGraph.getNodes().get(0);
					MapGraphNode bnode = mapGraph.getNodes().get(4);

					mGoogleMap.addMarker(new MarkerOptions().position(anode.getMapPosition()));
					mGoogleMap.addMarker(new MarkerOptions().position(bnode.getMapPosition()));

					runBFS(anode);

					List<LatLng> path = new ArrayList<>();
					// trace path back from end vertex to start
					while (bnode != null && bnode != anode) {
						path.add(bnode.getMapPosition());
						bnode = bnode.getParent();
					}
					path.add(anode.getMapPosition());

					mPathPolyline.setPoints(path);
				} catch (Exception ex) {
					ex.printStackTrace();
				}

			}
		});

	}

	public void Dijkstra(MapGraphNode source) {
		source.dist = 0f;
		PriorityQueue<MapGraphNode> q = new PriorityQueue<>();
		q.add(source);

		while (!q.isEmpty()) {
			MapGraphNode u = q.poll();

			// Visit each edge exiting u
			for (MapGraphEdge e : mapGraph.getNodeEdges(u)) {
				MapGraphNode v = getTrueNodeB(u, e);

				double weight = dist(e.nodeA(), e.nodeB());

				double distanceThroughU = u.dist + weight;
				if (distanceThroughU < v.dist) {
					q.remove(v);

					v.dist = (float) distanceThroughU;
					v.setParent(u);
					q.add(v);
				}
			}

		}


	}

	private void runBFS(MapGraphNode start) {
		// reset the graph


		// init the queue
		Queue<MapGraphNode> queue = new LinkedList<>();
		queue.add(start);

		// explore the graph
		while (!queue.isEmpty()) {
			MapGraphNode first = queue.poll();
			first.setVisited(true);
			List<MapGraphEdge> nodeEdges = mapGraph.getNodeEdges(first);

			Collections.sort(nodeEdges, new Comparator<MapGraphEdge>() {
				@Override
				public int compare(MapGraphEdge lhs, MapGraphEdge rhs) {
					double distL = dist(lhs.nodeA(), lhs.nodeB());
					double distR = dist(rhs.nodeA(), rhs.nodeB());

					return (int) (distR - distL);
				}
			});

			for (MapGraphEdge edge : nodeEdges) {

				MapGraphNode neighbor = getTrueNodeB(first, edge);

				if (!neighbor.isVisited()) {
					neighbor.setParent(first);
					queue.add(neighbor);
				}
			}
		}
	}

	public MapGraphNode getTrueNodeB(MapGraphNode trueNodeA, MapGraphEdge edge) {

		if (edge.nodeB().equals(trueNodeA)) {
			return edge.nodeA();
		}

		return edge.nodeB();
	}

	public static float dist(MapGraphNode a, MapGraphNode b) {
		LatLng latLng_A = a.getMapPosition();
		LatLng latLng_B = b.getMapPosition();
		return (float) MapTools.LatLngDistance(latLng_A.latitude, latLng_A.longitude, latLng_B.latitude, latLng_B.longitude);
	}

}