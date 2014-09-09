package uu.core.sadhealth.fragments;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.OutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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
import org.achartengine.chart.BarChart.Type;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Paint.Align;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.View.OnClickListener;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;


import uu.core.sadhealth.services.MainService;
import uu.core.sadhealth.services.UploadService;
import uu.core.sadhealth.utils.AppPreferences;
import uu.core.sadhealth.MainActivity;
import uu.core.sadhealth.R;
import uu.core.sadhealth.R.id;
import uu.core.sadhealth.R.layout;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GraphView.GraphViewData;
import com.jjoe64.graphview.GraphViewSeries;
import com.jjoe64.graphview.LineGraphView;

public class PersonalSectionFragment extends Fragment implements  OnItemSelectedListener, OnClickListener  {
	
	
	private final String TAG = "GraphActivity";
	private AppPreferences _appPrefs;
	static File sdcard = Environment.getExternalStorageDirectory();
	static String lightFileName = "phone_lightsensor";
	static String activityFileName = "phone_accelerometer";
	static String activityRecFileName="phone_activityRec";
	static String phoneUnlockFileName = "unlock_no";
	static String lightSourcesFileName="lightSources";
	static String questionnareFileName="questionnaire";
	static long min,max;
	static double maxActivityValue=0;
	private int periodNoInt;
	private List<Long> dates =new ArrayList<Long>();
	private List<Date> pdates =new ArrayList<Date>();
	private List<Date> datesQuestionnaire =new ArrayList<Date>();
	private List<String> QDays =new ArrayList<String>();
	private List<Double> y_vals =new ArrayList<Double>();
	private List<Double> converted_y =new ArrayList<Double>();
	private List<Double> py_vals =new ArrayList<Double>();
	private List<Double> pconverted_y =new ArrayList<Double>();
	private int[] activityCounts=new int[6];
	private int ALS,NLS;
	private List<Double> mood  =new ArrayList<Double>();
    private List<Double> sleep =new ArrayList<Double>();
    private List<Double> energy = new ArrayList<Double>();
    private List<Double> social =new ArrayList<Double>();
    private String[] moodConditions= new String[]{"Very happy","Happy","Down","Very Down"};
	private String[] sleepConditions=new String[]{"Very Good","Good","Bad","Very Bad"};
	private String[] energyConditions =new String[]{"Very Active","Active","Tired","Very Tired"};
	private String[] socialConditions=new String[]{"Very","Quite","a little","Not at all"};
	ImageButton uploadButton,refreshButton,exitButton,serviceImageButton;
	Button shareButton;
	private Spinner graphSpinner, granularitySpinner;

	GraphView graphView1,graphView2;
	
	LinearLayout  wholeLayout,graphScreen,menuButtons;
	TextView unlockNoview, dataViewTime;
	ListView listView;
	RadioGroup graphGroup;
	static boolean isLatestData = true;
	private String selectedGraph;
	private String periodNo;
	
	private GraphicalView mChart;
	ActionBar actionBar;
	private int granularity=1;
	
