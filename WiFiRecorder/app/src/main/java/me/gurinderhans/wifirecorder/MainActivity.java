package me.gurinderhans.wifirecorder;

import android.app.ActionBar;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;


public class MainActivity extends Activity {

    public static final String TAG = MainActivity.class.getSimpleName();
    boolean record = false;

    Context context;
    EditText recordDataTableName;
    String tableName;
    WifiManager service_WifiManager;
    WifiReceiver wifiReceiver;
    WiFiDatabaseManager mWiFiDatabaseManager;
    SimpleAdapter mSimpleAdapter;
    ArrayList<HashMap<String, String>> mSortedAPsList;
    Handler mHandler;
    Runnable scanner;
    MenuItem menuItem_record;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ActionBar actionBar = getActionBar();

        if (actionBar != null) {
            actionBar.setTitle("Recorder");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) actionBar.setElevation(0);
        }

        recordDataTableName = (EditText) findViewById(R.id.allAPsTableName);
        ListView lv_allWifiAPs = (ListView) findViewById(R.id.allWifiAPs_lv);

        context = getApplicationContext();
        mHandler = new Handler();
        mSortedAPsList = new ArrayList<>();
        wifiReceiver = new WifiReceiver();

        service_WifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        mWiFiDatabaseManager = new WiFiDatabaseManager(context);

        //set things in place and display networks
        int[] ids = {R.id.ssid, R.id.bssid, R.id.freq, R.id.level};
        String[] keys = {WiFiDatabaseManager.KEY_SSID, WiFiDatabaseManager.KEY_BSSID, WiFiDatabaseManager.KEY_FREQ, WiFiDatabaseManager.KEY_RSSI};

        mSimpleAdapter = new SimpleAdapter(context, mSortedAPsList, R.layout.lv_item_wifiap, keys, ids);

        lv_allWifiAPs.setAdapter(mSimpleAdapter);

        lv_allWifiAPs.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                record = false;
                recordingManager(record);
            }
        });

        scanner = new Runnable() {
            @Override
            public void run() {
                service_WifiManager.startScan();
            }
        };

    }

    public void recordingManager(boolean r) {
        if (r) {
            // Set text to Stop recording and disable text field
            menuItem_record.setTitle("Stop");
            menuItem_record.setIcon(getResources().getDrawable(R.drawable.ic_action_stop));
            recordDataTableName.setEnabled(false);
        } else {
            // Set text to record and enable text field
            menuItem_record.setTitle("Record");
            menuItem_record.setIcon(getResources().getDrawable(R.drawable.ic_action_play));
            recordDataTableName.setEnabled(true);
            recordDataTableName.setText("");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        menuItem_record = menu.getItem(0);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_record) {
            tableName = recordDataTableName.getText().toString();
            record = !record;
            recordingManager(record);
        }

        if (id == R.id.exportdb) {
            File file = new File(Environment.getExternalStorageDirectory() + WiFiDatabaseManager.DBPATH + WiFiDatabaseManager.DATABASE_NAME);
            boolean deleted = file.delete();

            Uri toShare = Uri.EMPTY;
            try {
                toShare = Uri.fromFile(mWiFiDatabaseManager.exportDB());
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (toShare != Uri.EMPTY && deleted) {
                Intent intent = new Intent(android.content.Intent.ACTION_SEND);
                intent.setType("text/plain");
                intent.putExtra(Intent.EXTRA_STREAM, toShare);
                startActivity(Intent.createChooser(intent, ""));
            }
        }

        if (id == R.id.view_tables)
            startActivity(new Intent(MainActivity.this, TablesListActivity.class));

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume() {
        super.onResume();
        registerReceiver(wifiReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        showData(service_WifiManager.getScanResults());
        mHandler.postDelayed(scanner, 0);
    }

    @Override
    public void onPause() {
        super.onPause();
        record = false;
        recordingManager(record);
        unregisterReceiver(wifiReceiver);
    }

    public void showData(List<ScanResult> wifiAPs) {
        mSortedAPsList.clear();

        //sort wifi results by rssi value
        Collections.sort(wifiAPs, new Comparator<ScanResult>() {
            @Override
            public int compare(ScanResult lhs, ScanResult rhs) {
                return (rhs.level < lhs.level ? -1 : (lhs.level == rhs.level ? 0 : 1));
            }
        });

        for (ScanResult result : wifiAPs) {
            HashMap<String, String> ap = new HashMap<>();
            ap.put(WiFiDatabaseManager.KEY_SSID, result.SSID);
            ap.put(WiFiDatabaseManager.KEY_BSSID, result.BSSID);
            ap.put(WiFiDatabaseManager.KEY_FREQ, result.frequency + " MHz");
            ap.put(WiFiDatabaseManager.KEY_RSSI, Integer.toString(result.level));

            String rec_time = Long.toString(System.currentTimeMillis());

            if (record)
                mWiFiDatabaseManager.addApData(result.SSID, result.BSSID,
                        Integer.toString(result.frequency),
                        Integer.toString(result.level),
                        rec_time, tableName);

            mSortedAPsList.add(ap);
        }

        mSimpleAdapter.notifyDataSetChanged();
    }

    private class WifiReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context c, Intent intent) {
            showData(service_WifiManager.getScanResults());
            mHandler.postDelayed(scanner, 0);
        }
    }
}
