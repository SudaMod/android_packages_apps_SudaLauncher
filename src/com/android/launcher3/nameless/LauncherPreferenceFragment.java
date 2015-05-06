/*
 * <!--
 *    Copyright (C) 2015 The NamelessRom Project
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 * -->
 */
package com.android.launcher3.nameless;

import android.animation.Animator;
import android.app.FragmentManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.preference.SwitchPreference;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;

import com.android.launcher3.AppsCustomizePagedView;
import com.android.launcher3.Launcher;
import com.android.launcher3.R;
import com.android.launcher3.settings.SettingsProvider;

public class LauncherPreferenceFragment extends PreferenceFragment implements Preference.OnPreferenceChangeListener {
    public static final String TAG = "LauncherPrefFragment";

    private static final String ANDROID_SETTINGS = "com.android.settings";
    private static final String ANDROID_PROTECTED_APPS =
            "com.android.settings.applications.ProtectedAppsActivity";

    private static final int DURATION_REVEAL_ANIMATION = 500;

    private static final String PREFIX_GENERAL = "general_";
    private static final String PREFIX_HOME_SCREEN = "homescreen_";

    private static final String KEY_GESTURES = "gestures";
    private static final String KEY_GRID_SIZE = "grid_size";
    private static final String KEY_PROTECTED_APPS = "protected_apps";
    private static final String KEY_SCROLL_EFFECT_DRAWER = "scroll_effect_drawer";
    private static final String KEY_SCROLL_EFFECT_HOME = "scroll_effect_home";

    private static Launcher mLauncher;

    private View mRootView;

    // Global
    private ListPreference mScreenOrientation;
    private SwitchPreference mStatusBarVisibility;

    // Drawer
    private ListPreference mSortMode;
    private SwitchPreference mHideTopBar;

    public LauncherPreferenceFragment() { }

    public static LauncherPreferenceFragment newInstance(final Launcher launcher) {
        mLauncher = launcher;
        return new LauncherPreferenceFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        // the theme of mLauncher differ from our wanted theme, so we need to inflate the layout
        // via a changed context.
        final int themeId = android.R.style.Theme_Material;
        mLauncher.setTheme(themeId);
        final Context contextThemeWrapper = new ContextThemeWrapper(mLauncher, themeId);
        final LayoutInflater localInflater = inflater.cloneInContext(contextThemeWrapper);


        mRootView = localInflater.inflate(R.layout.fragment_preferences, container, false);

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

        return mRootView;
    }

    private int[] getMetrics() {
        final WindowManager wm = (WindowManager) mLauncher.getSystemService(Context.WINDOW_SERVICE);
        final Display display = wm.getDefaultDisplay();
        final DisplayMetrics metrics = new DisplayMetrics();
        display.getRealMetrics(metrics);

        return new int[]{ metrics.widthPixels, metrics.heightPixels };
    }

    public void remove(final FragmentManager fragmentManager) {
        if (mRootView != null) {
            final int[] metrics = getMetrics();
            final int x = metrics[0];
            final int y = metrics[1];

            final int r = (int) Math.hypot(x, y);
            final Animator reveal = ViewAnimationUtils.createCircularReveal(mRootView, x, y, r, 0);
            reveal.setInterpolator(new AccelerateInterpolator(2f));
            reveal.setDuration(DURATION_REVEAL_ANIMATION);
            reveal.addListener(new Animator.AnimatorListener() {
                @Override public void onAnimationStart(Animator animation) { }

                @Override public void onAnimationEnd(Animator animation) {
                    // set visibility of the rootview to gone to prevent flickering as removing
                    // the fragment takes a little bit
                    mRootView.setVisibility(View.GONE);
                    fragmentManager.beginTransaction()
                            .remove(LauncherPreferenceFragment.this)
                            .commit();
                }

                @Override public void onAnimationCancel(Animator animation) { }

                @Override public void onAnimationRepeat(Animator animation) { }
            });
            reveal.start();
        } else {
            fragmentManager.beginTransaction().remove(this).commit();
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settings_main);

        // Global
        mScreenOrientation =
                (ListPreference) findPreference(SettingsProvider.SETTINGS_UI_GLOBAL_ORIENTATION);
        mScreenOrientation.setSummary(mScreenOrientation.getEntry());
        mScreenOrientation.setOnPreferenceChangeListener(this);

        mStatusBarVisibility = (SwitchPreference)
                findPreference(SettingsProvider.SETTINGS_UI_GLOBAL_HIDE_STATUS_BAR);
        mStatusBarVisibility.setOnPreferenceChangeListener(this);

        // Drawer
        mSortMode = (ListPreference) findPreference(SettingsProvider.SETTINGS_UI_DRAWER_SORT_MODE);
        mSortMode.setSummary(mSortMode.getEntry());
        mSortMode.setOnPreferenceChangeListener(this);

        mHideTopBar =
                (SwitchPreference) findPreference(SettingsProvider.SETTINGS_UI_DRAWER_HIDE_TOP_BAR);
        mHideTopBar.setOnPreferenceChangeListener(this);
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        final String key = preference.getKey();
        if (KEY_PROTECTED_APPS.equals(key)) {
            final Intent intent = new Intent();
            intent.setClassName(ANDROID_SETTINGS, ANDROID_PROTECTED_APPS);
            try {
                startActivity(intent);
            } catch (ActivityNotFoundException ane) {
                Log.e(TAG, "Could not start activity!", ane);
            }
            return true;
        } else if (KEY_GESTURES.equals(key)) {
            mLauncher.onClickGestureButton();
        } else if (KEY_GRID_SIZE.equals(key)) {
            mLauncher.onClickDynamicGridSizeButton();
        } else if (KEY_SCROLL_EFFECT_DRAWER.equals(key)) {
            mLauncher.onClickTransitionEffectButton(true);
        } else if (KEY_SCROLL_EFFECT_HOME.equals(key)) {
            mLauncher.onClickTransitionEffectButton(false);
        } else if (key.contains(PREFIX_HOME_SCREEN) || key.contains(PREFIX_GENERAL)) {
            mLauncher.updateDynamicGrid();
        }

        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference instanceof ListPreference) {
            final String value = String.valueOf(newValue);
            final int index = ((ListPreference) preference).findIndexOfValue(value);
            preference.setSummary(((ListPreference) preference).getEntries()[index]);
            if (mScreenOrientation == preference) {
                mLauncher.loadOrientation();
                mLauncher.unlockScreenOrientation(750);
            } else if (mSortMode == preference) {
                mLauncher.getAppsCustomizeContent()
                        .setSortMode(AppsCustomizePagedView.SortMode.getModeForValue(index));
            }
            return true;
        } else if (mStatusBarVisibility == preference) {
            final boolean value = (Boolean) newValue;
            SettingsProvider.putBoolean(getActivity(), mStatusBarVisibility.getKey(), value);
            mLauncher.updateStatusBarVisibility();
            return true;
        } else if (mHideTopBar == preference) {
            final boolean hide = (Boolean) newValue;
            SettingsProvider.putBoolean(getActivity(), mHideTopBar.getKey(), hide);
            mLauncher.setAppsCustomizeTopBarVisible(!hide);
            return true;
        }
        return false;
    }
}
