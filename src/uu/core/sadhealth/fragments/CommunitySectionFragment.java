package uu.core.sadhealth.fragments;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.FieldPosition;
import java.text.Format;
import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.PointStyle;
import org.achartengine.model.CategorySeries;
import org.achartengine.model.TimeSeries;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.DefaultRenderer;
import org.achartengine.renderer.SimpleSeriesRenderer;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.Paint.Align;

import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;
import android.widget.Toast;



import uu.core.sadhealth.services.MainService;
import uu.core.sadhealth.services.UploadService;
import uu.core.sadhealth.utils.AppPreferences;
import uu.core.sadhealth.MainActivity;
import uu.core.sadhealth.R;



import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GraphView.GraphViewData;
import com.jjoe64.graphview.GraphViewSeries;
import com.jjoe64.graphview.LineGraphView;



public class CommunitySectionFragment extends Fragment implements  OnItemSelectedListener {


	private final String TAG = "Community";
	private AppPreferences _appPrefs;
	static File sdcard = Environment.getExternalStorageDirectory();
	static String lightFileName = "Light";
	static String personalLightMaxFileName="maxlight";
	static String activityFileName = "phone_accelerometer";
	static String questionnaireFileName="Questionnaire";
	static String phoneUnlockFileName = "unlock_no";
	static String communityActivityFileName="Activity";
	static long min,max;
	private List<Date> dates =new ArrayList<Date>();
	private List<Date> pdates =new ArrayList<Date>();
	private List<Double> y_vals =new ArrayList<Double>();
	private List<Double> py_vals =new ArrayList<Double>();
	private List<Double> converted_y =new ArrayList<Double>();
	private List<Double> pconverted_y =new ArrayList<Double>();
	private List<String> QDays =new ArrayList<String>();
	private int cALS,cNLS,pALS,pNLS;
	private int periodNoInt,desiredMonth;
	private List<Double> cmood  =new ArrayList<Double>();
	private List<Double> csleep =new ArrayList<Double>();
	private List<Double> cenergy = new ArrayList<Double>();
	private List<Double> csocial =new ArrayList<Double>();
	private List<Double> pmood  =new ArrayList<Double>();
	private List<Double> psleep =new ArrayList<Double>();
	private List<Double> penergy = new ArrayList<Double>();
	private List<Double> psocial =new ArrayList<Double>();
	private List<Date> cdatesQuestionnaire =new ArrayList<Date>();
	private List<Date> pdatesQuestionnaire =new ArrayList<Date>();

	private String[] moodConditions= new String[]{"Very happy","Happy","Down","Very Down"};
	private String[] sleepConditions=new String[]{"Very Good","Good","Bad","Very Bad"};
	private String[] energyConditions =new String[]{"Very Active","Active","Tired","Very Tired"};
	private String[] socialConditions=new String[]{"Very","Quite","a little","Not at all"};


	GraphView graphView1,graphView2;
	private Spinner graphSpinner, granularitySpinner;


	LinearLayout  wholeLayout,graphScreen;
	TextView unlockNoview, dataViewTime;
	ListView listView;
	RadioGroup graphGroup;
	static boolean isLatestData = true;
	private int selectedGraph;
	private Button bOlderData,bLatestData;
	private String periodNo;
	private String country,city;

	private GraphicalView mChart;
	ImageButton uploadButton,refreshButton,exitButton,serviceImageButton;
	ActionBar actionBar;
	static MenuItem uploadMenuBtn, refreshMenuBtn;
	private int granularity=1;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		try
		{
			//setHasOptionsMenu(true);
			return inflater.inflate(R.layout.activity_community, container, false);
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
		periodNo=String.format("%04d", periodNoInt);

		actionBar=getActivity().getActionBar();
		actionBar.getCustomView();

		selectedGraph = _appPrefs.getGraphSelectionCommunity(getActivity());

		wholeLayout =(LinearLayout) getView().findViewById(R.id.GraphScreenLayout);
		graphScreen = (LinearLayout) getView().findViewById(R.id.Graph);
		graphGroup = (RadioGroup) getView().findViewById(R.id.Graphchoices);
		unlockNoview = (TextView) getView().findViewById(R.id.unlockView);
		dataViewTime = (TextView) getView().findViewById(R.id.DataViewTime);

		graphSpinner=(Spinner) getView().findViewById(R.id.PGraphs_spinner);
		granularitySpinner=(Spinner)getView().findViewById(R.id.PGraphsGranularity_spinner);

		graphSpinner.setOnItemSelectedListener(this);
		granularitySpinner.setOnItemSelectedListener(this);
		Log.i(TAG,"started");
		Log.i(TAG, "sel-Graph: " +String.valueOf(selectedGraph));
		//initGraph(selectedGraph);

		//phoneActivity();

		//lightGraph();

	}


	public void lightGraph()
	{

		dates =new ArrayList<Date>();
		y_vals =new ArrayList<Double>();
		converted_y =new ArrayList<Double>();
		pdates =new ArrayList<Date>();
		py_vals =new ArrayList<Double>();
		pconverted_y =new ArrayList<Double>();

		// Creating a dataset to hold each series
		XYMultipleSeriesDataset dataset = new XYMultipleSeriesDataset();
		TimeSeries personalSeries = new TimeSeries("Personal Light");
		TimeSeries communitySeries = new TimeSeries("Community Light");

		String[] conditions={"Pitch Black", "Very Dark", "Dark Indoors", "Dim Indoors", "Normal Indoors", "Bright Indoors", "Dim Outdoors", "Cloudy Outdoors", "Direct Sunlight"};
		country=_appPrefs.getCountry();
		city=_appPrefs.getCity();

		// Creating XYSeriesRenderer to customize visitsSeries
		XYSeriesRenderer communityRenderer = new XYSeriesRenderer();
		communityRenderer.setColor(Color.RED);
		communityRenderer.setPointStyle(PointStyle.CIRCLE);
		communityRenderer.setFillPoints(true);
		communityRenderer.setLineWidth(2);

		XYSeriesRenderer personalRenderer = new XYSeriesRenderer();
		personalRenderer.setColor(Color.BLUE);
		personalRenderer.setPointStyle(PointStyle.CIRCLE);
		personalRenderer.setFillPoints(true);
		personalRenderer.setLineWidth(2);
		//visitsRenderer.setDisplayChartValues(true);

		// Creating a XYMultipleSeriesRenderer to customize the whole chart
		XYMultipleSeriesRenderer multiRenderer = new XYMultipleSeriesRenderer();

		multiRenderer.setChartTitle("LIGHT");
		multiRenderer.setXTitle("Date/Time");
		//multiRenderer.setYTitle("Lux");
		multiRenderer.setZoomButtonsVisible(false);
		multiRenderer.setPanEnabled(false, false);
		multiRenderer.setZoomEnabled(false,false);
		//multiRenderer.setPanEnabled(false);
		multiRenderer.setYLabelsPadding(10);
		Calendar c = Calendar.getInstance(); 



		// Adding visitsRenderer and viewsRenderer to multipleRenderer
		// Note: The order of adding dataseries to dataset and renderers to multipleRenderer
		// should be same
		multiRenderer.addSeriesRenderer(communityRenderer);
		multiRenderer.addSeriesRenderer(personalRenderer);
		multiRenderer.setYAxisMax(9);
		//multiRenderer.setYLabels(0);

		for(int i = 1; i < conditions.length; i++){
			//Double d = dataset.getSeriesAt(0).getY(i);
			//Log.i(TAG,"Y-val"+String.valueOf(d));
			multiRenderer.addYTextLabel(i, conditions[i]);
		}

		multiRenderer.setApplyBackgroundColor(false);
		multiRenderer.setMarginsColor(Color.argb(0, 255, 255, 255));
		multiRenderer.setLabelsTextSize(18);
		multiRenderer.setShowGrid(true);
		multiRenderer.setMargins(new int[] {30, 100, 10, 0});
		multiRenderer.setYLabelsAlign(Align.RIGHT);
		multiRenderer.setYLabelsAngle(-45);


		switch(granularity)
		{
		case 0: //Yesterday
			try {
				getYesterday("Light");
			} catch (IOException e) {

				e.printStackTrace();
			}

			break;
		case 1:

			break;
		case 2:
			try {
				getLastWeek("Light");
			} catch (NumberFormatException e) {

				e.printStackTrace();
			} catch (IOException e) {

				e.printStackTrace();
			}


			break;
		case 3:
			try {
				getThisWeek("Light");
			} catch (IOException e) {

				e.printStackTrace();
			} catch (ParseException e) {

				e.printStackTrace();
			}


			break;
		case 4://Last Month
			try {
				getLastMonth("Light");
			} catch (NumberFormatException e) {

				e.printStackTrace();
			} catch (IOException e) {

				e.printStackTrace();
			}

			break;
		case 5://This Month
			try {
				getThisMonth("Light");
			} catch (NumberFormatException e) {

				e.printStackTrace();
			} catch (IOException e) {

				e.printStackTrace();
			}
			break;
		case 6://All Time
			try {
				getAllTime("Light");
			} catch (NumberFormatException e) {

				e.printStackTrace();
			} catch (IOException e) {

				e.printStackTrace();
			}
			break;

		}


		for(int i=0;i<py_vals.toArray().length;i++)
		{
			if((py_vals.get(i).intValue()>=0) &&(py_vals.get(i).intValue()<=10))
			{
				pconverted_y.add(Double.valueOf(1));
			}
			else if((py_vals.get(i).intValue()>=11) &&(py_vals.get(i).intValue()<=50))
			{
				pconverted_y.add(Double.valueOf(2));
			}
			else if((py_vals.get(i).intValue()>=51) &&(py_vals.get(i).intValue()<=200))
			{
				pconverted_y.add(Double.valueOf(3));
			}
			else if((py_vals.get(i).intValue()>=201) &&(py_vals.get(i).intValue()<=400))
			{
				pconverted_y.add(Double.valueOf(4));
			}
			else if((py_vals.get(i).intValue()>=401) &&(py_vals.get(i).intValue()<=1000))
			{
				pconverted_y.add(Double.valueOf(5));
			}
			else if((py_vals.get(i).intValue()>=1001) &&(py_vals.get(i).intValue()<=5000))
			{
				pconverted_y.add(Double.valueOf(6));
			}
			else if((py_vals.get(i).intValue()>=5001) &&(py_vals.get(i).intValue()<=10000))
			{
				pconverted_y.add(Double.valueOf(7));
			}
			else if ((py_vals.get(i).intValue()>=10001) &&(py_vals.get(i).intValue()<=30000))
			{
				pconverted_y.add(Double.valueOf(8));
			}
			else if ((py_vals.get(i).intValue()>=30001) &&(py_vals.get(i).intValue()<=100000))
			{
				pconverted_y.add(Double.valueOf(9));
			}
			else
			{
				Log.i(TAG,"error here! "+i);
				pconverted_y.add(Double.valueOf(100));
			}
		}

		for(int i=0;i<pdates.size();i++)
		{
			personalSeries.add(pdates.get(i),pconverted_y.get(i));
		}



		for (int i=0;i<y_vals.toArray().length;i++)
		{

			if((y_vals.get(i).intValue()>=0) &&(y_vals.get(i).intValue()<=10))
			{
				converted_y.add(Double.valueOf(1));
			}
			else if((y_vals.get(i).intValue()>=11) &&(y_vals.get(i).intValue()<=50))
			{
				converted_y.add(Double.valueOf(2));
			}
			else if((y_vals.get(i).intValue()>=51) &&(y_vals.get(i).intValue()<=200))
			{
				converted_y.add(Double.valueOf(3));
			}
			else if((y_vals.get(i).intValue()>=201) &&(y_vals.get(i).intValue()<=400))
			{
				converted_y.add(Double.valueOf(4));
			}
			else if((y_vals.get(i).intValue()>=401) &&(y_vals.get(i).intValue()<=1000))
			{
				converted_y.add(Double.valueOf(5));
			}
			else if((y_vals.get(i).intValue()>=1001) &&(y_vals.get(i).intValue()<=5000))
			{
				converted_y.add(Double.valueOf(6));
			}
			else if((y_vals.get(i).intValue()>=5001) &&(y_vals.get(i).intValue()<=10000))
			{
				converted_y.add(Double.valueOf(7));
			}
			else if ((y_vals.get(i).intValue()>=10001) &&(y_vals.get(i).intValue()<=30000))
			{
				converted_y.add(Double.valueOf(8));
			}
			else if ((y_vals.get(i).intValue()>=30001) &&(y_vals.get(i).intValue()<=100000))
			{
				converted_y.add(Double.valueOf(9));
			}
			else
			{
				Log.i(TAG,"error here! "+i);
				converted_y.add(Double.valueOf(100));
			}
		}
		//Log.i(TAG, "convertedY-size" +String.valueOf(converted_y.size()));
		for(int i=0;i<dates.size();i++)
		{
			communitySeries.add(dates.get(i),converted_y.get(i));
		}





		// Adding Visits Series to the dataset
		dataset.addSeries(communitySeries);
		dataset.addSeries(personalSeries);



		//multiRenderer.setXAxisMin(min);
		multiRenderer.setXAxisMax(c.getTime().getTime());

		// mChart=(GraphicalView)ChartFactory.getLineChartView(getActivity(), dataset, mRenderer);
		mChart=(GraphicalView)ChartFactory.getTimeChartView(getActivity(), dataset, multiRenderer, "MM/dd");
		graphScreen.addView(mChart);



	}





