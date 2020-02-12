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


    private int tipo;
    private Uri photoURI;
    private String currentPhotoPath;
    private FloatingActionButton fab;
    private Button bt;
    private int restantes = 3;

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
                accion();
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        bt = findViewById(R.id.btCancelar);

        tipo = getIntent().getExtras().getInt("TIPO");
    }

    /**
     * Método para agrupara todas las acciones que se puede realizar en esta actividad
     */
    private void accion() {
        switch (tipo){
            case R.id.btUnaFoto:
                realizaCaptura(0);
                break;
            case R.id.btVariasFotos:
                fab.setClickable(false);
                bt.setClickable(false);
                realizaCaptura(1);
                break;
            case R.id.btVideo:
                Intent takeVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
                if(takeVideoIntent.resolveActivity(getPackageManager()) != null){
                    startActivityForResult(takeVideoIntent, 2);
                }
                break;
        }
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
                photoFile = createImageFile();
            }catch (IOException e){

            }
            if(photoFile != null){
                photoURI = FileProvider.getUriForFile(context, "es.uva.gsic.adolfinstro.fileprovider", photoFile);

                takePicture.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePicture, requestCode);//Los requestCode solo pueden ser de 16 bits
            }
        }
    }

    /**
     * Creación del fichero donde se almacenará la foto
     * @return Fichero donde almacenar la foto
     * @throws IOException Se lanzará una excepción cuando se produzca un error al crear el fichero vacío
     */
    private File createImageFile() throws IOException{
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmssSSS").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDire = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(imageFileName, ".jpg", storageDire);
        currentPhotoPath = image.getAbsolutePath();
        return image;
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
        boolean salir = false;
        switch (requestCode){
            case 0:
                switch (resultCode){
                    case RESULT_OK:
                        Toast.makeText(this, "Imagen almacenada", Toast.LENGTH_SHORT).show();
                        volverMain();
                        break;
                    case RESULT_CANCELED:
                        break;
                    default:
                        errorToast();
                }
                break;
            case 1:
                switch (resultCode){
                    case RESULT_OK:
                        --restantes;
                        if(restantes==0){
                            Toast.makeText(this, "Imágenes almacenadas", Toast.LENGTH_SHORT).show();
                            volverMain();
                        }
                        else {
                            Toast.makeText(this, "Imagen almacenada. Haga la siguiente foto", Toast.LENGTH_SHORT).show();
                        }
                        break;
                    case RESULT_CANCELED:
                        bt.setClickable(true);
                        break;
                    default:
                        errorToast();
                }
                break;
            case 2:
                switch (resultCode){
                    case RESULT_OK:
                        Toast.makeText(this, "Vídeo almacenada", Toast.LENGTH_SHORT).show();
                        volverMain();
                        break;
                    case RESULT_CANCELED:
                        break;
                    default:
                        errorToast();
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * Impreme un toast para indicar al usuario que ha sucedido algún problema
     */
    private void errorToast() {
        Toast.makeText(this, "Error en la operación", Toast.LENGTH_SHORT).show();
    }

    /**
     * Método para volver a la actividad principal
     */
    private void volverMain(){
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    /**
     * Método para agrupar las acciones de los diferentes botones. El botón flotante de la cámara se
     * gestiona cuando se crea la actividad
     * @param view
     */
    public void boton(View view) {
        switch (view.getId()){
            case R.id.btCancelar:
                volverMain();
        }
    }
}
