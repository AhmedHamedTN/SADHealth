/**ActivityRecognitionServiceIntent.java
 * Ripped from the Google Recognition Sample. Modified to suit SADHealth needs
 * @author Kiril Tzvetanov Goguev
 * @date February 3rd, 2014
 */
package uu.core.sadhealth.services;


import uu.core.sadhealth.utils.AppPreferences;
import uu.core.sadhealth.utils.DateFormatUtils;
import uu.core.sadhealth.utils.FileToWrite;
import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;

import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.provider.Settings;
import android.util.Log;


import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Service that receives ActivityRecognition updates. It receives updates
 * in the background, even if the main Activity is not visible.
 */
public class ActivityRecognitionIntentService extends IntentService {
	private static final String TAG="ActivityRecognitionServiceIntent";
	private AppPreferences _appPrefs;
	private FileWriter activityRecognitionWriter,activityTimerWriter;
	private String periodNo;

   

    public ActivityRecognitionIntentService() {
        // Set the label for the service's background thread
        super("ActivityRecognitionIntentService");
    }
    
    /**
     * Called when a new activity detection update is available.
     */
    @Override
    protected void onHandleIntent(Intent intent) {
    	
    	

        // Get a handle to the repository
       _appPrefs=new AppPreferences(getApplicationContext());
       periodNo = String.format("%04d", _appPrefs.getPeriodNo());
       
       activityRecognitionWriter = FileToWrite.createLogFileWriter("phone_activityRec"+periodNo+".csv",_appPrefs.getUserID());
		if(activityRecognitionWriter==null){
			Log.v(TAG, "Failed to open file for activity recognition log");
		}
		
		activityTimerWriter = FileToWrite.createLogFileWriter("phone_activityRecTimer"+periodNo+".csv",_appPrefs.getUserID());
		if(activityTimerWriter==null){
			Log.v(TAG, "Failed to open file for activity timer log");
		}


        // If the intent contains an update
        if (ActivityRecognitionResult.hasResult(intent)) {
        	Log.i(TAG, "onHandleInten_hasResult");
            // Get the update
            ActivityRecognitionResult result = ActivityRecognitionResult.extractResult(intent);

            // Log the update
            logActivityRecognitionResult(result);

            // Get the most probable activity from the list of activities in the update
            DetectedActivity mostProbableActivity = result.getMostProbableActivity();

            // Get the confidence percentage for the most probable activity
            int confidence = mostProbableActivity.getConfidence();

            // Get the type of activity
            int activityType = mostProbableActivity.getType();
            
            _appPrefs.setPreviousActivity(activityType);
            
            /*if (isMoving(activityType) &&activityChanged(activityType) && (confidence >= 50)) {

                // Notify the user
                //sendNotification();
            }*/

         
        }
    }
    
    /**
     * Tests to see if the activity has changed
     *
     * @param currentType The current activity type
     * @return true if the user's current activity is different from the previous most probable
     * activity; otherwise, false.
     */
    private boolean activityChanged(int currentType) {

        // Get the previous type, otherwise return the "unknown" type
        int previousType = _appPrefs.getPreviousActivity();

        // If the previous type isn't the same as the current type, the activity has changed
        if (previousType != currentType) {
            return true;

        // Otherwise, it hasn't.
        } else {
            return false;
        }
    }

    /**
     * Determine if an activity means that the user is moving.
     *
     * @param type The type of activity the user is doing (see DetectedActivity constants)
     * @return true if the user seems to be moving from one location to another, otherwise false
     */
    private boolean isMoving(int type) {
        switch (type) {
            // These types mean that the user is probably not moving
            case DetectedActivity.STILL :
            case DetectedActivity.TILTING :
            case DetectedActivity.UNKNOWN :
                return false;
            default:
                return true;
        }
    }
    