	public void activityGraph(){

		String[] conditions={"not active","active","very active"};
		// Creating TimeSeries for Light
        TimeSeries visitsSeries = new TimeSeries("Activity");
     // Creating a dataset to hold each series
        XYMultipleSeriesDataset dataset = new XYMultipleSeriesDataset();
		
		DateTime now=new DateTime();
		dates =new ArrayList<Date>();
		y_vals =new ArrayList<Double>();
		converted_y =new ArrayList<Double>();
		pdates =new ArrayList<Date>();
		py_vals =new ArrayList<Double>();
		pconverted_y =new ArrayList<Double>();
		periodNoInt=_appPrefs.getPeriodNo();
		periodNo=String.format("%04d", periodNoInt);
		
		
		TimeSeries personalSeries = new TimeSeries("Personal Activity");
		TimeSeries communitySeries = new TimeSeries("Community Activity");
		   
		// Creating XYSeriesRenderer to customize visitsSeries
		XYSeriesRenderer communityRenderer = new XYSeriesRenderer();
		communityRenderer.setColor(Color.RED);
		communityRenderer.setPointStyle(PointStyle.CIRCLE);
		communityRenderer.setFillPoints(true);
		communityRenderer.setLineWidth(2);

		XYSeriesRenderer personalRenderer = new XYSeriesRenderer();
		personalRenderer.setColor(Color.BLUE);
		personalRenderer.setPointStyle(PointStyle.CIRCLE);
		personalRenderer.setFillPoints(true);
		personalRenderer.setLineWidth(2);
				
	     // Creating a XYMultipleSeriesRenderer to customize the whole chart
	     XYMultipleSeriesRenderer multiRenderer = new XYMultipleSeriesRenderer();
	 
	     multiRenderer.setChartTitle("PHYSICAL ACTIVITY");
	     multiRenderer.setXTitle("Date/Time");
	     multiRenderer.setYTitle("Magnitude");
	        
	     multiRenderer.setZoomButtonsVisible(false);
	     multiRenderer.setPanEnabled(false, false);
	     multiRenderer.setZoomEnabled(false,false);
	     multiRenderer.setYLabelsPadding(10);
	     Calendar c = Calendar.getInstance(); 
	     multiRenderer.setXAxisMax(c.getTime().getTime());
	 
	     multiRenderer.addSeriesRenderer(communityRenderer);
	     multiRenderer.addSeriesRenderer(personalRenderer);
	        
	 
	      multiRenderer.setApplyBackgroundColor(false);
	      multiRenderer.setMarginsColor(Color.argb(0, 255, 255, 255));
	      multiRenderer.setLabelsTextSize(18);
	      multiRenderer.setShowGrid(true);
	      multiRenderer.setMargins(new int[] {30, 100, 10, 0});
	      multiRenderer.setYLabelsAlign(Align.RIGHT);
	      multiRenderer.setYLabelsAngle(-45);
	    
		
		switch(granularity)
		{
		case 0: //Yesterday
			/*activityRecFileName = "phone_accelerometer" + String.format("%04d", (periodNoInt -1));
			try {
		    	
		    	//load list of GeoPoints
			    File sdcard = Environment.getExternalStorageDirectory();
			   // Log.i(TAG, "Light-filelatest "+lightFileName );
			    File file= new File (sdcard, "sadhealth/users/"+ _appPrefs.getUserID()+ "/"+activityRecFileName+".csv");		
			    getData(file);
			    
			    	
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			
	            
	        	
	        
			
			for (int i=0;i<y_vals.toArray().length;i++)
	        {
	        		Log.i(TAG,"y-val " +y_vals.get(i).floatValue()+" "+i);
	        		if(y_vals.get(i).floatValue()==0)
	        		{
	        			multiRenderer.addYTextLabel(Double.valueOf(y_vals.get(i).floatValue()), "not active");
	        		}
	        	 if((y_vals.get(i).floatValue()>0) &&(y_vals.get(i).floatValue()<=0.2))
	             {
	             	//converted_y.add(Double.valueOf(1));
	             	multiRenderer.addYTextLabel(0.2, "somewhat active");
		        	
	             }
	             else if((y_vals.get(i).floatValue()>=0.21) &&(y_vals.get(i).floatValue()<=0.39))
	             {
	             	//converted_y.add(Double.valueOf(2));
	            	 multiRenderer.addYTextLabel(0.4, "active");
	             }
	             else if((y_vals.get(i).floatValue()>=0.4) &&(y_vals.get(i).floatValue()<=0.5))
	             {
	             	//converted_y.add(Double.valueOf(3));
	            	 multiRenderer.addYTextLabel(0.5, "very active");
	             }
	             else if((y_vals.get(i).floatValue()>=0.51) &&(y_vals.get(i).floatValue()<=0.6))
	             {
	             	//converted_y.add(Double.valueOf(4));
	            	 multiRenderer.addYTextLabel(0.6, "very active");
	             }
	             else if((y_vals.get(i).floatValue()>=0.1) &&(y_vals.get(i).floatValue()<=0.14))
	             {
	             	//converted_y.add(Double.valueOf(5));
	             }
	             else if((y_vals.get(i).floatValue()>=0.15) &&(y_vals.get(i).floatValue()<=0.16))
	             {
	             	//converted_y.add(Double.valueOf(6));
	             }
	             else if((y_vals.get(i).floatValue()>=0.17) &&(y_vals.get(i).floatValue()<=0.18))
	             {
	             	//converted_y.add(Double.valueOf(7));
	             }
	             else if ((y_vals.get(i).floatValue()>=0.19) &&(y_vals.get(i).floatValue()<=0.2))
	             {
	             	//converted_y.add(Double.valueOf(8));
	             }
	             else if ((y_vals.get(i).floatValue()>=0.2) &&(y_vals.get(i).floatValue()<=1.5))
	             {
	             	//converted_y.add(Double.valueOf(9));
	             }
	             else
	             {
	            	 Log.i(TAG,"error here! "+i);
	            	 //converted_y.add(Double.valueOf(10));
	            	 //multiRenderer.addYTextLabel(Double.valueOf(y_vals.get(i).floatValue()), "Very Active");
	             }
	        }
	        //Log.i(TAG, "convertedY-size" +String.valueOf(converted_y.size()));
	        for(int i=0;i<dates.size();i++)
			{
				visitsSeries.add(new Date(dates.get(i)),y_vals.get(i));
			}
	        
	     // Adding Visits Series to the dataset
	        dataset.addSeries(visitsSeries);
	       
			
	        mChart=(GraphicalView)ChartFactory.getTimeChartView(getActivity(), dataset, multiRenderer, "HH:mm");*/
			break;
		case 1: //Today
			
			/*activityRecFileName="phone_accelerometer"+periodNo;
			try {
		    	
		    	//load list of GeoPoints
			    File sdcard = Environment.getExternalStorageDirectory();
			   // Log.i(TAG, "Light-filelatest "+lightFileName );
			    File file= new File (sdcard, "sadhealth/users/"+ _appPrefs.getUserID()+ "/"+activityRecFileName+".csv");		
			    getData(file);
			    
			    	
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			
	            
	        	
	        
			
			for (int i=0;i<y_vals.toArray().length;i++)
	        {
	        		Log.i(TAG,"y-val " +y_vals.get(i).floatValue()+" "+i);
	        		if(y_vals.get(i).floatValue()==0)
	        		{
	        			multiRenderer.addYTextLabel(Double.valueOf(y_vals.get(i).floatValue()), "not active");
	        		}
	        	 if((y_vals.get(i).floatValue()>0) &&(y_vals.get(i).floatValue()<=0.2))
	             {
	             	//converted_y.add(Double.valueOf(1));
	             	multiRenderer.addYTextLabel(0.2, "somewhat active");
		        	
	             }
	             else if((y_vals.get(i).floatValue()>=0.21) &&(y_vals.get(i).floatValue()<=0.3))
	             {
	             	//converted_y.add(Double.valueOf(2));
	            	 multiRenderer.addYTextLabel(0.4, "active");
	             }
	             else if((y_vals.get(i).floatValue()>=0.3) &&(y_vals.get(i).floatValue()<=0.39))
	             {
	             	//converted_y.add(Double.valueOf(2));
	            	 multiRenderer.addYTextLabel(0.3, "active");
	             }
	             else if((y_vals.get(i).floatValue()>=0.4) &&(y_vals.get(i).floatValue()<=0.5))
	             {
	             	//converted_y.add(Double.valueOf(3));
	            	 multiRenderer.addYTextLabel(0.5, "very active");
	             }
	             else if((y_vals.get(i).floatValue()>=0.51) &&(y_vals.get(i).floatValue()<=0.6))
	             {
	             	//converted_y.add(Double.valueOf(4));
	            	 multiRenderer.addYTextLabel(0.6, "very active");
	             }
	             else if((y_vals.get(i).floatValue()>=0.1) &&(y_vals.get(i).floatValue()<=0.14))
	             {
	             	//converted_y.add(Double.valueOf(5));
	             }
	             else if((y_vals.get(i).floatValue()>=0.15) &&(y_vals.get(i).floatValue()<=0.16))
	             {
	             	//converted_y.add(Double.valueOf(6));
	             }
	             else if((y_vals.get(i).floatValue()>=0.17) &&(y_vals.get(i).floatValue()<=0.18))
	             {
	             	//converted_y.add(Double.valueOf(7));
	             }
	             else if ((y_vals.get(i).floatValue()>=0.19) &&(y_vals.get(i).floatValue()<=0.2))
	             {
	             	//converted_y.add(Double.valueOf(8));
	             }
	             else if ((y_vals.get(i).floatValue()>=0.2) &&(y_vals.get(i).floatValue()<=1.5))
	             {
	             	//converted_y.add(Double.valueOf(9));
	             }
	             else
	             {
	            	 Log.i(TAG,"error here! "+i);
	            	 //converted_y.add(Double.valueOf(10));
	            	 //multiRenderer.addYTextLabel(Double.valueOf(y_vals.get(i).floatValue()), "Very Active");
	             }
	        }
	        //Log.i(TAG, "convertedY-size" +String.valueOf(converted_y.size()));
	        for(int i=0;i<dates.size();i++)
			{
				visitsSeries.add(new Date(dates.get(i)),y_vals.get(i));
			}
	        
	     // Adding Visits Series to the dataset
	        dataset.addSeries(visitsSeries);
	       
			
	        mChart=(GraphicalView)ChartFactory.getTimeChartView(getActivity(), dataset, multiRenderer, "HH:mm");*/
			
			break;
		case 2://Last Week
			break;
		case 3://This Week
			break;
		case 4://Last Month
			break;
		case 5://This Month
			break;
		case 6://All Time
			try {
				getAllTime("Activity");
			} catch (NumberFormatException e) {

				e.printStackTrace();
			} catch (IOException e) {

				e.printStackTrace();
			}
			

		
	        break;
		}
		
		
		for (int i=0;i<y_vals.toArray().length;i++)
        {
        		Log.i(TAG,"y-val " +y_vals.get(i).floatValue()+" "+i);
        		if(y_vals.get(i).floatValue()==0)
        		{
        			multiRenderer.addYTextLabel(Double.valueOf(y_vals.get(i).floatValue()), "not active");
        		}
        	 if((y_vals.get(i).floatValue()>0) &&(y_vals.get(i).floatValue()<=0.2))
             {
             	//converted_y.add(Double.valueOf(1));
             	multiRenderer.addYTextLabel(0.2, "somewhat active");
	        	
             }
             else if((y_vals.get(i).floatValue()>=0.21) &&(y_vals.get(i).floatValue()<=0.3))
             {
             	//converted_y.add(Double.valueOf(2));
            	 multiRenderer.addYTextLabel(0.4, "active");
             }
             else if((y_vals.get(i).floatValue()>=0.3) &&(y_vals.get(i).floatValue()<=0.39))
             {
             	//converted_y.add(Double.valueOf(2));
            	 multiRenderer.addYTextLabel(0.3, "active");
             }
             else if((y_vals.get(i).floatValue()>=0.4) &&(y_vals.get(i).floatValue()<=0.5))
             {
             	//converted_y.add(Double.valueOf(3));
            	 multiRenderer.addYTextLabel(0.5, "very active");
             }
             else if((y_vals.get(i).floatValue()>=0.51) &&(y_vals.get(i).floatValue()<=0.6))
             {
             	//converted_y.add(Double.valueOf(4));
            	 multiRenderer.addYTextLabel(0.6, "very active");
             }
             else if((y_vals.get(i).floatValue()>=0.1) &&(y_vals.get(i).floatValue()<=0.14))
             {
             	//converted_y.add(Double.valueOf(5));
             }
             else if((y_vals.get(i).floatValue()>=0.15) &&(y_vals.get(i).floatValue()<=0.16))
             {
             	//converted_y.add(Double.valueOf(6));
             }
             else if((y_vals.get(i).floatValue()>=0.17) &&(y_vals.get(i).floatValue()<=0.18))
             {
             	//converted_y.add(Double.valueOf(7));
             }
             else if ((y_vals.get(i).floatValue()>=0.19) &&(y_vals.get(i).floatValue()<=0.2))
             {
             	//converted_y.add(Double.valueOf(8));
             }
             else if ((y_vals.get(i).floatValue()>=0.2) &&(y_vals.get(i).floatValue()<=1.5))
             {
             	//converted_y.add(Double.valueOf(9));
             }
             else
             {
            	 Log.i(TAG,"error here! "+i);
            	 //converted_y.add(Double.valueOf(10));
            	 //multiRenderer.addYTextLabel(Double.valueOf(y_vals.get(i).floatValue()), "Very Active");
             }
        }
        //Log.i(TAG, "convertedY-size" +String.valueOf(converted_y.size()));
		for(int i=0;i<dates.size();i++)
		{
			communitySeries.add(dates.get(i),y_vals.get(i));
		}

        
     // Adding Visits Series to the dataset
        dataset.addSeries(communitySeries);
       
		
        mChart=(GraphicalView)ChartFactory.getTimeChartView(getActivity(), dataset, multiRenderer, "MM/dd");
		
	    graphScreen.addView(mChart);
    


	}

