package com.proj.drone_routing;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.content.ContextCompat;

import org.osmdroid.api.IMapController;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import dji.common.mission.waypoint.Waypoint;

import static android.content.Context.LOCATION_SERVICE;

/**
 * That class is managing the mapView it is associated with, it is used to display the aircraft, user and waypoint position
 */
public class MMap  {



    private MapView mv;


    private Marker DroneLoc;
    private Marker user;
    private Marker subject;
    private Location loc;
    private Context ctx;
    private LocationManager mLocationManager;
    private GeoPoint currentLocation;
    private IMapController mapController;
    private ArrayList<Marker> listeWaypoint = new ArrayList<>();

    /**
     *
     * @return the MapView associated with the MMap
     */
    MapView getMv() {
        return mv;
    }

    /**
     *
     * @return the updated location of the application
     */
    Location getLoc() {
        setCurrLoc();
        return loc;
    }

    MMap(MapView mv_,Context ctx_){
        this.mv=mv_;
        this.ctx=ctx_;
        init();
        setCurrLoc();
    }

    /**
     * Initialises the Open Street Map View
     */
    private void init(){
        mv.setTileSource(TileSourceFactory.MAPNIK);
        mv.setMultiTouchControls(true);
        mapController = mv.getController();
        mapController.setZoom(19.0);
    }

    /**
     * Get the current localisation of the smartphone and updates its position on the map
     */
    void setCurrLoc(){

        int off = 0;
        try {
            off = Settings.Secure.getInt(ctx.getContentResolver(), Settings.Secure.LOCATION_MODE);
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
        }
        if(off==0){
            Intent onGPS = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            ctx.startActivity(onGPS);
        }

        loc = getLastKnownLocation();

        if( loc != null ) {
            if ( user == null) user = new Marker(mv);
            currentLocation = new GeoPoint(loc.getLatitude(), loc.getLongitude());
            user.setPosition(currentLocation);
            user.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
            user.setIcon(ctx.getResources().getDrawable(R.drawable.user));
            mv.getOverlays().add(user);
            mapController.setCenter(currentLocation);
        }

    }

    /**
     * If GPS localisation is not available that method is called, which returns a less precise localisation
     * @return
     */
    private Location getLastKnownLocation() {
        mLocationManager = (LocationManager)ctx.getSystemService(LOCATION_SERVICE);
        List<String> providers = mLocationManager.getProviders(true);
        Location bestLocation;
        bestLocation = null;
        Location l=null;
        for (String provider : providers) {
            if(ContextCompat.checkSelfPermission( ctx, Manifest.permission.ACCESS_FINE_LOCATION ) == PackageManager.PERMISSION_GRANTED) {
                l = mLocationManager.getLastKnownLocation(provider);
            }
            if (l == null) {
                continue;
            }
            if (bestLocation == null || l.getAccuracy() < bestLocation.getAccuracy()) {
                bestLocation = l;
            }
        }
        return bestLocation;
    }


    public class MyLocationListener implements LocationListener {

        public void onLocationChanged(Location location) {
            currentLocation = new GeoPoint(location);
            showCurrentLoc();
        }


        public void onProviderDisabled(String provider) {
        }

        public void onProviderEnabled(String provider) {
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
        }
    }

    /**
     * Updates the position of the user's smartPhone on the map
     */
    private void showCurrentLoc(){
        if (user == null){
            user= new Marker(mv);
        }
        user.setPosition(currentLocation);
        user.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        user.setIcon(ctx.getResources().getDrawable(R.drawable.user));
        mv.getOverlays().add(user);
    }

    /**
     * Displays on the map the list of Waypoints that composes the DroneMission.
     * First Waypoint is red.
     * @param dm DroneMission
     */
    void addWaypoint(DroneMission dm){
        for (int i = dm.waypoints.size()-1; i >=0; i --){
            Waypoint w = dm.waypoints.get(i);
            GeoPoint point = new GeoPoint(w.coordinate.getLatitude(),w.coordinate.getLongitude());
            Marker passage = new Marker(mv);
            listeWaypoint.add(passage);
            passage.setPosition(point);
            passage.setAnchor(Marker.ANCHOR_CENTER,Marker.ANCHOR_CENTER);
            if (w == dm.waypoints.get(0)){
                passage.setIcon(ctx.getResources().getDrawable(R.drawable.first_wp));
            }
            else {
                passage.setIcon(ctx.getResources().getDrawable(R.drawable.wp));
            }
            mv.getOverlays().add(passage);
        }
        Collections.reverse(listeWaypoint);
    }

    /**
     *
     * @param index Index of the waypoint that should be made red
     */

    void makeWaypointRed(int index){
        listeWaypoint.get(index).setIcon(ctx.getResources().getDrawable(R.drawable.first_wp));
    }

    /**
     *
     * @param index Index of the waypoints that should be removed from the map
     */
    void removeWaypoint(int index){
        listeWaypoint.get(index).remove(mv);
    }

    /**
     * Removes all the waypoint from the map.
     */
    void clearWaypoint(){
        if (listeWaypoint.size()!=0){
            for ( Marker m : listeWaypoint){
                m.remove(mv);
            }
            listeWaypoint.clear();
        }
    }

    /**
     * Set the aircraft location on the map, set the heading direction of the aircraft too.
     * @param x latitude
     * @param y longitude
     * @param rotation rotation ( 0 is north )
     */
    void setDrone(double x, double y, float rotation){
        if (DroneLoc == null){
            DroneLoc = new Marker(mv);
        }
        GeoPoint point = new GeoPoint(x,y);
        DroneLoc.setPosition(point);
        DroneLoc.setIcon(ctx.getResources().getDrawable(R.drawable.drone));
        DroneLoc.setAnchor(Marker.ANCHOR_CENTER,Marker.ANCHOR_CENTER);
        DroneLoc.setRotation(rotation);
        mv.getOverlays().add(DroneLoc);
    }
}
