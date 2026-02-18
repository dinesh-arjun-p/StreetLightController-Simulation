package ohlisimulator.dao;

import java.util.List;

import redis.clients.jedis.resps.Tuple;

public abstract class Dao {
	public abstract boolean registerDevice(String SerialNumber,String x,String y,long time,List<Long>duration);
	public abstract void markDiscovered(String deviceId) ;
	public abstract String getLatitude(String topic);
	public abstract String getLongitude(String topic);
	public abstract int getIntUndiscovered();
	public abstract List<String> getCurrentRetryReadyDevice(long now);
	public abstract void updateRetry(String device,long time);
	public abstract void clearDatabase();
	public abstract List<Tuple> getAllRetryDevices();
	public abstract double getCreatedTime(String device);
	public abstract void nextRetry(String device, long long1);
}
