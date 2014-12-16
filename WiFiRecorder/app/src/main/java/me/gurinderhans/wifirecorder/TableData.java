package me.gurinderhans.wifirecorder;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;


public class TableData extends Activity {

    public final String TAG = getClass().getSimpleName();

    public static final String KEY_RSSI_DIFFERENCE = "rssi_diff";
    public static final String KEY_RECORDED_VAL = "recorded_val";

    int GOOD_RSSI_VAL = -65;
    ArrayList<String> ALL_SSIDS = new ArrayList<>();

    WiFiDatabaseManager mWifiDatabaseManager;
    SimpleAdapter mSimpleAdapter;
    ArrayList<HashMap<String, String>> allData;
    ArrayList<HashMap<String, String>> matchingSignalsPickedUp = new ArrayList<>();
    String tableName;



    WifiManager wifiManager;
    Handler mHandler;

    Runnable compareRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_table_data);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) getActionBar().setElevation(0);

        ALL_SSIDS.add("8EA535");
//        ALL_SSIDS.add("SFUNET");
//        ALL_SSIDS.add("SFUNET-SECURE");
//        ALL_SSIDS.add("eduroam");

        wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);


        tableName = getIntent().getExtras().getString(TablesListActivity.KEY_TABLE_NAME);
        getActionBar().setTitle(tableName);

        mWifiDatabaseManager = new WiFiDatabaseManager(this);

        allData = new ArrayList<>();


        int[] ids = {R.id.ssid, R.id.bssid, R.id.freq, R.id.level, R.id.rssi_diff, R.id.recorded_val};
        String[] keys = {WiFiDatabaseManager.KEY_SSID, WiFiDatabaseManager.KEY_BSSID, WiFiDatabaseManager.KEY_FREQ, WiFiDatabaseManager.KEY_RSSI, KEY_RSSI_DIFFERENCE, KEY_RECORDED_VAL};

//                                                        change to matchingSignalsPickedUp allData to view matched aps
        mSimpleAdapter = new SimpleAdapter(getApplicationContext(), matchingSignalsPickedUp, R.layout.wifi_ap_comparer, keys, ids);

        ListView tableDataListView = (ListView) findViewById(R.id.tableDataListView);
        tableDataListView.setAdapter(mSimpleAdapter);

        compareRunnable = new Runnable() {
            @Override
            public void run() {
                matchingSignalsPickedUp.clear();
                List<ScanResult> wifiAPs = wifiManager.getScanResults();

                ArrayList<HashMap<String, String>> scanResults = new ArrayList<>();


                for (ScanResult result : wifiAPs) {
                    HashMap<String, String> ap = new HashMap<>();
                    ap.put(WiFiDatabaseManager.KEY_SSID, result.SSID);
                    ap.put(WiFiDatabaseManager.KEY_BSSID, result.BSSID);
                    ap.put(WiFiDatabaseManager.KEY_FREQ, result.frequency + " MHz");
                    ap.put(WiFiDatabaseManager.KEY_RSSI, result.level + "");
                    ap.put(WiFiDatabaseManager.KEY_TIME, System.currentTimeMillis() + "");

                    scanResults.add(ap);
                }

                int min = Math.min(allData.size(), scanResults.size());

                for (int i = 0; i < min; i++) {
                    if (allData.get(i).get(WiFiDatabaseManager.KEY_BSSID).equals(scanResults.get(i).get(WiFiDatabaseManager.KEY_BSSID))) {
                        HashMap<String, String> map = scanResults.get(i);
                        int recordedVal = Integer.parseInt(allData.get(i).get(WiFiDatabaseManager.KEY_RSSI));
                        int newVal = Integer.parseInt(scanResults.get(i).get(WiFiDatabaseManager.KEY_RSSI));
                        map.put(KEY_RSSI_DIFFERENCE, "Difference: "+Math.abs(Math.abs(recordedVal) - Math.abs(newVal))+"");
                        map.put(KEY_RECORDED_VAL, "Recorded RSSI: "+recordedVal+"");
                        map.put(WiFiDatabaseManager.KEY_RSSI, "Current RSSI: "+newVal);
                        matchingSignalsPickedUp.add(map);
                    }
                }

                //show the rssi diff in listview
                mSimpleAdapter.notifyDataSetChanged();


                mHandler.postDelayed(this, 1000);
            }
        };

        showData(ALL_SSIDS);

    }


    public void showData(ArrayList<String> wifis) {
        ArrayList<ArrayList<HashMap<String, String>>> tmpList = filterAPs(mWifiDatabaseManager.getTableData(tableName), wifis);
        allData.clear();

        for (ArrayList<HashMap<String, String>> d : tmpList) {
            Collections.sort(d, new SortByTime(WiFiDatabaseManager.KEY_TIME));
            allData.addAll(d);
        }
//        mSimpleAdapter.notifyDataSetChanged();

        mHandler = new Handler();
        mHandler.removeCallbacks(compareRunnable);
        mHandler.postDelayed(compareRunnable, 100);
    }


    public ArrayList<ArrayList<HashMap<String, String>>> filterAPs(ArrayList<HashMap<String, String>> data, ArrayList<String> fromWifis) {

        ArrayList<ArrayList<HashMap<String, String>>> allWifisData = new ArrayList<>();

        for (String wifi : fromWifis) {

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

    public void ssidSelectorDialog() {
        final String[] ssid_options = {"SFUNET", "SFUNET-SECURE", "eduroam"};
        // arraylist to keep the selected items
        final ArrayList<Integer> selectedIndicies = new ArrayList<>();

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select SSID(s)");
        builder.setMultiChoiceItems(ssid_options, null, new DialogInterface.OnMultiChoiceClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int indexSelected,
                                boolean isChecked) {
                if (isChecked) {
                    // If the user checked the item, add it to the selected items
                    selectedIndicies.add(indexSelected);
                } else if (selectedIndicies.contains(indexSelected)) {
                    // Else, if the item is already in the array, remove it
                    selectedIndicies.remove(Integer.valueOf(indexSelected));
                }
            }
        }).setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                ArrayList<String> selectedWifis = new ArrayList<>();

                for (int i : selectedIndicies) selectedWifis.add(ssid_options[i]);

                showData(selectedWifis);

            }
        });

        builder.create().show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_table_data, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.select_wifi_ssid) {
            ssidSelectorDialog();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacks(compareRunnable);
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