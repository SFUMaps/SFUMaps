package me.gurinderhans.sfumaps;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.PointF;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.TileOverlayOptions;

public class MainActivity extends FragmentActivity {

    public static final String TAG = MainActivity.class.getSimpleName();

    WifiManager service_WifiManager;

    WifiReceiver wifiReceiver;

    Handler mHandler;

    Runnable scanner;

    DrawRecordedPaths drawRecordedPaths;

    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mHandler = new Handler();

        setUpMapIfNeeded();

        service_WifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);

        wifiReceiver = new WifiReceiver();

        /*scanner = new Runnable() {
            @Override
            public void run() {
                service_WifiManager.startScan();
            }
        };*/

    }

    private void setUpMap() {
        mMap.setMapType(GoogleMap.MAP_TYPE_NONE);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(0f, 0f), 2.0f));
        mMap.addTileOverlay(new TileOverlayOptions().tileProvider(new CustomMapTileProvider(getResources().getAssets())));

        drawRecordedPaths = new DrawRecordedPaths(true, getApplicationContext(), mMap);

        PointF point = new PointF(128, 128);


        mMap.addMarker(new MarkerOptions().position(MapTools.fromPointToLatLng(point)).title("Center from PointF"));
//
//        LatLng center = new LatLng(0, 0);
//
//        int numTiles = 1 << (int) (mMap.getCameraPosition().zoom);
//        PointF worldCoordinate = MapTools.fromLatLngToPoint(center);
//        PointF pixelCoordinate = new PointF(
//                worldCoordinate.x * numTiles,
//                worldCoordinate.y * numTiles);
//        PointF tileCoordinate = new PointF(
//                (float) (Math.floor(pixelCoordinate.x / AppConstants.TILE_SIZE)),
//                (float) (Math.floor(pixelCoordinate.y / AppConstants.TILE_SIZE)));
//
//        Log.i(TAG, "tile coordinate: " + tileCoordinate);
//        drawRouterDots();

    }

    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMap();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
    }

    // called when wifi scanner finishes scan
    private class WifiReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context c, Intent intent) {
//            displayData(service_WifiManager.getScanResults());
            mHandler.postDelayed(scanner, 0);
        }
    }
}
