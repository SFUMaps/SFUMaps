package me.gurinderhans.sfumaps.Factory;

import android.graphics.Point;
import android.graphics.PointF;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ghans on 15-07-29.
 */
public class MapGrid {

    public static final String TAG = MapGrid.class.getSimpleName();

    public static final float EACH_POINT_DIST = 0.125f;

    public final PointF startPoint;
    public final PointF endPoint;

    public final int rows;
    public final int cols;

    public ArrayList<ArrayList<GridNode>> mMapGrid = new ArrayList<>();

    public MapGrid(PointF startPoint, PointF endPoint) {
        this.startPoint = startPoint;
        this.endPoint = endPoint;

        this.rows = 8 * ((int) Math.abs(endPoint.y - startPoint.y));
        this.cols = 8 * ((int) Math.abs(endPoint.x - startPoint.x));

        Log.i(TAG, "map size: " + cols + " x " + rows);

        // create the map grid
        for (int x = 0; x < cols; x++) {
            ArrayList<GridNode> tmp = new ArrayList<>();
            for (int y = 0; y < rows; y++) {
                tmp.add(new GridNode(x, y, this));
            }
            mMapGrid.add(tmp);
        }
    }

    public GridNode get_node(int x, int y) {
        return mMapGrid.get(x).get(y);
    }

    public void createWalkablePath(GridNode node_from, GridNode node_to) {
        for (int x = node_from.x; x <= node_to.x; x++) {
            for (int y = node_from.y; y <= node_to.y; y++) {
                Log.i(TAG, "x: " + x + " y: " + y);
                get_node(x, y).setWalkable(true);
            }
        }
    }

    public boolean in_bounds(Point p) {
        return p.x >= 0 && p.x < cols
                && p.y >= 0 && p.y < rows;
    }

    public List<GridNode> getNeighbours(GridNode node) {
        int x = node.x;
        int y = node.y;


        // TODO: make sure x and y are in correct order
        // TODO: path blocker checks here or w/e
        if (!get_node(x, y).isWalkable) {
            return new ArrayList<>(); // return empty if its a path blocker
        }

        // TODO: simplify this

        // test neighbours
        List<Point> points = new ArrayList<>();
        points.add(new Point(x, y - 1));
        points.add(new Point(x, y + 1));
        points.add(new Point(x + 1, y));
        points.add(new Point(x - 1, y + 1));
        points.add(new Point(x - 1, y - 1));
        points.add(new Point(x - 1, y + 1));
        points.add(new Point(x + 1, y + 1));
        points.add(new Point(x + 1, y - 1));

        List<GridNode> neighbours = new ArrayList<>();

        for (Point p : points) {
            if (in_bounds(p)) {

                GridNode thisNode = get_node(p.x, p.y);

                if (thisNode.isWalkable) {
                    Log.i(TAG, "node, x: " + thisNode.x + " y: " + thisNode.y);
                    neighbours.add(thisNode);
                }
            }
        }

        return neighbours;
    }

}