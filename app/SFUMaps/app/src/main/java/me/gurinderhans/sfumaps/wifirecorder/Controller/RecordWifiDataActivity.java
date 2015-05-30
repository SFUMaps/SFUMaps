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


    // UI
    ListView mWifiApListView;
    EditText recordDataTableInput;
    Handler mHandler = new Handler();
    MenuItem recordButton;
    MenuItem listButton;
    String runningTable;
    Map<String, String> keepOnTop = new HashMap<>();
    ArrayList<HashMap<String, Object>> runningTableData = new ArrayList<>();

    // activity modes
    boolean MODE_RECORD_DATA;
    boolean MODE_INSPECT_DATA;

    // controller fields
    WifiAPListViewAdapter mWifiAPListViewAdapter;
    WifiManager mWifiManager;
    WiFiReceiver mWifiReceiver = new WiFiReceiver();
    DataBaseManager dbManager;
    private Runnable scanner = new Runnable() {
        @Override
        public void run() {
            mWifiManager.startScan();
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

        // set adapter
        mWifiAPListViewAdapter = new WifiAPListViewAdapter(getApplicationContext());
        mWifiApListView.setAdapter(mWifiAPListViewAdapter);

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
        runningTable = null;
        runningTableData.clear();
        keepOnTop.clear();
        recordDataTableInput.setEnabled(true);
        recordDataTableInput.setText("");

        // redraw menu
        invalidateOptionsMenu();

        // load scanned results initially to prevent blank table view
        manageData(mWifiManager.getScanResults());

        // register receiver and start recording
        registerReceiver(mWifiReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        mHandler.postDelayed(scanner, 0);
    }

    @Override
    protected void onPause() {
        super.onPause();

        // unregister the wifi receiver
        unregisterReceiver(mWifiReceiver);

        // remove handler callbacks
        mHandler.removeCallbacks(scanner);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_record_wifi_data, menu);

        // assign menu buttons
        recordButton = menu.getItem(0);
        listButton = menu.getItem(1);

        // update button icon & title
        recordButton.setIcon(MODE_RECORD_DATA ? R.drawable.ic_pause_white_24dp : R.drawable.ic_play_arrow_white_24dp);
        recordButton.setTitle(this.getString(MODE_RECORD_DATA ? R.string.menu_action_record_pause : R.string.menu_action_record_play));

        // list button icon & title
        listButton.setIcon(MODE_INSPECT_DATA ? R.drawable.ic_clear_white_24dp : R.drawable.ic_sort_white_24dp);
        listButton.setTitle(getString(MODE_INSPECT_DATA ? R.string.menu_action_cancel : R.string.menu_action_list_tables));

        // enable / disable
        listButton.setEnabled(!MODE_RECORD_DATA);
        recordButton.setEnabled(!MODE_INSPECT_DATA);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        // record button click
        if (id == R.id.record) {

            // flip state
            MODE_RECORD_DATA = !MODE_RECORD_DATA;

            // redraw menu
            invalidateOptionsMenu();

            recordDataTableInput.setEnabled(!MODE_RECORD_DATA);

            // clear text input after recording finished and enable field
            if (!MODE_RECORD_DATA) {
                recordDataTableInput.setText("");
            }

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

                        // now we are inspecting data
                        MODE_INSPECT_DATA = true;

                        // clear arrays for re-use
                        runningTableData.clear();
                        keepOnTop.clear();

                        // the user clicked on tables[which]
                        runningTable = tables[which].toString();

                        recordDataTableInput.setText(runningTable);
                        recordDataTableInput.setEnabled(false);

                        // redraw menu
                        invalidateOptionsMenu();


                    }
                }).show();

            } else {

                MODE_INSPECT_DATA = false;

                // clear views and inputs
                runningTable = null;
                recordDataTableInput.setText("");
                recordDataTableInput.setEnabled(true);

                // redraw menu
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

        // inspect table MODE
        if (runningTable != null) {
            inspectTableData(data);
        } else {

            // normal viewing / recording MODE

            // cache record so we don't abrupt data recording in the middle of the loop
            boolean localRecordDataFlag = MODE_RECORD_DATA;

            // clear adapter
            mWifiAPListViewAdapter.clear();

            // add data to adapter and if enabled, add to db
            for (ScanResult res : data) {

                // create point
                WiFiAccessPoint point = new WiFiAccessPoint(res.SSID, res.BSSID, res.level, res.frequency, -1l, null, null, null);

                // add to adapter
                mWifiAPListViewAdapter.add(point);

                // record if true
                if (localRecordDataFlag) {
                    dbManager.addAccessPoint(point, recordDataTableInput.getText().toString());
                }
            }

            // tell adapter to update
            mWifiAPListViewAdapter.notifyDataSetChanged();
        }

    }

    private void inspectTableData(List<ScanResult> scannedData) {

        mWifiAPListViewAdapter.clear();

        if (runningTableData.isEmpty()) {
            // get data
            runningTableData = dbManager.getTableData(runningTable);
            // remove duplicates
            MapTools.removeDups(runningTableData);

            // remove unnecessary networks
            for (Iterator<HashMap<String, Object>> it = runningTableData.iterator(); it.hasNext(); ) {
                if (!AppConfig.ALL_SSIDS.contains(it.next().get(Keys.KEY_SSID).toString()))
                    it.remove();
            }

            // remove aps based on rssi
            for (Iterator<HashMap<String, Object>> it = runningTableData.iterator(); it.hasNext(); ) {
                if (Integer.parseInt(it.next().get(Keys.KEY_RSSI).toString()) < -65)
                    it.remove();
            }
        }


        for (HashMap<String, Object> row : runningTableData) {
            for (ScanResult res : scannedData) {
                if (row.get(Keys.KEY_BSSID).equals(res.BSSID)) {

                    int diff = Math.abs(Integer.parseInt(row.get(Keys.KEY_RSSI).toString()) - res.level);
                    boolean onTop = keepOnTop.containsKey(res.BSSID);

                    mWifiAPListViewAdapter.add(new WiFiAccessPoint(res.SSID, res.BSSID, res.level, res.frequency, null, diff, Integer.parseInt(row.get(Keys.KEY_RSSI).toString()), onTop));

                    mWifiAPListViewAdapter.sort(new Comparator<WiFiAccessPoint>() {
                        @Override
                        public int compare(WiFiAccessPoint lhs, WiFiAccessPoint rhs) {
                            if (lhs.isOnTop && !rhs.isOnTop)
                                return -1;

                            if (!lhs.isOnTop && rhs.isOnTop)
                                return 1;

                            return 0;
                        }
                    });
                }
            }
        }

        mWifiAPListViewAdapter.notifyDataSetChanged();
    }

    private class WiFiReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {

            // manage the recorded data
            manageData(mWifiManager.getScanResults());

            // run scanner again
            mHandler.postDelayed(scanner, 0);
        }
    }

}
