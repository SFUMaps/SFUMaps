package me.gurinderhans.sfumaps;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

/**
 * Created by ghans on 2/9/15.
 */
public class MapTools {

    // TODO: Class under construction

    // empty constructor
    private MapTools() {
        /* To make sure this class cannot be instantiated */
    }

    /**
     * @param dataArray - the data array that we are splitting by keys
     * @param keys      - the key(s) that we split the data by
     * @param keyIndex  - the index of the key in that array object
     * @return - return the [Array] separated by keys
     */
    public static HashMap<String, ArrayList<HashMap<String, Object>>> separateByKeys(ArrayList<HashMap<String, Object>> dataArray, Set<String> keys, String keyIndex) {

        HashMap<String, ArrayList<HashMap<String, Object>>> seperated = new HashMap<>();

        for (String key : keys) {
            seperated.put(key, new ArrayList<HashMap<String, Object>>());
        }

        for (HashMap<String, Object> i : dataArray) {
            if(seperated.keySet().contains(i.get(keyIndex)))
                seperated.get(i.get(keyIndex)).add(i);
        }

        return seperated;
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
}
