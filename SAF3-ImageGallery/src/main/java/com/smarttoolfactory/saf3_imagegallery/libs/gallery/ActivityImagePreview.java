
package com.smarttoolfactory.saf3_imagegallery.libs.gallery;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.documentfile.provider.DocumentFile;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.smarttoolfactory.saf3_imagegallery.R;
import com.smarttoolfactory.saf3_imagegallery.config.Config;
import com.smarttoolfactory.saf3_imagegallery.libs.gallery.view.TouchImageView;
import com.smarttoolfactory.saf3_imagegallery.libs.utils.DisplayUtils;
import com.smarttoolfactory.saf3_imagegallery.libs.utils.FileUtils;

import java.io.File;
import java.util.List;

public class ActivityImagePreview extends AppCompatActivity {

    private TouchImageView touchImageView;
    private String imagePath = "";
    private boolean fullscreen = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_gallery_preview);
        setTitle("");

        touchImageView = (TouchImageView) findViewById(R.id.touchImageView);

        // TODO Set touchImageView size, without this it's width and height are 0
        int width = DisplayUtils.getScreenWidth(this);
        int height = DisplayUtils.getScreenHeight(this);
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(width, height);
        touchImageView.setLayoutParams(layoutParams);

        Intent intent = getIntent();
        imagePath = intent.getStringExtra(Config.GALLERY_IMAGE_PATH);
        setImageView();

        touchImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fullscreen = !fullscreen;
                setFullScreen(fullscreen);
            }
        });

    }

    private void setImageView() {

        Glide.with(this).clear(touchImageView);
        RequestOptions options = new RequestOptions();
        options.diskCacheStrategy(DiskCacheStrategy.NONE).skipMemoryCache(true);

        Glide.with(this)
                .load(imagePath)
                .addListener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable @org.jetbrains.annotations.Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        return false;
                    }
                })
                .apply(options)
                .into(touchImageView);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_gallery_image_fullscreen, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_option_ok:
                finish();
                break;
            case R.id.menu_option_edit:
                Intent editIntent = new Intent(Intent.ACTION_EDIT);

                editIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                editIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

                // Is it Content Uri or File Absolute Path
                if (FileUtils.isContentUri(imagePath)) {

                    Uri uri = Uri.parse(imagePath);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                        editIntent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
                    }

                    // Get Permission for Uri
                    List<ResolveInfo> resInfoList = getPackageManager().queryIntentActivities(editIntent,
                            PackageManager.MATCH_DEFAULT_ONLY);
                    for (ResolveInfo resolveInfo : resInfoList) {
                        String packageName = resolveInfo.activityInfo.packageName;
                        grantUriPermission(packageName, uri,
                                Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    }

                    editIntent.setDataAndType(uri, "image/*");

                } else {
                    editIntent.setDataAndType(Uri.fromFile(new File(imagePath)), "image/*");
                }

                // editIntent.setType("image/*");
                editIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                // editIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivityForResult((Intent.createChooser(editIntent, null)), Config.REQUEST_GALLERY_EDIT);
                break;

            case R.id.menu_option_delete:
                AlertDialog.Builder alertBuild = new AlertDialog.Builder(ActivityImagePreview.this);
                alertBuild.setTitle(getString(R.string.gallery_menu_delete_image)).setCancelable(false)
                        .setMessage(getString(R.string.gallery_menu_delete_image_message)).setIcon(R.mipmap.ic_launcher)
                        .setPositiveButton(getString(R.string.export_yes), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                boolean isFileDeleted = false;
                                if (FileUtils.isContentUri(imagePath)) {
                                    DocumentFile documentFile = DocumentFile.fromSingleUri(getApplicationContext(),
                                            Uri.parse(imagePath));
                                    Toast.makeText(getApplicationContext(), "Content Uri image delete()", Toast.LENGTH_LONG)
                                            .show();
                                    if (documentFile.exists()) {
                                        isFileDeleted = documentFile.delete();
                                    }
                                } else {
                                    File file = new File(imagePath);
                                    if (file.exists()) {
                                        isFileDeleted = file.delete();
                                    }
                                }

                                if (isFileDeleted) {
                                    endPreview();
                                } else {
                                    Toast.makeText(getApplicationContext(), R.string.gallery_file_not_deleted,
                                            Toast.LENGTH_SHORT).show();
                                }

                            }

                            private void endPreview() {
                                Toast.makeText(getApplicationContext(), R.string.gallery_file_deleted, Toast.LENGTH_SHORT)
                                        .show();
                                touchImageView.setImageBitmap(null);
                                setResult(Config.RESULT_IMAGE_DELETED);

                                // Wait 500 ms and finish activity
                                new Handler().postDelayed(new Runnable() {

                                    @Override
                                    public void run() {
                                        finish();
                                    }
                                }, 500);
                            }
                        }).setNegativeButton(getString(R.string.export_no), new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                AlertDialog alert = alertBuild.create();
                alert.show();

                break;
            case R.id.menu_option_share:
                try {
                    Intent share = new Intent(Intent.ACTION_SEND);
                    share.setType("image/*");
                    share.putExtra(Intent.EXTRA_STREAM, Uri.parse(imagePath));
                    startActivity(share);
                } catch (NullPointerException e) {
                    Toast.makeText(getApplicationContext(), R.string.gallery_file_not_available, Toast.LENGTH_SHORT).show();
                }
                break;

            case android.R.id.home:
                finish();
                break;
        }
        // TODO If returns false, previous Activity's onActivityResult() is not invoked
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Config.REQUEST_GALLERY_EDIT && resultCode == RESULT_OK) {
            setImageView();
//            new Handler().postDelayed(new Runnable() {
//                @Override
//                public void run() {
//                    setImageView();
//                }
//            }, 700);
        }
    }


    private void setFullScreen(boolean fullscreen) {
        if (fullscreen) {

        } else {

        }

    }
}
