package me.gurinderhans.sfumaps.devtools.placecreator;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.PointF;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.parse.DeleteCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.SaveCallback;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import me.gurinderhans.sfumaps.R;
import me.gurinderhans.sfumaps.devtools.wifirecorder.Keys;

/**
 * Created by ghans on 15-09-03.
 */
public class PlaceFormDialog extends Dialog implements OnClickListener, OnItemSelectedListener, OnSeekBarChangeListener {

	protected static final String TAG = PlaceFormDialog.class.getSimpleName();


	private Activity mActivity;
	ParseObject mMapPlace = new ParseObject("MapPlace");

	private EditText mPlaceTitleEditText;
	private Spinner mSpinner;
	private TextView markerRotateValueView;

	private final PointF mClickedPoint;
	private String mSelectedPlaceType = "";
	private final GoogleMap mMap;

	public PlaceFormDialog(Activity activity, GoogleMap map, PointF point, ParseObject oldPlace) {
		super(activity);

		mActivity = activity;
		mClickedPoint = point;
		mMap = map;

		if (oldPlace != null)
			mMapPlace = oldPlace;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setBackgroundDrawable(new ColorDrawable(0)); // set dialog background to transparent

		setContentView(R.layout.admin_create_place_form_dialog);

		// click listeners for form action buttons
		findViewById(R.id.btn_save_place).setOnClickListener(this);
		findViewById(R.id.btn_remove_place).setOnClickListener(this);

		mPlaceTitleEditText = (EditText) findViewById(R.id.text_place_title);
		markerRotateValueView = (TextView) findViewById(R.id.marker_rotate_value);

		// setup list selector
		mSpinner = (Spinner) findViewById(R.id.select_place_type);
		// Create an ArrayAdapter using the string array and a default spinner layout
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(),
				R.array.place_types, android.R.layout.simple_spinner_item);
		// Specify the layout to use when the list of choices appears
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		// Apply the adapter to the spinner
		mSpinner.setAdapter(adapter);
		mSpinner.setOnItemSelectedListener(this);

		((SeekBar) findViewById(R.id.marker_rotator)).setOnSeekBarChangeListener(this);


		// load place into views
		loadPlace();
	}

	void loadPlace() {

		mPlaceTitleEditText.setText(mMapPlace.getString(Keys.KEY_PLACE_TITLE));
		((TextView) findViewById(R.id.view_place_coords)).setText(mClickedPoint.x + ", " + mClickedPoint.y);

		// get position of place type
		int selectIndex = Arrays.asList(mActivity.getResources().getStringArray(R.array.place_types)).indexOf(mMapPlace.get(Keys.KEY_PLACE_TYPE));
		Log.i(TAG, "selectIndex: " + selectIndex);
		mSpinner.setSelection(selectIndex);

		// load checkboxes
		try {

			JSONArray zooms = mMapPlace.getJSONArray(Keys.KEY_PLACE_ZOOM);

			if (zooms == null) return;

			for (int i = 0; i < zooms.length(); i++) {
				switch (zooms.getInt(i)) {
					case 2:
						((CheckBox) findViewById(R.id.zoom_2)).setChecked(true);
						break;
					case 3:
						((CheckBox) findViewById(R.id.zoom_3)).setChecked(true);
						break;
					case 4:
						((CheckBox) findViewById(R.id.zoom_4)).setChecked(true);
						break;
					case 5:
						((CheckBox) findViewById(R.id.zoom_5)).setChecked(true);
						break;
					case 6:
						((CheckBox) findViewById(R.id.zoom_6)).setChecked(true);
						break;
					case 7:
						((CheckBox) findViewById(R.id.zoom_7)).setChecked(true);
						break;
					case 8:
						((CheckBox) findViewById(R.id.zoom_8)).setChecked(true);
						break;
				}
			}
		} catch (JSONException e) {
			// TODO: 15-09-04 handle exception here ?
			e.printStackTrace();
		}
	}

	void savePlace() {
		// place title
		mMapPlace.put(Keys.KEY_PLACE_TITLE, mPlaceTitleEditText.getText().toString());

		// place location
		try {
			JSONObject location = new JSONObject();
			location.put("x", mClickedPoint.x);
			location.put("y", mClickedPoint.y);

			mMapPlace.put(Keys.KEY_PLACE_POSITION, location);
		} catch (JSONException e) {
			// TODO: catch exception here!!
			e.printStackTrace();
		}

		// place type
		mMapPlace.put(Keys.KEY_PLACE_TYPE, mSelectedPlaceType);

		List<Integer> zooms = new ArrayList<>();

		// NOTE: yes I am doing this FO REAL!!
		// FIXME: 15-09-04 Get a better way to do this
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

		mMapPlace.put(Keys.KEY_PLACE_ZOOM, zooms);

		// add tmp marker

		// push place to parse servers
		mMapPlace.saveInBackground(new SaveCallback() {
			@Override
			public void done(ParseException e) {
				if (e != null)
					e.printStackTrace();
				Toast.makeText(getContext(), "Place saved.", Toast.LENGTH_LONG).show();

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
				mMapPlace.deleteInBackground(new DeleteCallback() {
					@Override
					public void done(ParseException e) {
						Toast.makeText(getContext(), "Place deleted.", Toast.LENGTH_LONG).show();
					}
				});
				break;
			default:
				break;
		}
		dismiss();
	}

	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
		// retrieve the selected item
		mSelectedPlaceType = parent.getItemAtPosition(position).toString();
	}

	@Override
	public void onNothingSelected(AdapterView<?> parent) {
	}

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
		markerRotateValueView.setText(progress + "°");
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