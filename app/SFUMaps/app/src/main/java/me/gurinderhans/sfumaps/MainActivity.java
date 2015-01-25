package me.gurinderhans.sfumaps;

import android.graphics.PointF;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.TileOverlayOptions;

public class MainActivity extends FragmentActivity {
    public static final String TAG = MainActivity.class.getSimpleName();
    public static final float TILE_SIZE = 256f;
    public PointF pixelOrigin_;
    public double pixelsPerLonDegree_, pixelsPerLonRadian_;
    private GoogleMap mMap; // Might be null if Google Play services APK is not available.

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        pixelOrigin_ = new PointF(TILE_SIZE / 2, TILE_SIZE / 2);
        pixelsPerLonDegree_ = TILE_SIZE / 360;
        pixelsPerLonRadian_ = TILE_SIZE / (2 * Math.PI);

        setUpMapIfNeeded();

    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
    }

    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMap();
            }
        }
    }

    private void setUpMap() {
        mMap.setMapType(GoogleMap.MAP_TYPE_NONE);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(0f, 0f), 2.0f));
        mMap.addTileOverlay(new TileOverlayOptions().tileProvider(new CustomMapTileProvider(getResources().getAssets())));


        PointF point = new PointF(128, 128);


        mMap.addMarker(new MarkerOptions().position(fromPointToLatLng(point)).title("Center from Point"));

        LatLng chicago = new LatLng(0, 0);

        int numTiles = 1 << (int) (mMap.getCameraPosition().zoom);
        PointF worldCoordinate = fromLatLngToPoint(chicago);
        PointF pixelCoordinate = new PointF(
                worldCoordinate.x * numTiles,
                worldCoordinate.y * numTiles);
        PointF tileCoordinate = new PointF(
                (float) (Math.floor(pixelCoordinate.x / TILE_SIZE)),
                (float) (Math.floor(pixelCoordinate.y / TILE_SIZE)));

        Log.i(TAG, "tile coordinate: " + tileCoordinate);

    }

    private PointF fromLatLngToPoint(LatLng latLng) {
        PointF point = new PointF(0, 0);
        PointF origin = this.pixelOrigin_;

        point.x = (float) (origin.x + latLng.longitude * this.pixelsPerLonDegree_);

        // Truncating to 0.9999 effectively limits latitude to 89.189. This is
        // about a third of a tile past the edge of the world tile.
        double siny = bound(Math.sin(degreesToRadians(latLng.latitude)), -0.9999,
                0.9999);
        point.y = (float) (origin.y + 0.5 * Math.log((1 + siny) / (1 - siny)) * -this.pixelsPerLonRadian_);
        return point;
    }

    public LatLng fromPointToLatLng(PointF point) {
        PointF origin = this.pixelOrigin_;
        double lng = (point.x - origin.x) / this.pixelsPerLonDegree_;
        double latRadians = (point.y - origin.y) / -this.pixelsPerLonRadian_;
        double lat = radiansToDegrees(2 * Math.atan(Math.exp(latRadians)) - Math.PI / 2);
        return new LatLng(lat, lng);
    }

    public double bound(double value, double opt_min, double opt_max) {
        if (opt_min != 0) return Math.max(value, opt_min);
        if (opt_max != 0) return Math.min(value, opt_max);
        return -1;
    }

    private double degreesToRadians(double deg) {
        return deg * (Math.PI / 180);
    }

    private double radiansToDegrees(double rad) {
        return rad / (Math.PI / 180);
    }

}
