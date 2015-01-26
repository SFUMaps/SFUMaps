package me.gurinderhans.sfumaps;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.PointF;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.TileOverlayOptions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends FragmentActivity {
    public static final String TAG = MainActivity.class.getSimpleName();
    public static final float TILE_SIZE = 256f;
    public PointF pixelOrigin_;
    public double pixelsPerLonDegree_, pixelsPerLonRadian_;
    private GoogleMap mMap; // Might be null if Google Play services APK is not available.

    // map database vars
    public static final String KEY_RSSI_DIFFERENCE = "rssi_diff";
    public static final String KEY_RECORDED_VAL = "recorded_val";
    public static final String[] DATA_TABLES = {"apsdata_AQ_EastToWestUP"
            , "apsdata_AQ_NorthToSouthLEFT"
            , "apsdata_AQ_NorthToSouthRIGHT"
            , "apsdata_AQ_EastToWestDOWN"};

    public static ArrayList<String> ALL_SSIDS;

    ArrayList<HashMap<String, String>> recordedAPs, matchingSignalsPickedUp;
    WifiManager service_WifiManager;
    WifiReceiver wifiReceiver;
    Handler mHandler;
    Runnable scanner;
    DataBaseManager mDataBaseManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ALL_SSIDS = new ArrayList<>(Arrays.asList("SFUNET", "SFUNET-SECURE", "eduroam"));

        mHandler = new Handler();
        service_WifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        wifiReceiver = new WifiReceiver();
        recordedAPs = new ArrayList<>();
        matchingSignalsPickedUp = new ArrayList<>();

        mDataBaseManager = new DataBaseManager(getApplicationContext());

        pixelOrigin_ = new PointF(TILE_SIZE / 2, TILE_SIZE / 2);
        pixelsPerLonDegree_ = TILE_SIZE / 360;
        pixelsPerLonRadian_ = TILE_SIZE / (2 * Math.PI);

        setUpMapIfNeeded();

        scanner = new Runnable() {
            @Override
            public void run() {
                service_WifiManager.startScan();
            }
        };

        getRecordedData();
        Log.i(TAG, "size: " + recordedAPs.size());

    }


    public void getRecordedData() {
        recordedAPs.clear();
        for (String table : DATA_TABLES) {

            ArrayList<HashMap<String, String>> tableRawData = mDataBaseManager.getTableData(table);

            ArrayList<ArrayList<HashMap<String, String>>> tmpList = ComplexFunctions.filterAPs(tableRawData, ALL_SSIDS);

            int max_ap_points = getArrayListMax(tmpList);

            for (ArrayList<HashMap<String, String>> d : tmpList) { //loop over each wifi SSID
                Collections.sort(d, new SortByTime(DataBaseManager.KEY_TIME));

                // LatLng Wlatlng = fromPointToLatLng(new PointF(10, ((TILE_SIZE / 9) * i) + 15)); //west
                // LatLng Elatlng = fromPointToLatLng(new PointF(246, ((TILE_SIZE / 9) * i) + 15)); //east
                //LatLng Nlatlng = fromPointToLatLng(new PointF(((TILE_SIZE / 9) * i) + 15, 12)); //north
                //LatLng Slatlng = fromPointToLatLng(new PointF(((TILE_SIZE / 9) * i) + 15, 248)); //south
                Log.i(TAG, "table: " + table);

                // TODO: create a better table naming scheme
                // TODO: check for list sizes, if they are > 0
//                PointF pointF = new PointF(0, 0);
//                if (table.toLowerCase().contains("northtosouthright")) {
//                    pointF = new PointF(242, TILE_SIZE / max_ap_points);
//                } else if (table.toLowerCase().contains("northtosouthleft")) {
//                    pointF = new PointF(5, TILE_SIZE / max_ap_points);
//                } else if (table.toLowerCase().contains("easttowestup")) {
//                    pointF = new PointF(TILE_SIZE / max_ap_points, 12);
//                } else if (table.toLowerCase().contains("easttowestdown_good2")) {
//                    //
//                }

                if (d.get(0).get(DataBaseManager.KEY_SSID).equals(ALL_SSIDS.get(0))) {
                    // SFUNET
                    if (table.toLowerCase().contains("northtosouthright")) {
                        for (int i = 0; i < d.size(); i++) {
                            LatLng latlng = fromPointToLatLng(new PointF(240, ((TILE_SIZE / max_ap_points) * i) + 15));
                            mMap.addMarker(new MarkerOptions()
                                    .position(latlng)
                                    .title(table)
                                    .snippet("#" + (i + 1))
                                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.sfunetdot)));
                        }
                    } else if(table.toLowerCase().contains("northtosouthleft")){
                        // check for size of list
                        for (int i = 0; i < d.size(); i++) {
                            LatLng latlng = fromPointToLatLng(new PointF(6, ((TILE_SIZE / max_ap_points) * i) + 15));
                            mMap.addMarker(new MarkerOptions()
                                    .position(latlng)
                                    .title(table)
                                    .snippet("#" + (i + 1))
                                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.sfunetdot)));
                        }

                    } else if (table.toLowerCase().contains("easttowestup")) {
                        for (int i = 0; i < d.size(); i++) {
                            LatLng latlng = fromPointToLatLng(new PointF(((TILE_SIZE / max_ap_points) * i) + 15, 11));
                            mMap.addMarker(new MarkerOptions()
                                    .position(latlng)
                                    .title(table)
                                    .snippet("#" + (i + 1))
                                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.sfunetdot)));
                        }

                    } else if (table.toLowerCase().contains("easttowestdown")) {
                        for (int i = 0; i < d.size(); i++) {
                            LatLng latlng = fromPointToLatLng(new PointF(((TILE_SIZE / max_ap_points) * i) + 15, 250));
                            mMap.addMarker(new MarkerOptions()
                                    .position(latlng)
                                    .title(table)
                                    .snippet("#" + (i + 1))
                                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.sfunetdot)));
                        }

                    }


                } else if (d.get(0).get(DataBaseManager.KEY_SSID).equals(ALL_SSIDS.get(1))) {
                    // SFUNET-SECURE
                    if (table.toLowerCase().contains("northtosouthright")) {
                        for (int i = 0; i < d.size(); i++) {
                            LatLng latlng = fromPointToLatLng(new PointF(245f, ((TILE_SIZE / max_ap_points) * i) + 15));
                            mMap.addMarker(new MarkerOptions()
                                    .position(latlng)
                                    .title(table)
                                    .snippet("#" + (i + 1))
                                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.sfunetsecuredot)));
                        }
                    } else if(table.toLowerCase().contains("northtosouthleft")){
                        for (int i = 0; i < d.size(); i++) {
                            LatLng latlng = fromPointToLatLng(new PointF(10, ((TILE_SIZE / max_ap_points) * i) + 15));
                            mMap.addMarker(new MarkerOptions()
                                    .position(latlng)
                                    .title(table)
                                    .snippet("#" + (i + 1))
                                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.sfunetsecuredot)));
                        }

                    } else if (table.toLowerCase().contains("easttowestup")) {

                        for (int i = 0; i < d.size(); i++) {
                            LatLng latlng = fromPointToLatLng(new PointF(((TILE_SIZE / max_ap_points) * i) + 15, 15));
                            mMap.addMarker(new MarkerOptions()
                                    .position(latlng)
                                    .title(table)
                                    .snippet("#" + (i + 1))
                                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.sfunetsecuredot)));
                        }

                    } else if (table.toLowerCase().contains("easttowestdown")) {
                        for (int i = 0; i < d.size(); i++) {
                            LatLng latlng = fromPointToLatLng(new PointF(((TILE_SIZE / max_ap_points) * i) + 15, 246));
                            mMap.addMarker(new MarkerOptions()
                                    .position(latlng)
                                    .title(table)
                                    .snippet("#" + (i + 1))
                                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.sfunetsecuredot)));
                        }

                    }
                } else {
                    // eduroam
                    Log.i(TAG, "#eduroam: " + d.size());
                    if (table.toLowerCase().contains("northtosouthright")) {
                        for (int i = 0; i < d.size(); i++) {
                            LatLng latlng = fromPointToLatLng(new PointF(250, ((TILE_SIZE / max_ap_points) * i) + 15));
                            mMap.addMarker(new MarkerOptions()
                                    .position(latlng)
                                    .title(table)
                                    .snippet("#" + (i + 1))
                                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.eduroamdot)));
                        }
                    } else if(table.toLowerCase().contains("northtosouthleft")){
                        for (int i = 0; i < d.size(); i++) {
                            LatLng latlng = fromPointToLatLng(new PointF(14, ((TILE_SIZE / max_ap_points) * i) + 15));
                            mMap.addMarker(new MarkerOptions()
                                    .position(latlng)
                                    .title(table)
                                    .snippet("#" + (i + 1))
                                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.eduroamdot)));
                        }

                    } else if (table.toLowerCase().contains("easttowestup")) {

                        for (int i = 0; i < d.size(); i++) {
                            LatLng latlng = fromPointToLatLng(new PointF(((TILE_SIZE / max_ap_points) * i) + 15, 19));
                            mMap.addMarker(new MarkerOptions()
                                    .position(latlng)
                                    .title(table)
                                    .snippet("#" + (i + 1))
                                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.eduroamdot)));
                        }

                    } else if (table.toLowerCase().contains("easttowestdown")) {
                        for (int i = 0; i < d.size(); i++) {
                            LatLng latlng = fromPointToLatLng(new PointF(((TILE_SIZE / max_ap_points) * i) + 15, 242));
                            mMap.addMarker(new MarkerOptions()
                                    .position(latlng)
                                    .title(table)
                                    .snippet("#" + (i + 1))
                                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.eduroamdot)));
                        }
                    }
                }

                recordedAPs.addAll(d);
            }

        }

    }


    private void displayData(List<ScanResult> wifiAPs) {

        matchingSignalsPickedUp.clear();

        ArrayList<HashMap<String, String>> scanResults = new ArrayList<>();


        // Convert ScanResult to ArrayList
        for (ScanResult result : wifiAPs) {
            HashMap<String, String> ap = new HashMap<>();
            ap.put(DataBaseManager.KEY_SSID, result.SSID);
            ap.put(DataBaseManager.KEY_BSSID, result.BSSID);
            ap.put(DataBaseManager.KEY_FREQ, result.frequency + " MHz");
            ap.put(DataBaseManager.KEY_RSSI, Integer.toString(result.level));
            ap.put(DataBaseManager.KEY_TIME, Long.toString(System.currentTimeMillis()));

            scanResults.add(ap);
        }

        /**
         * @see - probably should filter out some garbage / unwanted APs that were scanned
         */
        for (HashMap<String, String> recordedAP : recordedAPs) {
            String comparingBSSID = recordedAP.get(DataBaseManager.KEY_BSSID);
            for (HashMap<String, String> scannedAp : scanResults) {
                String comparingToBSSID = scannedAp.get(DataBaseManager.KEY_BSSID);
                if (comparingBSSID.equals(comparingToBSSID)) {
                    int recordedVal = Integer.parseInt(recordedAP.get(DataBaseManager.KEY_RSSI));
                    int newVal = Integer.parseInt(scannedAp.get(DataBaseManager.KEY_RSSI));
                    scannedAp.put(KEY_RSSI_DIFFERENCE, "Difference: " + Math.abs(Math.abs(recordedVal) - Math.abs(newVal)) + ""); //modifying
                    scannedAp.put(KEY_RECORDED_VAL, "Recorded RSSI: " + recordedVal + "");//adding new map
                    scannedAp.put(DataBaseManager.KEY_RSSI, "Current RSSI: " + newVal);//adding new map
                    matchingSignalsPickedUp.add(scannedAp);
                    break;
                }
            }
        }

        Collections.sort(matchingSignalsPickedUp, new Comparator<HashMap<String, String>>() {
            @Override
            public int compare(HashMap<String, String> lhs, HashMap<String, String> rhs) {
                int lhsDiff = Integer.parseInt(lhs.get(KEY_RSSI_DIFFERENCE).replaceAll("[^0-9]", ""));
                int rhsDiff = Integer.parseInt(rhs.get(KEY_RSSI_DIFFERENCE).replaceAll("[^0-9]", ""));
                return (lhsDiff < rhsDiff ? -1 : (rhsDiff == lhsDiff ? 0 : 1));
            }
        });

        // do something with this data.... somehow.....
        // user location will be decided here
    }

    private void setUpMap() {
        mMap.setMapType(GoogleMap.MAP_TYPE_NONE);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(0f, 0f), 2.0f));
        mMap.addTileOverlay(new TileOverlayOptions().tileProvider(new CustomMapTileProvider(getResources().getAssets())));


        PointF point = new PointF(128, 128);


        mMap.addMarker(new MarkerOptions().position(fromPointToLatLng(point)).title("Center from PointF"));

        LatLng chicago = new LatLng(0, 0);

        int numTiles = 1 << (int) (mMap.getCameraPosition().zoom);
        PointF worldCoordinate = fromLatLngToPoint(chicago);
        PointF pixelCoordinate = new PointF(
                worldCoordinate.x * numTiles,
                worldCoordinate.y * numTiles);
        PointF tileCoordinate = new PointF(
                (float) (Math.floor(pixelCoordinate.x / TILE_SIZE)),
                (float) (Math.floor(pixelCoordinate.y / TILE_SIZE)));

