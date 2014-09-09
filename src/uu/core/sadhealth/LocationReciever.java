package uu.core.sadhealth;


import com.crashlytics.android.Crashlytics;

import uu.core.sadhealth.services.CrowdSourceService;
import uu.core.sadhealth.services.LocationService;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import android.util.Log;


public class LocationReciever extends BroadcastReceiver {

	private static final String TAG = "Location_Scheduled";

	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		Intent locIntent;
		Log.i(TAG,"call location service");
    	//Intent StatusUpdateIntent = new Intent();
    	//StatusUpdateIntent.putExtra("running", true);
    	//StatusUpdateIntent.putExtra("username", userName);
    	
    	//StatusUpdateIntent.putExtra("updateStatus", "                 Collecting Location Data             ");
    	//.setAction(TO_MAINACTIVITY);
    	//sendBroadcast(StatusUpdateIntent);
    	
    	locIntent= new Intent(context, LocationService.class);
    	context.startService(locIntent);
		
	}
}
