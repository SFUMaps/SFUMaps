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

	public void hidePanel() {
		// hide the panel
		mPanel.togglePanelState(false);
		LinearViewAnimatorTranslateYToPos(mFab.getTranslationY(), 0, 80l, new ValueAnimator.AnimatorUpdateListener() {
			@Override
			public void onAnimationUpdate(ValueAnimator animation) {
				mFab.setTranslationY(Float.parseFloat(animation.getAnimatedValue().toString()));
			}
		});
	}

	public void setPlace(MapPlace mapPlace) {
		// show the panel along with the info
		mPanel.togglePanelState(true);
		LinearViewAnimatorTranslateYToPos(mFab.getTranslationY(), -50, 80l, new ValueAnimator.AnimatorUpdateListener() {
			@Override
			public void onAnimationUpdate(ValueAnimator animation) {
				mFab.setTranslationY(Float.parseFloat(animation.getAnimatedValue().toString()));
			}
		});

		((TextView) mPanel.findViewById(R.id.sliding_panel_collapsed_layout).findViewById(R.id.placeTitle)).setText(mapPlace.getTitle());

		MapPlace parentPlace = mapPlace.getParentPlace();
		if (parentPlace != null) {
			((TextView) mPanel.findViewById(R.id.sliding_panel_collapsed_layout).findViewById(R.id.placeParentTitle)).setText(parentPlace.getTitle());
		} else {
			((TextView) mPanel.findViewById(R.id.sliding_panel_collapsed_layout).findViewById(R.id.placeParentTitle)).setText(mapPlace.getType().getText());
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

	public void setSecondPlace(MapPlace mapPlace) {
		//
	}
}
