package me.gurinderhans.wifirecorder;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;

/**
 * Created by ghans on 14-12-17.
 */
public class CustomWiFiScanActivity extends Activity {

    public static final String TAG = CustomWiFiScanActivity.class.getSimpleName();

    WifiManager wifiManager;

    WifiReceiver receiverWifi;

    Runnable scanner;
    Handler handler;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_custom_wifi_scan);

        wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);

        receiverWifi = new WifiReceiver();

        registerReceiver(receiverWifi, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));

        handler = new Handler();

        scanner = new Runnable() {
            @Override
            public void run() {
                wifiManager.startScan();
            }
        };


        handler.postDelayed(scanner, 1);

    }

//    long time = 0l;

    class WifiReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context c, Intent intent) {
//            long tmp = time;
//            time = System.currentTimeMillis();
//            Log.i(TAG, "Scan Finished, took: "+(time-tmp)+" ms");
//            List<ScanResult> wifiList = wifisManager.getScanResults();
//            handler.postDelayed(scanner, 1);


        }
    }

}