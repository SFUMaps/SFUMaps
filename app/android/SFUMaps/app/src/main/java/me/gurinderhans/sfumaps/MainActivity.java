package me.gurinderhans.sfumaps;

import android.content.Intent;
import android.graphics.PointF;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnCameraChangeListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
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
import me.gurinderhans.sfumaps.wifirecorder.Controller.RecordWifiDataActivity;

public class MainActivity extends FragmentActivity implements OnCameraChangeListener, GoogleMap.OnMapClickListener {

    public static final String TAG = MainActivity.class.getSimpleName();

    private GoogleMap Map;

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

        //
        // grid setup
        //


        // create grid
        mapGrid = new MapGrid(new PointF(121f, 100f), new PointF(192f, 183f));
        frm = new GridNode(163, 229, mapGrid);
        to = new GridNode(254, 321, mapGrid);

        // walkable areas
        mapGrid.createWalkablePath(new GridNode(160, 228, mapGrid), new GridNode(170, 231, mapGrid));
        mapGrid.createWalkablePath(new GridNode(170, 232, mapGrid), new GridNode(170, 235, mapGrid));
        mapGrid.createWalkablePath(new GridNode(169, 236, mapGrid), new GridNode(170, 321, mapGrid));
        mapGrid.createWalkablePath(new GridNode(171, 319, mapGrid), new GridNode(255, 322, mapGrid));
        mapGrid.createWalkablePath(new GridNode(171, 228, mapGrid), new GridNode(255, 231, mapGrid));
        mapGrid.createWalkablePath(new GridNode(252, 232, mapGrid), new GridNode(255, 255, mapGrid));
        mapGrid.createWalkablePath(new GridNode(252, 255, mapGrid), new GridNode(253, 294, mapGrid));
        mapGrid.createWalkablePath(new GridNode(252, 295, mapGrid), new GridNode(255, 318, mapGrid));

