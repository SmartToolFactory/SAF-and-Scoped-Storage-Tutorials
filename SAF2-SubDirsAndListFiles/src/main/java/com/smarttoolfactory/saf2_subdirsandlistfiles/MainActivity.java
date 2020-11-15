package com.smarttoolfactory.saf2_subdirsandlistfiles;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.UriPermission;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.documentfile.provider.DocumentFile;

import com.smarttoolfactory.saf2_subdirsandlistfiles.fileutils.FileUtils;
import com.smarttoolfactory.saf2_subdirsandlistfiles.permissions.PermissionsUtils;

import java.io.File;
import java.io.FileDescriptor;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

@SuppressLint("NewApi")
public class MainActivity extends AppCompatActivity {
    private static final String MY_FOLDER = "MyFolder";

    private static final String TAG = MainActivity.class.getName();

    private static final String KEY_CURRENT_FOLDER = "PATH";

    private static final int REQUEST_FOLDER_ACCESS = 1001;
    private static final int READ_REQUEST_CODE = 42;

    // Views
    private TextView tvSAF1, tvSAF2;
    private ImageView ivSAF;

    // Last saved folder
    private String currentFolder = "";

    private SharedPreferences mSharedPreferences;

    private Bitmap mBitmap;
    private boolean isImageSaved = false;
    private String lastSavedImagePath = "";
    private String lastSavedImageName = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            PermissionsUtils.requestWriteExternalPermission(MainActivity.this);
        }

        tvSAF1 = (TextView) findViewById(R.id.tvSAF1);
        tvSAF2 = (TextView) findViewById(R.id.tvSAF2);

        ivSAF = (ImageView) findViewById(R.id.ivSAF);

        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
        // currentFolder should contain full URI: content://authority/path/
        currentFolder = mSharedPreferences.getString(KEY_CURRENT_FOLDER, "");

        DocumentFile mediaStorageDir = null;

        if (currentFolder.equals("")) {
            mediaStorageDir = DocumentFile.fromFile(Environment.getExternalStorageDirectory());
        } else {
            mediaStorageDir = DocumentFile.fromTreeUri(MainActivity.this, Uri.parse(currentFolder));
        }

        DocumentFile[] files = mediaStorageDir.listFiles();

        StringBuilder sb = new StringBuilder();
        sb.append("onCreate() Folders\n");
        if (files != null & files.length > 0) {
            for (DocumentFile documentFile : files) {
                boolean isUriWrite = isUriWritePermission(currentFolder);
                sb.append(documentFile.getUri().toString() + ", canWrite: " + documentFile.canWrite() + "\n");
                sb.append("*************\n");
            }
        }


        tvSAF1.setText("onCreate() currentFolder: " + currentFolder);
        tvSAF2.setText(sb.toString());

    }

    /**
     * Fires an intent to spin up the folder chooser
     */
    public void selectDirectory() {
        // ACTION_OPEN_DOCUMENT_TREE is the intent to choose a folder
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        startActivityForResult(intent, REQUEST_FOLDER_ACCESS);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {

            // selectDirectory() invoked
            if (requestCode == REQUEST_FOLDER_ACCESS) {

                if (data.getData() != null) {
                    Uri treeUri = data.getData();
                    currentFolder = treeUri.toString();
                    saveCurrentFolderToPrefs();

                    DocumentFile dir = DocumentFile.fromTreeUri(getApplicationContext(), treeUri);
                    DocumentFile[] files = dir.listFiles();

                    StringBuilder sb = new StringBuilder();
                    sb.append("onActivityResult() Folders\n");
                    if (files != null & files.length > 0) {
                        for (DocumentFile documentFile : files) {
                            sb.append(documentFile.getUri().toString() + "\n");
                        }
                    }
                    tvSAF1.setText("onActivityResult() treeUri.toString(): " + treeUri.toString());
                    tvSAF2.setText(sb.toString());

                    final int takeFlags = data.getFlags()
                            & (Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                    // Check for the freshest data.
                    getContentResolver().takePersistableUriPermission(treeUri, takeFlags);

                }
            }

            // openFile() invoked
            if (requestCode == READ_REQUEST_CODE) {
                Uri uri = data.getData();
                setImageViewFromBitmapAsync(uri);
                // Get Persistable Permissions
                final int takeFlags = data.getFlags()
                        & (Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                // Check for the freshest data.
                getContentResolver().takePersistableUriPermission(uri, takeFlags);

            }
        }
    }

    /**
     * Fires an intent to spin up the "file chooser" UI and select an image.
     */
    private void openFile() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        startActivityForResult(intent, READ_REQUEST_CODE);
    }

    /**
     * Create a Bitmap from the URI for that image and return it.
     *
     * @param uri the Uri for the image to return.
     */
    private Bitmap getBitmapFromUri(Uri uri) {
        ParcelFileDescriptor parcelFileDescriptor = null;
        try {
            parcelFileDescriptor = getContentResolver().openFileDescriptor(uri, "r");
            FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
            Bitmap image = BitmapFactory.decodeFileDescriptor(fileDescriptor);
            parcelFileDescriptor.close();
            return image;
        } catch (Exception e) {
            Log.e(TAG, "Failed to load image.", e);
            return null;
        } finally {
            try {
                if (parcelFileDescriptor != null) {
                    parcelFileDescriptor.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
                Log.e(TAG, "Error closing ParcelFile Descriptor");
            }
        }
    }

    /**
     * Sets imageView bitmap using uri retreived from SAF. Uses Asynctask for
     * loading the image
     *
     * @param uri path retreived from SAF
     */
    private void setImageViewFromBitmapAsync(Uri uri) {
        AsyncTask<Uri, Void, Bitmap> imageLoadAsyncTask = new AsyncTask<Uri, Void, Bitmap>() {
            @Override
            protected Bitmap doInBackground(Uri... uris) {
                return getBitmapFromUri(uris[0]);
            }

            @Override
            protected void onPostExecute(Bitmap bitmap) {
                mBitmap = bitmap;
                ivSAF.setImageBitmap(mBitmap);
            }
        };

        imageLoadAsyncTask.execute(uri);
    }

    /**
     * Saves image to a DocumentFile or File depending on which method used to
     * create a file for image
     *
     * @param mimeType image/png or image/jpeg
     * @param name     Name of the image file with prefix and time stamp
     */
    public void saveImage(String uriString, String mimeType, String name) {
        isImageSaved = false;
        DocumentFile saveDir = null;

        // *** Method1: Get Uri From SAF with Uri://content...
        saveDir = getSaveDirNew(uriString);

        /*
         * TODO Write permission is enabled for directories previously created on DEVICE
         * Or you can create a direct child of externalStorageDirectory(main) directory
         * Main/Folder1 is permitted, Main/Folder1/FolderSub1 is not permitted
         */

        if (saveDir != null && saveDir.exists() && saveDir.canWrite()) {
            try {

                DocumentFile documentFile = saveDir.createFile(mimeType, name);
                OutputStream outputStream = getContentResolver().openOutputStream(documentFile.getUri());
                isImageSaved = mBitmap.compress(CompressFormat.JPEG, 100, outputStream);
                if (isImageSaved) {
                    lastSavedImagePath = saveDir.getUri().toString();
                    lastSavedImageName = documentFile.getName();
                    System.out.println("YEP!!!lastSavedImagePath: " + lastSavedImagePath);

                    // TODO TEST
                    ivSAF.setImageBitmap(null);
                }

                outputStream.close();

            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            selectDirectory();
        }

    }

    @TargetApi(19)
    protected DocumentFile getSaveDirNew(String uriString) {
        DocumentFile saveDir = null;

        boolean canWrite = isUriWritePermission(uriString);
        Toast.makeText(this, "getSaveDirNew() canWrite: " + canWrite, Toast.LENGTH_LONG).show();

        if (canWrite) {
            try {
                saveDir = DocumentFile.fromTreeUri(MainActivity.this, Uri.parse(uriString));
            } catch (Exception e) {
                saveDir = null;
            }
        }
        Toast.makeText(this, "getSaveDirNew() saveDir: " + saveDir, Toast.LENGTH_LONG).show();
        if (saveDir == null) {
            return null;
        }

        tvSAF1.setText("getSaveDirNew() uriString: " + uriString);
        tvSAF2.setText("getSaveDirNew() saveDir.getUri().toString(): " + saveDir.getUri().toString() + ", canWrite: "
                + canWrite + ", exist: " + saveDir.exists() + ", isDirectory: " + saveDir.isDirectory());

        return saveDir;
    }

    private void deleteImage() {
        boolean isImageDeleted = false;
        DocumentFile imageToDeletePath = null;
        DocumentFile imageToDelete = null;


        // Method 1 Uri From SAF UI content Uri -> content://
        if (lastSavedImagePath.startsWith("content://")) {
            imageToDeletePath = DocumentFile.fromTreeUri(this,
                    Uri.parse(lastSavedImagePath));
            imageToDelete = imageToDeletePath.findFile(lastSavedImageName);
        } else if (lastSavedImagePath.startsWith("file:///")) {
            // Method 2 Uri From DocumentFile class file Uri -> file:/// or absolute file
            // path
            File filePath = FileUtils.getFileFromFileUriPath(lastSavedImagePath);
            System.out.println("deleteImage() file Uri: " + filePath.getAbsolutePath());

            imageToDeletePath = DocumentFile.fromFile(filePath);
            imageToDelete = imageToDeletePath.findFile(lastSavedImageName);
        }


        System.out.println("deleteImage() imageToDelete " + imageToDelete);
        if (imageToDelete != null && imageToDelete.exists() && imageToDelete.isFile()) {

            System.out.println("*****************");
            System.out.println("deleteImage() imageToDeletePath: " + imageToDeletePath.getUri().toString());
            System.out.println("deleteImage() imageToDelete: " + imageToDelete.getUri().toString());
            System.out.println("deleteImage() imageToDelete name: " + imageToDelete.getName());
            System.out.println("deleteImage() imageToDelete.exists(): " + imageToDelete.exists());
            System.out.println("deleteImage() imageToDelete.isFile(): " + imageToDelete.isFile());
            System.out.println("deleteImage() imageToDelete.lastModified(): " + new Date(imageToDelete.lastModified()));
            isImageDeleted = imageToDelete.delete();
            System.out.println("deleteImage() imageToDelete.delete(): " + isImageDeleted);


        }
        if (!isImageDeleted) {
            File fileToDelete = new File(lastSavedImagePath, lastSavedImageName);
            System.out.println("deleteImage() Filepath: " + fileToDelete.toString());
            if (fileToDelete != null && fileToDelete.exists()) {
                System.out.println("deleteImage() FileToDelete: " + fileToDelete.toString());
                System.out.println("deleteImage() FileToDelete length: " + fileToDelete.length());
                boolean isFinallyDeleted = fileToDelete.delete();
                System.out.println("deleteImage() FileToDelete deleted: " + isFinallyDeleted);
            }
        }
        System.out.println("*****************");
        Toast.makeText(this, "Image deleted " + isImageDeleted, Toast.LENGTH_LONG).show();
    }

    private boolean isUriWritePermission(String uriString) {
        boolean canWrite = false;

        List<UriPermission> perms = getContentResolver().getPersistedUriPermissions();
        for (UriPermission p : perms) {
            if (p.getUri().toString().equals(uriString) && p.isWritePermission()) {
                Toast.makeText(this, "canWrite() can write URI::  " + p.getUri().toString(), Toast.LENGTH_LONG).show();
                canWrite = true;
                break;
            }
        }
        return canWrite;
    }

    private void saveCurrentFolderToPrefs() {
        Editor edit = mSharedPreferences.edit();
        edit.putString(KEY_CURRENT_FOLDER, currentFolder);
        edit.commit();
        Toast.makeText(MainActivity.this, "Current Folder=> " + currentFolder, Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.reset_directory:
                DocumentFile baseDir = DocumentFile.fromFile(Environment.getExternalStorageDirectory());
                DocumentFile saveDir = baseDir.findFile(MY_FOLDER);
                if (saveDir == null || !saveDir.exists()) {
                    saveDir = baseDir.createDirectory(MY_FOLDER);
                }
                if (saveDir != null) {
                    currentFolder = saveDir.getUri().toString();
                    saveCurrentFolderToPrefs();

                    tvSAF1.setText("CURRENT FOLDER: " + currentFolder);
                    tvSAF2.setText("CURRENT FOLDER PATH: " + saveDir.getUri().toString() + " canWrite: " + saveDir.canWrite());
                }

                break;

            case R.id.select_directory:
                selectDirectory();
                break;
            case R.id.get_bitmap:
                openFile();
                break;
            case R.id.save_image:

                SimpleDateFormat sdf = new SimpleDateFormat("_hh_mm_ss", Locale.ROOT);
                String timeStamp = sdf.format(new Date());
                saveImage(currentFolder, "image/jpeg", "IMG" + timeStamp);
                break;

            case R.id.delete_image:
                deleteImage();
                break;
            case R.id.reset_bitmap:
                /**
                 * This is for checking for absolute path retrieved from FileUtils methods
                 */
                File fileBitmapPath = new File(lastSavedImagePath);
                File fileBitmap = new File(fileBitmapPath, lastSavedImageName);
                setImageViewFromBitmapAsync(Uri.fromFile(fileBitmap));
                break;

        }

        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflate = getMenuInflater();
        inflate.inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }

}
