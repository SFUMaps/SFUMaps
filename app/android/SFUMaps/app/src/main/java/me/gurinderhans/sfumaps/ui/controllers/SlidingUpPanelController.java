package me.gurinderhans.sfumaps.ui.controllers;

import android.animation.ValueAnimator;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.widget.TextView;

import me.gurinderhans.sfumaps.R;
import me.gurinderhans.sfumaps.factory.classes.MapPlace;
import me.gurinderhans.sfumaps.factory.libs.sliding_up_panel.SlidingUpPanel;
import me.gurinderhans.sfumaps.factory.libs.sliding_up_panel.SlidingUpPanel.PanelSlideListener;
import me.gurinderhans.sfumaps.factory.libs.sliding_up_panel.SlidingUpPanel.PanelState;

import static me.gurinderhans.sfumaps.utils.MapTools.LinearViewAnimatorTranslateYToPos;

/**
 * Created by ghans on 15-09-18.
 */
public class SlidingUpPanelController implements PanelSlideListener {

	protected static final String TAG = SlidingUpPanel.class.getSimpleName();

	private final SlidingUpPanel mPanel;
	private final FloatingActionButton mFab;

	@NonNull
	private PanelState mCurrentPanelState;

	private float mPanelPrevSlideValue;

	public SlidingUpPanelController(SlidingUpPanel panel, FloatingActionButton fab) {
		mPanel = panel;
		mFab = fab;
		mPanel.setPanelSlideListener(this);
		panel.disableSlide();
	}


	//
	// MARK: Panel controller methods
	//

	public void showPanel() {
		// show the panel along with the info
		mPanel.showPanel(true);

	}

	public void hidePanel() {
		mPanel.showPanel(false);
	}

	// TODO: 15-09-20 add temp marker at this position
	public void setPanelData(MapPlace place) {
		((TextView) mPanel.findViewById(R.id.sliding_panel_collapsed_layout).findViewById(R.id.placeTitle)).setText(place.getTitle());

		MapPlace parentPlace = place.getParentPlace();
		if (parentPlace != null) {
			((TextView) mPanel.findViewById(R.id.sliding_panel_collapsed_layout).findViewById(R.id.placeParentTitle)).setText(parentPlace.getTitle());
		} else {
			((TextView) mPanel.findViewById(R.id.sliding_panel_collapsed_layout).findViewById(R.id.placeParentTitle)).setText(place.getType().getText());
		}
	}


	//
	// MARK: Panel Slide listener methods
	//


	@Override
	public void onPanelSlide(float slideOffsetPx) {
		// update FAB
		mFab.setTranslationY((slideOffsetPx - mPanel.screenSize.y) + 90); // TODO: 15-09-18 what is the magic number 90 ?
	}

	@Override
	public void onPanelStateChanged(PanelState panelState) {
		mCurrentPanelState = panelState;

		switch (mCurrentPanelState) {
			case COLLAPSED:
				break;
			case ANCHORED:
			case EXPANDED:
				break;
			default:
				break;
		}
	}

}
