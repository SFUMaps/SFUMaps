package me.gurinderhans.sfumaps.ui;

import android.content.Context;
import android.view.MotionEvent;
import android.widget.FrameLayout;

/**
 * Created by ghans on 15-08-10.
 */

public class MapWrapperLayout extends FrameLayout {

	private OnDragListener mOnDragListener;

	public MapWrapperLayout(Context context) {
		super(context);
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		if (mOnDragListener != null) {
			mOnDragListener.onDrag(ev);
		}
		return super.dispatchTouchEvent(ev);
	}

	public void setOnDragListener(OnDragListener mOnDragListener) {
		this.mOnDragListener = mOnDragListener;
	}

	public interface OnDragListener {
		void onDrag(MotionEvent motionEvent);
	}
}