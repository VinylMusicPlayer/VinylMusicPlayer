package com.poupa.vinylmusicplayer.util;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.UriPermission;
import android.net.Uri;
import android.os.Build;
import android.os.ParcelFileDescriptor;
import android.provider.DocumentsContract;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.documentfile.provider.DocumentFile;

import com.poupa.vinylmusicplayer.R;
import com.poupa.vinylmusicplayer.model.Song;

import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.exceptions.CannotWriteException;
import org.jaudiotagger.audio.generic.Utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiFunction;

public class SAFUtil {

    public static final String TAG = SAFUtil.class.getSimpleName();
    public static final String SEPARATOR = "###/SAF/###";

    public static boolean isSAFRequired(File file) {
        return !file.canWrite();
    }

    public static boolean isSAFRequired(String path) {
        return isSAFRequired(new File(path));
    }

    public static boolean isSAFRequired(AudioFile audio) {
        return isSAFRequired(audio.getFile());
    }

    public static boolean isSAFRequired(Song song) {
        return isSAFRequired(song.data);
    }

    public static boolean isSAFRequired(List<String> paths) {
        for (String path : paths) {
            if (isSAFRequired(path)) return true;
        }
        return false;
    }

    public static boolean isSAFRequiredForSongs(List<Song> songs) {
        for (Song song : songs) {
            if (isSAFRequired(song)) return true;
        }
        return false;
    }

