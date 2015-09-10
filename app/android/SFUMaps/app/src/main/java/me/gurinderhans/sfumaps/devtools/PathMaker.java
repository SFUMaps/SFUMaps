package me.gurinderhans.sfumaps.devtools;

import android.graphics.Point;
import android.graphics.PointF;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.GroundOverlay;
import com.google.android.gms.maps.model.GroundOverlayOptions;
import com.google.android.gms.maps.model.LatLng;

import me.gurinderhans.sfumaps.R;
import me.gurinderhans.sfumaps.factory.classes.MapGrid;
import me.gurinderhans.sfumaps.factory.classes.MapPath;
import me.gurinderhans.sfumaps.ui.views.CustomMapFragment;
import me.gurinderhans.sfumaps.ui.views.MapWrapperLayout.OnDragListener;

import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;
import static me.gurinderhans.sfumaps.factory.classes.MapPath.mAllMapPaths;
import static me.gurinderhans.sfumaps.utils.MapTools.LatLngDistance;
import static me.gurinderhans.sfumaps.utils.MercatorProjection.fromLatLngToPoint;
import static me.gurinderhans.sfumaps.utils.MercatorProjection.fromPointToLatLng;

/**
 * Created by ghans on 15-08-10.
 */
public class PathMaker implements OnDragListener, OnClickListener {

	public static final String TAG = PathMaker.class.getSimpleName();
	public static boolean isEditingMap = false;
	private static PathMaker mInstance = null;

	// UI
	private final GoogleMap mGoogleMap;
	private final MapGrid mGrid;
	private final FragmentActivity mActivity;
	private GroundOverlay mTmpSelectedOverlay;

	// Logic
	boolean deleteMode = false;
	Point mPathStartGridIndices;
	Point mPathEndGridIndices;

	// @constructor
	PathMaker(FragmentActivity activity, GoogleMap map, MapGrid grid) {

		this.mGoogleMap = map;
		this.mGrid = grid;
		this.mActivity = activity;

		// ------------
		mActivity.findViewById(R.id.dev_overlay).setVisibility(VISIBLE);
		mActivity.findViewById(R.id.edit_map_path).setOnClickListener(this);
		mActivity.findViewById(R.id.delete_path_button).setOnClickListener(this);

		// ------------
		((CustomMapFragment) mActivity.getSupportFragmentManager().findFragmentById(R.id.map)).setOnDragListener(this);
	}

	// initializer
	public static void createPathMaker(FragmentActivity activity, GoogleMap map, MapGrid grid) {
		if (mInstance == null)
			mInstance = new PathMaker(activity, map, grid);
	}

	@Override
	public void onDrag(MotionEvent ev) {
		if (!isEditingMap)
			return;

		Point currentDragPointIndices = getGridIndices(ev.getX(), ev.getY());

		switch (ev.getAction() & MotionEvent.ACTION_MASK) {
			case MotionEvent.ACTION_DOWN:
				if (!deleteMode) {
					mPathStartGridIndices = currentDragPointIndices;
					mTmpSelectedOverlay = mGoogleMap.addGroundOverlay(new GroundOverlayOptions()
									.position(fromPointToLatLng(mGrid.getNode(mPathStartGridIndices).projCoords), 10000)
									.image(BitmapDescriptorFactory.fromResource(R.drawable.devtools_pathmaker_path_drawable))
									.transparency(0.2f)
									.anchor(0, 0.5f)
									.zIndex(10000)
					);
				}
				break;
			case MotionEvent.ACTION_UP:
				if (!deleteMode) {

					MapPath mapPath = new MapPath();
					mapPath.setStartPoint(mPathStartGridIndices);
					mapPath.setEndPoint(mPathEndGridIndices); // set size
					mapPath.setMapEditOverlay(mTmpSelectedOverlay);
					mapPath.setRotation(mTmpSelectedOverlay.getBearing());
//					mapPath.saveInBackground();
					Log.i(TAG, "SAVING -> start: " + mPathStartGridIndices + ", end: " + mPathEndGridIndices);

					mAllMapPaths.add(mapPath);

					mTmpSelectedOverlay = null;
				}

				break;
			case MotionEvent.ACTION_MOVE:

				if (!deleteMode && (Math.abs(currentDragPointIndices.x - mPathStartGridIndices.x) + Math.abs(currentDragPointIndices.y - mPathStartGridIndices.y)) >= 1) {

					Point nodeDist = new Point(
							currentDragPointIndices.x - mPathStartGridIndices.x,
							currentDragPointIndices.y - mPathStartGridIndices.y
					);

//					double dimsX = Math.abs(nodeDist.x) * mGrid.gridPointDist;
//					double dimsY = Math.abs(nodeDist.y) * mGrid.gridPointDist;
//					int grid_nodes_down = (int) ((dimsX + dimsY) / mGrid.gridPointDist);

					double dragAngle = (Math.atan2(nodeDist.y, nodeDist.x)) * 180 / Math.PI; // convert to degrees
					if (dragAngle > 67.5 && dragAngle <= 112.5) { // down
						mTmpSelectedOverlay.setBearing(90);
						mPathEndGridIndices = new Point(mPathStartGridIndices.x, currentDragPointIndices.y);

					} else if (dragAngle > -112.5 && dragAngle <= -67.5) { // up
						mTmpSelectedOverlay.setBearing(270);
						mPathEndGridIndices = new Point(mPathStartGridIndices.x, currentDragPointIndices.y);

					} else if (dragAngle > -22.5 && dragAngle <= 22.5) { // right
						mTmpSelectedOverlay.setBearing(0);
						mPathEndGridIndices = new Point(currentDragPointIndices.x, mPathStartGridIndices.y);

					} else if ((dragAngle <= -157.5 && dragAngle > -180) || (dragAngle > 157.5 && dragAngle <= 180)) { // left
						mTmpSelectedOverlay.setBearing(180);
						mPathEndGridIndices = new Point(currentDragPointIndices.x, mPathStartGridIndices.y);

					}

//					mTmpSelectedOverlay.setDimensions((float) (dimsX + dimsY), 10000);

					/*// diagonals
					else if (dragAngle > 22.5 && dragAngle <= 67.5) { // downright
						mTmpSelectedOverlay.setBearing(45);
						mPathEndGridIndices = new Point(mPathStartGridIndices.x + grid_nodes_down, mPathStartGridIndices.y + grid_nodes_down);

					} else if (dragAngle > 112.5 && dragAngle <= 157.5) { // downleft
						mTmpSelectedOverlay.setBearing(135);
						mPathEndGridIndices = new Point(mPathStartGridIndices.x - grid_nodes_down, mPathStartGridIndices.y + grid_nodes_down);

					} else if (dragAngle > -67.5 && dragAngle <= -22.5) { // topright
						mTmpSelectedOverlay.setBearing(-45);
						mPathEndGridIndices = new Point(mPathStartGridIndices.x + grid_nodes_down, mPathStartGridIndices.y - grid_nodes_down);

					} else { // topleft
						mTmpSelectedOverlay.setBearing(-135);
						mPathEndGridIndices = new Point(mPathStartGridIndices.x - grid_nodes_down, mPathStartGridIndices.y - grid_nodes_down);

					}*/
				}

				// delete mode
				else {

					MapPath toRemove = null;
					for (MapPath path : mAllMapPaths)
						if (path.getMapEditOverlay().getBounds().contains(mGoogleMap.getProjection().fromScreenLocation(new Point((int) ev.getX(), (int) ev.getY())))) {
							path.getMapEditOverlay().remove();
							path.deleteInBackground();
							toRemove = path;
							break;
						}
					if (toRemove != null)
						mAllMapPaths.remove(toRemove);

				}
				break;
			default:
				break;
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.edit_map_path:
				toggleEditing((ImageButton) v);
				break;
			case R.id.delete_path_button:
				deleteMode = !deleteMode;
				// change button background
				v.setBackgroundResource(deleteMode ? android.R.color.holo_green_light : android.R.color.holo_red_light);
				break;
			default:
				break;
		}
	}

