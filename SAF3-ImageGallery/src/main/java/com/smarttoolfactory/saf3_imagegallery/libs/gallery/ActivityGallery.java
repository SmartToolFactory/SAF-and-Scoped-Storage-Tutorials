package com.smarttoolfactory.saf3_imagegallery.libs.gallery;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.documentfile.provider.DocumentFile;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.smarttoolfactory.saf3_imagegallery.R;
import com.smarttoolfactory.saf3_imagegallery.config.Config;
import com.smarttoolfactory.saf3_imagegallery.libs.gallery.adapter.GalleryListAdapter;
import com.smarttoolfactory.saf3_imagegallery.libs.gallery.model.GalleryItem;
import com.smarttoolfactory.saf3_imagegallery.libs.utils.FileUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/* Description: Gallery with details and image preview
 * Select gallery directory using options or reset dir to default folder in Config.java
 */
@SuppressLint("NewApi")
public class ActivityGallery extends AppCompatActivity implements GalleryListAdapter.OnRecyclerViewItemClickListener {
    private static final int REQUEST_IMAGE_DIR_SELECTION = 100;

    // Views
    private RecyclerView mRecyclerView;
    private GalleryListAdapter mAdapter;
    private Toolbar toolbar;
    private RelativeLayout layoutContainer;

    // Gallery Data
    private List<GalleryItem> listGalleryItems;
    private ArrayList<String> listImagePaths;

    private String imageSaveUriString = "";
    private SharedPreferences mSharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);

        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        imageSaveUriString = mSharedPreferences.getString(getString((R.string.key_image_save_uri)), imageSaveUriString);
        listGalleryItems = new ArrayList<>();
        listImagePaths = new ArrayList<>();
        setUpGalleryData();
        bindViews();

    }

    private void setUpGalleryData() {
        listGalleryItems.clear();
        listImagePaths.clear();

        if (FileUtils.isContentUri(imageSaveUriString)) {
            DocumentFile dir = DocumentFile.fromTreeUri(getApplicationContext(), Uri.parse(imageSaveUriString));
            DocumentFile[] documentFiles = dir.listFiles();

            if (dir != null && documentFiles != null && documentFiles.length > 0) {
                for (int i = 0; i < documentFiles.length; i++) {
                    if (documentFiles[i].getUri().toString().toLowerCase().endsWith(Config.IMAGE_FORMAT_JPEG)
                            || documentFiles[i].getUri().toString().toLowerCase().endsWith(Config.IMAGE_FORMAT_PNG)) {
                        GalleryItem galleryItem = new GalleryItem();
                        galleryItem.setName(documentFiles[i].getName());
                        galleryItem.setSize(documentFiles[i].length());
                        galleryItem.setPath(documentFiles[i].getUri().toString());
                        galleryItem.setDateLastModified(documentFiles[i].lastModified());

                        listGalleryItems.add(galleryItem);
                        listImagePaths.add(galleryItem.getPath());
                    }
                }
            }
        } else {

            File dir = new File(Environment.getExternalStorageDirectory(), Config.APP_DIRECTORY);
            File[] files = dir.listFiles();

            if (dir != null && files != null && files.length > 0) {

                for (int i = 0; i < files.length; i++) {
                    if (files[i].getAbsolutePath().endsWith(".jpg") || files[i].getAbsolutePath().endsWith(".png")) {

                        GalleryItem galleryItem = new GalleryItem();
                        galleryItem.setName(files[i].getName());
                        galleryItem.setSize(files[i].length());
                        galleryItem.setPath(files[i].getAbsolutePath());
                        galleryItem.setDateLastModified(files[i].lastModified());

                        listGalleryItems.add(galleryItem);
                        listImagePaths.add(galleryItem.getPath());
                    }
                }
            }
        }
    }

    private void bindViews() {
        /*
         * Set toolbar and arrow icon to return back
         */
        toolbar = (Toolbar) findViewById(R.id.toolbarGallery);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Set title to current directory
        String title = null;
        if (FileUtils.isContentUri(imageSaveUriString)) {
            DocumentFile documentFile = DocumentFile.fromTreeUri(getApplicationContext(),
                    Uri.parse(imageSaveUriString));
            title = FileUtils.getPath(getApplicationContext(), documentFile.getUri());
        }

        if (title != null) {
            getSupportActionBar().setTitle(title);
        }

        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerviewGallery);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mAdapter = new GalleryListAdapter(this, listGalleryItems);
        mAdapter.setClickListener(this);
        mRecyclerView.setAdapter(mAdapter);

    }

