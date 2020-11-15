package com.smarttoolfactory.saf3_imagegallery.libs.exif;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;


import android.annotation.TargetApi;
import android.content.Context;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.ParcelFileDescriptor;

import androidx.annotation.RequiresApi;

import com.smarttoolfactory.saf3_imagegallery.libs.utils.FileUtils;

public final class ExifManager {

    private ExifManager() {

    }

    /**
     * Returns Exif tags available
     *
     * @return Exif tags
     */
    public static Set<String> getAllTags() {
        Set<String> exifTags = new HashSet<>();
        exifTags.add(ExifInterface.TAG_MAKE);
        exifTags.add(ExifInterface.TAG_MODEL);
        exifTags.add(ExifInterface.TAG_ORIENTATION);
        exifTags.add(ExifInterface.TAG_IMAGE_WIDTH);
        exifTags.add(ExifInterface.TAG_IMAGE_LENGTH);
        exifTags.add(ExifInterface.TAG_EXPOSURE_TIME);
        exifTags.add(ExifInterface.TAG_FOCAL_LENGTH);
        exifTags.add(ExifInterface.TAG_FLASH);
        exifTags.add(ExifInterface.TAG_WHITE_BALANCE);
        // GPS Data
        exifTags.add(ExifInterface.TAG_DATETIME);
        exifTags.add(ExifInterface.TAG_GPS_ALTITUDE);
        exifTags.add(ExifInterface.TAG_GPS_ALTITUDE_REF);
        exifTags.add(ExifInterface.TAG_GPS_LATITUDE);
        exifTags.add(ExifInterface.TAG_GPS_LATITUDE_REF);
        exifTags.add(ExifInterface.TAG_GPS_LONGITUDE);
        exifTags.add(ExifInterface.TAG_GPS_LONGITUDE_REF);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            // Deprecated counterpart exist
            exifTags.add(ExifInterface.TAG_APERTURE_VALUE);
            exifTags.add(ExifInterface.TAG_ISO_SPEED_RATINGS);

			/*
             * Only available for Api 24(N) and above
			 */
            exifTags.add(ExifInterface.TAG_SHUTTER_SPEED_VALUE);
            exifTags.add(ExifInterface.TAG_DIGITAL_ZOOM_RATIO);
            exifTags.add(ExifInterface.TAG_SCENE_TYPE);
            exifTags.add(ExifInterface.TAG_LIGHT_SOURCE);
            // User Info
            exifTags.add(ExifInterface.TAG_COPYRIGHT);
            exifTags.add(ExifInterface.TAG_ARTIST);
            exifTags.add(ExifInterface.TAG_SOFTWARE);
            exifTags.add(ExifInterface.TAG_IMAGE_DESCRIPTION);
            exifTags.add(ExifInterface.TAG_USER_COMMENT);
        } else {
            exifTags.add(ExifInterface.TAG_APERTURE);
            exifTags.add(ExifInterface.TAG_ISO);
        }

