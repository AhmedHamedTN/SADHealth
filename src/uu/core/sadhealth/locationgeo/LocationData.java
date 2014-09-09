/**LocationData.java
 * This class consists of various data structures to hold the location from google geocoder api. 
 * It is part of the locationGeo package using the Google API Geocoder v3 servers as a means of collecting location information based on coordinates (Lat & Long)
 * See: http://stackoverflow.com/questions/15182853/android-geocoder-getfromlocationname-always-returns-null for orginal idea. I use this method because the LocationServices is killed sometimes by
 * android OS and there is nothing we can do about it until the smartphone is restarted.
 * @Author  Kiril Tzvetanov Goguev
 * @Date February 11th,2013
 */
package uu.core.sadhealth.locationgeo;

import uu.core.sadhealth.locationgeo.LocationData.Location;


public class LocationData
{
	public Location currentLocation = new Location();
	
	

	public class Location
	{
		private String City;
		private String Country;
		
		public String getCity()
		{
			return City;
		}
		
		public void setCity(String City)
		{
			this.City=City;
		}
		
		public String getCountry()
		{
			return Country;
		}
		
		public void setCountry(String Country)
		{
			this.Country=Country;
		}
	}



	

	
	
	

}