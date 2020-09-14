package es.uva.gsic.adolfinstro;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import org.jetbrains.annotations.NotNull;

/**
 * Clase para controlar la pantalla de ajustes de la aplicación
 *
 * @author pablo
 * @version 20200727
 */
public class Ajustes extends AppCompatActivity
        implements SharedPreferences.OnSharedPreferenceChangeListener {

    /** Clave para controlar la preferencia de no molestar */
    public static final String NO_MOLESTAR_pref = "noMolestar";
    /** Clave para controlar el tiempo mínimo entre notificaciones */
    public static final String INTERVALO_pref = "intervalo";
    /** Clave para controlar el volver a mostrar el mensaje de problemas por la marca*/
    public static final String LISTABLANCA_pref = "listaBlanca";
    /** Clave para identificar la lista de etiquetas que el usuario quiere que se publique junto a la
     * respuesta */
    public static final String HASHTAG_pref = "hashtag";

    public static SharedPreferences sharedPreferences;
    private boolean reiniciaMapa = false;
    private boolean estadoAnterior;

    @Override
    public void onCreate(Bundle sIS){
        super.onCreate(sIS);
        setContentView(R.layout.activity_ajustes);
        getSupportFragmentManager().beginTransaction().replace(R.id.container_ajustes, new AjustesFragment()).commit();
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        estadoAnterior = sharedPreferences.getBoolean(Ajustes.NO_MOLESTAR_pref, false);
        SharedPreferences sharedPreferences = Ajustes.sharedPreferences;
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (Ajustes.NO_MOLESTAR_pref.equals(key)) {
            reiniciaMapa = estadoAnterior != sharedPreferences.getBoolean(key, false);
        }
    }

    @Override
    protected void onSaveInstanceState(@NotNull Bundle bundle) {
        super.onSaveInstanceState(bundle);
        bundle.putBoolean("REINICIAMAPA", reiniciaMapa);
    }

    @Override
    protected  void onRestoreInstanceState(Bundle bundle){
        reiniciaMapa = bundle.getBoolean("REINICIAMAPA");
    }

    @Override
    public boolean onSupportNavigateUp(){
        onBackPressed();
        return false;
    }

    @Override
    public void onBackPressed(){
        if(reiniciaMapa) {
            Intent intent = new Intent(this, Maps.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP);
            finishAffinity();
            startActivity(intent);
        }else {
            finish();
        }
    }
}

