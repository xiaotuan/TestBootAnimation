package com.test.bootanimation;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

public class BootCompletedReceiver extends BroadcastReceiver {
	private static final String TAG = "BootCompletedReceiver";
	private static final boolean DEBUG = true;
	
	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		if (DEBUG)
			Log.d(TAG, "onReceive=>action: " + action);
		if (Intent.ACTION_BOOT_COMPLETED.equals(action)) {
			startService(context, null, true);
		} else if ("android.provider.Telephony.SECRET_CODE".equals(action)) {
			Uri uri = intent.getData();
			if (DEBUG)
				Log.d(TAG, "onReceive=>uri: " + uri + " host: "
						+ (uri == null ? "null" : uri.getHost()));
			if (uri != null) {
				String host = uri.getHost();
				if (Utils.SECRET_CODE.equals(host)) {
					startService(context, host, false);
				}
			}
		} else if ("android.provider.Telephony.SECRET_CODE".equals(action)) {
			Uri uri = intent.getData();
			if (DEBUG)
				Log.d(TAG, "onReceive=>uri: " + uri + " host: "
						+ (uri == null ? "null" : uri.getHost()));
			if (uri != null) {
				String host = uri.getHost();
				if (Utils.SELECT_DIRECTORY_SECRET_CODE.equals(host)) {
					Intent activity = new Intent(context, BootAnimationActivity.class);
					activity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					context.startActivity(activity);
				}
			}
		}
	}
	
	private void startService(Context context, String host, boolean isBootCompleted) {
		if (DEBUG) Log.d(TAG, "startService=>host: " + host + " isBootCompleted: " + isBootCompleted);
		Intent service = new Intent(context, BootAnimationService.class);
		service.putExtra(Utils.CONFIGE_DIRECTORY_EXTRA, Utils.DEFAULT_CONFIGE_DIRECTORY);
		service.putExtra(Utils.BOOT_COMPLETED_EXTRA, isBootCompleted);
		service.putExtra(Utils.FROM_ACTIVITY_EXTRA, false);
		service.putExtra(Utils.STATE_EXTRA, false);
		service.putExtra(Utils.HOST_EXTRA, host);
		context.startService(service);
	}

}
