package me.gurinderhans.sfumaps;

import android.content.Intent;
import android.graphics.Point;
import android.graphics.PointF;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.android.gms.maps.model.TileProvider;

import java.util.ArrayList;
import java.util.List;

import me.gurinderhans.sfumaps.Factory.GridNode;
import me.gurinderhans.sfumaps.Factory.MapGrid;
import me.gurinderhans.sfumaps.Factory.PathFinder;
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

        setUpMapIfNeeded();
        createMapGrid();


        // random locations
        /*MapTools.addTextAndIconMarker(this,
                Map,
                new PointF(107f, 150f),
                MapTools.createPureTextIcon(this, "Naheeno Park", null),
                0f,
                null,
                MapTools.MapLabelIconAlign.TOP);


        MapTools.addTextAndIconMarker(this,
                Map,
                new PointF(121.805f, 104.698f),
                MapTools.createPureTextIcon(this, "W.A.C Bennett Library", null),
                0f,
                null,
                MapTools.MapLabelIconAlign.RIGHT);

        MapTools.addTextAndIconMarker(this,
                Map,
                new PointF(121.625f, 112.704f),
                MapTools.createPureTextIcon(this, "Food Court", null),
                0f,
                null,
                MapTools.MapLabelIconAlign.LEFT);

        MapTools.addTextAndIconMarker(this,
                Map,
                new PointF(98.211f, 120.623f),
                MapTools.createPureTextIcon(this, "Terry Fox Field", null),
                0f,
                null,
                MapTools.MapLabelIconAlign.TOP);


        // add road markers
        MapTools.addTextMarker(this,
                Map,
                new PointF(90.98202f, 139.12495f),
                MapTools.createPureTextIcon(this, "Gaglardi Way", null),
                -28f);

        MapTools.addTextMarker(this,
                Map,
                new PointF(35.420155f, 110.39347f),
                MapTools.createPureTextIcon(this, "University Dr W", null),
                -38f);

        MapTools.addTextMarker(this,
                Map,
                new PointF(198.84691f, 108.44006f),
                MapTools.createPureTextIcon(this, "University High Street", null),
                0f);

        MapTools.addTextMarker(this,
                Map,
                new PointF(214.28412f, 81.674225f),
                MapTools.createPureTextIcon(this, "University Crescent", null),
                5f);*/

    }

    public void createMapGrid() {

        mapGrid = new MapGrid(new PointF(121f, 100f), new PointF(192f, 183f));

        // add walkable areas
        mapGrid.createWalkablePath(new GridNode(160, 228, mapGrid), new GridNode(170, 231, mapGrid));
        mapGrid.createWalkablePath(new GridNode(170, 232, mapGrid), new GridNode(170, 235, mapGrid));
        mapGrid.createWalkablePath(new GridNode(169, 236, mapGrid), new GridNode(170, 321, mapGrid));
        mapGrid.createWalkablePath(new GridNode(171, 319, mapGrid), new GridNode(255, 322, mapGrid));
        mapGrid.createWalkablePath(new GridNode(171, 228, mapGrid), new GridNode(255, 230, mapGrid));
        mapGrid.createWalkablePath(new GridNode(252, 231, mapGrid), new GridNode(255, 255, mapGrid));
        mapGrid.createWalkablePath(new GridNode(252, 255, mapGrid), new GridNode(253, 294, mapGrid));
        mapGrid.createWalkablePath(new GridNode(252, 295, mapGrid), new GridNode(255, 318, mapGrid));
        mapGrid.createWalkablePath(new GridNode(242, 323, mapGrid), new GridNode(270, 337, mapGrid));
        mapGrid.createWalkablePath(new GridNode(271, 330, mapGrid), new GridNode(347, 337, mapGrid));

        // test points
        frm = new Point(163, 229);
        to = new Point(254, 321);

        path_line = Map.addPolyline(new PolylineOptions().width(15).color(0xFF4285F4).geodesic(true).zIndex(10000));

        AStar();
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
                pathPoints.add(MercatorProjection.fromPointToLatLng(node.node_position));
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
            // Try to obtain the map from the SupportMapFragment.
            Map = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            // Check if we were successful in obtaining the map.
            if (Map != null)
                setUpMap();
        }
    }

    /**
     * <ul>
     * <li>Define map settings</li>
     * <li>Set custom map tiles</li>
     * <li>Get user's initial location here</li>
     * <li>Draw the recorded paths here</li>
     * </ul>
     */
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

                for (int x = 0; x < mapGrid.mapGridSizeX; x++) {
                    for (int y = 0; y < mapGrid.mapGridSizeY; y++) {
                        GridNode thisNode = mapGrid.getNode(x, y);
                        if (MapTools.inRange(thisNode.node_position, pos, 0.1f)) {
                            Log.i(TAG, thisNode.toString());
                            to = new Point(thisNode.gridX, thisNode.gridY);
                            // redraw path
                            AStar();
                            break;
                        }
                    }
                }
            }
        });

        Map.setOnMapClickListener(this);

        TileProvider provider = new SVGTileProvider(MapTools.getMapTiles(this), getResources().getDisplayMetrics().densityDpi / 160f);
        Map.addTileOverlay(new TileOverlayOptions().tileProvider(provider));

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
                if (MapTools.inRange(thisNode.node_position, clickedPoint, 0.5f)) {
                    Map.addMarker(new MarkerOptions()
                            .position(MercatorProjection.fromPointToLatLng(thisNode.node_position))
                            .icon(BitmapDescriptorFactory.fromResource(thisNode.isWalkable ? R.drawable.map_path : R.drawable.no_path))
                            .anchor(0.5f, 0.5f)
                            .title("Pos: " + thisNode.gridX + ", " + thisNode.gridY));
                }
            }
        }
    }

}
