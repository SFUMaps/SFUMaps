package me.gurinderhans.sfumaps.devtools;

import android.graphics.Point;
import android.graphics.PointF;
import android.support.v4.app.FragmentActivity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.GroundOverlay;
import com.google.android.gms.maps.model.GroundOverlayOptions;
import com.google.android.gms.maps.model.LatLng;
import com.parse.ParseObject;

import me.gurinderhans.sfumaps.R;
import me.gurinderhans.sfumaps.app.Keys;
import me.gurinderhans.sfumaps.factory.classes.mapgraph.MapGraph;
import me.gurinderhans.sfumaps.factory.classes.mapgraph.MapGraphEdge;
import me.gurinderhans.sfumaps.factory.classes.mapgraph.MapGraphNode;
import me.gurinderhans.sfumaps.ui.views.CustomMapFragment;
import me.gurinderhans.sfumaps.ui.views.MapWrapperLayout.OnDragListener;
import me.gurinderhans.sfumaps.utils.Tools;

import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;

/**
 * Created by ghans on 15-08-10.
 */
public class PathMaker implements OnDragListener, OnClickListener {

	public static final String TAG = PathMaker.class.getSimpleName();
	public static final int SNAP_TO_NODE_SEARCH_RANGE = 25; // kms
	public static final int NODE_MAP_GIZMO_SIZE = 30000;
	public static final int EDGE_MAP_GIZMO_SIZE = 20000;

	public static boolean isEditingMap = false;
	private static PathMaker mInstance = null;

	// UI
	private final GoogleMap mGoogleMap;
	private final FragmentActivity mActivity;

	// Logic
	boolean deleteMode = false;
	public MapGraph mapGraph = MapGraph.getInstance();

	// @constructor
	PathMaker(FragmentActivity activity, GoogleMap map) {

		this.mGoogleMap = map;
		this.mActivity = activity;

		// ------------
//		mActivity.findViewById(R.id.dev_overlay).setVisibility(VISIBLE);
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
	LatLng tmpDragStartPos, tmpDragEndPos;
	GroundOverlay tmpEdgeOverlay;
	MapGraphEdge tmpGraphEdge;

	@Override
	public void onDrag(MotionEvent ev) {
		if (!isEditingMap)
			return;

		Point currentScreenDragPoint = new Point((int) ev.getX(), (int) ev.getY());

		switch (ev.getAction() & MotionEvent.ACTION_MASK) {

			case MotionEvent.ACTION_DOWN:
				if (!deleteMode) {
					tmpDragStartPos = mGoogleMap.getProjection().fromScreenLocation(currentScreenDragPoint);

					MapGraphNode nodeA = mapGraph.getNodeAt(tmpDragStartPos, SNAP_TO_NODE_SEARCH_RANGE);
					if (nodeA == null) {
						nodeA = new MapGraphNode(tmpDragStartPos);

						nodeA.setMapGizmo(
								mGoogleMap.addGroundOverlay(new GroundOverlayOptions()
										.position(tmpDragStartPos, NODE_MAP_GIZMO_SIZE)
										.zIndex(10001)
										.image(BitmapDescriptorFactory.fromResource(R.drawable.devtools_pathmaker_red_dot))
										.transparency(0.5f))
						);
						mapGraph.addNode(nodeA);
					}

					tmpGraphEdge = (MapGraphEdge) ParseObject.create(Keys.ParseMapGraphEdge.CLASS);
					tmpGraphEdge.setNodeA(nodeA);
					tmpEdgeOverlay = mGoogleMap.addGroundOverlay(new GroundOverlayOptions()
							.position(nodeA.getMapPosition(), EDGE_MAP_GIZMO_SIZE)
							.zIndex(10000)
							.image(BitmapDescriptorFactory.fromResource(R.drawable.devtools_pathmaker_green_dot))
							.transparency(0.2f)
							.anchor(0, 0.5f));
					tmpGraphEdge.setMapGizmo(tmpEdgeOverlay);
				}

				break;
			case MotionEvent.ACTION_UP:
				if (!deleteMode && tmpDragEndPos != null) {
					// see if there's a node where we are ending the drag, if yes link edge nodeB to this, else create new node here
					MapGraphNode nodeB = mapGraph.getNodeAt(tmpDragEndPos, SNAP_TO_NODE_SEARCH_RANGE);
					if (nodeB == null) {
						nodeB = new MapGraphNode(tmpDragEndPos);

						nodeB.setMapGizmo(
								mGoogleMap.addGroundOverlay(new GroundOverlayOptions()
										.position(tmpDragEndPos, NODE_MAP_GIZMO_SIZE)
										.zIndex(10001)
										.image(BitmapDescriptorFactory.fromResource(R.drawable.devtools_pathmaker_red_dot))
										.transparency(0.5f))
						);

						mapGraph.addNode(nodeB);
					}

					tmpGraphEdge.setNodeB(nodeB);
					tmpGraphEdge.setRotation(tmpEdgeOverlay.getBearing());

					mapGraph.addEdge(tmpGraphEdge);
					tmpGraphEdge.saveEventually();
				}
				break;
			case MotionEvent.ACTION_MOVE:
				if (!deleteMode) {
					tmpDragEndPos = mGoogleMap.getProjection().fromScreenLocation(currentScreenDragPoint);

					// compute edge rotation angle
					Point startPoint = mGoogleMap.getProjection().toScreenLocation(tmpDragStartPos);
					Point dist = new Point(currentScreenDragPoint.x - startPoint.x, currentScreenDragPoint.y - startPoint.y);
					double dragAngle = (Math.atan2(dist.y, dist.x)) * 180 / Math.PI; // convert to degrees
					tmpEdgeOverlay.setBearing((float) dragAngle);

					// compute edge size dimensions
					PointF dims = Tools.LocationUtils.getXYDist(tmpDragStartPos, tmpDragEndPos);
					float pathSize = (float) Math.sqrt(dims.x * dims.x + dims.y * dims.y);
					tmpEdgeOverlay.setDimensions(pathSize, 20000);
				} else {
					// delete nodes and edges
					mapGraph.removeEdgeAt(mGoogleMap.getProjection().fromScreenLocation(currentScreenDragPoint));
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