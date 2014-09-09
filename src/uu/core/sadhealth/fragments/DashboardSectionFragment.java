/**DashboardSectionFragment.java
 * This class is a fragment from the Fragment's package.
 * It is responsible for displaying to the user the main information about the app. I.e the weather information and diagnostics for the service.
 * It is instanciated from the MainActivity directly as a member of the pageviewer android component and is the default landing screen when the user runs the app or brings up the background activity on first boot.
 * @author Kiril Tzvetanov Goguev
 * @date January 15th,2014
 */
package uu.core.sadhealth.fragments;

import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.json.JSONException;

import com.crashlytics.android.Crashlytics;
import com.espian.showcaseview.ShowcaseView;
import com.espian.showcaseview.ShowcaseViews;
import com.espian.showcaseview.ShowcaseViews.ItemViewProperties;
import com.espian.showcaseview.targets.ViewTarget;
import uu.core.sadhealth.services.MainService;
import uu.core.sadhealth.services.UploadService;
import uu.core.sadhealth.utils.AppPreferences;
import uu.core.sadhealth.utils.FileToWrite;
import uu.core.sadhealth.utils.ServiceFunctions;
import uu.core.sadhealth.MainActivity;
import uu.core.sadhealth.R;
import uu.core.sadhealth.SetupActivity;
import uu.core.sadhealth.UploadActivity;
import uu.core.sadhealth.R.color;
import uu.core.sadhealth.R.drawable;
import uu.core.sadhealth.R.id;
import uu.core.sadhealth.R.layout;
import uu.core.sadhealth.weather.JSONWeatherParser;
import uu.core.sadhealth.weather.WeatherData;
import uu.core.sadhealth.weather.WeatherHttpClient;


