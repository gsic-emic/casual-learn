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
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.text.Html;
import android.text.Layout;
import android.text.SpannableStringBuilder;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.style.ClickableSpan;
import android.text.style.URLSpan;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.osmdroid.util.BoundingBox;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
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

/**
 * Clase auxiliar de la aplicación. Contiene los strings utilizados como identificadores de toda la
 * aplicación. Los métodos son utilizados en otras clases.
 *
 * @author Pablo
 * @version 20201002
 */
public class Auxiliar {

    public static final String direccionIP = "https://casuallearnapp.gsic.uva.es/app/";

    public static final String id = "id";
    public static final String tipoRespuesta = "tipoRespuesta";
    public static final String latitud = "latitud";
    public static final String longitud = "longitud";
    public static final String recursoImagenBaja = "thumbnail";
    public static final String recursoImagen = "recursoAsociadoImagen";
    public static final String recursoAsociadoTexto = "recursoAsociadoTexto";
    public static final String respuestaEsperada = "respuestaEsperada";
    public static final String titulo = "comment";
    public static final String instante = "instante";
    public static final String estadoTarea = "estadoTarea";
    public static final String rating = "rating";
    public static final String fechaNotificiacion = "fechaNotificacion";
    public static final String fechaInicio = "fechaInicio";
    public static final String fechaUltimaModificacion = "fechaUltimaModificacion";
    public static final String fechaFinalizacion = "fechaFinalizacion";
    public static final String origen = "origen";

    public static final String posUsuarioLat = "posUsuarioLat";
    public static final String posUsuarioLon = "posUsuarioLon";

    public static final String nunca_mas = "NUNCA_MAS";
    public static final String ahora_no = "AHORA_NO";

    /** Identificador del canal de tareas */
    public static final String channelId = "notiTareas";
    public static final String cargaImagenPreview = "imagenPreview";
    public static final String cargaImagenTarea = "imagenTarea";
    public static final String cargaImagenDetalles = "imagenDetalles";

    public static final String tipoSinRespuesta = "sinRespuesta";
    public static final String tipoImagen = "fotografia";
    public static final String tipoImagenMultiple = "multiplesFotografias";
    public static final String tipoVideo = "video";
    public static final String tipoPreguntaCorta = "texto";
    public static final String tipoPreguntaLarga = "preguntaLarga";
    public static final String tipoPreguntaImagen = "fotografiaYTexto";
    public static final String tipoPreguntaImagenes = "multiplesFotografiasYTexto";

    public static final String peticion = "peticion";

    public static final String idNotificacion = "idNotificacion";

    public static final String respuestas = "respuestaTarea";
    public static final String posicionRespuesta = "posicion";
    public static final String respuestaRespuesta = "respuesta";
    public static final String texto = "texto";
    public static final String uri = "uri";

    public static final String previa = "actividad_previa";
    public static final String notificacion = "notificacion";
    public static final String mapa = "mapa";
    public static final String tareasRechazadas = "tareasRechazadas";
    public static final String tareasPospuestas = "tareasPospuestas";
    public static final String zum = "zum";

    public static final String lat0 = "lat0";
    public static final String lon0 = "lon0";
    public static final String latN = "latN";
    public static final String latS = "latS";
    public static final String lonO = "latO";
    public static final String lonE = "lonE";
    public static final String tareas = "tareas";
    public static final String ficheroOrigen = "ficheroOrigen";
    public static final String textoParaElMapa = "textoParaElMapa";
    public static final String uid = "uid";
    public static final String idUsuario = "idUsuario";

    private static SimpleDateFormat formatoFecha = new SimpleDateFormat("HH:mm:ss - dd/MM/yyyy");


    private static final String[] listaFabricantes = {"huawei", "xiaomi", "samsung", "oneplus"};

    public static int incr = 0;

    public static JSONArray municipios = null;

