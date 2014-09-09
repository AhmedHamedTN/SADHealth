package uu.core.sadhealth;

import java.io.FileWriter;
import java.io.IOException;

import uu.core.sadhealth.utils.AppPreferences;
import uu.core.sadhealth.utils.FileToWrite;
import uu.core.sadhealth.utils.DateFormatUtils;

import android.hardware.Sensor;
import android.os.Bundle;
import android.app.Activity;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager.LayoutParams;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Toast;

public class QuestionnaireActivity extends Activity {
	
	private final String TAG = "Questionnaire";
	private FileWriter questionnaireWriter;
	private SeekBar mood, sleep, energy, social;
	private Button submit = null;
	int intMood=5, intSleep=5, intEnergy=5, intSocial = 5;
	private String periodNo;
	private AppPreferences _appPrefs;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_questionnaire);
		_appPrefs = new AppPreferences(getApplicationContext());
		
		this.getWindow().setFlags(
	    		LayoutParams.FLAG_SHOW_WHEN_LOCKED |
	    		LayoutParams.FLAG_DISMISS_KEYGUARD |
	    		LayoutParams.FLAG_TURN_SCREEN_ON |
	            LayoutParams.FLAG_KEEP_SCREEN_ON  |
	            LayoutParams.FLAG_FULLSCREEN
	            ,
	            LayoutParams.FLAG_TURN_SCREEN_ON |
	            LayoutParams.FLAG_SHOW_WHEN_LOCKED |
	            LayoutParams.FLAG_KEEP_SCREEN_ON |
	            LayoutParams.FLAG_DISMISS_KEYGUARD |
	            LayoutParams.FLAG_FULLSCREEN
	    		);
		periodNo=String.format("%04d", _appPrefs.getPeriodNo());
		questionnaireWriter = FileToWrite.createLogFileWriter("questionnaire"+periodNo+".csv",_appPrefs.getUserID());
		if(questionnaireWriter==null){
			Log.v(TAG, "Failed to open file for questionnaire log");
		}

		submit = (Button) findViewById(R.id.submit);
		mood = (SeekBar) findViewById(R.id.mood);
		sleep = (SeekBar) findViewById(R.id.sleep);
		energy = (SeekBar) findViewById(R.id.energy);
		social = (SeekBar) findViewById(R.id.social);
		
		
		mood.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				Toast.makeText(QuestionnaireActivity.this,"Mood: "+intMood,
                        Toast.LENGTH_SHORT).show();
			}
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {}
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				intMood = progress;
			}
		});
		
		sleep.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				Toast.makeText(QuestionnaireActivity.this,"Sleep: "+intSleep,
                        Toast.LENGTH_SHORT).show();
			}
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {}
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				intSleep = progress;
			}
		});
		
		energy.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				Toast.makeText(QuestionnaireActivity.this,"Energy: "+intEnergy,
                        Toast.LENGTH_SHORT).show();
			}
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {}
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				intEnergy = progress;
			}
		});
		
		
		social.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				Toast.makeText(QuestionnaireActivity.this,"Sociality: "+intSocial,
                        Toast.LENGTH_SHORT).show();
			}
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {}
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				intSocial = progress;
			}
		});
		
		submit.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				String str = intMood+","+ intSleep+","+ intEnergy+","+ intSocial+",";
				logQuestionnaire(DateFormatUtils.getMonth(System.currentTimeMillis()),DateFormatUtils.getMonthDay(System.currentTimeMillis()), str);
				Log.i(TAG,str);
				finish();
			}
		});
		
	}

	private boolean logQuestionnaire(String month, String monthDay, String rst) {
    	String fileText = month+","+monthDay+","+ rst +"\n";
    	try {
    		questionnaireWriter.append(fileText);
    		questionnaireWriter.flush();
			return true;
		}
    	catch (IOException e) {
    		Log.e(TAG, "Could not write to questionnaire log file: "+e.toString());
    		return false;
		}
	}
	
	
	/*
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.questionnaire, menu);
		return true;
	}
	*/

}