    /**
     * Write the activity recognition update to the log file

     * @param result The result extracted from the incoming Intent
     */
    private void logActivityRecognitionResult(ActivityRecognitionResult result) {
        // Get all the probably activities from the updated result
        for (DetectedActivity detectedActivity : result.getProbableActivities()) {

            // Get the activity type, confidence level, and human-readable name
            int activityType = detectedActivity.getType();
            int confidence = detectedActivity.getConfidence();
            String activityName = getNameFromType(activityType);

            if (confidence >=70)
            {
            	switch(activityType)
            	{
            	case 0:
            		_appPrefs.updateVehicleTimer(_appPrefs.getVehicleTimer()+120000);
            		break;
            	case 1:
            		_appPrefs.updateBikeTimer(_appPrefs.getBikeTimer()+120000);
            		break;
            	case 2:
            		_appPrefs.updateFootTimer(_appPrefs.getFootTimer()+120000);
            		break;
            	case 3:
            		_appPrefs.updateStillTimer(_appPrefs.getStillTimer()+120000);
            		break;
            	case 4:
            		_appPrefs.updateUnknownTimer(_appPrefs.getUnknownTimer()+120000);
            		break;
            	case 5:
            		_appPrefs.updateTiltingTimer(_appPrefs.getTiltingTimer()+120000);
            		break;
            		
            	}
            
	            logActivityClassificationReading(System.currentTimeMillis(),DateFormatUtils.getMonth(System.currentTimeMillis()),DateFormatUtils.getMonthDay(System.currentTimeMillis()),DateFormatUtils.getTime(System.currentTimeMillis()),activityType,activityName,confidence);
	            try {	 //Close the file, and catch an IOException if it occurs  	
					activityRecognitionWriter.close();
				}
				catch (IOException e){
					Log.e(TAG, "Error closing phone activity rec log file: "+e.toString());
				}
	            
	            logActivityTimerReading(System.currentTimeMillis(),DateFormatUtils.getMonth(System.currentTimeMillis()),DateFormatUtils.getMonthDay(System.currentTimeMillis()),DateFormatUtils.getTime(System.currentTimeMillis()),_appPrefs.getVehicleTimer(),_appPrefs.getBikeTimer(),_appPrefs.getFootTimer(),_appPrefs.getStillTimer(),_appPrefs.getUnknownTimer(),_appPrefs.getTiltingTimer());
	            try {	 //Close the file, and catch an IOException if it occurs  	
					activityTimerWriter.close();
				}
				catch (IOException e){
					Log.e(TAG, "Error closing phone activity rec log file: "+e.toString());
				}
	            break;
            }
            
            
          
        }
    }

    /**
     * Map detected activity types to strings
     *
     * @param activityType The detected activity type
     * @return A user-readable name for the type
     */
    private String getNameFromType(int activityType) {
        switch(activityType) {
            case DetectedActivity.IN_VEHICLE:
                return "in_vehicle";
            case DetectedActivity.ON_BICYCLE:
                return "on_bicycle";
            case DetectedActivity.ON_FOOT:
                return "on_foot";
            case DetectedActivity.STILL:
                return "still";
            case DetectedActivity.UNKNOWN:
                return "unknown";
            case DetectedActivity.TILTING:
                return "tilting";
        }
        return "unknown";
    }
    
    private boolean logActivityClassificationReading(Long cursystime,String month, String monthDay,String currentDateandTime, int activity_done, String FriendlyName, int confidence) {
    	String fileText = cursystime+","+month+","+monthDay+","+currentDateandTime+","+activity_done+","+FriendlyName+","+confidence+"\n";
    	try {
			activityRecognitionWriter.append(fileText);
			activityRecognitionWriter.flush();
			return true;
		}
    	catch (IOException e) {
    		Log.e(TAG, "Could not write to phone activity recognition log file: "+e.toString());
    		return false;
		}
	}
    
    private boolean logActivityTimerReading(Long cursystime,String month, String monthDay,String currentDateandTime, long vehicleTimer, long bikeTimer, long footTimer, long stillTimer, long unknownTimer, long tiltingTimer) {
    	String fileText = cursystime+","+month+","+monthDay+","+currentDateandTime+","+vehicleTimer+","+bikeTimer+","+footTimer+","+stillTimer+","+unknownTimer+","+tiltingTimer+"\n";
    	try {
    		activityTimerWriter.append(fileText);
    		activityTimerWriter.flush();
			return true;
		}
    	catch (IOException e) {
    		Log.e(TAG, "Could not write to phone activity timer log file: "+e.toString());
    		return false;
		}
	}
    
}