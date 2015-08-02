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

    public final int mapGridSizeY; // rows
    public final int mapGridSizeX; // cols

    public ArrayList<ArrayList<GridNode>> mMapGrid = new ArrayList<>();

    public MapGrid(PointF startPoint, PointF endPoint) {
        this.startPoint = startPoint;
        this.endPoint = endPoint;

        this.mapGridSizeY = 8 * ((int) Math.abs(endPoint.y - startPoint.y));
        this.mapGridSizeX = 8 * ((int) Math.abs(endPoint.x - startPoint.x));

        Log.i(TAG, "created map grid size: " + mapGridSizeX + " gridX " + mapGridSizeY);

        // create the map grid
        for (int x = 0; x < mapGridSizeX; x++) {
            ArrayList<GridNode> tmp = new ArrayList<>();

            for (int y = 0; y < mapGridSizeY; y++)
                tmp.add(new GridNode(x, y, this));

            mMapGrid.add(tmp);
        }
    }

    public GridNode getNode(int x, int y) {
        return mMapGrid.get(x).get(y);
    }

    public GridNode getNode(Point p) {
        return mMapGrid.get(p.x).get(p.y);
    }

    public void createWalkablePath(GridNode node_from, GridNode node_to) {
        for (int x = node_from.gridX; x <= node_to.gridX; x++) {
            for (int y = node_from.gridY; y <= node_to.gridY; y++) {
                Log.i(TAG, "set gridX: " + x + " gridY: " + y + " to walkable");
                getNode(x, y).setWalkable(true);
            }
        }
    }

//    public boolean inBounds(Point p) {
//        return p.x >= 0 && p.x < mapGridSizeX
//                && p.y >= 0 && p.y < mapGridSizeY;
//    }

//    public List<GridNode> getNeighbours(GridNode node) {
//        int x = node.gridX;
//        int y = node.gridY;
//
//
//        // TODO: make sure gridX and gridY are in correct order
//        // TODO: path blocker checks here or w/e
//        if (!getNode(x, y).isWalkable) {
//            return new ArrayList<>(); // return empty if its a path blocker
//        }
//
//        // TODO: simplify this
//
//        // test neighbours
//        List<Point> points = new ArrayList<>();
//        points.add(new Point(x, y - 1));
//        points.add(new Point(x, y + 1));
//        points.add(new Point(x + 1, y));
//        points.add(new Point(x - 1, y + 1));
//        points.add(new Point(x - 1, y - 1));
//        points.add(new Point(x - 1, y + 1));
//        points.add(new Point(x + 1, y + 1));
//        points.add(new Point(x + 1, y - 1));
//
//        List<GridNode> neighbours = new ArrayList<>();
//
//        for (Point p : points) {
//            if (inBounds(p)) {
//
//                GridNode thisNode = getNode(p.x, p.y);
//
//                if (thisNode.isWalkable) {
//                    Log.i(TAG, "is valid neighbour, gridX: " + thisNode.gridX + " gridY: " + thisNode.gridY);
//                    neighbours.add(thisNode);
//                }
//            }
//        }
//
//        return neighbours;
//    }

    public List<GridNode> getMyNeighbours(GridNode node) {
        List<GridNode> neighbours = new ArrayList<>();

        for (int x = -1; x <= 1; x++) {
            for (int y = -1; y <= 1; y++) {
                if (x == 0 && y == 0)
                    continue;

                int checkX = node.gridX + x;
                int checkY = node.gridY + y;

                if (checkX >= 0 && checkX < mapGridSizeX && checkY >= 0 && checkY < mapGridSizeY) {
                    neighbours.add(getNode(checkX, checkY));
                }

            }
        }

        return neighbours;
    }

}