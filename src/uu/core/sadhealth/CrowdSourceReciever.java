package uu.core.sadhealth;


import com.crashlytics.android.Crashlytics;

import uu.core.sadhealth.services.CrowdSourceService;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import android.util.Log;


public class CrowdSourceReciever extends BroadcastReceiver {

	private static final String TAG = "CS_Scheduled";

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.i(TAG,"scheduled CS check folder!");
		Intent csIntent = new Intent(context.getApplicationContext(), CrowdSourceService.class);
			context.startService(csIntent);
		
	}
}
