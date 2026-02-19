package ohlisimulator.dao;

import java.util.List;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.resps.Tuple;

public class DragonFly extends Dao {

	private static final JedisPool pool = new JedisPool("localhost", 6379);

	public boolean registerDevice(String deviceId, String x, String y, long time, List<Long> duration) {
		String device = "device/" + deviceId;
		int LOAD_OR_CHANGE = 0;
		double BAT_U_100MV = 14.4;
		double DEVICE_TEMP = 25;
		setLatitude(device, x);
		setLongitude(device, y);
		setLoadOrChange(device, LOAD_OR_CHANGE);
		setBatCapEnergy(device, 100);
		setBatU100mv(device, BAT_U_100MV);
		setBatI10ma(device, 0);
		setDeviceTemp(device, DEVICE_TEMP);
		setLedU100mv(device, 0);
		setLedI10ma(device, 0);
		setLedPowerIn(device, 0);
		setPvU100mv(device, 0);
		setPvI10ma(device, 0);
		setChargePower1w(device, 0);
		setMinBatUInDay100mv(device, 0);
		setMaxBatUInDay100mv(device, 0);
		setMaxChargeBatIInDay10ma(device, 0);
		setMaxDischargeBatIInDay10ma(device, 0);
		setMaxChargePowerInDay1w(device, 0);
		setMaxDischargePowerInDay1w(device, 0);
		setChargeWhInDay(device, 0);
		setDischargeWhInDay(device, 0);
		setDaysTotal(device, 0);
		setBatOverDiscTimes(device, 0);
		setBatOverChargeTimes(device, 0);
		setChargeAhTotal(device, 0);
		setDischargeAhTotal(device, 0);
		setChargeWhTotal(device, 0);
		setDischargeWhTotal(device, 0);
		setWorkState(device, 0);

		updateRetry(deviceId, time);
		nextRetry(deviceId, duration.get(0));
		return true;
	}

	public void setLatitude(String device, String x) {
		try (Jedis jedis = pool.getResource()) {
			jedis.hset(device, "X", x);
		}
	}

	public void setLongitude(String device, String y) {
		try (Jedis jedis = pool.getResource()) {
			jedis.hset(device, "Y", y);
		}
	}

	public void setLoadOrChange(String device, int LOAD_OR_CHANGE) {
		setLoadOrChange(device, String.valueOf(LOAD_OR_CHANGE));
	}

	public void setBatCapEnergy(String device, double value) {
		setBatCapSoc(device, String.valueOf(value));
	}

	public void setBatU100mv(String device, double value) {
		setBatU100mv(device, String.valueOf(value));
	}

	public void setBatI10ma(String device, double value) {
		setBatI10ma(device, String.valueOf(value));
	}

	public void setDeviceTemp(String device, double value) {
		setDeviceTemp(device, String.valueOf(value));
	}

	public void setLedU100mv(String device, double value) {
		setLedU100mv(device, String.valueOf(value));
	}

	public void setLedI10ma(String device, double value) {
		setLedI10ma(device, String.valueOf(value));
	}

	public void setLedPowerIn(String device, int value) {
		setLedPowerIn(device, String.valueOf(value));
	}

	public void setPvU100mv(String device, double value) {
		setPvU100mv(device, String.valueOf(value));
	}

	public void setPvI10ma(String device, double value) {
		setPvI10ma(device, String.valueOf(value));
	}

	public void setChargePower1w(String device, int value) {
		setChargePower1w(device, String.valueOf(value));
	}

	public void setMinBatUInDay100mv(String device, double value) {
		setMinBatUInDay100mv(device, String.valueOf(value));
	}

	public void setMaxBatUInDay100mv(String device, double value) {
		setMaxBatUInDay100mv(device, String.valueOf(value));
	}

	public void setMaxChargeBatIInDay10ma(String device, double value) {
		setMaxChargeBatIInDay10ma(device, String.valueOf(value));
	}

