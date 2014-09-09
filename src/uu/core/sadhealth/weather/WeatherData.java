/**WeatherData.java
 * This class consists of various data structures to hold the weather. 
 * It is part of the weather package using the OpenweatherMap servers as a means of collecting weather information based on location (Lat & Long)
 * See: http://www.javacodegeeks.com/2013/06/android-build-real-weather-app-json-http-and-openweathermap.html for the original
 * @Author (Modified) Kiril Tzvetanov Goguev
 * @Date October 15th,2013
 */
package uu.core.sadhealth.weather;

public class WeatherData
{
	public CurrentCondition currentCondition = new CurrentCondition();
	public Temperature temperature = new Temperature();
	public Wind wind = new Wind();
	public Rain rain = new Rain();
	public Snow snow = new Snow()	;
	public Clouds clouds = new Clouds();
	
	public Location currentLocation=new Location();
	public Provider provider=new Provider();
	public SunPhase sunphase=new SunPhase();

	public byte[] iconData;
	
	public class Provider
	{
		private String Provider;
		private String icon;
	}
	
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

	public  class CurrentCondition {
		private int weatherId;
		private String condition;
		private String descr;
		private String icon;
		private String icon_url;


		private float pressure;
		private float humidity;

		public int getWeatherId() {
			return weatherId;
		}
		public void setWeatherId(int weatherId) {
			this.weatherId = weatherId;
		}
		public String getCondition() {
			return condition;
		}
		public void setCondition(String condition) {
			this.condition = condition;
		}
		public String getDescr() {
			return descr;
		}
		public void setDescr(String descr) {
			this.descr = descr;
		}
		public String getIcon() {
			return icon;
		}
		public void setIcon(String icon) {
			this.icon = icon;
		}
		
		public String getIconURL()
		{
			return icon_url;
		}
		
		public void setIconURL(String url)
		{
			this.icon_url=url;
		}
		
		public float getPressure() {
			return pressure;
		}
		public void setPressure(float pressure) {
			this.pressure = pressure;
		}
		public float getHumidity() {
			return humidity;
		}
		public void setHumidity(float humidity) {
			this.humidity = humidity;
		}
		
		


	}

	public  class Temperature {
		private float temp;
		private float minTemp;
		private float maxTemp;

		public float getTemp() {
			return temp;
		}
		public void setTemp(float temp) {
			this.temp = temp;
		}
		public float getMinTemp() {
			return minTemp;
		}
		public void setMinTemp(float minTemp) {
			this.minTemp = minTemp;
		}
		public float getMaxTemp() {
			return maxTemp;
		}
		public void setMaxTemp(float maxTemp) {
			this.maxTemp = maxTemp;
		}

	}

	public  class Wind {
		private float speed;
		private float deg;
		public float getSpeed() {
			return speed;
		}
		public void setSpeed(float speed) {
			this.speed = speed;
		}
		public float getDeg() {
			return deg;
		}
		public void setDeg(float deg) {
			this.deg = deg;
		}


	}

	public  class Rain {
		private String time;
		private float ammount;
		public String getTime() {
			return time;
		}
		public void setTime(String time) {
			this.time = time;
		}
		public float getAmmount() {
			return ammount;
		}
		public void setAmmount(float ammount) {
			this.ammount = ammount;
		}



	}

	public  class Snow {
		private String time;
		private float ammount;

		public String getTime() {
			return time;
		}
		public void setTime(String time) {
			this.time = time;
		}
		public float getAmmount() {
			return ammount;
		}
		public void setAmmount(float ammount) {
			this.ammount = ammount;
		}


	}

	public  class Clouds {
		private int perc;

		public int getPerc() {
			return perc;
		}

		public void setPerc(int perc) {
			this.perc = perc;
		}


	}
	
	public class SunPhase 
	{
		private long sunRise;
		private long sunSet;
		private String sunRiseHr;
		private String sunRiseMin;
		private String sunSetHr;
		private String sunSetMin;
		
		public long getSunset() 
		{
            return sunSet;
		}
		
		public void setSunset(long sunset)
		{
            this.sunSet = sunset;
		}
		
		public long getSunrise() 
		{
            return sunRise;
		}
		
		public void setSunrise(long sunrise)
		{
            this.sunRise = sunrise;
		}
		
		public String getSunRiseHr()
		{
			return sunRiseHr;
		}
		
		public String getSunRiseMin()
		{
			return sunRiseMin;
		}
		
		public String getSunSetHr()
		{
			return sunSetHr;
		}
		
		public String getSunSetMin()
		{
			return sunSetMin;
		}
		
		public void setSunRiseHr(String sunRiseHr)
		{
			this.sunRiseHr=sunRiseHr;
		}
		
		public void setSunRiseMin(String sunRiseMin)
		{
			this.sunRiseMin=sunRiseMin;
		}
		
		public void setSunSetHr(String sunSetHr)
		{
			this.sunSetHr=sunSetHr;
		}
		
		public void setSunSetMin(String sunSetMin)
		{
			this.sunSetMin=sunSetMin;
		}
	}
	

}