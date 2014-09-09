package uu.core.sadhealth;

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

public class AboutActivity extends Activity {
	
	private final String TAG = "About";

	
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.about);
		
	}

	
	public void showLegalNotice(View v)
	{
		
	}
	
	public void showRecentChanges(View v)
	{
		
	}
	
}


