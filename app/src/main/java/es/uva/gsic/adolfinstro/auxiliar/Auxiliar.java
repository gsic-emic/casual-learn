package es.uva.gsic.adolfinstro.auxiliar;

import android.Manifest;
import android.app.Application;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import es.uva.gsic.adolfinstro.Ajustes;
import es.uva.gsic.adolfinstro.Maps;
import es.uva.gsic.adolfinstro.R;
import es.uva.gsic.adolfinstro.Puntuacion;
import es.uva.gsic.adolfinstro.persistencia.PersistenciaDatos;

public class Auxiliar {

    public static final String id = "id";
    public static final String tipoRespuesta = "tipoRespuesta";
    public static final String latitud = "latitud";
    public static final String longitud = "longitud";
    public static final String recursoImagenBaja = "recursoAsociadoImagen300px";
    public static final String recursoImagen = "recursoAsociadoImagen";
    public static final String recursoAsociadoTexto = "recursoAsociadoTexto";
    public static final String respuestaEsperada = "respuestaEsperada";
    public static final String titulo = "titulo";
    public static final String instante = "instante";
    public static final String estadoTarea = "estadoTarea";
    public static final String rating = "rating";
    public static final String fechaNotificiacion = "fechaNotificacion";
    public static final String fechaUltimaModificacion = "fechaUltimaModificacion";

    public static final String radio = "radio";
    public static final String nunca_mas = "NUNCA_MAS";
    public static final String ahora_no = "AHORA_NO";

    /** Identificador del canal de tareas */
    public static final String channelId = "notiTareas";
    public static final String cargaImagenPreview = "imagenPreview";
    public static final String cargaImagenTarea = "imagenTarea";
    public static final String cargaImagenDetalles = "imagenDetalles";

    public static final String tipoSinRespuesta = "sinRespuesta";
    public static final String tipoImagen = "imagen";
    public static final String tipoImagenMultiple = "imagenMultiple";
    public static final String tipoVideo = "video";
    public static final String tipoPreguntaCorta = "preguntaCorta";
    public static final String tipoPreguntaLarga = "preguntaLarga";
    public static final String tipoPreguntaImagen = "preguntaImagen";

    private static SimpleDateFormat formatoFecha = new SimpleDateFormat("HH:mm - dd/MM/yyyy");

    //private static Random random = new Random();


    private static final String[] listaFabricantes = {"huawei", "xiaomi"};

    /**
     * Creación del fichero donde se almacena la foto o el vídeo
     * @param type 1 foto, 2 vídeo
     * @return fichero donde se almacena la foto/vídeo
     * @throws IOException Se lanza una excepción cuando se produzca un error al crear el fichero vacío
     */
    public static File createFile(int type, Context context) throws IOException{
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmssSSS").format(new Date());
        File mediaFile = null;
        switch (type){
            case 0:
            case 1:
            case 2:
                mediaFile = File.createTempFile("JPG_"+timeStamp,".jpg", context.getExternalFilesDir(Environment.DIRECTORY_PICTURES));
                break;
            case 3:
                mediaFile = File.createTempFile("VID_"+timeStamp, ".mp4", context.getExternalFilesDir(Environment.DIRECTORY_MOVIES));
                break;
            default:
        }
        return mediaFile;
    }

    /**
     * Método utilizado para volver a la actividad principal
     */
    public static void returnMain(Context context){
        Intent intent = new Intent(context, Maps.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    /**
     * Impreme un toast para indicar al usuario que ha sucedido algún problema
     */
    public static void errorToast(Context context) {
        Toast.makeText(context, context.getString(R.string.errorOpera), Toast.LENGTH_SHORT).show();
    }

    /**
     * Método para preparar la petición de los permisos necesarios al usuario
     * @param context Contexto
     * @param permisos Vector donde agregar los permisos a los que el usuario aún no haya dado permiso
     */
    public static void preQueryPermisos(Context context, ArrayList<String> permisos){
        if(!(ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED))
            permisos.add(Manifest.permission.ACCESS_FINE_LOCATION);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
            if(!(ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED))
                permisos.add(Manifest.permission.ACCESS_BACKGROUND_LOCATION);
        if(!(ActivityCompat.checkSelfPermission(context, Manifest.permission.INTERNET) == PackageManager.PERMISSION_GRANTED))
            permisos.add(Manifest.permission.INTERNET);
        if(!(ActivityCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED))
            permisos.add(Manifest.permission.CAMERA);
        if(!(ActivityCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED))
            permisos.add(Manifest.permission.RECORD_AUDIO);
        if(!(ActivityCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED))
            permisos.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
    }

    /**
     * Método que muestra el mensaje de la lista blanca
     * @param context contexto
     * @param sharedPreferences preferencias
     */
    public static void dialogoAyudaListaBlanca(Context context, final SharedPreferences sharedPreferences){
        List<String> fabricantesProblemas = Arrays.asList(listaFabricantes);
        if (fabricantesProblemas.contains(Build.MANUFACTURER.toLowerCase())) {
            AlertDialog.Builder brandBuilder = new AlertDialog.Builder(context);
            brandBuilder.setTitle(context.getString(R.string.tituloErrorMarca) + Build.MANUFACTURER.toLowerCase())
                    .setMessage(context.getString(R.string.mensajeSolucionMarca))
                    .setPositiveButton(context.getString(R.string.accept), new Dialog.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            actualizaListaBlanca(false, sharedPreferences);
                        }
                    })
                    .setNegativeButton(context.getString(R.string.cancel), new Dialog.OnClickListener(){
                        @Override
                        public void onClick(DialogInterface dialog, int which) { }
                    });
            brandBuilder.show();
        }
        else{
            actualizaListaBlanca(false, sharedPreferences);
        }
    }

