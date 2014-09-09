package uu.core.sadhealth;

import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import com.crashlytics.android.Crashlytics;

import uu.core.sadhealth.services.MainService;
import uu.core.sadhealth.services.UploadService;
import uu.core.sadhealth.utils.AppPreferences;
import uu.core.sadhealth.utils.DateFormatUtils;
import uu.core.sadhealth.utils.FileToWrite;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

public class NetworkReciever extends BroadcastReceiver {
	private AppPreferences _appPrefs ;
	private int du_NetworkMode;
	private final String TAG = "NetworkReciever";
@Override
	public void onReceive(Context arg0, Intent arg1) {
	Intent notifyIntent;
	_appPrefs=new AppPreferences(arg0);
	Crashlytics.setString("Action-Upload", "Trying to Upload - NetworkMode is "+ String.valueOf(du_NetworkMode));
	du_NetworkMode=_appPrefs.getDUNetworkMode();
	if (du_NetworkMode==0)
	{
		try
		{
			Crashlytics.setString("Action-Upload-WIFI/MOBILE:", "Trying to Upload-WIFI/MOBILE");
			ConnectivityManager connMgr = (ConnectivityManager) arg0.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo wifi = connMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		    NetworkInfo mobile = connMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
			
			WifiManager wifiMgr;  
			NetworkInfo networkInfo;
			int wifiStrength;
			
			//check network status
    		networkInfo = connMgr.getActiveNetworkInfo();
    		
    		//priority wifi
    		if (wifi !=null && wifi.isAvailable())
    		{
    			//try wifi
    			if(wifi.isConnected())
    			{
    				
    				wifiMgr = (WifiManager) arg0.getSystemService (Context.WIFI_SERVICE);
        			//check if strength of signal is enough and there is enough battery
        			wifiStrength = WifiManager.calculateSignalLevel(wifiMgr.getConnectionInfo().getRssi(), 10);
        			Log.i(TAG, "wifi: "+ wifiStrength);
        			Crashlytics.setString("Wifi-Upload-Action", "Trying to Upload - SignalStrength"+String.valueOf(wifiStrength));
        			//if ( wifiStrength > goodWifiStrength && batteryPerc > enoughBatteryPerc )
    				
    					Calendar c = Calendar.getInstance(); 
                		int hour = c.get(Calendar.HOUR_OF_DAY);

                		//check time to avoid night notifications
                		if ((hour < 22) && (hour > 9))
                		{
                			Log.i(TAG, "notification activity for uploading");
                			
                			if (!MainService.uploading)
                			{
                				Intent uploadIntent = new Intent(arg0.getApplicationContext(), UploadService.class);
                	 			arg0.startService(uploadIntent);
                				
                				//Intent uploadIntent = new Intent(getApplicationContext(), UploadActivity.class);
                				//uploadIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                				//startActivity(uploadIntent);
                			}
                		}
    			}
    			else
    			{
    				//fall back on Mobile
    				if (mobile !=null && mobile.isAvailable())
            		{
            			Crashlytics.setString("Fall back action-Mobile", "Trying to Upload with MOBILE");
            			if(mobile.isConnected())
            			{
            					Log.i(TAG, "notification activity for uploading");
                    			
            					Calendar c = Calendar.getInstance(); 
                        		int hour = c.get(Calendar.HOUR_OF_DAY);

                        		//check time to avoid night notifications
                        		if ((hour < 22) && (hour > 9))
                        		{
                        			Log.i(TAG, "notification activity for uploading");
                        			
                        			if (!MainService.uploading)
                        			{
                        				Intent uploadIntent = new Intent(arg0.getApplicationContext(), UploadService.class);
                        	 			arg0.startService(uploadIntent);
                        				//Intent uploadIntent = new Intent(getApplicationContext(), UploadActivity.class);
                        				//uploadIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        				//startActivity(uploadIntent);
                        			}
                        		}
            			}
            		}
    				
    			}
    		}
    		else if (mobile !=null && mobile.isAvailable())
    		{
    			Crashlytics.setString("Upload Action-Mobile", "Trying to Upload with MOBILE");
    			if(mobile.isConnected())
    			{
    					Log.i(TAG, "notification activity for uploading");
            			
    					Calendar c = Calendar.getInstance(); 
                		int hour = c.get(Calendar.HOUR_OF_DAY);

                		//check time to avoid night notifications
                		if ((hour < 22) && (hour > 9))
                		{
                			Log.i(TAG, "notification activity for uploading");
                			
                			if (!MainService.uploading)
                			{
                				Intent uploadIntent = new Intent(arg0.getApplicationContext(), UploadService.class);
                	 			arg0.startService(uploadIntent);
                				//Intent uploadIntent = new Intent(getApplicationContext(), UploadActivity.class);
                				//uploadIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                				//startActivity(uploadIntent);
                			}
                		}
    				
    			}
    			else
    			{
    				// Prepare intent which is triggered if the
        		    // notification is selected
        			notifyIntent = new Intent(arg0.getApplicationContext(), UploadService.class);
    				//notifyIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    				Log.i(TAG, "notification activity for missed upload");
    				

    				PendingIntent pIntent = PendingIntent.getActivity(arg0.getApplicationContext(), 0, notifyIntent, 0);

    				// build notification
    				// the addAction re-use the same intent to keep the example short
    				NotificationCompat.Builder mBuilder =
            			    new NotificationCompat.Builder(arg0.getApplicationContext())
    				        .setContentTitle("Missed Upload")
    				        .setContentText("Mobile/WIFI network unavailable, reconnect and click to retry!")
    				        .setSmallIcon(R.drawable.file_upload)
    				        .setContentIntent(pIntent)
    				        .setAutoCancel(true);
    				       
    				    
    				  
    				NotificationManager notificationManager = 
    				  (NotificationManager) arg0.getSystemService(arg0.NOTIFICATION_SERVICE);

    				notificationManager.notify(0, mBuilder.build()); 
    			}
    		}
    		
			 
		}
		catch (Exception e)
		{
			
		}

		
	}
	else if (du_NetworkMode==1)
	{
		try
		{
			ConnectivityManager connMgr = (ConnectivityManager) arg0.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo wifi = connMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		    NetworkInfo mobile = connMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
		    
			
			WifiManager wifiMgr;  
			NetworkInfo networkInfo;
			int wifiStrength;
			
			//check network status
    		networkInfo = connMgr.getActiveNetworkInfo();
    		
    		//priority wifi
    		if (wifi !=null && wifi.isAvailable())
    		{
    			//try wifi
    			if(wifi.isConnected())
    			{
    				wifiMgr = (WifiManager) arg0.getSystemService (Context.WIFI_SERVICE);
        			//check if strength of signal is enough and there is enough battery
        			wifiStrength = WifiManager.calculateSignalLevel(wifiMgr.getConnectionInfo().getRssi(), 10);
        			Log.i(TAG, "wifi: "+ wifiStrength);
        			
    				//if ( wifiStrength > goodWifiStrength && batteryPerc > enoughBatteryPerc )
    				
    					Calendar c = Calendar.getInstance(); 
                		int hour = c.get(Calendar.HOUR_OF_DAY);

                		//check time to avoid night notifications
                		if ((hour < 22) && (hour > 9))
                		{
                			Log.i(TAG, "notification activity for uploading");
                			
                			if (!MainService.uploading)
                			{
                				Intent uploadIntent = new Intent(arg0.getApplicationContext(), UploadService.class);
                	 			arg0.startService(uploadIntent);
                				//Intent uploadIntent = new Intent(getApplicationContext(), UploadActivity.class);
                				//uploadIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                				//startActivity(uploadIntent);
                			}
                		}

    				}
    			else
    			{
    				// Prepare intent which is triggered if the
        		    // notification is selected
        			notifyIntent = new Intent(arg0.getApplicationContext(), UploadService.class);
    				notifyIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    				Log.i(TAG, "notification activity for missed upload");
    				

    				PendingIntent pIntent = PendingIntent.getActivity(arg0.getApplicationContext(), 0, notifyIntent, 0);

    				// build notification
    				// the addAction re-use the same intent to keep the example short
    				Notification n  = new Notification.Builder(arg0.getApplicationContext())
    				        .setContentTitle("Missed Upload")
    				        .setContentText("WIFI network unavailable, reconnect and click to retry!")
    				        .setSmallIcon(R.drawable.file_upload)
    				        .setContentIntent(pIntent)
    				        .setAutoCancel(true)
    				        .build();
    				    
    				  
    				NotificationManager notificationManager = 
    				  (NotificationManager) arg0.getSystemService(arg0.NOTIFICATION_SERVICE);

    				notificationManager.notify(0, n); 
    			}

    			
    		}
			 
		}
		catch (Exception e)
		{
			
		}
	}
	else if(du_NetworkMode==2)
	{
		try
		{
			ConnectivityManager connMgr = (ConnectivityManager) arg0.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo wifi = connMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		    NetworkInfo mobile = connMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
			
			WifiManager wifiMgr;  
			NetworkInfo networkInfo;
			int wifiStrength;
			
			//check network status
    		networkInfo = connMgr.getActiveNetworkInfo();
    		
    		if (mobile !=null && mobile.isAvailable())
    		{
    			if(mobile.isConnected())
    			{
    				//if(batteryPerc >enoughBatteryPerc)
    				
    					Log.i(TAG, "notification activity for uploading");
            			
    					Calendar c = Calendar.getInstance(); 
                		int hour = c.get(Calendar.HOUR_OF_DAY);

                		//check time to avoid night notifications
                		if ((hour < 22) && (hour > 9))
                		{
                			Log.i(TAG, "notification activity for uploading");
                			
                			if (!MainService.uploading)
                			{
                				Intent uploadIntent = new Intent(arg0.getApplicationContext(), UploadService.class);
                	 			arg0.startService(uploadIntent);
                				//Intent uploadIntent = new Intent(getApplicationContext(), UploadActivity.class);
                				//uploadIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                				//startActivity(uploadIntent);
                			}
                		}
    			}
    			else
    			{
    				// Prepare intent which is triggered if the
        		    // notification is selected
        			notifyIntent = new Intent(arg0.getApplicationContext(), UploadService.class);
    				notifyIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    				Log.i(TAG, "notification activity for missed upload");
    				

    				PendingIntent pIntent = PendingIntent.getActivity(arg0.getApplicationContext(), 0, notifyIntent, 0);

    				// build notification
    				// the addAction re-use the same intent to keep the example short
    				NotificationCompat.Builder mBuilder =
    			    new NotificationCompat.Builder(arg0.getApplicationContext())
    				        .setContentTitle("Missed Upload")
    				        .setContentText("Mobile network unavailable. Click here to retry!")
    				        .setSmallIcon(R.drawable.file_upload)
    				        .setContentIntent(pIntent)
    				        .setAutoCancel(true);
    				        
    				    
    				  
    				NotificationManager notificationManager = 
    				  (NotificationManager) arg0.getSystemService(arg0.NOTIFICATION_SERVICE);

    				notificationManager.notify(0, mBuilder.build()); 
    			}
    		}
    		
			 
		}
		catch (Exception e)
		{
			
		}
	}
	
	calculateNexUploadTime();        		
	
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
	
}























