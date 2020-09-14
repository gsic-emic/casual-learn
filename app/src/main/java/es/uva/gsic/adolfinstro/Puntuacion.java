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
 * @version 20200914
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
                        btEnviarPuntuacion.setText(getString(R.string.enviarPuntuacion));
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
        Auxiliar.guardaRespuesta(getApplication(), getApplicationContext(), idTarea);
        pantallaCompartir(getString(R.string.puntuaCompletada));
    }

    /**
     * Método para controlar las pulsaciones sobre los distintos botones de la pantalla.
     * @param view Vista pulsada
     */
    public void boton(View view) {
        if (view.getId() == R.id.btEnviarPuntuacion) {
            if (puntuacion > 0) {
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
                } catch (Exception e) {
                    if (json != null)
                        PersistenciaDatos.guardaJSON(getApplication(),
                                PersistenciaDatos.ficheroCompletadas,
                                json,
                                Context.MODE_PRIVATE);
                    pantallaCompartir(null);
                }
                Auxiliar.guardaRespuesta(getApplication(), getApplicationContext(), idTarea);
                pantallaCompartir(getString(R.string.gracias));
            } else {
                Auxiliar.guardaRespuesta(getApplication(), getApplicationContext(), idTarea);
                pantallaCompartir(getString(R.string.puntuaCompletada));
            }
        }
    }

    /**
     * Método para llevar al usuario a la pantalla de compartir
     * @param string Texto que se mostrará en la actividad de mapas
     */
    private void pantallaCompartir(String string) {
        Intent intent = new Intent(this, CompartirRespuesta.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(Auxiliar.textoParaElMapa, string);
        intent.putExtra("ID", idTarea);
        startActivity(intent);
        finishAffinity();
    }
}
