package me.gurinderhans.sfumaps.PathMaker;

import android.graphics.Point;
import android.graphics.PointF;
import android.util.Pair;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.GroundOverlay;
import com.google.android.gms.maps.model.GroundOverlayOptions;
import com.google.android.gms.maps.model.LatLngBounds;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import me.gurinderhans.sfumaps.Factory.MapGrid;
import me.gurinderhans.sfumaps.MapTools;
import me.gurinderhans.sfumaps.MercatorProjection;
import me.gurinderhans.sfumaps.R;

/**
 * Created by ghans on 15-08-10.
 */
public class PathMaker implements MapWrapperLayout.OnDragListener {

	public static final String TAG = PathMaker.class.getSimpleName();

	public static final String WALKABLE_KEY = "walkable";
	public static final String INDIVIDUAL_POINTS = "points";
	public static final String POINT_RECTS = "rects";

	public final GoogleMap mMap;
	public final MapGrid mGrid;

	// tracks all the "green" or "blue" markers placed on the grid so that the red markers don't override these
	public static List<Point> walkableMarkerIndices = new ArrayList<>();

	boolean isEditingMap = false;

	boolean boxMode = false;

	JSONObject gridRoot = new JSONObject();

	Point boxStartPoint = new Point(-1, -1), boxEndPoint = new Point(-1, -1);

	public PathMaker(CustomMapFragment mapFragment, GoogleMap map, View editButton,
	                 final View exportButton, final View boxButton, final MapGrid grid) {
		this.mMap = map;
		this.mGrid = grid;


		// read json file
//		MapTools.loadFile()


		// create the json tree structure
		try {
			gridRoot.put(WALKABLE_KEY, new JSONObject());
			gridRoot.getJSONObject(WALKABLE_KEY).put(INDIVIDUAL_POINTS, new JSONArray());
			gridRoot.getJSONObject(WALKABLE_KEY).put(POINT_RECTS, new JSONArray());
		} catch (JSONException e) {
			e.printStackTrace();
		}

		/* set input listeners on views */

		mapFragment.setOnDragListener(this);

		editButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {

				isEditingMap = !isEditingMap;

				exportButton.setVisibility(isEditingMap ? View.VISIBLE : View.INVISIBLE);
				boxButton.setVisibility(isEditingMap ? View.VISIBLE : View.INVISIBLE);

				((ImageButton) v).setImageResource(isEditingMap ? android.R.drawable.ic_menu_close_clear_cancel : android.R.drawable.ic_menu_edit);

				mMap.getUiSettings().setScrollGesturesEnabled(!isEditingMap);
			}
		});

		// export map path
		exportButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				try {
					for (int x = 0; x < mGrid.mapGridSizeX; x++)
						for (int y = 0; y < mGrid.mapGridSizeY; y++)
							if (mGrid.getNode(x, y).isWalkable)
								gridRoot.getJSONObject(WALKABLE_KEY).getJSONArray(INDIVIDUAL_POINTS).put(x + "," + y);

					// create file
					MapTools.createFile("map_grid.json", gridRoot.toString(4));
					Toast.makeText(v.getContext(), "Done", Toast.LENGTH_SHORT).show();

				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		});

		boxButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {

				boxMode = !boxMode;

				/*Pair<Point, Point> boxPoints = getFixedBoxBounds(boxStartPoint, boxEndPoint);
				if (boxPoints != null) {
					Point topLeft = boxPoints.first;
					Point bottomRight = boxPoints.second;

					// draw the box outline
					for (int i = topLeft.x; i <= bottomRight.x; i++)
						for (int j = topLeft.y; j <= bottomRight.y; j++)
							if (i == topLeft.x || i == bottomRight.x || j == topLeft.y || j == bottomRight.y) {
								mMap.addMarker(new MarkerOptions()
										.position(MercatorProjection.fromPointToLatLng(mGrid.getNode(i, j).projCoords))
										.icon(BitmapDescriptorFactory.fromResource(R.drawable.box_rect_outline))
										.anchor(0.5f, 0.5f));
								walkableMarkerIndices.add(new Point(i, j));
							}

					try {
						gridRoot.getJSONObject(WALKABLE_KEY).getJSONArray(POINT_RECTS)
								.put(boxStartPoint.x + "," + boxStartPoint.y + "|" + boxEndPoint.x + "," + boxEndPoint.y);
						Toast.makeText(v.getContext(), "Boxed!", Toast.LENGTH_SHORT).show();
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}*/
			}
		});
	}


	/**
	 * onDrag manage click vs drag to plot the corresponding marker on the grid
	 */

