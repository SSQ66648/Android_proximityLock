/* *************************************************************************************************
 * Project:     ProximitySensor
 * File:        MyAdmin.java
 * Author:      SSQ66648
 * Description: Subclass of DeviceAdminReceiver: allows ability to react on enabling and disabling
 *              of app security policies by user.
 * *************************************************************************************************
 * Notes:
 *      +
 * *************************************************************************************************
 * Major to-do list:
 *      +
 * Minor to-do list:
 *       +
 * ********************************************************************************************** */
package com.proximitylock;

import android.app.admin.DeviceAdminReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import androidx.annotation.NonNull;

public class MyAdmin extends DeviceAdminReceiver {

    @Override
    public void onEnabled(@NonNull Context context, @NonNull Intent intent) {
        super.onEnabled(context, intent);
        Toast.makeText(context, "Device Admin: enabled", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDisabled(@NonNull Context context, @NonNull Intent intent) {
        super.onDisabled(context, intent);
        Toast.makeText(context, "Device Admin: disabled", Toast.LENGTH_SHORT).show();
    }
}
