package me.gurinderhans.sfumaps.Factory;

import android.content.Context;
import android.graphics.PointF;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;

import me.gurinderhans.sfumaps.MercatorProjection;
import me.gurinderhans.sfumaps.R;

/**
 * Created by ghans on 15-07-29.
 */
public class MapGrid {

    public static final String TAG = MapGrid.class.getSimpleName();

    public static final String WALKABLE_PATH_CHAR = ".";
    public static final String NON_WALKABLE_PATH_CHAR = "x";
    public static final float VERTICAL_EACH_POINT_DIST = 0.272f;
    public static final float HORIZONTAL_EACH_POINT_DIST = 0.276f;

    public final PointF startPoint;
    public final PointF endPoint;

    public final int mapHeight;
    public final int mapWidth;

    public ArrayList<ArrayList<GridNode>> mMapGrid = new ArrayList<>();

    public MapGrid(PointF startPoint, PointF endPoint) {
        this.startPoint = startPoint;
        this.endPoint = endPoint;

        this.mapWidth = 4 * ((int) Math.abs(endPoint.x - startPoint.x));
        this.mapHeight = 4 * ((int) Math.abs(endPoint.y - startPoint.y));

        // create the map grid
        for (int i = 0; i < mapHeight; i++) {
            ArrayList<GridNode> tmpRow = new ArrayList<>();
            for (int j = 0; j < mapWidth; j++)
                tmpRow.add(new GridNode(j, i, WALKABLE_PATH_CHAR, startPoint));
            mMapGrid.add(tmpRow);
        }
    }

    public void setNonWalkablePath(GridNode node_from, GridNode node_to) {
        for (int i = node_from.x; i <= node_to.x; i++) {
            for (int j = node_from.y; j <= node_to.y; j++)
                mMapGrid.get(i).get(j).setNodeCharId(NON_WALKABLE_PATH_CHAR);
        }
    }

    public boolean in_bounds(GridNode node) {
        return node.x >= 0 && node.x < mapWidth
                && node.y >= 0 && node.y < mapHeight;
    }

    public ArrayList<GridNode> neighbours(GridNode node) {
        int x = node.x;
        int y = node.y;


        // TODO: make sure x and y are in correct order
        // TODO: path blocker checks here or w/e
        if (mMapGrid.get(x).get(y).charId.equals(NON_WALKABLE_PATH_CHAR)) {
            return new ArrayList<>(); // return empty if its a path blocker
        }

        // test nodes
        GridNode node1 = new GridNode(x, y - 1, WALKABLE_PATH_CHAR, startPoint);
        GridNode node2 = new GridNode(x, y + 1, WALKABLE_PATH_CHAR, startPoint);
        GridNode node3 = new GridNode(x + 1, y, WALKABLE_PATH_CHAR, startPoint);
        GridNode node4 = new GridNode(x - 1, y + 1, WALKABLE_PATH_CHAR, startPoint);
        GridNode node5 = new GridNode(x - 1, y - 1, WALKABLE_PATH_CHAR, startPoint);
        GridNode node6 = new GridNode(x - 1, y + 1, WALKABLE_PATH_CHAR, startPoint);
        GridNode node7 = new GridNode(x + 1, y + 1, WALKABLE_PATH_CHAR, startPoint);
        GridNode node8 = new GridNode(x + 1, y - 1, WALKABLE_PATH_CHAR, startPoint);

        ArrayList<GridNode> nreturn = new ArrayList<>();

        if (in_bounds(node1))
            nreturn.add(node1);
        if (in_bounds(node2))
            nreturn.add(node2);
        if (in_bounds(node3))
            nreturn.add(node3);
        if (in_bounds(node4))
            nreturn.add(node4);
        if (in_bounds(node5))
            nreturn.add(node5);
        if (in_bounds(node6))
            nreturn.add(node6);
        if (in_bounds(node7))
            nreturn.add(node7);
        if (in_bounds(node8))
            nreturn.add(node8);

        return nreturn;
    }

    /**
     * Prints the map on screen
     */
    public void printMap(Context c, GoogleMap map) {
        for (int i = 0; i < mapHeight; i++) {
            for (int j = 0; j < mapWidth; j++) {
                // get point
                GridNode node = mMapGrid.get(i).get(j);

                int icon_id;

                if (node.charId.equals(NON_WALKABLE_PATH_CHAR)) {
                    icon_id = R.drawable.grid_cross;
                    continue;
                } else if (node.charId.equals("A")) {
                    icon_id = R.drawable.sfunetsecuredot;
                } else if (node.charId.equals("B")) {
                    icon_id = R.drawable.eduroamdot;
                } else if (node.charId.equals("@")) {
                    icon_id = R.drawable.path_dot;
                    continue;
                } else {
                    icon_id = R.drawable.map_grid_point;
//                    return;
                }

                map.addMarker(new MarkerOptions()
                                .position(MercatorProjection.fromPointToLatLng(node.node_position))
                                .icon(BitmapDescriptorFactory.fromResource(icon_id))
                                .title("Pos: " + node.x + ", " + node.y)
                );
            }
        }
    }
}