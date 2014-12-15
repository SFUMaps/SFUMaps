package me.gurinderhans.wifirecorder;

import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;


public class TableData extends Activity {

    public final String TAG = getClass().getSimpleName();

    WiFiDatabaseManager mWifiDatabaseManager;

    int GOOD_RSSI_VAL = -65;

    String[] WIFIS = {"SFUNET", "SFUNET-SECURE", "eduroam"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_table_data);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) getActionBar().setElevation(0);

        String tableName = getIntent().getExtras().getString(TablesListActivity.KEY_TABLE_NAME);
        getActionBar().setTitle(tableName);

        mWifiDatabaseManager = new WiFiDatabaseManager(this);

        ArrayList<HashMap<String, String>> allData = new ArrayList<>();

        ArrayList<ArrayList<HashMap<String, String>>> tmpList = filterAPs(mWifiDatabaseManager.getTableData(tableName));

        for (ArrayList<HashMap<String, String>> d : tmpList) {
            Collections.sort(d, new SortByTime(WiFiDatabaseManager.KEY_TIME));
            allData.addAll(d);
        }

        int[] ids = {R.id.ssid, R.id.bssid, R.id.freq, R.id.level};
        String[] keys = {WiFiDatabaseManager.KEY_SSID, WiFiDatabaseManager.KEY_BSSID, WiFiDatabaseManager.KEY_FREQ, WiFiDatabaseManager.KEY_RSSI};

        SimpleAdapter mSimpleAdapter = new SimpleAdapter(getApplicationContext(), allData, R.layout.wifiap, keys, ids);

        ListView tableDataListView = (ListView) findViewById(R.id.tableDataListView);
        tableDataListView.setAdapter(mSimpleAdapter);

    }


    public ArrayList<ArrayList<HashMap<String, String>>> filterAPs(ArrayList<HashMap<String, String>> data) {

        ArrayList<ArrayList<HashMap<String, String>>> allWifisData = new ArrayList<>();

        for (String wifi : WIFIS) {

            ArrayList<HashMap<String, String>> ssidData = new ArrayList<>();

            ArrayList<HashMap<String, String>> filteredData = new ArrayList<>();

            //get current ssid rows
            for (HashMap<String, String> hashMap : data) { // ?remove this wifi from data to make data smaller?

                if (hashMap.get(WiFiDatabaseManager.KEY_SSID).equals(wifi)) {
                    ssidData.add(hashMap);
                }
            }

            for (HashMap<String, String> hashMap : getStrongestBssids(ssidData)) {

                if (Integer.parseInt(hashMap.get(WiFiDatabaseManager.KEY_RSSI)) > GOOD_RSSI_VAL) {
                    filteredData.add(hashMap);
                }
            }

            allWifisData.add(filteredData);
        }

        return allWifisData;
    }

    public ArrayList<HashMap<String, String>> getStrongestBssids(ArrayList<HashMap<String, String>> d) {


        Collections.sort(d, new SortByRSSI(WiFiDatabaseManager.KEY_RSSI));

        for (int i = 0; i < d.size(); i++) {
            HashMap<String, String> compareTo = d.get(i);
            for (int j = i + 1; j < d.size() - 1; j++) {
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