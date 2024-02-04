package com.poupa.vinylmusicplayer.preferences;

import android.content.Context;
import android.util.AttributeSet;

import com.kabouzeid.appthemehelper.common.prefs.supportv7.ATEDialogPreference;

/**
 * @author Andreas Lechner
 */
public class ExportSettingsPreference extends ATEDialogPreference {
    public ExportSettingsPreference(Context context) {
        super(context);
    }

    public ExportSettingsPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ExportSettingsPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public ExportSettingsPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }
}