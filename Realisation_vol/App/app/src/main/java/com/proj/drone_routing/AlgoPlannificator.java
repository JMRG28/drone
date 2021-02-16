package com.proj.drone_routing;

import android.util.Log;

import java.util.ArrayList;

class AlgoPlannificator {
    //Nb de waypoints entre 2 et 99
    //distance waypoint entre 2 et 1000 m
    static ArrayList<DroneWaypoint> algo(float coordInitX,
                                                float coordInitY,
                                                float longueurBat,
                                                float largeurBat,
                                                float altBat,
                                                float kHelice,
                                                int nbWaypoint,
                                                float angleBat) {
        if ( nbWaypoint > 99 || nbWaypoint < 2){
            return null;
        }

        ArrayList<DroneWaypoint> waypoints = new ArrayList<>();

        float z = 3;
        float theta = 0;
        float gimbal = 0;
        float buffer = 0;
        int nbwp2 = nbWaypoint/2;
        int nbwp1 = nbWaypoint-nbwp2;
        angleBat=(float)(Math.toRadians(angleBat)+Math.PI/2);
        float delta = (float)Math.toRadians((kHelice*360)/(nbwp1-1));
        double zForEachWaypoint = altBat / (nbwp1-1);

        for(int i = 0 ; i < nbwp1;i++) {

            float x = xToLat(coordInitX ,buffer+(float)(longueurBat * Math.cos(theta)* Math.cos(angleBat) - largeurBat* Math.sin(theta)* Math.sin(angleBat)));
            float y = yToLon(coordInitY, buffer+(float)(longueurBat* Math.cos(theta)* Math.sin(angleBat) + largeurBat* Math.sin(theta)* Math.cos(angleBat)));

            waypoints.add(new DroneWaypoint(x, y, z, 0, gimbal, true));
            Log.e("Gaspard","Waypoint cree up : "+Double.toString(x)+":"+Double.toString(y)+":"+Double.toString(z));
            z += zForEachWaypoint;
            theta+=delta;

        }

        z = altBat;
        theta=(float)Math.PI/2;
        delta = (float)Math.toRadians((float)(kHelice*360)/(nbwp2-1));
         zForEachWaypoint = altBat / (nbwp2-1);
        for(int i = 0 ; i < nbwp2;i++) {

            if(z<0){
                z=0;
            }
            float x = xToLat(coordInitX ,buffer+(float)(longueurBat * Math.cos(theta)* Math.cos(angleBat) - largeurBat* Math.sin(theta)* Math.sin(angleBat)));
            float y = yToLon(coordInitY, buffer+(float)(longueurBat* Math.cos(theta)* Math.sin(angleBat) + largeurBat* Math.sin(theta)* Math.cos(angleBat)));
            waypoints.add(new DroneWaypoint(x, y, z, 0, gimbal, true));
            Log.e("Gaspard","Waypoint cree down : "+Double.toString(x)+":"+Double.toString(y)+":"+Double.toString(z));
            z -= zForEachWaypoint;
            theta+=delta;

        }

        return waypoints;

    }

    private static double radTerre = 6378.137;
    private static double m = (1 / ((2 * Math.PI / 360) * radTerre)) / 1000;
    private static float xToLat(float latInit, float x) {
        return latInit + (x * (float)m);
    }

    private static float yToLon(float lonInit, float y) {
        return lonInit + (float)((y * m) / Math.cos(lonInit * (Math.PI / 180)));
    }

}