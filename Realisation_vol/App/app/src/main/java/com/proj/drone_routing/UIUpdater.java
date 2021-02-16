package com.proj.drone_routing;

import android.app.Activity;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.TextView;

import java.util.Locale;

import dji.common.battery.BatteryState;
import dji.common.flightcontroller.FlightControllerState;
import dji.sdk.base.BaseProduct;
import dji.sdk.flightcontroller.FlightController;
import dji.sdk.products.Aircraft;
import dji.sdk.sdkmanager.DJISDKManager;

import static java.lang.Math.pow;
import static java.lang.Math.sqrt;

/**
 * That class is managing the text information displayed in WorkingActivity, it displays text and manages information about the aircraft, such as speed and localisation
 */
class UIUpdater {
    private FlightController mFlightController = null;
    private String batteryLevel;
    private TextView batteryText;
    private Activity context;
    private double lat = 0;
    private double lon = 0;
    private float speedx;
    private float speedy;
    private float speedz;
    private TextView speedText;
    private String CurrentDisplayedSpeed;
    private TextView infoText;
    private String CurrentDisplayedInfo;
    private MMap map;
    private float rota=0;
    //update battery level text

    /**
     *
     * @param _context Context of the previous acitvity
     * @param batterie  TextView in which the battery level should be displayed
     * @param mapp Initialised MMap for further interaction
     * @param speed TextView in which the speed info should be written
     * @param info_texte TextView in which the common info will be written
     */
    UIUpdater(final Activity _context, TextView batterie, MMap mapp, TextView speed,TextView info_texte) {
        batteryText = batterie;
        context = _context;
        speedText=speed;
        initBattery();
        initFlightController();
        map = mapp;
        infoText = info_texte;
    }

    /**
     * Update the battery text field, using the batteryLevel field which is updated by the SDK
     */
    private void updateBatteryText() {
        int color;
        String toThis = batteryLevel;
        if (Integer.parseInt(toThis) > 50) {
            color = Color.GREEN;
        } else if (Integer.parseInt(toThis) > 25) {
            color = Color.YELLOW;
        } else {
            color = Color.RED;
        }
        batteryText.setText(toThis.concat("%"));
        batteryText.setTextColor(color);
    }

    /**
     * Initialisation of the battery level SDK callback
     */
    private void initBattery() {
        //Battery status
        DJISDKManager.getInstance().getProduct().getBattery().setStateCallback(new BatteryState.Callback() {
            @Override
            public void onUpdate(BatteryState djiBatteryState) {
                batteryLevel = Integer.toString(djiBatteryState.getChargeRemainingInPercent());
            }
        });
    }

    /**
     * Refresh the position of the aircraft on the Mmap
     */
    private void refreshMap() {
        if (map != null) {
            map.setDrone(lat, lon, rota);
            Log.e("Gaspard", "drone rota = "+Double.toString(rota));
            //Log.e("Gaspard", "Mise a jour de la pos du drone sur carte coord : "+Double.toString(lat)+":"+Double.toString(lon));
        }
    }

    /**
     * Refresh the speed text field with latest speed info
     */
    private void updateSpeedText(){
        String texte = " Speed : "+String.format("%.1f",sqrt(pow(speedx,2)+pow(speedy,2)))+" m/s Ascending : "+ Float.toString(speedz)+" m/s";
        if (CurrentDisplayedSpeed == null){
            CurrentDisplayedSpeed = texte;
            speedText.setText(texte);
            speedText.setTextColor(Color.WHITE);
        }
        else if ( texte.compareTo(CurrentDisplayedSpeed)!=0) {
            speedText.setText(texte);
            speedText.setTextColor(Color.WHITE);
            CurrentDisplayedSpeed = texte;
        }
    }

    /**
     *
     * @param _newText New text that should be displayed
     * Check if new Text is the same as the previous one, if it is not, update the displayedd info text
     */
    void updateInfoText(final String _newText){
        if (CurrentDisplayedInfo == null) {
            context.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    infoText.setText(_newText);
                    infoText.setTextColor(Color.WHITE);
                }
            });
        }
        else if ( _newText.compareTo(CurrentDisplayedInfo)!=0) {
            context.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    infoText.setText(_newText);
                    infoText.setTextColor(Color.WHITE);
                }
            });
        }
        CurrentDisplayedInfo =  _newText;
        Log.e("Gaspard", "New text ="+CurrentDisplayedInfo);
    }

    /**
     *
     * @param index index of the waypoint which marker should be red
     */
    void setMarkerRed(int index){
        map.makeWaypointRed(index);
    }

    /**
     *
     * @param index index of the waypoint which marker should be deleted
     */
    void removeMarker(int index){
        map.removeWaypoint(index);
    }

    /**
     *
     * @param x latitude
     * @param y longitude
     * @param _rota compass angle
     * update the fields for the localisation of the aircraft
     */
    private void setCoord(double x, double y, float _rota) {
        lat = x;
        lon = y;
        rota = _rota;
    }

    /**
     *
     * @param n new speed on x axis
     */
    private void setSpeedx(float n) {
        speedx = n;
    }

    /**
     *
     * @param n new speed on y axis
     */
    private void setSpeedy(float n) {
        speedy = n;
    }

    /**
     *
     * @param n new speed on z axis
     */

    private void setSpeedz(float n) {
        speedz = n;
    }

    /**
     * Initialisation of the FlightController, which monitors the state of the aircraft, our implemented callback gets the speed and the localisation of the aircraft, then update the displayed informations.
     */
    private void initFlightController() {
        BaseProduct product = MainActivity.getProductInstance();
        if (product != null && product.isConnected()) {
            if (product instanceof Aircraft) {
                mFlightController = ((Aircraft) product).getFlightController();
            }
        }
        if (mFlightController != null) {
            mFlightController.setStateCallback(
                    new FlightControllerState.Callback() {
                        @Override
                        public void onUpdate(@NonNull FlightControllerState djiFlightControllerCurrentState) {
                            //Log.e("Gaspard","Callback flight controller position  : " + Double.toString(lat)+":"+Double.toString(lon) +" speed x :"+Double.toString(speedx));
                            setCoord(djiFlightControllerCurrentState.getAircraftLocation().getLatitude(), djiFlightControllerCurrentState.getAircraftLocation().getLongitude(), djiFlightControllerCurrentState.getAircraftHeadDirection());
                            setSpeedx(djiFlightControllerCurrentState.getVelocityX());
                            setSpeedy(djiFlightControllerCurrentState.getVelocityY());
                            setSpeedz(djiFlightControllerCurrentState.getVelocityZ());
                            context.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    refreshMap();
                                    updateBatteryText();
                                    updateSpeedText();
                                }
                            });
                        }
                    }
            );
        }
    }
}
