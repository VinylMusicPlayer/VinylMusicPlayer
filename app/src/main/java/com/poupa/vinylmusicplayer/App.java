package com.poupa.vinylmusicplayer;

import android.content.Context;
import android.content.res.Resources;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.multidex.MultiDexApplication;

import com.kabouzeid.appthemehelper.ThemeStore;
import com.poupa.vinylmusicplayer.appshortcuts.DynamicShortcutManager;
import com.poupa.vinylmusicplayer.discog.Discography;
import com.poupa.vinylmusicplayer.model.Album;
import com.poupa.vinylmusicplayer.model.Artist;
import com.poupa.vinylmusicplayer.model.Genre;
import com.poupa.vinylmusicplayer.model.Song;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public class App extends MultiDexApplication {
    public static final String TAG = App.class.getSimpleName();
    public static final boolean DYNAMICS_PROCESSING_AVAILABLE = Build.VERSION.SDK_INT >= Build.VERSION_CODES.P;

    private static App app;

    private static Context context;

    private static Discography discography = null;

    @Override
    public void onCreate() {
        super.onCreate();
        app = this;

        context = getApplicationContext();

        // default theme
        if (!ThemeStore.isConfigured(this, 1)) {
            ThemeStore.editTheme(this)
                    .primaryColorRes(R.color.md_indigo_500)
                    .accentColorRes(R.color.md_pink_A400)
                    .commit();
        }

        // Set up dynamic shortcuts
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            new DynamicShortcutManager(this).initDynamicShortcuts();
        }

        // setup discography
        discography = new Discography();

        final Resources resources = context.getResources();
        Artist.UNKNOWN_ARTIST_DISPLAY_NAME = resources.getString(R.string.unknown_artist);
        Album.UNKNOWN_ALBUM_DISPLAY_NAME = resources.getString(R.string.unknown_album);
        Genre.UNKNOWN_GENRE_DISPLAY_NAME = resources.getString(R.string.unknown_genre);
        Song.UNTITLED_DISPLAY_NAME =  resources.getString(R.string.untitled_song);
    }

    public static App getInstance() {
        return app;
    }

    public static Context getStaticContext() {
        return context;
    }

    @NonNull
    public static Discography getDiscography() {
        return discography;
    }
}
