package es.uva.gsic.adolfinstro;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.FileProvider;

import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;

public class Ask_camera extends AppCompatActivity {
    EditText et;
    String respuesta;
    Uri photoURI;
    FloatingActionButton btCamara;
    Button btCancelar;
    Boolean estadoBtCamara, estadoBtCancelar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ask_camera);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        et = findViewById(R.id.etRespuestaPreguntaFoto);
        btCancelar = findViewById(R.id.btCancelarPreguntaFoto);

        btCamara = findViewById(R.id.btfotoPreguntaFoto);
        btCamara.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boton(view);
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    public void boton(View view) {
        switch (view.getId()){
            case R.id.btCancelarPreguntaFoto:
                Auxiliar.returnMain(this);
                break;
            case R.id.btfotoPreguntaFoto:
                String respuesta = et.getText().toString();
                respuesta = respuesta.trim();
                if(respuesta.isEmpty()){
                    Toast.makeText(this, getString(R.string.respuestaVacia), Toast.LENGTH_SHORT).show();
                }
                else{
                    this.respuesta = respuesta;
                    //Una vez que la respuesta no está vacía se realiza la foto
                    bloqueaBotones();
                    realizaCaptura();
                }
                break;
        }
    }

    /**
     * Agrupa las sentencias necesarias para la realización de una foto
     */
    private void realizaCaptura(){
        final Context context = getBaseContext();
        Intent takePicture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if(takePicture.resolveActivity(getPackageManager())!=null){
            File photoFile = null;
            try{
                photoFile = Auxiliar.createFile(1,this);
            }catch (IOException e){

            }
            if(photoFile != null){
                photoURI = FileProvider.getUriForFile(context, "es.uva.gsic.adolfinstro.fileprovider", photoFile);

                takePicture.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePicture, 0);//Los requestCode solo pueden ser de 16 bits
            }
        } else{
            Toast.makeText(this, getString(R.string.errorOpera), Toast.LENGTH_SHORT).show();
            desbloqueaBt();
        }
    }

    /**
     * Método que bloquea el botón de la cámara y el de cancelar
     */
    private void bloqueaBotones(){
        estadoBtCamara = false;
        estadoBtCancelar = false;
        setBotones();
    }

    /**
     * Método que establece si los botones son clicables o no dependiendo del estado de las variables
     * estadoBtCancelar y estadoBtCamara
     */
    private void setBotones(){
        btCancelar.setClickable(estadoBtCancelar);
        btCamara.setClickable(estadoBtCamara);
    }

    /**
     * Método que desbloqeua el botón de cancelar
     */
    private void desbloqueaBt(){
        estadoBtCancelar = true;
        setBotones();
    }

    /**
     * Método que se lanza para resolver el resultado de otra actividad. En nuestro caso se activa
     * cuando el usuario realice la captura o el vídeo
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case 0:
                switch (resultCode){
                    case RESULT_OK:
                        //AQUI TENDREMOS QUE GUARDAR LOS RESULTADOS EN LA BBDD
                        Toast.makeText(this, getString(R.string.imagenG), Toast.LENGTH_SHORT).show();
                        Auxiliar.returnMain(this);
                        break;
                    case RESULT_CANCELED:
                        desbloqueaBt();
                        break;
                    default:
                        Auxiliar.errorToast(this);
                }
                break;
            default:
                System.exit(-3);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
