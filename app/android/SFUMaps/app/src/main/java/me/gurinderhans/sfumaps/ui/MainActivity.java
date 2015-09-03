package me.gurinderhans.sfumaps.ui;

import android.graphics.PointF;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnCameraChangeListener;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.android.gms.maps.model.TileProvider;
import com.jakewharton.disklrucache.DiskLruCache;

import me.gurinderhans.sfumaps.R;
import me.gurinderhans.sfumaps.factory.classes.MapGrid;
import me.gurinderhans.sfumaps.utils.MapTools;

import static com.google.android.gms.maps.GoogleMap.MAP_TYPE_NONE;

public class MainActivity extends FragmentActivity implements OnCameraChangeListener {

	public static final String TAG = MainActivity.class.getSimpleName();

	private GoogleMap Map;
	private MapGrid mGrid;
	private DiskLruCache mTileCache;

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

		// setup Map
		setUpMapIfNeeded();

		// map grid
		mGrid = new MapGrid(this, new PointF(121f, 100f), new PointF(192f, 183f));

		// admin panel

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
	protected void onResume() {
		super.onResume();
		setUpMapIfNeeded();
	}

	@Override
	public void onCameraChange(CameraPosition cameraPosition) {

		// 1. limit map max zoom
		float maxZoom = 8f;
		if (cameraPosition.zoom > maxZoom)
			Map.animateCamera(CameraUpdateFactory.zoomTo(maxZoom));
	}
}
