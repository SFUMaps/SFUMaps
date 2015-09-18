package me.gurinderhans.sfumaps.ui.sliding_panel;

import android.content.Context;
import android.graphics.Point;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.RelativeLayout;

import me.gurinderhans.sfumaps.factory.classes.MapPlace;
import me.gurinderhans.sfumaps.utils.MapTools;

/**
 * Created by ghans on 15-09-17.
 */
public class SlidingUpPanel extends RelativeLayout {

	// TODO: 15-09-17 Anchor panel to a middle anchor point and full anchor point

	protected static final String TAG = SlidingUpPanel.class.getSimpleName();

	private Point mScreenSize;

	private static final float ANCHOR_POINT = 0.6f;// in percent

	// TODO: 15-09-17 convert to dp units
	private static final int PANEL_COLLAPSED_HEIGHT = 150; // px


	public SlidingUpPanel(Context context) {
		super(context);
	}

	public SlidingUpPanel(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public SlidingUpPanel(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}


	public void initWithScreenSize(Point screenSize) {
		// this allows onTouchEvent() to handle drag
		setOnClickListener(null);

		mScreenSize = screenSize;

		showPanel(false, null);

		Log.i(TAG, "screenSize: " + screenSize);
	}

	// NOTE: BAD confusing function.
	public void showPanel(boolean show, MapPlace showPlace) {
		// TODO: 15-09-17 Obvious animation require here
		setTranslationY(mScreenSize.y - (show ? PANEL_COLLAPSED_HEIGHT : 0));

		if (show && showPlace != null) {
			//
		}
	}

	@Override
	protected void onAttachedToWindow() {
		super.onAttachedToWindow();

		// params that shouldn't be set in the xml
		RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) getLayoutParams();
		params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
		setLayoutParams(params);
	}


	private int fingerOffset = 0;

	@Override
	public boolean onTouchEvent(MotionEvent event) {

		// finger offset from top of the panel
		if (event.getAction() == MotionEvent.ACTION_DOWN)
			fingerOffset = (int) event.getY();

		// offset
		float offsetVal = (getTranslationY() + event.getY()) - fingerOffset;
		setTranslationY((float) MapTools.ValueLimiter(offsetVal, (mScreenSize.y - PANEL_COLLAPSED_HEIGHT), 0));

		return true;
	}

}
