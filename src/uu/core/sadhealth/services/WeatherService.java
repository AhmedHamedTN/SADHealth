/**WeatherService.java
 * This class is part of the Services package in SADHealth
 * It is responsible for invoking the calls to the weatherHttpClient through the use of Asynchronous tasks (assuming internet connection is established)
 * It will retrieve weather from the current API provider (OpenWeatherMap) based on the Location(Longitude & Latitude) from Google's fused location services.
 * @author Kiril Tzvetanov Goguev
 * @Date January 28th,2014
 */
package uu.core.sadhealth.services;

import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.json.JSONException;

import com.crashlytics.android.Crashlytics;


import uu.core.sadhealth.utils.AppPreferences;
import uu.core.sadhealth.utils.FileToWrite;
import uu.core.sadhealth.weather.JSONWeatherParser;
import uu.core.sadhealth.weather.WeatherData;
import uu.core.sadhealth.weather.WeatherHttpClient;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.IBinder;
import android.util.Log;
import android.view.View;

public class WeatherService extends Service
{
	
	private final String TAG = "WeatherService";
	private AppPreferences _appPrefs;
	private FileWriter weatherWriter;
	private String location;
	private int tot_SunHrs, tot_SunMins, sunRiseHr, sunRiseMin, sunSetHr,sunSetMin;
	
	public void onCreate(){
		super.onCreate();
		Log.i(TAG,"started");
		_appPrefs = new AppPreferences(getApplicationContext());
		setupWeatherTask();
		
	}
	
	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
    public void onDestroy() 
    {
		super.onDestroy();  
		Log.i(TAG,"stopped");
		stopWeatherService();
        
    }
	
	private void setupWeatherTask()
	{
		try
        {
			weatherWriter = FileToWrite.createLogFileWriter("weather.csv",_appPrefs.getUserID());
			if(weatherWriter==null){
				Log.v(TAG, "Failed to open file for weather log");
			}
			
            ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo ni;
            if(cm != null)
            {
           	 
           	 Crashlytics.setString("Weather-code:", "Trying to Update Weather");
               
                ni=cm.getActiveNetworkInfo();
              //get last known location
        		location=_appPrefs.getLocation();
        		if (location !=null && ni !=null)
        		{
        			if (ni.isConnected())
        			{
        				
        				Crashlytics.setString("Last Action:", "Update Weather network not null");
                
	         	        JSONWeatherTask task = new JSONWeatherTask();
	         	        task.execute(_appPrefs.getLat(),_appPrefs.getLon());
        			}
                }
            }
        }
        catch(Exception e)
        {
       	 
        }
		
	}
	
	private class JSONWeatherTask extends AsyncTask<String, Void, WeatherData> {

		@Override
		protected WeatherData doInBackground(String... params) {
			
			WeatherData weather = new WeatherData();
			Log.i(TAG, "LAT="+params[0]+" LON="+params[1]);
			String data = ( (new WeatherHttpClient()).getWeatherData(params[0], params[1]));

			try 
			{	
				if (data !=null)
				{
					weather = JSONWeatherParser.getWeather(data);
				}

			} catch (JSONException e) {				
				e.printStackTrace();
			}
			return weather;

	}


