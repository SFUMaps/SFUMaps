package me.gurinderhans.wifirecorder;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

public class RecordedAPsFragment extends Fragment {

    private final String TAG = getClass().getSimpleName();

    Context context;

    WiFiDatabaseManager mWifiDatabaseManager;
    SimpleAdapter mSimpleAdapter;
    String tableName;
    private ArrayList<HashMap<String, String>> allData;

    public RecordedAPsFragment newInstance(String tblName) {
        RecordedAPsFragment fragment = new RecordedAPsFragment();
        Bundle args = new Bundle();
        args.putString(WiFiDatabaseManager.KEY_TABLE_NAME, tblName);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_recorded_aps, container, false);
        setHasOptionsMenu(true);

        tableName = this.getArguments().getString(WiFiDatabaseManager.KEY_TABLE_NAME, "null");

        context = getActivity().getApplicationContext();
        mWifiDatabaseManager = new WiFiDatabaseManager(context);
        allData = new ArrayList<>();

        int[] ids = {R.id.ssid, R.id.bssid, R.id.freq, R.id.level};
        String[] keys = {WiFiDatabaseManager.KEY_SSID, WiFiDatabaseManager.KEY_BSSID, WiFiDatabaseManager.KEY_FREQ, WiFiDatabaseManager.KEY_RSSI};

        mSimpleAdapter = new SimpleAdapter(context, allData, R.layout.lv_item_wifiap, keys, ids);

        ListView tableDataListView = (ListView) rootView.findViewById(R.id.recordTableDataListView);
        tableDataListView.setAdapter(mSimpleAdapter);

        showData(VisibleAPsFragment.ALL_SSIDS);

        return rootView;
    }

    public void showData(ArrayList<String> selectedWiFis) {
        ArrayList<ArrayList<HashMap<String, String>>> tmpList = ComplexFunctions.filterAPs(mWifiDatabaseManager.getTableData(tableName), selectedWiFis);
        allData.clear();

        for (ArrayList<HashMap<String, String>> d : tmpList) {
            Collections.sort(d, new SortByTime(WiFiDatabaseManager.KEY_TIME));
            allData.addAll(d);
        }

        mSimpleAdapter.notifyDataSetChanged();

    }

    public void ssidSelectorDialog() {
        final String[] ssid_options = {"SFUNET", "SFUNET-SECURE", "eduroam"};
        // arraylist to keep the selected items
        final ArrayList<Integer> selectedIndicies = new ArrayList<>();

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Select SSID(s)");
        builder.setMultiChoiceItems(ssid_options, null, new DialogInterface.OnMultiChoiceClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int indexSelected, boolean isChecked) {
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
                ArrayList<String> wifis = new ArrayList<>();

                for (int i : selectedIndicies) wifis.add(ssid_options[i]);

                Log.i(TAG, "changing");

                showData(wifis);

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

        if (id == R.id.select_wifi_ssid) {
            ssidSelectorDialog();
        }

        return false;
    }

}
