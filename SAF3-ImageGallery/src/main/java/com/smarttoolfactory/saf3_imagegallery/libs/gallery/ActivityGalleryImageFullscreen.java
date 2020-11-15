package com.smarttoolfactory.saf3_imagegallery.libs.gallery;

import java.util.ArrayList;
import java.util.List;

import com.smarttoolfactory.saf3_imagegallery.R;
import com.smarttoolfactory.saf3_imagegallery.libs.gallery.adapter.FullScreenImageGalleryAdapter;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

public class ActivityGalleryImageFullscreen extends AppCompatActivity {

	public static final String KEY_IMAGES = "KEY_IMAGES";
	public static final String KEY_POSITION = "KEY_POSITION";

	// Views
	private Toolbar toolbar;
	private ViewPager viewPager;

	// Image Positions
	private int position;
	private List<String> images;

	private final ViewPager.OnPageChangeListener viewPagerOnPageChangeListener = new ViewPager.OnPageChangeListener() {
		@Override
		public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

		}

		@Override
		public void onPageSelected(int position) {

			if (viewPager != null) {
				viewPager.setCurrentItem(position);
				setActionBarTitle(position);
			}
		}

		@Override
		public void onPageScrollStateChanged(int state) {

		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_gallery_preview_pager_adapter);
		toolbar = (Toolbar) findViewById(R.id.toolbar);
		viewPager = (ViewPager) findViewById(R.id.viewpager);

		setSupportActionBar(toolbar);
		ActionBar actionBar = getSupportActionBar();
		if (actionBar != null)
			actionBar.setDisplayHomeAsUpEnabled(true);

		Intent intent = getIntent();
		if (intent != null) {
			Bundle extras = intent.getExtras();
			if (extras != null) {
				images = extras.getStringArrayList(KEY_IMAGES);
				position = extras.getInt(KEY_POSITION);
			}
		}
		setUpViewPager();

	}

	private void setActionBarTitle(int position) {
		if (viewPager != null && images.size() > 1) {
			int totalPages = viewPager.getAdapter().getCount();

			ActionBar actionBar = getSupportActionBar();
			if (actionBar != null) {
				actionBar.setTitle(String.format("%d/%d", (position + 1), totalPages));
			}
		}
	}

	private void setUpViewPager() {
		ArrayList<String> imageList = new ArrayList<>();
		imageList.addAll(images);

		FullScreenImageGalleryAdapter fullScreenImageGalleryAdapter = new FullScreenImageGalleryAdapter(imageList);
		// fullScreenImageGalleryAdapter.setFullScreenImageLoader(this);
		viewPager.setAdapter(fullScreenImageGalleryAdapter);
		viewPager.addOnPageChangeListener(viewPagerOnPageChangeListener);
		viewPager.setCurrentItem(position);

		setActionBarTitle(position);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		viewPager.removeOnPageChangeListener(viewPagerOnPageChangeListener);
	}
}
