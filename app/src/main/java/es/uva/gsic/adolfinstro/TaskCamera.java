package es.uva.gsic.adolfinstro;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.FileProvider;

import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class TaskCamera extends AppCompatActivity {

    /** Identificador de la tarea a realizar*/
    private int tipo;
    /** URI de la última foto/vídeo que se ha tomado*/
    private Uri photoURI, videoURI;
    /** Localización de la última foto/vídeo tomada*/
    private String currentPath;
    /** Botón flotante para realizar las fotos y vídeos */
    private FloatingActionButton fab;
    /** Instancia del botón de cancelación de la tarea */
    private Button bt;
    /** Número de fotos a realizar*/
    private int restantes = 3; //Este valor será dinámico y dependerá de la tarea solicitada. Solo se utiliza en múltiples fotos
    /** Objetos que establecen si un botón es clicable o no*/
    private boolean estadoFav, estadoBt;

    /**
     * Método de creación del layout. Se crea desde java el botón flotante para realizar las
     * capturas o los vídeos.
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_camera);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boton(view);
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        bt = findViewById(R.id.btCancelar);
        estadoFav = true;
        estadoBt = true;

        tipo = getIntent().getExtras().getInt("TIPO");
    }

    /**
     * Método para agrupara todas las acciones que se puede realizar en esta actividad
     */
    private void accion() {
        switch (tipo){
            case R.id.btUnaFoto:
                bloqueaBotones();
                realizaCaptura(0);
                break;
            case R.id.btVariasFotos:
                bloqueaBotones();
                realizaCaptura(1);
                break;
            case R.id.btVideo:
                bloqueaBotones();
                //Se graba el vídeo
                Intent takeVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
                File videoFile = null;
                try {
                    videoFile = Auxiliar.createFile(2, this);
                }catch (IOException e){

                }
                if(videoFile != null){
                    videoURI = FileProvider.getUriForFile(getBaseContext(), "es.uva.gsic.adolfinstro.fileprovider", videoFile);
                    takeVideoIntent.putExtra(MediaStore.EXTRA_OUTPUT, videoURI);
                    if(takeVideoIntent.resolveActivity(getPackageManager()) != null){
                        //CALIDAD MMS
                        //https://developer.android.com/reference/android/provider/MediaStore#EXTRA_VIDEO_QUALITY
                        takeVideoIntent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 0);
                        startActivityForResult(takeVideoIntent, 2);
                    }else{
                        Toast.makeText(this, getString(R.string.errorOpera), Toast.LENGTH_SHORT).show();
                        bt.setClickable(true);
                    }
                }
                break;
        }
    }

    /**
     * Método que bloquea el botón de la cámara y el de cancelar
     */
    private void bloqueaBotones(){
        estadoFav = false;
        estadoBt = false;
        setBotones();
    }

    /**
     * Agrupa las sentencias necesarias para la realización de una foto
     * @param requestCode Código que se utiliza para cuando finalice la actividad. 0 para foto individual,
     *                    1 para múltiples fotos
     */
    private void realizaCaptura(int requestCode){
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
                startActivityForResult(takePicture, requestCode);//Los requestCode solo pueden ser de 16 bits
            }
        } else{
            Toast.makeText(this, getString(R.string.errorOpera), Toast.LENGTH_SHORT).show();
            desbloqueaBt();
        }
    }

    /**
     * Método que desbloqeua el botón de cancelar
     */
    private void desbloqueaBt(){
        estadoBt = true;
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        switch (requestCode){
            case 0:
                switch (resultCode){
                    case RESULT_OK:
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
            case 1:
                switch (resultCode){
                    case RESULT_OK:
                        --restantes;
                        if(restantes==0){
                            Toast.makeText(this, getString(R.string.imagenesG), Toast.LENGTH_SHORT).show();
                            Auxiliar.returnMain(this);
                        }
                        else {
                            Toast.makeText(this, getString(R.string.imagenGN), Toast.LENGTH_SHORT).show();
                            realizaCaptura(1);
                        }
                        break;
                    case RESULT_CANCELED:
                        desbloqueaBt();
                        break;
                    default:
                        Auxiliar.errorToast(this);
                }
                break;
            case 2:
                switch (resultCode){
                    case RESULT_OK:
                        Toast.makeText(this, getString(R.string.videoG), Toast.LENGTH_SHORT).show();
                        Auxiliar.returnMain(this);
                        break;
                    case RESULT_CANCELED:
                        desbloqueaBt();
                        break;
                    default:
                        Auxiliar.errorToast(this);
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }



    /**
     * Método para agrupar las acciones de los diferentes botones. El botón flotante de la cámara se
     * gestiona cuando se crea la actividad
     * @param view
     */
    public void boton(View view) {
        switch (view.getId()){
            case R.id.btCancelar:
                Auxiliar.returnMain(this);
                break;
            case R.id.fab:
                accion();
                break;
        }
    }

    /**
     * Método que se utiliza para almacenar el valor de algunas variables de la clase
     * @param bundle Objeto donde se almacenan las variables de la clase
     */
    @Override
    protected void onSaveInstanceState(Bundle bundle){
        super.onSaveInstanceState(bundle);
        bundle.putInt("RESTANTES", restantes);
        bundle.putBoolean("ESTADOFAV", estadoFav);
        bundle.putBoolean("ESTADOBT", estadoBt);
    }

    /**
     * Método utilizado para restablecer el valor de algunas variables de la clase
     * @param bundle Objeto donde están almacenadas las variables de la clase entre otras cosas
     */
    @Override
    protected void onRestoreInstanceState(Bundle bundle){
        super.onRestoreInstanceState(bundle);
        restantes = bundle.getInt("RESTANTES");
        estadoFav = bundle.getBoolean("ESTADOFAV");
        estadoBt = bundle.getBoolean("ESTADOBT");
        setBotones();
    }

    /**
     * Método que establece si los botones son clicables o no dependiendo del estado de las variables
     * estadoFav y estadoBt
     */
    private void setBotones(){
        fab.setClickable(estadoFav);
        bt.setClickable(estadoBt);
    }


}
