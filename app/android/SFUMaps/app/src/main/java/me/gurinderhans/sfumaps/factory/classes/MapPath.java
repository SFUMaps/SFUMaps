package me.gurinderhans.sfumaps.factory.classes;

import com.google.android.gms.maps.model.GroundOverlay;
import com.google.android.gms.maps.model.LatLng;
import com.parse.ParseClassName;
import com.parse.ParseObject;

import java.util.ArrayList;
import java.util.List;

import me.gurinderhans.sfumaps.app.Keys.ParseMapPath;
import me.gurinderhans.sfumaps.app.Keys.ParseMapPathNode;

/**
 * Created by ghans on 15-09-07.
 */

@ParseClassName(ParseMapPath.CLASS)
public class MapPath extends ParseObject {

	// storage
	public static List<MapPath> mAllMapPaths = new ArrayList<>();

	public MapPath() {
		/* empty constructor, not be used by anyone other than Parse */
	}

	public void addNode(MapPathNode node) {
		add(ParseMapPath.NODES, node);
	}

	public List<MapPathNode> getNodes() {
		return getList(ParseMapPath.NODES);
	}

	@ParseClassName(ParseMapPathNode.CLASS)
	public static class MapPathNode extends ParseObject {

		private GroundOverlay groundOverlay;

		public MapPathNode() {
			/* empty constructor, not be used by anyone other than Parse */
		}

		public void setPosition(LatLng position) {
			put(ParseMapPathNode.LAT, position.latitude);
			put(ParseMapPathNode.LNG, position.longitude);
		}

		public LatLng getPosition() {
			return new LatLng(getDouble(ParseMapPathNode.LAT), getDouble(ParseMapPathNode.LNG));
		}

		public void addNeighbour(MapPathNode node) {
			add(ParseMapPathNode.NEIGHBORS, node);
		}

		public List<MapPathNode> getNodeNeighbours() {
			return getList(ParseMapPathNode.NEIGHBORS);
		}

		public GroundOverlay getMapEditOverlay() {
			return groundOverlay;
		}

		public void setMapEditOverlay(GroundOverlay overlay) {
			this.groundOverlay = overlay;
		}
	}
}