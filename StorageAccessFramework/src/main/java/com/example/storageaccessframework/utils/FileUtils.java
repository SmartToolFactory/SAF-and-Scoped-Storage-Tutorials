package com.example.storageaccessframework.fileutils;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Log;
import androidx.documentfile.provider.DocumentFile;
import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

public class FileUtils {
    private static final String LOG_TAG = FileUtils.class.getName();

    /**
     * Get absolute paths of memory  and SD cards
     *
     * @param context Required for getting external storage dirs
     * @return returns external storage paths (directory of external memory card)
     * as array of Strings
     */
    public static String[] getExternalStorageDirectories(Context context) {

        List<String> results = new ArrayList<>();

        //Method 1 for KitKat & above
        File[] externalDirs = context.getExternalFilesDirs(null);

        for (File file : externalDirs) {
            String path = file.getPath().split("/Android")[0];

            boolean addPath = Environment.isExternalStorageRemovable(file);


            if (addPath) {
                results.add(path);
            }
        }

        if (results.isEmpty()) { //Method 2 for all versions
            // better variation of: http://stackoverflow.com/a/40123073/5002496
            String output = "";
            try {
                final Process process = new ProcessBuilder().command("mount | grep /dev/block/vold")
                        .redirectErrorStream(true).start();
                process.waitFor();
                final InputStream is = process.getInputStream();
                final byte[] buffer = new byte[1024];
                while (is.read(buffer) != -1) {
                    output = output + new String(buffer);
                }
                is.close();
            } catch (final Exception e) {
                e.printStackTrace();
            }
            if (!output.trim().isEmpty()) {
                String[] devicePoints = output.split("\n");
                for (String voldPoint : devicePoints) {
                    results.add(voldPoint.split(" ")[2]);
                }
            }
        }

        //Below few lines is to remove paths which may not be external memory card,
        // like OTG (feel free to comment them out)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            for (int i = 0; i < results.size(); i++) {
                if (!results.get(i).toLowerCase().matches(".*[0-9a-f]{4}[-][0-9a-f]{4}")) {
                    Log.d(LOG_TAG, results.get(i) + " might not be extSDcard");
                    results.remove(i--);
                }
            }
        } else {
            for (int i = 0; i < results.size(); i++) {
                if (!results.get(i).toLowerCase().contains("ext") &&
                        !results.get(i).toLowerCase().contains("sdcard")
                ) {
                    Log.d(LOG_TAG, results.get(i) + " might not be extSDcard");
                    results.remove(i--);
                }
            }
        }

        String[] storageDirectories = new String[results.size()];
        for (int i = 0; i < results.size(); ++i) storageDirectories[i] = results.get(i);

        return storageDirectories;
    }

    /**
     * Returns File with absolute path to physical file in memory. Uri should be a valid File Uri fil:///
     *
     * @param path Should contain a valid File Uri path
     * @return File pointing to physical file in memory
     */
    public static File getFileFromFileUriPath(String path) {
        try {
            Uri uri = Uri.parse(path);
            return new File(URI.create(uri.toString()));
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Gets File from DocumentFile if Uri is file:///
     *
     * @param documentFile Document file that contains Uri to create File from
     * @return File with absolute path to the physical file on device's memory
     */
    public static File getFileFromDocumentFile(DocumentFile documentFile) {
        try {
            return new File(URI.create(documentFile.getUri().toString()));
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Gets absolute path of a file in SD Card if Uri of Document file is content Uri content:// .
     *
     * @param documentFile DocumentFile Uri is content uri
     * @return Absolute path of the file
     */

    public static String getSDCardPathFromDocumentFile(DocumentFile documentFile) {
        // We can't get absolute path from DocumentFile or Uri.
        // It is a hack to build absolute path by DocumentFile.
        // May not work on some devices.
        try {
            Uri uri = documentFile.getUri();
            final String docId = DocumentsContract.getDocumentId(uri);
            final String[] split = docId.split(":");

            String sd = null;
            sd = System.getenv("SECONDARY_STORAGE");

            if (sd == null) {
                // sd = System.getenv("EXTERNAL_STORAGE");

                return "/storage" + "/" + docId.replace(":", "/");
            }
            if (sd != null) {
                // On some devices SECONDARY_STORAGE has several paths
                // separated with a colon (":"). This is why we split
                // the String.
                String[] paths = sd.split(":");
                for (String p : paths) {
                    File fileSD = new File(p);
                    if (fileSD.isDirectory()) {
                        sd = fileSD.getAbsolutePath();
                    }
                }
                String id = split[1];
                String documentPath = sd + "/" + id;
                return documentPath;
            }
        } catch (Exception e) {
            System.out.println("FileUtils getSDCardPathFromDocumentFile() ERROR " + e.toString());
            return null;
        }

        return null;
    }

    /**
     * Get a file path from a Uri. This will get the the path for Storage Access
     * Framework Documents, as well as the _data field for the MediaStore and other
     * file-based ContentProviders.
     *
     * @param context The context.
     * @param uri     The Uri to query.
     */
    public static String getPath(final Context context, final Uri uri) {
        // DocumentProvider
        if (DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];
                System.out.println("getPath() docId: " + docId + ", split: " + split.length + ", type: " + type);

                if ("primary".equalsIgnoreCase(type)) {
                    if (split.length > 1) {
                        return Environment.getExternalStorageDirectory() + "/" + split[1] + "/";
                    } else {
                        return Environment.getExternalStorageDirectory() + "/";
                    }
                } else {
                    return "storage" + "/" + docId.replace(":", "/");
                }

            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {

                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri =
                        ContentUris.withAppendedId(
                                Uri.parse("content://downloads/public_downloads"),
                                Long.parseLong(id)
                        );

                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[]{split[1]};

                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {

            // Return the remote address
            if (isGooglePhotosUri(uri))
                return uri.getLastPathSegment();

            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }

    /**
     * Get the value of the data column for this Uri. This is useful for MediaStore
     * Uris, and other file-based ContentProviders.
     *
     * @param context       The context.
     * @param uri           The Uri to query.
     * @param selection     (Optional) Filter used in the query.
     * @param selectionArgs (Optional) Selection arguments used in the query.
     * @return The value of the _data column, which is typically a file path.
     */
    public static String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {column};

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null);
            if (cursor != null && cursor.moveToFirst()) {
                final int index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is Google Photos.
     */
    public static boolean isGooglePhotosUri(Uri uri) {
        return "com.google.android.apps.photos.content".equals(uri.getAuthority());
    }

}