package ohlisimulator.dao;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

import ohlisimulator.vendor.Bosun.Register;

public class DragonFlyBosunRegisters {
	public static enum DragonFlyBosunFieldName {
		LOAD_OR_CHANGE("LOAD_OR_CHANGE"),
		FAULT_CODE_1("FAULT_CODE_1"),
		FAULT_CODE_2("FAULT_CODE_2"),
	    BAT_CAP_SOC("BAT_CAP_SOC"),
	    BAT_CUR_ENERGY("BAT_CUR_ENERGY"),
	    BAT_CAP_ENERGY("BAT_CAP_ENERGY"),
	    BAT_U_100MV("BAT_U_100MV"),
	    BAT_I_10MA("BAT_I_10MA"),
	    DEVICE_TEMP("DEVICE_TEMP"),
	    LED_U_100MV("LED_U_100MV"),
	    LED_I_10MA("LED_I_10MA"),
	    LED_POWER_1W("LED_POWER_1W"),
	    PV_U_100MV("PV_U_100MV"),
	    PV_I_10MA("PV_I_10MA"),
	    CHARGE_POWER_1W("CHARGE_POWER_1W"),
	    DC_ON_OFF("DC_ON_OFF"),
	    MIN_BAT_U_IN_DAY_100MV("MIN_BAT_U_IN_DAY_100MV"),
	    MAX_BAT_U_IN_DAY_100MV("MAX_BAT_U_IN_DAY_100MV"),
	    MAX_CHARGE_BAT_I_IN_DAY_10MA("MAX_CHARGE_BAT_I_IN_DAY_10MA"),
	    MAX_DISCHARGE_BAT_I_IN_DAY_10MA("MAX_DISCHARGE_BAT_I_IN_DAY_10MA"),
	    MAX_CHARGE_POWER_IN_DAY_1W("MAX_CHARGE_POWER_IN_DAY_1W"),
	    MAX_DISCHARGE_POWER_IN_DAY_1W("MAX_DISCHARGE_POWER_IN_DAY_1W"),
	    CHARGE_AH_IN_DAY("CHARGE_AH_IN_DAY"),
	    DISCHARGE_AH_IN_DAY("DISCHARGE_AH_IN_DAY"),
	    CHARGE_WH_IN_DAY("CHARGE_WH_IN_DAY"),
	    DISCHARGE_WH_IN_DAY("DISCHARGE_WH_IN_DAY"),
	    DAYS_TOTAL("DAYS_TOTAL"),
	    BAT_OVER_DISC_TIMES("BAT_OVER_DISC_TIMES"),
	    BAT_OVER_CHARGE_TIMES("BAT_OVER_CHARGE_TIMES"),
	    CHARGE_AH_TOTAL("CHARGE_AH_TOTAL"),
	    DISCHARGE_AH_TOTAL("DISCHARGE_AH_TOTAL"),
	    CHARGE_WH_TOTAL("CHARGE_WH_TOTAL"),
	    DISCHARGE_WH_TOTAL("DISCHARGE_WH_TOTAL"),
	    WORK_STATE("WORK_STATE"),
	    FAULT_CODE_A_1("FAULT_CODE_A_1"),
	    FAULT_CODE_A_2("FAULT_CODE_A_2"),
	    MAX_DEVICE_TEMP("MAX_DEVICE_TEMP"),
	    MIN_DEVICE_TEMP("MIN_DEVICE_TEMP"),
	    DAY_LENGHT_1S("DAY_LENGHT_1S"),
	    NIGHT_LENGHT_1S("NIGHT_LENGHT_1S");


		private final String field;

		DragonFlyBosunFieldName(String field) {
			this.field = field;
		}

		public String getField() {
			return field;
		}
	}
	static {
		loadFieldToRegister();
	}
	static Map<String, String> vendorToField;
	
	
	private static void loadFieldToRegister() {
		vendorToField = Arrays.stream(DragonFlyBosunFieldName.values())
				.collect(Collectors.toMap(DragonFlyBosunFieldName::name, DragonFlyBosunFieldName::getField));

	}
}
