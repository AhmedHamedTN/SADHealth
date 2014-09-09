package uu.core.sadhealth;

import uu.core.sadhealth.utils.AppPreferences;

import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.Menu;
import android.view.View;

public class UserSettingsActivity extends Activity {
	
	private final String TAG = "UserSettings";
	//Create shared preferences so we can store valuable data related to the app
	private AppPreferences _appPrefs;
	
	static final int RESULT_CLOSE_ALL =0;
	static final int BACKTOMAIN=1;
	static String USERID = "";	
	static int prefs=R.xml.preferences;
	
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//setContentView(R.layout.activity_user_settings);
		
		_appPrefs = new AppPreferences(getApplicationContext());
		
		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			if(extras.containsKey("username"))
			{
				String value = extras.getString("username");
				Log.i(TAG, value);
				_appPrefs.setUserRegistered(true);
				_appPrefs.setUserID(value);
			}
			
			if(extras.containsKey("settingsMenu"))
			{
				Log.i(TAG, "extras containsKey");
				prefs=R.xml.preferences_nobuttons;
			}
		}
		else
		{
			Log.i(TAG,"no-value");
		}
		 PreferenceManager.setDefaultValues(this, prefs, false);
 		
		getFragmentManager().beginTransaction().replace(android.R.id.content,new UserPreferencesFragment()).commit();		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		//getMenuInflater().inflate(R.menu.user_settings, menu);
		return true;
	}
	
	public void exitApp(View v)
    {
	   setResult(RESULT_CLOSE_ALL);
       this.finish();
    } 
	
	public void nextScreen(View v)
	{
		USERID=_appPrefs.getUserID();
		Intent resultIntent = new Intent();
		resultIntent.putExtra("username", USERID);
		setResult(RESULT_CLOSE_ALL, resultIntent);
		finish();
		
	}
	
	@Override
	public void onBackPressed() {
	   Log.d("CDA", "onBackPressed Called");
	   USERID=_appPrefs.getUserID();
		Intent resultIntent = new Intent();
		resultIntent.putExtra("username", USERID);
		setResult(RESULT_CLOSE_ALL, resultIntent);
		finish();
	}
	
	public static class UserPreferencesFragment extends PreferenceFragment {
        private final static String TAG = "UserPreferencesFragment";

        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            Log.i(TAG, "fragment onCreate");
           
            addPreferencesFromResource(prefs);
        }
        
    }

}


