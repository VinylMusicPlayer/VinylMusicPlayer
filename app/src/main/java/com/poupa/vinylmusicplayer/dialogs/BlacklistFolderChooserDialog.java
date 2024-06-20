package com.poupa.vinylmusicplayer.dialogs;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;

import com.poupa.vinylmusicplayer.R;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * @author Aidan Follestad (afollestad), modified by Karim Abou Zeid
 */
public class BlacklistFolderChooserDialog extends DialogFragment implements DialogInterface.OnClickListener {

    private File parentFolder;
    private File[] parentContents;
    private String[] parentContentsAsString;
    private boolean canGoUp = false;

    private FolderCallback callback;

    final String initialPath = Environment.getExternalStorageDirectory().getAbsolutePath();

    private final String KEY = "current_path";

    private String[] getContentsArray() {
        if (parentContents == null) {
            if (canGoUp) {
                return new String[]{".."};
            }
            return new String[]{};
        }
        String[] results = new String[parentContents.length + (canGoUp ? 1 : 0)];
        if (canGoUp) {
            results[0] = "..";
        }
        for (int i = 0; i < parentContents.length; i++) {
            results[canGoUp ? i + 1 : i] = parentContents[i].getName();
        }
        return results;
    }

    private File[] listFiles() {
        File[] contents = parentFolder.listFiles();
        List<File> results = new ArrayList<>();
        if (contents != null) {
            for (File fi : contents) {
                if (fi.isDirectory()) {
                    results.add(fi);
                }
            }
            Collections.sort(results, new FolderSorter());
            return results.toArray(new File[0]);
        }
        return null;
    }

    public static BlacklistFolderChooserDialog create() {
        return new BlacklistFolderChooserDialog();
    }

    @NonNull
    @Override
    public AlertDialog onCreateDialog(Bundle savedInstanceState) {
        final Activity activity = requireActivity();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_MEDIA_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                return new AlertDialog.Builder(activity)
                        .setTitle(R.string.md_error_label)
                        .setMessage(R.string.android13_storage_perm_error)
                        .setPositiveButton(android.R.string.ok, (dialog, which) -> dialog.dismiss())
                        .create();
            }
        } else if ((Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                && (ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED))
        {
                return new AlertDialog.Builder(activity)
                        .setTitle(R.string.md_error_label)
                        .setMessage(R.string.md_storage_perm_error)
                        .setPositiveButton(android.R.string.ok, ((dialog, which) -> dialog.dismiss()))
                        .create();
        }
        if (savedInstanceState == null) {
            savedInstanceState = new Bundle();
        }
        if (!savedInstanceState.containsKey(KEY)) {
            savedInstanceState.putString(KEY, initialPath);
        }
        parentFolder = new File(savedInstanceState.getString(KEY, "/"));
        checkIfCanGoUp();
        parentContents = listFiles();
        parentContentsAsString = getContentsArray();
        return new AlertDialog.Builder(activity)
                .setTitle(parentFolder.getAbsolutePath())
                .setItems(parentContentsAsString, this)
                .setCancelable(false)
                .setPositiveButton(R.string.add_action, (dialog, which) -> {
                    dismiss();
                    callback.onFolderSelection(this, parentFolder);
                })
                .setNegativeButton(android.R.string.cancel, (materialDialog, dialogAction) -> dismiss())
                .create();
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        if (canGoUp && which == 0) {
            parentFolder = parentFolder.getParentFile();
            if (parentFolder.getAbsolutePath().equals("/storage/emulated")) {
                parentFolder = parentFolder.getParentFile();
            }
            canGoUp = parentFolder.getParent() != null;
        } else {
            parentFolder = parentContents[canGoUp ? which - 1 : which];
            canGoUp = true;
            if (parentFolder.getAbsolutePath().equals("/storage/emulated")) {
                parentFolder = Environment.getExternalStorageDirectory();
            }
        }
        reload();
    }

    private void checkIfCanGoUp() {
        try {
            canGoUp = parentFolder.getPath().split("/").length > 1;
        } catch (IndexOutOfBoundsException e) {
            canGoUp = false;
        }
    }

    private void reload() {
        parentContents = listFiles();
        AlertDialog dialog = (AlertDialog) getDialog();
        dialog.setTitle(parentFolder.getAbsolutePath());
        ((ArrayAdapter)dialog.getListView().getAdapter()).notifyDataSetChanged();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(KEY, parentFolder.getAbsolutePath());
    }

    public void setCallback(FolderCallback callback) {
        this.callback = callback;
    }

    public interface FolderCallback {
        void onFolderSelection(@NonNull BlacklistFolderChooserDialog dialog, @NonNull File folder);
    }

    static class FolderSorter implements Comparator<File> {

        @Override
        public int compare(File lhs, File rhs) {
            return lhs.getName().compareTo(rhs.getName());
        }
    }
}
