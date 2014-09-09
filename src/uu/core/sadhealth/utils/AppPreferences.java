/** AppPreferences.java
 *  This class consists of methods which manage this application's shared preferences and data
 *  The shared preferences hold all the necessary key-values for switches in this application
 *  Values here are saved even after shutdown of app.
 *  Standard get/set methods for OOP.
 *  @author Kiril Tzvetanov Goguev
 *  @date Oct 20th, 2013
 */
package uu.core.sadhealth.utils;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;

public class AppPreferences
{
	//These preferences are for the user login & registration tokens
	public static final String PREF_USER_EMAIL="user_email";
	public static final String PREF_USER_NAME="user_id";
	public static final String PREF_USER_REG="user_registered";
	public static final String PREF_USER_AUTH_TOKEN="user_token";
	
	//These preferences are for the Data Collection and the NetworkMode configurations
	public static final String PREF_DC_LIGHT="Light_DC";
	public static final String PREF_DC_ACCEL="Accelerometer_DC";
	public static final String PREF_DC_LOCATION="Location_DC";
	public static final String PREF_DU_NETWORKMODE="Data_UploadConfig";
	public static final String PREF_DIAGNOSTICS_SHOW="DiagnosticsShow";
	public static final String PREF_QUESTIONNAREOPTIN="QuestionnareShow";
	
	//These preferences are for the Location Services
	public static final String LAST_KNOWN_LOC="location";
	public static final String LAST_KNOWN_LAT="lat";
	public static final String LAST_KNOWN_LON="lon";
	public static final String CITY="city";
	public static final String COUNTRY="country";
	
	public static final String ALSTIME="als_time";
	public static final String NLSTIME="nls_time";
	public static final String CURWEATHER="current_Weather";
	
	public static final String PERIODNO="periodNo";
	public static final String NEXTUPLOADTIME ="nextUploadTime";
	
	public static final String LASTSUCCESSFULUPLOAD="lastPeriodUploaded";
	public static final String UPLOADCOUNTER ="uploadedcounter";
	
	public static final String GRAPHSELECTION ="selectedgraph";
	public static final String GRAPHSELECTIONCOMMUNITY ="selectedgraphcommunity";
	public static final String GRAPHSPINNERSELECTION="graph_spinnerSelection";
	public static final String GRAPHGRANULARITYSELECTION="granularity_spinnerSelection";
	public static final String GRAPHSPINNERSELECTIONCOMMUNITY="graph_spinnerSelectionCommunity";
	public static final String GRAPHGRANULARITYSELECTIONCOMMUNITY="granularity_spinnerSelectionCommunity";
	public static final String TOTALSGRANULARITYSELECTION="totalsgranularity_spinnerSelection";
	
	public static final String VERSIONNR="version";
	public static final String TUTORIAL="tutorial";
	
	
	
	public static final String WEATHERCITY="weather_city";
	public static final String WEATHERCOUNTRY="weather_country";
	public static final String WEATHERCONDITION="weather_condition";
	public static final String WEATHERTEMP="weather_temp";
	public static final String WEATHERICONNAME="weather_icon_name";
	public static final String WEATHERSUNTOTALTIME="weather_sun_total_time";
	public static final String WEATHERSUNRISETIME="weather_sun_rise_time";
	public static final String WEATHERSUNSETTIME="weather_sun_set_time";
	public static final String WEATHERPOTLUX="weather_potential_lux";
	
	public static final String MAXLUXVALUE="max_luxValue";
	
	public static final String PREVIOUSACTIVITY="previous_activity";
	public static final String VEHICILETIMER="vehicle_timer";
	public static final String BIKETIMER="bike_timer";
	public static final String FOOTTIMER="foot_timer";
	public static final String STILLTIMER="still_timer";
	public static final String UNKNOWNTIMER="unknown_timer";
	public static final String TILTINGTIMER="tilting_timer";
	
	
	//Crowd source Weather values-change this to SQL Database later
	public static final String TSTORMLRAIN ="thunderstormLightRain";
	public static final String TSTORMRAIN="thunderstormRain";
	public static final String TSTORMHRAIN="thunderstormHeavyRain";
	public static final String TSTORM="thunderstorm";
	
	
	private static final String APP_SHARED_PREFS = AppPreferences.class.getSimpleName();
	
