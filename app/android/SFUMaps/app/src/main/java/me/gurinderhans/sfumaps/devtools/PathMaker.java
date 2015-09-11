package me.gurinderhans.sfumaps.devtools;

import android.graphics.Point;
import android.support.v4.app.FragmentActivity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.GroundOverlayOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

import me.gurinderhans.sfumaps.R;
import me.gurinderhans.sfumaps.factory.classes.MapPath;
import me.gurinderhans.sfumaps.ui.views.CustomMapFragment;
import me.gurinderhans.sfumaps.ui.views.MapWrapperLayout.OnDragListener;
import me.gurinderhans.sfumaps.utils.MapTools;

import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;
import static me.gurinderhans.sfumaps.factory.classes.MapPath.mAllMapPaths;

/**
 * Created by ghans on 15-08-10.
 */
public class PathMaker implements OnDragListener, OnClickListener {

	public static final String TAG = PathMaker.class.getSimpleName();
	public static final int NODE_DIST = 70; // kms

	public static boolean isEditingMap = false;
	private static PathMaker mInstance = null;

	// UI
	private final GoogleMap mGoogleMap;
	private final FragmentActivity mActivity;

	// Logic
	boolean deleteMode = false;


	// @constructor
	PathMaker(FragmentActivity activity, GoogleMap map) {

		this.mGoogleMap = map;
//		this.mGrid = grid;
		this.mActivity = activity;

		// ------------
		mActivity.findViewById(R.id.dev_overlay).setVisibility(VISIBLE);
		mActivity.findViewById(R.id.edit_map_path).setOnClickListener(this);
		mActivity.findViewById(R.id.delete_path_button).setOnClickListener(this);

		// ------------
		((CustomMapFragment) mActivity.getSupportFragmentManager().findFragmentById(R.id.map)).setOnDragListener(this);
	}

	// initializer
	public static void createPathMaker(FragmentActivity activity, GoogleMap map) {
		if (mInstance == null)
			mInstance = new PathMaker(activity, map);
	}

	// onDrag variables
	LatLngBounds mTmpNodeBounds;
	MapPath mTmpMapPath;

	@Override
	public void onDrag(MotionEvent ev) {
		if (!isEditingMap)
			return;

		Point currentScreenDragPoint = new Point((int) ev.getX(), (int) ev.getY());

		switch (ev.getAction() & MotionEvent.ACTION_MASK) {

			case MotionEvent.ACTION_DOWN:
				if (!deleteMode)
					mTmpMapPath = new MapPath();

				break;
			case MotionEvent.ACTION_UP:
				if (!deleteMode) {
					mAllMapPaths.add(mTmpMapPath);

					// calculate neighbors for each node
//					for (int i = 0; i < mAllMapPaths.size(); i++) {
//						MapPath path = mAllMapPaths.get(i);
//						List<MapPath.MapPathNode> nodes = path.getNodes();
//
//					}



					mTmpMapPath.saveInBackground();
					mTmpMapPath = null;
				}

				/*// save new nodes
				for (int i = 0; i < mAllMapPaths.size(); i++) {

					MapPath compareTo = mAllMapPaths.get(i);

					LatLngBounds nodeBounds = new LatLngBounds(
							MapTools.LatLngFrom(compareTo.getPosition(), 225, (NODE_DIST + (NODE_DIST / 2f))),
							MapTools.LatLngFrom(compareTo.getPosition(), 45, (NODE_DIST + (NODE_DIST / 2f)))
					);

					for (int j = 0; j < mAllMapPaths.size(); j++) {
						MapPath compareFrom = mAllMapPaths.get(j);
						if (compareFrom.getPosition().equals(compareTo.getPosition()))
							continue;

						if (nodeBounds.contains(compareFrom.getPosition()))
							compareTo.addNeighbour(compareFrom);
					}
				}*/

				break;
			case MotionEvent.ACTION_MOVE:
				if (!deleteMode) {
					LatLng nodePos = mGoogleMap.getProjection().fromScreenLocation(currentScreenDragPoint);
					if (mTmpNodeBounds == null || !mTmpNodeBounds.contains(nodePos)) {
						// update bounds
						mTmpNodeBounds = new LatLngBounds(
								MapTools.LatLngFrom(nodePos, 225, NODE_DIST),
								MapTools.LatLngFrom(nodePos, 45, NODE_DIST)
						);

						// create map path node and add it to map path
						MapPath.MapPathNode node = new MapPath.MapPathNode();
						node.setPosition(nodePos);
						node.setMapEditOverlay(mGoogleMap.addGroundOverlay(new GroundOverlayOptions()
								.image(BitmapDescriptorFactory.fromResource(R.drawable.devtools_pathmaker_green_dot))
								.zIndex(100)
								.position(nodePos, 20000)));

						mTmpMapPath.addNode(node);
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
				deleteMode = !deleteMode;
				v.setBackgroundResource(deleteMode
						? android.R.color.holo_green_light
						: android.R.color.holo_red_light);
				break;
			default:
				break;
		}
	}

	/* edit map grid toggle */
	private void toggleEditing(ImageButton editButton) {

		isEditingMap = !isEditingMap;

		mGoogleMap.getUiSettings().setScrollGesturesEnabled(!isEditingMap);

		// change the button icon / background
		editButton.setImageResource(isEditingMap ? android.R.drawable.ic_menu_close_clear_cancel : android.R.drawable.ic_menu_edit);
		editButton.setBackgroundResource(isEditingMap ? android.R.color.holo_green_light : android.R.color.holo_red_light);

		// hide edit controls
		mActivity.findViewById(R.id.delete_path_button).setVisibility(isEditingMap ? VISIBLE : INVISIBLE);
	}

}