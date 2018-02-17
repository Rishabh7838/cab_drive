package com.hello.one.cabdrive.historyRecyclerview;

/**
 * Created by one on 10/21/2017.
 */

public class historyObjects {
    private String rideId;
    private String time;

    public historyObjects(String rideId, String time){
        this.rideId = rideId;
        this.time = time;
    }

    public String getRideId(){return rideId;}
    public void setRideId(String rideId) {
        this.rideId = rideId;
    }

    public String getTime(){return time;}
    public void setTime(String time) {
        this.time = time;
    }
}
