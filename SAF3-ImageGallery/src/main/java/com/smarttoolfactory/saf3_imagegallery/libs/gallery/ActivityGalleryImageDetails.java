package com.smarttoolfactory.saf3_imagegallery.libs.gallery;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.smarttoolfactory.saf3_imagegallery.R;
import com.smarttoolfactory.saf3_imagegallery.config.Config;
import com.smarttoolfactory.saf3_imagegallery.libs.exif.ExifManager;
import com.smarttoolfactory.saf3_imagegallery.libs.utils.FileUtils;
import com.smarttoolfactory.saf3_imagegallery.libs.utils.ImageUtils;
import android.content.Intent;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.documentfile.provider.DocumentFile;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Map;

public class ActivityGalleryImageDetails extends AppCompatActivity {
    // Exif data
    private Map<String, String> exifData;
    /*
    * Views
     */
    private RelativeLayout layoutContainer;
    private ImageView ivImageDetails;
    private String imagePathUri, dateFormat;
    // Measurement
    private TextView tvAngle, tvPitch, tvRoll, tvAzimuth, tvBering;
    // Image
    private TextView tvPath, tvName, tvDetails, tvCaptureDate, tvOrientation;
    // Camera
    private TextView tvMaker, tvModel, tvSoftware;
    // Advanced
    private TextView tvFocalLength, tvAperture, tvExposureTime, tvISO;
    // User
    private TextView tvArtist, tvCopyright, tvDescription, tvComment;

    /*
    * Exif Data Strings
     */

    // Image
    String stringCaptureDate, stringOrientation;
    private String stringWidth, stringLength, stringSize, stringName;

    // Camera
    String stringMaker, stringModel, stringSoftware;
    // Advanced
    String stringFocalLength, stringAperture, stringShutterSpeed, stringExposureTime, stringISO;
    // User Entered
    String stringArtist, stringCopyright, stringDescription, stringComment;

    private float focalLength, aperture, shutterSpeed, exposureTime, ISO;


