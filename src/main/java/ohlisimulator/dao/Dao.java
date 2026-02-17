package ohlisimulator.dao;

public abstract class Dao {
	public abstract boolean registerDevice(int SerialNumber,String x,String y);
	public abstract void updateDiscover(String topic,String value);
	public abstract String getLatitude(String topic);
	public abstract String getLongitude(String topic);
}
