package uu.core.sadhealth;


import com.crashlytics.android.Crashlytics;

import uu.core.sadhealth.services.AccService;
import uu.core.sadhealth.services.CrowdSourceService;
import uu.core.sadhealth.services.LightService;
import uu.core.sadhealth.services.LocationService;
import uu.core.sadhealth.services.MainService;
import uu.core.sadhealth.services.MainService2;
import uu.core.sadhealth.services.WeatherService;

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

public class WeatherReciever extends BroadcastReceiver {

	private static final String TAG = "Weather_Scheduled";
	
	@Override
	public void onReceive(Context context, Intent intent) {
		Intent weatherIntent= new Intent(context, WeatherService.class);
		
		
    		Log.i(TAG,"call weather service");
    		
    		//send a broadcast back to the main activity because the weather is in a fragment and fragments can only be updated from the UI thread(Main) since it uses fragment manager
    		//add running flag to intent
        	Intent WeatherUpdateIntent = new Intent();
        	WeatherUpdateIntent.putExtra("running", true);
        	//WeatherUpdateIntent.putExtra("username", userName);
        	WeatherUpdateIntent.putExtra("updateWeather", true);
        	WeatherUpdateIntent.putExtra("updateStatus", "                Collecting Weather Data             " );
        	WeatherUpdateIntent.setAction(MainService2.TO_MAINACTIVITY);
        	context.sendBroadcast(WeatherUpdateIntent);
        	context.startService(weatherIntent);
        	Timer timer = null;
			timer = new Timer();
        	ServiceStopWeatherTask myTimerTask =new ServiceStopWeatherTask(context);
			
			timer.schedule(myTimerTask, 60000);
        	
        	
        
		
	}
}

class ServiceStopWeatherTask extends TimerTask {

	private static final String TAG = "stopTimer";
	private Context cntx=null;
	  public ServiceStopWeatherTask(Context c) {
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
		  Intent serviceIntent = new Intent(cntx, WeatherService.class);
			cntx.stopService(serviceIntent);
	  
	 }
}