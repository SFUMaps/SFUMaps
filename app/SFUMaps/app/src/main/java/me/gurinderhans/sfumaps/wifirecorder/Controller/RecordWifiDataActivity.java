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
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

import me.gurinderhans.sfumaps.R;
import me.gurinderhans.sfumaps.wifirecorder.Model.WiFiAccessPoint;
import me.gurinderhans.sfumaps.wifirecorder.View.WifiAPListViewAdapter;

public class RecordWifiDataActivity extends ActionBarActivity {

    public static final String TAG = RecordWifiDataActivity.class.getSimpleName();

    // adapter data
    ListView mWifiApListView;
    WifiAPListViewAdapter mWifiAPListViewAdapter;

    // manager and reciever to handle wifi scans
    WifiManager mWifiManager;
    WiFiReceiver mWifiReceiver;

    // looper to run wifi scans simultaneously
    Handler mHandler;
    Runnable scanner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // set activity theme to light text
        this.setTheme(R.style.RecorderActivity);

        // initial setup
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_wifi_data);

        // get listview
        mWifiApListView = (ListView) findViewById(R.id.scanned_APs);

        // assign wifi handlers
        mWifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        mWifiReceiver = new WiFiReceiver();
        mHandler = new Handler();

        // create and set adapter for showing scanned wifi networks
        mWifiAPListViewAdapter = new WifiAPListViewAdapter(getApplicationContext());
        mWifiApListView.setAdapter(mWifiAPListViewAdapter);

        // init scanner
        scanner = new Runnable() {
            @Override
            public void run() {
                mWifiManager.startScan();
            }
        };

    }

    @Override
    protected void onResume() {
        super.onResume();

        registerReceiver(mWifiReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        mHandler.postDelayed(scanner, 0);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_record_wifi_data, menu);
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
            // set icon to stop
            item.setIcon(R.drawable.ic_pause_white_24dp);
            return true;
        }

        // list tables button click
        if (id == R.id.list_tables) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private class WiFiReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {

            // clear adapter
            mWifiAPListViewAdapter.clear();

            // add data to adapter
            for (ScanResult res : mWifiManager.getScanResults()) {
                mWifiAPListViewAdapter.add(new WiFiAccessPoint(res.SSID, res.BSSID, res.level, res.frequency, -1));
            }

            // tell adapter to update
            mWifiAPListViewAdapter.notifyDataSetChanged();

            // run scanner again
            mHandler.postDelayed(scanner, 0);
        }
    }
}
