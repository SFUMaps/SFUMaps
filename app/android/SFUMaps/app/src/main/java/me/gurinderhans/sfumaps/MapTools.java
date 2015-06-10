package me.gurinderhans.sfumaps;

import android.content.Context;
import android.graphics.PointF;
import android.util.Log;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.jakewharton.disklrucache.DiskLruCache;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

/**
 * Created by ghans on 2/9/15.
 */
public class MapTools {

    public static final String TAG = MapTools.class.getSimpleName();
    private static final String TILE_PATH = "maptiles";
    // TODO: Class under construction
    /* SVG Tile Provider */
    private static String[] mapTileAssets;

    // empty constructor
    private MapTools() {
        /* To make sure this class cannot be instantiated */
    }

    /**
     * @param dataArray - the data array that we are splitting by keys
     * @param keyIndex  - the index of the key in that array object
     * @return - return the [Array] separated by keys
     */
    public static HashMap<String, ArrayList<HashMap<String, Object>>> separateByKeys(ArrayList<HashMap<String, Object>> dataArray, String keyIndex) {

        HashMap<String, ArrayList<HashMap<String, Object>>> separated = new HashMap<>();

        for (HashMap<String, Object> row : dataArray) {

            String key = (String) row.get(keyIndex);

            if (!separated.containsKey(key)) {
                separated.put(key, new ArrayList<HashMap<String, Object>>());
            }

            separated.get(key).add(row);
        }

        return separated;
    }

    /**
     * @param input - input data to remove dups from
     */
    public static void removeDups(ArrayList<HashMap<String, Object>> input) {

        Collections.sort(input, new Comparator<HashMap<String, Object>>() {
            @Override
            public int compare(HashMap<String, Object> lhs, HashMap<String, Object> rhs) {
                int firstValue = Integer.parseInt(lhs.get(Keys.KEY_RSSI).toString());
                int secondValue = Integer.parseInt(rhs.get(Keys.KEY_RSSI).toString());
                return (secondValue < firstValue ? -1 : (secondValue == firstValue ? 0 : 1));
            }
        });

        int count = input.size();

        // ___
        for (int i = 0; i < count; i++) {
            for (int j = i + 1; j < count; j++) {
                HashMap<String, Object> a = input.get(i);
                HashMap<String, Object> b = input.get(j);

                boolean ssid = a.get(Keys.KEY_SSID).equals(b.get(Keys.KEY_SSID));
                boolean bssid = a.get(Keys.KEY_BSSID).equals(b.get(Keys.KEY_BSSID));

                if (ssid && bssid) {
                    input.remove(j--);
                    count--;
                }
            }
        }
    }

    /**
     * @param googleMap - the map
     * @param latLng    - latlng coordinate on the map
     * @param ssid      - SSID name for the label
     * @param dir       - direction?
     */
    public static void addMarker(GoogleMap googleMap, LatLng latLng, String ssid, String dir) {

        googleMap.addMarker(new MarkerOptions()
                .position(latLng)
                .title(dir)
                .snippet(ssid)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.routerdot)));
    }

    /**
     * @param points - list of points
     * @return - return the centroid point (mean value)
     */
    public static PointF getCentroid(ArrayList<PointF> points) {

        float Sx = 0, Sy = 0;

        for (PointF point : points) {
            Sx += point.x;
            Sy += point.y;
        }

        Sx /= points.size();
        Sy /= points.size();


        return new PointF(Sx, Sy);
    }


    //
    // SVG TILE PROVIDER Methods
    //

    /**
     * Returns true if the given tile file exists as a local asset.
     */
    public static boolean hasTileAsset(Context context, String filename) {

        //cache the list of available files
        if (mapTileAssets == null) {
            try {
                mapTileAssets = context.getAssets().list("maptiles");
            } catch (IOException e) {
                // no assets
                mapTileAssets = new String[0];
            }
        }

        // search for given filename
        for (String s : mapTileAssets) {
            if (s.equals(filename)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Copy the file from the assets to the map tiles directory if it was
     * shipped with the APK.
     */
    public static boolean copyTileAsset(Context context, String filename) {
        if (!hasTileAsset(context, filename)) {
            // file does not exist as asset
            return false;
        }

        // copy file from asset to internal storage
        try {
            InputStream is = context.getAssets().open(TILE_PATH + File.separator + filename);
            File f = getTileFile(context, filename);
            FileOutputStream os = new FileOutputStream(f);

            byte[] buffer = new byte[1024];
            int dataSize;
            while ((dataSize = is.read(buffer)) > 0) {
                os.write(buffer, 0, dataSize);
            }
            os.close();
        } catch (IOException e) {
            return false;
        }

        return true;
    }

    /**
     * Return a {@link File} pointing to the storage location for map tiles.
     */
    public static File getTileFile(Context context, String filename) {
        File folder = new File(context.getFilesDir(), TILE_PATH);
        if (!folder.exists()) {
            folder.mkdirs();
        }
        return new File(folder, filename);
    }


    public static void removeUnusedTiles(Context mContext, final ArrayList<String> usedTiles) {
        // remove all files are stored in the tile path but are not used
        File folder = new File(mContext.getFilesDir(), TILE_PATH);
        File[] unused = folder.listFiles(new FilenameFilter() {

            @Override
            public boolean accept(File dir, String filename) {
                return !usedTiles.contains(filename);
            }
        });

        if (unused != null) {
            for (File f : unused) {
                f.delete();
            }
        }
    }

    public static boolean hasTile(Context mContext, String filename) {
        return getTileFile(mContext, filename).exists();
    }


    //
    // LRU Cache
    //

    private static final int MAX_DISK_CACHE_BYTES = 1024 * 1024 * 2; // 2MB

    public static DiskLruCache openDiskCache(Context c) {
        File cacheDir = new File(c.getCacheDir(), "tiles");
        try {
            return DiskLruCache.open(cacheDir, 1, 3, MAX_DISK_CACHE_BYTES);
        } catch (IOException e) {
            Log.e(TAG, "Couldn't open disk cache.");

        }
        return null;
    }

    public static void clearDiskCache(Context c) {
        DiskLruCache cache = openDiskCache(c);
        if (cache != null) {
            try {
                Log.d(TAG, "Clearing map tile disk cache");
                cache.delete();
                cache.close();
            } catch (IOException e) {
                // ignore
            }
        }
    }


}
