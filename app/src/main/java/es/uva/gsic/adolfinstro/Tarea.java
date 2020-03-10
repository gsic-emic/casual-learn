package es.uva.gsic.adolfinstro;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.InputFilter;
import android.text.InputType;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import static es.uva.gsic.adolfinstro.Auxiliar.returnMain;
import static java.util.Objects.*;

public class Tarea extends AppCompatActivity {

    TextView tvDescripcion;
    ImageView ivImagenDescripcion;
    EditText etRespuestaTextual;
    Button btVolver, btAceptar, btCamara;
    String tipo;
    Bundle extras;
    int restantes = 3; //Este valor habrá que obtenerlo del intent

    Uri photoURI, videoURI;

    boolean estadoBtCamara, estadoBtCancelar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tarea);

        ivImagenDescripcion = findViewById(R.id.ivImagenDescripcion);
        extras = getIntent().getExtras();
        try {//ImagenDescriptiva
            new DownloadImages().execute(new URL(extras.getString("recursoAsociadoImagen")));
        }catch (Exception e){//Saltará cuando no tenga un recurso de imagen asociado
        }
        tvDescripcion = findViewById(R.id.tvDescripcion);
        etRespuestaTextual = findViewById(R.id.etRespuestaTextual);
        btVolver = findViewById(R.id.btVolver);
        btAceptar = findViewById(R.id.btAceptar);
        btCamara = findViewById(R.id.btCamara);

        try{//Descripcion
            tvDescripcion.setText(getIntent().getExtras().getString("recursoAsociadoTexto"));
            tvDescripcion.setVisibility(View.VISIBLE);
        }catch (Exception e){
            tvDescripcion.setText(getString(R.string.sinDescripcion));
        }
        tipo = requireNonNull(extras.getString("tipoRespuesta"));
        switch (tipo){
            case "preguntaCorta":
                etRespuestaTextual.setInputType(InputType.TYPE_CLASS_TEXT);
                etRespuestaTextual.setFilters(new InputFilter[] {new InputFilter.LengthFilter(40)});
                etRespuestaTextual.setVisibility(View.VISIBLE);
                btAceptar.setVisibility(View.VISIBLE);
                break;
            case "preguntaLarga":
                etRespuestaTextual.setVisibility(View.VISIBLE);
                btAceptar.setVisibility(View.VISIBLE);
                break;
            case "preguntaImagen":
                etRespuestaTextual.setVisibility(View.VISIBLE);
                btCamara.setVisibility(View.VISIBLE);
                break;
            case "imagen":
            case "imagenMultiple":
            case "video":
                btCamara.setVisibility(View.VISIBLE);
                break;
        }

        estadoBtCamara = true;
        estadoBtCancelar = true;
    }

    /**
     * Método que atiende a las pulsaciones en los botones
     * @param view Referencia al lanzador del evento
     */
    public void boton(View view) {
        switch (view.getId()){
            case R.id.btVolver:
                returnMain(this);
                break;
            case R.id.btAceptar:
                if(guardaRespuesta()) {
                    returnMain(this);
                }
                break;
            case R.id.btCamara:
                switch (tipo){
                    case "preguntaImagen":
                        if(guardaRespuesta()) {
                            bloqueaBotones();
                            realizaCaptura(0);
                        }
                        break;
                    case "imagen":
                            bloqueaBotones();
                            realizaCaptura(1);
                        break;
                    case "imagenMultiple":
                            bloqueaBotones();
                            realizaCaptura(2);
                        break;
                    case "video":
                            bloqueaBotones();
                            realizaVideo();
                        break;
                }
                break;
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
        btVolver.setClickable(estadoBtCancelar);
        btCamara.setClickable(estadoBtCamara);
    }

    /**
     * Agrupa las sentencias necesarias para la realización de una foto
     * @param tipo Tipo de operación desde donde se invoca al método. Se utiliza para diferenciarlas
     *             cuando se vuelve de la actividad de la cámara
     */
    private void realizaCaptura(int tipo){
        final Context context = getBaseContext();
        Intent takePicture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if(takePicture.resolveActivity(getPackageManager())!=null){
            File photoFile = null;
            try{
                photoFile = Auxiliar.createFile(tipo,this);
            }catch (IOException e){

            }
            if(photoFile != null){
                photoURI = FileProvider.getUriForFile(context, "es.uva.gsic.adolfinstro.fileprovider", photoFile);

                takePicture.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePicture, tipo);//Los requestCode solo pueden ser de 16 bits
            }
        } else{
            Toast.makeText(this, getString(R.string.errorOpera), Toast.LENGTH_SHORT).show();
            desbloqueaBt();
        }
    }

    private void realizaVideo(){
        Intent takeVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        File videoFile = null;
        try {
            videoFile = Auxiliar.createFile(3, this);
        }catch (IOException e){

        }
        if(videoFile != null){
            videoURI = FileProvider.getUriForFile(getBaseContext(), "es.uva.gsic.adolfinstro.fileprovider", videoFile);
            takeVideoIntent.putExtra(MediaStore.EXTRA_OUTPUT, videoURI);
            if(takeVideoIntent.resolveActivity(getPackageManager()) != null){
                //CALIDAD MMS
                //https://developer.android.com/reference/android/provider/MediaStore#EXTRA_VIDEO_QUALITY
                takeVideoIntent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 0);
                startActivityForResult(takeVideoIntent, 3);
            }else{
                Toast.makeText(this, getString(R.string.errorOpera), Toast.LENGTH_SHORT).show();
                desbloqueaBt();
            }
        }
    }

    /**
     * Método que desbloquea el botón de cancelar
     */
    private void desbloqueaBt(){
        estadoBtCancelar = true;
        setBotones();
    }

    /**
     * Método que se lanza para resolver el resultado de otra actividad. En nuestro caso se activa
     * cuando el usuario realice la captura o el vídeo
     * @param requestCode Código que identifica si ha sido una foto, un vídeo...
     * @param resultCode Código que devuelve la operación
     * @param data Intent
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        switch (requestCode){
            case 0: //Pregunta + imagen
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
            case 1://imagen
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
            case 2://imagen multiple
                switch (resultCode){
                    case RESULT_OK:
                        --restantes;
                        if(restantes==0){
                            Toast.makeText(this, getString(R.string.imagenesG), Toast.LENGTH_SHORT).show();
                            Auxiliar.returnMain(this);
                        }
                        else {
                            Toast.makeText(this, getString(R.string.imagenGN), Toast.LENGTH_SHORT).show();
                            realizaCaptura(2);
                        }
                        break;
                    case RESULT_CANCELED:
                        desbloqueaBt();
                        break;
                    default:
                        Auxiliar.errorToast(this);
                }
                break;
            case 3://video
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
     * Método para almacenar la respuesta del usuario
     * @return Devolverá true cuando la respuestas esté correctamente almacenada y
     * false en cualquier otro caso
     */
    private boolean guardaRespuesta(){
        String respuesta = etRespuestaTextual.getText().toString();
        respuesta = respuesta.trim();
        boolean salida = false;
        if(respuesta.isEmpty()){
            etRespuestaTextual.setError(getString(R.string.respuestaVacia));
        }
        else{
            //SALVAR LA RESPUESTA EN BBDD ¿ENVIARLA? SE UTILIZA TAMBIÉN PARA LA PREGUNTA CAMARA!!
            Toast.makeText(this, getString(R.string.respuestaG), Toast.LENGTH_SHORT).show();
            salida = true;
        }
        return salida;
    }

    /**
     * Método que se utiliza para almacenar el valor de algunas variables de la clase
     * @param bundle Objeto donde se almacenan las variables de la clase
     */
    @Override
    protected void onSaveInstanceState(Bundle bundle){
        super.onSaveInstanceState(bundle);
        bundle.putInt("RESTANTES", restantes);
        bundle.putBoolean("ESTADOCAMARA", estadoBtCamara);
        bundle.putBoolean("ESTADOCANCELAR", estadoBtCancelar);
    }

    /**
     * Método utilizado para restablecer el valor de algunas variables de la clase
     * @param bundle Objeto donde están almacenadas las variables de la clase entre otras cosas
     */
    @Override
    protected void onRestoreInstanceState(Bundle bundle){
        super.onRestoreInstanceState(bundle);
        restantes = bundle.getInt("RESTANTES");
        estadoBtCamara = bundle.getBoolean("ESTADOCAMARA");
        estadoBtCancelar = bundle.getBoolean("ESTADOCANCELAR");
        setBotones();
    }


    /**
     * Clase que se encarga de obtener la imagen del servidor
     */
    private class DownloadImages extends AsyncTask<URL, Void, Bitmap> {

        /**
         * Método encargado de descargar las imágenes de las URLs que indiquen
         * @param urls URLs a descargar. Según está implementado, solamente descarga la primera URL
         * @return Imagen descargada o null si no se pudo descargar
         */
        protected Bitmap doInBackground(URL... urls) {
            try {
                ivImagenDescripcion.setImageResource(R.drawable.ic_cloud_download_blue_80dp);
                ivImagenDescripcion.setVisibility(View.VISIBLE);
                RotateAnimation rotateAnimation = new RotateAnimation(0f, 359f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                rotateAnimation.setInterpolator(new LinearInterpolator());
                rotateAnimation.setRepeatCount(Animation.INFINITE);
                rotateAnimation.setDuration(1200);
                ivImagenDescripcion.startAnimation(rotateAnimation);
                return BitmapFactory.decodeStream(urls[0].openConnection().getInputStream());
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }

        /**
         * Método que carga la imagen descargada en la interfaz gráfica de la aplicación
         * @param bitmap Imagen que se va a mostrar
         */
        @Override
        protected void onPostExecute(Bitmap bitmap){
            if (bitmap != null) {
                ivImagenDescripcion.setAnimation(null);
                ivImagenDescripcion.setImageBitmap(bitmap);
                ivImagenDescripcion.setAdjustViewBounds(true);
            } else {
                ivImagenDescripcion.setImageResource(R.drawable.ic_close_red_80dp);
            }
            ivImagenDescripcion.setVisibility(View.VISIBLE);
        }
    }
}
