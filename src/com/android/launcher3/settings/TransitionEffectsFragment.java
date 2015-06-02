package com.android.launcher3.settings;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.android.launcher3.R;
import org.namelessrom.perception.LauncherConfiguration;
import org.namelessrom.perception.SlidingFragment;

public class TransitionEffectsFragment extends SlidingFragment
        implements MenuItem.OnMenuItemClickListener {
    public static final String PAGE_OR_DRAWER_SCROLL_SELECT = "pageOrDrawer";
    public static final String TRANSITION_EFFECTS_FRAGMENT = "transitionEffectsFragment";
    ImageView mTransitionIcon;
    ListView mListView;
    View mCurrentSelection;

    String[] mTransitionStates;
    TypedArray mTransitionDrawables;
    String mCurrentState;
    int mCurrentPosition;
    boolean mIsDrawer;
    String mSettingsProviderValue;
    int mPreferenceValue;

    OnClickListener mSettingsItemListener = new OnClickListener() {

        @Override
        public void onClick(View v) {
            ViewHolder viewHolder = (ViewHolder) v.getTag();
            if (viewHolder == null) {
                return;
            }
            if (mCurrentPosition == viewHolder.position) {
                return;
            }
            mCurrentPosition = viewHolder.position;
            mCurrentState = mTransitionStates[mCurrentPosition];

            setCleared(mCurrentSelection);
            setSelected(v);
            mCurrentSelection = v;

            new Thread(mImageEffectThreadRunnable).start();

            ((TransitionsArrayAdapter) mListView.getAdapter()).notifyDataSetChanged();
        }
    };

    private final Runnable mImageEffectThreadRunnable = new Runnable() {
        public void run() {
            mTransitionIcon.post(mImageEffectRunnable);
        }
    };

    private final Runnable mImageEffectRunnable = new Runnable() {
        public void run() {
            setImageViewToEffect();
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        setHasOptionsMenu(true);

        View v = inflater.inflate(R.layout.settings_transitions_screen, container, false);
        mListView = (ListView) v.findViewById(R.id.settings_transitions_list);

        mIsDrawer = getArguments().getBoolean(PAGE_OR_DRAWER_SCROLL_SELECT);

        mSettingsProviderValue = mIsDrawer ?
                SettingsProvider.SETTINGS_UI_DRAWER_SCROLLING_TRANSITION_EFFECT
                : SettingsProvider.SETTINGS_UI_HOMESCREEN_SCROLLING_TRANSITION_EFFECT;
        mPreferenceValue =
                mIsDrawer ? R.string.preferences_interface_drawer_scrolling_transition_effect
                        : R.string.preferences_interface_homescreen_scrolling_transition_effect;

        mTransitionIcon = (ImageView) v.findViewById(R.id.settings_transition_image);
        mTransitionIcon.post(mImageEffectRunnable);

        String[] titles = getResources().getStringArray(
                R.array.transition_effect_entries);
        mListView.setAdapter(new TransitionsArrayAdapter(getActivity(),
                R.layout.settings_pane_list_item, titles));

        mTransitionStates = getResources().getStringArray(
                R.array.transition_effect_values);
        mTransitionDrawables = getResources().obtainTypedArray(
                R.array.transition_effect_drawables);

        mCurrentState = SettingsProvider.getString(getActivity(),
                mSettingsProviderValue, mPreferenceValue);
        mCurrentPosition = mapEffectToPosition(mCurrentState);

        mListView.setSelection(mCurrentPosition);

        return v;
    }

    @Override public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        inflater.inflate(R.menu.scrolling_settings, menu);
        MenuItem pageOutlines = menu.findItem(R.id.scrolling_page_outlines);
        MenuItem fadeAdjacent = menu.findItem(R.id.scrolling_fade_adjacent);

        pageOutlines.setVisible(!mIsDrawer);
        pageOutlines.setChecked(SettingsProvider.getBoolean(getActivity(),
                SettingsProvider.SETTINGS_UI_HOMESCREEN_SCROLLING_PAGE_OUTLINES,
                R.bool.preferences_interface_homescreen_scrolling_page_outlines_default
        ));
        pageOutlines.setOnMenuItemClickListener(this);

        fadeAdjacent.setChecked(SettingsProvider.getBoolean(getActivity(),
                !mIsDrawer ?
                        SettingsProvider.SETTINGS_UI_HOMESCREEN_SCROLLING_FADE_ADJACENT :
                        SettingsProvider.SETTINGS_UI_DRAWER_SCROLLING_FADE_ADJACENT,
                !mIsDrawer ?
                        R.bool.preferences_interface_homescreen_scrolling_fade_adjacent_default :
                        R.bool.preferences_interface_drawer_scrolling_fade_adjacent_default
        ));
        fadeAdjacent.setOnMenuItemClickListener(this);
    }


    @Override public boolean onMenuItemClick(MenuItem item) {
        final int id = item.getItemId();
        switch (id) {
            case R.id.scrolling_page_outlines: {
                final boolean value = !item.isChecked();
                SettingsProvider.putBoolean(getActivity(),
                        SettingsProvider.SETTINGS_UI_HOMESCREEN_SCROLLING_PAGE_OUTLINES, value);
                item.setChecked(value);
                LauncherConfiguration.updateWorkspace = true;
                break;
            }
            case R.id.scrolling_fade_adjacent: {
                final String key = !mIsDrawer ?
                        SettingsProvider.SETTINGS_UI_HOMESCREEN_SCROLLING_FADE_ADJACENT :
                        SettingsProvider.SETTINGS_UI_DRAWER_SCROLLING_FADE_ADJACENT;
                final boolean value = !item.isChecked();
                SettingsProvider.putBoolean(getActivity(), key, value);
                item.setChecked(value);
                if (!mIsDrawer) {
                    LauncherConfiguration.updateWorkspace = true;
                } else {
                    LauncherConfiguration.updateAppsFadeInAdjacentScreens = true;
                }
                break;
            }
            default:
                return false;
        }

        return true;
    }

    public void setEffect() {
        String mSettingsProviderValue = mIsDrawer ?
                SettingsProvider.SETTINGS_UI_DRAWER_SCROLLING_TRANSITION_EFFECT :
                SettingsProvider.SETTINGS_UI_HOMESCREEN_SCROLLING_TRANSITION_EFFECT;
        SettingsProvider.putString(getActivity(), mSettingsProviderValue, mCurrentState);

        if (mIsDrawer) {
            LauncherConfiguration.updateAppsTransition = true;
        } else {
            LauncherConfiguration.updateWorkspace = true;
        }
    }

    private int mapEffectToPosition(String effect) {
        int length = mTransitionStates.length;
        for (int i = 0; i < length; i++) {
            if (effect.equals(mTransitionStates[i])) {
                return i;
            }
        }
        return -1;
    }

    private void setImageViewToEffect() {
        mTransitionIcon.setBackgroundResource(mTransitionDrawables
                .getResourceId(mCurrentPosition, R.drawable.transition_none));

        AnimationDrawable frameAnimation = (AnimationDrawable) mTransitionIcon.getBackground();
        frameAnimation.start();
    }

    @Override
    public void onStop() {
        super.onStop();

        // explicitly stop animation to ensure that we release references from the
        // view root's run queue
        AnimationDrawable frameAnimation = (AnimationDrawable) mTransitionIcon.getBackground();
        if (frameAnimation != null) {
            frameAnimation.stop();
        }
    }

    private void setSelected(View v) {
        v.setBackgroundColor(Color.WHITE);
        TextView t = (TextView) v.findViewById(R.id.item_name);
        t.setTextColor(getResources().getColor(R.color.settings_bg_color));
    }

    private void setCleared(View v) {
        v.setBackgroundColor(getResources().getColor(R.color.settings_bg_color));
        TextView t = (TextView) v.findViewById(R.id.item_name);
        t.setTextColor(Color.WHITE);
    }

    public static class ViewHolder {
        TextView textView;
        int position;

        public ViewHolder(View convertView) {
            textView = (TextView) convertView.findViewById(R.id.item_name);
            position = 0;
        }
    }

    private class TransitionsArrayAdapter extends ArrayAdapter<String> {
        Context mContext;
        String[] titles;

        public TransitionsArrayAdapter(Context context, int textViewResourceId,
                String[] objects) {
            super(context, textViewResourceId, objects);

            mContext = context;
            titles = objects;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final ViewHolder viewHolder;
            if (convertView == null) {
                convertView = LayoutInflater.from(mContext)
                        .inflate(R.layout.settings_pane_list_item, parent, false);
                viewHolder = new ViewHolder(convertView);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            // RTL
            Configuration config = getResources().getConfiguration();
            if (config.getLayoutDirection() == View.LAYOUT_DIRECTION_RTL) {
                viewHolder.textView.setGravity(Gravity.RIGHT);
            }

            viewHolder.textView.setText(titles[position]);
            // Set Selected State
            if (position == mCurrentPosition) {
                mCurrentSelection = convertView;
                setSelected(mCurrentSelection);
            }

            viewHolder.position = position;
            convertView.setOnClickListener(mSettingsItemListener);
            return convertView;
        }
    }
}
