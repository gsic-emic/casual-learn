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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.preference.EditTextPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SeekBarPreference;
import androidx.preference.SwitchPreference;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

import es.uva.gsic.adolfinstro.auxiliar.Auxiliar;
import es.uva.gsic.adolfinstro.auxiliar.ColaConexiones;
import es.uva.gsic.adolfinstro.persistencia.PersistenciaDatos;

/**
 * Clase para controlar la pantalla de ajustes de la aplicación (fragmento). Ajuste de los máximos del
 * intervalo.
 *
 * @author pablo
 * @version 20201203
 */
public class AjustesFragment extends PreferenceFragmentCompat
        implements SharedPreferences.OnSharedPreferenceChangeListener {

    private PreferenceCategory preferenceCategory;
    private SeekBarPreference seekBarPreference;
    private Preference compartirPorta;
    private SwitchPreference preferenciaRetardo;
    private JSONObject idUsuario;
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences, rootKey);
        idUsuario = PersistenciaDatos.recuperaTarea(
                (Application) requireContext().getApplicationContext(),
                PersistenciaDatos.ficheroUsuario,
                Auxiliar.id);
        final Context context = getContext();
        seekBarPreference = findPreference(Ajustes.INTERVALO_pref);
        assert seekBarPreference != null;
        seekBarPreference.setMax(15);

        SharedPreferences sharedPreferences = Ajustes.sharedPreferences;
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);
        preferenceCategory = findPreference("categoriaPreferencias");
        compartirPorta = findPreference(Ajustes.IDPORTAFOLIO_pref);
        SwitchPreference compartirProtafolio = findPreference(Ajustes.PORTAFOLIO_pref);
        SwitchPreference noMolestar = findPreference(Ajustes.NO_MOLESTAR_pref);
        final EditTextPreference hashtag = findPreference(Ajustes.HASHTAG_pref);
        SwitchPreference datos = findPreference(Ajustes.WIFI_pref);
        preferenciaRetardo = findPreference(Ajustes.RETARDOPORTA_pref);
        if(idUsuario != null) {
            compartirProtafolio.setEnabled(true);
            noMolestar.setEnabled(true);
            hashtag.setEnabled(true);
            datos.setEnabled(true);
            seekBarPreference.setEnabled(true);
            compartirPorta.setEnabled(sharedPreferences.getBoolean(Ajustes.PORTAFOLIO_pref, false));
            preferenciaRetardo.setEnabled(compartirPorta.isEnabled());
            compartirPorta.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    try {
                        String urlPortafolio = Auxiliar.rutaPortafolio + idUsuario.getString(Auxiliar.idPortafolio);
                        //String urlPortafolio = Auxiliar.direccionIP;
                        Intent intent = new Intent(Intent.ACTION_SEND);
                        intent.putExtra(Intent.EXTRA_TEXT, urlPortafolio);
                        intent.setType("text/plain");
                        startActivity(Intent.createChooser(intent, context.getResources().getString(R.string.app_name)));
                    } catch (Exception e) {
                        Toast.makeText(context, R.string.idporta_no_disponible, Toast.LENGTH_LONG).show();
                    }
                    return false;
                }
            });
        } else {
            compartirProtafolio.setEnabled(false);
            compartirPorta.setEnabled(false);
            noMolestar.setEnabled(false);
            hashtag.setEnabled(false);
            datos.setEnabled(false);
            preferenciaRetardo.setEnabled(false);
            seekBarPreference.setEnabled(false);
        }
        onSharedPreferenceChanged(sharedPreferences, Ajustes.INTERVALO_pref);
        onSharedPreferenceChanged(sharedPreferences, Ajustes.NO_MOLESTAR_pref);
        if(hashtag!=null)
            hashtag.setOnBindEditTextListener(new EditTextPreference.OnBindEditTextListener() {
                @Override
                public void onBindEditText(@NonNull EditText editText) {
                    editText.setFilters(new InputFilter[]{
                            new InputFilter.LengthFilter(50)});
                }
            });

        //Cierre de sesión
        Preference preference = findPreference(Ajustes.CIERRE_pref);
        assert preference != null;
        if(idUsuario == null){//Si el usuario no está identificado se oculta
            preference.setVisible(false);
        }else{
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
                if(idUsuario != null)
                    seekBarPreference.setEnabled(!sharedPreferences.getBoolean(key, false));
                break;
            case Ajustes.PORTAFOLIO_pref:
            case Ajustes.RETARDOPORTA_pref:
                final Context context = getContext();
                if(idUsuario == null || idUsuario.has(Auxiliar.uid)) {
                    final boolean publico = sharedPreferences.getBoolean(Ajustes.PORTAFOLIO_pref, false);
                    final boolean retardado = sharedPreferences.getBoolean(Ajustes.RETARDOPORTA_pref, true);
                    try {
                        JSONObject infoUsuario = new JSONObject();
                        JsonObjectRequest jsonObjectRequest;
                        infoUsuario.put(Auxiliar.publico, publico);
                        infoUsuario.put(Auxiliar.retardado, retardado);
                        if (idUsuario != null && idUsuario.has(Auxiliar.idPortafolio)) {//Es una actualización
                            jsonObjectRequest = new JsonObjectRequest(
                                    Request.Method.PUT,
                                    Auxiliar.rutaPortafolio + idUsuario.getString(Auxiliar.idPortafolio),
                                    infoUsuario,
                                    new Response.Listener<JSONObject>() {
                                        @Override
                                        public void onResponse(JSONObject response) {
                                            compartirPorta.setEnabled(publico);
                                            preferenciaRetardo.setEnabled(publico);
                                        }
                                    },
                                    new Response.ErrorListener() {
                                        @Override
                                        public void onErrorResponse(VolleyError error) {
                                            idUsuario.remove(Auxiliar.idPortafolio);
                                            PersistenciaDatos.reemplazaJSON(
                                                    (Application) context.getApplicationContext(),
                                                    PersistenciaDatos.ficheroUsuario,
                                                    idUsuario);
                                            Toast.makeText(context, context.getString(R.string.errorCambioEstado), Toast.LENGTH_SHORT).show();
                                        }
                                    }
                            );
                        } else {//Es una creación
                            infoUsuario.put(Auxiliar.idUsuario, idUsuario.getString(Auxiliar.uid));
                            jsonObjectRequest = new JsonObjectRequest(
                                    Request.Method.POST,
                                    Auxiliar.direccionIP + "portafolio",
                                    infoUsuario,
                                    new Response.Listener<JSONObject>() {
                                        @Override
                                        public void onResponse(JSONObject docUsuario) {
                                            if (docUsuario != null) {
                                                try {
                                                    idUsuario.put(Auxiliar.idPortafolio, docUsuario.getString(Auxiliar.idPortafolio));
                                                    PersistenciaDatos.reemplazaJSON(
                                                            (Application) context.getApplicationContext(),
                                                            PersistenciaDatos.ficheroUsuario,
                                                            idUsuario);
                                                    compartirPorta.setEnabled(publico);
                                                    preferenciaRetardo.setEnabled(publico);
                                                } catch (JSONException e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                        }
                                    },
                                    null
                            );
                        }

                        ColaConexiones.getInstance(context).getRequestQueue().add(jsonObjectRequest);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    assert context != null;
                    Toast.makeText(context, context.getResources().getString(R.string.aun_no_disponible), Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                break;
        }
    }

}
