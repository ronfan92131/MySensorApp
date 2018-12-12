package com.doyen.fans.mysensorapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.hardware.TriggerEvent;
import android.hardware.TriggerEventListener;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements SensorEventListener{
    public static final String TAG = "MainActivity";

    SensorManager mSensorManager;
    Sensor mCountSensor;
    Sensor mAccelerometerSensor;

    TextView tvStepCount;
    TextView xAccelValue, yAccelValue, zAccelValue;
    Ringtone alarm ;

    boolean running = false;
    private TriggerEventListener mTriggerEventListener;
    Button btnUpdate;
    Button btnResetSteps;

    public static final String SHARED_PREFS = "sharedPrefs";
    public static final String KEY_SAVEDSTEPS = "keySavedSteps";

    int stepsOfEvent;  //hardware counter of steps since last device power cycle
    int stepsOfSaved;  //steps in the shared pref, since last app button reset
    int stepsCurrent = 0;  //stepsOfEvent - stepsOfSaved

    int crashThreshHold = 50; // bigger than 5 times Gravity
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mCountSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);

        tvStepCount = findViewById(R.id.stepCounter);
        tvStepCount.setText("0");
        stepsOfSaved = loadExistingSteps();

        btnResetSteps = findViewById(R.id.btnStepsReset);
        btnResetSteps.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                //reset the stepcount to 0
                stepsOfSaved += stepsCurrent;
                saveExistingSteps(stepsOfSaved);
                tvStepCount.setText("0");
            }
        });

        xAccelValue = findViewById(R.id.tv_accelerometer_x);
        yAccelValue = findViewById(R.id.tv_accelerometer_y);
        zAccelValue = findViewById(R.id.tv_accelerometer_z);
        //accelerometer
        mAccelerometerSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mSensorManager.registerListener(MainActivity.this, mAccelerometerSensor, SensorManager.SENSOR_DELAY_NORMAL);
        alarm = RingtoneManager.getRingtone(this, RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM) );
        Log.d(TAG, "onCreate");
    }
    public int loadExistingSteps(){
        SharedPreferences prefs = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        return prefs.getInt(KEY_SAVEDSTEPS, 0);
    }

    public void saveExistingSteps(int steps){
        SharedPreferences prefs = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(KEY_SAVEDSTEPS, steps);
        editor.apply();
    }

    @Override
    protected void onResume() {
        super.onResume();

        running = true;
        if(mCountSensor!=null){
            mSensorManager.registerListener(this, mCountSensor, SensorManager.SENSOR_DELAY_UI);
        }else{
            Toast.makeText(MainActivity.this,"Sensor not found!", Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        running = false;

        //unregister? the hardhware will stop counting. so don;t do it here.
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        //check what event first
       if(event.sensor.getName().contains("Accelerometer"))
       {
           xAccelValue.setText("x: " + event.values[0]);
           yAccelValue.setText("y: " + event.values[1]);
           zAccelValue.setText("z: " + event.values[2]);
           if((event.values[0] > crashThreshHold) || (event.values[1] > crashThreshHold) || (event.values[2] > crashThreshHold )) {
               xAccelValue.setTextColor(Color.RED);
               yAccelValue.setTextColor(Color.RED);
               zAccelValue.setTextColor(Color.RED);
               Log.d(TAG, "CRASH ALER: " + event.values[0] + " " + event.values[1] + " " + event.values[2] );
               Toast.makeText(MainActivity.this,"CRASH ALERT!!!", Toast.LENGTH_SHORT).show();
               alarm.play();
           }else{
               xAccelValue.setTextColor(Color.BLACK);
               yAccelValue.setTextColor(Color.BLACK);
               zAccelValue.setTextColor(Color.BLACK);
               alarm.stop();
           }
       }else {
           if (running) {
               stepsOfEvent = (int) event.values[0];
               stepsCurrent = stepsOfEvent - stepsOfSaved;
               tvStepCount.setText(stepsCurrent + "");
           }
       }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }
}
