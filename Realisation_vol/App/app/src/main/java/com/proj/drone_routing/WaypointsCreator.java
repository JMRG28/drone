package com.proj.drone_routing;

import android.util.Log;

import java.util.ArrayList;
import dji.common.mission.waypoint.Waypoint;
import dji.common.mission.waypoint.WaypointAction;
import dji.common.mission.waypoint.WaypointActionType;

class WaypointsCreator {

	//////////////////////////////////////////////////////
	int createWaypoints(ArrayList<Waypoint> waypointsDji,
							   float coordInitX,
							   float coordInitY,
							   float longueurBat,
							   float largeurBat,
							   float altBat,
							   float angleBat,
							   float kHelice,
							   int nbWaypoint) {

		ArrayList<DroneWaypoint> droneWaypoints = AlgoPlannificator.algo(
				coordInitX, coordInitY,
				longueurBat, largeurBat,
				altBat,
				kHelice,
				nbWaypoint,
				angleBat);

		int res = 0;
		if ( droneWaypoints!=null) {
			for (DroneWaypoint wp : droneWaypoints) {
				Log.e("Gaspard Waypoints ", wp.toString());
				Waypoint wpDji = new Waypoint(wp.getLat(), wp.getLon(), wp.getAlt());
				wpDji.heading = wp.getDirection();
				wpDji.gimbalPitch = wp.getGimbal();

				if (wp.isPhoto()) {
					WaypointAction wpAction = new WaypointAction(WaypointActionType.START_TAKE_PHOTO, 6);
					wpDji.addAction(wpAction);
				}

				waypointsDji.add(wpDji);

				res++;

			}

			return res;
		}
		else{
			return 0;
		}
	}

}
