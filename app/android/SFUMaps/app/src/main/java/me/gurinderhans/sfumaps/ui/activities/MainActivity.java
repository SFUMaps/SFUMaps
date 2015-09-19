package me.gurinderhans.sfumaps.ui.activities;

import android.graphics.Color;
import android.graphics.PointF;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Pair;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnCameraChangeListener;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
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
import com.parse.ParseQuery;
import com.parse.SaveCallback;

import java.util.List;

import me.gurinderhans.sfumaps.BuildConfig;
import me.gurinderhans.sfumaps.R;
import me.gurinderhans.sfumaps.devtools.PathMaker;
import me.gurinderhans.sfumaps.devtools.placecreator.controllers.PlaceFormDialog;
import me.gurinderhans.sfumaps.factory.classes.MapPlace;
import me.gurinderhans.sfumaps.factory.classes.PathSearch;
import me.gurinderhans.sfumaps.ui.controllers.SlidingUpPanelController;
import me.gurinderhans.sfumaps.ui.slidingUpPanel.SlidingUpPanel;
import me.gurinderhans.sfumaps.ui.views.CustomMapFragment;
import me.gurinderhans.sfumaps.ui.views.MapPlaceSearchBoxView;
import me.gurinderhans.sfumaps.utils.CachedTileProvider;
import me.gurinderhans.sfumaps.utils.MapTools;
import me.gurinderhans.sfumaps.utils.MarkerCreator;
import me.gurinderhans.sfumaps.utils.MercatorProjection;
import me.gurinderhans.sfumaps.utils.SVGTileProvider;

import static com.google.android.gms.maps.GoogleMap.MAP_TYPE_NONE;
import static com.parse.ParseQuery.CachePolicy.CACHE_ELSE_NETWORK;
import static com.parse.ParseQuery.CachePolicy.NETWORK_ELSE_CACHE;
import static me.gurinderhans.sfumaps.app.Keys.ParseMapPlace.CLASS;
import static me.gurinderhans.sfumaps.app.Keys.ParseMapPlace.PARENT_PLACE;
import static me.gurinderhans.sfumaps.factory.classes.MapPlace.mAllMapPlaces;

