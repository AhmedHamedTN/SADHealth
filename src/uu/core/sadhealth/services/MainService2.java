package uu.core.sadhealth.services;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import com.crashlytics.android.Crashlytics;

import uu.core.sadhealth.utils.AppPreferences;
import uu.core.sadhealth.utils.DateFormatUtils;
import uu.core.sadhealth.utils.FileToWrite;
import uu.core.sadhealth.AccelerometerReciever;
import uu.core.sadhealth.CrowdSourceReciever;
import uu.core.sadhealth.LightReciever;
import uu.core.sadhealth.LocationReciever;
import uu.core.sadhealth.MainActivity;
import uu.core.sadhealth.NetworkReciever;
import uu.core.sadhealth.QuestionnaireActivity;
import uu.core.sadhealth.QuestionnaireReciever;
import uu.core.sadhealth.R;
import uu.core.sadhealth.ResetReciever;

import uu.core.sadhealth.UploadActivity;
import uu.core.sadhealth.R.drawable;
import uu.core.sadhealth.WeatherReciever;



import android.app.AlarmManager;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.BatteryManager;
import android.os.Environment;
import android.os.IBinder;
import android.os.PowerManager;
import android.preference.ListPreference;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

public class MainService2 extends Service {
	
	private final String TAG = "MainService2";
	
	//Create shared preferences so we can store valuable data related to the app
	private AppPreferences _appPrefs;
		
	
	private static Timer timer; 
    private static int currentTime, accelerometerTimer, accelerometerRecordingTime,crowdSourceTimer,activityRecordingTime, weatherRecordingTime, questionnaireTimer, locationTimer, lightTimer, activityRecognitionTimer;
    private static int lightSensingTimer;
    private static int networkStatusTimer;
    private static int weatherDataTimer;
    private static boolean lightSensing;
    private static boolean isFar;
    private static int level,scale;
    private static float batteryPerc;
    private static int periodNo;
    public static int unlockNo;
    private static int beginTime;
    private static String userName;
    private static int accelerometerTimerValue,lightSensorTimerValue, networkStatusTimerValue, lengthOfDataPartition, locationTimerValue, goodWifiStrength, questionnaireTimerValue, weatherDataTimerValue;
    private static int activityRecognitionTimerValue,crowdSourceTimerValue;
    private static float enoughBatteryPerc;
    private static int uploadCounter;
    public static boolean uploading = false;
    public static boolean manualUploadingIndicator=false;
    
    private boolean dc_Light, dc_Accelerometer, dc_Location;
    private int du_NetworkMode;
    private boolean QuestionnareShow;
    
	private String location;
	private SimpleDateFormat sdf;

   
    public static final String TO_MAINACTIVITY ="uu.core.sadhealth.mybroadcast";
    Context cntx; 
    Intent lightIntent, accIntent, locIntent, notifyIntent, weatherIntent, actRecIntent, lightAlarmTest;
    
    FileWriter unlockNoWriter,lightSourceWriter;
    
