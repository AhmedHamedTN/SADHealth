package uu.core.sadhealth;

import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import uu.core.sadhealth.services.MainService;
import uu.core.sadhealth.utils.AppPreferences;
import uu.core.sadhealth.utils.DateFormatUtils;
import uu.core.sadhealth.utils.FileToWrite;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

public class ResetReciever extends BroadcastReceiver {
	
	private AppPreferences _appPrefs ;
	private FileWriter lightSourceWriter,maxlightWriter;
	private SimpleDateFormat sdf=new SimpleDateFormat("yyyy/MM/dd");
	private int oldPeriodNo;
	private int newPeriodNo;
	
	@Override
	public void onReceive(Context arg0, Intent arg1) {
		 _appPrefs=new AppPreferences(arg0);
		 DateTime dt= new DateTime();
		 DateTime fixedDt=dt.minusDays(1);
		 
		 DateTimeFormatter dtf=DateTimeFormat.forPattern("MM/dd/yyyy");
		 
		
		 DateTimeFormatter sdfx = DateTimeFormat.forPattern("d");
		 oldPeriodNo=_appPrefs.getPeriodNo();
		 _appPrefs.setPeriodNo(Integer.parseInt(DateFormatUtils.getPeriodNo(System.currentTimeMillis())));
		 newPeriodNo=_appPrefs.getPeriodNo();
		 MainService.unlockNo=0;
		 
		 Log.i("light max","writing out max light for yesterday");
			try{
				FileWriter maxlightWriter = FileToWrite.createLogFileWriter("maxlight.csv",_appPrefs.getUserID()); //file writer which overwrites 
				maxlightWriter.append(String.valueOf(dt.getYear())+","+String.valueOf(dt.getMonthOfYear())+","+sdfx.print(fixedDt)+","+fixedDt.toString("MM/dd/yyyy") +","+_appPrefs.getMaxLux()+"\n");
				maxlightWriter.flush();
				maxlightWriter.close();
			}
			catch(IOException e){

			}
		 
		 //MainService.dataPartitioning();
		Log.i("light sources","writing out light sources");
		try{
			FileWriter lightSourceWriter = FileToWrite.createLogFileWriter("lightSources.csv",_appPrefs.getUserID()); //file writer which overwrites 
			lightSourceWriter.append(sdf.format(new Date(System.currentTimeMillis()))+"," +_appPrefs.getALSTime()+","+_appPrefs.getNLSTime()+"\n");
			lightSourceWriter.flush();
			lightSourceWriter.close();
		}
		catch(IOException e){

		}
		_appPrefs.setMaxLux((float)0.0);
		_appPrefs.updateVehicleTimer(000000);
		_appPrefs.updateBikeTimer(000000);
		_appPrefs.updateFootTimer(000000);
		_appPrefs.updateStillTimer(000000);
		_appPrefs.updateTiltingTimer(000000);
		_appPrefs.setALSTime("00:00:00");
		_appPrefs.setNLSTime("00:00:00");
	}

}