    /**
     * Creación del fichero donde se almacena la foto o el vídeo
     * @param type 0, 1, 2: foto; 3 video;
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
                mediaFile = File.createTempFile("JPG_"+timeStamp,".jpg",
                        context.getExternalFilesDir(Environment.DIRECTORY_PICTURES));
                break;
            case 3:
                mediaFile = File.createTempFile("VID_"+timeStamp, ".mp4",
                        context.getExternalFilesDir(Environment.DIRECTORY_MOVIES));
                break;
            case 4:
                mediaFile = File.createTempFile(timeStamp, ".jpg",
                        context.getExternalFilesDir("Cache"));
                break;
            default:
                break;
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
     * Método para preparar la petición de los permisos relacionados con la posición
     * @param context Contexto
     * @return Permisos que faltan por condeder
     */
    public static ArrayList<String> preQueryPermisos(Context context){
        ArrayList<String> permisos = new ArrayList<>();
        if(!(ActivityCompat.checkSelfPermission(
                context, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED))
            permisos.add(Manifest.permission.ACCESS_FINE_LOCATION);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
            if(!(ActivityCompat.checkSelfPermission(
                    context, Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                    == PackageManager.PERMISSION_GRANTED))
                permisos.add(Manifest.permission.ACCESS_BACKGROUND_LOCATION);
        return permisos;
    }

    /**
     * Método que muestra el mensaje de la lista blanca
     * @param context contexto
     * @param sharedPreferences preferencias
     */
    public static void dialogoAyudaListaBlanca(Context context,
                                               final SharedPreferences sharedPreferences){
        List<String> fabricantesProblemas = Arrays.asList(listaFabricantes);
        if (fabricantesProblemas.contains(Build.MANUFACTURER.toLowerCase())) {
            AlertDialog.Builder brandBuilder = new AlertDialog.Builder(context);
            brandBuilder.setTitle(context.getString(R.string.tituloErrorMarca) + Build.MANUFACTURER.toLowerCase())
                    .setMessage(context.getString(R.string.mensajeSolucionMarca))
                    .setPositiveButton(context.getString(R.string.entiendo), new Dialog.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            actualizaListaBlanca(false, sharedPreferences);
                        }
                    })
                    .setNegativeButton(context.getString(R.string.volverRecordar), new Dialog.OnClickListener(){
                        @Override
                        public void onClick(DialogInterface dialog, int which) { }
                    })
                    .setCancelable(false);
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
    private static void actualizaListaBlanca(boolean listaBlanca,
                                             SharedPreferences sharedPreferences) {
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
    public static double calculaDistanciaDosPuntos(double lat1, double lon1,
                                                   double lat2, double lon2){
        //Aproximación del radio de la Tierra en el ecuador
        double radioTierra = 6378.137;
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
    public static JSONObject tareaMasCercana(Application app,
                                             double latitudUsuario,
                                             double longitudUsuario,
                                             String idUsuario){
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
                if(!tareaRegistrada(app, tareaEvaluada.getString(Auxiliar.id), idUsuario) &&
                        !PersistenciaDatos.existeTarea(
                                app,
                                PersistenciaDatos.ficheroNotificadas,
                                tareaEvaluada.getString(Auxiliar.id),
                                idUsuario)) {
                    if (!tarea.isEmpty()) {
                        distancia = calculaDistanciaDosPuntos(tareaEvaluada.getDouble(Auxiliar.latitud),
                                tareaEvaluada.getDouble(Auxiliar.longitud),
                                latitudUsuario,
                                longitudUsuario);
                        if (distancia < distanciaMin) {
                            distanciaMin = distancia;
                            tarea = new ArrayList<>();
                            tarea.add(tareaEvaluada);
                        } else {
                            if (distancia == distanciaMin) {
                                tarea.add(tareaEvaluada);
                            }
                        }
                    } else {//Solo se va entrar aquí con la primera tarea
                        tarea.add(tareaEvaluada);
                        distanciaMin = calculaDistanciaDosPuntos(tareaEvaluada.getDouble(Auxiliar.latitud),
                                tareaEvaluada.getDouble(Auxiliar.longitud),
                                latitudUsuario,
                                longitudUsuario);
                    }
                }
            }
            //Devolvemos una de las tareas del vector escogida de manera aleatorio
            //return tarea.get((int)(Math.random()*tarea.size()));
            //Devolvemos la primera tarea que será la recomenda por Recombee
            return tarea.get(0);
        }
        catch (Exception e){
            return null;
        }
    }

    /**
     * Método que se puede utilizar para comprobar si una tarea está registrada en algún fichero del sistema
     * @param app Aplicación
     * @param idTarea Identificador de la tarea
     * @return Verdadero si el usuario ya ha interactuado con la tarea
     */
    public static boolean tareaRegistrada(Application app, String idTarea, @NonNull String idUser){
        return PersistenciaDatos.existeTarea(app, PersistenciaDatos.ficheroTareasPospuestas, idTarea, idUser) ||
                PersistenciaDatos.existeTarea(app, PersistenciaDatos.ficheroTareasRechazadas, idTarea, idUser) ||
                PersistenciaDatos.existeTarea(app, PersistenciaDatos.ficheroCompletadas, idTarea, idUser) ||
                PersistenciaDatos.existeTarea(app, PersistenciaDatos.ficheroDenunciadas, idTarea, idUser);
    }

    /**
     * Método para obtener la fecha y hora actual con el formato HH:mm - dd/MM/yyyy
     * @return Cadena de texto con la hora y la fecha actual.
     */
    public static String horaFechaActual() {
        return formatoFecha.format(Calendar.getInstance().getTime());
    }

    /**
     * Filtro neceario para activar el canal de notificaciones interno de la app
     * @return IntentFilter con los posibles receptores de la notificación
     */
    public static IntentFilter intentFilter() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Auxiliar.nunca_mas);
        intentFilter.addAction(Auxiliar.ahora_no);
        return intentFilter;
    }

