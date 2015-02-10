package me.gurinderhans.sfumaps;

import android.graphics.PointF;
import android.net.wifi.ScanResult;
import android.util.Log;

import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 * Created by ghans on 1/26/15.
 */
public class DrawRecordedPaths {

    DataBaseManager mDataBaseManager;

    ArrayList<HashMap<String, String>> currentDataSet = new ArrayList<>();

    public DrawRecordedPaths() {
        //
    }

    private int getMaxAPPoints(ArrayList<ArrayList<HashMap<String, String>>> data) {
        int max = 0;
        for (ArrayList<HashMap<String, String>> list : data)
            if (list.size() > max) max = list.size();
        return max;
    }


    public void getRecordedData() {
        recordedAPs.clear();
        for (String table : DATA_TABLES) {

            ArrayList<HashMap<String, String>> tableRawData = mDataBaseManager.getTableData(table);

            ArrayList<ArrayList<HashMap<String, String>>> tmpList = ComplexFunctions.filterAPs(tableRawData, ALL_SSIDS);

            int max_ap_points = getMaxAPPoints(tmpList);

            for (ArrayList<HashMap<String, String>> d : tmpList) { //loop over each wifi SSID
                Collections.sort(d, new SortByTime(DataBaseManager.KEY_TIME));

                Log.i(TAG, "table: " + table);

                /**
                 *  -----------------------------
                 * | Current Table Name Proposal |
                 *  -----------------------------
                 = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = =

                 * globalprefix = 'apsdata_'
                 * locationSpecificName -> ex. AQ_{North, South, East, West} ,TASC1_Lvl9_Far, ASBLvlM (M = main)
                 * direction = VR (vertical) | HR (horizontal) or in rare cases CSTNA ( custom n/a )
                 *
                 * tableName = globaleprefix + locationSpecificName + '_' + direction
                 * ex. -> 'apsdata_AQ_North_HR'
                 */

                // TODO: figure out points path for each data set

                recordedAPs.addAll(d);
            }

        }

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


    private void displayData(List<ScanResult> wifiAPs) {

        matchingSignalsPickedUp.clear();

        ArrayList<HashMap<String, String>> scanResults = new ArrayList<>();


        // Convert ScanResult to ArrayList
        for (ScanResult result : wifiAPs) {
            HashMap<String, String> ap = new HashMap<>();
            ap.put(DataBaseManager.KEY_SSID, result.SSID);
            ap.put(DataBaseManager.KEY_BSSID, result.BSSID);
            ap.put(DataBaseManager.KEY_FREQ, Integer.toString(result.frequency));
            ap.put(DataBaseManager.KEY_RSSI, Integer.toString(result.level));
            ap.put(DataBaseManager.KEY_TIME, Long.toString(System.currentTimeMillis()));

            scanResults.add(ap);
        }

        /**
         * @see - probably should filter out some garbage / unwanted APs that were already scanned ?
         */
/*
        for (HashMap<String, String> recordedAP : recordedAPs) {
            String comparingBSSID = recordedAP.get(DataBaseManager.KEY_BSSID);
            for (HashMap<String, String> scannedAp : scanResults) {
                String comparingToBSSID = scannedAp.get(DataBaseManager.KEY_BSSID);
                if (comparingBSSID.equals(comparingToBSSID)) {
                    int recordedVal = Integer.parseInt(recordedAP.get(DataBaseManager.KEY_RSSI));
                    int newVal = Integer.parseInt(scannedAp.get(DataBaseManager.KEY_RSSI));
                    scannedAp.put(KEY_RSSI_DIFFERENCE, Math.abs(Math.abs(recordedVal) - Math.abs(newVal)) + "");
                    scannedAp.put(KEY_RECORDED_VAL, recordedVal + "");//adding new map
                    scannedAp.put(DataBaseManager.KEY_RSSI, newVal + "");//adding new map
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

*/

        // do something with this data.... somehow.....
        // user location will be decided here
    }

    //        getRecordedData();
//        Log.i(TAG, "size: " + recordedAPs.size());


}
