package ohlisimulator.random;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;
import java.util.concurrent.ThreadLocalRandom;

public class Location {
	static {
		try {
			Properties props = new Properties();
			InputStream input = new FileInputStream("config/config.properties");

			props.load(input);
			latitude=Double.parseDouble(props.getProperty("latitude"+props.getProperty("region")));
			longitude=Double.parseDouble(props.getProperty("longitude"+props.getProperty("region")));
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
	static double latitude;
	static double longitude;
//	static double latitude=22.5744;
//	static double longitude=88.3629;
    public double generateRandomLatitude() {
//    	synchronized(this) {
    	return latitude+0.000002;
//    	}
//        return ThreadLocalRandom.current()
//                .nextDouble(-90.0, 90.0);
    }
    
    public double generateRandomLongitude() {
    	
    	return longitude+0.000002;
//    	return ThreadLocalRandom.current()
//                .nextDouble(-180.0, 180.0);
    }
}
