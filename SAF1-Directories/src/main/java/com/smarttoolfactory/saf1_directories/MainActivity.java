package com.smarttoolfactory.saf1_directories;

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
import android.provider.DocumentsContract;
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

import com.smarttoolfactory.saf1_directories.fileutils.FileUtils;
import com.smarttoolfactory.saf1_directories.permissions.PermissionsUtils;

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
    private TextView tvSAF1, tvSAF2, tvSAF3, tvSAF4;
    private ImageView ivSAF;

    // Last saved folder
    private String currentFolder = "";

    private SharedPreferences mSharedPreferences;

    private Bitmap bitmap;
    private String lastSavedImagePath = "";
    private String lastSavedImageName = "";

    private static final String DIR_MAIN = "Main Folder";
    private static final String DIR_SUB = "Sub Folder";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Check R/W permission
        if (ActivityCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(MainActivity.this, "Permission to write is required", Toast.LENGTH_LONG).show();
            PermissionsUtils.requestWriteExternalPermission(MainActivity.this);
        }

        /**
         *  ********* EXTERNAL STORAGE CHECK *********
         */
//        // Displays External Directory and SD Cards
//        File[] dirs = getExternalFilesDirs(null);
//        for (File file : dirs) {
//            String path = file.getPath().split("/Android")[0];
//            System.out.println("Dir: " + path + "\n");
//
//            try {
//                File fileTmp = new File(path, "file.tmp");
//                boolean isFileCreated = fileTmp.createNewFile();
//                System.out.println("File " + fileTmp.toString() + ", isCreated: " + isFileCreated + "\n");
//                System.out.println("File " + fileTmp.toString() + ", isDirectory: " + fileTmp.isDirectory()
//                        + ", isFile: " + fileTmp.isFile() + "\n");
//            } catch (IOException e) {
//                e.printStackTrace();
//                System.out.println("Error: " + e.getMessage() + "\n");
//            }
//
//        }
//
//        String[] paths = FileUtils.getExternalStorageDirectories(MainActivity.this);
//
//        for (String path : paths) {
//            System.out.println("path: " + path + "\n");
//        }
        // ********* EXTERNAL STORAGE CHECK *********

        tvSAF1 = (TextView) findViewById(R.id.tvSAF1);
        tvSAF2 = (TextView) findViewById(R.id.tvSAF2);
        tvSAF3 = (TextView) findViewById(R.id.tvSAF3);
        tvSAF4 = (TextView) findViewById(R.id.tvSAF4);
        ivSAF = (ImageView) findViewById(R.id.ivSAF);

        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);

        File mediaStorageDir = new File(Environment.getExternalStorageDirectory(), "");

        // currentFolder should contain full URI: content://authority/path/id
        currentFolder = mSharedPreferences.getString(KEY_CURRENT_FOLDER, mediaStorageDir.getAbsolutePath());

        /**
         * Don't create File from path if it's Content Uri content://authority/path/id </br>
         * Returns /content:com.android.externalstorage.documents/tree/primary for main
         * memory using SAF
         */

        // Don't create file using File Uri: file:///
        File file = new File(currentFolder);

        // Returns Uri with file:///storage/emulated/0 for main device memory
        // DocumentFile documentFile =
        // DocumentFile.fromFile(Environment.getExternalStorageDirectory());
        /**
         * Returns with SAF->
         * file:///content:com.android.externalstorage.documents/tree/primary Returns
         * WITHOUT SAF-> file:///storage/emulated/0
         */

        // !!! IMPORTANT !!! Don't use fromFile with a File with File Uri-> file:///
        DocumentFile documentFileFromFile = DocumentFile.fromFile(file);
        tvSAF4.setText("onCreate() DocumentFile.fromFile().getUri().toString(): " + documentFileFromFile.getUri().toString()
                + ", exists: " + documentFileFromFile.exists() + ", canWrite: " + documentFileFromFile.canWrite());

        try {
            DocumentFile documentFileFromUri = DocumentFile.fromTreeUri(MainActivity.this, Uri.parse(currentFolder));
            if (documentFileFromUri != null) {

                DocumentFile[] files = documentFileFromUri.listFiles();

                StringBuilder sb = new StringBuilder();

                sb.append("onCreate() Folders\n");
                if (files != null & files.length > 0) {
                    for (DocumentFile documentFile : files) {
                        boolean isUriWrite = isUriWritePermission(currentFolder);
                        sb.append(documentFile.getUri().toString() + ", canWrite: " + documentFile.canWrite() + "\n");
                        sb.append("*************\n");
                    }
                }
                System.out.println(sb.toString());
                tvSAF4.setText("onCreate() DocumentFile.fromTreeUri().getUri().toString(): " + documentFileFromUri.getUri().toString());

            }
        } catch (Exception e) {
            System.out.println("onCreate() documentFileFromUri ERROR: " + e.getMessage());
        }

        tvSAF1.setText("onCreate() URI Parsed toString: " + Uri.parse(currentFolder).toString());
        tvSAF2.setText("onCreate() file.getAbsolutePath(): " + file.getAbsolutePath()+ ", exists: " + file.exists() + ", canWrite: " + file.canWrite());
        tvSAF3.setText("onCreate() currentFolder: " + currentFolder);

        // !!! Works only with valid content Uri content://
        try {
            Uri uri = Uri.parse(currentFolder);
//            uri = DocumentsContract.buildDocumentUriUsingTree(uri, DocumentsContract.getTreeDocumentId(uri));
            uri = DocumentsContract.buildDocumentUri(uri.getAuthority(), DocumentsContract.getTreeDocumentId(uri));
            boolean isDocUri = DocumentsContract.isDocumentUri(MainActivity.this, uri);
            boolean isTreeUri = DocumentsContract.isTreeUri(uri);
            System.out.println("onCreate() Uri " + uri.toString() + ", isDocUri: " + isDocUri + ", isTreeUri: " + isTreeUri);
            tvSAF3.setText("onCreate() DocumentsContract buildDocumentUriUsingTree Uri: " + uri.toString());
        } catch (NullPointerException | IllegalArgumentException e) {
            Toast.makeText(getApplicationContext(), "Exception " + e.getMessage(), Toast.LENGTH_LONG).show();
        }


        Toast.makeText(MainActivity.this, "Current Folder: " + currentFolder, Toast.LENGTH_LONG).show();
        Toast.makeText(MainActivity.this, "Current Folder is from Uri?: " + currentFolder.contains("content://"),
                Toast.LENGTH_LONG).show();

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

                    tvSAF1.setText("onActivityResult() treeUri.toString(): " + treeUri.toString());
                    tvSAF2.setText("onActivityResult() File uri: " + new File(treeUri.toString()).getAbsolutePath());
                    tvSAF3.setText(" treeUri.getPath(): " + treeUri.getPath());

                    saveCurrentFolderToPrefs();

                    // grantUriPermission(getPackageName(), treeUri,
                    // Intent.FLAG_GRANT_READ_URI_PERMISSION |
                    // Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

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

        // ACTION_OPEN_DOCUMENT is the intent to choose a file via the system's file
        // browser.
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);

        // Filter to only show results that can be "opened", such as a
        // file (as opposed to a list of contacts or timezones)
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        // Filter to show only images, using the image MIME data type.
        // If one wanted to search for ogg vorbis files, the type would be "audio/ogg".
        // To search for all documents available via installed storage providers,
        // it would be "*/*".
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
                MainActivity.this.bitmap = bitmap;
                ivSAF.setImageBitmap(MainActivity.this.bitmap);
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
        boolean isImageSaved = false;
        DocumentFile saveDir = null;

        // *** Method1: Get Uri From SAF with Uri://content...
        saveDir = getSaveDirNew(uriString);
        String pathFromContentUri = FileUtils.getPath(MainActivity.this, saveDir.getUri());
