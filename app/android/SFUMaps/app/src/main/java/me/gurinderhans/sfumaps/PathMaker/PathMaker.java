package me.gurinderhans.sfumaps.PathMaker;

import android.graphics.Point;
import android.graphics.PointF;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import me.gurinderhans.sfumaps.Factory.MapGrid;
import me.gurinderhans.sfumaps.MercatorProjection;
import me.gurinderhans.sfumaps.R;

/**
 * Created by ghans on 15-08-10.
 */
public class PathMaker implements MapWrapperLayout.OnDragListener {

    public static final String TAG = PathMaker.class.getSimpleName();

    public final GoogleMap mMap;
    public final MapGrid mMapGrid;

    boolean isEditingMap = false;

    Polyline map_draw_path_line;

    public PathMaker(CustomMapFragment mapFragment, GoogleMap map, View editButton, MapGrid grid) {
        this.mMap = map;
        this.mMapGrid = grid;

        mapFragment.setOnDragListener(this);

        map_draw_path_line = mMap.addPolyline(new PolylineOptions().width(5).color(0xFFc41230).geodesic(true).zIndex(10000));

        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                isEditingMap = !isEditingMap;

                ((ImageButton) v).setImageResource(isEditingMap ? android.R.drawable.ic_menu_close_clear_cancel : android.R.drawable.ic_menu_edit);

                mMap.getUiSettings().setScrollGesturesEnabled(!isEditingMap);
            }
        });
    }


    @Override
    public void onDrag(MotionEvent motionEvent) {
        // allow for normal map stuff if not in editing mode
        if (!isEditingMap)
            return;

        Point screenPoint = new Point((int) motionEvent.getX(), (int) motionEvent.getY());
        LatLng latlng = mMap.getProjection().fromScreenLocation(screenPoint);
        PointF mapPoint = MercatorProjection.fromLatLngToPoint(latlng);

        // get dist of each point and figure out index of closest point
        mMap.addMarker(new MarkerOptions()
                .position(MercatorProjection.fromPointToLatLng(getClosestPoint(mapPoint, mMapGrid)))
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.map_path))
                .anchor(0.5f, 0.5f));


    }

    public static PointF getClosestPoint(PointF point, MapGrid grid) {

        // convert dist to grid index and return the position of the node at that index
        int xDiff = (int) ((point.x - grid.getNode(0, 0).node_position.x) / MapGrid.EACH_POINT_DIST);
        int yDiff = (int) ((point.y - grid.getNode(0, 0).node_position.y) / MapGrid.EACH_POINT_DIST);

        return grid.getNode(xDiff, yDiff).node_position;
    }

}