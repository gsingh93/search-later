package com.gulshansingh.googlelater;

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
        bindPreferenceSummaryToValue(findPreference("enable_notifications"));
    }

    private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener =
            new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object value) {
            if (preference.getKey().equals("enable_notifications")) {
                Boolean b = (Boolean) value;
                if (b) {
                    preference.setSummary("Notifications are enabled");
                } else {
                    preference.setSummary("Notifications are disabled");
                }
            }

            return true;
        }
    };

    private static void bindPreferenceSummaryToValue(Preference preference) {
        preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);

        Object value = null;
        String key = preference.getKey();
        if (key.equals("enable_notifications")) {
            value = PreferenceManager
                    .getDefaultSharedPreferences(preference.getContext())
                    .getBoolean(key, true);
        }

        sBindPreferenceSummaryToValueListener.onPreferenceChange(preference, value);
    }
}
