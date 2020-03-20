package es.uva.gsic.adolfinstro;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.RatingBar;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONStringer;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Objects;

import static es.uva.gsic.adolfinstro.Auxiliar.returnMain;

public class Score extends AppCompatActivity {

    private RatingBar barraPuntuacion;
    private String idTarea;
    public final String ficheroPuntuacion = "puntuaciones.txt";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_score);
        try {
            idTarea = getIntent().getExtras().getString("ID");
        }catch (NullPointerException e){//SOLO DEBERÍA SALTAR EN DESARROLLO
        }
        barraPuntuacion = findViewById(R.id.rbPuntuacion);

        barraPuntuacion.setOnRatingBarChangeListener( new RatingBar.OnRatingBarChangeListener() {

            /**
             * Notification that the rating has changed. Clients can use the
             * fromUser parameter to distinguish user-initiated changes from those
             * that occurred programmatically. This will not be called continuously
             * while the user is dragging, only when the user finalizes a rating by
             * lifting the touch.
             *
             * @param ratingBar The RatingBar whose rating has changed.
             * @param rating    The current rating. This will be in the range
             *                  0..numStars.
             * @param fromUser  True if the rating change was initiated by a user's
             */
            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                //SE GUARDA LA PUNTUACIÓN
                JSONObject json = new JSONObject();
                try {
                    json.put("idTarea", idTarea);
                    json.put("rating", rating);
                }catch (JSONException e){
                    Toast.makeText(getApplication().getApplicationContext(), "Error JSON", Toast.LENGTH_SHORT).show();
                    returnMain(getApplication().getApplicationContext());
                }
                OutputStreamWriter outputStreamWriter;
                File directorioPrueba = new File(getApplication().getFilesDir(), "directorioPrueba");
                if(!directorioPrueba.exists()){
                    directorioPrueba.mkdirs();
                }
                directorioPrueba = new File(getApplication().getFilesDir(), ficheroPuntuacion);

                try {
                    boolean creado;
                    if(!directorioPrueba.exists()){
                        creado = directorioPrueba.createNewFile();
                    }
                    outputStreamWriter = new OutputStreamWriter(getApplication().openFileOutput(ficheroPuntuacion, Context.MODE_APPEND));
                    //todo
                    //todo
                    //todo
                    //todo
                    //todo
                    //todo
                    //todo
                    //todo
                    //todo
                    //todo
                    //todo
                    //todo
                    //todo
                    //todo
                    //todo
                    outputStreamWriter.write(json.toString()+" ");//ESTOY HAY QUE MEJORARLO..
                    outputStreamWriter.close();
                }catch (IOException e){
                    Toast.makeText(getApplication().getApplicationContext(), "Error Writer", Toast.LENGTH_SHORT).show();
                    returnMain(getApplication().getApplicationContext());
                }finally {
                    outputStreamWriter = null;
                    json = null;
                }
                try{
                    InputStreamReader inputStreamReader = new InputStreamReader(getApplication().openFileInput(ficheroPuntuacion));
                    BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                    StringBuffer stringBuffer = new StringBuffer();
                    String prueba;
                    while((prueba = bufferedReader.readLine())!= null){
                        stringBuffer.append(prueba);
                    }
                    prueba = stringBuffer.toString();
                    String[] jsons = prueba.split(" ");//NECESITA UPDATE!!!!!!!!!!!
                    for(String j : jsons){
                        JSONObject t = new JSONObject(j);
                        t.get("idTarea");
                    }
                }
                catch (Exception e){

                }

                returnMain(getApplication().getBaseContext());
            }
        });
    }

    @Override
    protected void onSaveInstanceState(Bundle b) {
        super.onSaveInstanceState(b);
        b.putString("ID", idTarea);
    }

    @Override
    protected void onRestoreInstanceState(Bundle b){
        super.onRestoreInstanceState(b);
        idTarea = b.getString("ID");
    }

    /**
     * Método para que cuando el usuario pulse atrás vuelva a la actividad principal y no a la tarea
     */
    @Override
    public void onBackPressed(){
        super.onBackPressed();
        returnMain(this);
    }
}
