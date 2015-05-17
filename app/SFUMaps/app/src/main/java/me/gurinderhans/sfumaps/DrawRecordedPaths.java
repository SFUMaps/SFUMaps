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
    "startPoint": "167.19162, 86.0535"
    "endPoint": "167.19162, 169.8098"
    AQ = 79.37495 units long
     */


    public static final String TAG = DrawRecordedPaths.class.getSimpleName();

    DataBaseManager dataBaseManager;
    GoogleMap mMap;

    static HashMap<String, ArrayList<HashMap<String, Object>>> allAPs = new HashMap<>();

    public DrawRecordedPaths(Context ctx, GoogleMap map) {
        this.mMap = map;

        dataBaseManager = new DataBaseManager(ctx);

        for (String tableName : dataBaseManager.getTableNames()) {

            ArrayList<HashMap<String, Object>> forward = new ArrayList<>();
            ArrayList<HashMap<String, Object>> reverse = new ArrayList<>();

            boolean do_reverse = false;

            for (HashMap<String, Object> row : dataBaseManager.getTableData(tableName)) {
                if (row.get(Keys.KEY_SSID).equals(Keys.KEY_REVERSED) && row.get(Keys.KEY_BSSID).equals(Keys.KEY_REVERSED)) {
                    do_reverse = true; // if true we are done the normal way and should start adding to the reverse row
                    continue;
                }

                if (do_reverse) {
                    reverse.add(row);
                } else {
                    forward.add(row);
                }

            }

            // plotting needs to happen differently
            plotData(forward, false);
            plotData(reverse, true);



            // after the plotting we can combine both arrays and split by bssids to make searching for a point easier

            // split forward and reverse by BSSID
            ArrayList<HashMap<String, Object>> combinedAPList = new ArrayList<>();
            combinedAPList.addAll(forward);
            combinedAPList.addAll(reverse);

            allAPs = MapTools.separateByKeys(combinedAPList, Keys.KEY_BSSID);

            Log.i(TAG, allAPs.size()+"");




            // TODO: Make a Header in the table for splitting by keys to get a constant runtime

        }

    }

    float AQ_SIZE = 83f;

    // computes the points for each access point
    // FIXME: we could probably do this before-hand unless we're going the "machine learning way" where it learns every time it is run
    void plotData(ArrayList<HashMap<String, Object>> data, boolean isReversed) {

        long startT = Long.parseLong(String.valueOf(data.get(0).get(Keys.KEY_TIME)));
        long endT = Long.parseLong(String.valueOf(data.get(data.size() - 1).get(Keys.KEY_TIME)));

        float totalSeconds = (endT - startT) / 1000f;
        float scaleFactor = AQ_SIZE / totalSeconds;

        PointF point = new PointF(168, 88);

        for (HashMap<String, Object> row : data) {

            float pos = ((endT - Long.parseLong(String.valueOf(row.get(Keys.KEY_TIME)))) / 1000f) * scaleFactor;

            if (isReversed) {
                pos = AQ_SIZE - pos;
            }

            point.y = 88 + pos;

//            Log.i(TAG, "pointY: " + point.y);
//            Log.i(TAG, "data: " + row);

            row.put(Keys.KEY_POINT, point);

            if (Integer.parseInt(String.valueOf(row.get(Keys.KEY_ROWID))) % 200 == 0) {
                MapTools.addMarker(mMap, MercatorProjection.fromPointToLatLng(point), row.get(Keys.KEY_SSID).toString(), "d");
            }

        }
    }
}
