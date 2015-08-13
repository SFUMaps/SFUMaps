package me.gurinderhans.sfumaps.PathMaker;

import android.graphics.Point;
import android.graphics.PointF;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import me.gurinderhans.sfumaps.Factory.MapGrid;
import me.gurinderhans.sfumaps.MapTools;
import me.gurinderhans.sfumaps.MercatorProjection;
import me.gurinderhans.sfumaps.R;

/**
 * Created by ghans on 15-08-10.
 */
public class PathMaker implements MapWrapperLayout.OnDragListener {

    public static final String TAG = PathMaker.class.getSimpleName();

    public static final String WALKABLE_KEY = "walkable";

    public final GoogleMap mMap;
    public final MapGrid mMapGrid;

    boolean isEditingMap = false;

    public PathMaker(CustomMapFragment mapFragment, GoogleMap map, View editButton, final View exportButton, final MapGrid grid) {
        this.mMap = map;
        this.mMapGrid = grid;

        mapFragment.setOnDragListener(this);

        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                isEditingMap = !isEditingMap;

                exportButton.setVisibility(isEditingMap ? View.VISIBLE : View.INVISIBLE);

                ((ImageButton) v).setImageResource(isEditingMap ? android.R.drawable.ic_menu_close_clear_cancel : android.R.drawable.ic_menu_edit);

                mMap.getUiSettings().setScrollGesturesEnabled(!isEditingMap);
            }
        });

        // export map path
        exportButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    //
                    JSONObject root = new JSONObject();

                    root.put(WALKABLE_KEY, new JSONArray());

                    for (int x = 0; x < mMapGrid.mapGridSizeX; x++) {
                        for (int y = 0; y < mMapGrid.mapGridSizeY; y++) {
                            if (mMapGrid.getNode(x, y).isWalkable)
                                root.getJSONArray(WALKABLE_KEY).put(x + "," + y);
                        }
                    }

                    // create file
                    MapTools.createFile("map_grid.json", root.toString());
                    Toast.makeText(v.getContext(), "Done", Toast.LENGTH_SHORT).show();

                } catch (JSONException e) {
                    e.printStackTrace();
                }
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

        Point closestPoint = getClosestPoint(mapPoint, mMapGrid);
        mMapGrid.mMapGrid.get(closestPoint.x).get(closestPoint.y).isWalkable = true;

        // get dist of each point and figure out index of closest point
        mMap.addMarker(new MarkerOptions()
                .position(MercatorProjection.fromPointToLatLng(mMapGrid.getNode(closestPoint).node_position))
                .icon(BitmapDescriptorFactory.fromResource(mMapGrid.getNode(closestPoint).isWalkable ? R.drawable.map_path : R.drawable.no_path))
                .anchor(0.5f, 0.5f));


    }

    public static Point getClosestPoint(PointF point, MapGrid grid) {

        PointF gridNode = grid.getNode(0, 0).node_position;

        // convert dist to grid index and return the position of the node at that index

        return new Point((int) ((point.x - gridNode.x) / MapGrid.EACH_POINT_DIST), (int) ((point.y - gridNode.y) / MapGrid.EACH_POINT_DIST));
    }

}