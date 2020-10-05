package es.uva.gsic.adolfinstro;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import es.uva.gsic.adolfinstro.auxiliar.Auxiliar;
import es.uva.gsic.adolfinstro.persistencia.PersistenciaDatos;

/**
 * Clase para que el usuario pueda compartir la respuesta dada en las distintas redes sociales que
 * soporta la aplicación.
 *
 * @author Pablo
 * @version 20201005
 */
public class CompartirRespuesta extends AppCompatActivity
        implements SharedPreferences.OnSharedPreferenceChangeListener{

    /** Etiqueta que el usuario ha definido */
    private String hashtag;
    /** Identificador de la tarea en el fichero de tareas completadas */
    private String idTarea;
    /** Texto que se le va a pasar a la pantalla de mapas */
    private String textoPuntua;

    private JSONObject tarea;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compartir_respuesta);

        idTarea = getIntent().getExtras().getString("ID");
        textoPuntua = getIntent().getExtras().getString(Auxiliar.textoParaElMapa);

        try {
            tarea = PersistenciaDatos.recuperaTarea(
                    getApplication(),
                    PersistenciaDatos.ficheroCompletadas,
                    idTarea,
                    PersistenciaDatos.recuperaTarea(
                            getApplication(), PersistenciaDatos.ficheroUsuario, Auxiliar.id)
                            .getString(Auxiliar.uid));
            String tipo = tarea.getString(Auxiliar.tipoRespuesta);
            if(tipo.equals(Auxiliar.tipoSinRespuesta) ||
                    tipo.equals(Auxiliar.tipoPreguntaCorta) ||
                    tipo.equals(Auxiliar.tipoPreguntaLarga)){
                Button insta = findViewById(R.id.btCompartirInsta);
                insta.setVisibility(View.GONE);
            }
        } catch (JSONException e) {
            Button insta = findViewById(R.id.btCompartirInsta);
            insta.setVisibility(View.GONE);
            insta = findViewById(R.id.btCompartirTwitter);
            insta.setVisibility(View.GONE);
            insta = findViewById(R.id.btCompartirYammer);
            insta.setVisibility(View.GONE);
            Toast.makeText(this, getString(R.string.tituErrorBBDD), Toast.LENGTH_LONG).show();
        }

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        onSharedPreferenceChanged(sharedPreferences, Ajustes.HASHTAG_pref);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (Ajustes.HASHTAG_pref.equals(key)) {
            hashtag = sharedPreferences.getString(key, getString(R.string.hashtag));
        }
    }

    public void boton(View view) {
        switch (view.getId()){
            case R.id.btCompartirVolver:
                muestraMapa(textoPuntua);
                break;
            case R.id.btCompartirTwitter:
                Auxiliar.mandaTweet(
                        this,
                        tarea,
                        hashtag);
                break;
            case R.id.btCompartirYammer:
                Auxiliar.mandaYammer(this,
                        tarea,
                        hashtag);
                break;
            case R.id.btCompartirInsta:
                Auxiliar.mandaInsta(this,
                        tarea,
                        hashtag);
                break;
            default:
                break;
        }
    }

    /**
     * Método para llevar al usuario a la pantalla de mapas
     * @param string Texto que se le va a mostrar en el mapa
     */
    private void muestraMapa(String string) {
        Intent intent = new Intent(this, Maps.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if(string != null)
            intent.putExtra(Auxiliar.textoParaElMapa, string);
        startActivity(intent);
        finishAffinity();
    }

    @Override
    public void onBackPressed(){
        super.onBackPressed();
        muestraMapa(textoPuntua);
    }

    /**
     * Se guarda el identificiador de la tarea y el texto para el mapa
     * @param b Bundle donde se almacena el identificador
     */
    @Override
    protected void onSaveInstanceState(@NotNull Bundle b) {
        super.onSaveInstanceState(b);
        b.putString("ID", idTarea);
        b.putString(Auxiliar.textoParaElMapa, textoPuntua);
    }

    /**
     * Se restaura el identificador de la tarea y el texto para el mapa
     * @param b Bundle donde está almacenado el identificador
     */
    @Override
    protected void onRestoreInstanceState(@NotNull Bundle b){
        super.onRestoreInstanceState(b);
        idTarea = b.getString("ID");
        textoPuntua = b.getString(Auxiliar.textoParaElMapa);
    }
}