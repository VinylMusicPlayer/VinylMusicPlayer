package com.poupa.vinylmusicplayer.ui.activities;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.material.navigation.NavigationView;
import com.kabouzeid.appthemehelper.ThemeStore;
import com.kabouzeid.appthemehelper.util.ATHUtil;
import com.kabouzeid.appthemehelper.util.NavigationViewUtil;
import com.poupa.vinylmusicplayer.R;
import com.poupa.vinylmusicplayer.databinding.ActivityMainContentBinding;
import com.poupa.vinylmusicplayer.databinding.ActivityMainDrawerLayoutBinding;
import com.poupa.vinylmusicplayer.databinding.SlidingMusicPanelLayoutBinding;
import com.poupa.vinylmusicplayer.dialogs.ChangelogDialog;
import com.poupa.vinylmusicplayer.discog.Discography;
import com.poupa.vinylmusicplayer.glide.GlideApp;
import com.poupa.vinylmusicplayer.glide.VinylGlideExtension;
import com.poupa.vinylmusicplayer.helper.MusicPlayerRemote;
import com.poupa.vinylmusicplayer.helper.SearchQueryHelper;
import com.poupa.vinylmusicplayer.interfaces.PaletteColorHolder;
import com.poupa.vinylmusicplayer.loader.AlbumLoader;
import com.poupa.vinylmusicplayer.loader.ArtistLoader;
import com.poupa.vinylmusicplayer.model.Song;
import com.poupa.vinylmusicplayer.provider.StaticPlaylist;
import com.poupa.vinylmusicplayer.service.MusicService;
import com.poupa.vinylmusicplayer.ui.activities.base.AbsSlidingMusicPanelActivity;
import com.poupa.vinylmusicplayer.ui.activities.intro.AppIntroActivity;
import com.poupa.vinylmusicplayer.ui.fragments.mainactivity.folders.FoldersFragment;
import com.poupa.vinylmusicplayer.ui.fragments.mainactivity.library.LibraryFragment;
import com.poupa.vinylmusicplayer.util.FileUtil;
import com.poupa.vinylmusicplayer.util.MusicUtil;
import com.poupa.vinylmusicplayer.util.PreferenceUtil;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import java.io.File;
import java.util.List;

public class MainActivity extends AbsSlidingMusicPanelActivity implements PaletteColorHolder {
    public static final String TAG = MainActivity.class.getSimpleName();
    public static final int APP_INTRO_REQUEST = 100;

    private static final int LIBRARY = 0;
    private static final int FOLDERS = 1;
    private static final int SD_FOLDERS = 2;

    NavigationView navigationView;
    DrawerLayout drawerLayout;

    @Nullable
    MainActivityFragmentCallbacks currentFragment;

    @Nullable
    private View navigationDrawerHeader;

