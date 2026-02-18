package ohlisimulator.dao;

import java.util.List;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.resps.Tuple;

public class DragonFly extends Dao {
	static Jedis jedis= new Jedis("localhost", 6379);

	public boolean registerDevice(String deviceId, String x, String y,long time,List<Long> duration) {
		String device="device:"+deviceId;
		jedis.hset(device, "X", x);
		jedis.hset(device, "Y", y);
		updateRetry(deviceId,time);
		nextRetry(deviceId,duration.get(1));
		return true;
	}
	
	public void updateRetry(String deviceId,long time) {
		jedis.zadd("undiscovered_devices", time, deviceId);
	}
	
	public void nextRetry(String deviceId,long duration) {
		double currentRetry;
		try {
			currentRetry=jedis.zscore("nextretry", deviceId);
		}
		catch(NullPointerException e) {
			currentRetry=jedis.zscore("undiscovered_devices", deviceId);
		}
		long nextRetry=(long)currentRetry+duration;
		jedis.zadd("nextretry", nextRetry,deviceId);
	}
	
	
	
	public void markDiscovered(String deviceId) {
	    jedis.zrem("undiscovered_devices", deviceId);
	    jedis.zrem("nextretry",deviceId);
	}


	@Override
	public String getLatitude(String topic) {
		return jedis.hget(topic,"X");
		
	}

	@Override
	public String getLongitude(String topic) {
		return jedis.hget(topic,"Y");
	}



	@Override
	public int getIntUndiscovered() {
		return (int)jedis.zcard("undiscovered_devices");
	}



	@Override
	public List<String> getCurrentRetryReadyDevice(long now) {
		

		    return jedis.zrangeByScore(
		            "nextretry",
		            0,
		            now
		    );
		}

	@Override
	public void clearDatabase() {
		 jedis.flushDB(); 
		
	}

	@Override
	public List<Tuple> getAllRetryDevices() {
		return jedis.zrangeWithScores("undiscovered_devices", 0, -1);
	}

	@Override
	public double getCreatedTime(String deviceId) {
		return jedis.zscore("undiscovered_devices", deviceId);
	}
	



	
	
}
