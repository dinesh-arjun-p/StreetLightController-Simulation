package ohlisimulator.dao;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

import ohlisimulator.dao.DragonFlyBosunRegisters.DragonFlyBosunFieldName;

public class DragonFlyServiceRegisters {
	public static enum DragonFlyServiceFieldName {
		loadOrChange("LOAD_OR_CHANGE"),
	    batteryPercentage("BAT_CAP_SOC"),
	    batteryCurrentCapacity("BAT_CUR_ENERGY"),
	    batteryCapacity("BAT_CAP_ENERGY"),
	    batteryVoltage("BAT_U_100MV"),
	    batteryCurrent("BAT_I_10MA"),
	    deviceTemp("DEVICE_TEMP"),
	    loadVoltage("LED_U_100MV"),
	    loadCurrent("LED_I_10MA"),
	    loadPower("LED_POWER_1W"),
	    panelVoltage("PV_U_100MV"),
	    panelCurrent("PV_I_10MA"),
	    panelPower("CHARGE_POWER_1W"),
	    minBatVolDuringNight("MIN_BAT_U_IN_DAY_100MV"),
	    MaxBatVolDuringDay("MAX_BAT_U_IN_DAY_100MV"),
	    MaxChargingBatteryCurrentInDay("MAX_CHARGE_BAT_I_IN_DAY_10MA"),
	    MaxDischargingBatteryCurrentInDay("MAX_DISCHARGE_BAT_I_IN_DAY_10MA"),
	    MaxChargingBatteryPowerInDay("MAX_CHARGE_POWER_IN_DAY_1W"),
	    MaxDischargingBatteryPowerInDay("MAX_DISCHARGE_POWER_IN_DAY_1W"),
	    DailyChargingWh("CHARGE_WH_IN_DAY"),
	    DailyDischargingWh("DISCHARGE_WH_IN_DAY"),
	    DaysTotal("DAYS_TOTAL"),
	    BatOverDischargeTimes("BAT_OVER_DISC_TIMES"),
	    BatOverChargeTimes("BAT_OVER_CHARGE_TIMES"),
	    HistoricalCumulativeChargingAh("CHARGE_AH_TOTAL"),
	    HistoricalCumulativeDischargingAh("DISCHARGE_AH_TOTAL"),
	    HistoricalCumulativeChargingWh("CHARGE_WH_TOTAL"),
	    HistoricalCumulativeDischargingWh("DISCHARGE_WH_TOTAL"),
	    WorkState("WORK_STATE"),
	    DayLengthIs("DAY_LENGHT_1S"),
	    NightLengthIs("NIGHT_LENGHT_1S"),
		panelTemp("PANEL_TEMP"),
		
		
		ledFullCurrentValue("LED_CURRENT"),
		ledLevel("LED_LEVEL"),
		
		;
		private  final String field;

		DragonFlyServiceFieldName(String field) {
			this.field = field;
		}

		public  String getField() {
			return field;
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
}