        return exifTags;
    }

    /**
     * Writes Exif data to image file specified by absolute path fileName.
     *
     * @param fileName Path of physical file on disk
     * @param exifData Map that contains Exif tags and attributes
     */
    public static void writeExifData(String fileName, Map<String, String> exifData) {
        // System.out.println("****** ExifWriter writeEXIFWithFile() ******");
        ExifInterface exifInterface = null;
        Set<String> exifTags = exifData.keySet();
        try {
            exifInterface = new ExifInterface(fileName);
            for (String exifTag : exifTags) {
                // System.out.println("Tag: " + exifTag);
                if (exifData.get(exifTag) != null) {
                    // System.out.println("MATCHED Tag: " + exifTag + ", value: " +
                    // exifData.get(exifTag));
                    exifInterface.setAttribute(exifTag, exifData.get(exifTag));
                }
            }
            exifInterface.saveAttributes();
        } catch (IOException e) {
            e.printStackTrace();
        }
        // System.out.println("****** !!!ExifWriter writeEXIFWithFile()!!! ******");

    }

    /**
     * Writes Exif data to image file specified by Uri using
     * <strong>ParcelFileDescriptor</strong>
     *
     * @param context  Context is needed to create ParcelFileDescriptor from
     *                 ContentResolver
     * @param uri      Uri of the physical file on the disk that Exif data to be written
     *                 into
     * @param exifData Map that contains Exif tags and attributes
     */

    @RequiresApi(api = Build.VERSION_CODES.N)
    public static void writeExifData(Context context, Uri uri, Map<String, String> exifData) {

        // System.out.println("ExifWriter writeEXIFWithFileDescriptor() for Api>=24");
        ParcelFileDescriptor parcelFileDescriptor = null;
        try {

            parcelFileDescriptor = context.getContentResolver().openFileDescriptor(uri, "rw");
            FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
            ExifInterface exifInterface = new ExifInterface(fileDescriptor);
            Set<String> exifTags = exifData.keySet();
            for (String exifTag : exifTags) {
                // System.out.println("Tag: " + exifTag);
                if (exifData.get(exifTag) != null) {
                    // System.out.println("MATCHED Tag: " + exifTag + ", value: " +
                    // exifData.get(exifTag));
                    exifInterface.setAttribute(exifTag, exifData.get(exifTag));
                }
            }
            exifInterface.saveAttributes();

        } catch (FileNotFoundException e) {

        } catch (IOException e) {
            // Handle any errors
            e.printStackTrace();
        } finally {
            if (parcelFileDescriptor != null) {
                try {
                    parcelFileDescriptor.close();
                } catch (IOException ignored) {
                    ignored.printStackTrace();
                }
            }
        }
    }

    private static void setExifTagsAndValues(ExifInterface exifInterface) {
		/*
		 * ***** Get all Exif tags and values *****
		 */
        double latitude = 32.7825;
        double longitude = -96.8207;

        exifInterface.setAttribute(ExifInterface.TAG_MODEL, Build.MODEL);
        exifInterface.setAttribute(ExifInterface.TAG_MAKE, Build.MANUFACTURER);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy:MM:dd HH:mm:ss", Locale.ROOT);
        String time = sdf.format(new Date());
		/*
		 * GPS and Time Info
		 */
        exifInterface.setAttribute(ExifInterface.TAG_DATETIME, time);

        exifInterface.setAttribute(ExifInterface.TAG_GPS_LATITUDE, EXIFGPSConvertor.convert(latitude));
        exifInterface.setAttribute(ExifInterface.TAG_GPS_LATITUDE_REF, EXIFGPSConvertor.latitudeRef(latitude));
        exifInterface.setAttribute(ExifInterface.TAG_GPS_LONGITUDE, EXIFGPSConvertor.convert(longitude));
        exifInterface.setAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF, EXIFGPSConvertor.longitudeRef(longitude));

        exifInterface.setAttribute(ExifInterface.TAG_ARTIST, "Unknown Artist");
        // exifInterface.setAttribute(ExifInterface.TAG_SOFTWARE, Config.APP_NAME);
        exifInterface.setAttribute(ExifInterface.TAG_COPYRIGHT, "Smart Tools Company");
        exifInterface.setAttribute(ExifInterface.TAG_IMAGE_DESCRIPTION, "This is description");
        exifInterface.setAttribute(ExifInterface.TAG_USER_COMMENT, "Hello World");
        exifInterface.setAttribute(ExifInterface.TAG_FOCAL_LENGTH, "1150/1000");
        exifInterface.setAttribute(ExifInterface.TAG_FOCAL_LENGTH_IN_35MM_FILM, "1250/1000");

		/*
		 *
		 * ****************************************
		 */
    }

    /**
     * Get exif tag and attribute pairs from an absolute file path
     *
     * @param fileName Absolute path of the jpg file
     * @return Map that contains Exif data with Exif tags
     */
    public static Map<String, String> getExifDataMap(String fileName) {
        Map<String, String> exifData = new HashMap<>();
        try {
            ExifInterface exifInterface = new ExifInterface(fileName);
            for (String exifTag : getAllTags()) {
                String exifAttribute = exifInterface.getAttribute(exifTag);
                if (exifAttribute != null) {
                    exifData.put(exifTag, exifAttribute);
//                    System.out.println(
//                            "ExifManager getExifDataMap(String) tag: " + exifTag + ", exifAttribute: " + exifAttribute);
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return exifData;
    }

    /**
     * Get Exif tag and attribute pairs using a content uri and temporary file if
     * Api is below 24
     *
     * @param context   Context to open InputStream and OutputStream
     * @param uriString String that contains content uri
     * @return Map that contains Exif data with Exif tags
     */
    public static Map<String, String> getExifDataMap(Context context, String uriString) {
        Map<String, String> exifData = new HashMap<>();

        if (!FileUtils.isContentUri(uriString)) {
            exifData = ExifManager.getExifDataMap(uriString);
        } else {
            OutputStream os = null;
            InputStream is = null;

            File bufFile = new File(Environment.getExternalStorageDirectory(), "tmp.jpg");

            try {
                int len;
                byte[] buf = new byte[4096];

                os = context.getContentResolver().openOutputStream(Uri.fromFile(bufFile));
                is = context.getContentResolver().openInputStream(Uri.parse(uriString));

                while ((len = is.read(buf)) > 0) {
                    os.write(buf, 0, len);
                }

                exifData = ExifManager.getExifDataMap(bufFile.getAbsolutePath());

                if (bufFile != null) {
                    bufFile.delete();
                    bufFile = null;
                }

            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    os.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return exifData;
    }

    /**
     * Returns Exif tag and attribute pairs using a content Uri and InputStream
     * </br>
     * This method is available for Api 24 and above.
     *
     * @param inputStream InputStream is opened using a content uri
     * @return Map that contains Exif data with Exif tags
     */
    @TargetApi(Build.VERSION_CODES.N)
    public static Map<String, String> getExifDataMap(InputStream inputStream) {
        Map<String, String> exifData = new HashMap<>();
        try {
            ExifInterface exifInterface = new ExifInterface(inputStream);
            for (String exifTag : getAllTags()) {
                String exifAttribute = exifInterface.getAttribute(exifTag);
                if (exifAttribute != null) {
                    exifData.put(exifTag, exifAttribute);
//                    System.out.println("ExifManager getExifDataMap(InputStream) tag: " + exifTag + ", exifAttribute: "
//                            + exifAttribute);
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return exifData;
    }

    /**
     * Reads Exif data from a file and adds data to existing map of Exif data.
     *
     * @param exifData non-null Exif data map
     * @param fileName Path of physical file on disk that contains Exif info
     */
    public static void addExifData(Map<String, String> exifData, String fileName) {
        try {
            ExifInterface exifInterface = new ExifInterface(fileName);
            for (String exifTag : getAllTags()) {
                String exifAttribute = exifInterface.getAttribute(exifTag);
                if (exifAttribute != null) {
                    exifData.put(exifTag, exifAttribute);
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * Reads Exif data from an InputStream and adds data to existing map of Exif
     * data.
     *
     * @param exifData    non-null Exif data map
     * @param inputStream InputStream to physical file on disk that contains Exif info
     */
    @RequiresApi(api = Build.VERSION_CODES.N)
    public static void addExifData(Map<String, String> exifData, InputStream inputStream) {
        try {
            ExifInterface exifInterface = new ExifInterface(inputStream);
            for (String exifTag : getAllTags()) {
                String exifAttribute = exifInterface.getAttribute(exifTag);
                if (exifAttribute != null) {
                    exifData.put(exifTag, exifAttribute);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