    private boolean isApi24 = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery_preview_details);

        isApi24 = Build.VERSION.SDK_INT >= 24 ? true : false;

        // Get  image path/uri from Intent
        Intent intent = getIntent();
        imagePathUri = intent.getStringExtra(Config.GALLERY_IMAGE_PATH);
        dateFormat = Config.DATE_FORMAT_DISPLAY;
        // Bind views from xml
        bindViews();
        // Load image into ImageView
        loadImage();
        getExifData();
        setExifData();


    }

    private void bindViews() {
        /*
         * Set toolbar and arrow icon to return back
		 */
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbarGalleryDetails);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        layoutContainer = (RelativeLayout) findViewById(R.id.rlGalleryPrevDetails);
        ivImageDetails = (ImageView) findViewById(R.id.ivImageDetails);
        ivImageDetails.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intentPreview = new Intent(ActivityGalleryImageDetails.this, ActivityImagePreview.class);
                intentPreview.putExtra(Config.GALLERY_IMAGE_PATH, imagePathUri);
                startActivityForResult(intentPreview, Config.REQUEST_IMAGE_PREVIEW);
            }
        });

        /*
        * Textviews
         */
        tvAngle = (TextView) findViewById(R.id.tvAngle);
        tvPitch = (TextView) findViewById(R.id.tvPitch);
        tvRoll = (TextView) findViewById(R.id.tvRoll);
        tvAzimuth = (TextView) findViewById(R.id.tvAzimuth);
        tvBering = (TextView) findViewById(R.id.tvBeaering);

        // Image
        tvPath = (TextView) findViewById(R.id.tvPath);
        tvName = (TextView) findViewById(R.id.tvName);
        tvDetails = (TextView) findViewById(R.id.tvDetails);
        tvCaptureDate = (TextView) findViewById(R.id.tvCaptureDate);
        tvOrientation = (TextView) findViewById(R.id.tvOrientation);
        // Camera
        tvMaker = (TextView) findViewById(R.id.tvMaker);
        tvModel = (TextView) findViewById(R.id.tvModel);
        tvSoftware = (TextView) findViewById(R.id.tvSoftware);
        // Advanced
        tvFocalLength = (TextView) findViewById(R.id.tvFocalLength);
        tvAperture = (TextView) findViewById(R.id.tvAperture);
        tvExposureTime = (TextView) findViewById(R.id.tvExposureTime);
        tvISO = (TextView) findViewById(R.id.tvImageISO);
        // User
        tvArtist = (TextView) findViewById(R.id.tvArtist);
        tvCopyright = (TextView) findViewById(R.id.tvCopyright);
        tvDescription = (TextView) findViewById(R.id.tvDescription);
        tvComment = (TextView) findViewById(R.id.tvComment);
    }


    private void getExifData() {
        if (FileUtils.isContentUri(imagePathUri)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {

                try {
                    InputStream inputStream = getContentResolver().openInputStream(Uri.parse(imagePathUri));
                    exifData = ExifManager.getExifDataMap(inputStream);
                    inputStream.close();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                exifData = ExifManager.getExifDataMap(getApplicationContext(), imagePathUri);
            }
        } else {

            exifData = ExifManager.getExifDataMap(imagePathUri);
        }
    }

    private void setExifData() {

        tvAngle.setText("-");
        tvPitch.setText("-");
        tvRoll.setText("-");
        tvAzimuth.setText("-");
        tvBering.setText("-");

       /*
        * Set Strings
        */

        stringCaptureDate = exifData.get(androidx.exifinterface.media.ExifInterface.TAG_DATETIME);
        stringOrientation = exifData.get(ExifInterface.TAG_ORIENTATION);
        stringWidth = exifData.get(ExifInterface.TAG_IMAGE_WIDTH);
        stringLength = exifData.get(ExifInterface.TAG_IMAGE_LENGTH);

        /*
        * Get data from physical file
         */

        if (FileUtils.isContentUri(imagePathUri)) {
            DocumentFile documentFile = DocumentFile.fromSingleUri(this, Uri.parse(imagePathUri));
            if (documentFile != null && documentFile.exists()) {
                stringName = documentFile.getName();
                // Set image size in kb or mb
                long length = documentFile.length() / 1024;
                String unit = "kb";
                if (length > 1000) {
                    length /= 1024;
                    unit = "mb";
                }
                stringSize = length + unit;

                // If image dimensions and time is null from exif get from uri
                ImageUtils.Size size = null;
                if (stringWidth == null || stringLength == null) {
                    ImageUtils.getImageDimensions(this, Uri.parse(imagePathUri));
                }

                if (stringWidth == null && size != null) {
                    stringWidth = "" + size.width;
                }

                if (stringLength == null && size != null) {
                    stringLength = "" + size.height;
                }

                if (stringCaptureDate == null) {
                    SimpleDateFormat sdf = new SimpleDateFormat(dateFormat, Locale.ROOT);
                    stringCaptureDate = sdf.format(documentFile.lastModified());
                }
            }
        } else {
            File file = new File(imagePathUri);
            if (file != null && file.exists()) {
                stringName = file.getName();
                // Set image size in kb or mb
                long length = file.length() / 1024;
                String unit = "kb";
                if (length > 1000) {
                    length /= 1024;
                    unit = "mb";
                }
                stringSize = length + unit;

                // If image dimensions and time is null from exif get from uri
                ImageUtils.Size size = null;
                if (stringWidth == null || stringLength == null) {
                    ImageUtils.getImageDimensions(this, Uri.parse(imagePathUri));
                }

                if (stringWidth == null && size != null) {
                    stringWidth = "" + size.width;
                }

                if (stringLength == null && size != null) {
                    stringLength = "" + size.height;
                }

                if (stringCaptureDate == null) {
                    SimpleDateFormat sdf = new SimpleDateFormat(dateFormat, Locale.ROOT);
                    stringCaptureDate = sdf.format(file.lastModified());
                }
            }
        }


        if (stringOrientation != null) {
            int orientation = ExifInterface.ORIENTATION_UNDEFINED;
            try {
                orientation = Integer.parseInt(stringOrientation);
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
            switch (orientation) {
                case ExifInterface.ORIENTATION_UNDEFINED:
                    stringOrientation = getString(R.string.gallery_undefined);
                    break;
                case ExifInterface.ORIENTATION_NORMAL:
                    stringOrientation = getString(R.string.gallery_horizontal_normal);
                    break;
                case ExifInterface.ORIENTATION_ROTATE_90:
                    stringOrientation = getString(R.string.gallery_rotate90cw);
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    stringOrientation = getString(R.string.gallery_rotate180cw);
                    break;
                case ExifInterface.ORIENTATION_FLIP_HORIZONTAL:
                    stringOrientation = getString(R.string.gallery_mirror_horizontal);
                    break;
                case ExifInterface.ORIENTATION_FLIP_VERTICAL:
                    stringOrientation = getString(R.string.gallery_mirror_vertical);
                    break;
                case ExifInterface.ORIENTATION_TRANSPOSE:
                    stringOrientation = getString(R.string.gallery_mirror_horizontal_90);
                    break;
                case ExifInterface.ORIENTATION_TRANSVERSE:
                    stringOrientation = getString(R.string.gallery_mirror_horizontal_270);
                    break;
            }
        }

        stringMaker = exifData.get(ExifInterface.TAG_MAKE);
        stringModel = exifData.get(ExifInterface.TAG_MODEL);
        stringSoftware = exifData.get(ExifInterface.TAG_SOFTWARE);


        /*
        * **** ADVANCED ****
         */
        stringFocalLength = exifData.get(ExifInterface.TAG_FOCAL_LENGTH);
        if (stringFocalLength != null) {
            if (stringFocalLength.contains("/")) {
                try {
                    stringFocalLength = stringFocalLength.substring(0, stringFocalLength.indexOf("/"));
                    focalLength = Float.parseFloat(stringFocalLength);
                    stringFocalLength = focalLength / 1000f + "mm";

                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
            }
        }
        stringExposureTime = exifData.get(ExifInterface.TAG_EXPOSURE_TIME);
        if (stringExposureTime != null) {
            try {
                exposureTime = Float.parseFloat(stringExposureTime);
                int exposureTimeInv = Math.round((1 / exposureTime));
                stringExposureTime = "1/" + exposureTimeInv + " (" + exposureTime + "s)";
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }

        if (isApi24) {
            stringAperture = exifData.get(ExifInterface.TAG_APERTURE_VALUE);
            stringISO = exifData.get(ExifInterface.TAG_ISO_SPEED_RATINGS);

        } else {
            stringAperture = exifData.get(ExifInterface.TAG_APERTURE);
            stringISO = exifData.get(ExifInterface.TAG_ISO);
        }

        if (stringAperture != null) {
            if (stringAperture.contains("/")) {
                try {
                    stringAperture = stringAperture.substring(0, stringAperture.indexOf("/"));
                    aperture = Float.parseFloat(stringAperture);
                    stringAperture = "f/" + aperture / 100f;

                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
            }
        }

        // User Entered
        stringArtist = exifData.get(ExifInterface.TAG_ARTIST);
        stringCopyright = exifData.get(ExifInterface.TAG_COPYRIGHT);
        stringDescription = exifData.get(ExifInterface.TAG_IMAGE_DESCRIPTION);
        stringComment = exifData.get(ExifInterface.TAG_USER_COMMENT);



        /*
        * Add data to views
         */
        // Image
        if (FileUtils.isContentUri(imagePathUri)) {
            tvPath.setText(FileUtils.getPath(this, Uri.parse(imagePathUri)));
        } else {
            tvPath.setText(imagePathUri);
        }

        if (stringName != null) {
            tvName.setText(stringName);
        }

        if (stringCaptureDate != null) {
            tvCaptureDate.setText(stringCaptureDate);
        }

        if (stringSize != null) {
            tvDetails.setText(stringCaptureDate + " | " + stringSize + " | " + stringWidth + "x" + stringLength);
        }

        if (stringOrientation != null) {
            tvOrientation.setText(stringOrientation);
        }


        // Camera
        if (stringMaker != null) {
            tvMaker.setText(stringMaker);
        }

        if (stringModel != null) {
            tvModel.setText(stringModel);
        }

        if (stringSoftware != null) {
            tvSoftware.setText(stringSoftware);
        }

        // Advanced
        if (stringAperture != null) {
            tvAperture.setText(stringAperture);
        }

        if (stringExposureTime != null) {
            tvExposureTime.setText(stringExposureTime);
        }

        if (stringISO != null) {
            tvISO.setText(stringISO);
        }

        if (stringFocalLength != null) {
            tvFocalLength.setText(stringFocalLength);
        }

        // TODO Add Professional Tags and Attributes

        // User
        if (stringArtist != null) {
            tvArtist.setText(stringArtist);
        }

        if (stringCopyright != null) {
            tvCopyright.setText(stringCopyright);
        }

        if (stringDescription != null) {
            tvDescription.setText(stringDescription);
        }


        if (stringComment != null) {
            tvComment.setText(stringComment);
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Image is deleted
        if (requestCode == Config.REQUEST_IMAGE_PREVIEW) {
            setResult(resultCode);
            if (resultCode == Config.RESULT_IMAGE_DELETED) {
                finish();
            } else {
                // Image is not deleted, reload the image incase it's edited
                loadImage();
            }
        }
    }

    private void loadImage() {
        Glide.with(this).clear(ivImageDetails);
        RequestOptions options = new RequestOptions();
        options.diskCacheStrategy(DiskCacheStrategy.NONE).skipMemoryCache(true);
        Glide.with(this).load(imagePathUri).apply(options).into(ivImageDetails);
    }

}
