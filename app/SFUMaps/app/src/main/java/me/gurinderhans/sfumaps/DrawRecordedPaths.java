package me.gurinderhans.sfumaps;

import android.content.Context;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by ghans on 1/26/15.
 */
public class DrawRecordedPaths {

    /**
     *  -----------------------------
     * |  Current Table Name Scheme  |
     *  -----------------------------
     = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = =

     * 1. globalprefix = 'apsdata_'

     * 2. location general name -> ex. AQ, TASC1, ASB

     * 3. location specific name generalName_ [ {North, South, East, West}, {Lvl9_Far} ]

     * 4. floor level (M = main) and (M+n) for floors above M and (M-n) for floors below M - AQ 3000 is considered as floor M

     * 5. direction = VR (vertical) | HR (horizontal) or in rare cases CSTNA ( custom n/a )

     * ==> tableName = '{0}_{1}_{2}_{3}_{4}' % (globaleprefix, locationGeneralName, locationSpecificName, floor level, direction)

     * ex. -> 'apsdata_AQ_North_M_HR'

     */



    public static final String TAG = DrawRecordedPaths.class.getSimpleName();

    boolean DEBUG = false;

    DataBaseManager mDataBaseManager;

    public DrawRecordedPaths(boolean debug, Context ctx) {
        DEBUG = debug;

        mDataBaseManager = new DataBaseManager(ctx);

        fetchData();

    }

    public void fetchData(){
        for(String table: mDataBaseManager.getTables()) {
            ArrayList<HashMap<String, String>> tableRawData = mDataBaseManager.getTableData(table);
        }
    }




//    public void getRecordedData() {
//        recordedAPs.clear();
//        for (String table : DATA_TABLES) {
//
//            ArrayList<HashMap<String, String>> tableRawData = mDataBaseManager.getTableData(table);
//
//            ArrayList<ArrayList<HashMap<String, String>>> tmpList = ComplexFunctions.filterAPs(tableRawData, ALL_SSIDS);
//
//            int max_ap_points = getMaxAPPoints(tmpList);
//
//            for (ArrayList<HashMap<String, String>> d : tmpList) { //loop over each wifi SSID
//                Collections.sort(d, new SortByTime(DataBaseManager.KEY_TIME));
//
//                Log.i(TAG, "table: " + table);
//
//
//                // TODO: figure out points path for each data set
//
//                recordedAPs.addAll(d);
//            }
//
//        }
//
//    }
//
//    private void drawRouterDots() {
//
//        // AQ West and East
//        for (int i = 0; i <= 9; i++) {
//            LatLng Wlatlng = fromPointToLatLng(new PointF(10, ((TILE_SIZE / 9) * i) + 15)); //west
//            mMap.addMarker(new MarkerOptions()
//                    .position(Wlatlng)
//                    .title("West")
//                    .snippet("#" + (i + 1))
//                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.routerdot)));
//
//            LatLng Elatlng = fromPointToLatLng(new PointF(246, ((TILE_SIZE / 9) * i) + 15)); //east
//            mMap.addMarker(new MarkerOptions()
//                    .position(Elatlng)
//                    .title("East")
//                    .snippet("#" + (i + 1))
//                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.routerdot)));
//        }
//
//        // AQ North and South
//        for (int i = 0; i <= 9; i++) {
//            LatLng Nlatlng = fromPointToLatLng(new PointF(((TILE_SIZE / 9) * i) + 15, 12)); //north
//            mMap.addMarker(new MarkerOptions()
//                    .position(Nlatlng)
//                    .title("North")
//                    .snippet("#" + (i + 1))
//                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.routerdot)));
//
//            LatLng Slatlng = fromPointToLatLng(new PointF(((TILE_SIZE / 9) * i) + 15, 248)); //south
//            mMap.addMarker(new MarkerOptions()
//                    .position(Slatlng)
//                    .title("North")
//                    .snippet("#" + (i + 1))
//                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.routerdot)));
//        }
//    }


//    private void displayData(List<ScanResult> wifiAPs) {
//
//        matchingSignalsPickedUp.clear();
//
//        ArrayList<HashMap<String, String>> scanResults = new ArrayList<>();
//
//
//        // Convert ScanResult to ArrayList
//        for (ScanResult result : wifiAPs) {
//            HashMap<String, String> ap = new HashMap<>();
//            ap.put(DataBaseManager.KEY_SSID, result.SSID);
//            ap.put(DataBaseManager.KEY_BSSID, result.BSSID);
//            ap.put(DataBaseManager.KEY_FREQ, Integer.toString(result.frequency));
//            ap.put(DataBaseManager.KEY_RSSI, Integer.toString(result.level));
//            ap.put(DataBaseManager.KEY_TIME, Long.toString(System.currentTimeMillis()));
//
//            scanResults.add(ap);
//        }

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
//    }

    //        getRecordedData();
//        Log.i(TAG, "size: " + recordedAPs.size());


        private int getMaxAPPoints(ArrayList<ArrayList<HashMap<String, String>>> data) {
            int max = 0;
            for (ArrayList<HashMap<String, String>> list : data)
                if (list.size() > max) max = list.size();
            return max;
        }


}
