package me.gurinderhans.wifirecorder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

/**
 * Created by ghans on 14-12-17.
 */
public class ComplexFunctions {

    public static final String TAG = ComplexFunctions.class.getSimpleName();
    public static final int RSSI_THRESHOLD = -65;

    /**
     * @param in_data             - all raw data inputted into this
     * @param selected_ssid_names - a list of wifi SSIDS to filter out unwanted aps
     * @return - return filtered / usable data
     */
    public static ArrayList<ArrayList<HashMap<String, String>>> filterAPs(ArrayList<HashMap<String, String>> in_data, ArrayList<String> selected_ssid_names) {

        ArrayList<ArrayList<HashMap<String, String>>> allWifisData = new ArrayList<>();

        for (String wifi : selected_ssid_names) {

            ArrayList<HashMap<String, String>> currentSSIDData = new ArrayList<>();
            ArrayList<HashMap<String, String>> filteredData = new ArrayList<>();

            //get current ssid data and put into @param - currentSSIDData
            for (HashMap<String, String> hashMap : in_data) {
                /** @see - remove this wifi from data to make data smaller ?? */

                if (hashMap.get(WiFiDatabaseManager.KEY_SSID).equals(wifi))
                    currentSSIDData.add(hashMap);

            }

            // once current SSID data is run through getStrongestBSSIDs() filter this data with RSSI_THRESHOLD
            for (HashMap<String, String> hashMap : getStrongestBSSIDs(currentSSIDData)) {

                if (Integer.parseInt(hashMap.get(WiFiDatabaseManager.KEY_RSSI)) > (RSSI_THRESHOLD))
                    filteredData.add(hashMap);

            }

            //finally add this filtered data ArrayList to a parent array list
            allWifisData.add(filteredData);
        }

        return allWifisData;
    }

    /**
     * @param d - array-list of a single wifi ssid with duplicate APs
     *          we sort the input data based on RSSI values for the APs and then
     *          remove the duplicate APs with RSSI values weaker than others in the list
     * @return - return the unique APs list
     */
    public static ArrayList<HashMap<String, String>> getStrongestBSSIDs(ArrayList<HashMap<String, String>> d) {

        Collections.sort(d, new SortByRSSI(WiFiDatabaseManager.KEY_RSSI));

        for (int i = 0; i < d.size(); i++) {
            HashMap<String, String> compareTo = d.get(i);
            for (int j = i + 1; j < d.size(); j++) {
                HashMap<String, String> comparing = d.get(j);

                String compareToStr = compareTo.get(WiFiDatabaseManager.KEY_BSSID);
                String comparingStr = comparing.get(WiFiDatabaseManager.KEY_BSSID);

                boolean compareResult = compareToStr.equals(comparingStr);

                if (compareResult) {
                    d.remove(j);
                    j -= 1;
                }

            }
        }

        return d;
    }
}

class SortByRSSI implements Comparator<HashMap<String, String>> {
    private final String key;

    public SortByRSSI(String key) {
        this.key = key;
    }

    public int compare(HashMap<String, String> first, HashMap<String, String> second) {
        int firstValue = Integer.parseInt(first.get(key));
        int secondValue = Integer.parseInt(second.get(key));
        return (secondValue < firstValue ? -1 : (secondValue == firstValue ? 0 : 1));
    }
}

class SortByTime implements Comparator<HashMap<String, String>> {
    private final String key;

    public SortByTime(String key) {
        this.key = key;
    }

    public int compare(HashMap<String, String> first, HashMap<String, String> second) {
        long firstValue = Long.parseLong(first.get(key));
        long secondValue = Long.parseLong(second.get(key));
        return (firstValue < secondValue ? -1 : (firstValue == secondValue ? 0 : 1));
    }
}

