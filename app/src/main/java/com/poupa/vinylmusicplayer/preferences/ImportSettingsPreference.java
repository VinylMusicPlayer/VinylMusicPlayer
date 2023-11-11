package com.poupa.vinylmusicplayer.preferences;

import android.content.Context;
import android.util.AttributeSet;

import com.kabouzeid.appthemehelper.common.prefs.supportv7.ATEDialogPreference;

/**
 * @author SC (soncaokim)
 */
public class ImportSettingsPreference extends ATEDialogPreference {
    public ImportSettingsPreference(Context context) {
        super(context);
    }

    public ImportSettingsPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ImportSettingsPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public ImportSettingsPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }
}