package com.proj.drone_routing;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.ArrayList;

import dji.common.error.DJIError;
import dji.common.error.DJIMissionError;
import dji.common.mission.waypoint.Waypoint;
import dji.common.mission.waypoint.WaypointMission;
import dji.common.mission.waypoint.WaypointMissionDownloadEvent;
import dji.common.mission.waypoint.WaypointMissionExecutionEvent;
import dji.common.mission.waypoint.WaypointMissionFinishedAction;
import dji.common.mission.waypoint.WaypointMissionFlightPathMode;
import dji.common.mission.waypoint.WaypointMissionGotoWaypointMode;
import dji.common.mission.waypoint.WaypointMissionHeadingMode;
import dji.common.mission.waypoint.WaypointMissionState;
import dji.common.mission.waypoint.WaypointMissionUploadEvent;
import dji.common.model.LocationCoordinate2D;
import dji.common.util.CommonCallbacks;
import dji.sdk.base.BaseProduct;
import dji.sdk.flightcontroller.FlightController;
import dji.sdk.mission.waypoint.WaypointMissionOperator;
import dji.sdk.mission.waypoint.WaypointMissionOperatorListener;
import dji.sdk.products.Aircraft;


class DroneMission {


    private WaypointMission         m_wpMission;
    private WaypointMissionOperator m_wpMissionOp;
    private FlightController mFlightController;
    public ArrayList<Waypoint> waypoints;
    private UIUpdater uiUpdater = null;
    private int oldWp = 0;

    //////////////////////////////////////////////////////
    boolean generate(float lat, float lon,
                            float largeurBat, float longueurBat,
                            float alt, float angleBat, int nbWaypoints) {
        initFlightController();
        m_wpMissionOp = new WaypointMissionOperator();
        waypoints = new ArrayList<>();
        oldWp = 0;
        // Gets the waypoints.
        float kHelice    = 1f;
        float mSpeed = 6.0f;
        WaypointsCreator wpCreator = new WaypointsCreator();
        wpCreator.createWaypoints(waypoints, lat, lon, largeurBat, longueurBat, alt, angleBat,
                kHelice, nbWaypoints);

        Log.e("Gaspard","Number of Waypoint : " + Integer.toString(waypoints.size()));
        // Builds the mission.
        WaypointMission.Builder builder = new WaypointMission.Builder()
                .finishedAction(WaypointMissionFinishedAction.NO_ACTION)
                .headingMode(WaypointMissionHeadingMode.TOWARD_POINT_OF_INTEREST)
                .autoFlightSpeed(mSpeed)
                .maxFlightSpeed(mSpeed)
                .flightPathMode(WaypointMissionFlightPathMode.NORMAL)
                .waypointList(waypoints)
                .waypointCount(waypoints.size())
                .gotoFirstWaypointMode(WaypointMissionGotoWaypointMode.POINT_TO_POINT)
                .setPointOfInterest(new LocationCoordinate2D(lat,lon));
        DJIError error = builder.checkParameters();

        if (error ==null){
            m_wpMission = builder.build();
            return (m_wpMission != null);
        }
        else {
            String err;
            if (error == DJIMissionError.WAYPOINT_COUNT_NOT_VALID || DJIMissionError.WAYPOINT_DISTANCE_TOO_CLOSE==error){
                err = "too much waypoints!";
            }
            else {
                err = "";
            }
            sendTextToUI("Error while trying to generate the path : "+err);
            Log.e("Gaspard",error.getDescription());
            return false;
        }
    }

    /**
     *
     * @param _uiupdater UIUpdater that should be used to display info
     */
    void setUiUpdater(UIUpdater _uiupdater){
        uiUpdater = _uiupdater;
    }

