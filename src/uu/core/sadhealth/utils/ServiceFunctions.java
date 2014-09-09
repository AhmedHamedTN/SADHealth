/* ServiceFunctions.java
 * @Author Kiril Tzvetanov Goguev
 *  This class is mainly used to provide functions that may be called multiple times
 *  throughout the lifetime of the app. Helper functions mostly..
 *  Functions like logging, checking service connectivity, etc... should go here
 */

package uu.core.sadhealth.utils;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Scanner;

import uu.core.sadhealth.MainActivity;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.util.Log;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.Context;

public class ServiceFunctions extends Activity{
	
	public static Boolean hasInternetConnection(Context context)
	{
		ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
		if (networkInfo != null)
		{
			return true;
		}
		else
		{
			return false;
		}
	}
	
	public Boolean checkServiceRunning()
	{
		ActivityManager manager = (ActivityManager) getSystemService(getApplicationContext().ACTIVITY_SERVICE);
	    for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
	        if (service.service.getClassName().equals("uu.core.sadhealth.MainService")) {
	            return true;
	        }
	    }
	    return false;
	}
	
	public static int readUploadPeriodNo(){  // read the period No from the SD
		try{
			File sdcard = Environment.getExternalStorageDirectory();
		    File file= new File (sdcard, "sadhealth/users/"+ MainActivity.USERID + "/upload_pn.csv");
		    Scanner scanner = new Scanner(file);
		    return (scanner.nextInt());
		}
		catch (IOException e){
			return 0;
		}
	}
	

}