    private boolean blockRequestPermissions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setDrawUnderStatusbar();

        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT) {
            navigationView.setFitsSystemWindows(false); // for header to go below statusbar
        }

        setUpDrawerLayout();

        if (savedInstanceState == null) {
            setMusicChooser(PreferenceUtil.getInstance().getLastMusicChooser());
        } else {
            restoreCurrentFragment();
        }

        if (!checkShowIntro()) {
            showChangelog();
        }
    }

    private void setMusicChooser(int key) {
        PreferenceUtil.getInstance().setLastMusicChooser(key);
        switch (key) {
            case LIBRARY:
                navigationView.setCheckedItem(R.id.nav_library);
                setCurrentFragment(LibraryFragment.newInstance());
                break;
            case SD_FOLDERS:
                final File cardPath = FileUtil.getSDCardDirectory(this);
                if (cardPath != null) {
                    navigationView.setCheckedItem(R.id.nav_sd_folders);
                    setCurrentFragment(FoldersFragment.newInstance(cardPath));
                    break;
                }
                // else fall-though to the case FOLDERS
                // unlikely to have this case, since the SD_FOLDERS choice is only available if the cardPath exists
            case FOLDERS:
                navigationView.setCheckedItem(R.id.nav_folders);
                setCurrentFragment(FoldersFragment.newInstance());
                break;
        }
    }

    private void setCurrentFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, fragment, null).commit();
        currentFragment = (MainActivityFragmentCallbacks) fragment;
    }

    private void restoreCurrentFragment() {
        currentFragment = (MainActivityFragmentCallbacks) getSupportFragmentManager().findFragmentById(R.id.fragment_container);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == APP_INTRO_REQUEST) {
            blockRequestPermissions = false;
            if (!hasPermissions()) {
                requestPermissions();
            }
        }
    }

    @Override
    protected void requestPermissions() {
        if (!blockRequestPermissions) super.requestPermissions();
    }

    @Override
    protected View createContentView() {
        ActivityMainDrawerLayoutBinding binding = ActivityMainDrawerLayoutBinding.inflate(getLayoutInflater());
        navigationView = binding.navigationView;
        drawerLayout = binding.drawerLayout;

        ViewGroup drawerContent = binding.drawerContentContainer;

        SlidingMusicPanelLayoutBinding slidingPanelBinding = createSlidingMusicPanel();
        ActivityMainContentBinding.inflate(
                getLayoutInflater(),
                slidingPanelBinding.contentContainer,
                true);
        drawerContent.addView(slidingPanelBinding.getRoot());

        return binding.getRoot();
    }

    private void setUpNavigationView() {
        int accentColor = ThemeStore.accentColor(this);
        NavigationViewUtil.setItemIconColors(navigationView, ATHUtil.resolveColor(this, R.attr.iconColor, ThemeStore.textColorSecondary(this)), accentColor);
        NavigationViewUtil.setItemTextColors(navigationView, ThemeStore.textColorPrimary(this), accentColor);
        if(FileUtil.getSDCardDirectory(this) != null){
            navigationView.getMenu().findItem(R.id.nav_sd_folders).setVisible(true);
        }

        navigationView.setNavigationItemSelectedListener(menuItem -> {
            drawerLayout.closeDrawers();
            final int itemId = menuItem.getItemId();
            if (itemId == R.id.nav_library) {
                new Handler().postDelayed(() -> setMusicChooser(LIBRARY), 200);
            } else if (itemId == R.id.nav_folders) {
                new Handler().postDelayed(() -> setMusicChooser(FOLDERS), 200);
            } else if (itemId == R.id.nav_sd_folders) {
                new Handler().postDelayed(() -> setMusicChooser(SD_FOLDERS), 200);
            } else if (itemId == R.id.action_reset_discography) {
                new MaterialDialog.Builder(this)
                        .title(R.string.reset_discography)
                        .content(R.string.reset_discography_warning)
                        .autoDismiss(true)
                        .onPositive((dialog, which) -> Discography.getInstance().triggerSyncWithMediaStore(true))
                        .positiveText(R.string.reset_discography)
                        .negativeText(android.R.string.cancel)
                        .show();
            } else if (itemId == R.id.nav_settings) {
                new Handler().postDelayed(() -> startActivity(new Intent(MainActivity.this, SettingsActivity.class)), 200);
            } else if (itemId == R.id.nav_about) {
                new Handler().postDelayed(() -> startActivity(new Intent(MainActivity.this, AboutActivity.class)), 200);
            }
            return true;
        });
    }

    private void setUpDrawerLayout() {
        setUpNavigationView();
    }

    private void updateNavigationDrawerHeader() {
        if (!MusicPlayerRemote.getPlayingQueue().isEmpty()) {
            Song song = MusicPlayerRemote.getCurrentSong();
            if (navigationDrawerHeader == null) {
                navigationDrawerHeader = navigationView.inflateHeaderView(R.layout.navigation_drawer_header);
                navigationDrawerHeader.setOnClickListener(v -> {
                    drawerLayout.closeDrawers();
                    if (getPanelState() == SlidingUpPanelLayout.PanelState.COLLAPSED) {
                        expandPanel();
                    }
                });
            }
            ((TextView) navigationDrawerHeader.findViewById(R.id.title)).setText(song.getTitle());
            ((TextView) navigationDrawerHeader.findViewById(R.id.text)).setText(MusicUtil.getSongInfoString(song));
            GlideApp.with(this)
                    .asDrawable()
                    .load(VinylGlideExtension.getSongModel(song))
                    .transition(VinylGlideExtension.getDefaultTransition())
                    .songOptions(song)
                    .into(((ImageView) navigationDrawerHeader.findViewById(R.id.image)));
        } else {
            if (navigationDrawerHeader != null) {
                navigationView.removeHeaderView(navigationDrawerHeader);
                navigationDrawerHeader = null;
            }
        }
    }

    @Override
    public void onPlayingMetaChanged() {
        super.onPlayingMetaChanged();
        updateNavigationDrawerHeader();
    }

    @Override
    public void onServiceConnected() {
        super.onServiceConnected();
        updateNavigationDrawerHeader();
        handlePlaybackIntent(getIntent());
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            if (drawerLayout.isDrawerOpen(navigationView)) {
                drawerLayout.closeDrawer(navigationView);
            } else {
                drawerLayout.openDrawer(navigationView);
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean handleBackPress() {
        if (drawerLayout.isDrawerOpen(navigationView)) {
            drawerLayout.closeDrawers();
            return true;
        }
        return super.handleBackPress() || (currentFragment != null && currentFragment.handleBackPress());
    }

    private void handlePlaybackIntent(@Nullable Intent intent) {
        if (intent == null) {
            return;
        }

        Uri uri = intent.getData();
        String mimeType = intent.getType();
        boolean handled = false;

        if (intent.getAction() != null && intent.getAction().equals(MediaStore.INTENT_ACTION_MEDIA_PLAY_FROM_SEARCH)) {
            final String focus = intent.getStringExtra(MediaStore.EXTRA_MEDIA_FOCUS);
            final List<? extends Song> songs =
                    SearchQueryHelper.getSongs(focus, intent.getExtras());
            // Guards against no songs found. Will cause a crash otherwise
            if (songs.size() > 0) {
                if (MusicPlayerRemote.getShuffleMode() == MusicService.SHUFFLE_MODE_SHUFFLE) {
                    MusicPlayerRemote.openAndShuffleQueue(songs, true);
                } else {
                    MusicPlayerRemote.openQueue(songs, 0, true);
                }
                handled = true;
            }
        }

        if (uri != null && uri.toString().length() > 0) {
            MusicPlayerRemote.playFromUri(uri);
            handled = true;
        } else if (MediaStore.Audio.Playlists.CONTENT_TYPE.equals(mimeType)) {
            final long id = parseIdFromIntent(intent, "playlistId", "playlist");
            if (id >= 0) {
                int position = intent.getIntExtra("position", 0);
                final StaticPlaylist playlist = StaticPlaylist.getPlaylist(id);
                if (playlist != null) {
                    MusicPlayerRemote.openQueue(playlist.asSongs(), position, true);
                }
                handled = true;
            }
        } else if (MediaStore.Audio.Albums.CONTENT_TYPE.equals(mimeType)) {
            final long id = parseIdFromIntent(intent, "albumId", "album");
            if (id >= 0) {
                int position = intent.getIntExtra("position", 0);
                MusicPlayerRemote.openQueue(AlbumLoader.getAlbum(id).songs, position, true);
                handled = true;
            }
        } else if (MediaStore.Audio.Artists.CONTENT_TYPE.equals(mimeType)) {
            final long id = parseIdFromIntent(intent, "artistId", "artist");
            if (id >= 0) {
                // TODO ArtistId might be not usable if it's sent by another app
                //      Discography (used by ArtistLoader) has an internal ID
                int position = intent.getIntExtra("position", 0);
                MusicPlayerRemote.openQueue(ArtistLoader.getArtist(id).getSongs(), position, true);
                handled = true;
            }
        }
        if (handled) {
            setIntent(new Intent());
        }
    }

    private long parseIdFromIntent(@NonNull Intent intent, String longKey,
                                   String stringKey) {
        long id = intent.getLongExtra(longKey, -1);
        if (id < 0) {
            String idString = intent.getStringExtra(stringKey);
            if (idString != null) {
                try {
                    id = Long.parseLong(idString);
                } catch (NumberFormatException e) {
                    Log.e(TAG, e.getMessage());
                }
            }
        }
        return id;
    }

    @Override
    public void onPanelExpanded(View view) {
        super.onPanelExpanded(view);
        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
    }

    @Override
    public void onPanelCollapsed(View view) {
        super.onPanelCollapsed(view);
        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
    }

    private boolean checkShowIntro() {
        if (!PreferenceUtil.getInstance().introShown()) {
            PreferenceUtil.getInstance().setIntroShown();
            ChangelogDialog.setChangelogRead(this);
            blockRequestPermissions = true;
            new Handler().postDelayed(() -> startActivityForResult(new Intent(MainActivity.this, AppIntroActivity.class), APP_INTRO_REQUEST), 50);
            return true;
        }
        return false;
    }

    private void showChangelog() {
        try {
            PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            int currentVersion = pInfo.versionCode;
            if (currentVersion != PreferenceUtil.getInstance().getLastChangelogVersion()) {
                new ChangelogDialog.Builder(this).show();
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void reload() {
    }

    public interface MainActivityFragmentCallbacks {
        boolean handleBackPress();
    }

    @Override
    @ColorInt
    public int getPaletteColor() {return ThemeStore.primaryColor(this);}
}
