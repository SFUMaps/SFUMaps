package me.gurinderhans.sfumaps.factory.libs.sliding_up_panel;

import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.Context;
import android.graphics.Point;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.widget.RelativeLayout;

import me.gurinderhans.sfumaps.utils.Tools;

import static me.gurinderhans.sfumaps.factory.libs.sliding_up_panel.SlidingUpPanel.PanelState.ANCHORED;
import static me.gurinderhans.sfumaps.factory.libs.sliding_up_panel.SlidingUpPanel.PanelState.COLLAPSED;
import static me.gurinderhans.sfumaps.factory.libs.sliding_up_panel.SlidingUpPanel.PanelState.EXPANDED;
import static me.gurinderhans.sfumaps.utils.Tools.ViewUtils.LinearViewAnimatorTranslateYToPos;
import static me.gurinderhans.sfumaps.utils.Tools.ViewUtils.convertDpToPixel;

/**
 * Created by ghans on 15-09-17.
 */
public class SlidingUpPanel extends RelativeLayout {

	// TODO: 15-09-17 Anchor panel to a middle anchor point && full expanded point && collapsed point
	// TODO: 15-09-21 Use CardView or something to add shadow, basic TODO is add shadow

	protected static final String TAG = SlidingUpPanel.class.getSimpleName();

	/**
	 * Default anchor point height
	 */
	public static final float DEFAULT_ANCHOR_POINT = 0.6f; // In relative %

	/**
	 * Default peeking out panel height
	 */
	private static final int DEFAULT_PANEL_HEIGHT = 80; // dp;
	private boolean mSlideEnabled = true;

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
	 * Panel Animation Listener to update panel slidable view
	 */
	private AnimatorUpdateListener mAnimationUpdateListener;

	/**
	 * Listener for monitoring events about sliding panes.
	 */
	public interface PanelSlideListener {
		/**
		 * Called when a sliding pane's position changes.
		 *
		 * @param slideOffsetPx The new offset of this sliding pane within its range, from 0-1
		 */
		void onPanelSlide(float slideOffsetPx);

		/**
		 * Called when a sliding panel state changes.
		 *
		 * @param panelState The state that the panel changed to.
		 */
		void onPanelStateChanged(PanelState panelState);
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

		mAnimationUpdateListener = new AnimatorUpdateListener() {
			@Override
			public void onAnimationUpdate(ValueAnimator animation) {

				float offsetPxVal = Float.parseFloat(animation.getAnimatedValue().toString());

				setTranslationY(offsetPxVal);

				// send % panel dragged up or down
				if (mPanelSlideListener != null)
					mPanelSlideListener.onPanelSlide(offsetPxVal);
			}
		};

		// calculate screen size
		DisplayMetrics metrics = mContext.getResources().getDisplayMetrics();
		screenSize = new Point(metrics.widthPixels, metrics.heightPixels);

		// hide panel
		showPanel(false);
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
	public void showPanel(boolean show) {
		float scrollToVal = screenSize.y - (show ? convertDpToPixel(DEFAULT_PANEL_HEIGHT, getContext()) : 0);

		LinearViewAnimatorTranslateYToPos(getTranslationY(), scrollToVal, 80l, new AnimatorUpdateListener() {
			@Override
			public void onAnimationUpdate(ValueAnimator animation) {
				setTranslationY(Float.parseFloat(animation.getAnimatedValue().toString()));
			}
		});

		setPanelState(show ? PanelState.COLLAPSED : PanelState.HIDDEN);
	}

	public void setPanelState(PanelState panelState) {
		this.mPanelState = panelState;

		if (mPanelSlideListener != null)
			mPanelSlideListener.onPanelStateChanged(panelState);
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
	public boolean onTouchEvent(@NonNull MotionEvent event) {

		/* offset values */

		float offsetMin = (mPanelState == ANCHORED || mPanelState == EXPANDED) ? 0 : (1 - DEFAULT_ANCHOR_POINT) * screenSize.y;
		float offsetMax = (screenSize.y - convertDpToPixel(DEFAULT_PANEL_HEIGHT, mContext));

		float offsetPxVal = (float) Tools.NumberUtils.ValueLimiter(((getTranslationY() + event.getY()) - mFingerOffset), offsetMax, offsetMin);

		switch (event.getAction() & MotionEvent.ACTION_MASK) {

			case MotionEvent.ACTION_DOWN:

				// record panel translateY for computing slide length
				slideStartValPx = getTranslationY();

				// record finger offset from top of the panel
				mFingerOffset = (int) event.getY();

				break;
			case MotionEvent.ACTION_UP:

				float dragLength = slideStartValPx - offsetPxVal;

				if (mSlideEnabled) {
					if (Math.abs(dragLength) > PANEL_CLIP_TO_THRESHOLD) {

					/* figure out which direction the panel is going and which state its currently in and animate accordingly */

						boolean dragUp = dragLength > 0;

						// TODO: 15-09-18 Track finger velocity to match it here so the animation follows the original speed
						switch (mPanelState) {
							case ANCHORED:

								if (dragUp) {
									LinearViewAnimatorTranslateYToPos(getTranslationY(), 0, 80l, mAnimationUpdateListener);
									setPanelState(EXPANDED);
								} else {
									LinearViewAnimatorTranslateYToPos(getTranslationY(), (screenSize.y - convertDpToPixel(DEFAULT_PANEL_HEIGHT, mContext)), 80l, mAnimationUpdateListener);
									setPanelState(COLLAPSED);
								}
								break;

							case EXPANDED:

								PanelState state = ANCHORED;

								float animOffsetVal = verticalPercentToScreenPixels(DEFAULT_ANCHOR_POINT);

								float anchorDiff = verticalScreenPixelsToPerent(getTranslationY()) - DEFAULT_ANCHOR_POINT;
								if (anchorDiff < 0 && anchorDiff < -0.2) {
									state = COLLAPSED;
									animOffsetVal = (screenSize.y - convertDpToPixel(DEFAULT_PANEL_HEIGHT, mContext));
								}

								LinearViewAnimatorTranslateYToPos(getTranslationY(), animOffsetVal, 80l, mAnimationUpdateListener);

								setPanelState(state);

								break;
							case COLLAPSED:
								LinearViewAnimatorTranslateYToPos(getTranslationY(), verticalPercentToScreenPixels(DEFAULT_ANCHOR_POINT), 80l, mAnimationUpdateListener);
								setPanelState(ANCHORED);
								break;
						}

					} else {
						// animate back to position it started
						LinearViewAnimatorTranslateYToPos(getTranslationY(), slideStartValPx, 80l, mAnimationUpdateListener);
					}
				}

				break;
			case MotionEvent.ACTION_MOVE:

				if (mSlideEnabled) {
					setTranslationY(offsetPxVal);

					// send % panel dragged up or down
					if (mPanelSlideListener != null) {
						mPanelSlideListener.onPanelSlide(offsetPxVal);
					}
				}


				break;
		}

		return true;
	}

	public int verticalPercentToScreenPixels(float percent) {
		return (int) ((1 - percent) * screenSize.y);
	}

	public float verticalScreenPixelsToPerent(float yPixel) {
		return 1 - (yPixel / screenSize.y);
	}

	public void disableSlide() {
		mSlideEnabled = false;
	}

}
