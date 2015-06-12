package me.gurinderhans.sfumaps;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.android.gms.maps.model.TileProvider;
import com.jakewharton.disklrucache.DiskLruCache;

import java.io.IOException;

import me.gurinderhans.sfumaps.wifirecorder.Controller.RecordWifiDataActivity;

public class MainActivity extends FragmentActivity {

    public static final String TAG = MainActivity.class.getSimpleName();

    // TODO: either disable indoor map of real life buildings on map, or simply don't allow that much zooming in

    // google maps
    GoogleMap Map;
    Marker userNavMarker; // marks users current location

    // tile provider cache
    private DiskLruCache mTileCache;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // load app preferences
        AppConfig.loadPreferences(getApplicationContext());

        (findViewById(R.id.backend_panel)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), RecordWifiDataActivity.class));
            }
        });

        // init map
        setUpMapIfNeeded();
    }

    /**
     * If (Map == null) then get the map fragment and initialize it
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
     * - define map settings
     * - set custom map tiles
     * - TODO: get user's initial location here
     * - draw the recorded paths here
     */
    private void setUpMap() {

        Map.setMapType(GoogleMap.MAP_TYPE_NONE); // hide the default google maps overlay
        Map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(0, 0), 1f)); // set the camera to (0,0) with zoom=1


        // here we add our own tile overlay with custom image tiles

        mTileCache = MapTools.openDiskCache(this);

        TileProvider provider;
        try {

            SVGTileProvider svgProvider = new SVGTileProvider(MapTools.getTileFiles(this), getResources().getDisplayMetrics().densityDpi / 160f);
            if (mTileCache == null) {
                // Use the SVGTileProvider directly as the TileProvider without a cache
                provider = svgProvider;
            } else {
                // Wrap the SVGTileProvider in a CachedTileProvider for caching on disk
                provider = new CachedTileProvider(Integer.toString(0), svgProvider, mTileCache);
            }
        } catch (IOException e) {
            e.printStackTrace();
            Log.d(TAG, "Could not create Tile Provider.");
            return;
        }

        Map.addTileOverlay(new TileOverlayOptions().tileProvider(provider));

        // hide the marker toolbar - the two buttons on the bottom right that go to google maps
        Map.getUiSettings().setMapToolbarEnabled(false);

        // draw our recorded paths
//        drawRecordedPaths = new DrawRecordedPaths(getApplicationContext(), Map);

        // just put the user navigation marker in the center as we don't yet know user's location
        LatLng mapCenter = new LatLng(0, 0);//MercatorProjection.fromPointToLatLng(new PointF(AppConfig.TILE_SIZE, AppConfig.TILE_SIZE));
        userNavMarker = Map.addMarker(new MarkerOptions()
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
}
