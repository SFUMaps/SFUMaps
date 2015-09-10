package me.gurinderhans.sfumaps.factory.classes;

import com.google.android.gms.maps.model.GroundOverlay;
import com.google.android.gms.maps.model.LatLng;
import com.parse.ParseClassName;
import com.parse.ParseObject;

import java.util.ArrayList;
import java.util.List;

import me.gurinderhans.sfumaps.app.Keys.ParseMapPathNode;

/**
 * Created by ghans on 15-09-07.
 */

@ParseClassName(ParseMapPathNode.CLASS)
public class MapPathNode extends ParseObject {

	// storage
	public static List<MapPathNode> mAllMapPathNodes = new ArrayList<>();

	private GroundOverlay groundOverlay;

	public List<MapPathNode> nbrs = new ArrayList<>();

	public MapPathNode() {
		/* empty constructor, not be used by anyone other than Parse */
	}

	public LatLng getPosition() {
		return new LatLng(getDouble(ParseMapPathNode.LAT), getDouble(ParseMapPathNode.LNG));
	}

	public void setPosition(LatLng position) {
		put(ParseMapPathNode.LAT, position.latitude);
		put(ParseMapPathNode.LNG, position.longitude);

	}

	public void addNeighbour(MapPathNode node) {
//		add(ParseMapPathNode.NEIGHBORS, node);
		nbrs.add(node);
	}

	public void getNodeNeighbours() {
//		return getJSONArray()
	}

	public GroundOverlay getMapEditOverlay() {
		return groundOverlay;
	}

	public void setMapEditOverlay(GroundOverlay overlay) {
		this.groundOverlay = overlay;
	}
}