public class MainActivity extends AppCompatActivity
		implements
		OnCameraChangeListener,
		OnMapClickListener,
		OnMapLongClickListener,
		OnMarkerClickListener,
		OnMarkerDragListener,
		OnClickListener {

	protected static final String TAG = MainActivity.class.getSimpleName();

	// UI
	private MapPlaceSearchBoxView mSearchView;
	private GoogleMap Map;
	private SlidingUpPanelController mPanelController;

	// Data
	private int mapCurrentZoom; // used for detecting when map zoom changes
	private DiskLruCache mTileCache;
	private Pair<MapPlace, MapPlace> mPlaceFromTo;

	// TEMP
	private boolean selectingSecondPlace = false;
	PathSearch pathSearch;
	private FloatingActionButton fab;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		this.setTheme(R.style.MainActivity);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		setupStatusBar();

		setupToolbar();

		fab = (FloatingActionButton) findViewById(R.id.search_init_button);
		fab.setOnClickListener(this);

		mPanelController = new SlidingUpPanelController(
				(SlidingUpPanel) findViewById(R.id.sliding_panel),
				fab
		);

		// cache for map tiles
		mTileCache = MapTools.openDiskCache(this);

		// additional setup
		setUpMapIfNeeded();
//		setupMapSearchBox();
		setupPlaces();

		// requires Map object
		pathSearch = new PathSearch(Map);


		//
		// MARK: DEV Controls
		//


		if (BuildConfig.DEBUG) {
			PathMaker.createPathMaker(this, Map);
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
		Map.setOnMapClickListener(this);
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
			syncMarkers();
		}
	}

	@Override
	public void onMapClick(LatLng latLng) {
		mPanelController.hidePanel();
	}

	@Override
	public void onMapLongClick(LatLng latLng) {
		if (BuildConfig.DEBUG && !PathMaker.isEditingMap) {

			MapPlace newPlace = new MapPlace();
			newPlace.setPosition(MercatorProjection.fromLatLngToPoint(latLng));
			newPlace.setMapGizmo(MarkerCreator.createPlaceMarker(getApplicationContext(), Map, newPlace));
			mAllMapPlaces.add(newPlace);

			// send to edit
			new PlaceFormDialog(this,
					getPlaceIndex(MercatorProjection.fromPointToLatLng(newPlace.getPosition())))
					.show();
		}
	}

	@Override
	public boolean onMarkerClick(Marker marker) {

		// find the clicked marker
		int clickedPlaceIndex = getPlaceIndex(marker.getPosition());
		if (clickedPlaceIndex != -1) {

			if (!BuildConfig.DEBUG) // edit place
				new PlaceFormDialog(this, clickedPlaceIndex).show();
			else {
				mPanelController.setPlace(mAllMapPlaces.get(clickedPlaceIndex));

				if (selectingSecondPlace) {

					mPlaceFromTo = Pair.create(mPlaceFromTo.first, mAllMapPlaces.get(clickedPlaceIndex));

					pathSearch.newSearch(mPlaceFromTo.first, mPlaceFromTo.second);

					mPanelController.setSecondPlace(mAllMapPlaces.get(clickedPlaceIndex));

				} else {
					mPlaceFromTo = Pair.create(mAllMapPlaces.get(clickedPlaceIndex), null);
				}
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
			mAllMapPlaces.get(draggedPlaceIndex).setPosition(
					MercatorProjection.fromLatLngToPoint(marker.getPosition())
			);

			mAllMapPlaces.get(draggedPlaceIndex).savePlaceWithCallback(new SaveCallback() {
				@Override
				public void done(ParseException e) {
					Snackbar.make(findViewById(android.R.id.content), "Place location updated", Snackbar.LENGTH_LONG).show();
				}
			});
		}
	}


	//
	// MARK: Custom helper methods
	//

	private void setupStatusBar() {
		// make the status bar transparent
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
			getWindow().getDecorView().setSystemUiVisibility(
					View.SYSTEM_UI_FLAG_LAYOUT_STABLE
							| View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
			getWindow().setStatusBarColor(Color.parseColor("#80000000"));
	}

	private void setupToolbar() {
		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

		toolbar.setTitleTextColor(Color.parseColor("#FFFFFFFF"));

		toolbar.setPadding(0, getStatusBarHeight(), 0, 0);

		// Show menu icon
//		final ActionBar ab = getSupportActionBar();
//		ab.setHomeAsUpIndicator(R.drawable.ic_close_white_48dp);
//		ab.setDisplayHomeAsUpEnabled(true);
	}

	// A method to find height of the status bar
	public int getStatusBarHeight() {
		int result = 0;
		int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
		if (resourceId > 0) {
			result = getResources().getDimensionPixelSize(resourceId);
		}
		return result;
	}

	public void setupPlaces() {
		// TODO: 15-09-17 Use local data-store as well
		ParseQuery<MapPlace> query = ParseQuery.getQuery(CLASS);
		query.include(PARENT_PLACE);
		query.setCachePolicy(BuildConfig.DEBUG ? NETWORK_ELSE_CACHE : CACHE_ELSE_NETWORK);
		query.findInBackground(new FindCallback<MapPlace>() {
			@Override
			public void done(List<MapPlace> objects, ParseException e) {

				if (e != null) // There was an error or the network wasn't available.
					return;

				for (MapPlace place : objects) {

					place.setMapGizmo(MarkerCreator.createPlaceMarker(
							getApplicationContext(), Map, place));

					mAllMapPlaces.add(place);
				}

				syncMarkers();
			}
		});
	}

	private void setupMapSearchBox() {
		// get adapter from PlaceFormDialog.class just so the same adapter is being used
//		mSearchView = (MapPlaceSearchBoxView) findViewById(R.id.main_search_view);
		ArrayAdapter<MapPlace> mSearchAutoCompleteAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
		mSearchView.setAdapter(mSearchAutoCompleteAdapter);
	}

	private int getPlaceIndex(LatLng placePos) {

		for (int i = 0; i < mAllMapPlaces.size(); i++) {
			// level the LatLng to same 'precision'
			PointF thisMarkerPoint = MercatorProjection.fromLatLngToPoint(
					mAllMapPlaces.get(i).getMapGizmo().getPosition());

			if (thisMarkerPoint.equals(MercatorProjection.fromLatLngToPoint(placePos)))
				return i;
		}

		return -1;
	}

	private void syncMarkers() {
		for (MapPlace el : mAllMapPlaces)
			for (int zoom : el.getZooms()) {
				if (zoom == mapCurrentZoom) {
					el.getMapGizmo().setVisible(true);
					break;
				} else {
					el.getMapGizmo().setVisible(false);
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

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.search_init_button:
				// change icon
				selectingSecondPlace = !selectingSecondPlace;
				fab.setBackgroundTintList(getResources().getColorStateList(!selectingSecondPlace ? R.color.app_color_primary : android.R.color.holo_green_dark));
				fab.setImageResource(!selectingSecondPlace ? R.drawable.ic_directions_white_48dp : R.drawable.ic_close_white_48dp);

				if (!selectingSecondPlace) {
					pathSearch.clearPaths();
					mPanelController.hidePanel();
				}
				break;
			default:
				break;
		}
	}
}
