package es.uva.gsic.adolfinstro.auxiliar;

import android.Manifest;
import android.app.Activity;
import android.app.Application;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.text.style.ClickableSpan;
import android.text.style.URLSpan;
import android.view.View;
import android.view.Window;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.preference.PreferenceManager;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.osmdroid.util.BoundingBox;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import es.uva.gsic.adolfinstro.Ajustes;
import es.uva.gsic.adolfinstro.CompruebaEnvios;
import es.uva.gsic.adolfinstro.Login;
import es.uva.gsic.adolfinstro.Maps;
import es.uva.gsic.adolfinstro.R;
import es.uva.gsic.adolfinstro.Puntuacion;
import es.uva.gsic.adolfinstro.persistencia.PersistenciaDatos;

/**
 * Clase auxiliar de la aplicación. Contiene los strings utilizados como identificadores de toda la
 * aplicación. Los métodos son utilizados en otras clases.
 *
 * @author Pablo
 * @version 20210407
 */
public class Auxiliar {

    //TODO comprueba que sea la dirección correcta
    //public static final String direccionIP = "https://casuallearnapp.gsic.uva.es/app/";
    //public static final String direccionIP = "http://10.0.104.17:10001/app/";
    public static final String direccionIP = "http://192.168.1.222:10001/app/";

    private static final String rutaTareasCompletadas = direccionIP + "tareasCompletadas";
    public static final String rutaTareas = direccionIP + "tareas";
    public static final String rutaContextos = direccionIP + "contextos";
    public static final String rutaPortafolio = direccionIP + "portafolio/";
    public static final String rutaCanales = direccionIP + "channels";

    public static final String id = "id";
    public static final String tipoRespuesta = "tipoRespuesta";
    public static final String latitud = "latitud";
    public static final String longitud = "longitud";
    public static final String recursoImagenBaja = "thumbnail";
    public static final String recursoImagen = "imagen";
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
    public static final String contexto = "contexto";
    public static final String nTareas = "nTareas";
    public static final String enlaceWiki = "enlaceWiki";
    public static final String textoLicencia = "textoLicencia";
    public static final String busquedasMunicipio = "busquedaMunicipio";
    public static final String creadoPor = "creadoPor";
    public static final String imagen = "imagen";

    public static final String canal = "channel";
    public static final String tipo = "type";

    public static final String creadorInvestigadores = "https://casuallearn.gsic.uva.es/researchers";
    public static final String r1 = "charo1";
    public static final String r2 = "charo2";
    public static final String r3 = "charo3";
    public static final String r4 = "charo4";

    public static final String posUsuarioLat = "posUsuarioLat";
    public static final String posUsuarioLon = "posUsuarioLon";

    public static final String nunca_mas = "NUNCA_MAS";
    public static final String ahora_no = "AHORA_NO";
    public static final String ahora_no_contexto = "AHORA_NO_CONTEXTO";

    /** Identificador del canal de tareas */
    public static final String channelId = "notiTareas";
    public static final String cargaImagenPreview = "imagenPreview";
    public static final String cargaImagenTarea = "imagenTarea";
    public static final String cargaImagenDetalles = "imagenDetalles";

    public static final String tipoSinRespuesta = "sinRespuesta";
    public static final String tipoImagen = "fotografia";
    public static final String tipoImagenMultiple = "multiplesFotografias";
    public static final String tipoVideo = "video";
    public static final String tipoPreguntaCorta = "textoCorto";
    public static final String tipoPreguntaLarga = "texto";
    public static final String tipoPreguntaImagen = "fotografiaYTexto";
    public static final String tipoPreguntaImagenes = "multiplesFotografiasYTexto";
    public static final String tipoPreguntaVideo = "videoYTexto";

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
    public static final String idPortafolio = "idPorta";
    public static final String idToken = "idToken";
    public static final String publico = "publico";
    public static final String retardado = "retardado";

    public static final String norte = "norte";
    public static final String sur = "sur";
    public static final String este = "este";
    public static final String oeste = "oeste";
    public static final String contextos = "contextos";

    public static final String caducidad = "caducidad";
    public static final String label = "label";
    public static final String comment = "comment";
    public static final String ficheroZona = "ficheroZona";
    public static final String configuracionActual = "configuracionActual";
    public static final String marcado = "marcado";
    public static final String marcador = "marcador";
    public static final String caracteristica = "caracteristica";
    public static final String detallesCreador = "detailsCreator";

    private static SimpleDateFormat formatoFecha = new SimpleDateFormat("HH:mm:ss - dd/MM/yyyy");

    public static int incr = 0;

