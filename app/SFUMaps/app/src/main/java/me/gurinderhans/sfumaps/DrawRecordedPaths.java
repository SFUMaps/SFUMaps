package me.gurinderhans.sfumaps;

import android.content.Context;
import android.graphics.PointF;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.Arrays;
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


    /* only draw recorded paths of where the user is currently at not the whole campus at runtime d*/



    public static final String TAG = DrawRecordedPaths.class.getSimpleName();

    boolean DEBUG = false;

    DataBaseManager mDataBaseManager;

    GoogleMap mMap;

    public DrawRecordedPaths(boolean debug, Context ctx, GoogleMap map) {
        this.DEBUG = debug;
        this.mDataBaseManager = new DataBaseManager(ctx);
        this.mMap = map;

        PointF initPoint = new PointF(60f, 60f);

        for (String table : mDataBaseManager.getTables()) drawData_AQ(table, initPoint);

    }

    public void drawData_AQ(String table, PointF initPoint) {

        String[] table_S = Arrays.copyOfRange(table.split("_"), 1, 5); // remove the `apsdata` prefix
        String direction = table_S[3]; //vertical or horizontal
        String floorLevel = table_S[2]; //floor level, worry about this later

        ArrayList<HashMap<String, String>> tableData = mDataBaseManager.getTableData(table);

        float mvDiff = 0f;
        for(ArrayList<HashMap<String, String>> aps: seperateBySSID(tableData)){
            String thisSSID = aps.get(0).get(DataBaseManager.KEY_SSID);
            for (int i = 1; i < aps.size(); i++) {
                PointF point = new PointF(0f, 0f);
                if (direction.equals("VR")) {
                    if (table_S[1].equals("East"))
                        point.x = AppConstants.TILE_SIZE - initPoint.x + 5;
                    else point.x = initPoint.x;

                    // current-index - 1 and aps.size() - 1 because index 0 contains ssid name
                    point.y = initPoint.y + ( (i-1) * (AppConstants.AQ_SIZE / ((aps.size()-1) - 1)));

                } else {
                    point.x = initPoint.x + ( (i-1) * (AppConstants.AQ_SIZE / ((aps.size()-1) - 1)));

                    if (table_S[1].equals("South"))
                        point.y = AppConstants.TILE_SIZE - initPoint.y + 5;
                    else point.y = initPoint.y;
                }

                point.x += mvDiff;
                point.y += mvDiff;

                addMarker(MapTools.fromPointToLatLng(point), thisSSID, table_S[1]);
            }
            mvDiff -= 2;
        }
    }

    public void addMarker(LatLng latLng, String ssid, String dir) {

        int icon;
        if (ssid.equals(AppConstants.ALL_SSIDS[0])) icon = R.drawable.sfunetdot;
        else if (ssid.equals(AppConstants.ALL_SSIDS[1])) icon = R.drawable.sfunetsecuredot;
        else if (ssid.equals(AppConstants.ALL_SSIDS[2])) icon = R.drawable.eduroamdot;
        else icon = R.drawable.routerdot;

        mMap.addMarker(new MarkerOptions()
                .position(latLng)
                .title(dir)
                .snippet(ssid)
                .icon(BitmapDescriptorFactory.fromResource(icon)));
    }

    public ArrayList<ArrayList<HashMap<String, String>>> seperateBySSID(ArrayList<HashMap<String, String>> d) {

        ArrayList<ArrayList<HashMap<String, String>>> splittedData = new ArrayList<>();

        for (String ssid : AppConstants.ALL_SSIDS) {
            ArrayList<HashMap<String, String>> tmp = new ArrayList<>();
            for (HashMap<String, String> ap : d) {
                if (ap.get(DataBaseManager.KEY_SSID).equals(ssid))
                    tmp.add(ap);

            }

            HashMap<String, String> apSSID = new HashMap<>();
            apSSID.put(DataBaseManager.KEY_SSID, ssid);
            tmp.add(0, apSSID);

            splittedData.add(tmp);
        }

        return splittedData;
    }

}
