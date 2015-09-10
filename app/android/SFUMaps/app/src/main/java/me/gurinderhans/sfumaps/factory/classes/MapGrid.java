package me.gurinderhans.sfumaps.factory.classes;

import android.graphics.Point;
import android.graphics.PointF;

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

	public MapGrid(PointF startPoint, PointF endPoint) {

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
	}

	public GridNode getNode(int x, int y) {
		return mMapGrid.get(x).get(y);
	}

	public GridNode getNode(Point p) {
		return mMapGrid.get(p.x).get(p.y);
	}

	public void createWalkableArea(Point indicesFrom, Point indicesTo, float pathRotation) {

		if (pathRotation == -45f) {
			for (int x = indicesFrom.x; x >= indicesTo.x; x--)
				for (int y = indicesTo.y; y >= indicesFrom.y; y--)
					if (Math.abs(x - indicesTo.x) == Math.abs(y - indicesTo.y))
						getNode(x, y).setWalkable(true);

			return;
		}

		for (int x = indicesFrom.x; x <= indicesTo.x; x++)
			for (int y = indicesFrom.y; y <= indicesTo.y; y++) {
				if (pathRotation == 45f) {
					if (Math.abs(x - indicesFrom.x) == Math.abs(y - indicesFrom.y))
						getNode(x, y).setWalkable(true);

					continue;
				}

				getNode(x, y).setWalkable(true);
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