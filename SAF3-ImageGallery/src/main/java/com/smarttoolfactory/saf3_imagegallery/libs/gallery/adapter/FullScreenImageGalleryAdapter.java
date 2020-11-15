package com.smarttoolfactory.saf3_imagegallery.libs.gallery.adapter;

import java.util.List;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.smarttoolfactory.saf3_imagegallery.R;
import com.smarttoolfactory.saf3_imagegallery.libs.gallery.view.TouchImageView;
import com.smarttoolfactory.saf3_imagegallery.libs.utils.DisplayUtils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.palette.graphics.Palette;
import androidx.viewpager.widget.PagerAdapter;

public class FullScreenImageGalleryAdapter extends PagerAdapter {

	private final List<String> images;

	public FullScreenImageGalleryAdapter(List<String> images) {
		this.images = images;
	}

	@Override
	public Object instantiateItem(ViewGroup container, int position) {
		LayoutInflater inflater = (LayoutInflater) container.getContext()
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		View view = inflater.inflate(R.layout.gallery_pager_adapter_fullscreen_image, null);

		TouchImageView iv = (TouchImageView) view.findViewById(R.id.iv);
		final LinearLayout linearLayout = (LinearLayout) view.findViewById(R.id.ll);

		String image = images.get(position);

		final Context context = iv.getContext();

		int width = DisplayUtils.getScreenWidth(context);
		int height = DisplayUtils.getScreenHeight(context);
		// Toast.makeText(this, "Activity loadFullScreenImage() width " + width + ",
		// height " + height, Toast.LENGTH_SHORT).show();
		LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(width, height);
		iv.setLayoutParams(layoutParams);
		iv.requestLayout();

		Glide.with(context).load(image).listener(new RequestListener<Drawable>() {

			@Override
			public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target,
					DataSource dataSource, boolean isFirstResource) {

				// TODO Set Background Color with Palette
				Bitmap bitmap = ((BitmapDrawable) resource).getBitmap();
				Palette.from(bitmap).generate(new Palette.PaletteAsyncListener() {
					public void onGenerated(Palette palette) {
						Palette.Swatch vibrantSwatch = palette.getVibrantSwatch();
						if (vibrantSwatch != null) {
							linearLayout.setBackgroundColor(vibrantSwatch.getRgb());
						}
					}
				});
				return false;
			}

			@Override
			public boolean onLoadFailed(GlideException e, Object model, Target<Drawable> target,
					boolean isFirstResource) {
				return false;
			}
		}).into(iv);

		container.addView(view, 0);
		return view;
	}

	@Override
	public int getCount() {
		return images.size();
	}

	@Override
	public void destroyItem(ViewGroup container, int position, Object object) {
		container.removeView((View) object);
	}

	@Override
	public boolean isViewFromObject(View view, Object object) {
		return view == object;
	}

}
