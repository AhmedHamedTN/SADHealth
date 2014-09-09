/**JSONWeatherParser.java
 * This class is responsible for receiving data in JSON format from WeatherHttpClient and extracting it for use with the Weather data structures.
 * extracts based on the example from the following api provider. http://openweathermap.org/API
 * It is generally a small modified version of the weather app tutorial found here: http://www.javacodegeeks.com/2013/06/android-build-real-weather-app-json-http-and-openweathermap.html
 * @Author (Modified)Kiril Tzvetanov Goguev
 * @Date October 15th,2013
 */
package uu.core.sadhealth.weather;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class JSONWeatherParser
{
	public static WeatherData getWeather(String data) throws JSONException
	{
		//create new variable to hold all the weather data we want
		WeatherData Weather = new WeatherData();
		
		//init JSONobjects for parsing of the weatherdata
		JSONObject jObj = new JSONObject(data);
		
		Weather.currentLocation.setCity(getString("name", jObj));
		
		JSONObject sysObj = getObject("sys", jObj);
		Weather.currentLocation.setCountry(getString("country", sysObj));
		Weather.sunphase.setSunrise(getLong("sunrise", sysObj));
		Weather.sunphase.setSunset(getLong("sunset", sysObj));
		
		JSONArray jArr = jObj.getJSONArray("weather");

		// We use only the first value
		JSONObject JSONWeather = jArr.getJSONObject(0);
		Weather.currentCondition.setWeatherId(getInt("id", JSONWeather));
		Weather.currentCondition.setDescr(getString("description", JSONWeather));
		Weather.currentCondition.setCondition(getString("main", JSONWeather));
		Weather.currentCondition.setIcon(getString("icon", JSONWeather));

		JSONObject mainObj = getObject("main", jObj);
		Weather.currentCondition.setHumidity(getInt("humidity", mainObj));
		Weather.currentCondition.setPressure(getInt("pressure", mainObj));
		Weather.temperature.setMaxTemp(getFloat("temp_max", mainObj));
		Weather.temperature.setMinTemp(getFloat("temp_min", mainObj));
		Weather.temperature.setTemp(getFloat("temp", mainObj));

		// Wind
		JSONObject wObj = getObject("wind", jObj);
		Weather.wind.setSpeed(getFloat("speed", wObj));
		Weather.wind.setDeg(getFloat("deg", wObj));

		// Clouds
		JSONObject cObj = getObject("clouds", jObj);
		Weather.clouds.setPerc(getInt("all", cObj));
		
		/*
		JSONObject currentObservation = getObject("current_observation", jObj);
		JSONObject displayLocation=getObject("display_location",currentObservation);
		
		Weather.currentLocation.setCity(getString("city",displayLocation));
		Weather.currentLocation.setCountry(getString("country",displayLocation));
		
		Weather.currentCondition.setCondition(getString("weather",currentObservation));
		Weather.currentCondition.setIcon(getString("icon",currentObservation));
		Weather.currentCondition.setIconURL(getString("icon_url",currentObservation));
		Weather.temperature.setTemp(getFloat("temp_c",currentObservation));
		
		JSONObject sunPhase = getObject("sun_phase",jObj);
		JSONObject sunPhase_sunrise =getObject("sunrise",sunPhase);
		JSONObject sunPhase_sunset=getObject("sunset",sunPhase);
		
		Weather.sunphase.setSunRiseHr(getString("hour",sunPhase_sunrise));
		Weather.sunphase.setSunRiseMin(getString("minute",sunPhase_sunrise));
		
		Weather.sunphase.setSunSetHr(getString("hour",sunPhase_sunset));
		Weather.sunphase.setSunSetMin(getString("minute",sunPhase_sunset));*/
		
		return Weather;
		
		
	}
	
	private static JSONObject getObject(String tagName, JSONObject jObj)  throws JSONException {
		JSONObject subObj = jObj.getJSONObject(tagName);
		return subObj;
	}

	private static String getString(String tagName, JSONObject jObj) throws JSONException {
		return jObj.getString(tagName);
	}

	private static float  getFloat(String tagName, JSONObject jObj) throws JSONException {
		return (float) jObj.getDouble(tagName);
	}

	private static int  getInt(String tagName, JSONObject jObj) throws JSONException {
		return jObj.getInt(tagName);
		
	}
	
	private static long getLong(String tagName, JSONObject jObj) throws JSONException{
		return jObj.getLong(tagName);
	}

	
}