/*
 * <!--
 *    Copyright (C) 2014 - 2015 The NamelessRom Project
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
package com.android.launcher3.nameless.gestures;

import android.content.res.Resources;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.android.launcher3.R;
import com.android.launcher3.nameless.SlidingFragment;

public class GestureFragment extends SlidingFragment {
    public static final String TAG = "GESTURE_FRAGMENT";

    private static final int POS_SWIPE_DOWN = 0;
    private static final int POS_SWIPE_UP = 1;
    private static final int POS_SPECIAL = 2;

    private GestureHeaderAdapter mGestureHeaderAdapter;

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
            final Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.settings_gestures_screen, container, false);

        final ListView listView = (ListView) v.findViewById(R.id.settings_gestures_list);
        initializeAdapter(listView);

        return v;
    }

    public void initializeAdapter(final ListView listView) {
        listView.setOverScrollMode(ListView.OVER_SCROLL_NEVER);
        final Resources res = getResources();

        final String[] headers = new String[]{
                res.getString(R.string.gesture_swipe_down),
                res.getString(R.string.gesture_swipe_up),
                res.getString(R.string.gesture_special) };

        final int[] swipeDown = new int[]{
                R.string.gesture_swipe_down_left,
                R.string.gesture_swipe_down_middle,
                R.string.gesture_swipe_down_right
        };

        final int[] swipeUp = new int[]{
                R.string.gesture_swipe_up_left,
                R.string.gesture_swipe_up_middle,
                R.string.gesture_swipe_up_right
        };

        final int[] special = new int[]{
                R.string.gesture_double_tap
        };

        mGestureHeaderAdapter = new GestureHeaderAdapter(getActivity());
        mGestureHeaderAdapter.setHeaders(headers);
        mGestureHeaderAdapter.addPartition(false, true);
        mGestureHeaderAdapter.addPartition(false, true);
        mGestureHeaderAdapter.addPartition(false, true);
        mGestureHeaderAdapter.mPinnedHeaderCount = headers.length;

        mGestureHeaderAdapter.changeCursor(POS_SWIPE_DOWN, createCursor(headers[0], swipeDown));
        mGestureHeaderAdapter.changeCursor(POS_SWIPE_UP, createCursor(headers[1], swipeUp));
        mGestureHeaderAdapter.changeCursor(POS_SPECIAL, createCursor(headers[2], special));
        listView.setAdapter(mGestureHeaderAdapter);
    }

    private Cursor createCursor(String header, int[] values) {
        MatrixCursor cursor = new MatrixCursor(new String[]{ "_id", header });
        int count = values.length;
        for (int i = 0; i < count; i++) {
            cursor.addRow(new Object[]{ i, values[i] });
        }
        return cursor;
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mGestureHeaderAdapter != null) {
            mGestureHeaderAdapter.onPause();
        }
    }

    public void setGestureDone() {
        // Nothing to do
    }

}
