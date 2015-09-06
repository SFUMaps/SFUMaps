package me.gurinderhans.sfumaps.factory.classes;


import android.graphics.PointF;
import android.support.annotation.NonNull;

import com.google.android.gms.maps.model.Marker;
import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.parse.SaveCallback;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

import me.gurinderhans.sfumaps.utils.MarkerCreator.MapLabelIconAlign;
import me.gurinderhans.sfumaps.utils.MarkerCreator.MapPlaceType;

import static me.gurinderhans.sfumaps.app.Keys.ParseMapPlace;

/**
 * Created by ghans on 15-09-05.
 */

@ParseClassName(ParseMapPlace.CLASS)
public class MapPlace extends ParseObject {

	protected static final String TAG = MapPlace.class.getSimpleName();

	/* Member variables */
	private Marker mMapPlaceMarker;


	public MapPlace() {
		/* empty constructor, not be used by anyone other than Parse */
	}

	/* Parse methods */

	public String getTitle() {
		return getString(ParseMapPlace.TITLE);
	}

	public void setTitle(String title) {
		put(ParseMapPlace.TITLE, title);
	}

	public MapPlaceType getType() {
		return MapPlaceType.fromString(getString(ParseMapPlace.TYPE));
	}

	public void setType(MapPlaceType type) {
		put(ParseMapPlace.TYPE, type.getText());
	}

	public MapLabelIconAlign getIconAlignment() {
		return MapLabelIconAlign.fromString(getString(ParseMapPlace.ICON_ALIGNMENT));
	}

	public void setIconAlignment(MapLabelIconAlign alignment) {
		put(ParseMapPlace.ICON_ALIGNMENT, alignment.getText());
	}

	public List<Integer> getZooms() {
		List<Integer> zooms = new ArrayList<>();

		JSONArray jsonArray = getJSONArray(ParseMapPlace.ZOOM);

		if (jsonArray == null)
			return zooms;

		for (int i = 0; i < jsonArray.length(); i++)
			try {
				zooms.add(jsonArray.getInt(i));
			} catch (JSONException e) {
				e.printStackTrace();
			}

		return zooms;
	}

	public void setZooms(List<Integer> zooms) {
		put(ParseMapPlace.ZOOM, zooms);
	}

	public PointF getPosition() {
		float x = (float) getDouble(ParseMapPlace.POSITION_X);
		float y = (float) getDouble(ParseMapPlace.POSITION_Y);

		return new PointF(x, y);
	}

	public void setPosition(PointF position) {
		put(ParseMapPlace.POSITION_X, position.x);
		put(ParseMapPlace.POSITION_Y, position.y);
	}

	public int getMarkerRotation() {
		return getInt(ParseMapPlace.MARKER_ROTATION);
	}

	public void setMarkerRotation(int rotation) {
		put(ParseMapPlace.MARKER_ROTATION, rotation);

		// rotate the marker
		getPlaceMarker().setRotation(rotation);
	}


	// save methods
	public void savePlace() {
		saveInBackground();
	}

	public void savePlaceWithCallback(@NonNull SaveCallback saveCallback) {
		saveInBackground(saveCallback);
	}


	/* MapPlace Class methods */
	public void tieWithMarker(Marker marker) {
		this.mMapPlaceMarker = marker;
	}

	public Marker getPlaceMarker() {
		return mMapPlaceMarker;
	}

}