	/* edit map grid toggle */
	private void toggleEditing(ImageButton editButton) {

		// tell map path data isn't available yet, so try again later
		if (mAllMapPaths.isEmpty())
			Toast.makeText(mActivity.getApplicationContext(), "Map Path data isn't yet available.", Toast.LENGTH_LONG).show();

		isEditingMap = !isEditingMap;

		// show / hide the overlays
		for (MapPath path : mAllMapPaths)
			path.getMapEditOverlay().setVisible(isEditingMap);

		mGoogleMap.getUiSettings().setScrollGesturesEnabled(!isEditingMap);

		// change the button icon / background
		editButton.setImageResource(isEditingMap ? android.R.drawable.ic_menu_close_clear_cancel : android.R.drawable.ic_menu_edit);
		editButton.setBackgroundResource(isEditingMap ? android.R.color.holo_green_light : android.R.color.holo_red_light);

		// hide edit controls
		mActivity.findViewById(R.id.delete_path_button).setVisibility(isEditingMap ? VISIBLE : INVISIBLE);
	}

	/**
	 * Given the screen coordinate it computes the closes grid node to the screen point
	 *
	 * @param screenX - x value of the screen coordinate
	 * @param screenY - y value of the screen coordinate
	 * @return - The (x, y) indices of the grid array
	 */
	private Point getGridIndices(float screenX, float screenY) {

		PointF mapPoint = fromLatLngToPoint(
				mGoogleMap.getProjection().fromScreenLocation(new Point((int) screenX, (int) screenY)));
		PointF gridFirstPoint = mGrid.getNode(0, 0).projCoords;

		// convert dist to grid index and return the position of the node at that index
		return new Point((int) ((mapPoint.x - gridFirstPoint.x) / MapGrid.EACH_POINT_DIST), (int) ((mapPoint.y - gridFirstPoint.y) / MapGrid.EACH_POINT_DIST));
	}


	/**
	 * Calculate the horizontal and vertical distance between points a and b
	 *
	 * @param dragStartCoordinates   - screen point
	 * @param dragCurrentCoordinates - indices
	 * @return - {@link Point} object containing the horizontal and vertical distance
	 */
	private PointF getXYDist(LatLng dragStartCoordinates, LatLng dragCurrentCoordinates) {

		// calculate the middle corner point
		PointF dragStart = fromLatLngToPoint(dragStartCoordinates);
		PointF dragCurrent = fromLatLngToPoint(dragCurrentCoordinates);

		// the middle corner point
		dragCurrent.set(dragCurrent.x, dragStart.y);

		LatLng middleCornerPoint = fromPointToLatLng(dragCurrent);

		// horizontal distance
		float hDist = (float) LatLngDistance(dragStartCoordinates.latitude, dragStartCoordinates.longitude, middleCornerPoint.latitude, middleCornerPoint.longitude);

		// vertical distance
		float vDist = (float) LatLngDistance(dragCurrentCoordinates.latitude, dragCurrentCoordinates.longitude, middleCornerPoint.latitude, middleCornerPoint.longitude);

		return new PointF(hDist, vDist);
	}

}