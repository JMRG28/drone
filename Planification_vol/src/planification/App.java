package planification;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

public class App {

	public static void main(String[] args) throws IOException {
		
		Scanner sc = new Scanner(System.in);
		String line = sc.nextLine();
		
		double lat;
		double lon;
		double longueur;
		double largeur;
		double alt;
		double k;
		double nbWaypoint;
		
		String[] parts = line.split(" ");
		lat = Double.parseDouble(parts[0]);
		lon = Double.parseDouble(parts[1]);
		longueur = Double.parseDouble(parts[2]);
		largeur = Double.parseDouble(parts[3]);
		alt = Double.parseDouble(parts[4]);
		k = Double.parseDouble(parts[5]);
		nbWaypoint= Double.parseDouble(parts[6]);

		double zForEachWaypoint = alt / nbWaypoint;
		
		ArrayList<Waypoint> waypoints = new ArrayList<Waypoint>();
		
		double r = Math.max(longueur, largeur) + 3;
		double z = 0;
		double theta = 0;
		double gimbal = 0;
		
		for(int i = 0 ; i < nbWaypoint ; ++i) {
			
			theta = z / k;
			double x = r * Math.cos(theta);
			double y = r * Math.sin(theta);
			
			waypoints.add(new Waypoint(x, y, z, theta, gimbal, true));
			System.out.println("x = " + x + " y = " + y + " z = " + z + " t = " + theta);
			
			z += zForEachWaypoint;
			
		}

		z = alt;
		for(int i = 0 ; i < nbWaypoint ; ++i) {
			
			theta = z / k + 180;
			double x = r * Math.cos(theta);
			double y = r * Math.sin(theta);
			
			waypoints.add(new Waypoint(x, y, z, theta, gimbal, true));
			System.out.println("x = " + x + " y = " + y + " z = " + z + " t = " + theta);
			
			z -= zForEachWaypoint;
			
		}
		
		BufferedWriter writer = new BufferedWriter(new FileWriter("./flightplan.txt"));
		for(int i = 0 ; i < waypoints.size() ; ++i) {
			Waypoint wp = waypoints.get(i);
			String s = wp.getLat() + ":" + wp.getLon() + ":" + wp.getAlt() + ":" + wp.getDirection() + ":" + wp.getGimbal() + ":" + wp.isPhoto() + "\n";
		    writer.write(s);
		}
	    writer.close();

	}

}