	@SuppressWarnings("deprecation")
	public void onCreate(){
		super.onCreate();
		int currentPeriodNo;
		Log.i(TAG,"created");
		
		//fetch the user name
		
		sdf = new SimpleDateFormat("yyyy/MM/dd");
		
		//obtain preferences
		_appPrefs = new AppPreferences(getApplicationContext());
		userName = _appPrefs.getUserID();
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
	
		Log.i(TAG,"network mode is "+sharedPreferences.getString("Data_UploadConfig", "0"));
	
		_appPrefs.setDUNetworkMode(Integer.valueOf(sharedPreferences.getString("Data_UploadConfig", "0")));
		
		_appPrefs.setQuestionnareShow(sharedPreferences.getBoolean("QuestionareOptin", true));
		
		dc_Light =_appPrefs.getDCLight();
		dc_Accelerometer=_appPrefs.getDCAccelerometer();
		dc_Location=_appPrefs.getDCLocation();
		
		du_NetworkMode=_appPrefs.getDUNetworkMode();
		uploadCounter=_appPrefs.getUploadCounter();
		
		QuestionnareShow=_appPrefs.getQuestionnareShow();
		
		//make sure service will not get killed
		Intent i=new Intent(this, MainActivity.class);
		i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
		PendingIntent pi=PendingIntent.getActivity(this, 0, i, 0);
		
		
		Notification note = new Notification(R.drawable.ic_launcher, "SADHealth Service is Running", System.currentTimeMillis());
		note.setLatestEventInfo(this, "SADHealth","Logged in as: "+ userName, pi);
		
		//calculate the next upload time at the start of the service
		calculateNexUploadTime();
		//clear the number of uploads for this service run
		_appPrefs.setUploadCounter(0);
		_appPrefs.setALSTime("00:00:00");
		_appPrefs.setNLSTime("00:00:00");
		
		startForeground(1, note);
		
		//reset the timer and initializing the parameters
		timer = new Timer();
		currentTime=0;
		lightTimer=2;
		lightSensing=false;
		isFar = true;
		lightSensingTimer=5;
		accelerometerTimer=0;
		locationTimer=5;
		activityRecognitionTimer=20;
		networkStatusTimer=10815;//21600; //6hours
		questionnaireTimer=60;
		weatherDataTimer=10;
		crowdSourceTimer=80;
		
		
		//set the timer lengths
		lengthOfDataPartition = 86400;//10780;//86400;//every 1 day//172800;  //two days
		crowdSourceTimerValue=10925;
		networkStatusTimerValue = 10815; //3 hours
		accelerometerTimerValue = 110; 
		accelerometerRecordingTime = 10;
		weatherRecordingTime=10;
		locationTimerValue = 3600; //1 hours
		activityRecordingTime=20;
		activityRecognitionTimerValue=300;//5mins
		goodWifiStrength = 7;
		lightSensorTimerValue = 600; // 10 min
		enoughBatteryPerc = 0.7f;
		questionnaireTimerValue = 28800; //8 hours
		weatherDataTimerValue=10785; // every 3 hrs
		
		
		//get the context and declare intents
		cntx = getApplicationContext();
		lightIntent = new Intent(cntx, LightService.class);
		accIntent= new Intent(cntx, AccService.class);
		locIntent= new Intent(cntx, LocationService.class);
		weatherIntent = new Intent(cntx,WeatherService.class);
		actRecIntent = new Intent(cntx,ActivityRecognitionService.class);
		lightAlarmTest = new Intent(cntx, LightReciever.class);
		
		//Register the broadcast receiver for presence of user
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(android.content.Intent.ACTION_USER_PRESENT);
		intentFilter.addAction(android.content.Intent.ACTION_BATTERY_CHANGED);
		registerReceiver(Receiver, intentFilter);
		 currentPeriodNo = Integer.parseInt(DateFormatUtils.getPeriodNo(System.currentTimeMillis()));
		
		
		
		periodNo=getPeriodNo();  // retrieve the current period from the saved data
		if (periodNo != currentPeriodNo) //check if the current period from saved data is not the correct one
		{
			unlockNo=0; 
			_appPrefs.setPeriodNo(currentPeriodNo); //set the correct one and save it
			periodNo=getPeriodNo();
			
			DateTime dt= new DateTime();
			DateTime fixedDt=dt.minusDays(1);
			 DateTimeFormatter sdfx = DateTimeFormat.forPattern("d");
			 
			 
			 MainService2.unlockNo=0;
			 Log.i("light max","writing out max light for yesterday");
				try{
					FileWriter maxlightWriter = FileToWrite.createLogFileWriter("maxlight.csv",_appPrefs.getUserID()); //file writer which overwrites 
					maxlightWriter.append(String.valueOf(dt.getYear())+","+String.valueOf(dt.getMonthOfYear())+","+sdfx.print(fixedDt)+","+fixedDt.toString("MM/dd/yyyy") +","+_appPrefs.getMaxLux()+"\n");
					maxlightWriter.flush();
					maxlightWriter.close();
				}
				catch(IOException e){

				}
			 
			 //MainService.dataPartitioning();
			Log.i("light sources","writing out light sources");
			try{
				FileWriter lightSourceWriter = FileToWrite.createLogFileWriter("lightSources.csv",_appPrefs.getUserID()); //file writer which overwrites 
				lightSourceWriter.append(sdf.format(new Date(System.currentTimeMillis()))+"," +_appPrefs.getALSTime()+","+_appPrefs.getNLSTime()+"\n");
				lightSourceWriter.flush();
				lightSourceWriter.close();
			}
			catch(IOException e){

			}
			_appPrefs.setMaxLux((float)0.0);
			_appPrefs.updateVehicleTimer(000000);
			_appPrefs.updateBikeTimer(000000);
			_appPrefs.updateFootTimer(000000);
			_appPrefs.updateStillTimer(000000);
			_appPrefs.updateTiltingTimer(000000);
			_appPrefs.setALSTime("00:00:00");
			_appPrefs.setNLSTime("00:00:00");
			
		}
		Log.i(TAG,String.format("value = %d", periodNo));
		
		unlockNo=0;
		beginTime = 0;
		scheduleResetAlarm();
		scheduleServiceAlarms();
		//MainService
		//startService();
	}
	
	

