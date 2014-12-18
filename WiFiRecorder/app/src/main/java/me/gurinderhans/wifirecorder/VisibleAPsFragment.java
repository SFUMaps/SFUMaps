package me.gurinderhans.wifirecorder;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 * Created by ghans on 14-12-17.
 */
public class VisibleAPsFragment extends Fragment {

    public static final String TAG = VisibleAPsFragment.class.getSimpleName();

    public static final String KEY_RSSI_DIFFERENCE = "rssi_diff";
    public static final String KEY_RECORDED_VAL = "recorded_val";
    public static final int GOOD_RSSI_VAL = -65;
    public static ArrayList<String> ALL_SSIDS;
    Context context;

    WiFiDatabaseManager mWifiDatabaseManager;
    SimpleAdapter mSimpleAdapter;

    ArrayList<HashMap<String, String>> allData, matchingSignalsPickedUp;

    String tableName;

    WifiManager wifiManager;
    Handler mHandler;
    Runnable compareRunnable;


    public VisibleAPsFragment newInstance(String tblName) {
        VisibleAPsFragment fragment = new VisibleAPsFragment();
        Bundle args = new Bundle();
        args.putString(WiFiDatabaseManager.KEY_TABLE_NAME, tblName);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.visible_aps_fragment, container, false);

        setHasOptionsMenu(true);

        ALL_SSIDS = new ArrayList<>(Arrays.asList("8EA535", "SFUNET", "SFUNET-SECURE", "eduroam"));

        context = getActivity().getApplicationContext();

        mWifiDatabaseManager = new WiFiDatabaseManager(context);

        tableName = this.getArguments().getString(WiFiDatabaseManager.KEY_TABLE_NAME, "null");

        allData = new ArrayList<>();
        matchingSignalsPickedUp = new ArrayList<>();


        wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);

        int[] ids = {R.id.ssid, R.id.bssid, R.id.freq, R.id.level, R.id.rssi_diff, R.id.recorded_val};
        String[] keys = {WiFiDatabaseManager.KEY_SSID, WiFiDatabaseManager.KEY_BSSID, WiFiDatabaseManager.KEY_FREQ, WiFiDatabaseManager.KEY_RSSI, KEY_RSSI_DIFFERENCE, KEY_RECORDED_VAL};

        mSimpleAdapter = new SimpleAdapter(context, matchingSignalsPickedUp, R.layout.wifi_ap_comparer, keys, ids);

        ListView tableDataListView = (ListView) rootView.findViewById(R.id.visibleTableDataListView);
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
                        map.put(KEY_RSSI_DIFFERENCE, "Difference: " + Math.abs(Math.abs(recordedVal) - Math.abs(newVal)) + "");
                        map.put(KEY_RECORDED_VAL, "Recorded RSSI: " + recordedVal + "");
                        map.put(WiFiDatabaseManager.KEY_RSSI, "Current RSSI: " + newVal);
                        matchingSignalsPickedUp.add(map);
                    }
                }

                //show the rssi diff in listview
                mSimpleAdapter.notifyDataSetChanged();


                mHandler.postDelayed(this, 1000);
            }
        };

        showData(ALL_SSIDS);


        return rootView;
    }


    public void showData(ArrayList<String> wifis) {
        ArrayList<ArrayList<HashMap<String, String>>> tmpList = ComplexFunctions.filterAPs(mWifiDatabaseManager.getTableData(tableName), wifis);
        allData.clear();


        for (ArrayList<HashMap<String, String>> d : tmpList) {
            Collections.sort(d, new SortByTime(WiFiDatabaseManager.KEY_TIME));
            allData.addAll(d);
        }

        mHandler = new Handler();
        mHandler.removeCallbacks(compareRunnable);
        mHandler.postDelayed(compareRunnable, 100);
    }


    public void ssidSelectorDialog() {
        final String[] ssid_options = {"SFUNET", "SFUNET-SECURE", "eduroam"};
        // arraylist to keep the selected items
        final ArrayList<Integer> selectedIndicies = new ArrayList<>();

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
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
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.select_wifi_ssid) {
            ssidSelectorDialog();
        }

        return false;
    }
}