//        lastSavedImagePath = pathFromContentUri;
        tvSAF4.setText("saveImage() FileUtils.getPath(): " + pathFromContentUri);
        pathFromContentUri = FileUtils.getSDCardPathFromDocumentFile(saveDir);
        tvSAF3.setText("saveImage() FileUtils.getSDCardPathFromDocumentFile(): " + pathFromContentUri);


        // *** Method2: Get Uri DocumentFile.fromFile() Uri: file:///...
//        saveDir = getSaveDirMainMemory();
//        String pathFromFileUri = FileUtils.getFileFromDocumentFile(saveDir).toString();
//        lastSavedImagePath = pathFromFileUri;
//        tvSAF4.setText("saveImage() FileUtils.getFileFromDocumentFile(): " + pathFromFileUri);

        // TODO Check saving Current Folder
        currentFolder = saveDir.getUri().toString();
        saveCurrentFolderToPrefs();

		/*
         * TODO Write permission is enabled for directories previously created on DEVICE
		 * Or you can create a direct child of externalStorageDirectory(main) directory
		 * Main/Folder1 is permitted, Main/Folder1/FolderSub1 is not permitted
		 */

        if (saveDir != null && saveDir.exists() && saveDir.canWrite()) {
            try {

                DocumentFile documentFile = saveDir.createFile(mimeType, name);
                OutputStream outputStream = getContentResolver().openOutputStream(documentFile.getUri());
                isImageSaved = bitmap.compress(CompressFormat.JPEG, 100, outputStream);

                if (isImageSaved) {
                    lastSavedImagePath = saveDir.getUri().toString();
                    lastSavedImageName = documentFile.getName();
                    System.out.println("YEP!!!lastSavedImagePath: " + lastSavedImagePath + ", imageName: " + lastSavedImageName);
                    tvSAF3.setText("saveImage() saveDir.getUri.toString(): " +
                            saveDir.getUri().toString());
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

        boolean isWritePermission = isUriWritePermission(uriString);
        System.out.println("getSaveDirNew() uriString: " + uriString + ", isWritePermission: " + isWritePermission);

        try {
            if (uriString.startsWith("content://")) {
                saveDir = DocumentFile.fromTreeUri(MainActivity.this, Uri.parse(uriString));
            } else if (uriString.startsWith("file:///")) {
                saveDir = DocumentFile.fromFile(FileUtils.getFileFromFileUriPath(uriString));
                System.out.println("getSaveDirNew() File Uri saveDir: " + saveDir.getUri().toString() + ", canWrite: " + saveDir.canWrite());
            } else {
                saveDir = DocumentFile.fromFile(new File(uriString));
                System.out.println("getSaveDirNew() Absolute Path: " + saveDir.getUri().toString() + ", canWrite: " + saveDir.canWrite());
            }

        } catch (Exception e) {
            System.out.println("getSaveDirNew() ERROR: " + e.getMessage());
        }

        if (saveDir == null) {
            return null;
        }
        System.out.println("getSaveDirNew() saveDir: " + saveDir);
        tvSAF1.setText("getSaveDirNew() uriString: " + uriString);
        tvSAF2.setText("getSaveDirNew() saveDir.getUri().toString(): " + saveDir.getUri().toString() + ", canWrite: "
                + saveDir.canWrite() + ", exist: " + saveDir.exists() + ", isDirectory: " + saveDir.isDirectory() + ", isFile: " + saveDir.isFile());

        return saveDir;
    }

    @TargetApi(19)
    protected DocumentFile getSaveDirMainMemory() {
        DocumentFile saveDir = null;
        saveDir = DocumentFile.fromFile(Environment.getExternalStorageDirectory());

        // saveDir =
        // DocumentFile.fromFile(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM));
        // saveDir =
        // DocumentFile.fromFile(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES));
        // saveDir = DocumentFile.fromTreeUri(this, Uri.parse(currentFolder));

        boolean isUriWritePermission = false;
        String uriString = saveDir.getUri().toString();
        isUriWritePermission = isUriWritePermission(uriString);
        Toast.makeText(MainActivity.this,
                "getSaveDirMain() uriString: " + uriString + ", CANWRITE: " + isUriWritePermission, Toast.LENGTH_LONG)
                .show();
        /**
         * *** CREATE SUB-FOLDERS ***
         */
        DocumentFile newDir = null;
        /*
         * Check or create Main Folder
		 */

        // Check if main folder exist
        newDir = saveDir.findFile(DIR_MAIN);

        // Main folder exists
        if (newDir != null && newDir.exists()) {
            Toast.makeText(this, DIR_MAIN + " Folder exist!", Toast.LENGTH_LONG).show();
        }

        // Folder does not exist, create it
        if (newDir == null || !newDir.exists()) {
            Toast.makeText(this, "Creating " + DIR_MAIN + " Folder", Toast.LENGTH_LONG).show();
            newDir = saveDir.createDirectory(DIR_MAIN);
        }
        /*
         * Check or create Sub-Folder
		 */
        DocumentFile newSubDir = null;

        // Check if sub-folder exist
        newSubDir = newDir.findFile(DIR_SUB);

        // Sub-folder exists
        if (newSubDir != null && newSubDir.exists()) {
            Toast.makeText(this, DIR_SUB + " Folder exist!", Toast.LENGTH_LONG).show();
        }

        // Folder does not exist, create it
        if (newSubDir == null || !newSubDir.exists()) {
            Toast.makeText(this, "Creating " + DIR_SUB + " Folder", Toast.LENGTH_LONG).show();
            newSubDir = newDir.createDirectory(DIR_SUB);
        }
        tvSAF1.setText("getSaveDirMain() saveDir.getUri().toString(): " + saveDir.getUri().toString()
                + ", uriWritePermission: " + isUriWritePermission + ", saveDir.canWrite(): " + saveDir.canWrite()
                + ", exist: " + saveDir.exists() + ", isDirectory: " + saveDir.isDirectory());
        if (newSubDir != null) {
            tvSAF2.setText("getSaveDirMain() newSubDir.getUri().toString(): " + newSubDir.getUri().toString()
                    + ", newDir.canWrite(): " + newSubDir.canWrite() + ", exist: " + newSubDir.exists()
                    + ", isDirectory: " + newSubDir.isDirectory());
        }
        if (newSubDir != null && newSubDir.exists()) {
            return newSubDir;
        } else if (newDir != null && newDir.exists()) {
            return newDir;
        } else {
            return saveDir;
        }

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
            System.out.println("isUriWritePermission() Persisted Permissions: " + p.getUri().toString() + ", persisted date: " + new Date(p.getPersistedTime()));
            if (p.getUri().toString().equals(uriString) && p.isWritePermission()) {
                System.out.println("isUriWritePermission() MATCHED Uri: " + p.toString());
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
                    currentFolder = FileUtils.getFileFromDocumentFile(saveDir).toString();
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
