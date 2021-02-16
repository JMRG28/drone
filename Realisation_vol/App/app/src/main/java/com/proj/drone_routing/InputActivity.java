
package com.proj.drone_routing;
/*
 * Revoir graphisme disposition champs texte
 * separer la partie Algo en lui faisant une classe a elle
 * supprimer les fonction inutiles
 * bouton valider, et c'est d'ici qu'on chargera les waypoints donc renvoyer une mission DJI ( tester si on peut creer une mission DJI sans etre connecte a un appareil
 * changer le current coord en placage de point sur une map
 * implementer une OSM ici ( on pourra reutiliser la classe MapView qu'on aura deja fait)*/

import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.PopupMenu;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import org.osmdroid.config.Configuration;
import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.MapEventsOverlay;
import org.osmdroid.views.overlay.Marker;

/**
 * InputActivity is used for the users to enter the specification of the subject, that will be used be Working Activity to generate the mission.
 */

public class InputActivity extends AppCompatActivity {
    public MMap mmap;
    public Boolean chooseOnMap = true;
    public EditText w, l, h, lat, lon,ang,nb;
    public Button gen;
    public Intent intentact;
    public Marker addP = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_input);
        final Context ctx = getApplicationContext();
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));
        MapView map = findViewById(R.id.input_mapView);
        mmap = new MMap(map, ctx);
        w =  findViewById(R.id.tb_width);
        l =  findViewById(R.id.tb_length);
        h = findViewById(R.id.tb_height);
        lat = findViewById(R.id.tb_lat);
        lon = findViewById(R.id.tb_lon);
        ang = findViewById(R.id.tb_angle);
        nb = findViewById(R.id.tb_nb);
        gen = findViewById(R.id.button);

        intentact = new Intent();
        MapEventsReceiver mReceive = new MapEventsReceiver() {
            @Override
            public boolean singleTapConfirmedHelper(GeoPoint p) {

                // write your code here
                Log.d("loc", p.toString());

                return false;
            }


            public boolean longPressHelper(final GeoPoint p) {
                // write your code here
                if (chooseOnMap) {
                    Log.d("longPress", "LongPressed " + p.toString());
                    addP = new Marker(mmap.getMv());
                    addP.setPosition(p);
                    addP.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                    mmap.getMv().getOverlays().add(0, addP);
                    chooseOnMap = false;
                    lat.setText(Double.toString(p.getLatitude()));
                    lon.setText(Double.toString(p.getLongitude()));
//                    PopupMenu popup = new PopupMenu(InputActivity.this, mmap.getMv(), Gravity.NO_GRAVITY, R.attr.actionOverflowMenuStyle, 0);
//                    //PopupWindow popup=new PopupWindow(layout,LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT,true);
//                    //Inflating the Popup using xml file
//                    popup.getMenuInflater().inflate(R.menu.popup_menu, popup.getMenu());
//
//                    //registering popup with OnMenuItemClickListener
//                    popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
//                        public boolean onMenuItemClick(MenuItem item) {
//                            switch (item.getItemId()) {
//                                case R.id.addP:
//                                    addP = new Marker(mmap.getMv());
//                                    addP.setPosition(p);
//                                    addP.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
//                                    mmap.getMv().getOverlays().add(0, addP);
//                                    chooseOnMap = false;
//                                    lat.setText(Double.toString(p.getLatitude()));
//                                    lon.setText(Double.toString(p.getLongitude()));
//                                    return true;
//                                case R.id.cancel:
//                                    return true;
//                                default:
//                                    return false;
//                            }
//                        }
//                    });

//                    popup.show();//showing popup menu
                }
                return false;
            }
        };
        MapEventsOverlay OverlayEvents = new MapEventsOverlay(getBaseContext(), mReceive);
        mmap.getMv().getOverlays().add(OverlayEvents);
    }

    /**
     * Stores the parameters of the flight plan and exit the activity
     * @param view unused param
     */
    public void generate(View view) {

        Intent intent = new Intent();

        intent.putExtra("param",lat.getText()+":"+lon.getText()+":"+l.getText()+":"+w.getText()+":"+h.getText()+":"+ang.getText()+":"+nb.getText()+":"+"0");
        setResult(RESULT_OK,intent);
        finish();
    }

    public void getCurr(View view) {
        lat.setText(Double.toString(mmap.getLoc().getLatitude()));
        lon.setText(Double.toString(mmap.getLoc().getLongitude()));
    }


    public void remove(View v) {
        Log.d("remove", "remove Pressed");
        if (!chooseOnMap && !mmap.getMv().getOverlays().isEmpty()) {
            chooseOnMap=true;
            mmap.getMv().getOverlays().remove(0);
            mmap.getMv().invalidate();
            lat.setText("");
            lon.setText("");
        }
    }
}
