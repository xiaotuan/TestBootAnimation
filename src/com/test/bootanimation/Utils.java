package com.test.bootanimation;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.view.WindowManager;
import android.widget.Toast;
import android.util.Log;

public class Utils {
	private static final String TAG = "Utils";
	private static final boolean DEBUG = true;

	public static final String SECRET_CODE = "83998";
	public static final String SELECT_DIRECTORY_SECRET_CODE = "83997";
	public static final String DICTIONARY_SIZE = "17";

	public static final long LOGO_FILE_SIZE_LIMIT = (22 * 1024);
	public static final int BRAND_SIZE_LIMIT = 16;
	public static final int MODEL_SIZE_LIMIT = 16;

	public static final String KEY_IS_SETTING_RUNNING = "setting_running";
	public static final String ACTION_STATE = "com.trf.bootanimation.state";

	public static final String CONFIGE_DIRECTORY_EXTRA = "directory";
	public static final String BOOT_COMPLETED_EXTRA = "boot_completed";
	public static final String HOST_EXTRA = "host";
	public static final String FROM_ACTIVITY_EXTRA = "from_activity_extre";
	public static final String STATE_EXTRA = "state";
	public static final String LOG_EXTRA = "log";
	public static final String POWER_DOWN_ANIMATION_EXTRA = "power_down_animation";
	public static final String POWER_ON_ANIMATION_EXTRA = "power_up_animation";
	public static final String POWER_DOWN_RING_EXTRA = "power_down_ring";
	public static final String POWER_ON_RING_EXTRA = "power_up_ring";
	public static final String BRAND_EXTRA = "brand";
	public static final String MODEL_EXTRA = "model";
	public static final String DIALOG_TYPE_EXTRA = "dialog_type";

	public static final String DEFAULT_CONFIGE_DIRECTORY = "boot";
	public static final String CONFIGE_LIST_DIRECTORY = "bootList";
	public static final String COPY_DIRECTORY = "/mnt/custom_boot";
	public static final String CONFIGE_FILE_NAME = "confige.ini";
	public static final String POWER_ON_ANIMATION_FILE_NAME = "boot.zip";
	public static final String POWER_DOWN_ANIMATION_FILE_NAME = "shut.zip";
	public static final String POWER_ON_RING_FILE_NAME = "bootup.mp3";
	public static final String POWER_DOWN_RING_FILE_NAME = "shutdown.mp3";
	public static final String LOG_PICTURE_FILE_NAME = "logo.raw";
	public static final String PREVIEW_PICTURE_FILE_NAME = "preview.bmp";
	public static final String LOGO_PICTURE_TEMP_FILE_NAME = "logo";
	public static final String AUTO_RUN_KEY = "auto";
	public static final String BRAND_KEY = "brand";
	public static final String MODEL_KEY = "model";
	public static final String TYPE_FAIL = "fail_type";
	public static final String TYPE_INFO = "info_type";

	public static File getConfigeDirectory(Context context, String directory) {
		File result = null;
		if (DEBUG)
			Log.d(TAG, "getConfigeDirectory=>directory: " + directory);
		if (directory != null) {
			directory = directory.toLowerCase().trim();
			File bootList = getScreenResolutionDirectoryFile(context,
					getBootListDirectoryFile(context));
			if (bootList != null && bootList.exists() && bootList.isDirectory()) {
				File[] files = bootList.listFiles();
				for (File f : files) {
					if (f != null && f.exists() && f.isDirectory()) {
						String name = f.getName();
						name = name.toLowerCase().trim();
						if (DEBUG)
							Log.d(TAG, "getConfigeDirectory=>name: " + name);
						if (directory.equals(name)) {
							result = f;
							break;
						}
					}
				}
			}
		}

		if (DEBUG)
			Log.d(TAG, "getConfigeDirectory=>Directory: "
					+ (result == null ? "null" : result.getAbsolutePath()));
		return result;
	}

	public static File getBootListDirectoryFile(Context context) {
		File result = null;
		MountPointManager mpm = MountPointManager.getInstance();
		mpm.init(context);
		boolean isExistSdCard = mpm.isExistExternalStorage();
		if (DEBUG)
			Log.d(TAG, "getConfigeDirectory=>exist external storage: "
					+ isExistSdCard);
		if (isExistSdCard) {
			File external = mpm.getExternalStorageFile();
			if (DEBUG)
				Log.d(TAG,
						"getConfigeDirectory=>external path: "
								+ (external == null ? "null" : external
										.getAbsolutePath()));
			if (external != null && external.exists() && external.isDirectory()) {
				File[] files = external.listFiles();
				if (files != null && files.length > 0) {
					for (File f : files) {
						if (f != null && f.exists() && f.isDirectory()
								&& !f.isHidden()) {
							String name = f.getName();
							name = name.toLowerCase().trim();
							if (Utils.CONFIGE_LIST_DIRECTORY.toLowerCase()
									.trim().equals(name)) {
								result = f;
								break;
							}
						}
					}
				}
			}
		} else {
			//Toast.makeText(context, context.getString(R.string.no_sdcard),
			//		Toast.LENGTH_SHORT).show();
			Log.d(TAG, "getBootListDirectoryFile=>no sdcard.");
		}

		if (DEBUG)
			Log.d(TAG, "getBootListDirectoryFile=>result: " + result);
		return result;
	}

