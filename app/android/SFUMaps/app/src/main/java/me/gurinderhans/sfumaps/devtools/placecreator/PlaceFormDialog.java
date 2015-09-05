package me.gurinderhans.sfumaps.devtools.placecreator;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
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
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
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
import me.gurinderhans.sfumaps.app.Keys;
import me.gurinderhans.sfumaps.utils.MercatorProjection;

/**
 * Created by ghans on 15-09-03.
 */
public class PlaceFormDialog extends Dialog implements OnClickListener, OnSeekBarChangeListener {

	protected static final String TAG = PlaceFormDialog.class.getSimpleName();

	// activity of dialog origin
	private Activity mActivity;

	// place being created / edited in this dialog
	private Pair<ParseObject, Marker> mTmpPlace;
	private final PointF mClickedPoint;

	// global views
	private EditText mPlaceTitleEditText;
	private Spinner mSpinner;
	private TextView markerRotateValueView;
	private SeekBar mMarkerRotator;


	public PlaceFormDialog(Activity activity, GoogleMap map, PointF point, Pair<ParseObject, Marker> oldPlaceOpt) {
		super(activity);

		mActivity = activity;
		mClickedPoint = point;


		if (oldPlaceOpt == null) {
			mTmpPlace = Pair.create(
					new ParseObject(Keys.KEY_PLACE),
					map.addMarker(new MarkerOptions()
							.position(MercatorProjection.fromPointToLatLng(point)))
			);
		} else {
			mTmpPlace = oldPlaceOpt;
		}

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

		mPlaceTitleEditText.setText(mTmpPlace.first.getString(Keys.KEY_PLACE_TITLE));
		((TextView) findViewById(R.id.view_place_coords)).setText(mClickedPoint.x + ", " + mClickedPoint.y);

		// get position of place type
		int selectIndex = Arrays.asList(mActivity.getResources().getStringArray(R.array.place_types)).indexOf(mTmpPlace.first.get(Keys.KEY_PLACE_TYPE));
		Log.i(TAG, "selectIndex: " + selectIndex);
		mSpinner.setSelection(selectIndex);

		// load rotation
		mMarkerRotator.setProgress(mTmpPlace.first.getInt(Keys.KEY_PLACE_MARKER_ROTATION));

		// load checkboxes
		try {

			JSONArray zooms = mTmpPlace.first.getJSONArray(Keys.KEY_PLACE_ZOOM);

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
		mTmpPlace.first.put(Keys.KEY_PLACE_TITLE, mPlaceTitleEditText.getText().toString());

		// place location
		try {
			JSONObject location = new JSONObject();
			location.put("x", mClickedPoint.x);
			location.put("y", mClickedPoint.y);

			mTmpPlace.first.put(Keys.KEY_PLACE_POSITION, location);
		} catch (JSONException e) {
			// TODO: catch exception here!!
			e.printStackTrace();
		}

		// place type
		mTmpPlace.first.put(Keys.KEY_PLACE_TYPE, mSpinner.getSelectedItem().toString());

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

		mTmpPlace.first.put(Keys.KEY_PLACE_ZOOM, zooms);

		// place marker rotation
		mTmpPlace.first.put(Keys.KEY_PLACE_MARKER_ROTATION, mMarkerRotator.getProgress());


		// push place to parse servers
		mTmpPlace.first.saveInBackground(new SaveCallback() {
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
				mTmpPlace.first.deleteInBackground(new DeleteCallback() {
					@Override
					public void done(ParseException e) {
						Toast.makeText(getContext(), "Place deleted.", Toast.LENGTH_LONG).show();
					}
				});

				break;
			default:
				break;
		}

		mTmpPlace.second.remove();

		dismiss();
	}

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
		markerRotateValueView.setText(progress + "°");
		mTmpPlace.second.setRotation(progress);
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