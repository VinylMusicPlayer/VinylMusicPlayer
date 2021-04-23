package com.poupa.vinylmusicplayer.preferences;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.NumberPicker;

import androidx.annotation.NonNull;
import androidx.core.util.Pair;
import androidx.fragment.app.DialogFragment;

import com.afollestad.materialdialogs.MaterialDialog;
import com.poupa.vinylmusicplayer.R;
import com.poupa.vinylmusicplayer.util.PreferenceUtil;

import java.time.temporal.ChronoUnit;

/**
 * @author SC (soncaokim)
 */
public class SmartPlaylistPreferenceDialog extends DialogFragment {
    @NonNull
    public static SmartPlaylistPreferenceDialog newInstance(@NonNull String preference) {
        return new SmartPlaylistPreferenceDialog(preference);
    }

    @NonNull private final String preferenceKey;

    public SmartPlaylistPreferenceDialog(@NonNull String preference) {
        preferenceKey = preference;
    }

    final static ChronoUnit[] POSSIBLE_TIME_UNITS = {
            ChronoUnit.DAYS,
            ChronoUnit.WEEKS,
            ChronoUnit.MONTHS,
            ChronoUnit.YEARS
    };
    final static String[] POSSIBLE_TIME_UNIT_SHORT_NAMES = {
            "d",
            "w",
            "m",
            "y"
    };
    final static String[] POSSIBLE_TIME_UNIT_LONG_NAMES = {
            // TODO Localizable?
            ChronoUnit.DAYS.name(),
            ChronoUnit.WEEKS.name(),
            ChronoUnit.MONTHS.name(),
            ChronoUnit.YEARS.name()
    };

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final Context context = getContext();
        final Resources resources = context.getResources();

        // ---- Retrieve the stored value
        String prefName = "";
        Pair<Integer, ChronoUnit> prefValue = PreferenceUtil.getInstance().getCutoffTimeV2(preferenceKey);
        switch (preferenceKey) {
            case PreferenceUtil.LAST_ADDED_CUTOFF_V2:
                prefName = resources.getString(R.string.pref_title_last_added_interval);
                break;
            case PreferenceUtil.RECENTLY_PLAYED_CUTOFF_V2:
                prefName = resources.getString(R.string.pref_title_recently_played_interval);
                break;
            case PreferenceUtil.NOT_RECENTLY_PLAYED_CUTOFF_V2:
                prefName = resources.getString(R.string.pref_title_not_recently_played_interval);
                break;
        }

        // ---- Build the dialog
        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.HORIZONTAL);
        layout.setGravity(Gravity.CENTER);

        final NumberPicker valueInput = new NumberPicker(context);
        valueInput.setMinValue(0);
        valueInput.setMaxValue(100);
        valueInput.setValue(prefValue.first);
        layout.addView(valueInput);

        int unitPosition = -1;
        for (int i=0; i<POSSIBLE_TIME_UNITS.length; ++i) {
            if (prefValue.second == POSSIBLE_TIME_UNITS[i]) {
                unitPosition = i;
                break;
            }
        }
        final NumberPicker unitInput = new NumberPicker(context);
        unitInput.setMinValue(0);
        unitInput.setMaxValue(POSSIBLE_TIME_UNITS.length - 1);
        unitInput.setDisplayedValues(POSSIBLE_TIME_UNIT_LONG_NAMES);
        unitInput.setValue(unitPosition);
        layout.addView(unitInput);

        return new MaterialDialog.Builder(context)
                .title(prefName)
                .positiveText(android.R.string.ok)
                .negativeText(android.R.string.cancel)
                .customView(layout, false)
                .autoDismiss(false)
                .onPositive((materialDialog, dialogAction) -> {
                    @SuppressLint("DefaultLocale")
                    final String newPrefValue = String.format("%d%s",
                            valueInput.getValue(),
                            POSSIBLE_TIME_UNIT_SHORT_NAMES[unitInput.getValue()]
                    );
                    PreferenceManager.getDefaultSharedPreferences(context)
                            .edit()
                            .putString(preferenceKey, newPrefValue)
                            .apply();
                    // TODO If the playlist was enabled and now disabled, show a toast
                    //      hinting the user that it can be enabled back in Settings

                    dismiss();
                })
                .onNegative((materialDialog, dialogAction) -> dismiss())
                .build();
    }
}
