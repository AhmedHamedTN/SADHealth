/**ActivityRecognitionScan.java
 * Taken from this tutorial: http://opensignal.com/blog/2013/05/16/getting-started-with-activity-recognition-android-developer-guide/
 * It is responsible for connecting to google play services' Activity recognition feature
 * Allows for classification of specific activities based on Accellerometer data.
 * @author Kiril Tzvetanov Goguev
 * @date February 1st, 2014
 */
package uu.core.sadhealth.services;

import java.io.FileWriter;
import java.io.IOException;

import uu.core.sadhealth.utils.AppPreferences;
import uu.core.sadhealth.utils.DateFormatUtils;
import uu.core.sadhealth.utils.FileToWrite;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.ActivityRecognitionClient;
import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;

import android.app.IntentService;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

public class ActivityRecognitionService extends Service implements GooglePlayServicesClient.ConnectionCallbacks,GooglePlayServicesClient.OnConnectionFailedListener
{
	

	private static final String TAG="ActivityRecognitionService";
	private static ActivityRecognitionClient mAC;
	private Context c;
	private static PendingIntent callbackIntent;
	private AppPreferences _appPrefs;
	private int periodNoInt;

	
	public void onCreate(){
		super.onCreate();
		Log.i(TAG,"started");
		c=getApplicationContext();
		_appPrefs = new AppPreferences(c);
		periodNoInt=_appPrefs.getPeriodNo();
		
		invokeActivityScanner();
		
		
	}
	
	@Override
    public void onDestroy() 
    {
		super.onDestroy();  
		Log.i(TAG,"stopped");
		stopActivityScanner();
    }
	
	public void invokeActivityScanner()
	{
		Log.i(TAG, "invokeActivityScanner");
		mAC=new ActivityRecognitionClient(c,this,this);
		
		mAC.connect();
		
	}
	
	public void stopActivityScanner()
	{
			mAC.removeActivityUpdates(callbackIntent);
		
		stopSelf();
	}

	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onConnectionFailed(ConnectionResult result) {
		Log.w(TAG, "Connection to Google Activity Recognition Failed!");
		
	}

	@Override
	public void onConnected(Bundle connectionHint) {
		Log.i(TAG, "onConnected");
		Intent intent = new Intent(c, ActivityRecognitionIntentService.class);
		callbackIntent = PendingIntent.getService(c, 0, intent,
		PendingIntent.FLAG_UPDATE_CURRENT);
		mAC.requestActivityUpdates(120000, callbackIntent);
		//startService(intent);
		
		
	}

	@Override
	public void onDisconnected() {
		// TODO Auto-generated method stub
		
	}
	
	
	
	
private boolean servicesConnected() {
    	
        // Check that Google Play services is available
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        
        // If Google Play services is available
        if (ConnectionResult.SUCCESS == resultCode) {
            return true;
        } else {
 
            return false;
        }
    }
	
	
	
}