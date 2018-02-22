package com.example.deviceadminsample;

import android.app.admin.DeviceAdminReceiver;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.squareup.picasso.Picasso;


/**
 * Sample implementation of a DeviceAdminReceiver.  Your controller must provide one,
 * although you may or may not implement all of the methods shown here.
 *
 * All callbacks are on the UI thread and your implementations should not engage in any
 * blocking operations, including disk I/O.
 * adb shell dpm set-device-owner com.example.deviceadminsample/.DeviceAdminSampleReceiver
 */
public class DeviceAdminSampleReceiver extends DeviceAdminReceiver {
    public static String TAG = DeviceAdminSampleReceiver.class.getName();
    void showToast(Context context, String msg) {
        String status = context.getString(R.string.admin_receiver_status, msg);
        Toast.makeText(context, status, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction() == ACTION_DEVICE_ADMIN_DISABLE_REQUESTED) {
            abortBroadcast();
        }
        super.onReceive(context, intent);
    }

    private DevicePolicyManager mManager;

    @Override
    public void onEnabled(final Context context, Intent intent) {
        showToast(context, context.getString(R.string.admin_receiver_status_enabled));
        // https://developers.google.com/android/work/dpc/logging
        mManager =
                (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
        if ( mManager == null )
        {
            throw new IllegalStateException("Unable to get DevicePolicyManager");
        }
        if (mManager.isDeviceOwnerApp(context.getPackageName())) {
            mManager.setNetworkLoggingEnabled(getWho(context), true);
            Log.d(TAG, "!!! Network logging enabled !!!");
        }
        else
        {
            Toast.makeText(context, "This application is not device owner. DNS logging only works" +
                    " when this application is setup as the Device Owner", Toast.LENGTH_LONG).show();
        }
        /*
        AsyncTask<Void, Void, Void> as = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                while (true) {
                    try {
                        Thread.sleep(6000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    mManager.retrieveNetworkLogs(getWho(context), 0L);
                }
            }
        };
        as.execute(new Void[]{});
        */
    }

    @Override
    public void onNetworkLogsAvailable(Context context, Intent intent, long batchToken, int networkLogsCount) {
        super.onNetworkLogsAvailable(context, intent, batchToken, networkLogsCount);
        Toast.makeText(context, "!!! onNetworkLogsAvailable !!!", Toast.LENGTH_LONG).show();
        Log.d(TAG, "!!! HERE !!! " + networkLogsCount);
    }

    @Override
    public CharSequence onDisableRequested(Context context, Intent intent) {
        return context.getString(R.string.admin_receiver_status_disable_warning);
    }

    @Override
    public void onDisabled(Context context, Intent intent) {
        showToast(context, context.getString(R.string.admin_receiver_status_disabled));
    }

    @Override
    public void onPasswordChanged(Context context, Intent intent) {
        showToast(context, context.getString(R.string.admin_receiver_status_pw_changed));
    }

    @Override
    public void onPasswordFailed(Context context, Intent intent) {
        showToast(context, context.getString(R.string.admin_receiver_status_pw_failed));
    }

    @Override
    public void onPasswordSucceeded(Context context, Intent intent) {
        showToast(context, context.getString(R.string.admin_receiver_status_pw_succeeded));
    }

    @Override
    public void onPasswordExpiring(Context context, Intent intent) {
        DevicePolicyManager dpm = (DevicePolicyManager) context.getSystemService(
                Context.DEVICE_POLICY_SERVICE);
        long expr = dpm.getPasswordExpiration(
                new ComponentName(context, DeviceAdminSampleReceiver.class));
        long delta = expr - System.currentTimeMillis();
        boolean expired = delta < 0L;
        String message = context.getString(expired ?
                R.string.expiration_status_past : R.string.expiration_status_future);
        showToast(context, message);
        Log.v(TAG, message);
    }
}