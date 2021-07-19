package com.poupa.vinylmusicplayer.preferences;

import java.util.Locale;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.InputType;
import android.util.DisplayMetrics;

import androidx.annotation.NonNull;
import androidx.preference.EditTextPreference;
import com.kabouzeid.appthemehelper.common.prefs.supportv7.ATEPreferenceFragmentCompat;
import com.poupa.vinylmusicplayer.R;
import com.poupa.vinylmusicplayer.misc.RandomAlbum.NextRandomAlbum;
import com.poupa.vinylmusicplayer.util.PreferenceUtil;


public class NextRandomAlbumPreference extends ATEPreferenceFragmentCompat implements OnSharedPreferenceChangeListener {

    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.pref_next_random_album);

        androidx.preference.EditTextPreference editTextPreference = findPreference(PreferenceUtil.NEXT_RANDOM_ALBUM_HISTORY_SIZE);
        editTextPreference.setOnBindEditTextListener(editText -> {
            editText.setInputType(InputType.TYPE_CLASS_NUMBER);
            editText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(1)});
        });

        updateNextRandomAlbumSearchStyle(false);
        updateNextRandomAlbumHistorySize();

        enablePreference();
    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        switch (key) {
            case PreferenceUtil.ALLOW_RANDOM_ALBUM:
                enablePreference();
                break;
            case PreferenceUtil.NEXT_RANDOM_ALBUM_HISTORY_SIZE:
                updateNextRandomAlbumHistorySize();
                break;
            case PreferenceUtil.SEARCH_STYLE:
                updateNextRandomAlbumSearchStyle(true);
                break;
            case PreferenceUtil.SECONDARY_SEARCH_STYLE:
                updateSpecificNextRandomAlbumSearchStyle(PreferenceUtil.SECONDARY_SEARCH_STYLE, PreferenceUtil.TERTIARY_SEARCH_STYLE, PreferenceUtil.SEARCH_STYLE, true);
                break;
            case PreferenceUtil.TERTIARY_SEARCH_STYLE:

                break;
        }
    }

    private void enablePreference() {
        boolean enable = PreferenceUtil.getInstance().allowRandomAlbum();
        findPreference("next_random_album_history_size").setEnabled(enable);
        findPreference("search_style").setEnabled(enable);
        findPreference("secondary_search_style").setEnabled(enable);
        findPreference("tertiary_search_style").setEnabled(enable);
    }

    private void updateNextRandomAlbumSearchStyle(boolean reset) {
        boolean isShown = updateSpecificNextRandomAlbumSearchStyle(PreferenceUtil.SEARCH_STYLE, PreferenceUtil.SECONDARY_SEARCH_STYLE, null, reset);
        if (!isShown) {
            updateSpecificNextRandomAlbumSearchStyle(PreferenceUtil.SEARCH_STYLE, PreferenceUtil.TERTIARY_SEARCH_STYLE, null, reset);
        } else {
            updateSpecificNextRandomAlbumSearchStyle(PreferenceUtil.SECONDARY_SEARCH_STYLE, PreferenceUtil.TERTIARY_SEARCH_STYLE, PreferenceUtil.SEARCH_STYLE, reset);
        }
    }

    private boolean updateSpecificNextRandomAlbumSearchStyle(@NonNull CharSequence key, @NonNull CharSequence otherListKey, CharSequence test, boolean reset) {
        String keyEntry = PreferenceUtil.getInstance().getNextRandomAlbumSearchHistory(key.toString());
        boolean enablePreference = !(keyEntry.equals("none") || keyEntry.equals("random"));

        com.kabouzeid.appthemehelper.common.prefs.supportv7.ATEListPreference otherListPreference = findPreference(otherListKey);
        if (enablePreference) {
            CharSequence[] entries = null;
            CharSequence[] entriesValues = null;
            int noneValue = 0;

            if (keyEntry.equals("artist") && test == null) {
                entries = new CharSequence[]{getContext().getResources().getString(R.string.genre), getContext().getResources().getString(R.string.random), getContext().getResources().getString(R.string.none)};
                entriesValues = new CharSequence[]{"genre", "random", "none"};
                noneValue = 2;
            } else if (keyEntry.equals("genre") && test == null) {
                entries = new CharSequence[]{getContext().getResources().getString(R.string.artist), getContext().getResources().getString(R.string.random), getContext().getResources().getString(R.string.none)};
                entriesValues = new CharSequence[]{"artist", "random", "none"};
                noneValue = 2;
            } else if (test != null) {
                entries = new CharSequence[]{getContext().getResources().getString(R.string.random), getContext().getResources().getString(R.string.none)};
                entriesValues = new CharSequence[]{"random", "none"};
                noneValue = 1;
            }

            otherListPreference.setEntries(entries);
            otherListPreference.setEntryValues(entriesValues);
            if (reset)
                otherListPreference.setValueIndex(noneValue);

        }

        otherListPreference.setEnabled(enablePreference);
        otherListPreference.setVisible(enablePreference);

        return enablePreference;
    }

    private void updateNextRandomAlbumHistorySize() {
        EditTextPreference editTextPreference = findPreference(PreferenceUtil.NEXT_RANDOM_ALBUM_HISTORY_SIZE);
        editTextPreference.setSummary(editTextPreference.getText());

        NextRandomAlbum.getInstance().setHistoriesSize(PreferenceUtil.getInstance().getNextRandomAlbumHistorySize());
    }

}
