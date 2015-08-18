package me.gurinderhans.sfumaps;

import android.content.Context;
import android.content.Intent;
import android.graphics.Picture;
import android.graphics.Point;
import android.graphics.PointF;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.util.Pair;
import android.view.View;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.android.gms.maps.model.TileProvider;
import com.larvalabs.svgandroid.SVGBuilder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import me.gurinderhans.sfumaps.Factory.GridNode;
import me.gurinderhans.sfumaps.Factory.MapGrid;
import me.gurinderhans.sfumaps.Factory.PathFinder;
import me.gurinderhans.sfumaps.PathMaker.CustomMapFragment;
import me.gurinderhans.sfumaps.PathMaker.PathMaker;
import me.gurinderhans.sfumaps.wifirecorder.Controller.RecordWifiDataActivity;

import static com.google.android.gms.maps.GoogleMap.MAP_TYPE_NONE;
import static com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import static com.google.android.gms.maps.GoogleMap.OnMarkerDragListener;

public class MainActivity extends FragmentActivity implements OnMapClickListener {

	public static final String TAG = MainActivity.class.getSimpleName();

	private GoogleMap Map;

	MapGrid mapGrid;
	Point frm, to;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		// set activity theme to light text
		this.setTheme(R.style.MainActivity);

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// setting the status bar transparent
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
			getWindow().getDecorView().setSystemUiVisibility(
					View.SYSTEM_UI_FLAG_LAYOUT_STABLE
							| View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
			getWindow().setStatusBarColor(getResources().getColor(R.color.transparent_status_bar_color));

		// load app preferences
		AppConfig.loadPreferences(this);

		(findViewById(R.id.backend_panel)).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivity(new Intent(getApplicationContext(), RecordWifiDataActivity.class));
			}
		});

		createMapGrid();
		setUpMapIfNeeded();

	}

	public void createMapGrid() {

		mapGrid = new MapGrid(MainActivity.this, new PointF(121f, 100f), new PointF(192f, 183f));

        /*// test points
        frm = new Point(163, 229);
        to = new Point(254, 321);

        path_line = Map.addPolyline(new PolylineOptions().width(15).color(0xFF4285F4).geodesic(true).zIndex(10000));

        AStar();*/
	}

	Polyline path_line;

	public void AStar() {

		// AStar search
		long start = System.currentTimeMillis();
		List<GridNode> path = PathFinder.getPath(frm, to, mapGrid);
		Log.i(TAG, "path search took: " + (System.currentTimeMillis() - start) + "ms");

		Log.i(TAG, "path: " + path);
		if (path != null) {
			List<LatLng> pathPoints = new ArrayList<>();
			for (GridNode node : path) {
				pathPoints.add(MercatorProjection.fromPointToLatLng(node.projCoords));
			}
			path_line.setPoints(pathPoints);
		}
	}

	/**
	 * If (Map == null) then get the map fragment and initialize it.
	 */
	private void setUpMapIfNeeded() {
		// Do a null check to confirm that we have not already instantiated the map.
		if (Map == null) {
			CustomMapFragment customMapFragment = ((CustomMapFragment) getSupportFragmentManager()
					.findFragmentById(R.id.map));

			Map = customMapFragment.getMap();

			if (Map != null) {

				// set up path maker
				new PathMaker(customMapFragment, Map, mapGrid,
						findViewById(R.id.edit_map_path),
						findViewById(R.id.export_map_path),
						findViewById(R.id.create_box_rect),
						findViewById(R.id.delete_box_rect));

				// set up map UI
				setUpMap();
			}
		}
	}

	private void setUpMap() {

		// hide default overlay and set initial position
		Map.setMapType(MAP_TYPE_NONE);
		Map.setIndoorEnabled(false);
		Map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(0, 0), 2f));

		// hide the marker toolbar - the two buttons on the bottom right that go to google maps
		Map.getUiSettings().setMapToolbarEnabled(false);

		// just put the user navigation marker in the center as we don't yet know user's location
		LatLng mapCenter = new LatLng(0, 0);//MercatorProjection.fromPointToLatLng(new PointF(AppConfig.TILE_SIZE, AppConfig.TILE_SIZE));
		Map.addMarker(new MarkerOptions()
				.position(mapCenter)
				.title("Position")
				.snippet(MercatorProjection.fromLatLngToPoint(mapCenter).toString())
				.draggable(true));

		Map.setOnMarkerDragListener(new OnMarkerDragListener() {
			@Override
			public void onMarkerDragStart(Marker marker) {
			}

			@Override
			public void onMarkerDrag(Marker marker) {
			}

			@Override
			public void onMarkerDragEnd(Marker marker) {

				PointF pos = MercatorProjection.fromLatLngToPoint(marker.getPosition());

				marker.setSnippet(pos.toString());

				/*for (int x = 0; x < mapGrid.mapGridSizeX; x++) {
					for (int y = 0; y < mapGrid.mapGridSizeY; y++) {
						GridNode thisNode = mapGrid.getNode(x, y);
						if (MapTools.inRange(thisNode.projCoords, pos, 0.1f)) {
							Log.i(TAG, thisNode.toString());
							to = new Point(thisNode.gridX, thisNode.gridY);
							// redraw path
							AStar();
							break;
						}
					}
				}*/
			}
		});

		Map.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
			@Override
			public boolean onMarkerClick(Marker marker) {
//				marker.showInfoWindow();
				return true;
			}
		});

		Map.setOnMapClickListener(this);

		TileProvider basemapTileProvider = new SVGTileProvider(MapTools.getBaseMapTiles(this), getResources().getDisplayMetrics().densityDpi / 160f);
		Map.addTileOverlay(new TileOverlayOptions().tileProvider(basemapTileProvider).zIndex(10));

		// overlay tile provider to switch floor level stuff
		TileProvider overlayProvider = new SVGTileProvider(getOverlayTiles(this), getResources().getDisplayMetrics().densityDpi / 160f);
		Map.addTileOverlay(new TileOverlayOptions().tileProvider(overlayProvider).zIndex(11));

	}

	// FIXME: temp method
	public static ArrayList<Pair<String, Picture>> getOverlayTiles(Context c) {
		try {
			ArrayList<Pair<String, Picture>> tileFiles = new ArrayList<>();

			Picture currentFile = new SVGBuilder().readFromInputStream(
					c.getAssets().open("sfumap-overlay.svg")).build().getPicture();

			for (int i = 0; i < 25; i++)
				tileFiles.add(Pair.create(i + "", currentFile));

			return tileFiles;

		} catch (IOException e) {
			e.printStackTrace();
			Log.d(MainActivity.TAG, "Could not create Tile Provider. Unable to list map tile files directory");
		}

		return null;
	}

	@Override
	protected void onResume() {
		super.onResume();
		setUpMapIfNeeded();
	}

	@Override
	public void onPause() {
		super.onPause();
	}

	@Override
	public void onMapClick(LatLng latLng) {
		PointF clickedPoint = MercatorProjection.fromLatLngToPoint(latLng);
		// get markers at the area clicked
		for (int x = 0; x < mapGrid.mapGridSizeX; x++) {
			for (int y = 0; y < mapGrid.mapGridSizeY; y++) {
				GridNode thisNode = mapGrid.getNode(x, y);
				if (MapTools.inRange(thisNode.projCoords, clickedPoint, 0.5f)) {
					Map.addMarker(new MarkerOptions()
							.position(MercatorProjection.fromPointToLatLng(thisNode.projCoords))
							.icon(BitmapDescriptorFactory.fromResource(thisNode.isWalkable() ? R.drawable.map_path : R.drawable.no_path))
							.anchor(0.5f, 0.5f)
							.title("Pos: " + thisNode.projCoords.x + ", " + thisNode.projCoords.y));
				}
			}
		}
	}

}
