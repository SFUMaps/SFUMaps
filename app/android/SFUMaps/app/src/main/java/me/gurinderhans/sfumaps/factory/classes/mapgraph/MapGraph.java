package me.gurinderhans.sfumaps.factory.classes.mapgraph;

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
		/* empty constructor, not be used by anyone other than Parse */
	}


	public Vector<MapGraphEdge> getEdges() {
		return edges;
	}

	public void addEdge(MapGraphEdge edge) {
		edges.add(edge);
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

}