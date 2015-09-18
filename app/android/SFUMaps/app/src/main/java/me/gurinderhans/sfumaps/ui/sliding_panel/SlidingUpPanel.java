package me.gurinderhans.sfumaps.ui.sliding_panel;

import android.content.Context;
import android.graphics.Point;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;

import me.gurinderhans.sfumaps.utils.MapTools;

import static me.gurinderhans.sfumaps.ui.sliding_panel.SlidingUpPanel.PanelState.ANCHORED;
import static me.gurinderhans.sfumaps.ui.sliding_panel.SlidingUpPanel.PanelState.COLLAPSED;
import static me.gurinderhans.sfumaps.utils.MapTools.LinearViewAnimatorTranslateYToPos;
import static me.gurinderhans.sfumaps.utils.MapTools.convertDpToPixel;

/**
 * Created by ghans on 15-09-17.
 */
public class SlidingUpPanel extends RelativeLayout {

	// TODO: 15-09-17 Anchor panel to a middle anchor point && full expanded point && collapsed point

	protected static final String TAG = SlidingUpPanel.class.getSimpleName();

	/**
	 * Default anchor point height
	 */
	private static final float DEFAULT_ANCHOR_POINT = 0.6f; // In relative %

	/**
	 * Default peeking out panel height
	 */
	private static final int DEFAULT_PANEL_HEIGHT = 70; // dp;

	/**
	 * Current state of the slideable view.
	 */
	public enum PanelState {
		EXPANDED,
		COLLAPSED,
		ANCHORED,
		HIDDEN,
		DRAGGING;

	}

	private PanelState mPanelState = PanelState.HIDDEN;

	/**
	 * App context used for various tasks
	 */
	private Context mContext;

	/**
	 * Finger offset from top of panel
	 */
	private int mFingerOffset = 0;

	/**
	 * Screen size of the device
	 */
	public Point screenSize;

	private PanelSlideListener mPanelSlideListener;

	/**
	 * Listener for monitoring events about sliding panes.
	 */
	public interface PanelSlideListener {
		/**
		 * Called when a sliding pane's position changes.
		 *
		 * @param panel       The child view that was moved
		 * @param slideOffset The new offset of this sliding pane within its range, from 0-1
		 */
		public void onPanelSlide(View panel, float slideOffset);

		/**
		 * Called when a sliding panel becomes slid completely collapsed.
		 *
		 * @param panel The child view that was slid to an collapsed position
		 */
		public void onPanelCollapsed(View panel);

		/**
		 * Called when a sliding panel becomes slid completely expanded.
		 *
		 * @param panel The child view that was slid to a expanded position
		 */
		public void onPanelExpanded(View panel);

		/**
		 * Called when a sliding panel becomes anchored.
		 *
		 * @param panel The child view that was slid to a anchored position
		 */
		public void onPanelAnchored(View panel);

		/**
		 * Called when a sliding panel becomes completely hidden.
		 *
		 * @param panel The child view that was slid to a hidden position
		 */
		public void onPanelHidden(View panel);
	}


	// default constructors

	public SlidingUpPanel(Context context) {
		super(context);
		init();
	}

	public SlidingUpPanel(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public SlidingUpPanel(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init();
	}

	/**
	 * Handle private initialization here
	 */
	private void init() {
		// this allows onTouchEvent() to handle drag
		setOnClickListener(null);

		mContext = getContext();

		// calculate screen size
		DisplayMetrics metrics = mContext.getResources().getDisplayMetrics();
		screenSize = new Point(metrics.widthPixels, metrics.heightPixels);

		// hide panel
		togglePanelState(false);
	}

	/**
	 * Sets the panel slide listener
	 *
	 * @param listener
	 */
	public void setPanelSlideListener(PanelSlideListener listener) {
		mPanelSlideListener = listener;
	}

	/**
	 * Show / hide panel
	 *
	 * @param show - if true show panel, else hide
	 */
	public void togglePanelState(boolean show) {
		float scrollToVal = screenSize.y - (show ? convertDpToPixel(DEFAULT_PANEL_HEIGHT, getContext()) : 0);
		LinearViewAnimatorTranslateYToPos(this, scrollToVal, 80l);
		setPanelState(show ? PanelState.COLLAPSED : PanelState.HIDDEN);
	}

	public void setPanelState(PanelState panelState) {
		this.mPanelState = panelState;
	}

	@Override
	protected void onAttachedToWindow() {
		super.onAttachedToWindow();

		// default attrs
		RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) getLayoutParams();
		params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
		setLayoutParams(params);
	}

	// TODO: 15-09-18 convert to percent units
	private static final float PANEL_CLIP_TO_THRESHOLD = 100f; // px units

	private float slideStartValPx = 0f;

	@Override
	public boolean onTouchEvent(MotionEvent event) {

		// offset
		float offsetPxVal = (float) MapTools.ValueLimiter(((getTranslationY() + event.getY()) - mFingerOffset), (screenSize.y - convertDpToPixel(DEFAULT_PANEL_HEIGHT, mContext)), (1 - DEFAULT_ANCHOR_POINT) * screenSize.y);

		switch (event.getAction() & MotionEvent.ACTION_MASK) {

			case MotionEvent.ACTION_DOWN:

				// record finger offset from top of the panel
				mFingerOffset = (int) event.getY();

				slideStartValPx = getTranslationY();

				break;
			case MotionEvent.ACTION_UP:

				Log.i(TAG, "offset: " + offsetPxVal);

//				Log.i(TAG, "should anchor panel: " + inRange(offsetVal, 0.5, 0.7));

				if (Math.abs(slideStartValPx - offsetPxVal) > PANEL_CLIP_TO_THRESHOLD) {
					// figure out which direction the panel is going and which state its currently in and animate accordingly?

					boolean dragUp = slideStartValPx - offsetPxVal > 0;

					Log.i(TAG, "passed slide threshold");
					Log.i(TAG, "panelState: " + mPanelState);
					Log.i(TAG, "offsetDiff: " + (slideStartValPx - offsetPxVal));
					Log.i(TAG, "dragUp: " + dragUp);


					if (mPanelState == ANCHORED && !dragUp) {
						LinearViewAnimatorTranslateYToPos(this, (screenSize.y - convertDpToPixel(DEFAULT_PANEL_HEIGHT, mContext)), 80l);
						setPanelState(COLLAPSED);
					} else if (mPanelState == COLLAPSED && dragUp) {
						LinearViewAnimatorTranslateYToPos(this, verticalPercentToScreenPixels(DEFAULT_ANCHOR_POINT), 80l);
						setPanelState(ANCHORED);
					}
				} else {
					// animate back to position it started
					LinearViewAnimatorTranslateYToPos(this, slideStartValPx, 80l);
				}

				break;
			case MotionEvent.ACTION_MOVE:

				Log.i(TAG, "translateY %: " + verticalScreenPixelsToPerent(offsetPxVal));

				// send % panel dragged up or down
				if (mPanelSlideListener != null)
					mPanelSlideListener.onPanelSlide(this, offsetPxVal);

				setTranslationY(offsetPxVal);

				break;
		}

		return true;
	}

	private void computePanelTop(float topPercent) {
	}


	private int verticalPercentToScreenPixels(float percent) {
		return (int) ((1 - percent) * screenSize.y);
	}

	private float verticalScreenPixelsToPerent(float yPixel) {
		return 1 - (yPixel / screenSize.y);
	}

}
