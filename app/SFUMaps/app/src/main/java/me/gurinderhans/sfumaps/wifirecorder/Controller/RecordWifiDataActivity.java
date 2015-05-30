package me.gurinderhans.sfumaps.wifirecorder.Controller;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import me.gurinderhans.sfumaps.AppConfig;
import me.gurinderhans.sfumaps.DataBaseManager;
import me.gurinderhans.sfumaps.Keys;
import me.gurinderhans.sfumaps.MapTools;
import me.gurinderhans.sfumaps.R;
import me.gurinderhans.sfumaps.wifirecorder.Model.WiFiAccessPoint;
import me.gurinderhans.sfumaps.wifirecorder.View.WifiAPListViewAdapter;

public class RecordWifiDataActivity extends ActionBarActivity {


    public static final String TAG = RecordWifiDataActivity.class.getSimpleName();

    // Constants
    public static final int TOTAL_SCANS = 20;

    // UI
    ListView mWifiApListView;
    EditText recordDataTableInput;
    Handler mHandler = new Handler();
    String inspectingTable;
    Map<String, String> keepOnTop = new HashMap<>();
    ArrayList<HashMap<String, Object>> inspectingTableData = new ArrayList<>();

    // Activity Modes
    boolean MODE_RECORD_DATA;
    boolean MODE_INSPECT_DATA;

    // controller fields
    int numScansLeft = -1;
    private WifiAPListViewAdapter mWifiAPListViewAdapter;
    private WifiManager mWifiManager;
    private WiFiReceiver mWifiReceiver = new WiFiReceiver();
    private DataBaseManager dbManager;
    private Runnable scanner = new Runnable() {
        @Override
        public void run() {
            mWifiManager.startScan();
        }
    };
    private Comparator<WiFiAccessPoint> comparator = new Comparator<WiFiAccessPoint>() {
        @Override
        public int compare(WiFiAccessPoint lhs, WiFiAccessPoint rhs) {
            if (lhs.isOnTop && !rhs.isOnTop)
                return -1;

            if (!lhs.isOnTop && rhs.isOnTop)
                return 1;

            return 0;
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // set activity theme to light text
        this.setTheme(R.style.RecorderActivity);

        // initial setup
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_wifi_data);

        // get views and services
        mWifiApListView = (ListView) findViewById(R.id.scanned_APs);
        recordDataTableInput = (EditText) findViewById(R.id.database_table_name);
        mWifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        dbManager = DataBaseManager.getInstance(getApplicationContext());

        mWifiAPListViewAdapter = new WifiAPListViewAdapter(getApplicationContext());
        mWifiApListView.setAdapter(mWifiAPListViewAdapter);

        // used for sorting listview items, when clicked the list view item will get put on top, and vice-versa
        mWifiApListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String key = mWifiAPListViewAdapter.getItem(position).BSSID;
                if (keepOnTop.containsKey(key))
                    keepOnTop.remove(key);
                else
                    keepOnTop.put(key, "");

            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();

        // reset
        MODE_RECORD_DATA = false;
        MODE_INSPECT_DATA = false;
        inspectingTable = null;
        inspectingTableData.clear();
        keepOnTop.clear();
        recordDataTableInput.setEnabled(true);
        recordDataTableInput.setText("");

        invalidateOptionsMenu();

        manageData(mWifiManager.getScanResults());

        registerReceiver(mWifiReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        mHandler.postDelayed(scanner, 0);
    }

    @Override
    protected void onPause() {
        super.onPause();

        // pause recorder
        unregisterReceiver(mWifiReceiver);
        mHandler.removeCallbacks(scanner);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_record_wifi_data, menu);

        // assign menu buttons
        MenuItem recordButton = menu.getItem(0);
        MenuItem listButton = menu.getItem(1);

        recordButton.setIcon(MODE_RECORD_DATA ? R.drawable.ic_pause_white_24dp : R.drawable.ic_play_arrow_white_24dp);
        recordButton.setTitle(this.getString(MODE_RECORD_DATA ? R.string.menu_action_record_pause : R.string.menu_action_record_play));
        recordButton.setEnabled(!MODE_INSPECT_DATA);

        listButton.setIcon(MODE_INSPECT_DATA ? R.drawable.ic_clear_white_24dp : R.drawable.ic_sort_white_24dp);
        listButton.setTitle(getString(MODE_INSPECT_DATA ? R.string.menu_action_cancel : R.string.menu_action_list_tables));
        listButton.setEnabled(!MODE_RECORD_DATA);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        // record button click
        if (id == R.id.record) {

            MODE_RECORD_DATA = !MODE_RECORD_DATA;

            invalidateOptionsMenu();

            recordDataTableInput.setEnabled(!MODE_RECORD_DATA);

            // clear text input after recording finished and enable field
            if (!MODE_RECORD_DATA) {
                recordDataTableInput.setText("");
            }

            numScansLeft = TOTAL_SCANS;

            return true;
        }

        // list tables button click
        if (id == R.id.list_tables) {

            if (!MODE_INSPECT_DATA) {

                // show tables dialog
                ArrayList<String> dataTables = dbManager.getDataTables();
                final CharSequence tables[] = dataTables.toArray(new CharSequence[dataTables.size()]);

                // create dialog
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(getString(R.string.choose_data_table));
                builder.setItems(tables, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        MODE_INSPECT_DATA = true;

                        // clear arrays for re-use
                        inspectingTableData.clear();

                        inspectingTable = tables[which].toString();

                        // disable text field and set title to inspecting table
                        recordDataTableInput.setText(inspectingTable);
                        recordDataTableInput.setEnabled(false);

                        invalidateOptionsMenu();


                    }
                }).show();

            } else {

                MODE_INSPECT_DATA = false;

                // clear views and inputs
                inspectingTable = null;
                recordDataTableInput.setText("");
                recordDataTableInput.setEnabled(true);

                invalidateOptionsMenu();
            }

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * takes care of the scanned data, whether to just display it, record it, or inspect it
     *
     * @param data
     */
    private void manageData(List<ScanResult> data) {

        if (inspectingTable != null) {
            inspectTableData(data);

        } else {


            // cache record so we don't abrupt data recording in the middle of the loop
            boolean localRecordDataFlag = MODE_RECORD_DATA;

            // manage number of scans
            if (localRecordDataFlag) {
                getSupportActionBar().setTitle(numScansLeft + "");
                numScansLeft--;

                // no more scans left
                if (numScansLeft == 0) {
                    MODE_RECORD_DATA = false;

                    recordDataTableInput.setText("");
                    recordDataTableInput.setEnabled(true);

                    invalidateOptionsMenu();
                }
            } else {
                getSupportActionBar().setTitle(getString(R.string.recorder_activity_title));
            }


            mWifiAPListViewAdapter.clear();

            // add data to adapter and if enabled, add to db
            for (ScanResult res : data) {

                WiFiAccessPoint point = new WiFiAccessPoint(res.SSID, res.BSSID, res.level, res.frequency, -1l, null, null, keepOnTop.containsKey(res.BSSID));
                mWifiAPListViewAdapter.add(point);

                if (localRecordDataFlag) {
                    dbManager.addAccessPoint(point, recordDataTableInput.getText().toString());
                }
            }

            mWifiAPListViewAdapter.sort(comparator);

            mWifiAPListViewAdapter.notifyDataSetChanged();
        }

    }

