package com.example.macadoshus.spoton;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.widget.RelativeLayout;
import android.os.Bundle;
import android.widget.Toast;

import static com.example.macadoshus.spoton.R.string.score;

public class SpotOn extends Activity {

    private SpotOnView view;

    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private ShakeDetector mShakeDetector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        RelativeLayout layout = (RelativeLayout) findViewById(R.id.relativeLayout);
        view = new SpotOnView(this, getPreferences(Context.MODE_PRIVATE), layout);
        layout.addView(view, 0);


        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mShakeDetector = new ShakeDetector();
        mShakeDetector.setOnShakeListener(new ShakeDetector.OnShakeListener() {

            @Override
            public void onShake(int count) {
                handleShakeEvent(count);
            }
        });

    }

    public void handleShakeEvent(int count){

        if(view.isGameOver() && count == 1) {
            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(view.getContext());
            dialogBuilder.setTitle(R.string.highscorelist);
            dialogBuilder.setMessage(view.getHighscoreList().toString());
            dialogBuilder.show();
        }
    }
    @Override
    public void onPause(){
        mSensorManager.unregisterListener(mShakeDetector);
        super.onPause();
        view.pause();
    }

    public void onResume(){
        mSensorManager.registerListener(mShakeDetector, mAccelerometer,	SensorManager.SENSOR_DELAY_UI);
        super.onResume();
        view.resume(this);
    }

}
