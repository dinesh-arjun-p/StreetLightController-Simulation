package ohlisimulator.dao;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;


public class DragonFlyServiceRegisters {
	
	public static enum DataType {
	    INT,
	    LONG,
	    DOUBLE,
	    BOOLEAN
	}
	
	
	public static enum DragonFlyServiceFieldName {
		loadOrChange("LOAD_OR_CHANGE",DataType.INT),
		cannotUpdate("CANNOT_UPDATE",DataType.INT),
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
	    maxBatVolDuringDay("MAX_BAT_U_IN_DAY_100MV",DataType.DOUBLE),
	    maxChargingBatteryCurrentInDay("MAX_CHARGE_BAT_I_IN_DAY_10MA",DataType.DOUBLE),
	    maxDischargingBatteryCurrentInDay("MAX_DISCHARGE_BAT_I_IN_DAY_10MA",DataType.DOUBLE),
	    maxChargingBatteryPowerInDay("MAX_CHARGE_POWER_IN_DAY_1W",DataType.DOUBLE),
	    maxDischargingBatteryPowerInDay("MAX_DISCHARGE_POWER_IN_DAY_1W",DataType.DOUBLE),
	    DailyChargingWh("CHARGE_WH_IN_DAY",DataType.DOUBLE),
	    DailyDischargingWh("DISCHARGE_WH_IN_DAY",DataType.DOUBLE),
	    daysTotal("DAYS_TOTAL",DataType.INT),
	    batOverDischargeTimes("BAT_OVER_DISC_TIMES",DataType.INT),
	    batOverChargeTimes("BAT_OVER_CHARGE_TIMES",DataType.INT),
	    HistoricalCumulativeChargingAh("CHARGE_AH_TOTAL",DataType.DOUBLE),
	    HistoricalCumulativeDischargingAh("DISCHARGE_AH_TOTAL",DataType.DOUBLE),
	    HistoricalCumulativeChargingWh("CHARGE_WH_TOTAL",DataType.DOUBLE),
	    HistoricalCumulativeDischargingWh("DISCHARGE_WH_TOTAL",DataType.DOUBLE),
	    workState("WORK_STATE",DataType.INT),
	    dayLengthIs("DAY_LENGHT_1S",DataType.INT),
	    nightLengthIs("NIGHT_LENGHT_1S",DataType.INT),
		panelTemp("PANEL_TEMP",DataType.DOUBLE),
		
		systemVoltage("SYSTEM_VOLTAGE",DataType.INT),
		BatteryFullChargeVoltage("BAT_FULL_U_100MV",DataType.DOUBLE),
		BatteryEmptyChargeVoltage("BAT_EMPTY_U_100MV",DataType.DOUBLE),
		BatteryOverChargeVoltage("BAT_OVER_VOLT",DataType.DOUBLE),
		BatteryOverChargeReturnVoltage("BAT_RECHARGE_U_100MV",DataType.DOUBLE),
		BatteryOverDischargeVoltage("BAT_OVER_DISCH_U",DataType.DOUBLE),
		BatteryOverDischargeReturnVoltage("BAT_OVER_DISCHARGE_BACK_U",DataType.DOUBLE),
		
		
		
		manualModePower("MANUAL_MODE_POWER",DataType.INT),
		manualModeTime("MANUAL_MODE_TIME",DataType.LONG),
		
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
		
			
		
		dataScheduler("DATA_SCHEDULER",DataType.LONG),
		
		
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
	
	public static Object convertToDbType(double value, DataType type) {

		switch (type) {

        case INT:
            return (int) value;

        case LONG:
            return (long)value;

        case DOUBLE:
            return value;

        case BOOLEAN:
            return value > 0;

        default:
            return value;
		}
	}
	
}