    /**
     * Método para actualizar el valor de la preferencia listaBlanca
     * @param listaBlanca valor que va a tomar la preferencia
     * @param sharedPreferences preferencias
     */
    private static void actualizaListaBlanca(boolean listaBlanca, SharedPreferences sharedPreferences) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(Ajustes.LISTABLANCA_pref, listaBlanca);
        editor.commit();
    }

    /**
     * Método para puntuar una tarea una vez que se ha completado
     * @param context Contexto
     * @param idTarea Identificador de la tarea a puntuar
     */
    public static void puntuaTarea(Context context, String idTarea){
        Intent intent = new Intent(context, Puntuacion.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("ID", idTarea);
        context.startActivity(intent);
    }

    /**
     * Método para calcular la distancia entre dos puntos
     * @param lat1 Latitud del punto 1 (en grados)
     * @param lon1 Longitud del punto 1 (en grados)
     * @param lat2 Latitud del punto 2 (en grados)
     * @param lon2 Longitud del punto 2 (en grados)
     * @return Distancia (en km) entre los dos puntos
     */
    public static double calculaDistanciaDosPuntos(double lat1, double lon1, double lat2, double lon2){
        //Aproximación del radio de la Tierra
        double radioTierra = 6371;
        return 2 * radioTierra *
                Math.asin(Math.sqrt(
                        (1-Math.cos(Math.toRadians(lat2-lat1)))/2 +
                                Math.cos(Math.toRadians(lat1))*
                                        Math.cos(Math.toRadians(lat2))*
                                        ((1-Math.cos(Math.toRadians(lon2 - lon1)))/2)
                ));
    }
    /**
     * Método que obtiene la última parte de un recurso
     * @param tipoRespuesta URI del tipo de tarea
     * @return Tipo de tarea
     */
    public static String ultimaParte(String tipoRespuesta) {
        String[] string = tipoRespuesta.split("/");
        return string[string.length - 1];
    }

    /**
     * Método para obtener la tarea más cercana al usuario. Si hay varias tareas a la misma
     * distancia se obtiene una de ellas aleatoriamente.
     *
     * @param app Aplicación
     * @param latitudUsuario Latitud de la posción del usuario
     * @param longitudUsuario Longitud de la posición del usuario
     * @return JSONObject con la tarea a realizar o null
     */
    public static JSONObject tareaMasCercana(Application app, double latitudUsuario, double longitudUsuario){
        //Inicializo la lista de tareas a la misma distancia
        List<JSONObject> tarea = new ArrayList<>();
        //Creo la referencia al objeto JSONObject para que no se esté creando y destruyendo en cada
        //iteración del bucle
        JSONObject tareaEvaluada;
        //Distancia a la que se encuentra el usuario de la tarea
        double distancia;
        //Distancia más pequeña encontrada del usuario a una tarea
        double distanciaMin = 10000;
        try{
            //Se obtienen las tareas del fichero
            JSONArray vectorTareas = PersistenciaDatos.leeFichero(app, PersistenciaDatos.ficheroTareasUsuario);
            for(int i = 0; i < vectorTareas.length(); i++){//Se recorren todas las tareas del fichero
                tareaEvaluada = vectorTareas.getJSONObject(i);
                if(!tarea.isEmpty()){
                    distancia = calculaDistanciaDosPuntos(tareaEvaluada.getDouble(Auxiliar.latitud),
                            tareaEvaluada.getDouble(Auxiliar.longitud),
                            latitudUsuario,
                            longitudUsuario);
                    if(distancia < distanciaMin){
                        distanciaMin = distancia;
                        tarea = new ArrayList<>();
                        tarea.add(tareaEvaluada);
                    }else{
                        if(distancia == distanciaMin){
                            tarea.add(tareaEvaluada);
                        }
                    }
                }else {//Solo se va entrar aquí con la primera tarea
                    tarea.add(tareaEvaluada);
                    distanciaMin = calculaDistanciaDosPuntos(tareaEvaluada.getDouble(Auxiliar.latitud),
                            tareaEvaluada.getDouble(Auxiliar.longitud),
                            latitudUsuario,
                            longitudUsuario);
                }
            }
            //Devolvemos una de las tareas del vector escogida de manera aleatorio
            return tarea.get((int)(Math.random()*tarea.size()));
        }
        catch (Exception e){
            return null;
        }
    }

    public static String horaFechaActual() {
        return formatoFecha.format(Calendar.getInstance().getTime());
    }

    public static IntentFilter intentFilter() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Auxiliar.nunca_mas);
        intentFilter.addAction(Auxiliar.ahora_no);
        return intentFilter;
    }

    public static int iconoTipoTarea(String tR) {
        int iconoTarea;
        switch (tR){
            case Auxiliar.tipoSinRespuesta:
                iconoTarea = R.drawable.ic_sin_respuesta;
                break;
            case Auxiliar.tipoPreguntaCorta:
                iconoTarea = R.drawable.ic_preguntacorta;
                break;
            case Auxiliar.tipoPreguntaLarga:
                iconoTarea = R.drawable.ic_preguntalarga;
                break;
            case Auxiliar.tipoPreguntaImagen:
                iconoTarea = R.drawable.ic_preguntaimagen;
                break;
            case Auxiliar.tipoImagen:
                iconoTarea = R.drawable.ic_imagen;
                break;
            case Auxiliar.tipoImagenMultiple:
                iconoTarea = R.drawable.ic_imagenmultiple;
                break;
            case Auxiliar.tipoVideo:
                iconoTarea = R.drawable.ic_video;
                break;
            default:
                iconoTarea = 0;
                break;
        }
        return iconoTarea;
    }
}
