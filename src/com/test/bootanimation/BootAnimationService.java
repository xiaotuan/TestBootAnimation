package com.test.bootanimation;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

import com.test.bootanimation.sevenencode.LzmaAlone;

import android.app.KeyguardManager;
import android.app.Notification;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.PowerManager;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.os.SystemProperties;
import android.os.ServiceManager;
import android.util.Log;
import android.widget.Toast;
import android.os.SystemClock;

public class BootAnimationService extends Service {
	private static final String TAG = "BootAnimationService";
	private static final boolean DEBUG = true;

	private static final int BOOTANIMATION_STATUS = 6;
	private static final int SHOW_LOGO_TOO_BIG_ERROR = 1;

	private PowerManager mPowerManager;
	private CopyAsyncTask mCopyAsyncTask;

	private File mConfigeDirectory;
	private File mConfigeFile;
	private File mPowerDownAnimation;
	private File mPowerOnAnimation;
	private File mPowerOnRingFile;
	private File mPowerDownRingFile;
	private File mLogoPictureFile;
	private File mPreviewPictureFile;

	private String mBrand;
	private String mModel;
	private boolean mEnabledAutoRun = false;
	private boolean mFail = false;

	// hcl add for nv
	private static final int logNV_length_index = 1024;
	private static final int logNV_length_index_length = 20;
	private static final int logNV_data_index = 1024 + 20;
	private static final int SWITCH_ANMI_ID = 45;
	private static final int enable_diy_index = 1;
	private static final int enable_diy_buff_max = 1024 * 1024 + 1024 * 512
			- 20;
	private static final int diy_model_index = 50;
	private static final int diy_brand_index = 50 + 16;
	private static final int diy_brand_length = 16;