	private SharedPreferences _sharedPrefs;
    private Editor _prefsEditor;
    
    public AppPreferences(Context context) {
        this._sharedPrefs = context.getSharedPreferences(APP_SHARED_PREFS, Activity.MODE_PRIVATE);
        this._prefsEditor = _sharedPrefs.edit();
    }
    
    public String getUserEmail() {
        return _sharedPrefs.getString(PREF_USER_EMAIL, null); // Get the user's email, for use with Google Auth services
    }

    public void setUserEmail(String text) {
        _prefsEditor.putString(PREF_USER_EMAIL, text);
        _prefsEditor.commit();
    }
	
    public String getUserID() {
        return _sharedPrefs.getString(PREF_USER_NAME, null); // Get the users name from the google account. Used for display of the person who is logged in on the action bar
    }

    public void setUserID(String text) {
        _prefsEditor.putString(PREF_USER_NAME, text);
        _prefsEditor.commit();
    }
	
    public boolean getUserRegistered() {
        return _sharedPrefs.getBoolean(PREF_USER_REG, false); // Get whether or not a user is currently registered in the app. For use on first run of the application, and re-registering a new user
    }

    public void setUserRegistered(Boolean value) {
        _prefsEditor.putBoolean(PREF_USER_REG, value);
        _prefsEditor.commit();
    }
    
    public String getUserAuthToken() {
        return _sharedPrefs.getString(PREF_USER_AUTH_TOKEN, null); // Get the current user's Google Account Auth token.
    }

    public void setUserAuthToken(String text) {
        _prefsEditor.putString(PREF_USER_AUTH_TOKEN, text);
        _prefsEditor.commit();
    }
    
    public Boolean getDCLight() {
        return _sharedPrefs.getBoolean(PREF_DC_LIGHT, true); // Get the current user's Google Account Auth token.
    }

    public void setDCLight(Boolean value) {
        _prefsEditor.putBoolean(PREF_USER_AUTH_TOKEN, value);
        _prefsEditor.commit();
    }
    
    public Boolean getDCAccelerometer() {
        return _sharedPrefs.getBoolean(PREF_DC_ACCEL, true); // Get the current user's Google Account Auth token.
    }

    public void setDCAccelerometer(Boolean value) {
        _prefsEditor.putBoolean(PREF_DC_ACCEL, value);
        _prefsEditor.commit();
    }
    
    public Boolean getDCLocation() {
        return _sharedPrefs.getBoolean(PREF_DC_LIGHT, true); // Get the current user's Google Account Auth token.
    }

    public void setDCLocation(Boolean value) {
        _prefsEditor.putBoolean(PREF_DC_LOCATION, value);
        _prefsEditor.commit();
    }
    
    public int getDUNetworkMode() {
        return _sharedPrefs.getInt(PREF_DU_NETWORKMODE, 1); // Get the current user's Google Account Auth token.
    }

    public void setDUNetworkMode(int value) {
        _prefsEditor.putInt(PREF_DU_NETWORKMODE, value);
        _prefsEditor.commit();
    }
    
    public void setDiagnosticsShow(Boolean value)
    {
    	_prefsEditor.putBoolean(PREF_DIAGNOSTICS_SHOW, value);
        _prefsEditor.commit();
    }
    
    public Boolean getDiagnosticsShow() {
        return _sharedPrefs.getBoolean(PREF_DU_NETWORKMODE, false); // Get the current user's Google Account Auth token.
    }
    
    
    public String getLocation()
    {
    	return _sharedPrefs.getString(LAST_KNOWN_LOC, null);
    }
    
    public void setLocation(String text) {
        _prefsEditor.putString(LAST_KNOWN_LOC, text);
        _prefsEditor.commit();
    }
    
