package me.gurinderhans.sfumaps.Factory;

import android.content.Context;
import android.graphics.Point;
import android.graphics.PointF;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import me.gurinderhans.sfumaps.MapTools;
import me.gurinderhans.sfumaps.PathMaker.PathMaker;

/**
 * Created by ghans on 15-07-29.
 */
public class MapGrid {

    public static final String TAG = MapGrid.class.getSimpleName();

    public static final float EACH_POINT_DIST = 1 / 8f;

    public final PointF startPoint;
    public final PointF endPoint;

    public final int mapGridSizeY; // rows
    public final int mapGridSizeX; // cols

    public ArrayList<ArrayList<GridNode>> mMapGrid = new ArrayList<>();

    public MapGrid(Context ctx, PointF startPoint, PointF endPoint) {

        this.startPoint = startPoint;
        this.endPoint = endPoint;

        this.mapGridSizeY = 8 * ((int) Math.abs(endPoint.y - startPoint.y));
        this.mapGridSizeX = 8 * ((int) Math.abs(endPoint.x - startPoint.x));

        Log.i(TAG, "created map grid size: " + mapGridSizeX + " gridX " + mapGridSizeY);

        // FIXME: create grid in another thread as it creates an overhead on the UI

        // TODO: handle exceptions and take care of filename
        List<Point> walkablePoints = new ArrayList<>();
        try {
            // parse json and map the grid
            JSONObject gridJsonData = new JSONObject(MapTools.loadFile(ctx, "map_grid.json"));
            // get walkable points
            JSONArray walkablePointsArray = gridJsonData.getJSONArray(PathMaker.WALKABLE_KEY);

            for (int i = 0; i < walkablePointsArray.length(); i++) {

                // split string to separate x,y coords
                String[] arrVal = walkablePointsArray.get(i).toString().split(",");

                // FIXME: a more efficient way to store and parse json ?
                if (arrVal.length == 2)
                    walkablePoints.add(new Point(Integer.parseInt(arrVal[0]), Integer.parseInt(arrVal[1])));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // create grid
        for (int x = 0; x < mapGridSizeX; x++) {
            ArrayList<GridNode> tmp = new ArrayList<>();
            for (int y = 0; y < mapGridSizeY; y++) {
                GridNode newNode = new GridNode(x, y, this);

                if (walkablePoints.contains(new Point(x, y)))
                    newNode.setWalkable(true);

                tmp.add(newNode);
            }

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

    public List<GridNode> getNeighbors(GridNode node) {
        List<GridNode> neighbors = new ArrayList<>();

        for (int x = -1; x <= 1; x++) {
            for (int y = -1; y <= 1; y++) {
                if (x == 0 && y == 0)
                    continue;

                int checkX = node.gridX + x;
                int checkY = node.gridY + y;

                if (checkX >= 0 && checkX < mapGridSizeX && checkY >= 0 && checkY < mapGridSizeY) {
                    neighbors.add(getNode(checkX, checkY));
                }

            }
        }

        return neighbors;
    }

}