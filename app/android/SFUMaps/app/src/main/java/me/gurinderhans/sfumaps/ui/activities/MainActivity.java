package me.gurinderhans.sfumaps.ui.activities;

import android.graphics.PointF;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.TextView;

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

import java.util.List;

import me.gurinderhans.sfumaps.BuildConfig;
import me.gurinderhans.sfumaps.R;
import me.gurinderhans.sfumaps.devtools.PathMaker;
import me.gurinderhans.sfumaps.devtools.placecreator.controllers.PlaceFormDialog;
import me.gurinderhans.sfumaps.factory.classes.MapGrid;
import me.gurinderhans.sfumaps.factory.classes.MapPlace;
import me.gurinderhans.sfumaps.factory.classes.PathSearch;
import me.gurinderhans.sfumaps.ui.views.CustomMapFragment;
import me.gurinderhans.sfumaps.utils.CachedTileProvider;
import me.gurinderhans.sfumaps.utils.MapTools;
import me.gurinderhans.sfumaps.utils.MarkerCreator;
import me.gurinderhans.sfumaps.utils.MercatorProjection;
import me.gurinderhans.sfumaps.utils.SVGTileProvider;

import static com.google.android.gms.maps.GoogleMap.MAP_TYPE_NONE;

public class MainActivity extends FragmentActivity
		implements
		OnCameraChangeListener,
		OnMapLongClickListener,
		OnMarkerClickListener,
		OnMarkerDragListener {

	protected static final String TAG = MainActivity.class.getSimpleName();

	/* !! NOTE !!
	 * `BuildConfig.DEBUG` is our "dev mode". Since only the developer can generate app-debug.apk
	 * this should make it secure for someone who tries to mod the apk to turn this on or something.
	 */

	// member variables
	private int mapCurrentZoom; // used for detecting when map zoom changes
	private GoogleMap Map;
	private DiskLruCache mTileCache;
	private PlaceFormDialog mPlaceFormDialog;
	private PathSearch mPathSearch;
	private Pair<MapPlace, MapPlace> mPlaceFromTo;

	private FindCallback<ParseObject> onZoomChangedCallback = new FindCallback<ParseObject>() {
		@Override
		public void done(List<ParseObject> results, ParseException e) {

			if (e != null) {
				// There was an error or the network wasn't available.
				return;
			}

			for (ParseObject result : results) {

				MapPlace place = (MapPlace) result;

				int placeIndex = getPlaceIndex(
						MercatorProjection.fromPointToLatLng(place.getPosition())
				);

				if (placeIndex == -1) { // place is new!

					place.tieWithMarker(
							MarkerCreator.createPlaceMarker(getApplicationContext(), Map, place)
					);

					MapPlace.mAllMapPlaces.add(place);
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

		MapGrid mapGrid = new MapGrid(this, new PointF(121f, 100f), new PointF(192f, 183f));

		setUpMapIfNeeded();

		mPathSearch = new PathSearch(Map, mapGrid);



		/* Dev Controls */

		// show dev controls if app is in dev mode
		if (BuildConfig.DEBUG) {
			// create admin panel
			PathMaker.createPathMaker(this, Map, mapGrid);
		}
	}

	private void setUpMapIfNeeded() {
		// Do a null check to confirm that we have not already instantiated the map.
		if (Map == null) {

			Map = ((CustomMapFragment) getSupportFragmentManager()
					.findFragmentById(R.id.map)).getMap();

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

	@Override
	public void onCameraChange(CameraPosition cameraPosition) {

		// Temp: set map zoom on textview
		((TextView) findViewById(R.id.map_current_zoom)).setText(cameraPosition.zoom + "");

		// 1. limit map max zoom
		float maxZoom = 8f;
		if (cameraPosition.zoom > maxZoom)
			Map.animateCamera(CameraUpdateFactory.zoomTo(maxZoom));

		// 2. load this zoom markers
		if (mapCurrentZoom != (int) cameraPosition.zoom) { // on zoom change
			mapCurrentZoom = (int) cameraPosition.zoom;
			MapTools.getZoomMarkers(mapCurrentZoom, onZoomChangedCallback);
			syncMarkers();
		}
	}


	@Override
	public void onMapLongClick(LatLng latLng) {
		if (BuildConfig.DEBUG && !PathMaker.isEditingMap) {

			// create new place
			MapPlace newPlace = new MapPlace();
			newPlace.setPosition(MercatorProjection.fromLatLngToPoint(latLng));
			newPlace.tieWithMarker(MarkerCreator.createPlaceMarker(getApplicationContext(), Map, newPlace));
			MapPlace.mAllMapPlaces.add(newPlace);

			// show dialog asking place info
			mPlaceFormDialog = new PlaceFormDialog(
					this,
					getPlaceIndex(
							MercatorProjection.fromPointToLatLng(newPlace.getPosition())
					)
			);
			mPlaceFormDialog.show();
		}
	}

	@Override
	public boolean onMarkerClick(Marker marker) {

		// find the clicked marker
		int clickedPlaceIndex = getPlaceIndex(marker.getPosition());
		if (clickedPlaceIndex != -1) {

			if (BuildConfig.DEBUG) {
				// do path search
				if (mPlaceFromTo == null) {
					mPlaceFromTo = Pair.create(MapPlace.mAllMapPlaces.get(clickedPlaceIndex), null);
				} else {
					mPlaceFromTo = Pair.create(mPlaceFromTo.first, MapPlace.mAllMapPlaces.get(clickedPlaceIndex));
					mPathSearch.drawPath(mPlaceFromTo.first, mPlaceFromTo.second);
				}
				try {
					Log.i(TAG, "first: " + mPlaceFromTo.first + ", " + mPlaceFromTo.second);
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else {
				mPlaceFormDialog = new PlaceFormDialog(
						this,
						clickedPlaceIndex
				);
				mPlaceFormDialog.show();
			}
		}

		return true;
	}

	@Override
	public void onMarkerDragStart(Marker marker) {
	}

	@Override
	public void onMarkerDrag(Marker marker) {
	}

	@Override
	public void onMarkerDragEnd(Marker marker) {

		// find the clicked marker
		int draggedPlaceIndex = getPlaceIndex(marker.getPosition());
		if (draggedPlaceIndex != -1) {
			MapPlace.mAllMapPlaces.get(draggedPlaceIndex).setPosition(
					MercatorProjection.fromLatLngToPoint(marker.getPosition())
			);

			MapPlace.mAllMapPlaces.get(draggedPlaceIndex).savePlaceWithCallback(new SaveCallback() {
				@Override
				public void done(ParseException e) {
					Snackbar.make(findViewById(android.R.id.content), "Place location updated", Snackbar.LENGTH_LONG).show();
				}
			});
		}
	}


	/* Custom helper methods */

	private int getPlaceIndex(LatLng placePos) {

		for (int i = 0; i < MapPlace.mAllMapPlaces.size(); i++) {
			// level the LatLng to same 'precision'
			PointF thisMarkerPoint = MercatorProjection.fromLatLngToPoint(
					MapPlace.mAllMapPlaces.get(i).getPlaceMarker().getPosition());

			if (thisMarkerPoint.equals(MercatorProjection.fromLatLngToPoint(placePos)))
				return i;
		}

		return -1;
	}

	private void syncMarkers() {
		for (MapPlace el : MapPlace.mAllMapPlaces)
			for (int zoom : el.getZooms()) {
				if (zoom == mapCurrentZoom) {
					el.getPlaceMarker().setVisible(true);
					break;
				} else {
					el.getPlaceMarker().setVisible(false);
				}
			}
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
}
