package uu.core.sadhealth.services;

import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.json.JSONException;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import uu.core.sadhealth.locationgeo.LocationData;
import uu.core.sadhealth.locationgeo.LocationGeoCoder;

import uu.core.sadhealth.utils.AppPreferences;
import uu.core.sadhealth.utils.FileToWrite;
import uu.core.sadhealth.utils.DateFormatUtils;
import uu.core.sadhealth.weather.JSONWeatherParser;
import uu.core.sadhealth.weather.WeatherData;
import uu.core.sadhealth.weather.WeatherHttpClient;
import uu.core.sadhealth.MainActivity;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;

public class LocationService extends Service implements GooglePlayServicesClient.ConnectionCallbacks, GooglePlayServicesClient.OnConnectionFailedListener, LocationListener{
	
	private final String TAG = "LocationService";
	//Create shared preferences so we can store valuable data related to the app
	private AppPreferences _appPrefs;
	
	private FileWriter locationWriter;
	private Location newestLocation;
	 
	private LocationClient mLocationClient;
	private LocationRequest mLocationRequest;
	// Flag that indicates if a request is underway.
	private boolean mInProgress;
	    
	private Boolean servicesAvailable = false;
	private String periodNo;
	private int periodNoInt;

	public void onCreate(){
		super.onCreate();
		
		_appPrefs = new AppPreferences(getApplicationContext());
		periodNoInt=_appPrefs.getPeriodNo();
		periodNo= String.format("%04d", periodNoInt);
		
		mInProgress = false;
        // Create the LocationRequest object
        mLocationRequest = LocationRequest.create();
        // Use high accuracy
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        // Set the update interval to 5 seconds
        mLocationRequest.setInterval(100);
        servicesAvailable = servicesConnected();
        
        /*
         * Create a new location client, using the enclosing class to
         * handle callbacks.
         */
        mLocationClient = new LocationClient(this, this, this);
        mLocationClient.connect();
        
        locationWriter = FileToWrite.createLogFileWriter("phone_location"+periodNo+".csv",_appPrefs.getUserID());
		if(locationWriter==null){
			Log.v(TAG, "Failed to open file for location sensor log");
		}
		
		
	}
	
	private boolean servicesConnected() {
    	
        // Check that Google Play services is available
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        
        // If Google Play services is available
        if (ConnectionResult.SUCCESS == resultCode) {
            return true;
        } else {
 
            return false;
        }
    }
	
	
	private void stopLocation(){
		if (newestLocation!=null)
			logLocationReading(DateFormatUtils.getMonth(System.currentTimeMillis()),DateFormatUtils.getMonthDay(System.currentTimeMillis()),String.valueOf(System.currentTimeMillis()), newestLocation.getLatitude(), newestLocation.getLongitude(), newestLocation.getTime());
		try {	 //Close the file, and catch an IOException if it occurs  	
			locationWriter.close();
		}
		catch (IOException e){
			Log.e(TAG, "Error closing phone location log file: "+e.toString());
		}
		stopSelf();
	}
	
	private boolean logLocationReading(String month, String monthDay,String currentDateandTime, double lat, double lon, long tim) {
    	String fileText = month+","+monthDay+","+currentDateandTime+","+lat+","+lon+","+tim+"\n";
    	try {
			locationWriter.append(fileText);
			locationWriter.flush();
			return true;
		}
    	catch (IOException e) {
    		Log.e(TAG, "Could not write to phone accelerometer log file: "+e.toString());
    		return false;
		}
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	@Override
    public void onDestroy() 
    {
		super.onDestroy();
	    Log.i(TAG,"stopped");
    }
	  
	@Override
	public void onLocationChanged(Location location) {
		newestLocation = location;
		Log.i(TAG,"new location is determined");
		Log.i(TAG, "Location Request :" + location.getLatitude() + "," + location.getLongitude());
		_appPrefs.setLocation(location.getLatitude()+","+location.getLongitude());
		_appPrefs.setLat(String.valueOf(location.getLatitude()));
		_appPrefs.setLon(String.valueOf(location.getLongitude()));
		mLocationClient.removeLocationUpdates(this);
		Log.i(TAG,"listener has stopped");
		
		Geocoder gcd = new Geocoder(this, Locale.getDefault());
		List<Address> addresses;
		try {
			addresses = gcd.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
			if (addresses.size() > 0)
			{
				if(addresses.get(0).getCountryName()!=null)
				{
					_appPrefs.setCountry(addresses.get(0).getCountryName());
				}
			    //System.out.println(addresses.get(0).getLocality());
			    if (addresses.get(0).getLocality()!=null)
			    {
			    	_appPrefs.setCity(addresses.get(0).getLocality());
			    }
			}
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		Log.i(TAG, "City: " + _appPrefs.getCity());
		JSONLocationDataTask task = new JSONLocationDataTask();
	        task.execute(_appPrefs.getLat(),_appPrefs.getLon());
		
		stopLocation();

	}

	@Override
	public void onConnectionFailed(ConnectionResult arg0) {
		// TODO Auto-generated method stub
		Log.i(TAG, "onConnectionFailed");
		
	}

	@Override
	public void onConnected(Bundle arg0) {
		// TODO Auto-generated method stub
		Log.i(TAG, "onConnected");
		mLocationClient.requestLocationUpdates(mLocationRequest, this);
	}

	@Override
	public void onDisconnected() {
		// TODO Auto-generated method stub
		Log.i(TAG, "onDisconnected");
	}
	
	private class JSONLocationDataTask extends AsyncTask<String, Void, LocationData> {

		@Override
		protected LocationData doInBackground(String... params) {
			
			LocationData locdata = new LocationData();
			String data = ( (new LocationGeoCoder()).getLocationGeoCode(params[0], params[1]));

			try 
			{	
				if (data !=null)
				{
					locdata = new LocationGeoCoder().getLocationData(data);
					Log.i(TAG, "LocationGeoCoder City is "+locdata.currentLocation.getCity()+" Country is "+ locdata.currentLocation.getCountry());
					_appPrefs.setCity(locdata.currentLocation.getCity());
					_appPrefs.setCountry(locdata.currentLocation.getCountry());
				}

			} catch (JSONException e) {				
				e.printStackTrace();
			}
			
			return locdata;
			
	}


	@Override
		protected void onPostExecute(LocationData locData) {			
			super.onPostExecute(locData);
		
		
		}

	}

}
