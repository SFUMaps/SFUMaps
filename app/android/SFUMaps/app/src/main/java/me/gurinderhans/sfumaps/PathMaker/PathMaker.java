package me.gurinderhans.sfumaps.PathMaker;

import android.graphics.Point;
import android.graphics.PointF;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.GroundOverlay;
import com.google.android.gms.maps.model.GroundOverlayOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

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

	public final GoogleMap mGoogleMap;
	public MapGrid mGrid;


	// TODO: 15-08-16 improve application mode management
	boolean isEditingMap = false;
	boolean boxMode = false;
	boolean deleteMode = false;

	// this is only used for holding onto ground overlays until removed
	private List<GroundOverlay> boxRects = new ArrayList<>();

	JSONObject jsonGridRoot = new JSONObject();

	public PathMaker(CustomMapFragment mapFragment, GoogleMap map, View editButton,
	                 final View exportButton, final View boxButton, final View deleteButton, final MapGrid grid) {
		this.mGoogleMap = map;
		this.mGrid = grid;


		// read json file and plot grid
//		MapTools.loadFile()


		// create the json tree structure
		try {
			jsonGridRoot.put(WALKABLE_KEY, new JSONObject());
			jsonGridRoot.getJSONObject(WALKABLE_KEY).put(INDIVIDUAL_POINTS, new JSONArray());
			jsonGridRoot.getJSONObject(WALKABLE_KEY).put(POINT_RECTS, new JSONArray());
		} catch (JSONException e) {
			e.printStackTrace();
		}

		/* set input listeners on views */

		mapFragment.setOnDragListener(this);

		editButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {

				isEditingMap = !isEditingMap;

				//
				exportButton.setVisibility(isEditingMap ? View.VISIBLE : View.INVISIBLE);
				boxButton.setVisibility(isEditingMap ? View.VISIBLE : View.INVISIBLE);
				deleteButton.setVisibility(isEditingMap ? View.VISIBLE : View.INVISIBLE);

				((ImageButton) v).setImageResource(isEditingMap ? android.R.drawable.ic_menu_close_clear_cancel : android.R.drawable.ic_menu_edit);

				mGoogleMap.getUiSettings().setScrollGesturesEnabled(!isEditingMap);
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
								jsonGridRoot.getJSONObject(WALKABLE_KEY).getJSONArray(INDIVIDUAL_POINTS).put(x + "," + y);

					// create file
					MapTools.createFile("map_grid.json", jsonGridRoot.toString(4));
					Toast.makeText(v.getContext(), "Grid exported!", Toast.LENGTH_SHORT).show();

				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		});

		boxButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				boxMode = !boxMode;
				v.setBackgroundResource(boxMode ? R.drawable.box_rect_outline : R.drawable.sfunetsecuredot);

				if (boxMode) {
					deleteMode = false;
					deleteButton.setBackgroundResource(R.drawable.sfunetsecuredot);
				}
			}
		});

		deleteButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				deleteMode = !deleteMode;
				v.setBackgroundResource(deleteMode ? R.drawable.box_rect_outline : R.drawable.sfunetsecuredot);

				if (deleteMode) {
					boxMode = false;
					boxButton.setBackgroundResource(R.drawable.sfunetsecuredot);
				}
			}
		});
	}


	/**
	 * onDrag manage click vs drag to plot the corresponding marker on the grid
	 */

	Point mTmpDragStartGridIndices;
	GroundOverlay mTmpSelectedArea;
	boolean boxCreated;

	@Override
	public void onDrag(MotionEvent ev) {
		if (!isEditingMap)
			return;

		Point currentDragPointIndices = getGridIndices(ev.getX(), ev.getY());

		switch (ev.getAction() & MotionEvent.ACTION_MASK) {
			case MotionEvent.ACTION_DOWN:

				boxCreated = false;

				if (boxMode) {
					mTmpDragStartGridIndices = currentDragPointIndices;
					mTmpSelectedArea = mGoogleMap.addGroundOverlay(new GroundOverlayOptions()
									.position(MercatorProjection.fromPointToLatLng(mGrid.getNode(currentDragPointIndices).projCoords), 1000)
									.image(BitmapDescriptorFactory.fromResource(R.drawable.box_rect_outline))
									.transparency(0.2f)
									.zIndex(10000)
									.anchor(0, 0)
					);
					boxRects.add(mTmpSelectedArea);
				}
				break;
			case MotionEvent.ACTION_UP:
				if (boxMode && boxCreated) {
					// add box to json tree
					try {
						jsonGridRoot.getJSONObject(WALKABLE_KEY).getJSONArray(POINT_RECTS).put(
								mTmpDragStartGridIndices.x + "," + mTmpDragStartGridIndices.y + "|" + currentDragPointIndices.x + "," + currentDragPointIndices.y
						);
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}
				break;
			case MotionEvent.ACTION_MOVE:

				if (boxMode) {
					boxCreated = !currentDragPointIndices.equals(mTmpDragStartGridIndices);
					try {
						PointF dims = getXYDist(
								MercatorProjection.fromPointToLatLng(
										mGrid.getNode(mTmpDragStartGridIndices).projCoords
								),
								MercatorProjection.fromPointToLatLng(
										mGrid.getNode(currentDragPointIndices).projCoords
								)
						);

						if (dims != null)
							mTmpSelectedArea.setDimensions(dims.x, dims.y);
					} catch (Exception e) {
						e.printStackTrace();
					}
				} else if (deleteMode) {
					// get the clicked on rectangle
					// here we assume no two rectangles overlap each other
					for (GroundOverlay rectBox : boxRects) {
						if (rectBox.getBounds().contains(mGoogleMap.getProjection().fromScreenLocation(new Point((int) ev.getX(), (int) ev.getY()))))
							rectBox.remove();
					}
				} else {
					// add map path marker
					mGoogleMap.addMarker(new MarkerOptions()
							.position(MercatorProjection.fromPointToLatLng(mGrid.getNode(currentDragPointIndices).projCoords))
							.icon(BitmapDescriptorFactory.fromResource(R.drawable.map_path))
							.anchor(0.5f, 0.5f));

					// set to walkable point
					mGrid.getNode(currentDragPointIndices).setWalkable(true);
				}
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
	 * @return - The x,y index of the grid array
	 */
	public Point getGridIndices(float x, float y) {

		PointF mapPoint = MercatorProjection.fromLatLngToPoint(
				mGoogleMap.getProjection().fromScreenLocation(new Point((int) x, (int) y)));
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