    public String getLat()
    {
    	return _sharedPrefs.getString(LAST_KNOWN_LAT, null);
    }
    
    public void setLat(String text) {
        _prefsEditor.putString(LAST_KNOWN_LAT, text);
        _prefsEditor.commit();
    }
    
    public String getLon()
    {
    	return _sharedPrefs.getString(LAST_KNOWN_LON, null);
    }
    
    public void setLon(String text) {
        _prefsEditor.putString(LAST_KNOWN_LON, text);
        _prefsEditor.commit();
    }
    
    public String getCity()
    {
    	return _sharedPrefs.getString(CITY, null);
    }
    
    public void setCity(String text) {
        _prefsEditor.putString(CITY, text);
        _prefsEditor.commit();
    }
    
    public String getCountry()
    {
    	return _sharedPrefs.getString(COUNTRY, null);
    }
    
    public void setCountry(String text) {
        _prefsEditor.putString(COUNTRY, text);
        _prefsEditor.commit();
    }
    

	
	public String getWeatherCountry()
	{
		return _sharedPrefs.getString(WEATHERCOUNTRY, null);
	}
	
	public void setWeatherCountry(String value)
	{
		_prefsEditor.putString(WEATHERCOUNTRY, value);
		_prefsEditor.commit();
	}
	
	public String getWeatherCity()
    {
    	return _sharedPrefs.getString(WEATHERCITY, null);
    }
    
    public void setWeatherCity(String text) {
        _prefsEditor.putString(WEATHERCITY, text);
        _prefsEditor.commit();
    }
    
    public String getWeatherCondition()
    {
    	return _sharedPrefs.getString(WEATHERCONDITION, null);
    }
    
    public void setWeatherCondition(String text) {
        _prefsEditor.putString(WEATHERCONDITION, text);
        _prefsEditor.commit();
    }
    
    public float getWeatherTemp()
    {
    	return _sharedPrefs.getFloat(WEATHERTEMP, (float) 0.0);
    }
    
    public void setWeatherTemp(float value) {
        _prefsEditor.putFloat(WEATHERTEMP, value);
        _prefsEditor.commit();
    }
    
    public String getWeatherIconName()
    {
    	return _sharedPrefs.getString(WEATHERICONNAME, "weather_sunny_n");
    }
    
    public void setWeatherIconName(String text)
    {
    	_prefsEditor.putString(WEATHERICONNAME, text);
    	_prefsEditor.commit();
    }
    //Total
    public String getWeatherTotalSunlightHrs()
    {
    	return _sharedPrefs.getString(WEATHERSUNTOTALTIME, null);
    }
    
    public void setWeatherTotalSunlightHrs(String text) {
        _prefsEditor.putString(WEATHERSUNTOTALTIME, text);
        _prefsEditor.commit();
    }
    
    //SunRise
    public String getWeatherSunRise()
    {
    	return _sharedPrefs.getString(WEATHERSUNRISETIME, null);
    }
    
    public void setWeatherSunRise(String text) {
        _prefsEditor.putString(WEATHERSUNRISETIME, text);
        _prefsEditor.commit();
    }
    
  //SunSet
    public String getWeatherSunSet()
    {
    	return _sharedPrefs.getString(WEATHERSUNSETTIME, null);
    }
    
    public void setWeatherSunSet(String text) {
        _prefsEditor.putString(WEATHERSUNSETTIME, text);
        _prefsEditor.commit();
    }
    
  //Potential lux
    public String getWeatherPotentialLux()
    {
    	return _sharedPrefs.getString(WEATHERPOTLUX, null);
    }
    
    public void setWeatherPotentialLux(String text) {
        _prefsEditor.putString(WEATHERPOTLUX, text);
        _prefsEditor.commit();
    }
    
    
    
    
    public String getALSTime()
    {
    	return _sharedPrefs.getString(ALSTIME, "00:00:00");
    }
    
    public void setALSTime(String text) {
        _prefsEditor.putString(ALSTIME, text);
        _prefsEditor.commit();
    }
    
