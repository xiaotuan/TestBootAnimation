package com.test.bootanimation;
  
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Vibrator;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.webkit.WebView.FindListener;
import android.widget.TextView;
import android.widget.Toast;
import com.android.internal.app.AlertActivity;
import com.android.internal.app.AlertController;

public class BootInfoDialogActivity extends AlertActivity implements
		DialogInterface.OnClickListener {
	private static final String TAG = "BootInfoDialogActivity";
	private static final boolean DEBUG = true;

	private static final Uri WARNING_SOUND_URI = Uri
			.parse("file:///system/media/audio/ui/VideoRecord.ogg");

	private Ringtone mRingtone;
	private Vibrator mVibrator;

	private String mLog;
	private String mBrand;
	private String mModel;
	private String mPowerOnAnimation;
	private String mPowerDownAnimation;
	private String mPowerOnRing;
	private String mPowerDownRing;
	private String mType;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Intent intent = getIntent();
		if (intent != null) {
			mType = intent.getStringExtra(Utils.DIALOG_TYPE_EXTRA);
			if (!TextUtils.isEmpty(mType) && (mType.equals(Utils.TYPE_FAIL) || mType.equals(Utils.TYPE_INFO))) {
				if (mType.equals(Utils.TYPE_INFO)) {
					mLog = intent.getStringExtra(Utils.LOG_EXTRA);
					mBrand = intent.getStringExtra(Utils.BRAND_EXTRA);
					mModel = intent.getStringExtra(Utils.MODEL_EXTRA);
					mPowerOnAnimation = intent
							.getStringExtra(Utils.POWER_ON_ANIMATION_EXTRA);
					mPowerDownAnimation = intent
							.getStringExtra(Utils.POWER_DOWN_ANIMATION_EXTRA);
					mPowerOnRing = intent.getStringExtra(Utils.POWER_ON_RING_EXTRA);
					mPowerDownRing = intent
							.getStringExtra(Utils.POWER_DOWN_RING_EXTRA);
					if (mLog != null || mBrand != null || mModel != null
							|| mPowerOnAnimation != null
							|| mPowerDownAnimation != null || mPowerOnRing != null
							|| mPowerDownRing != null) {
						showInfoDialog();
					}
				} else if (mType.equals(Utils.TYPE_FAIL)) {
					showFailDialog();
				}
				return;
			}
		}
		startService(false);
		finish();
	}

	protected void onDestroy() {
		super.onDestroy();
	}

	private void showInfoDialog() {
		infoMessageDialog(R.string.info_title);
		playAlertSound(WARNING_SOUND_URI);
		vibrator();
	}
	
	private void showFailDialog() {
		failMessageDialog(R.string.fail_title);
		playAlertSound(WARNING_SOUND_URI);
		vibrator();
	}
	
	private void failMessageDialog(int titleResId) {
		final AlertController.AlertParams p = mAlertParams;
		p.mTitle = getString(titleResId);
		p.mView = createFailView();
		p.mPositiveButtonText = getString(android.R.string.ok);
		p.mPositiveButtonListener = this;
		p.mNegativeButtonText = getString(android.R.string.cancel);
		p.mNegativeButtonListener = this;
		setupAlert();
	}
	
	private View createFailView() {
		View view = getLayoutInflater().inflate(R.layout.activity_fail_info,
				null);
		TextView failMsg = (TextView) view.findViewById(R.id.fail_message);
		failMsg.setText(getString(R.string.fail_message));
		return view;
	}

	/**
	 * 
	 * @param context
	 *            The Context that had been passed to
	 *            {@link #warningMessageDialog(Context, int, int, int)}
	 * @param titleResId
	 *            Set the title using the given resource id.
	 * @param messageResId
	 *            Set the message using the given resource id.
	 * @return Creates a {@link AlertDialog} with the arguments supplied to this
	 *         builder.
	 */
	private void infoMessageDialog(int titleResId) {
		final AlertController.AlertParams p = mAlertParams;
		p.mTitle = getString(titleResId);
		p.mView = createInfoView();
		p.mPositiveButtonText = getString(R.string.agree);
		p.mPositiveButtonListener = this;
		p.mNegativeButtonText = getString(android.R.string.cancel);
		p.mNegativeButtonListener = this;
		setupAlert();
	}

	private View createInfoView() {
		View view = getLayoutInflater().inflate(R.layout.activity_boot_info,
				null);
		String noFile = getString(R.string.no_file);
		String notSet = getString(R.string.not_set);
		TextView log = (TextView) view.findViewById(R.id.log);
		log.setText(getString(R.string.logo, (mLog == null ? noFile : mLog)));
		TextView brand = (TextView) view.findViewById(R.id.brand);
		brand.setText(getString(R.string.brand, (mBrand == null ? notSet
				: mBrand)));
		TextView model = (TextView) view.findViewById(R.id.model);
		model.setText(getString(R.string.model, (mModel == null ? notSet
				: mModel)));
		TextView powerDownAnimation = (TextView) view
				.findViewById(R.id.power_down_animation);
		powerDownAnimation.setText(getString(R.string.power_down_animation,
				(mPowerDownAnimation == null ? noFile : mPowerDownAnimation)));
		TextView powerOnAnimation = (TextView) view
				.findViewById(R.id.power_on_animation);
		powerOnAnimation.setText(getString(R.string.power_on_animation,
				(mPowerOnAnimation == null ? noFile : mPowerOnAnimation)));
		TextView powerDownRing = (TextView) view
				.findViewById(R.id.power_down_ring);
		powerDownRing.setText(getString(R.string.power_down_ring,
				(mPowerDownRing == null ? noFile : mPowerDownRing)));
		TextView powerOnRing = (TextView) view.findViewById(R.id.power_on_ring);
		powerOnRing.setText(getString(R.string.power_on_ring,
				(mPowerOnRing == null ? noFile : mPowerOnRing)));
		return view;
	}

	/**
	 * 
	 * @param context
	 *            The Context that had been passed to
	 *            {@link #warningMessageDialog(Context, Uri)}
	 * @param defaultUri
	 */

	private void playAlertSound(Uri defaultUri) {

		if (defaultUri != null) {
			mRingtone = RingtoneManager.getRingtone(this, defaultUri);
			if (mRingtone != null) {
				mRingtone.setStreamType(AudioManager.STREAM_SYSTEM);
				mRingtone.play();
			}
		}
	}

	private void stopRingtone() {
		if (mRingtone != null) {
			mRingtone.stop();
		}
	}

	private void vibrator() {
		mVibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
		long[] pattern = { 500, 1000, 500, 1000 };
		mVibrator.vibrate(pattern, -1);
	}

	public void onClick(DialogInterface dialogInterface, int button) {
		Log.d(TAG, "onClick");
		if (button == DialogInterface.BUTTON_POSITIVE) {
			if (mType.equals(Utils.TYPE_INFO)) {
				stopRingtone();
				startService(true);
			} else {
				stopRingtone();
				startService(false);
			}
		} else {
			stopRingtone();
			startService(false);
		}
		mVibrator.cancel();
		finish();
	}

	private void startService(boolean ok) {
		Intent service = new Intent(this, BootAnimationService.class);
		service.putExtra(Utils.FROM_ACTIVITY_EXTRA, true);
		service.putExtra(Utils.STATE_EXTRA, ok);
		startService(service);
	}
}
