package uu.core.sadhealth.utils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;



import android.os.Environment;
import android.util.Log;

public class FileToWrite {
	
	//Returns a FileWriter object for the specified file that other methods can use
	public static FileWriter createLogFileWriter(String logFileName,String user){
		FileWriter fileWriter = null;
		File root = new File(Environment.getExternalStorageDirectory(), "sadhealth/users/"+user);
		if (!root.exists()) {	//Create the subfolder if it does not exist
			root.mkdirs();
		}
		File file = new File(root, logFileName);	//Create the file object using the provided file name
		//Open the writer
		try{
			fileWriter = new FileWriter(file, true);			
		}
		catch (IOException e){
			Log.v("LOG FILE", "Failed to open file for '"+logFileName+"'");			
		}		
		return fileWriter;
	}
	
	//Returns a FileWriter object for the specified file that other methods can use 
		public static FileWriter createLogFileWriter(String logFileName,String user, boolean overwrite){
			FileWriter fileWriter = null;
			File root = new File(Environment.getExternalStorageDirectory(), "sadhealth/users/"+user);
			if (!root.exists()) {	//Create the subfolder if it does not exist
				root.mkdirs();
			}
			File file = new File(root, logFileName);	//Create the file object using the provided file name
			//Open the writer
			try{
				fileWriter = new FileWriter(file, overwrite);			
			}
			catch (IOException e){
				Log.v("LOG FILE", "Failed to open file for '"+logFileName+"'");			
			}		
			return fileWriter;
		}
}