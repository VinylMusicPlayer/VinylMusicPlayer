package com.poupa.vinylmusicplayer.ui.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.media.audiofx.AudioEffect;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.DialogFragment;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceManager;
import androidx.preference.TwoStatePreference;

import com.afollestad.materialdialogs.color.ColorChooserDialog;
import com.kabouzeid.appthemehelper.ThemeStore;
import com.kabouzeid.appthemehelper.common.prefs.supportv7.ATEColorPreference;
import com.kabouzeid.appthemehelper.common.prefs.supportv7.ATEPreferenceFragmentCompat;
import com.kabouzeid.appthemehelper.util.ColorUtil;
import com.poupa.vinylmusicplayer.R;
import com.poupa.vinylmusicplayer.appshortcuts.DynamicShortcutManager;
import com.poupa.vinylmusicplayer.databinding.ActivityPreferencesBinding;
import com.poupa.vinylmusicplayer.dialogs.BottomSheetDialog.BottomSheetDialogWithButtons;
import com.poupa.vinylmusicplayer.dialogs.BottomSheetDialog.BottomSheetDialogWithButtons.ButtonInfo;
import com.poupa.vinylmusicplayer.preferences.BlacklistPreference;
import com.poupa.vinylmusicplayer.preferences.BlacklistPreferenceDialog;
import com.poupa.vinylmusicplayer.preferences.ExportSettingsPreference;
import com.poupa.vinylmusicplayer.preferences.ExportSettingsPreferenceDialog;
import com.poupa.vinylmusicplayer.preferences.LibraryPreference;
import com.poupa.vinylmusicplayer.preferences.LibraryPreferenceDialog;
import com.poupa.vinylmusicplayer.preferences.NowPlayingScreenPreference;
import com.poupa.vinylmusicplayer.preferences.NowPlayingScreenPreferenceDialog;
import com.poupa.vinylmusicplayer.preferences.PreAmpPreference;
import com.poupa.vinylmusicplayer.preferences.PreAmpPreferenceDialog;
import com.poupa.vinylmusicplayer.preferences.SharedPreferencesImporter;
import com.poupa.vinylmusicplayer.preferences.SmartPlaylistPreference;
import com.poupa.vinylmusicplayer.preferences.SmartPlaylistPreferenceDialog;
import com.poupa.vinylmusicplayer.preferences.SongConfirmationPreference;
import com.poupa.vinylmusicplayer.service.MusicService;
import com.poupa.vinylmusicplayer.ui.activities.base.AbsBaseActivity;
import com.poupa.vinylmusicplayer.ui.fragments.player.NowPlayingScreen;
import com.poupa.vinylmusicplayer.util.FileUtil;
import com.poupa.vinylmusicplayer.util.ImageTheme.ThemeStyleUtil;
import com.poupa.vinylmusicplayer.util.MusicUtil;
import com.poupa.vinylmusicplayer.util.NavigationUtil;
import com.poupa.vinylmusicplayer.util.PreferenceUtil;
import com.poupa.vinylmusicplayer.util.VinylMusicPlayerColorUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;


public class SettingsActivity extends AbsBaseActivity implements ColorChooserDialog.ColorCallback {

    Toolbar toolbar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityPreferencesBinding binding = ActivityPreferencesBinding.inflate(LayoutInflater.from(this));
        toolbar = binding.toolbar;
        setContentView(binding.getRoot());

        setDrawUnderStatusbar();

        setStatusbarColorAuto();
        setNavigationbarColorAuto();
        setTaskDescriptionColorAuto();

