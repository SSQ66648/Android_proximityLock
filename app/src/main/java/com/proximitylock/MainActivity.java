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
 *          +   todo display distance from sensor
 *          +   todo find limit for trigger lock
 *          +   todo lock screen
 *          +   todo move process to (foreground) service
 *          +   todo (further) prevent proximity lock from triggering if other app is using sensor
 *                  (ie screen turn off on phone call etc)
 * Minor to-do list:
 *          +   read up on manifest backup process
 *          +   find "correct" way to handle possible null pointer warnings
 * ********************************************************************************************** */

package com.proximitylock;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

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
        Log.d(TAG, "onCreate: ");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        proximitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);

        //check if sensor is not available
        if (proximitySensor == null) {
            Log.e(TAG, "onCreate: Sensor not found/ not available: end process");

            //alert user to error, end process
            Toast.makeText(this, "sensor not available", Toast.LENGTH_LONG).show();
            finish();
        }

        // listener
        proximityEventListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                Log.d(TAG, "onSensorChanged: ");
                //testing:
                //if detected proximity less than max range: change background colour
                if (event.values[0] < proximitySensor.getMaximumRange()) {
                    Log.d(TAG, "onSensorChanged: value below max range: trigger");
                    getWindow().getDecorView().setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.RED));
                } else {
                    Log.d(TAG, "onSensorChanged: value exceeds max range: revert");
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
        Log.d(TAG, "onPause: unregistering sensor listener");
        //unregister listener
        sensorManager.unregisterListener(proximityEventListener);
    }
}
