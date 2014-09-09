package uu.core.sadhealth;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.accounts.AccountManager;
import android.app.Activity;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;


import uu.core.sadhealth.googleauth.AuthForeground;
import uu.core.sadhealth.utils.AppPreferences;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.common.GooglePlayServicesUtil;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Intent;
import android.content.SharedPreferences;

public class RegistrationActivity extends Activity {
	
	private final String TAG = "RegistrationActivity";
	
	//Create shared preferences so we can store valuable data related to the app
	private AppPreferences _appPrefs;
	
	static final int SETTINGS=1;
	static final int RESULT_CLOSE_ALL =0;
	
	//Accounts management - CAM = Central Account Manager
	/* Account management works by email addresses, enumerate
	 *  the emails first THEN request a token from the google server
	 *  if all goes well then you have access to users data from the cloud
	 */
	private AccountManager CAM;
	private String[] registeredEmailsArray;
	private Spinner registeredAccounts;
	private String selectedEmail;
			
	//Special variables required for Google Authentication task
	private static final String SCOPE = "oauth2:https://www.googleapis.com/auth/userinfo.profile";
	public static final String EXTRA_ACCOUNTNAME = "extra_accountname";
		    
	static final int REQUEST_CODE_RECOVER_FROM_AUTH_ERROR = 1001;
	static final int REQUEST_CODE_RECOVER_FROM_PLAY_SERVICES_ERROR = 1002;
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_registration);
		//Get Default preferences on application Start up
		_appPrefs = new AppPreferences(getApplicationContext());
		
		registeredEmailsArray = getAccountNames();
		registeredAccounts = initializeSpinner(R.id.google_accounts_spinner, registeredEmailsArray);
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		//getMenuInflater().inflate(R.menu.registration, menu);
		return true;
	}
	
	//Called by the system when an activity returns to this one with a result
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		 if (requestCode == REQUEST_CODE_RECOVER_FROM_AUTH_ERROR) 
		 {
		            handleAuthorizeResult(resultCode, data);
		            return;
		  }
		 else if(resultCode==RESULT_CLOSE_ALL)
		 {
			 Log.i(TAG,"Data passed back");
			
			 setResult(RESULT_CLOSE_ALL,data);
		     finish();
		 }
			 super.onActivityResult(requestCode, resultCode, data);

	}
	
	
	 private void handleAuthorizeResult(int resultCode, Intent data) 
	 {
	        if (data == null) {
	        	Log.i(TAG, "Error data is null");
	            return;
	        }
	        if (resultCode == RESULT_OK) {
	            Log.i(TAG, "Retrying");
				new AuthForeground(RegistrationActivity.this, selectedEmail, SCOPE,REQUEST_CODE_RECOVER_FROM_AUTH_ERROR).execute();
				
	            setContentView(R.layout.accept_authpermissions);
	            return;
	        }
	        if (resultCode == RESULT_CANCELED) {
	        	setContentView(R.layout.reject_authpermissions);
	        }
	        
	        
	    }
	
	private String[] getAccountNames() {
        CAM = AccountManager.get(this);
        Account[] accounts = CAM.getAccountsByType(GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE);
        String[] names = new String[accounts.length];
        for (int i = 0; i < names.length; i++) {
            names[i] = accounts[i].name;
        }
        return names;
    }
   
	private Spinner initializeSpinner(int id, String[] values) {
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, values);
        Spinner spinner = (Spinner) findViewById(id);
        spinner.setAdapter(adapter);
        return spinner;
    }
	
	public void exitApp(View v)
    {
	   setResult(RESULT_CLOSE_ALL);
       this.finish();
    } 
	
	public void nextScreen(View v)
	{
		
		// Get Authentication in the background
		int accountIndex = registeredAccounts.getSelectedItemPosition();
        if (accountIndex < 0) {
            // this happens when the sample is run in an emulator which has no google account
            // added yet. should return an error here
        	//TODO: add code here to handle a situation with no google account present
            return;
        }
        
       
        selectedEmail = registeredEmailsArray[accountIndex];
        
        _appPrefs.setUserRegistered(true);
        _appPrefs.setUserEmail(selectedEmail);
        
        new AuthForeground(RegistrationActivity.this, selectedEmail, SCOPE,REQUEST_CODE_RECOVER_FROM_AUTH_ERROR).execute();
	}
	
	public void updateStatus(String msg)
	{
		
	}
	
	public void onSuccess(String name)
	{
		Log.i(TAG, "Succeeded in getting the token");
		Log.i(TAG,name);
		Intent setupIntent = new Intent(getApplicationContext(), UserSettingsActivity.class);
		setupIntent.putExtra("username", name);
		startActivityForResult(setupIntent,0 );
		
	}

}