	// hcl add for use system bootanimition
	/*
	 * static boolean WriteUseSystmBootToNV() { boolean ret_v = false; IBinder
	 * binder = ServiceManager.getService("NvRAMAgent"); NvRAMAgent agent =
	 * NvRAMAgent.Stub.asInterface(binder); byte buff[] = new byte[512]; try{
	 * buff = agent.readFile(SWITCH_ANMI_ID); }catch(Exception e){
	 * e.printStackTrace(); return false; } buff[enable_diy_index] =
	 * (byte)('0'); try { int flag = agent.writeFile(SWITCH_ANMI_ID, buff); if
	 * (flag > 0) { Log.d(TAG, "WriteBrandDataToNV write success"); ret_v =
	 * true; } else { Log.d(TAG, "WriteBrandDataToNV write fail"); ret_v =
	 * false; }
	 * 
	 * } catch (RemoteException e) { e.printStackTrace(); }
	 * 
	 * if(ret_v){ SystemProperties.set("persist.sys.bootanimation_diy","1"); }
	 * 
	 * return ret_v; }
	 */
	static boolean WriteBrandDataToNV(byte[] data) {
		boolean ret_v = false;
		IBinder binder = ServiceManager.getService("NvRAMAgent");
		NvRAMAgent agent = NvRAMAgent.Stub.asInterface(binder);
		byte buff[] = new byte[512];
		try {
			buff = agent.readFile(SWITCH_ANMI_ID);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		Log.d(TAG, "hcl diy WriteBrandDataToNV diy src data data.length");
		if (data.length > diy_brand_length) {
			Log.d(TAG,
					"hcl diy WriteBrandDataToNV diy src data error data.length");
			return false;
		}

		{
			// memset data
			for (int i = diy_brand_index; i < diy_brand_index
					+ diy_brand_length; i++) {
				buff[i] = 0;
			}

			for (int j = 0; j < data.length; j++) {
				buff[diy_model_index + j] = (byte) (data[j]);
			}
		}

		try {
			int flag = agent.writeFile(SWITCH_ANMI_ID, buff);
			if (flag > 0) {
				Log.d(TAG, "WriteBrandDataToNV write success data.length()="
						+ data.length);
				ret_v = true;
			} else {
				Log.d(TAG, "WriteBrandDataToNV write fail data.length()="
						+ data.length);
				ret_v = false;
			}

		} catch (RemoteException e) {
			e.printStackTrace();
		}
		String string_data = new String(data);
		if (ret_v) {
			SystemProperties.set("persist.sys.brand_diy", string_data);
		}

		return ret_v;
	}

	static boolean WriteModelDataToNV(byte[] data) {
		boolean ret_v = false;
		IBinder binder = ServiceManager.getService("NvRAMAgent");
		NvRAMAgent agent = NvRAMAgent.Stub.asInterface(binder);
		byte buff[] = new byte[512];
		try {
			buff = agent.readFile(SWITCH_ANMI_ID);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		Log.d(TAG, "hcl diy WriteModelDataToNV diy src data data.length");
		if (data.length > diy_brand_length) {
			Log.d(TAG,
					"hcl diy WriteModelDataToNV diy src data error data.length");
			return false;
		}
		// write length
		{
			// memset data
			for (int i = diy_model_index; i < diy_model_index
					+ diy_brand_length; i++) {
				buff[i] = 0;
			}

			for (int j = 0; j < data.length; j++) {
				buff[diy_model_index + j] = (byte) (data[j]);
			}
		}

		try {
			int flag = agent.writeFile(SWITCH_ANMI_ID, buff);
			if (flag > 0) {
				Log.d(TAG, "WriteModelDataToNV write success data.length()="
						+ data.length);
				ret_v = true;
			} else {
				Log.d(TAG, "WriteModelDataToNV write fail data.length()="
						+ data.length);
				ret_v = false;
			}

		} catch (RemoteException e) {
			e.printStackTrace();
		}
		String string_data = new String(data);

		Log.d(TAG, "WriteModelDataToNV write ok string_data=" + string_data);

		if (ret_v) {
			SystemProperties.set("persist.sys.model_diy", string_data);
		}

		return ret_v;
	}

	static boolean WriteLogDataToNV(byte[] data, long length) {
		boolean ret_v = false;
		IBinder binder = ServiceManager.getService("NvRAMAgent");
		NvRAMAgent agent = NvRAMAgent.Stub.asInterface(binder);
		byte buff[] = new byte[1024 * 1024 + 1024 * 512];
		try {
			buff = agent.readFile(SWITCH_ANMI_ID);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		Log.d(TAG, "hcl diy get diy src data data.length=" + data.length
				+ ",data_length=" + length);
		if (length > enable_diy_buff_max || data.length > enable_diy_buff_max) {
			Log.d(TAG, "hcl diy get diy src data error data.length="
					+ data.length + ",data_length=" + length);
			return false;
		}
		// write length
		{
			String length_string = Long.toString(length);
			// memset data
			for (int i = 1024; i < 1024 + length; i++) {
				buff[i] = 0;
			}

			// wirte length
			for (int j = 0; j < length_string.length(); j++) {
				if (j > logNV_length_index_length) {
					break;
				}
				buff[logNV_length_index + j] = (byte) (length_string.charAt(j));
			}

			// wirte data enable_diy_buff_max
			for (int k = 0; k < length; k++) {
				// for(int k=0;k<enable_diy_buff_max;k++){
				buff[logNV_data_index + k] = data[k];
			}

			// wirte diy flag
			buff[enable_diy_index] = '1';

		}

		try {
			int flag = agent.writeFile(SWITCH_ANMI_ID, buff);
			if (flag > 0) {
				Log.d(TAG, "WriteLogDataToNV write success data.length()="
						+ data.length);
				ret_v = true;
			} else {
				Log.d(TAG,
						"WriteSwitchSignalParaFlagToNV write fail data.length()="
								+ data.length);
				ret_v = false;
			}

		} catch (RemoteException e) {
			e.printStackTrace();
		}

		if (ret_v) {
			SystemProperties.set("persist.sys.bootanimation_diy", "1");
		}

		return ret_v;
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		mPowerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
		Notification status = new Notification(0, null,
				System.currentTimeMillis());
		status.flags |= Notification.FLAG_HIDE_NOTIFICATION;
		startForeground(BOOTANIMATION_STATUS, status);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (intent != null) {
			String directory = intent
					.getStringExtra(Utils.CONFIGE_DIRECTORY_EXTRA);
			boolean isBootCompleted = intent.getBooleanExtra(
					Utils.BOOT_COMPLETED_EXTRA, false);
			boolean isFromeActivity = intent.getBooleanExtra(
					Utils.FROM_ACTIVITY_EXTRA, false);
			boolean ok = intent.getBooleanExtra(Utils.STATE_EXTRA, false);
			String host = intent.getStringExtra(Utils.HOST_EXTRA);
			if (DEBUG)
				Log.d(TAG, "onStartCommand=>boot: " + isBootCompleted
						+ " activity: " + isFromeActivity + " ok: " + ok
						+ " host: " + host + " fail: " + mFail);

			if (directory != null) {
				initConfigeFile(directory);
				parseConfigeFile();
				checkFile();
			}

			if (!mFail) {
				if (!isFromeActivity
						&& ((isBootCompleted && mEnabledAutoRun) || (!isBootCompleted))) {
					needWakeUpScreen();
					showInfoDialog();
				} else if (isFromeActivity) {
					if (ok) {
						startWork();
					} else {
						stopSelf();
					}
				}
			} else {
				stopSelf();
			}
		} else {
			stopSelf();
		}
		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		resetSettingState();
		stopForeground(true);
		mEnabledAutoRun = false;
		mFail = false;
	}

	private void initConfigeFile(String directory) {
		mConfigeDirectory = Utils.getConfigeDirectory(this, directory);
		if (DEBUG)
			Log.d(TAG,
					"initConfigeFile=>mConfigeDirectory: "
							+ (mConfigeDirectory == null ? "null"
									: mConfigeDirectory.getAbsolutePath()));
		if (mConfigeDirectory != null) {
			File[] files = mConfigeDirectory.listFiles();
			for (File f : files) {
				if (f != null && f.exists() && f.isFile()) {
					String name = f.getName();
					if (DEBUG)
						Log.d(TAG, "initConfigeFile=>name: " + name);
					if (Utils.POWER_ON_ANIMATION_FILE_NAME.equals(name)) {
						mPowerOnAnimation = f;
					} else if (Utils.POWER_DOWN_ANIMATION_FILE_NAME
							.equals(name)) {
						mPowerDownAnimation = f;
					} else if (Utils.POWER_ON_RING_FILE_NAME.equals(name)
							&& name.endsWith(".mp3")) {
						mPowerOnRingFile = f;
					} else if (Utils.POWER_DOWN_RING_FILE_NAME.equals(name)
							&& name.endsWith(".mp3")) {
						mPowerDownRingFile = f;
					} else if (Utils.CONFIGE_FILE_NAME.equals(name)
							&& name.endsWith(".ini")) {
						mConfigeFile = f;
					} else if (Utils.LOG_PICTURE_FILE_NAME.equals(name)) {
						mLogoPictureFile = f;
					} else if (Utils.PREVIEW_PICTURE_FILE_NAME.equals(name)) {
						mPreviewPictureFile = f;
					}
				}
			}
		} else {
			// Toast.makeText(this, getString(R.string.no_confige_directory),
			// Toast.LENGTH_SHORT).show();
			Log.d(TAG, "initConfigeFile=>not found directory.");
			mFail = true;
		}
	}

	private void parseConfigeFile() {
		if (mConfigeFile != null && mConfigeFile.exists()
				&& mConfigeFile.isFile()) {
			InputStreamReader read = null;
			BufferedReader br = null;
			try {
				read = new InputStreamReader(new FileInputStream(mConfigeFile),
						"utf-8");
				br = new BufferedReader(read);
				String line = null;
				while ((line = br.readLine()) != null) {
					if (DEBUG)
						Log.d(TAG, "parseConfigeFile=>line: " + line);
					if (line.contains("=")) {
						int index = line.lastIndexOf("=");
						String key = line.substring(0, index);
						String value = line.substring(index + 1, line.length());
						if (DEBUG)
							Log.d(TAG, "parseConfigeFile=>key: " + key
									+ " value: " + value);
						if (Utils.AUTO_RUN_KEY.equals(key)) {
							if ("1".equals(value)) {
								mEnabledAutoRun = true;
							}
						} else if (Utils.BRAND_KEY.equals(key)) {
							mBrand = value;
						} else if (Utils.MODEL_KEY.equals(key)) {
							mModel = value;
						}
					}
				}
			} catch (UnsupportedEncodingException e) {
				mFail = true;
				Log.d(TAG, "parseConfigeFile=>error: " + e.toString());
				Toast.makeText(this, getString(R.string.encord_error),
						Toast.LENGTH_SHORT).show();
			} catch (FileNotFoundException e) {
				mFail = true;
				Log.d(TAG, "parseConfigeFile=>error: " + e.toString());
				Toast.makeText(this, getString(R.string.file_not_found),
						Toast.LENGTH_SHORT).show();
			} catch (IOException e) {
				mFail = true;
				Log.d(TAG, "parseConfigeFile=>error: " + e.toString());
				Toast.makeText(this, getString(R.string.read_failed),
						Toast.LENGTH_SHORT).show();
			} finally {
				try {
					if (br != null) {
						br.close();
						br = null;
					}
				} catch (Exception e) {
				}
				try {
					if (read != null) {
						read.close();
						read = null;
					}
				} catch (Exception e) {
				}
			}
		}
		if (DEBUG)
			Log.d(TAG, "parseConfigeFile=>enabled: " + mEnabledAutoRun
					+ " brand: " + mBrand + " model: " + mModel);
	}

	private void checkFile() {
		if (mLogoPictureFile != null && mLogoPictureFile.exists()
				&& mLogoPictureFile.isFile()) {
			long logoFileSize = mLogoPictureFile.length();
			if (DEBUG)
				Log.d(TAG, "checkFile=>fileSize: " + logoFileSize
						+ " limitSize: " + Utils.getLogoPictureSizeLimit(this));
			if (logoFileSize > Utils.getLogoPictureSizeLimit(this)) {
				mFail = true;
				Toast.makeText(this, getString(R.string.logo_file_too_big),
						Toast.LENGTH_SHORT).show();
				return;
			}
		}

		if (mPowerOnRingFile != null && mPowerOnRingFile.exists()
				&& mPowerOnRingFile.isFile()) {
			boolean isMp3File = MediaFile.isMp3FileType(MediaFile
					.getFileTypeForMimeType(MediaFile
							.getMimeTypeForFile(mPowerOnRingFile
									.getAbsolutePath())));
			if (!isMp3File) {
				mFail = true;
				Toast.makeText(this,
						getString(R.string.power_on_ring_not_legal_file),
						Toast.LENGTH_SHORT).show();
				return;
			}
		}

		if (mPowerDownRingFile != null && mPowerDownRingFile.exists()
				&& mPowerDownRingFile.isFile()) {
			boolean isMp3File = MediaFile.isMp3FileType(MediaFile
					.getFileTypeForMimeType(MediaFile
							.getMimeTypeForFile(mPowerDownRingFile
									.getAbsolutePath())));
			if (!isMp3File) {
				mFail = true;
				Toast.makeText(this,
						getString(R.string.shut_down_ring_not_legal_file),
						Toast.LENGTH_SHORT).show();
				return;
			}
		}

		if (!TextUtils.isEmpty(mBrand)) {
			if (mBrand.length() > Utils.BRAND_SIZE_LIMIT) {
				mFail = true;
				Toast.makeText(this, getString(R.string.brand_size_too_long),
						Toast.LENGTH_SHORT).show();
				return;
			}
		}

		if (!TextUtils.isEmpty(mModel)) {
			if (mModel.length() > Utils.MODEL_SIZE_LIMIT) {
				mFail = true;
				Toast.makeText(this, getString(R.string.model_size_too_long),
						Toast.LENGTH_SHORT).show();
				return;
			}
		}
	}

	private void needWakeUpScreen() {
		if (DEBUG)
			Log.d(TAG,
					"needWakeUpScreen=>screen on: "
							+ mPowerManager.isScreenOn());
		if (!mPowerManager.isScreenOn()) {
			KeyguardManager km = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
			if (km.isKeyguardLocked()) {
				KeyguardManager.KeyguardLock kl = km.newKeyguardLock("unLock");
				kl.disableKeyguard();
			}
			PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
			PowerManager.WakeLock wl = pm.newWakeLock(
					PowerManager.ACQUIRE_CAUSES_WAKEUP
							| PowerManager.SCREEN_DIM_WAKE_LOCK, "bright");
			wl.acquire();
			wl.release();
		}
	}

	private void showInfoDialog() {
		if (DEBUG)
			Log.d(TAG, "showInfoDialog()");
		Intent dialog = new Intent(this, BootInfoDialogActivity.class);
		dialog.putExtra(Utils.DIALOG_TYPE_EXTRA, Utils.TYPE_INFO);
		dialog.putExtra(Utils.LOG_EXTRA, (mLogoPictureFile == null ? null
				: mLogoPictureFile.getName()));
		dialog.putExtra(Utils.BRAND_EXTRA, mBrand);
		dialog.putExtra(Utils.MODEL_EXTRA, mModel);
		dialog.putExtra(
				Utils.POWER_DOWN_ANIMATION_EXTRA,
				(mPowerDownAnimation == null ? null : mPowerDownAnimation
						.getName()));
		dialog.putExtra(
				Utils.POWER_ON_ANIMATION_EXTRA,
				(mPowerOnAnimation == null ? null : mPowerOnAnimation.getName()));
		dialog.putExtra(
				Utils.POWER_DOWN_RING_EXTRA,
				(mPowerDownRingFile == null ? null : mPowerDownRingFile
						.getName()));
		dialog.putExtra(Utils.POWER_ON_RING_EXTRA,
				(mPowerOnRingFile == null ? null : mPowerOnRingFile.getName()));
		dialog.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(dialog);
	}

	private void showFailDialog() {
		if (DEBUG)
			Log.d(TAG, "showFailDialog()");
		Intent dialog = new Intent(this, BootInfoDialogActivity.class);
		dialog.putExtra(Utils.DIALOG_TYPE_EXTRA, Utils.TYPE_FAIL);
		dialog.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(dialog);
	}

	private void startWork() {
		if (DEBUG)
			Log.d(TAG, "startWork()");
		mCopyAsyncTask = new CopyAsyncTask();
		if (mCopyAsyncTask.getStatus() == AsyncTask.Status.RUNNING) {
			mCopyAsyncTask.cancel(true);
		}
		mCopyAsyncTask.execute(new Void[] {});
	}

	private byte[] getLogoByteArrays(File file) {
		byte[] results = null;
		if (file != null && file.exists() && file.isFile()) {
			ByteArrayOutputStream bos = new ByteArrayOutputStream(
					(int) file.length());
			BufferedInputStream in = null;
			try {
				in = new BufferedInputStream(new FileInputStream(file));
				int buf_size = 1024;
				byte[] buffer = new byte[buf_size];
				int len = 0;
				while (-1 != (len = in.read(buffer, 0, buf_size))) {
					bos.write(buffer, 0, len);
				}
				results = bos.toByteArray();
			} catch (IOException e) {
				Log.e(TAG, "getLogoByteArrays=>error: " + e.toString());
			} finally {
				try {
					in.close();
					in = null;
				} catch (IOException e) {
					e.printStackTrace();
				}
				try {
					bos.close();
					bos = null;
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		} else {
			if (DEBUG)
				Log.d(TAG, "getLogoByteArrays=>file: " + file + " exists: "
						+ (file != null ? file.exists() : "null") + " isFile: "
						+ (file != null ? file.isFile() : "null"));
		}

		if (DEBUG)
			Log.d(TAG, "getLogoByteArrays=>result: " + results + " length: "
					+ (results == null ? 0 : results.length));
		return results;
	}

	private long getLogoLength(File file) {
		long result = -1;
		if (file != null && file.exists() && file.isFile()) {
			result = file.length();
		}

		if (DEBUG)
			Log.d(TAG, "getLogoLength=>result: " + result);
		return result;
	}

	/**
	 * 将Logo图片写入NV，写入成功，返回true；写入失败，返回false
	 */
	private boolean writeLogoToNV(byte[] data, long length) {
		boolean result = false;
		if ((length > 0) && data != null && (data.length == length)) {
			result = WriteLogDataToNV(data, length);
		}

		return result;
	}

	/**
	 * 将品牌写入NV，写入成功，返回true；写入失败，返回false
	 */
	private boolean writeBrandToNV(String brand) {
		byte[] srtbyte = brand.getBytes();
		boolean result = false;
		result = WriteBrandDataToNV(srtbyte);
		return result;
	}

	/**
	 * 将型号写入NV，写入成功，返回true；写入失败，返回false
	 */
	private boolean writeModelToNV(String model) {
		byte[] srtbyte = model.getBytes();
		boolean result = false;
		result = WriteModelDataToNV(srtbyte);
		return result;
	}

	private void resetSettingState() {
		Editor e = PreferenceManager.getDefaultSharedPreferences(
				BootAnimationService.this).edit();
		e.putBoolean(Utils.KEY_IS_SETTING_RUNNING, false);
		e.commit();
	}

	private void sendSettingBroadcast(boolean success) {
		if (DEBUG)
			Log.d(TAG, "showInfoDialog(),hcl_eee mBrand=" + mBrand + ",mModel="
					+ mModel);
		Intent intent = new Intent("com.hcl.diy_para_set");
		intent.putExtra(Utils.BRAND_EXTRA, mBrand);
		intent.putExtra(Utils.MODEL_EXTRA, mModel);
		intent.putExtra("state", success);

		sendBroadcast(intent);
	}

	class CopyAsyncTask extends AsyncTask<Void, Void, Boolean> {

		@Override
		protected Boolean doInBackground(Void... params) {
			boolean result = false;
			File logo = null;
			try {
				if (mLogoPictureFile != null && mLogoPictureFile.exists()
						&& mLogoPictureFile.isFile()) {
					String logoTemp = mLogoPictureFile.getParent()
							+ File.separator + Utils.LOGO_PICTURE_TEMP_FILE_NAME;
					logo = new File(logoTemp);
					if (logo == null || !logo.exists() || !logo.isFile()) {
						if (logo != null && logo.exists() && logo.isDirectory()) {
							Utils.deleteFoalder(logo.getAbsolutePath());
						}
						String[] command = { "e", "-d" + Utils.DICTIONARY_SIZE,
								mLogoPictureFile.getAbsolutePath(), logoTemp };
						long start = SystemClock.elapsedRealtime();
						if (DEBUG)
							Log.d(TAG, "doInBackground=>command: " + getArrayString(command) + " start: " + start);
						LzmaAlone.execCommand(command);
						if (DEBUG)
							Log.d(TAG, "doInBackground=>end: " + (SystemClock.elapsedRealtime() - start));
						logo = new File(logoTemp);
						if (logo != null && logo.exists() && logo.isFile()) {
							long length = getLogoLength(logo);
							if (length > Utils.LOGO_FILE_SIZE_LIMIT) {
								mHandler.sendEmptyMessage(SHOW_LOGO_TOO_BIG_ERROR);
								return false;
							}
						}
					}
				}
				if (writeLogoToNV(getLogoByteArrays(logo), getLogoLength(logo))) {
					if (writeBrandToNV(mBrand)) {
						if (writeModelToNV(mModel)) {
							if (Utils.copyFileToDestinationDirectory(
									Utils.COPY_DIRECTORY, mPowerDownAnimation,
									mPowerDownRingFile, mPowerOnAnimation,
									mPowerOnRingFile, mPreviewPictureFile)) {
								BootAnimationService.this
										.sendSettingBroadcast(true);
								result = true;
							} else {
								Log.d(TAG, "doInBackground=>copy file fail.");
							}
						} else {
							Log.d(TAG, "doInBackground=>write model fail.");
						}
					} else {
						Log.d(TAG, "doInBackground=>write brand fail.");
					}
				} else {
					Log.d(TAG, "doInBackground=>write logo fail.");
				}
			} catch (Exception e) {
				Log.d(TAG, "doInBackground=>error: " + e.getMessage());
			}
			
			if (logo != null && logo.exists()) {
				//logo.delete();
			}
			if (DEBUG)
				Log.d(TAG, "doInBackground=>result: " + result);
			return result;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			super.onPostExecute(result);
			if (DEBUG)
				Log.d(TAG, "onPostExecute=>result: " + result);
			resetSettingState();
			Intent state = new Intent(Utils.ACTION_STATE);
			state.setPackage(getPackageName());
			BootAnimationService.this.sendBroadcast(state);
			if (result.booleanValue()) {
				Toast.makeText(BootAnimationService.this,
						getString(R.string.set_success), Toast.LENGTH_SHORT)
						.show();
			} else {
				showFailDialog();
				/**
				 * Toast.makeText(BootAnimationService.this,
				 * getString(R.string.set_fail), Toast.LENGTH_SHORT).show();
				 */
			}
			stopSelf();
		}

		@Override
		protected void onCancelled(Boolean result) {
			super.onCancelled(result);
			Editor e = PreferenceManager.getDefaultSharedPreferences(
					BootAnimationService.this).edit();
			e.putBoolean(Utils.KEY_IS_SETTING_RUNNING, false);
			e.commit();
		}
	}

	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case SHOW_LOGO_TOO_BIG_ERROR:
				Toast.makeText(getBaseContext(), R.string.logo_file_too_big,
						Toast.LENGTH_SHORT).show();
				break;
			}
		}
	};
	
	public String getArrayString(String[] strs) {
		String result = "";
		if (strs != null && strs.length > 0) {
			for (String s : strs) {
				result += s;
				result += " ";
			}
		}
		return result;
	}
		
}
