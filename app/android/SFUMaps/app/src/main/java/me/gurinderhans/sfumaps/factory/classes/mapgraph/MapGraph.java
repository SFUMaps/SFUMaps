package me.gurinderhans.sfumaps.factory.classes.mapgraph;

import android.support.annotation.NonNull;

import com.google.android.gms.maps.model.LatLng;
import com.parse.ParseClassName;
import com.parse.ParseObject;

import java.util.Vector;

import static me.gurinderhans.sfumaps.app.Keys.ParseMapGraph.CLASS;

/**
 * Created by ghans on 15-09-07.
 */

@ParseClassName(CLASS)
public class MapGraph extends ParseObject {

	protected Vector<MapGraphNode> nodes = new Vector<>();
	protected Vector<MapGraphEdge> edges = new Vector<>();

	public MapGraph() {
		/* empty constructor, not be used by anyone other than Parse */
	}

	public boolean addEdge(@NonNull LatLng nodeAPos, @NonNull LatLng nodeBPos) {

		MapGraphNode nodeA = new MapGraphNode(nodeAPos);
		MapGraphNode nodeB = new MapGraphNode(nodeBPos);
		MapGraphEdge edge = new MapGraphEdge(nodeA, nodeB, 0);

		return !(edges.contains(edge) || (nodes.contains(nodeA) && nodes.contains(nodeB))) && edges.add(edge);

	}

	public Vector<MapGraphNode> getNodes() {
		return nodes;
	}

	public Vector<MapGraphEdge> getEdges() {
		return edges;
	}

	public MapGraphNode getNodeAt(int i) {
		return nodes.elementAt(i);
	}

}