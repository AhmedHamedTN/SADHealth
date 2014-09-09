/**WeatherHttpClient.java
 * This class is responsible for contacting the weather network api. Currently using OpenWeatherMap
 * Taken from the following tutorial: http://www.javacodegeeks.com/2013/06/android-build-real-weather-app-json-http-and-openweathermap.html
 * @Author (Modified) Kiril Tzvetanov Goguev
 * @Date October 15th,2013
 */
package uu.core.sadhealth.weather;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class WeatherHttpClient
{

	
	//private static String BASE_URL = "http://api.wunderground.com/api/6af0e833622fb9b8/astronomy/conditions/q/";
	private static String BASE_URL = "http://api.openweathermap.org/data/2.5/weather?";
	private static String UNITS="&units=metric";
	private static String APPID="&APPID=57f4e7895449fecd84e617966abd62f9";
	private static String OUTPUT =".json";
	
	private static String IMG_URL="http://icons.wxug.com/i/c/k/";

	public String getWeatherData(String lat, String lon) 
	{
		HttpURLConnection con = null ;
		InputStream is = null;
	
		try 
		{
			con = (HttpURLConnection) ( new URL(BASE_URL+"lat="+lat+"&lon="+lon+UNITS+APPID)).openConnection();
			con.setRequestMethod("GET");
			con.setDoInput(true);
			con.setDoOutput(true);
			con.connect();

			// Let's read the response
			StringBuffer buffer = new StringBuffer();
			is = con.getInputStream();
			BufferedReader br = new BufferedReader(new InputStreamReader(is));
			String line = null;
			while (  (line = br.readLine()) != null )
				buffer.append(line + "\r\n");

			is.close();
			con.disconnect();
			return buffer.toString();
		}
		catch(Throwable t) 
		{
			t.printStackTrace();
		}
		finally 
		{
			try { is.close(); } catch(Throwable t) {}
			try { con.disconnect(); } catch(Throwable t) {}
		}

		return null;
	}

	public byte[] getImage(String icon_url) 
	{
		HttpURLConnection con = null ;
		InputStream is = null;
		try 
		{
			con = (HttpURLConnection) ( new URL(icon_url)).openConnection();
			con.setRequestMethod("GET");
			con.setDoInput(true);
			con.setDoOutput(true);
			con.connect();

			// Let's read the response
			is = con.getInputStream();
			byte[] buffer = new byte[1024];
			ByteArrayOutputStream baos = new ByteArrayOutputStream();

			while ( is.read(buffer) != -1)
				baos.write(buffer);

			return baos.toByteArray();
		}
		catch(Throwable t) 
		{
			t.printStackTrace();
		}
		finally 
		{
			try { is.close(); } catch(Throwable t) {}
			try { con.disconnect(); } catch(Throwable t) {}
		}

		return null;
	}
}