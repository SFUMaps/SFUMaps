package me.gurinderhans.sfumaps.factory.classes;

import android.content.Context;
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

	public static final float EACH_POINT_DIST = 1 / 8f; // each grid node point distance

	public final PointF startPoint; // grid map start point
	public final PointF endPoint; // grid map end point

	public final int mapGridSizeY; // rows
	public final int mapGridSizeX; // cols

	public ArrayList<ArrayList<GridNode>> mMapGrid = new ArrayList<>(); // map grid

	public MapGrid(Context ctx, PointF startPoint, PointF endPoint) {

		this.startPoint = startPoint;
		this.endPoint = endPoint;

		this.mapGridSizeY = 8 * ((int) Math.abs(endPoint.y - startPoint.y));
		this.mapGridSizeX = 8 * ((int) Math.abs(endPoint.x - startPoint.x));

		// create grid
		for (int x = 0; x < mapGridSizeX; x++) {
			ArrayList<GridNode> tmp = new ArrayList<>();
			for (int y = 0; y < mapGridSizeY; y++)
				tmp.add(new GridNode(this, x, y));

			mMapGrid.add(tmp);
		}

		/*// FIXME: load json file in another thread as it creates an overhead on the UI thread
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

				createWalkableArea(start, end);
			}

			JSONArray individualPoints = walkablePointsNode.getJSONArray(PathMaker.INDIVIDUAL_POINTS);
			// plot the individual points
			for (int i = 0; i < individualPoints.length(); i++) {
				String[] xy = individualPoints.getString(i).split(",");
				getNode(Integer.parseInt(xy[0]), Integer.parseInt(xy[1])).setWalkable(true);
			}

		} catch (JSONException e) {
			e.printStackTrace();
		}*/
	}

	public GridNode getNode(int x, int y) {
		return mMapGrid.get(x).get(y);
	}

	public GridNode getNode(Point p) {
		return mMapGrid.get(p.x).get(p.y);
	}

	public void createWalkableArea(Point indicesFrom, Point indicesTo) {
		int distX = indicesTo.x - indicesFrom.x;
		int distY = indicesTo.y - indicesFrom.y;

		// find top left point from the two
		Point topLeft = (!(distX >= 0 && distY >= 0)) ? indicesTo : indicesFrom;

		Log.i(TAG, "topLeft: " + topLeft);
		Log.i(TAG, "from: " + indicesFrom + ", to: " + indicesTo);

		for (int x = 0; x < Math.abs(distX); x++)
			for (int y = 0; y < Math.abs(distY); y++)
				getNode(topLeft.x + x, topLeft.y + y).setWalkable(true);

	}

	public List<GridNode> getNeighbors(GridNode node) {
		List<GridNode> neighbors = new ArrayList<>();

		for (int x = -1; x <= 1; x++) {
			for (int y = -1; y <= 1; y++) {

				if (x == 0 && y == 0)
					continue;

				int checkX = node.gridX + x;
				int checkY = node.gridY + y;

				if (checkX >= 0 && checkX < mapGridSizeX && checkY >= 0 && checkY < mapGridSizeY)
					neighbors.add(getNode(checkX, checkY));
			}
		}
		return neighbors;
	}

	public static class GridNode {

		public static final String TAG = GridNode.class.getSimpleName();

		// map world position
		public final PointF projCoords;

		// array indices
		public final int gridX;
		public final int gridY;

		// node costs
		public float gCost = -1f;
		public float hCost = -1f;
		public GridNode parentNode = null;

		// private
		private boolean isWalkable = false;

		// @constructor
		public GridNode(MapGrid mapGrid, int x, int y) {
			this.gridX = x;
			this.gridY = y;
			this.projCoords = new PointF(mapGrid.startPoint.x + x * EACH_POINT_DIST, mapGrid.startPoint.y + y * EACH_POINT_DIST);
		}

		public float getFCost() {
			return gCost + hCost;
		}

		public boolean isWalkable() {
			return isWalkable;
		}

		public void setWalkable(boolean walkable) {
			this.isWalkable = walkable;
		}

	}
}