	public void lightSourceGraph()
	{
		// Pie Chart Section Names
		String[] pieChartNames = new String[] {"Community Artificial Light", "Community Natural Light", "Personal Artificial Light", "Personal Natural Light"};
		// Color of each Pie Chart Sections
		int[] colors = { Color.BLUE, Color.GREEN, Color.RED, Color.YELLOW};

		switch(granularity)
		{
		case 0: //Yesterday
			try {
				getYesterday("Light Distribution");	
			} catch (IOException e) {
				e.printStackTrace();
			}
			break;
		case 1: //Today
			try {
				getToday("Light Distribution");	
			} catch (IOException e) {
				e.printStackTrace();
			}

			break;
		case 2://Last Week
			try {
				getLastWeek("Light Distribution");	
			} catch (IOException e) {
				e.printStackTrace();
			}
			break;
		case 3://This Week

			try {
				getThisWeek("Light Distribution");
			} catch (IOException e1) {

				e1.printStackTrace();
			} catch (ParseException e1) {

				e1.printStackTrace();
			}

			break;
		case 4://Last Month
			try {
				getLastMonth("Light Distribution");	
			} catch (IOException e) {
				e.printStackTrace();
			}
			break;
		case 5://This Month
			try {
				getThisMonth("Light Distribution");	
			} catch (IOException e) {
				e.printStackTrace();
			}
			break;
		case 6://All Time
			try {
				getAllTime("Light Distribution");	
			} catch (IOException e) {
				e.printStackTrace();
			}
			break;
		}


		// Pie Chart Section Value
		double[] distribution = { cALS, cNLS, pALS, pNLS } ;



		// Instantiating CategorySeries to plot Pie Chart
		CategorySeries distributionSeries = new CategorySeries("Artifical Light vs Natural Light");
		for(int i=0 ;i < distribution.length;i++){
			// Adding a slice with its values and name to the Pie Chart
			distributionSeries.add(pieChartNames[i], distribution[i]);
		}

		// Instantiating a renderer for the Pie Chart
		DefaultRenderer multiRenderer  = new DefaultRenderer();
		for(int i = 0 ;i<distribution.length;i++){
			SimpleSeriesRenderer seriesRenderer = new SimpleSeriesRenderer();
			seriesRenderer.setColor(colors[i]);
			seriesRenderer.setDisplayChartValues(true);
			// Adding a renderer for a slice
			multiRenderer.addSeriesRenderer(seriesRenderer);
		}

		multiRenderer.setChartTitle("Light Sources(in hours) ");
		multiRenderer.setDisplayValues(true);
		multiRenderer.setLabelsTextSize(18);
		multiRenderer.setChartTitleTextSize(20);
		multiRenderer.setZoomButtonsVisible(false);
		multiRenderer.setPanEnabled(false);
		multiRenderer.setZoomEnabled(false);

		// Creating an intent to plot bar chart using dataset and multipleRenderer
		mChart= ChartFactory.getPieChartView(getActivity(), distributionSeries , multiRenderer);
		graphScreen.addView(mChart);



	}


	public void questionnareGraph(int id)
	{
		
		country=_appPrefs.getCountry();
		city=_appPrefs.getCity();
		
		// Creating a dataset to hold each series
        XYMultipleSeriesDataset dataset = new XYMultipleSeriesDataset();
        // Creating a XYMultipleSeriesRenderer to customize the whole chart
        XYMultipleSeriesRenderer multiRenderer = new XYMultipleSeriesRenderer();
        
        multiRenderer.setXTitle("Date");
        
        multiRenderer.setYAxisMin(-10);
        multiRenderer.setYAxisMax(0);
        multiRenderer.setZoomButtonsVisible(false);
        multiRenderer.setPanEnabled(false, false);
        multiRenderer.setZoomEnabled(false,false);
        
        multiRenderer.setLabelsTextSize(18);
	    multiRenderer.setApplyBackgroundColor(false);
	    multiRenderer.setMarginsColor(Color.argb(0, 255, 255, 255));
	    multiRenderer.setPanEnabled(false, false);    // will fix the chart position
	    multiRenderer.setMargins(new int[] {30, 70, 10, 0});
        multiRenderer.setYLabelsAlign(Align.RIGHT);
        multiRenderer.setYLabelsAngle(-45);


		cdatesQuestionnaire =new ArrayList<Date>();
		pdatesQuestionnaire =new ArrayList<Date>();
		QDays =new ArrayList<String>();
		cmood  =new ArrayList<Double>();
		csleep =new ArrayList<Double>();
		cenergy = new ArrayList<Double>();
		csocial =new ArrayList<Double>();
		pmood  =new ArrayList<Double>();
		psleep =new ArrayList<Double>();
		penergy = new ArrayList<Double>();
		psocial =new ArrayList<Double>();
	
		switch (id)
	    {
	    case R.id.MoodGraph:
	    	multiRenderer.setChartTitle("MOOD");
	    	//Creating an  XYSeries for Mood
	        TimeSeries cmoodSeries = new TimeSeries("Community Mood");
	        TimeSeries pmoodSeries = new TimeSeries("Personal Mood");
	        switch(granularity)
			{
			case 0: //Yesterday
				try {
				    getYesterday("Mood");	
				} catch (IOException e) {
					e.printStackTrace();
				}
				break;
			case 1: //Today
				try {
				    getToday("Mood");	
				} catch (IOException e) {
					e.printStackTrace();
				}
				break;
			case 2://Last Week
				try {
					getLastWeek("Mood");
				} catch (NumberFormatException e2) {
					
					e2.printStackTrace();
				} catch (IOException e2) {
					
					e2.printStackTrace();
				}
				break;
			case 3://This Week
				try {
					getThisWeek("Mood");
				} catch (IOException e1) {
					
					e1.printStackTrace();
				} catch (ParseException e1) {
					
					e1.printStackTrace();
				}
				
				break;
			case 4://Last Month
				try {
				    getLastMonth("Mood");	
				} catch (IOException e) {
					e.printStackTrace();
				}
				break;
			case 5://This Month
				try {
				    getThisMonth("Mood");	
				} catch (IOException e) {
					e.printStackTrace();
				}
				break;
			case 6://All Time
				try {
				    getAllTime("Mood");	
				} catch (IOException e) {
					e.printStackTrace();
				}
				break;
			}
				
				 
			        // Adding data to all Series
			        for(int i=0;i<cdatesQuestionnaire.toArray().length;i++){
			            cmoodSeries.add(cdatesQuestionnaire.get(i), cmood.get(i));
			        }
			        
			        for(int i=0;i<pdatesQuestionnaire.toArray().length;i++){
			            pmoodSeries.add(pdatesQuestionnaire.get(i), pmood.get(i));
			        }
			     // Adding Income Series to the dataset
			        dataset.addSeries(cmoodSeries);
			        dataset.addSeries(pmoodSeries);
			        
			     // Creating XYSeriesRenderer to customize incomeSeries
			        XYSeriesRenderer cmoodRenderer = new XYSeriesRenderer();
			        cmoodRenderer.setColor(Color.BLUE);
			        cmoodRenderer.setPointStyle(PointStyle.CIRCLE);
			        cmoodRenderer.setFillPoints(true);
			        cmoodRenderer.setLineWidth(2);
			        
			     // Creating XYSeriesRenderer to customize incomeSeries
			        XYSeriesRenderer pmoodRenderer = new XYSeriesRenderer();
			        pmoodRenderer.setColor(Color.WHITE);
			        pmoodRenderer.setPointStyle(PointStyle.CIRCLE);
			        pmoodRenderer.setFillPoints(true);
			        pmoodRenderer.setLineWidth(2);
				
				
				for(int i=0;i<11;i++){
		        	if (i==0)
		        	{
		        		multiRenderer.addYTextLabel(-i, moodConditions[0]);
		        	}
		        	else if(i==4)
		        	{
		        		multiRenderer.addYTextLabel(-i, moodConditions[1]);
		        	}
		        	else if(i==7)
		        	{
		        		multiRenderer.addYTextLabel(-i, moodConditions[2]);
		        	}
		        	else if(i==10)
		        	{
		        		multiRenderer.addYTextLabel(-i, moodConditions[3]);
		        	}
		        	else
		        	{
		        		multiRenderer.addYTextLabel(-i, "");
		        	}
		        }
		        
		        
		        multiRenderer.addSeriesRenderer(cmoodRenderer);
		        multiRenderer.addSeriesRenderer(pmoodRenderer);
		        // Creating a Line Chart
		        mChart = ChartFactory.getTimeChartView(getActivity(), dataset, multiRenderer, "MM/dd");
				
				break;
			
	    case R.id.SleepGraph:
	    	multiRenderer.setChartTitle("SLEEP");
	    	//Creating an  XYSeries for Sleep
	        TimeSeries csleepSeries = new TimeSeries("Community Sleep");
	        TimeSeries psleepSeries = new TimeSeries("Personal Sleep");
	        
	        switch(granularity)
			{
			case 0: //Yesterday
				try {
				    getYesterday("Sleep");	
				} catch (IOException e) {
					e.printStackTrace();
				}
				break;
			case 1: //Today
				try {
				    getToday("Sleep");	
				} catch (IOException e) {
					e.printStackTrace();
				}
				break;
			case 2://Last Week
				try {
					getLastWeek("Sleep");
				} catch (NumberFormatException e2) {
					
					e2.printStackTrace();
				} catch (IOException e2) {
					
					e2.printStackTrace();
				}
				break;
			case 3://This Week
				try {
					getThisWeek("Sleep");
				} catch (IOException e1) {
					
					e1.printStackTrace();
				} catch (ParseException e1) {
					
					e1.printStackTrace();
				}
				
				break;
			case 4://Last Month
				try {
				    getLastMonth("Sleep");	
				} catch (IOException e) {
					e.printStackTrace();
				}
				break;
			case 5://This Month
				try {
				    getThisMonth("Sleep");	
				} catch (IOException e) {
					e.printStackTrace();
				}
				break;
			case 6://All Time
				try {
				    getAllTime("Sleep");	
				} catch (IOException e) {
					e.printStackTrace();
				}
				break;
			}
	        
	        // Adding data to all Series
	        for(int i=0;i<cdatesQuestionnaire.toArray().length;i++){
	            csleepSeries.add(cdatesQuestionnaire.get(i),csleep.get(i));
	        }
	        
	        for(int i=0;i<pdatesQuestionnaire.toArray().length;i++){
	            psleepSeries.add(pdatesQuestionnaire.get(i),psleep.get(i));
	        }
	        
	     // Adding Expense Series to dataset
	        dataset.addSeries(csleepSeries);
	        dataset.addSeries(psleepSeries);
	        
	        // Creating XYSeriesRenderer to customize expenseSeries
	        XYSeriesRenderer csleepRenderer = new XYSeriesRenderer();
	        csleepRenderer.setColor(Color.YELLOW);
	        csleepRenderer.setPointStyle(PointStyle.CIRCLE);
	        csleepRenderer.setFillPoints(true);
	        csleepRenderer.setLineWidth(2);
	        
	     // Creating XYSeriesRenderer to customize expenseSeries
	        XYSeriesRenderer psleepRenderer = new XYSeriesRenderer();
	        psleepRenderer.setColor(Color.WHITE);
	        psleepRenderer.setPointStyle(PointStyle.CIRCLE);
	        psleepRenderer.setFillPoints(true);
	        psleepRenderer.setLineWidth(2);
	        
	        
	        
	        for(int i=0;i<11;i++){
	        	if (i==0)
	        	{
	        		multiRenderer.addYTextLabel(-i, sleepConditions[0]);
	        	}
	        	else if(i==4)
	        	{
	        		multiRenderer.addYTextLabel(-i, sleepConditions[1]);
	        	}
	        	else if(i==7)
	        	{
	        		multiRenderer.addYTextLabel(-i, sleepConditions[2]);
	        	}
	        	else if(i==10)
	        	{
	        		multiRenderer.addYTextLabel(-i, sleepConditions[3]);
	        	}
	        	else
	        	{
	        		multiRenderer.addYTextLabel(-i, "");
	        	}
	        }
	        multiRenderer.addSeriesRenderer(csleepRenderer);
	        multiRenderer.addSeriesRenderer(psleepRenderer);
	        // Creating a Line Chart
	        mChart = ChartFactory.getTimeChartView(getActivity(), dataset, multiRenderer, "MM/dd");
	        
	    	break;
	    case R.id.EnergyGraph:
	    	multiRenderer.setChartTitle("ENERGY");
	    	//Creating an XYSeries for Energy
	        TimeSeries cenergySeries =new TimeSeries("Community Energy");
	        TimeSeries penergySeries =new TimeSeries("Personal Energy");
	        
	        switch(granularity)
			{
			case 0: //Yesterday
				try {
				    getYesterday("Energy");	
				} catch (IOException e) {
					e.printStackTrace();
				}
				break;
			case 1: //Today
				try {
				    getToday("Energy");	
				} catch (IOException e) {
					e.printStackTrace();
				}
				break;
			case 2://Last Week
				try {
					getLastWeek("Energy");
				} catch (NumberFormatException e2) {
					
					e2.printStackTrace();
				} catch (IOException e2) {
					
					e2.printStackTrace();
				}
				break;
			case 3://This Week
				try {
					getThisWeek("Energy");
				} catch (IOException e1) {
					
					e1.printStackTrace();
				} catch (ParseException e1) {
					
					e1.printStackTrace();
				}
				
				break;
			case 4://Last Month
				try {
				    getLastMonth("Energy");	
				} catch (IOException e) {
					e.printStackTrace();
				}
				break;
			case 5://This Month
				try {
				    getThisMonth("Energy");	
				} catch (IOException e) {
					e.printStackTrace();
				}
				break;
			case 6://All Time
				try {
				    getAllTime("Energy");	
				} catch (IOException e) {
					e.printStackTrace();
				}
				break;
			}
	        
	        // Adding data to all Series
	        for(int i=0;i<cdatesQuestionnaire.toArray().length;i++){
	            cenergySeries.add(cdatesQuestionnaire.get(i), cenergy.get(i));
	        }
	        
	        for(int i=0;i<pdatesQuestionnaire.toArray().length;i++){
	            penergySeries.add(pdatesQuestionnaire.get(i), penergy.get(i));
	        }
	        
	     // Adding the energy Series
	        dataset.addSeries(cenergySeries);
	        dataset.addSeries(penergySeries);
	        
	     // Creating XYSeriesRenderer to customize incomeSeries
	        XYSeriesRenderer cenergyRenderer = new XYSeriesRenderer();
	        cenergyRenderer.setColor(Color.RED);
	        cenergyRenderer.setPointStyle(PointStyle.CIRCLE);
	        cenergyRenderer.setFillPoints(true);
	        cenergyRenderer.setLineWidth(2);
	        
	        XYSeriesRenderer penergyRenderer = new XYSeriesRenderer();
	        penergyRenderer.setColor(Color.WHITE);
	        penergyRenderer.setPointStyle(PointStyle.CIRCLE);
	        penergyRenderer.setFillPoints(true);
	        penergyRenderer.setLineWidth(2);
	        
	        
	        
	        
	        for(int i=0;i<11;i++){
	        	if (i==0)
	        	{
	        		multiRenderer.addYTextLabel(-i, energyConditions[0]);
	        	}
	        	else if(i==4)
	        	{
	        		multiRenderer.addYTextLabel(-i, energyConditions[1]);
	        	}
	        	else if(i==7)
	        	{
	        		multiRenderer.addYTextLabel(-i, energyConditions[2]);
	        	}
	        	else if(i==10)
	        	{
	        		multiRenderer.addYTextLabel(-i, energyConditions[3]);
	        	}
	        	else
	        	{
	        		multiRenderer.addYTextLabel(-i, "");
	        	}
	        }
	        
	        multiRenderer.addSeriesRenderer(cenergyRenderer);
	        multiRenderer.addSeriesRenderer(penergyRenderer);
	        
	        // Creating a Line Chart
	        mChart = ChartFactory.getTimeChartView(getActivity(), dataset, multiRenderer, "MM/dd");
	    	break;
	    case R.id.SocialGraph:
	        multiRenderer.setChartTitle("SOCIAL");
	    	//Creating an XYSeries for Social
	        TimeSeries csocialSeries =new TimeSeries("Community Social");
	        TimeSeries psocialSeries =new TimeSeries("Personal Social");
	        
	     // Creating XYSeriesRenderer to customize expenseSeries
	        XYSeriesRenderer csocialRenderer = new XYSeriesRenderer();
	        csocialRenderer.setColor(Color.BLUE);
	        csocialRenderer.setPointStyle(PointStyle.CIRCLE);
	        csocialRenderer.setFillPoints(true);
	        csocialRenderer.setLineWidth(2);
	        
	        XYSeriesRenderer psocialRenderer = new XYSeriesRenderer();
	        psocialRenderer.setColor(Color.WHITE);
	        psocialRenderer.setPointStyle(PointStyle.CIRCLE);
	        psocialRenderer.setFillPoints(true);
	        psocialRenderer.setLineWidth(2);
	        
	        
	        switch(granularity)
			{
			case 0: //Yesterday
				try {
				    getYesterday("Social");	
				} catch (IOException e) {
					e.printStackTrace();
				}
				break;
			case 1: //Today
				try {
				    getToday("Social");	
				} catch (IOException e) {
					e.printStackTrace();
				}
				break;
			case 2://Last Week
				try {
					getLastWeek("Social");
				} catch (NumberFormatException e2) {
					
					e2.printStackTrace();
				} catch (IOException e2) {
					
					e2.printStackTrace();
				}
				break;
			case 3://This Week
				try {
					getThisWeek("Social");
				} catch (IOException e1) {
					
					e1.printStackTrace();
				} catch (ParseException e1) {
					
					e1.printStackTrace();
				}
				
				break;
			case 4://Last Month
				try {
				    getLastMonth("Social");	
				} catch (IOException e) {
					e.printStackTrace();
				}
				break;
			case 5://This Month
				try {
				    getThisMonth("Social");	
				} catch (IOException e) {
					e.printStackTrace();
				}
				break;
			case 6://All Time
				try {
				    getAllTime("Social");	
				} catch (IOException e) {
					e.printStackTrace();
				}
				break;
			}
	        
	        // Adding data to all Series
	        for(int i=0;i<cdatesQuestionnaire.toArray().length;i++){
	            csocialSeries.add(cdatesQuestionnaire.get(i),csocial.get(i));
	        }
	        
	     // Adding data to all Series
	        for(int i=0;i<pdatesQuestionnaire.toArray().length;i++){
	            psocialSeries.add(pdatesQuestionnaire.get(i),psocial.get(i));
	        }
	        
	        //Adding the social Series
	        dataset.addSeries(csocialSeries);
	        dataset.addSeries(psocialSeries);
	        
	        for(int i=0;i<11;i++){
	        	if (i==0)
	        	{
	        		multiRenderer.addYTextLabel(-i, socialConditions[0]);
	        	}
	        	else if(i==4)
	        	{
	        		multiRenderer.addYTextLabel(-i, socialConditions[1]);
	        	}
	        	else if(i==7)
	        	{
	        		multiRenderer.addYTextLabel(-i, socialConditions[2]);
	        	}
	        	else if(i==10)
	        	{
	        		multiRenderer.addYTextLabel(-i, socialConditions[3]);
	        	}
	        	else
	        	{
	        		multiRenderer.addYTextLabel(-i, "");
	        	}
	        }
	        multiRenderer.addSeriesRenderer(csocialRenderer);
	        multiRenderer.addSeriesRenderer(psocialRenderer);
	        // Creating a Line Chart
	        mChart = ChartFactory.getTimeChartView(getActivity(), dataset, multiRenderer, "MM/dd");
	    	break;
	    }
		
	    
       
 
        // Adding the Line Chart to the LinearLayout
        graphScreen.addView(mChart);
		
		
	}
	

