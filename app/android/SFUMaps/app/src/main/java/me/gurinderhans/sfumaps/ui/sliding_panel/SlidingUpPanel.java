package me.gurinderhans.sfumaps.ui.sliding_panel;

import android.content.Context;
import android.graphics.Point;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import me.gurinderhans.sfumaps.utils.MapTools;

/**
 * Created by ghans on 15-09-17.
 */
public class SlidingUpPanel extends FrameLayout {

	protected static final String TAG = SlidingUpPanel.class.getSimpleName();

	private LayoutInflater mInflater;
	private Point mScreenSize;

	private static float ANCHOR_POINT = 0.6f;// in percent


	public SlidingUpPanel(Context context) {
		super(context);
		mInflater = LayoutInflater.from(context);
	}

	public SlidingUpPanel(Context context, AttributeSet attrs) {
		super(context, attrs);
		mInflater = LayoutInflater.from(context);
	}

	public SlidingUpPanel(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		mInflater = LayoutInflater.from(context);
	}


	public void initWithScreenSize(Point screenSize) {
		// this allows onTouchEvent() to handle drag
		setOnClickListener(null);

		mScreenSize = screenSize;

		setTranslationY(mScreenSize.y - 300);

		Log.i(TAG, "screenSize: " + screenSize);

	}

	@Override
	protected void onAttachedToWindow() {
		super.onAttachedToWindow();

		// params that shouldn't be set in the xml
		RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) getLayoutParams();
		params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
		setLayoutParams(params);
	}


	int fingerOffset = 0;

	@Override
	public boolean onTouchEvent(MotionEvent event) {

		Log.i(TAG, "top: " + getTop());

		// finger offset from top of the panel
		if (event.getAction() == MotionEvent.ACTION_DOWN)
			fingerOffset = (int) event.getY();

		// offset
		float offsetVal = (getTranslationY() + event.getY()) - fingerOffset;
		setTranslationY((float) MapTools.ValueLimiter(offsetVal, mScreenSize.y - 150, 0));

		return super.onTouchEvent(event);
	}


}
