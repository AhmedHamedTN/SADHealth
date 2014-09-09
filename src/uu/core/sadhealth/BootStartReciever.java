package uu.core.sadhealth;

import java.util.Timer;
import java.util.TimerTask;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import uu.core.sadhealth.services.MainService;

public class BootStartReciever extends BroadcastReceiver{
	
	//Start the main Service on Boot up
	@Override
	public void onReceive(Context c, Intent i) {
		
		Timer timer = null;
		timer = new Timer();
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(c);
		if (sharedPreferences.getBoolean("BootUp", true))
		{
			Intent serviceIntent = new Intent(c, MainService.class);
			c.startService(serviceIntent);
			//ServiceStartTask myTimerTask =new ServiceStartTask(c);
		
			//timer.schedule(myTimerTask, 30000);
		}
		
	}
}

class ServiceStartTask extends TimerTask {

	private Context cntx=null;
	  public ServiceStartTask(Context c) {
		cntx=c;
	}

	@Override
	  public void run() {
		  Intent serviceIntent = new Intent(cntx, MainService.class);
			cntx.startService(serviceIntent);
	  
	 }
}
