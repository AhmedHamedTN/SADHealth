package uu.core.sadhealth;


import com.crashlytics.android.Crashlytics;

import uu.core.sadhealth.services.AccService;
import uu.core.sadhealth.services.CrowdSourceService;
import uu.core.sadhealth.services.LightService;
import uu.core.sadhealth.services.LocationService;
import uu.core.sadhealth.services.MainService;
import uu.core.sadhealth.services.MainService2;
import uu.core.sadhealth.utils.AppPreferences;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

import android.support.v4.app.NotificationCompat;
import android.util.Log;

public class QuestionnaireReciever extends BroadcastReceiver {
	private AppPreferences _appPrefs ;
	private static final String TAG = "Questionnaire_Scheduled";
	
	@Override
	public void onReceive(Context context, Intent intent) {
		Intent notifyIntent;
		_appPrefs=new AppPreferences(context);

// check if it needs to ask about answering the questionnaire 
           
        	
        		Calendar c = Calendar.getInstance(); 
        		int hour = c.get(Calendar.HOUR_OF_DAY);
        		
        		//check time to avoid night notifications
        		if ((hour < 22) && (hour > 9)){

        			// Prepare intent which is triggered if the
        		    // notification is selected
        			notifyIntent = new Intent(context.getApplicationContext(), QuestionnaireActivity.class);
    				notifyIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    				Log.i(TAG, "notification activity for questionnaire");
    				notifyIntent.putExtra("Task", "Questionnaire");

    				PendingIntent pIntent = PendingIntent.getActivity(context.getApplicationContext(), 0, notifyIntent, 0);

    				// build notification
    				// the addAction re-use the same intent to keep the example short
    				NotificationCompat.Builder mBuilder =
    			    new NotificationCompat.Builder(context.getApplicationContext())
    				        .setContentTitle("Questionnare Time!")
    				        .setContentText("Click here to start the questionnare")
    				        .setSmallIcon(R.drawable.ic_launcher)
    				        .setContentIntent(pIntent)
    				        .setAutoCancel(true);
    				       
    				    
    				  
    				NotificationManager notificationManager = 
    				  (NotificationManager) context.getSystemService(context.NOTIFICATION_SERVICE);

    				notificationManager.notify(0, mBuilder.build()); 
        		
        		}
	}
            }
        	