package me.gurinderhans.sfumaps;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Picture;
import android.support.annotation.Nullable;
import android.util.Log;
import android.util.Pair;

import com.google.android.gms.maps.model.Tile;
import com.google.android.gms.maps.model.TileProvider;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by ghans on 15-06-07.
 */
public class SVGTileProvider implements TileProvider {

    public static final String TAG = SVGTileProvider.class.getSimpleName();

    // map tile asset name info indices
    public static final int FILE_NAME_ZOOM_LVL_INDEX = 0;
    public static final int FILE_NAME_FLOOR_LEVEL_INDEX = 1;


    private static final int POOL_MAX_SIZE = 5;
    private static final int BASE_TILE_SIZE = 256;

    private final int mScale;
    private final int mDimension;


    private final TileGeneratorPool mPool;
    private final Matrix mBaseMatrix;

    private final ArrayList<Pair<String, Picture>> mTilePictures;

    @Nullable
    private Picture mSvgPicture;

    public SVGTileProvider(ArrayList<Pair<String, Picture>> files, float dpi) {
        mScale = Math.round(dpi + .3f); // Make it look nice on N7 (1.3 dpi)
        mDimension = BASE_TILE_SIZE * mScale;
        mPool = new TileGeneratorPool(POOL_MAX_SIZE);
        mBaseMatrix = new Matrix();
        mBaseMatrix.setScale(0.25f, 0.25f); // scale to fit to screen
        mTilePictures = files;
    }

    @Override
    public Tile getTile(int x, int y, int zoom) {
        TileGenerator tileGenerator = mPool.get();
        byte[] tileData = tileGenerator.getTileImageData(x, y, zoom);
        mPool.restore(tileGenerator);
        return new Tile(mDimension, mDimension, tileData);
    }

    private class TileGeneratorPool {
        private final ConcurrentLinkedQueue<TileGenerator> mPool = new ConcurrentLinkedQueue<>();
        private final int mMaxSize;

        private TileGeneratorPool(int maxSize) {
            mMaxSize = maxSize;
        }

        public TileGenerator get() {
            TileGenerator i = mPool.poll();
            if (i == null) {
                return new TileGenerator();
            }
            return i;
        }

        public void restore(TileGenerator tileGenerator) {
            if (mPool.size() < mMaxSize && mPool.offer(tileGenerator)) {
                return;
            }
            // pool is too big or returning to pool failed, so just try to clean up.
            tileGenerator.cleanUp();
        }
    }

    public class TileGenerator {
        private Bitmap mBitmap;
        private ByteArrayOutputStream mStream;

        public TileGenerator() {
            mBitmap = Bitmap.createBitmap(mDimension, mDimension, Bitmap.Config.ARGB_8888);
            mStream = new ByteArrayOutputStream(mDimension * mDimension * 4);
        }

        public byte[] getTileImageData(int x, int y, int zoom) {

            mSvgPicture = mTilePictures.get(zoom).second;

            mStream.reset();
            Matrix matrix = new Matrix(mBaseMatrix);
            float scale = (float) (Math.pow(2, zoom) * mScale);
            matrix.postScale(scale, scale);
            matrix.postTranslate(-x * mDimension, -y * mDimension);

            mBitmap.eraseColor(Color.TRANSPARENT);
            Canvas c = new Canvas(mBitmap);
            c.setMatrix(matrix);

            if (mSvgPicture != null)
                mSvgPicture.draw(c);

            BufferedOutputStream stream = new BufferedOutputStream(mStream);
            mBitmap.compress(Bitmap.CompressFormat.PNG, 0, stream);
            try {
                stream.close();
            } catch (IOException e) {
                Log.e(TAG, "Error while closing tile byte stream.");
                e.printStackTrace();
            }
            return mStream.toByteArray();
        }

        /**
         * Attempt to free memory and remove references.
         */
        public void cleanUp() {
            mBitmap.recycle();
            mBitmap = null;
            try {
                mStream.close();
            } catch (IOException e) {
                // ignore
            }
            mStream = null;
        }
    }
}