	private void scheduleServiceAlarms() {
		// TODO Auto-generated method stub
		Log.i(TAG,"CrowdSource Scheduled");
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(System.currentTimeMillis());
		calendar.set(Calendar.SECOND, 80);
		//calendar.set(Calendar.MINUTE, 0);
		//calendar.set(Calendar.HOUR, 0);
		//calendar.set(Calendar.AM_PM, Calendar.AM);
		//calendar.add(Calendar.DAY_OF_MONTH, 1);
		
		Intent intentAlarm = new Intent(this, CrowdSourceReciever.class);
		AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
		//alarmManager.set(AlarmManager.RTC_WAKEUP,calendar.getTimeInMillis(), PendingIntent.getBroadcast(this,1,  intentAlarm, PendingIntent.FLAG_UPDATE_CURRENT));
		alarmManager.setRepeating(AlarmManager.RTC, calendar.getTimeInMillis(), 1000*60*200, PendingIntent.getBroadcast(this,1,  intentAlarm, PendingIntent.FLAG_UPDATE_CURRENT));
		
		Calendar networkCal = Calendar.getInstance();
		int hour=networkCal.get(Calendar.HOUR_OF_DAY);
        do
        {
        	networkCal.add(Calendar.HOUR_OF_DAY, 3);
        	hour=networkCal.get(Calendar.HOUR_OF_DAY);
        }while(hour >22 ||hour <8);
		
		SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
        String nextUploadTime = sdf.format(networkCal.getTime());
        
        Log.i(TAG,"The next upload time  is calculated-D: "+nextUploadTime);
		
		Intent networkAlarm = new Intent(this, NetworkReciever.class);
		AlarmManager alarmManagerNetwork = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
		//alarmManager.set(AlarmManager.RTC_WAKEUP,calendar.getTimeInMillis(), PendingIntent.getBroadcast(this,1,  intentAlarm, PendingIntent.FLAG_UPDATE_CURRENT));
		alarmManagerNetwork.setRepeating(AlarmManager.RTC_WAKEUP, networkCal.getTimeInMillis(), 1000*60*200, PendingIntent.getBroadcast(this,1,  networkAlarm, PendingIntent.FLAG_UPDATE_CURRENT));
		
		calendar = Calendar.getInstance();
		calendar.setTimeInMillis(System.currentTimeMillis());
		calendar.set(Calendar.SECOND, 5);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.HOUR, 0);
		
