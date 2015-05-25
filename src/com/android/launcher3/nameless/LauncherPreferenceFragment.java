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

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.preference.SwitchPreference;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.launcher3.R;
import com.android.launcher3.settings.SettingsProvider;

public class LauncherPreferenceFragment extends PreferenceFragment implements Preference.OnPreferenceChangeListener {
    public static final String TAG = "LauncherPrefFragment";

    private static final String ANDROID_SETTINGS = "com.android.settings";
    private static final String ANDROID_PROTECTED_APPS =
            "com.android.settings.applications.ProtectedAppsActivity";

    private static final String PREFIX_GENERAL = "general_";
    private static final String PREFIX_HOME_SCREEN = "homescreen_";

    private static final String KEY_GESTURES = "gestures";
    private static final String KEY_GRID_SIZE = "grid_size";
    private static final String KEY_PROTECTED_APPS = "protected_apps";
    private static final String KEY_SCROLL_EFFECT_DRAWER = "scroll_effect_drawer";
    private static final String KEY_SCROLL_EFFECT_HOME = "scroll_effect_home";

    private PrefNavListener mListener;

    // Global
    private ListPreference mScreenOrientation;
    private SwitchPreference mStatusBarVisibility;

    // Drawer
    private ListPreference mSortMode;
    private SwitchPreference mHideTopBar;

    public LauncherPreferenceFragment() { }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_preferences, container, false);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mListener = ((PrefNavListener) getActivity());

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
            mListener.onGestureFragment();
        } else if (KEY_GRID_SIZE.equals(key)) {
            mListener.onDynamicGridSizeFragment();
        } else if (KEY_SCROLL_EFFECT_DRAWER.equals(key)) {
            mListener.onTransitionEffectFragment(true);
        } else if (KEY_SCROLL_EFFECT_HOME.equals(key)) {
            mListener.onTransitionEffectFragment(false);
        } else if (key.contains(PREFIX_HOME_SCREEN) || key.contains(PREFIX_GENERAL)) {
            LauncherConfiguration.updateDynamicGrid = true;
        }

        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference instanceof ListPreference) {
            final String value = String.valueOf(newValue);
            final int index = ((ListPreference) preference).findIndexOfValue(value);
            preference.setSummary(((ListPreference) preference).getEntries()[index]);
            if (mSortMode == preference) {
                LauncherConfiguration.updateSortMode = true;
            }
            return true;
        } else if (mStatusBarVisibility == preference) {
            final boolean value = (Boolean) newValue;
            SettingsProvider.putBoolean(getActivity(), mStatusBarVisibility.getKey(), value);
            return true;
        } else if (mHideTopBar == preference) {
            final boolean hide = (Boolean) newValue;
            SettingsProvider.putBoolean(getActivity(), mHideTopBar.getKey(), hide);
            LauncherConfiguration.updateDrawerTopBar = true;
            return true;
        }
        return false;
    }
}
