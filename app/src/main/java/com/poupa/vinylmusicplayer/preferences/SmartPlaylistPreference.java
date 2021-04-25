package com.poupa.vinylmusicplayer.preferences;

import android.content.Context;
import android.util.AttributeSet;

import com.kabouzeid.appthemehelper.common.prefs.supportv7.ATEDialogPreference;

/**
 * @author SC (soncaokim)
 */
public class SmartPlaylistPreference extends ATEDialogPreference {
    public SmartPlaylistPreference(Context context) {
        super(context);
    }

    public SmartPlaylistPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SmartPlaylistPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public SmartPlaylistPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }
}