		Intent locationAlarm = new Intent(this, LocationReciever.class);
		AlarmManager alarmManagerLocation = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
		//alarmManager.set(AlarmManager.RTC_WAKEUP,calendar.getTimeInMillis(), PendingIntent.getBroadcast(this,1,  intentAlarm, PendingIntent.FLAG_UPDATE_CURRENT));
		alarmManagerLocation.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), 1000*60*200, PendingIntent.getBroadcast(this,1,  locationAlarm, PendingIntent.FLAG_UPDATE_CURRENT));
		
		calendar = Calendar.getInstance();
		calendar.setTimeInMillis(System.currentTimeMillis());
		calendar.set(Calendar.SECOND, 5);
		//calendar.set(Calendar.MINUTE, 10);
		//calendar.set(Calendar.HOUR, 0);
		
		Intent lightAlarm = new Intent(this, LightReciever.class);
		AlarmManager alarmManagerlight = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
		//alarmManager.set(AlarmManager.RTC_WAKEUP,calendar.getTimeInMillis(), PendingIntent.getBroadcast(this,1,  intentAlarm, PendingIntent.FLAG_UPDATE_CURRENT));
		alarmManagerlight.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), 1000*60*10, PendingIntent.getBroadcast(this,1,  lightAlarm, PendingIntent.FLAG_UPDATE_CURRENT));
		
		calendar = Calendar.getInstance();
		calendar.setTimeInMillis(System.currentTimeMillis());
		calendar.set(Calendar.SECOND, 5);
		//calendar.set(Calendar.MINUTE, 10);
		//calendar.set(Calendar.HOUR, 0);
		
		Intent accelerometerAlarm = new Intent(this, AccelerometerReciever.class);
		AlarmManager alarmManagerAccelerometer= (AlarmManager) getSystemService(Context.ALARM_SERVICE);
		//alarmManager.set(AlarmManager.RTC_WAKEUP,calendar.getTimeInMillis(), PendingIntent.getBroadcast(this,1,  intentAlarm, PendingIntent.FLAG_UPDATE_CURRENT));
		alarmManagerAccelerometer.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), 70000, PendingIntent.getBroadcast(this,1,  accelerometerAlarm, PendingIntent.FLAG_UPDATE_CURRENT));
		calendar = Calendar.getInstance();
		calendar.setTimeInMillis(System.currentTimeMillis());
		calendar.set(Calendar.SECOND, 2);
		//calendar.set(Calendar.MINUTE, 10);
		//calendar.set(Calendar.HOUR, 0);
		
		Intent weatherAlarm = new Intent(this, WeatherReciever.class);
		AlarmManager alarmManagerWeather= (AlarmManager) getSystemService(Context.ALARM_SERVICE);
		//alarmManager.set(AlarmManager.RTC_WAKEUP,calendar.getTimeInMillis(), PendingIntent.getBroadcast(this,1,  intentAlarm, PendingIntent.FLAG_UPDATE_CURRENT));
		alarmManagerWeather.setRepeating(AlarmManager.RTC, calendar.getTimeInMillis(), 1000*60*300, PendingIntent.getBroadcast(this,1,  weatherAlarm, PendingIntent.FLAG_UPDATE_CURRENT));
		
		if(_appPrefs.getQuestionnareShow())
		{
		Log.i(TAG," Questionnaire alarm being scheduled");
		calendar = Calendar.getInstance();
			calendar.setTimeInMillis(System.currentTimeMillis());
			calendar.set(Calendar.SECOND,35);
			//calendar.set(Calendar.MINUTE, 10);
			//calendar.set(Calendar.HOUR, 0);
			
		Intent questionnaireAlarm = new Intent(this, QuestionnaireReciever.class);
		AlarmManager alarmManagerQuestionnaire= (AlarmManager) getSystemService(Context.ALARM_SERVICE);
		//alarmManager.set(AlarmManager.RTC_WAKEUP,calendar.getTimeInMillis(), PendingIntent.getBroadcast(this,1,  intentAlarm, PendingIntent.FLAG_UPDATE_CURRENT));
		alarmManagerQuestionnaire.setRepeating(AlarmManager.RTC, calendar.getTimeInMillis(), 1000*60*24, PendingIntent.getBroadcast(this,1,  questionnaireAlarm, PendingIntent.FLAG_UPDATE_CURRENT));
		}
	}



	private void startService() 
    {         
		Log.i(TAG,"started");
        
		//call TimeCounter to send current time every second
        timer.scheduleAtFixedRate(new TimeCounter(), 0, 1000);
    }
	
	// counts the time and schedule other tasks
	private class TimeCounter extends TimerTask
    { 
		
		public PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		public PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,"");
		
		SensorManager sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
		Sensor sensorProximity = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
		
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
		};

		@Override
		public void run() {
			// TODO Auto-generated method stub
			
		}
		
		
		
           
    }
	
  	BroadcastReceiver Receiver = new BroadcastReceiver(){
		@Override
		public void onReceive (Context arg0, Intent i) {
			
		
			// if an unlock happens
			if ( i.getAction().equals(Intent.ACTION_USER_PRESENT)){
				Log.i(TAG,"User Unlocked Screen");		
				startService(lightIntent);
				lightSensing=true;
				if(lightSensing)
				{
					Timer timer = null;
					timer = new Timer();
				
					ServiceStopTask myTimerTask =new ServiceStopTask(arg0);
					
					timer.schedule(myTimerTask, 5000);
				}
				unlockNo++; // count the number of unlocks
				try{
					unlockNoWriter = FileToWrite.createLogFileWriter("unlock_no"+String.format("%04d", periodNo)+".csv",_appPrefs.getUserID(),false); //file writer which overwrites 
					unlockNoWriter.append(System.currentTimeMillis()+"," +String.valueOf(unlockNo) );
					unlockNoWriter.flush();
					unlockNoWriter.close();
				}
				catch(IOException e){

				}
			}
			
			//if battery changed
			if ( i.getAction().equals(Intent.ACTION_BATTERY_CHANGED)){
				Log.i(TAG,"battery changed");		
				level = i.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
				scale = i.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
				batteryPerc = level / (float)scale;
				Log.i(TAG,"battery: "+ batteryPerc);
			}
		}
	};



	//destroy the service and timer and unregister the broadcast receiver
	@Override
	public void onDestroy() 
	{
		super.onDestroy();
		Log.i(TAG,"stopped");
		timer.cancel();
		stopService(accIntent);
		stopService(locIntent);
		stopService(lightIntent);
		stopForeground(true);
		unregisterReceiver(Receiver);
		
	}

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}
	
	
	
	private  int readUploadPeriodNo(){  // read the period No from the SD
		return _appPrefs.getPeriodNo();
	}
	
	private  int getPeriodNo(){  // read the period No from the SD
		return _appPrefs.getPeriodNo();
	}
	
	private  void writePeriodNo(){  // read the period No from the SD
		FileWriter periodNoWriter;
		try{
			periodNoWriter = FileToWrite.createLogFileWriter("period_no.csv",_appPrefs.getUserID(),false); //file writer which overwrites 
        	periodNoWriter.append(String.valueOf(periodNo));
        	periodNoWriter.flush();
        	periodNoWriter.close();
		    
		}
		catch (IOException e){
			e.printStackTrace();
		}
	}
	
	private void calculateNexUploadTime() {
		
		Calendar c = Calendar.getInstance(); 
		
		int hour=c.get(Calendar.HOUR_OF_DAY);
        do
        {
        	c.add(Calendar.HOUR_OF_DAY, 3);
        	hour=c.get(Calendar.HOUR_OF_DAY);
        }while(hour >22 ||hour <8);
        
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
        String nextUploadTime = sdf.format(c.getTime());
        
		Log.i(TAG,"The next upload time is "+nextUploadTime);
		
		_appPrefs.setNextUploadTime(nextUploadTime);
		
	}

	private void scheduleResetAlarm()
	{
	
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(System.currentTimeMillis());
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.HOUR, 0);
		calendar.set(Calendar.AM_PM, Calendar.AM);
		calendar.add(Calendar.DAY_OF_MONTH, 1);
		
		Intent intentAlarm = new Intent(this, ResetReciever.class);
		AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
		//alarmManager.set(AlarmManager.RTC_WAKEUP,calendar.getTimeInMillis(), PendingIntent.getBroadcast(this,1,  intentAlarm, PendingIntent.FLAG_UPDATE_CURRENT));
		alarmManager.setRepeating(AlarmManager.RTC, calendar.getTimeInMillis(), 1000*60*60*24, PendingIntent.getBroadcast(this,1,  intentAlarm, PendingIntent.FLAG_UPDATE_CURRENT));
		
	}
	
	
	
	
	public static void dataPartitioning(){
		try {
	    	File sdcard = Environment.getExternalStorageDirectory();
	    	
	    	File from= new File (sdcard, "sadhealth/users/"+ userName + "/phone_location.csv");
	    	File to = new File(sdcard,"sadhealth/users/"+ userName + "/phone_location"+String.format("%02d", periodNo)+ ".csv");
	    	from.renameTo(to);
	    	from.createNewFile();
	    	
	    	from= new File (sdcard, "sadhealth/users/"+ userName + "/phone_accelerometer.csv");
	    	to = new File(sdcard,"sadhealth/users/"+ userName + "/phone_accelerometer"+String.format("%02d", periodNo)+ ".csv");
	    	from.renameTo(to);
	    	from.createNewFile();
	    	
	    	from= new File (sdcard, "sadhealth/users/"+ userName + "/phone_lightsensor.csv");
	    	to = new File(sdcard,"sadhealth/users/"+ userName + "/phone_lightsensor"+String.format("%02d", periodNo)+ ".csv");
	    	from.renameTo(to);
	    	from.createNewFile();
	    	
	    	from= new File (sdcard, "sadhealth/users/"+ userName + "/unlock_no.csv");
	    	to = new File(sdcard,"sadhealth/users/"+ userName + "/unlock_no"+String.format("%02d", periodNo)+ ".csv");
	    	from.renameTo(to);
	    	from.createNewFile();
	    	
	    	from= new File (sdcard, "sadhealth/users/"+ userName + "/raw_accelerometer.csv");
	    	to = new File(sdcard,"sadhealth/users/"+ userName + "/raw_accelerometer"+String.format("%02d", periodNo)+ ".csv");
	    	from.renameTo(to);
	    	from.createNewFile();
	    	
	    	from= new File (sdcard, "sadhealth/users/"+ userName + "/questionnaire.csv");
	    	to = new File(sdcard,"sadhealth/users/"+ userName + "/questionnaire"+String.format("%02d", periodNo)+ ".csv");
	    	from.renameTo(to);
	    	from.createNewFile();
	    	
	    	from= new File (sdcard, "sadhealth/users/"+ userName + "/weather.csv");
	    	to = new File(sdcard,"sadhealth/users/"+ userName + "/weather"+String.format("%02d", periodNo)+ ".csv");
	    	from.renameTo(to);
	    	from.createNewFile();
	    	
	    	
	    	unlockNo=0;    	
	    	beginTime = currentTime;
	    	//periodNo++;
	    	
	    	//writePeriodNo();
			
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
		
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

	
}
