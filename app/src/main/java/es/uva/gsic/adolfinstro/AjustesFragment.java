package es.uva.gsic.adolfinstro;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

public class AjustesFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {

    SharedPreferences sharedPreferences;
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences, rootKey);
        sharedPreferences = Ajustes.sharedPreferences;
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);
    }

    /**
     * Método que se ejecuta cuando el valor de una preferencia cambia
     * @param sharedPreferences
     * @param key
     */
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        /*Preference preference = findPreference(key);
        Log.i("onSharedPreferenceC", key);*/
    }
}
