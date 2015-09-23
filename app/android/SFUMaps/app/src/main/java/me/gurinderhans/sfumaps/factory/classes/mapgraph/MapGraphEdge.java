package me.gurinderhans.sfumaps.factory.classes.mapgraph;

import com.google.android.gms.maps.model.GroundOverlay;
import com.google.android.gms.maps.model.LatLng;
import com.parse.ParseClassName;
import com.parse.ParseObject;

import static me.gurinderhans.sfumaps.app.Keys.ParseMapGraphEdge.CLASS;
import static me.gurinderhans.sfumaps.app.Keys.ParseMapGraphEdge.NODE_A_LAT;
import static me.gurinderhans.sfumaps.app.Keys.ParseMapGraphEdge.NODE_A_LNG;
import static me.gurinderhans.sfumaps.app.Keys.ParseMapGraphEdge.NODE_B_LAT;
import static me.gurinderhans.sfumaps.app.Keys.ParseMapGraphEdge.NODE_B_LNG;
import static me.gurinderhans.sfumaps.app.Keys.ParseMapGraphEdge.ROTATION;

/**
 * Created by ghans on 15-09-10.
 */

@ParseClassName(CLASS)
public class MapGraphEdge extends ParseObject {

	private GroundOverlay map_gizmo;

	private MapGraphNode nodeA = null;
	private MapGraphNode nodeB = null;

	public MapGraphEdge() {
	}

	public void setNodeA(MapGraphNode node) {
		LatLng latLng = node.getMapPosition();

		put(NODE_A_LAT, latLng.latitude);
		put(NODE_A_LNG, latLng.longitude);

		nodeA().updatePosition(latLng);
	}

	public void setNodeB(MapGraphNode node) {
		LatLng latLng = node.getMapPosition();

		put(NODE_B_LAT, latLng.latitude);
		put(NODE_B_LNG, latLng.longitude);

		nodeB().updatePosition(latLng);
	}

	public MapGraphNode nodeA() {
		if (nodeA == null)
			nodeA = new MapGraphNode(new LatLng(getDouble(NODE_A_LAT), getDouble(NODE_A_LNG)));

		return nodeA;
	}

	public MapGraphNode nodeB() {
		if (nodeB == null)
			nodeB = new MapGraphNode(new LatLng(getDouble(NODE_B_LAT), getDouble(NODE_B_LNG)));

		return nodeB;
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

	public void removeNodeA() {
		nodeA = null;
		remove(NODE_A_LAT);
		remove(NODE_A_LNG);
	}

	public void removeNodeB() {
		nodeB = null;
		remove(NODE_B_LAT);
		remove(NODE_B_LNG);
	}
}