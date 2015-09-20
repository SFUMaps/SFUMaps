package me.gurinderhans.sfumaps.devtools.placecreator.views;

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

public class MapPlaceDialogTitleCompletionView extends TokenCompleteTextView<MapPlace> {

	public static final int TOKENIZER_MAX = 2;

	public MapPlaceDialogTitleCompletionView(Context context, AttributeSet attrs) {
		super(context, attrs);

		allowDuplicates(false); // trying to do dups don't make no sense boy!

		performBestGuess(false); // allow freestyle writing

		// only allow for a parent to the current reference, NO MORE!!
		setTokenLimit(TOKENIZER_MAX);
	}

	@Override
	protected View getViewForObject(MapPlace completionText) {

		LayoutInflater l = (LayoutInflater) getContext().getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
		LinearLayout view = (LinearLayout) l.inflate(R.layout.devtools_placecreator_place_token_layout, (ViewGroup) getParent(), false);

		((TextView) view.findViewById(R.id.textData)).setText(completionText.getTitle());

		view.findViewById(R.id.textData).setBackgroundResource(
				(getObjects().size() == 1)
						? android.R.color.background_dark
						: R.color.app_color_primary
		);

		return view;
	}

	@Override
	protected MapPlace defaultObject(String completionText) {
		return MapPlace.createPlace(completionText);
	}
}
