/* *************************************************************************************************
 * Project:     ProximitySensor
 * File:        MainActivity.java
 * Author:      SSQ66648
 * Description: Basic app to lock screen when generic/non-smart flip-cover is closed over the
 *              proximity sensor.
 * *************************************************************************************************
 * Notes:
 *      +   Test device (VFD 710: API 26) only has binary proximity sensor so exact value cannot be
 *          refined to help prevent accidental triggering.
 *      +   WakeLock seems to override android auto-sleep function (ie 15 second sleep timer begins after screen turns back on from proximity-off regardless of how long the sensor has been covered)
 * *************************************************************************************************
 * Major to-do list:
 *  +   todo move process to (foreground) service
 *  +   todo (further) prevent proximity lock from triggering if other app is using sensor
 *                  (ie screen turn off on phone call etc)
 * Minor to-do list:
 *  +   read up on manifest backup process
 *  +   find "correct" way to handle possible null pointer warnings
 *  +   replace string literal with resources
 * ********************************************************************************************** */

package com.proximitylock;

import android.app.Activity;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    //--------------------------------------
    // constants
    //--------------------------------------
    public static final String TAG = "ProximitySensor_main";
    public static final int RESULT_ENABLE = 11;


    //--------------------------------------
    // member variables
    //--------------------------------------
    private SensorManager sensorManager;
    private Sensor proximitySensor;
    private SensorEventListener proximityEventListener;
    private DevicePolicyManager devicePolicyManager;
    private ComponentName componentName;

    //layout views
    private TextView txt_sensorValueText;
    private Button btn_enable;
    private Button btn_disable;


    //--------------------------------------
    // lifecycle
    //--------------------------------------
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate: ");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //initialise views
        txt_sensorValueText = findViewById(R.id.txt_prox_distance);
        btn_enable = findViewById(R.id.button_policy_enable);
        btn_disable = findViewById(R.id.button_policy_disable);

        //set view listeners
        btn_enable.setOnClickListener(this);
        btn_disable.setOnClickListener(this);

        //create sensor and manager
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
//                Log.d(TAG, "onSensorChanged: sensor value: " + event.values[0]);
                //display raw senor value
//                txt_sensorValueText.setText(String.format("Proximity value:\n%s", event.values[0]));

                //if detected proximity less than max range: change background colour
                if (event.values[0] < proximitySensor.getMaximumRange()) {
                    Log.d(TAG, "onSensorChanged: value below max range: trigger");
                    getWindow().getDecorView().setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.RED));

                    //lock screen if permissions are in place
                    if (devicePolicyManager.isAdminActive(componentName)) {
                        Log.d(TAG, "onSensorChanged: Admin: active: locking screen...");
                        devicePolicyManager.lockNow();
                    } else {
                        Log.d(TAG, "onSensorChanged: Admin: not active: notify user");
                        Toast.makeText(MainActivity.this, "Please enable Admin Device permissions", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.d(TAG, "onSensorChanged: value exceeds max range: revert");
                    getWindow().getDecorView().setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.GREEN));
                }
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {
                //unused but mandatory override
            }
        };

        //create policy manager and component of DeviceAdminReceiver subclass
        devicePolicyManager = (DevicePolicyManager) getSystemService(DEVICE_POLICY_SERVICE);
        //able to check if security policies associated with class are active
        componentName = new ComponentName(this, MyAdmin.class);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: registering listener");
        //register proximity listener with manager
        // TODO: 08/07/2020 read up on sampling period for listeners
        sensorManager.registerListener(proximityEventListener, proximitySensor, 2 * 1000 * 1000);

        //show/hide buttons depending on policy status
        boolean isPolicyActive = devicePolicyManager.isAdminActive(componentName);
        btn_disable.setVisibility(isPolicyActive ? View.VISIBLE : View.GONE);
        btn_enable.setVisibility(isPolicyActive ? View.GONE : View.VISIBLE);
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause: unregistering sensor listener");
        //unregister listener
        sensorManager.unregisterListener(proximityEventListener);
    }


    //--------------------------------------
    // listeners
    //--------------------------------------
    @Override
    public void onClick(View v) {
        if (v == btn_enable) {
            //gain admin permission to lock device
            Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
            intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, componentName);
            intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "Permission is required to be able to lock screen");
            startActivityForResult(intent, RESULT_ENABLE);
        } else {
            //remove permission and swap button visibility
            devicePolicyManager.removeActiveAdmin(componentName);
            btn_disable.setVisibility(View.GONE);
            btn_enable.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //handle result of permission enabling (switch is probably superfluous: remove if not used)
        switch (requestCode) {
            case RESULT_ENABLE:
                if (resultCode == Activity.RESULT_OK) {
                    Log.d(TAG, "onActivityResult: Device Admin Enabled");
                    Toast.makeText(this, "Device Admin enabled", Toast.LENGTH_SHORT).show();
                } else {
                    Log.e(TAG, "onActivityResult: error enabling Device Admin...");
                    Toast.makeText(this, "An error occurred while trying to enable Device Admin", Toast.LENGTH_SHORT).show();
                }
                break;
            //space for future additional cases
            default:
                Log.w(TAG, "onActivityResult: no expected result returned: default case triggered...");
        }
    }
}
