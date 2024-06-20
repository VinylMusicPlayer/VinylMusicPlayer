package com.poupa.vinylmusicplayer.dialogs;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.Html;
import android.text.Spanned;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.fragment.app.DialogFragment;

import com.poupa.vinylmusicplayer.R;
import com.poupa.vinylmusicplayer.discog.tagging.MultiValuesTagUtil;
import com.poupa.vinylmusicplayer.model.Album;
import com.poupa.vinylmusicplayer.model.Song;
import com.poupa.vinylmusicplayer.util.AutoCloseAudioFile;
import com.poupa.vinylmusicplayer.util.MusicUtil;
import com.poupa.vinylmusicplayer.util.SAFUtil;

import org.jaudiotagger.audio.AudioHeader;

import java.io.File;
import java.util.Date;
import java.util.Locale;
import java.util.function.Function;

/**
 * @author Karim Abou Zeid (kabouzeid), Aidan Follestad (afollestad)
 */
public class SongDetailDialog extends DialogFragment {

    public static final String TAG = SongDetailDialog.class.getSimpleName();

    @NonNull
    public static SongDetailDialog create(@NonNull Song song) {
        SongDetailDialog dialog = new SongDetailDialog();
        Bundle args = new Bundle();
        args.putParcelable("song", song);
        dialog.setArguments(args);
        return dialog;
    }

    // Utility class, to collect and build rich text
    static class HtmlBuilder {
        private final Context context;
        private final StringBuilder stringBuilder = new StringBuilder();

        HtmlBuilder(@NonNull Context context) {
            this.context = context;
        }

        HtmlBuilder append(@NonNull final String text) {
            stringBuilder.append(text);
            return this;
        }

        HtmlBuilder append(@StringRes int stringId) {
            stringBuilder.append(context.getString(stringId));
            return this;
        }

        HtmlBuilder appendLine(@StringRes int labelStringId, @NonNull final String... texts) {
            append(labelStringId).append(": ").append("<b>");
            for (String text : texts) {
                append(text);
            }
            return append("</b>").append("<br/>");
        }

        Spanned build() {
            final String savedText = stringBuilder.toString();
            stringBuilder.setLength(0); // reset for next use
            return Html.fromHtml(savedText);
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final @NonNull Activity context = requireActivity();
        final @NonNull Song song = requireArguments().getParcelable("song");

        final View customView = getLayoutInflater().inflate(R.layout.dialog_file_details, null);
        AlertDialog dialog = new AlertDialog.Builder(context)
                .setView(customView)
                .setTitle(context.getResources().getString(R.string.label_details))
                .setPositiveButton(android.R.string.ok, ((dialog1, which) -> dialog1.dismiss()))
                .create();

        final TextView filesystemInfo = customView.findViewById(R.id.filesystem_info);
        final TextView discographyInfo = customView.findViewById(R.id.discography_info);

        HtmlBuilder htmlBuilder = new HtmlBuilder(context);

        // ---- Information from the filesystem
        final File songFile = new File(song.data);
        if (songFile.exists()) {
            htmlBuilder.appendLine(R.string.label_file_path, songFile.getAbsolutePath())
                    .appendLine(R.string.label_file_size,
                            String.format(Locale.getDefault(), "%.2f MB", 1.0 * songFile.length() / 1024 / 1024));
        } else {
            htmlBuilder.appendLine(R.string.label_file_path, "-");
        }

        try (AutoCloseAudioFile audioFile = SAFUtil.loadReadOnlyAudioFile(context, song)) {
            AudioHeader audioHeader = audioFile.get().getAudioHeader();

            htmlBuilder.appendLine(R.string.label_file_format, audioHeader.getFormat())
                    .appendLine(R.string.label_bit_rate, audioHeader.getBitRate(), " kb/s")
                    .appendLine(R.string.label_sampling_rate, audioHeader.getSampleRate(), " Hz");
        } catch (@NonNull Exception | NoSuchMethodError | VerifyError e) {
            htmlBuilder.appendLine(R.string.label_file_format, "-")
                    .appendLine(R.string.label_bit_rate, "- kb/s")
                    .appendLine(R.string.label_sampling_rate, "- Hz");
        }

        filesystemInfo.setText(htmlBuilder.build());

        // ---- Information from the mediastore / discography
        Function<Long, String> formatDate = seconds -> {
            final Date date = new Date(1000 * seconds);
            return date.toString();
        };
        htmlBuilder.appendLine(R.string.label_date_added, formatDate.apply(song.dateAdded));
        htmlBuilder.appendLine(R.string.label_date_modified, formatDate.apply(song.dateModified));
        htmlBuilder.appendLine(R.string.track_number, String.valueOf(song.trackNumber));
        htmlBuilder.appendLine(R.string.disc_number, String.valueOf(song.discNumber));
        htmlBuilder.appendLine(R.string.title, song.getTitle());
        htmlBuilder.appendLine(R.string.artist, MultiValuesTagUtil.infoStringAsArtists(song.artistNames));
        htmlBuilder.appendLine(R.string.album, Album.getTitle(song.albumName));
        htmlBuilder.appendLine(R.string.album_artist, MultiValuesTagUtil.infoStringAsArtists(song.albumArtistNames));
        htmlBuilder.appendLine(R.string.genre, MultiValuesTagUtil.infoStringAsGenres(song.genres));
        htmlBuilder.appendLine(R.string.year, MusicUtil.getYearString(song.year));

        htmlBuilder.appendLine(R.string.label_track_length, MusicUtil.getReadableDurationString(song.duration));

        final String rgTrack = song.replayGainTrack != 0.0f
                ? String.format(Locale.getDefault(), "%s: %.2f dB ", context.getString(R.string.song), song.replayGainTrack)
                : "- ";
        final String rgAlbum = song.replayGainAlbum != 0.0f
                ? String.format(Locale.getDefault(), "%s: %.2f dB ", context.getString(R.string.album), song.replayGainAlbum)
                : "- ";
        htmlBuilder.appendLine(R.string.label_replay_gain, rgTrack, rgAlbum);

        final String rgPeakTrack = song.replayGainPeakTrack != 1.0f
                ? String.format(Locale.getDefault(), "%s: %.2f ", context.getString(R.string.song), song.replayGainPeakTrack)
                : "- ";
        final String rgPeakAlbum = song.replayGainPeakAlbum != 1.0f
                ? String.format(Locale.getDefault(), "%s: %.2f ", context.getString(R.string.album), song.replayGainPeakAlbum)
                : "- ";
        htmlBuilder.appendLine(R.string.label_replay_gain_peak, rgPeakTrack, rgPeakAlbum);
        discographyInfo.setText(htmlBuilder.build());

        return dialog;
    }
}
