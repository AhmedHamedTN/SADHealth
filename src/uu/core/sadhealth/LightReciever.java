package uu.core.sadhealth;


import com.crashlytics.android.Crashlytics;

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

public class LightReciever extends BroadcastReceiver {

	private static final String TAG = "Light_Scheduled";
	Boolean isFar=false;
	@Override
	public void onReceive(Context context, Intent intent) {
		Intent lightIntent = new Intent(context, LightService.class);
		/*SensorManager sensorManager = (SensorManager) context.getSystemService(context.SENSOR_SERVICE);
		final Sensor sensorProximity = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
		
		SensorEventListener proximityListener = new SensorEventListener() {
    		public void onSensorChanged(SensorEvent event) {
    			if (event.sensor.getType()==Sensor.TYPE_PROXIMITY){
    				Log.i(TAG,event.values[0]+""); 
    				if (event.values[0] < sensorProximity.getMaximumRange()) {
    					isFar=false;
    				}
    				else{
    					isFar=true;
    				}
    			}
    		}
			public void onAccuracyChanged(Sensor sensor, int accuracy) {
			}
		};*/
		
		
		// register proximity sensor just before light sensor expires
        
    		//sensorManager.registerListener(proximityListener,sensorProximity, SensorManager.SENSOR_DELAY_FASTEST);
        
        
        
        	
    		//Log.i(TAG,"is far "+isFar); 
    		//if (isFar){
    			Log.i(TAG,"call light service"); 
    			Intent StatusUpdateIntent = new Intent();
            	//StatusUpdateIntent.putExtra("running", true);
            	//StatusUpdateIntent.putExtra("username", userName);
            	
            	StatusUpdateIntent.putExtra("updateStatus", "                    Collecting Light Data             ");
            	StatusUpdateIntent.setAction(MainService2.TO_MAINACTIVITY);
            	context.sendBroadcast(StatusUpdateIntent);
				context.startService(lightIntent);
				//lightSensing=true;
				
				Timer timer = null;
				timer = new Timer();
				
				ServiceStopTask myTimerTask =new ServiceStopTask(context);
				
				timer.schedule(myTimerTask, 5000);
				
    		
		

				//sensorManager.unregisterListener(proximityListener);
        }            	
        //stop light sensor
        /*if ((lightSensingTimer == 0) && (lightSensing) ){
        	Log.i(TAG,"call light service to stop");
        	Intent StatusUpdateIntent = new Intent();
        	StatusUpdateIntent.putExtra("running", true);
        	StatusUpdateIntent.putExtra("username", userName);
        	
        	StatusUpdateIntent.putExtra("updateStatus", "               Stopped Collecting Light Data             ");
        	StatusUpdateIntent.setAction(TO_MAINACTIVITY);
        	sendBroadcast(StatusUpdateIntent);
        	stopService(lightIntent);
        	lightSensing=false;
        	lightSensingTimer= 5;
        }*/
		
		
		
		
//	}
	
}

class ServiceStopTask extends TimerTask {

	private static final String TAG = "stopTimer";
	private Context cntx=null;
	  public ServiceStopTask(Context c) {
		cntx=c;
	}

	@Override
	  public void run() {
		
		Log.i(TAG,"call light service to stop");
		  Intent serviceIntent = new Intent(cntx, LightService.class);
			cntx.stopService(serviceIntent);
	  
	 }
}
