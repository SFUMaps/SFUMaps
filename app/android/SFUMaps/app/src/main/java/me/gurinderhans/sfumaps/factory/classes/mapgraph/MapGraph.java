package me.gurinderhans.sfumaps.factory.classes.mapgraph;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

import java.util.List;
import java.util.Vector;

import me.gurinderhans.sfumaps.utils.MapTools;

/**
 * Created by ghans on 15-09-07.
 */

public class MapGraph {

	protected static final String TAG = MapGraph.class.getSimpleName();

	private static MapGraph mInstance = new MapGraph();

	private Vector<MapGraphNode> nodes = new Vector<>();
	private Vector<MapGraphEdge> edges = new Vector<>();

	private MapGraph() {
	}

	public static MapGraph getInstance() {
		return mInstance;
	}

	public Vector<MapGraphEdge> getEdges() {
		return edges;
	}

	public boolean addEdge(MapGraphEdge edge) {
		if (edges.contains(edge))
			return false;

		edges.add(edge);

		return true;
	}

	public Vector<MapGraphNode> getNodes() {
		return nodes;
	}

	public boolean addNode(MapGraphNode node) {
		if (nodes.contains(node))
			return false;

		nodes.add(node);

		return true;
	}

	@Nullable
	public MapGraphNode getNodeAt(LatLng position, double kmRange) {

		// create node search bounds
		LatLngBounds searchBounds = new LatLngBounds.Builder()
				.include(MapTools.LatLngFrom(position, 225, kmRange))
				.include(MapTools.LatLngFrom(position, 45, kmRange))
				.build();

		List<MapGraphNode> searchNodes = getNodes();
		if (searchNodes != null)
			for (MapGraphNode node : searchNodes)
				if (searchBounds.contains(node.getMapPosition()))
					return node;

		return null;
	}

	public void removeEdgeAt(LatLng nodePosition) {

		for (MapGraphEdge edge : getEdges()) {
			if (edge.getMapGizmo().getBounds().contains(nodePosition)) {


				MapGraphNode nodeA = edge.nodeA();
				MapGraphNode nodeB = edge.nodeB();

				if (getNodeEdges(nodeA).size() == 1) {
					if (nodeA.getMapGizmo() != null)
						nodeA.getMapGizmo().remove();

					nodeA.deleteInBackground();
					getNodes().remove(nodeB);
				}

				if (getNodeEdges(nodeB).size() == 1) {
					if (nodeB.getMapGizmo() != null)
						nodeB.getMapGizmo().remove();

					nodeB.deleteInBackground();
					getNodes().remove(nodeB);
				}

				// remove edge map gizmo, local copy and server copy
				edge.getMapGizmo().remove();
				edge.deleteInBackground();
				getEdges().remove(edge);

				return;
			}
		}
	}

	public Vector<MapGraphEdge> getNodeEdges(@NonNull MapGraphNode node) {
		Vector<MapGraphEdge> returnEdges = new Vector<>();

		for (MapGraphEdge edge : edges)
			if (edge.nodeA().equals(node) || edge.nodeB().equals(node))
				returnEdges.add(edge);

		return returnEdges;
	}

}