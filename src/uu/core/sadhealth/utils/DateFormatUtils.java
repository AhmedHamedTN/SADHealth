package uu.core.sadhealth.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DateFormatUtils {
	
	private static SimpleDateFormat sdffull = new SimpleDateFormat("MM/dd-HH:mm");
	private static SimpleDateFormat sdfmonthDay = new SimpleDateFormat("MM/dd");
	private static SimpleDateFormat sdfmonth = new SimpleDateFormat("MM");
	private static SimpleDateFormat iconTime=new SimpleDateFormat("HH:mm");
	private static SimpleDateFormat hoursFormat=new SimpleDateFormat("HH");
	private static SimpleDateFormat minutesFormat=new SimpleDateFormat("mm");
	private static SimpleDateFormat sdfperiodformat = new SimpleDateFormat("MMdd");
	private static SimpleDateFormat sdfMonthDayYear = new SimpleDateFormat("MM/dd/yyyy");
	
	
	public static String getDate(Long currentDateTime)
	{
		return sdffull.format(currentDateTime);
	}
	
	public static String getMonth(Long currentDateTime)
	{
		return sdfmonth.format(currentDateTime);
	}
	
	public static String getMonthDay(Long currentDateTime)
	{
		return sdfmonthDay.format(currentDateTime);
	}
	
	public static String getTime(Long currentDateTime)
	{
		return iconTime.format(currentDateTime);
	}
	
	public static String getHours(Long currentDateTime)
	{
		return hoursFormat.format(currentDateTime);
	}
	
	public static String getMinutes(Long currentDateTime)
	{
		return minutesFormat.format(currentDateTime);
	}
	
	public static String getPeriodNo(Long currentDateTime)
	{
		return sdfperiodformat.format(currentDateTime);
	}
	
	public static String getMonthDayYear(Long currentDateTime)
	{
		return sdfMonthDayYear.format(currentDateTime);
	}

}
