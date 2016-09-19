package com.test.bootanimation;

import java.io.File;

import android.graphics.Bitmap;

public class BootFileInfo {
	private File mBootFile;
	private String mName;
	private Bitmap mPreviewBitmap;
	private boolean mIsCurrent;

	public BootFileInfo(String name, File bootFile, Bitmap bitmap, boolean isCurrent) {
		mName = name;
		mBootFile = bootFile;
		mPreviewBitmap = bitmap;
		mIsCurrent = isCurrent;
	}

	public Bitmap getPreviewBitmap() {
		return mPreviewBitmap;
	}

	public String getName() {
		return mName;
	}

	public File getBootFile() {
		return mBootFile;
	}
	
	public boolean isCurrent() {
		return mIsCurrent;
	}

	@Override
	public String toString() {
		return "Name: "
				+ mName
				+ "\nBoot File: "
				+ (mBootFile != null ? mBootFile.getAbsolutePath() : "null")
				+ "\nPreview bitmap: "
				+ mPreviewBitmap;
	}
}
