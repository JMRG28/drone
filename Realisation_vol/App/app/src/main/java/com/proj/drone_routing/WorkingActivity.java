package com.proj.drone_routing;
/*
* Separer d'un coté le code relatif a la camera
* on a les deux bouton load et strat, du coup renommer correctement les fonction associées aux boutons, appeller les nouvelles calsses
* et une fois le  plan de vol charger, remplacer les deux boutons par des boutons de controle ( play pause, stop, return ? )
* donc ces boutons implementent le SDK pour controler le planned vol,
* du coup il faut supprimer les boutons precedent, et les rappeller quand ils reservent
* suppriemr fonction inutiles*/
import android.content.Intent;
import android.preference.PreferenceManager;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.content.Context;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

//import com.google.android.gms.maps.MapView;

import org.osmdroid.config.Configuration;
import org.osmdroid.views.MapView;


/**
 * WorkingActivity is the running mission activity, from wich users can set up and start a mission, it allows user to keep control over the mission, displaying information and control buttons.
 */
public class WorkingActivity extends AppCompatActivity {
    private VideoFeed videoView =null;
    public Button load, start, pause, stop;
    private boolean isloaded = false;
    private UIUpdater uiUpdater = null;
    public MMap mmap = null;
    private DroneMission dronemission=null;
    private boolean ispause = false;

