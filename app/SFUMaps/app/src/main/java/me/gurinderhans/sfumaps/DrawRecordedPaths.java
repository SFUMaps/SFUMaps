package me.gurinderhans.sfumaps;

import android.content.Context;
import android.graphics.PointF;

import com.google.android.gms.maps.GoogleMap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import me.gurinderhans.sfumaps.mapsystem.Campus;

/**
 * Created by ghans on 1/26/15.
 */
public class DrawRecordedPaths {

    /*
     * NOTE for the aop
     * anything going vertical will be called a "Street"
     * likewise anything going horizontal is a "Avenue"
     * - Things like the AQ are "States" / "Provinces"
     * - "SFU Burnaby" is a Country
     */

    /**
     * -----------------------------
     * Table Name Scheme
     * -----------------------------
     * = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = =
     * 1. prefix = 'apsdata_'
     * -
     * 2. location general name -> ex. AQ, TASC1, ASB
     * -
     * 3. location specific name generalName_ [ {North, South, East, West}, {Lvl9_Far} ]
     * -
     * 4. floor level (M = main) and (M+n) for floors above M and (M-n) for floors below M - AQ 3000 is considered as floor M
     * -
     * 5. direction = VR (vertical) | HR (horizontal) or in rare cases 'CSTNA' ( custom n/a )
     * -
     * ==> tableName = '{0}_{1}_{2}_{3}_{4}' % (prefix, locationGeneralName, locationSpecificName, floor level, direction)
     * -
     * i.e. -> 'apsdata_AQ_North_M_HR'
     * i.e  -> 'apsdata_AQ_Lvl9_Near_M_HR'
     */

    /* NOTE: only draw recorded paths of where the user is currently at not the whole campus at runtime

    - It's just good memory wise for app performance because android apps are sandboxed
     given their own little memory and other resources
    **/


    public static final String TAG = DrawRecordedPaths.class.getSimpleName();

    public static final int AQ_SIZE = 140;

    boolean DEBUG = false; // presumingly this would enable and disable aps markers
    DataBaseManager mDataBaseManager;
    GoogleMap mMap;

    HashMap<String, ArrayList<HashMap<String, Object>>> separatedData;
    ArrayList<HashMap<String, Object>> combinedList;

    public DrawRecordedPaths(boolean debugState, Context ctx, GoogleMap map) {
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
