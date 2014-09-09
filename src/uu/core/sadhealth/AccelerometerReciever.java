package uu.core.sadhealth;


import com.crashlytics.android.Crashlytics;

import uu.core.sadhealth.services.AccService;
import uu.core.sadhealth.services.CrowdSourceService;
import uu.core.sadhealth.services.LightService;
import uu.core.sadhealth.services.LocationService;
import uu.core.sadhealth.services.MainService;
import uu.core.sadhealth.services.MainService2;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import java.util.Timer;
import java.util.TimerTask;

import android.util.Log;

public class AccelerometerReciever extends BroadcastReceiver {

	private static final String TAG = "Accelerometer_Scheduled";
	
	@Override
	public void onReceive(Context context, Intent intent) {
		Intent accIntent= new Intent(context, AccService.class);
		
         	//start accelerometer service
         	Log.i(TAG,"call accelerometer service");
         	Intent StatusUpdateIntent = new Intent();
         	StatusUpdateIntent.putExtra("running", true);
         	//StatusUpdateIntent.putExtra("username", userName);
         	StatusUpdateIntent.putExtra("updateStatus", "               Collecting Accelerometer Data             ");
         	StatusUpdateIntent.setAction(MainService2.TO_MAINACTIVITY);
         	context.sendBroadcast(StatusUpdateIntent);
         	context.startService(accIntent);
         	Timer timer = null;
			timer = new Timer();
			
			ServiceStopAccelTask myTimerTask =new ServiceStopAccelTask(context);
			
			timer.schedule(myTimerTask, 60000);
		
        }            	
        

	
}

class ServiceStopAccelTask extends TimerTask {

	private static final String TAG = "stopTimer";
	private Context cntx=null;
	  public ServiceStopAccelTask(Context c) {
		cntx=c;
	}

	@Override
	  public void run() {
		
		Log.i(TAG,"call accelerometer service to stop");
     	Intent StatusUpdateIntent = new Intent();
     	StatusUpdateIntent.putExtra("running", true);
     	//StatusUpdateIntent.putExtra("username", userName);
     	StatusUpdateIntent.putExtra("updateStatus", "                   Service is Running                  ");
     	StatusUpdateIntent.setAction(MainService2.TO_MAINACTIVITY);
     	cntx.sendBroadcast(StatusUpdateIntent);
		  Intent serviceIntent = new Intent(cntx, AccService.class);
			cntx.stopService(serviceIntent);
	  
	 }
}