    public String getNLSTime()
    {
    	return _sharedPrefs.getString(NLSTIME, "00:00:00");
    }
    
    public void setNLSTime(String text) {
        _prefsEditor.putString(NLSTIME, text);
        _prefsEditor.commit();
    }
    
    public String getCurrentWeatherCondition()
    {
    	return _sharedPrefs.getString(CURWEATHER, null);
    }
    
    public void setCurrentWeatherCondition(String text)
    {
    	_prefsEditor.putString(CURWEATHER, text);
    	_prefsEditor.commit();
    }
    
    public void setUploadCounter(int value)
    {
    	_prefsEditor.putInt(UPLOADCOUNTER, value);
    	_prefsEditor.commit();
    }
    
    public int getUploadCounter()
    {
    	return _sharedPrefs.getInt(UPLOADCOUNTER, 0);
    }
    
    public void setGraphSelection(int id)
    {
    	_prefsEditor.putInt(GRAPHSELECTION, id);
    	_prefsEditor.commit();
    }
    
    public int getGraphSelection(Context context)
    {
    	
    	return _sharedPrefs.getInt(GRAPHSELECTION, context.getResources().getIdentifier("LightGraph", "id", "uu.core.sadhealth"));
    }
    
    public void setGraphSelectionCommunity(int id)
    {
    	_prefsEditor.putInt(GRAPHSELECTIONCOMMUNITY, id);
    	_prefsEditor.commit();
    }
    
    public int getGraphSelectionCommunity(Context context)
    {
    	
    	return _sharedPrefs.getInt(GRAPHSELECTIONCOMMUNITY, context.getResources().getIdentifier("LightGraph", "id", "uu.core.sadhealth"));
    }
    
    
    public void setGraphSpinnerSelection(String value)
    {
    	_prefsEditor.putString(GRAPHSPINNERSELECTION, value);
    	_prefsEditor.commit();
    }
    
    public String getGraphSpinnerSelection()
    {
    	
    	return _sharedPrefs.getString(GRAPHSPINNERSELECTION, null);
    }
    
    public void setGraphSpinnerSelectionCommunity(String value)
    {
    	_prefsEditor.putString(GRAPHSPINNERSELECTIONCOMMUNITY, value);
    	_prefsEditor.commit();
    }
    
    public String getGraphSpinnerSelectionCommunity()
    {
    	
    	return _sharedPrefs.getString(GRAPHSPINNERSELECTIONCOMMUNITY, null);
    }
    
    public void setGranularitySpinnerSelection(String value)
    {
    	_prefsEditor.putString(GRAPHGRANULARITYSELECTION, value);
    	_prefsEditor.commit();
    }
    
    public String getGranularitySpinnerSelection()
    {
    	
    	return _sharedPrefs.getString(GRAPHGRANULARITYSELECTION, null);
    }
    
    public void setGranularitySpinnerSelectionCommunity(String value)
    {
    	_prefsEditor.putString(GRAPHGRANULARITYSELECTIONCOMMUNITY, value);
    	_prefsEditor.commit();
    }
    
    public String getGranularitySpinnerSelectionCommunity()
    {
    	
    	return _sharedPrefs.getString(GRAPHGRANULARITYSELECTIONCOMMUNITY, null);
    }
    
    public void setTotalsGranularitySpinnerSelection(String value)
    {
    	_prefsEditor.putString(TOTALSGRANULARITYSELECTION, value);
    	_prefsEditor.commit();
    }
    
    public String getTotalsGranularitySpinnerSelection()
    {
    	
    	return _sharedPrefs.getString(TOTALSGRANULARITYSELECTION, null);
    }
    
    
    
    public String getNextUploadTime()
    {
    	return _sharedPrefs.getString(NEXTUPLOADTIME, "0:00");
    }
    
    public void setNextUploadTime(String text) {
        _prefsEditor.putString(NEXTUPLOADTIME, text);
        _prefsEditor.commit();
    }
    
    public void setQuestionnareShow(Boolean value)
    {
    	_prefsEditor.putBoolean(PREF_QUESTIONNAREOPTIN, value);
        _prefsEditor.commit();
    }
    
