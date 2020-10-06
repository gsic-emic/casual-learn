package es.uva.gsic.adolfinstro;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputFilter;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.preference.EditTextPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SeekBarPreference;

import org.json.JSONObject;

import es.uva.gsic.adolfinstro.auxiliar.Auxiliar;
import es.uva.gsic.adolfinstro.persistencia.PersistenciaDatos;

/**
 * Clase para controlar la pantalla de ajustes de la aplicación (fragmento). Ajuste de los máximos del
 * intervalo.
 *
 * @author pablo
 * @version 20201006
 */
public class AjustesFragment extends PreferenceFragmentCompat
        implements SharedPreferences.OnSharedPreferenceChangeListener {

    private PreferenceCategory preferenceCategory;
    private SeekBarPreference seekBarPreference;
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences, rootKey);
        seekBarPreference = findPreference(Ajustes.INTERVALO_pref);
        assert seekBarPreference != null;
        seekBarPreference.setMax(15);

        SharedPreferences sharedPreferences = Ajustes.sharedPreferences;
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);
        preferenceCategory = findPreference("categoriaPreferencias");
        onSharedPreferenceChanged(sharedPreferences, Ajustes.INTERVALO_pref);
        onSharedPreferenceChanged(sharedPreferences, Ajustes.NO_MOLESTAR_pref);
        final EditTextPreference hashtag = findPreference(Ajustes.HASHTAG_pref);
        if(hashtag!=null)
            hashtag.setOnBindEditTextListener(new EditTextPreference.OnBindEditTextListener() {
                @Override
                public void onBindEditText(@NonNull EditText editText) {
                    editText.setFilters(new InputFilter[]{
                            new InputFilter.LengthFilter(50)});
                }
            });

        //Cierre de sesión
        JSONObject idUsuario = PersistenciaDatos.recuperaTarea(
                (Application) requireContext().getApplicationContext(),
                PersistenciaDatos.ficheroUsuario,
                Auxiliar.id);
        Preference preference = findPreference(Ajustes.CIERRE_pref);
        assert preference != null;
        if(idUsuario == null){//Si el usuario no está identificado se oculta
            preference.setVisible(false);
        }else{
            final Context context = getContext();
            assert context != null;
            final Application app = (Application) context.getApplicationContext();
            final Activity obj = this.getActivity();
            preference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    AlertDialog.Builder dialogoCerrarSesion = new AlertDialog.Builder(context);
                    dialogoCerrarSesion.setTitle(getString(R.string.cerrarSesion));
                    dialogoCerrarSesion.setMessage(getString(R.string.cerrarSesionMensaje));
                    dialogoCerrarSesion.setCancelable(true);
                    dialogoCerrarSesion.setPositiveButton(getString(R.string.cerrarSesion), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (Auxiliar.cerrarSesion(context, app, obj)) {
                                Intent intent = new Intent(context, Login.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                                assert obj != null;
                                obj.finishAffinity();
                                obj.finish();
                            }
                        }
                    });
                    dialogoCerrarSesion.setNegativeButton(getString(R.string.unaTareaMas), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    });
                    dialogoCerrarSesion.show();
                    return false;
                }
            });

        }
    }

    /**
     * Método que se ejecuta cuando el valor de una preferencia cambia
     * @param sharedPreferences preferencia
     * @param key Clave
     */
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        switch (key){
            case Ajustes.INTERVALO_pref:
                try {
                    String valorActual = getResources().getString(R.string.valorActual);
                    String valorTexto = Auxiliar.valorTexto(getResources(), sharedPreferences.getInt(key, 5));
                    preferenceCategory.setSummary(
                            String.format("%s %s",
                                    valorActual,
                                    valorTexto));
                }catch (Exception e){
                    e.printStackTrace();
                }
                break;
            case Ajustes.NO_MOLESTAR_pref:
                seekBarPreference.setEnabled(!sharedPreferences.getBoolean(key, false));
                break;
            default:
                break;
        }
    }

}