	public static File getScreenResolutionDirectoryFile(Context context,
			File bootFile) {
		File result = null;
		if (bootFile != null && bootFile.exists() && bootFile.isDirectory()) {
			WindowManager wm = (WindowManager) context
					.getSystemService(Context.WINDOW_SERVICE);
			int width = wm.getDefaultDisplay().getWidth();
			int height = wm.getDefaultDisplay().getHeight();
			if (height > 960 && height <= 1270) {
				height = 1270;
			} else if (height > 854 && height <= 960) {
				height = 960;
			} else if (height > 800 && height <= 854) {
				height = 854;
			}
			String directoryName1 = width + "x" + height;
			String directoryName2 = height + "x" + width;
			if (DEBUG)
				Log.d(TAG, "getScreenResolutionDirectoryFile=>name1: "
						+ directoryName1 + " name2: " + directoryName2);
			File[] files = bootFile.listFiles();
			if (files != null && files.length > 0) {
				for (File f : files) {
					if (f != null && f.exists() && f.isDirectory()) {
						String name = f.getName();
						name = name.toLowerCase().trim();
						if (DEBUG)
							Log.d(TAG,
									"getScreenResolutionDirectoryFile=>directory name: "
											+ name);
						if (name != null
								&& (name.equals(directoryName1) || name
										.equals(directoryName2))) {
							result = f;
							break;
						}
					}
				}
			}
		}

		if (DEBUG)
			Log.d(TAG, "getScreenResolutionDirectoryFile=>bootFile: "
					+ bootFile + " result: "
					+ (result == null ? "null" : result.getAbsolutePath()));
		return result;
	}

	public static boolean copyFileToDestinationDirectory(String copyDirectory,
			File... files) {
		boolean result = false;
		File destination = new File(copyDirectory);
		if (DEBUG)
			Log.d(TAG, "copyFileToDestinationDirectory=>destination: "
					+ destination);
		if (destination != null) {
			if (!destination.exists()) {
				destination.mkdirs();
			}
			if (destination != null && destination.exists()
					&& destination.isDirectory()) {
				for (File f : files) {
					if (f != null && f.isFile() && f.exists()) {
						result = copyFile(f.getAbsolutePath(), copyDirectory
								+ File.separator + f.getName(), false);
						if (DEBUG)
							Log.d(TAG, "copyFileToDestinationDirectory=>file: "
									+ f.getAbsolutePath() + " result: "
									+ result);
						if (!result) {
							break;
						}
					}
				}
			}
		}
		return result;
	}

	private static boolean copyFile(String path, String toPath, boolean isCut) {
		File file = new File(path);
		if (DEBUG)
			Log.d(TAG, "copyFile=>file: " + file + " exist: "
					+ (file == null ? "false" : file.exists()));
		if (file != null && file.exists()) {
			InputStream fosFrom = null;
			OutputStream fosTo = null;
			try {
				fosFrom = new FileInputStream(file);
				fosTo = new FileOutputStream(new File(toPath));

				byte bt[] = new byte[1024];
				int c;
				while ((c = fosFrom.read(bt)) > 0) {
					fosTo.write(bt, 0, c);
				}

				fosTo.flush();
				fosTo.close();
				fosFrom.close();
				fosTo = null;
				fosFrom = null;

				if (isCut) {
					file.delete();
				}
				return true;
			} catch (Exception e) {
				Log.d(TAG, "copyFile=>error: " + e.toString());
			} finally {
				try {
					if (fosTo != null) {
						fosTo.close();
						fosTo = null;
					}
				} catch (Exception e) {
				}
				try {
					if (fosFrom != null) {
						fosFrom.close();
						fosFrom = null;
					}
				} catch (Exception e) {
				}
			}
		}

		return false;
	}

