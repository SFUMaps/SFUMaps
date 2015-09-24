package me.gurinderhans.sfumaps.devtools.wifirecorder.models;

/**
 * Created by ghans on 15-05-28.
 */
public class WiFiAccessPoint {

	public static final String TAG = WiFiAccessPoint.class.getSimpleName();

	// class vars
	public String SSID;
	public String BSSID;
	public Integer RSSI;
	public Integer FREQ;
	public Long TIME;
	public Integer RSSI_DIFF;
	public Integer RECORDED_RSSI;
	public Boolean isOnTop;


	public WiFiAccessPoint(String ssid, String bssid, Integer rssi, Integer freq, Long time, Integer rssi_diff, Integer recorded_rssi, Boolean isOnTop) {
		super();

		// assignments
		this.SSID = ssid;
		this.BSSID = bssid;
		this.RSSI = rssi;
		this.FREQ = freq;
		this.TIME = time;
		this.RSSI_DIFF = rssi_diff;
		this.RECORDED_RSSI = recorded_rssi;
		this.isOnTop = isOnTop;
	}
}
