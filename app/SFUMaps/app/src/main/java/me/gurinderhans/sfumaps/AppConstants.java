package me.gurinderhans.sfumaps;

/**
 * Created by ghans on 2/9/15.
 */
public final class AppConstants {

    public static final float TILE_SIZE = 256f;

    // map database vars
//    public static final String KEY_RSSI_DIFFERENCE = "rssi_diff";
//    public static final String KEY_RECORDED_VAL = "recorded_val";
//    public static final String MAX_SSID_APS = "aps_max";

    // Keys for accessing stored wifi data
    public static final String KEY_ROWID   =   "_id";
    public static final String KEY_SSID    =   "ssid";
    public static final String KEY_BSSID   =   "bssid";
    public static final String KEY_FREQ    =   "freq";
    public static final String KEY_RSSI    =   "level";
    public static final String KEY_TIME    =   "rec_time";
    public static final String KEY_POINT   =   "point";

    public static final int RSSI_THRESHOLD = -65;

    // TODO: Let's not hardcode these ***
    public static String[] ALL_SSIDS = {"SFUNET", "SFUNET-SECURE", "eduroam"};


    private AppConstants() {
        /* To make sure this class cannot be instantiated */
    }
}
