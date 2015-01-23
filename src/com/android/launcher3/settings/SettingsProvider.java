/*
 * Copyright (C) 2013 The CyanogenMod Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.launcher3.settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public final class SettingsProvider {
    // Global
    public static final String SETTINGS_UI_GLOBAL_ORIENTATION = "global_orientation";
    public static final String SETTINGS_UI_GLOBAL_HIDE_STATUS_BAR = "global_hide_status_bar";

    // Drawer
    public static final String SETTINGS_UI_DRAWER_SORT_MODE = "drawer_sort_mode";
    public static final String SETTINGS_UI_DRAWER_HIDE_ICON_LABELS = "drawer_hide_icon_labels";

    // Home screen
    public static final String SETTINGS_UI_HOMESCREEN_SEARCH = "homescreen_search";
    public static final String SETTINGS_UI_HOMESCREEN_HIDE_ICON_LABELS = "homescreen_hide_icon_labels";
    public static final String SETTINGS_UI_HOMESCREEN_SCROLLING_WALLPAPER_SCROLL = "homescreen_scrolling_wallpaper_scroll";

    // General
    public static final String SETTINGS_UI_GENERAL_ICONS_LARGE = "general_icons_large";

    public static final String SETTINGS_UI_HOMESCREEN_DEFAULT_SCREEN_ID = "ui_homescreen_default_screen_id";
    public static final String SETTINGS_UI_HOMESCREEN_SCROLLING_TRANSITION_EFFECT = "ui_homescreen_scrolling_transition_effect";
    public static final String SETTINGS_UI_HOMESCREEN_SCROLLING_PAGE_OUTLINES = "ui_homescreen_scrolling_page_outlines";
    public static final String SETTINGS_UI_HOMESCREEN_SCROLLING_FADE_ADJACENT = "ui_homescreen_scrolling_fade_adjacent";
    public static final String SETTINGS_UI_DYNAMIC_GRID_SIZE = "ui_dynamic_grid_size";
    public static final String SETTINGS_UI_HOMESCREEN_ROWS = "ui_homescreen_rows";
    public static final String SETTINGS_UI_HOMESCREEN_COLUMNS = "ui_homescreen_columns";
    public static final String SETTINGS_UI_DRAWER_SCROLLING_TRANSITION_EFFECT = "ui_drawer_scrolling_transition_effect";
    public static final String SETTINGS_UI_DRAWER_SCROLLING_FADE_ADJACENT = "ui_drawer_scrolling_fade_adjacent";
    public static final String SETTINGS_UI_DRAWER_REMOVE_HIDDEN_APPS_SHORTCUTS = "ui_drawer_remove_hidden_apps_shortcuts";
    public static final String SETTINGS_UI_DRAWER_REMOVE_HIDDEN_APPS_WIDGETS = "ui_drawer_remove_hidden_apps_widgets";
    public static final String SETTINGS_UI_GENERAL_ICONS_TEXT_FONT_FAMILY = "ui_general_icons_text_font";
    public static final String SETTINGS_UI_GENERAL_ICONS_TEXT_FONT_STYLE = "ui_general_icons_text_font_style";

    private static SharedPreferences get(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context);
    }

    public static int getIntCustomDefault(Context context, String key, int def) {
        try {
            return Integer.parseInt(get(context).getString(key, String.valueOf(def)));
        } catch (NumberFormatException nfe) {
            return def;
        }
    }

    public static int getInt(Context context, String key, int resource) {
        return getIntCustomDefault(context, key, context.getResources().getInteger(resource));
    }

    public static long getLongCustomDefault(Context context, String key, long def) {
        return get(context).getLong(key, def);
    }

    public static long getLong(Context context, String key, int resource) {
        return getLongCustomDefault(context, key, context.getResources().getInteger(resource));
    }

    public static boolean getBooleanCustomDefault(Context context, String key, boolean def) {
        return get(context).getBoolean(key, def);
    }

    public static boolean getBoolean(Context context, String key, int resource) {
        return getBooleanCustomDefault(context, key, context.getResources().getBoolean(resource));
    }

    public static String getStringCustomDefault(Context context, String key, String def) {
        return get(context).getString(key, def);
    }

    public static String getString(Context context, String key, int resource) {
        return getStringCustomDefault(context, key, context.getResources().getString(resource));
    }

    public static void putString(Context context, String key, String value) {
        get(context).edit().putString(key, value).commit();
    }

    public static void putInt(Context context, String key, int value) {
        get(context).edit().putInt(key, value).commit();
    }

    public static void putBoolean(Context context, String key, boolean value) {
        get(context).edit().putBoolean(key, value).commit();
    }

    public static void putLong(Context context, String key, long value) {
        get(context).edit().putLong(key, value).commit();
    }
}
