package me.gurinderhans.sfumaps;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.PointF;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.TileOverlayOptions;

import java.util.HashMap;
import java.util.List;

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
        service_WifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        wifiReceiver = new WifiReceiver();

        setUpMapIfNeeded();

        scanner = new Runnable() {
            @Override
            public void run() {
                service_WifiManager.startScan();
            }
        };

    }

    private void setUpMap() {
        mMap.setMapType(GoogleMap.MAP_TYPE_NONE);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(0f, 0f), 2.0f));
        mMap.addTileOverlay(new TileOverlayOptions().tileProvider(new CustomMapTileProvider(getResources().getAssets())));

        drawRecordedPaths = new DrawRecordedPaths(true, getApplicationContext(), mMap);

        LatLng Wlatlng = MapTools.fromPointToLatLng(new PointF(AppConstants.TILE_SIZE / 2, AppConstants.TILE_SIZE / 2)); //west
        mMap.addMarker(new MarkerOptions()
                .position(Wlatlng)
                .title("Center")
                .snippet("User dot")
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.userdot)));


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
        registerReceiver(wifiReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
//        showData(service_WifiManager.getScanResults());
        mHandler.postDelayed(scanner, 0);
    }

    @Override
    public void onPause() {
        super.onPause();
        unregisterReceiver(wifiReceiver);
    }

    // called when wifi scanner finishes scan
    private class WifiReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context c, Intent intent) {
            displayData(service_WifiManager.getScanResults());
//            Log.i(TAG, service_WifiManager.getScanResults()+"");
            mHandler.postDelayed(scanner, 0);
        }
    }

    public void displayData(List<ScanResult> scanData) {

        for (String key: drawRecordedPaths.seperatedData.keySet()) {
            for (HashMap<String, Object> dataRow: drawRecordedPaths.seperatedData.get(key)) {
                for (ScanResult res: scanData) {
                    if (dataRow.get(DataBaseManager.KEY_SSID).equals(res.BSSID)) {
                        // we need to compare atleast a few of recorded points and see which has the least difference
                        // as in |recordedRSSI| - |scannedRSSI| and put us at the one with least difference

                        // for now add these differene values along with recorded value index in some array / hashmap
                        // then get min of that array (as in least differnce) put user marker there (outside this loop)
                    }
                }
            }
        }
    }
}
