package ohlisimulator.dao;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

import ohlisimulator.dao.DragonFlyBosunRegisters.DragonFlyBosunFieldName;

public class DragonFlyServiceRegisters {
	
	public static enum DataType {
	    INT,
	    LONG,
	    DOUBLE,
	    BOOLEAN
	}
	
	
	public static enum DragonFlyServiceFieldName {
		loadOrChange("LOAD_OR_CHANGE",DataType.INT),
	    batteryPercentage("BAT_CAP_SOC",DataType.INT),
	    batteryCurrentCapacity("BAT_CUR_ENERGY",DataType.LONG),
	    batteryCapacity("BAT_CAP_ENERGY",DataType.LONG),
	    batteryVoltage("BAT_U_100MV",DataType.DOUBLE),
	    batteryCurrent("BAT_I_10MA",DataType.DOUBLE),
	    deviceTemp("DEVICE_TEMP",DataType.DOUBLE),
	    loadVoltage("LED_U_100MV",DataType.DOUBLE),
	    loadCurrent("LED_I_10MA",DataType.DOUBLE),
	    loadPower("LED_POWER_1W",DataType.DOUBLE),
	    panelVoltage("PV_U_100MV",DataType.DOUBLE),
	    panelCurrent("PV_I_10MA",DataType.DOUBLE),
	    panelPower("CHARGE_POWER_1W",DataType.DOUBLE),
	    minBatVolDuringNight("MIN_BAT_U_IN_DAY_100MV",DataType.DOUBLE),
	    MaxBatVolDuringDay("MAX_BAT_U_IN_DAY_100MV",DataType.DOUBLE),
	    MaxChargingBatteryCurrentInDay("MAX_CHARGE_BAT_I_IN_DAY_10MA",DataType.DOUBLE),
	    MaxDischargingBatteryCurrentInDay("MAX_DISCHARGE_BAT_I_IN_DAY_10MA",DataType.DOUBLE),
	    MaxChargingBatteryPowerInDay("MAX_CHARGE_POWER_IN_DAY_1W",DataType.DOUBLE),
	    MaxDischargingBatteryPowerInDay("MAX_DISCHARGE_POWER_IN_DAY_1W",DataType.DOUBLE),
	    DailyChargingWh("CHARGE_WH_IN_DAY",DataType.DOUBLE),
	    DailyDischargingWh("DISCHARGE_WH_IN_DAY",DataType.DOUBLE),
	    DaysTotal("DAYS_TOTAL",DataType.INT),
	    BatOverDischargeTimes("BAT_OVER_DISC_TIMES",DataType.INT),
	    BatOverChargeTimes("BAT_OVER_CHARGE_TIMES",DataType.INT),
	    HistoricalCumulativeChargingAh("CHARGE_AH_TOTAL",DataType.DOUBLE),
	    HistoricalCumulativeDischargingAh("DISCHARGE_AH_TOTAL",DataType.DOUBLE),
	    HistoricalCumulativeChargingWh("CHARGE_WH_TOTAL",DataType.DOUBLE),
	    HistoricalCumulativeDischargingWh("DISCHARGE_WH_TOTAL",DataType.DOUBLE),
	    WorkState("WORK_STATE",DataType.INT),
	    DayLengthIs("DAY_LENGHT_1S",DataType.INT),
	    NightLengthIs("NIGHT_LENGHT_1S",DataType.INT),
		panelTemp("PANEL_TEMP",DataType.DOUBLE),
		
		
		ledFullCurrentValue("LED_CURRENT",DataType.DOUBLE),
		ledPowerSavingMode("LED_POWER_SAVING_MODE",DataType.INT),
		timePeriod1("LED_TIME1",DataType.INT),
		ledLevel1("LED_LEVEL1",DataType.INT),
		timePeriod2("LED_TIME2",DataType.INT),
		ledLevel2("LED_LEVEL2",DataType.INT),
		timePeriod3("LED_TIME3",DataType.INT),
		ledLevel3("LED_LEVEL3",DataType.INT),
		timePeriod4("LED_TIME4",DataType.INT),
		ledLevel4("LED_LEVEL4",DataType.INT),
		timePeriod5("LED_TIME5",DataType.INT),
		ledLevel5("LED_LEVEL5",DataType.INT),
		timePeriod6("LED_TIME6",DataType.INT),
		ledLevel6("LED_LEVEL6",DataType.INT),
		timePeriod7("LED_TIME7",DataType.INT),
		ledLevel7("LED_LEVEL7",DataType.INT),
		timePeriod8("LED_TIME8",DataType.INT),
		ledLevel8("LED_LEVEL8",DataType.INT),
		timePeriod9("LED_TIME9",DataType.INT),
		ledLevel9("LED_LEVEL9",DataType.INT),
		timePeriod10("LED_TIME10",DataType.INT),
		ledLevel10("LED_LEVEL10",DataType.INT),
		
			
		
		
		;
		private  final String field;
		private final DataType type;

		DragonFlyServiceFieldName(String field, DataType type) {
			this.field = field;
			this.type = type;
		}

		public  String getField() {
			return field;
		}
		public DataType getType() {
	        return type;
	    }
		
	}
	static {
		loadFieldToRegister();
	}
	public static Map<String, String> serviceToDao;
	
	
	private static void loadFieldToRegister() {
		serviceToDao = Arrays.stream(DragonFlyServiceFieldName.values())
				.collect(Collectors.toMap(DragonFlyServiceFieldName::name, DragonFlyServiceFieldName::getField));

	}
	
	public Object convert(String value, DataType type) {

	    switch (type) {
	        case INT:
	            return Integer.parseInt(value);

	        case LONG:
	            return Long.parseLong(value);

	        case DOUBLE:
	            return Double.parseDouble(value);

	        case BOOLEAN:
	            return Boolean.parseBoolean(value);

	        default:
	            return value;
	    }
	}
	
	
}
