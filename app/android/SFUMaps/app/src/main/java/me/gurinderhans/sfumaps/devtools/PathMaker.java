package me.gurinderhans.sfumaps.devtools;

import android.graphics.Point;
import android.graphics.PointF;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.GroundOverlay;
import com.google.android.gms.maps.model.GroundOverlayOptions;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;

import me.gurinderhans.sfumaps.R;
import me.gurinderhans.sfumaps.factory.classes.MapGrid;
import me.gurinderhans.sfumaps.factory.classes.MapPath;
import me.gurinderhans.sfumaps.ui.views.CustomMapFragment;
import me.gurinderhans.sfumaps.ui.views.MapWrapperLayout.OnDragListener;
import me.gurinderhans.sfumaps.utils.MapTools;
import me.gurinderhans.sfumaps.utils.MercatorProjection;

import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;

/**
 * Created by ghans on 15-08-10.
 */
public class PathMaker implements OnDragListener, OnClickListener {

	public static final String TAG = PathMaker.class.getSimpleName();

	//
	public static boolean isEditingMap = false;
	private static PathMaker mInstance = null;
	private static final int MOVE_THRESHOLD = 2; // grid units

	// UI
	private ImageButton deleteButton;
	private GroundOverlay mTmpSelectedOverlay;
	private final GoogleMap mGoogleMap;
	private final MapGrid mGrid;
	private final FragmentActivity mActivity;

	// Logic
	boolean deleteMode = false;
	Point mBoxStartGridIndices;
	private List<MapPath> mNewThisSessionPaths = new ArrayList<>();

	// @constructor
	PathMaker(FragmentActivity activity, GoogleMap map, MapGrid grid) {

		this.mGoogleMap = map;
		this.mGrid = grid;
		this.mActivity = activity;

		// ------------
		mActivity.findViewById(R.id.dev_overlay).setVisibility(VISIBLE);

		// ------------
		deleteButton = (ImageButton) mActivity.findViewById(R.id.delete_path_button);

		//
		mActivity.findViewById(R.id.edit_map_path).setOnClickListener(this);
		deleteButton.setOnClickListener(this);

		((CustomMapFragment) mActivity.getSupportFragmentManager().findFragmentById(R.id.map)).setOnDragListener(this);

		// TODO: 15-09-07 load the map edit data here.
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
				mBoxStartGridIndices = currentDragPointIndices;
				mTmpSelectedOverlay = mGoogleMap.addGroundOverlay(new GroundOverlayOptions()
								.position(MercatorProjection.fromPointToLatLng(mGrid.getNode(currentDragPointIndices).projCoords), 10000)
								.image(BitmapDescriptorFactory.fromResource(R.drawable.green_bg))
								.transparency(0.2f)
								.zIndex(10000)
								.anchor(0, 0)
				);
				break;
			case MotionEvent.ACTION_UP:
				if (mTmpSelectedOverlay != null) {

					MapPath mapPath = new MapPath();
					mapPath.setStartPoint(mBoxStartGridIndices);
					mapPath.setEndPoint(currentDragPointIndices);
					mapPath.setMapOverlay(mTmpSelectedOverlay);

					mNewThisSessionPaths.add(mapPath);

					mTmpSelectedOverlay = null;
				}

				break;
			case MotionEvent.ACTION_MOVE:

				if (deleteMode) {
					// delete stuff
					MapPath toRemove = null;
					for (MapPath path : mNewThisSessionPaths)
						if (path.getMapOverlay().getBounds().contains(mGoogleMap.getProjection().fromScreenLocation(new Point((int) ev.getX(), (int) ev.getY())))) {
							path.getMapOverlay().remove();
							path.deleteInBackground();
							toRemove = path;
						}
					if (toRemove != null)
						mNewThisSessionPaths.remove(toRemove);
				} else {
					if (!((Math.abs(currentDragPointIndices.x - mBoxStartGridIndices.x) + Math.abs(currentDragPointIndices.y - mBoxStartGridIndices.y)) >= MOVE_THRESHOLD))
						return;

					// create box
					try {

						PointF dims = getXYDist(
								MercatorProjection.fromPointToLatLng(
										mGrid.getNode(mBoxStartGridIndices).projCoords
								),
								MercatorProjection.fromPointToLatLng(
										mGrid.getNode(currentDragPointIndices).projCoords
								)
						);

						if (dims.x == 0f)
							dims.offset(8888, 0);
						if (dims.y == 0f)
							dims.offset(0, 8888);

						if (mTmpSelectedOverlay != null)
							mTmpSelectedOverlay.setDimensions(dims.x, dims.y);
					} catch (Exception e) {
						e.printStackTrace();
					}
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
				toggleDeleteMode();
				break;
			default:
				break;
		}

	}

	/* views toggle */
	private void toggleEditing(ImageButton editButton) {
		isEditingMap = !isEditingMap;


		if (!isEditingMap) {
			// save the edited map
			MapPath.saveAllInBackground(mNewThisSessionPaths);
			Log.i(TAG, "saving: " + mNewThisSessionPaths.size() + " paths");
		}

		mGoogleMap.getUiSettings().setScrollGesturesEnabled(!isEditingMap);

		// change the button icon / background
		editButton.setImageResource(isEditingMap ? android.R.drawable.ic_menu_close_clear_cancel : android.R.drawable.ic_menu_edit);
		editButton.setBackgroundResource(isEditingMap ? android.R.color.holo_green_light : android.R.color.holo_red_light);

		// hide edit controls
		mActivity.findViewById(R.id.delete_path_button).setVisibility(isEditingMap ? VISIBLE : INVISIBLE);
	}

	private void toggleDeleteMode() {
		deleteMode = !deleteMode;

		// change button background
		deleteButton.setBackgroundResource(deleteMode ? android.R.color.holo_green_light : android.R.color.holo_red_light);

	}

	/**
	 * Given the screen coordinate it computes the closes grid node to the screen point
	 *
	 * @param screenX - x value of the screen coordinate
	 * @param screenY - y value of the screen coordinate
	 * @return - The (x, y) indices of the grid array
	 */
	private Point getGridIndices(float screenX, float screenY) {

		PointF mapPoint = MercatorProjection.fromLatLngToPoint(
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
	 * @return - {@link PointF} object containing the horizontal and vertical distance in meters
	 */
	private PointF getXYDist(LatLng dragStartCoordinates, LatLng dragCurrentCoordinates) {

		// calculate the middle corner point
		PointF dragStart = MercatorProjection.fromLatLngToPoint(dragStartCoordinates);
		PointF dragCurrent = MercatorProjection.fromLatLngToPoint(dragCurrentCoordinates);

		// the middle corner point
		dragCurrent.set(dragCurrent.x, dragStart.y);

		LatLng middleCornerPoint = MercatorProjection.fromPointToLatLng(dragCurrent);

		// horizontal distance
		float hDist = MapTools.LatLngDistance(dragStartCoordinates.latitude, dragStartCoordinates.longitude, middleCornerPoint.latitude, middleCornerPoint.longitude);

		// vertical distance
		float vDist = MapTools.LatLngDistance(dragCurrentCoordinates.latitude, dragCurrentCoordinates.longitude, middleCornerPoint.latitude, middleCornerPoint.longitude);

		return new PointF(hDist, vDist);
	}


}