	public void setMaxDischargeBatIInDay10ma(String device, double value) {
		setMaxDischargeBatIInDay10ma(device, String.valueOf(value));
	}

	public void setMaxChargePowerInDay1w(String device, double value) {
		setMaxChargePowerInDay1w(device, String.valueOf(value));
	}

	public void setMaxDischargePowerInDay1w(String device, double value) {
		setMaxDischargePowerInDay1w(device, String.valueOf(value));
	}

	public void setChargeWhInDay(String device, double value) {
		setChargeWhInDay(device, String.valueOf(value));
	}

	public void setDischargeWhInDay(String device, double value) {
		setDischargeWhInDay(device, String.valueOf(value));
	}

	public void setDaysTotal(String device, int value) {
		setDaysTotal(device, String.valueOf(value));
	}

	public void setBatOverDiscTimes(String device, int value) {
		setBatOverDiscTimes(device, String.valueOf(value));
	}

	public void setBatOverChargeTimes(String device, int value) {
		setBatOverChargeTimes(device, String.valueOf(value));
	}

	public void setChargeAhTotal(String device, int value) {
		setChargeAhTotal(device, String.valueOf(value));
	}

	public void setDischargeAhTotal(String device, int value) {
		setDischargeAhTotal(device, String.valueOf(value));
	}

	public void setChargeWhTotal(String device, int value) {
		setChargeWhTotal(device, String.valueOf(value));
	}

	public void setDischargeWhTotal(String device, int value) {
		setDischargeWhTotal(device, String.valueOf(value));
	}

	public void setWorkState(String device, int value) {
		setWorkState(device, String.valueOf(value));
	}

	public void setLoadOrChange(String device, String LOAD_OR_CHANGE) {
		try (Jedis jedis = pool.getResource()) {
			jedis.hset(device, "LOAD_OR_CHANGE", LOAD_OR_CHANGE);
		}
	}

	public void setBatCapSoc(String device, String BAT_CAP_SOC) {
		try (Jedis jedis = pool.getResource()) {
			jedis.hset(device, "BAT_CAP_SOC", BAT_CAP_SOC);
		}
	}

	public void setBatU100mv(String device, String BAT_U_100MV) {
		try (Jedis jedis = pool.getResource()) {
			jedis.hset(device, "BAT_U_100MV", BAT_U_100MV);
		}
	}

	public void setBatI10ma(String device, String BAT_I_10MA) {
		try (Jedis jedis = pool.getResource()) {
			jedis.hset(device, "BAT_I_10MA", BAT_I_10MA);
		}
	}

	public void setDeviceTemp(String device, String DEVICE_TEMP) {
		try (Jedis jedis = pool.getResource()) {
			jedis.hset(device, "DEVICE_TEMP", DEVICE_TEMP);
		}
	}

	public void setLedU100mv(String device, String LED_U_100MV) {
		try (Jedis jedis = pool.getResource()) {
			jedis.hset(device, "LED_U_100MV", LED_U_100MV);
		}
	}

	public void setLedI10ma(String device, String LED_I_10MA) {
		try (Jedis jedis = pool.getResource()) {
			jedis.hset(device, "LED_I_10MA", LED_I_10MA);
		}
	}

	public void setLedPowerIn(String device, String LED_POWER_IN) {
		try (Jedis jedis = pool.getResource()) {
			jedis.hset(device, "LED_POWER_IN", LED_POWER_IN);
		}
	}

	public void setPvU100mv(String device, String PV_U_100MV) {
		try (Jedis jedis = pool.getResource()) {
			jedis.hset(device, "PV_U_100MV", PV_U_100MV);
		}
	}

	public void setPvI10ma(String device, String PV_I_10MA) {
		try (Jedis jedis = pool.getResource()) {
			jedis.hset(device, "PV_I_10MA", PV_I_10MA);
		}
	}

	public void setChargePower1w(String device, String CHARGE_POWER_1W) {
		try (Jedis jedis = pool.getResource()) {
			jedis.hset(device, "CHARGE_POWER_1W", CHARGE_POWER_1W);
		}
	}

