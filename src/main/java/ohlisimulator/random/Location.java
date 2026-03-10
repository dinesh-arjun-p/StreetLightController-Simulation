package ohlisimulator.random;

import java.util.concurrent.ThreadLocalRandom;

public class Location {
	static double latitude=40.43;
	static double longitude= -74.01;
//	static double latitude=22.5744;
//	static double longitude=88.3629;
    public double generateRandomLatitude() {

    	return latitude+0.0002;
//        return ThreadLocalRandom.current()
//                .nextDouble(-90.0, 90.0);
    }
    
    public double generateRandomLongitude() {
    	
    	return longitude+0.0002;
//    	return ThreadLocalRandom.current()
//                .nextDouble(-180.0, 180.0);
    }
}
