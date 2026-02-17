package ohlisimulator.dao;

import ohlisimulator.main.*;
import ohlisimulator.service.Service;
import redis.clients.jedis.Jedis;

public class DragonFly extends Dao {
	static Jedis jedis= new Jedis("localhost", 6379);

	public boolean registerDevice(int SerialNumber, String x, String y) {
		String device="device:"+SerialNumber;
		jedis.hset(device, "X", x);
		jedis.hset(device, "Y", y);
		jedis.hset(device,"discover", "no");
		return true;
	}

	@Override
	public void updateDiscover(String topic, String value) {
		String device="device:"+topic;
		jedis.hset(device ,"discover",value);
		
	}

	@Override
	public String getLatitude(String topic) {
		return jedis.hget(topic,"X");
	}

	@Override
	public String getLongitude(String topic) {
		return jedis.hget(topic,"Y");
	}
	
}
