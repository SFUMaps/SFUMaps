package me.gurinderhans.sfumaps.ui.sliding_panel;

import android.content.Context;
import android.graphics.Point;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;

import me.gurinderhans.sfumaps.utils.MapTools;

import static me.gurinderhans.sfumaps.ui.sliding_panel.SlidingUpPanel.PanelState.COLLAPSED;
import static me.gurinderhans.sfumaps.utils.MapTools.LinearAnimTranslateViewToPos;
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
		MapTools.LinearAnimTranslateViewToPos(this, scrollToVal, 80l);
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


	@Override
	public boolean onTouchEvent(MotionEvent event) {

		// finger offset from top of the panel
		if (event.getAction() == MotionEvent.ACTION_DOWN)
			mFingerOffset = (int) event.getY();

		// offset
		float offsetVal = (getTranslationY() + event.getY()) - mFingerOffset;
		offsetVal = (float) MapTools.ValueLimiter(offsetVal, (screenSize.y - convertDpToPixel(DEFAULT_PANEL_HEIGHT, mContext)), (1 - DEFAULT_ANCHOR_POINT) * screenSize.y);

		float offsetPercent = 1 - (offsetVal / screenSize.y);

		// send % panel dragged up or down
		if (mPanelSlideListener != null)
			mPanelSlideListener.onPanelSlide(this, offsetPercent);


		setTranslationY(offsetVal);

		if (event.getAction() == MotionEvent.ACTION_UP) {
			if (mPanelState == COLLAPSED /* && goingUp ? */ && offsetPercent != DEFAULT_ANCHOR_POINT) {
				//
				float valTo = (1 - DEFAULT_ANCHOR_POINT) * screenSize.y;
				LinearAnimTranslateViewToPos(this, valTo, 80l);
//				setPanelState(ANCHORED);
			}
		}

		return true;
	}

}
