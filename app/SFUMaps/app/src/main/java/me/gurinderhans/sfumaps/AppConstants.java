package me.gurinderhans.sfumaps;

/**
 * Created by ghans on 2/9/15.
 */
public final class AppConstants {

    public static final float TILE_SIZE = 256f;
    public static final int AQ_SIZE = 140;

    // map database vars
    public static final String KEY_RSSI_DIFFERENCE = "rssi_diff";
    public static final String KEY_RECORDED_VAL = "recorded_val";

    public static final String MAX_SSID_APS = "aps_max";

    public static String[] ALL_SSIDS = {"SFUNET", "SFUNET-SECURE", "eduroam"};


    private AppConstants() {
        /* To make sure this class cannot be instantiated */
    }
}