	public static Bitmap resizeDefaultBitmap(Context context, int width,
			int height, int defaultId) {
		Bitmap bitmap = null;
		try {
			Bitmap tempBitmap = null;
			Drawable d = context.getResources().getDrawable(defaultId);
			if (d != null) {
				tempBitmap = ((BitmapDrawable) d).getBitmap();
			}

			if (tempBitmap != null) {
				bitmap = Bitmap.createBitmap(width, height,
						tempBitmap.getConfig());
				float sx = width / (float) tempBitmap.getWidth();
				float sy = height / (float) tempBitmap.getHeight();
				Canvas canvas = new Canvas(bitmap);
				canvas.scale(sx, sy);
				Paint paint = new Paint();
				paint.setAntiAlias(true);
				paint.setFilterBitmap(true);
				canvas.drawBitmap(tempBitmap, 0, 0, paint);
				tempBitmap.recycle();
			}
		} catch (Exception e) {
			Log.d(TAG, "getPreViewBitmap=>error: " + e.getMessage());
			bitmap = null;
		}
		if (DEBUG)
			Log.d(TAG, "getPreViewBitmap=>defaultId: " + defaultId + " bitmap: " + bitmap);
		return bitmap;
	}

	public static Bitmap resizeBitmap(int width, int height,
			File file) {
		Bitmap bitmap = null;
		if (file != null && file.exists() && file.isFile()) {
			try {
				Bitmap tempBitmap = BitmapFactory
						.decodeStream(new FileInputStream(file));

				if (tempBitmap != null) {
					bitmap = Bitmap.createBitmap(width, height,
							tempBitmap.getConfig());
					float sx = width / (float) tempBitmap.getWidth();
					float sy = height / (float) tempBitmap.getHeight();
					Canvas canvas = new Canvas(bitmap);
					canvas.scale(sx, sy);
					Paint paint = new Paint();
					paint.setAntiAlias(true);
					paint.setFilterBitmap(true);
					canvas.drawBitmap(tempBitmap, 0, 0, paint);
					tempBitmap.recycle();
				}
			} catch (Exception e) {
				Log.d(TAG, "getPreViewBitmap=>error: " + e.getMessage());
				bitmap = null;
			}
		}
		if (DEBUG)
			Log.d(TAG, "getPreViewBitmap=>file: " + file + " bitmap: " + bitmap);
		return bitmap;
	}

	public static Bitmap resizeBitmap(int width, int height, Bitmap bp) {
		Bitmap bitmap = null;
		if (bp != null) {
			bitmap = Bitmap.createBitmap(width, height, bp.getConfig());
			float sx = width / (float) bp.getWidth();
			float sy = height / (float) bp.getHeight();
			Canvas canvas = new Canvas(bitmap);
			canvas.scale(sx, sy);
			Paint paint = new Paint();
			paint.setAntiAlias(true);
			paint.setFilterBitmap(true);
			canvas.drawBitmap(bp, 0, 0, paint);
			bp.recycle();
		}
		if (DEBUG)
			Log.d(TAG, "getPreViewBitmap=>bp: " + bp + " bitmap: " + bitmap);
		return bitmap;
	}

	public static Bitmap resizeBitmap(int width, int height, Drawable drawable) {
		Bitmap bitmap = null;
		if (drawable != null) {
			Bitmap bp = ((BitmapDrawable) drawable).getBitmap();
			bitmap = Bitmap.createBitmap(width, height, bp.getConfig());
			float sx = width / (float) bp.getWidth();
			float sy = height / (float) bp.getHeight();
			Canvas canvas = new Canvas(bitmap);
			canvas.scale(sx, sy);
			Paint paint = new Paint();
			paint.setAntiAlias(true);
			paint.setFilterBitmap(true);
			canvas.drawBitmap(bp, 0, 0, paint);
			//bp.recycle();
		}
		if (DEBUG)
			Log.d(TAG, "getPreViewBitmap=>drawable: " + drawable + " bitmap: "
					+ bitmap);
		return bitmap;
	}

	public static long getLogoPictureSizeLimit(Context context) {
		long result = LOGO_FILE_SIZE_LIMIT;
		WindowManager wm = (WindowManager) context
				.getSystemService(Context.WINDOW_SERVICE);
		if (wm != null) {
			int width = wm.getDefaultDisplay().getWidth();
			int height = wm.getDefaultDisplay().getHeight();
			if (height > 960 && height <= 1270) {
				height = 1270;
			} else if (height > 854 && height <= 960) {
				height = 960;
			} else if (height > 800 && height <= 854) {
				height = 854;
			}
			result = width * height * 2;
		}
		if (DEBUG)
			Log.d(TAG, "getLogoPictureSizeLimit=>result: " + result);
		return result;
	}
	
	public static void deleteFoalder(String path) {
		File file = new File(path);
		if (file.exists()) {
			File[] fileList = file.listFiles();
			for (File f : fileList) {
				if (file.exists()) {
					if (f.isDirectory()) {
						deleteFoalder(f.getAbsolutePath());
					} else {
						f.delete();
					}
				}
			}
		}

		file.delete();
	}
}
