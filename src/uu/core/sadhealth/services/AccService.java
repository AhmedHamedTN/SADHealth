/** AccService.java
 * This class is part of the services that collect data from the MainService
 * It is responsible for collecting Accelerometer data 
 * @Author (Original) Kamyar Niroumand, (Modified)Kiril Tzvetanov Goguev
 * @Date January 30th,2014
 */
package uu.core.sadhealth.services;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import uu.core.sadhealth.utils.AppPreferences;
import uu.core.sadhealth.utils.FileToWrite;
import uu.core.sadhealth.utils.StatComputation;
import uu.core.sadhealth.MainActivity;

import android.app.Service;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;

public class AccService extends Service {
	
	private final String TAG = "AccService";
	private FileWriter accelerometerWriter;
	private FileWriter rawAccelerometerWriter;
	Sensor sensorAccelerometer, sensorRawAccelerometer;
	SensorManager sensorManager;
	SensorEventListener accelerometerListener, rawAccelerometerListener;
	List<Double> value = new ArrayList<Double>(); 
	public long systemBooted;
	private String periodNo;
	private AppPreferences _appPrefs;

	public void onCreate(){
		super.onCreate();
		Log.i(TAG,"started");
		setupAccelerometerPhone();
		setupRawAccelerometerPhone();
	}
	
	
	private void setupAccelerometerPhone(){
		_appPrefs = new AppPreferences(getApplicationContext());
		
		periodNo = String.format("%04d", _appPrefs.getPeriodNo());
		accelerometerWriter = FileToWrite.createLogFileWriter("phone_accelerometer"+periodNo+".csv",_appPrefs.getUserID());
		if(accelerometerWriter==null){
			Log.v(TAG, "Failed to open file for accelerometer log");
		}
		
		//Now request the system to provide accelerometer updates
		sensorManager=(SensorManager)getSystemService(SENSOR_SERVICE);
		sensorAccelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
		
		systemBooted = System.currentTimeMillis() - SystemClock.uptimeMillis();
		
		
		accelerometerListener = new SensorEventListener() {
			public void onAccuracyChanged(Sensor sensor, int accuracy){}
			//Called by the system when a sensor reading is available
			float accel;
			double accX,accY,accZ;
						
			public void onSensorChanged(SensorEvent event) {
				if (event.sensor.getType()==Sensor.TYPE_LINEAR_ACCELERATION){
					accX = event.values[0];
			        accY = event.values[1];
			        accZ = event.values[2];
			        accel = (float) Math.sqrt((double) (accX*accX + accY*accY + accZ*accZ));
			        value.add(Double.valueOf(accel));
	            }
			}
		};
		sensorManager.registerListener(accelerometerListener, sensorAccelerometer, SensorManager.SENSOR_DELAY_GAME);
	}
	
private void setupRawAccelerometerPhone(){
	_appPrefs = new AppPreferences(getApplicationContext());
	
	periodNo = String.format("%04d", _appPrefs.getPeriodNo());
		rawAccelerometerWriter = FileToWrite.createLogFileWriter("raw_accelerometer"+periodNo+".csv",_appPrefs.getUserID());
		if(rawAccelerometerWriter==null){
			Log.v(TAG, "Failed to open file for raw accelerometer log");
		}
		
		//Now request the system to provide accelerometer updates
		sensorManager=(SensorManager)getSystemService(SENSOR_SERVICE);
		sensorRawAccelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		
		systemBooted = System.currentTimeMillis() - SystemClock.uptimeMillis();
		
		
		rawAccelerometerListener = new SensorEventListener() {
			public void onAccuracyChanged(Sensor sensor, int accuracy){}
			//Called by the system when a sensor reading is available
			double accX,accY,accZ;
						
			public void onSensorChanged(SensorEvent event) {
				if (event.sensor.getType()==Sensor.TYPE_ACCELEROMETER){
					accX = event.values[0];
			        accY = event.values[1];
			        accZ = event.values[2];
			        long timestamp = event.timestamp /1000 /1000;
			        logRawAccelerometerReading(systemBooted + timestamp, accX, accY, accZ);
	            }
			}
		};
		sensorManager.registerListener(rawAccelerometerListener, sensorRawAccelerometer, SensorManager.SENSOR_DELAY_GAME);
	}
	
	private void stopAccelerometerPhone(){

		sensorManager.unregisterListener(accelerometerListener);	//Remove the accelerometer listener from the sensor manager
		sensorManager.unregisterListener(rawAccelerometerListener);	//Remove the raw accelerometer listener from the sensor manager

		logAccelerometerReading(System.currentTimeMillis(), java.lang.Math.abs(StatComputation.getMean(value)));
		try {	 //Close the file, and catch an IOException if it occurs  	
			accelerometerWriter.close();
			rawAccelerometerWriter.close();
		}
		catch (IOException e){
			Log.e(TAG, "Error closing phone raw accelerometer log file: "+e.toString());
		}
	}
	
	
	private boolean logAccelerometerReading(long l, double act) {
    	String fileText = l+","+String.valueOf(act)+"\n";
    	try {
			accelerometerWriter.append(fileText);
			accelerometerWriter.flush();
			return true;
		}
    	catch (IOException e) {
    		Log.e(TAG, "Could not write to phone accelerometer log file: "+e.toString());
    		return false;
		}
	}
	
	private boolean logRawAccelerometerReading(long l, double x, double y, double z) {
    	String fileText = l+ "," +x+ "," +y + "," +z+ "\n";
    	try {
			rawAccelerometerWriter.append(fileText);
			rawAccelerometerWriter.flush();
			return true;
		}
    	catch (IOException e) {
    		Log.e(TAG, "Could not write to phone accelerometer log file: "+e.toString());
    		return false;
		}
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
    public void onDestroy() 
    {
		super.onDestroy();  
		Log.i(TAG,"stopped");
        stopAccelerometerPhone();
    }

}