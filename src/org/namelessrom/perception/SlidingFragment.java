package org.namelessrom.perception;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.app.Fragment;
import android.util.DisplayMetrics;
import android.view.View;

public class SlidingFragment extends Fragment {
    @Override
    public Animator onCreateAnimator(int transit, boolean enter, int nextAnim) {
        final DisplayMetrics displaymetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);

        int width = displaymetrics.widthPixels;
        if (getResources().getConfiguration().getLayoutDirection() == View.LAYOUT_DIRECTION_RTL) {
            width = -width;
        }

        if (enter) {
            return ObjectAnimator.ofFloat(this, "translationX", width, 0);
        }

        return ObjectAnimator.ofFloat(this, "translationX", 0, width);
    }
}
