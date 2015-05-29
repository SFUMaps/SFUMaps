package me.gurinderhans.sfumaps.wifirecorder.Model;

/**
 * Created by ghans on 15-05-28.
 */
public class WiFiAccessPoint {

    public static final String TAG = WiFiAccessPoint.class.getSimpleName();

    // member variables
    public String SSID;
    public String BSSID;
    public int RSSI;
    public int FREQ;
    public long TIME;


    public WiFiAccessPoint(String ssid, String bssid, int rssi, int freq, long time) {
        super();

        // assign variables
        this.SSID = ssid;
        this.BSSID = bssid;
        this.RSSI = rssi;
        this.FREQ = freq;
        this.TIME = time;
    }
}
