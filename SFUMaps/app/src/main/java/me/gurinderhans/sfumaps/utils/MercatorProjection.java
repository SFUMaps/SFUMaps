package me.gurinderhans.sfumaps.utils;

import android.graphics.PointF;

import com.google.android.gms.maps.model.LatLng;

import me.gurinderhans.sfumaps.app.AppConfig;

/**
 * Created by ghans on 15-04-11.
 */
public class MercatorProjection {

	// Projection constants
	static PointF pixelOrigin_ = new PointF(AppConfig.TILE_SIZE / 2, AppConfig.TILE_SIZE / 2);
	static double pixelsPerLonDegree_ = AppConfig.TILE_SIZE / 360;
	static double pixelsPerLonRadian_ = AppConfig.TILE_SIZE / (2 * Math.PI);

	/**
	 * @param latLng - LatLng object that we convert from
	 * @return - the coordinate on the screen corresponding to the LatLng
	 */
	public static PointF fromLatLngToPoint(LatLng latLng) {
		PointF point = new PointF(0, 0);

		point.x = (float) (pixelOrigin_.x + latLng.longitude * pixelsPerLonDegree_);

		// Truncating to 0.9999 effectively limits latitude to 89.189. This is
		// about a third of a tile past the edge of the world tile.
		double siny = bound(Math.sin(degreesToRadians(latLng.latitude)), -0.9999, 0.9999);

		point.y = (float) (pixelOrigin_.y + 0.5 * Math.log((1 + siny) / (1 - siny)) * -pixelsPerLonRadian_);
		return point;
	}

	/**
	 * @param point - PointF object that we convert from
	 * @return - the object containing lat / lng corresponding to the Point
	 */
	public static LatLng fromPointToLatLng(PointF point) {

		double lng = (point.x - pixelOrigin_.x) / pixelsPerLonDegree_;
		double latRadians = (point.y - pixelOrigin_.y) / -pixelsPerLonRadian_;
		double lat = radiansToDegrees(2 * Math.atan(Math.exp(latRadians)) - Math.PI / 2);

		return new LatLng(lat, lng);
	}

	/**
	 * @param value   - the value to bound
	 * @param opt_min - the min bound value
	 * @param opt_max - the max bound value
	 * @return - the bounded value
	 */
	public static double bound(double value, double opt_min, double opt_max) {
		if (opt_min != 0) return Math.max(value, opt_min);
		if (opt_max != 0) return Math.min(value, opt_max);
		return -1;
	}

	/**
	 * @param deg - θ value in degrees
	 * @return - θ value in radians
	 */
	private static double degreesToRadians(double deg) {
		return deg * (Math.PI / 180);
	}

	/**
	 * @param rad - θ value in radians
	 * @return - θ value in degrees
	 */
	private static double radiansToDegrees(double rad) {
		return rad / (Math.PI / 180);
	}
}
