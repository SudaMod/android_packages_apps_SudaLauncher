package org.namelessrom.perception;

import android.animation.Animator;
import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;

import com.android.launcher3.R;
import org.namelessrom.perception.gestures.GestureFragment;
import com.android.launcher3.settings.DynamicGridSizeFragment;
import com.android.launcher3.settings.TransitionEffectsFragment;

public class LauncherPreferenceActivity extends Activity implements PrefNavListener {
    private static final int DURATION_REVEAL_ANIMATION = 500;

    private View mRootView;

    private Animator mRevealAnimator;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(0, 0);

        setContentView(R.layout.activity_preferences);

        final ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        setTitle(R.string.preferences_text);

        final Window window = getWindow();
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        mRootView = window.getDecorView();

        // create a circular reveal animation
        mRootView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom,
                    int oldLeft, int oldTop, int oldRight, int oldBottom) {
                v.removeOnLayoutChangeListener(this);

                final int[] metrics = getMetrics();
                final int x = metrics[0];
                final int y = metrics[1];

                // get the hypothenuse so the radius is from one corner to the other
                final int radius = (int) Math.hypot(right, bottom);

                final Animator reveal = ViewAnimationUtils.createCircularReveal(v, x, y, 0, radius);
                reveal.setInterpolator(new AccelerateInterpolator(2f));
                reveal.setDuration(DURATION_REVEAL_ANIMATION);
                reveal.start();
            }
        });
    }

    @Override public boolean onOptionsItemSelected(MenuItem item) {
        final int id = item.getItemId();

        switch (id) {
            case android.R.id.home: {
                onBackPressed();
                return true;
            }
        }

        return super.onOptionsItemSelected(item);
    }

    @Override public void onBackPressed() {
        boolean shouldCallSuper = true;

        Fragment f1 = getFragmentManager().findFragmentByTag(
                TransitionEffectsFragment.TRANSITION_EFFECTS_FRAGMENT);
        Fragment f2 = getFragmentManager().findFragmentByTag(
                DynamicGridSizeFragment.DYNAMIC_GRID_SIZE_FRAGMENT);
        Fragment f3 = getFragmentManager().findFragmentByTag(GestureFragment.TAG);
        if (f1 instanceof TransitionEffectsFragment) {
            ((TransitionEffectsFragment) f1).setEffect();
        } else if (f2 instanceof DynamicGridSizeFragment) {
            // do not do anything here
        } else if (f3 instanceof GestureFragment) {
            ((GestureFragment) f3).setGestureDone();
        } else {
            shouldCallSuper = false;
        }

        if (shouldCallSuper) {
            setTitle(R.string.preferences_text);
            super.onBackPressed();
        } else {
            remove();
        }
    }

    private void remove() {
        if (mRootView == null || (mRevealAnimator != null && mRevealAnimator.isRunning())) {
            return;
        }

        final int[] metrics = getMetrics();
        final int x = metrics[0];
        final int y = metrics[1];

        final int r = (int) Math.hypot(x, y);
        mRevealAnimator = ViewAnimationUtils.createCircularReveal(mRootView, x, y, r, 0);
        mRevealAnimator.setInterpolator(new AccelerateInterpolator(2f));
        mRevealAnimator.setDuration(DURATION_REVEAL_ANIMATION);
        mRevealAnimator.addListener(new Animator.AnimatorListener() {
            @Override public void onAnimationStart(Animator animation) { }

            @Override public void onAnimationEnd(Animator animation) {
                // set visibility of the rootview to gone to prevent flickering as removing
                // the fragment takes a little bit
                mRootView.setVisibility(View.GONE);
                finish();
            }

            @Override public void onAnimationCancel(Animator animation) { }

            @Override public void onAnimationRepeat(Animator animation) { }
        });
        mRevealAnimator.start();
    }

    private int[] getMetrics() {
        final WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        final Display display = wm.getDefaultDisplay();
        final DisplayMetrics metrics = new DisplayMetrics();
        display.getRealMetrics(metrics);

        return new int[]{ metrics.widthPixels, metrics.heightPixels };
    }

    private void replaceFragment(Fragment fragment, String tag) {
        final FragmentManager fragmentManager = getFragmentManager();
        final FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.replace(R.id.fragment_container, fragment, tag);
        fragmentTransaction.commit();
    }

    @Override public void onGestureFragment() {
        setTitle(R.string.gesture_settings);
        replaceFragment(new GestureFragment(), GestureFragment.TAG);
    }

    @Override public void onDynamicGridSizeFragment() {
        setTitle(R.string.grid_size_text);
        replaceFragment(new DynamicGridSizeFragment(),
                DynamicGridSizeFragment.DYNAMIC_GRID_SIZE_FRAGMENT);
    }

    @Override public void onTransitionEffectFragment(final boolean isDrawer) {
        final Bundle bundle = new Bundle();
        bundle.putBoolean(TransitionEffectsFragment.PAGE_OR_DRAWER_SCROLL_SELECT, isDrawer);

        final TransitionEffectsFragment transitionEffectsFragment = new TransitionEffectsFragment();
        transitionEffectsFragment.setArguments(bundle);

        setTitle(R.string.scroll_effect_text);
        replaceFragment(transitionEffectsFragment,
                TransitionEffectsFragment.TRANSITION_EFFECTS_FRAGMENT);
    }

}
