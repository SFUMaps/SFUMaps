package me.gurinderhans.sfumaps.devtools.placecreator;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.PointF;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.parse.DeleteCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.SaveCallback;

import org.json.JSONException;
import org.json.JSONObject;

import java.security.Key;
import java.util.ArrayList;
import java.util.List;

import me.gurinderhans.sfumaps.R;
import me.gurinderhans.sfumaps.devtools.wifirecorder.Keys;

/**
 * Created by ghans on 15-09-03.
 */
public class PlaceFormDialog extends Dialog implements OnClickListener, OnItemSelectedListener {

	protected static final String TAG = PlaceFormDialog.class.getSimpleName();

	ParseObject mMapPlace = new ParseObject("MapPlace");

	private EditText mPlaceTitleEditText;
	private final PointF mClickedPoint;
	private String mSelectedPlaceType = "";
	private final GoogleMap mMap;

	public PlaceFormDialog(Activity activity, GoogleMap map, PointF point, ParseObject oldPlace) {
		super(activity);

		mClickedPoint = point;
		mMap = map;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.admin_create_place_form_dialog);

		// click listeners for form action buttons
		findViewById(R.id.btn_save_place).setOnClickListener(this);
		findViewById(R.id.btn_remove_place).setOnClickListener(this);

		mPlaceTitleEditText = (EditText) findViewById(R.id.text_place_title);

		// set location
		((TextView) findViewById(R.id.view_place_coords)).setText(mClickedPoint.x + ", " + mClickedPoint.y);


		// setup list selector
		Spinner spinner = (Spinner) findViewById(R.id.select_place_type);
		// Create an ArrayAdapter using the string array and a default spinner layout
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(),
				R.array.place_types, android.R.layout.simple_spinner_item);
		// Specify the layout to use when the list of choices appears
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		// Apply the adapter to the spinner
		spinner.setAdapter(adapter);
		spinner.setOnItemSelectedListener(this);

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
		mMapPlace.put(Keys.KEY_PLACE_POSITION, mSelectedPlaceType);

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

		mMapPlace.addAll(Keys.KEY_PLACE_ZOOM, zooms);


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

}