	public void setMinBatUInDay100mv(String device, String MIN_BAT_U_IN_DAY_100MV) {
		try (Jedis jedis = pool.getResource()) {
			jedis.hset(device, "MIN_BAT_U_IN_DAY_100MV", MIN_BAT_U_IN_DAY_100MV);
		}
	}

	public void setMaxBatUInDay100mv(String device, String MAX_BAT_U_IN_DAY_100MV) {
		try (Jedis jedis = pool.getResource()) {
			jedis.hset(device, "MAX_BAT_U_IN_DAY_100MV", MAX_BAT_U_IN_DAY_100MV);
		}
	}

	public void setMaxChargeBatIInDay10ma(String device, String MAX_CHARGE_BAT_I_IN_DAY_10MA) {
		try (Jedis jedis = pool.getResource()) {
			jedis.hset(device, "MAX_CHARGE_BAT_I_IN_DAY_10MA", MAX_CHARGE_BAT_I_IN_DAY_10MA);
		}
	}

	public void setMaxDischargeBatIInDay10ma(String device, String MAX_DISCHARGE_BAT_I_IN_DAY_10MA) {
		try (Jedis jedis = pool.getResource()) {
			jedis.hset(device, "MAX_DISCHARGE_BAT_I_IN_DAY_10MA", MAX_DISCHARGE_BAT_I_IN_DAY_10MA);
		}
	}

	public void setMaxChargePowerInDay1w(String device, String MAX_CHARGE_POWER_IN_DAY_1W) {
		try (Jedis jedis = pool.getResource()) {
			jedis.hset(device, "MAX_CHARGE_POWER_IN_DAY_1W", MAX_CHARGE_POWER_IN_DAY_1W);
		}
	}

	public void setMaxDischargePowerInDay1w(String device, String MAX_DISCHARGE_POWER_IN_DAY_1W) {
		try (Jedis jedis = pool.getResource()) {
			jedis.hset(device, "MAX_DISCHARGE_POWER_IN_DAY_1W", MAX_DISCHARGE_POWER_IN_DAY_1W);
		}
	}

	public void setChargeWhInDay(String device, String CHARGE_WH_IN_DAY) {
		try (Jedis jedis = pool.getResource()) {
			jedis.hset(device, "CHARGE_WH_IN_DAY", CHARGE_WH_IN_DAY);
		}
	}

	public void setDischargeWhInDay(String device, String DISCHARGE_WH_IN_DAY) {
		try (Jedis jedis = pool.getResource()) {
			jedis.hset(device, "DISCHARGE_WH_IN_DAY", DISCHARGE_WH_IN_DAY);
		}
	}

	public void setDaysTotal(String device, String DAYS_TOTAL) {
		try (Jedis jedis = pool.getResource()) {
			jedis.hset(device, "DAYS_TOTAL", DAYS_TOTAL);
		}
	}

	public void setBatOverDiscTimes(String device, String BAT_OVER_DISC_TIMES) {
		try (Jedis jedis = pool.getResource()) {
			jedis.hset(device, "BAT_OVER_DISC_TIMES", BAT_OVER_DISC_TIMES);
		}
	}

	public void setBatOverChargeTimes(String device, String BAT_OVER_CHARGE_TIMES) {
		try (Jedis jedis = pool.getResource()) {
			jedis.hset(device, "BAT_OVER_CHARGE_TIMES", BAT_OVER_CHARGE_TIMES);
		}
	}

	public void setChargeAhTotal(String device, String CHARGE_AH_TOTAL) {
		try (Jedis jedis = pool.getResource()) {
			jedis.hset(device, "CHARGE_AH_TOTAL", CHARGE_AH_TOTAL);
		}
	}

	public void setDischargeAhTotal(String device, String DISCHARGE_AH_TOTAL) {
		try (Jedis jedis = pool.getResource()) {
			jedis.hset(device, "DISCHARGE_AH_TOTAL", DISCHARGE_AH_TOTAL);
		}
	}

