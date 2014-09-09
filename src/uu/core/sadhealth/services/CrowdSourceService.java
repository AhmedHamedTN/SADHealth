/**CrowdSourceService.java
 * This service class is responsible for downloading pre-processed crowd source information from the server
 * Technique from: http://www.coderzheaven.com/2012/04/29/download-file-android-device-remote-server-custom-progressbar-showing-progress/
 * @Author Kiril Tzvetanov Goguev
 * @Date January 23rd, 2014
**/
package uu.core.sadhealth.services;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import uu.core.sadhealth.utils.AppPreferences;

import android.app.IntentService;
import android.app.Service;
import android.content.Intent;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;

public class CrowdSourceService extends IntentService
{
	private final String TAG="CrowdSource_Service";
	private AppPreferences _appPrefs;
	private final String baseurl = "http://darth.it.uu.se:1024/sad/CS/";
	private String country,city;

	public CrowdSourceService() {
		super("CrowdSourceService");
		
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		_appPrefs = new AppPreferences(getApplicationContext());
		country=_appPrefs.getCountry();
		city=_appPrefs.getCity();
		Log.i(TAG, "CrowdService-Started");
		DownloadLux();
		DownloadLight();
		DownloadQuestionnaire();
		DownloadActivity();
		
		
	}
	
	private void DownloadLux()
	{
		String luxURL=baseurl+country+"/"+city+"/Lux.csv";
		int totalSize = 0;
		int downloadedSize = 0;
		try
		{
			URL url =new URL(luxURL);
			Log.i(TAG, luxURL);
			HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
			urlConnection.setRequestMethod("GET");
            urlConnection.setDoOutput(true);
 
            //connect
            urlConnection.connect();
            
          //set the path where we want to save the file           
            File SDCardRoot = Environment.getExternalStorageDirectory(); 
            //create a new file, to save the downloaded file 
            File file = new File(SDCardRoot,"sadhealth/CS/"+country+"/"+city+"/");
            if (!file.exists()) {	//Create the subfolder if it does not exist
    			file.mkdirs();
    		}
            
            file = new File(SDCardRoot,"sadhealth/CS/"+country+"/"+city+"/Lux.csv");
  
            FileOutputStream fileOutput = new FileOutputStream(file);
 
            //Stream used for reading the data from the internet
            InputStream inputStream = urlConnection.getInputStream();
 
            //this is the total size of the file which we are downloading
            totalSize = urlConnection.getContentLength();
            
          //create a buffer...
            byte[] buffer = new byte[1024];
            int bufferLength = 0;
 
            while ( (bufferLength = inputStream.read(buffer)) > 0 ) {
                fileOutput.write(buffer, 0, bufferLength);
                downloadedSize += bufferLength;
            }
            fileOutput.close();
            Log.i(TAG, "Download done!");
		}
		catch (final MalformedURLException e) {
         
            e.printStackTrace();
		}
		 catch (final IOException e) {
	                    
	            e.printStackTrace();
	        }
	}
	
	private void DownloadLight()
	{
		String lightURL=baseurl+country+"/"+city+"/Light.csv";
		int totalSize = 0;
		int downloadedSize = 0;
		try
		{
			URL url =new URL(lightURL);
			Log.i(TAG, lightURL);
			HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
			urlConnection.setRequestMethod("GET");
            urlConnection.setDoOutput(true);
 
            //connect
            urlConnection.connect();
            
          //set the path where we want to save the file           
            File SDCardRoot = Environment.getExternalStorageDirectory(); 
            //create a new file, to save the downloaded file 
            File file = new File(SDCardRoot,"sadhealth/CS/"+country+"/"+city+"/");
            if (!file.exists()) {	//Create the subfolder if it does not exist
    			file.mkdirs();
    		}
            
            file = new File(SDCardRoot,"sadhealth/CS/"+country+"/"+city+"/Light.csv");
  
            FileOutputStream fileOutput = new FileOutputStream(file);
 
            //Stream used for reading the data from the internet
            InputStream inputStream = urlConnection.getInputStream();
 
            //this is the total size of the file which we are downloading
            totalSize = urlConnection.getContentLength();
            
          //create a buffer...
            byte[] buffer = new byte[1024];
            int bufferLength = 0;
 
            while ( (bufferLength = inputStream.read(buffer)) > 0 ) {
                fileOutput.write(buffer, 0, bufferLength);
                downloadedSize += bufferLength;
            }
            fileOutput.close();
            Log.i(TAG, "Download done!");
		}
		catch (final MalformedURLException e) {
         
            e.printStackTrace();
		}
		 catch (final IOException e) {
	                    
	            e.printStackTrace();
	        }
	}
	