    public static JSONArray municipios = null;
    private static final String idTarea = "idTarea";
    private static final String instanteInicio = "instanteInicio";
    private static final String instanteFin = "instanteFin";
    private static final String instanteModificacion = "instanteModificacion";
    private static final String respuestaTextual = "respuestaTextual";
    private static final String numeroMedia = "numeroMedia";
    private static final String puntuacion = "puntuacion";
    public static final String twitter = "twitter";
    public static final String teams = "teams";
    public static final String yammer = "yammer";
    public static final String instagram = "instagram";

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
                /*if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
                    ContentResolver contentResolver = context.getContentResolver();
                    ContentValues contentValues = new ContentValues();
                    contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, timeStamp + ".jpg");
                    contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg");
                    contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES);
                    Uri uri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
                    mediaFile = new File(uri.getPath() + "/"+timeStamp+".jpg");
                }else{
                    String imagesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString();
                    mediaFile = new File(imagesDir, timeStamp + ".jpg");
                }*/
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
     * Método para obtener el contexto más cercana al usuario. Si hay varios contextos a la misma
     * distancia se obtiene uno de ellos aleatoriamente. Solo se envía un contexto en el que no se haya
     * realizado ninguna tarea.
     *
     * @param app Aplicación
     * @param latitudUsuario Latitud de la posción del usuario
     * @param longitudUsuario Longitud de la posición del usuario
     * @return JSONObject con la información del contexto o null
     */
    public static JSONObject contextoMasCercano(
            Application app,
            double latitudUsuario,
            double longitudUsuario,
            String idUsuario){
        JSONObject lugar, auxiliar;
        boolean lugarConTareaCompletada, distanciaNotificacion;
        double distanciaMin = -1, distancia;
        String contextoLugar;
        JSONArray aMismaDistancia = null;
        try{
            JSONArray lugaresUsuario = PersistenciaDatos.leeFichero(
                    app,
                    PersistenciaDatos.ficheroContextos);
            JSONArray lugaresNotificados = PersistenciaDatos.leeTareasUsuario(
                    app,
                    PersistenciaDatos.ficheroContextosNotificados,
                    idUsuario);
            JSONArray tareasCompletadas = PersistenciaDatos.leeTareasUsuario(
                    app,
                    PersistenciaDatos.ficheroCompletadas,
                    idUsuario);

            for(int i = 0; i < lugaresUsuario.length(); i++){
                lugar = lugaresUsuario.getJSONObject(i);
                contextoLugar = lugar.getString(Auxiliar.contexto);
                lugarConTareaCompletada = false;
                distanciaNotificacion = true;//Por defecto hago el resto de comprobaciones
                for(int j = 0; j < lugaresNotificados.length(); j++){
                    auxiliar = lugaresNotificados.getJSONObject(j);
                    /*Si el contexto es igual que el de auxiliar se comprueba si tiene el atributo
                    * de los instantes (nueva actualización para que no existan problemas con la
                    * versión en producción). Si tiene el valor del instante de notificación se
                    * comprueba si el momento actual es superior al de la notifiación más el incremento
                    * que se le haya dado cuando se notificó.*/
                    if(contextoLugar.equals(auxiliar.getString(Auxiliar.contexto))){
                        if(auxiliar.has(Auxiliar.instante))
                            distanciaNotificacion = System.currentTimeMillis() > auxiliar.getLong(Auxiliar.instante);
                        break;
                    }
                }
                if(distanciaNotificacion) {
                    if (lugar.has(Auxiliar.instante) && (System.currentTimeMillis() <= lugar.getLong(Auxiliar.instante)))
                        break;
                    for (int j = 0; j < tareasCompletadas.length(); j++) {
                        auxiliar = tareasCompletadas.getJSONObject(j);
                        if (contextoLugar.equals(auxiliar.getString(Auxiliar.contexto))) {
                            lugarConTareaCompletada = true;
                            break;
                        }
                    }
                    if (!lugarConTareaCompletada) {
                        distancia = calculaDistanciaDosPuntos(
                                latitudUsuario, longitudUsuario,
                                lugar.getDouble(Auxiliar.latitud), lugar.getDouble(Auxiliar.longitud)
                        );
                        if (distanciaMin < 0 || distancia < distanciaMin) {
                            distanciaMin = distancia;
                            aMismaDistancia = new JSONArray();//Es más rápido que borrar
                        }
                        if (distanciaMin == distancia) {
                            aMismaDistancia.put(lugar);
                        }
                    }
                }
            }
            if(aMismaDistancia != null) {
                return aMismaDistancia.getJSONObject((int) (Math.random() * aMismaDistancia.length()));
            }
            else  return null;
        } catch (Exception e){
            e.printStackTrace();
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
        return PersistenciaDatos.existeTarea(
                app,
                PersistenciaDatos.ficheroTareasPospuestas,
                idTarea,
                idUser
        ) || PersistenciaDatos.existeTarea(
                app,
                PersistenciaDatos.ficheroTareasRechazadas,
                idTarea,
                idUser
        ) || PersistenciaDatos.existeTarea(
                app,
                PersistenciaDatos.ficheroCompletadas,
                idTarea,
                idUser
        ) || PersistenciaDatos.existeTarea(
                app,
                PersistenciaDatos.ficheroDenunciadas,
                idTarea,
                idUser
        );
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
            case Auxiliar.tipoPreguntaVideo:
                iconoTarea =  R.drawable.ic_preguntavideo;
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
            case Auxiliar.tipoPreguntaVideo:
                iconoTarea =  R.drawable.ic_preguntavideo_lista;
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
            case 5:
            case 6:
            case 7:
                return String.format("%d %s", posicion - 1, resources.getString(R.string.horas));
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
     * Método para centar el mapa entre el rectángulo que se crea con dos latitudos y dos longitudes.
     * Devuelve al mapa un área que tiene que representar.
     *
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

    /**
     * Método para configurar los distintos intents de las redes soportadas
     *
     * @param red Identificador de la red social
     * @param context Contexto
     * @param tarea JSONObject con la respuesta del usuario
     * @param hashtag Etiqueta que podrá ir en la respuesta de la red social
     */
    private static void mandaRed(String red, Context context, JSONObject tarea, String hashtag){
        int maxMulti;
        String paquete;
        switch (red){
            case Auxiliar.twitter:
                maxMulti = 4;
                paquete = "com.twitter.android";
                break;
            case Auxiliar.instagram:
                maxMulti = 10;
                paquete = "com.instagram.android";
                break;
            case Auxiliar.teams:
                maxMulti = 10;
                paquete = "com.microsoft.teams";
                break;
            case Auxiliar.yammer:
                maxMulti = 1;
                paquete = "com.yammer.v1";
                break;
            default:
                maxMulti = 0;
                paquete = null;
                break;
        }
        if(paquete != null){
            try{
                context.getPackageManager().getPackageInfo(paquete, PackageManager.GET_ACTIVITIES);

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
                        if (!Auxiliar.stringVacio(respuesta.getString(Auxiliar.respuestaRespuesta))) {
                            textoUsuario = respuesta.getString(Auxiliar.respuestaRespuesta);
                        }
                    } else {//URI de video o fotos
                        listaURI.add(respuesta.getString(Auxiliar.respuestaRespuesta));
                    }
                }

                switch (tarea.getString(Auxiliar.tipoRespuesta)){
                    case Auxiliar.tipoPreguntaCorta:
                    case Auxiliar.tipoPreguntaLarga:
                    case Auxiliar.tipoSinRespuesta:
                        switch (red){
                            case Auxiliar.twitter:
                                intent = new Intent(Intent.ACTION_SEND);
                                intent.putExtra(Intent.EXTRA_TEXT,
                                        contenidoT(context, tarea, hashtag));
                                break;
                            case Auxiliar.instagram:
                                Toast.makeText(context,
                                        context.getString(R.string.errorInstagram),
                                        Toast.LENGTH_LONG).show();
                                break;
                            default:
                                intent = new Intent(Intent.ACTION_SEND);
                                intent.putExtra(Intent.EXTRA_TEXT,
                                        contenidoTexto(context, tarea, textoUsuario, hashtag));
                                break;
                        }
                        if(intent != null)
                            intent.setType("text/plain");
                        break;
                    case Auxiliar.tipoPreguntaImagen:
                    case Auxiliar.tipoImagen:
                    case Auxiliar.tipoImagenMultiple:
                    case Auxiliar.tipoPreguntaImagenes:
                    case Auxiliar.tipoVideo:
                    case Auxiliar.tipoPreguntaVideo:
                        ArrayList<Uri> uris = new ArrayList<>();
                        if(red.equals(Auxiliar.yammer))
                            intent = new Intent(Intent.ACTION_SEND);
                        else
                        if(listaURI.size() > 0)
                            intent = new Intent(Intent.ACTION_SEND_MULTIPLE);
                        else
                            intent = new Intent(Intent.ACTION_SEND);

                        switch (red){
                            case Auxiliar.twitter:
                                intent.putExtra(Intent.EXTRA_TEXT,
                                        Auxiliar.contenidoT(context, tarea, hashtag));
                                break;
                            case Auxiliar.yammer:
                            case Auxiliar.teams:
                                intent.putExtra(Intent.EXTRA_TEXT,
                                        Auxiliar.contenidoTexto(context, tarea, textoUsuario, hashtag));
                                break;
                            default:
                                break;
                        }

                        if(!listaURI.isEmpty()){
                            if(red.equals(Auxiliar.yammer)){
                                intent.putExtra(Intent.EXTRA_STREAM, Uri.parse(listaURI.get(0)));
                            }else {
                                int i = 0;
                                for (String s : listaURI) {
                                    if (i < maxMulti) {
                                        uris.add(Uri.parse(s));
                                        ++i;
                                    } else
                                        break;
                                }
                                intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
                            }
                            if(tarea.getString(Auxiliar.tipoRespuesta).equals(Auxiliar.tipoVideo)
                                    || tarea.getString(Auxiliar.tipoRespuesta).equals(Auxiliar.tipoPreguntaVideo))
                                if(red.equals(Auxiliar.yammer))
                                    intent.setType("video/*");
                                else
                                    intent.setType("*/*");
                            else
                            if(red.equals(Auxiliar.teams))
                                intent.setType("image/jpeg");
                            else
                                intent.setType("image/*");
                        }
                        break;
                    default:
                        intent = null;
                        break;
                }

                if(intent != null) {
                    intent.setPackage(paquete);
                    context.startActivity(intent);
                }
            } catch (PackageManager.NameNotFoundException e) {
                switch (red){
                    case Auxiliar.twitter:
                        Toast.makeText(context,
                                context.getString(R.string.instalaTwitter),
                                Toast.LENGTH_SHORT).show();
                        break;
                    case Auxiliar.instagram:
                        Toast.makeText(context,
                                context.getString(R.string.instalaInsta),
                                Toast.LENGTH_SHORT).show();
                        break;
                    case Auxiliar.teams:
                        Toast.makeText(context,
                                context.getString(R.string.instalaTeams),
                                Toast.LENGTH_SHORT).show();
                        break;
                    case Auxiliar.yammer:
                        Toast.makeText(context,
                                context.getString(R.string.instalaYammer),
                                Toast.LENGTH_SHORT).show();
                        break;
                    default:
                        break;
                }
            } catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    /**
     * Envía el contenido a la app de Twitter para que el usuario pueda compartirlo
     * @param context Contexto
     * @param tarea JSON de la tarea
     * @param hashtag Etiquetas con la que se envía el tweet
     */
    public static void mandaTweet(Context context, JSONObject tarea, String hashtag){
        Auxiliar.mandaRed(Auxiliar.twitter, context, tarea, hashtag);
    }

    /**
     * Método para compartir la respuesta del usuario con Yammer. Solo permite compartir un único contenido
     * multimedia (es lo que permite la app de Yammer)
     * @param context Contexto
     * @param tarea Tarea completa. Contiene la respuesta del usuario
     * @param hashtag Etiquetas configurables por el usuario.
     */
    public static void mandaYammer(Context context, JSONObject tarea, String hashtag){
        Auxiliar.mandaRed(Auxiliar.yammer, context, tarea, hashtag);
    }

    /**
     * Método para compartir la respuesta del usuario con Microsoft Teams.
     * @param context Contexto
     * @param tarea JSONObject con las respuestas del usuario
     * @param hashtag Eitqueta que puede acompañar al texto
     */
    public static void mandaTeams(Context context, JSONObject tarea, String hashtag){
        Auxiliar.mandaRed(Auxiliar.teams, context, tarea, hashtag);
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
        Auxiliar.mandaRed(Auxiliar.instagram, context, tarea, hashtag);
    }

    /**
     * Método para crear el texto a compartir en Twitter
     * @param context Contexto
     * @param tarea Tarea completa. Contiene todos los datos necesarios.
     * @param hashtag Lista de etiquetas del usuario. Están separadas con comas
     * @return Frase que se le pasará a la aplicación deseada
     */
    private static String contenidoT(
            Context context,
            JSONObject tarea,
            String hashtag){
        String texto;

        String[] hashtags = hashtag.split(",");
        int tama = 0;
        for(int i = 0; i < hashtags.length; i++){
            hashtags[i] = String.format("#%s", hashtags[i].replace(" ", ""));
            tama += hashtags[i].length();
        }

        try{
            texto = String.format("%s %s %s",
                    context.getResources().getString(R.string.twitSinTexto),
                    Auxiliar.articuloDeterminado(context, tarea.getString(Auxiliar.titulo)),
                    tarea.getString(Auxiliar.titulo));
            String link = "";
            if(tarea.has(Auxiliar.idToken) && tarea.has(Auxiliar.publico)) {
                if (tarea.getBoolean(Auxiliar.publico)) {
                    String idUsuario = PersistenciaDatos.recuperaTarea(
                            (Application) context.getApplicationContext(),
                            PersistenciaDatos.ficheroUsuario,
                            Auxiliar.id)
                            .getString(Auxiliar.idPortafolio);
                    if (!Auxiliar.stringVacio(idUsuario))
                        link = Auxiliar.rutaPortafolio + idUsuario + "/" + tarea.getString(Auxiliar.idToken);
                }
            }
            if(!Auxiliar.stringVacio(link))
                tama += 23;
            if(texto.length() + hashtags.length + tama > 279){ //espacios + texto
                texto = texto.substring(0, 279 - (hashtags.length + tama + 5)) + "...";
            }

            for (String string : hashtags) {
                texto = String.format("%s %s", texto, string);
            }

            texto = String.format("%s %s", texto, link);

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
                                         String hashtag){
        String texto = null;

        String[] hashtags = hashtag.split(",");
        int tama = 0;
        for(int i = 0; i < hashtags.length; i++){
            hashtags[i] = String.format("#%s", hashtags[i].replace(" ", ""));
            tama += hashtags[i].length();
        }

        try {
            //texto = tarea.getString(Auxiliar.titulo);
            texto = "";
            if(textoUsuario != null && !Auxiliar.stringVacio(texto)){
                texto = String.format("%s\n%s", texto, textoUsuario);

                texto = String.format("%s %s\n\n%s %s\n\n%s %s",
                        context.getString(R.string.yammerSinTexto),
                        tarea.getString(Auxiliar.titulo),
                        context.getString(R.string.tareaPregunta),
                        Auxiliar.quitaEnlaces(tarea.getString(Auxiliar.recursoAsociadoTexto)),
                        context.getString(R.string.tareaRespuesta),
                        textoUsuario);

            }else{
                texto = String.format("%s %s\n\n%s %s",
                        context.getString(R.string.yammerSinTexto),
                        tarea.getString(Auxiliar.titulo),
                        context.getString(R.string.tareaPregunta),
                        tarea.getString(Auxiliar.recursoAsociadoTexto));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return texto;
    }

    /**
     * Método para concatenar el artículo determinado a una palabra.
     * @param nombreLugar Palabra a la que agregar el artículo
     * @return Palabra con artículo
     */
    public static String articuloDeterminado(Context context, String nombreLugar){
        String[] femenina = {"catedral", "necrópolis", "torre", "casona-torre", "concatedral",
                "casa-fuerte", "casa-torre", "carcel", "mansión", "cruz", "estación"};

        String[] femeninas = {"catedrales", "escuelas", "murallas", "casonas", "ruinas", "casas",
                "fachadas", "salinas", "facultad"};

        String[] masculinos = {"puentes", "reales", "restos"};

        try{
            String[] lugar = nombreLugar.split(" ");
            String primera = lugar[0].toLowerCase();
            if(lugar.length == 1 ||
                    primera.equals("el") || primera.equals("los") ||
                    primera.equals("las") || primera.equals("la"))
                return "";
            else {
                String caracter = lugar[0].substring(lugar[0].length() - 1);
                if (caracter.equals("o"))
                    return context.getString(R.string.el);
                else {
                    if (caracter.equals("a"))
                        return context.getString(R.string.la);
                    else {
                        String salida =  context.getString(R.string.el);
                        boolean encontrado = false;
                        for (String a : femeninas)
                            if (a.equals(primera)) {
                                salida = context.getString(R.string.las);
                                encontrado = true;
                                break;
                            }
                        if(!encontrado)
                            for (String a : femenina)
                                if (a.equals(primera)) {
                                    salida = context.getString(R.string.la);
                                    encontrado = true;
                                    break;
                                }
                        if(!encontrado)
                            for (String a : masculinos)
                                if (a.equals(primera)) {
                                    salida = context.getString(R.string.los);
                                    break;
                                }
                        return salida;
                    }
                }
            }
        }catch (Exception e){
            return nombreLugar;
        }
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
        //Si no está conectado o está conectado pero solo lo quiere enviar por WiFi guardo el
        // envío para después
        if (tipoConectividad == -1 || (tipoConectividad == 1 && enviaWifi)) {
            try {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put(Auxiliar.id, idTarea);
                PersistenciaDatos.guardaJSON(app,
                        PersistenciaDatos.ficheroSinEnviar,
                        jsonObject,
                        Context.MODE_PRIVATE);
                new CompruebaEnvios().activaCompruebaEnvios(appContext);
            }catch (Exception e){
                e.printStackTrace();
            }
        }else {
            enviaResultados(app, appContext, idTarea);
        }
    }

    /**
     * Método para enviar las respuestas de los usuarios al servidor
     * @param app Aplicación
     * @param contexto Contexto
     * @param idTarea Identificador de la tarea
     */
    public static void enviaResultados(final Application app, Context contexto, String idTarea){
        JSONObject jsonObject = new JSONObject();
        String idUsuario;
        try {
            idUsuario = PersistenciaDatos.recuperaTarea(
                    app,
                    PersistenciaDatos.ficheroUsuario,
                    Auxiliar.id
            ).getString(Auxiliar.uid);
        }catch (JSONException e) {
            idUsuario = null;
        }
        final JSONObject tarea = PersistenciaDatos.recuperaTarea(
                app,
                PersistenciaDatos.ficheroCompletadas,
                idTarea,
                idUsuario);
        try {
            jsonObject.put(Auxiliar.idTarea, idTarea);
            jsonObject.put(Auxiliar.idUsuario, idUsuario);
            jsonObject.put(Auxiliar.instanteInicio, tarea.getString(Auxiliar.fechaInicio));
            if(tarea.has(Auxiliar.fechaFinalizacion))
                jsonObject.put(Auxiliar.instanteFin, tarea.getString(Auxiliar.fechaFinalizacion));
            else
            if(tarea.has(Auxiliar.fechaUltimaModificacion))
                jsonObject.put(Auxiliar.instanteFin, tarea.getString(Auxiliar.fechaUltimaModificacion));
            else
                jsonObject.put(Auxiliar.instanteFin, Auxiliar.horaFechaActual());
            jsonObject.put(Auxiliar.instanteModificacion,
                    tarea.getString(Auxiliar.fechaUltimaModificacion));
            jsonObject.put(Auxiliar.tipoRespuesta, tarea.getString(Auxiliar.tipoRespuesta));
            jsonObject.put(Auxiliar.publico, tarea.getBoolean(Auxiliar.publico));

            int numeroMedia = 0;
            if (tarea.has(Auxiliar.respuestas)) {
                JSONArray respuestas = tarea.getJSONArray(Auxiliar.respuestas);
                JSONObject respuesta;
                for (int i = 0; i < respuestas.length(); i++) {
                    respuesta = respuestas.getJSONObject(i);
                    if (respuesta.getString(Auxiliar.tipoRespuesta).equals(Auxiliar.texto)) {
                        jsonObject.put(Auxiliar.respuestaTextual,
                                respuesta.getString(Auxiliar.respuestaRespuesta));
                    } else {
                        ++numeroMedia;
                    }
                }
                if (numeroMedia > 0)
                    jsonObject.put(Auxiliar.numeroMedia, numeroMedia);
            }
            if (tarea.has(Auxiliar.rating))
                jsonObject.put(Auxiliar.puntuacion, tarea.getDouble(Auxiliar.rating));
        } catch (Exception e) {
            jsonObject = null;
        }

        try {
            if (jsonObject != null) {
                final String finalIdUsuario = idUsuario;
                JsonObjectRequest jsonObjectRequest;
                if (tarea.has(Auxiliar.idToken)) {//Actualización
                    jsonObjectRequest = new JsonObjectRequest(Request.Method.PUT,
                            Auxiliar.rutaTareasCompletadas + "/" + tarea.getString(Auxiliar.idToken),
                            jsonObject,
                            null,
                            null);
                } else {//Creación
                    jsonObjectRequest = new JsonObjectRequest(Request.Method.POST,
                            Auxiliar.rutaTareasCompletadas,
                            jsonObject,
                            new Response.Listener<JSONObject>() {
                                @Override
                                public void onResponse(JSONObject response) {
                                    if (response.has(Auxiliar.idToken)) {
                                        try {
                                            tarea.put(Auxiliar.idToken, response.getString(Auxiliar.idToken));
                                            PersistenciaDatos.reemplazaJSON(
                                                    app,
                                                    PersistenciaDatos.ficheroCompletadas,
                                                    tarea,
                                                    finalIdUsuario);
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }
                            },
                            null
                    );
                }
                ColaConexiones.getInstance(contexto).getRequestQueue().add(jsonObjectRequest);
            }
        }catch (JSONException e){
            e.printStackTrace();
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
        ConnectivityManager connectivityManager = (ConnectivityManager) contexto.
                getSystemService(Context.CONNECTIVITY_SERVICE);
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
        dialogo.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialogo.setContentView(R.layout.popweb);
        dialogo.setCancelable(true);
        WebView webView = dialogo.findViewById(R.id.wbNavegador);
        //WebSettings webSettings = webView.getSettings();
        //webSettings.setJavaScriptEnabled(true);
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
    public static SpannableStringBuilder creaEnlaces(
            final Context contexto,
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
     * @return URL para de la licencia de la fotografía
     */
    public static String enlaceLicencia(
            final Context context,
            ImageView ivInfoFotoPreview,
            String urlImagen) {
        if(urlImagen != null && urlImagen.contains("wikimedia")){
            ivInfoFotoPreview.setVisibility(View.VISIBLE);
            return urlImagen
                    .replace("Special:FilePath/", "File:")
                    .replace("?widh=300px", "")
                    .replace("?width=300", "")
                    .concat(context.getString(R.string.ultimaParteLicencia));
        }else{
            return null;
        }
    }

    /**
     * Método para intentar enlazar la imagen con la licencia
     * @param context Contexto
     * @param textView TextView con el texto
     * @param urlImagen URL de la imagen donde está su licencia
     * @return Enlace de la licencia o null si no se ha conseguido crear
     */
    public static String enlaceLicencia(
            final Context context,
            TextView textView,
            String urlImagen) {
        if(urlImagen != null) {
            if(urlImagen.contains("upload.wikimedia")){
                textView.setVisibility(View.VISIBLE);
                return "https://commons.wikimedia.org/wiki/File:"
                        .concat(urlImagen.split("/")[urlImagen.split("/").length - 2])
                        .concat(context.getString(R.string.ultimaParteLicencia));
            }else{
                if(urlImagen.contains("wikimedia")){
                    textView.setVisibility(View.VISIBLE);
                    return urlImagen
                            .replace("Special:FilePath/", "File:")
                            .replace("?widh=300px", "")
                            .replace("?width=300", "")
                            .concat(context.getString(R.string.ultimaParteLicencia));
                } else
                    return null;
            }
        } else
            return null;
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
            case Auxiliar.tipoPreguntaVideo:
                return contexto.getString(R.string.preguntaVideo);
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
            InputStreamReader inputStreamReader = new InputStreamReader(
                    context.getResources().openRawResource(R.raw.municipios_castilla_y_leon));
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

    /**
     * Método para cerrar la sesión. Borrará los ficheros de identificación del usuario pero dejará
     * las tareas con las que ha interaccionado por si se vuelve a identificar en este dispositivo
     * @param context Contexto
     * @param app Aplicación
     * @param actividad Actividad
     * @return Verdaadero si se ha consegido realizar correctamente todas las operaciones para el
     * cierre de sesión
     */
    public static boolean cerrarSesion(
            final Context context,
            Application app,
            Object actividad){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        try {
            if(PersistenciaDatos.borraTodosFicheros(app)) {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean(Ajustes.NO_MOLESTAR_pref, false);
                editor.commit();
                editor.putString(Ajustes.HASHTAG_pref, app.getString(R.string.hashtag));
                editor.commit();
                editor.putInt(Ajustes.INTERVALO_pref, 4);
                editor.commit();
                editor.putBoolean(Ajustes.WIFI_pref, false);
                editor.commit();
                try {
                    Login.firebaseAuth.signOut();
                    Login.googleSignInClient.signOut().addOnCompleteListener(
                            (Activity) actividad,
                            new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                }
                            });
                    return true;
                }catch (Exception e){
                    return false;
                }
            }else{
                return false;
            }
        }catch (Exception e){
            return false;
        }
    }

    /**
     * Método para obtener la URL con la query que se va a realizar para la comunicación con el
     * servidor
     * @param ruta Dirección del servidor con la ruta hasta el recurso donde se va a realizar la query
     * @param key Lista de objetos clave
     * @param objects Lista de objetos valor
     * @return URL con query entendible por el servidor
     */
    public static String creaQuery(String ruta, List<String> key, List<Object> objects){
        String salida = String.format("%s?", ruta);
        int tama;
        if((tama = key.size()) == objects.size()) {
            for (int i = 0; i < tama; i++) {
                if (i == tama - 1)
                    salida = String.format("%s%s=%s", salida, key.get(i), objects.get(i).toString());
                else
                    salida = String.format("%s%s=%s&", salida, key.get(i), objects.get(i).toString());
            }
        } else
            salida = null;
        return salida;
    }

    /**
     * Método para eliminar los enlaces de un String. Para que el usuario no los vea en las notificaciones
     * por ejemplo
     * @param string Texto con enlaces
     * @return Texto sin enlaces
     */
    public static String quitaEnlaces(String string) {
        return string
                .replaceAll("</a>", "")
                .replaceAll("<a.*?>","")
                .replace("<span>", "")
                .replace("</span>", "")
                .replace("<br>"," ");
    }

    /**
     * Método para obtener la lista de fabricantes que pueden tener problemas con el servicio en segundo plano.
     * Basado en:
     *     https://stackoverflow.com/questions/48166206/how-to-start-power-manager-of-all-android-manufactures-to-enable-background-and
     * @return Intents para saltar a la aplicación de gestión de energía del fabricante del dispositivo
     */
    public static Intent[] intentProblematicos() {
        return new Intent[]{
                new Intent().setComponent(new ComponentName("com.miui.securitycenter", "com.miui.permcenter.autostart.AutoStartManagementActivity")),
                new Intent().setComponent(new ComponentName("com.miui.powerkeeper", "com.miui.powerkeeper.ui.HiddenAppsConfigActivity")),
                new Intent().setComponent(new ComponentName("com.letv.android.letvsafe", "com.letv.android.letvsafe.AutobootManageActivity")),
                new Intent().setComponent(new ComponentName("com.huawei.systemmanager", "com.huawei.systemmanager.startupmgr.ui.StartupNormalAppListActivity")),
                new Intent().setComponent(new ComponentName("com.huawei.systemmanager", "com.huawei.systemmanager.optimize.process.ProtectActivity")),
                new Intent().setComponent(new ComponentName("com.huawei.systemmanager", "com.huawei.systemmanager.appcontrol.activity.StartupAppControlActivity")),
                new Intent().setComponent(new ComponentName("com.huawei.systemmanager", "com.huawei.systemmanager.startupmgr.ui.StartupNormalAppListActivity")),
                new Intent().setComponent(new ComponentName("com.coloros.safecenter", "com.coloros.safecenter.permission.startup.StartupAppListActivity")),
                new Intent().setComponent(new ComponentName("com.coloros.safecenter", "com.coloros.safecenter.startupapp.StartupAppListActivity")),
                new Intent().setComponent(new ComponentName("com.oppo.safe", "com.oppo.safe.permission.startup.StartupAppListActivity")),
                new Intent().setComponent(new ComponentName("com.iqoo.secure", "com.iqoo.secure.ui.phoneoptimize.AddWhiteListActivity")),
                new Intent().setComponent(new ComponentName("com.iqoo.secure", "com.iqoo.secure.ui.phoneoptimize.BgStartUpManager")),
                new Intent().setComponent(new ComponentName("com.vivo.permissionmanager", "com.vivo.permissionmanager.activity.BgStartUpManagerActivity")),
                new Intent().setComponent(new ComponentName("com.samsung.android.lool", "com.samsung.android.sm.ui.battery.BatteryActivity")),
                new Intent().setComponent(new ComponentName("com.htc.pitroad", "com.htc.pitroad.landingpage.activity.LandingPageActivity")),
                new Intent().setComponent(new ComponentName("com.asus.mobilemanager", "com.asus.mobilemanager.MainActivity")),
                new Intent().setComponent(new ComponentName("com.transsion.phonemanager", "com.itel.autobootmanager.activity.AutoBootMgrActivity"))
        };
    }

    /**
     * Método para obtener los identificadores de las tareas que ya ha realizado el usuario.
     * @return Lisita de identificaciones de tareas completadas
     */
    public static List<String> getListaTareasCompletadas(Application app, String idUsuario){
        if(idUsuario != null) {
            JSONArray tareasCompletadas = PersistenciaDatos.leeFichero(app, PersistenciaDatos.ficheroCompletadas);
            List<String> listaId = new ArrayList<>();
            try {
                JSONObject tarea;
                for (int i = 0; i < tareasCompletadas.length(); i++) {
                    tarea = tareasCompletadas.getJSONObject(i);
                    if (tarea.get(Auxiliar.idUsuario).equals(idUsuario))
                        listaId.add(tareasCompletadas.getJSONObject(i).getString(Auxiliar.id));
                }
            } catch (Exception e) {
                listaId = new ArrayList<>();
            }
            return listaId;
        } else {
            return new ArrayList<>();
        }
    }

    /**
     * Método para obtener la lista de contextos con sus tareas completadas
     * @param app Aplicación
     * @param idUsuario Identificador del usuario
     * @return Lista de identificadores de contextos con todas las taeras completadas.
     */
    public static List<String> getListaContextosTareasCompletadas(Application app, String idUsuario){
        if(idUsuario != null) {
            JSONArray tareasCompletadas = PersistenciaDatos.leeFichero(app, PersistenciaDatos.ficheroCompletadas);
            List<String> listaId = new ArrayList<>();
            try {
                JSONObject tarea;
                for (int i = 0; i < tareasCompletadas.length(); i++) {
                    tarea = tareasCompletadas.getJSONObject(i);
                    if(tarea.get(Auxiliar.idUsuario).equals(idUsuario))
                        listaId.add(tareasCompletadas.getJSONObject(i).getString(Auxiliar.contexto));
                }
            }catch (Exception e){
                listaId = new ArrayList<>();
            }
            return listaId;
        } else {
            return new ArrayList<>();
        }
    }

    public static int[] obtenIdMarcadores(){
        return new int[]{R.drawable.ic_marcador100, R.drawable.ic_marcador100_especial,
                R.drawable.ic_marcador100_especial1, R.drawable.ic_marcador100_especial2,
                R.drawable.ic_marcador100_especial3, R.drawable.ic_marcador100_especial4};
    }

    public static boolean stringVacio(String s){
        if(s == null)
            return true;
        else
            return s.trim().isEmpty();
    }
}
