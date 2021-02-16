package planification;

public class Waypoint {
	
	private double lat;
	private double lon;
	private double alt;
	private double direction;
	private double gimbal;
	private boolean photo;
	
	public Waypoint(double lat, double lon, double alt, double turnDegree, double gimbal, boolean photo) {
		super();
		this.lat = lat;
		this.lon = lon;
		this.alt = alt;
		this.direction = direction;
		this.gimbal = gimbal;
		this.photo = photo;
	}

	public double getLat() {
		return lat;
	}

	public double getLon() {
		return lon;
	}

	public double getAlt() {
		return alt;
	}

	public double getDirection() {
		return direction;
	}

	public double getGimbal() {
		return gimbal;
	}

	public boolean isPhoto() {
		return photo;
	}

	
	
}
	
