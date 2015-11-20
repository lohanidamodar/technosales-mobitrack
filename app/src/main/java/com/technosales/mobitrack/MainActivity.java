/*
 * Copyright 2012 - 2015 Anton Tananaev (anton.tananaev@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.technosales.mobitrack;

import android.Manifest;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.preference.TwoStatePreference;
import android.telephony.TelephonyManager;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

@SuppressWarnings("deprecation")
public class MainActivity extends PreferenceActivity implements OnSharedPreferenceChangeListener {

    public static final String KEY_DEVICE = "id";
    public static final String KEY_ADDRESS = "address";
    public static final String KEY_PORT = "port";
    public static final String KEY_INTERVAL = "interval";
    public static final String KEY_PROVIDER = "provider";
    public static final String KEY_STATUS = "status";

    private static final int PERMISSIONS_REQUEST_LOCATION = 2;

    private SharedPreferences sharedPreferences;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (BuildConfig.HIDDEN_APP) {
            removeLauncherIcon();
        }

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        addPreferencesFromResource(R.xml.preferences);
        initPreferences();

        if (sharedPreferences.getBoolean(KEY_STATUS, false)) {
            startTrackingService(true, false);
        }
    }

    private void removeLauncherIcon() {
        String className = MainActivity.class.getCanonicalName().replace(".MainActivity", ".Launcher");
        ComponentName componentName = new ComponentName(getPackageName(), className);
        PackageManager packageManager = getPackageManager();
        if (packageManager.getComponentEnabledSetting(componentName) != PackageManager.COMPONENT_ENABLED_STATE_DISABLED) {
            packageManager.setComponentEnabledSetting(
                    componentName, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setIcon(android.R.drawable.ic_dialog_alert);
            builder.setMessage(getString(R.string.hidden_alert));
            builder.setPositiveButton(android.R.string.ok, null);
            builder.show();
        }
    }

    private boolean hasPermission(String permission) {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP_MR1) {
            return true;
        }
        return checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    protected void onResume() {
        super.onResume();
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(this);
    }

    private void setPreferencesEnabled(boolean enabled) {
        PreferenceScreen preferenceScreen = getPreferenceScreen();
        preferenceScreen.findPreference(KEY_DEVICE).setEnabled(enabled);
        preferenceScreen.findPreference(KEY_INTERVAL).setEnabled(enabled);
        preferenceScreen.findPreference(KEY_PROVIDER).setEnabled(enabled);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(KEY_STATUS)) {
            if (sharedPreferences.getBoolean(KEY_STATUS, false)) {
                startTrackingService(true, false);
            } else {
                stopTrackingService();
            }
        } else if (key.equals(KEY_DEVICE)) {
            findPreference(KEY_DEVICE).setSummary(sharedPreferences.getString(KEY_DEVICE, null));
        }
        findPreference(KEY_DEVICE).setSummary(sharedPreferences.getString(KEY_DEVICE, null));
        findPreference(KEY_ADDRESS).setSummary(sharedPreferences.getString(KEY_ADDRESS, null));
        findPreference(KEY_PORT).setSummary(sharedPreferences.getString(KEY_PORT, null));
        findPreference(KEY_INTERVAL).setSummary(sharedPreferences.getString(KEY_INTERVAL, null));
        findPreference(KEY_PROVIDER).setSummary(sharedPreferences.getString(KEY_PROVIDER, null));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.status) {
            startActivity(new Intent(this, StatusActivity.class));
            return true;
        } else if (item.getItemId() == R.id.about) {
            startActivity(new Intent(this, AboutActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void initPreferences() {
        findPreference(KEY_ADDRESS).setEnabled(false);
        findPreference(KEY_PORT).setEnabled(false);
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);


        if (!sharedPreferences.contains(KEY_DEVICE)) {
            TelephonyManager tm = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
            String number = tm.getLine1Number();
            if (number == null) {
                number = "98510";
            }
            sharedPreferences.edit().putString(KEY_DEVICE, number).commit();
            ((EditTextPreference) findPreference(KEY_DEVICE)).setText(number);
        }
        findPreference(KEY_DEVICE).setSummary(sharedPreferences.getString(KEY_DEVICE, null));
        findPreference(KEY_ADDRESS).setSummary(sharedPreferences.getString(KEY_ADDRESS, null));
        findPreference(KEY_PORT).setSummary(sharedPreferences.getString(KEY_PORT, null));
        findPreference(KEY_INTERVAL).setSummary(sharedPreferences.getString(KEY_INTERVAL, null));
        findPreference(KEY_PROVIDER).setSummary(sharedPreferences.getString(KEY_PROVIDER, null));
    }

    private void startTrackingService(boolean checkPermission, boolean permission) {
        if (checkPermission) {
            Set<String> missingPermissions = new HashSet<String>();
            if (!hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)) {
                missingPermissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
            }
            if (!hasPermission(Manifest.permission.ACCESS_COARSE_LOCATION)) {
                missingPermissions.add(Manifest.permission.ACCESS_COARSE_LOCATION);
            }
            if (missingPermissions.isEmpty()) {
                permission = true;
            } else {
                requestPermissions(missingPermissions.toArray(new String[missingPermissions.size()]), PERMISSIONS_REQUEST_LOCATION);
                return;
            }
        }

        if (permission) {
            if (isValidMobile(sharedPreferences.getString(KEY_DEVICE, null))) {
                setPreferencesEnabled(false);
                startService(new Intent(this, TrackingService.class));
            } else {
                Toast.makeText(this, "Invalid mobile number, please enter your number", Toast.LENGTH_SHORT).show();
                showInputMobileDialog();
            }
        } else {
            errorStartingTrackingService();
        }
    }

    private void errorStartingTrackingService() {
        sharedPreferences.edit().putBoolean(KEY_STATUS, false).commit();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            TwoStatePreference preference = (TwoStatePreference) findPreference(KEY_STATUS);
            preference.setChecked(false);
        } else {
            CheckBoxPreference preference = (CheckBoxPreference) findPreference(KEY_STATUS);
            preference.setChecked(false);
        }
    }
    private void stopTrackingService() {
        stopService(new Intent(this, TrackingService.class));
        setPreferencesEnabled(true);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == PERMISSIONS_REQUEST_LOCATION) {
            startTrackingService(false, grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                    (permissions.length < 2 || grantResults[1] == PackageManager.PERMISSION_GRANTED));
        }
    }

    private boolean isValidMobile(String phone) {
        boolean check = false;
        if (!Pattern.matches("[a-zA-Z]+", phone)) {
            if (phone.length() != 10) {
                check = false;
            } else {
                if (phone.charAt(0) != '9' || (phone.charAt(1) != '8' && phone.charAt(1) != '7'))
                    check = false;
                else
                    check = true;
            }
        } else {
            check = false;
        }
        return check;
    }

    private void showInputMobileDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Mobile Number");

// Set up the input
        final EditText input = new EditText(this);
// Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        input.setInputType(InputType.TYPE_CLASS_PHONE);
        builder.setView(input);

// Set up the buttons
        builder.setPositiveButton("SET", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String phone = input.getText().toString();
                sharedPreferences.edit().putString(KEY_DEVICE, phone).commit();
                findPreference(KEY_DEVICE).setSummary(sharedPreferences.getString(KEY_DEVICE, null));
                startTrackingService(true, false);
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                errorStartingTrackingService();
            }
        });

        builder.show();
    }

}
