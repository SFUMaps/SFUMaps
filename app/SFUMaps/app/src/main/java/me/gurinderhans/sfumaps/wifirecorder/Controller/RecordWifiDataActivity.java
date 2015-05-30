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
import android.util.Log;
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
    EditText recordDataName;
    Handler mHandler = new Handler();
    MenuItem recordButton;
    MenuItem listButton;
    boolean record;
    String runningTable;
    Map<String, String> keepOnTop = new HashMap<>();
    ArrayList<HashMap<String, Object>> runningTableData = new ArrayList<>();


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
        recordDataName = (EditText) findViewById(R.id.database_table_name);
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
        record = false;
        runningTable = null;
        runningTableData.clear();
        keepOnTop.clear();
        recordDataName.setEnabled(true);
        recordDataName.setText("");
        mHandler.removeCallbacks(scanner);
        this.invalidateOptionsMenu();

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
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_record_wifi_data, menu);

        // assign menu buttons
        recordButton = menu.getItem(0);
        listButton = menu.getItem(1);

        // set button state
        recordButton.setIcon(R.drawable.ic_play_arrow_white_24dp);
        listButton.setIcon(R.drawable.ic_sort_white_24dp);

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

            // set icon to opposite and inverse record
            record = !record;
            int record_icon = record ? R.drawable.ic_pause_white_24dp : R.drawable.ic_play_arrow_white_24dp;
            recordButton.setIcon(record_icon);

            // set button title
            recordButton.setTitle(record ? "Pause" : "Play");

            recordDataName.setEnabled(!record);
            listButton.setEnabled(!record);

            // clear text input after recording finished and enable field
            if (!record) {
                recordDataName.setText("");
            }


            return true;
        }

        // list tables button click
        if (id == R.id.list_tables) {

            if (runningTable == null) {
                // show tables dialog
                ArrayList<String> dataTables = dbManager.getDataTables();
                final CharSequence tables[] = dataTables.toArray(new CharSequence[dataTables.size()]);

                // create dialog
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Pick a table");
                builder.setItems(tables, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        // the user clicked on tables[which]
                        runningTable = tables[which].toString();
                        listButton.setIcon(R.drawable.ic_clear_white_24dp);
                        listButton.setTitle("Cancel");
                        recordButton.setEnabled(false);

                        recordDataName.setText(runningTable);
                        recordDataName.setEnabled(false);

                    }
                }).show();

            } else {
                runningTable = null;
                runningTableData.clear();
                listButton.setIcon(R.drawable.ic_sort_white_24dp);
                listButton.setTitle("List");
                recordButton.setEnabled(true);
                recordDataName.setText("");
                recordDataName.setEnabled(true);
            }

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void manageData(List<ScanResult> data) {

        // inspect table MODE
        if (runningTable != null) {
            inspectTableData(data);
        } else {

            // normal viewing / recording MODE

            // cache record so we don't abrupt data recording in the middle of the loop
            boolean localRecordDataFlag = record;

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
                    dbManager.addAccessPoint(point, recordDataName.getText().toString());
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
