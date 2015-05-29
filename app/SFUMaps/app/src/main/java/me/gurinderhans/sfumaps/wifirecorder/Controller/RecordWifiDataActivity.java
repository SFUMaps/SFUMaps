package me.gurinderhans.sfumaps.wifirecorder.Controller;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ListView;

import java.util.List;

import me.gurinderhans.sfumaps.DataBaseManager;
import me.gurinderhans.sfumaps.R;
import me.gurinderhans.sfumaps.wifirecorder.Model.WiFiAccessPoint;
import me.gurinderhans.sfumaps.wifirecorder.View.WifiAPListViewAdapter;

public class RecordWifiDataActivity extends ActionBarActivity {

    public static final String TAG = RecordWifiDataActivity.class.getSimpleName();


    // UI
    ListView mWifiApListView;
    EditText recordDataName;
    Handler mHandler = new Handler();
    ;
    boolean record;
    MenuItem recordButton;


    // controller fields
    WifiAPListViewAdapter mWifiAPListViewAdapter;
    WifiManager mWifiManager;
    WiFiReceiver mWifiReceiver = new WiFiReceiver();
    ;
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

    }

    @Override
    protected void onResume() {
        super.onResume();

        // reset
        record = false;
        recordDataName.setEnabled(true);
        recordDataName.setText("");
        mHandler.removeCallbacks(scanner);
        this.invalidateOptionsMenu();

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

        // get record action button
        recordButton = menu.getItem(0);

        // set button state
        recordButton.setIcon(R.drawable.ic_play_arrow_white_24dp);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
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

            return true;
        }

        // list tables button click
        if (id == R.id.list_tables) {
            this.startActivity(new Intent(RecordWifiDataActivity.this, ListDataTablesActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    private void manageData(List<ScanResult> data) {

        // cache record so we don't abrupt data recording in the middle of the loop
        boolean localRecordData = record;

        // clear adapter
        mWifiAPListViewAdapter.clear();

        // add data to adapter and if enabled, add to db
        for (ScanResult res : data) {

            // create point
            WiFiAccessPoint point = new WiFiAccessPoint(res.SSID, res.BSSID, res.level, res.frequency, -1);

            // add to adapter
            mWifiAPListViewAdapter.add(point);

            // record if true
            if (localRecordData) {
                dbManager.addAccessPoint(point, recordDataName.getText().toString());
            }
        }

        // tell adapter to update
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
