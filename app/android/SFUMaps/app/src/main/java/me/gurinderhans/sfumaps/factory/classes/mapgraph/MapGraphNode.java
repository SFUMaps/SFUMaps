package me.gurinderhans.sfumaps.factory.classes.mapgraph;

import android.support.annotation.Nullable;

import com.google.android.gms.maps.model.GroundOverlay;
import com.google.android.gms.maps.model.LatLng;
import com.parse.ParseClassName;
import com.parse.ParseObject;

import static me.gurinderhans.sfumaps.app.Keys.ParseMapGraphNode.CLASS;
import static me.gurinderhans.sfumaps.app.Keys.ParseMapGraphNode.LAT;
import static me.gurinderhans.sfumaps.app.Keys.ParseMapGraphNode.LNG;

/**
 * Created by ghans on 15-09-10.
 */

@ParseClassName(CLASS)
public class MapGraphNode extends ParseObject {

	private boolean visited = false;
	private GroundOverlay map_gizmo;

	public MapGraphNode() {
		/* empty constructor, not be used by anyone other than Parse */
	}

	public MapGraphNode(LatLng position) {
		put(LAT, position.latitude);
		put(LNG, position.longitude);
	}

	public LatLng getMapPosition() {
		return new LatLng(getDouble(LAT), getDouble(LNG));
	}

	public boolean isVisited() {
		return visited;
	}

	public void setVisited(boolean visited) {
		this.visited = visited;
	}

	public GroundOverlay getMapGizmo() {
		return map_gizmo;
	}

	public void setMapGizmo(GroundOverlay overlay) {
		this.map_gizmo = overlay;
	}

}