    public static void saveTreeUri(Context context, Uri uri) {
        context.getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
        PreferenceUtil.getInstance().setSAFSDCardUri(uri);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static boolean isTreeUriSaved() {
        return !TextUtils.isEmpty(PreferenceUtil.getInstance().getSAFSDCardUri());
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static boolean isSDCardAccessGranted(Context context) {
        if (!isTreeUriSaved()) return false;

        String sdcardUri = PreferenceUtil.getInstance().getSAFSDCardUri();

        List<UriPermission> perms = context.getContentResolver().getPersistedUriPermissions();
        for (UriPermission perm : perms) {
            if (perm.getUri().toString().equals(sdcardUri) && perm.isWritePermission()) {
                return true;
            }
        }

        return false;
    }

    @TargetApi(Build.VERSION_CODES.TIRAMISU)
    public static boolean isStorageAccessGranted(Context context, @NonNull final String folder) {
        BiFunction<String, String, Boolean> areEquivalentFolders = (uriPermissionPath, path) -> {
            String normalizedPath = uriPermissionPath
                    .replaceFirst("^/tree/primary:", "/storage/emulated/0/")
                    .replaceFirst("/tree/([^/]*):", "/storage/$1/");
            return normalizedPath.equals(path);
        };

        List<UriPermission> perms = context.getContentResolver().getPersistedUriPermissions();
        for (UriPermission perm : perms) {
            final String path = perm.getUri().getPath();
            if (areEquivalentFolders.apply(path, folder) && perm.isWritePermission()) {
                return true;
            }
        }

        return false;
    }
    /**
     * https://github.com/vanilla-music/vanilla-music-tag-editor/commit/e00e87fef289f463b6682674aa54be834179ccf0#diff-d436417358d5dfbb06846746d43c47a5R359
     * Finds needed file through Document API for SAF. It's not optimized yet - you can still gain wrong URI on
     * files such as "/a/b/c.mp3" and "/b/a/c.mp3", but I consider it complete enough to be usable.
     *
     * @param dir      - document file representing current dir of search
     * @param segments - path segments that are left to find
     * @return URI for found file. Null if nothing found.
     */
    @Nullable
    public static Uri findDocument(@Nullable DocumentFile dir, @NonNull List<String> segments) {
        if (dir == null) {return null;}

        for (DocumentFile file : dir.listFiles()) {
            int index = segments.indexOf(file.getName());
            if (index == -1) {
                continue;
            }

            if (file.isDirectory()) {
                segments.remove(file.getName());
                return findDocument(file, segments);
            }

            if (file.isFile() && index == segments.size() - 1) {
                // got to the last part
                return file.getUri();
            }
        }

        return null;
    }

    public static void write(Context context, AudioFile audio, Uri safUri) {
        if (isSAFRequired(audio)) {
            writeSAF(context, audio, safUri);
        } else {
            try {
                writeFile(audio);
            } catch (CannotWriteException e) {
                e.printStackTrace();
            }
        }
    }

    public static void writeFile(AudioFile audio) throws CannotWriteException {
        audio.commit();
    }

    public static void writeSAF(Context context, AudioFile audio, Uri safUri) {
        Uri uri = null;

        if (context == null) {
            Log.e(TAG, "writeSAF: context == null");
            return;
        }

        if (isTreeUriSaved()) {
            List<String> pathSegments = new ArrayList<>(Arrays.asList(audio.getFile().getAbsolutePath().split("/")));
            Uri sdcard = Uri.parse(PreferenceUtil.getInstance().getSAFSDCardUri());
            uri = findDocument(DocumentFile.fromTreeUri(context, sdcard), pathSegments);
        }

        if (uri == null) {
            uri = safUri;
        }

        if (uri == null) {
            Log.e(TAG, "writeSAF: Can't get SAF URI");
            toast(context, context.getString(R.string.saf_error_uri));
            return;
        }

        try (ParcelFileDescriptor pfd = context.getContentResolver().openFileDescriptor(uri, "rw")) {
            if (pfd == null) {
                Log.e(TAG, "writeSAF: SAF provided incorrect URI: " + uri);
                return;
            }

            // copy file to app folder to use jaudiotagger
            final File original = audio.getFile();
            File temp = File.createTempFile("tmp-media", '.' + Utils.getExtension(original));
            Utils.copy(original, temp);
            temp.deleteOnExit();
            audio.setFile(temp);
            writeFile(audio);

            // now read persisted data and write it to real FD provided by SAF
            FileInputStream fis = new FileInputStream(temp);
            byte[] audioContent = FileUtil.readBytes(fis);
            FileOutputStream fos = new FileOutputStream(pfd.getFileDescriptor());
            fos.write(audioContent);
            fos.close();

            temp.delete();
        } catch (final Exception e) {
            Log.e(TAG, "writeSAF: Failed to write to file descriptor provided by SAF", e);

            toast(context, String.format(context.getString(R.string.saf_write_failed), e.getLocalizedMessage()));
        }
    }

    public static void delete(Context context, String path, Uri safUri) {
        if (isSAFRequired(path)) {
            deleteSAF(context, path, safUri);
        } else {
            try {
                deleteFile(path);
            } catch (NullPointerException e) {
                Log.e("MusicUtils", "Failed to find file " + path);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void deleteFile(String path) {
        new File(path).delete();
    }

    public static void deleteSAF(Context context, String path, Uri safUri) {
        Uri uri = null;

        if (context == null) {
            Log.e(TAG, "deleteSAF: context == null");
            return;
        }

        if (isTreeUriSaved()) {
            List<String> pathSegments = new ArrayList<>(Arrays.asList(path.split("/")));
            Uri sdcard = Uri.parse(PreferenceUtil.getInstance().getSAFSDCardUri());
            uri = findDocument(DocumentFile.fromTreeUri(context, sdcard), pathSegments);
        }

        if (uri == null) {
            uri = safUri;
        }

        if (uri == null) {
            Log.e(TAG, "deleteSAF: Can't get SAF URI");
            toast(context, context.getString(R.string.saf_error_uri));
            return;
        }

        try {
            DocumentsContract.deleteDocument(context.getContentResolver(), uri);
        } catch (final Exception e) {
            Log.e(TAG, "deleteSAF: Failed to delete a file descriptor provided by SAF", e);

            toast(context, String.format(context.getString(R.string.saf_delete_failed), e.getLocalizedMessage()));
        }
    }

    private static void toast(final Context context, final String message) {
        if (context instanceof Activity) {
            ((Activity) context).runOnUiThread(() -> Toast.makeText(context, message, Toast.LENGTH_SHORT).show());
        }
    }

}