    public Boolean getQuestionnareShow() {
        return _sharedPrefs.getBoolean(PREF_QUESTIONNAREOPTIN, true); // Get the current users preference for showing Questionnares
    }
    
    public int getVersionNR() {
        return _sharedPrefs.getInt(VERSIONNR, 9); // Get the current users preference for showing Questionnares
    }
    
    public void setVersionNR(int versionCode)
    {
    	_prefsEditor.putInt(VERSIONNR, versionCode);
        _prefsEditor.commit();
    }
    
    public Boolean getShowTutorial(){
    	return _sharedPrefs.getBoolean(TUTORIAL, true);
    }
    
    public void setShowTutorial(Boolean value)
    {
    	_prefsEditor.putBoolean(TUTORIAL, value);
        _prefsEditor.commit();
    }
    
    public void setPeriodNo(int periodNo)
    {
    	_prefsEditor.putInt(PERIODNO, periodNo);
        _prefsEditor.commit();
    }
    
    public int getPeriodNo()
    {
    	return _sharedPrefs.getInt(PERIODNO, Integer.parseInt(DateFormatUtils.getPeriodNo(System.currentTimeMillis())));
        
    }
    
    public void setLastPeriodUploaded(int periodNo)
    {
    	_prefsEditor.putInt(LASTSUCCESSFULUPLOAD, periodNo);
    	_prefsEditor.commit();
    }
    
    public int getLastPeriodUploaded()
    {
    	return _sharedPrefs.getInt(LASTSUCCESSFULUPLOAD,getPeriodNo()-1);
    }
    
    public int getPreviousActivity() {
        return _sharedPrefs.getInt(PREVIOUSACTIVITY, 4); // Get the current user's Google Account Auth token.
    }

    public void setPreviousActivity(int value) {
        _prefsEditor.putInt(PREVIOUSACTIVITY, value);
        _prefsEditor.commit();
    }
    
    public float getMaxLux()
    {
    	return _sharedPrefs.getFloat(MAXLUXVALUE, (float) 0.0);
    }
    
    public void setMaxLux(float value) {
        _prefsEditor.putFloat(MAXLUXVALUE, value);
        _prefsEditor.commit();
    }
    
    public long getVehicleTimer()
    {
    	return _sharedPrefs.getLong(VEHICILETIMER, 000000);
    }
    
    public void updateVehicleTimer(long value)
    {
    	_prefsEditor.putLong(VEHICILETIMER, value);
    	_prefsEditor.commit();
    }
    
    public long getBikeTimer()
    {
    	return _sharedPrefs.getLong(BIKETIMER, 000000);
    }
    
    public void updateBikeTimer(long value)
    {
    	_prefsEditor.putLong(BIKETIMER, value);
    	_prefsEditor.commit();
    }
    
    public long getFootTimer()
    {
    	return _sharedPrefs.getLong(FOOTTIMER, 000000);
    }
    
    public void updateFootTimer(long value)
    {
    	_prefsEditor.putLong(FOOTTIMER, value);
    	_prefsEditor.commit();
    }
    
    public long getStillTimer()
    {
    	return _sharedPrefs.getLong(STILLTIMER, 000000);
    }
    
    public void updateStillTimer(long value)
    {
    	_prefsEditor.putLong(STILLTIMER, value);
    	_prefsEditor.commit();
    }
    
    public long getUnknownTimer()
    {
    	return _sharedPrefs.getLong(UNKNOWNTIMER, 000000);
    }
    
    public void updateUnknownTimer(long value)
    {
    	_prefsEditor.putLong(UNKNOWNTIMER, value);
    	_prefsEditor.commit();
    }
    
    public long getTiltingTimer()
    {
    	return _sharedPrefs.getLong(TILTINGTIMER, 000000);
    }
    
    public void updateTiltingTimer(long value)
    {
    	_prefsEditor.putLong(TILTINGTIMER, value);
    	_prefsEditor.commit();
    }
    
}