package com.poupa.vinylmusicplayer.util;

import android.content.Context;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.MimeTypeMap;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.poupa.vinylmusicplayer.discog.Discography;
import com.poupa.vinylmusicplayer.model.Song;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Function;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public final class FileUtil {
    private FileUtil() {
    }

    @NonNull
    public static ArrayList<Song> matchFilesWithMediaStore(@NonNull List<File> files) {
        ArrayList<Song> songs = new ArrayList<>();
        for (File file : files) {
            String path = safeGetCanonicalPath(file);
            Song song = Discography.getInstance().getSongByPath(path);
            if (!song.equals(Song.EMPTY_SONG)) {
                songs.add(song);
            }
        }
        return songs;
    }

    @NonNull
    public static List<File> listFiles(@NonNull File directory, @Nullable FileFilter fileFilter) {
        List<File> fileList = new LinkedList<>();
        File[] found = directory.listFiles(fileFilter);
        if (found != null) {
            Collections.addAll(fileList, found);
        }
        return fileList;
    }

    @NonNull
    public static List<File> listFilesDeep(@NonNull File directory, @Nullable FileFilter fileFilter) {
        List<File> files = new LinkedList<>();
        internalListFilesDeep(files, directory, fileFilter);
        return files;
    }

    @NonNull
    public static List<File> listFilesDeep(@NonNull Collection<File> files, @Nullable FileFilter fileFilter) {
        List<File> resFiles = new LinkedList<>();
        for (File file : files) {
            if (file.isDirectory()) {
                internalListFilesDeep(resFiles, file, fileFilter);
            } else if (fileFilter == null || fileFilter.accept(file)) {
                resFiles.add(file);
            }
        }
        return resFiles;
    }

    private static void internalListFilesDeep(@NonNull Collection<File> files, @NonNull File directory, @Nullable FileFilter fileFilter) {
        File[] found = directory.listFiles(fileFilter);

        if (found != null) {
            for (File file : found) {
                if (file.isDirectory()) {
                    internalListFilesDeep(files, file, fileFilter);
                } else {
                    files.add(file);
                }
            }
        }
    }

    public static boolean fileIsMimeType(File file, String mimeType, MimeTypeMap mimeTypeMap) {
        if (mimeType == null || mimeType.equals("*/*")) {
            return true;
        } else {
            // get the file mime type
            // Dont use MimeTypeMap.getFileExtensionFromUrl since it fails to process URI with non-Latin characters
            Function<File, String> getFileExtension = (theFile) -> {
                final String uri = theFile.toURI().toString();
                int dotPos = uri.lastIndexOf('.');
                if (dotPos < 0) {return "";}
                return uri.substring(dotPos + 1).toLowerCase();
            };
            final String fileExtension = getFileExtension.apply(file);
            final String fileType = mimeTypeMap.getMimeTypeFromExtension(fileExtension);
            if (fileType == null) {
                return false;
            }
            // check the 'type/subtype' pattern
            if (fileType.equals(mimeType)) {
                return true;
            }
            // check the 'type/*' pattern
            int mimeTypeDelimiter = mimeType.lastIndexOf('/');
            if (mimeTypeDelimiter == -1) {
                return false;
            }
            String mimeTypeMainType = mimeType.substring(0, mimeTypeDelimiter);
            String mimeTypeSubtype = mimeType.substring(mimeTypeDelimiter + 1);
            if (!mimeTypeSubtype.equals("*")) {
                return false;
            }
            int fileTypeDelimiter = fileType.lastIndexOf('/');
            if (fileTypeDelimiter == -1) {
                return false;
            }
            String fileTypeMainType = fileType.substring(0, fileTypeDelimiter);
            return fileTypeMainType.equals(mimeTypeMainType);
        }
    }

    public static String stripExtension(String str) {
        if (str == null) return null;
        int pos = str.lastIndexOf('.');
        if (pos == -1) return str;
        return str.substring(0, pos);
    }

    public static String readFromStream(InputStream is) throws Exception {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            if (sb.length() > 0) sb.append("\n");
            sb.append(line);
        }
        reader.close();
        return sb.toString();
    }

    public static String read(File file) throws Exception {
        FileInputStream fin = new FileInputStream(file);
        String ret = readFromStream(fin);
        fin.close();
        return ret;
    }

    public static String safeGetCanonicalPath(File file) {
        try {
            return file.getCanonicalPath();
        } catch (IOException e) {
            e.printStackTrace();
            return file.getAbsolutePath();
        }
    }

    public static File safeGetCanonicalFile(File file) {
        try {
            return file.getCanonicalFile();
        } catch (IOException e) {
            e.printStackTrace();
            return file.getAbsoluteFile();
        }
    }

    public static byte[] readBytes(InputStream stream) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[4096];
        int count;
        while ((count = stream.read(buffer)) != -1) {
            baos.write(buffer, 0, count);
        }
        stream.close();
        return baos.toByteArray();
    }

    public static File getSDCardDirectory(Context context) {
        File sdFolder = null;
        for (File dir : context.getExternalFilesDirs(null)) {
            if(dir != null) {
                if (!dir.equals(context.getExternalFilesDir(null))) {
                    // first directory which is not primary storage - should be sd card
                    String path = dir.getAbsolutePath();
                    String base_path = path.substring(0, path.indexOf("Android/data"));
                    sdFolder = new File(base_path);
                    break;
                }
            }
        }
        return sdFolder;
    }

    /**
     * Workaround to find all possible storage devices' root paths.
     * s.a. '/sdcard/0', '/storage/emulated/0', or '/storage/SDCARD_NAME'.
     *
     * @return a non-null list of distinct Files, in no particular order.
     */
    public static List<File> getAllExternalStorageRootPaths(Context context){
        List<String> suggestedDrives = new ArrayList<>();

        // Primary SD card, not emulated
        suggestedDrives.add( System.getenv("EXTERNAL_STORAGE") );
        // Primary emulated SD card
        suggestedDrives.add( System.getenv("EMULATED_STORAGE_TARGET") );
        suggestedDrives.add( System.getenv("EXTERNAL_SDCARD_STORAGE") );
        // Emulated external storage
        suggestedDrives.add( Environment.getExternalStorageDirectory().getAbsolutePath() );

        if(context != null){
            suggestedDrives.add(FileUtil.getSDCardDirectory(context).getAbsolutePath());
        }

        // List of secondary SD cards, separated by ":".
        String externalStoragesStr = System.getenv("SECONDARY_STORAGE");
        if(!TextUtils.isEmpty(externalStoragesStr)){
            Collections.addAll(suggestedDrives, externalStoragesStr.split(":"));
        }

        for(int i = 0; i < 9; ++i){
            suggestedDrives.add("/storage/sdcard" + i);
            suggestedDrives.add("/storage/emulated/" + i);
        }

        Set<File> drives = new TreeSet<>();
        for(String drive : suggestedDrives){
            try{
                if( null == drive ) continue;
                File driveFile = new File(drive);
                if(driveFile.exists() && driveFile.isDirectory() && driveFile.canRead()){
                    drives.add(driveFile);
                }
            } catch( Exception ex ){
                Log.e(FileUtil.class.getSimpleName(), "Could not determine if the drive "+ drive + " is usable.", ex);
            }
        }
        return new ArrayList<>(drives);
    }
}
