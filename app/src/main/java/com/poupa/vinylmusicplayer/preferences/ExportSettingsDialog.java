package com.poupa.vinylmusicplayer.preferences;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.InputType;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.afollestad.materialdialogs.MaterialDialog;
import com.poupa.vinylmusicplayer.R;
import com.poupa.vinylmusicplayer.model.Song;
import com.poupa.vinylmusicplayer.util.PlaylistsUtil;
import com.poupa.vinylmusicplayer.util.SafeToast;
/**
 * @author Andreas Lechner ()
 */
public class ExportSettingsDialog extends DialogFragment {

    @NonNull
    public static ExportSettingsDialog newInstance(@NonNull String preference) {
        return new ExportSettingsDialog(preference);
    }

    @NonNull private final String preferenceKey;

    public ExportSettingsDialog(@NonNull String preference) {
        preferenceKey = preference;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final Activity activity = requireActivity();
        return new MaterialDialog.Builder(activity)
                .title(R.string.export_settings)
                .positiveText(android.R.string.ok)
                .negativeText(android.R.string.cancel)
                .inputType(InputType.TYPE_MASK_CLASS |
                        InputType.TYPE_TEXT_VARIATION_PERSON_NAME |
                        InputType.TYPE_TEXT_FLAG_CAP_WORDS)
                .input(R.string.playlist_name_empty, 0, false, (materialDialog, charSequence) -> {
                    final String name = charSequence.toString().trim();
                    if (!name.isEmpty()) {
                        if (!PlaylistsUtil.doesPlaylistExist(name)) {
                            final long playlistId = PlaylistsUtil.createPlaylist(activity, name);
                        } else {
                            SafeToast.show(
                                    activity,
                                    getActivity().getResources().getString(R.string.playlist_exists, name)
                            );
                        }
                    }
                })
                .build();
    }



}