	public void phoneActivity(){


		try{
			File sdcard = Environment.getExternalStorageDirectory();
			File file= new File (sdcard, "sadhealth/users/"+ MainActivity.USERID + "/"+phoneUnlockFileName+".csv");		
			BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
			String s=null;
			if((s=bufferedReader.readLine())!=null){
				String d[] = s.split(",");
				//unlockNoview.setText(d[1]);
			}
			else{
				//unlockNoview.setText("0");
			}
			bufferedReader.close();
		}
		catch (IOException e){

		}

	}

	public void getDataCommunity (File fn) throws IOException
	{
		FileReader fr= new FileReader(fn);
		BufferedReader bufferedReader = new BufferedReader(fr);
		DateTimeFormatter dtf=DateTimeFormat.forPattern("MM/dd/yyyy");
		DateTime now=new DateTime();
		String line = null;

		String[] data;
		while ((line = bufferedReader.readLine()) != null) {
			data = line.split(",");
			//Log.i(TAG,"Read data "+ data[0]);
			//check if the currently processed line is on the same year
			if(Integer.valueOf(data[0])==now.getYear())
			{
				//check if current month
				if(Integer.valueOf(data[1])==desiredMonth)
				{
					dates.add(dtf.parseDateTime(data[3]).toDate());
					y_vals.add(Double.valueOf(data[4]));
				}
			}
		}
		//max = lmax;
		bufferedReader.close();

	}



	public void getQuestionnareData (File fn) throws IOException
	{
		Log.i(TAG,"readQuestionnareData");
		FileReader fr= new FileReader(fn);
		BufferedReader bufferedReader = new BufferedReader(fr);
		DateTimeFormatter dtf=DateTimeFormat.forPattern("MM/dd/yyyy");
		DateTime now=new DateTime();
		String line = null;

		String[] data;
		while ((line = bufferedReader.readLine()) != null) {
			data = line.split(",");
			//Log.i(TAG,"Read data "+ data[0]);
			//check if the currently processed line is on the same year
			if(Integer.valueOf(data[0])==now.getYear())
			{
				//check if current month
				if(Integer.valueOf(data[1])==desiredMonth)
				{
					cdatesQuestionnaire.add(dtf.parseDateTime(data[3]).toDate());
					cmood.add(-Double.valueOf(data[4]));
					csleep.add(-Double.valueOf(data[5]));
					cenergy.add(-Double.valueOf(data[6]));
					csocial.add(-Double.valueOf(data[7]));
				}
			}
		}
		//max = lmax;
		bufferedReader.close();

	}




	public GraphViewData[] toData (File filename) throws IOException{
		FileReader fileReader = new FileReader(filename);
		BufferedReader bufferedReader = new BufferedReader(fileReader);
		List<GraphViewData> lines = new ArrayList<GraphViewData>();
		String line = null;
		boolean is1st=true;
		String[] data;
		while ((line = bufferedReader.readLine()) != null) {
			data = line.split(",");
			if (is1st){
				min=Long.valueOf(data[0]);
				is1st=false;
			}
			max=Long.valueOf(data[0]);
			lines.add(new GraphViewData(Double.valueOf(data[0]),Double.valueOf(data[1])));
		}
		//max = lmax;
		bufferedReader.close();
		return lines.toArray(new GraphViewData[lines.size()]);


	}

