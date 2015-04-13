package me.gurinderhans.sfumaps;

import android.content.Context;
import android.graphics.PointF;

import com.google.android.gms.maps.GoogleMap;

import java.util.ArrayList;
import java.util.HashMap;

import me.gurinderhans.sfumaps.mapsystem.Campus;

/**
 * Created by ghans on 1/26/15.
 */
public class DrawRecordedPaths {

    /*  University
        |--Campuses
           |--Buildings
              |--Floors
                 |--Individual Roads (Streets and Avenues)
     */

    /**
     * -------------------------------
     * Table Name Scheme for wifi data
     * -------------------------------
     * = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = =
     * 1. prefix = 'apsdata_'
     * -
     * 2. university name -> ex. SFU
     * -
     * 3. university campus name -> ex. SFU {Burnaby, Surrey, etc...}
     * -
     * 4. building name -> ex. AQ, TASC1, ASB
     * -
     * 5. floor level -> ex. 3000 at AQ or 9000 at TASC1
     * -
     * 6. pathway name (road) -> {North, South} in terms of AQ or Lvl9_Far in terms of TASC1
     * -
     * 7. direction -> is the path vertical or horizontal? : Street = Vertical, Avenue = Horizontal or SP = at some angle
     * -
     * ==> tableName = '{1}_{2}_{3}_{4}_{5}_{6}_{7}' % (prefix, universityName, campusName, buildingName, floorLevel, pathwayName, direction)
     * -
     * i.e. -> 'apsdata_SFU_BURNABY_AQ_3000_North_Avenue'
     * i.e  -> 'apsdata_SFU_BURNABY_TASC1_9000_Near_Avenue'
     */


    /* NOTE: only draw recorded paths of where the user is currently at not the whole campus at runtime
    - It's just good memory wise for app performance because android apps are sand-boxed
     given their own little memory and other resources
    **/


    public static final String TAG = DrawRecordedPaths.class.getSimpleName();


    boolean DEBUG = false; // presumingly this would enable and disable aps markers
    DataBaseManager mDataBaseManager;
    GoogleMap mMap;

    HashMap<String, ArrayList<HashMap<String, Object>>> separatedData;
    ArrayList<HashMap<String, Object>> combinedList;

    public static final int AQ_SIZE = 140;

    public DrawRecordedPaths(Context ctx, GoogleMap map, boolean debugState) {
        this.DEBUG = debugState;
        this.mDataBaseManager = new DataBaseManager(ctx);
        this.mMap = map;

        combinedList = new ArrayList<>();

        new Campus("SFU Burnaby");

        for (String table : mDataBaseManager.getTableNames()) {
            if (!table.equals("apsdata_AQ_East_M_VR")) continue;

            // TODO: How about a Header for splitting by keys to get a constant runtime method
            separatedData = MapTools.separateByKeys(mDataBaseManager.getTableData(table), AppConfig.ALL_SSIDS, Keys.KEY_SSID);
            plotData(separatedData);
        }
    }

    void plotData(HashMap<String, ArrayList<HashMap<String, Object>>> data) {

        for (String key : data.keySet()) {
            for (int i = 0; i < data.get(key).size(); i++) {
                HashMap<String, Object> dataRow = data.get(key).get(i);
                PointF point = new PointF(196, 60);

                point.y += (i * (AQ_SIZE / (data.get(key).size() - 1.03)));
                dataRow.put(Keys.KEY_POINT, point);

                MapTools.addMarker(mMap, MercatorProjection.fromPointToLatLng(point),
                        (String) dataRow.get(Keys.KEY_SSID),
                        (String) dataRow.get(Keys.KEY_BSSID));
            }
        }

        // maybe wont need this if we use id from database
        for (String key : separatedData.keySet()) {
            for (HashMap<String, Object> row : separatedData.get(key)) {
                combinedList.add(row);
            }
        }
    }
}
