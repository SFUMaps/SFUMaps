package me.gurinderhans.sfumaps;

import android.content.Context;
import android.graphics.PointF;
import android.util.Log;

import com.google.android.gms.maps.GoogleMap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

/**
 * Created by ghans on 1/26/15.
 */
public class DrawRecordedPaths {


    /**
     * -------------------------------
     * Table Name Scheme for wifi data
     * -------------------------------
     * = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = =
     * 1. [prefix] = 'apsdata_'
     * -
     * 2. [university name] -> ex. SFU
     * -
     * 3. [university campus name] -> ex. SFU {Burnaby, Surrey, etc...}
     * -
     * 4. [building name] -> ex. AQ, TASC1, ASB
     * -
     * 5. [floor level] -> ex. 3000 at AQ or 9000 at TASC1
     * -
     * 6. [pathway name (road)] -> {North, South} in terms of AQ or Lvl9_Far in terms of TASC1
     * -
     * 7. [direction] -> is the path vertical or horizontal? : Street = Vertical, Avenue = Horizontal or SP = at some angle
     * -
     * 8. [reverse] -> _R
     * -
     * ==> [tableName] = '{1}_{2}_{3}_{4}_{5}_{6}_{7}' % (prefix, universityName, campusName, buildingName, floorLevel, pathwayName, direction)
     * -
     * i.e. -> 'apsdata_SFU_BURNABY_AQ_3000_North_Avenue'
     * i.e  -> 'apsdata_SFU_BURNABY_TASC1_9000_Near_Avenue'
     */

    /*  University
        |--Campuses
            |--Buildings
                |--Floors
                    |--Individual Roads (Streets and Avenues)
     */


    public static final String TAG = DrawRecordedPaths.class.getSimpleName();
    static int MAP_X = 128;
    static int MAP_Y = 90;
    static int MAP_SZ = 80;
    static HashMap<String, ArrayList<HashMap<String, Object>>> allAPs = new HashMap<>();
    static ArrayList<HashMap<String, Object>> specialAPs = new ArrayList<>();
    DataBaseManager dataBaseManager;
    GoogleMap mMap;

    public DrawRecordedPaths(Context ctx, GoogleMap map) {
        this.mMap = map;

        dataBaseManager = new DataBaseManager(ctx);

        for (String dataTable : dataBaseManager.getDataTables()) {

            ArrayList<HashMap<String, Object>> allTableData = dataBaseManager.getTableData(dataTable);

            long startT = Long.parseLong(allTableData.get(0).get(Keys.KEY_TIME) + "");
            long endT = Long.parseLong(allTableData.get(allTableData.size() - 1).get(Keys.KEY_TIME) + "");

            allAPs = MapTools.separateByKeys(allTableData, Keys.KEY_BSSID);

            specialAPs = getSpecialAPs(allAPs);
            Log.i(TAG, "special APs size: " + specialAPs.size());

            // plot special AP's
            plotData(specialAPs, startT, endT, MAP_SZ, true);

            // plot other AP's
            for (String key : allAPs.keySet()) {
                plotData(allAPs.get(key), startT, endT, MAP_SZ, false);
            }

        }

    }


    void plotData(ArrayList<HashMap<String, Object>> data, long startT, long endT, float path_length, boolean drawOnMap) {

        float totalSeconds = (endT - startT) / 1000f;
        float scaleFactor = path_length / totalSeconds;

        PointF point = new PointF(MAP_X, MAP_Y);

        for (HashMap<String, Object> row : data) {

            float pos = ((endT - Long.parseLong(String.valueOf(row.get(Keys.KEY_TIME)))) / 1000f) * scaleFactor;

            point.y = MAP_Y + pos;

            row.put(Keys.KEY_POINT, point);

            if (drawOnMap) {
                MapTools.addMarker(mMap, MercatorProjection.fromPointToLatLng(point), row.get(Keys.KEY_SSID).toString(), row.get(Keys.KEY_RSSI) + "");
            }

        }
    }

    ArrayList<HashMap<String, Object>> getSpecialAPs(HashMap<String, ArrayList<HashMap<String, Object>>> input) {
        ArrayList<HashMap<String, Object>> specialAPs = new ArrayList<>();


        for (String key : input.keySet()) {
            ArrayList<HashMap<String, Object>> thisAP = input.get(key);

            Collections.sort(thisAP, new Comparator<HashMap<String, Object>>() {
                @Override
                public int compare(HashMap<String, Object> lhs, HashMap<String, Object> rhs) {
                    int firstValue = Integer.parseInt(lhs.get(Keys.KEY_RSSI) + "");
                    int secondValue = Integer.parseInt(rhs.get(Keys.KEY_RSSI) + "");
                    return (secondValue < firstValue ? -1 : (secondValue == firstValue ? 0 : 1));
                }
            });

            HashMap<String, Object> bestAP = thisAP.get(0);
            int rssi = Integer.parseInt(bestAP.get(Keys.KEY_RSSI) + "");

            if (rssi > AppConfig.RSSI_THRESHOLD &&
                    AppConfig.ALL_SSIDS.contains(bestAP.get(Keys.KEY_SSID))) {
                Log.i(TAG, "AP: " + bestAP.get(Keys.KEY_SSID) + " RSSI: " + rssi);
                specialAPs.add(thisAP.get(0));
            }
        }

        return specialAPs;

    }

}