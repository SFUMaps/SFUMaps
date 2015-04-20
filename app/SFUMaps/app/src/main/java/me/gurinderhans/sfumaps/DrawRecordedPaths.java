package me.gurinderhans.sfumaps;

import android.content.Context;
import android.graphics.PointF;
import android.util.Log;

import com.google.android.gms.maps.GoogleMap;

import java.util.ArrayList;
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

    /* AQ
    "startPoint": "57.959, 54.997"
    "endPoint": "201.212, 201.065"
    AQ = 146.068 units long
     */


    public static final String TAG = DrawRecordedPaths.class.getSimpleName();

    DataBaseManager dataBaseManager;
    GoogleMap mMap;

    public DrawRecordedPaths(Context ctx, GoogleMap map, boolean debugState) {
        this.mMap = map;

        dataBaseManager = new DataBaseManager(ctx);

        for (String tableName : dataBaseManager.getTableNames()) {

            ArrayList<HashMap<String, Object>> forward = new ArrayList<>();
            ArrayList<HashMap<String, Object>> reverse = new ArrayList<>();

            boolean do_reverse = false;

            for (HashMap<String, Object> row : dataBaseManager.getTableData(tableName)) {
                // check for reverse header row
                // TODO: maybe wanna do more stricter checking later on
                if (row.get(Keys.KEY_SSID).equals(Keys.KEY_REVERSED) && row.get(Keys.KEY_BSSID).equals(Keys.KEY_REVERSED)) {
                    do_reverse = true;
                    continue;
                }

                if (do_reverse) {
                    reverse.add(row);
                } else {
                    forward.add(row);
                }

            }
            Log.i(TAG, "forward and reverse combined: " + (forward.size() + reverse.size()));
            plotData(forward, false);
            plotData(reverse, true);

            // parse out the data


            // TODO: How about a Header for splitting by keys to get a constant runtime method
//            MapTools.separateByKeys(dataBaseManager.getTableData(tableName), AppConfig.ALL_SSIDS, Keys.KEY_SSID);

        }

    }

    float AQ_SIZE = 140f;

    void plotData(ArrayList<HashMap<String, Object>> data, boolean isReversed) {

        long startT = Long.parseLong(String.valueOf(data.get(0).get(Keys.KEY_TIME)));
        long endT = Long.parseLong(String.valueOf(data.get(data.size() - 1).get(Keys.KEY_TIME)));

        float totalSeconds = (endT - startT) / 1000f;
        float scaleFactor = AQ_SIZE / totalSeconds;

        PointF point = new PointF(196, 60);

        for (HashMap<String, Object> row : data) {

            float pos = ((endT - Long.parseLong(String.valueOf(row.get(Keys.KEY_TIME)))) / 1000f) * scaleFactor;

            if (isReversed) {
                pos = AQ_SIZE - pos;
            }

            point.y = 60 + pos;

            Log.i(TAG, "pointY: " + point.y);

            if (Integer.parseInt(String.valueOf(row.get(Keys.KEY_ROWID))) % 100 == 0) {
                MapTools.addMarker(mMap, MercatorProjection.fromPointToLatLng(point), row.get(Keys.KEY_SSID).toString(), "d");
            }

        }
    }
}
