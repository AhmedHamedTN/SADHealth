package uu.core.sadhealth.locationgeo;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import uu.core.sadhealth.locationgeo.LocationData;



public class LocationGeoCoder{
	final static String TAG="LocationGeoCoder";
	
	
	
	private static String BASE_URL = "http://maps.google.com/maps/api/geocode/json?latlng=";
	public String getLocationGeoCode(String lat, String lon)
	{
		HttpURLConnection con = null ;
		InputStream is = null;
		try
		{
			con = (HttpURLConnection) ( new URL(BASE_URL+lat+","+lon+"&sensor=true")).openConnection();
			con.setRequestMethod("GET");
			con.setDoInput(true);
			con.setDoOutput(true);
			con.connect();
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
	
	public LocationData getLocationData(String data) throws JSONException
	{
		LocationData locData =new LocationData();
		JSONObject jObj=new JSONObject(data);
		JSONArray ResultsJSONArray = jObj.getJSONArray("results");
		
		
		JSONObject JSONAddressObject = ResultsJSONArray.getJSONObject(1);
	
		JSONArray JSONAddressArray=JSONAddressObject.getJSONArray("address_components");
		for(int i=0;i<JSONAddressArray.length();i++)
		{
			JSONObject jAdderObjects=JSONAddressArray.getJSONObject(i);
			if(jAdderObjects.getJSONArray("types").getString(0).compareTo("locality")==0)
			{
				locData.currentLocation.setCity(jAdderObjects.getString("long_name"));
			}
			else if(jAdderObjects.getJSONArray("types").getString(0).compareTo("country")==0)
			{
				locData.currentLocation.setCountry(jAdderObjects.getString("long_name"));
			}
		}
		
		
		
		return locData;
		
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