        toolbar.setBackgroundColor(PreferenceUtil.getInstance().getPrimaryColor());
        setSupportActionBar(toolbar);
        //noinspection ConstantConditions
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.content_frame, new SettingsFragment()).commit();
        } else {
            SettingsFragment frag = (SettingsFragment) getSupportFragmentManager().findFragmentById(R.id.content_frame);
            if (frag != null) frag.invalidateSettings();
        }

        final Collection<String> usedUndeclaredPrefKeys = PreferenceUtil.getInstance().getUndeclaredPrefKeys();
        for(String prefKey : usedUndeclaredPrefKeys){
            Log.i(this.getClass().getSimpleName(), "Used but not declared Pref Key: " + prefKey);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        SettingsFragment frag = (SettingsFragment) getSupportFragmentManager().findFragmentById(R.id.content_frame);
        if (frag != null) frag.invalidateSettings();
    }

    @Override
    public void onColorSelection(@NonNull ColorChooserDialog dialog, @ColorInt int selectedColor) {
        final int title = dialog.getTitle();
        if (title == R.string.primary_color) {
            PreferenceUtil.getInstance().setPrimaryColor(selectedColor);
            ThemeStore.editTheme(this)
                    .primaryColor(selectedColor)
                    .commit();
        } else if (title == R.string.accent_color) {
            PreferenceUtil.getInstance().setAccentColor(selectedColor);
            ThemeStore.editTheme(this)
                    .accentColor(selectedColor)
                    .commit();
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            new DynamicShortcutManager(this).updateDynamicShortcuts();
        }
        recreate();
    }

    @Override
    public void onColorChooserDismissed(@NonNull ColorChooserDialog dialog) {
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public static class SettingsFragment extends ATEPreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {

        ActivityResultLauncher sharedPreferencesImporter;

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            sharedPreferencesImporter = registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(), result -> {
                        // Set the theming and recreate the settings activity after importing.
                        Activity activity = getActivity();
                        if(result != null) {
                            ThemeStore.editTheme(activity).primaryColor(PreferenceUtil.getInstance().getPrimaryColor()).commit();;
                            ThemeStore.editTheme(activity).accentColor(PreferenceUtil.getInstance().getAccentColor()).commit();
                            activity.recreate();
                        }
                    });
        }

        private static void setSummary(@NonNull Preference preference) {
            setSummary(preference, PreferenceManager
                    .getDefaultSharedPreferences(preference.getContext())
                    .getString(preference.getKey(), ""));
        }

        private static void setSummary(Preference preference, @NonNull Object value) {
            String stringValue = value.toString();

            if (preference instanceof ListPreference) {
                ListPreference listPreference = (ListPreference) preference;
                int index = listPreference.findIndexOfValue(stringValue);
                preference.setSummary(
                        index >= 0
                                ? listPreference.getEntries()[index]
                                : null);
            } else {
                preference.setSummary(stringValue);
            }
        }

        @Override
        public void onCreatePreferences(Bundle bundle, String s) {
            addPreferencesFromResource(R.xml.pref_library);
            addPreferencesFromResource(R.xml.pref_colors);
            addPreferencesFromResource(R.xml.pref_notification);
            addPreferencesFromResource(R.xml.pref_now_playing_screen);
            addPreferencesFromResource(R.xml.pref_images);
            addPreferencesFromResource(R.xml.pref_audio);
            addPreferencesFromResource(R.xml.pref_playlists);
            addPreferencesFromResource(R.xml.pref_migrating);
            addPreferencesFromResource(R.xml.pref_development);

            // set summary for whitelist, in order to indicate start directory
            final String strSummaryWhitelist = getString(R.string.pref_summary_whitelist);
            final File startDirectory = PreferenceUtil.getInstance().getStartDirectory();
            final String startPath = FileUtil.safeGetCanonicalPath(startDirectory);
            findPreference(PreferenceUtil.WHITELIST_ENABLED).setSummary(strSummaryWhitelist+startPath);
        }

        @Nullable
        @Override
        public DialogFragment onCreatePreferenceDialog(Preference preference) {
            if (preference instanceof NowPlayingScreenPreference) {
                return NowPlayingScreenPreferenceDialog.newInstance();
            } else if (preference instanceof BlacklistPreference) {
                return BlacklistPreferenceDialog.newInstance();
            } else if (preference instanceof LibraryPreference) {
                return LibraryPreferenceDialog.newInstance();
            } else if (preference instanceof PreAmpPreference) {
                return PreAmpPreferenceDialog.newInstance();
            } else if (preference instanceof SmartPlaylistPreference) {
                return SmartPlaylistPreferenceDialog.newInstance(preference.getKey());
            } else if (preference instanceof ExportSettingsPreference) {
                return ExportSettingsPreferenceDialog.newInstance();
            } else if (preference instanceof SongConfirmationPreference) {
                final List<ButtonInfo> possibleActions = Arrays.asList(
                        SongConfirmationPreference.ASK.setAction(() -> PreferenceUtil.getInstance().setEnqueueSongsDefaultChoice(PreferenceUtil.ENQUEUE_SONGS_CHOICE_ASK)),
                        SongConfirmationPreference.REPLACE.setAction(() -> PreferenceUtil.getInstance().setEnqueueSongsDefaultChoice(PreferenceUtil.ENQUEUE_SONGS_CHOICE_REPLACE)),
                        SongConfirmationPreference.NEXT.setAction(() -> PreferenceUtil.getInstance().setEnqueueSongsDefaultChoice(PreferenceUtil.ENQUEUE_SONGS_CHOICE_NEXT)),
                        SongConfirmationPreference.ADD.setAction(() -> PreferenceUtil.getInstance().setEnqueueSongsDefaultChoice(PreferenceUtil.ENQUEUE_SONGS_CHOICE_ADD))
                );
                int defaultValue = -1;
                int id = PreferenceUtil.getInstance().getEnqueueSongsDefaultChoice();
                for (int i = 0; i < possibleActions.size(); i++) {
                    if (id == possibleActions.get(i).id) {
                        defaultValue = i;
                    }
                }
                BottomSheetDialogWithButtons songActionDialog = BottomSheetDialogWithButtons.newInstance();
                songActionDialog.setTitle(getContext().getResources().getString(R.string.pref_title_enqueue_song_default_choice))
                        .setButtonList(possibleActions)
                        .setDefaultButtonIndex(defaultValue);
                return songActionDialog;
            }
            return super.onCreatePreferenceDialog(preference);
        }

        @Override
        public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            getListView().setPadding(0, 0, 0, 0);
            invalidateSettings();
            PreferenceUtil.getInstance().registerOnSharedPreferenceChangedListener(this);
        }

        @Override
        public void onDestroyView() {
            super.onDestroyView();
            PreferenceUtil.getInstance().unregisterOnSharedPreferenceChangedListener(this);
        }

        void invalidateSettings() {
            final Preference generalTheme = findPreference(PreferenceUtil.GENERAL_THEME);
            if (generalTheme != null) {
                final Context context = getContext();
                if (context != null && VinylMusicPlayerColorUtil.isSystemThemeSupported()) {
                    // Extend the list, to add choices to follow system theme
                    ListPreference listPref = (ListPreference) generalTheme;
                    ArrayList<CharSequence> entries = new ArrayList<>(Arrays.asList(listPref.getEntries()));
                    ArrayList<CharSequence> values = new ArrayList<>(Arrays.asList(listPref.getEntryValues()));

                    values.add(PreferenceUtil.GENERAL_THEME_FOLLOW_SYSTEM_LIGHT_OR_DARK);
                    entries.add(String.format("%s\n%s",
                            context.getString(R.string.follow_system_theme_name),
                            MusicUtil.buildInfoString(
                                    context.getString(R.string.light_theme_name),
                                    context.getString(R.string.dark_theme_name)
                            )));

                    values.add(PreferenceUtil.GENERAL_THEME_FOLLOW_SYSTEM_LIGHT_OR_BLACK);
                    entries.add(String.format("%s\n%s",
                            context.getString(R.string.follow_system_theme_name),
                            MusicUtil.buildInfoString(
                                    context.getString(R.string.light_theme_name),
                                    context.getString(R.string.black_theme_name)
                            )));

                    listPref.setEntries(entries.toArray(new CharSequence[0]));
                    listPref.setEntryValues(values.toArray(new CharSequence[0]));
                }

                setSummary(generalTheme);
                generalTheme.setOnPreferenceChangeListener((preference, o) -> {
                    String themeName = (String) o;

                    setSummary(generalTheme, o);

                    if (getActivity() != null) {
                        ThemeStore.markChanged(getActivity());
                    }

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
                        // Set the new theme so that updateAppShortcuts can pull it
                        getActivity().setTheme(PreferenceUtil.getThemeResFromPrefValue(themeName));
                        new DynamicShortcutManager(getActivity()).updateDynamicShortcuts();
                    }

                    getActivity().recreate();
                    return true;
                });
            }

            final Preference themeStyle = findPreference(PreferenceUtil.THEME_STYLE);
            ThemeStyleUtil.updateInstance(PreferenceUtil.getInstance().getThemeStyle());
            //ThemeStore.markChanged(getActivity());
            themeStyle.setOnPreferenceChangeListener((preference, o) -> {
                ThemeStyleUtil.updateInstance((String) o);
                if (getActivity() != null) {
                    ThemeStore.markChanged(getActivity());
                }

                return true;
            });

            final Preference autoDownloadImagesPolicy = findPreference(PreferenceUtil.AUTO_DOWNLOAD_IMAGES_POLICY);
            if (autoDownloadImagesPolicy != null) {
                setSummary(autoDownloadImagesPolicy);
                autoDownloadImagesPolicy.setOnPreferenceChangeListener((preference, o) -> {
                    setSummary(autoDownloadImagesPolicy, o);
                    return true;
                });
            }

            final TwoStatePreference rememberLastTab = findPreference(PreferenceUtil.REMEMBER_LAST_TAB);
            if (rememberLastTab != null) {
                rememberLastTab.setChecked(PreferenceUtil.getInstance().rememberLastTab());
                rememberLastTab.setOnPreferenceChangeListener((preference, newValue) -> {
                    // Save preference
                    PreferenceUtil.getInstance().setRememberLastTab((Boolean) newValue);

                    return true;
                });
            }

            final TwoStatePreference whitelistEnabled = findPreference(PreferenceUtil.WHITELIST_ENABLED);
            if (whitelistEnabled != null) {
                whitelistEnabled.setChecked(PreferenceUtil.getInstance().getWhitelistEnabled());
            }
          
            final ATEColorPreference primaryColorPref = findPreference(PreferenceUtil.PRIMARY_COLOR);
            if (getActivity() != null && primaryColorPref != null) {
                final int primaryColor = PreferenceUtil.getInstance().getPrimaryColor();
                primaryColorPref.setColor(primaryColor, ColorUtil.darkenColor(primaryColor));
                primaryColorPref.setOnPreferenceClickListener(preference -> {
                    new ColorChooserDialog.Builder(getActivity(), R.string.primary_color)
                            .accentMode(false)
                            .allowUserColorInput(true)
                            .allowUserColorInputAlpha(false)
                            .preselect(primaryColor)
                            .show(getActivity());
                    return true;
                });
            }

            final ATEColorPreference accentColorPref = findPreference(PreferenceUtil.ACCENT_COLOR);
            if (getActivity() != null && accentColorPref != null) {
                final int accentColor = PreferenceUtil.getInstance().getAccentColor();
                accentColorPref.setColor(accentColor, ColorUtil.darkenColor(accentColor));
                accentColorPref.setOnPreferenceClickListener(preference -> {
                    new ColorChooserDialog.Builder(getActivity(), R.string.accent_color)
                            .accentMode(true)
                            .allowUserColorInput(true)
                            .allowUserColorInputAlpha(false)
                            .preselect(accentColor)
                            .show(getActivity());
                    return true;
                });
            }

            TwoStatePreference colorNavBar = findPreference(PreferenceUtil.COLORED_NAVBAR);
            if (colorNavBar != null) {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                    colorNavBar.setVisible(false);
                } else {
                    final Activity activity = requireActivity();
                    colorNavBar.setChecked(PreferenceUtil.getInstance().coloredNavigationBar());
                    colorNavBar.setOnPreferenceChangeListener((preference, newValue) -> {
                        PreferenceUtil.getInstance().setColoredNavigationBar((Boolean) newValue);
                        ThemeStore.editTheme(activity)
                                .coloredNavigationBar(PreferenceUtil.getInstance().coloredNavigationBar())
                                .commit();
                        activity.recreate();
                        return true;
                    });
                }
            }

            final TwoStatePreference classicNotification = findPreference(PreferenceUtil.CLASSIC_NOTIFICATION);
            if (classicNotification != null) {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
                    classicNotification.setVisible(false);
                } else {
                    classicNotification.setChecked(PreferenceUtil.getInstance().classicNotification());
                    classicNotification.setOnPreferenceChangeListener((preference, newValue) -> {
                        // Save preference
                        PreferenceUtil.getInstance().setClassicNotification((Boolean) newValue);
                        return true;
                    });
                }
            }

            final TwoStatePreference coloredNotification = findPreference(PreferenceUtil.COLORED_NOTIFICATION);
            if (coloredNotification != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    coloredNotification.setEnabled(PreferenceUtil.getInstance().classicNotification());
                } else {
                    coloredNotification.setChecked(PreferenceUtil.getInstance().coloredNotification());
                    coloredNotification.setOnPreferenceChangeListener((preference, newValue) -> {
                        // Save preference
                        PreferenceUtil.getInstance().setColoredNotification((Boolean) newValue);
                        return true;
                    });
                }
            }

            final TwoStatePreference colorAppShortcuts = findPreference(PreferenceUtil.COLORED_APP_SHORTCUTS);
            if (colorAppShortcuts != null) {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N_MR1) {
                    colorAppShortcuts.setVisible(false);
                } else {
                    colorAppShortcuts.setChecked(PreferenceUtil.getInstance().coloredAppShortcuts());
                    colorAppShortcuts.setOnPreferenceChangeListener((preference, newValue) -> {
                        // Save preference
                        PreferenceUtil.getInstance().setColoredAppShortcuts((Boolean) newValue);

                        // Update app shortcuts
                        new DynamicShortcutManager(getActivity()).updateDynamicShortcuts();

                        return true;
                    });
                }
            }

            final TwoStatePreference transparentWidgets = findPreference(PreferenceUtil.TRANSPARENT_BACKGROUND_WIDGET);
            if (transparentWidgets != null) {
                transparentWidgets.setChecked(PreferenceUtil.getInstance().transparentBackgroundWidget());
                transparentWidgets.setOnPreferenceChangeListener((preference, newValue) -> {
                    // Save preference
                    PreferenceUtil.getInstance().setTransparentBackgroundWidget((Boolean) newValue);

                    // Update app shortcuts
                    new DynamicShortcutManager(getActivity()).updateDynamicShortcuts();

                    return true;
                });
            }

            final TwoStatePreference audioDucking = findPreference(PreferenceUtil.AUDIO_DUCKING);
            if (audioDucking != null) {
                audioDucking.setChecked(PreferenceUtil.getInstance().audioDucking());
                audioDucking.setOnPreferenceChangeListener((preference, newValue) -> {
                    // Save preference
                    PreferenceUtil.getInstance().setAudioDucking((Boolean) newValue);

                    return true;
                });
            }

            final TwoStatePreference gaplessPlayback = findPreference(PreferenceUtil.GAPLESS_PLAYBACK);
            if (gaplessPlayback != null) {
                gaplessPlayback.setChecked(PreferenceUtil.getInstance().gaplessPlayback());
                gaplessPlayback.setOnPreferenceChangeListener((preference, newValue) -> {
                    // Save preference
                    PreferenceUtil.getInstance().setGaplessPlayback((Boolean) newValue);

                    return true;
                });
            }

            final TwoStatePreference rememberShuffle = findPreference(PreferenceUtil.REMEMBER_SHUFFLE);
            if (rememberShuffle != null) {
                rememberShuffle.setChecked(PreferenceUtil.getInstance().rememberShuffle());
                rememberShuffle.setOnPreferenceChangeListener((preference, newValue) -> {
                    // Save preference
                    PreferenceUtil.getInstance().setRememberShuffle((Boolean) newValue);

                    return true;
                });
            }
          
            final Preference equalizer = findPreference(PreferenceUtil.EQUALIZER);
            if (equalizer != null) {
                if (!hasEqualizer()) {
                    equalizer.setEnabled(false);
                    equalizer.setSummary(getResources().getString(R.string.no_equalizer));
                }
                equalizer.setOnPreferenceClickListener(preference -> {
                    NavigationUtil.openEqualizer(getActivity());
                    return true;
                });
            }

            if (PreferenceUtil.getInstance().getReplayGainSourceMode().equals(PreferenceUtil.RG_SOURCE_MODE_NONE)) {
                Preference pref = findPreference(PreferenceUtil.RG_PREAMP);
                if (pref != null) {
                    pref.setEnabled(false);
                    pref.setSummary(getResources().getString(R.string.pref_rg_disabled));
                }
            }

            final TwoStatePreference maintainTopTrackPlaylist = findPreference(PreferenceUtil.MAINTAIN_TOP_TRACKS_PLAYLIST);
            if (maintainTopTrackPlaylist != null) {
                maintainTopTrackPlaylist.setChecked(PreferenceUtil.getInstance().maintainTopTrackPlaylist());
                maintainTopTrackPlaylist.setOnPreferenceChangeListener((preference, newValue) -> {
                    // Save preference
                    PreferenceUtil.getInstance().setMaintainTopTrackPlaylist((Boolean) newValue);

                    return true;
                });
            }

            final TwoStatePreference maintainSkippedSongsPlaylist = findPreference(PreferenceUtil.MAINTAIN_SKIPPED_SONGS_PLAYLIST);
            if (maintainSkippedSongsPlaylist != null) {
                maintainSkippedSongsPlaylist.setChecked(PreferenceUtil.getInstance().maintainSkippedSongsPlaylist());
                maintainSkippedSongsPlaylist.setOnPreferenceChangeListener((preference, newValue) -> {
                    // Save preference
                    PreferenceUtil.getInstance().setMaintainSkippedSongsPlaylist((Boolean) newValue);

                    return true;
                });
            }

            final TwoStatePreference oopsHandlerEnabled = findPreference(PreferenceUtil.OOPS_HANDLER_ENABLED);
            if (oopsHandlerEnabled != null) {
                oopsHandlerEnabled.setChecked(PreferenceUtil.getInstance().isOopsHandlerEnabled());
                oopsHandlerEnabled.setOnPreferenceChangeListener((preference, newValue) -> {
                    // Save preference
                    PreferenceUtil.getInstance().setOopsHandlerEnabled((Boolean) newValue);

                    return true;
                });
            }

            final Preference importSettings = findPreference(PreferenceUtil.IMPORT_SETTINGS);
            if (importSettings != null) {
                importSettings.setOnPreferenceClickListener((preference) -> {
                    sharedPreferencesImporter.launch(new Intent(getContext(), SharedPreferencesImporter.class));
                    return true;
                });
            }

            updateNowPlayingScreen();
            updatePlaylistsSummary();
            updateConfirmationSongSummary();
        }

        private boolean hasEqualizer() {
            final Intent effects = new Intent(AudioEffect.ACTION_DISPLAY_AUDIO_EFFECT_CONTROL_PANEL);
            if (getActivity() != null) {
                PackageManager pm = getActivity().getPackageManager();
                ResolveInfo ri = pm.resolveActivity(effects, 0);
                return ri != null;
            }
            return false;
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            if (TextUtils.equals(key, PreferenceUtil.NOW_PLAYING_SCREEN_ID)) {
                updateNowPlayingScreen();
            } else if (TextUtils.equals(key, PreferenceUtil.CLASSIC_NOTIFICATION)) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    findPreference(PreferenceUtil.COLORED_NOTIFICATION).setEnabled(sharedPreferences.getBoolean(key, false));
                }
            } else if (TextUtils.equals(key, PreferenceUtil.RG_SOURCE_MODE_V2)) {
                Preference pref = findPreference(PreferenceUtil.RG_PREAMP);
                if (pref != null) {
                    if (!sharedPreferences.getString(key, PreferenceUtil.RG_SOURCE_MODE_NONE).equals(PreferenceUtil.RG_SOURCE_MODE_NONE)) {
                        pref.setEnabled(true);
                        pref.setSummary(R.string.pref_summary_rg_preamp);
                    } else {
                        pref.setEnabled(false);
                        pref.setSummary(getResources().getString(R.string.pref_rg_disabled));
                    }
                }
            } else if (TextUtils.equals(key, PreferenceUtil.WHITELIST_ENABLED)) {
                getContext().sendBroadcast(new Intent(MusicService.MEDIA_STORE_CHANGED));
            } else if (TextUtils.equals(key, PreferenceUtil.RECENTLY_PLAYED_CUTOFF_V2)
                    || TextUtils.equals(key, PreferenceUtil.NOT_RECENTLY_PLAYED_CUTOFF_V2)
                    || TextUtils.equals(key, PreferenceUtil.LAST_ADDED_CUTOFF_V2)
            ) {
                updatePlaylistsSummary();
            } else if (TextUtils.equals(key, PreferenceUtil.ENQUEUE_SONGS_DEFAULT_CHOICE)) {
                updateConfirmationSongSummary();
            }
        }

        private void updateNowPlayingScreen() {
            final Preference nowPlayingScreenPref = findPreference(PreferenceUtil.NOW_PLAYING_SCREEN_ID);
            NowPlayingScreen nowPlayingScreen = PreferenceUtil.getInstance().getNowPlayingScreen();
            nowPlayingScreenPref.setSummary(nowPlayingScreen.titleRes);
            NowPlayingScreenPreferenceDialog.newInstance().onPageSelected(nowPlayingScreen.id);
        }

        private void updatePlaylistsSummary() {
            final Context context = getContext();
            final PreferenceUtil preferenceUtil = PreferenceUtil.getInstance();

            findPreference(PreferenceUtil.RECENTLY_PLAYED_CUTOFF_V2)
                    .setSummary(preferenceUtil.getRecentlyPlayedCutoffText(context));
            findPreference(PreferenceUtil.NOT_RECENTLY_PLAYED_CUTOFF_V2)
                    .setSummary(preferenceUtil.getNotRecentlyPlayedCutoffText(context));
            findPreference(PreferenceUtil.LAST_ADDED_CUTOFF_V2)
                    .setSummary(preferenceUtil.getLastAddedCutoffText(context));
        }

        private void updateConfirmationSongSummary() {
            final int id = PreferenceUtil.getInstance().getEnqueueSongsDefaultChoice();
            for (final BottomSheetDialogWithButtons.ButtonInfo info : SongConfirmationPreference.possibleActions) {
                if (info.id == id) {
                    findPreference(PreferenceUtil.ENQUEUE_SONGS_DEFAULT_CHOICE).setSummary(info.titleId);
                    return;
                }
            }
            findPreference(PreferenceUtil.ENQUEUE_SONGS_DEFAULT_CHOICE).setSummary(R.string.action_always_ask_for_confirmation);
        }
    }
}