        AStar();
    }

    MapGrid mapGrid;
    GridNode frm, to;

    public void AStar() {

        //
        // search
        //

        ArrayList<GridNode> open_list = new ArrayList<>();
        ArrayList<GridNode> closed_list = new ArrayList<>();

        // add initial node (start node)
        open_list.add(frm.computeCost(frm, to));

        GridNode endNode = new GridNode(-1, -1, mapGrid);

        while (open_list.size() != 0) {
            Log.i(TAG, "open list size: " + open_list.size());

            int min_fcost_node_index = GridNode.getMinFcostNodeIndex(open_list);

            GridNode current_node = open_list.get(min_fcost_node_index);

            if (current_node.x == to.x && current_node.y == to.y) {
                // path found
                endNode = current_node;
                break;
            }


            // add to closed list and remove from open
            closed_list.add(current_node);
            open_list.remove(min_fcost_node_index);

            for (GridNode n : mapGrid.getNeighbours(current_node)) {

                if (GridNode.searchNode(n, closed_list) != -1)
                    continue;

//                Log.i(TAG, "isWalkable: " + n.isWalkable + " x: " + n.x + ", " + n.y);
                float tenative_g_score = current_node.gcost + GridNode.dist(current_node, n);

                // TODO: some adjustments required here
                if (GridNode.searchNode(n, open_list) == -1 || tenative_g_score < n.gcost) {
                    n.parentNode = current_node;

                    if (GridNode.searchNode(n, open_list) == -1)
                        open_list.add(n.computeCost(frm, to));

                    int nbr_index = GridNode.searchNode(n, open_list);
                    GridNode tmp = open_list.get(nbr_index);
                    tmp.gcost = tenative_g_score;
                    tmp.fcost = tenative_g_score + GridNode.dist(n, to);
                    open_list.set(nbr_index, tmp);
                }

            }

        }


        List<LatLng> points = new ArrayList<>();
        while (endNode.parentNode != null) {
            endNode = endNode.parentNode;
            // need to switch x and y here for the indicies, as real life x, y are inverse of matrix x,y
            GridNode mapNode = new GridNode(endNode.x, endNode.y, mapGrid);
            points.add(MercatorProjection.fromPointToLatLng(mapNode.node_position));
        }

        if (path_line == null)
            path_line = Map.addPolyline(path_line_data);

        path_line.setPoints(points);
        path_line.setZIndex(1000); // Or some large number :)

    }

    Polyline path_line;
    PolylineOptions path_line_data = new PolylineOptions().width(15).color(0xFF4285F4).geodesic(true);

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
        Map.setMapType(GoogleMap.MAP_TYPE_NONE);
        Map.setIndoorEnabled(false);
        Map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(0, 0), 2f));

        // set max zoom for map
        Map.setOnCameraChangeListener(this);

        // hide the marker toolbar - the two buttons on the bottom right that go to google maps
        Map.getUiSettings().setMapToolbarEnabled(false);

        // just put the user navigation marker in the center as we don't yet know user's location
        LatLng mapCenter = new LatLng(0, 0);//MercatorProjection.fromPointToLatLng(new PointF(AppConfig.TILE_SIZE, AppConfig.TILE_SIZE));
        Map.addMarker(new MarkerOptions()
                .position(mapCenter)
                .title("Position")
                .snippet(MercatorProjection.fromLatLngToPoint(mapCenter).toString())
                .draggable(true));

        Map.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
            @Override
            public void onMarkerDragStart(Marker marker) {
            }

            @Override
            public void onMarkerDrag(Marker marker) {
                PointF pos = MercatorProjection.fromLatLngToPoint(marker.getPosition());
                for (int x = 0; x < mapGrid.cols; x++) {
                    for (int y = 0; y < mapGrid.rows; y++) {
                        GridNode thisNode = mapGrid.get_node(x, y);
                        if (inRange(thisNode.node_position, pos, 0.05f)) {
                            Log.i(TAG, thisNode.toString());
                            to = thisNode;
                            AStar();
                            break;
                        }
                    }
                }
            }

            @Override
            public void onMarkerDragEnd(Marker marker) {
                Log.i(TAG, marker.getPosition() + "");
                marker.setSnippet(MercatorProjection.fromLatLngToPoint(marker.getPosition()).toString());
            }
        });
        Map.setOnMapClickListener(this);

        // Polylines are useful for marking paths and routes on the map.
        Polyline polyline = Map.addPolyline(new PolylineOptions().geodesic(true)
                .add(new LatLng(-33.866, 151.195))  // Sydney
                .add(new LatLng(-18.142, 178.431))  // Fiji
                .add(new LatLng(21.291, -157.821))  // Hawaii
                .add(new LatLng(37.423, -122.091))  // Mountain View
        );
        polyline.setZIndex(1000); // Or some large number :)

        // draw our recorded paths
//        drawRecordedPaths = new DrawRecordedPaths(getApplicationContext(), Map);

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
    public void onCameraChange(CameraPosition cameraPosition) {
        ((TextView) findViewById(R.id.mapZoomLevelDisplay)).setText(cameraPosition.zoom + "");
    }


    @Override
    public void onMapClick(LatLng latLng) {
        PointF clickedPoint = MercatorProjection.fromLatLngToPoint(latLng);
        // get markers at the area clicked
        for (int x = 0; x < mapGrid.cols; x++) {
            for (int y = 0; y < mapGrid.rows; y++) {
                GridNode thisNode = mapGrid.get_node(x, y);
                if (inRange(thisNode.node_position, clickedPoint, 0.5f)) {
                    Map.addMarker(new MarkerOptions()
                            .position(MercatorProjection.fromPointToLatLng(thisNode.node_position))
                            .icon(BitmapDescriptorFactory.fromResource(thisNode.isWalkable ? R.drawable.map_path : R.drawable.no_path))
                            .anchor(0.5f, 0.5f)
                            .title("Pos: " + thisNode.x + ", " + thisNode.y));
                }
            }
        }
    }

    public boolean inRange(PointF a, PointF b, float range) {
        return Math.abs(a.x - b.x) <= range && Math.abs(a.y - b.y) <= range;
    }
}
