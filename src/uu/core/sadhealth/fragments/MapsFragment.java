package uu.core.sadhealth.fragments;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;


import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import uu.core.sadhealth.services.UploadService;
import uu.core.sadhealth.utils.AppPreferences;
import uu.core.sadhealth.MainActivity;
import uu.core.sadhealth.R;
import uu.core.sadhealth.R.drawable;
import uu.core.sadhealth.R.id;
import uu.core.sadhealth.R.layout;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapsFragment extends Fragment
{
	private final String TAG = "LocationActivity";
	static String locationFileName = "phone_location";
	static boolean isLatestData = true;
	private SupportMapFragment fragment;
	private GoogleMap map;
	private Button bOlderData,bLatestData;
	private AppPreferences _appPrefs;
	private int periodNoInt;
	private String periodNo;
	ImageButton uploadButton,refreshButton,exitButton,serviceImageButton;
	static MenuItem uploadMenuBtn, refreshMenuBtn;	
		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
		{
			try
			{
				//setHasOptionsMenu(true);
				return inflater.inflate(R.layout.activity_map, container, false);
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
			
			return null;
		}
		
		@Override
		public void onActivityCreated(Bundle savedInstanceState)
		{
			super.onActivityCreated(savedInstanceState);
			_appPrefs = new AppPreferences(getActivity());
			periodNoInt=_appPrefs.getPeriodNo();
			//uploadButton=(ImageButton) getActivity().findViewById(R.id.UploadMenu);
			bOlderData =(Button) getView().findViewById(R.id.OlderData);
			bLatestData =(Button) getView().findViewById(R.id.LatestData);
			bOlderData.setOnClickListener(mGlobal_OnClickListener);
			bLatestData.setOnClickListener(mGlobal_OnClickListener);
			FragmentManager fm = getChildFragmentManager();
			
			fragment=(SupportMapFragment) fm.findFragmentById(R.id.map);
			if(fragment ==null)
			{
				fragment = SupportMapFragment.newInstance();
				map=fragment.getMap();
				fm.beginTransaction().replace(R.id.map, fragment).commit();
				
			}
			toMap();
			
		}
		
		public void toMap()
		{
			periodNoInt=_appPrefs.getPeriodNo();
			periodNo=String.format("%04d", periodNoInt);
			if (isLatestData==true)
			{
				locationFileName="phone_location"+periodNo;
			}
			//load list of GeoPoints
		    try {
		    	
		    	//load list of GeoPoints
			    File sdcard = Environment.getExternalStorageDirectory();
			    File file= new File (sdcard, "sadhealth/users/"+_appPrefs.getUserID() + "/"+locationFileName+".csv");		
		    	toGeo(file,map);
				
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		@SuppressLint("SimpleDateFormat")
		public static void toGeo (File filename, GoogleMap gm) throws IOException{
			//itemized = null;
			BufferedReader input = null;
			
			LatLng point;
			SimpleDateFormat sdf = new SimpleDateFormat("MM/dd-HH:mm");
			try 
			{
			    input =  new BufferedReader(new FileReader(filename));
			    String line = null;
			    
			   
			    while (( line = input.readLine()) != null)
			    {
			    	String[] data = line.split(",");
			    	Log.i("toGEO",data[3].toString());
			    	Log.i("toGEO",data[4].toString());
			    	double lat = Double.valueOf(data[3]);
			    	double lon = Double.valueOf(data[4]);
	                point = new LatLng ((int)lat,(int)lon);
	        		String currentDateandTime = sdf.format(new Date(Long.valueOf(data[2])));
	        		// create marker
	        		MarkerOptions marker = new MarkerOptions().position(new LatLng(lat, lon)).title(currentDateandTime);
	        		 
	        		// Changing marker icon
	        		marker.icon(BitmapDescriptorFactory.fromResource(R.drawable.pin));
	        		Log.i("toGEO","marker added");
	        		// adding marker
	        		gm.addMarker(marker);
	        		CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(marker.getPosition(), 12);
	        	    gm.animateCamera(cameraUpdate);
	                
	            }
			}
			catch (Exception ex)
			{
			      ex.printStackTrace();
			}
			finally 
			{
			    if(input != null)
			    {
			    	input.close();
			    }
			}
		}
		
		public void getOlderData(View v)
		{
			if ((periodNoInt <= 1) || (!isLatestData) ){
				Context context = getActivity();
				CharSequence text = "No older data exist";
				int duration = Toast.LENGTH_SHORT;

				Toast toast = Toast.makeText(context, text, duration);
				toast.show();
			}
			else{
			locationFileName = "phone_location" + String.format("%04d", (periodNoInt -1));
			isLatestData = false;
			toMap();
		    
			}

		}
		
		public void getLatestData(View v)
		{
			if (isLatestData){
				Context context = getActivity();
				CharSequence text = "No newer data exist";
				int duration = Toast.LENGTH_SHORT;

				Toast toast = Toast.makeText(context, text, duration);
				toast.show();
			}
			else{
				locationFileName ="phone_location"+ String.format("%04d", periodNoInt);
				isLatestData = true;
				toMap();
				
				
			}
			
		}
		
		//Global On click listener for all views
	    final OnClickListener mGlobal_OnClickListener = new OnClickListener() {
	        public void onClick(final View v) {
	            switch(v.getId()) {
	                case R.id.OlderData:
	                    //Inform the user the button1 has been clicked
	                	getOlderData(v);            
	                break;
	                case R.id.LatestData:
	                	getLatestData(v);             
	                break;
	            }
	        }
	    };
	    
	   
	   
	    
	    
	   

}