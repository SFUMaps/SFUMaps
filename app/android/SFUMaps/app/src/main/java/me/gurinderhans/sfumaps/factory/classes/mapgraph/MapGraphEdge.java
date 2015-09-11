package me.gurinderhans.sfumaps.factory.classes.mapgraph;

import com.parse.ParseClassName;
import com.parse.ParseObject;

import static me.gurinderhans.sfumaps.app.Keys.ParseMapGraphEdge.CLASS;
import static me.gurinderhans.sfumaps.app.Keys.ParseMapGraphEdge.NODE_FROM;
import static me.gurinderhans.sfumaps.app.Keys.ParseMapGraphEdge.NODE_TO;
import static me.gurinderhans.sfumaps.app.Keys.ParseMapGraphEdge.WEIGHT;

/**
 * Created by ghans on 15-09-10.
 */

@ParseClassName(CLASS)
public class MapGraphEdge extends ParseObject {

	public MapGraphEdge() {
			/* empty constructor, not be used by anyone other than Parse */
	}

	public MapGraphEdge(MapGraphNode nodeFrom, MapGraphNode nodeTo, int edgeWeight) {
		put(NODE_FROM, nodeFrom);
		put(NODE_TO, nodeFrom);
		put(WEIGHT, nodeFrom);
	}

	public MapGraphNode fromNode() {
		return (MapGraphNode) get(NODE_FROM);
	}

	public MapGraphNode toNode() {
		return (MapGraphNode) get(NODE_TO);
	}

}