	private void DownloadQuestionnaire()
	{
		String lightURL=baseurl+country+"/"+city+"/Questionnaire.csv";
		int totalSize = 0;
		int downloadedSize = 0;
		try
		{
			URL url =new URL(lightURL);
			Log.i(TAG, lightURL);
			HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
			urlConnection.setRequestMethod("GET");
            urlConnection.setDoOutput(true);
 
            //connect
            urlConnection.connect();
            
          //set the path where we want to save the file           
            File SDCardRoot = Environment.getExternalStorageDirectory(); 
            //create a new file, to save the downloaded file 
            File file = new File(SDCardRoot,"sadhealth/CS/"+country+"/"+city+"/");
            if (!file.exists()) {	//Create the subfolder if it does not exist
    			file.mkdirs();
    		}
            
            file = new File(SDCardRoot,"sadhealth/CS/"+country+"/"+city+"/Questionnaire.csv");
  
            FileOutputStream fileOutput = new FileOutputStream(file);
 
            //Stream used for reading the data from the internet
            InputStream inputStream = urlConnection.getInputStream();
 
            //this is the total size of the file which we are downloading
            totalSize = urlConnection.getContentLength();
            
          //create a buffer...
            byte[] buffer = new byte[1024];
            int bufferLength = 0;
 
            while ( (bufferLength = inputStream.read(buffer)) > 0 ) {
                fileOutput.write(buffer, 0, bufferLength);
                downloadedSize += bufferLength;
            }
            fileOutput.close();
            Log.i(TAG, "Download done!");
		}
		catch (final MalformedURLException e) {
         
            e.printStackTrace();
		}
		 catch (final IOException e) {
	                    
	            e.printStackTrace();
	        }
	}
	
	
	private void DownloadActivity()
	{
		String lightURL=baseurl+country+"/"+city+"/Activity.csv";
		int totalSize = 0;
		int downloadedSize = 0;
		try
		{
			URL url =new URL(lightURL);
			Log.i(TAG, lightURL);
			HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
			urlConnection.setRequestMethod("GET");
            urlConnection.setDoOutput(true);
 
            //connect
            urlConnection.connect();
            
          //set the path where we want to save the file           
            File SDCardRoot = Environment.getExternalStorageDirectory(); 
            //create a new file, to save the downloaded file 
            File file = new File(SDCardRoot,"sadhealth/CS/"+country+"/"+city+"/");
            if (!file.exists()) {	//Create the subfolder if it does not exist
    			file.mkdirs();
    		}
            
            file = new File(SDCardRoot,"sadhealth/CS/"+country+"/"+city+"/Activity.csv");
  
            FileOutputStream fileOutput = new FileOutputStream(file);
 
            //Stream used for reading the data from the internet
            InputStream inputStream = urlConnection.getInputStream();
 
            //this is the total size of the file which we are downloading
            totalSize = urlConnection.getContentLength();
            
          //create a buffer...
            byte[] buffer = new byte[1024];
            int bufferLength = 0;
 
            while ( (bufferLength = inputStream.read(buffer)) > 0 ) {
                fileOutput.write(buffer, 0, bufferLength);
                downloadedSize += bufferLength;
            }
            fileOutput.close();
            Log.i(TAG, "Download done!");
		}
		catch (final MalformedURLException e) {
         
            e.printStackTrace();
		}
		 catch (final IOException e) {
	                    
	            e.printStackTrace();
	        }
	}
	

	
	
}