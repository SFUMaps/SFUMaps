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
import android.util.Log;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.TileOverlayOptions;

import java.util.HashMap;
import java.util.List;

import me.gurinderhans.sfumaps.wifirecorder.Controller.RecordWifiDataActivity;

public class MainActivity extends FragmentActivity {

    public static final String TAG = MainActivity.class.getSimpleName();

    // system classes that provide access to the wifi scanner

    WifiManager wifiManager; // system service that handles wifi
    WifiReceiver wifiReceiver; // broadcast receiver that listens for wifi scans and gets back the results
    Handler mHandler; // handler for initiating a wifi scan
    Runnable wifiScanner; // runs the system wifi scan

    // google maps

    GoogleMap Map;
    Marker userNavMarker; // marks users current location

    // reference to our custom class that draws recorded paths

    DrawRecordedPaths drawRecordedPaths;

    // TEMP: for debugging

    TextView infoBox;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.startActivity(new Intent(this, RecordWifiDataActivity.class));

//        // load app preferences
//        AppConfig.loadPreferences(getApplicationContext());
//
//        // load wifi manager, reciever, handler to handle wifi scans
//        wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
//        mHandler = new Handler();
//        wifiReceiver = new WifiReceiver();
//
//        // create the runnable to handle scans
//        wifiScanner = new Runnable() {
//            @Override
//            public void run() {
//                wifiManager.startScan();
//            }
//        };


        // init map
//        setUpMapIfNeeded();

        // info box to show stuff for dev purporses
//        infoBox = (TextView) findViewById(R.id.infoBox);

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
     * - TODO: figure out user's initial location here
     * - draw the recorded paths here
     */
    private void setUpMap() {

        Map.setMapType(GoogleMap.MAP_TYPE_NONE); // hide the default google maps overlay
        Map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(0f, 0f), 2.0f)); // set the camera to (0,0) with zoom=2

        // here we add our own tile overlay with custom image tiles
        Map.addTileOverlay(new TileOverlayOptions().tileProvider(new CustomMapTileProvider(getResources().getAssets())));

        // hide the marker toolbar - the two buttons on the bottom right that go to google maps
        Map.getUiSettings().setMapToolbarEnabled(false);

        // draw our recorded paths
        drawRecordedPaths = new DrawRecordedPaths(getApplicationContext(), Map);

        // just put the user navigation marker in the center as we don't yet know user's location
        LatLng mapCenter = MercatorProjection.fromPointToLatLng(new PointF(AppConfig.TILE_SIZE, AppConfig.TILE_SIZE));
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

        // register receiver
        registerReceiver(wifiReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));

    }

    @Override
    protected void onResume() {
        super.onResume();

//        setUpMapIfNeeded();

//        mHandler.postDelayed(wifiScanner, 0); // start scanner
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    String getBestMatch(List<ScanResult> scanResults) {
        HashMap<String, Object> bestAP = new HashMap<>();

        int minDiff = Integer.MAX_VALUE;
        String apBSSID = "";

        for (ScanResult result : scanResults) {
            int scanRSSI = result.level;
            for (HashMap<String, Object> ap : DrawRecordedPaths.specialAPs) {
                if (ap.get(Keys.KEY_BSSID).equals(result.BSSID)) {
                    int recordedRSSI = Integer.parseInt(ap.get(Keys.KEY_RSSI) + "");
                    int diff = Math.abs(scanRSSI - recordedRSSI);

                    if (diff < minDiff) {
                        minDiff = diff;
                        apBSSID = result.BSSID;
                    }

                }
            }
        }

        return apBSSID;
    }

    boolean isInRange(int min, int max, int value) {
        return (value >= min && value <= max);
    }

    // broadcast receiver for knowing when the wifi scan finishes
    private class WifiReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context c, Intent intent) {

            List<ScanResult> scanResults = wifiManager.getScanResults();

            StringBuilder seenAPs = new StringBuilder("");
            String bestMatchBSSID = getBestMatch(scanResults);

            seenAPs.append(DrawRecordedPaths.allAPs.get(bestMatchBSSID).get(0).get(Keys.KEY_SSID));
            seenAPs.append(",");
            seenAPs.append(bestMatchBSSID);

//            ArrayList<HashMap<String, Object>> bestMatch = DrawRecordedPaths.allAPs.get(bestMatchBSSID);

            for (HashMap<String, Object> ap: DrawRecordedPaths.specialAPs) {
                if (ap.get(Keys.KEY_BSSID).equals(bestMatchBSSID)) {
                    userNavMarker.setPosition(MercatorProjection.fromPointToLatLng((PointF) ap.get(Keys.KEY_POINT)));
                }
            }




//            ArrayList<PointF> matchedPoints = new ArrayList<>();
//            for(ScanResult res: scanResults) {
//                if (res.BSSID.equals(bestMatchBSSID)) {
//                    // figure out further location from bestMatchBSSID
//                    for (HashMap<String, Object> match : bestMatch) {
//                        int recordedRSSI = Integer.parseInt(match.get(Keys.KEY_RSSI)+"");
//                        if (isInRange(recordedRSSI - 1, recordedRSSI +1, res.level)) {
//                            matchedPoints.add((PointF) match.get(Keys.KEY_POINT));
//                        }
//                    }
//                }
//            }
//
//            Log.i(TAG, "matched size: " + matchedPoints.size());
//
//            // for now we assume user's position is the centroid of all points
//            if (matchedPoints.size() > 0) {
//                PointF centroid = MapTools.getCentroid(matchedPoints);
//                Log.i(TAG, "loc computed: " + centroid);
//                userNavMarker.setPosition(MercatorProjection.fromPointToLatLng(centroid));
//            }


//            Toast.makeText(getApplicationContext(), "Scan finished", Toast.LENGTH_SHORT).show();
            infoBox.setText(seenAPs);

            // scan again
            mHandler.postDelayed(wifiScanner, 0);

        }
    }


}
