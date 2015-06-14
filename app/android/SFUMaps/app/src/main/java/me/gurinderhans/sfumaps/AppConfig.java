package me.gurinderhans.sfumaps;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by ghans on 15-04-11.
 */
public class AppConfig {

    /**
     * :: Absolute Constants ::
     */
    public static final String TAG = AppConfig.class.getSimpleName();
    public static final float TILE_SIZE = 256f;
    public static final String DATABASE_TABLE_PREFIX = "apsdata";
    public static final String TILE_PATH = "maptiles";

    /**
     * :: Varying Constants ::
     */
    public static Set<String> ALL_SSIDS;
    public static int RSSI_THRESHOLD;

    // empty constructor
    private AppConfig() {
        /* To make sure this class cannot be instantiated */
    }

    /**
     * Loads the saved prefs and stores them into class variables
     *
     * @param ctx - application context
     */
    public static void loadPreferences(Context ctx) {

        SharedPreferences prefs = ctx.getSharedPreferences(Keys.KEY_APP_CONFIG_PREFS, Context.MODE_PRIVATE);
        harcodePrefs(prefs); // set our prefs before loading them

        ALL_SSIDS = prefs.getStringSet(Keys.KEY_CONFIG_SSID_SET, new HashSet<String>());
        RSSI_THRESHOLD = prefs.getInt(Keys.KEY_CONFIG_RSSI_THRESHOLD, 0);
    }

    /**
     * Saves the preferences onto device
     *
     * @param prefs - sharedPrefs
     */
    private static void harcodePrefs(SharedPreferences prefs) {

        prefs.edit().putInt(Keys.KEY_CONFIG_RSSI_THRESHOLD, -65).apply();

        Set<String> ssidSet = new HashSet<>();
        ssidSet.add("SFUNET-SECURE");
        ssidSet.add("SFUNET");
        ssidSet.add("eduroam");
        ssidSet.add("TELUS0469");

        prefs.edit().putStringSet(Keys.KEY_CONFIG_SSID_SET, ssidSet).apply();

    }
}