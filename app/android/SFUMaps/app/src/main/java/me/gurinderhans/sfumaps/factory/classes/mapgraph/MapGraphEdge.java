package me.gurinderhans.sfumaps.factory.classes.mapgraph;

import com.google.android.gms.maps.model.GroundOverlay;
import com.parse.ParseClassName;
import com.parse.ParseObject;

import static me.gurinderhans.sfumaps.app.Keys.ParseMapGraphEdge.CLASS;
import static me.gurinderhans.sfumaps.app.Keys.ParseMapGraphEdge.NODE_A;
import static me.gurinderhans.sfumaps.app.Keys.ParseMapGraphEdge.NODE_B;
import static me.gurinderhans.sfumaps.app.Keys.ParseMapGraphEdge.ROTATION;

/**
 * Created by ghans on 15-09-10.
 */

@ParseClassName(CLASS)
public class MapGraphEdge extends ParseObject {

	private GroundOverlay map_gizmo;

	public MapGraphEdge() {
			/* empty constructor, not be used by anyone other than Parse */
	}

	public static MapGraphEdge createEdge(MapGraphNode nodeA) {
		return new MapGraphEdge(nodeA);
	}

	private MapGraphEdge(MapGraphNode nodeA) {
		put(NODE_A, nodeA);
	}

	public void setNodeA(MapGraphNode nodeA) {
		put(NODE_A, nodeA);
	}

	public void setNodeB(MapGraphNode nodeB) {
		put(NODE_B, nodeB);
	}

	public MapGraphNode nodeA() {
		return (MapGraphNode) get(NODE_A);
	}

	public MapGraphNode nodeB() {
		return (MapGraphNode) get(NODE_B);
	}

	public void setRotation(float rotation) {
		put(ROTATION, rotation);
	}

	public float getRotation() {
		return getNumber(ROTATION).floatValue();
	}

	public GroundOverlay getMapGizmo() {
		return map_gizmo;
	}

	public void setMapGizmo(GroundOverlay overlay) {
		this.map_gizmo = overlay;
	}

}