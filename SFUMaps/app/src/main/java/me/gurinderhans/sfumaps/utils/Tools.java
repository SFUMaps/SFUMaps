package me.gurinderhans.sfumaps.utils;

import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Picture;
import android.graphics.Point;
import android.graphics.PointF;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.jakewharton.disklrucache.DiskLruCache;
import com.larvalabs.svgandroid.SVGBuilder;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import static me.gurinderhans.sfumaps.utils.MercatorProjection.fromLatLngToPoint;
import static me.gurinderhans.sfumaps.utils.MercatorProjection.fromPointToLatLng;

/**
 * Created by ghans on 2/9/15.
 */
public class Tools {

	public static final String TAG = Tools.class.getSimpleName();


	/**
	 * Handles I/O from local and network
	 */
	public static class DataUtils {

		/**
		 * @param c
		 * @param query
		 * @param includes
		 * @param callback
		 */
		public static void parseFetchClass(final Context c, final ParseQuery<ParseObject> query,
		                                   final List<String> includes,
		                                   final FetchResultsCallback callback) {
			query.fromLocalDatastore();
			query.setLimit(1000);

			// include any given pointers
			for (String include : includes) query.include(include);

			query.findInBackground(new FindCallback<ParseObject>() {
				@Override
				public void done(List<ParseObject> objects, ParseException e) {

					if (e != null) {
						Log.i(TAG, "fetch from local data store error: " + e.getMessage());
						e.printStackTrace();
						return;
					}

					Log.i(TAG, "Class: " + query.getClassName() + ", LOCAL, fetched: " + objects);

					if (objects != null && objects.size() == 0) {
						Log.i(TAG, "not in local data store, fetching from ONLINE");
						// local data store is empty, try fetching from online
						ParseQuery<ParseObject> onlineQuery = ParseQuery.getQuery(query.getClassName());
						onlineQuery.setLimit(1000);

						// include given pointers to fetch form online
						for (String include : includes) onlineQuery.include(include);

						queryParseServers(c, onlineQuery, new FindCallback<ParseObject>() {
							@Override
							public void done(final List<ParseObject> objects, ParseException e) {
								if (e != null) {
									Log.i(TAG, "queryParseServers error:");
									e.printStackTrace();
									return;
								}

								Log.i(TAG, "ONLINE, fetched: " + objects.size());

								ParseObject.pinAllInBackground(objects);

								callback.onResults(objects);
							}
						});

						return;
					}

					callback.onResults(objects);
				}
			});
		}


		/**
		 * Checks for network connectivity and fetches data from online
		 * <p/>
		 * ParseQuery & FindCallback must be type for same class
		 */
		private static void queryParseServers(Context c, ParseQuery<ParseObject> parseQuery, FindCallback<ParseObject> findCallback) {
			ConnectivityManager cm = (ConnectivityManager) c.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo ni = cm.getActiveNetworkInfo();
			if ((ni != null) && (ni.isConnected())) {
				// If we have a network connection
				parseQuery.findInBackground(findCallback);
			} else {
				// If there is no connection, let the user know
				Toast.makeText(
						c,
						"Your device appears to be offline. We were unable to download required data. Try again later!",
						Toast.LENGTH_LONG).show();
			}
		}


		/*public static void AparseFetchClass(Context c, ParseQuery<ParseObject> query,
		                                    List<String> includes, boolean a,
		                                    final FetchResultsCallback cb) {

			// pointer includes
			for (String include : includes) query.include(include);
			query.setCachePolicy(ParseQuery.CachePolicy.CACHE_ELSE_NETWORK);
			query.findInBackground(new FindCallback<ParseObject>() {
				@Override
				public void done(List<ParseObject> objects, ParseException e) {
					if (e != null) {
						e.printStackTrace();
					}

					cb.onResults(objects);
				}
			});
		}*/


		/**
		 * TODO: add error method
		 */
		public interface FetchResultsCallback {
			/**
			 * @param objects
			 */
			void onResults(List<?> objects);
		}
	}


	/**
	 * Class wrapper for methods dealing with raw numbers
	 */
	public static class NumberUtils {

		/**
		 * @param val - the value to limit
		 * @param MAX - Max allowed value
		 * @param MIN - Min allowed value
		 * @return - return the limited value
		 */
		public static double ValueLimiter(double val, double MAX, double MIN) {
			return (val > MAX) ? MAX : (val < MIN ? MIN : val);
		}
	}


	/**
	 * Deals with all the map location conversions
	 */
	public static class LocationUtils {

		/**
		 * Calculates LatLng of some point at a distance from given latitude, longitude at an angle
		 *
		 * @param location   - given location
		 * @param bearing    - give bearing / angle (in degrees)
		 * @param distanceKm - distance in Km
		 * @return - new LatLng that is distance away from current point at some angle
		 */
		public static LatLng LatLngFrom(LatLng location, double bearing, double distanceKm) {

			float radius = 6378.1f;
			double latitude = location.latitude;
			double longitude = location.longitude;

			// new latitude
			double nLat = Math.toDegrees(Math.asin(Math.sin(Math.toRadians(latitude)) * Math.cos(distanceKm / radius) + Math.cos(Math.toRadians(latitude)) * Math.sin(distanceKm / radius) * Math.cos(Math.toRadians(bearing))));
			double nLng = Math.toDegrees(Math.toRadians(longitude) + Math.atan2(Math.sin(Math.toRadians(bearing)) * Math.sin(distanceKm / radius) * Math.cos(Math.toRadians(latitude)), Math.cos(distanceKm / radius) - Math.sin(Math.toRadians(latitude)) * Math.sin(Math.toRadians(nLat))));

			return new LatLng(nLat, nLng);
		}