    /**
     * Devuelve el identificador único de la tarea dependiendo del tipo de respuestas esperado
     * @param tR Tipo de respuesta esperado
     * @return Identificador del icono en el sistema
     */
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
            case Auxiliar.tipoPreguntaImagenes:
                iconoTarea = R.drawable.ic_preguntaimagenesmultiples;
                break;
            default:
                iconoTarea = 0;
                break;
        }
        return iconoTarea;
    }

    /**
     * Método para obtener el icono que se muestra en la barra de de la descripción de la tarea y en
     * la lista de tareas que están dentro del marcador
     * @param tR Tipo de tarea
     * @return Identificador único del icono
     */
    public static int iconoTipoTareaLista(String tR) {
        int iconoTarea;
        switch (tR){
            case Auxiliar.tipoSinRespuesta:
                iconoTarea = R.drawable.ic_sinrespuesta_lista;
                break;
            case Auxiliar.tipoPreguntaCorta:
                iconoTarea = R.drawable.ic_preguntacorta_lista;
                break;
            case Auxiliar.tipoPreguntaLarga:
                iconoTarea = R.drawable.ic_preguntalarga_lista;
                break;
            case Auxiliar.tipoPreguntaImagen:
                iconoTarea = R.drawable.ic_preguntaimagen_lista;
                break;
            case Auxiliar.tipoImagen:
                iconoTarea = R.drawable.ic_imagen_lista;
                break;
            case Auxiliar.tipoImagenMultiple:
                iconoTarea = R.drawable.ic_imagenmultiple_lista;
                break;
            case Auxiliar.tipoVideo:
                iconoTarea = R.drawable.ic_video_lista;
                break;
            case Auxiliar.tipoPreguntaImagenes:
                iconoTarea = R.drawable.ic_preguntaimagenesmultiples_lista;
                break;
            default:
                iconoTarea = 0;
                break;
        }
        return iconoTarea;
    }

    /**
     * Método para traducir la posición de la barra deslizable en ajustes a texto entendible por el
     * usuario
     * @param resources Recursos
     * @param posicion Posición
     * @return Texto que se tiene que mostrar a los usuarios
     */
    public static String valorTexto(Resources resources, int posicion){
        switch (posicion){
            case 0:
                return String.format("5 %s", resources.getString(R.string.minutos));
            case 1:
                return String.format("15 %s", resources.getString(R.string.minutos));
            case 2:
                return String.format("30 %s", resources.getString(R.string.minutos));
            case 3:
                return String.format("1 %s", resources.getString(R.string.hora));
            case 4:
                return String.format("3 %s", resources.getString(R.string.horas));
            case 5:
                return String.format("4 %s", resources.getString(R.string.horas));
            case 6:
                return String.format("5 %s", resources.getString(R.string.horas));
            case 7:
                return String.format("6 %s", resources.getString(R.string.horas));
            case 8:
                return String.format("12 %s", resources.getString(R.string.horas));
            case 9:
                return String.format("1 %s", resources.getString(R.string.dia));
            case 10:
            case 11:
            case 12:
            case 13:
            case 14:
            case 15:
                return String.format("%d %s", posicion - 8, resources.getString(R.string.dias));
            default:
                return resources.getString(R.string.valorActual);
        }
    }

    /** Vector con los intervalos configurables para automatizar la notificación de tareas*/
    private static final int[] minutosAjustes =
            {5, 15, 30, 60, 180, 240, 300, 360, 720, 1440, 2280, 4320, 5790, 7200, 8640, 10080};

    /**
     * Método que devuelve la cantidad e minutos que se debe espera dependiendo de lo configurado
     * por el usuario
     * @param intervalo Posición que ocupa el intervalo en el vector
     * @return Mínimo de minutos que tiene que pasar entre notificaciones
     */
    public static int intervaloMinutos(int intervalo) {
        return minutosAjustes[intervalo];
    }

    /**
     * Método para centar el mapa entre el rectángulo que se crea con dos latitudos y dos longitudes
     * @param lat1 Latitud 1
     * @param long1 Longitud 1
     * @param lat2 Latitud 2
     * @param long2 Longitud 2
     * @return Objeto que respresenta el mapa. Valido para osmdroid
     */
    public static BoundingBox colocaMapa(Double lat1, Double long1, Double lat2, Double long2) {
        return  new BoundingBox(
                Math.min(lat1, lat2),
                Math.max(long1, long2),
                Math.max(lat1, lat2),
                Math.min(long1, long2));
    }

    /*public static void publicaGaleria(Context contexto, Uri uri) {
        Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        intent.setData(uri);
        contexto.sendBroadcast(intent);
    }*/

    /**
     * Envía el contenido a la app de Twitter para que el usuario pueda compartirlo
     * @param context Contexto
     * @param tarea JSON de la tarea
     * @param hashtag Etiquetas con la que se envía el tweet
     */
    public static void mandaTweet(Context context, JSONObject tarea, String hashtag){
        final int maxMulti = 4;
        try {
            //Compruebo que tiene instalado el cliente oficial de twitter antes de seguir
            context.getPackageManager().getPackageInfo("com.twitter.android",
                    PackageManager.GET_ACTIVITIES);

            Intent intent;
            List<String> listaURI = new ArrayList<>();
            String textoUsuario = null;
            JSONArray respuestas;
            try {
                respuestas = tarea.getJSONArray(Auxiliar.respuestas);
            }catch (Exception e){
                respuestas = new JSONArray();
            }
            JSONObject respuesta;
            for(int i = 0; i < respuestas.length(); i++){
                respuesta = respuestas.getJSONObject(i);
                if (respuesta.getString(Auxiliar.tipoRespuesta).equals(Auxiliar.texto)) {
                    if (!respuesta.getString(Auxiliar.respuestaRespuesta).equals("")) {
                        textoUsuario = respuesta.getString(Auxiliar.respuestaRespuesta);
                    }
                } else {//URI de video o fotos
                    listaURI.add(respuesta.getString(Auxiliar.respuestaRespuesta));
                }
            }

            ArrayList<Uri> uris = new ArrayList<>();
            /*uris.add(
                    uriTexto(
                            context,
                            context.getResources().getDrawable(R.drawable.ic_por_defecto_insta),
                            contenidoTexto(
                                    context,
                                    tarea,
                                    textoUsuario,
                                    hashtag,
                                    false)));*/

            switch (tarea.getString(Auxiliar.tipoRespuesta)){
                case Auxiliar.tipoPreguntaCorta:
                case Auxiliar.tipoPreguntaLarga:
                case Auxiliar.tipoSinRespuesta:
                    intent = new Intent(Intent.ACTION_SEND);
                    intent.putExtra(Intent.EXTRA_TEXT,
                            contenidoT(context, tarea, hashtag, true));
                    intent.setType("text/plain");
                    //intent.putExtra(Intent.EXTRA_STREAM, uris.get(0));
                    //intent.setType("image/*");
                    break;
                case Auxiliar.tipoPreguntaImagen:
                case Auxiliar.tipoImagen:
                case Auxiliar.tipoImagenMultiple:
                case Auxiliar.tipoPreguntaImagenes:
                case Auxiliar.tipoVideo:
                    //if(listaURI.size() > 0)
                        intent = new Intent(Intent.ACTION_SEND_MULTIPLE);
                    //else
                        //intent = new Intent(Intent.ACTION_SEND);
                    intent.putExtra(Intent.EXTRA_TEXT,
                            contenidoT(context, tarea, hashtag, true));
                    intent.setType("text/plain");
                    if(tarea.getString(Auxiliar.tipoRespuesta).equals(Auxiliar.tipoVideo)){
                        if(!listaURI.isEmpty()){
                            int i = 0;
                            for(String s : listaURI){
                                if(i < maxMulti) {
                                    uris.add(Uri.parse(s));
                                    ++i;
                                } else
                                    break;
                            }
                            intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
                            intent.setType("*/*");
                        }
                    }else{
                        if(!listaURI.isEmpty()){
                            int i = 0;
                            for(String s : listaURI){
                                if(i < maxMulti) {
                                    uris.add(Uri.parse(s));
                                    ++i;
                                } else
                                    break;
                            }
                            intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
                            intent.setType("image/*");
                        }
                    }
                    break;
                default:
                    intent = null;
                    break;
            }

            if(intent != null) {
                intent.setPackage("com.twitter.android");
                context.startActivity(intent);
            }
        } catch (PackageManager.NameNotFoundException e) {
            Toast.makeText(context,
                    context.getString(R.string.instalaTwitter),
                    Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Método para compartir la respuesta del usuario con Yammer. Solo permite compartir un único contenido
     * multimedia (es lo que permite la app de Yammer)
     * @param context Contexto
     * @param tarea Tarea completa. Contiene la respuesta del usuario
     * @param hashtag Etiquetas configurables por el usuario.
     */
    public static void mandaYammer(Context context, JSONObject tarea, String hashtag){
        try {
            context.getPackageManager().getPackageInfo(
                    "com.yammer.v1", PackageManager.GET_ACTIVITIES);

            Intent intent;
            List<String> listaURI = new ArrayList<>();
            String textoUsuario = null;
            JSONArray respuestas;
            try {
                respuestas = tarea.getJSONArray(Auxiliar.respuestas);
            } catch (Exception e) {
                respuestas = new JSONArray();
            }
            JSONObject respuesta;
            for (int i = 0; i < respuestas.length(); i++) {
                respuesta = respuestas.getJSONObject(i);
                if (respuesta.getString(Auxiliar.tipoRespuesta).equals(Auxiliar.texto)) {
                    if (!respuesta.getString(Auxiliar.respuestaRespuesta).equals("")) {
                        textoUsuario = respuesta.getString(Auxiliar.respuestaRespuesta);
                    }
                } else {//URI de video o fotos
                    listaURI.add(respuesta.getString(Auxiliar.respuestaRespuesta));
                }
            }
            switch (tarea.getString(Auxiliar.tipoRespuesta)) {
                case Auxiliar.tipoPreguntaCorta:
                case Auxiliar.tipoPreguntaLarga:
                case Auxiliar.tipoSinRespuesta:
                    intent = new Intent(Intent.ACTION_SEND);
                    intent.putExtra(Intent.EXTRA_TEXT,
                            contenidoTexto(context, tarea, textoUsuario, hashtag, false));
                    intent.setType("text/plain");
                    break;
                case Auxiliar.tipoPreguntaImagen:
                case Auxiliar.tipoImagen:
                case Auxiliar.tipoImagenMultiple:
                case Auxiliar.tipoPreguntaImagenes:
                    //if(listaURI.size() > 0)
                    //intent = new Intent(Intent.ACTION_SEND_MULTIPLE);
                    //else
                    intent = new Intent(Intent.ACTION_SEND);
                    intent.putExtra(Intent.EXTRA_TEXT,
                            contenidoTexto(context, tarea, textoUsuario, hashtag, false));
                    intent.setType("text/plain");
                    if (!listaURI.isEmpty()) {
                        //Solo me permite enviar una imagen la app de yammer
                        intent.putExtra(Intent.EXTRA_STREAM, Uri.parse(listaURI.get(0)));
                        intent.setType("image/*");
                    }
                    break;
                case Auxiliar.tipoVideo:
                    intent = new Intent(Intent.ACTION_SEND);
                    intent.putExtra(Intent.EXTRA_TEXT,
                            contenidoTexto(context, tarea, textoUsuario, hashtag, false));
                    intent.setType("text/plain");
                    if (!listaURI.isEmpty()) {
                        intent.putExtra(Intent.EXTRA_STREAM, Uri.parse(listaURI.get(0)));
                        intent.setType("video/*");
                    }
                    break;
                default:
                    intent = null;
                    break;
            }

            if (intent != null) {
                intent.setPackage("com.yammer.v1");
                context.startActivity(intent);
            }
        } catch (PackageManager.NameNotFoundException e) {
            Toast.makeText(context,
                    context.getString(R.string.instalaYammer),
                    Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Método para publicar una respuesta en la red social Instagram. Como no permite el envío de texto se
     * genera una imagen donde se escribe la respuesta del usuario. La tarea se publica como una historia.
     *
     * @param context Contexto
     * @param tarea Tarea completa con la respuesta del usuario
     * @param hashtag Lista de etiquetas que el usuario ha configurado en los ajustes de la aplicación.
     */
    public static void mandaInsta(Context context, JSONObject tarea, String hashtag){
        final int maxMulti = 10;
        try {
            context.getPackageManager().getPackageInfo(
                    "com.instagram.android", PackageManager.GET_ACTIVITIES);

            Intent intent = null;
            List<String> listaURI = new ArrayList<>();
            String textoUsuario = null;
            JSONArray respuestas;
            try {
                respuestas = tarea.getJSONArray(Auxiliar.respuestas);
            }catch (Exception e){
                respuestas = new JSONArray();
            }
            JSONObject respuesta;
            for(int i = 0; i < respuestas.length(); i++){
                respuesta = respuestas.getJSONObject(i);
                if (respuesta.getString(Auxiliar.tipoRespuesta).equals(Auxiliar.texto)) {
                    if (!respuesta.getString(Auxiliar.respuestaRespuesta).equals("")) {
                        textoUsuario = respuesta.getString(Auxiliar.respuestaRespuesta);
                    }
                } else {//URI de video o fotos
                    listaURI.add(respuesta.getString(Auxiliar.respuestaRespuesta));
                }
            }

            ArrayList<Uri> uris = new ArrayList<>();
            /*uris.add(
                    uriTexto(
                            context,
                            context.getResources().getDrawable(R.drawable.ic_por_defecto_insta),
                            contenidoTexto(
                                    context,
                                    tarea,
                                    textoUsuario,
                                    hashtag,
                                    false)));*/

            switch (tarea.getString(Auxiliar.tipoRespuesta)){
                case Auxiliar.tipoPreguntaCorta:
                case Auxiliar.tipoPreguntaLarga:
                case Auxiliar.tipoSinRespuesta:
                    /*intent = new Intent(Intent.ACTION_SEND);
                    intent.putExtra(Intent.EXTRA_STREAM, uris.get(0));
                    intent.setType("image/*");*/
                    Toast.makeText(context,
                            context.getString(R.string.errorInstagram),
                            Toast.LENGTH_LONG).show();
                    break;
                case Auxiliar.tipoPreguntaImagen:
                case Auxiliar.tipoImagen:
                case Auxiliar.tipoImagenMultiple:
                case Auxiliar.tipoPreguntaImagenes:
                case Auxiliar.tipoVideo:
                    if(listaURI.size() > 0)
                        intent = new Intent(Intent.ACTION_SEND_MULTIPLE);
                    else
                        intent = new Intent(Intent.ACTION_SEND);
                    if(tarea.getString(Auxiliar.tipoRespuesta).equals(Auxiliar.tipoVideo)){
                        if(!listaURI.isEmpty()){
                            int i = 0;
                            for(String s : listaURI){
                                if(i < maxMulti) {
                                    uris.add(Uri.parse(s));
                                    ++i;
                                } else
                                    break;
                            }
                            intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
                            intent.setType("*/*");
                        }
                    }else{
                        if(!listaURI.isEmpty()){
                            int i = 0;
                            for(String s : listaURI){
                                if(i < maxMulti) {
                                    uris.add(Uri.parse(s));
                                    ++i;
                                } else
                                    break;
                            }
                            intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
                            intent.setType("image/*");
                        }
                    }
                    break;
                default:
                    intent = null;
                    break;
            }

            if(intent != null) {
                intent.setPackage("com.instagram.android");
                context.startActivity(intent);
            }
        } catch (PackageManager.NameNotFoundException e) {
            Toast.makeText(context,
                    context.getString(R.string.instalaInsta),
                    Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Método con el que se crea una imagen con la respuesta que ha dado el usuario. Vacía el directorio
     * de ficheros intermedios antes de generar el nuevo.
     *
     * @param context Contexto
     * @param drawable Imagen genérica. Editable para que sea configurable con el color secundario
     * @param texto Texto del usuario.
     * @return Uri del fichero creado.
     */
    private static Uri uriTexto(Context context, Drawable drawable, String texto) {
        Bitmap bitmap = Bitmap.createBitmap(
                drawable.getIntrinsicWidth(),
                drawable.getIntrinsicHeight(),
                Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        TextPaint paint = new TextPaint();
        paint.setARGB(255, 255, 255, 255); //blanco
        paint.setStyle(Paint.Style.FILL);
        paint.setTextSize(((int)(22 * context.getResources().getDisplayMetrics().density)));
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setAntiAlias(true);
        StaticLayout staticLayout = new StaticLayout(
                texto,
                paint,
                (int)(bitmap.getWidth()/1.2),
                Layout.Alignment.ALIGN_NORMAL,
                1f,
                0f,
                false);

        canvas.save();
        canvas.translate(
                (bitmap.getWidth())/2f,
                ((bitmap.getHeight() - staticLayout.getHeight())/2f > 0)?
                        ((bitmap.getHeight() - staticLayout.getHeight())/2f):
                        0);
        staticLayout.draw(canvas);
        canvas.restore();

        try {
            File cache = context.getExternalFilesDir("cache");
            assert cache != null;
            if(cache.isDirectory()){
                File[] borrarFicheros = cache.listFiles();
                assert borrarFicheros != null;
                for(File borrar : borrarFicheros){
                    borrar.delete();
                }
            }

            File foto = Auxiliar.createFile(4, context);

            FileOutputStream fileOutputStream = null;
            try {
                fileOutputStream = new FileOutputStream(foto);
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            } finally {
                try {
                    if (fileOutputStream != null)
                        fileOutputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            return FileProvider.getUriForFile(context, context.getResources().getString(R.string.fileProvider), foto);
        }catch (IOException e){
            return null;
        }
    }

    /**
     * Método para crear el texto a compartir en Twitter
     * @param context Contexto
     * @param tarea Tarea completa. Contiene todos los datos necesarios.
     * @param hashtag Lista de etiquetas del usuario. Están separadas con comas
     * @param recorta Indica si debe recortar o no el mensaje.
     * @return Frase que se le pasará a la aplicación deseada
     */
    private static String contenidoT(Context context,
                                     JSONObject tarea,
                                     String hashtag,
                                     boolean recorta){
        String texto = null;

        String[] femenina = {"catedral", "necrópolis", "torre", "casona-torre", "concatedral", "casa-fuerte", "casa-torre", "carcel", "mansión", "cruz", "estación"};

        String[] femeninas = {"catedrales", "escuelas", "murallas", "casonas", "ruinas", "casas", "fachadas", "salinas", "facultad"};

        String[] masculinos = {"puentes", "reales", "restos"};

        String[] hashtags = hashtag.split(",");
        int tama = 0;
        for(int i = 0; i < hashtags.length; i++){
            hashtags[i] = String.format("#%s", hashtags[i].replace(" ", ""));
            tama += hashtags[i].length();
        }

        try{
            String[] lugar = tarea.getString(Auxiliar.titulo).split(" ");
            String primera = lugar[0].toLowerCase();
            if(lugar.length == 1 || primera.equals("el") || primera.equals("los") || primera.equals("ie")){
                texto = String.format("%s %s", context.getResources().getString(R.string.twitSinTexto), lugar[0]);
            }else{
                String caracter = lugar[0].substring(lugar[0].length() - 1);
                if(caracter.equals("o")){
                    texto = String.format("%s %s", context.getResources().getString(R.string.twitSinTextoM), tarea.getString(Auxiliar.titulo));
                }else{
                    if(caracter.equals("a")){
                        texto = String.format("%s %s", context.getResources().getString(R.string.twitSinTextoF), tarea.getString(Auxiliar.titulo));
                    }else{
                        texto = String.format("%s %s", context.getResources().getString(R.string.twitSinTextoM), tarea.getString(Auxiliar.titulo));
                        boolean encontrado = false;
                        for(String a : femeninas){
                            if(a.equals(primera)){
                                texto = String.format("%s %s", context.getResources().getString(R.string.twitSinTextoFs), tarea.getString(Auxiliar.titulo));
                                encontrado = true;
                                break;
                            }
                        }
                        if(!encontrado){
                            for(String a : femenina){
                                if(a.equals(primera)){
                                    texto = String.format("%s %s", context.getResources().getString(R.string.twitSinTextoF), tarea.getString(Auxiliar.titulo));
                                    encontrado = true;
                                    break;
                                }
                            }
                        }
                        if(!encontrado){
                            for(String a : masculinos){
                                if(a.equals(primera)){
                                    texto = String.format("%s %s", context.getResources().getString(R.string.twitSinTextoMs), tarea.getString(Auxiliar.titulo));
                                    break;
                                }
                            }
                        }
                    }
                }
            }
            if(recorta && texto.length() + hashtags.length + tama > 279){ //espacios + texto
                texto = texto.substring(0, 279 - (hashtags.length + tama + 4)) + "...";
            }

            for (String string : hashtags) {
                texto = String.format("%s %s", texto, string);
            }

            return texto;
        }catch (Exception e){
            return null;
        }
    }

    /**
     * Método para formar el contenido textual de la respuesta en la red social
     * @param context Contexto
     * @param tarea Tarea completa
     * @param textoUsuario Texto del usuario
     * @param hashtag Hashtag o lista de hashtags
     * @return Frase que se publicará en el tweet
     */
    private static String contenidoTexto(Context context,
                                         JSONObject tarea,
                                         String textoUsuario,
                                         String hashtag,
                                         boolean recorta){
        String texto = null;

        String[] femeninas = {"catedral", "calle"};

        String[] hashtags = hashtag.split(",");
        int tama = 0;
        for(int i = 0; i < hashtags.length; i++){
            hashtags[i] = String.format("#%s", hashtags[i].replace(" ", ""));
            tama += hashtags[i].length();
        }

        try {
            //texto = tarea.getString(Auxiliar.titulo);
            if(textoUsuario != null && !textoUsuario.equals("")){
                texto = String.format("%s\n%s", texto, textoUsuario);
                if(recorta && texto.length() + hashtags.length + tama > 279){ //espacios + texto
                    texto = texto.substring(0, 279 - (hashtags.length + tama + 4)) + "...";
                }
                if(!recorta){
                    texto = String.format("%s %s\n\n%s %s\n\n%s %s",
                            context.getString(R.string.yammerSinTexto),
                            tarea.getString(Auxiliar.titulo),
                            context.getString(R.string.tareaPregunta),
                            tarea.getString(Auxiliar.recursoAsociadoTexto),
                            context.getString(R.string.tareaRespuesta),
                            textoUsuario);
                }
            }else{
                if(recorta)
                    texto = String.format("%s %s", context.getString(R.string.twitSinTexto), texto);
                else
                    texto = String.format("%s %s\n\n%s %s",
                            context.getString(R.string.yammerSinTexto),
                            tarea.getString(Auxiliar.titulo),
                            context.getString(R.string.tareaPregunta),
                            tarea.getString(Auxiliar.recursoAsociadoTexto));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        if(recorta) {
            for (String string : hashtags) {
                texto = String.format("%s %s", texto, string);
            }
        }
        return texto;
    }

    /**
     * Método para almacenar los metadatos de la respuesta en el servidor
     * @param app Aplicación
     * @param appContext Contexto
     * @param idTarea Identificador único de la tarea
     * @param enviaWifi Preferencia del usuario que indica si solo quiere enviar por Wi-Fi
     */
    public static void guardaRespuesta(Application app,
                                       Context appContext,
                                       String idTarea,
                                       Boolean enviaWifi){

        int tipoConectividad = tipoConectividad(appContext);
        //Si no está conectado o está conectado pero solo lo quiere enviar por WiFi guardo el envío para después
        if (tipoConectividad == -1 || (tipoConectividad == 1 && enviaWifi)) {
            try {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put(Auxiliar.id, idTarea);
                PersistenciaDatos.guardaJSON(app,
                        PersistenciaDatos.ficheroSinEnviar,
                        jsonObject,
                        Context.MODE_PRIVATE);
            }catch (Exception e){
                e.printStackTrace();
            }
        }else {
            enviaResultados(app, appContext, idTarea);
        }
    }

    public static void enviaResultados(Application app, Context contexto, String idTarea){
        JSONObject jsonObject = new JSONObject();
        String idUsuario;
        try {
            idUsuario = PersistenciaDatos.recuperaTarea(app,
                    PersistenciaDatos.ficheroUsuario,
                    Auxiliar.id)
                    .getString(Auxiliar.uid);
        }catch (JSONException e) {
            idUsuario = null;
        }
        JSONObject tarea = PersistenciaDatos.recuperaTarea(
                app,
                PersistenciaDatos.ficheroCompletadas,
                idTarea,
                idUsuario);
        try {
            jsonObject.put("idTarea", idTarea);
            jsonObject.put("idUsuario", idUsuario);
            jsonObject.put("instanteInicio", tarea.getString(Auxiliar.fechaInicio));
            jsonObject.put("instanteFin", tarea.getString(Auxiliar.fechaFinalizacion));
            jsonObject.put("instanteModificacion",
                    tarea.getString(Auxiliar.fechaUltimaModificacion));
            jsonObject.put(Auxiliar.tipoRespuesta, tarea.getString(Auxiliar.tipoRespuesta));

            int numeroMedia = 0;
            if (tarea.has(Auxiliar.respuestas)) {
                JSONArray respuestas = tarea.getJSONArray(Auxiliar.respuestas);
                JSONObject respuesta;
                for (int i = 0; i < respuestas.length(); i++) {
                    respuesta = respuestas.getJSONObject(i);
                    if (respuesta.getString(Auxiliar.tipoRespuesta).equals(Auxiliar.texto)) {
                        jsonObject.put("respuestaTextual",
                                respuesta.getString(Auxiliar.respuestaRespuesta));
                    } else {
                        ++numeroMedia;
                    }
                }
                if (numeroMedia > 0)
                    jsonObject.put("numeroMedia", numeroMedia);
            }
            if (tarea.has(Auxiliar.rating))
                jsonObject.put("puntuacion", tarea.getDouble(Auxiliar.rating));
        } catch (Exception e) {
            jsonObject = null;
        }

        if (jsonObject != null) {
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST,
                    Auxiliar.direccionIP + "tareasCompletadas",
                    jsonObject,
                    null,
                    null
            );
            ColaConexiones.getInstance(contexto).getRequestQueue().add(jsonObjectRequest);
        }
    }

    /**
     * Comprueba que tipo de conectividad tiene el dispositivo
     * @param contexto Contexto
     * @return  0 Wi-FI
     *          1 Datos
     *          -1 Sin conectividad
     */
    public static int tipoConectividad(Context contexto){
        ConnectivityManager connectivityManager = (ConnectivityManager) contexto.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if(networkInfo == null || !networkInfo.isConnected())
            return -1;
        else{
            if(networkInfo.getType() == ConnectivityManager.TYPE_WIFI)
                return 0;
            else
                return 1;
        }
    }

    /**
     * Método para saber si la aplicación puede compartir algún dato a través de Internet
     * @param contexto Contexto
     * @param enviaWifi Preferencia del usuario
     * @return Verdadero si puede enviar, falso si no puede.
     */
    public static boolean puedoEnviar(Context contexto, Boolean enviaWifi){
        int conectividad = tipoConectividad(contexto);
        return conectividad != -1 && (conectividad != 1 || !enviaWifi);
    }



    /**
     * Método para recortar el identificador de la tarea y que aún así pueda ser reconstruido
     * @return Últimas dos partes del path
     */
    public static String idReducida(String idTarea){
        String[] vectorId = idTarea.split("/");
        StringBuilder salida = new StringBuilder();
        for(int i = vectorId.length; i > (vectorId.length - 2); i--)
            salida.append(vectorId[i - 1]).append("/");
        return salida.toString();
    }

    /**
     * Método que abre el navegador interno en un popup
     * @param contexto Contexto
     * @param url Url que se carga en el navegador
     */
    public static void navegadorInterno(final Context contexto, final String url){
        final Dialog dialogo = new Dialog(contexto);
        dialogo.setContentView(R.layout.popweb);
        dialogo.setCancelable(true);
        WebView webView = dialogo.findViewById(R.id.wbNavegador);
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webView.setWebViewClient(new ClienteWeb() {
            @Override
            public void navegadorExterno() {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                contexto.startActivity(intent);
            }
        });
        webView.loadUrl(url);
        dialogo.show();
    }

    /**
     * Método para buscar enlaces (formato HTML) en un string. Al pulsar uno de los enlaces se abre
     * el navegador interno.
     * @param contexto Contexto
     * @param texto Texto con el contenido a mostrar (se elimina el código html)
     * @param fuera Indica si se desea iniciar en el navegador interno (false) o el externo (true)
     * @return Objeto que se puede pasar a un TextView con los enlaces subrayados. NECESITA QUE EL
     * TEXTVIEW SE LE INDIQUE TEXTVIEW.setMovementMethod(LinkMovementMethod.getInstance());
     */
    public static SpannableStringBuilder creaEnlaces(final Context contexto,
                                                     String texto,
                                                     final boolean fuera){
        CharSequence charSequence = Html.fromHtml(texto);
        SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder(charSequence);
        URLSpan[] urlSpans = spannableStringBuilder.getSpans(0,
                spannableStringBuilder.length(),
                URLSpan.class);
        for(final URLSpan urlSpan : urlSpans){
            int start = spannableStringBuilder.getSpanStart(urlSpan);
            int end = spannableStringBuilder.getSpanEnd(urlSpan);
            int flags = spannableStringBuilder.getSpanFlags(urlSpan);
            ClickableSpan clickableSpan = new ClickableSpan() {
                @Override
                public void onClick(@NonNull View widget) {
                    if(fuera){
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.addCategory(Intent.CATEGORY_BROWSABLE);
                        intent.setData(Uri.parse(urlSpan.getURL()));
                        contexto.startActivity(intent);
                    }
                    else
                        Auxiliar.navegadorInterno(contexto, urlSpan.getURL());
                }
            };
            spannableStringBuilder.removeSpan(urlSpan);
            spannableStringBuilder.setSpan(clickableSpan, start, end, flags);
        }
        return spannableStringBuilder;
    }

    /**
     * Método para establecer el enlace al fichero que contiene la imagen para la licencia
     * @param context Contexto
     * @param ivInfoFotoPreview Identificador del botón de información
     * @param urlImagen URL de la imagen
     */
    public static String enlaceLicencia(final Context context,
                                      ImageView ivInfoFotoPreview,
                                      String urlImagen) {
        if(urlImagen != null && urlImagen.contains("wikimedia")){
            /*final Uri url = Uri.parse(urlImagen.replace("Special:FilePath/", "File:")
                    .replace("?width=300", "").concat(context.getString(R.string.ultimaParteLicencia)));*/
            ivInfoFotoPreview.setVisibility(View.VISIBLE);
            return urlImagen.replace("Special:FilePath/", "File:")
                    .replace("?width=300", "").concat(context.getString(R.string.ultimaParteLicencia));
            /*ivInfoFotoPreview.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    /*Intent intent = new Intent(Intent.ACTION_VIEW, url);
                    intent.addCategory(Intent.CATEGORY_BROWSABLE);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(intent);
                    Auxiliar.navegadorInterno(context, url);
                }
            });*/
        }else{
            return null;
        }
    }

    /**
     * Método para recuperar el texto asociado al tipo de tarea
     * @param contexto Contexto
     * @param tipoTarea Última parte de la información que envía desde el punto SPARQL
     * @return Frase explicativa del tipo de tarea
     */
    public static String textoTipoTarea(Context contexto, String tipoTarea) {
        switch (tipoTarea){
            case Auxiliar.tipoSinRespuesta:
                return contexto.getString(R.string.sinRespuesta);
            case Auxiliar.tipoPreguntaCorta:
                return contexto.getString(R.string.preguntaCorta);
            case Auxiliar.tipoPreguntaLarga:
                return contexto.getString(R.string.preguntaLarga);
            case Auxiliar.tipoPreguntaImagen:
                return contexto.getString(R.string.preguntaImagen);
            case Auxiliar.tipoImagen:
                return contexto.getString(R.string.imagen);
            case Auxiliar.tipoImagenMultiple:
                return contexto.getString(R.string.imagenMultiple);
            case Auxiliar.tipoVideo:
                return contexto.getString(R.string.video);
            case Auxiliar.tipoPreguntaImagenes:
                return contexto.getString(R.string.preguntaImagenes);
            default:
                return null;
        }
    }

    /**
     * Método para obtener una lista de municipios dependiente de los caracteres que haya introducido
     * el usuario.
     * @param context Contexto
     * @param query Caracteres introducidos por el usuario
     * @return Lista de municipios que coinciden con la búsqueda. El array puede estar vacío.
     */
    public static JSONArray buscaMunicipio(Context context, String query) {
        JSONArray salida = new JSONArray();
        String[] bdV, userV;
        List<Integer> municipiosValidos = new ArrayList<>();
        int coincidenciaPalabra, coincidencias = 0;
        if(Auxiliar.municipios == null || Auxiliar.municipios.length() == 0)
            Auxiliar.municipios = leeFicheroMunicipios(context);

        JSONObject municipio;
        try {
            userV = query.split(" ");
            int tama = Auxiliar.municipios.length();
            for(int i = 0; i < tama; i++){
                municipio = Auxiliar.municipios.getJSONObject(i);
                bdV = municipio.getString("n").split(" ");
                coincidenciaPalabra = 0;
                for (String s : userV) {
                    for (String bd : bdV) {
                        if (bd.contains(s)) {
                            coincidenciaPalabra++;
                            if (coincidenciaPalabra > coincidencias) {
                                coincidencias = coincidenciaPalabra;
                                municipiosValidos = new ArrayList<>();
                            }
                            if (coincidenciaPalabra == coincidencias)
                                municipiosValidos.add(i);
                            break;
                        }
                    }
                }
            }
            for(int i : municipiosValidos){
                salida.put(Auxiliar.municipios.get(i));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return salida;
    }

    /**
     * Método para cargar la lista de municipios en memoria.
     * @param context Contexto
     * @return Municipios de Castilla y León
     */
    public static JSONArray leeFicheroMunicipios(Context context) {
        JSONArray array;
        BufferedReader bufferedReader = null;
        try {
            InputStreamReader inputStreamReader = new InputStreamReader(context.getResources().openRawResource(R.raw.municipios_castilla_y_leon));
            bufferedReader = new BufferedReader(inputStreamReader);
            StringBuffer stringBuffer = new StringBuffer();
            String inter;
            while ((inter = bufferedReader.readLine()) != null) {
                stringBuffer.append(inter);
            }
            array = new JSONArray(stringBuffer.toString());
        } catch (IOException | JSONException e){
            array = null;
        }finally {
            if(bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return array;
    }
}
