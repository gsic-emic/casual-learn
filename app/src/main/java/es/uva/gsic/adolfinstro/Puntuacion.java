package es.uva.gsic.adolfinstro;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.Toast;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import es.uva.gsic.adolfinstro.auxiliar.Auxiliar;
import es.uva.gsic.adolfinstro.persistencia.PersistenciaDatos;

/**
 * Clase para gestionar la puntuación de las tareas
 *
 * @author GSIC
 */
public class Puntuacion extends AppCompatActivity {

    private String idTarea;
    private float puntuacion;
    private Button btEnviarPuntuacion;

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
                        btEnviarPuntuacion.setText(getString(R.string.enviar));
                        puntuacion = rating;
                    }else{
                        btEnviarPuntuacion.setText(getString(R.string.cancel));
                        puntuacion = 0;
                    }
                }
            }
        });
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
        Auxiliar.returnMain(this);
    }

    public void boton(View view) {
        switch (view.getId()){
            case R.id.btEnviarPuntuacion:
                if(puntuacion > 0){
                    //SE GUARDA LA PUNTUACIÓN
                    JSONObject json = null;
                    try {
                        json = PersistenciaDatos.obtenTarea(getApplication(), PersistenciaDatos.ficheroCompletadas, idTarea);
                        json.put(Auxiliar.rating, puntuacion);
                        json.put(Auxiliar.fechaUltimaModificacion, Auxiliar.horaFechaActual());
                        PersistenciaDatos.guardaJSON(getApplication(), PersistenciaDatos.ficheroCompletadas, json, Context.MODE_PRIVATE);
                    }catch (Exception e){
                        if(json != null)
                            PersistenciaDatos.guardaJSON(getApplication(), PersistenciaDatos.ficheroCompletadas, json, Context.MODE_PRIVATE);
                        Auxiliar.returnMain(getApplication().getApplicationContext());
                    }
                    Toast.makeText(this, getString(R.string.gracias), Toast.LENGTH_SHORT).show();
                    Auxiliar.returnMain(getApplication().getBaseContext());
                }
                else{
                    Auxiliar.returnMain(getApplication().getBaseContext());
                }
        }
    }
}
