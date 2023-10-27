package com.poupa.vinylmusicplayer.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.widget.Toast;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.transition.Transition;
import com.poupa.vinylmusicplayer.App;
import com.poupa.vinylmusicplayer.glide.GlideApp;
import com.poupa.vinylmusicplayer.glide.VinylSimpleTarget;
import com.poupa.vinylmusicplayer.model.Artist;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Locale;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */

public class CustomArtistImageUtil {
    private static final String CUSTOM_ARTIST_IMAGE_PREFS = "custom_artist_image";
    private static final String FOLDER_NAME = "/custom_artist_images/";

    private static CustomArtistImageUtil sInstance;

    private final SharedPreferences mPreferences;

    private CustomArtistImageUtil(@NonNull final Context context) {
        mPreferences = context.getSharedPreferences(CUSTOM_ARTIST_IMAGE_PREFS, Context.MODE_PRIVATE);
    }

    public static CustomArtistImageUtil getInstance(@NonNull final Context context) {
        if (sInstance == null) {
            sInstance = new CustomArtistImageUtil(context.getApplicationContext());
        }
        return sInstance;
    }

    public void setCustomArtistImage(final Artist artist, Uri uri, @Nullable Runnable postExec) {
        GlideApp.with(App.getInstance())
                .asBitmap()
                .load(uri)
                .apply(new RequestOptions()
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .skipMemoryCache(true)
                )
                .into(new VinylSimpleTarget<Bitmap>() {
                    @SuppressLint("StaticFieldLeak")
                    @Override
                    public void onResourceReady(@NonNull final Bitmap resource, Transition<? super Bitmap> glideAnimation) {
                        new AsyncTask<Void, Void, Void>() {
                            @SuppressLint("ApplySharedPref")
                            @Override
                            protected Void doInBackground(Void... params) {
                                File dir = new File(App.getInstance().getFilesDir(), FOLDER_NAME);
                                if (!dir.exists()) {
                                    if (!dir.mkdirs()) { // create the folder
                                        return null;
                                    }
                                }

                                boolean succesful = false;
                                try {
                                    File file = new File(dir, getFileName(artist));
                                    OutputStream os = new BufferedOutputStream(new FileOutputStream(file));
                                    succesful = ImageUtil
                                            .resizeBitmap(resource, 2048)
                                            .compress(Bitmap.CompressFormat.JPEG, 100, os);
                                    os.close();
                                } catch (IOException e) {
                                    Toast.makeText(App.getInstance(), e.toString(), Toast.LENGTH_LONG).show();
                                }

                                if (succesful) {
                                    mPreferences.edit().putBoolean(getFileName(artist), true).apply();
                                    ArtistSignatureUtil.getInstance().updateArtistSignature(artist.getName());
                                    // trigger media store changed to force artist image reload
                                    App.getInstance().getContentResolver().notifyChange(Uri.parse("content://media"), null);
                                }
                                return null;
                            }

                            @MainThread
                            protected void onPostExecute(Void result) {
                                if (postExec != null) {postExec.run();}
                            }
                        }.execute();
                    }
                });
    }

    @SuppressLint("StaticFieldLeak")
    public void resetCustomArtistImage(@NonNull final Artist artist, @Nullable Runnable postExec) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                mPreferences.edit().putBoolean(getFileName(artist), false).apply();
                ArtistSignatureUtil.getInstance().updateArtistSignature(artist.getName());
                // trigger media store changed to force artist image reload
                App.getInstance().getContentResolver().notifyChange(Uri.parse("content://media"), null);

                File file = getFile(artist);
                if (file.exists()) {
                    file.delete();
                }
                return null;
            }

            @MainThread
            protected void onPostExecute(Void result) {
                if (postExec != null) {postExec.run();}
            }
        }.execute();
    }

    // shared prefs saves us many IO operations
    public boolean hasCustomArtistImage(Artist artist) {
        return mPreferences.getBoolean(getFileName(artist), false);
    }

    private static String getFileName(Artist artist) {
        String artistName = artist.getName();
        if (artistName == null)
            artistName = "";
        // replace everything that is not a letter or a number with _
        artistName = artistName.replaceAll("[^a-zA-Z0-9]", "_");
        return String.format(Locale.US, "#%d#%s.jpeg", artist.getId(), artistName);
    }

    @NonNull
    public static File getFile(Artist artist) {
        File dir = new File(App.getInstance().getFilesDir(), FOLDER_NAME);
        return new File(dir, getFileName(artist));
    }
}
