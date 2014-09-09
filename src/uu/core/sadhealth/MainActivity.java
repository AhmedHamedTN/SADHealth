package uu.core.sadhealth;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Scanner;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;



import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.PopupMenu.OnMenuItemClickListener;
import android.widget.TextView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.espian.showcaseview.ShowcaseView;

import com.espian.showcaseview.targets.ActionItemTarget;
import com.espian.showcaseview.targets.ActionViewTarget;
import com.espian.showcaseview.targets.ViewTarget;
import uu.core.sadhealth.fragments.CommunitySectionFragment;
import uu.core.sadhealth.fragments.DashboardSectionFragment;
import uu.core.sadhealth.fragments.MapsFragment;
import uu.core.sadhealth.fragments.PersonalSectionFragment;
import uu.core.sadhealth.fragments.PersonalStatsFragment;
import uu.core.sadhealth.services.CrowdSourceService;
import uu.core.sadhealth.services.MainService;
import uu.core.sadhealth.services.MainService2;
import uu.core.sadhealth.services.UploadService;
import uu.core.sadhealth.utils.AppPreferences;
import uu.core.sadhealth.utils.FileToWrite;

import com.google.android.gms.maps.SupportMapFragment;
import com.inscription.ChangeLogDialog;

public class MainActivity extends FragmentActivity implements
		ActionBar.TabListener, OnClickListener, OnMenuItemClickListener {
	
	//Create shared preferences so we can store valuable data related to the app
		private static AppPreferences _appPrefs;
		
		//Google play services required
		private String selectedEmail;	    
		    
		private TextView User;
		private static TextView status_text;
			
		private Boolean NetworkConnectivity;
		
		private PopupMenu popupMenu;
		private final static int CNG_SETTINGS = 1;
	    private final static int CNG_USER = 2;
	    private final static int ABOUT=3;
	    private final static int HELP=4;

		//User info
		public static String USERID = "";		
		private String location;

		private static boolean userLoggedIn = false;
		public static boolean uploading = false;
		public static boolean manualUploadingIndicator=false;

		//Activity signatures
		private final String TAG = "MainActivity";
		final int LOGIN = 1;
		final int LOGOUT = 2;
		final int REGISTER = 3;
		final int SETUP=4;
		static final int RESULT_CLOSE_ALL =0;
		static final int UPLOADEXIT= 5;

		//Service status
		public static boolean running=false;
		static String STATUS="";

		static boolean userRegistered=false;
		FragmentReceiver fReceiver;
		public static Intent serviceIntent;


		TextView userView, loginView, serviceStatusValue, uploadCounterValue, noWeatherConnectionView, nextTimeToUploadValue;
		TextView ALSValue,NLSValue;
		Button  mapImageButton, graphImageButton;
		static ImageButton uploadButton;

		ImageButton refreshButton;

		ImageButton exitButton;

		ImageButton serviceImageButton;
		
		//Stuff for weather
		private FileWriter weatherWriter;
		private ImageView imgView;
		private TextView cityText, condDescr, temp, sunTotalView, sunRiseTimeView, sunSetTimeView, potLux, NLS, ALS;
		private int tot_SunHrs, tot_SunMins, sunRiseHr, sunRiseMin, sunSetHr,sunSetMin;
		private String country, city, condition, sunRiseHrStr, sunRiseMinStr, sunSetHrStr, sunSetMinStr, sunlightHrsStr, weatherIconName;
		private float tempstr;
		
		private static final float ROTATE_FROM = 0.0f;
	    private static final float ROTATE_TO = -10.0f * 360.0f;// 3.141592654f * 32.0f;

	    
	    private ShowcaseView sv;
	    private int counter=0;
		public static boolean inTutorial=false;
		public static int myPosition=0;
		
		static final int NoSections=4;
		
		private static SupportMapFragment mMapFragment;
		ActionBar actionBar;
		
		static MenuItem uploadMenuBtn, refreshMenuBtn;
		ActionItemTarget uploadTarget;
		ActionItemTarget settingsTarget;
		ActionItemTarget overflowTarget;
		ActionItemTarget homeTarget;

	/**
	 * The {@link android.support.v4.view.PagerAdapter} that will provide
	 * fragments for each of the sections. We use a
	 * {@link android.support.v4.app.FragmentPagerAdapter} derivative, which
	 * will keep every loaded fragment in memory. If this becomes too memory
	 * intensive, it may be best to switch to a
	 * {@link android.support.v4.app.FragmentStatePagerAdapter}.
	 */
	SectionsPagerAdapter mSectionsPagerAdapter;

	/**
	 * The {@link ViewPager} that will host the section contents.
	 */
	ViewPager mViewPager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Crashlytics.start(this);
		setContentView(R.layout.activity_main);
		
		int versionCode=0;
		try {
			versionCode = getPackageManager().getPackageInfo(getPackageName(), 0).versionCode;
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// Set up the action bar.
		actionBar = getActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		
		//actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
		/** Implement the custom action bar
		 *  A layout is inflated from 'sadhealth_actionbar' xml file
		 * **/
		final ViewGroup actionBarLayout = (ViewGroup) getLayoutInflater().inflate(R.layout.sadhealth_actionbar, null);
		actionBar.setDisplayShowHomeEnabled(true);
		actionBar.setDisplayShowTitleEnabled(true);
		
		//actionBar.setDisplayShowCustomEnabled(true);
		//actionBar.setCustomView(actionBarLayout, new ActionBar.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
		
		
		//findViewById(R.id.Settings).setOnClickListener(this);
		
		
		
		
		
		serviceIntent= new Intent(getApplicationContext(), MainService2.class);
		_appPrefs = new AppPreferences(getApplicationContext());
		
		
		//check to display the change log on upgrade of the version
				if(_appPrefs.getVersionNR()< versionCode)
				{
					ConvertFiles();
					ChangeLogDialog _ChangelogDialog = new ChangeLogDialog(this); 
					_ChangelogDialog.show();
					_appPrefs.setVersionNR(versionCode);
					
				}
				
				
		
		
		
		// Create the adapter that will return a fragment for each of the three
		// primary sections of the app.
		mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

		// Set up the ViewPager with the sections adapter.
		mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setAdapter(mSectionsPagerAdapter);

		// When swiping between different sections, select the corresponding
		// tab. We can also use ActionBar.Tab#select() to do this if we have
		// a reference to the Tab.
		mViewPager
				.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
					@Override
					public void onPageSelected(int position) {
						actionBar.setSelectedNavigationItem(position);
					}
				});

		// For each of the sections in the app, add a tab to the action bar.
		for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
			// Create a tab with text corresponding to the page title defined by
			// the adapter. Also specify this Activity object, which implements
			// the TabListener interface, as the callback (listener) for when
			// this tab is selected.
			actionBar.addTab(actionBar.newTab()
					.setText(mSectionsPagerAdapter.getPageTitle(i))
					.setTabListener(this));
		}
		
		//set the default tab
		mViewPager.setCurrentItem(1);
		
		uploadCounterValue = (TextView) findViewById(R.id.UploadCounterValue);
		mViewPager.setOffscreenPageLimit(1);
		
		
	}
	
	
	
	private void ConvertFiles() {
		//open maxlight and test data[0] is from the old version.
		SimpleDateFormat sdf=new SimpleDateFormat("yyyy/MM/dd");
		DateTimeFormatter dtf=DateTimeFormat.forPattern("yyyy/MM/dd");
		 File sdcard = Environment.getExternalStorageDirectory();
		File fn = new File (sdcard, "sadhealth/users/"+ _appPrefs.getUserID()+"/lightSources.csv");
		Boolean badFormat=false;
		FileReader fr = null;
		try {
			fr = new FileReader(fn);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		BufferedReader bufferedReader = new BufferedReader(fr);
		
		String line = null;

		String[] data;
		try {
			while ((line = bufferedReader.readLine()) != null) {
				data = line.split(",");

				try{
					dtf.parseDateTime(data[0]);
				}
				catch(IllegalArgumentException e)
				{
					badFormat=true;
					break;
				}
				
			
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		

		//max = lmax;
		try {
			bufferedReader.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if(badFormat)
		{
			try{
				FileWriter lightSourceWriter = FileToWrite.createLogFileWriter("lightSourcesfixed.csv",_appPrefs.getUserID()); //file writer which overwrites 
				
				try {
					fr = new FileReader(fn);
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				 bufferedReader = new BufferedReader(fr);
				
				 line = null;

				String[] datatoCopy;
				try {
					while ((line = bufferedReader.readLine()) != null) {
						datatoCopy = line.split(",");
						lightSourceWriter.append(sdf.format(datatoCopy[0])+"," +datatoCopy[1]+","+datatoCopy[2]+"\n");
						
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				

				//max = lmax;
				try {
					bufferedReader.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				
				
				
				
				
				lightSourceWriter.flush();
				lightSourceWriter.close();
			}
			catch(IOException e){

			}
			//delete the old lightSources file and rename the new one
			 File file = new File(sdcard,"sadhealth/users/"+ _appPrefs.getUserID()+"/lightSources.csv");
			
			if (file.exists())
			{
				boolean deleted = file.delete();
				Log.i(TAG, "the file was deleted: "+deleted);
			}
			File dir = Environment.getExternalStorageDirectory();
			if(dir.exists()){
			    File from = new File(dir,"sadhealth/users/"+ _appPrefs.getUserID()+"/lightSourcesfixed.csv");
			    File to = new File(dir,"sadhealth/users/"+ _appPrefs.getUserID()+"/lightSources.csv");
			     if(from.exists())
			        from.renameTo(to);
			}
		}
		
	}



	public  int getPeriodNo(){  // read the period No from the SD
		
		return _appPrefs.getPeriodNo();
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.clear();
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main_activity_actions, menu);
		 uploadTarget = new ActionItemTarget(this, R.id.action_upload);
		 settingsTarget = new ActionItemTarget(this, R.id.action_settings);
		 overflowTarget = new ActionItemTarget(this, R.id.a_More);
		 homeTarget=new ActionItemTarget (this,android.R.id.home);
		return true;
	}

	@Override
	public void onTabSelected(ActionBar.Tab tab,
			FragmentTransaction fragmentTransaction) {
		// When the given tab is selected, switch to the corresponding page in
		// the ViewPager.
		mViewPager.setCurrentItem(tab.getPosition());
		myPosition=tab.getPosition();
		
		
		
	}

	@Override
	public void onTabUnselected(ActionBar.Tab tab,
			FragmentTransaction fragmentTransaction) {
		
	}

	@Override
	public void onTabReselected(ActionBar.Tab tab,
			FragmentTransaction fragmentTransaction) {
		//invalidateOptionsMenu();
	}

	/**
	 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
	 * one of the sections/tabs/pages.
	 */
	public class SectionsPagerAdapter extends FragmentStatePagerAdapter {
		private Map<Integer, Fragment> mPageReferenceMap = new HashMap<Integer, Fragment>();

		public SectionsPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int position) {
			// getItem is called to instantiate the fragment for the given page.
			// Return a DummySectionFragment (defined as a static inner class
			// below) with the page number as its lone argument.
			Fragment fragment;
			switch(position)
			{
			case 0:
				//location fragment
				fragment = new PersonalStatsFragment();
				mPageReferenceMap.put(Integer.valueOf(position), fragment);
				return fragment;
			case 1:
				fragment = new DashboardSectionFragment();
				mPageReferenceMap.put(Integer.valueOf(position), fragment);
				return fragment;
			case 2:
				fragment = new PersonalSectionFragment();
				mPageReferenceMap.put(Integer.valueOf(position), fragment);
				return fragment;
			case 3:
				 fragment = new CommunitySectionFragment();
				 mPageReferenceMap.put(Integer.valueOf(position), fragment);
				return fragment;
				
			}
			
			
			return null;
			
			
		}
		
		

		@Override
		public int getCount() {
			// Show 3 total pages.
			return NoSections;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			Locale l = Locale.getDefault();
			switch (position) {
			case 0:
				return getString(R.string.title_section1).toUpperCase(l);
			case 1:
				return getString(R.string.title_section2).toUpperCase(l);
			case 2:
				return getString(R.string.title_section3).toUpperCase(l);
			case 3:
				return getString(R.string.title_section4).toUpperCase(l);
			}
			return null;
		}
		
		public Fragment getFragment(int key) {
			
			return mPageReferenceMap.get(key);
		}
	}
	
	
	
	
	
	
	
		
	
	
	
	public void launchExitProcedures()
	{
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
 
			// set title
			alertDialogBuilder.setTitle("Exit SADHealth");
 
			// set dialog message
			alertDialogBuilder
				.setMessage("Warning: You are about to Exit this application.")
				.setCancelable(false)
				.setIcon(android.R.drawable.ic_dialog_alert)
				.setPositiveButton("Exit Only",new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog,int id) {
						// if this button is clicked, close
						// current activity
						stopService(serviceIntent);
						android.os.Process.killProcess(android.os.Process.myPid());
					}
				  })
				.setNeutralButton("Upload Data & Exit",new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog,int id) {
						// if this button is clicked, just close
						// the dialog box and do nothing
						stopService(serviceIntent);
						Intent uploadIntent = new Intent(getApplication(), UploadService.class);
				   	
				   		uploadIntent.putExtra("ExitAfter",true);
				   		startService(uploadIntent);
						dialog.dismiss();
						
					}
				})
				.setNegativeButton("Cancel",new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog,int id) {
						// if this button is clicked, just close
						// the dialog box and do nothing
						dialog.cancel();
					}
				});
 
				// create alert dialog
				AlertDialog alertDialog = alertDialogBuilder.create();
 
				// show it
				alertDialog.show();
		
	}
	
	public void serviceSwitch(View v)
	{
		
		serviceStatusValue =(TextView) findViewById(R.id.ServiceStatusValue);
		serviceImageButton=(ImageButton)findViewById(R.id.on_off_switch);
		
		if (running)
		{
			stopService(serviceIntent);
			running=false;
			serviceStatusValue.setText("Not Running");
			serviceStatusValue.setTextColor(getResources().getColor(R.color.red));
			serviceImageButton.setImageResource(R.drawable.off_switch);
		}
		else
		{
			startService(serviceIntent);
			running=true;
			serviceStatusValue.setText(" Running");
			serviceStatusValue.setTextColor(getResources().getColor(R.color.green_apple));
			serviceImageButton.setImageResource(R.drawable.on_switch);
		}
	}
	
	

	@Override
	public boolean onMenuItemClick(MenuItem item) {
		switch(item.getItemId())
		{
			case CNG_SETTINGS:
				Intent settingsIntent = new Intent(getApplicationContext(), UserSettingsActivity.class);
				settingsIntent.putExtra("settingsMenu", true);
				startActivity(settingsIntent);
				break;
			case CNG_USER:
				
				Log.i(TAG,"users auth token is: "+_appPrefs.getUserAuthToken());
				Log.i(TAG,"deregistering user, launching setup again");
				Intent setupIntent = new Intent(getApplicationContext(), SetupActivity.class);
		 		startActivityForResult(setupIntent,0 );	
				break;
				
			case HELP:
				Log.i(TAG,"user clicked help");
				if(!inTutorial)
				{
					mViewPager.setCurrentItem(1);
					if (myPosition==1)
					{
						tutorial();
					}
				}
				break;
			case ABOUT:
				Log.i(TAG,"user clicked about");
				Intent aboutIntent = new Intent(getApplicationContext(), AboutActivity.class);
				startActivityForResult(aboutIntent,0 );
				break;
				
		}
		return false;
	}

	@Override
	public void onClick(View arg0) {
		
		
	}
	

	public void tutorial()
	  {
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
	      ShowcaseView.ConfigOptions mOptions = new ShowcaseView.ConfigOptions();
	      counter=0;
	      sv = ShowcaseView.insertShowcaseViewWithType(ShowcaseView.ITEM_ACTION_ITEM, R.id.action_upload, this,
		            R.string.showcase_tutorial_title, R.string.showcase_tutorial_message, new ShowcaseView.ConfigOptions());
	      //sv = ShowcaseView.insertShowcaseView(new ViewTarget(findViewById(R.id.User)), this);
	      tutorialViewHide(true);
	      inTutorial=true;
	      sv.setShowcase(new ViewTarget(findViewById(R.id.UploadCounterValue)), true);
	      sv.setScaleMultiplier(0.0f);
	      sv.setText(R.string.showcase_tutorial_title, R.string.showcase_tutorial_message);
	
	      findViewById(R.id.DiagnosticsFragment).setVisibility(View.INVISIBLE);
	      sv.overrideButtonClick(new View.OnClickListener() {
	          @Override
	          public void onClick(View view) {
	              switch (counter) {
	              	case 0:
	              		sv.setShowcase(new ViewTarget(findViewById(R.id.UploadCounterValue)), true);
	              		sv.setScaleMultiplier(0.0f);
	              		sv.setText(R.string.showcase_tutorialCont_title, R.string.showcase_tutorialCont_message);
	              		break;
	              	case 1:
	              		sv.setShowcase(new ViewTarget(findViewById(R.id.UploadCounterValue)), true);
	              		sv.setScaleMultiplier(0.0f);
	              		sv.setText(R.string.showcase_tutorialCont2_title, R.string.showcase_tutorialCont2_message);
	              		break;
	            	case 2:
	            		sv.setShowcase(uploadTarget,true);
	            		sv.setScaleMultiplier(0.2f);
	            		
	              		sv.setText(R.string.showcase_uploadBtn_title, R.string.showcase_uploadBtn_message);
	  	                break;
	              	
	                  case 3:
	                	  sv.setShowcase(settingsTarget, true);
		              		sv.setScaleMultiplier(0.2f);
	                      sv.setText(R.string.showcase_settingsBtn_title, R.string.showcase_settingsBtn_message);
	                      break;
	                  case 4:
	                	  sv.setShowcase(overflowTarget, true);
		              		sv.setScaleMultiplier(0.2f);
	                  	sv.setText(R.string.showcase_overflowBtn_title, R.string.showcase_overflowBtn_message);
	                      break;
	                  case 5:
	                	  
	                	  findViewById(R.id.status_layout).setVisibility(View.VISIBLE);
		                  	sv.setShowcase(new ViewTarget(findViewById(R.id.status_layout)), true);
		                  	sv.setScaleMultiplier(0.6f);
		                  	sv.setText(R.string.showcase_status_title, R.string.showcase_status_message);
	                      break;
	                  case 6:
	                	  findViewById(R.id.status_layout).setVisibility(View.INVISIBLE);
		                  	findViewById(R.id.WeatherOverlay).setVisibility(View.VISIBLE);
		                  	sv.setShowcase(uploadTarget, true);
		                  	sv.setScaleMultiplier(0.8f);
		                  	sv.setText(R.string.showcase_weather_title, R.string.showcase_weather_message);
	                  	
	                      break;        
	                  case 7:
	                	  findViewById(R.id.WeatherOverlay).setVisibility(View.INVISIBLE);
		                  	findViewById(R.id.LightSrcs).setVisibility(View.VISIBLE);
		                  	sv.setShowcase(new ViewTarget(findViewById(R.id.LightSrcs)), true);
		                  	sv.setScaleMultiplier(0.8f);
		                  	sv.setText(R.string.showcase_lightSrc_title, R.string.showcase_lightSrc_message);
	                      break;
	                  case 8:
	                	  findViewById(R.id.LightSrcs).setVisibility(View.INVISIBLE);
		                  	sv.setShowcase(new ViewTarget(findViewById(R.id.LightSrcs)), true);
		                  	sv.setScaleMultiplier(0.0f);
		                      sv.setText(R.string.showcase_notification_title,R.string.showcase_notification_message);
	                      break;
	                 
	                  case 9:
	                	  actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
	                      sv.setShowcase(new ViewTarget(findViewById(R.id.NLSLabel)), true);
	                      sv.setScaleMultiplier(0.0f);
	                      sv.setText(R.string.showcase_swipe_title,R.string.showcase_swipe_message);
	                      sv.animateGesture(0, -400, -400,-400);
	                      break;
	                      
	                  case 10:
	                	  
	                      sv.setShowcase(new ViewTarget(findViewById(R.id.NLSLabel)), true);
	                      sv.setScaleMultiplier(0.0f);
	                      sv.setText(R.string.showcase_swipe2_title,R.string.showcase_swipe2_message);
	                      
	                      break;
	                      
	                      
	                  case 11:
	                	 
	                      sv.setShowcase(new ViewTarget(findViewById(R.id.NLSLabel)), true);
	                      sv.setScaleMultiplier(0.0f);
	                      sv.setText(R.string.showcase_swipe3_title,R.string.showcase_swipe3_message);
	                     
	                      break;
	                      
	                  case 12:
	                	  sv.hide();
	                      inTutorial=false;
	                      tutorialViewHide(false);
	                      
	                      _appPrefs.setShowTutorial(false);
	                      
	                      break;
	                  
	                      
	              }
	              counter++;
	          }
	      });
	      
	      
	     
	      }
	  	
	  private void tutorialViewHide(Boolean show)
	  {
	  	if (show==true)
	  	{
	  	    findViewById(R.id.status_layout).setVisibility(View.INVISIBLE);
	  		findViewById(R.id.WeatherOverlay).setVisibility(View.INVISIBLE);
	  		findViewById(R.id.LightSrcs).setVisibility(View.INVISIBLE);
	  		
	  	}
	  	else
	  	{
	  	    findViewById(R.id.status_layout).setVisibility(View.VISIBLE);
	  		findViewById(R.id.WeatherOverlay).setVisibility(View.VISIBLE);
	  		findViewById(R.id.LightSrcs).setVisibility(View.VISIBLE);
	  		
	  		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
	  		if(sharedPreferences.getBoolean("DiagnosticsShow", false))
	  		{
	  			findViewById(R.id.DiagnosticsFragment).setVisibility(View.VISIBLE);
	  		}
	  	}
	  	
	  }
	  	
	  	private void setAlpha(float alpha, View... views) 
	  	{
	  	
	  	    for (View view : views)
	  	    {
	  	        view.setAlpha(alpha);
	  	    }
	  	}
	  	
	  	
	  	
	  
	 	   
	 	  @Override
		    public boolean onOptionsItemSelected(MenuItem item) {
		      switch (item.getItemId()) {
		      // action with ID action_refresh was selected
		      case R.id.action_upload:
		    	
		    	  uploadMenuBtn=item;
		    	  initUpload(item);
		        return true;
		      case R.id.action_settings:
		    	
		    	  Intent settingsIntent = new Intent(getApplicationContext(), UserSettingsActivity.class);
					settingsIntent.putExtra("settingsMenu", true);
					startActivity(settingsIntent);
		    	 return true;
		      case R.id.action_aboutSAD:
		    	  Intent aboutSADIntent = new Intent(getApplicationContext(), AboutSADActivity.class);
					startActivityForResult(aboutSADIntent,0 );
		    	  return true;
		      case R.id.action_aboutSADHealth:
		    	  Intent aboutIntent = new Intent(getApplicationContext(), AboutActivity.class);
					startActivityForResult(aboutIntent,0 );
		    	  return true;
		      case R.id.action_tutorial:
		    	  if(!inTutorial)
					{
						mViewPager.setCurrentItem(1);
						if (myPosition==1)
						{
							tutorial();
						}
					}
		    	  return true;
		      case R.id.action_exit:
		    	  launchExitProcedures();
		    	  return true;
		      
		      }

		      return true;
		    } 	
	  	
	  
	  
	  
 	
	 	 public void initUpload(final MenuItem item)
		   	{
		       	Log.i(TAG,"in main upload");
		   	   
		       	
		       	LayoutInflater inflater = (LayoutInflater) getApplication()
		       			.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		       			ImageView iv = (ImageView) inflater.inflate(R.layout.action_upload,
		       			null);

		       			Animation rotation = AnimationUtils.loadAnimation(getApplication(),
		       			R.anim.action_upload);
		       			rotation.setRepeatCount(Animation.INFINITE);
		       			iv.startAnimation(rotation);

		       			item.setActionView(iv);
		   		
		   		Intent uploadIntent = new Intent(getApplication(), UploadService.class);
		   		Log.i(TAG,"in main upload myPosition is: "+ String.valueOf(myPosition));
		   		uploadIntent.putExtra("fromFragment",(int) myPosition);
		   		startService(uploadIntent);
		   		
		   	}
	 	 
	 	@Override
		public void onResume() // 
		{   
			Log.i(TAG, "on Resume");
			
			
			super.onResume();
			
			IntentFilter uploadFilter = new IntentFilter(UploadService.TO_MAINACTIVITY);
			fReceiver = new FragmentReceiver();
			registerReceiver(fReceiver, uploadFilter);
		}

		@Override
		public void onPause()
		{
			super.onPause();
			unregisterReceiver(fReceiver);
		}
	 	 
	 	 	
	 	 	//FragmentReceiver -Receive broadcasts from the serviceIntents
	 		public class FragmentReceiver extends BroadcastReceiver
	 		{
				@Override
	 			public void onReceive(Context context, Intent intent)
	 			{
					if (intent.getExtras() !=null)
	 				{
						if(intent.hasExtra("resultUpload"))
	 					{
	 						if(intent.hasExtra("toService"))
	 						{
	 							if(uploadMenuBtn!=null)
	 							{
	 								uploadMenuBtn.getActionView().clearAnimation();
	 								uploadMenuBtn.setActionView(null);
	 							}
	 							else
	 							{
	 								
	 							}
	 							
	 						}
	 					}
	 				}
	 			}
				
	 		}




}
