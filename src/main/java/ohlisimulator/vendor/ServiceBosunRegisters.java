package ohlisimulator.vendor;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public class ServiceBosunRegisters {
	
	public static enum DataType {
	    INT,
	    LONG,
	    DOUBLE,
	    BOOLEAN
	}
		public static enum ServiceBosunFieldName {
			 LOAD_OR_CHANGE("loadOrChange", DataType.INT),
			    FAULT_CODE_1("faultCode1", DataType.INT),
			    FAULT_CODE_2("faultCode2", DataType.INT),

			    BAT_CAP_SOC("batteryPercentage", DataType.INT),
			    BAT_CUR_ENERGY("batteryCurrentCapacity", DataType.LONG),
			    BAT_CAP_ENERGY("batteryCapacity", DataType.LONG),

			    BAT_U_100MV("batteryVoltage", DataType.DOUBLE),
			    BAT_I_10MA("batteryCurrent", DataType.DOUBLE),
			    DEVICE_TEMP("deviceTemp", DataType.DOUBLE),

			    LED_U_100MV("loadVoltage", DataType.DOUBLE),
			    LED_I_10MA("loadCurrent", DataType.DOUBLE),
			    LED_POWER_1W("loadPower", DataType.DOUBLE),

			    PV_U_100MV("panelVoltage", DataType.DOUBLE),
			    PV_I_10MA("panelCurrent", DataType.DOUBLE),
			    CHARGE_POWER_1W("panelPower", DataType.DOUBLE),

			    DC_ON_OFF("dcOnOff", DataType.INT),

			    MIN_BAT_U_IN_DAY_100MV("minBatVolDuringNight", DataType.DOUBLE),
			    MAX_BAT_U_IN_DAY_100MV("maxBatVolDuringDay", DataType.DOUBLE),

			    MAX_CHARGE_BAT_I_IN_DAY_10MA("maxChargingBatteryCurrentInDay", DataType.DOUBLE),
			    MAX_DISCHARGE_BAT_I_IN_DAY_10MA("maxDischargingBatteryCurrentInDay", DataType.DOUBLE),

			    MAX_CHARGE_POWER_IN_DAY_1W("maxChargingBatteryPowerInDay", DataType.DOUBLE),
			    MAX_DISCHARGE_POWER_IN_DAY_1W("maxDischargingBatteryPowerInDay", DataType.DOUBLE),

			    CHARGE_AH_IN_DAY("dailyChargingAh", DataType.DOUBLE),
			    DISCHARGE_AH_IN_DAY("dailyDischargingAh", DataType.DOUBLE),

			    CHARGE_WH_IN_DAY("dailyChargingWh", DataType.DOUBLE),
			    DISCHARGE_WH_IN_DAY("dailyDischargingWh", DataType.DOUBLE),

			    DAYS_TOTAL("daysTotal", DataType.INT),
			    BAT_OVER_DISC_TIMES("batOverDischargeTimes", DataType.INT),
			    BAT_OVER_CHARGE_TIMES("batOverChargeTimes", DataType.INT),

			    CHARGE_AH_TOTAL("historicalCumulativeChargingAh", DataType.DOUBLE),
			    DISCHARGE_AH_TOTAL("historicalCumulativeDischargingAh", DataType.DOUBLE),

			    CHARGE_WH_TOTAL("historicalCumulativeChargingWh", DataType.DOUBLE),
			    DISCHARGE_WH_TOTAL("historicalCumulativeDischargingWh", DataType.DOUBLE),

			    WORK_STATE("workState", DataType.INT),

			    DAY_LENGHT_1S("dayLengthIs", DataType.INT),
			    NIGHT_LENGHT_1S("nightLengthIs", DataType.INT),

			    LED_CURRENT("ledFullCurrentValue", DataType.DOUBLE),
			    LED_POWER_SAVING_MODE("ledPowerSavingMode", DataType.INT),

			    LED_TIME1("timePeriod1", DataType.INT),
			    LED_LEVEL1("ledLevel1", DataType.INT),

			    LED_TIME2("timePeriod2", DataType.INT),
			    LED_LEVEL2("ledLevel2", DataType.INT),

			    LED_TIME3("timePeriod3", DataType.INT),
			    LED_LEVEL3("ledLevel3", DataType.INT),

			    LED_TIME4("timePeriod4", DataType.INT),
			    LED_LEVEL4("ledLevel4", DataType.INT),

			    LED_TIME5("timePeriod5", DataType.INT),
			    LED_LEVEL5("ledLevel5", DataType.INT),

			    LED_TIME6("timePeriod6", DataType.INT),
			    LED_LEVEL6("ledLevel6", DataType.INT),

			    LED_TIME7("timePeriod7", DataType.INT),
			    LED_LEVEL7("ledLevel7", DataType.INT),

			    LED_TIME8("timePeriod8", DataType.INT),
			    LED_LEVEL8("ledLevel8", DataType.INT),

			    LED_TIME9("timePeriod9", DataType.INT),
			    LED_LEVEL9("ledLevel9", DataType.INT),

			    LED_TIME10("timePeriod10", DataType.INT),
			    LED_LEVEL10("ledLevel10", DataType.INT);

			private final String field;
			private final DataType dataType;


			ServiceBosunFieldName(String field, DataType dataType) {
				this.field = field;
				this.dataType = dataType;
			}

			public String getField() {
				return field;
			}
			 public DataType getDataType() {
			        return dataType;
			    }
		}
		static {
			loadFieldToRegister();
		}
		static Map<String, String> vendorToField;
		
		
		private static void loadFieldToRegister() {
			vendorToField = Arrays.stream(ServiceBosunFieldName.values())
					.collect(Collectors.toMap(ServiceBosunFieldName::name, ServiceBosunFieldName::getField));

		}
		
		public static  Object convertToDbType(double value, DataType type) {

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
