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

/**
 * Created by ghans on 15-09-16.
 */
public class MapPlaceSearchBoxView extends TokenCompleteTextView<MapPlace> {

	public static final int TOKENIZER_MAX = 2;

	public MapPlaceSearchBoxView(Context context, AttributeSet attrs) {
		super(context, attrs);

		setSingleLine();

		setHint("Search SFU...");

		allowDuplicates(false); // trying to do dups don't make no sense boy!
	}

	@Override
	protected View getViewForObject(MapPlace completionText) {

		LayoutInflater l = (LayoutInflater) getContext().getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
		LinearLayout view = (LinearLayout) l.inflate(R.layout.devtools_placecreator_place_token_layout, (ViewGroup) getParent(), false);

		((TextView) view.findViewById(R.id.textData)).setText(completionText.getTitle());

		view.findViewById(R.id.textData).setBackgroundResource(android.R.color.darker_gray);

		return view;
	}

	@Override
	protected MapPlace defaultObject(String completionText) {
		return new MapPlace(completionText);
	}
}
