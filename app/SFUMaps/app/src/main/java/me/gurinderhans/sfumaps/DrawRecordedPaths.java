package me.gurinderhans.sfumaps;

import android.content.Context;
import android.graphics.PointF;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by ghans on 1/26/15.
 */
public class DrawRecordedPaths {

    /**
     * -----------------------------
     * |  Current Table Name Scheme  |
     * -----------------------------
     * = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = =
     * <p>
     * 1. globalprefix = 'apsdata_'
     * <p/>
     * 2. location general name -> ex. AQ, TASC1, ASB
     * <p>
     * 3. location specific name generalName_ [ {North, South, East, West}, {Lvl9_Far} ]
     * <p/>
     * 4. floor level (M = main) and (M+n) for floors above M and (M-n) for floors below M - AQ 3000 is considered as floor M
     * <p>
     * 5. direction = VR (vertical) | HR (horizontal) or in rare cases CSTNA ( custom n/a )
     * <p>
     * ==> tableName = '{0}_{1}_{2}_{3}_{4}' % (globaleprefix, locationGeneralName, locationSpecificName, floor level, direction)
     * <p/>
     * i.e. -> 'apsdata_AQ_North_M_HR'
     */


    /* only draw recorded paths of where the user is currently at not the whole campus at runtime */


    public static final String TAG = DrawRecordedPaths.class.getSimpleName();
    public static final int AQ_SIZE = 140;

    boolean DEBUG = false;
    DataBaseManager mDataBaseManager;
    GoogleMap mMap;

    HashMap<String, ArrayList<HashMap<String, Object>>> seperatedData;

    public DrawRecordedPaths(boolean debugState, Context ctx, GoogleMap map) {
        this.DEBUG = debugState;
        this.mDataBaseManager = new DataBaseManager(ctx);
        this.mMap = map;

        for (String table : mDataBaseManager.getTables()) {
            if (!table.equals("apsdata_AQ_East_M_VR")) continue;
            seperatedData = MapTools.seperateByKeys(mDataBaseManager.getTableData(table), AppConstants.ALL_SSIDS, DataBaseManager.KEY_SSID);
            plotData(seperatedData);
        }

    }

    void plotData(HashMap<String, ArrayList<HashMap<String, Object>>> data) {

        for (String key : data.keySet()) {
            for (int i = 0; i < data.get(key).size(); i++) {
                HashMap<String, Object> dataRow = data.get(key).get(i);
                PointF point = new PointF(196, 60);

                point.y += (i * (AQ_SIZE / (data.get(key).size() - 1.03)));
                dataRow.put(DataBaseManager.KEY_POINT, point);

                addMarker(MapTools.fromPointToLatLng(point),
                        (String) dataRow.get(DataBaseManager.KEY_SSID),
                        (String) dataRow.get(DataBaseManager.KEY_BSSID));
            }
        }
    }


    public void addMarker(LatLng latLng, String ssid, String dir) {

        mMap.addMarker(new MarkerOptions()
                .position(latLng)
                .title(dir)
                .snippet(ssid)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.routerdot)));
    }

}
