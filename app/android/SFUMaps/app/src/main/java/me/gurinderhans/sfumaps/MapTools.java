package me.gurinderhans.sfumaps;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Picture;
import android.graphics.PointF;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.PictureDrawable;
import android.util.Log;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.ui.IconGenerator;
import com.larvalabs.svgandroid.SVGBuilder;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by ghans on 2/9/15.
 */
public class MapTools {

    public static final String TAG = MapTools.class.getSimpleName();

    private static String[] mapTileAssets; // tile assets list cache

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
                .snippet(ssid));
    }


    public static Marker addTextMarker(Context c, GoogleMap map, PointF screenLocation,
                                       Bitmap textIcon, Integer imageIcon) {

        // get passed in icon or use the default one
        int iconId = (imageIcon == null) ? R.drawable.location_marker : imageIcon;

        Bitmap a = pictureDrawableToBitmap(new SVGBuilder().readFromResource(c.getResources(), iconId)
                .build().getPicture());
        a = combineImages(a, textIcon);

        Log.i(TAG, "width: " + a.getScaledWidth((int) c.getResources().getDisplayMetrics().density));
        // add icon image on actual point
        return map.addMarker(new MarkerOptions()
                        .position(MercatorProjection.fromPointToLatLng(screenLocation))
                        .icon(BitmapDescriptorFactory.fromBitmap(a))
        );
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

    /**
     * Returns true if the given tile file exists as a local asset.
     */
    public static boolean hasTileAsset(Context context, String filename) {

        // cache the list of available files
        if (mapTileAssets == null) {
            try {
                mapTileAssets = context.getAssets().list(AppConfig.TILE_PATH);
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
            InputStream is = context.getAssets().open(AppConfig.TILE_PATH + File.separator + filename);
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
        File folder = new File(context.getFilesDir(), AppConfig.TILE_PATH);
        if (!folder.exists()) {
            folder.mkdirs();
        }
        return new File(folder, filename);
    }

    /**
     * @param mContext  - application context
     * @param usedTiles - list of tiles to remove
     */
    public static void removeUnusedTiles(Context mContext, final ArrayList<String> usedTiles) {
        // remove all files are stored in the tile path but are not used
        File folder = new File(mContext.getFilesDir(), AppConfig.TILE_PATH);
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

    public static HashMap<String, File> getTileFiles(Context c) {

        HashMap<String, File> tileFiles = new HashMap<>();

        try {
            String[] files = c.getAssets().list(AppConfig.TILE_PATH);

            for (String f : files) {
                if (copyTileAsset(c, f)) {
                    String zoomLvl = f.split("-")[SVGTileProvider.FILE_NAME_ZOOM_LVL_INDEX];
                    tileFiles.put(zoomLvl, getTileFile(c, f));
                    Log.i(TAG, "copied: " + f + " to files dir && " + "added: " + f + " to tileFiles list");
                }
            }

        } catch (IOException e) {
            Log.i(TAG, "unable to list assets directory");
        }
        return tileFiles;
    }

    /**
     * @param c
     * @param text
     * @param rotation
     * @param contentRotation
     * @return
     */
    public static Bitmap createPureTextIcon(Context c, String text, Integer rotation, Integer contentRotation) {

        IconGenerator generator = new IconGenerator(c);
        generator.setBackground(null);
        generator.setTextAppearance(R.style.MapTextRawStyle);
        generator.setContentPadding(0, 0, 0, 0);

        if (rotation != null)
            generator.setRotation(rotation);

        if (contentRotation != null)
            generator.setContentRotation(contentRotation);

        return generator.makeIcon(text);
    }

    /**
     * @param picture
     * @return
     */
    public static Bitmap pictureDrawableToBitmap(Picture picture) {
        PictureDrawable pd = new PictureDrawable(picture);
        Bitmap bitmap = Bitmap.createBitmap(pd.getIntrinsicWidth(), pd.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        canvas.drawPicture(pd.getPicture());
        return bitmap;
    }


    public static Bitmap combineImages(Bitmap c, Bitmap s) { // can add a 3rd parameter 'String loc' if you want to save the new image - left some code to do that at the bottom

        Bitmap cs;
        int width, height;

        width = c.getWidth() + s.getWidth() + 5;
        height = c.getHeight() + 5;

        cs = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

        Canvas comboImage = new Canvas(cs);

        comboImage.drawBitmap(c, 0f, 5f, null);
        comboImage.drawBitmap(s, c.getWidth() + 5, 0f, null);

        return cs;
    }

}
