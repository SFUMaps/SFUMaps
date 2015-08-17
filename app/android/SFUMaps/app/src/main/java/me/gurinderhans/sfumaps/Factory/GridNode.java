package me.gurinderhans.sfumaps.Factory;

import android.graphics.PointF;

/**
 * Created by ghans on 15-07-29.
 */
public class GridNode {

    public static final String TAG = GridNode.class.getSimpleName();

    // map world position
    public PointF projCoords;

    public boolean isWalkable = false;

    // array indices
    public final int gridX;
    public final int gridY;

    // node costs
    public float gCost = -1f;
    public float hCost = -1f;

    public GridNode parentNode = null;

    // @constructor
    public GridNode(int x, int y, MapGrid mapGrid) {
        this.gridX = x;
        this.gridY = y;
        this.projCoords = new PointF(mapGrid.startPoint.x + x * MapGrid.EACH_POINT_DIST, mapGrid.startPoint.y + y * MapGrid.EACH_POINT_DIST);
    }

    public float getFCost() {
        return gCost + hCost;
    }

    public void setWalkable(boolean walkable) {
        this.isWalkable = walkable;
    }

}
