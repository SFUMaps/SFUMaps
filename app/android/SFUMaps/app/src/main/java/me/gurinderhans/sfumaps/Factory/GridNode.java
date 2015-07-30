package me.gurinderhans.sfumaps.Factory;

import android.graphics.PointF;

import java.util.ArrayList;

/**
 * Created by ghans on 15-07-29.
 */
public class GridNode {
    public PointF node_position;
    public String charId;

    public final int x;
    public final int y;

    // node costs
    public float gcost = -1f;
    public float hcost = -1f;
    public float fcost = -1f;

    public GridNode parentNode = null;

    // @constructor
    public GridNode(int x, int y, String charId, PointF startPoint) {
        this.node_position = new PointF(startPoint.x + x * MapGrid.EACH_POINT_DIST, startPoint.y + y * MapGrid.EACH_POINT_DIST);
        this.charId = charId;

        this.x = x;
        this.y = y;
    }

    public void setNodeCharId(String charId) {
        this.charId = charId;
    }

    public GridNode cost(GridNode start, GridNode end) {
        this.gcost = dist(this, start);
        this.hcost = dist(this, end);
        this.fcost = this.gcost + this.hcost;

        return this;
    }

    public static float dist(GridNode a, GridNode b) {
        float dX = Math.abs(a.x - b.x);
        float dY = Math.abs(a.y - b.y);

        if (dX > dY)
            return 1.4f * dY + (dX - dY);

        return 1.4f * dX + (dY - dX);
    }

    public static int searchNode(GridNode node, ArrayList<GridNode> node_list) {
        for (int i = 0; i < node_list.size(); i++) {
            if (node.x == node_list.get(i).x && node.y == node_list.get(i).y) {
                return i;
            }
        }
        return -1;
    }

    public static int getMinFcostNodeIndex(ArrayList<GridNode> list) {
        float minCost = Float.MAX_VALUE;
        int returnIndex = -1;
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).fcost < minCost) {
                minCost = list.get(i).fcost;
                returnIndex = i;
            }
        }

        return returnIndex;
    }
}
