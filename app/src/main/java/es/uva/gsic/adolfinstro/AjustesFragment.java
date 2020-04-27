package es.uva.gsic.adolfinstro;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.SeekBar;
import android.widget.Toast;

import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SeekBarPreference;

import es.uva.gsic.adolfinstro.auxiliar.Auxiliar;

public class AjustesFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {

    private PreferenceCategory preferenceCategory;
    private SeekBarPreference seekBarPreference;
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences, rootKey);
        seekBarPreference = findPreference(Ajustes.INTERVALO_pref);
        seekBarPreference.setMax(10);

        SharedPreferences sharedPreferences = Ajustes.sharedPreferences;
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);
        preferenceCategory = findPreference("categoriaPreferencias");
        onSharedPreferenceChanged(sharedPreferences, Ajustes.INTERVALO_pref);
        onSharedPreferenceChanged(sharedPreferences, Ajustes.NO_MOLESTAR_pref);
    }

    /**
     * Método que se ejecuta cuando el valor de una preferencia cambia
     * @param sharedPreferences preferencia
     * @param key llave
     */
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        switch (key){
            case Ajustes.INTERVALO_pref:
                preferenceCategory.setSummary(
                        String.format("%s %s",
                                getResources().getString(R.string.valorActual),
                                Auxiliar.valorTexto(getResources(), sharedPreferences.getInt(key, 0))));
                break;
            case Ajustes.NO_MOLESTAR_pref:
                seekBarPreference.setEnabled(!sharedPreferences.getBoolean(key, false));
                break;
        }
    }

}
