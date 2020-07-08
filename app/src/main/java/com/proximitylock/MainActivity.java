/* *************************************************************************************************
 * Project:     ProximitySensor
 * File:        MainActivity.java
 * Author:      SSQ66648
 * Description: Basic app to lock screen when generic/non-smart flip-cover is closed over the
 *              proximity sensor.
 * *************************************************************************************************
 * Notes:
 *          +
 * *************************************************************************************************
 * Major to-do list:
 *          +   implement sensor listening
 *          +   display distance from sensor
 *          +   find limit for trigger lock
 *          +   lock screen
 *          +   move process to (foreground) service
 *          +   (further) prevent proximity lock from triggering if other app is using sensor
 *                  (ie screen turn off on phone call etc)
 * Minor to-do list:
 *          +   read up on manifest backup process
 *          +   find "correct" way to handle possible null pointer warnings
 * ********************************************************************************************** */

package com.proximitylock;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    public static final String TAG = "ProximitySensor_main";

    //--------------------------------------
    // member variables
    //--------------------------------------
    private SensorManager sensorManager;
    private Sensor proximitySensor;
    private SensorEventListener proximityEventListener;


    //--------------------------------------
    // lifecycle
    //--------------------------------------
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        proximitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);

        //check if sensor is not available
        if (proximitySensor == null) {
            //alert user to error, end process
            Toast.makeText(this, "sensor not available", Toast.LENGTH_LONG).show();
            finish();
        }

        // listener
        proximityEventListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                //testing:
                //if detected proximity less than max range: change background colour
                if (event.values[0] < proximitySensor.getMaximumRange()) {
                    getWindow().getDecorView().setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.RED));
                } else {
                    getWindow().getDecorView().setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.GREEN));

                }
            }

            //unused but mandatory override
            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {

            }
        };

        //register listener with manager
        // TODO: 08/07/2020 read up on sampling period for listeners
        sensorManager.registerListener(proximityEventListener, proximitySensor, 2 * 1000 * 1000);
    }

    @Override
    protected void onPause() {
        super.onPause();
        //unregister listener
        sensorManager.unregisterListener(proximityEventListener);
    }
}