    /**
     *  Generates the droneMission with the given parameters and enable the start button
     * @param param lat:lon:width:length:height:orientation
     */
    public void LoadMission (String param){
        isloaded = true;
        mmap.clearWaypoint();
        dronemission = new DroneMission();
        dronemission.setUiUpdater(uiUpdater);
        String[] tab = param.split(":");
        float lat = Float.parseFloat(tab[0]);
        float lon = Float.parseFloat(tab[1]);
        float lar = Float.parseFloat(tab[2]);
        float longu = Float.parseFloat(tab[3]);
        float alt = Float.parseFloat(tab[4]);
        float angle = Float.parseFloat(tab[5]);
        int nb = Integer.parseInt(tab[6]);
        if (dronemission.generate(lat, lon, lar, longu, alt, angle, nb)) {
            mmap.addWaypoint(dronemission);
            enableStartButton();
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        isloaded = getIntent().getBooleanExtra("isloaded",false);
        initUI();
        if (isloaded){
            LoadMission(getIntent().getStringExtra("param"));
        }

    }

    /**
     * Puts the buttons back to default ( load, and start if the parameters are loaded )
     */
    public void Reset(){
        removeButton(stop);
        removeButton(pause);
        enableLoadButton();
        if (isloaded) enableStartButton();
    }
    public void onResume(){
        super.onResume();
        mmap.getMv().onResume();

    }

    public void onPause(){
        super.onPause();
        mmap.getMv().onPause();
        mmap.setCurrLoc();
    }

    /**
     * Initialises the MMap with the MapView
     */
    public void initMap(){
        final Context ctx = getApplicationContext();
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));
        mmap = new MMap((MapView) findViewById(R.id.mapView),ctx);
    }

    /**
     * Initialises the UiUpdater with the mapView and the text fieds
     */
    private void initUIUpdater(){
        uiUpdater = new UIUpdater(this, (TextView) findViewById(R.id.battery_level), mmap,(TextView) findViewById(R.id.info_speed), (TextView) findViewById(R.id.info_text));
    }

    /**
     * Initialises the layout of that activity with all of its components
     */
    private void initUI() {
        setContentView(R.layout.activity_woking);
        load = null;
        start = null;
        videoView = new VideoFeed((TextureView) findViewById(R.id.retour_video));
        initMap();
        initUIUpdater();
        enableLoadButton();
    }

    /**
     * Behavior when InputActivity returns
     * @param requestCode
     * @param resultCode
     * @param dataIntent
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent dataIntent){
        if (requestCode == 1 && resultCode==RESULT_OK){
            LoadMission(dataIntent.getStringExtra("param"));
        }
    }

    /**
     * Enable the Start button
     */
    public void enableStartButton(){
            start = new Button(this);
            start.setId(R.id.button_work_start);
            start.setBackgroundResource(R.drawable.rounded_button_blue);
            start.setText(R.string.button_start);
            start.setOnClickListener(new Button.OnClickListener(){
                public void onClick(View view){
                    onClickStart(view);
                }
            });
            ConstraintLayout ll = findViewById(R.id.layout_working);

            ConstraintLayout.LayoutParams lp = new ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.WRAP_CONTENT,
                    ConstraintLayout.LayoutParams.WRAP_CONTENT);
            lp.endToEnd=(R.id.layout_working);
            lp.bottomToBottom=(R.id.layout_working);
            ll.addView(start,lp);
    }

    /**
     * Enable the Load Button
     */
    public void enableLoadButton(){
            load = new Button(this);
            load.setId(R.id.button_work_load);
            load.setBackgroundResource(R.drawable.rounded_button_blue);
            load.setText(R.string.button_load);
            load.setOnClickListener(new Button.OnClickListener(){
                public void onClick(View view){
                    onClickload(view);
                }
            });
            ConstraintLayout ll = findViewById(R.id.layout_working);

            ConstraintLayout.LayoutParams lp = new ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.WRAP_CONTENT,
                    ConstraintLayout.LayoutParams.WRAP_CONTENT);
            lp.startToEnd=(R.id.retour_video);
            lp.bottomToBottom=(R.id.layout_working);
            ll.addView(load,lp);
        }

    /**
     *
     * @param button Button to be removedd from the current layout
     */
    public void removeButton(View button){
        ConstraintLayout ll = findViewById(R.id.layout_working);
        if ( null!= ll){
            ll.removeView(button);
        }
    }

    /**
     * Remove the Start and Load buttons and replaces them with Pause and Stop
     */
    public void refreshButtons(){
        removeButton(start);
        removeButton(load);
        stop = new Button(this);
        pause = new Button (this);
        stop.setId(R.id.button_stop);
        pause.setId(R.id.button_pause);
        stop.setBackgroundResource(R.drawable.rounded_button_blue);
        pause.setBackgroundResource(R.drawable.rounded_button_blue);
        pause.setText(R.string.pause);
        stop.setText(R.string.stop);
        stop.setOnClickListener(new Button.OnClickListener(){
            public void onClick(View view){
                stop();
            }
        });
        pause.setOnClickListener(new Button.OnClickListener(){
            public void onClick(View view){
                pause();
            }
        });
        ConstraintLayout ll = findViewById(R.id.layout_working);

        ConstraintLayout.LayoutParams lp = new ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.WRAP_CONTENT,
                ConstraintLayout.LayoutParams.WRAP_CONTENT);
        lp.startToEnd=(R.id.retour_video);
        lp.bottomToBottom=(R.id.layout_working);
        ConstraintLayout.LayoutParams lp2 = new ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.WRAP_CONTENT,
                ConstraintLayout.LayoutParams.WRAP_CONTENT);
        lp2.startToEnd=R.id.button_stop;
        lp2.bottomToBottom=(R.id.layout_working);
        ll.addView(stop, lp);
        ll.addView(pause,lp2);
    }

    /**
     * Starts the generated mission
     * @param view
     */
    public void onClickStart (View view) {
        if(isloaded) {
            uiUpdater.updateInfoText("Uploading the path to the aircraft");

            dronemission.start();
            refreshButtons();
            isloaded = false;
        }
    }

    /**
     * Open InputActivity and awaits for its result
     * @param view
     */
    public void onClickload (View view){
        Intent intent = new Intent(this, InputActivity.class);
        startActivityForResult(intent,1);
    }

    /**
     * Pauses the mission or resume it if it was paused
     */
    public void pause (){
        if ( ispause) {
            dronemission.resume();
            pause.setText(R.string.pause);
            ispause = false;
        }
        else {
            dronemission.pause();
            pause.setText(R.string.resume);
            ispause = true;
        }
    }

    /**
     * Stops the mission
     */
    public void stop (){
        dronemission.stop();
        Reset();
    }
}
