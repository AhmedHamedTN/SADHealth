package uu.core.sadhealth;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.util.EntityUtils;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;

import uu.core.sadhealth.services.MainService;
import uu.core.sadhealth.utils.AppPreferences;
import uu.core.sadhealth.utils.FileToWrite;
import uu.core.sadhealth.utils.Hash;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationCompat.Builder;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager.LayoutParams;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ImageButton;


public class UploadActivity extends Activity {
	
	private final String TAG = "UploadActivity";
	static final int UPLOADEXIT= 5;

	
	//Create shared preferences so we can store valuable data related to the app
	private AppPreferences _appPrefs;
	
	private final String url = "http://darth.it.uu.se:1024/sad/upload_test_revised.php";
	TextView networkStatusText,periodNoText;
	Button uploadButton;
	boolean networkAvailable = false;
	int periodNo;
	static File sdcard = Environment.getExternalStorageDirectory();
	private boolean skipPeriodCheck=false;
	Uploader upLoadTask;
	private int uploadCounter=0;
	private String country,city;
	private int curUploadnum=0;
	Intent notifyIntent;
	
	
	//Notifications
	NotificationManager mNotifyManager;
	Builder mBuilder;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_upload);
		_appPrefs = new AppPreferences(getApplicationContext());
		uploadCounter=_appPrefs.getUploadCounter();
		country=_appPrefs.getCountry();
		city=_appPrefs.getCity();
		
		this.getWindow().setFlags(
	    		LayoutParams.FLAG_SHOW_WHEN_LOCKED |
	    		LayoutParams.FLAG_DISMISS_KEYGUARD |
	    		LayoutParams.FLAG_TURN_SCREEN_ON |
	            LayoutParams.FLAG_KEEP_SCREEN_ON  |
	            LayoutParams.FLAG_FULLSCREEN
	            ,
	            LayoutParams.FLAG_TURN_SCREEN_ON |
	            LayoutParams.FLAG_SHOW_WHEN_LOCKED |
	            LayoutParams.FLAG_KEEP_SCREEN_ON |
	            LayoutParams.FLAG_DISMISS_KEYGUARD |
	            LayoutParams.FLAG_FULLSCREEN
	    		);
		
		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			if(extras.containsKey("ExitAfter"))
			{
				Log.i(TAG, "extras containsKey");
				skipPeriodCheck=extras.getBoolean("ExitAfter");
			}
		}
		
		Log.i(TAG,"Started");
		
		networkStatusText = (TextView) findViewById(R.id.textView2);
		periodNoText = (TextView) findViewById(R.id.textView4);
		
		ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
		if (networkInfo != null){
			networkAvailable = true;
			networkStatusText.setText(networkInfo.getTypeName() +"("+networkInfo.getState()+")" );
		}else{
			networkStatusText.setText("No network available");
			networkAvailable = false;
			
		}
		
		periodNo=_appPrefs.getPeriodNo();
		periodNoText.setText(String.valueOf(periodNo));
		mBuilder=new NotificationCompat.Builder(getApplicationContext());
		mNotifyManager= (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		if (networkAvailable)
		{
			List<File> myFilelist=null;
			try{
					myFilelist = getFileList();
				}catch (IOException e) {
						e.printStackTrace();
					}
					if (skipPeriodCheck)
					{
						
							if (myFilelist.size()!=0)
							{
								upLoadTask = new Uploader (myFilelist);
								
								upLoadTask.execute();
							}
					}
					else
					{
						//if ( (periodNo >1) && (myFilelist.size()!=0) && (periodNo-readUploadPeriodNo() > 0) ){ //days make sure there is data to upload
							
						if (myFilelist.size()!=0) { //days make sure there is data to upload
							
							mBuilder=new NotificationCompat.Builder(getApplicationContext());
							upLoadTask = new Uploader (myFilelist);
							upLoadTask.execute();
						}else{ //too little data
							Context context = getApplicationContext();
							CharSequence text = "Too little data to upload, please wait until there is enough data";
							int duration = Toast.LENGTH_LONG;

							Toast toast = Toast.makeText(context, text, duration);
							toast.show();
							
							// Prepare intent which is triggered if the
                		    // notification is selected
                			notifyIntent = new Intent(getApplicationContext(), UploadActivity.class);
            				notifyIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            				Log.i(TAG, "notification activity for missed upload");
            				

            				PendingIntent pIntent = PendingIntent.getActivity(getApplicationContext(), 0, notifyIntent, 0);

            				// build notification
            				// the addAction re-use the same intent to keep the example short
            				NotificationCompat.Builder mBuilder =
            					    new NotificationCompat.Builder(getApplicationContext())
            				        .setContentTitle("Missed Upload")
            				        .setContentText("Too little data to upload, wait until there is enough data")
            				        .setSmallIcon(R.drawable.file_upload)
            				        .setContentIntent(pIntent)
            				        .setAutoCancel(true);
            				        
            				    
            				  
            				NotificationManager notificationManager = 
            				  (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

            				notificationManager.notify(0, mBuilder.build()); 
						}
					
					}
				}else{//no network avail
					Context context = getApplicationContext();
					CharSequence text = "No network available";
					int duration = Toast.LENGTH_SHORT;

					Toast toast = Toast.makeText(context, text, duration);
					toast.show();
				}
			
		//Intent resultIntent = new Intent();
			//setResult(UPLOADEXIT, resultIntent);
			finish();
		
		
		
	}
	

	
	private  List<File> getFileList() throws IOException{
		List<File> myList = new ArrayList<File>();
		String postFix="";
		File file;
		//for (int i = 1; i < periodNo ; i++) {
			postFix = String.format("%04d", periodNo);
			file= new File (sdcard, "sadhealth/users/"+ MainActivity.USERID + "/phone_location"+postFix+".csv");
			if (file.exists())
				myList.add(file);
			file= new File (sdcard, "sadhealth/users/"+ MainActivity.USERID + "/phone_accelerometer"+postFix+".csv");
			if (file.exists())
				myList.add(file);
			file= new File (sdcard, "sadhealth/users/"+ MainActivity.USERID + "/phone_lightsensor"+postFix+".csv");
			if (file.exists())
				myList.add(file);
			file= new File (sdcard, "sadhealth/users/"+ MainActivity.USERID + "/unlock_no"+postFix+".csv");
			if (file.exists())
				myList.add(file);
			file= new File (sdcard, "sadhealth/users/"+ MainActivity.USERID + "/raw_accelerometer"+postFix+".csv");
			if (file.exists())
				myList.add(file);
			file= new File (sdcard, "sadhealth/users/"+ MainActivity.USERID + "/questionnaire"+postFix+".csv");
			if (file.exists())
				myList.add(file);
			file= new File (sdcard, "sadhealth/users/"+ MainActivity.USERID + "/weather.csv");
			if (file.exists())
				myList.add(file);
			file= new File (sdcard, "sadhealth/users/"+ MainActivity.USERID + "/lightSources.csv");
			if (file.exists())
				myList.add(file);
		//}
		
		return myList;
		
	}
	
	
	class Uploader extends AsyncTask<Void, String, Integer>
    {
        protected static final int ID = 0;
		private List<File> files;
        private int uploaded;

        public Uploader(List<File> files)
        {
            this.files = files;
        }
        
        private void writeUploadPeriodNo(){  // read the period No from the SD
    		FileWriter periodNoWriter;
    		try{
    			periodNoWriter = FileToWrite.createLogFileWriter("upload_pn.csv",_appPrefs.getUserID(),false); //file writer which overwrites 
            	periodNoWriter.append(String.valueOf(periodNo));
            	periodNoWriter.flush();
            	periodNoWriter.close();
    		    
    		}
    		catch (IOException e){
    			e.printStackTrace();
    		}
    	}

        @Override
        protected void onPreExecute()
        {
    		
    		MainService.uploading = true;
    		curUploadnum=0;
    		mBuilder.setContentTitle("SADHealth Server Upload")
    		.setProgress(files.size(),0,false)
    	    .setContentText("Upload in progress "+curUploadnum+"/"+files.size())
    	    .setSmallIcon(R.drawable.file_upload);
    		// Displays the progress bar for the first time.
            mNotifyManager.notify(0, mBuilder.build());	
        }

        @Override
        protected void onPostExecute(Integer result)
        {
            Toast.makeText(UploadActivity.this, result + " files uploaded",
                    Toast.LENGTH_LONG).show();
            //writeUploadPeriodNo();
            mBuilder.setContentText("Upload complete")
            // Removes the progress bar
                    .setProgress(0,0,false);
            mNotifyManager.notify(0, mBuilder.build());
            MainService.uploading=false;
            uploadCounter+=1;
            _appPrefs.setUploadCounter(uploadCounter);
            _appPrefs.setLastPeriodUploaded(periodNo);
            calculateNexUploadTime();
            MainService.manualUploadingIndicator=false;
            if (skipPeriodCheck)
            {
            	android.os.Process.killProcess(android.os.Process.myPid());
            }
            Intent resultIntent = new Intent();
            setResult(UPLOADEXIT, resultIntent);
            finish();
        }

        
        @Override
        protected Integer doInBackground(Void... voids)
        {
            uploaded = 0;
            String md5="";
            try
            {
                Iterator<File> it = this.files.iterator();
                while (it.hasNext()){
                	File file = it.next();
                	it.remove();

                	String msg = "";



                	Log.i(TAG,"new upload");
                	HttpClient httpclient = new DefaultHttpClient();
                	httpclient.getParams().setParameter(CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);

                	HttpPost httppost = new HttpPost(url);
                	Charset chars = Charset.forName("UTF-8");

                	MultipartEntity mpEntity = new MultipartEntity();
                	Log.i(TAG,country+'-'+city+'-'+file.getName());
                	mpEntity.addPart("country", new StringBody(country));
                	mpEntity.addPart("city",new StringBody(city));
                	mpEntity.addPart("userfile", new FileBody(file, " text/csv"));
                	mpEntity.addPart("userid", new StringBody(MainActivity.USERID,chars));

                	httppost.setEntity(mpEntity);
                	

                	HttpResponse response = httpclient.execute(httppost);
                	HttpEntity resEntity = response.getEntity();
                	String resMd5 = EntityUtils.toString(resEntity);
                	/*try{
                		md5 = Hash.getMD5Checksum(file.toString());
                		if ( resMd5.equals(md5)){
                			if (!file.getName().equals("period_no.csv"))
                				file.delete();
                		}
                	}
                	catch(IOException e){
                	}*/
                	httpclient.getConnectionManager().shutdown();

                	msg = ("uploading: " + file);


                	uploaded++;
                	
                	publishProgress(msg);
                	


                }
            } catch (Exception e)
            {
                publishProgress("error uploading: " + e);
            }

            return uploaded;
        }
        
      

        @Override
        protected void onProgressUpdate(String... strings)
        {
            //Toast.makeText(UploadActivity.this, strings[0], Toast.LENGTH_SHORT).show();
            curUploadnum+=1;
            mBuilder.setProgress(files.size(), curUploadnum, false);
            mBuilder.setContentText("Upload in progress "+curUploadnum+"/"+files.size());
            mNotifyManager.notify(0, mBuilder.build());
           
            
        }
    }
	
	private void calculateNexUploadTime() {
		
		Calendar c = Calendar.getInstance(); 
		int hour=c.get(Calendar.HOUR_OF_DAY);
        do
        {
        	c.add(Calendar.HOUR_OF_DAY, 3);
        	hour=c.get(Calendar.HOUR_OF_DAY);
        }while(hour >22 ||hour <8);
        
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
        String nextUploadTime = sdf.format(c.getTime());
        
		Log.i(TAG,"The next upload time is "+nextUploadTime);
		
		_appPrefs.setNextUploadTime(nextUploadTime);
		
	}
}
