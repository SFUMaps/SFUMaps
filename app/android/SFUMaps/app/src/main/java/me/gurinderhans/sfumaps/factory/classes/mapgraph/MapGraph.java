package me.gurinderhans.sfumaps.factory.classes.mapgraph;

import android.support.annotation.NonNull;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

import java.util.List;
import java.util.Vector;

import me.gurinderhans.sfumaps.utils.MapTools;

/**
 * Created by ghans on 15-09-07.
 */

public class MapGraph {

	private Vector<MapGraphNode> nodes = new Vector<>();
	private Vector<MapGraphEdge> edges = new Vector<>();

	public MapGraph() {
	}

	public Vector<MapGraphEdge> getEdges() {
		return edges;
	}

	public boolean addEdge(MapGraphEdge edge) {
		if (edges.contains(edge))
			return false;

		edges.add(edge);
		edge.saveInBackground(); // saves edge nodes too

		return true;
	}

	public Vector<MapGraphNode> getNodes() {
		return nodes;
	}

	public void addNode(MapGraphNode node) {
		nodes.add(node);
	}

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

	public Vector<MapGraphEdge> getNodeEdges(@NonNull MapGraphNode node) {
		Vector<MapGraphEdge> returnEdges = new Vector<>();

		for (MapGraphEdge edge : edges)
			if (edge.nodeA().equals(node) || edge.nodeB().equals(node))
				returnEdges.add(edge);

		return returnEdges;
	}

}