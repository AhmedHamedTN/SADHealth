package uu.core.sadhealth.fragments;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;


import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
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

public class PersonalStatsFragment extends ListFragment implements OnItemSelectedListener
{
	private final String TAG = "PersonalStatsFragment";
	static String unlockFileName = "unlock_no";
	static boolean isLatestData = true;
	private AppPreferences _appPrefs;
	private int periodNoInt;
	private String periodNo;
	ImageButton uploadButton,refreshButton,exitButton,serviceImageButton;
	private Spinner granularitySpinner;	
	static File sdcard = Environment.getExternalStorageDirectory();
	private int granularity=0;
	private  ArrayList<HashMap<String, String>> mylist;
	private HashMap<String, String> map;
	private SimpleAdapter mSchedule;
	private List<String> QDays =new ArrayList<String>();
		
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		try
		{
			//setHasOptionsMenu(true);
			return inflater.inflate(R.layout.activity_personalstats, container, false);
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
			uploadButton=(ImageButton) getActivity().findViewById(R.id.UploadMenu);
			
			granularitySpinner=(Spinner)getView().findViewById(R.id.PTotalsGranularity_spinner);
			granularitySpinner.setOnItemSelectedListener(this);
			    
			   
			
			
		}
		
		private void constructList()
		{
			mylist = new ArrayList<HashMap<String, String>>();
		    map= new HashMap<String, String>();
		    
		    map.put("Title","No. Times phone is in use:" );
		    try {
				map.put("Value", getNoUnlocks());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		    mylist.add(map);
		    map = new HashMap<String, String>();
		    map.put("Title", "Highest Lux Encountered:");
		    map.put("Value", getHighestLux());
		    mylist.add(map);
		    map = new HashMap<String, String>();
		    map.put("Title", "No of Times reported happy:");
		    map.put("Value", getTimesHappy());
		    mylist.add(map);
		    map = new HashMap<String, String>();
		    map.put("Title", "No of Times reported sad:");
		    map.put("Value", getTimesSad());
		    mylist.add(map);
		    map = new HashMap<String, String>();
		    map.put("Title", "No of Times uploaded:");
		    map.put("Value", getTimesUploaded());
		    mylist.add(map);
		  
		     mSchedule   = new SimpleAdapter(this.getActivity(), mylist, R.layout.row,
		                new String[] {"Title", "Value"}, new int[] { R.id.FROM_CELL, R.id.TO_CELL});
		    setListAdapter(mSchedule);
		}
		
		private String getTimesHappy() {
			switch(granularity)
			{
			case 0://Yesterday
				return "0";
				
			case 1://Today
				return "1";
				
			case 2://Last Week
				return "3";
				
			case 3://This Week
				return "2";
			
			case 4://Last Month
				return "15";
				
			case 5://This Month
				return "10";
				
			case 6://All Time
				return "60";
				
			}
			return null;
		}
		
		private String getTimesSad() {
			switch(granularity)
			{
			case 0://Yesterday
				return "1";
				
			case 1://Today
				return "0";
				
			case 2://Last Week
				return "4";
				
			case 3://This Week
				return "0";
			
			case 4://Last Month
				return "25";
				
			case 5://This Month
				return "15";
				
			case 6://All Time
				return "75";
				
			}
			return null;
		}

		private String getHighestLux() {
			switch(granularity)
			{
			case 0://Yesterday
				break;
			case 1://Today
				return String.valueOf(_appPrefs.getMaxLux());
			case 2://Last Week
				break;
			case 3://This Week
				break;
			case 4://Last Month
				break;
			case 5://This Month
				break;
			case 6://All Time
				break;
			}
			return null;
		}
		
		private String getTimesUploaded() {
			switch(granularity)
			{
			case 0://Yesterday
				break;
			case 1://Today
				return String.valueOf(_appPrefs.getUploadCounter());				
			case 2://Last Week
				break;
			case 3://This Week
				break;
			case 4://Last Month
				break;
			case 5://This Month
				break;
			case 6://All Time
				break;
			}
			return null;
		}

		private String getNoUnlocks() throws IOException {
			QDays =new ArrayList<String>();
			String value="0";
			DateTime dt=new DateTime();
			File file=null;
			LocalDate ld=new LocalDate();
			LocalDate startOfWeek;
			LocalDate endOfWeek;
			
			DateTimeFormatter sdf = DateTimeFormat.forPattern("MMdd");
			DateTimeFormatter dtf=DateTimeFormat.forPattern("MM/dd");
			int sum=0;
			
			switch(granularity)
			{
			case 0://Yesterday
				dt=dt.minusDays(1);
				file= new File (sdcard, "sadhealth/users/"+_appPrefs.getUserID()+"/unlock_no"+String.format("%02d", dt.getMonthOfYear())+String.format("%02d", dt.getDayOfMonth())+".csv");
				if(file.exists())
				{
					Log.i(TAG, "unlock file yesterday does exist");
					FileReader fr= new FileReader(file);
					BufferedReader bufferedReader = new BufferedReader(fr);
					String line = null;
			        
			        String[] data;
			        while ((line = bufferedReader.readLine()) != null) {
			       
			        data = line.split(",");
			        value=data[1];
			        }
			        bufferedReader.close();
			        return value;
				}
				
				break;
			case 1://Today
				
				Log.i(TAG, "unlock_no"+String.format("%02d", dt.getMonthOfYear())+String.format("%02d", dt.getDayOfMonth())+".csv");
				file= new File (sdcard, "sadhealth/users/"+_appPrefs.getUserID()+"/unlock_no"+String.format("%02d", dt.getMonthOfYear())+String.format("%02d", dt.getDayOfMonth())+".csv");
				if(file.exists())
				{

					FileReader fr= new FileReader(file);
					BufferedReader bufferedReader = new BufferedReader(fr);
					String line = null;
			        
			        String[] data;
			        while ((line = bufferedReader.readLine()) != null) {
			       
			        data = line.split(",");
			        Log.i(TAG, "Todays "+ data[1]);
			        value=data[1];
			        }
			        bufferedReader.close();
			        return value;
				}
				break;
			case 2://Last Week
				
				 startOfWeek = ld.minusDays(ld.dayOfWeek().get() - 1);
				endOfWeek = ld.plusDays(7 - ld.dayOfWeek().get());
				
				startOfWeek=startOfWeek.minusWeeks(1);
				endOfWeek=endOfWeek.minusWeeks(1);
				sum=0;
				for (LocalDate date = startOfWeek; date.isBefore(endOfWeek) ||date.isEqual(endOfWeek); date = date.plusDays(1))
	 			{
					file= new File (sdcard, "sadhealth/users/"+ MainActivity.USERID + "/unlock_no"+date.toString(sdf)+".csv");
						if (file.exists())
						{
							FileReader fr= new FileReader(file);
							BufferedReader bufferedReader = new BufferedReader(fr);
							String line = null;
					       
					        String[] data;
					        while ((line = bufferedReader.readLine()) != null) {
					       
					        	 data = line.split(",");
							        
							        sum+=Integer.parseInt(data[1]);
					        }
					        bufferedReader.close();
						
						}
	 			}
				value=String.valueOf(sum);
				return value;
			case 3://This Week
				
				 startOfWeek = ld.minusDays(ld.dayOfWeek().get() - 1);
				endOfWeek = ld.plusDays(7 - ld.dayOfWeek().get());
				
				sum=0;
				
				for (LocalDate date = startOfWeek; date.isBefore(endOfWeek) ||date.isEqual(endOfWeek); date = date.plusDays(1))
	 			{
					file= new File (sdcard, "sadhealth/users/"+ MainActivity.USERID + "/unlock_no"+date.toString(sdf)+".csv");
						if (file.exists())
						{
							FileReader fr= new FileReader(file);
							BufferedReader bufferedReader = new BufferedReader(fr);
							String line = null;
					       
					        String[] data;
					        while ((line = bufferedReader.readLine()) != null) {
					       
					        	 data = line.split(",");
							     
					        }
					        bufferedReader.close();
						
						}
	 			}
				value=String.valueOf(sum);
				return value;
				
			case 4://Last Month
				dt=dt.minusMonths(1);
				//Log.i(TAG," month is "+ dt.getMonthOfYear()+"  and it has "+daysOfMonth(dt.getYear(),dt.getMonthOfYear()));
				sum=0;
				getDaysinMonth(dt.getMonthOfYear(),daysOfMonth(dt.getYear(),dt.getMonthOfYear()));
				
				for (int i=0;i<QDays.size();i++)
				{
					file= new File (sdcard, "sadhealth/users/"+ MainActivity.USERID + "/unlock_no"+QDays.get(i)+".csv");
						if (file.exists())
						{
							FileReader fr= new FileReader(file);
							BufferedReader bufferedReader = new BufferedReader(fr);
							String line = null;
					        
					        String[] data;
					        while ((line = bufferedReader.readLine()) != null) {
					       
					        data = line.split(",");
					        sum+=Integer.parseInt(data[1]);
					       
					        }
					        bufferedReader.close();

						}
					}
				value=String.valueOf(sum);
				return value;
				
			case 5://This Month
				
				//Log.i(TAG," month is "+ dt.getMonthOfYear()+"  and it has "+daysOfMonth(dt.getYear(),dt.getMonthOfYear()));
				sum=0;
				getDaysinMonth(dt.getMonthOfYear(),daysOfMonth(dt.getYear(),dt.getMonthOfYear()));
				
				for (int i=0;i<QDays.size();i++)
				{
					file= new File (sdcard, "sadhealth/users/"+ MainActivity.USERID + "/unlock_no"+QDays.get(i)+".csv");
						if (file.exists())
						{
							FileReader fr= new FileReader(file);
							BufferedReader bufferedReader = new BufferedReader(fr);
							String line = null;
					        
					        String[] data;
					        while ((line = bufferedReader.readLine()) != null) {
					       
					        data = line.split(",");
					        sum+=Integer.parseInt(data[1]);
					       
					        }
					        bufferedReader.close();

						}
					}
				value=String.valueOf(sum);
				return value;
			case 6://All Time
				File f = null;
				
			      File[] paths;
			      sum=0;
			      
			         
			         // create new file
			         f = new File(sdcard,"/sadhealth/users/"+ _appPrefs.getUserID()+"/");
			         
			         // create new filename filter
			         FilenameFilter fileNameFilter = new FilenameFilter() {
			   
			            @Override
			            public boolean accept(File dir, String name) {
			            	
			              return name.contains("unlock_no");
			            }
			         };
			         // returns pathnames for files and directory
			         paths = f.listFiles(fileNameFilter);
			         
			         // for each pathname in pathname array
			         for(File path:paths)
			         {
			        	 Log.i(TAG,"filenames: "+path);
			        	 
			        	 FileReader fr= new FileReader(path);
			        	 BufferedReader bufferedReader = new BufferedReader(fr);
							String line = null;
					       
					        String[] data;
					        while ((line = bufferedReader.readLine()) != null) {
					       
					        data = line.split(",");
					        sum+=Integer.parseInt(data[1]);
					        }
					         bufferedReader.close();
			         }
			         value=String.valueOf(sum);
			         return value;
			}
			return value;
		}

		@Override
		  public void onListItemClick(ListView l, View v, int position, long id) {
		    // do something with the data

		  }
		
		
		public void initGranularity(String id)
		{
			
			if(id.contentEquals("Yesterday"))
			{
				Log.i(TAG,"Granulairity set to Yesterday");
				granularity=0;
			}
			else if(id.contentEquals("Today"))
			{
				granularity=1;
				
			}
			else if(id.contentEquals("Last Week"))
			{
				granularity=2;
				
			}
			else if(id.contentEquals("This Week"))
			{
				granularity=3;
				
			}
			else if(id.contentEquals("Last Month"))
			{
				granularity=4;
				
			}
			else if(id.contentEquals("This Month"))
			{
				granularity=5;
				
			}
			else if(id.contentEquals("All Time"))
			{
				granularity=6;
				
			}
			constructList();
			/*switch(graphSpinner.getSelectedItemPosition())
			{
			case 0:
				Log.i(TAG,"Execute Light Graph");
				
				lightGraph();
				break;
			case 1:
				activityGraph();
				break;
			case 2:
				lightSourceGraph();
				break;
			case 3:
				questionnareGraph(R.id.MoodGraph);
				break;
			case 4:
				questionnareGraph(R.id.SleepGraph);
				break;
			case 5:
				questionnareGraph(R.id.EnergyGraph);
				break;
			case 6:
				questionnareGraph(R.id.SocialGraph);
				break;
				
			}*/
			
		}
		
		
	    
	   /* public void initUpload(View v)
		{
		    final Animation animation = new AlphaAnimation(1, 0); // Change alpha from fully visible to invisible
		    animation.setDuration(500); // duration - half a second
		    animation.setInterpolator(new LinearInterpolator()); // do not alter animation rate
		    animation.setRepeatCount(Animation.INFINITE); // Repeat animation infinitely
		    animation.setRepeatMode(Animation.REVERSE); // Reverse animation at the end so the button will fade back in
		    
		    uploadButton.startAnimation(animation);
		    uploadButton.setEnabled(false);
			
			Intent uploadIntent = new Intent(getActivity(), UploadService.class);
			//uploadIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			//startActivityForResult(uploadIntent,UPLOADEXIT );
			getActivity().startService(uploadIntent);
			
		}*/
		
		  public static int daysOfMonth(int year, int month) {
	    	  DateTime dateTime = new DateTime(year, month, 14, 12,0,0);
	    	  return dateTime.dayOfMonth().getMaximumValue();
	    	}
	    
	    public  void getDaysinMonth(int month,int days)
	    {
	    	Log.i(TAG, "# of days "+days);
	    	String tmp=String.format("%02d", month);
	    	for(int i=1;i<=days;i++)
	    	{
	    		tmp=tmp+String.format("%02d",i);
	    		
	    		Log.i(TAG,"QDays: "+tmp);
	    		QDays.add(tmp);
	    		tmp=String.format("%02d", month);
	    	}
	    	
	    	
	    }
	    

	    @Override
		public void onItemSelected(AdapterView<?> parent, View view, int pos,
				long id) {
			
			 Spinner spinner = (Spinner) parent;
		     if(spinner.getId() == R.id.PTotalsGranularity_spinner)
		     {
		    	
		    	 _appPrefs.setTotalsGranularitySpinnerSelection(parent.getItemAtPosition(pos).toString());
		    	 initGranularity(parent.getItemAtPosition(pos).toString());
		     }
			
		}

		@Override
		public void onNothingSelected(AdapterView<?> parent) {
			
			
		}

}