	@Override
		protected void onPostExecute(WeatherData weather) {			
			super.onPostExecute(weather);
			Log.i(TAG, "retrieved-weatherData!");
			//int sunRiseHr = 0,sunRiseMin=0, sunSetHr=0, sunSetMin=0,tot_SunHrs,tot_SunMins ;
			SimpleDateFormat sdf = new SimpleDateFormat("MM/dd-HH:mm");
			SimpleDateFormat iconTime=new SimpleDateFormat("HH:mm");
			SimpleDateFormat hoursFormat=new SimpleDateFormat("HH");
			SimpleDateFormat minutesFormat=new SimpleDateFormat("mm");
			String currentDateandTime = sdf.format(new Date(System.currentTimeMillis()));
			String convertedIconName, potentialLux,sunlightHrsStr;
			String SimpleEvent;
			long sunRise, sunSet;
			int result=-1;
			boolean useNightIcon=false;
			
			_appPrefs.setWeatherCountry(weather.currentLocation.getCountry());
			_appPrefs.setWeatherCity( weather.currentLocation.getCity());
			_appPrefs.setWeatherCondition(weather.currentCondition.getDescr());
			_appPrefs.setWeatherTemp(weather.temperature.getTemp());
			
			sunRise=weather.sunphase.getSunrise();
			sunSet=weather.sunphase.getSunset();
			
			Date sunRiseDate = new Date(sunRise*1000);
			Date sunSetDate = new Date(sunSet*1000);
			
			//check the current time against sun set 
			String currentIconTime = iconTime.format(new Date(System.currentTimeMillis()));
			String sunRiseTime= iconTime.format(new Date(sunRise*1000));
			String sunSetTime= iconTime.format(new Date(sunSet*1000));
			
			
			String sunRiseHrStr = hoursFormat.format(sunRiseDate);
			String sunRiseMinStr = minutesFormat.format(sunRiseDate);
			String sunSetHrStr = hoursFormat.format(sunSetDate);
			String sunSetMinStr = minutesFormat.format(sunSetDate);
			
			
			/*weatherIconName=weather.currentCondition.getIcon();
			
			Log.i(TAG, "weatherIconName "+ weatherIconName);
			*/
			
			_appPrefs.setCurrentWeatherCondition(weather.currentCondition.getDescr());
			
			
			try {
				sunRiseHr = Integer.parseInt(sunRiseHrStr);
			    sunRiseMin = Integer.parseInt(sunRiseMinStr);
			    sunSetHr = Integer.parseInt(sunSetHrStr);
			    sunSetMin = Integer.parseInt(sunSetMinStr);
			} 
			catch(NumberFormatException nfe) 
			{
				
			}
			
			tot_SunHrs=sunSetHr-sunRiseHr;
			tot_SunMins=sunSetMin-sunRiseMin;
			
			if (tot_SunMins < 0)
			{
				tot_SunMins=Math.abs(tot_SunMins);
			}
			
			Log.i(TAG, "tot_SunHrs "+ tot_SunHrs);
			Log.i(TAG, "tot_SunHrs "+ tot_SunMins);
			
			sunlightHrsStr=String.format("%02d", tot_SunHrs)+":"+String.format("%02d", tot_SunMins);
			
			
			
			try {
				Date curT = iconTime.parse(currentIconTime);
				Date sunR = iconTime.parse(sunRiseTime);
				Date sunS = iconTime.parse(sunSetTime);
				
				result=curT.compareTo(sunR);
				if(result==-1)
				{
					useNightIcon=true;
					
				}
				else
				{
					result=curT.compareTo(sunS);
					if (result==-1)
					{
						useNightIcon=false;
					}
					else
					{
						useNightIcon=true;
					}
				}
				
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			/**
			 * get the name of the icon from the weather data and then set the right image for the job
			 * all possible names are from here : http://www.wunderground.com/weather/api/d/community.html
			 */
			
			switch(weather.currentCondition.getWeatherId())
			{
			//Thunder Storm codes
			case 200:
				potentialLux="100";
				convertedIconName="weather_storm";
				SimpleEvent="Storm";
				break;
			case 201:
				potentialLux="100";
				convertedIconName="weather_storm";
				SimpleEvent="Storm";
				break;
			case 202:
				potentialLux="100";
				convertedIconName="weather_storm";
				SimpleEvent="Storm";
				break;
			case 210:
				potentialLux="100";
				convertedIconName="weather_storm";
				SimpleEvent="Storm";
				break;
			case 211:
				potentialLux="100";
				convertedIconName="weather_storm";
				SimpleEvent="Storm";
				break;
			case 212:
				potentialLux="100";
				convertedIconName="weather_storm";
				SimpleEvent="Storm";
				break;
			case 221:
				potentialLux="100";
				convertedIconName="weather_storm";
				SimpleEvent="Storm";
				break;
			case 230:
				potentialLux="100";
				convertedIconName="weather_storm";
				SimpleEvent="Storm";
				break;
			case 231:
				potentialLux="100";
				convertedIconName="weather_storm";
				SimpleEvent="Storm";
				break;
			case 232:
				potentialLux="100";
				convertedIconName="weather_storm";
				SimpleEvent="Storm";
				break;
				
			//Drizzle codes
			case 300:
				potentialLux="2000";
				convertedIconName="weather_lightrain";
				SimpleEvent="Rain";
				break;
			case 301:
				potentialLux="2000";
				convertedIconName="weather_lightrain";
				SimpleEvent="Rain";
				break;
			case 302:
				potentialLux="2000";
				convertedIconName="weather_lightrain";
				SimpleEvent="Rain";
				break;
			case 310:
				potentialLux="2000";
				convertedIconName="weather_lightrain";
				SimpleEvent="Rain";
				break;
			case 311:
				potentialLux="2000";
				convertedIconName="weather_lightrain";
				SimpleEvent="Rain";
				break;
			case 312:
				potentialLux="2000";
				convertedIconName="weather_lightrain";
				SimpleEvent="Rain";
				break;
			case 313:
				potentialLux="2000";
				convertedIconName="weather_lightrain";
				SimpleEvent="Rain";
				break;
			case 314:
				potentialLux="2000";
				convertedIconName="weather_lightrain";
				SimpleEvent="Rain";
				break;
			case 321:
				potentialLux="2000";
				convertedIconName="weather_lightrain";
				SimpleEvent="Rain";
				break;
				
			//Rain
			case 500:
				potentialLux="2000";
				convertedIconName="weather_lightrain";
				SimpleEvent="Rain";
				break;
			case 501:
				potentialLux="2000";
				convertedIconName="weather_rain";
				SimpleEvent="Rain";
				break;
			case 502:
				potentialLux="2000";
				convertedIconName="weather_rain";
				SimpleEvent="Rain";
				break;
			case 503:
				potentialLux="2000";
				convertedIconName="weather_rain";
				SimpleEvent="Rain";
				break;
			case 504:
				potentialLux="2000";
				convertedIconName="weather_rain";
				SimpleEvent="Rain";
				break;
			case 511:
				potentialLux="2000";
				convertedIconName="weather_icyrain";
				SimpleEvent="Rain";
				break;
			case 520:
				potentialLux="2000";
				convertedIconName="weather_rain";
				SimpleEvent="Rain";
				break;
			case 521:
				potentialLux="2000";
				convertedIconName="weather_rain";
				SimpleEvent="Rain";
				break;
			case 522:
				potentialLux="2000";
				convertedIconName="weather_rain";
				SimpleEvent="Rain";
				break;
			case 531:
				potentialLux="2000";
				convertedIconName="weather_rain";
				SimpleEvent="Rain";
				break;
				
			//Snow
			case 600:
				potentialLux="1000";
				convertedIconName="weather_snow";
				SimpleEvent="Snow";
				break;
			case 601:
				potentialLux="1000";
				convertedIconName="weather_snow";
				SimpleEvent="Snow";
				break;
			case 602:
				potentialLux="1000";
				convertedIconName="weather_snow";
				SimpleEvent="Snow";
				break;
			case 611:
				potentialLux="1000";
				convertedIconName="weather_snow";
				SimpleEvent="Snow";
				break;
			case 612:
				potentialLux="1000";
				convertedIconName="weather_snow";
				SimpleEvent="Snow";
				break;
			case 615:
				potentialLux="1000";
				convertedIconName="weather_snow";
				SimpleEvent="Snow";
				break;
			case 616:
				potentialLux="1000";
				convertedIconName="weather_snow";
				SimpleEvent="Snow";
				break;
			case 620:
				potentialLux="1000";
				convertedIconName="weather_snow";
				SimpleEvent="Snow";
				break;
			case 621:
				potentialLux="1000";
				convertedIconName="weather_snow";
				SimpleEvent="Snow";
				break;
			case 622:
				potentialLux="1000";
				convertedIconName="weather_snow";
				SimpleEvent="Snow";
				break;
				
			//Atmosphere
			case 701:
				potentialLux="7500";
				convertedIconName="weather_cloudyrain";
				SimpleEvent="Cloudy";
				break;
			case 711:
				potentialLux="20";
				convertedIconName="weather_haze";
				SimpleEvent="Fog";
				break;
			case 721:
				potentialLux="200";
				convertedIconName="weather_haze";
				SimpleEvent="Fog";
				break;
			case 731:
				potentialLux="200";
				convertedIconName="weather_haze";
				SimpleEvent="Fog";
				break;
			case 741:
				potentialLux="50";
				convertedIconName="weather_fog";
				SimpleEvent="Fog";
				break;
			case 751:
				potentialLux="200";
				convertedIconName="weather_haze";
				SimpleEvent="Fog";
				break;
			case 761:
				potentialLux="200";
				convertedIconName="weather_haze";
				SimpleEvent="Fog";
				break;
			case 762:
				potentialLux="200";
				convertedIconName="weather_haze";
				SimpleEvent="Fog";
				break;
			case 771:
				potentialLux="200";
				convertedIconName="weather_haze";
				SimpleEvent="Fog";
				break;
			case 781:
				potentialLux="200";
				convertedIconName="weather_haze";
				SimpleEvent="Fog";
				break;
				
			//Clouds
			case 800:
				potentialLux="65,000";
				convertedIconName="weather_sunny";
				SimpleEvent="Clear";
				break;
			case 801:
				potentialLux="30,000";
				convertedIconName="weather_mostlysunny";
				SimpleEvent="Clear";
				break;
			case 802:
				potentialLux="1000";
				convertedIconName="weather_mostlycloudy";
				SimpleEvent="Cloudy";
				break;
			case 803:
				potentialLux="1000";
				convertedIconName="weather_mostlycloudy";
				SimpleEvent="Cloudy";
				break;
			case 804:
				potentialLux="20,000";
				convertedIconName="weather_cloudy";
				SimpleEvent="Cloudy";
				break;
			default:
				potentialLux="0";
				convertedIconName="weather_sunny";
				SimpleEvent="Sunny";
				break;
			}
			
			//switch for night
			if(useNightIcon==true)
			{
				if(convertedIconName.equals("weather_chancesnow"))
				{
					potentialLux="10";
					convertedIconName=convertedIconName+"_n";
				}
				else if (convertedIconName.equals("weather_chancestorm"))
				{
					potentialLux="5";
					convertedIconName=convertedIconName+"_n";
				}
				else if (convertedIconName.equals("weather_cloudyrain"))
				{
					potentialLux="20";
					convertedIconName=convertedIconName+"_n";
				}
				else if (convertedIconName.equals("weather_mostlycloudy"))
				{
					potentialLux="25";
					convertedIconName=convertedIconName+"_n";
				}
				else if (convertedIconName.equals("weather_mostlysunny"))
				{
					potentialLux="5";
					convertedIconName=convertedIconName+"_n";
				}
				else if (convertedIconName.equals("weather_sunny"))
				{
					potentialLux="3";
					convertedIconName=convertedIconName+"_n";
				}
				
			}
			
			
			Log.i(TAG,"icon name is "+ convertedIconName);
			Log.i(TAG, "sunRiseTime "+ sunRiseTime);
			Log.i(TAG, "sunSetTime "+ sunSetTime);
			_appPrefs.setWeatherIconName(convertedIconName);
			_appPrefs.setWeatherTotalSunlightHrs(sunlightHrsStr);
			_appPrefs.setWeatherSunRise(sunRiseTime);
			_appPrefs.setWeatherSunSet(sunSetTime);
			_appPrefs.setWeatherPotentialLux(potentialLux);
			
			//imgView.setImageResource(getResources().getIdentifier(convertedIconName, "drawable", "uu.core.sadhealth"));
			
			//cityText.setText(city + "," + country );
			//condDescr.setText(condition);
			//temp.setText(String.format("%.1f", Float.valueOf(tempstr))+ " °C");
			//sunTotalView.setText(sunlightHrsStr);
			//sunRiseTimeView.setText(sunRiseTime);
			//sunSetTimeView.setText(sunSetTime);
			
			//calculate the potential lux
			//potLux.setText(potentialLux);

			//get latest entry & check if condition is the same
			
			logWeather(currentDateandTime,SimpleEvent,weather.temperature.getTemp(),weather.currentCondition.getPressure(),sunRiseHrStr,sunRiseMinStr,sunSetHrStr,sunSetMinStr,sunlightHrsStr);

		}

	}



	


	private boolean logWeather(String dateTime, String condition,float tempstr,float barometer ,String sunRiseHrStr,String sunRiseMinStr, String sunSetHrStr, String sunSetMinStr, String sunlightHrsStr) {
		String fileText = dateTime+","+condition+","+tempstr+","+sunRiseHrStr+","+sunRiseMinStr+","+sunSetHrStr+","+sunSetMinStr+","+sunlightHrsStr+"\n";
		try 
		{
			weatherWriter.append(fileText);
			weatherWriter.flush();
			return true;
		}
		catch (IOException e) 
		{
			Log.e(TAG, "Could not write to weather file: "+e.toString());
			return false;
		}

	}
	
	private void stopWeatherService()
	{
	
		try 
		{	  	
			//Close the file, and catch an IOException if it occurs 
			weatherWriter.close();
		}
		catch (IOException e)
		{
			Log.e(TAG, "Error closing weather log file: "+e.toString());
		}
	}
	
}