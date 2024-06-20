package com.poupa.vinylmusicplayer.preferences;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Html;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import com.poupa.vinylmusicplayer.R;
import com.poupa.vinylmusicplayer.dialogs.BlacklistFolderChooserDialog;
import com.poupa.vinylmusicplayer.provider.BlacklistStore;

import java.io.File;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public class BlacklistPreferenceDialog extends DialogFragment implements BlacklistFolderChooserDialog.FolderCallback {

    private String[] paths;
    private DialogInterface.OnClickListener onPathSelected;

    public static BlacklistPreferenceDialog newInstance() {
        return new BlacklistPreferenceDialog();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        BlacklistFolderChooserDialog blacklistFolderChooserDialog = (BlacklistFolderChooserDialog) getChildFragmentManager().findFragmentByTag("FOLDER_CHOOSER");
        if (blacklistFolderChooserDialog != null) {
            blacklistFolderChooserDialog.setCallback(this);
        }

        onPathSelected = (outterDialog, outterWhich) -> {
            outterDialog.dismiss();
            final String outterText = paths[outterWhich];

            new AlertDialog.Builder(getContext())
                    .setTitle(R.string.remove_from_blacklist)
                    .setMessage(Html.fromHtml(getString(R.string.do_you_want_to_remove_from_the_blacklist, outterText)))
                    .setPositiveButton(R.string.remove_action, (innerDialog, innerWhich) -> {
                        innerDialog.dismiss();
                        BlacklistStore.getInstance(requireContext()).removePath(new File(outterText));
                        refreshBlacklistData();
                    })
                    .setNegativeButton(android.R.string.cancel, (innerDialog, innerWhich) -> innerDialog.dismiss())
                    .show();
        };

        refreshBlacklistData();
        return new AlertDialog.Builder(getContext())
                .setTitle(R.string.blacklist)
                .setPositiveButton(android.R.string.ok, (materialDialog, dialogAction) -> dismiss())
                .setNeutralButton(R.string.clear_action, (materialDialog, dialogAction) -> new AlertDialog.Builder(getContext())
                        .setTitle(R.string.clear_blacklist)
                        .setMessage(R.string.do_you_want_to_clear_the_blacklist)
                        .setPositiveButton(R.string.clear_action, (dialog, which) -> {
                            BlacklistStore.getInstance(requireContext()).clear();
                            refreshBlacklistData();
                        })
                        .setNegativeButton(android.R.string.cancel, ((dialog, which) -> dialog.dismiss()))
                        .show())
                .setNegativeButton(R.string.add_action, (materialDialog, dialogAction) -> {
                    BlacklistFolderChooserDialog dialog = BlacklistFolderChooserDialog.create();
                    dialog.setCallback(this);
                    dialog.show(getChildFragmentManager(), "FOLDER_CHOOSER");
                })
                .setItems(paths, onPathSelected)
                .create();
    }

    private void refreshBlacklistData() {
        paths = BlacklistStore.getInstance(requireContext()).getPaths().toArray(new String[0]);

        final AlertDialog dialog = (AlertDialog) getDialog();
        if (dialog != null) {
            ((ArrayAdapter)dialog.getListView().getAdapter()).notifyDataSetChanged();
        }
    }

    @Override
    public void onFolderSelection(@NonNull BlacklistFolderChooserDialog folderChooserDialog, @NonNull File file) {
        BlacklistStore.getInstance(requireContext()).addPath(file);
        refreshBlacklistData();
    }
}