	private void getYesterday(String id) throws IOException {
		DateTime dt= new DateTime();
		dt=dt.minusDays(1);

		if(id.contentEquals("Light"))
		{
			//load list of GeoPoints
			File sdcard = Environment.getExternalStorageDirectory();
			Log.i(TAG, "Light-filelatest "+lightFileName );
			File file= new File (sdcard, "sadhealth/CS/"+country+"/"+city+ "/"+lightFileName+".csv");
			if(file.exists())
			{
				FileReader fr= new FileReader(file);
				BufferedReader bufferedReader = new BufferedReader(fr);
				DateTimeFormatter dtf=DateTimeFormat.forPattern("MM/dd/yyyy");
				DateTime now=new DateTime();
				String line = null;

				String[] data;
				while ((line = bufferedReader.readLine()) != null) {
					data = line.split(",");
					//Log.i(TAG,"Read data "+ data[0]);
					//check if the currently processed line is on the same year
					if(Integer.valueOf(data[0])==now.getYear())
					{
						if(Integer.valueOf(data[1])==dt.getMonthOfYear())
						{

							//check if current month
							if(Integer.valueOf(data[2])==dt.getDayOfMonth())
							{
								dates.add(dtf.parseDateTime(data[3]).toDate());
								y_vals.add(Double.valueOf(data[4]));
							}
						}
					}
				}
				//max = lmax;
				bufferedReader.close();
			}

			file= new File (sdcard, "sadhealth/users/"+ _appPrefs.getUserID()+ "/"+personalLightMaxFileName+".csv");
			if (file.exists())
			{
				FileReader fr= new FileReader(file);
				BufferedReader bufferedReader = new BufferedReader(fr);
				DateTimeFormatter dtf=DateTimeFormat.forPattern("MM/dd/yyyy");

				String line = null;

				String[] data;
				Log.i(TAG,"Community-Light-Yesterday "+dt.getDayOfMonth());
				while ((line = bufferedReader.readLine()) != null) {
					data = line.split(",");
					//Log.i(TAG,"Read data "+ data[0]);
					//check if the currently processed line is on the same year
					if(Integer.valueOf(data[0])==dt.getYear())
					{
						//check if current month
						if(Integer.valueOf(data[1])==dt.getMonthOfYear())
						{
							if(Integer.valueOf(data[2])==dt.getDayOfMonth())
							{
								pdates.add(dtf.parseDateTime(data[3]).toDate());
								py_vals.add(Double.valueOf(data[4]));
								Log.i(TAG,"Community-Light-Yesterday py_vals "+ data[4]);
							}
						}
					}
				}
				//max = lmax;
				bufferedReader.close();
			}


		}
		else if(id.contentEquals("Activity"))
		{

		}
		else if(id.contentEquals("Light Distribution"))
		{
			DateTimeFormatter sdf = DateTimeFormat.forPattern("HH:mm:ss");
			DateTimeFormatter dtf=DateTimeFormat.forPattern("yyyy/MM/dd");

			File fn = new File (sdcard, "sadhealth/users/"+ _appPrefs.getUserID()+"/lightSources.csv");
			FileReader fr= new FileReader(fn);
			BufferedReader bufferedReader = new BufferedReader(fr);
			double psumALS=0;
			double psumNLS=0;
			String line = null;

			String[] data;
			while ((line = bufferedReader.readLine()) != null) {
				data = line.split(",");

				if (dtf.parseDateTime(data[0]).getDayOfMonth()==dt.getDayOfMonth())
				{

					DateTime ALS = sdf.parseDateTime(data[1]);
					DateTime NLS =sdf.parseDateTime(data[2]);

					psumALS+=ALS.getHourOfDay();
					psumNLS+=NLS.getHourOfDay();
					psumALS+=ALS.getMinuteOfDay()%60;
					psumNLS+=NLS.getMinuteOfDay()%60;
				}

			}

			pALS=(int) psumALS;
			pNLS=(int) psumNLS;

			//max = lmax;
			bufferedReader.close();
		}
		else if(id.contentEquals("Mood")|| id.contentEquals("Sleep") ||id.contentEquals("Energy") || id.contentEquals("Social") )
		{

			File file;

			Log.i(TAG," day is "+ dt.getDayOfMonth());

			file= new File (sdcard, "sadhealth/CS/"+country+"/"+city+ "/Questionnaire.csv");
			if(file.exists())
			{
				Log.i(TAG,"Questionnaire-file-exists");
				FileReader fr= new FileReader(file);
				BufferedReader bufferedReader = new BufferedReader(fr);
				String line = null;   
				String[] data;
				while ((line = bufferedReader.readLine()) != null)
				{
					data = line.split(",");
					Log.i(TAG,data[0]+" = "+dt.getYear());
					if(Integer.valueOf(data[0])==dt.getYear())
					{
						Log.i(TAG,data[1]+" = "+dt.getMonthOfYear());
						//check if current month
						if(Integer.valueOf(data[1])==dt.getMonthOfYear())
						{
							Log.i(TAG,data[2]+" = "+dt.getDayOfMonth());
							if(Integer.valueOf(data[2])==dt.getDayOfMonth())
							{
								try {
									cdatesQuestionnaire.add(new SimpleDateFormat("MM/dd").parse(data[3]));
								} catch (ParseException e) {

									e.printStackTrace();
								}
								cmood.add(-Double.valueOf(data[4]));
								csleep.add(-Double.valueOf(data[5]));
								cenergy.add(-Double.valueOf(data[6]));
								csocial.add(-Double.valueOf(data[7]));
								Log.i(TAG," questionnaire values are "+data[4]+" "+data[5]+" "+data[6]+" "+data[7]);
							}
						}
					}
					
				
				}
				bufferedReader.close();
			}

			file= new File (sdcard, "sadhealth/users/"+ MainActivity.USERID + "/questionnaire"+String.format("%02d", dt.getMonthOfYear())+String.format("%02d", dt.getDayOfMonth())+".csv");
			if (file.exists())
			{
				FileReader fr= new FileReader(file);
				BufferedReader bufferedReader = new BufferedReader(fr);
				String line = null;   
				String[] data;
				while ((line = bufferedReader.readLine()) != null)
				{
					data = line.split(",");
					try {
						pdatesQuestionnaire.add(new SimpleDateFormat("MM/dd").parse(data[1]));
					} catch (ParseException e) {

						e.printStackTrace();
					}
					pmood.add(-Double.valueOf(data[2]));
					psleep.add(-Double.valueOf(data[3]));
					penergy.add(-Double.valueOf(data[4]));
					psocial.add(-Double.valueOf(data[5]));
					break;
				}
				bufferedReader.close();

			}

		}

	}

	private void getToday(String id) throws IOException {
		DateTime dt= new DateTime();
		if(id.contentEquals("Light"))
		{
			//load list of GeoPoints
			File sdcard = Environment.getExternalStorageDirectory();
			Log.i(TAG, "Light-filelatest "+lightFileName );
			File file= new File (sdcard, "sadhealth/CS/"+country+"/"+city+ "/"+lightFileName+".csv");
			if(file.exists())
			{
				FileReader fr= new FileReader(file);
				BufferedReader bufferedReader = new BufferedReader(fr);
				DateTimeFormatter dtf=DateTimeFormat.forPattern("MM/dd/yyyy");
				DateTime now=new DateTime();
				String line = null;

				String[] data;
				while ((line = bufferedReader.readLine()) != null) {
					data = line.split(",");
					//Log.i(TAG,"Read data "+ data[0]);
					//check if the currently processed line is on the same year
					if(Integer.valueOf(data[0])==now.getYear())
					{
						if(Integer.valueOf(data[1])==dt.getMonthOfYear())
						{

							//check if current month
							if(Integer.valueOf(data[2])==dt.getDayOfMonth())
							{
								dates.add(dtf.parseDateTime(data[3]).toDate());
								y_vals.add(Double.valueOf(data[4]));
							}
						}
					}
				}
				//max = lmax;
				bufferedReader.close();
			}

			file= new File (sdcard, "sadhealth/users/"+ _appPrefs.getUserID()+ "/maxlightblocks.csv");
			if (file.exists())
			{
				FileReader fr= new FileReader(file);
				BufferedReader bufferedReader = new BufferedReader(fr);
				DateTimeFormatter dtf=DateTimeFormat.forPattern("MMdd");

				String line = null;

				String[] data;
				Log.i(TAG,"Community-Light-Yesterday "+dt.getDayOfMonth());
				while ((line = bufferedReader.readLine()) != null) {
					data = line.split(",");
					//Log.i(TAG,"Read data "+ data[0]);
					//check if the currently processed line is on the same year


					pdates.add(dtf.parseDateTime(data[1]).toDate());
					py_vals.add(Double.valueOf(data[2]));
					Log.i(TAG,"Community-Light-Yesterday py_vals "+ data[2]);



				}
				//max = lmax;
				bufferedReader.close();
			}
		}
		else if(id.contentEquals("Activity"))
		{

		}
		else if(id.contentEquals("Light Distribution"))
		{
			DateTimeFormatter sdf = DateTimeFormat.forPattern("HH:mm:ss");
			DateTimeFormatter dtf=DateTimeFormat.forPattern("M/d/yyyy");
			File file = new File (sdcard, "sadhealth/CS/"+country+"/"+city+ "/Lux.csv");
			if(file.exists())
			{
				FileReader fr= new FileReader(file);
				BufferedReader bufferedReader = new BufferedReader(fr);
				double sumALS=0;
				double sumNLS=0;
				String line = null;

				String[] data;
				while ((line = bufferedReader.readLine()) != null) {
					data = line.split(",");
					if(dtf.parseDateTime(data[0]).getYear()==dt.getYear())
					{
						if(dtf.parseDateTime(data[0]).getMonthOfYear()==dt.getMonthOfYear())
						{

							//check if current month
							if(dtf.parseDateTime(data[0]).getDayOfMonth()==dt.getDayOfMonth())
							{
								DateTime ALS = sdf.parseDateTime(data[1]);
								DateTime NLS =sdf.parseDateTime(data[2]);

								sumALS+=ALS.getHourOfDay();
								sumNLS+=NLS.getHourOfDay();
								sumALS+=ALS.getMinuteOfDay()%60;
								sumNLS+=NLS.getMinuteOfDay()%60;
							}
						}
					}

				}
				cALS=(int) sumALS;
				cNLS=(int) sumNLS;

				//max = lmax;
				bufferedReader.close();
			}



			dtf=DateTimeFormat.forPattern("yyyy/MM/dd");
			file = new File (sdcard, "sadhealth/users/"+ _appPrefs.getUserID()+"/lightSources.csv");
			if(file.exists())
			{

				FileReader fr= new FileReader(file);
				BufferedReader bufferedReader = new BufferedReader(fr);
				double psumALS=0;
				double psumNLS=0;
				String line = null;

				String[] data;
				while ((line = bufferedReader.readLine()) != null) {
					data = line.split(",");
					Log.i(TAG,"XD- "+dtf.parseDateTime(data[0]).getDayOfMonth()+" "+dt.getDayOfMonth());
					if (dtf.parseDateTime(data[0]).getDayOfMonth()==dt.getDayOfMonth())
					{

						DateTime ALS = sdf.parseDateTime(data[1]);
						DateTime NLS =sdf.parseDateTime(data[2]);

						psumALS+=ALS.getHourOfDay();
						psumNLS+=NLS.getHourOfDay();
						psumALS+=ALS.getMinuteOfDay()%60;
						psumNLS+=NLS.getMinuteOfDay()%60;

					}

				}

				pALS=(int) psumALS;
				pNLS=(int) psumNLS;

				//max = lmax;
				bufferedReader.close();
			}
		}
		else if(id.contentEquals("Mood")|| id.contentEquals("Sleep") ||id.contentEquals("Energy") || id.contentEquals("Social") )
		{

			File file;

			Log.i(TAG," day is "+ dt.getDayOfMonth());

			file= new File (sdcard, "sadhealth/CS/"+country+"/"+city+ "/Questionnaire.csv");
			if(file.exists())
			{
				Log.i(TAG,"Questionnaire-file-exists");
				FileReader fr= new FileReader(file);
				BufferedReader bufferedReader = new BufferedReader(fr);
				String line = null;   
				String[] data;
				while ((line = bufferedReader.readLine()) != null)
				{
					data = line.split(",");
					if(Integer.valueOf(data[0])==dt.getYear())
					{
						//check if current month
						if(Integer.valueOf(data[1])==dt.getMonthOfYear())
						{
							Log.i(TAG,data[2]+" = "+dt.getDayOfMonth());
							if(Integer.valueOf(data[2])==dt.getDayOfMonth())
							{
								try {
									cdatesQuestionnaire.add(new SimpleDateFormat("MM/dd").parse(data[3]));
								} catch (ParseException e) {

									e.printStackTrace();
								}
								cmood.add(-Double.valueOf(data[4]));
								csleep.add(-Double.valueOf(data[5]));
								cenergy.add(-Double.valueOf(data[6]));
								csocial.add(-Double.valueOf(data[7]));
								Log.i(TAG," questionnaire values are "+data[4]+" "+data[5]+" "+data[6]+" "+data[7]);
							}
						}
					}
					
					
				}
				bufferedReader.close();
			}

			file= new File (sdcard, "sadhealth/users/"+ MainActivity.USERID + "/questionnaire"+String.format("%02d", dt.getMonthOfYear())+String.format("%02d", dt.getDayOfMonth())+".csv");
			if (file.exists())
			{
				FileReader fr= new FileReader(file);
				BufferedReader bufferedReader = new BufferedReader(fr);
				String line = null;   
				String[] data;
				while ((line = bufferedReader.readLine()) != null)
				{
					data = line.split(",");
					try {
						pdatesQuestionnaire.add(new SimpleDateFormat("MM/dd").parse(data[1]));
					} catch (ParseException e) {

						e.printStackTrace();
					}
					pmood.add(-Double.valueOf(data[2]));
					psleep.add(-Double.valueOf(data[3]));
					penergy.add(-Double.valueOf(data[4]));
					psocial.add(-Double.valueOf(data[5]));
					break;
				}
				bufferedReader.close();

			}
		}


	}

