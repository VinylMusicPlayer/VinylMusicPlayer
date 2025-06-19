package com.poupa.vinylmusicplayer.dialogs;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;

import com.afollestad.materialdialogs.MaterialDialog;
import com.poupa.vinylmusicplayer.R;
import com.poupa.vinylmusicplayer.util.FileUtil;

import java.io.File;
import java.util.Comparator;
import java.util.Set;
import java.util.TreeSet;

/**
 * @author Aidan Follestad (afollestad), modified by Karim Abou Zeid
 */
public class BlacklistFolderChooserDialog extends DialogFragment implements MaterialDialog.ListCallback {

    private File parentFolder;
    private File[] parentContents;
    private boolean canGoUp = false;

    private FolderCallback callback;

    public static final String INITIAL_PATH = Environment.getExternalStorageDirectory().getAbsolutePath();

    private final String KEY = "current_path";

    private boolean isAtRootLevel(){
        return  "/".equals(parentFolder.getAbsolutePath());
    }

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
            results[canGoUp ? i + 1 : i] = isAtRootLevel() ? parentContents[i].getAbsolutePath() : parentContents[i].getName();
        }
        return results;
    }

    private File[] listFiles() {
        File[] contents = parentFolder.listFiles();
        Set<File> results = new TreeSet<>();

        if( isAtRootLevel() ){
            results.addAll(FileUtil.getAllExternalStorageRootPaths(this.getContext()));
        }

        if (contents != null) {
            for (File fi : contents) {
                if (fi.isDirectory()) {
                    results.add(fi);
                }
            }
        }
        return results.stream().sorted(new FolderSorter()).toArray(File[]::new);
    }

    public static BlacklistFolderChooserDialog create() {
        return new BlacklistFolderChooserDialog();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final Activity activity = requireActivity();
        if (Build.VERSION.SDK_INT >= VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_MEDIA_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                return new MaterialDialog.Builder(activity)
                        .title(R.string.md_error_label)
                        .content(R.string.android13_storage_perm_error)
                        .positiveText(android.R.string.ok)
                        .build();
            }
        } else if ((Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                && (ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED))
        {
                return new MaterialDialog.Builder(activity)
                        .title(R.string.md_error_label)
                        .content(R.string.md_storage_perm_error)
                        .positiveText(android.R.string.ok)
                        .build();
        }
        if (savedInstanceState == null) {
            savedInstanceState = new Bundle();
        }
        if (!savedInstanceState.containsKey(KEY)) {
            savedInstanceState.putString(KEY, INITIAL_PATH);
        }
        parentFolder = new File(savedInstanceState.getString(KEY, "/"));
        checkIfCanGoUp();
        parentContents = listFiles();
        MaterialDialog.Builder builder =
                new MaterialDialog.Builder(activity)
                        .title(parentFolder.getAbsolutePath())
                        .items(getContentsArray())
                        .itemsCallback(this)
                        .autoDismiss(false)
                        .onPositive((dialog, which) -> {
                            dismiss();
                            callback.onFolderSelection(BlacklistFolderChooserDialog.this, parentFolder);
                        })
                        .onNegative((materialDialog, dialogAction) -> dismiss())
                        .positiveText(R.string.add_action)
                        .negativeText(android.R.string.cancel);
        return builder.build();
    }

    @Override
    public void onSelection(MaterialDialog materialDialog, View view, int i, CharSequence s) {
        if (canGoUp && i == 0) {
            parentFolder = parentFolder.getParentFile();
            canGoUp = parentFolder.getParent() != null;
        } else {
            parentFolder = parentContents[canGoUp ? i - 1 : i];
            canGoUp = true;
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
        MaterialDialog dialog = (MaterialDialog) getDialog();
        dialog.setTitle(parentFolder.getAbsolutePath());
        dialog.setItems(getContentsArray());
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
