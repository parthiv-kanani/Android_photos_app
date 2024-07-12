package com.example.android22.Tag;

import android.view.GestureDetector;
import android.view.MotionEvent;

public class SwipeGestureListener extends GestureDetector.SimpleOnGestureListener {
    private static final int SWIPE_THRESHOLD = 100;
    private static final int SWIPE_VELOCITY_THRESHOLD = 100;

    public interface OnSwipeListener {
        void onSwipeLeft();

        void onSwipeRight();
    }

    private OnSwipeListener onSwipeListener;

    public SwipeGestureListener(OnSwipeListener onSwipeListener) {
        this.onSwipeListener = onSwipeListener;
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        float distanceX = e2.getX() - e1.getX();
        if (Math.abs(distanceX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
            if (distanceX > 0) {
                if (onSwipeListener != null) {
                    onSwipeListener.onSwipeRight();
                }
            } else {
                if (onSwipeListener != null) {
                    onSwipeListener.onSwipeLeft();
                }
            }
            return true;
        }
        return false;
    }
}
