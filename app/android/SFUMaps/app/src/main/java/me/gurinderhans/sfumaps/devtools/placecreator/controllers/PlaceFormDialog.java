package me.gurinderhans.sfumaps.devtools.placecreator.controllers;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.parse.DeleteCallback;
import com.parse.ParseException;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.List;

import me.gurinderhans.sfumaps.R;
import me.gurinderhans.sfumaps.devtools.placecreator.views.MapPlaceDialogTitleCompletionView;
import me.gurinderhans.sfumaps.factory.classes.MapPlace;
import me.gurinderhans.sfumaps.utils.MarkerCreator;
import me.gurinderhans.sfumaps.utils.MarkerCreator.MapLabelIconAlign;
import me.gurinderhans.sfumaps.utils.MarkerCreator.MapPlaceType;

/**
 * Created by ghans on 15-09-03.
 */
public class PlaceFormDialog extends Dialog implements OnClickListener, OnSeekBarChangeListener {

	protected static final String TAG = PlaceFormDialog.class.getSimpleName();

	// activity of dialog origin
	private Activity mActivity;

	// place being created / edited in this dialog
	private MapPlace mTmpPlace;
	private int mEditingPlaceIndex;

	// global views
	private MapPlaceDialogTitleCompletionView mPlaceTitleEditText;
	private Spinner mPlaceTypeSelector, mIconAlignmentSelector;
	private TextView markerRotateValueView;
	private SeekBar mMarkerRotator;


	public PlaceFormDialog(Activity activity, int placeIndex) {
		super(activity);

		mActivity = activity;
		mEditingPlaceIndex = placeIndex;
		mTmpPlace = MapPlace.mAllMapPlaces.get(placeIndex);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
		setContentView(R.layout.devtools_placecreator_create_place_dialog_form);

		// views
		mPlaceTitleEditText = (MapPlaceDialogTitleCompletionView) findViewById(R.id.text_place_title);
		markerRotateValueView = (TextView) findViewById(R.id.marker_rotate_value);
		mPlaceTypeSelector = (Spinner) findViewById(R.id.select_place_type);
		mIconAlignmentSelector = (Spinner) findViewById(R.id.select_icon_alignment);
		mMarkerRotator = ((SeekBar) findViewById(R.id.marker_rotator));

		/* place type selector */
		ArrayAdapter<String> placeTypeAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item);
		placeTypeAdapter.addAll(MapPlaceType.allValues());
		placeTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		mPlaceTypeSelector.setAdapter(placeTypeAdapter);

