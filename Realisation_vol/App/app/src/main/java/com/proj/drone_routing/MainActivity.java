package com.proj.drone_routing;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import dji.common.error.DJIError;
import dji.common.error.DJISDKError;
import dji.sdk.base.BaseComponent;
import dji.sdk.base.BaseProduct;
import dji.sdk.products.Aircraft;
import dji.sdk.sdkmanager.DJISDKManager;

/**
 * Starting Activity in which the SDK is loaded and the app is registered, it is possible to enter InputActivity from here with no device connected, but WorkingActivity can only be accessed if an aircraft is connected.
 */
public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getName();
    public static final String FLAG_CONNECTION_CHANGE = "dji_sdk_connection_change";
    private static BaseProduct mProduct;
    private static BaseProduct product;
    private Handler mHandler;
    private boolean isConnected =false;
    private Thread screenUpdater = null;
    private String param;

    private static final String[] REQUIRED_PERMISSION_LIST = new String[]{
//            Manifest.permission.VIBRATE,
            Manifest.permission.INTERNET,
            Manifest.permission.ACCESS_WIFI_STATE,
            Manifest.permission.WAKE_LOCK,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.CHANGE_WIFI_STATE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
//            Manifest.permission.BLUETOOTH,
//            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.READ_PHONE_STATE,
    };
    private List<String> missingPermission = new ArrayList<>();
    private AtomicBoolean isRegistrationInProgress = new AtomicBoolean(false);
    private static final int REQUEST_PERMISSION_CODE = 12345;
    private BaseComponent.ComponentListener mDJIComponentListener = new BaseComponent.ComponentListener() {

        @Override
        public void onConnectivityChange(boolean isConnected) {
            notifyStatusChange();
        }
    };
    public Button BtnStart=null;
    public Button BtnLoad;
    public TextView TextePrcp;
    private ArrayList<String> waypointList = null;
    public boolean isloaded = false;

    /**
     * Check et request permissions, initialise the displayed information and buttons
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // When the compile and target version is higher than 22, please request the following permission at runtime to ensure the SDK works well.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkAndRequestPermissions();
        }

        setContentView(R.layout.activity_main);

        mHandler = new Handler(Looper.getMainLooper());
        BtnStart = (Button) findViewById(R.id.button_Main_start);
        BtnLoad = (Button) findViewById(R.id.button_Main_Load);
        TextePrcp = findViewById(R.id.welcome_text);
        initUpdateThread(this);

    }

    /**
     * Only displayed when an aircraft is connected, launch the working activity, with parameters if existing.
     * @param view
     */
    public void btnMainStart (View view){
        stopUpdatethread();
        Intent WorkingAct = new Intent(MainActivity.this, WorkingActivity.class);
        WorkingAct.putExtra("isloaded",isloaded);
        if (isloaded){
            WorkingAct.putExtra("param",param);
        }
        startActivity(WorkingAct);
    }

    /**
     * Start the InputActivity and awaits for its result
     * @param view
     */
    public void btnMainLoad (View view){
        Intent InputAct = new Intent(MainActivity.this, InputActivity.class);
        startActivityForResult(InputAct,1);
    }

    /**
     * Stores the parameters returned by the InputActivity
     * @param requestCode
     * @param resultCode
     * @param dataIntent
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent dataIntent){
        if (requestCode == 1 && resultCode==RESULT_OK){
            param = dataIntent.getStringExtra("param");
            isloaded = true;
        }
    }
    /**
     * Checks if there is any missing permissions, and
     * requests runtime permission if needed.
     */
    private void checkAndRequestPermissions() {
        // Check for permissions
        for (String eachPermission : REQUIRED_PERMISSION_LIST) {
            if (ContextCompat.checkSelfPermission(this, eachPermission) != PackageManager.PERMISSION_GRANTED) {
                missingPermission.add(eachPermission);
            }
        }
        // Request for missing permissions
        if (missingPermission.isEmpty()) {
            startSDKRegistration();
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            showToast("Need to grant the permissions!");
            ActivityCompat.requestPermissions(this,
                    missingPermission.toArray(new String[missingPermission.size()]),
                    REQUEST_PERMISSION_CODE);
        }

    }

    /**
     * Result of runtime permission request
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // Check for granted permission and remove from missing list
        if (requestCode == REQUEST_PERMISSION_CODE) {
            for (int i = grantResults.length - 1; i >= 0; i--) {
                if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                    missingPermission.remove(permissions[i]);
                }
            }
        }
        // If there is enough permission, we will start the registration
        if (missingPermission.isEmpty()) {
            startSDKRegistration();
        } else {
            showToast("Missing permissions!!!");
        }
    }

    /**
     * Start the registration of the SDK, overrides onProductConnect and onProductDisconnect to check if an aircraft is connected
     */
    private void startSDKRegistration() {
        if (isRegistrationInProgress.compareAndSet(false, true)) {
            AsyncTask.execute(new Runnable() {
                @Override
                public void run() {
                    DJISDKManager.getInstance().registerApp(MainActivity.this.getApplicationContext(), new DJISDKManager.SDKManagerCallback() {
                        @Override
                        public void onRegister(DJIError djiError) {
                            if (djiError == DJISDKError.REGISTRATION_SUCCESS) {
                                showToast("Register Success");
                                DJISDKManager.getInstance().startConnectionToProduct();
                            } else {
                                showToast("Register sdk fails");
                            }
                            Log.v(TAG, djiError.getDescription());
                        }

                        @Override
                        public void onProductDisconnect() {
                            notifyStatusChange();
                            isConnected =false;
                        }
                        @Override
                        public void onProductConnect(BaseProduct baseProduct) {
                            notifyStatusChange();
                            if (baseProduct instanceof Aircraft)
                                isConnected=true;
                        }
                        @Override
                        public void onComponentChange(BaseProduct.ComponentKey componentKey, BaseComponent oldComponent,
                                                      BaseComponent newComponent) {

                            if (newComponent != null) {
                                newComponent.setComponentListener(new BaseComponent.ComponentListener() {

                                    @Override
                                    public void onConnectivityChange(boolean isConnected) {
                                        notifyStatusChange();
                                    }
                                });
                            }
                        }
                    });
                }
            });
        }
    }

    private void notifyStatusChange() {
        mHandler.removeCallbacks(updateRunnable);
        mHandler.postDelayed(updateRunnable, 500);
    }

    /**
     * Creates and enable the start button
     */
    public void enableStartButton(){
        if(BtnStart==null) {
            BtnStart = new Button(this);
            BtnStart.setId(R.id.button_Main_start);
            BtnStart.setBackgroundResource(R.drawable.rounded_button_blue);
            BtnStart.setText(R.string.button_start);
            BtnStart.setOnClickListener(new Button.OnClickListener() {
                public void onClick(View view) {
                    btnMainStart(view);
                }
            });
            ConstraintLayout ll = (ConstraintLayout) findViewById(R.id.layout_main);
            ConstraintLayout.LayoutParams lp = new ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.WRAP_CONTENT,
                    ConstraintLayout.LayoutParams.WRAP_CONTENT);
            lp.topToTop = (R.id.layout_main);
            lp.leftToLeft = (R.id.layout_main);
            lp.rightToRight = (R.id.layout_main);
            lp.bottomToBottom = (R.id.layout_main);
            lp.horizontalBias = (float) 0.6;
            lp.verticalBias = (float) 0.7;
            ll.addView(BtnStart, lp);
        }
    }

    /**
     * Deletes the start button
     */
    public void disableStartButton(){
        if (BtnStart !=null) {
            ConstraintLayout ll = (ConstraintLayout) findViewById(R.id.layout_main);
            if (null != ll) {
                ll.removeView(BtnStart);
                BtnStart = null;
            }
        }
    }
    public void stopUpdatethread(){
        if (screenUpdater!=null){
            screenUpdater.interrupt();
        }

    }

    /**
     * Initialises an UI thread that updates the start Button when it should be displayed or not ( if an aircraft is connected )
     * @param context
     */
    public void initUpdateThread(final Activity context){

        screenUpdater = new Thread(){
            @Override
            public void run(){
                try {
                    while (!isInterrupted()) {
                        Thread.sleep(1000);
                        context.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (isConnected){
                                    TextePrcp.setText(R.string.text_connected);
                                    enableStartButton();
                                }
                                else {
                                    TextePrcp.setText(R.string.start_text);
                                    disableStartButton();
                                }

                            }
                        });
                    }
                } catch (InterruptedException e) {
                }
            }
        };
        screenUpdater.start();
    }

    private Runnable updateRunnable = new Runnable() {

        @Override
        public void run() {
            Intent intent = new Intent(FLAG_CONNECTION_CHANGE);
            sendBroadcast(intent);
        }
    };

    private void showToast(final String toastMsg) {

        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), toastMsg, Toast.LENGTH_LONG).show();
            }
        });

    }
    public static synchronized BaseProduct getProductInstance() {
        product = DJISDKManager.getInstance().getProduct();
        return product;
    }
}
