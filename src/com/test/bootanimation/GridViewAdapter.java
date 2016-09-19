package com.test.bootanimation;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

public class GridViewAdapter extends BaseAdapter {
	private static final String TAG = "DirectoryListAdapter";
	private static final boolean DEBUG = true;

	private Context mContext;
	private LayoutInflater mInflater;
	private ArrayList<BootFileInfo> mList;
	private Resources mResources;
	private Bitmap mCurrentBitmap;

	private int mCurrentSelect = -1;
	private int mPreviewBitmapWidth;
	private int mPreviewBitmapHeight;

	public GridViewAdapter(Context context, ArrayList<BootFileInfo> list) {
		mContext = context;
		mResources = mContext.getResources();
		mList = list;
		mInflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mPreviewBitmapWidth = mResources
				.getInteger(R.integer.preview_picture_width);
		mPreviewBitmapHeight = mResources
				.getInteger(R.integer.preview_picture_height);
		mCurrentBitmap = Utils.resizeBitmap(mPreviewBitmapWidth,
				mPreviewBitmapHeight,
				mResources.getDrawable(R.drawable.ic_current));
	}

	public void setCurrentSelect(int position) {
		mCurrentSelect = position;
		notifyDataSetChanged();
	}

	public int getCurrentSelect() {
		return mCurrentSelect;
	}

	@Override
	public int getCount() {
		return mList == null ? 0 : mList.size();
	}

	@Override
	public Object getItem(int position) {
		return mList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder = null;
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.adapter_grid_view, parent,
					false);
			holder = new ViewHolder();
			holder.mPreview = (ImageView) convertView
					.findViewById(R.id.preview);
			holder.mCurrentView = (ImageView) convertView
					.findViewById(R.id.current);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		BootFileInfo info = mList.get(position);
		holder.mPreview.setImageBitmap(info.getPreviewBitmap());
		holder.mCurrentView.setImageBitmap(mCurrentBitmap);
		if (info.isCurrent()) {
			holder.mCurrentView.setVisibility(View.VISIBLE);
		} else {
			holder.mCurrentView.setVisibility(View.GONE);
		}

		return convertView;
	}

	class ViewHolder {
		ImageView mPreview;
		ImageView mCurrentView;
	}
}
