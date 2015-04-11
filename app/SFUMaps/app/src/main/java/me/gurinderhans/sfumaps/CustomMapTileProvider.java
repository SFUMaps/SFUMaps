package me.gurinderhans.sfumaps;

import android.content.res.AssetManager;

import com.google.android.gms.maps.model.Tile;
import com.google.android.gms.maps.model.TileProvider;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by ghans on 1/17/15.
 */
public class CustomMapTileProvider implements TileProvider {

    public static final String TAG = CustomMapTileProvider.class.getSimpleName();

    private static final int BUFFER_SIZE = 16 * 1024;

    private AssetManager mAssets;


    public CustomMapTileProvider(AssetManager assets) {
        mAssets = assets;
    }

    @Override
    public Tile getTile(int x, int y, int zoom) {
        byte[] image = readTileImage(x, y, zoom);
        return image == null ? null : new Tile((int) AppConfig.TILE_SIZE, (int) AppConfig.TILE_SIZE, image);
    }

    private byte[] readTileImage(int x, int y, int zoom) {
        InputStream in = null;
        ByteArrayOutputStream buffer = null;

        try {
            in = mAssets.open(getTileFilename(x, y, zoom));
            buffer = new ByteArrayOutputStream();

            int nRead;
            byte[] data = new byte[BUFFER_SIZE];

            while ((nRead = in.read(data, 0, BUFFER_SIZE)) != -1) {
                buffer.write(data, 0, nRead);
            }
            buffer.flush();

            return buffer.toByteArray();
        } catch (IOException e) {
            return null;
        } catch (OutOfMemoryError e) {
            return null;
        } finally {
            if (in != null) try {
                in.close();
            } catch (Exception ignored) {
            }
            if (buffer != null) try {
                buffer.close();
            } catch (Exception ignored) {
            }
        }
    }

    private String getTileFilename(int x, int y, int zoom) {
        return "maptiles/" + zoom + '/' + x + '/' + y + ".png";
    }

}
