package com.smarttoolfactory.saf3_imagegallery.libs.gallery.adapter;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.smarttoolfactory.saf3_imagegallery.R;
import com.smarttoolfactory.saf3_imagegallery.config.Config;
import com.smarttoolfactory.saf3_imagegallery.libs.gallery.model.GalleryItem;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

public class GalleryListAdapter extends RecyclerView.Adapter<GalleryListAdapter.MyViewHolder> {

	private LayoutInflater inflater;
	private List<GalleryItem> data = Collections.emptyList();
	private OnRecyclerViewItemClickListener recyclerClickListener;
	private Context mContext;
	private SimpleDateFormat sdf;
	private DecimalFormat mDecimalFormat;

	public GalleryListAdapter(Context context, List<GalleryItem> data) {
		mContext = context;
		inflater = LayoutInflater.from(context);
		this.data = data;
		sdf = new SimpleDateFormat(Config.DATE_FORMAT_DISPLAY, Locale.ROOT);
		mDecimalFormat = new DecimalFormat("#.##");

	}

	@Override
	public int getItemCount() {
		return data.size();
	}

	@Override
	public void onBindViewHolder(MyViewHolder holder, final int position) {
		GalleryItem galleryItem = data.get(position);
		String label = galleryItem.getName();
		int size = (int) (galleryItem.getSize() / (1024));
		String unit = "kb";
		if (size > 1000) {
			size /= 1024;
			unit = "mb";
		}
		Long date = galleryItem.getDateLastModified();

		RequestOptions options = new RequestOptions();
		options.diskCacheStrategy(DiskCacheStrategy.NONE).skipMemoryCache(true);
		Glide.with(mContext).load(galleryItem.getPath()).apply(options).listener(new RequestListener<Drawable>() {

			@Override
			public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target,
					DataSource dataSource, boolean isFirstResource) {
				return false;
			}

			@Override
			public boolean onLoadFailed(GlideException e, Object model, Target<Drawable> target,
					boolean isFirstResource) {
				return false;
			}
		})

				.into(holder.ivGallery);

		holder.tvGalleryName.setText(label);
		holder.tvGallerySize.setText(size + unit);
		holder.tvGalleryDate.setText("" + sdf.format(new Date(date)));
	}

	@Override
	public MyViewHolder onCreateViewHolder(ViewGroup parent, int arg1) {
		View view = null;

		view = inflater.inflate(R.layout.custom_row_gallery_card_view, parent, false);

		MyViewHolder viewHolder = new MyViewHolder(view);
		return viewHolder;
	}

	public void delete(int position) {
		data.remove(position);
		notifyItemRemoved(position);
	}

	class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
		// Views
		// private TextView tvTitle, tvNote, tvAngle, tvAzimuth, tvPitch, tvRoll,
		// tvBearing, tvDate;

		private ImageView ivGallery;
		private TextView tvGalleryName, tvGallerySize, tvGalleryDate;

		public MyViewHolder(View itemView) {
			super(itemView);
			ivGallery = itemView.findViewById(R.id.ivGallery);
			tvGalleryName = itemView.findViewById(R.id.tvGalleryName);
			tvGallerySize = itemView.findViewById(R.id.tvGallerySize);
			tvGalleryDate = itemView.findViewById(R.id.tvGalleryDate);
			itemView.setOnClickListener(this);
		}

		@Override
		public void onClick(View v) {
			if (recyclerClickListener != null) {
				recyclerClickListener.onItemClicked(v, getLayoutPosition());
			}
		}

	}

	/**
	 * get an instance of OnRecyclerViewClickListener interface
	 *
	 * @param recyclerClickListener
	 *            callback that is used by adapter to invoke the method of the class
	 *            implements the OnRecyclerViewClickListener interface
	 */
	public void setClickListener(OnRecyclerViewItemClickListener recyclerClickListener) {
		this.recyclerClickListener = recyclerClickListener;
	}

	/**
	 * RecyclerViewClickListener interface helps user to set a clickListener to the
	 * RecyclerView. By setting this listener, any item of Recycler View can respond
	 * to any interaction.
	 */
	public interface OnRecyclerViewItemClickListener {
		/**
		 * This is a callback method that be overriden by the class that implements this
		 * interface
		 */
		public void onItemClicked(View view, int position);
	}

}
