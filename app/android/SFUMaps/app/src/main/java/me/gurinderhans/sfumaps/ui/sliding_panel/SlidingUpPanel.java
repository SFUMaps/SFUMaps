package me.gurinderhans.sfumaps.ui.sliding_panel;

import android.content.Context;
import android.graphics.Point;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

/**
 * Created by ghans on 15-09-17.
 */
public class SlidingUpPanel extends FrameLayout {

	protected static final String TAG = SlidingUpPanel.class.getSimpleName();

	private LayoutInflater mInflater;
	private Point mScreenSize;


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
		// this allows dispatchTouchEvent() to handle drag
		setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
			}
		});

		mScreenSize = screenSize;

		setTranslationY(screenSize.y - 150);

	}

	@Override
	protected void onAttachedToWindow() {
		super.onAttachedToWindow();

		// params that shouldn't be set in the xml
		RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) getLayoutParams();
		params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
		setLayoutParams(params);
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {

		Log.i(TAG, "onTouch, y:" + ev.getY());
//		float translationVal = -((screenSize.y * slideValue) * 1.44f) + getResources().getDimensionPixelSize(R.dimen.plane_image_height);

		float yVal = (float) mScreenSize.y / ev.getY();
		Log.i(TAG, "y: " + yVal);

//		setTranslationY(ev.getY());


		return super.dispatchTouchEvent(ev);
	}

}
