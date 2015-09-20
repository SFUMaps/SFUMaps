package me.gurinderhans.sfumaps.ui.controllers;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.tokenautocomplete.TokenCompleteTextView;

import me.gurinderhans.sfumaps.R;
import me.gurinderhans.sfumaps.factory.classes.MapPlace;

public class MapPlaceSearchCompletionView extends TokenCompleteTextView<MapPlace> {

	public MapPlaceSearchCompletionView(Context context, AttributeSet attrs) {
		super(context, attrs);

		allowDuplicates(false);
	}

	@Override
	protected View getViewForObject(MapPlace completionText) {

		LayoutInflater l = (LayoutInflater) getContext().getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
		LinearLayout view = (LinearLayout) l.inflate(R.layout.activity_main_placesearch_token_layout, (ViewGroup) getParent(), false);

		((TextView) view.findViewById(R.id.placeName)).setText(completionText.getTitle());

		return view;
	}

	@Override
	protected MapPlace defaultObject(String completionText) {
		return MapPlace.createPlace(completionText);
	}
}
