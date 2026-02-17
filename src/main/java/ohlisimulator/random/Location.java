package ohlisimulator.random;

import java.util.concurrent.ThreadLocalRandom;

public class Location {

    public double generateRandomLatitude() {


        return ThreadLocalRandom.current()
                .nextDouble(-90.0, 90.0);
    }
    
    public double generateRandomLongitude() {
    	
    	return ThreadLocalRandom.current()
                .nextDouble(-180.0, 180.0);
    }
}