    /**
     *
     * @param text Text to be displayed by the UIUpdater
     */
    private void sendTextToUI(String text){
        if ( uiUpdater != null){
            uiUpdater.updateInfoText(text);
        }
    }
    //////////////////////////////////////////////////////
    /**
     * The WaypointMissionOperatorListener is listening to the updates on the mission progress, it is used to get information about the running mission.
     */
    private WaypointMissionOperatorListener eventNotificationListener = new WaypointMissionOperatorListener() {
        @Override
        public void onDownloadUpdate(@NonNull WaypointMissionDownloadEvent downloadEvent) {
        }

        @Override
        public void onUploadUpdate(@NonNull WaypointMissionUploadEvent uploadEvent) {
                String progress;
                if ( uploadEvent.getProgress()!= null){
                    progress = " Waypoint "+Integer.toString(uploadEvent.getProgress().uploadedWaypointIndex + 1)+"/"+Integer.toString(waypoints.size());
                    sendTextToUI("Uploading waypoint "+progress+" to the aircraft");
                }
        }

        /**
         *
         * @param executionEvent
         * That method is used in order to get a feedback about the execution, but is also used in order to remove already visited Waypoints from the map view.
         */
        @Override
        public void onExecutionUpdate(@NonNull WaypointMissionExecutionEvent executionEvent) {
                if ( oldWp != executionEvent.getProgress().targetWaypointIndex){
                    uiUpdater.removeMarker(oldWp);
                    oldWp ++;
                    uiUpdater.setMarkerRed(oldWp);
                }
                String texte;
                switch (executionEvent.getProgress().executeState) {
                    case INITIALIZING:
                        texte = "Mission Started, flying to 1st waypoint";
                    break;
                    case MOVING:
                        texte = "Flying to waypoint "+Integer.toString(oldWp+1)+"/"+waypoints.size();
                    break;
                    case BEGIN_ACTION:
                        texte ="Preparing to shoot from waypoint "+Integer.toString(oldWp+1)+"/"+waypoints.size();
                        break;
                    case DOING_ACTION:
                        texte = "Shooting the subject, waypoint "+Integer.toString(oldWp+1)+"/"+waypoints.size();
                        break;
                    case FINISHED_ACTION:
                        texte = "Shooting done, waypoint "+Integer.toString(oldWp+1)+"/"+waypoints.size();
                        break;
                    case RETURN_TO_FIRST_WAYPOINT:
                        texte = "Mission done, flying to first waypoint";
                        break;
                    default:
                        texte = "Working";
                }
                sendTextToUI(texte);
        }

        @Override
        public void onExecutionStart() {
            sendTextToUI("Execution Started");
        }

        @Override
        public void onExecutionFinish(@Nullable final DJIError error) {
            sendTextToUI("Execution complete");
            uiUpdater.removeMarker(oldWp);
        }
    };
    //////////////////////////////////////////////////////

    /**
     * Load m_wpMission as a DJI Mission in the application, then  uploads it to the aircraft
     * @return Error generated by loadMission, null if none
     */
    private DJIError load() {
        DJIError error= m_wpMissionOp.loadMission(m_wpMission);
        if (error == null) {
            Log.e("Gaspard","loadWaypoint succeeded");
        } else {
            Log.e("Gaspard","loadWaypoint failed " + error.getDescription());
            return error;
        }
        m_wpMissionOp.addListener(eventNotificationListener);
        m_wpMissionOp.uploadMission(null);
        return null;
    }

    /**
     * Checks if the aircraft is flying, if it is not, take off, then load the mission, uploads it, awaits until it is done, then start it up and set the speed to 5 m/s
     */
    void start() {
        if (!mFlightController.getState().isFlying()) {
            sendTextToUI("Taking off!");
            mFlightController.startTakeoff(new CommonCallbacks.CompletionCallback() {
                public void onResult(@NonNull DJIError djiError) {
                    sendTextToUI("Took off!");
                }
            });
            //Attente de 5 secondes le temps du decollage
            synchronized (this) {
                try {
                    wait(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        //chargement de la mission et upload vers l'appareil
        DJIError error = load();
        while(m_wpMissionOp.getCurrentState()!= WaypointMissionState.READY_TO_EXECUTE){
            synchronized (this) {
                try {
                    wait(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        if (error==null){
            sendTextToUI("Mission is ready, starting...");
            //demarrage de la mission
            m_wpMissionOp.startMission(new CommonCallbacks.CompletionCallback() {
                @Override
                public void onResult(DJIError djiError) {
                    if (djiError != null){
                        sendTextToUI("Error while starting the mission");
                    }
                    else {
                        sendTextToUI("Starting complete");
                        m_wpMissionOp.setAutoFlightSpeed(5.0f, new CommonCallbacks.CompletionCallback() {
                            @Override
                            public void onResult(DJIError djiError) {
                                if ( djiError != null){
                                    sendTextToUI("Error while adjusting the speed");
                                }
                            }
                        });
                    }
                }
            });
        }
        else {
            sendTextToUI("Chargement de la mission impossible");
        }
    }

    /**
     * Pause the mission
     */
    void pause(){
        m_wpMissionOp.pauseMission(null);
        sendTextToUI("Mission paused");
    }

    /**
     * Resume the mission
     */
    void resume(){m_wpMissionOp.resumeMission(null);
    sendTextToUI("Mission resumed");}

    /**
     * Stops the mission
     */
    void stop(){m_wpMissionOp.stopMission(null);
    sendTextToUI("Mission stopped");}

    /**
     * gets the flightcontroller and stores it
     */
    private void initFlightController() {
        BaseProduct product = MainActivity.getProductInstance();
        if (product != null && product.isConnected()) {
            if (product instanceof Aircraft) {
                mFlightController = ((Aircraft) product).getFlightController();
            }
        }
    }

}