		/* icon alignment selector */
		ArrayAdapter<String> iconAlignmentAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item);
		iconAlignmentAdapter.addAll(MapLabelIconAlign.allValues());
		iconAlignmentAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		mIconAlignmentSelector.setAdapter(iconAlignmentAdapter);


		// event listeners
		findViewById(R.id.btn_save_place).setOnClickListener(this);
		findViewById(R.id.btn_remove_place).setOnClickListener(this);
		mMarkerRotator.setOnSeekBarChangeListener(this);


		// place data will most likely be loaded before this dialog is instantiated
		// load adapter data
		List<MapPlace> places = new ArrayList<>();
		for (MapPlace place : MapPlace.mAllMapPlaces)
			if (place != null && place.getTitle() != null)
				places.add(place);

		ArrayAdapter<MapPlace> autoCompleteAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, places);
		mPlaceTitleEditText.setAdapter(autoCompleteAdapter);

		// load place into views
		loadPlaceViews();
	}

	void loadPlaceViews() {
		if (mTmpPlace.getParentPlace() != null)
			mPlaceTitleEditText.addObject(mTmpPlace.getParentPlace());
		if (mTmpPlace.getTitle() != null)
			mPlaceTitleEditText.addObject(mTmpPlace);

		// set place type
		int spinnerSelectIndex = MapPlaceType.allValues().indexOf(mTmpPlace.getType().getText());
		mPlaceTypeSelector.setSelection(spinnerSelectIndex);

		// set icon alignment
		int iconAlignmentIndex = MapLabelIconAlign.allValues().indexOf(mTmpPlace.getIconAlignment().getText());
		mIconAlignmentSelector.setSelection(iconAlignmentIndex);

		// load rotation
		mMarkerRotator.setProgress(mTmpPlace.getMarkerRotation());

		// load checkboxes
		List<Integer> zooms = mTmpPlace.getZooms();
		((CheckBox) findViewById(R.id.zoom_2)).setChecked(zooms.contains(2));
		((CheckBox) findViewById(R.id.zoom_3)).setChecked(zooms.contains(3));
		((CheckBox) findViewById(R.id.zoom_4)).setChecked(zooms.contains(4));
		((CheckBox) findViewById(R.id.zoom_5)).setChecked(zooms.contains(5));
		((CheckBox) findViewById(R.id.zoom_6)).setChecked(zooms.contains(6));
		((CheckBox) findViewById(R.id.zoom_7)).setChecked(zooms.contains(7));
		((CheckBox) findViewById(R.id.zoom_8)).setChecked(zooms.contains(8));
	}

	void savePlace() {

		List<MapPlace> objects = mPlaceTitleEditText.getObjects();
		if (objects.size() == 0) {
			Toast.makeText(getContext(), "Not saving place without title.", Toast.LENGTH_LONG).show();
			return;
		} else {
			if (objects.size() == 2) {
				// set title and parent
				mTmpPlace.setParentPlace(objects.get(0));
				mTmpPlace.setTitle(objects.get(1).getTitle());
			} else {
				mTmpPlace.setTitle(objects.get(0).getTitle());
				mTmpPlace.setParentPlace(null);
			}
		}


		// get place zooms
		List<Integer> zooms = new ArrayList<>();
		if (((CheckBox) findViewById(R.id.zoom_2)).isChecked())
			zooms.add(2);
		if (((CheckBox) findViewById(R.id.zoom_3)).isChecked())
			zooms.add(3);
		if (((CheckBox) findViewById(R.id.zoom_4)).isChecked())
			zooms.add(4);
		if (((CheckBox) findViewById(R.id.zoom_5)).isChecked())
			zooms.add(5);
		if (((CheckBox) findViewById(R.id.zoom_6)).isChecked())
			zooms.add(6);
		if (((CheckBox) findViewById(R.id.zoom_7)).isChecked())
			zooms.add(7);
		if (((CheckBox) findViewById(R.id.zoom_8)).isChecked())
			zooms.add(8);


		mTmpPlace.setZooms(zooms);
		mTmpPlace.setType(MapPlaceType.fromString(mPlaceTypeSelector.getSelectedItem().toString()));
		mTmpPlace.setIconAlignment(MapLabelIconAlign.fromString(mIconAlignmentSelector.getSelectedItem().toString()));

		// update list
		MapPlace.mAllMapPlaces.set(mEditingPlaceIndex, mTmpPlace);


		/* update the marker */
		// update marker text along with icon alignment
		mTmpPlace.getMapGizmo().setIcon(BitmapDescriptorFactory.fromBitmap(
				MarkerCreator.createPlaceIcon(mActivity.getApplicationContext(), mTmpPlace, mTmpPlace.getIconAlignment())
		));
		// update marker anchor point
		PointF anchorPoint = mTmpPlace.getIconAlignment().getAnchorPoint();
		mTmpPlace.getMapGizmo().setAnchor(anchorPoint.x, anchorPoint.y);
		// update marker flat
		mTmpPlace.getMapGizmo().setFlat(mTmpPlace.getType() == MapPlaceType.ROAD);


		// finally, save the place
		mTmpPlace.savePlaceWithCallback(new SaveCallback() {
			@Override
			public void done(ParseException e) {
				if (e == null) {
					Toast.makeText(getContext(), "MapPlace saved.", Toast.LENGTH_LONG).show();
				} else {
					Toast.makeText(getContext(), "Unable to save.", Toast.LENGTH_LONG).show();
				}
			}
		});
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.btn_save_place:
				savePlace();
				break;
			case R.id.btn_remove_place:
				// remove place
				mTmpPlace.deleteInBackground(new DeleteCallback() {
					@Override
					public void done(ParseException e) {
						Toast.makeText(getContext(), "MapPlace deleted.", Toast.LENGTH_LONG).show();
					}
				});
				mTmpPlace.getMapGizmo().remove();
				MapPlace.mAllMapPlaces.remove(mEditingPlaceIndex);
				break;
			default:
				break;
		}

		dismiss();
	}

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
		markerRotateValueView.setText(progress + "°");
		mTmpPlace.setMarkerRotation(progress);
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
		// hide stuff
		findViewById(R.id.add_place_image).setVisibility(View.INVISIBLE);
		findViewById(R.id.text_place_title).setVisibility(View.INVISIBLE);
		findViewById(R.id.select_inputs_wrapper).setVisibility(View.INVISIBLE);
		findViewById(R.id.zooms_selects).setVisibility(View.INVISIBLE);
		findViewById(R.id.form_actions).setVisibility(View.INVISIBLE);

		// remove white background
		findViewById(R.id.form_place_dialog_wrapper).setBackgroundResource(android.R.color.transparent);

		getWindow().setDimAmount(0f);
	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		// show stuff
		findViewById(R.id.add_place_image).setVisibility(View.VISIBLE);
		findViewById(R.id.text_place_title).setVisibility(View.VISIBLE);
		findViewById(R.id.select_inputs_wrapper).setVisibility(View.VISIBLE);
		findViewById(R.id.zooms_selects).setVisibility(View.VISIBLE);
		findViewById(R.id.form_actions).setVisibility(View.VISIBLE);

		// remove white background
		findViewById(R.id.form_place_dialog_wrapper).setBackgroundResource(R.drawable.devtools_placecreator_card_shape_round_corners);

		getWindow().setDimAmount(0.55f);
	}

}