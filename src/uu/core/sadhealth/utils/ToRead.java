package uu.core.sadhealth.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ToRead {
	public static List<List<String>> myData (File filename) throws IOException{
		BufferedReader input = null;
		List<List<String>> csvData = new ArrayList<List<String>>();
		try 
		{
		    input =  new BufferedReader(new FileReader(filename));
		    String line = null;
		    while (( line = input.readLine()) != null)
		    {
		    	String[] data = line.split(",");
		    	csvData.add(Arrays.asList(data));
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
		return null;	
	}

}