    /**
     * compares scanned data with previously recorded data and displays the union
     *
     * @param scannedData
     */
    private void inspectTableData(List<ScanResult> scannedData) {

        mWifiAPListViewAdapter.clear();

        if (inspectingTableData.isEmpty()) {
            // get data
            inspectingTableData = dbManager.getTableData(inspectingTable);

            MapTools.removeDups(inspectingTableData);

            // remove unnecessary networks
            for (Iterator<HashMap<String, Object>> it = inspectingTableData.iterator(); it.hasNext(); ) {
                if (!AppConfig.ALL_SSIDS.contains(it.next().get(Keys.KEY_SSID).toString()))
                    it.remove();
            }
            // remove aps based on rssi
            for (Iterator<HashMap<String, Object>> it = inspectingTableData.iterator(); it.hasNext(); ) {
                if (Integer.parseInt(it.next().get(Keys.KEY_RSSI).toString()) < -65)
                    it.remove();
            }
        }


        for (HashMap<String, Object> row : inspectingTableData) {
            for (ScanResult res : scannedData) {
                if (row.get(Keys.KEY_BSSID).equals(res.BSSID)) {

                    int diff = Math.abs(Integer.parseInt(row.get(Keys.KEY_RSSI).toString()) - res.level);
                    boolean onTop = keepOnTop.containsKey(res.BSSID);

                    mWifiAPListViewAdapter.add(new WiFiAccessPoint(res.SSID, res.BSSID, res.level, res.frequency, null, diff, Integer.parseInt(row.get(Keys.KEY_RSSI).toString()), onTop));
                }
            }
        }

        mWifiAPListViewAdapter.sort(comparator);

        mWifiAPListViewAdapter.notifyDataSetChanged();
    }

    private class WiFiReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {

            manageData(mWifiManager.getScanResults());

            mHandler.postDelayed(scanner, 0);
        }
    }

}
