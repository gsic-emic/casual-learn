package es.uva.gsic.adolfinstro;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
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

import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

import es.uva.gsic.adolfinstro.auxiliar.Auxiliar;
import es.uva.gsic.adolfinstro.persistencia.PersistenciaDatos;

import static java.util.Objects.*;

/**
 * Clase encargada de mostrar la tarea al usuario. A medida que complete la actividad se irá almacenando
 * la respuesta en una base de datos.
 *
 * @author GSIC
 * @version 20200319
 */
public class Tarea extends AppCompatActivity {

    /** Instancia donde se colocará la imagen descriptiva de la tarea*/
    private ImageView ivImagenDescripcion;
    /** Instancia del campo de texto donde introduce el usuario la respuesta*/
    private EditText etRespuestaTextual;
    /** Instancia para volver a la actividad principal sin finalizar la tarea*/
    private Button btVolver;
    /** Instancia del botón de la cámara*/
    private Button btCamara;
    /** Instancia donde se almacena el tipo de tarea*/
    private String tipo;
    /** Instancia donde se almacena el identificador de la tarea */
    private String idTarea;
    /** Imagen descriptiva en baja resolución */
    private String recursoAsociadoImagen300px;
    /** Imagen descriptiva en alta resolución */
    private String recursoAsociadoImagen;
    /** Texto que se espera que tenga la respuesta del alumno*/
    private String respuestaEsperada;
    /** Número de fotos de la serie */
    private int restantes = 3; //Este valor habrá que obtenerlo del intento

    /** URI de la imagen que se acaba de tomar */
    private Uri photoURI;
    /** URI del vídeo que se acaba de tomar */
    private Uri videoURI;

    /** Estado del botón de la cámara */
    private boolean estadoBtCamara;
    /** Estado del botón para volver a la actividad principal*/
    private boolean estadoBtCancelar;

    private JSONObject respuesta;

    /**
     * Método que se lanza al inicio de la vida de la actividad. Se encarga de dibujar la interfaz
     * gráfica sobre la que va a trabajar el cliente. Establece la conexión con la base de datos
     * para que se consiga la persistencia.
     *
     * @param savedInstanceState Bundle
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tarea);

        ivImagenDescripcion = findViewById(R.id.ivImagenDescripcion);
        Bundle extras = getIntent().getExtras();
        assert extras != null;
        idTarea = extras.getString(Auxiliar.id);
        try {//ImagenDescriptiva. Se intenta mostrar la imagen de baja resolución
            new DownloadImages().execute(new URL(extras.getString(Auxiliar.recursoImagenBaja)));
            recursoAsociadoImagen300px = extras.getString(Auxiliar.recursoImagenBaja);
            recursoAsociadoImagen = extras.getString(Auxiliar.recursoImagen);
        } catch (Exception e) {//Saltará cuando no tenga una imagen de baja resolución asociada
            try {
                new DownloadImages().execute(new URL(extras.getString(Auxiliar.recursoImagen)));
                recursoAsociadoImagen = extras.getString(Auxiliar.recursoImagen);
            } catch (Exception e2) {//No tiene ni la imagen principal ni el icono

            }
        }
        tipo = requireNonNull(extras.getString(Auxiliar.tipoRespuesta));
        respuestaEsperada = extras.getString(Auxiliar.respuestaEsperada);
        TextView tvDescripcion = findViewById(R.id.tvDescripcion);
        etRespuestaTextual = findViewById(R.id.etRespuestaTextual);
        btVolver = findViewById(R.id.btVolver);
        Button btAceptar = findViewById(R.id.btAceptar);
        btCamara = findViewById(R.id.btCamara);

        try {//Descripcion
            tvDescripcion.setText(getIntent().getExtras().getString(Auxiliar.recursoAsociadoTexto));
            tvDescripcion.setVisibility(View.VISIBLE);
        } catch (Exception e) {
            tvDescripcion.setText(getString(R.string.sinDescripcion));
        }

        try {
            switch (tipo) {
                case "sinRespuesta":
                    btAceptar.setText(R.string.voy);
                    btAceptar.setVisibility(View.VISIBLE);
                    break;
                case "preguntaCorta":
                    etRespuestaTextual.setInputType(InputType.TYPE_CLASS_TEXT);
                    etRespuestaTextual.setFilters(new InputFilter[]{new InputFilter.LengthFilter(40)});
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
                case "video":
                case "imagenMultiple":
                    btCamara.setVisibility(View.VISIBLE);
                    break;
                default:
                    break;
            }
        } catch (Exception e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
        }

        estadoBtCamara = true;
        estadoBtCancelar = true;

        checkPermissions();

        if(!PersistenciaDatos.existeTarea(getApplication(), PersistenciaDatos.ficheroRespuestas, idTarea))
            mensajeError();

    }

    private void mensajeError(){
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
        alertBuilder.setTitle(getString(R.string.tituErrorBBDD));
        alertBuilder.setMessage(getString(R.string.ErrorBBDD));
        alertBuilder.setPositiveButton(getString(R.string.accept), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Auxiliar.returnMain(getBaseContext());
            }
        });
        alertBuilder.show();
    }

    /** Código de identificación para la solicitud de los permisos de la app */
    private static final int requestCodePermissions = 1002;

