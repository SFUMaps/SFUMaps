package me.gurinderhans.sfumaps.factory.classes.mapgraph;

import android.graphics.PointF;
import android.support.annotation.NonNull;

import com.google.android.gms.maps.model.GroundOverlay;
import com.google.android.gms.maps.model.LatLng;

import me.gurinderhans.sfumaps.utils.MercatorProjection;

/**
 * Created by ghans on 15-09-10.
 */

public class MapGraphNode implements Comparable<MapGraphNode> {


	private GroundOverlay map_gizmo = null;
	private boolean visited = false;
	private LatLng nodePos = null;

	private MapGraphNode parent;
	private double dist = Double.POSITIVE_INFINITY;

	public MapGraphNode(LatLng position) {
		this.nodePos = position;
	}

	public MapGraphNode getParent() {
		return parent;
	}

	public void setParent(MapGraphNode parent) {
		this.parent = parent;
	}

	public PointF getMapPoint() {
		return MercatorProjection.fromLatLngToPoint(getMapPosition());
	}

	public double getDist() {
		return dist;
	}

	public void setDist(double dist) {
		this.dist = dist;
	}

	@NonNull
	public LatLng getMapPosition() {
		return nodePos;
	}

	public void updatePosition(LatLng position) {
		this.nodePos = position;
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


	@Override
	public boolean equals(Object o) {
		boolean retVal = false;

		if (o instanceof MapGraphNode) {
			MapGraphNode ptr = (MapGraphNode) o;
			retVal = ptr.getMapPosition().equals(this.getMapPosition());
		}

		return retVal;
	}

	@Override
	public int hashCode() {
		int hash = 7;
		hash = hash + (this.getMapPosition() != null ? this.getMapPosition().hashCode() : 0);
		return hash;
	}

	@Override
	public int compareTo(MapGraphNode another) {
		return Double.compare(dist, another.dist);
	}
}