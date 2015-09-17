package me.gurinderhans.sfumaps.utils;

import android.util.Log;

import com.google.android.gms.maps.model.Tile;
import com.google.android.gms.maps.model.TileProvider;
import com.jakewharton.disklrucache.DiskLruCache;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by ghans on 15-08-19.
 * Taken from https://github.com/google/iosched/blob/master/android/src/main/java/com/google/samples/apps/iosched/ui/CachedTileProvider.java
 */

public class CachedTileProvider implements TileProvider {

	private static final String TAG = SVGTileProvider.class.getSimpleName();

	private static final String KEY_FORMAT = "%d_%d_%d_%s";

	// Index for cache entry streams
	private static final int INDEX_DATA = 0;
	private static final int INDEX_HEIGHT = 1;
	private static final int INDEX_WIDTH = 2;


	private final String mKeyTag;
	private final TileProvider mTileProvider;
	private final DiskLruCache mCache;

	/**
	 * TileProvider that wraps another TileProvider and caches all Tiles in a DiskLruCache.
	 * <p/>
	 * <p>A {@link DiskLruCache} can be reused across multiple instances.
	 * The keyTag is used to annotate entries for this TileProvider, it is recommended to use a unique
	 * String for each instance to prevent collisions.
	 * <p/>
	 * <p>NOTE: The supplied {@link DiskLruCache} requires space for
	 * 3 entries per cached object.
	 *
	 * @param keyTag       identifier used to identify tiles for this CachedTileProvider instance
	 * @param tileProvider tiles from this TileProvider will be cached.
	 * @param cache        the cache used to store tiles
	 */
	public CachedTileProvider(String keyTag, TileProvider tileProvider, DiskLruCache cache) {
		mKeyTag = keyTag;
		mTileProvider = tileProvider;
		mCache = cache;
	}

	private static String generateKey(int x, int y, int zoom, String tag) {
		return String.format(KEY_FORMAT, x, y, zoom, tag);
	}

	private static void writeByteArrayToStream(byte[] data, OutputStream stream) throws IOException {
		try {
			stream.write(data);
		} finally {
			stream.close();
		}
	}

	private static void writeIntToStream(int data, OutputStream stream) throws IOException {
		DataOutputStream dos = new DataOutputStream(stream);
		try {
			dos.writeInt(data);
		} finally {
			try {
				dos.close();
			} finally {
				stream.close();
			}
		}
	}

	private static byte[] readStreamAsByteArray(InputStream inputStream) throws IOException {
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		int read = 0;
		byte[] data = new byte[1024];
		try {
			while ((read = inputStream.read(data, 0, data.length)) != -1) {
				buffer.write(data, 0, read);
			}
		} finally {
			inputStream.close();
		}
		return buffer.toByteArray();
	}

	private static int readStreamAsInt(InputStream inputStream) throws IOException {
		DataInputStream buffer = new DataInputStream(inputStream);
		try {
			return buffer.readInt();
		} finally {
			inputStream.close();
		}
	}

	/**
	 * Load a tile.
	 * If cached, the data for the tile is read from the underlying cache, otherwise the tile is
	 * generated by the {@link com.google.android.gms.maps.model.TileProvider} and added to the
	 * cache.
	 *
	 * @param x
	 * @param y
	 * @param zoom
	 * @return
	 */
	@Override
	public Tile getTile(int x, int y, int zoom) {
		final String key = CachedTileProvider.generateKey(x, y, zoom, mKeyTag);
		Tile tile = getCachedTile(key);

		if (tile == null) {
			// tile not cached, load from provider and then cache
			tile = mTileProvider.getTile(x, y, zoom);
			if (cacheTile(key, tile)) {
				Log.d(TAG, "Added tile to cache " + key);
			}
		}
		return tile;
	}

	/**
	 * Load a tile from cache.
	 * Returns null if there is no corresponding cache entry or it could not be loaded.
	 *
	 * @param key
	 * @return
	 */
	private Tile getCachedTile(String key) {
		try {
			DiskLruCache.Snapshot snapshot = mCache.get(key);
			if (snapshot == null) {
				// tile is not in cache
				return null;
			}

			final byte[] data = readStreamAsByteArray(snapshot.getInputStream(INDEX_DATA));
			final int height = readStreamAsInt(snapshot.getInputStream(INDEX_HEIGHT));
			final int width = readStreamAsInt(snapshot.getInputStream(INDEX_WIDTH));
			if (data != null) {
				Log.d(TAG, "Cache hit for tile " + key);
				return new Tile(width, height, data);
			}

		} catch (IOException e) {
			// ignore error
		}
		return null;
	}

	private boolean cacheTile(String key, Tile tile) {
		try {
			DiskLruCache.Editor editor = mCache.edit(key);
			if (editor == null) {
				// editor is not available
				return false;
			}
			writeByteArrayToStream(tile.data, editor.newOutputStream(INDEX_DATA));
			writeIntToStream(tile.height, editor.newOutputStream(INDEX_HEIGHT));
			writeIntToStream(tile.width, editor.newOutputStream(INDEX_WIDTH));
			editor.commit();
			return true;
		} catch (IOException e) {
			// Tile could not be cached
		}
		return false;
	}

}