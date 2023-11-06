package com.poupa.vinylmusicplayer.preferences;

import android.content.Context;
import android.util.AttributeSet;

import com.kabouzeid.appthemehelper.common.prefs.supportv7.ATEDialogPreference;

/**
 * @author SC (soncaokim)
 */
public class MigratingPreference extends ATEDialogPreference {
    public MigratingPreference(Context context) {
        super(context);
    }

    public MigratingPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MigratingPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public MigratingPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }
}