import android.app.ActionBar;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class DashboardSectionFragment extends Fragment {
	
	private final String TAG = "Dashboard";
	private AppPreferences _appPrefs;
	
	final int LOGIN = 1;
	final int LOGOUT = 2;
	final int REGISTER = 3;
	final int SETUP=4;
	static final int RESULT_CLOSE_ALL =0;
	static final int UPLOADEXIT= 5;

	
	//User info
	static String USERID = "";		
	private static String location;
	//Google play services required
	private String selectedEmail;	    
			    
	static boolean userRegistered=false;
	
	private static boolean userLoggedIn = false;
	public static boolean uploading = false;
	public static boolean manualUploadingIndicator=false;
	
	private TextView User;
	private static TextView status_text;
	private Boolean NetworkConnectivity;
	
	//Service status
	//static boolean running=false;
	static String STATUS="";

	
	ServiceReceiver serviceReceiver;
	Intent serviceIntent;

	
	TextView userView, loginView, serviceStatusValue, uploadCounterValue, noWeatherConnectionView, nextTimeToUploadValue;
	TextView ALSValue,NLSValue;
	Button  mapImageButton, graphImageButton;
	ImageButton uploadButton,refreshButton,exitButton,serviceImageButton;
	
	static MenuItem uploadMenuBtn, refreshMenuBtn;
	
	//Stuff for weather
	
	private ImageView imgView;
	private TextView cityText, condDescr, temp, sunTotalView, sunRiseTimeView, sunSetTimeView, potLux, NLS, ALS;
	private String country, city, condition,  sunlightHrsStr, sunRiseTime,sunSetTime,potentialLux, weatherIconName;
	
	private ShowcaseView sv;
	private int counter=0;
	private boolean inTutorial=false;
	private float tempstr;
	
	private static final float ROTATE_FROM = 0.0f;
    private static final float ROTATE_TO = -10.0f * 360.0f;// 3.141592654f * 32.0f;
    
    ActionBar actionBar;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		try
		{
			//setHasOptionsMenu(true);
			return inflater.inflate(R.layout.fragment_main, container, false);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
		return null;
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState)
	{
		super.onActivityCreated(savedInstanceState);
		
		
		
		//Setup the main items on the action bar
		//User = (TextView) getActivity().findViewById(R.id.User);
		status_text = (TextView) getView().findViewById(R.id.status_text);
		//User.setText("No User!");
		actionBar=getActivity().getActionBar();
		//actionBar.setTitle(User.getText()); <-Setup the action bar from the fragment

		//uploadButton=(ImageButton) actionBar..findViewById(R.id.UploadMenu);
		//refreshButton=(ImageButton) getActivity().findViewById(R.id.RefreshMenu);
		//exitButton=(ImageButton) getActivity().findViewById(R.id.ExitMenu);
		
		//uploadButton.setOnClickListener(mGlobal_OnClickListener);
		//refreshButton.setOnClickListener(mGlobal_OnClickListener);
		
		serviceStatusValue=(TextView)getView().findViewById(R.id.ServiceStatusValue);
		uploadCounterValue=(TextView)getView().findViewById(R.id.UploadCounterValue);
		noWeatherConnectionView=(TextView)getView().findViewById(R.id.NoWeatherConnection);
		nextTimeToUploadValue = (TextView)getView().findViewById(R.id.NextUploadTimeValue);
		ALSValue = (TextView)getView().findViewById(R.id.ALS);
		NLSValue = (TextView)getView().findViewById(R.id.NLS);
		
		//weather
		cityText = (TextView) getView().findViewById(R.id.City);
 	    condDescr = (TextView) getView().findViewById(R.id.Weather);
 		temp = (TextView) getView().findViewById(R.id.temp);
 	    imgView = (ImageView) getView().findViewById(R.id.condIcon);
 	    sunTotalView=(TextView) getView().findViewById(R.id.SunTotalTime);
 	    sunRiseTimeView=(TextView) getView().findViewById(R.id.SunRiseTime);
 	    sunSetTimeView=(TextView) getView().findViewById(R.id.SunSetTime);
 	    potLux =(TextView)getView().findViewById(R.id.PotLux);
 	    
 	   _appPrefs = new AppPreferences(getActivity());
		
		userRegistered=_appPrefs.getUserRegistered();

		//Always check for internet connection on first load.
		NetworkConnectivity = ServiceFunctions.hasInternetConnection(getActivity());
		Log.v("Internet-Connection", NetworkConnectivity.toString());
		
		serviceIntent =MainActivity.serviceIntent;
		
	     
		
		if (userRegistered==false)
		{
			Log.i(TAG,"no user registered!, launching setup");
			launchSetup();
		}
		else
		{
			Log.i(TAG,"Finished with the setup proceed to start service");
			
			selectedEmail=_appPrefs.getUserEmail();
			
			if(NetworkConnectivity)
			{
				if (selectedEmail!=null)
				{
					Log.i(TAG, "Trying to login");
					userlogin(selectedEmail);
					if (!MainActivity.running)
					{ // in case of not running start running
						if (userLoggedIn)
						{
							//start service
							Log.i(TAG,"start click registered");
							getActivity().startService(serviceIntent);			
							MainActivity.running = true;
							serviceStatusValue.setText("Running");
							serviceStatusValue.setTextColor(getResources().getColor(R.color.green_apple));
							updateStatus("      SadHealth Service is Running      ");
							Crashlytics.setString("Action-1", "Start.After.Login");
							//Update the weather fragment 
							UpdateWeatherNow();
						}
					}
					else
					{
						updateStatus("      SadHealth Service is Running      ");
						serviceStatusValue.setText("Running");
						serviceStatusValue.setTextColor(getResources().getColor(R.color.green_apple));
					}
				}
				else
				{
					Log.i(TAG, "selectedEmail is null!");
				}
				
			}
			else
			{
				Log.i(TAG, "Trying to login");
				userlogin(selectedEmail);
			}
			//check to display the tutorial at least once
			if (_appPrefs.getShowTutorial())
			{
				getView().findViewById(R.id.DiagnosticsFragment).setVisibility(View.INVISIBLE);
				
				((MainActivity)getActivity()).tutorial();
						
			}
			
		}
		addListenerOnButton();
		
		

	}
	
	//Called by the system when an activity returns to this one with a result
			@Override
			public void onActivityResult(int requestCode, int resultCode, Intent data){
				
				 if (resultCode == RESULT_CLOSE_ALL) 
				 {	 
					 if (data!=null)
					 {
						Log.i(TAG,"Data passed back-final");
						Log.i(TAG,data.getExtras().getString("username"));
						 if ( data.getExtras()!=null)
							{
								
								    String value = data.getStringExtra("username");
								    Log.i(TAG, value);
								    
								    selectedEmail = _appPrefs.getUserEmail();
								    
									//check for internet connection
									ConnectivityManager connMgr = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);  
									NetworkInfo networkInfo;

					        		//check network status
					        		networkInfo = connMgr.getActiveNetworkInfo();
					        		
									
									if(networkInfo.getState()==NetworkInfo.State.CONNECTED)
									{
										if (selectedEmail!=null)
										{
											Log.i(TAG, "Trying to login fom usersettings result");
											Log.i(TAG, "selectedEmail is "+selectedEmail);
											userlogin(selectedEmail);
											if (!MainActivity.running)
											{ // in case of not running start running
												if (userLoggedIn)
												{
													//start service
													Log.i(TAG,"start click registered");
													getActivity().startService(serviceIntent);
													
													MainActivity.running = true;
													
													serviceStatusValue.setText("Running");
													serviceStatusValue.setTextColor(getResources().getColor(R.color.green_apple));
													updateStatus("      SadHealth Service is Running      ");
													//Update Weather
													UpdateWeatherNow();
															
												}
											}
											else
											{
												UpdateWeatherNow();
											}
											
										}
										else
										{
											Log.i(TAG, "selectedEmail is null!");
										}
									}
								    
								}
								else
								{
									Log.i(TAG,"no-value");
								}
						//check to display the tutorial at least once
							if (_appPrefs.getShowTutorial())
							{
								getView().findViewById(R.id.DiagnosticsFragment).setVisibility(View.INVISIBLE);
								
								((MainActivity)getActivity()).tutorial();
										
							}
					 }
					 addListenerOnButton();
					
				 }
				 else if (resultCode==UPLOADEXIT)
				 {
					 //Log.i(TAG, "Killing application");
					 //android.os.Process.killProcess(android.os.Process.myPid());
					 uploadButton.clearAnimation();
					 
				 }
				 else
				 {
					 Log.i(TAG, "HERE finally");
				 }
				 
				
				 super.onActivityResult(requestCode, resultCode, data);

			}
			
			@Override
			public void onResume() // 
			{   
				Log.i(TAG, "on Resume");
				
				//refresh the screen & update weather
				refreshAll(getView().findViewById(android.R.id.content));
				
				SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
				_appPrefs.setDUNetworkMode(Integer.valueOf(sharedPreferences.getString("Data_UploadConfig", "0")));
				uploadCounterValue.setText(String.valueOf(_appPrefs.getUploadCounter()));
				boolean autoStart = sharedPreferences.getBoolean("DiagnosticsShow", false);
				if (autoStart)
				{
					if ((_appPrefs.getShowTutorial()==false)&&(!MainActivity.inTutorial))
						getView().findViewById(R.id.DiagnosticsFragment).setVisibility(View.VISIBLE);
				}
				else
				{
					getView().findViewById(R.id.DiagnosticsFragment).setVisibility(View.INVISIBLE);
				}
				
				super.onResume();
				IntentFilter timeFilter = new IntentFilter(MainService.TO_MAINACTIVITY);
				IntentFilter uploadFilter = new IntentFilter(UploadService.TO_MAINACTIVITY);
				serviceReceiver = new ServiceReceiver();
				getActivity().registerReceiver(serviceReceiver, timeFilter);
				getActivity().registerReceiver(serviceReceiver, uploadFilter);
			}

			@Override
			public void onPause()
			{
				super.onPause();
				getActivity().unregisterReceiver(serviceReceiver);
			}
			
			
			
			public void refreshAll(View v)
			{
				/*refreshButton.setEnabled(false);
				refreshButton.postDelayed(new Runnable() {
				    @Override
				    public void run() {
				        refreshButton.setEnabled(true);
				    }
				}, 5000);
				RotateAnimation r; 
				r = new RotateAnimation(ROTATE_FROM, ROTATE_TO, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
				  r.setDuration((long) 2*1500);
				 r.setRepeatCount(0);
				refreshButton.startAnimation(r);*/
				uploadCounterValue.setText(String.valueOf(_appPrefs.getUploadCounter()));
				
				
				
					if (!userLoggedIn)
					{
						if (selectedEmail!=null)
						{
							Log.i(TAG, "Trying to login");
							userlogin(selectedEmail);
							if(!MainActivity.running)
							{
								getActivity().startService(serviceIntent);
								MainActivity.running=true;
								serviceStatusValue.setText(" Running");
								serviceStatusValue.setTextColor(getResources().getColor(R.color.green_apple));
								serviceImageButton.setImageResource(R.drawable.on_switch);
							}
						}
					
					}
					else
					{
					
						if(!MainActivity.running)
						{
							getActivity().startService(serviceIntent);
							MainActivity.running=true;
							serviceStatusValue.setText(" Running");
							serviceStatusValue.setTextColor(getResources().getColor(R.color.green_apple));
							serviceImageButton.setImageResource(R.drawable.on_switch);
						}
					}
					UpdateWeatherNow();
				   
			}
	
	private void launchSetup()
 	{
 		Intent setupIntent = new Intent(getActivity(), SetupActivity.class);
 		startActivityForResult(setupIntent,0 );	
 	}
	
	public void updateStatus(final String message) 
 	{
 		Log.v("UpdateStatus", message);
 		status_text.setText(message);
 	}
	
	private void userlogin(String selectedEmail)
 	{
 		USERID=_appPrefs.getUserID();
 		MainActivity.USERID=USERID;
 		Log.i(TAG, "DashBoard USERID is "+USERID);
 		if (USERID!=null)
 		{
 			actionBar.setTitle(USERID);
 			//User.setText(USERID);
 			userLoggedIn = true;
 			Crashlytics.setUserName(USERID);
 			Crashlytics.setUserEmail(selectedEmail);
 			}

 	}
	
	private void UpdateWeatherNow()
	{
		Boolean isAvailable =false;
		
	     location=_appPrefs.getLocation();
	  // Check availability of network connection
         try
         {
        	 
             ConnectivityManager cm = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
             NetworkInfo ni;
             if(cm == null)
             {
                 isAvailable = false;
             }
             else
             {
            	 
            	// Crashlytics.setString("Weather-code:", "Trying to Update Weather");
                 isAvailable = cm.getActiveNetworkInfo().isAvailable();
                 ni=cm.getActiveNetworkInfo();
               //get last known location
         		location=_appPrefs.getLocation();
         		if (location !=null && ni !=null)
         		{
         			if (ni.isConnected())
         			{
         				noWeatherConnectionView.setVisibility(View.GONE);
         				Crashlytics.setString("Last Action:", "Update Weather network not null");
                 
	         	        country=_appPrefs.getWeatherCountry();
						city = _appPrefs.getWeatherCity();
						condition=_appPrefs.getWeatherCondition();
						tempstr=_appPrefs.getWeatherTemp();
						
						weatherIconName=_appPrefs.getWeatherIconName();
						sunlightHrsStr=_appPrefs.getWeatherTotalSunlightHrs();
						Log.i(TAG, "W_SR"+_appPrefs.getWeatherSunRise());
						sunRiseTime=_appPrefs.getWeatherSunRise();
						sunSetTime=_appPrefs.getWeatherSunSet();
						potentialLux=_appPrefs.getWeatherPotentialLux();
						imgView.setImageResource(getResources().getIdentifier(weatherIconName, "drawable", "uu.core.sadhealth"));
						
						cityText.setText(city + "," + country );
						condDescr.setText(condition);
						temp.setText(String.format("%.1f", Float.valueOf(tempstr))+ " °C");
						sunTotalView.setText(sunlightHrsStr);
						sunRiseTimeView.setText(sunRiseTime);
						sunSetTimeView.setText(sunSetTime);
						
						//calculate the potential lux
						potLux.setText(potentialLux);
         			}
                 }
             }
         }
         catch(Exception e)
         {
        	 
         }
         
         
		
	}
	
	
	
	public  void UpdateWeatherNowOnSwipe()
	{
		//noWeatherConnectionView.setVisibility(View.GONE);
        Crashlytics.setString("Last Action:", "Update Weather network not null");
        
        cityText = (TextView) getView().findViewById(R.id.City);
 	    condDescr = (TextView) getView().findViewById(R.id.Weather);
 		temp = (TextView) getView().findViewById(R.id.temp);
 	    imgView = (ImageView) getView().findViewById(R.id.condIcon);
 	    sunTotalView=(TextView) getView().findViewById(R.id.SunTotalTime);
 	    sunRiseTimeView=(TextView) getView().findViewById(R.id.SunRiseTime);
 	    sunSetTimeView=(TextView) getView().findViewById(R.id.SunSetTime);
 	    potLux =(TextView)getView().findViewById(R.id.PotLux);
 	    
 	   _appPrefs = new AppPreferences(getActivity());
                 
        country=_appPrefs.getWeatherCountry();
		city = _appPrefs.getWeatherCity();
		condition=_appPrefs.getWeatherCondition();
		tempstr=_appPrefs.getWeatherTemp();
						
		weatherIconName=_appPrefs.getWeatherIconName();
		sunlightHrsStr=_appPrefs.getWeatherTotalSunlightHrs();
		Log.i(TAG, "W_SR"+_appPrefs.getWeatherSunRise());
		sunRiseTime=_appPrefs.getWeatherSunRise();
		sunSetTime=_appPrefs.getWeatherSunSet();
		potentialLux=_appPrefs.getWeatherPotentialLux();
		imgView.setImageResource(getResources().getIdentifier(weatherIconName, "drawable", "uu.core.sadhealth"));
						
		cityText.setText(city + "," + country );
		condDescr.setText(condition);
		temp.setText(String.format("%.1f", Float.valueOf(tempstr))+ " °C");
		sunTotalView.setText(sunlightHrsStr);
		sunRiseTimeView.setText(sunRiseTime);
		sunSetTimeView.setText(sunSetTime);
						
		//get the potential lux
		potLux.setText(potentialLux);
       
         
	}
	

	
	
 	

 	public void addListenerOnButton() {

	
	serviceImageButton= (ImageButton) getView().findViewById(R.id.on_off_switch);
	
	if (!MainActivity.running) //check to select proper icon for image button based on running of main service
		serviceImageButton.setImageResource(R.drawable.off_switch);
	else
		serviceImageButton.setImageResource(R.drawable.on_switch);
	
}
 
 	//broadcast receiver to get service provided intents

 		public class ServiceReceiver extends BroadcastReceiver
 		{
			@Override
 			public void onReceive(Context context, Intent intent)
 			{
				/*if (getUserVisibleHint()) 
			    {
			    	Log.i(TAG,"in Dashboard visible- setting user hint! and updating weather");
			    	UpdateWeatherNowOnSwipe(); 
			    }*/
 				
 				if (intent.getExtras() !=null)
 				{
 					nextTimeToUploadValue.setText(_appPrefs.getNextUploadTime());
 					ALSValue.setText(_appPrefs.getALSTime());
 					NLSValue.setText(_appPrefs.getNLSTime());
 					if (intent.hasExtra("running"))
 					{	
 						//set the running status
 						MainActivity.running = intent.getExtras().getBoolean("running");	
 					}
 					if(intent.hasExtra("username"))
 					{
 						USERID= intent.getExtras().getString("username").toString();	
 					}
 					if (intent.hasExtra("uploadCounter"))
 					{
 						uploadCounterValue.setText(String.valueOf(intent.getExtras().getInt("uploadCounter")));
 					}
 					if(intent.hasExtra("updateWeather"))
 					{
 						Log.i(TAG,"BroadcastRecieved has update Weather");
 						if (intent.getExtras().getBoolean("updateWeather"))	
 						{
 							UpdateWeatherNow();
 						}
 					}
 					
 					if(intent.hasExtra("updateStatus"))
 					{
 						updateStatus(intent.getStringExtra("updateStatus"));
 					}
 					
 					/*if(intent.hasExtra("resultUpload"))
 					{
 						if(intent.hasExtra("toService"))
 						{
 							if(intent.getIntExtra("toService", 1)==1)
 							{
 								uploadMenuBtn.getActionView().clearAnimation();
 								uploadMenuBtn.setActionView(null);
 							}
 						}
 					}*/
 				}
 				
 				
 				if (USERID!=null)
 				{
 					userLoggedIn = true;
 				}
 				else
 				{
 					if (MainActivity.running)
 					{
 						serviceStatusValue.setText("Not Running");
 						serviceStatusValue.setTextColor(getResources().getColor(R.color.red));
 						getActivity().stopService(serviceIntent);
 						MainActivity.running = false;
 					}
 				}
 			}
 		}
 		
 	
 	   
 	
 	   
 	 
 	  
	
}