//        Log.i(TAG, "tile coordinate: " + tileCoordinate);
//        drawRouterDots();

    }

    private void drawRouterDots() {

        // AQ West and East
        for (int i = 0; i <= 9; i++) {
            LatLng Wlatlng = fromPointToLatLng(new PointF(10, ((TILE_SIZE / 9) * i) + 15)); //west
            mMap.addMarker(new MarkerOptions()
                    .position(Wlatlng)
                    .title("West")
                    .snippet("#" + (i + 1))
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.routerdot)));

            LatLng Elatlng = fromPointToLatLng(new PointF(246, ((TILE_SIZE / 9) * i) + 15)); //east
            mMap.addMarker(new MarkerOptions()
                    .position(Elatlng)
                    .title("East")
                    .snippet("#" + (i + 1))
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.routerdot)));
        }

        // AQ North and South
        for (int i = 0; i <= 9; i++) {
            LatLng Nlatlng = fromPointToLatLng(new PointF(((TILE_SIZE / 9) * i) + 15, 12)); //north
            mMap.addMarker(new MarkerOptions()
                    .position(Nlatlng)
                    .title("North")
                    .snippet("#" + (i + 1))
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.routerdot)));

            LatLng Slatlng = fromPointToLatLng(new PointF(((TILE_SIZE / 9) * i) + 15, 248)); //south
            mMap.addMarker(new MarkerOptions()
                    .position(Slatlng)
                    .title("North")
                    .snippet("#" + (i + 1))
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.routerdot)));
        }
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

    // called when wifi scanner finishes scan
    private class WifiReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context c, Intent intent) {
            displayData(service_WifiManager.getScanResults());
            mHandler.postDelayed(scanner, 0);
        }
    }

    private PointF fromLatLngToPoint(LatLng latLng) {
        PointF point = new PointF(0, 0);
        PointF origin = this.pixelOrigin_;

        point.x = (float) (origin.x + latLng.longitude * this.pixelsPerLonDegree_);

        // Truncating to 0.9999 effectively limits latitude to 89.189. This is
        // about a third of a tile past the edge of the world tile.
        double siny = bound(Math.sin(degreesToRadians(latLng.latitude)), -0.9999,
                0.9999);
        point.y = (float) (origin.y + 0.5 * Math.log((1 + siny) / (1 - siny)) * -this.pixelsPerLonRadian_);
        return point;
    }

    public LatLng fromPointToLatLng(PointF point) {
        PointF origin = this.pixelOrigin_;
        double lng = (point.x - origin.x) / this.pixelsPerLonDegree_;
        double latRadians = (point.y - origin.y) / -this.pixelsPerLonRadian_;
        double lat = radiansToDegrees(2 * Math.atan(Math.exp(latRadians)) - Math.PI / 2);
        return new LatLng(lat, lng);
    }

    public double bound(double value, double opt_min, double opt_max) {
        if (opt_min != 0) return Math.max(value, opt_min);
        if (opt_max != 0) return Math.min(value, opt_max);
        return -1;
    }

    private double degreesToRadians(double deg) {
        return deg * (Math.PI / 180);
    }

    private double radiansToDegrees(double rad) {
        return rad / (Math.PI / 180);
    }

    private int getArrayListMax(ArrayList<ArrayList<HashMap<String, String>>> data) {
        List<Integer> sz = new ArrayList<>();
        int max = 0;
        for (ArrayList<HashMap<String, String>> list : data) sz.add(list.size());
        for (int num : sz) if (num >= max) max = num;
        return max;
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
    }
}
