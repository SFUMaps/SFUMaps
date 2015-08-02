package me.gurinderhans.sfumaps.Factory;

import android.graphics.Point;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by ghans on 15-08-02.
 */
public class PathFinder {

    public static final String TAG = PathFinder.class.getSimpleName();

    // @constructor - NOT USED
    private PathFinder() {
    }

    public static List<GridNode> getPath(Point from, Point to, MapGrid grid) {
        GridNode startNode = grid.getNode(from);
        GridNode targetNode = grid.getNode(to);

        List<GridNode> openSet = new ArrayList<>();
        List<GridNode> closedSet = new ArrayList<>();

        openSet.add(startNode);

        while (openSet.size() > 0) {

            // get node with min fcost from openset
            GridNode currentNode = openSet.get(0);
            for (int i = 1; i < openSet.size(); i++) {
                if (openSet.get(i).getFCost() < currentNode.getFCost() || openSet.get(i).getFCost() == currentNode.getFCost() && openSet.get(i).hCost < currentNode.hCost) {
                    currentNode = openSet.get(i);
                }
            }

            openSet.remove(currentNode);
            closedSet.add(currentNode);

            if (currentNode.gridX == targetNode.gridX && currentNode.gridY == targetNode.gridY) {
                // retrace path and return it
                List<GridNode> path = new ArrayList<>();
                GridNode thisNode = targetNode;
                while (thisNode != startNode) {
                    path.add(thisNode);
                    thisNode = thisNode.parentNode;
                }
                Collections.reverse(path);

                return path;
            }

            for (GridNode neighborNode : grid.getMyNeighbours(currentNode)) {

                if (!neighborNode.isWalkable || closedSet.contains(neighborNode))
                    continue;

                float newMovementCost = currentNode.gCost + dist(currentNode, neighborNode);
                if (newMovementCost < neighborNode.gCost || !openSet.contains(neighborNode)) {
                    neighborNode.gCost = newMovementCost;
                    neighborNode.hCost = dist(neighborNode, targetNode);
                    neighborNode.parentNode = currentNode;

                    if (!openSet.contains(neighborNode))
                        openSet.add(neighborNode);
                }
            }
        }

        return null;
    }

    public static float dist(GridNode a, GridNode b) {
        float dstX = Math.abs(a.gridX - b.gridX);
        float dstY = Math.abs(a.gridY - b.gridY);

        if (dstX > dstY)
            return 1.4f * dstY + (dstX - dstY);

        return 1.4f * dstX + (dstY - dstX);
    }
}