	private void getLastWeek(String id) throws NumberFormatException, IOException {
		LocalDate dateTime = new LocalDate();
		if(id.contentEquals("Light"))
		{
			DateTimeFormatter dtf=DateTimeFormat.forPattern("M/d/yyyy");
			LocalDate startOfWeek = dateTime.minusDays(dateTime.dayOfWeek().get() - 1);
			LocalDate endOfWeek = dateTime.plusDays(7 - dateTime.dayOfWeek().get());

			startOfWeek=startOfWeek.minusWeeks(1);
			endOfWeek=endOfWeek.minusWeeks(1);

			Log.i(TAG, "last week"+startOfWeek.toString(dtf)+" to "+endOfWeek.toString(dtf));

			//load list of GeoPoints
			File sdcard = Environment.getExternalStorageDirectory();
			Log.i(TAG, "Light-filelatest "+lightFileName );
			File file= new File (sdcard, "sadhealth/CS/"+country+"/"+city+ "/"+lightFileName+".csv");
			if(file.exists())
			{
				FileReader fr= new FileReader(file);
				BufferedReader bufferedReader = new BufferedReader(fr);

				String line = null;

				String[] data;
				for (LocalDate date = startOfWeek; date.isBefore(endOfWeek) ||date.isEqual(endOfWeek); date = date.plusDays(1))
				{
					while ((line = bufferedReader.readLine()) != null) {
						data = line.split(",");
						Log.i(TAG, "Comlast-Week"+data[3]+"  ->  "+date.toString(dtf));
						if (data[3].contentEquals(date.toString(dtf)))
						{
							dates.add(dtf.parseDateTime(data[3]).toDate());
							y_vals.add(Double.valueOf(data[4]));
							break;
						}
					}

				}
				//max = lmax;
				bufferedReader.close();
			}

			dtf=DateTimeFormat.forPattern("MM/dd/yyyy");
			file = new File (sdcard, "sadhealth/users/"+ MainActivity.USERID + "/maxlight.csv");
			if (file.exists())
			{
				FileReader fr = null;

				fr = new FileReader(file);

				BufferedReader bufferedReader = new BufferedReader(fr);
				String line = null;

				String[] data;
				for (LocalDate date = startOfWeek; date.isBefore(endOfWeek) ||date.isEqual(endOfWeek); date = date.plusDays(1))
				{
					Log.i(TAG, "last week"+date.toString(dtf));

					while ((line = bufferedReader.readLine()) != null) 
					{
						data = line.split(",");
						Log.i(TAG, "This-Week"+data[3]);
						if (data[3].contentEquals(date.toString(dtf)))
						{
							pdates.add(dtf.parseDateTime(data[3]).toDate());
							py_vals.add(Double.valueOf(data[4]));
							Log.i(TAG,"This-Week-found"+ data[4]);
							break;
						}
					}

				}
				bufferedReader.close();

			}
		}
		else if(id.contentEquals("Activity"))
		{

		}
		else if(id.contentEquals("Light Distribution"))
		{
			DateTimeFormatter sdf = DateTimeFormat.forPattern("HH:mm:ss");
			DateTimeFormatter dtf=DateTimeFormat.forPattern("M/d/yyyy");
			LocalDate startOfWeek = dateTime.minusDays(dateTime.dayOfWeek().get() - 1);
			LocalDate endOfWeek = dateTime.plusDays(7 - dateTime.dayOfWeek().get());

			startOfWeek=startOfWeek.minusWeeks(1);
			endOfWeek=endOfWeek.minusWeeks(1);

			Log.i(TAG, "this week"+startOfWeek.toString(dtf)+" to "+endOfWeek.toString(dtf));

			File file = new File (sdcard, "sadhealth/CS/"+country+"/"+city+ "/Lux.csv");
			if (file.exists())
			{	
				FileReader fr= new FileReader(file);
				BufferedReader bufferedReader = new BufferedReader(fr);
				double sumALS=0;
				double sumNLS=0;
				String line = null;

				String[] data;

				for (LocalDate date = startOfWeek; date.isBefore(endOfWeek) ||date.isEqual(endOfWeek); date = date.plusDays(1))
				{
					while ((line = bufferedReader.readLine()) != null) {
						data = line.split(",");

						
						if (data[0].contentEquals(date.toString(dtf)))
						{

							DateTime ALS = sdf.parseDateTime(data[1]);
							DateTime NLS =sdf.parseDateTime(data[2]);

							sumALS+=ALS.getHourOfDay();
							sumNLS+=NLS.getHourOfDay();
							sumALS+=ALS.getMinuteOfDay()%60;
							sumNLS+=NLS.getMinuteOfDay()%60;

							break;
						}
					}
				}

				cALS=(int) sumALS;
				cNLS=(int) sumNLS;
				bufferedReader.close();
			}

			dtf=DateTimeFormat.forPattern("yyyy/MM/dd");
			File fn = new File (sdcard, "sadhealth/users/"+ _appPrefs.getUserID()+"/lightSources.csv");
			if (file.exists())
			{
				FileReader fr= new FileReader(fn);
				BufferedReader bufferedReader = new BufferedReader(fr);
				double psumALS=0;
				double psumNLS=0;
				String line = null;

				String[] data;


				while ((line = bufferedReader.readLine()) != null) {
					data = line.split(",");
					for (LocalDate date = startOfWeek; date.isBefore(endOfWeek) ||date.isEqual(endOfWeek); date = date.plusDays(1))
					{
						Log.i(TAG,"LD -Data[0] "+data[0]+"-> " +date.toString(dtf));
						if (data[0].contentEquals(date.toString(dtf)))
						{

							DateTime ALS = sdf.parseDateTime(data[1]);
							DateTime NLS =sdf.parseDateTime(data[2]);

							psumALS+=ALS.getHourOfDay();
							psumNLS+=NLS.getHourOfDay();
							psumALS+=ALS.getMinuteOfDay()%60;
							psumNLS+=NLS.getMinuteOfDay()%60;

							break;
						}
					}
				}

				pALS=(int) psumALS;
				pNLS=(int) psumNLS;

				//max = lmax;
				bufferedReader.close();
			}
		}
		else if(id.contentEquals("Mood")|| id.contentEquals("Sleep") ||id.contentEquals("Energy") || id.contentEquals("Social") )
		{
			File file;

			DateTimeFormatter sdf = DateTimeFormat.forPattern("MMdd");
			DateTimeFormatter xdtf=DateTimeFormat.forPattern("M/d/yyyy");
			DateTimeFormatter dtf=DateTimeFormat.forPattern("MM/dd");
			LocalDate startOfWeek = dateTime.minusDays(dateTime.dayOfWeek().get() - 1);
			LocalDate endOfWeek = dateTime.plusDays(7 - dateTime.dayOfWeek().get());

			startOfWeek=startOfWeek.minusWeeks(1);
			endOfWeek=endOfWeek.minusWeeks(1);
			
			Log.i(TAG, "last week"+startOfWeek.toString(dtf)+" to "+endOfWeek.toString(dtf));
			
			
			file= new File (sdcard, "sadhealth/CS/"+country+"/"+city+ "/Questionnaire.csv");
			if(file.exists())
			{
				FileReader fr= new FileReader(file);
				BufferedReader bufferedReader = new BufferedReader(fr);
				String line = null;

				String[] data;
				for (LocalDate date = startOfWeek; date.isBefore(endOfWeek) ||date.isEqual(endOfWeek); date = date.plusDays(1))
				{
					while ((line = bufferedReader.readLine()) != null)
					{
						data=line.split(",");
						
							Log.i(TAG,"LD -Data[3] "+data[3]+"-> " +date.toString(xdtf));
							if (data[3].contentEquals(date.toString(xdtf)))
							{
								Log.i(TAG,"contentEquals "+date.toString());
								try {
									cdatesQuestionnaire.add(new SimpleDateFormat("MM/dd").parse(data[3]));
								} catch (ParseException e) {
		
									e.printStackTrace();
								}
								Log.i(TAG,"Here!");
								cmood.add(-Double.valueOf(data[4]));
								csleep.add(-Double.valueOf(data[5]));
								cenergy.add(-Double.valueOf(data[6]));
								csocial.add(-Double.valueOf(data[7]));
								break;
							}
							
					}
					Log.i(TAG,"LD - next date: " +date.toString(xdtf));
				}
				bufferedReader.close();
			}
			
			
			for (LocalDate date = startOfWeek; date.isBefore(endOfWeek) ||date.isEqual(endOfWeek); date = date.plusDays(1))
			{
				file= new File (sdcard, "sadhealth/users/"+ MainActivity.USERID + "/questionnaire"+date.toString(sdf)+".csv");
				if (file.exists())
				{
					FileReader fr= new FileReader(file);
					BufferedReader bufferedReader = new BufferedReader(fr);
					String line = null;

					String[] data;
					while ((line = bufferedReader.readLine()) != null) {

						data = line.split(",");
						try {
							pdatesQuestionnaire.add(new SimpleDateFormat("MM/dd").parse(data[1]));
						} catch (ParseException e) {

							e.printStackTrace();
						}
						pmood.add(-Double.valueOf(data[2]));
						psleep.add(-Double.valueOf(data[3]));
						penergy.add(-Double.valueOf(data[4]));
						psocial.add(-Double.valueOf(data[5]));
						break;
					}
					bufferedReader.close();

				}
			}
		}
	}


	private void getThisWeek(String id) throws IOException, ParseException {
		LocalDate dateTime = new LocalDate();
		if(id.contentEquals("Light"))
		{
			DateTimeFormatter dtf=DateTimeFormat.forPattern("M/d/yyyy");
			LocalDate startOfWeek = dateTime.minusDays(dateTime.dayOfWeek().get() - 1);
			LocalDate endOfWeek = dateTime.plusDays(7 - dateTime.dayOfWeek().get());



			Log.i(TAG, "last week"+startOfWeek.toString(dtf)+" to "+endOfWeek.toString(dtf));

			//load list of GeoPoints
			File sdcard = Environment.getExternalStorageDirectory();
			Log.i(TAG, "Light-filelatest "+lightFileName );
			File file= new File (sdcard, "sadhealth/CS/"+country+"/"+city+ "/"+lightFileName+".csv");
			if(file.exists())
			{
				FileReader fr= new FileReader(file);
				BufferedReader bufferedReader = new BufferedReader(fr);

				String line = null;

				String[] data;
				for (LocalDate date = startOfWeek; date.isBefore(endOfWeek) ||date.isEqual(endOfWeek); date = date.plusDays(1))
				{
					while ((line = bufferedReader.readLine()) != null) {
						data = line.split(",");
						Log.i(TAG, "Comlast-Week"+data[3]+"  ->  "+date.toString(dtf));
						if (data[3].contentEquals(date.toString(dtf)))
						{
							dates.add(dtf.parseDateTime(data[3]).toDate());
							y_vals.add(Double.valueOf(data[4]));
							break;
						}
					}

				}
				//max = lmax;
				bufferedReader.close();
			}

			dtf=DateTimeFormat.forPattern("MM/dd/yyyy");
			file = new File (sdcard, "sadhealth/users/"+ MainActivity.USERID + "/maxlight.csv");
			if (file.exists())
			{
				FileReader fr = null;

				fr = new FileReader(file);

				BufferedReader bufferedReader = new BufferedReader(fr);
				String line = null;

				String[] data;
				for (LocalDate date = startOfWeek; date.isBefore(endOfWeek) ||date.isEqual(endOfWeek); date = date.plusDays(1))
				{
					Log.i(TAG, "last week"+date.toString(dtf));

					while ((line = bufferedReader.readLine()) != null) 
					{
						data = line.split(",");
						
						if (data[3].contentEquals(date.toString(dtf)))
						{
							pdates.add(dtf.parseDateTime(data[3]).toDate());
							py_vals.add(Double.valueOf(data[4]));
							
							break;
						}
					}

				}
				bufferedReader.close();

			}
		}
		else if(id.contentEquals("Activity"))
		{

		}
		else if(id.contentEquals("Light Distribution"))
		{
			DateTimeFormatter sdf = DateTimeFormat.forPattern("HH:mm:ss");
			DateTimeFormatter dtf=DateTimeFormat.forPattern("M/d/yyyy");
			LocalDate startOfWeek = dateTime.minusDays(dateTime.dayOfWeek().get() - 1);
			LocalDate endOfWeek = dateTime.plusDays(7 - dateTime.dayOfWeek().get());

			File file = new File (sdcard, "sadhealth/CS/"+country+"/"+city+ "/Lux.csv");
			if (file.exists())
			{	
				FileReader fr= new FileReader(file);
				BufferedReader bufferedReader = new BufferedReader(fr);
				double sumALS=0;
				double sumNLS=0;
				String line = null;

				String[] data;

				for (LocalDate date = startOfWeek; date.isBefore(endOfWeek) ||date.isEqual(endOfWeek); date = date.plusDays(1))
				{
					while ((line = bufferedReader.readLine()) != null) {
						data = line.split(",");

						
						if (data[0].contentEquals(date.toString(dtf)))
						{

							DateTime ALS = sdf.parseDateTime(data[1]);
							DateTime NLS =sdf.parseDateTime(data[2]);

							sumALS+=ALS.getHourOfDay();
							sumNLS+=NLS.getHourOfDay();
							sumALS+=ALS.getMinuteOfDay()%60;
							sumNLS+=NLS.getMinuteOfDay()%60;

							break;
						}
					}
				}

				cALS=(int) sumALS;
				cNLS=(int) sumNLS;
				bufferedReader.close();
			}

			dtf=DateTimeFormat.forPattern("yyyy/MM/dd");
			file= new File (sdcard, "sadhealth/users/"+ _appPrefs.getUserID()+"/lightSources.csv");
			if (file.exists())
			{
				FileReader fr= new FileReader(file);
				BufferedReader bufferedReader = new BufferedReader(fr);
				double psumALS=0;
				double psumNLS=0;
				String line = null;

				String[] data;


				while ((line = bufferedReader.readLine()) != null) {
					data = line.split(",");
					for (LocalDate date = startOfWeek; date.isBefore(endOfWeek) ||date.isEqual(endOfWeek); date = date.plusDays(1))
					{
						
						if (data[0].contentEquals(date.toString(dtf)))
						{

							DateTime ALS = sdf.parseDateTime(data[1]);
							DateTime NLS =sdf.parseDateTime(data[2]);

							psumALS+=ALS.getHourOfDay();
							psumNLS+=NLS.getHourOfDay();
							psumALS+=ALS.getMinuteOfDay()%60;
							psumNLS+=NLS.getMinuteOfDay()%60;

							break;
						}
					}
				}

				pALS=(int) psumALS;
				pNLS=(int) psumNLS;

				//max = lmax;
				bufferedReader.close();
			}
		}
		else if(id.contentEquals("Mood")|| id.contentEquals("Sleep") ||id.contentEquals("Energy") || id.contentEquals("Social") )
		{
			File file;

			DateTimeFormatter sdf = DateTimeFormat.forPattern("MMdd");
			DateTimeFormatter xdtf=DateTimeFormat.forPattern("M/d/yyyy");
			DateTimeFormatter dtf=DateTimeFormat.forPattern("MM/dd");
			LocalDate startOfWeek = dateTime.minusDays(dateTime.dayOfWeek().get() - 1);
			LocalDate endOfWeek = dateTime.plusDays(7 - dateTime.dayOfWeek().get());

			
			
			Log.i(TAG, "this week"+startOfWeek.toString(dtf)+" to "+endOfWeek.toString(dtf));
			
			
			file= new File (sdcard, "sadhealth/CS/"+country+"/"+city+ "/Questionnaire.csv");
			if(file.exists())
			{
				FileReader fr= new FileReader(file);
				BufferedReader bufferedReader = new BufferedReader(fr);
				String line = null;

				String[] data;
				for (LocalDate date = startOfWeek; date.isBefore(endOfWeek) ||date.isEqual(endOfWeek); date = date.plusDays(1))
				{
					while ((line = bufferedReader.readLine()) != null)
					{
						data=line.split(",");
						
							Log.i(TAG,"LD -Data[3] "+data[3]+"-> " +date.toString(xdtf));
							if (data[3].contentEquals(date.toString(xdtf)))
							{
								Log.i(TAG,"contentEquals "+date.toString());
								try {
									cdatesQuestionnaire.add(new SimpleDateFormat("MM/dd").parse(data[3]));
								} catch (ParseException e) {
		
									e.printStackTrace();
								}
								Log.i(TAG,"Here!");
								cmood.add(-Double.valueOf(data[4]));
								csleep.add(-Double.valueOf(data[5]));
								cenergy.add(-Double.valueOf(data[6]));
								csocial.add(-Double.valueOf(data[7]));
								break;
							}
							
					}
					Log.i(TAG,"LD - next date: " +date.toString(xdtf));
				}
				bufferedReader.close();
			}
			
			
			for (LocalDate date = startOfWeek; date.isBefore(endOfWeek) ||date.isEqual(endOfWeek); date = date.plusDays(1))
			{
				file= new File (sdcard, "sadhealth/users/"+ MainActivity.USERID + "/questionnaire"+date.toString(sdf)+".csv");
				if (file.exists())
				{
					FileReader fr= new FileReader(file);
					BufferedReader bufferedReader = new BufferedReader(fr);
					String line = null;

					String[] data;
					while ((line = bufferedReader.readLine()) != null) {

						data = line.split(",");
						try {
							pdatesQuestionnaire.add(new SimpleDateFormat("MM/dd").parse(data[1]));
						} catch (ParseException e) {

							e.printStackTrace();
						}
						pmood.add(-Double.valueOf(data[2]));
						psleep.add(-Double.valueOf(data[3]));
						penergy.add(-Double.valueOf(data[4]));
						psocial.add(-Double.valueOf(data[5]));
						break;
					}
					bufferedReader.close();

				}
			}
		}
		

	}


