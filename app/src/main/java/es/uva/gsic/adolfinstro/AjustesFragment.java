package es.uva.gsic.adolfinstro;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SeekBarPreference;

public class AjustesFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {

    SharedPreferences sharedPreferences;
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences, rootKey);
        SeekBarPreference seekBarPreference = findPreference(Ajustes.INTERVALODIA_pref);
        seekBarPreference.setMax(6);
        seekBarPreference = findPreference(Ajustes.INTERVALOHORA_pref);
        seekBarPreference.setMax(23);
        seekBarPreference = findPreference(Ajustes.INTERVALOMIN_pref);
        seekBarPreference.setMax(59);
        sharedPreferences = Ajustes.sharedPreferences;
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);
    }

    /**
     * Método que se ejecuta cuando el valor de una preferencia cambia
     * @param sharedPreferences preferencia
     * @param key llave
     */
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        /*Preference preference = findPreference(key);
        Log.i("onSharedPreferenceC", key);*/
    }
}
