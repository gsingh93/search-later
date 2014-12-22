package com.gulshansingh.googlelater;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

public class SettingsActivity extends PreferenceActivity {
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        setupSimplePreferencesScreen();
    }

    private void setupSimplePreferencesScreen() {
        addPreferencesFromResource(R.xml.pref_general);
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        sp.registerOnSharedPreferenceChangeListener(sBindPreferenceSummaryToValueListener);

        sBindPreferenceSummaryToValueListener.onSharedPreferenceChanged(sp, "enable_notifications");
        sBindPreferenceSummaryToValueListener.onSharedPreferenceChanged(sp, "reminder_interval_dialog");
    }

    private SharedPreferences.OnSharedPreferenceChangeListener sBindPreferenceSummaryToValueListener =
            new SharedPreferences.OnSharedPreferenceChangeListener() {
                @Override
                public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                    Preference pref = findPreference(key);
                    if (key.equals("enable_notifications")) {
                        boolean b = sharedPreferences.getBoolean(key, true);
                        if (b) {
                            pref.setSummary("Notifications are enabled");
                        } else {
                            pref.setSummary("Notifications are disabled");
                        }
                    } else if (key.equals("reminder_interval_dialog")) {
                        SharedPreferences prefs = PreferenceManager
                                .getDefaultSharedPreferences(getApplicationContext());
                        int timeAmount = prefs.getInt("time_amount", 1);
                        String timeUnit = prefs.getString("time_unit", "Days").toLowerCase();
                        String summary = "Reminder interval time is " + timeAmount + " " + timeUnit;
                        pref.setSummary(summary);
                    }
                }
            };
}