	private void getLastMonth(String id) throws NumberFormatException, IOException {
		DateTime dt= new DateTime();
		if(id.contentEquals("Light"))
		{
			dt=dt.minusMonths(1);
			DateTimeFormatter dtf=DateTimeFormat.forPattern("M/d/yyyy");

			//load list of GeoPoints
			File sdcard = Environment.getExternalStorageDirectory();
			Log.i(TAG, "Light-filelatest "+lightFileName );
			File file= new File (sdcard, "sadhealth/CS/"+country+"/"+city+ "/"+lightFileName+".csv");
			if(file.exists())
			{
				FileReader fr= new FileReader(file);
				BufferedReader bufferedReader = new BufferedReader(fr);

				String line = null;

				String[] data;

				while ((line = bufferedReader.readLine()) != null) {
					data = line.split(",");

					if (data[1].contentEquals(String.valueOf(dt.getMonthOfYear())))
					{
						dates.add(dtf.parseDateTime(data[3]).toDate());
						y_vals.add(Double.valueOf(data[4]));

					}
				}


				//max = lmax;
				bufferedReader.close();
			}



			dtf=DateTimeFormat.forPattern("MM/dd/yyyy");

			Log.i(TAG, "last month"+dt.getMonthOfYear());

			//Log.i(TAG, "this week"+date.toString(dtf));
			file = new File (sdcard, "sadhealth/users/"+ MainActivity.USERID + "/maxlight.csv");
			if (file.exists())
			{
				FileReader fr = null;

				fr = new FileReader(file);

				BufferedReader bufferedReader = new BufferedReader(fr);
				String line = null;

				String[] data;


				while ((line = bufferedReader.readLine()) != null) 
				{
					data = line.split(",");
					Log.i(TAG, "This-Week"+data[3]);
					if (data[1].contentEquals(String.valueOf(dt.getMonthOfYear())))
					{
						pdates.add(dtf.parseDateTime(data[3]).toDate());
						py_vals.add(Double.valueOf(data[4]));
					}
				}
				bufferedReader.close();

			}
		}
		else if(id.contentEquals("Activity"))
		{

		}
		else if(id.contentEquals("Light Distribution"))
		{
			DateTimeFormatter sdf = DateTimeFormat.forPattern("HH:mm:ss");
			DateTimeFormatter dtf=DateTimeFormat.forPattern("M/d/yyyy");
			dt=dt.minusMonths(1);


			File file = new File (sdcard, "sadhealth/CS/"+country+"/"+city+ "/Lux.csv");
			if (file.exists())
			{	
				FileReader fr= new FileReader(file);
				BufferedReader bufferedReader = new BufferedReader(fr);
				double sumALS=0;
				double sumNLS=0;
				String line = null;

				String[] data;


				while ((line = bufferedReader.readLine()) != null) {
					data = line.split(",");


					if (dtf.parseDateTime(data[0]).getMonthOfYear()==dt.getMonthOfYear())
					{

						DateTime ALS = sdf.parseDateTime(data[1]);
						DateTime NLS =sdf.parseDateTime(data[2]);

						sumALS+=ALS.getHourOfDay();
						sumNLS+=NLS.getHourOfDay();
						sumALS+=ALS.getMinuteOfDay()%60;
						sumNLS+=NLS.getMinuteOfDay()%60;


					}

				}

				cALS=(int) sumALS;
				cNLS=(int) sumNLS;
				bufferedReader.close();
			}

			dtf=DateTimeFormat.forPattern("yyyy/MM/dd");
			file = new File (sdcard, "sadhealth/users/"+ _appPrefs.getUserID()+"/lightSources.csv");
			if (file.exists())
			{
				FileReader fr= new FileReader(file);
				BufferedReader bufferedReader = new BufferedReader(fr);
				double psumALS=0;
				double psumNLS=0;
				String line = null;

				String[] data;


				while ((line = bufferedReader.readLine()) != null) {
					data = line.split(",");


					if (dtf.parseDateTime(data[0]).getMonthOfYear()==dt.getMonthOfYear())
					{

						DateTime ALS = sdf.parseDateTime(data[1]);
						DateTime NLS =sdf.parseDateTime(data[2]);

						psumALS+=ALS.getHourOfDay();
						psumNLS+=NLS.getHourOfDay();
						psumALS+=ALS.getMinuteOfDay()%60;
						psumNLS+=NLS.getMinuteOfDay()%60;


					}

				}

				pALS=(int) psumALS;
				pNLS=(int) psumNLS;

				//max = lmax;
				bufferedReader.close();
			}
		}
		else if(id.contentEquals("Mood")|| id.contentEquals("Sleep") ||id.contentEquals("Energy") || id.contentEquals("Social") )
		{
			File file;
			dt=dt.minusMonths(1);
			Log.i(TAG," month is "+ dt.getMonthOfYear()+"  and it has "+daysOfMonth(dt.getYear(),dt.getMonthOfYear()));
			
			
			file= new File (sdcard, "sadhealth/CS/"+country+"/"+city+ "/Questionnaire.csv");
			if(file.exists())
			{
				FileReader fr= new FileReader(file);
				BufferedReader bufferedReader = new BufferedReader(fr);
				String line = null;

				String[] data;
				
					while ((line = bufferedReader.readLine()) != null)
					{
						data=line.split(",");
						
							
						if (data[1].contentEquals(String.valueOf(dt.getMonthOfYear())))
						{
								
								try {
									cdatesQuestionnaire.add(new SimpleDateFormat("MM/dd").parse(data[3]));
								} catch (ParseException e) {
		
									e.printStackTrace();
								}
								Log.i(TAG,"Here!");
								cmood.add(-Double.valueOf(data[4]));
								csleep.add(-Double.valueOf(data[5]));
								cenergy.add(-Double.valueOf(data[6]));
								csocial.add(-Double.valueOf(data[7]));
								
							}
							
					}
					
				
				bufferedReader.close();
			}
			

			getDaysinMonth(dt.getMonthOfYear(),daysOfMonth(dt.getYear(),dt.getMonthOfYear()));

			for (int i=0;i<QDays.size();i++)
			{
				file= new File (sdcard, "sadhealth/users/"+ MainActivity.USERID + "/questionnaire"+QDays.get(i)+".csv");
				if (file.exists())
				{
					FileReader fr= new FileReader(file);
					BufferedReader bufferedReader = new BufferedReader(fr);
					String line = null;

					String[] data;
					while ((line = bufferedReader.readLine()) != null) {

						data = line.split(",");
						try {
							pdatesQuestionnaire.add(new SimpleDateFormat("MM/dd").parse(data[1]));
						} catch (ParseException e) {

							e.printStackTrace();
						}
						pmood.add(-Double.valueOf(data[2]));
						psleep.add(-Double.valueOf(data[3]));
						penergy.add(-Double.valueOf(data[4]));
						psocial.add(-Double.valueOf(data[5]));
						break;
					}
					bufferedReader.close();

				}


			}
		}
	}



