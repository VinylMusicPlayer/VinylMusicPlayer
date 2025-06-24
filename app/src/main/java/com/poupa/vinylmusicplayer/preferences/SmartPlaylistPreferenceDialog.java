package com.poupa.vinylmusicplayer.preferences;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.Gravity;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.NumberPicker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
    private static final String PREFERENCE = "preference";

    @NonNull
    public static SmartPlaylistPreferenceDialog newInstance(@NonNull String preference) {
        return new SmartPlaylistPreferenceDialog(preference);
    }

    @NonNull private String preferenceKey;

    /** Note: default constructor must exist for screen to rotate. */
    public SmartPlaylistPreferenceDialog() {

    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState){
        super.onSaveInstanceState(outState);
        outState.putString(PREFERENCE, preferenceKey);
    }

    public SmartPlaylistPreferenceDialog(@NonNull String preference) {
        preferenceKey = preference;
    }

    static class TimeUnit {
        final @NonNull ChronoUnit unit;
        final @NonNull String preferencePostfix;
        final int stringResourceId;

        TimeUnit(final @NonNull ChronoUnit unit,
                final @NonNull String preferencePostfix,
                final int stringResourceId) {
            this.unit = unit;
            this.preferencePostfix = preferencePostfix;
            this.stringResourceId = stringResourceId;
        }
    }
    final TimeUnit[] POSSIBLE_TIME_UNITS = {
            new TimeUnit(ChronoUnit.DAYS, "d", R.string.day),
            new TimeUnit(ChronoUnit.WEEKS, "w", R.string.week),
            new TimeUnit(ChronoUnit.MONTHS, "m", R.string.month),
            new TimeUnit(ChronoUnit.YEARS, "y", R.string.year)
    };

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        @NonNull final Context context = requireContext();
        final Resources resources = context.getResources();
        if(savedInstanceState != null && savedInstanceState.containsKey(PREFERENCE)){
            this.preferenceKey=  savedInstanceState.getString(PREFERENCE);
        }

        // ---- Retrieve the stored value
        String prefName = "";
        Pair<Integer, ChronoUnit> prefValue = PreferenceUtil.getInstance().getCutoffTimeV2(preferenceKey);
        if (TextUtils.equals(preferenceKey, PreferenceUtil.LAST_ADDED_CUTOFF_V2)) {
            prefName = resources.getString(R.string.pref_title_last_added_interval);
        } else if (TextUtils.equals(preferenceKey, PreferenceUtil.RECENTLY_PLAYED_CUTOFF_V2)) {
            prefName = resources.getString(R.string.pref_title_recently_played_interval);
        } else if (TextUtils.equals(preferenceKey, PreferenceUtil.NOT_RECENTLY_PLAYED_CUTOFF_V2)) {
            prefName = resources.getString(R.string.pref_title_not_recently_played_interval);
        }

        // ---- Build the dialog
        LinearLayout outerLayout = new LinearLayout(context);
        outerLayout.setOrientation(LinearLayout.VERTICAL);
        outerLayout.setGravity(Gravity.CENTER);

        LinearLayout innerUpperLayout = new LinearLayout(context);
        innerUpperLayout.setOrientation(LinearLayout.HORIZONTAL);
        innerUpperLayout.setGravity(Gravity.CENTER);
        outerLayout.addView(innerUpperLayout);

        final NumberPicker valueInput = new NumberPicker(context);
        valueInput.setMinValue(1);
        valueInput.setMaxValue(100);
        valueInput.setValue(prefValue.first);
        innerUpperLayout.addView(valueInput);

        int unitPosition = -1;
        String[] unitDisplayedValues = new String[POSSIBLE_TIME_UNITS.length];
        for (int i=0; i<POSSIBLE_TIME_UNITS.length; ++i) {
            unitDisplayedValues[i] = resources.getString(POSSIBLE_TIME_UNITS[i].stringResourceId);
            if (prefValue.second == POSSIBLE_TIME_UNITS[i].unit) {
                unitPosition = i;
            }
        }
        final NumberPicker unitInput = new NumberPicker(context);
        unitInput.setMinValue(0);
        unitInput.setMaxValue(POSSIBLE_TIME_UNITS.length - 1);
        unitInput.setDisplayedValues(unitDisplayedValues);
        unitInput.setValue(unitPosition);
        innerUpperLayout.addView(unitInput);

        LinearLayout innerLowerLayout = new LinearLayout(context);
        innerLowerLayout.setOrientation(LinearLayout.HORIZONTAL);
        innerLowerLayout.setGravity(Gravity.CENTER);
        outerLayout.addView(innerLowerLayout);

        CheckBox isDisabledCheckbox = new CheckBox(context);
        isDisabledCheckbox.setText(R.string.pref_playlist_disabled);
        isDisabledCheckbox.setChecked(prefValue.first == 0);
        isDisabledCheckbox.setOnCheckedChangeListener((checkBox, isChecked) -> {
            unitInput.setEnabled(!isChecked);
            valueInput.setEnabled(!isChecked);
        });
        innerLowerLayout.addView(isDisabledCheckbox);

        return new MaterialDialog.Builder(context)
                .title(prefName)
                .positiveText(android.R.string.ok)
                .negativeText(android.R.string.cancel)
                .customView(outerLayout, false)
                .autoDismiss(false)
                .onPositive((materialDialog, dialogAction) -> {
                    @SuppressLint("DefaultLocale")
                    final String newPrefValue = isDisabledCheckbox.isChecked()
                            ? "0d"
                            : String.format("%d%s",
                                    valueInput.getValue(),
                                    POSSIBLE_TIME_UNITS[unitInput.getValue()].preferencePostfix
                            );
                    PreferenceManager.getDefaultSharedPreferences(context)
                            .edit()
                            .putString(preferenceKey, newPrefValue)
                            .apply();

                    dismiss();
                })
                .onNegative((materialDialog, dialogAction) -> dismiss())
                .build();
    }
}
