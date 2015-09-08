package me.gurinderhans.sfumaps.factory.classes;

import android.graphics.Point;

import com.google.android.gms.maps.model.GroundOverlay;
import com.parse.ParseClassName;
import com.parse.ParseObject;

import java.util.ArrayList;
import java.util.List;

import me.gurinderhans.sfumaps.app.Keys.ParseMapPath;

/**
 * Created by ghans on 15-09-07.
 */

@ParseClassName(ParseMapPath.CLASS)
public class MapPath extends ParseObject {

	// storage
	public static List<MapPath> mAllMapPaths = new ArrayList<>();

	private GroundOverlay groundOverlay;

	public MapPath() {
		/* empty constructor, not be used by anyone other than Parse */
	}

	public void setStartPoint(Point startPoint) {
		put(ParseMapPath.POINT_START_X, startPoint.x);
		put(ParseMapPath.POINT_START_Y, startPoint.y);
	}

	public Point getStartPoint() {
		return new Point(getInt(ParseMapPath.POINT_START_X), getInt(ParseMapPath.POINT_START_Y));
	}

	public void setEndPoint(Point endPoint) {
		put(ParseMapPath.POINT_END_X, endPoint.x);
		put(ParseMapPath.POINT_END_Y, endPoint.y);
	}

	public Point getEndPoint() {
		return new Point(getInt(ParseMapPath.POINT_END_X), getInt(ParseMapPath.POINT_END_Y));
	}

	public void setMapEditOverlay(GroundOverlay overlay) {
		this.groundOverlay = overlay;
	}

	public GroundOverlay getMapEditOverlay() {
		return groundOverlay;
	}
}