		/**
		 * @return - distance in meters
		 */
		public static double LatLngDistance(double lat1, double lng1, double lat2, double lng2) {
			double earthRadius = 6371000; //meters
			double dLat = Math.toRadians(lat2 - lat1);
			double dLng = Math.toRadians(lng2 - lng1);
			double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
					Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
							Math.sin(dLng / 2) * Math.sin(dLng / 2);
			double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
			return earthRadius * c;
		}

		/**
		 * Calculate the horizontal and vertical distance between points a and b
		 *
		 * @param coordA - screen point
		 * @param coordB - indices
		 * @return - {@link Point} object containing the horizontal and vertical distance
		 */
		public static PointF getXYDist(LatLng coordA, LatLng coordB) {

			// calculate the middle corner point
			PointF dragStart = fromLatLngToPoint(coordA);
			PointF dragCurrent = fromLatLngToPoint(coordB);

			// the middle corner point
			dragCurrent.set(dragCurrent.x, dragStart.y);

			LatLng middleCornerPoint = fromPointToLatLng(dragCurrent);

			// horizontal distance
			float hDist = (float) LatLngDistance(coordA.latitude, coordA.longitude, middleCornerPoint.latitude, middleCornerPoint.longitude);

			// vertical distance
			float vDist = (float) LatLngDistance(coordB.latitude, coordB.longitude, middleCornerPoint.latitude, middleCornerPoint.longitude);

			return new PointF(hDist, vDist);
		}
	}


	/**
	 * View utils class to deal with views
	 */
	public static class ViewUtils {
		/**
		 * This method converts dp unit to equivalent pixels, depending on device density.
		 *
		 * @param dp      A value in dp (density independent pixels) unit. Which we need to convert into pixels
		 * @param context Context to get resources and device specific display metrics
		 * @return A float value to represent px equivalent to dp depending on device density
		 */
		public static float convertDpToPixel(float dp, Context context) {
			Resources resources = context.getResources();
			DisplayMetrics metrics = resources.getDisplayMetrics();
			float px = dp * (metrics.densityDpi / 160f);
			return px;
		}

		/**
		 * This method converts device specific pixels to density independent pixels.
		 *
		 * @param px      A value in px (pixels) unit. Which we need to convert into db
		 * @param context Context to get resources and device specific display metrics
		 * @return A float value to represent dp equivalent to px value
		 */
		public static float convertPixelsToDp(float px, Context context) {
			Resources resources = context.getResources();
			DisplayMetrics metrics = resources.getDisplayMetrics();
			float dp = px / (metrics.densityDpi / 160f);
			return dp;
		}

		/**
		 * Linear Animate view to a pos
		 *
		 * @param to       - val to animate to
		 * @param duration - duration of animation
		 * @param listener - listen for value changes
		 */
		public static void LinearViewAnimatorTranslateYToPos(final float from, final float to, long duration, ValueAnimator.AnimatorUpdateListener listener) {
			ValueAnimator va = ValueAnimator.ofFloat(from, to);
			va.setInterpolator(new LinearInterpolator());
			va.setDuration(duration);
			va.addUpdateListener(listener);
			va.start();
		}

		/**
		 * Helper method to hide the keyboard
		 *
		 * @param activity
		 */
		public static void hideKeyboard(Activity activity) {
			// Check if no view has focus:
			View view = activity.getCurrentFocus();
			if (view != null) {
				InputMethodManager inputManager = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
				inputManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
			}
		}
		//
	}


	/**
	 * Handles reading, writing map tiles
	 */
	public static class TileManager {

		public static final String TILES_PATH = "maptiles";
		static final int MAX_DISK_CACHE_BYTES = 1024 * 1024 * 2; // 2MB

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

		/**
		 * Iterates through the asset tile files and maps each tile asset file to the corresponding
		 * zoom level and returns the list containing that correspondence
		 *
		 * @param c - context
		 * @return - tiles list or null IF an error occurred while moving tiles
		 */
		public static ArrayList<Pair<String, Picture>> getBaseMapTiles(Context c) {

			try {
				String basePath = TILES_PATH + File.separator + "basemap";

				String[] tiles = c.getAssets().list(basePath);

				// try to copy tile assets
				boolean tilesCopied = copyTileAssets(c, basePath, tiles);
				if (!tilesCopied) return null;

				ArrayList<Pair<String, Picture>> returnTileFiles = new ArrayList<>();

				// FIXME: loading .svg file can create performance issues during app startup
				// TODO: make sure the file at index 0 is the one with lowest zoom
				Picture currentFile = new SVGBuilder()
						.readFromInputStream(new FileInputStream(
								getTileFile(c, basePath, tiles[0])))
						.build()
						.getPicture();

				// map to zoom level range [0,15), to keep out of bounds for safety
				for (int i = 0; i < 15; i++) {
					for (String f : tiles) {
						String zoom_floor_val = f.split(Pattern.quote("."))[0]; // extract zoom and floor level values by removing extension

						// if file zoom value matches with the loop value
						if (zoom_floor_val.split("-")[SVGTileProvider.FILE_NAME_ZOOM_LVL_INDEX].equals(i + ""))
							currentFile = new SVGBuilder().readFromInputStream(
									new FileInputStream(getTileFile(c, basePath, f))).build().getPicture();
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

		/**
		 * Fetches overlay building tiles
		 */
		public static ArrayList<Pair<String, Picture>> getOverlayTiles(Context c) {
			try {
				String basePath = TILES_PATH + File.separator + "overlay";

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

}