	static MenuItem uploadMenuBtn, refreshMenuBtn;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		try
		{
			//setHasOptionsMenu(true);
			return inflater.inflate(R.layout.activity_graph, container, false);
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
		selectedGraph = _appPrefs.getGraphSpinnerSelection();
		actionBar=getActivity().getActionBar();
		actionBar.setTitle(_appPrefs.getUserID());
		//uploadButton=(ImageButton) actionBar.getCustomView().findViewById(R.id.UploadMenu);
		wholeLayout =(LinearLayout) getView().findViewById(R.id.GraphScreenLayout);
		graphScreen = (LinearLayout) getView().findViewById(R.id.Graph);
		menuButtons =(LinearLayout)getView().findViewById(R.id.MenuButtons);
		shareButton =(Button) getView().findViewById(R.id.shareBtn);
		//graphGroup = (RadioGroup) getView().findViewById(R.id.Graphchoices);
		
		dataViewTime = (TextView) getView().findViewById(R.id.DataViewTime);
		graphSpinner=(Spinner) getView().findViewById(R.id.PGraphs_spinner);
		granularitySpinner=(Spinner)getView().findViewById(R.id.PGraphsGranularity_spinner);
		
		graphSpinner.setOnItemSelectedListener(this);
		granularitySpinner.setOnItemSelectedListener(this);
		 shareButton.setOnClickListener(this);
		Log.i(TAG,"started");
	
		phoneActivity();
		
	}

	
	public void lightGraph(){
		

		String[] conditions={"Pitch Black", "Very Dark", "Dark Indoors", "Dim Indoors", "Normal Indoors", "Bright Indoors", "Dim Outdoors", "Cloudy Outdoors", "Direct Sunlight"};
		// Creating TimeSeries for Light
        TimeSeries visitsSeries = new TimeSeries("Light");
     // Creating a dataset to hold each series
        XYMultipleSeriesDataset dataset = new XYMultipleSeriesDataset();
		
		DateTime now=new DateTime();
		dates =new ArrayList<Long>();
		y_vals =new ArrayList<Double>();
		converted_y =new ArrayList<Double>();
		pdates =new ArrayList<Date>();
		py_vals =new ArrayList<Double>();
		pconverted_y =new ArrayList<Double>();
		periodNoInt=_appPrefs.getPeriodNo();
		periodNo=String.format("%04d", periodNoInt);
		
		
		   
	     // Creating XYSeriesRenderer to customize visitsSeries
	        XYSeriesRenderer visitsRenderer = new XYSeriesRenderer();
	        visitsRenderer.setColor(Color.WHITE);
	        visitsRenderer.setPointStyle(PointStyle.CIRCLE);
	        visitsRenderer.setFillPoints(true);
	        visitsRenderer.setLineWidth(2);
	        //visitsRenderer.setDisplayChartValues(true);
	        
	     // Creating a XYMultipleSeriesRenderer to customize the whole chart
	        XYMultipleSeriesRenderer multiRenderer = new XYMultipleSeriesRenderer();
	 
	        multiRenderer.setChartTitle("LIGHT");
	        multiRenderer.setXTitle("Date/Time");
	        //multiRenderer.setYTitle("Lux");
	        multiRenderer.setZoomButtonsVisible(false);
	        multiRenderer.setPanEnabled(false, false);
	        multiRenderer.setZoomEnabled(false,false);
	        multiRenderer.setYLabelsPadding(10);
	        Calendar c = Calendar.getInstance(); 
	        multiRenderer.setXAxisMax(c.getTime().getTime());
	 
	        // Adding visitsRenderer and viewsRenderer to multipleRenderer
	        // Note: The order of adding dataseries to dataset and renderers to multipleRenderer
	        // should be same
	        multiRenderer.addSeriesRenderer(visitsRenderer);
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
	        //multiRenderer.setXAxisMin(min);
	        //multiRenderer.setXAxisMax();
		
		
		switch(granularity)
		{
		case 0: //Yesterday
			Log.i(TAG,"Yesterday selected");
			lightFileName = "phone_lightsensor" + String.format("%04d", (periodNoInt-1));
			Log.i(TAG,"Yesterday light file name"+ lightFileName);
			try {
		    	
		    	//load list of GeoPoints
			    File sdcard = Environment.getExternalStorageDirectory();
			   // Log.i(TAG, "Light-filelatest "+lightFileName );
			    File file= new File (sdcard, "sadhealth/users/"+ _appPrefs.getUserID()+ "/"+lightFileName+".csv");		
			    getData(file);
			    
			    	
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			
			for (int i=0;i<y_vals.toArray().length;i++)
	        {
	        		//Log.i(TAG,"y-val " +y_vals.get(i).intValue()+" "+i);
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
	        Log.i(TAG, "convertedY-size" +String.valueOf(converted_y.size()));
	        for(int i=0;i<dates.size();i++)
			{
				visitsSeries.add(new Date(dates.get(i)),converted_y.get(i));
			}
	        
	     // Adding Visits Series to the dataset
	        dataset.addSeries(visitsSeries);
	       
			
	        mChart=(GraphicalView)ChartFactory.getTimeChartView(getActivity(), dataset, multiRenderer, "HH:mm");
			break;
		case 1: //Today
			lightFileName="phone_lightsensor"+periodNo;
			try {
		    	
		    	//load list of GeoPoints
			    File sdcard = Environment.getExternalStorageDirectory();
			   // Log.i(TAG, "Light-filelatest "+lightFileName );
			    File file= new File (sdcard, "sadhealth/users/"+ _appPrefs.getUserID()+ "/"+lightFileName+".csv");		
			    getData(file);
			    
			    	
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			for (int i=0;i<y_vals.toArray().length;i++)
	        {
	        		//Log.i(TAG,"y-val " +y_vals.get(i).intValue()+" "+i);
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
	        Log.i(TAG, "convertedY-size" +String.valueOf(converted_y.size()));
	        for(int i=0;i<dates.size();i++)
			{
				visitsSeries.add(new Date(dates.get(i)),converted_y.get(i));
			}
	        
	     // Adding Visits Series to the dataset
	        dataset.addSeries(visitsSeries);
	       
	        
	        mChart=(GraphicalView)ChartFactory.getTimeChartView(getActivity(), dataset, multiRenderer, "HH:mm");
			break;
		case 2://Last Week
			try {
				getLastWeek("Light");
			} catch (NumberFormatException e1) {
				
				e1.printStackTrace();
			} catch (IOException e1) {
				
				e1.printStackTrace();
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
	        	visitsSeries.add(pdates.get(i),pconverted_y.get(i));
			}
        	
        	// Adding Visits Series to the dataset
            dataset.addSeries(visitsSeries);
           //DateTime dt = new DateTime(2014, 2, 23, 0, 0);
			
           
            //multiRenderer.setXAxisMax(dt.toDate().getTime());
			 mChart=(GraphicalView)ChartFactory.getTimeChartView(getActivity(), dataset, multiRenderer, "MM/dd");
			break;
		case 3://This Week
			try {
				getThisWeek("Light");
			} catch (IOException e) {
				
				e.printStackTrace();
			} catch (ParseException e) {
				
				e.printStackTrace();
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
	        	visitsSeries.add(pdates.get(i),pconverted_y.get(i));
			}
        	
        	// Adding Visits Series to the dataset
            dataset.addSeries(visitsSeries);
           //DateTime dtr = new DateTime(2014, 2, 23, 0, 0);
			
           
            //multiRenderer.setXAxisMax(dtr.toDate().getTime());
			 mChart=(GraphicalView)ChartFactory.getTimeChartView(getActivity(), dataset, multiRenderer, "MM/dd");
			break;
		case 4://Last Month
			try {
				getLastMonth("Light");
			} catch (NumberFormatException e1) {
				
				e1.printStackTrace();
			} catch (IOException e1) {
				
				e1.printStackTrace();
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
	        	visitsSeries.add(pdates.get(i),pconverted_y.get(i));
			}
        	
        	// Adding Visits Series to the dataset
            dataset.addSeries(visitsSeries);
           
        	
        	 mChart=(GraphicalView)ChartFactory.getTimeChartView(getActivity(), dataset, multiRenderer, "MM/dd");
			break;
		case 5://This Month
			try {
				getThisMonth("Light");
			} catch (NumberFormatException e) {
				
				e.printStackTrace();
			} catch (IOException e) {
				
				e.printStackTrace();
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
	        	visitsSeries.add(pdates.get(i),pconverted_y.get(i));
			}
        	
        	// Adding Visits Series to the dataset
            dataset.addSeries(visitsSeries);
           
        	
        	 mChart=(GraphicalView)ChartFactory.getTimeChartView(getActivity(), dataset, multiRenderer, "MM/dd");
			break;
		case 6://All Time
			try {
				getAllTime("Light");
			} catch (NumberFormatException e) {
				
				e.printStackTrace();
			} catch (IOException e) {
				
				e.printStackTrace();
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
        	
        	for(int i=0;i<pdates.size()-1;i++)
			{
	        	visitsSeries.add(pdates.get(i),pconverted_y.get(i));
			}
        	
        	// Adding Visits Series to the dataset
            dataset.addSeries(visitsSeries);
           
        	
        	 mChart=(GraphicalView)ChartFactory.getTimeChartView(getActivity(), dataset, multiRenderer, "MM/dd");
        	 
			break;
		}
	   
	    
	     graphScreen.addView(mChart);
	  

	}
	
	
	
	public void activityGraph(){

		String[] conditions={"not active","active","very active"};
		// Creating TimeSeries for Light
        TimeSeries visitsSeries = new TimeSeries("Activity");
     // Creating a dataset to hold each series
        XYMultipleSeriesDataset dataset = new XYMultipleSeriesDataset();
		
		DateTime now=new DateTime();
		dates =new ArrayList<Long>();
		y_vals =new ArrayList<Double>();
		converted_y =new ArrayList<Double>();
		pdates =new ArrayList<Date>();
		py_vals =new ArrayList<Double>();
		pconverted_y =new ArrayList<Double>();
		periodNoInt=_appPrefs.getPeriodNo();
		periodNo=String.format("%04d", periodNoInt);
		
		
		   
	     // Creating XYSeriesRenderer to customize visitsSeries
	        XYSeriesRenderer visitsRenderer = new XYSeriesRenderer();
	        visitsRenderer.setColor(Color.WHITE);
	        visitsRenderer.setPointStyle(PointStyle.CIRCLE);
	        visitsRenderer.setFillPoints(true);
	        visitsRenderer.setLineWidth(2);
	        //visitsRenderer.setDisplayChartValues(true);
	        
	     // Creating a XYMultipleSeriesRenderer to customize the whole chart
	        XYMultipleSeriesRenderer multiRenderer = new XYMultipleSeriesRenderer();
	 
	        multiRenderer.setChartTitle("PHYSICAL ACTIVITY");
	        multiRenderer.setXTitle("Date/Time");
	        multiRenderer.setYTitle("Magnitude");
	        //multiRenderer.setYTitle("Lux");
	        multiRenderer.setZoomButtonsVisible(false);
	        multiRenderer.setPanEnabled(false, false);
	        multiRenderer.setZoomEnabled(false,false);
	        multiRenderer.setYLabelsPadding(10);
	        Calendar c = Calendar.getInstance(); 
	        multiRenderer.setXAxisMax(c.getTime().getTime());
	 
	        // Adding visitsRenderer and viewsRenderer to multipleRenderer
	        // Note: The order of adding dataseries to dataset and renderers to multipleRenderer
	        // should be same
	        multiRenderer.addSeriesRenderer(visitsRenderer);
	        //multiRenderer.setYAxisMax(4);
	        //multiRenderer.setYLabels(0);
	        
	        
	 
	        multiRenderer.setApplyBackgroundColor(false);
	        multiRenderer.setMarginsColor(Color.argb(0, 255, 255, 255));
	        multiRenderer.setLabelsTextSize(18);
	        multiRenderer.setShowGrid(true);
	        multiRenderer.setMargins(new int[] {30, 100, 10, 0});
	        multiRenderer.setYLabelsAlign(Align.RIGHT);
	        multiRenderer.setYLabelsAngle(-45);
	        //multiRenderer.setXAxisMin(min);
	        //multiRenderer.setXAxisMax();
		
		
		switch(granularity)
		{
		case 0: //Yesterday
			activityRecFileName = "phone_accelerometer" + String.format("%04d", (periodNoInt -1));
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
	       
			
	        mChart=(GraphicalView)ChartFactory.getTimeChartView(getActivity(), dataset, multiRenderer, "HH:mm");
			break;
		case 1: //Today
			
			activityRecFileName="phone_accelerometer"+periodNo;
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
	       
			
	        mChart=(GraphicalView)ChartFactory.getTimeChartView(getActivity(), dataset, multiRenderer, "HH:mm");
			
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
			break;
		}
		
		
		
		
	    graphScreen.addView(mChart);
    
        
		

	}

	

	public void activityGraph2(){
		maxActivityValue=0;
		
		switch(granularity)
		{
		case 0: //Yesterday
			activityRecFileName = "phone_activityRec" + String.format("%04d", (periodNoInt -1));
			break;
		case 1: //Today
			activityRecFileName="phone_activityRec"+periodNo;
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
			break;
		}
		
		
		for (int i=0;i<6;i++)
		{
			activityCounts[i]=0;
		}
		
		
		try {
	    	
	    	//load list of GeoPoints
		    File sdcard = Environment.getExternalStorageDirectory();
		   // Log.i(TAG, "Light-filelatest "+lightFileName );
		    File file= new File (sdcard, "sadhealth/users/"+ _appPrefs.getUserID()+ "/"+activityRecFileName+".csv");		
		    getActivityRecognitionData(file);
		    
		    	
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		String[] conditions={"In Vehicle","On Bike","On Foot","Still","Unknown","Tilting"};
		
		// Creating an  XYSeries for Income
        XYSeries counts = new XYSeries("activities");
        
        
        for(int i=0;i<activityCounts.length;i++){
        	if(activityCounts[i]>maxActivityValue)
        	{
        		maxActivityValue=activityCounts[i];
        	}
            counts.add(i,activityCounts[i]);
           
        }
 
        XYMultipleSeriesDataset dataset = new XYMultipleSeriesDataset();  // collection of series under one object.,there could any
        dataset.addSeries(counts);                            // number of series
        
        //customization of the chart
    
        XYSeriesRenderer renderer = new XYSeriesRenderer();     // one renderer for one series
        renderer.setColor(Color.RED);
        renderer.setDisplayChartValues(false);
        //renderer.setChartValuesSpacing((float) 0.2d);
        //renderer.setLineWidth((float) 0.5d);
        

        XYMultipleSeriesRenderer multiRenderer = new XYMultipleSeriesRenderer();   // collection multiple values for one renderer or series
        multiRenderer.addSeriesRenderer(renderer);
        multiRenderer.setChartTitle("PHYSICAL ACTIVITY");
        //multiRenderer.setXTitle("xValues");
        multiRenderer.setYTitle("Count");
        multiRenderer.setZoomButtonsVisible(false);    
        multiRenderer.setShowLegend(false);
        multiRenderer.setShowGridX(true);      // this will show the grid in  graph
        multiRenderer.setShowGridY(true);     
        multiRenderer.setBarWidth((float)2d);
        //multiRenderer.setAntialiasing(true);
        multiRenderer.setBarSpacing(-50);   // adding spacing between the line or stacks
        multiRenderer.setApplyBackgroundColor(true);
        multiRenderer.setBackgroundColor(Color.BLACK);
        multiRenderer.setXAxisMin(-0.5);
        //multiRenderer.setYAxisMin(.5);
        multiRenderer.setXAxisMax(5);
        multiRenderer.setYAxisMin(0);
        multiRenderer.setYAxisMax(maxActivityValue);
        multiRenderer.setLabelsTextSize(18);
    
        multiRenderer.setXLabels(0);
          for(int i=0; i< activityCounts.length;i++){
            multiRenderer.addXTextLabel(i, conditions[i]);
        }
 
       
        multiRenderer.setApplyBackgroundColor(false);
        multiRenderer.setMarginsColor(Color.argb(0, 255, 255, 255));
        multiRenderer.setPanEnabled(false, false);
        multiRenderer.setZoomEnabled(false,false);
        mChart=(GraphicalView)ChartFactory.getBarChartView(getActivity(), dataset, multiRenderer, Type.DEFAULT);
	    graphScreen.addView(mChart);
    
        
		

	}
	
	public void lightSourceGraph()
	{
		// Pie Chart Section Names
        String[] pieChartNames = new String[] {"Artificial Light", "Natural Light"};
     // Color of each Pie Chart Sections
        int[] colors = { Color.BLUE, Color.GREEN};
        
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
			Log.i(TAG,"XD-today "+ ALS +" "+NLS);
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
		
		
		
		Log.i(TAG,"XD-final "+ ALS +" "+NLS);
        // Pie Chart Section Value
        double[] distribution = { ALS, NLS } ;
 
        
 
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

        
		
		
		datesQuestionnaire =new ArrayList<Date>();
		QDays =new ArrayList<String>();
		
		mood  =new ArrayList<Double>();
	    sleep =new ArrayList<Double>();
	    energy = new ArrayList<Double>();
	    social =new ArrayList<Double>();
	 
	    switch (id)
	    {
	    case R.id.MoodGraph:
	    	multiRenderer.setChartTitle("MOOD");
	    	//Creating an  XYSeries for Mood
	        TimeSeries moodSeries = new TimeSeries("Mood");
	     
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
			        for(int i=0;i<datesQuestionnaire.toArray().length;i++){
			            moodSeries.add(datesQuestionnaire.get(i), mood.get(i));
			        }
			     // Adding Income Series to the dataset
			        dataset.addSeries(moodSeries);
			        
			     // Creating XYSeriesRenderer to customize incomeSeries
			        XYSeriesRenderer moodRenderer = new XYSeriesRenderer();
			        moodRenderer.setColor(Color.WHITE);
			        moodRenderer.setPointStyle(PointStyle.CIRCLE);
			        moodRenderer.setFillPoints(true);
			        moodRenderer.setLineWidth(2);
			        
			        
				
				
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
		        
		        
		        multiRenderer.addSeriesRenderer(moodRenderer);
		        
		        // Creating a Line Chart
		        mChart = ChartFactory.getTimeChartView(getActivity(), dataset, multiRenderer, "MM/dd");
				
				break;
			
	    case R.id.SleepGraph:
	    	//Creating an  XYSeries for Sleep
	        TimeSeries sleepSeries = new TimeSeries("Sleep");
	        
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
	        for(int i=0;i<datesQuestionnaire.toArray().length;i++){
	            sleepSeries.add(datesQuestionnaire.get(i),sleep.get(i));
	        }
	     // Adding Expense Series to dataset
	        dataset.addSeries(sleepSeries);
	        // Creating XYSeriesRenderer to customize expenseSeries
	        XYSeriesRenderer sleepRenderer = new XYSeriesRenderer();
	        sleepRenderer.setColor(Color.YELLOW);
	        sleepRenderer.setPointStyle(PointStyle.CIRCLE);
	        sleepRenderer.setFillPoints(true);
	        sleepRenderer.setLineWidth(2);
	        
	        
	        multiRenderer.setChartTitle("SLEEP");
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
	        multiRenderer.addSeriesRenderer(sleepRenderer);
	        
	        // Creating a Line Chart
	        mChart = ChartFactory.getTimeChartView(getActivity(), dataset, multiRenderer, "MM/dd");
	        
	    	break;
	    case R.id.EnergyGraph:
	    	//Creating an XYSeries for Energy
	        TimeSeries energySeries =new TimeSeries("Energy");
	        
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
	        for(int i=0;i<datesQuestionnaire.toArray().length;i++){
	            energySeries.add(datesQuestionnaire.get(i), energy.get(i));
	        }
	     // Adding the energy Series
	        dataset.addSeries(energySeries);
	     // Creating XYSeriesRenderer to customize incomeSeries
	        XYSeriesRenderer energyRenderer = new XYSeriesRenderer();
	        energyRenderer.setColor(Color.RED);
	        energyRenderer.setPointStyle(PointStyle.CIRCLE);
	        energyRenderer.setFillPoints(true);
	        energyRenderer.setLineWidth(2);
	        
	        
	        multiRenderer.setChartTitle("ENERGY");
	        
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
	        
	        multiRenderer.addSeriesRenderer(energyRenderer);
	        // Creating a Line Chart
	        mChart = ChartFactory.getTimeChartView(getActivity(), dataset, multiRenderer, "MM/dd");
	    	break;
	    case R.id.SocialGraph:
	        multiRenderer.setChartTitle("SOCIAL");
	    	//Creating an XYSeries for Social
	        TimeSeries socialSeries =new TimeSeries("Social");
	     // Creating XYSeriesRenderer to customize expenseSeries
	        XYSeriesRenderer socialRenderer = new XYSeriesRenderer();
	        socialRenderer.setColor(Color.BLUE);
	        socialRenderer.setPointStyle(PointStyle.CIRCLE);
	        socialRenderer.setFillPoints(true);
	        socialRenderer.setLineWidth(2);
	        
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
	        for(int i=0;i<datesQuestionnaire.toArray().length;i++){
	            socialSeries.add(datesQuestionnaire.get(i),social.get(i));
	        }
	        //Adding the social Series
	        dataset.addSeries(socialSeries);
	     
	        
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
	        multiRenderer.addSeriesRenderer(socialRenderer);
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
		    File file= new File (sdcard, "sadhealth/users/"+ _appPrefs.getUserID() + "/"+phoneUnlockFileName+periodNo+".csv");		
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
	
	public void getData (File fn) throws IOException
	{
		FileReader fr= new FileReader(fn);
		BufferedReader bufferedReader = new BufferedReader(fr);
		
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
            dates.add(Long.valueOf(data[0]));
            y_vals.add(Double.valueOf(data[1]));
            
           
           
        }
        //max = lmax;
        bufferedReader.close();
        
	}
	
	public void getActivityRecognitionData (File fn) throws IOException
	{
		FileReader fr= new FileReader(fn);
		BufferedReader bufferedReader = new BufferedReader(fr);
		
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
            dates.add(Long.valueOf(data[0]));
           
            switch(Integer.valueOf(data[4]))
            {
            	case 0:
            		activityCounts[0]+=1;
            	break;
            	case 1:
            		activityCounts[1]+=1;
            	break;
            	case 2:
            		activityCounts[2]+=1;
            	break;
            	case 3:
            		activityCounts[3]+=1;
            	break;
            	case 4:
            		activityCounts[4]+=1;
            	break;
            	case 5:
            		activityCounts[5]+=1;
            	break;
            		
            }
           
        }
        //max = lmax;
        bufferedReader.close();
        
	}
	
	public void getActivityTimerData (File fn) throws IOException
	{
		FileReader fr= new FileReader(fn);
		BufferedReader bufferedReader = new BufferedReader(fr);
		
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
            dates.add(Long.valueOf(data[0]));
            y_vals.add(Double.valueOf(data[1]));
           
        }
        //max = lmax;
        bufferedReader.close();
        
	}
	
	
	
	private void getYesterday(String id) throws IOException {
		DateTime dt= new DateTime();
		if(id.contentEquals("Light"))
		{
			
		}
		else if(id.contentEquals("Activity"))
		{
			
		}
		else if(id.contentEquals("Light Distribution"))
		{
			DateTimeFormatter sdf = DateTimeFormat.forPattern("HH:mm:ss");
			DateTimeFormatter dtf=DateTimeFormat.forPattern("yyyy/MM/dd");
			dt=dt.minusDays(1);
			File fn = new File (sdcard, "sadhealth/users/"+ _appPrefs.getUserID()+"/lightSources.csv");
			FileReader fr= new FileReader(fn);
			BufferedReader bufferedReader = new BufferedReader(fr);
			double sumALS=0;
			double sumNLS=0;
			String line = null;
	       
	        String[] data;
	        while ((line = bufferedReader.readLine()) != null) {
	        	data = line.split(",");
	        	
	        	if (dtf.parseDateTime(data[0]).getDayOfMonth()==dt.getDayOfMonth())
				{
	        	
	            DateTime ALS = sdf.parseDateTime(data[1]);
				DateTime NLS =sdf.parseDateTime(data[2]);
				
				sumALS=ALS.getHourOfDay();
				sumNLS=NLS.getHourOfDay();
				//sumALS+=ALS.getMinuteOfDay()%60;
				//sumNLS+=NLS.getMinuteOfDay()%60;
				}
	           
	        }
	        
	        ALS=(int) sumALS;
	        NLS=(int) sumNLS;
	        Log.i(TAG,"XD-Yesterday "+ ALS);
	        //max = lmax;
	        bufferedReader.close();
		}
		else if(id.contentEquals("Mood")|| id.contentEquals("Sleep") ||id.contentEquals("Energy") || id.contentEquals("Social") )
		{
			dt=dt.minusDays(1);
			File file;
			
			Log.i(TAG," day is "+ dt.getDayOfMonth());
			
			file= new File (sdcard, "sadhealth/users/"+ MainActivity.USERID + "/questionnaire"+String.format("%02d", dt.getMonthOfYear())+String.format("%02d", dt.getDayOfMonth())+".csv");
					if (file.exists())
					{
						FileReader fr= new FileReader(file);
						BufferedReader bufferedReader = new BufferedReader(fr);
						String line = null;
				        
				        String[] data;
				        while ((line = bufferedReader.readLine()) != null) {
				       
				        data = line.split(",");
				        try {
							datesQuestionnaire.add(new SimpleDateFormat("MM/dd").parse(data[1]));
						} catch (ParseException e) {
							
							e.printStackTrace();
						}
				        mood.add(-Double.valueOf(data[2]));
				        sleep.add(-Double.valueOf(data[3]));
				        energy.add(-Double.valueOf(data[4]));
				        social.add(-Double.valueOf(data[5]));
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
			double sumALS=0;
			double sumNLS=0;
			String line = null;
	        
	        String[] data;
	        while ((line = bufferedReader.readLine()) != null) {
	        	data = line.split(",");
	        	Log.i(TAG,"XD- "+dtf.parseDateTime(data[0]).getDayOfMonth()+" "+dt.getDayOfMonth());
	        	if (dtf.parseDateTime(data[0]).getDayOfMonth()==dt.getDayOfMonth())
				{
	        		
	        		DateTime ALS = sdf.parseDateTime(data[1]);
	        		DateTime NLS =sdf.parseDateTime(data[2]);
				
	        		sumALS=ALS.getHourOfDay();
	        		Log.i(TAG,"XD-today found " + sumALS+" "+ALS.getHourOfDay());
	        		sumNLS=NLS.getHourOfDay();
	        		//sumALS+=ALS.getMinuteOfDay()%60;
	        		//sumNLS+=NLS.getMinuteOfDay()%60;
	        		Log.i(TAG,"XD-today found " + sumALS);
				}
	           
	        }
	       
	        ALS=(int) sumALS;
	        NLS=(int) sumNLS;
	        Log.i(TAG,"XD-today after " + ALS);
	        //max = lmax;
	        bufferedReader.close();
		}
		else if(id.contentEquals("Mood")|| id.contentEquals("Sleep") ||id.contentEquals("Energy") || id.contentEquals("Social") )
		{
			
			File file;
			
			Log.i(TAG," day is "+ dt.getDayOfMonth());
			
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
							datesQuestionnaire.add(new SimpleDateFormat("MM/dd").parse(data[1]));
						} catch (ParseException e) {
							
							e.printStackTrace();
						}
				        
				   mood.add(-Double.valueOf(data[2]));
				   sleep.add(-Double.valueOf(data[3]));
				   energy.add(-Double.valueOf(data[4]));
				   social.add(-Double.valueOf(data[5]));
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
			DateTimeFormatter dtf=DateTimeFormat.forPattern("MM/dd/yyyy");
			LocalDate startOfWeek = dateTime.minusDays(dateTime.dayOfWeek().get() - 1);
			LocalDate endOfWeek = dateTime.plusDays(7 - dateTime.dayOfWeek().get());
			
			startOfWeek=startOfWeek.minusWeeks(1);
			endOfWeek=endOfWeek.minusWeeks(1);
			
			Log.i(TAG, "last week"+startOfWeek.toString(dtf)+" to "+endOfWeek.toString(dtf));
				File file = new File (sdcard, "sadhealth/users/"+ MainActivity.USERID + "/maxlight.csv");
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
			DateTimeFormatter dtf=DateTimeFormat.forPattern("yyyy/MM/dd");
			LocalDate startOfWeek = dateTime.minusDays(dateTime.dayOfWeek().get() - 1);
			LocalDate endOfWeek = dateTime.plusDays(7 - dateTime.dayOfWeek().get());
			
			startOfWeek=startOfWeek.minusWeeks(1);
			endOfWeek=endOfWeek.minusWeeks(1);
			
			Log.i(TAG, "this week"+startOfWeek.toString(dtf)+" to "+endOfWeek.toString(dtf));
			File fn = new File (sdcard, "sadhealth/users/"+ _appPrefs.getUserID()+"/lightSources.csv");
			FileReader fr= new FileReader(fn);
			BufferedReader bufferedReader = new BufferedReader(fr);
			double sumALS=0;
			double sumNLS=0;
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
							
							sumALS+=ALS.getHourOfDay();
							sumNLS+=NLS.getHourOfDay();
							sumALS+=ALS.getMinuteOfDay()%60;
							sumNLS+=NLS.getMinuteOfDay()%60;
							Log.i(TAG,"LD-this week: "+date.toString(dtf)+" "+sumALS);
							break;
						}
		 			 }
			}
	        
	        ALS=(int) sumALS;
	        NLS=(int) sumNLS;
	        Log.i(TAG,"XD-This Week "+ ALS);
	        //max = lmax;
	        bufferedReader.close();
		}
		else if(id.contentEquals("Mood")|| id.contentEquals("Sleep") ||id.contentEquals("Energy") || id.contentEquals("Social") )
		{
			File file;
			
			DateTimeFormatter sdf = DateTimeFormat.forPattern("MMdd");
			DateTimeFormatter dtf=DateTimeFormat.forPattern("MM/dd");
			LocalDate startOfWeek = dateTime.minusDays(dateTime.dayOfWeek().get() - 1);
			LocalDate endOfWeek = dateTime.plusDays(7 - dateTime.dayOfWeek().get());
			
			startOfWeek=startOfWeek.minusWeeks(1);
			endOfWeek=endOfWeek.minusWeeks(1);
			
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
							datesQuestionnaire.add(new SimpleDateFormat("MM/dd").parse(data[1]));
						} catch (ParseException e) {
							
							e.printStackTrace();
						}
				        mood.add(-Double.valueOf(data[2]));
				        sleep.add(-Double.valueOf(data[3]));
				        energy.add(-Double.valueOf(data[4]));
				        social.add(-Double.valueOf(data[5]));
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
			DateTimeFormatter dtf=DateTimeFormat.forPattern("MM/dd/yyyy");
			LocalDate startOfWeek = dateTime.minusDays(dateTime.dayOfWeek().get() - 1);
			LocalDate endOfWeek = dateTime.plusDays(7 - dateTime.dayOfWeek().get());
			Log.i(TAG, "this week"+startOfWeek.toString(dtf)+" to "+endOfWeek.toString(dtf));
			
				//Log.i(TAG, "this week"+date.toString(dtf));
				File file = new File (sdcard, "sadhealth/users/"+ MainActivity.USERID + "/maxlight.csv");
				if (file.exists())
				{
					FileReader fr = null;
					
						fr = new FileReader(file);
					
					BufferedReader bufferedReader = new BufferedReader(fr);
					String line = null;
			        
			        String[] data;
			        for (LocalDate date = startOfWeek; date.isBefore(endOfWeek) ||date.isEqual(endOfWeek); date = date.plusDays(1))
					{
					
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
			DateTimeFormatter dtf=DateTimeFormat.forPattern("yyyy/MM/dd");
			LocalDate startOfWeek = dateTime.minusDays(dateTime.dayOfWeek().get() - 1);
			LocalDate endOfWeek = dateTime.plusDays(7 - dateTime.dayOfWeek().get());
			
			Log.i(TAG, "this week"+startOfWeek.toString(dtf)+" to "+endOfWeek.toString(dtf));
			File fn = new File (sdcard, "sadhealth/users/"+ _appPrefs.getUserID()+"/lightSources.csv");
			FileReader fr= new FileReader(fn);
			BufferedReader bufferedReader = new BufferedReader(fr);
			double sumALS=0;
			double sumNLS=0;
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
							
							sumALS+=ALS.getHourOfDay();
							sumNLS+=NLS.getHourOfDay();
							sumALS+=ALS.getMinuteOfDay()%60;
							sumNLS+=NLS.getMinuteOfDay()%60;
							Log.i(TAG,"LD-this week: "+date.toString(dtf)+" "+sumALS);
							break;
						}
		 			 }
			}
	        
	        ALS=(int) sumALS;
	        NLS=(int) sumNLS;
	        Log.i(TAG,"XD-This Week "+ ALS);
	        //max = lmax;
	        bufferedReader.close();
		}
		else if(id.contentEquals("Mood")|| id.contentEquals("Sleep") ||id.contentEquals("Energy") || id.contentEquals("Social") )
		{
			File file;
			
			DateTimeFormatter sdf = DateTimeFormat.forPattern("MMdd");
			DateTimeFormatter dtf=DateTimeFormat.forPattern("MM/dd");
			LocalDate startOfWeek = dateTime.minusDays(dateTime.dayOfWeek().get() - 1);
			LocalDate endOfWeek = dateTime.plusDays(7 - dateTime.dayOfWeek().get());
			
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
							datesQuestionnaire.add(new SimpleDateFormat("MM/dd").parse(data[1]));
						} catch (ParseException e) {
							
							e.printStackTrace();
						}
				        mood.add(-Double.valueOf(data[2]));
				        sleep.add(-Double.valueOf(data[3]));
				        energy.add(-Double.valueOf(data[4]));
				        social.add(-Double.valueOf(data[5]));
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
			DateTimeFormatter dtf=DateTimeFormat.forPattern("MM/dd/yyyy");
			dt=dt.minusMonths(1);
			Log.i(TAG, "last month"+dt.getMonthOfYear());
			
				//Log.i(TAG, "this week"+date.toString(dtf));
				File file = new File (sdcard, "sadhealth/users/"+ MainActivity.USERID + "/maxlight.csv");
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
			DateTimeFormatter dtf=DateTimeFormat.forPattern("yyyy/MM/dd");
			dt=dt.minusMonths(1);
			File fn = new File (sdcard, "sadhealth/users/"+ _appPrefs.getUserID()+"/lightSources.csv");
			FileReader fr= new FileReader(fn);
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
	        
	        ALS=(int) sumALS;
	        NLS=(int) sumNLS;
	        Log.i(TAG,"XD-Last Month "+ ALS);
	        //max = lmax;
	        bufferedReader.close();
		}
		else if(id.contentEquals("Mood")|| id.contentEquals("Sleep") ||id.contentEquals("Energy") || id.contentEquals("Social") )
		{
			File file;
			dt=dt.minusMonths(1);
			Log.i(TAG," month is "+ dt.getMonthOfYear()+"  and it has "+daysOfMonth(dt.getYear(),dt.getMonthOfYear()));
			
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
							datesQuestionnaire.add(new SimpleDateFormat("MM/dd").parse(data[1]));
						} catch (ParseException e) {
							
							e.printStackTrace();
						}
				        mood.add(-Double.valueOf(data[2]));
				        sleep.add(-Double.valueOf(data[3]));
				        energy.add(-Double.valueOf(data[4]));
				        social.add(-Double.valueOf(data[5]));
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
			DateTimeFormatter dtf=DateTimeFormat.forPattern("MM/dd/yyyy");
			
			Log.i(TAG, "this month"+dt.getMonthOfYear());
			
				//Log.i(TAG, "this week"+date.toString(dtf));
				File file = new File (sdcard, "sadhealth/users/"+ MainActivity.USERID + "/maxlight.csv");
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
			DateTimeFormatter dtf=DateTimeFormat.forPattern("yyyy/MM/dd");
			File fn = new File (sdcard, "sadhealth/users/"+ _appPrefs.getUserID()+"/lightSources.csv");
			FileReader fr= new FileReader(fn);
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
	        
	        ALS=(int) sumALS;
	        NLS=(int) sumNLS;
	        Log.i(TAG,"XD-This Month "+ ALS);
	        //max = lmax;
	        bufferedReader.close();
		}
		else if(id.contentEquals("Mood")|| id.contentEquals("Sleep") ||id.contentEquals("Energy") || id.contentEquals("Social") )
		{
			
			File file;
		
			Log.i(TAG," month is "+ dt.getMonthOfYear()+"  and it has "+daysOfMonth(dt.getYear(),dt.getMonthOfYear()));
			
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
							datesQuestionnaire.add(new SimpleDateFormat("MM/dd").parse(data[1]));
						} catch (ParseException e) {
							
							e.printStackTrace();
						}
				        mood.add(-Double.valueOf(data[2]));
				        sleep.add(-Double.valueOf(data[3]));
				        energy.add(-Double.valueOf(data[4]));
				        social.add(-Double.valueOf(data[5]));
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
			File fn = new File (sdcard, "sadhealth/users/"+ _appPrefs.getUserID()+"/maxlight.csv");
			if (fn.exists())
			{
				FileReader fr = null;
				fr = new FileReader(fn);
				
				BufferedReader bufferedReader = new BufferedReader(fr);
				DateTimeFormatter dtf=DateTimeFormat.forPattern("MM/dd/yyyy");
				DateTime now=new DateTime();
				String line = null;
		        
		        String[] data;
		        
					while ((line = bufferedReader.readLine()) != null) {
						data = line.split(",");
						
						pdates.add(dtf.parseDateTime(data[3]).toDate());
						py_vals.add(Double.valueOf(data[4]));
						  
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
			File fn = new File (sdcard, "sadhealth/users/"+ _appPrefs.getUserID()+"/lightSources.csv");
			FileReader fr= new FileReader(fn);
			BufferedReader bufferedReader = new BufferedReader(fr);
			double sumALS=0;
			double sumNLS=0;
			String line = null;
	        boolean is1st=true;
	        String[] data;
	        while ((line = bufferedReader.readLine()) != null) {
	        	data = line.split(",");
	        	if (is1st){
	        		//min=Long.valueOf(data[0]);
	        		is1st=false;
	        	}
	        	
	            DateTime ALS = sdf.parseDateTime(data[1]);
				DateTime NLS =sdf.parseDateTime(data[2]);
				
				sumALS+=ALS.getHourOfDay();
				sumNLS+=NLS.getHourOfDay();
				sumALS+=ALS.getMinuteOfDay()%60;
				sumNLS+=NLS.getMinuteOfDay()%60;
	           
	        }
	        
	        ALS=(int) sumALS;
	        NLS=(int) sumNLS;
	        Log.i(TAG,"XD-All Time "+ ALS);
	        //max = lmax;
	        bufferedReader.close();
		}
		else if(id.contentEquals("Mood")|| id.contentEquals("Sleep") ||id.contentEquals("Energy") || id.contentEquals("Social") )
		{
			File f = null;
			
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
							datesQuestionnaire.add(new SimpleDateFormat("MM/dd").parse(data[1]));
						} catch (ParseException e) {
							
							e.printStackTrace();
						}
				        mood.add(-Double.valueOf(data[2]));
				        sleep.add(-Double.valueOf(data[3]));
				        energy.add(-Double.valueOf(data[4]));
				        social.add(-Double.valueOf(data[5]));
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
		Log.i(TAG, "number of children in the view "+ String.valueOf(graphScreen.getChildCount()));
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
		
		switch(graphSpinner.getSelectedItemPosition())
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
			
		}
		
	}
	
    
   
    
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
	     if(spinner.getId() == R.id.PGraphs_spinner)
	     {
	    	
	    	 _appPrefs.setGraphSpinnerSelection(parent.getItemAtPosition(pos).toString());
	    	 initGraph(parent.getItemAtPosition(pos).toString());
	    	 
	     }
	     else if(spinner.getId() == R.id.PGraphsGranularity_spinner)
	     {
	    	 
	    	 _appPrefs.setGranularitySpinnerSelection(parent.getItemAtPosition(pos).toString());
	    	 initGranularity(parent.getItemAtPosition(pos).toString());
	     }
		
	}

	@Override
	public void onNothingSelected(AdapterView<?> parent) {
		
		
	}
	
	private void SaveCurrentlyViewedGraph()
	{
		graphScreen.setDrawingCacheEnabled(true);
	      // this is the important code :)  
	      // Without it the view will have a dimension of 0,0 and the bitmap will be null          

		graphScreen.measure(MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED), 
	            MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));

		//graphScreen.layout(0, 0, graphScreen.getMeasuredWidth(), graphScreen.getMeasuredHeight()); 

		graphScreen.buildDrawingCache(true);
	      Bitmap b = Bitmap.createBitmap(graphScreen.getDrawingCache());
	      graphScreen.setDrawingCacheEnabled(false); // clear drawing cache
	      
	      String path = Environment.getExternalStorageDirectory().toString();
	      OutputStream fOut = null;
	      //graphSpinner, granularitySpinner;
	      File file = new File(path,"sadhealth/users/"+graphSpinner.getSelectedItem().toString()+"-"+granularitySpinner.getSelectedItem().toString()+ ".jpg");
	      try {
			fOut = new FileOutputStream(file);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	      b.compress(Bitmap.CompressFormat.JPEG, 85, fOut);
	      try {
			fOut.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	      try {
			fOut.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	  
    
	public void launchSharing()
	{
		SaveCurrentlyViewedGraph();
		Intent picMessageIntent = new Intent(android.content.Intent.ACTION_SEND);
		picMessageIntent.setType("image/jpeg");
		
		String path = Environment.getExternalStorageDirectory().toString();
		File downloadedPic =  new File(path,"sadhealth/users/"+graphSpinner.getSelectedItem().toString()+"-"+granularitySpinner.getSelectedItem().toString()+ ".jpg");
		    

		picMessageIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(downloadedPic));
		startActivity(picMessageIntent);  
	}

	@Override
	public void onClick(View arg0) {
		Log.i(TAG,"share button clicked");
		launchSharing();
		
	}
	

	 


}