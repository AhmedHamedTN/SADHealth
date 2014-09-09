/**LightService.java
 * This class is part of the services package for SADHealth
 * It is responsible for invoking calls to the smartphone's hardware light sensor and recording values to a file.
 * @Author (Original) Kamyar Niroumand, (Modified) Kiril Tzvetanov Goguev
 * @Date January 21st, 2014
 */
package uu.core.sadhealth.services;

import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import uu.core.sadhealth.utils.AppPreferences;
import uu.core.sadhealth.utils.FileToWrite;
import uu.core.sadhealth.MainActivity;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

public class LightService extends Service{
	
	private final String TAG = "LightService";
	private FileWriter lightSensorWriter;
	Sensor lightSensor;
	SensorManager sensorManager;
	SensorEventListener lightSensorListener;
	private AppPreferences _appPrefs;
	private String periodNo;
	
	float lightValue=0;
	String timestamp;
	
	public void onCreate(){
		super.onCreate();
		Log.i(TAG,"started");
		setupLightSensorPhone();
	}
	
	private void setupLightSensorPhone(){	
		_appPrefs = new AppPreferences(getApplicationContext());
		periodNo = String.format("%04d", _appPrefs.getPeriodNo());
		lightSensorWriter = FileToWrite.createLogFileWriter("phone_lightsensor"+periodNo+".csv",_appPrefs.getUserID());
		if(lightSensorWriter==null){
			Log.v(TAG, "Failed to open file for light sensor log");
		}
		
		//Now request the system to provide light sensor updates
		sensorManager=(SensorManager)getSystemService(SENSOR_SERVICE);
		lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
		
		lightSensorListener = new SensorEventListener() {
			public void onAccuracyChanged(Sensor sensor, int accuracy){				
			}
			//Called by the system when a sensor reading is available
			public void onSensorChanged(SensorEvent event) {
				if (event.sensor.getType()==Sensor.TYPE_LIGHT){
					if (lightValue < event.values[0]){   //calculate the MAX value for the light
						lightValue = event.values[0];
					}
					
	            }
			}
		};
		sensorManager.registerListener(lightSensorListener, lightSensor, SensorManager.SENSOR_DELAY_FASTEST);
	}

	@SuppressLint("SimpleDateFormat")
	private void stopLightSensorPhone(){
		sensorManager.unregisterListener(lightSensorListener);	//Remove the light sensor listener from the sensor manager
		if(_appPrefs.getMaxLux()<lightValue)
		{
			_appPrefs.setMaxLux(lightValue);
		}
		// write the max value
		logRawLightSensorReading(System.currentTimeMillis(), lightValue);
		updateLightSourceTime(lightValue);
		
		try {	  	
			//Close the file, and catch an IOException if it occurs 
			lightSensorWriter.close();
		}
		catch (IOException e){
			Log.e(TAG, "Error closing phone light sensor log file: "+e.toString());
		}
	}
	
	private void updateLightSourceTime(float lightValue) {
		if(lightValue >=11 && lightValue <=50)
		{
			_appPrefs.setALSTime(calculateNewLightTime(_appPrefs.getALSTime()));
		}
		else if (lightValue >=51 && lightValue <=200)
		{
			_appPrefs.setALSTime(calculateNewLightTime(_appPrefs.getALSTime()));
		}
		else if (lightValue >=201 && lightValue <=400)
		{
			_appPrefs.setALSTime(calculateNewLightTime(_appPrefs.getALSTime()));
		}
		else if (lightValue >=1001 && lightValue <=5000)
		{
			_appPrefs.setALSTime(calculateNewLightTime(_appPrefs.getALSTime()));
		}
		else if (lightValue >=5001 && lightValue <=5500)
		{
			_appPrefs.setALSTime(calculateNewLightTime(_appPrefs.getALSTime()));
		}
		else if(lightValue >=5501)
		{
			_appPrefs.setNLSTime(calculateNewLightTime(_appPrefs.getNLSTime()));
		}
		
	}

	private boolean logRawLightSensorReading(long l, float val) {
    	String fileText = l+","+val+"\n";
    	try {
			lightSensorWriter.append(fileText);
			lightSensorWriter.flush();
			return true;
		}
    	catch (IOException e) {
    		Log.e(TAG, "Could not write to phone light sensor log file: "+e.toString());
    		return false;
		}
	}
	
	
	private String calculateNewLightTime(String time)
	{
		Calendar cal = Calendar.getInstance();
	    SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
	    try {
			cal.setTime(sdf.parse(time));
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}// all done
		
		cal.add(cal.SECOND, 5);
		cal.add(cal.MINUTE, 10);
        String newTime = sdf.format(cal.getTime());
		return newTime;
	}

	@Override
    public void onDestroy() 
    {
		super.onDestroy();
		Log.i(TAG,"stopped");
        stopLightSensorPhone();
          
    }
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

}
