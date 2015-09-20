package me.gurinderhans.sfumaps.ui.activities;

import android.content.Context;
import android.graphics.PointF;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AccelerateInterpolator;
import android.view.inputmethod.InputMethodManager;
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
import com.tokenautocomplete.TokenCompleteTextView.TokenListener;

import java.util.List;

import me.gurinderhans.sfumaps.BuildConfig;
import me.gurinderhans.sfumaps.R;
import me.gurinderhans.sfumaps.devtools.PathMaker;
import me.gurinderhans.sfumaps.devtools.placecreator.controllers.PlaceFormDialog;
import me.gurinderhans.sfumaps.factory.classes.MapPlace;
import me.gurinderhans.sfumaps.factory.classes.PathSearch;
import me.gurinderhans.sfumaps.factory.libs.sliding_up_panel.SlidingUpPanel;
import me.gurinderhans.sfumaps.ui.controllers.SlidingUpPanelController;
import me.gurinderhans.sfumaps.ui.views.CustomMapFragment;
import me.gurinderhans.sfumaps.ui.views.MapPlaceSearchCompletionView;
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
import static me.gurinderhans.sfumaps.utils.MercatorProjection.fromPointToLatLng;

public class MainActivity extends AppCompatActivity
		implements
		OnCameraChangeListener,
		OnMapClickListener,
		OnMapLongClickListener,
		OnMarkerClickListener,
		OnMarkerDragListener,
		OnClickListener,
		TokenListener {

	protected static final String TAG = MainActivity.class.getSimpleName();

	// UI
	private GoogleMap Map;
	private SlidingUpPanelController mPanelController;
	private Toolbar mSearchToolbar;
	private FloatingActionButton mFloatingActionButton;
	private MapPlaceSearchCompletionView mPlaceSearch, mPlaceFromSearchBox, mPlaceToSearchBox;

	// Data
	private int mapCurrentZoom; // used for detecting when map zoom changes
	private DiskLruCache mTileCache;
	private ArrayAdapter<MapPlace> placeSearchAdapter;
	private PathSearch mPathSearch;
	private MapPlace mSelectedPlace;


	@Override
	protected void onCreate(Bundle savedInstanceState) {

		this.setTheme(R.style.MainActivity);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// setup from top down
		setupStatusBar();
		setUpSearchAndToolbar();
		setUpMap();
		fetchPlaces();
		setupFABAndSlidingPanel();
		manageDevMode();
	}


	/**
	 * Makes the status bar transparent and allows views to be drawn behind the status bar
	 */
	private void setupStatusBar() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
			getWindow().getDecorView().setSystemUiVisibility(
					View.SYSTEM_UI_FLAG_LAYOUT_STABLE
							| View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
			getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.transparent_status_bar_color));
	}


	/**
	 * Initializes map search box and toolbar search
	 */
	private void setUpSearchAndToolbar() {
		mSearchToolbar = (Toolbar) findViewById(R.id.search_toolbar);
		setSupportActionBar(mSearchToolbar);

		int statusBarHeight = getStatusBarHeight();
		int toolbarHeight = getResources().getDimensionPixelSize(R.dimen.activity_main_toolbar_height);
		int extraPadding = getResources().getDimensionPixelOffset(R.dimen.activity_main_toolbar_bottom_padding);

		mSearchToolbar.setPadding(0, statusBarHeight + extraPadding, 0, extraPadding);
		mSearchToolbar.setTranslationY(-(toolbarHeight + statusBarHeight + extraPadding));

		// add the search layout
		View view = LayoutInflater.from(this).inflate(R.layout.activity_main_toolbar_search, mSearchToolbar, false);
		mSearchToolbar.addView(view);

		// customize action bar
		final ActionBar ab = getSupportActionBar();
		if (ab != null) {
			ab.setDisplayShowTitleEnabled(false);
			ab.setDisplayHomeAsUpEnabled(true);
		}

		/* setup search */

		mPlaceSearch = (MapPlaceSearchCompletionView) findViewById(R.id.main_search_view);
		mPlaceSearch.setLayoutId(R.layout.activity_main_placesearch_token_layout);

		mPlaceFromSearchBox = (MapPlaceSearchCompletionView) mSearchToolbar.findViewById(R.id.place_from);
		mPlaceToSearchBox = (MapPlaceSearchCompletionView) mSearchToolbar.findViewById(R.id.place_to);

		placeSearchAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line);

		mPlaceSearch.setAdapter(placeSearchAdapter);
		mPlaceFromSearchBox.setAdapter(placeSearchAdapter);
		mPlaceToSearchBox.setAdapter(placeSearchAdapter);

		// add token listeners
		mPlaceSearch.setTokenListener(this);
		mPlaceFromSearchBox.setTokenListener(this);
		mPlaceToSearchBox.setTokenListener(this);

	}


	/**
	 * Retrieves the Google Maps fragment and loads the custom settings such as custom Map tiles
	 * plus registers different listener events on the Map
	 */
	private void setUpMap() {

		// cache for map tiles
		mTileCache = MapTools.openDiskCache(this);

		Map = ((CustomMapFragment) getSupportFragmentManager()
				.findFragmentById(R.id.map)).getMap();

		// map options
		Map.setMapType(MAP_TYPE_NONE);
		Map.setIndoorEnabled(false);
		// hide the marker mSearchToolbar - the two buttons on the bottom right that go to google maps
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


		mPathSearch = new PathSearch(Map);
	}


	/**
	 * Makes an asynchronous call to the Parse servers and loads all the @link{MapPlace} objects
	 * into search adapter @link{placeSearchAdapter}
	 */
	public void fetchPlaces() {
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
					Marker marker = MarkerCreator.createPlaceMarker(
							getApplicationContext(), Map, place);
					marker.setVisible(false);

					place.setMapGizmo(marker);

					mAllMapPlaces.add(place);
				}

				placeSearchAdapter.addAll(objects);

				syncMarkers();
			}
		});
	}


	/**
	 * Sets up the @link{SlidingUpPanel} and @link{FloatingActionButton} to be used for the app
	 */
	private void setupFABAndSlidingPanel() {
		mFloatingActionButton = (FloatingActionButton) findViewById(R.id.get_directions_fab);
		mFloatingActionButton.setOnClickListener(this);

		mPanelController = new SlidingUpPanelController(
				(SlidingUpPanel) findViewById(R.id.sliding_panel),
				mFloatingActionButton
		);
	}


	/**
	 * Checks if @link{BuildConfig.DEBUG} is true, if yes, enables the custom dev options
	 */
	private void manageDevMode() {
		if (BuildConfig.DEBUG) {
			PathMaker.createPathMaker(this, Map);
		}
	}

	/**
	 * Helper method to hide the keyboard
	 */
	private void hideKeyboard() {
		// Check if no view has focus:
		View view = this.getCurrentFocus();
		if (view != null) {
			InputMethodManager inputManager = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
			inputManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
		}
	}


	/**
	 * Helper method to search through the local places list and find the place mathcing given
	 * input position
	 *
	 * @param placePos - position of the place to find
	 * @return - index of place in List @link{mAllMapPlaces}
	 */
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


	/**
	 * Called on zoom change, syncs the map markers to match current zoom level
	 */
	private void syncMarkers() {
		for (MapPlace el : mAllMapPlaces) {
			boolean containsZoom = false;
			for (int zoom : el.getZooms()) {
				if (zoom == mapCurrentZoom) {
					containsZoom = true;
					break;
				} else {
					containsZoom = false;
				}
			}
			// set visibility
			el.getMapGizmo().setVisible(containsZoom);
		}
	}


	/**
	 * Helper method to find height of the status bar
	 *
	 * @return - height of the status bar
	 */
	public int getStatusBarHeight() {
		int result = 0;
		int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
		if (resourceId > 0) {
			result = getResources().getDimensionPixelSize(resourceId);
		}
		return result;
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
			case R.id.get_directions_fab:
				// show search toolbar asking for place {FROM} and {TO}
				mSearchToolbar.animate()
						.translationY(0)
						.setInterpolator(new AccelerateInterpolator())
						.setDuration(150l)
						.start();

				mFloatingActionButton.hide();

				if (mPlaceSearch.getObjects().size() == 1) {
					mPlaceFromSearchBox.addObject(mPlaceSearch.getObjects().get(0));
				}
				if (mSelectedPlace != null) {
					mPlaceFromSearchBox.addObject(mSelectedPlace);
				}

				mPanelController.hidePanel();

				break;
			default:
				break;
		}
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
		mSelectedPlace = null;
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
					getPlaceIndex(fromPointToLatLng(newPlace.getPosition())))
					.show();
		}
	}

	@Override
	public boolean onMarkerClick(Marker marker) {

		// find the clicked marker
		int clickedPlaceIndex = getPlaceIndex(marker.getPosition());
		if (clickedPlaceIndex != -1) {
			if (BuildConfig.DEBUG) { // edit place
				new PlaceFormDialog(this, clickedPlaceIndex).show();
			} else {
				mSelectedPlace = mAllMapPlaces.get(clickedPlaceIndex);

				mPanelController.setPlace(mSelectedPlace);

				// set on place from
				mPlaceFromSearchBox.addObject(mSelectedPlace);

				// hide search toolbar
				int statusBarHeight = getStatusBarHeight();
				int toolbarHeight = getResources().getDimensionPixelSize(R.dimen.activity_main_toolbar_height);
				int extraPadding = getResources().getDimensionPixelOffset(R.dimen.activity_main_toolbar_bottom_padding);

				// show search toolbar asking for place {FROM} and {TO}
				mSearchToolbar.animate()
						.translationY(-(toolbarHeight + statusBarHeight + extraPadding))
						.setInterpolator(new AccelerateInterpolator())
						.setDuration(150l)
						.start();

				mFloatingActionButton.show();
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

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:

				int statusBarHeight = getStatusBarHeight();
				int toolbarHeight = getResources().getDimensionPixelSize(R.dimen.activity_main_toolbar_height);
				int extraPadding = getResources().getDimensionPixelOffset(R.dimen.activity_main_toolbar_bottom_padding);

				// show search toolbar asking for place {FROM} and {TO}
				mSearchToolbar.animate()
						.translationY(-(toolbarHeight + statusBarHeight + extraPadding))
						.setInterpolator(new AccelerateInterpolator())
						.setDuration(150l)
						.start();

				mFloatingActionButton.show();

				mPathSearch.clearPaths();

				mPlaceFromSearchBox.clear();
				mPlaceToSearchBox.clear();

				mSelectedPlace = null;

				break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onTokenAdded(Object o) {
		if (mPlaceFromSearchBox.getObjects().size() == 1 && mPlaceToSearchBox.getObjects().size() == 1) {
			hideKeyboard();
			mPathSearch.newSearch(
					mPlaceFromSearchBox.getObjects().get(0),
					mPlaceToSearchBox.getObjects().get(0)
			);
		}

		if (mPlaceSearch.getObjects().size() == 1) {
			hideKeyboard();
			Map.animateCamera(CameraUpdateFactory.newLatLngZoom(fromPointToLatLng(mPlaceSearch.getObjects().get(0).getPosition()), mPlaceSearch.getObjects().get(0).getZooms().get(0)));
		}
	}

	@Override
	public void onTokenRemoved(Object o) {
	}

}
