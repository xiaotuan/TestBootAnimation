package com.test.bootanimation;

import java.io.File;
import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;
import android.os.IBinder;
import android.os.SystemProperties;
import android.os.ServiceManager;
import android.os.RemoteException;


public class BootAnimationActivity extends Activity implements
		OnItemClickListener {
	private static final String TAG = "BootAnimationActivity";
	private static final boolean DEBUG = true;

	private static final String KEY_SHOW_DISCLAIMER = "show_disclaimer";

	private GridView mGridView;
	private TextView mEmptyView;

	private AlertDialog mDisclaimerDialog;
	private SharedPreferences mSharedPreferences;
	private ArrayList<BootFileInfo> mList;
	private GridViewAdapter mAdapter;
	private LoadTask mLoadTask;
	private Resources mResources;
	private Bitmap mDefaultBitmap;

	private int mPreviewBitmapWidth;
	private int mPreviewBitmapHeight;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_boot_animation);

		init();
	}

	@Override
	protected void onStart() {
		super.onStart();

	}

	@Override
	protected void onResume() {
		super.onResume();
		startLoadPreviewList();
		IntentFilter filter = new IntentFilter();
		filter.addAction(Utils.ACTION_STATE);
		registerReceiver(mStateReceiver, filter);
		boolean isShowDisclaimer = mSharedPreferences.getBoolean(
				KEY_SHOW_DISCLAIMER, false);
		if (!isShowDisclaimer) {
			if (mDisclaimerDialog == null) {
				mDisclaimerDialog = createAlertDialog();
			}
			mDisclaimerDialog.show();
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		unregisterReceiver(mStateReceiver);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		Editor e = mSharedPreferences.edit();
		e.putBoolean(KEY_SHOW_DISCLAIMER, false);
		e.commit();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_boot_animation, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.restore) {
			deleteCopyDirectoryFiles();
			restoreFactorySettings();
			startLoadPreviewList();
			Toast.makeText(this, R.string.restore_success, Toast.LENGTH_SHORT).show();
			return true;
		} else if (item.getItemId() == R.id.clean) {
			deleteAllLogoTempFile();
			Toast.makeText(this, R.string.clean_complete, Toast.LENGTH_SHORT).show();
			return true;
		}
		
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		BootFileInfo info = (BootFileInfo) mAdapter.getItem(position);
		if (!info.isCurrent()) {
			if (!isSettingRunning()) {
				Editor e = mSharedPreferences.edit();
				e.putBoolean(Utils.KEY_IS_SETTING_RUNNING, true);
				e.commit();
				startService(info.getName());
			} else {
				Toast.makeText(this, R.string.setting_running,
						Toast.LENGTH_SHORT).show();
			}
		}
	}

	private void init() {
		mResources = getResources();
		mPreviewBitmapWidth = mResources
				.getInteger(R.integer.preview_picture_width);
		mPreviewBitmapHeight = mResources
				.getInteger(R.integer.preview_picture_height);
		mDefaultBitmap = Utils.resizeDefaultBitmap(this, mPreviewBitmapWidth, mPreviewBitmapHeight, R.drawable.ic_default_logo);
		mDisclaimerDialog = createAlertDialog();
		mSharedPreferences = PreferenceManager
				.getDefaultSharedPreferences(this);

		mGridView = (GridView) findViewById(R.id.grid_view);
		mEmptyView = (TextView) findViewById(R.id.empty_view);

		mGridView.setEmptyView(mEmptyView);
		mGridView.setOnItemClickListener(this);
		resetSettingState();
	}

	private void startLoadPreviewList() {
		mLoadTask = new LoadTask();
		if (mLoadTask.getStatus() == AsyncTask.Status.RUNNING) {
			mLoadTask.cancel(true);  
		}
		mLoadTask.execute(new Void[] {});
	}

	private void startService(String directory) {
		Intent service = new Intent(this, BootAnimationService.class);
		service.putExtra(Utils.CONFIGE_DIRECTORY_EXTRA, directory);
		service.putExtra(Utils.BOOT_COMPLETED_EXTRA, false);
		service.putExtra(Utils.FROM_ACTIVITY_EXTRA, false);
		service.putExtra(Utils.STATE_EXTRA, false);
		startService(service);
	}

	private BootFileInfo createBootFileInfo(File file, boolean isCurrent) {
		BootFileInfo result = null;
		if (file != null && file.exists() && file.isDirectory()) {
			String name = file.getName();
			File preview = null;
			File[] files = file.listFiles();
			if (files != null && files.length > 0) {
				String fileName = null;
				for (File f : files) {
					if (f != null && f.exists() && f.isFile()) {
						fileName = f.getName();
						fileName = fileName.toLowerCase().trim();
						if (DEBUG) Log.d(TAG, "createBootFileInfo=>fileName: " + fileName);
						if (Utils.PREVIEW_PICTURE_FILE_NAME.toLowerCase().equals(
								fileName)) {
							preview = f;
						}
					}
				}
			}
			
			Bitmap bitmap = Utils.resizeBitmap(mPreviewBitmapWidth, mPreviewBitmapHeight, preview);
			if (isCurrent) {
				if (bitmap != null) {
					result = new BootFileInfo(name, file, bitmap, isCurrent);
				}
			} else {
				if (bitmap != null) {
					result = new BootFileInfo(name, file, bitmap, isCurrent);
				} else {
					result = new BootFileInfo(name, file, mDefaultBitmap, isCurrent);
				}
			}
		}
		if (DEBUG)
			Log.d(TAG, "createBootFileInfo=>file: " + file + " result: "
					+ (result != null ? result.toString() : "null") + " current: " + isCurrent);
		return result;
	}

	private void addCurrentBootFileInfo() {
		BootFileInfo result = null;
		File file = new File(Utils.COPY_DIRECTORY);
		if (file != null && file.exists() && file.isDirectory()) {
			result = createBootFileInfo(file, true);
			if (result != null) {
				mList.add(result);
			}
		}
	}

	private boolean isSettingRunning() {
		boolean result = mSharedPreferences.getBoolean(
				Utils.KEY_IS_SETTING_RUNNING, false);
		if (DEBUG)
			Log.d(TAG, "isSettingRunning=>result: " + result);
		return result;
	}
	
	/**
	 * 还原出厂设置
	 */
	 	//hcl add for nv
	private static final int logNV_length_index = 1024;
	private static final int logNV_length_index_length = 20;
	private static final int logNV_data_index = 1024+20;
	private static final int SWITCH_ANMI_ID = 45;
	private static final int enable_diy_index = 1;
	private static final int enable_diy_buff_max = 1024*1024+1024*512-20;
	private static final int diy_model_index=50;
	private static final int diy_brand_index=50+16;	
	private static final int diy_brand_length=16;	

	//hcl add for use system bootanimition
	static boolean WriteUseSystmBootToNV() {
		boolean ret_v = false;
		IBinder binder = ServiceManager.getService("NvRAMAgent");
		NvRAMAgent agent = NvRAMAgent.Stub.asInterface(binder);
		byte buff[] = new byte[512];
		try{
			buff = agent.readFile(SWITCH_ANMI_ID);
		}catch(Exception e){
			e.printStackTrace();
			return false;
		}
		buff[enable_diy_index] = (byte)('0');
		try {
			int flag = agent.writeFile(SWITCH_ANMI_ID, buff);
			if (flag > 0) {
				Log.d(TAG, "WriteBrandDataToNV write success");
				ret_v = true;
			} else {
				Log.d(TAG, "WriteBrandDataToNV write fail");
				ret_v = false;
			}

		} catch (RemoteException e) {
			e.printStackTrace();
		}

		if(ret_v){
			//SystemProperties.set("persist.sys.bootanimation_diy","0");
			//BootAnimationActivity.this.sendSettingBroadcast(false);
		}
		
		return ret_v;
	}
	 
	 
	private void restoreFactorySettings() {
		WriteUseSystmBootToNV();
	}

	private void deleteCopyDirectoryFiles() {
		File file = new File(Utils.COPY_DIRECTORY);
		if (file != null && file.exists() && file.isDirectory()) {
			File[] files = file.listFiles();
			if (files != null && files.length > 0) {
				for (File f : files) {
					if (f != null && f.exists() && f.isFile()) {
						String name = f.getName();
						if (DEBUG) Log.d(TAG, "deleteCopyDirectoryFiles=>name: " + name);
						if (!TextUtils.isEmpty(name)
								&& (name.equals(Utils.POWER_ON_ANIMATION_FILE_NAME)
										|| name.equals(Utils.POWER_DOWN_ANIMATION_FILE_NAME)
										|| name.equals(Utils.POWER_ON_RING_FILE_NAME)
										|| name.equals(Utils.POWER_DOWN_RING_FILE_NAME)
										|| name.equals(Utils.LOG_PICTURE_FILE_NAME) || name
											.equals(Utils.PREVIEW_PICTURE_FILE_NAME))) {
							f.delete();
						}
					}
				}
			}
		}
	}

	private AlertDialog createAlertDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);

		builder.setIcon(R.drawable.ic_disclaimer);
		builder.setTitle(R.string.disclaimer_title);
		builder.setMessage(R.string.disclaimer_message);
		builder.setNegativeButton(R.string.cancel,
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						finish();
					}
				});
		builder.setPositiveButton(R.string.agree,
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						Editor e = mSharedPreferences.edit();
						e.putBoolean(KEY_SHOW_DISCLAIMER, true);
						e.commit();
						dialog.dismiss();
					}
				});

		return builder.create();
	}
	
	private void resetSettingState() {
		Editor e = PreferenceManager.getDefaultSharedPreferences(this).edit();
		e.putBoolean(Utils.KEY_IS_SETTING_RUNNING, false);
		e.commit();
	}
	
	private void deleteAllLogoTempFile() {
		File bootListFile = Utils.getScreenResolutionDirectoryFile(this,
					Utils.getBootListDirectoryFile(BootAnimationActivity.this));
		if (bootListFile != null && bootListFile.exists() && bootListFile.isDirectory()) {
			File[] files = bootListFile.listFiles();
			if (files != null && files.length > 0) {
				for (File f : files) {
					if (f != null && f.exists() && f.isDirectory()) {
						File[] lists = f.listFiles();
						if (lists != null && lists.length > 0) {
							for (File file : lists) {
								if (file != null && file.exists()) {
									String name = file.getName();
									if (Utils.LOGO_PICTURE_TEMP_FILE_NAME.equals(name)) {
										if (file.isFile()) {
											file.delete();
										} else {
											Utils.deleteFoalder(file.getAbsolutePath());
										}
									}
								}
							}
						}
					}
				}
			}
		}
	}

	private BroadcastReceiver mStateReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (DEBUG)
				Log.d(TAG, "onReceive=>action: " + action);
			if (!TextUtils.isEmpty(action) && action.equals(Utils.ACTION_STATE)) {
				startLoadPreviewList();
			}
		}
	};

	private class LoadTask extends AsyncTask<Void, Void, Boolean> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			mEmptyView.setText(R.string.loading_msg);
		}

		@Override
		protected Boolean doInBackground(Void... params) {
			Boolean result = false;
			if (mList == null) {
				mList = new ArrayList<BootFileInfo>();
			} else {
				mList.clear();
			}
			File bootListFile = Utils.getScreenResolutionDirectoryFile(
					BootAnimationActivity.this,
					Utils.getBootListDirectoryFile(BootAnimationActivity.this));
			if (bootListFile != null && bootListFile.exists()
					&& bootListFile.isDirectory()) {
				File[] files = bootListFile.listFiles();
				if (files != null && files.length > 0) {
					BootFileInfo info = null;
					for (File f : files) {
						if (f != null && f.exists() && f.isDirectory()) {
							info = createBootFileInfo(f, false);
							mList.add(info);
						}
					}
				}
			}
			if (mList != null && mList.size() > 0) {
				addCurrentBootFileInfo();
			}
			return result;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			super.onPostExecute(result);
			mAdapter = new GridViewAdapter(BootAnimationActivity.this, mList);
			mGridView.setAdapter(mAdapter);
			mEmptyView.setText(R.string.empty_msg);
		}
	}
}