	public void setChargeWhTotal(String device, String CHARGE_WH_TOTAL) {
		try (Jedis jedis = pool.getResource()) {
			jedis.hset(device, "CHARGE_WH_TOTAL", CHARGE_WH_TOTAL);
		}
	}

	public void setDischargeWhTotal(String device, String DISCHARGE_WH_TOTAL) {
		try (Jedis jedis = pool.getResource()) {
			jedis.hset(device, "DISCHARGE_WH_TOTAL", DISCHARGE_WH_TOTAL);
		}
	}

	public void setWorkState(String device, String WORK_STATE) {
		try (Jedis jedis = pool.getResource()) {
			jedis.hset(device, "WORK_STATE", WORK_STATE);
		}
	}

	public void updateRetry(String deviceId, long time) {
		try (Jedis jedis = pool.getResource()) {
			jedis.zadd("undiscovered_devices", time, deviceId);
		}
	}

	public void nextRetry(String deviceId, long duration) {
		try (Jedis jedis = pool.getResource()) {
			double currentRetry;
			try {
				currentRetry = jedis.zscore("nextretry", deviceId);
			} catch (NullPointerException e) {
				currentRetry = jedis.zscore("undiscovered_devices", deviceId);
			}
			System.out.println("Current Retry in DragonFly:" + currentRetry);
			long nextRetry = (long) currentRetry + duration;
			jedis.zadd("nextretry", (double) nextRetry, deviceId);
		}
	}

	public void markDiscovered(String deviceId) {
		try (Jedis jedis = pool.getResource()) {
			jedis.zrem("undiscovered_devices", deviceId);
			jedis.zrem("nextretry", deviceId);
			long now = System.currentTimeMillis();
			jedis.zadd("discovered_devices", now, deviceId);
		}
	}

	@Override
	public String getLatitude(String topic) {
		try (Jedis jedis = pool.getResource()) {
			return jedis.hget(topic, "X");
		}

	}

	@Override
	public String getLongitude(String topic) {
		try (Jedis jedis = pool.getResource()) {
			return jedis.hget(topic, "Y");
		}
	}

	@Override
	public int getIntUndiscovered() {
		try (Jedis jedis = pool.getResource()) {
			return (int) jedis.zcard("undiscovered_devices");
		}
	}

	@Override
	public List<String> getCurrentRetryReadyDevice(long now) {

		try (Jedis jedis = pool.getResource()) {
			return jedis.zrangeByScore("nextretry", 0, now);
		}
	}

	@Override
	public void clearDatabase() {
		try (Jedis jedis = pool.getResource()) {
			jedis.flushAll(); // optional
		}
		pool.close();

	}

	@Override
	public List<Tuple> getAllRetryDevices() {
		try (Jedis jedis = pool.getResource()) {
			return jedis.zrangeWithScores("undiscovered_devices", 0, -1);
		}
	}

	@Override
	public double getCreatedTime(String deviceId) {

		try (Jedis jedis = pool.getResource()) {
			return jedis.zscore("undiscovered_devices", deviceId);
		}
	}

	@Override
	public double getFieldValue(String device, String field) {
		double value;
		try (Jedis jedis = pool.getResource()) {
			try {
				value = Double.parseDouble(jedis.hget(device, field));
			} catch (NullPointerException e) {
				value = 0;
			}
		}
		return value;
	}

	@Override
	public List<String> getDiscoveryDeviceFilter(long duration) {
		List<String> StatsReadyDevice;
		try (Jedis jedis = pool.getResource()) {
			long now = System.currentTimeMillis();
			long time = now - duration;
			StatsReadyDevice = jedis.zrangeByScore("discovered_devices", 0, time);
			for (String device : StatsReadyDevice) {
				jedis.zadd("discovered_devices", now, device);
			}
		}
		return StatsReadyDevice;

	}

}
