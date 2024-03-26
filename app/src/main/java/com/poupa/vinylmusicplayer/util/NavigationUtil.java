package com.poupa.vinylmusicplayer.util;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.media.audiofx.AudioEffect;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.util.Pair;

import com.afollestad.materialdialogs.MaterialDialog;
import com.poupa.vinylmusicplayer.R;
import com.poupa.vinylmusicplayer.discog.Discography;
import com.poupa.vinylmusicplayer.helper.MusicPlayerRemote;
import com.poupa.vinylmusicplayer.model.Artist;
import com.poupa.vinylmusicplayer.model.Genre;
import com.poupa.vinylmusicplayer.model.Playlist;
import com.poupa.vinylmusicplayer.ui.activities.AlbumDetailActivity;
import com.poupa.vinylmusicplayer.ui.activities.ArtistDetailActivity;
import com.poupa.vinylmusicplayer.ui.activities.GenreDetailActivity;
import com.poupa.vinylmusicplayer.ui.activities.PlaylistDetailActivity;

import java.util.List;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public class NavigationUtil {

    @SafeVarargs
    public static void goToArtist(@NonNull final Activity activity, @NonNull final List<String> artistNames, @Nullable final Pair<View, String>... sharedElements) {
        if (artistNames.isEmpty()) {return;}
        if (artistNames.size() == 1) {
            goToArtist(activity, artistNames.get(0), sharedElements);
        } else {
            // Popup to select one name to navigate to
            new MaterialDialog.Builder(activity)
                    .title(R.string.action_go_to_artist)
                    .items(artistNames)
                    .itemsCallback((dialog, view, which, text) -> goToArtist(activity, text.toString(), sharedElements))
                    .show();
        }
    }

    @SafeVarargs
    private static void goToArtist(@NonNull final Activity activity, @NonNull final String artistName, @Nullable final Pair<View, String>... sharedElements) {
        final Artist artist = Discography.getInstance().getArtistByName(artistName);
        if (artist != null) {
            goToArtist(activity, artist.id, sharedElements);
        }
    }

    @SafeVarargs
    public static void goToArtist(@NonNull final Activity activity, final long artistId, @Nullable final Pair<View, String>... sharedElements) {
        final Intent intent = new Intent(activity, ArtistDetailActivity.class);
        intent.putExtra(ArtistDetailActivity.EXTRA_ARTIST_ID, artistId);

        if (sharedElements != null && sharedElements.length > 0) {
            activity.startActivity(intent, ActivityOptionsCompat.makeSceneTransitionAnimation(activity, sharedElements).toBundle());
        } else {
            activity.startActivity(intent);
        }
    }

    @SafeVarargs
    public static void goToAlbum(@NonNull final Activity activity, final long albumId, @Nullable final Pair<View, String>... sharedElements) {
        final Intent intent = new Intent(activity, AlbumDetailActivity.class);
        intent.putExtra(AlbumDetailActivity.EXTRA_ALBUM_ID, albumId);

        if (sharedElements != null && sharedElements.length > 0) {
            activity.startActivity(intent, ActivityOptionsCompat.makeSceneTransitionAnimation(activity, sharedElements).toBundle());
        } else {
            activity.startActivity(intent);
        }
    }

    public static void goToGenre(@NonNull final Activity activity, final Genre genre) {
        final Intent intent = new Intent(activity, GenreDetailActivity.class);
        intent.putExtra(GenreDetailActivity.EXTRA_GENRE, genre);

        activity.startActivity(intent);
    }

    public static void goToPlaylist(@NonNull final Activity activity, final Playlist playlist) {
        final Intent intent = new Intent(activity, PlaylistDetailActivity.class);
        intent.putExtra(PlaylistDetailActivity.EXTRA_PLAYLIST, playlist);

        activity.startActivity(intent);
    }

    public static void openEqualizer(@NonNull final Activity activity) {
        final int sessionId = MusicPlayerRemote.getAudioSessionId();
        if (sessionId == AudioEffect.ERROR_BAD_VALUE) {
            SafeToast.show(activity, activity.getResources().getString(R.string.no_audio_ID));
        } else {
            try {
                final Intent effects = new Intent(AudioEffect.ACTION_DISPLAY_AUDIO_EFFECT_CONTROL_PANEL);
                effects.putExtra(AudioEffect.EXTRA_AUDIO_SESSION, sessionId);
                effects.putExtra(AudioEffect.EXTRA_CONTENT_TYPE, AudioEffect.CONTENT_TYPE_MUSIC);
                activity.startActivityForResult(effects, 0);
            } catch (@NonNull final ActivityNotFoundException notFound) {
                SafeToast.show(activity, activity.getResources().getString(R.string.no_equalizer));
            }
        }
    }
}