    /**
     * Método para comprobar si el usuario ha otorgado a la aplicación los permisos necesarios.
     * En la actualidad, solicita permisos de localización y cámara.
     */
    public void checkPermissions() {
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY))
            System.exit(-1);
        ArrayList<String> permisos = new ArrayList<>();
        Auxiliar.preQueryPermisos(this, permisos);
        if (permisos.size() > 0) //Evitamos hacer una petición con un array nulo
            ActivityCompat.requestPermissions(this, permisos.toArray(new String[permisos.size()]), requestCodePermissions);
    }

    /**
     * Método que devuelve el resultado de la solicitud de permisos.
     * @param requestCode Código de la petición de permismos.
     * @param permissions Permisos que se han solicitado.
     * @param grantResults Valor otorgado por el usuario al permiso.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NotNull String[] permissions, int[] grantResults) {
        //Se comprueba uno a uno si alguno de los permisos no se había aceptado
        for (int i : grantResults) {
            if (i == -1) {
                AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
                alertBuilder.setTitle(getString(R.string.permi));
                alertBuilder.setMessage(getString(R.string.permiM));
                alertBuilder.setPositiveButton(getString(R.string.accept), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //Se comprueba todos los permisos que necesite la app de nuevo, por este
                        // motivo se puede salir del for directamente
                        checkPermissions();
                    }
                });
                alertBuilder.setNegativeButton(getString(R.string.exi), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //Si el usuario no quiere conceder los permisos que necesita la aplicación se
                        //cierra
                        System.exit(0);
                    }
                });
                alertBuilder.show();
                break;
            }
        }
    }

    /**
     * Método que atiende a las pulsaciones en los botones
     * @param view Referencia al lanzador del evento
     */
    public void boton(View view) {
        switch (view.getId()){
            case R.id.btVolver:
                Auxiliar.returnMain(this);
                break;
            case R.id.btAceptar:
                if(tipo.equals(TiposTareas.SIN_RESPUESTA.getValue())){
                    try {
                        respuesta = PersistenciaDatos.generaJSON(idTarea, tipo, EstadoTarea.COMPLETADA);
                        PersistenciaDatos.guardaJSON(getApplication(), PersistenciaDatos.ficheroRespuestas, respuesta, Context.MODE_PRIVATE);
                    }catch (Exception e){
                        mensajeError();
                    }
                    Auxiliar.puntuaTarea(this, idTarea);
                } else
                    if(guardaRespuestaPregunta())
                        Auxiliar.puntuaTarea(this, idTarea);
                break;
            case R.id.btCamara:
                switch (tipo){
                    case "preguntaImagen":
                        if(guardaRespuestaPregunta()) {
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
            case R.id.ivImagenDescripcion:
                if(recursoAsociadoImagen != null) {//Si la imagen en alta resolución existe se salta simpre a ella para la vista en detalle
                    Intent intent = new Intent(this, ImagenCompleta.class);
                    intent.putExtra("IMAGENCOMPLETA", recursoAsociadoImagen);
                    startActivity(intent);
                }else{//Ya está visible la imagen de resolución baja y no hay una alta asociada
                    if(recursoAsociadoImagen300px != null){
                        Intent intent = new Intent(this, ImagenCompleta.class);
                        intent.putExtra("IMAGENCOMPLETA", recursoAsociadoImagen);
                        startActivity(intent);
                    }
                    else{//No hay ninguna imagen. Este mensaje no debería aparecer nunca
                        Toast.makeText(this, getString(R.string.recursoMaximaResolucion), Toast.LENGTH_LONG).show();
                    }
                }
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
                System.err.println("realizaCaptura: Error al crear el fichero base");
            }
            if(photoFile != null){
                photoURI = FileProvider.getUriForFile(context, "es.uva.gsic.adolfinstro.fileprovider", photoFile);

                takePicture.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePicture, tipo);//Los requestCode solo pueden ser de 16 bits
            }
            else{
                Toast.makeText(this, getString(R.string.errorOpera), Toast.LENGTH_SHORT).show();
                desbloqueaBt();
            }
        } else{
            Toast.makeText(this, getString(R.string.errorOpera), Toast.LENGTH_SHORT).show();
            desbloqueaBt();
        }
    }

    /**
     * Método encargado de realizar las tareas necesarias para que el usuario pueda grabar un vídeo
     * con la aplicación. Realiza una llamada a la cámara del sistema. Para ahorrar espacio, los
     * vídeos se realizan en baja calidad.
     */
    private void realizaVideo(){
        Intent takeVideo = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);

        File videoFile = null;
        try {
            videoFile = Auxiliar.createFile(3, this);
        }catch (IOException e){
            //
        }
        if(videoFile != null){
            videoURI = FileProvider.getUriForFile(getBaseContext(), "es.uva.gsic.adolfinstro.fileprovider", videoFile);
            takeVideo.putExtra(MediaStore.EXTRA_OUTPUT, videoURI);
            if(takeVideo.resolveActivity(getPackageManager()) != null){
                //CALIDAD MMS
                //https://developer.android.com/reference/android/provider/MediaStore#EXTRA_VIDEO_QUALITY
                takeVideo.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 0);
                startActivityForResult(takeVideo, 3);
            }else{
                Toast.makeText(this, getString(R.string.errorOpera), Toast.LENGTH_SHORT).show();
                desbloqueaBt();
            }
        }
        else{
            Toast.makeText(this, getString(R.string.errorOpera), Toast.LENGTH_SHORT).show();
            desbloqueaBt();
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
            case 1: //Imagen
                switch (resultCode){
                    case RESULT_OK:
                        if(!PersistenciaDatos.guardaJSON(getApplication(), PersistenciaDatos.ficheroRespuestas,
                                PersistenciaDatos.generaJSON(idTarea, tipo, EstadoTarea.COMPLETADA),
                                photoURI.toString(), Context.MODE_PRIVATE))
                            mensajeError();
                        Toast.makeText(this, getString(R.string.imagenG), Toast.LENGTH_SHORT).show();

                        Auxiliar.puntuaTarea(this, idTarea);
                        break;
                    case RESULT_CANCELED:
                        if(!PersistenciaDatos.guardaJSON(getApplication(), PersistenciaDatos.ficheroRespuestas,
                                PersistenciaDatos.generaJSON(idTarea, tipo, EstadoTarea.NO_COMPLETADA),
                                Context.MODE_PRIVATE))
                            mensajeError();
                        desbloqueaBt();
                        break;
                    default:
                        if(!PersistenciaDatos.guardaJSON(getApplication(), PersistenciaDatos.ficheroRespuestas,
                                PersistenciaDatos.generaJSON(idTarea, tipo, EstadoTarea.NO_COMPLETADA),
                                Context.MODE_PRIVATE))
                            mensajeError();
                        Auxiliar.errorToast(this);
                }
                break;
            case 2://imagen multiple
                switch (resultCode){
                    case RESULT_OK:
                        --restantes;
                        String respuesta;
                        if(!PersistenciaDatos.guardaJSON(getApplication(), PersistenciaDatos.ficheroRespuestas,
                                PersistenciaDatos.generaJSON(idTarea, tipo, EstadoTarea.NO_COMPLETADA),
                                photoURI.toString(), Context.MODE_PRIVATE))
                            mensajeError();
                        if(restantes==0){
                            if(!PersistenciaDatos.guardaJSON(getApplication(), PersistenciaDatos.ficheroRespuestas,
                                    PersistenciaDatos.generaJSON(idTarea, tipo, EstadoTarea.COMPLETADA),
                                    photoURI.toString(), Context.MODE_PRIVATE))
                                mensajeError();
                            Toast.makeText(this, getString(R.string.imagenesG), Toast.LENGTH_SHORT).show();
                            Auxiliar.puntuaTarea(this, idTarea);
                        }
                        else {
                            if(!PersistenciaDatos.guardaJSON(getApplication(), PersistenciaDatos.ficheroRespuestas,
                                    PersistenciaDatos.generaJSON(idTarea, tipo, EstadoTarea.NO_COMPLETADA),
                                    Context.MODE_PRIVATE))
                                mensajeError();
                            Toast.makeText(this, getString(R.string.imagenGN), Toast.LENGTH_SHORT).show();
                            realizaCaptura(2);
                        }
                        break;
                    case RESULT_CANCELED:
                        if(!PersistenciaDatos.guardaJSON(getApplication(), PersistenciaDatos.ficheroRespuestas,
                                PersistenciaDatos.generaJSON(idTarea, tipo, EstadoTarea.NO_COMPLETADA),
                                Context.MODE_PRIVATE))
                            mensajeError();
                        desbloqueaBt();
                        break;
                    default:
                        if(!PersistenciaDatos.guardaJSON(getApplication(), PersistenciaDatos.ficheroRespuestas,
                                PersistenciaDatos.generaJSON(idTarea, tipo, EstadoTarea.NO_COMPLETADA),
                                Context.MODE_PRIVATE))
                            mensajeError();
                        Auxiliar.errorToast(this);
                }
                break;
            case 3://video
                switch (resultCode){
                    case RESULT_OK:
                        if(!PersistenciaDatos.guardaJSON(getApplication(), PersistenciaDatos.ficheroRespuestas,
                                PersistenciaDatos.generaJSON(idTarea, tipo, EstadoTarea.COMPLETADA),
                                videoURI.toString(), Context.MODE_PRIVATE))
                            mensajeError();
                        Toast.makeText(this, getString(R.string.videoG), Toast.LENGTH_SHORT).show();
                        Auxiliar.puntuaTarea(this, idTarea);
                        break;
                    case RESULT_CANCELED:
                        if(!PersistenciaDatos.guardaJSON(getApplication(), PersistenciaDatos.ficheroRespuestas,
                                PersistenciaDatos.generaJSON(idTarea, tipo, EstadoTarea.NO_COMPLETADA),
                                Context.MODE_PRIVATE))
                            mensajeError();
                        desbloqueaBt();
                        break;
                    default:
                        if(!PersistenciaDatos.guardaJSON(getApplication(), PersistenciaDatos.ficheroRespuestas,
                                PersistenciaDatos.generaJSON(idTarea, tipo, EstadoTarea.NO_COMPLETADA),
                                Context.MODE_PRIVATE))
                            mensajeError();
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
    private boolean guardaRespuestaPregunta(){
        String respuesta = etRespuestaTextual.getText().toString();
        respuesta = respuesta.trim();
        boolean salida = false;
        if(respuesta.isEmpty()){
            etRespuestaTextual.setError(getString(R.string.respuestaVacia));
        }
        else{
            if(tipo.equals("preguntaCorta") || tipo.equals("preguntaLarga")) {
                if(!PersistenciaDatos.guardaJSON(getApplication(), PersistenciaDatos.ficheroRespuestas,
                        PersistenciaDatos.generaJSON(idTarea, tipo, EstadoTarea.COMPLETADA),
                        respuesta, Context.MODE_PRIVATE))
                    mensajeError();
            }
            else {
                if(!PersistenciaDatos.guardaJSON(getApplication(), PersistenciaDatos.ficheroRespuestas,
                        PersistenciaDatos.generaJSON(idTarea, tipo, EstadoTarea.NO_COMPLETADA),
                        respuesta, Context.MODE_PRIVATE))
                    mensajeError();
            }
            if(respuestaEsperada!=null){
                if (respuesta.contains(respuestaEsperada)) {
                    Toast.makeText(this, R.string.respuestaEspeCorrecta, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, R.string.respuestaEspeIncrrecta + " " + respuestaEsperada, Toast.LENGTH_SHORT).show();
                }
            }else {
                Toast.makeText(this, getString(R.string.respuestaG), Toast.LENGTH_SHORT).show();
            }
            salida = true;
        }

        return salida;
    }

    private void mensaje(boolean acierto){
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
        alertBuilder.setTitle(getString(R.string.respuestaEspeTitulo));
        if(acierto){
            alertBuilder.setIcon(R.drawable.ic_check_green_24dp);
            alertBuilder.setMessage(R.string.respuestaEspeCorrecta);
        }else{
            alertBuilder.setIcon(R.drawable.ic_thumb_down_red_24dp);
            alertBuilder.setMessage(getString(R.string.respuestaEspeIncrrecta)+"\r\n"+respuestaEsperada);
        }

        alertBuilder.setPositiveButton(getString(R.string.accept), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        alertBuilder.show();
    }

    /**
     * Método que se utiliza para almacenar el valor de algunas variables de la clase
     * @param bundle Objeto donde se almacenan las variables de la clase
     */
    @Override
    protected void onSaveInstanceState(@NotNull Bundle bundle){
        super.onSaveInstanceState(bundle);
        bundle.putInt("RESTANTES", restantes);
        bundle.putBoolean("ESTADOCAMARA", estadoBtCamara);
        bundle.putBoolean("ESTADOCANCELAR", estadoBtCancelar);
        bundle.putString("IDTAREA", idTarea);
        bundle.putString("TIPOTAREA", tipo);
        bundle.putString("RECURSOIMAGEN300", recursoAsociadoImagen300px);
        bundle.putString("RECURSOIMAGEN", recursoAsociadoImagen);
    }

    /**
     * Método utilizado para restablecer el valor de algunas variables de la clase
     * @param bundle Objeto donde están almacenadas las variables de la clase entre otras cosas
     */
    @Override
    protected void onRestoreInstanceState(@NotNull Bundle bundle){
        super.onRestoreInstanceState(bundle);
        restantes = bundle.getInt("RESTANTES");
        estadoBtCamara = bundle.getBoolean("ESTADOCAMARA");
        estadoBtCancelar = bundle.getBoolean("ESTADOCANCELAR");
        idTarea = bundle.getString("IDTAREA");
        tipo = bundle.getString("TIPOTAREA");
        recursoAsociadoImagen300px = bundle.getString("RECURSOIMAGEN300");
        recursoAsociadoImagen = bundle.getString("RECURSOIMAGEN");
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
                RotateAnimation rotateAnimation = new RotateAnimation(
                        0f,
                        359f,
                        Animation.RELATIVE_TO_SELF,
                        0.5f,
                        Animation.RELATIVE_TO_SELF,
                        0.5f
                );
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
            ivImagenDescripcion.setAnimation(null);
            if (bitmap != null) {
                ivImagenDescripcion.setImageBitmap(bitmap);
                ivImagenDescripcion.setAdjustViewBounds(true);
            } else {
                ivImagenDescripcion.setImageResource(R.drawable.ic_close_red_80dp);
            }
            ivImagenDescripcion.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Método que establece la conexión con la base de datos si no existiera
     */
    public void onResume() {
        super.onResume();
    }

    /**
     * Método para controlar la llamada del usuario al botón atrás
     */
    @Override
    public void onBackPressed(){
        super.onBackPressed();
        Auxiliar.returnMain(this);
    }
}
