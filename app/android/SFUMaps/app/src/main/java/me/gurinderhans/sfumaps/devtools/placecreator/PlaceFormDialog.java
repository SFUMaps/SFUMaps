package me.gurinderhans.sfumaps.devtools.placecreator;

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
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.parse.DeleteCallback;
import com.parse.ParseException;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import me.gurinderhans.sfumaps.R;
import me.gurinderhans.sfumaps.factory.classes.MapPlace;
import me.gurinderhans.sfumaps.ui.MainActivity;
import me.gurinderhans.sfumaps.utils.MarkerCreator;

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
	private EditText mPlaceTitleEditText;
	private Spinner mSpinner;
	private TextView markerRotateValueView;
	private SeekBar mMarkerRotator;


	public PlaceFormDialog(Activity activity, GoogleMap map, int placeIndex) {
		super(activity);

		mActivity = activity;
		mEditingPlaceIndex = placeIndex;
		mTmpPlace = MainActivity.mAllMapPlaces.get(placeIndex);

		setCancelable(false);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

		setContentView(R.layout.admin_create_place_form_dialog);

		// click listeners for form action buttons
		findViewById(R.id.btn_save_place).setOnClickListener(this);
		findViewById(R.id.btn_remove_place).setOnClickListener(this);

		mPlaceTitleEditText = (EditText) findViewById(R.id.text_place_title);
		markerRotateValueView = (TextView) findViewById(R.id.marker_rotate_value);

		// setup list selector
		mSpinner = (Spinner) findViewById(R.id.select_place_type);
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(),
				R.array.place_types, android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		mSpinner.setAdapter(adapter);

		mMarkerRotator = ((SeekBar) findViewById(R.id.marker_rotator));
		mMarkerRotator.setOnSeekBarChangeListener(this);

		// load place into views
		loadPlace();
	}

	void loadPlace() {

		mPlaceTitleEditText.setText(mTmpPlace.getTitle());

		PointF position = mTmpPlace.getPosition();
		((TextView) findViewById(R.id.view_place_coords)).setText(position.x + ", " + position.y);


		// get position of place type
		int spinnerSelectIndex = Arrays.asList(mActivity
				.getResources()
				.getStringArray(R.array.place_types)).indexOf(mTmpPlace.getType());
		mSpinner.setSelection(spinnerSelectIndex);

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
		if (mPlaceTitleEditText.getText().toString().isEmpty()) {
			Toast.makeText(getContext(), "Not saving place without title.", Toast.LENGTH_LONG).show();
			return;
		}

		List<Integer> zooms = new ArrayList<>();

		// get place zooms
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


		mTmpPlace.setTitle(mPlaceTitleEditText.getText().toString());
		mTmpPlace.setZooms(zooms);
		mTmpPlace.setType(mSpinner.getSelectedItem().toString());

		// update list
		MainActivity.mAllMapPlaces.set(mEditingPlaceIndex, mTmpPlace);

		// update marker text
		mTmpPlace.getPlaceMarker().setIcon(BitmapDescriptorFactory.fromBitmap(
				MarkerCreator.createPlaceIcon(mActivity.getApplicationContext(), mTmpPlace, MarkerCreator.MapLabelIconAlign.TOP)
		));


		mTmpPlace.savePlaceWithCallback(new SaveCallback() {
			@Override
			public void done(ParseException e) {
				if (e != null)
					Toast.makeText(getContext(), "Unable to save.", Toast.LENGTH_LONG).show();
				else
					Toast.makeText(getContext(), "MapPlace saved.", Toast.LENGTH_LONG).show();
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
				mTmpPlace.getPlaceMarker().remove();
				MainActivity.mAllMapPlaces.remove(mEditingPlaceIndex);
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
		findViewById(R.id.text_place_title).setVisibility(View.INVISIBLE);
		findViewById(R.id.view_place_coords).setVisibility(View.INVISIBLE);
		findViewById(R.id.select_place_type).setVisibility(View.INVISIBLE);
		findViewById(R.id.zooms_selects).setVisibility(View.INVISIBLE);
		findViewById(R.id.form_actions).setVisibility(View.INVISIBLE);
		findViewById(R.id.add_image).setVisibility(View.INVISIBLE);

		// remove white background
		findViewById(R.id.form_place_dialog_wrapper).setBackgroundColor(mActivity.getResources().getColor(android.R.color.transparent));

		getWindow().setDimAmount(0f);
	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		// show stuff
		findViewById(R.id.text_place_title).setVisibility(View.VISIBLE);
		findViewById(R.id.view_place_coords).setVisibility(View.VISIBLE);
		findViewById(R.id.select_place_type).setVisibility(View.VISIBLE);
		findViewById(R.id.zooms_selects).setVisibility(View.VISIBLE);
		findViewById(R.id.form_actions).setVisibility(View.VISIBLE);
		findViewById(R.id.add_image).setVisibility(View.VISIBLE);

		// remove white background
		findViewById(R.id.form_place_dialog_wrapper).setBackgroundColor(mActivity.getResources().getColor(R.color.dialog_background));

		getWindow().setDimAmount(0.55f);
	}

}