package es.uva.gsic.adolfinstro;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.Spanned;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.preference.EditTextPreference;
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
        seekBarPreference.setMax(15);

        SharedPreferences sharedPreferences = Ajustes.sharedPreferences;
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);
        preferenceCategory = findPreference("categoriaPreferencias");
        onSharedPreferenceChanged(sharedPreferences, Ajustes.INTERVALO_pref);
        onSharedPreferenceChanged(sharedPreferences, Ajustes.NO_MOLESTAR_pref);
        PreferenceCategory datosUsuario=findPreference("datosUsuario");
        datosUsuario.setSummary(Login.firebaseAuth.getUid());
        /*EditTextPreference et = findPreference("nombre");
        et.setSummary(Login.firebaseAuth.getCurrentUser().getDisplayName());
        et = findPreference("correo");
        et.setSummary(Login.firebaseAuth.getCurrentUser().getEmail());*/
        final EditTextPreference hashtag = findPreference(Ajustes.HASHTAG_pref);
        if(hashtag!=null)
            hashtag.setOnBindEditTextListener(new EditTextPreference.OnBindEditTextListener() {
                @Override
                public void onBindEditText(@NonNull EditText editText) {
                    editText.setFilters(new InputFilter[]{
                            new InputFilter.LengthFilter(15)});
                }
            });
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
                String valorActual = getResources().getString(R.string.valorActual);
                String valorTexto = Auxiliar.valorTexto(getResources(), sharedPreferences.getInt(key, 5));
                preferenceCategory.setSummary(
                        String.format("%s %s",
                                valorActual,
                                valorTexto));
                break;
            case Ajustes.NO_MOLESTAR_pref:
                seekBarPreference.setEnabled(!sharedPreferences.getBoolean(key, false));
                break;
            default:
                break;
        }
    }

}
