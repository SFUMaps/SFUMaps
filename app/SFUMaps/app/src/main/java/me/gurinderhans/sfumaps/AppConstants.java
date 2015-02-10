package me.gurinderhans.sfumaps;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by ghans on 2/9/15.
 */
public final class AppConstants {

    public static final float TILE_SIZE = 256f;
    // map database vars
    public static final String KEY_RSSI_DIFFERENCE = "rssi_diff";
    public static final String KEY_RECORDED_VAL = "recorded_val";
    public static final String[] DATA_TABLES = {"apsdata_AQ_EastToWestUP"
            , "apsdata_AQ_NorthToSouthLEFT"
            , "apsdata_AQ_NorthToSouthRIGHT"
            , "apsdata_AQ_EastToWestDOWN"};
    public static ArrayList<String> ALL_SSIDS = new ArrayList<>(Arrays.asList("SFUNET", "SFUNET-SECURE", "eduroam"));


    private AppConstants() {
        /* To make sure this class cannot be instantiated */
    }
}