//	private float mDownX, mDownY;
	private Point initialPoint;
	private static final float SCROLL_THRESHOLD = 10;
	private boolean isOnClick;

	GroundOverlay mSelectedArea;

	@Override
	public void onDrag(MotionEvent ev) {
		if (!isEditingMap)
			return;

		Point gridPointIndices = getGridPoint(ev.getX(), ev.getY());

		switch (ev.getAction() & MotionEvent.ACTION_MASK) {
			case MotionEvent.ACTION_DOWN:
				initialPoint = new Point((int) ev.getX(), (int) ev.getY());
				isOnClick = true;
				mSelectedArea = mMap.addGroundOverlay(new GroundOverlayOptions()
								.positionFromBounds(new LatLngBounds(mMap.getProjection().fromScreenLocation(initialPoint), mMap.getProjection().fromScreenLocation(initialPoint)))
								.image(BitmapDescriptorFactory.fromResource(R.drawable.box_rect_outline))
								.zIndex(10000)
								.transparency(0.25f)
				);
				break;
			case MotionEvent.ACTION_CANCEL:
			case MotionEvent.ACTION_UP:
				if (isOnClick) {
					// track the last two points on 'click'
					boxStartPoint = boxEndPoint;
					boxEndPoint = gridPointIndices;
					// add the green point indicating this is a box point
					/*mMap.addMarker(new MarkerOptions()
							.position(MercatorProjection.fromPointToLatLng(mGrid.getNode(gridPointIndices).projCoords))
							.icon(BitmapDescriptorFactory.fromResource(R.drawable.box_rect_outline))
							.anchor(0.5f, 0.5f));*/
				}
				break;
			case MotionEvent.ACTION_MOVE:
				if (boxMode) {

				}

				try {
					PointF pointF = mGrid.getNode(gridPointIndices).projCoords;
					mSelectedArea.setPositionFromBounds(
							new LatLngBounds(MercatorProjection.fromPointToLatLng(pointF), mMap.getProjection().fromScreenLocation(initialPoint)));
				} catch (Exception e) {
					e.printStackTrace();
				}


				/*if (Math.abs(mDownX - ev.getX()) > SCROLL_THRESHOLD || Math.abs(mDownY - ev.getY()) > SCROLL_THRESHOLD) {
					isOnClick = false;
					// add the default blue point indicating this is an individual point
					mGrid.getNode(gridPointIndices).setWalkable(true);
					mMap.addMarker(new MarkerOptions()
							.position(MercatorProjection.fromPointToLatLng(mGrid.getNode(gridPointIndices).projCoords))
							.icon(BitmapDescriptorFactory.fromResource(R.drawable.map_path))
							.anchor(0.5f, 0.5f));

					walkableMarkerIndices.add(gridPointIndices);
				}*/
				break;
			default:
				break;
		}
	}


	/**
	 * Given the screen coordinate it computes the closes grid node to the screen point
	 *
	 * @param x - x value of the screen coordinate
	 * @param y - y value of the screen coordinate
	 * @return
	 */
	public Point getGridPoint(float x, float y) {

		PointF mapPoint = MercatorProjection.fromLatLngToPoint(
				mMap.getProjection().fromScreenLocation(new Point((int) x, (int) y)));
		PointF gridFirstPoint = mGrid.getNode(0, 0).projCoords;

		// convert dist to grid index and return the position of the node at that index
		return new Point((int) ((mapPoint.x - gridFirstPoint.x) / MapGrid.EACH_POINT_DIST), (int) ((mapPoint.y - gridFirstPoint.y) / MapGrid.EACH_POINT_DIST));
	}

	/**
	 * Given two points of a rect it computes the Top Left and Bottom Right points of that rect
	 *
	 * @param topLeft     - the assume top left point of the rectangle
	 * @param bottomRight - assumed bottom right point of the rectangle
	 * @return
	 */
	private Pair<Point, Point> getFixedBoxBounds(Point topLeft, Point bottomRight) {

		// check for point validity
		if (topLeft.equals(-1, -1) || bottomRight.equals(-1, -1))
			return null;

		int xDiff = bottomRight.x - topLeft.x;
		int yDiff = bottomRight.y - topLeft.y;

		if (xDiff > 0 && yDiff > 0)
			return Pair.create(topLeft, bottomRight);

		if (xDiff < 0 && yDiff < 0)
			return Pair.create(bottomRight, topLeft);

		if (xDiff < 0 || yDiff < 0) {

			Point tl = new Point(topLeft.x + xDiff, topLeft.y);
			Point br = new Point(bottomRight.x - xDiff, bottomRight.y);

			return (xDiff < 0) ? Pair.create(tl, br) : Pair.create(br, tl);
		}

		// last case will be if the rectangle is one dimensional
		return Pair.create(topLeft, bottomRight);
	}

}