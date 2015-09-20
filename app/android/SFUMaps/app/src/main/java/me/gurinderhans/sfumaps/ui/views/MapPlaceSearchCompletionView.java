package me.gurinderhans.sfumaps.ui.views;

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

	private int layoutId = R.layout.activity_main_place_directions_search_token_layout;

	public MapPlaceSearchCompletionView(Context context, AttributeSet attrs) {
		super(context, attrs);

		allowDuplicates(false);

		setTokenLimit(1); // allow only one address

	}

	@Override
	protected View getViewForObject(MapPlace completionPlace) {

		LayoutInflater l = (LayoutInflater) getContext().getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
		LinearLayout view = (LinearLayout) l.inflate(layoutId, (ViewGroup) getParent(), false);


		String placeTitle = "";
		if (completionPlace.getParentPlace() != null)
			placeTitle = completionPlace.getParentPlace().getTitle() + " ";
		placeTitle += completionPlace.getTitle();

		((TextView) view.findViewById(R.id.placeName)).setText(placeTitle);

		return view;
	}

	@Override
	protected MapPlace defaultObject(String completionText) {
		return null;
	}

	public void setLayoutId(int layoutId) {
		this.layoutId = layoutId;
	}
}
