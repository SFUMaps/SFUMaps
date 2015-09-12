package me.gurinderhans.sfumaps.factory.classes.mapgraph;

import android.support.annotation.Nullable;

import com.google.android.gms.maps.model.GroundOverlay;
import com.parse.ParseClassName;
import com.parse.ParseObject;

import static me.gurinderhans.sfumaps.app.Keys.ParseMapGraphEdge.CLASS;
import static me.gurinderhans.sfumaps.app.Keys.ParseMapGraphEdge.NODE_FROM;
import static me.gurinderhans.sfumaps.app.Keys.ParseMapGraphEdge.NODE_TO;
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

	public MapGraphEdge(MapGraphNode nodeA) {
		put(NODE_FROM, nodeA);
	}

	public void setNodeA(MapGraphNode nodeA) {
		put(NODE_FROM, nodeA);
	}

	public void setNodeB(MapGraphNode nodeB) {
		put(NODE_TO, nodeB);
	}

	public MapGraphNode fromNode() {
		return (MapGraphNode) get(NODE_FROM);
	}

	public MapGraphNode toNode() {
		return (MapGraphNode) get(NODE_TO);
	}

	public void setRotation(float rotation) {
		put(ROTATION, rotation);
	}

	public float getRotation() {
		return getNumber(ROTATION).floatValue();
	}

	@Nullable
	public GroundOverlay getMapGizmo() {
		return map_gizmo;
	}

	public void setMapGizmo(GroundOverlay overlay) {
		this.map_gizmo = overlay;
	}

}