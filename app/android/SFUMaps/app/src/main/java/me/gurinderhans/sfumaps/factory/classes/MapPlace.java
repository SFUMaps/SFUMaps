package me.gurinderhans.sfumaps.factory.classes;


import android.graphics.PointF;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

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

	public static final String TAG = MapPlace.class.getSimpleName();

	public static List<MapPlace> mAllMapPlaces = new ArrayList<>();

	/* Member variables */
	private Marker mPlaceMarker;

	public MapPlace() {
	}

	public MapPlace(String title) {
		/* constructor only to be used for AutocompleteTextView adding view tokens */
		put(ParseMapPlace.TITLE, title);
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
		getMapGizmo().setRotation(rotation);
	}

	public MapPlace getParentPlace() {
		return (MapPlace) get(ParseMapPlace.PARENT_PLACE);
	}

	public void setParentPlace(MapPlace parentPlace) {
		if (parentPlace == null) {
			remove(ParseMapPlace.PARENT_PLACE);
		} else {
			put(ParseMapPlace.PARENT_PLACE, parentPlace);
		}
	}

	// save methods
	public void savePlace() {
		saveInBackground();
	}

	public void savePlaceWithCallback(@NonNull SaveCallback saveCallback) {
		saveInBackground(saveCallback);
	}

	/**
	 * Get place title
	 *
	 * @return - place title
	 */
	@Override
	public String toString() {
		return getTitle();
	}


	/* MapPlace Class methods */
	public void setMapGizmo(Marker marker) {
		this.mPlaceMarker = marker;
	}

	public Marker getMapGizmo() {
		return mPlaceMarker;
	}


	// not too sure about this functions implementation, how would it deal with two places with same name if case be?
	@Nullable
	public static MapPlace findPlaceWithTitle(String title) {
		for (MapPlace place : mAllMapPlaces) {
			if (place.getTitle().equals(title)) {
				return place;
			}
		}

		return null;
	}
}

