package me.gurinderhans.sfumaps.devtools;

import android.graphics.Point;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;

import me.gurinderhans.sfumaps.R;
import me.gurinderhans.sfumaps.factory.classes.mapgraph.MapGraph;
import me.gurinderhans.sfumaps.ui.views.CustomMapFragment;
import me.gurinderhans.sfumaps.ui.views.MapWrapperLayout.OnDragListener;

import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;

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
	MapGraph mMapGraph = new MapGraph();


	// @constructor
	PathMaker(FragmentActivity activity, GoogleMap map) {

		this.mGoogleMap = map;
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
	LatLng dragStartPos;
	LatLng dragEndPos;

	@Override
	public void onDrag(MotionEvent ev) {
		if (!isEditingMap)
			return;

		Point currentScreenDragPoint = new Point((int) ev.getX(), (int) ev.getY());

		switch (ev.getAction() & MotionEvent.ACTION_MASK) {

			case MotionEvent.ACTION_DOWN:
				if (!deleteMode)
					dragStartPos = mGoogleMap.getProjection().fromScreenLocation(currentScreenDragPoint);

				break;
			case MotionEvent.ACTION_UP:
				if (!deleteMode) {
					Log.i(TAG, "added edge: " + mMapGraph.addEdge(dragStartPos, dragEndPos));
					Log.i(TAG, "added edge: " + mMapGraph.addEdge(dragStartPos, dragEndPos));
				}

				break;
			case MotionEvent.ACTION_MOVE:
				if (!deleteMode)
					dragEndPos = mGoogleMap.getProjection().fromScreenLocation(currentScreenDragPoint);

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