	private void getThisMonth(String id) throws NumberFormatException, IOException {

		DateTime dt= new DateTime();
		if(id.contentEquals("Light"))
		{

			DateTimeFormatter dtf=DateTimeFormat.forPattern("M/d/yyyy");

			//load list of GeoPoints
			File sdcard = Environment.getExternalStorageDirectory();
			Log.i(TAG, "Light-filelatest "+lightFileName );
			File file= new File (sdcard, "sadhealth/CS/"+country+"/"+city+ "/"+lightFileName+".csv");
			if(file.exists())
			{
				FileReader fr= new FileReader(file);
				BufferedReader bufferedReader = new BufferedReader(fr);

				String line = null;

				String[] data;

				while ((line = bufferedReader.readLine()) != null) {
					data = line.split(",");

					if (data[1].contentEquals(String.valueOf(dt.getMonthOfYear())))
					{
						dates.add(dtf.parseDateTime(data[3]).toDate());
						y_vals.add(Double.valueOf(data[4]));

					}
				}


				//max = lmax;
				bufferedReader.close();
			}



			dtf=DateTimeFormat.forPattern("MM/dd/yyyy");

			Log.i(TAG, "last month"+dt.getMonthOfYear());

			//Log.i(TAG, "this week"+date.toString(dtf));
			file = new File (sdcard, "sadhealth/users/"+ MainActivity.USERID + "/maxlight.csv");
			if (file.exists())
			{
				FileReader fr = null;

				fr = new FileReader(file);

				BufferedReader bufferedReader = new BufferedReader(fr);
				String line = null;

				String[] data;


				while ((line = bufferedReader.readLine()) != null) 
				{
					data = line.split(",");
					Log.i(TAG, "This-Week"+data[3]);
					if (data[1].contentEquals(String.valueOf(dt.getMonthOfYear())))
					{
						pdates.add(dtf.parseDateTime(data[3]).toDate());
						py_vals.add(Double.valueOf(data[4]));
					}
				}
				bufferedReader.close();

			}
		}
		else if(id.contentEquals("Activity"))
		{

		}
		else if(id.contentEquals("Light Distribution"))
		{
			DateTimeFormatter sdf = DateTimeFormat.forPattern("HH:mm:ss");
			DateTimeFormatter dtf=DateTimeFormat.forPattern("M/d/yyyy");



			File file = new File (sdcard, "sadhealth/CS/"+country+"/"+city+ "/Lux.csv");
			if (file.exists())
			{	
				FileReader fr= new FileReader(file);
				BufferedReader bufferedReader = new BufferedReader(fr);
				double sumALS=0;
				double sumNLS=0;
				String line = null;

				String[] data;


				while ((line = bufferedReader.readLine()) != null) {
					data = line.split(",");


					if (dtf.parseDateTime(data[0]).getMonthOfYear()==dt.getMonthOfYear())
					{

						DateTime ALS = sdf.parseDateTime(data[1]);
						DateTime NLS =sdf.parseDateTime(data[2]);

						sumALS+=ALS.getHourOfDay();
						sumNLS+=NLS.getHourOfDay();
						sumALS+=ALS.getMinuteOfDay()%60;
						sumNLS+=NLS.getMinuteOfDay()%60;


					}

				}

				cALS=(int) sumALS;
				cNLS=(int) sumNLS;
				bufferedReader.close();
			}

			dtf=DateTimeFormat.forPattern("yyyy/MM/dd");
			file = new File (sdcard, "sadhealth/users/"+ _appPrefs.getUserID()+"/lightSources.csv");
			if (file.exists())
			{
				FileReader fr= new FileReader(file);
				BufferedReader bufferedReader = new BufferedReader(fr);
				double psumALS=0;
				double psumNLS=0;
				String line = null;

				String[] data;


				while ((line = bufferedReader.readLine()) != null) {
					data = line.split(",");


					if (dtf.parseDateTime(data[0]).getMonthOfYear()==dt.getMonthOfYear())
					{

						DateTime ALS = sdf.parseDateTime(data[1]);
						DateTime NLS =sdf.parseDateTime(data[2]);

						psumALS+=ALS.getHourOfDay();
						psumNLS+=NLS.getHourOfDay();
						psumALS+=ALS.getMinuteOfDay()%60;
						psumNLS+=NLS.getMinuteOfDay()%60;


					}

				}

				pALS=(int) psumALS;
				pNLS=(int) psumNLS;

				//max = lmax;
				bufferedReader.close();
			}
		}
		else if(id.contentEquals("Mood")|| id.contentEquals("Sleep") ||id.contentEquals("Energy") || id.contentEquals("Social") )
		{

			File file;

			Log.i(TAG," month is "+ dt.getMonthOfYear()+"  and it has "+daysOfMonth(dt.getYear(),dt.getMonthOfYear()));
			
			file= new File (sdcard, "sadhealth/CS/"+country+"/"+city+ "/Questionnaire.csv");
			if(file.exists())
			{
				FileReader fr= new FileReader(file);
				BufferedReader bufferedReader = new BufferedReader(fr);
				String line = null;

				String[] data;
				
					while ((line = bufferedReader.readLine()) != null)
					{
						data=line.split(",");
						
							
						if (data[1].contentEquals(String.valueOf(dt.getMonthOfYear())))
						{
								
								try {
									cdatesQuestionnaire.add(new SimpleDateFormat("MM/dd").parse(data[3]));
								} catch (ParseException e) {
		
									e.printStackTrace();
								}
								Log.i(TAG,"Here!");
								cmood.add(-Double.valueOf(data[4]));
								csleep.add(-Double.valueOf(data[5]));
								cenergy.add(-Double.valueOf(data[6]));
								csocial.add(-Double.valueOf(data[7]));
								
							}
							
					}
					
				
				bufferedReader.close();
			}

			getDaysinMonth(dt.getMonthOfYear(),daysOfMonth(dt.getYear(),dt.getMonthOfYear()));

			for (int i=0;i<QDays.size();i++)
			{
				file= new File (sdcard, "sadhealth/users/"+ MainActivity.USERID + "/questionnaire"+QDays.get(i)+".csv");
				if (file.exists())
				{
					FileReader fr= new FileReader(file);
					BufferedReader bufferedReader = new BufferedReader(fr);
					String line = null;

					String[] data;
					while ((line = bufferedReader.readLine()) != null) {

						data = line.split(",");
						try {
							pdatesQuestionnaire.add(new SimpleDateFormat("MM/dd").parse(data[1]));
						} catch (ParseException e) {

							e.printStackTrace();
						}
						pmood.add(-Double.valueOf(data[2]));
						psleep.add(-Double.valueOf(data[3]));
						penergy.add(-Double.valueOf(data[4]));
						psocial.add(-Double.valueOf(data[5]));
						break;
					}
					bufferedReader.close();

				}


			}

		}


	}


	private void getAllTime(String id) throws NumberFormatException, IOException {
		if(id.contentEquals("Light"))
		{

			DateTimeFormatter dtf=DateTimeFormat.forPattern("M/d/yyyy");

			//load list of GeoPoints
			File sdcard = Environment.getExternalStorageDirectory();
			Log.i(TAG, "Light-filelatest "+lightFileName );
			File file= new File (sdcard, "sadhealth/CS/"+country+"/"+city+ "/"+lightFileName+".csv");
			if(file.exists())
			{
				FileReader fr= new FileReader(file);
				BufferedReader bufferedReader = new BufferedReader(fr);

				String line = null;
				String[] data;
				while ((line = bufferedReader.readLine()) != null)
				{
					data = line.split(",");
					dates.add(dtf.parseDateTime(data[3]).toDate());
					y_vals.add(Double.valueOf(data[4]));

				}
				bufferedReader.close();
			}

			dtf=DateTimeFormat.forPattern("MM/dd/yyyy");

			file = new File (sdcard, "sadhealth/users/"+ MainActivity.USERID + "/maxlight.csv");
			if (file.exists())
			{
				FileReader fr = new FileReader(file);
				BufferedReader bufferedReader = new BufferedReader(fr);
				String line = null;
				String[] data;
				while ((line = bufferedReader.readLine()) != null) 
				{
					data = line.split(",");
					pdates.add(dtf.parseDateTime(data[3]).toDate());
					py_vals.add(Double.valueOf(data[4]));

				}
				bufferedReader.close();

			}

		}
		else if(id.contentEquals("Activity"))
		{
			DateTimeFormatter dtf=DateTimeFormat.forPattern("M/d/yyyy");

			//load list of GeoPoints
			File sdcard = Environment.getExternalStorageDirectory();
			Log.i(TAG, "Light-filelatest "+lightFileName );
			File file= new File (sdcard, "sadhealth/CS/"+country+"/"+city+ "/"+communityActivityFileName+".csv");
			if(file.exists())
			{
				FileReader fr= new FileReader(file);
				BufferedReader bufferedReader = new BufferedReader(fr);

				String line = null;
				String[] data;
				while ((line = bufferedReader.readLine()) != null)
				{
					data = line.split(",");
					dates.add(dtf.parseDateTime(data[0]).toDate());
					y_vals.add(Double.valueOf(data[1]));

				}
				bufferedReader.close();
			}

			/*dtf=DateTimeFormat.forPattern("MM/dd/yyyy");

			file = new File (sdcard, "sadhealth/users/"+ MainActivity.USERID + "/maxlight.csv");
			if (file.exists())
			{
				FileReader fr = new FileReader(file);
				BufferedReader bufferedReader = new BufferedReader(fr);
				String line = null;
				String[] data;
				while ((line = bufferedReader.readLine()) != null) 
				{
					data = line.split(",");
					pdates.add(dtf.parseDateTime(data[3]).toDate());
					py_vals.add(Double.valueOf(data[4]));

				}
				bufferedReader.close();

			}*/
		}
		else if(id.contentEquals("Light Distribution"))
		{
			DateTimeFormatter sdf = DateTimeFormat.forPattern("HH:mm:ss");
			DateTimeFormatter dtf=DateTimeFormat.forPattern("M/d/yyyy");



			File file = new File (sdcard, "sadhealth/CS/"+country+"/"+city+ "/Lux.csv");
			if (file.exists())
			{	
				FileReader fr= new FileReader(file);
				BufferedReader bufferedReader = new BufferedReader(fr);
				double sumALS=0;
				double sumNLS=0;
				String line = null;

				String[] data;


				while ((line = bufferedReader.readLine()) != null)
				{
					data = line.split(",");

					DateTime ALS = sdf.parseDateTime(data[1]);
					DateTime NLS =sdf.parseDateTime(data[2]);

					sumALS+=ALS.getHourOfDay();
					sumNLS+=NLS.getHourOfDay();
					sumALS+=ALS.getMinuteOfDay()%60;
					sumNLS+=NLS.getMinuteOfDay()%60;
				}

				cALS=(int) sumALS;
				cNLS=(int) sumNLS;
				bufferedReader.close();
			}

			dtf=DateTimeFormat.forPattern("yyyy/MM/dd");
			file = new File (sdcard, "sadhealth/users/"+ _appPrefs.getUserID()+"/lightSources.csv");
			if (file.exists())
			{
				FileReader fr= new FileReader(file);
				BufferedReader bufferedReader = new BufferedReader(fr);
				double psumALS=0;
				double psumNLS=0;
				String line = null;

				String[] data;

				while ((line = bufferedReader.readLine()) != null) 
				{
					data = line.split(",");

					DateTime ALS = sdf.parseDateTime(data[1]);
					DateTime NLS =sdf.parseDateTime(data[2]);

					psumALS+=ALS.getHourOfDay();
					psumNLS+=NLS.getHourOfDay();
					psumALS+=ALS.getMinuteOfDay()%60;
					psumNLS+=NLS.getMinuteOfDay()%60;

				}

				pALS=(int) psumALS;
				pNLS=(int) psumNLS;

				//max = lmax;
				bufferedReader.close();
			}
		}
		else if(id.contentEquals("Mood")|| id.contentEquals("Sleep") ||id.contentEquals("Energy") || id.contentEquals("Social") )
		{
			File f = null;
			
			f= new File (sdcard, "sadhealth/CS/"+country+"/"+city+ "/Questionnaire.csv");
			if(f.exists())
			{
				FileReader fr= new FileReader(f);
				BufferedReader bufferedReader = new BufferedReader(fr);
				String line = null;

				String[] data;
				
					while ((line = bufferedReader.readLine()) != null)
					{
						data=line.split(",");
						
							
								try {
									cdatesQuestionnaire.add(new SimpleDateFormat("MM/dd").parse(data[3]));
								} catch (ParseException e) {
		
									e.printStackTrace();
								}
								Log.i(TAG,"Here!");
								cmood.add(-Double.valueOf(data[4]));
								csleep.add(-Double.valueOf(data[5]));
								cenergy.add(-Double.valueOf(data[6]));
								csocial.add(-Double.valueOf(data[7]));
								
							}
							
					
					
				
				bufferedReader.close();
			}

			File[] paths;

			try{      
				// create new file
				f = new File(sdcard,"/sadhealth/users/"+ _appPrefs.getUserID()+"/");

				// create new filename filter
				FilenameFilter fileNameFilter = new FilenameFilter() {

					@Override
					public boolean accept(File dir, String name) {

						return name.contains("questionnaire");
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
						try {
							pdatesQuestionnaire.add(new SimpleDateFormat("MM/dd").parse(data[1]));
						} catch (ParseException e) {

							e.printStackTrace();
						}
						pmood.add(-Double.valueOf(data[2]));
						psleep.add(-Double.valueOf(data[3]));
						penergy.add(-Double.valueOf(data[4]));
						psocial.add(-Double.valueOf(data[5]));
						break;
					}
					bufferedReader.close();
				}

			}catch(Exception e){
				// if any error occurs
				e.printStackTrace();
			}
		}


	}



	public void initGraph(String id)
	{
		if (graphScreen.getChildCount()>0)
		{
			graphScreen.removeAllViews();
		}
		if(id.contentEquals("Light"))
		{
			lightGraph();
		}
		else if(id.contentEquals("Activity"))
		{
			activityGraph();
		}
		else if(id.contentEquals("Light Distribution"))
		{
			lightSourceGraph();
		}
		else if(id.contentEquals("Mood"))
		{
			questionnareGraph(R.id.MoodGraph);
		}
		else if(id.contentEquals("Sleep"))
		{
			questionnareGraph(R.id.SleepGraph);
		}
		else if(id.contentEquals("Energy"))
		{
			questionnareGraph(R.id.EnergyGraph);
		}
		else if(id.contentEquals("Social"))
		{
			questionnareGraph(R.id.SocialGraph);
		}

	}

	public void initGranularity(String id)
	{
		Log.i(TAG, "number of children in the view "+ String.valueOf(graphScreen.getChildCount()));
		if (graphScreen.getChildCount()>0)
		{
			graphScreen.removeAllViews();
		}
		if(id.contentEquals("Yesterday"))
		{
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

		switch(graphSpinner.getSelectedItemPosition())
		{
		case 0:			
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

		}

	}


	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int pos,
			long id) {

		Spinner spinner = (Spinner) parent;
		if(spinner.getId() == R.id.PGraphs_spinner)
		{
			_appPrefs.setGraphSpinnerSelectionCommunity(parent.getItemAtPosition(pos).toString());
			initGraph(parent.getItemAtPosition(pos).toString());

		}
		else if(spinner.getId() == R.id.PGraphsGranularity_spinner)
		{
			_appPrefs.setGranularitySpinnerSelectionCommunity(parent.getItemAtPosition(pos).toString());
			initGranularity(parent.getItemAtPosition(pos).toString());
		}

	}

	@Override
	public void onNothingSelected(AdapterView<?> parent) {
	}


	public static int daysOfMonth(int year, int month) {
		DateTime dateTime = new DateTime(year, month, 14, 12,0,0);
		return dateTime.dayOfMonth().getMaximumValue();
	}

	public  void getDaysinMonth(int month,int days)
	{

		String tmp=String.format("%02d", month);
		for(int i=1;i<=days;i++)
		{
			tmp=tmp+String.format("%02d",i);
			QDays.add(tmp);
			tmp=String.format("%02d", month);
		}


	}













}