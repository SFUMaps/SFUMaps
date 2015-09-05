package me.gurinderhans.sfumaps.ui;

import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.graphics.PointF;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Pair;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnCameraChangeListener;
import com.google.android.gms.maps.GoogleMap.OnMapLongClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerDragListener;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.android.gms.maps.model.TileProvider;
import com.jakewharton.disklrucache.DiskLruCache;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.SaveCallback;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

import me.gurinderhans.sfumaps.BuildConfig;
import me.gurinderhans.sfumaps.R;
import me.gurinderhans.sfumaps.app.Keys;
import me.gurinderhans.sfumaps.devtools.pathmaker.PathMaker;
import me.gurinderhans.sfumaps.devtools.placecreator.PlaceFormDialog;
import me.gurinderhans.sfumaps.factory.classes.MapGrid;
import me.gurinderhans.sfumaps.utils.MapTools;
import me.gurinderhans.sfumaps.utils.MarkerCreator;
import me.gurinderhans.sfumaps.utils.MercatorProjection;

import static com.google.android.gms.maps.GoogleMap.MAP_TYPE_NONE;

public class MainActivity extends FragmentActivity
		implements
		OnCameraChangeListener,
		OnMapLongClickListener,
		OnMarkerClickListener,
		OnDismissListener,
		OnMarkerDragListener {

	protected static final String TAG = MainActivity.class.getSimpleName();

	/* !! NOTE !!
	 * `BuildConfig.DEBUG` is our "dev mode". Since only the developer can generate app-debug.apk
	 * this should make it secure for someone who tries to mod the apk to turn this on or something.
	 */

	// member variables
	private GoogleMap Map;
	private MapGrid mGrid;
	private DiskLruCache mTileCache;
	private PlaceFormDialog mPlaceFormDialog;

	private List<Pair<ParseObject, Marker>> mAllMapPlaces = new ArrayList<>();

	FindCallback<ParseObject> onZoomChangedCallback = new FindCallback<ParseObject>() {
		@Override
		public void done(List<ParseObject> places, ParseException e) {

			for (ParseObject newPlace : places) {
				int placeIndex = getPlaceIndex(newPlace);

				if (placeIndex == -1) { // place is new!
					mAllMapPlaces.add(Pair.create(newPlace,
							MarkerCreator.addTextAndIconMarker(
									getApplicationContext(),
									Map,
									MarkerCreator.MapLabelIconAlign.TOP,
									newPlace
							)));
				}

				if (BuildConfig.DEBUG && placeIndex >= 0) { // update place
					Marker placeMarker = mAllMapPlaces.get(placeIndex).second;
					placeMarker.remove();
					placeMarker = MarkerCreator.addTextAndIconMarker(
							getApplicationContext(),
							Map,
							MarkerCreator.MapLabelIconAlign.TOP,
							newPlace
					);
					mAllMapPlaces.set(placeIndex, Pair.create(newPlace, placeMarker));
				}
			}

			syncMarkers();
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		// set activity theme to light text
		this.setTheme(R.style.MainActivity);

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// make the status bar transparent
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
			getWindow().getDecorView().setSystemUiVisibility(
					View.SYSTEM_UI_FLAG_LAYOUT_STABLE
							| View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
			getWindow().setStatusBarColor(getResources().getColor(R.color.transparent_status_bar_color));

		// cache for map tiles
		mTileCache = MapTools.openDiskCache(this);

		// map grid
		mGrid = new MapGrid(this, new PointF(121f, 100f), new PointF(192f, 183f));

		// setup Map
		setUpMapIfNeeded();

		// show dev controls if app is in dev mode
		if (BuildConfig.DEBUG)
			findViewById(R.id.main_dev_layout).setVisibility(View.VISIBLE);
	}

	private void setUpMapIfNeeded() {
		// Do a null check to confirm that we have not already instantiated the map.
		if (Map == null) {
			CustomMapFragment fragment = (CustomMapFragment) getSupportFragmentManager()
					.findFragmentById(R.id.map);

			Map = fragment.getMap();

			// create admin panel
			if (BuildConfig.DEBUG)
				PathMaker.initPathMaker(Map, mGrid, fragment,
						findViewById(R.id.edit_map_grid_controls));

			// set up map UI
			if (Map != null)
				setUpMap();
		}
	}

	private void setUpMap() {

		// map options
		Map.setMapType(MAP_TYPE_NONE);
		Map.setIndoorEnabled(false);
		// hide the marker toolbar - the two buttons on the bottom right that go to google maps
		Map.getUiSettings().setMapToolbarEnabled(false);

		Map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(0, 0), 2f));

		Map.setOnCameraChangeListener(this);
		Map.setOnMapLongClickListener(this);
		Map.setOnMarkerClickListener(this);
		Map.setOnMarkerDragListener(this);

		// base map overlay
		Map.addTileOverlay(new TileOverlayOptions()
				.tileProvider(
						getTileProvider(1, new SVGTileProvider(MapTools.getBaseMapTiles(this),
								getResources().getDisplayMetrics().densityDpi / 160f)))
				.zIndex(10));

		// overlay tile provider to switch floor level stuff
		Map.addTileOverlay(new TileOverlayOptions()
				.tileProvider(
						getTileProvider(2, new SVGTileProvider(MapTools.getOverlayTiles(this),
								getResources().getDisplayMetrics().densityDpi / 160f)))
				.zIndex(11));
	}


	@Override
	protected void onResume() {
		super.onResume();
		setUpMapIfNeeded();
	}


	private int mMapCurrentZoom;


	@Override
	public void onCameraChange(CameraPosition cameraPosition) {

		// Temp: set map zoom on textview
		((TextView) findViewById(R.id.map_current_zoom)).setText(cameraPosition.zoom + "");

		// 1. limit map max zoom
		float maxZoom = 8f;
		if (cameraPosition.zoom > maxZoom)
			Map.animateCamera(CameraUpdateFactory.zoomTo(maxZoom));

		// 2. load this zoom markers
		if (mMapCurrentZoom != (int) cameraPosition.zoom) { // on zoom change
			mMapCurrentZoom = (int) cameraPosition.zoom;
			MapTools.getZoomMarkers(mMapCurrentZoom, onZoomChangedCallback);
			syncMarkers();
		}
	}


	@Override
	public void onMapLongClick(LatLng latLng) {
		if (BuildConfig.DEBUG) {
			// show dialog asking place info
			mPlaceFormDialog = new PlaceFormDialog(this, Map, MercatorProjection.fromLatLngToPoint(latLng), null);
			mPlaceFormDialog.setOnDismissListener(this);
			mPlaceFormDialog.show();
		}
	}

	@Override
	public boolean onMarkerClick(Marker marker) {

		Pair<ParseObject, Marker> clickedPlace = null;

		// find this marker in list
		for (Pair<ParseObject, Marker> el : mAllMapPlaces)
			if (el.second.getPosition().equals(marker.getPosition())) {
				clickedPlace = el;
				break;
			}

		if (clickedPlace != null) {

			if (BuildConfig.DEBUG) {
				mPlaceFormDialog = new PlaceFormDialog(MainActivity.this, Map, MercatorProjection.fromLatLngToPoint(marker.getPosition()), clickedPlace);
				mPlaceFormDialog.setOnDismissListener(this);
				mPlaceFormDialog.show();
			}
		}

		return true;
	}

	/**
	 * Helper method to choose tile provider
	 *
	 * @param layer           - layer number for overlay tile provider to keep cache tiles for each overlay separate
	 * @param svgTileProvider - an instance of SVGTileProvider.class
	 * @return - IF cache supported, CachedTileProvider object ELSE the given SVGTileProvider object
	 */
	public TileProvider getTileProvider(int layer, SVGTileProvider svgTileProvider) {
		return mTileCache == null
				? svgTileProvider
				: new CachedTileProvider(Integer.toString(layer), svgTileProvider, mTileCache);
	}

	@Override
	public void onDismiss(DialogInterface dialog) {
		// refresh markers
		MapTools.getZoomMarkers(mMapCurrentZoom, onZoomChangedCallback);
	}


	/* marker drag */
	@Override
	public void onMarkerDragStart(Marker marker) {
	}

	@Override
	public void onMarkerDrag(Marker marker) {
	}

	@Override
	public void onMarkerDragEnd(Marker marker) {
		/* find the marker that was drag and update its location */
		Pair<ParseObject, Marker> foundPair = null;

		// find this marker in list
		for (Pair<ParseObject, Marker> el : mAllMapPlaces) {
			if (el.second.getPosition().equals(marker.getPosition())) {
				foundPair = el;
				break;
			}
		}

		if (foundPair != null) { // marker found
			PointF location = MercatorProjection.fromLatLngToPoint(marker.getPosition());
			foundPair.first.put(Keys.KEY_PLACE_POSITION_X, location.x);
			foundPair.first.put(Keys.KEY_PLACE_POSITION_Y, location.y);
			foundPair.first.saveInBackground(new SaveCallback() {
				@Override
				public void done(ParseException e) {
					Toast.makeText(getApplicationContext(), "Location Updated!", Toast.LENGTH_SHORT).show();
				}
			});
		}
	}

	/**
	 * Checks if the place that we just downloaded is already stored locally in mAllMapPlaces
	 *
	 * @param place - downloaded place
	 * @return - true if its already downloaded
	 */
	int getPlaceIndex(ParseObject place) {
		PointF placePos = new PointF(
				(float) place.getDouble(Keys.KEY_PLACE_POSITION_X),
				(float) place.getDouble(Keys.KEY_PLACE_POSITION_Y)
		);

		for (int i = 0; i < mAllMapPlaces.size(); i++) {
			// compare by location
			PointF thisPlacePos = new PointF(
					(float) mAllMapPlaces.get(i).first.getDouble(Keys.KEY_PLACE_POSITION_X),
					(float) mAllMapPlaces.get(i).first.getDouble(Keys.KEY_PLACE_POSITION_Y)
			);

			if (placePos.equals(thisPlacePos)) {
				return i;
			}
		}

		return -1;
	}

	private void syncMarkers() {
		for (Pair<ParseObject, Marker> el : mAllMapPlaces) {
			JSONArray placeZooms = el.first.getJSONArray(Keys.KEY_PLACE_ZOOM);

			for (int i = 0; i < placeZooms.length(); i++) {
				try {
					if (placeZooms.getInt(i) == mMapCurrentZoom) {
						el.second.setVisible(true);
						break;
					} else {
						el.second.setVisible(false);
					}
				} catch (JSONException ex) {
					ex.printStackTrace();
				}
			}
		}
	}
}
