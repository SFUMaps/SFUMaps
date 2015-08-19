package me.gurinderhans.sfumaps.factory.classes;

import android.content.Context;
import android.graphics.Point;
import android.graphics.PointF;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import me.gurinderhans.sfumaps.utils.MapTools;
import me.gurinderhans.sfumaps.devtools.pathmaker.PathMaker;

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

		// create grid
		for (int x = 0; x < mapGridSizeX; x++) {
			ArrayList<GridNode> tmp = new ArrayList<>();
			for (int y = 0; y < mapGridSizeY; y++)
				tmp.add(new GridNode(x, y, this));

			mMapGrid.add(tmp);
		}

		Log.i(TAG, "created map grid size: " + mapGridSizeX + " gridX " + mapGridSizeY);


		// FIXME: load json file in another thread as it creates an overhead on the UI thread
		// TODO: handle exceptions and filename specs
		try {
			// parse json and map the grid
			JSONObject walkablePointsNode = new JSONObject(MapTools.loadFile(ctx, "map_grid.json"))
					.getJSONObject(PathMaker.WALKABLE_KEY);

			JSONArray boxRects = walkablePointsNode.getJSONArray(PathMaker.BOX_RECTS);
			// unwrap the box and add to walkablePoints array
			for (int i = 0; i < boxRects.length(); i++) {
				String[] boxString = boxRects.getString(i).split(",");
				Point start = new Point(Integer.parseInt(boxString[0]), Integer.parseInt(boxString[1]));
				Point end = new Point(Integer.parseInt(boxString[2]), Integer.parseInt(boxString[3]));

				createWalkablePath(start, end);
			}

			JSONArray individualPoints = walkablePointsNode.getJSONArray(PathMaker.INDIVIDUAL_POINTS);
			// plot the individual points
			for (int i = 0; i < individualPoints.length(); i++) {
				String[] xy = individualPoints.getString(i).split(",");
				getNode(Integer.parseInt(xy[0]), Integer.parseInt(xy[1])).setWalkable(true);
			}

		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	public GridNode getNode(int x, int y) {
		return mMapGrid.get(x).get(y);
	}

	public GridNode getNode(Point p) {
		return mMapGrid.get(p.x).get(p.y);
	}

	public void createWalkablePath(Point indicesFrom, Point indicesTo) {
		for (int x = indicesFrom.x; x <= indicesTo.x; x++) {
			for (int y = indicesFrom.y; y <= indicesTo.y; y++) {
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