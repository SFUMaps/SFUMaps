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
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.android.gms.maps.model.TileProvider;

import java.util.ArrayList;

import me.gurinderhans.sfumaps.Factory.GridNode;
import me.gurinderhans.sfumaps.Factory.MapGrid;
import me.gurinderhans.sfumaps.wifirecorder.Controller.RecordWifiDataActivity;

public class MainActivity extends FragmentActivity implements OnCameraChangeListener {

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


        // some random sample text to fill up the map

        // random locations
        MapTools.addTextAndIconMarker(this,
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
                5f);


        // create grid
        MapGrid grid = new MapGrid(new PointF(158.65297f, 106.69752f), new PointF(170.47316f, 118.315834f));

        GridNode frm = new GridNode(2, 3, MapGrid.WALKABLE_PATH_CHAR, new PointF(158.65297f, 106.69752f));
        GridNode to = new GridNode(6, 8, MapGrid.WALKABLE_PATH_CHAR, new PointF(158.65297f, 106.69752f));
        grid.mMapGrid.get(frm.x).get(frm.y).setNodeCharId("A");
        grid.mMapGrid.get(to.x).get(to.y).setNodeCharId("B");

        // no walk area
//        GridNode no_walk = new GridNode(4, 5, MapGrid.NON_WALKABLE_PATH_CHAR, new PointF(158.65297f, 106.69752f));
//        grid.mMapGrid.get(no_walk.x).get(no_walk.y).setNodeCharId(MapGrid.NON_WALKABLE_PATH_CHAR);

        // A* search
        ArrayList<GridNode> open_list = new ArrayList<>();
        ArrayList<GridNode> closed_list = new ArrayList<>();

        //
        open_list.add(frm.cost(frm, to));

        GridNode endNode = new GridNode(-1, -1, "@", new PointF(158.65297f, 106.69752f));

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

            for (GridNode n : grid.neighbours(current_node)) {
                Log.i(TAG, "node: " + n.toString());
                n.parentNode = current_node;

                if (GridNode.searchNode(n, closed_list) > -1) {
                    continue;
                }

                float tenative_g_score = current_node.gcost + GridNode.dist(current_node, n);

                if (GridNode.searchNode(n, open_list) == -1 || tenative_g_score < n.gcost) {
                    open_list.add(n.cost(frm, to));
                    int nbr_indedx = GridNode.searchNode(n, open_list);
                    GridNode tmp = open_list.get(nbr_indedx);
                    tmp.gcost = tenative_g_score;
                    tmp.fcost = tenative_g_score + GridNode.dist(n, to);

                    Log.i(TAG, "node parent: " + tmp.parentNode);
                    open_list.set(nbr_indedx, tmp);
                }
            }

        }

        ArrayList<GridNode> cpath = new ArrayList<>();

        while (endNode.parentNode != null) {
            endNode = endNode.parentNode;
            cpath.add(endNode);
        }

        cpath.remove(cpath.size() - 1);

        Log.i(TAG, "node path size: " + cpath.size());


        for (GridNode pathNode : cpath) {
            grid.mMapGrid.get(pathNode.x).get(pathNode.y).setNodeCharId("@");
        }

        grid.printMap(this, Map);
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
            }

            @Override
            public void onMarkerDragEnd(Marker marker) {
                Log.i(TAG, marker.getPosition() + "");
                marker.setSnippet(MercatorProjection.fromLatLngToPoint(marker.getPosition()).toString());
            }
        });

        // Polylines are useful for marking paths and routes on the map.
        Polyline polyline = Map.addPolyline(new PolylineOptions().geodesic(true)
                .add(new LatLng(-33.866, 151.195))  // Sydney
                .add(new LatLng(-18.142, 178.431))  // Fiji
                .add(new LatLng(21.291, -157.821))  // Hawaii
                .add(new LatLng(37.423, -122.091))  // Mountain View
        );
        polyline.setZIndex(1000); //Or some large number :)

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

}
