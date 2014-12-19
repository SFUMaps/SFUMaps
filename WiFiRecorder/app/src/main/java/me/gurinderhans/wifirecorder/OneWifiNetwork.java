package me.gurinderhans.wifirecorder;

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
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;


public class OneWifiNetwork extends Activity {

    private final String TAG = getClass().getSimpleName();

    SimpleAdapter mSimpleAdapter;
    WifiReceiver wifiReceiver;
    WifiManager service_WifiManager;
    WiFiDatabaseManager mWiFiDatabaseManager;
    ArrayList<HashMap<String, String>> mSortedAPsList;
    Context context;

    Handler mHandler;
    Runnable scanner;

    boolean record;

    String thisSSID;
    String tableName;

    EditText onewifiTableName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_one_wifi_network);

        getActionBar().setTitle(thisSSID);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) getActionBar().setElevation(0);

        thisSSID = getIntent().getExtras().getString(MainActivity.KEY_SSID_SEND);

        onewifiTableName = (EditText) findViewById(R.id.onewifiTableName);
        ListView lv_oneWifiAPs = (ListView) findViewById(R.id.oneWifiListView);

        context = getApplicationContext();
        mHandler = new Handler();
        mSortedAPsList = new ArrayList<>();
        wifiReceiver = new WifiReceiver();

        mWiFiDatabaseManager = new WiFiDatabaseManager(context);
        service_WifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);

        int[] ids = {R.id.ssid, R.id.bssid, R.id.freq, R.id.level};
        String[] keys = {WiFiDatabaseManager.KEY_SSID, WiFiDatabaseManager.KEY_BSSID, WiFiDatabaseManager.KEY_FREQ, WiFiDatabaseManager.KEY_RSSI};

        mSimpleAdapter = new SimpleAdapter(context, mSortedAPsList, R.layout.lv_item_wifiap, keys, ids);
        lv_oneWifiAPs.setAdapter(mSimpleAdapter);

        scanner = new Runnable() {
            @Override
            public void run() {
                service_WifiManager.startScan();
            }
        };

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_record) {
            tableName = onewifiTableName.getText().toString();
            record = true;
            onewifiTableName.setEnabled(false);
        }
        if (id == R.id.action_record_stop) {
            record = false;
            onewifiTableName.setEnabled(true);
            onewifiTableName.setText("");
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
            if (result.SSID.equals(thisSSID)) {
                HashMap<String, String> ap = new HashMap<>();
                ap.put(WiFiDatabaseManager.KEY_SSID, result.SSID);
                ap.put(WiFiDatabaseManager.KEY_BSSID, result.BSSID);
                ap.put(WiFiDatabaseManager.KEY_FREQ, result.frequency + " MHz");
                ap.put(WiFiDatabaseManager.KEY_RSSI, result.level + "");

                String rec_time = System.currentTimeMillis() + "";

                if (record)
                    mWiFiDatabaseManager.addApData(result.SSID, result.BSSID, result.frequency + "", result.level + "", rec_time, tableName);

                mSortedAPsList.add(ap);
            }
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
