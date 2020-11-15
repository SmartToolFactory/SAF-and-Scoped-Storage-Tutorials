package com.smarttoolfactory.saf3_imagegallery.libs.utils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;

public final class ImageUtils {

    private ImageUtils() {

    }

    public static Size getImageDimensions(String path) {

        Size size = new Size();
        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);

        // Calculate inSampleSize, Raw height and width of image
        size.height = options.outHeight;
        size.width = options.outWidth;

        return size;
    }

    public static Size getImageDimensions(Context context, Uri imageUri) {
        Size size = new Size();

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        InputStream imageStream = null;
        try {
            imageStream = context.getContentResolver().openInputStream(imageUri);
            BitmapFactory.decodeStream(imageStream, null, options);
            imageStream.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Calculate inSampleSize, Raw height and width of image
        size.height = options.outHeight;
        size.width = options.outWidth;

        return size;
    }

    public static float getImageWidthHeightRatio(String path) {
        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);

        // Calculate inSampleSize, Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;

        return (float) width / height;
    }

    public static float getImageWidthHeightRatio(Context context, Uri imageUri) {
        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        InputStream imageStream = null;
        try {
            imageStream = context.getContentResolver().openInputStream(imageUri);
            BitmapFactory.decodeStream(imageStream, null, options);
            imageStream.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Calculate inSampleSize, Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;

        return (float) width / height;
    }

    /**
     * Decodes byte array to Bitmap. Bitmap is mutable if inMutable is set to true.
     * Mutable bitmap is required for drawing to a Canvas.
     *
     * @param data      byte array that contains image
     * @param inMutable If set, decode methods will always return a mutable Bitmap instead
     *                  of an immutable one. This can be used for instance to
     *                  programmatically apply effects to a Bitmap loaded through
     *                  BitmapFactory.
     * @return Bitmap decoded from byte array
     */
    public static Bitmap decodeByteArray(byte[] data, boolean inMutable) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inMutable = inMutable;
        return BitmapFactory.decodeByteArray(data, 0, data.length, options);
    }

    /**
     * Decodes Bitmap from an InputStream. <strong>mutable</strong> Bitmap if
     * inMutable is true. Mutable Bitmap file is required for Canvas
     * <p>
     * The Image file
     *
     * @return A mutable Bitmap
     * @throws IOException
     */
    public static Bitmap decodeStream(byte[] data, boolean inMutable) throws IOException {

        InputStream imageStream = new ByteArrayInputStream(data);
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inMutable = inMutable;
        Bitmap bitmap = BitmapFactory.decodeStream(imageStream, null, options);
        imageStream.close();

        return bitmap;
    }

    /**
     * Gets bitmap using content Uri and down-samples Bitmap using specified width
     * and height
     *
     * @param context       The current context
     * @param selectedImage The Image URI
     * @return Bitmap image results
     * @throws IOException
     */
    public static Bitmap decodeSampledBitmapStream(Context context, Uri selectedImage, int reqWidth, int reqHeight)
            throws IOException {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        InputStream imageStream = context.getContentResolver().openInputStream(selectedImage);
        BitmapFactory.decodeStream(imageStream, null, options);
        imageStream.close();
        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        imageStream = context.getContentResolver().openInputStream(selectedImage);
        Bitmap img = BitmapFactory.decodeStream(imageStream, null, options);
        imageStream.close();
        return img;
    }

    /**
     * Down-scale an image from absolute path and get result as a Bitmap file
     *
     * @param path
     * @param reqWidth
     * @param reqHeight
     * @return
     */
    public static Bitmap decodeSampledBitmapStream(String path, int reqWidth, int reqHeight) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);

        // Calculate inSampleSize, Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        int inSampleSize = 1;

        if (height > reqHeight) {
            inSampleSize = Math.round((float) height / (float) reqHeight);
        }
        int expectedWidth = width / inSampleSize;

        if (expectedWidth > reqWidth) {
            // if(Math.round((float)width / (float)reqWidth) > inSampleSize) //
            // If bigger SampSize..
            inSampleSize = Math.round((float) width / (float) reqWidth);
        }

        options.inSampleSize = inSampleSize;
        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;

        return BitmapFactory.decodeFile(path, options);
    }

    /**
     * Calculate an inSampleSize for use in a {@link BitmapFactory.Options} object
     * when decoding bitmaps using the decode methods from {@link BitmapFactory}.
     * This implementation calculates the closest inSampleSize that will result in
     * the final decoded bitmap having a width and height equal to or larger than
     * the requested width and height. This implementation does not ensure a power
     * of 2 is returned for inSampleSize which can be faster when decoding but
     * results in a larger bitmap which isn't as useful for caching purposes.
     *
     * @param options   An options object with out params already populated (run through a
     *                  decode* method with inJustDecodeBounds==true
     * @param reqWidth  The requested width of the resulting bitmap
     * @param reqHeight The requested height of the resulting bitmap
     * @return The value to be used for inSampleSize
     */
    private static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;

        int inSampleSize = 1;
        // Toast.makeText(context, "Options height " + height + ", desired
        // Height " + reqHeight, Toast.LENGTH_LONG).show();
        // Toast.makeText(context, "Options width " + width + ", desired Width "
        // + reqWidth, Toast.LENGTH_LONG).show();
        if (height > reqHeight || width > reqWidth) {

            // Calculate ratios of height and width to requested height and
            // width
            final int heightRatio = Math.round((float) height / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);

            // Choose the smallest ratio as inSampleSize value, this will
            // guarantee a final image
            // with both dimensions larger than or equal to the requested height
            // and width.
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
            // Toast.makeText(context, "inSampleSize calculated " +
            // inSampleSize, Toast.LENGTH_LONG).show();

            // This offers some additional logic in case the image has a strange
            // aspect ratio. For example, a panorama may have a much larger
            // width than height. In these cases the total pixels might still
            // end up being too large to fit comfortably in memory, so we should
            // be more aggressive with sample down the image (=larger
            // inSampleSize).

            final float totalPixels = width * height;

            // Anything more than 2x the requested pixels we'll sample down
            // further
            final float totalReqPixelsCap = reqWidth * reqHeight * 2;

            while (totalPixels / (inSampleSize * inSampleSize) > totalReqPixelsCap) {
                inSampleSize++;
            }
        }
        return inSampleSize;
    }

    /**
     * Get image orientation from a file using ExifInterface
     *
     * @param imagePath Original path of the image on disk or SD card
     * @return orientation of the image
     */
    public static int getOrientationFromEXIF(String imagePath) {
        int orientation = ExifInterface.ORIENTATION_NORMAL;
        try {
            ExifInterface ei = new ExifInterface(imagePath);
            orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return orientation;
    }

    public static int getRotationAngleFromEXIF(String imagePath) {
        int degreesToRotate = 0;
        try {
            ExifInterface ei = new ExifInterface(imagePath);
            int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    degreesToRotate = 90;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    degreesToRotate = 180;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    degreesToRotate = 270;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return degreesToRotate;
    }

    /**
     * This method rotates a previously saved image inside disk. Gets imagePath and
     * checks Exif data to find rotation values
     *
     * @param img           final image after rotation of captured images
     * @param selectedImage path of the saved image
     * @return
     * @throws IOException
     */
    public static Bitmap rotateCapturedImageFromExif(Bitmap img, Uri selectedImage) throws IOException {

        ExifInterface ei = new ExifInterface(selectedImage.getPath());
        int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                return rotateBitmap(img, 90);
            case ExifInterface.ORIENTATION_ROTATE_180:
                return rotateBitmap(img, 180);
            case ExifInterface.ORIENTATION_ROTATE_270:
                return rotateBitmap(img, 270);
            default:
                return img;
        }
    }

    /**
     * This method rotates saved image. Gets imagePath and checks Exif data to find
     * rotation values
     *
     * @param bitmap        Final rotated bitmap retreived from a previously captured image
     *                      path.
     * @param fileImagePath path of the image on disk
     * @return
     * @throws IOException
     */
    public static Bitmap rotateCapturedImageFromExif(Bitmap bitmap, File fileImagePath) throws IOException {
        ExifInterface ei = new ExifInterface(fileImagePath.getPath());
        int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                return rotateBitmap(bitmap, 90);
            case ExifInterface.ORIENTATION_ROTATE_180:
                return rotateBitmap(bitmap, 180);
            case ExifInterface.ORIENTATION_ROTATE_270:
                return rotateBitmap(bitmap, 270);
            default:
                return bitmap;
        }
    }

    /**
     * Rotates and/or flips Bitmap depending on orientation written to Exif data of
     * the image
     *
     * @param bitmap      Bitmap retrieved from image on disk
     * @param orientation Orientation is retrieved from Exif orientation of image on disk
     * @return Returns bitmap rotated and/or flipped which depends on Exif
     * orientation
     */
    public static Bitmap rotateFlipCapturedImageFromExif(Bitmap bitmap, int orientation) {

        Matrix matrix = new Matrix();
        switch (orientation) {
            case ExifInterface.ORIENTATION_NORMAL:
                return bitmap;
            case ExifInterface.ORIENTATION_FLIP_HORIZONTAL:
                matrix.setScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_ROTATE_180:
                matrix.setRotate(180);
                break;
            case ExifInterface.ORIENTATION_FLIP_VERTICAL:
                matrix.setRotate(180);
                matrix.postScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_TRANSPOSE:
                matrix.setRotate(90);
                matrix.postScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_ROTATE_90:
                matrix.setRotate(90);
                break;
            case ExifInterface.ORIENTATION_TRANSVERSE:
                matrix.setRotate(-90);
                matrix.postScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_ROTATE_270:
                matrix.setRotate(-90);
                break;
            default:
                return bitmap;
        }
        try {
            Bitmap bmRotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
            bitmap.recycle();
            return bmRotated;
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
            return bitmap;
        }
    }

    /**
     * Rotates Bitmap by specified angle using Matrix rotation
     *
     * @param img     Bitmap to be rotated
     * @param degrees Rotation degree
     * @return Returns rotated Bitmap
     */
    public static Bitmap rotateBitmap(Bitmap img, int degrees) {
        // Toast.makeText(context, "Bitmap rotation angle " + degree,
        // Toast.LENGTH_SHORT).show();
        Matrix matrix = new Matrix();
        matrix.postRotate(degrees);
        Bitmap rotatedImg = Bitmap.createBitmap(img, 0, 0, img.getWidth(), img.getHeight(), matrix, true);
        if (rotatedImg == null) {
            return img;
        }
        img.recycle();
        return rotatedImg;
    }

    /**
     * Rotates and/or flips Bitmap
     *
     * @param img     Bitmap image to be operated on
     * @param degrees Degrees of rotation
     * @param x       Scaling factor for x. -1 mirrors image horizontally
     * @param y       Scaling factor for y. -1 mirrors image vertically
     * @return Rotated and flipped bitmap
     */
    public static Bitmap rotateAndFlipBitmap(Bitmap img, int degrees, int x, int y) {

        Matrix matrix = new Matrix();
        matrix.setRotate(degrees);
        matrix.postScale(x, y);
        Bitmap rotatedImg = Bitmap.createBitmap(img, 0, 0, img.getWidth(), img.getHeight(), matrix, true);
        if (rotatedImg == null) {
            return img;
        }
        img.recycle();
        return rotatedImg;
    }

    public static class Size {
        public static int width = 0;
        public static int height = 0;
    }

}
