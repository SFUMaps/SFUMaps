package me.gurinderhans.sfumaps;

/**
 * Created by ghans on 2/9/15.
 */
public final class Keys {

    // Keys for accessing table data
    public static final String KEY_ROWID = "_id";
    public static final String KEY_SSID = "ssid";
    public static final String KEY_BSSID = "bssid";
    public static final String KEY_FREQ = "freq"; // Use this to give priority to one AP over another
    public static final String KEY_RSSI = "level";
    public static final String KEY_TIME = "rec_time";
    public static final String KEY_POINT = "point";

    // SharedPrefs keys
    public static final String KEY_APP_CONFIG_PREFS = "MapConfig";

    // App Config Preferences keys
    public static final String KEY_CONFIG_RSSI_THRESHOLD = "MIN_RSSI_THRESHOLD";
    public static final String KEY_CONFIG_SSID_SET = "USABLE_SSID_SET";

    // App Hierarchy keys
    public static final String KEY_HIERARCHY_NAME = "name";
    public static final String KEY_HIERARCHY_SELF_ID = "self";
    public static final String KEY_HIERARCHY_PARENT_ID = "parent";
    public static final String KEY_HIERARCHY_VALUE= "value";



    // empty constructor
    private Keys() {
        /* To make sure this class cannot be instantiated */
    }
}