//

    /**
     * Starts a new intent to select custom folder to save image to
     */
    private void selectCustomFolder() {
        if (Build.VERSION.SDK_INT >= 21) {
            // TODO ACTION_OPEN_DOCUMENT_TREE is for choosing folder
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                    | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION | Intent.FLAG_GRANT_PREFIX_URI_PERMISSION);
            startActivityForResult(intent, REQUEST_IMAGE_DIR_SELECTION);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // selectDirectory() invoked
        if (requestCode == REQUEST_IMAGE_DIR_SELECTION && resultCode == RESULT_OK) {
            // Valid directory using SAF is choosen
            if (data.getData() != null) {
                Uri treeUri = data.getData();

                final int takeFlags = data.getFlags()
                        & (Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                // Check for the freshest data.
                if (Build.VERSION.SDK_INT >= 19) {
                    getContentResolver().takePersistableUriPermission(treeUri, takeFlags);
                }

                imageSaveUriString = treeUri.toString();

                Editor editor = mSharedPreferences.edit();
                editor.putString(getString(R.string.key_image_save_uri), imageSaveUriString);
                editor.commit();

                imageSaveUriString = mSharedPreferences.getString(getString((R.string.key_image_save_uri)),
                        imageSaveUriString);

                // Get Image info from new directory
                setUpGalleryData();
                // Notify RecyclerView about data changed
                if (mAdapter != null) {
                    mAdapter.notifyDataSetChanged();
                }
            }
        }
        // Returned RESULT_IMAGE_DELETED(image deleted) from Image Details
        if (requestCode == Config.REQUEST_IMAGE_DETAILS && resultCode == Config.RESULT_IMAGE_DELETED) {
            // Get Image info from new directory
            setUpGalleryData();
            // Notify RecyclerView about data changed
            if (mAdapter != null) {
                mAdapter.notifyDataSetChanged();
            }
        }

    }

    @Override
    public void onItemClicked(View view, int position) {
        // TODO Fullscreen ViewPager Activity
        // Intent intentFullscreen = new Intent(ActivityGallery.this,
        // ActivityGalleryImageFullscreen.class);
        // intentFullscreen.putStringArrayListExtra(ActivityGalleryImageFullscreen.KEY_IMAGES,
        // listImagePaths);
        // intentFullscreen.putExtra(ActivityGalleryImageFullscreen.KEY_POSITION,
        // position);
        // startActivityForResult(intentFullscreen,REQUEST_IMAGE_DETAILS);

        // TODO Image Preview
        // Intent intentPreview = new Intent(ActivityGallery.this,
        // ActivityImagePreview.class);
        // intentPreview.putExtra(Config.URI_CONTENT, listImagePaths.get(position));
        // startActivityForResult(intentPreview,REQUEST_IMAGE_DETAILS);

        // TODO Image Preview With TouchImageView
        // Intent intentPreview = new Intent(ActivityGallery.this,
        // ActivityImagePreview.class);
        // intentPreview.putExtra(Config.URI_CONTENT, listImagePaths.get(position));
        // startActivityForResult(intentPreview, REQUEST_IMAGE_DETAILS);

        Intent intentImageDetails = new Intent(ActivityGallery.this, ActivityGalleryImageDetails.class);
        intentImageDetails.putExtra(Config.GALLERY_IMAGE_PATH, listImagePaths.get(position));

        startActivityForResult(intentImageDetails, Config.REQUEST_IMAGE_DETAILS);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_gallery, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();
        switch (id) {
            case R.id.menu_option_reset_dir:
                imageSaveUriString = "";
                Editor editor = mSharedPreferences.edit();
                editor.putString(getString(R.string.key_image_save_uri), imageSaveUriString);
                editor.commit();

                imageSaveUriString = mSharedPreferences.getString(getString((R.string.key_image_save_uri)),
                        imageSaveUriString);

                // Get Image info from new directory
                setUpGalleryData();
                // Notify RecyclerView about data changed
                if (mAdapter != null) {
                    mAdapter.notifyDataSetChanged();
                }

                break;
            case R.id.menu_option_select_dir:
                selectCustomFolder();

                break;

            default:
                break;
        }

        return true;
    }
}
