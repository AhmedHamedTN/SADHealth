package uu.core.sadhealth;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.View;

public class SetupActivity extends Activity {
	
	static final int RESULT_CLOSE_ALL =0;
	private final String TAG = "SetupActivity";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_setup);	
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	    switch(resultCode)
	    {
	    case RESULT_CLOSE_ALL:
	    	Log.i(TAG,"Data passed back");
	        setResult(RESULT_CLOSE_ALL,data);
	        finish();
	    }
	    super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		//getMenuInflater().inflate(R.menu.setup, menu);
		return true;
	}
	
	public void exitApp(View v)
    {
	   setResult(RESULT_CLOSE_ALL);
       this.finish();
    } 
	
	public void nextScreen(View v)
	{
		Intent setupIntent = new Intent(getApplicationContext(), RegistrationActivity.class);
		startActivityForResult(setupIntent,0 );	
	}

	
}
