package es.uva.gsic.adolfinstro;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RatingBar;

import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import es.uva.gsic.adolfinstro.auxiliar.Auxiliar;
import es.uva.gsic.adolfinstro.persistencia.PersistenciaDatos;

/**
 * Clase para gestionar la puntuación de las tareas
 *
 * @author Pablo
 */
public class Puntuacion extends AppCompatActivity {

    private String idTarea;
    private float puntuacion;
    private Button btEnviarPuntuacion;
    //private String hashtag;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_score);

        btEnviarPuntuacion = findViewById(R.id.btEnviarPuntuacion);

        idTarea = getIntent().getExtras().getString("ID");
        RatingBar barraPuntuacion = findViewById(R.id.rbPuntuacion);
        barraPuntuacion.setOnRatingBarChangeListener( new RatingBar.OnRatingBarChangeListener() {
            /**
             * Método que se mantiene a la espera de que el usuario seleccione una puntuación para
             * la tarea
             *
             * @param ratingBar Objeto que ha llamado al método
             * @param rating    Puntuación seleccionada por el usuario
             * @param fromUser  Verdadero si la acción la realiza el usuario, falso si se autocompleta
             */
            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                if(fromUser){
                    if(rating > 0){
                        btEnviarPuntuacion.setText(getString(R.string.enviarPuntuacion));
                        puntuacion = rating;
                    }else{
                        btEnviarPuntuacion.setText(getString(R.string.cancel));
                        puntuacion = 0;
                    }
                }
            }
        });

        /*SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        onSharedPreferenceChanged(sharedPreferences, Ajustes.HASHTAG_pref);*/
    }

    /**
     * Se guarda el identificiador de la tarea
     * @param b Bundle donde se almacena el identificador
     */
    @Override
    protected void onSaveInstanceState(@NotNull Bundle b) {
        super.onSaveInstanceState(b);
        b.putString("ID", idTarea);
        b.putFloat("PUNTUACION", puntuacion);
    }

    /**
     * Se restaura el identificador de la tarea
     * @param b Bundle donde está almacenado el identificador
     */
    @Override
    protected void onRestoreInstanceState(@NotNull Bundle b){
        super.onRestoreInstanceState(b);
        idTarea = b.getString("ID");
        puntuacion = b.getFloat("PUNTUACION");
    }

    /**
     * Método para que cuando el usuario pulse atrás vuelva a la actividad principal y no a la tarea
     */
    @Override
    public void onBackPressed(){
        super.onBackPressed();
        Auxiliar.guardaRespuesta(getApplication(), getApplicationContext(), idTarea);
        vuelveInicio(getString(R.string.puntuaCompletada));
    }

    /**
     * Método para controlar las pulsaciones sobre los distintos botones de la pantalla.
     * @param view Vista pulsada
     */
    public void boton(View view) {
        switch (view.getId()){
            case R.id.btEnviarPuntuacion:
                if(puntuacion > 0){
                    //SE GUARDA LA PUNTUACIÓN
                    JSONObject json = null;
                    try {
                        json = PersistenciaDatos.obtenTarea(
                                getApplication(),
                                PersistenciaDatos.ficheroCompletadas,
                                idTarea);
                        json.put(Auxiliar.rating, puntuacion);
                        json.put(Auxiliar.fechaUltimaModificacion, Auxiliar.horaFechaActual());
                        PersistenciaDatos.guardaJSON(getApplication(),
                                PersistenciaDatos.ficheroCompletadas,
                                json,
                                Context.MODE_PRIVATE);
                    }catch (Exception e){
                        if(json != null)
                            PersistenciaDatos.guardaJSON(getApplication(),
                                    PersistenciaDatos.ficheroCompletadas,
                                    json,
                                    Context.MODE_PRIVATE);
                        vuelveInicio(null);
                    }
                    //Toast.makeText(this, getString(R.string.gracias), Toast.LENGTH_SHORT).show();
                    //Auxiliar.returnMain(getApplication().getBaseContext());
                    Auxiliar.guardaRespuesta(getApplication(), getApplicationContext(), idTarea);
                    vuelveInicio(getString(R.string.gracias));
                } else{
                    Auxiliar.guardaRespuesta(getApplication(), getApplicationContext(), idTarea);
                    vuelveInicio(getString(R.string.puntuaCompletada));
                    //Auxiliar.returnMain(getApplication().getBaseContext());
                }
                break;
            /*case R.id.btCompartirPuntua:
                if((findViewById(R.id.btCompartirPuntuaTwitter)).getVisibility() == View.VISIBLE){
                    muestraOculta(false);
                }else{
                    muestraOculta(true);
                }
                break;
            case R.id.btCompartirPuntuaTwitter:
                Auxiliar.mandaTweet(
                        this,
                        PersistenciaDatos.recuperaTarea(getApplication(),
                                PersistenciaDatos.ficheroCompletadas,
                                idTarea),
                        hashtag);
                muestraOculta(false);
                break;
            case R.id.btCompartirPuntuaYammer:
                Auxiliar.mandaYammer(this,
                        PersistenciaDatos.recuperaTarea(getApplication(),
                                PersistenciaDatos.ficheroCompletadas,
                                idTarea),
                        hashtag);
                muestraOculta(false);
                break;
            case R.id.btCompartirPuntuaInstagram:
                Auxiliar.mandaInsta(this,
                        PersistenciaDatos.recuperaTarea(getApplication(),
                                PersistenciaDatos.ficheroCompletadas,
                                idTarea),
                        hashtag);
                muestraOculta(false);
                break;*/
            default:
                break;
        }
    }

    /*private void muestraOculta(boolean mostrar){
        Integer[] lista = {
                R.id.btCompartirPuntuaTwitter,
                R.id.btCompartirPuntuaYammer,
                R.id.btCompartirPuntuaInstagram
        };
        for(int i : lista) {
            if (mostrar)
                ((FloatingActionButton) findViewById(i)).show();
            else
                ((FloatingActionButton) findViewById(i)).hide();
        }
        if (mostrar)
            ((FloatingActionButton) findViewById(R.id.btCompartirPuntua))
                    .setImageDrawable(getResources().getDrawable(R.drawable.ic_baseline_close_secondary));
        else
            ((FloatingActionButton) findViewById(R.id.btCompartirPuntua))
                    .setImageDrawable(getResources().getDrawable(R.drawable.ic_share_secondary));
    }*/

    private void vuelveInicio(String string) {
        Intent intent = new Intent(this, CompartirRespuesta.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        //if(string != null)
        intent.putExtra(Auxiliar.textoParaElMapa, string);
        intent.putExtra("ID", idTarea);
        startActivity(intent);
        finishAffinity();
    }

    /*@Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        switch (key){
            case Ajustes.HASHTAG_pref:
                hashtag = sharedPreferences.getString(key, getString(R.string.hashtag));
                break;
            default:
                break;
        }
    }*/
}
