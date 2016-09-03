package com.ch.popularmovies;

import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.util.Log;

public class SettingsActivity extends PreferenceActivity
        implements Preference.OnPreferenceChangeListener {

    private final String LOG_TAG = SettingsActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pref_general);

        bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_order_by_key)));
    }

    public void bindPreferenceSummaryToValue(Preference pref){
        pref.setOnPreferenceChangeListener(this);

        Log.v(LOG_TAG, ">> " + PreferenceManager.getDefaultSharedPreferences(pref.getContext()).getString(pref.getKey(), ""));

        onPreferenceChange(pref, PreferenceManager
            .getDefaultSharedPreferences(pref.getContext())
            .getString(pref.getKey(), ""));
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValuePreferenceObject) {
        String newValue = newValuePreferenceObject.toString();

        if (preference instanceof ListPreference) {
            updateListPreferenceSummary(preference, newValue);
            return true;
        }

        return false;
    }

    private void updateListPreferenceSummary(Preference preference, String newValue) {
        ListPreference listPreference = (ListPreference) preference;
        int prefixIndex = listPreference.findIndexOfValue(newValue);
        if (prefixIndex >= 0) {
            preference.setSummary(listPreference.getEntries()[prefixIndex]);
        }
    }
}
