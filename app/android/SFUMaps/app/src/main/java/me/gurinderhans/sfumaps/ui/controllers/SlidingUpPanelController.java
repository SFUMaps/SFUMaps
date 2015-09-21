package me.gurinderhans.sfumaps.ui.controllers;

import android.support.design.widget.FloatingActionButton;
import android.widget.TextView;

import me.gurinderhans.sfumaps.R;
import me.gurinderhans.sfumaps.factory.classes.MapPlace;
import me.gurinderhans.sfumaps.factory.libs.sliding_up_panel.SlidingUpPanel;
import me.gurinderhans.sfumaps.factory.libs.sliding_up_panel.SlidingUpPanel.PanelSlideListener;
import me.gurinderhans.sfumaps.factory.libs.sliding_up_panel.SlidingUpPanel.PanelState;

/**
 * Created by ghans on 15-09-18.
 */
public class SlidingUpPanelController implements PanelSlideListener {

	protected static final String TAG = SlidingUpPanel.class.getSimpleName();

	public final SlidingUpPanel panel;

	public SlidingUpPanelController(SlidingUpPanel panel, FloatingActionButton fab) {
		this.panel = panel;
		this.panel.setPanelSlideListener(this);
//		panel.disableSlide();
	}


	// TODO: 15-09-20 add temp marker at this position
	public void setPanelData(MapPlace place) {
		((TextView) panel.findViewById(R.id.sliding_panel_collapsed_layout).findViewById(R.id.placeTitle)).setText(place.getTitle());

		MapPlace parentPlace = place.getParentPlace();
		if (parentPlace != null) {
			((TextView) panel.findViewById(R.id.sliding_panel_collapsed_layout).findViewById(R.id.placeParentTitle)).setText(parentPlace.getTitle());
		} else {
			((TextView) panel.findViewById(R.id.sliding_panel_collapsed_layout).findViewById(R.id.placeParentTitle)).setText(place.getType().getText());
		}
	}


	@Override
	public void onPanelSlide(float slideOffsetPx) {
	}

	@Override
	public void onPanelStateChanged(PanelState panelState) {
	}

}
