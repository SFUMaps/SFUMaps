package me.gurinderhans.sfumaps;

import android.graphics.PointF;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

/**
 * Created by ghans on 2/9/15.
 */
public class MapTools {

    public static final String TAG = MapTools.class.getSimpleName();

    // TODO: Class under construction

    // empty constructor
    private MapTools() {
        /* To make sure this class cannot be instantiated */
    }

    /**
     * @param dataArray - the data array that we are splitting by keys
     * @param keyIndex  - the index of the key in that array object
     * @return - return the [Array] separated by keys
     */
    public static HashMap<String, ArrayList<HashMap<String, Object>>> separateByKeys(ArrayList<HashMap<String, Object>> dataArray, String keyIndex) {

        HashMap<String, ArrayList<HashMap<String, Object>>> separated = new HashMap<>();

        for (HashMap<String, Object> row : dataArray) {

            String key = (String) row.get(keyIndex);

            if (!separated.containsKey(key)) {
                separated.put(key, new ArrayList<HashMap<String, Object>>());
            }

            separated.get(key).add(row);
        }

        return separated;
    }


    /**
     * @param input - input data to remove dups from
     */
    public static void removeDups(ArrayList<HashMap<String, Object>> input) {

        Collections.sort(input, new Comparator<HashMap<String, Object>>() {
            @Override
            public int compare(HashMap<String, Object> lhs, HashMap<String, Object> rhs) {
                int firstValue = Integer.parseInt(lhs.get(Keys.KEY_RSSI).toString());
                int secondValue = Integer.parseInt(rhs.get(Keys.KEY_RSSI).toString());
                return (secondValue < firstValue ? -1 : (secondValue == firstValue ? 0 : 1));
            }
        });

        int count = input.size();

        // ___
        for (int i = 0; i < count; i++) {
            for (int j = i + 1; j < count; j++) {
                HashMap<String, Object> a = input.get(i);
                HashMap<String, Object> b = input.get(j);

                boolean ssid = a.get(Keys.KEY_SSID).equals(b.get(Keys.KEY_SSID));
                boolean bssid = a.get(Keys.KEY_BSSID).equals(b.get(Keys.KEY_BSSID));

                if (ssid && bssid) {
                    input.remove(j--);
                    count--;
                }
            }
        }
    }


    /**
     * @param googleMap - the map
     * @param latLng    - latlng coordinate on the map
     * @param ssid      - SSID name for the label
     * @param dir       - direction?
     */
    public static void addMarker(GoogleMap googleMap, LatLng latLng, String ssid, String dir) {

        googleMap.addMarker(new MarkerOptions()
                .position(latLng)
                .title(dir)
                .snippet(ssid)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.routerdot)));
    }


    /**
     * @param points - list of points
     * @return - return the centroid point (mean value)
     */
    public static PointF getCentroid(ArrayList<PointF> points) {

        float Sx = 0, Sy = 0;

        for (PointF point : points) {
            Sx += point.x;
            Sy += point.y;
        }

        Sx /= points.size();
        Sy /= points.size();


        return new PointF(Sx, Sy);
    }
}
