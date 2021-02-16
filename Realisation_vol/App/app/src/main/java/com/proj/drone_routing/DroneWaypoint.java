package com.proj.drone_routing;

import android.support.annotation.NonNull;

public class DroneWaypoint {

    private float lat;
    private float lon;
    private float alt;
    private int direction;
    private float gimbal;
    private boolean photo;

    DroneWaypoint(float lat, float lon, float alt, int direction, float gimbal, boolean photo) {
        super();
        this.lat = lat;
        this.lon = lon;
        this.alt = alt;
        this.direction = direction;
        this.gimbal = gimbal;
        this.photo = photo;
    }

    float getLat() {
        return lat;
    }

    float getLon() {
        return lon;
    }

    float getAlt() {
        return alt;
    }

    int getDirection() {
        return direction;
    }

    float getGimbal() {
        return gimbal;
    }



    boolean isPhoto() {
        return photo;
    }
    @NonNull
    public String toString(){
        return Float.toString(lat)+":"+ Float.toString(lon)+":"+Float.toString(alt)+":"+ Integer.toString(direction)+":"+Float.toString(gimbal);
    }


}
