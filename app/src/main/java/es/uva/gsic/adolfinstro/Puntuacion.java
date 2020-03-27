package es.uva.gsic.adolfinstro;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.Bundle;
import android.widget.RatingBar;
import android.widget.Toast;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import es.uva.gsic.adolfinstro.auxiliar.Auxiliar;
import es.uva.gsic.adolfinstro.persistencia.PersistenciaDatos;

public class Puntuacion extends AppCompatActivity {

    private String idTarea;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_score);
        try {
            idTarea = getIntent().getExtras().getString("ID");
        }catch (NullPointerException e){
            //TODO SOLO DEBERÍA SALTAR EN DESARROLLO ya que SIEMPRE se va a
            System.err.println("No se va a puntuar una tarea que exista.");
            idTarea = "AAA_123_IdentificadorTareaFalsoDesarrollo";
        }
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
                //SE GUARDA LA PUNTUACIÓN
                JSONObject json = new JSONObject();
                JSONArray array;
                try {
                    json.put("id", idTarea);
                    json.put("rating", rating);
                }catch (JSONException e){
                    Toast.makeText(getApplication().getApplicationContext(), "Error JSON", Toast.LENGTH_SHORT).show();
                    Auxiliar.returnMain(getApplication().getApplicationContext());
                }

                PersistenciaDatos.guardaJSON(getApplication(), PersistenciaDatos.ficheroPuntuaciones, json, Context.MODE_PRIVATE);
                Auxiliar.returnMain(getApplication().getBaseContext());
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
    }

    /**
     * Se restaura el identificador de la tarea
     * @param b Bundle donde está almacenado el identificador
     */
    @Override
    protected void onRestoreInstanceState(@NotNull Bundle b){
        super.onRestoreInstanceState(b);
        idTarea = b.getString("ID");
    }

    /**
     * Método para que cuando el usuario pulse atrás vuelva a la actividad principal y no a la tarea
     */
    @Override
    public void onBackPressed(){
        super.onBackPressed();
        Auxiliar.returnMain(this);
    }
}
