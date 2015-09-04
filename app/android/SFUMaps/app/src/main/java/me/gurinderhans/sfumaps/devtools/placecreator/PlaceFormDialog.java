package me.gurinderhans.sfumaps.devtools.placecreator;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import me.gurinderhans.sfumaps.R;

/**
 * Created by ghans on 15-09-03.
 */
public class PlaceFormDialog extends Dialog implements
		OnClickListener, AdapterView.OnItemSelectedListener {

	public Activity mActivity;
	public Dialog mFormDialog;
	public Button YES, NO;


	public PlaceFormDialog(Activity a) {
		super(a);
		this.mActivity = a;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.admin_create_place_form_dialog);

		setTitle("Customize Place");

		YES = (Button) findViewById(R.id.btn_save_place);
		NO = (Button) findViewById(R.id.btn_remove_place);
		Spinner spinner = (Spinner) findViewById(R.id.select_place_type);
		// Create an ArrayAdapter using the string array and a default spinner layout
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(),
				R.array.place_types, android.R.layout.simple_spinner_item);
		// Specify the layout to use when the list of choices appears
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		// Apply the adapter to the spinner
		spinner.setAdapter(adapter);

		YES.setOnClickListener(this);
		NO.setOnClickListener(this);

	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.btn_save_place:
				mActivity.finish();
				break;
			case R.id.btn_remove_place:
				dismiss();
				break;
			default:
				break;
		}
		dismiss();
	}

	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
		// retrieve the selected item
		// parent.getItemAtPosition(pos)
	}

	@Override
	public void onNothingSelected(AdapterView<?> parent) {

	}
}