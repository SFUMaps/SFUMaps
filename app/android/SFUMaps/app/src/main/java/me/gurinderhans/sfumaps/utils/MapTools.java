package me.gurinderhans.sfumaps.utils;

import android.content.Context;
import android.graphics.Picture;
import android.graphics.PointF;
import android.os.Environment;
import android.util.Log;
import android.util.Pair;

import com.jakewharton.disklrucache.DiskLruCache;
import com.larvalabs.svgandroid.SVGBuilder;
import com.parse.FindCallback;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Pattern;

import me.gurinderhans.sfumaps.BuildConfig;
import me.gurinderhans.sfumaps.app.Keys;

/**
 * Created by ghans on 2/9/15.
 */
public class MapTools {

	public static final String TAG = MapTools.class.getSimpleName();

	static final int MAX_DISK_CACHE_BYTES = 1024 * 1024 * 2; // 2MB


	//
	// MARK: Parse
	//

	public static void getZoomMarkers(int zoom, FindCallback<ParseObject> callback) {
		ParseQuery<ParseObject> query = ParseQuery.getQuery(Keys.ParseMapPlace.CLASS);
		Integer[] zooms = {zoom};
		query.include(Keys.ParseMapPlace.PARENT_PLACE);

		// set cache
		if (BuildConfig.DEBUG)
			query.setCachePolicy(ParseQuery.CachePolicy.NETWORK_ELSE_CACHE);
		else
			query.setCachePolicy(ParseQuery.CachePolicy.CACHE_ELSE_NETWORK);

		query.whereContainedIn(Keys.ParseMapPlace.ZOOM, Arrays.asList(zooms));
		query.findInBackground(callback);
	}


	//
	// MARK: general methods
	//

	public static void createFile(String filename, String fileData) {
		File file = new File(Environment.getExternalStorageDirectory() + File.separator + filename);

		try {
			OutputStream fo = new FileOutputStream(file);
			fo.write(fileData.getBytes());
			fo.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static String loadFile(Context context, String filename) {
		BufferedReader reader = null;
		StringBuilder buffer = new StringBuilder();
		try {
			reader = new BufferedReader(
					new InputStreamReader(context.getAssets().open(filename)));

			String mLine;
			while ((mLine = reader.readLine()) != null) {
				buffer.append(mLine);
			}
		} catch (IOException e) {
			//log the exception
			e.printStackTrace();
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					//log the exception
				}
			}
		}

		return buffer.toString();
	}

	// checks if point B is within point A of `range`
	public static boolean inRange(PointF a, PointF b, float range) {
		return Math.abs(a.x - b.x) <= range && Math.abs(a.y - b.y) <= range;
	}


	//
	// MARK: Map Tiles Cache
	//

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


	//
	// MARK: Map Tiles
	//


	/**
	 * Iterates through the asset tile files and maps each tile asset file to the corresponding
	 * zoom level and returns the list containing that correspondence
	 *
	 * @param c - context
	 * @return - tiles list or null IF an error occurred while moving tiles
	 */
	public static ArrayList<Pair<String, Picture>> getBaseMapTiles(Context c) {

		try {
			String basePath = TileManager.TILES_PATH + File.separator + "basemap";

			String[] tiles = c.getAssets().list(basePath);

			// try to copy tile assets
			boolean tilesCopied = TileManager.copyTileAssets(c, basePath, tiles);
			if (!tilesCopied) return null;

			ArrayList<Pair<String, Picture>> returnTileFiles = new ArrayList<>();

			// FIXME: loading .svg file can create performance issues during app startup
			// TODO: make sure the file at index 0 is the one with lowest zoom
			Picture currentFile = new SVGBuilder()
					.readFromInputStream(new FileInputStream(
							TileManager.getTileFile(c, basePath, tiles[0])))
					.build()
					.getPicture();

			// map to zoom level range [0,15), to keep out of bounds for safety
			for (int i = 0; i < 15; i++) {
				for (String f : tiles) {
					String zoom_floor_val = f.split(Pattern.quote("."))[0]; // extract zoom and floor level values by removing extension

					// if file zoom value matches with the loop value
					if (zoom_floor_val.split("-")[SVGTileProvider.FILE_NAME_ZOOM_LVL_INDEX].equals(i + ""))
						currentFile = new SVGBuilder().readFromInputStream(
								new FileInputStream(TileManager.getTileFile(c, basePath, f))).build().getPicture();
				}
				returnTileFiles.add(Pair.create(i + "", currentFile));
			}

			return returnTileFiles;

		} catch (IOException e) {
			e.printStackTrace();
			Log.d(TAG, "Could not create BaseMap Tile Provider.");
		}

		return null;
	}

	public static ArrayList<Pair<String, Picture>> getOverlayTiles(Context c) {
		try {
			String basePath = TileManager.TILES_PATH + File.separator + "overlay";

			ArrayList<Pair<String, Picture>> tileFiles = new ArrayList<>();

			// FIXME: loading .svg file can create performance issues during app startup
			Picture currentFile = new SVGBuilder().readFromInputStream(
					c.getAssets().open(basePath + File.separator + "sfumap-overlay.svg")).build().getPicture();

			for (int i = 0; i < 15; i++)
				tileFiles.add(Pair.create(i + "", currentFile));

			return tileFiles;

		} catch (IOException e) {
			e.printStackTrace();
			Log.d(TAG, "Could not create Tile Provider. Unable to list map tile files directory");
		}

		return null;
	}

	/**
	 * @return - distance in meters
	 */
	public static float LatLngDistance(double lat1, double lng1, double lat2, double lng2) {
		double earthRadius = 6371000; //meters
		double dLat = Math.toRadians(lat2 - lat1);
		double dLng = Math.toRadians(lng2 - lng1);
		double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
				Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
						Math.sin(dLng / 2) * Math.sin(dLng / 2);
		double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
		return (float) (earthRadius * c);
	}


	/**
	 * Handles reading, writing map tiles
	 */
	static class TileManager {

		public static final String TILES_PATH = "maptiles";

		/**
		 * Copy the file from the assets to the map tiles directory
		 */
		public static boolean copyTileAssets(Context context, String basePath, String[] tilesList) {

			try {
				// check if tiles already exist
				if (getTileFile(context, basePath, tilesList[0]).exists())
					return true;

				// copy tiles to internal storage
				for (String tileFile : tilesList) {

					InputStream is = context.getAssets().open(basePath + File.separator + tileFile);

					FileOutputStream os = new FileOutputStream(
							getTileFile(context, basePath, tileFile));

					byte[] buffer = new byte[1024];
					int dataSize;
					while ((dataSize = is.read(buffer)) > 0)
						os.write(buffer, 0, dataSize);

					os.close();
				}

			} catch (IOException e) {
				return false;
			}

			return true;
		}

		/**
		 * Return a {@link File} pointing to the storage location for map tiles.
		 */
		public static File getTileFile(Context context, String basePath, String filename) {
			File folder = new File(context.getFilesDir(), basePath);
			if (!folder.exists())
				folder.mkdirs();

			return new File(folder, filename);
		}
	}

}