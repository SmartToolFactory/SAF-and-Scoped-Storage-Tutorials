package com.smarttoolfactory.saf3_imagegallery.libs.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.os.Environment;
import android.provider.MediaStore;

import com.smarttoolfactory.saf3_imagegallery.config.Config;
import com.smarttoolfactory.saf3_imagegallery.libs.gallery.model.GalleryItem;

public class GalleryUtils {

	// Define bucket name from which you want to take images Example '/DCIM/Camera'
	// for camera images
	public static final String CAMERA_IMAGE_BUCKET_NAME = Environment.getExternalStorageDirectory().toString()
			+ File.separator + Config.APP_DIRECTORY;

	// method to get id of image bucket from path
	public static String getBucketId(String path) {
		return String.valueOf(path.toLowerCase().hashCode());
	}

	// method to get images
	public static List<GalleryItem> getImages(Context context) {
		// Columns to be retreived
		final String[] projection = { MediaStore.Images.Media.DISPLAY_NAME, MediaStore.Images.Media.DATA };
		// Select projection WHERE selection (id = ?) == selectionArgs
		final String selection = MediaStore.Images.Media.BUCKET_ID + " = ?";
		final String[] selectionArgs = { GalleryUtils.getBucketId(CAMERA_IMAGE_BUCKET_NAME) };

		final Cursor cursor = context.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
				projection, selection, selectionArgs, null);

		ArrayList<GalleryItem> result = new ArrayList<>(cursor.getCount());

		if (cursor.moveToFirst()) {
			final int dataColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
			final int nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME);
			final int sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.SIZE);
			final int dateColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_MODIFIED);

			while (cursor.moveToNext()) {
				GalleryItem imageItem = new GalleryItem();
				imageItem.setName(cursor.getString(nameColumn));
				imageItem.setPath(cursor.getString(dataColumn));
				imageItem.setSize(0);
				imageItem.setDateLastModified(System.currentTimeMillis());
				result.add(imageItem);
			}
		}
		cursor.close();
		return result;

	}
}
