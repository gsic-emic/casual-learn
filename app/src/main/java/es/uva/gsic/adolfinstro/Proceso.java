package es.uva.gsic.adolfinstro;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;
import androidx.preference.PreferenceManager;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;

import es.uva.gsic.adolfinstro.auxiliar.Auxiliar;
import es.uva.gsic.adolfinstro.auxiliar.ColaConexiones;
import es.uva.gsic.adolfinstro.persistencia.PersistenciaDatos;

/**
 * Clase encargada de mantener el proceso en memoria y lanzar las notificaciones de tareas cuando sea
 * necesario. Recupera la posición del usuario periódicamente.
 *
 * @author GSIC
 */
public class Proceso extends Service implements SharedPreferences.OnSharedPreferenceChangeListener {

    /**
     * Objeto utilizado para obtener la posición. Es el objeto al que hay que solicitar el inicio y
     * finalización de la solicitud de la posición
     */
    private FusedLocationProviderClient fusedLocationProviderClient;
    /** Instancia que realiza la petición para obtener la posición*/
    private LocationRequest locationRequest;
    /** Instancia donde están asociadas las tareas a realizar cuando se recupera la posición */
    private LocationCallback locationCallback;

    /** Canal utilizado para las notificaciones de las tareas */
    private NotificationChannel channel;
    /** Canal utilizado para la notificación persistente */
    private NotificationChannel channelPersis;
    /** Identificador del canal de tareas */
    private static String channelId = "notiTareas";
    /** Identificador del canal por donde irá la notificación persistente */
    private static String channelPersisId = "notiPersistencia";
    /** Instancia del NotificationManager*/
    private NotificationManager notificationManager;
    /** Valor incremental de la notificación que lanza el sistema*/
    static int incr = 0;

    /** Valor actual de la preferencia no Molestar */
    private boolean noMolestar;
    /** Valor actual de la preferencia intervalo por la que se muestra la notificación automática */
    private int intervalo;

    /** Contexto del proceso */
    private Context context;

    /**Tiempo entre cada comprobación de la posición del alumno.*/
    private final long intervaloComprobacion = 30000;

    /** Distancia máxima que podría andar en el intervalo de comprobación*/
    private final double maxAndado = (5 * ((double)intervaloComprobacion/1000) / 3600);

    /** Instante en el que se realizó la última notificación automática */
    private long instanteUltimaNotif = 0;

    /** Latitiud desde donde se han recuperado las tareas del servidor */
    public static double latitudGet = -5;
    /** Longitud desde donde se han recuperado las tareas del servidor */
    public static double longitudGet = -5;

    /** Última latitud obtenida */
    private double latitudAnt = -5;
    /** Última longitud obtenida */
    private double longitudAnt = -5;

    // Métodos necesarios para heredar de Service
    public class ProcesoBinder extends Binder {
        Proceso getService() {
            return Proceso.this;
        }
    }

    private final IBinder iBinder = new ProcesoBinder();
    @Override
    public IBinder onBind(Intent intent){
        return iBinder;
    }

    /**
     * Método de creación del proceso. Se comprueba si los servicios siguen siendo los que necesita
     * la app
     */
    @Override
    public void onCreate(){
        context = this;
        ArrayList<String> permisos = new ArrayList<>();
        Auxiliar.preQueryPermisos(context, permisos);
        if(permisos.size()>0){ // Si se le han revocado permisos a la aplicación se mata el proceso
            terminaServicio();
        }

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){ //Se necesita un canal para API 26 y superior
            channel = new NotificationChannel(channelId, getString(R.string.canalTareas), NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription(getString(R.string.canalTareas));
            channelPersis = new NotificationChannel(channelPersisId, getString(R.string.canalPersistente), NotificationManager.IMPORTANCE_LOW);
            channelPersis.setDescription(getString(R.string.canalPersistente));
            notificationManager = context.getSystemService(NotificationManager.class);
            assert notificationManager != null;
            notificationManager.createNotificationChannel(channel);
            notificationManager.createNotificationChannel(channelPersis);
        }
        else{
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                notificationManager = context.getSystemService(NotificationManager.class);
            else{//API 22
                notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            }
        }

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);
        posicionamiento();
        onSharedPreferenceChanged(sharedPreferences, Ajustes.INTERVALO_pref);
        onSharedPreferenceChanged(sharedPreferences, Ajustes.NO_MOLESTAR_pref);

        mantenServicio();
    }

    /**
     * Inicia los objetos necesarios para llevar a cabo el seguimiento de la posición
     */
    private void posicionamiento(){
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        locationRequest = new LocationRequest().create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(intervaloComprobacion)
                .setFastestInterval(intervaloComprobacion);
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult){
                if(locationResult == null){
                    return;
                }
                for(Location location : locationResult.getLocations()){
                    compruebaTareas(location);
                }
            }
        };
        startLocation();
    }

    /**
     * Método donde se comprueba si es necesario solicitar al servidor nuevas tareas ya que el
     * alumno se ha desplazado lo suficiente
     * @param location Objeto que contiene la ubicación del alumno
     */
    private void compruebaTareas(Location location) {
        double latitud = location.getLatitude();
        double longitud = location.getLongitude();
        //TODO PETICIÓN GET AL SERVIDOR Y ACTUALIZACIÓN DEL FICHERO CON TAREAS (SOBRESCTRITURA). Si se consigue guardar el fichero se guarda la posición en latitudGet y longitudGet
        if(latitudGet<0 || longitudGet<0){//Inicio del servicio, se tiene que recuperar la tarea del servidor
            peticionTareasServidor(location);
        }else{
            double distanciaOrigen = Auxiliar.calculaDistanciaDosPuntos(latitud, longitud, latitudGet, longitudGet);
            if(distanciaOrigen >= 0.75){//Las tareas en local están obsoletas, hay que pedir unas nuevas al servidor
                peticionTareasServidor(location);
            }else {//El fichero sigue siendo válido
                compruebaLocalizacion(location);
            }
        }
    }

    public void peticionTareasServidor(final Location location){
        try{
            JSONArray array = new JSONArray();
            JSONObject jsonObject = new JSONObject();
            String url = "http://192.168.1.14:8080/tareas?latitude="+location.getLatitude()
                    +"&longitude="+location.getLongitude()
                    +"&radio=1.25";
            jsonObject.put("latitud", location.getLatitude());
            jsonObject.put("longitud", location.getLongitude());
            jsonObject.put("radio", "1.25");
            array.put(jsonObject);
            JsonArrayRequest jsonObjectRequest = new JsonArrayRequest(Request.Method.GET, url, null, new Response.Listener<JSONArray>() {
                @Override
                public void onResponse(JSONArray response) {
                    PersistenciaDatos.guardaFichero(getApplication(), PersistenciaDatos.ficheroTareas, response, Context.MODE_PRIVATE);
                    latitudGet = location.getLatitude();
                    longitudGet = location.getLongitude();
                    compruebaLocalizacion(location);
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    compruebaLocalizacion(location);
                }

            });
            ColaConexiones.getInstance(getApplicationContext()).getRequestQueue().add(jsonObjectRequest);
        }catch (JSONException ex){
            //
        }
    }


    /* PRUEBAS */
    final double plazaMayorLat = 41.6520;
    final double plazaMayorLong = -4.7286;
    final double laAntiguaLat = 41.6547;
    final double laAntiguaLong = -4.7231;
    final double castilloPLat = 41.5966;
    final double castilloPLong = -4.1144;
    final double plazaMayorSLat = 40.9650;
    final double plazaMayorSLong = -5.6641;
    final double cigalesLat = 41.7581;
    final double cigalesLong = -4.698;
    final double tareaLat = 42.0076;
    final double tareaLong = -4.52449;
    /**
     * Método de pruebas
     * @param location Posición
     */
    private void compruebaLocalizacion(Location location) {
        double distanciaAndada=1200, latitud=0, longitud=0;
        boolean datosValidos = false;
        if(latitudAnt < 0){//Se acaba de iniciar el servicio
            latitudAnt = location.getLatitude();
            longitudAnt = location.getLongitude();
        }
        else{
            latitud = location.getLatitude();
            longitud = location.getLongitude();
            distanciaAndada = Auxiliar.calculaDistanciaDosPuntos(latitudAnt, longitudAnt, latitud, longitud);
            latitudAnt = latitud; longitudAnt = longitud;
            datosValidos = true;
        }

        Date date = new Date();
        long instante = date.getTime();
        boolean comprueba = instante >= instanteUltimaNotif + ((intervalo * 3600 > 0)?intervalo*3600:59000);
        if(comprueba){//Se comprueba cuando se ha lanzado la última notificación
            if(datosValidos){//Se comprueba si los datos son válidos (inicio proceso)
                if(distanciaAndada <= maxAndado){//Se comprueba si el usuario está caminando
                    //Comprobación de la ubucación actual a las tareas almacenadas
                    //TODO FALTA COMPROBAR EL MÉTODO TAREAS
                    //Se obtiene la distancia más baja a la tarea
                    double distancia = Auxiliar.calculaDistanciaDosPuntos(latitud,longitud,plazaMayorLat,plazaMayorLong);
                    if(distancia < 0.15){//Si el usuario está lo suficientemente cerca, se le envía una notificación
                        pintaNotificacion(String.format("%d",(int) (Math.random()*6)));
                    }
                }
            }
        }
    }

    //TODO Método únicamente utilizado para desarrollo BORRAR!!
    private void pintaNotificacion(String idTarea) {
        String id = "https://casssualearn.gsic.uva.es/resource/Ermita_de_San_Juan_Bautista_(Palencia)/compararPortadaRomanicoGotico/10";
        id = id + System.nanoTime();
        //GrupoTareas tarea;
        String titu;
        Intent intent = new Intent(context, Tarea.class);
        String recursoAsociadoTexto = "Fotografía la portada que da acceso a la Ermita de San Juan Bautista. Luego puedes acercarte a la Iglesia de San Francisco. Fotografía también su portada y compara ambas";
        intent.putExtra(Auxiliar.id, id);
        intent.putExtra(Auxiliar.recursoAsociadoTexto, recursoAsociadoTexto);
        //intent.putExtra(Tarea.recursoImagen, "https://upload.wikimedia.org/wikipedia/commons/6/69/Salamanca_Parroquia_Arrabal.jpg");
        //intent.putExtra(Tarea.recursoImagen, "https://commons.wikimedia.org/wiki/Special:FilePath/Calatañazor-Castillo.jpg");
        //intent.putExtra(Tarea.recursoImagenBaja, "https://upload.wikimedia.org/wikipedia/commons/thumb/5/53/Calata%C3%B1azor-Castillo.jpg/300px-Calata%C3%B1azor-Castillo.jpg");
        intent.putExtra(Auxiliar.recursoImagen, "https://upload.wikimedia.org/wikipedia/commons/1/13/Iglesia_de_San_Francisco_%28Palencia%29._Fachada.jpg");
        intent.putExtra(Auxiliar.recursoImagenBaja, "https://upload.wikimedia.org/wikipedia/commons/thumb/1/13/Iglesia_de_San_Francisco_%28Palencia%29._Fachada.jpg/300px-Iglesia_de_San_Francisco_%28Palencia%29._Fachada.jpg");
        idTarea = "1";
        switch (idTarea){
            case "0":
                titu = "sinRespuesta";
                intent.putExtra("tipoRespuesta", TiposTareas.SIN_RESPUESTA.getValue());
                //tarea = new GrupoTareas(id, TiposTareas.SIN_RESPUESTA.getValue(), EstadoTarea.NOTIFICADA);
                break;
            case "1":
                titu = "preguntaCorta";
                intent.putExtra("tipoRespuesta", TiposTareas.PREGUNTA_CORTA.getValue());
                //tarea = new GrupoTareas(id, TiposTareas.PREGUNTA_CORTA.getValue(), EstadoTarea.NOTIFICADA);
                break;
            case "2":
                titu = "preguntaLarga";
                intent.putExtra("tipoRespuesta", TiposTareas.PREGUNTA_LARGA.getValue());
                //tarea = new GrupoTareas(id, TiposTareas.PREGUNTA_LARGA.getValue(), EstadoTarea.NOTIFICADA);
                break;
            case "3":
                titu = "preguntaImagen";
                intent.putExtra("tipoRespuesta", TiposTareas.PREGUNTA_IMAGEN.getValue());
                //tarea = new GrupoTareas(id, TiposTareas.PREGUNTA_IMAGEN.getValue(), EstadoTarea.NOTIFICADA);
                break;
            case "4":
                titu = "imagen";
                intent.putExtra("tipoRespuesta", TiposTareas.IMAGEN.getValue());
                //tarea = new GrupoTareas(id, TiposTareas.IMAGEN.getValue(), EstadoTarea.NOTIFICADA);
                break;
            case "5":
                titu = "imagenMultiple";
                intent.putExtra("tipoRespuesta", TiposTareas.IMAGEN_MULTIPLE.getValue());
                //tarea = new GrupoTareas(id, TiposTareas.IMAGEN_MULTIPLE.getValue(), EstadoTarea.NOTIFICADA);
                break;
            case "6":
                titu = "video";
                intent.putExtra("tipoRespuesta", TiposTareas.VIDEO.getValue());
                //tarea = new GrupoTareas(id, TiposTareas.VIDEO.getValue(), EstadoTarea.NOTIFICADA);
                break;
            default:
                return;
        }
        //db.grupoTareasDao().insertTarea(tarea);
        try {
            //JSONArray jsonArray = Auxiliar.leeFichero(getApplication(), Auxiliar.ficheroRespuestas);
            //No se comprueba si la tarea existía en la base de datos ya que se ha realizado esta comprobación el método previo
            JSONObject jsonObject = PersistenciaDatos.generaJSON(id, titu, EstadoTarea.NOTIFICADA);
            //jsonArray.put(jsonObject);
            if(!PersistenciaDatos.guardaJSON(getApplication(), PersistenciaDatos.ficheroRespuestas, jsonObject, Context.MODE_PRIVATE))
                throw new Exception();
            //Se crea aquí la notificación para que la hora a la que se lanza la notificación sea el correcto
            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId)
                    .setSmallIcon(R.drawable.ic_launcher_foreground)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setContentTitle(titu)
                    .setContentText(recursoAsociadoTexto);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, incr, intent, PendingIntent.FLAG_CANCEL_CURRENT);

            builder.setContentIntent(pendingIntent);
            builder.setAutoCancel(true);
            //builder.setTimeoutAfter(tiempoNotificacion); No es necesario en este tipo de notificaciones

            //Botones extra
            Intent intentBoton = new Intent(context, RecepcionNotificaciones.class);
            intentBoton.setAction("AHORA_NO");
            intentBoton.putExtra("id", id);
            intentBoton.putExtra("idNotificacion", incr);
            PendingIntent ahoraNoPending = PendingIntent.getBroadcast(context, incr + 999, intentBoton, PendingIntent.FLAG_UPDATE_CURRENT);
            builder.addAction(R.drawable.ic_thumb_down_black_24dp, getString(R.string.ahoraNo), ahoraNoPending);

            intentBoton.setAction("NUNCA_MAS");
            PendingIntent nuncaMasP = PendingIntent.getBroadcast(context, incr + 1000, intentBoton, PendingIntent.FLAG_UPDATE_CURRENT);
            builder.addAction(R.drawable.ic_delete_black_24dp, getString(R.string.nuncaMas), nuncaMasP);

            notificationManager.notify(incr, builder.build());

            instanteUltimaNotif = new Date().getTime();

            ++incr;
        }catch (Exception e){
            e.printStackTrace();
        }
    }


    /**
     * Método encargado de lanzar la notificación automática
     * @param jsonObject Objeto JSON con los datos de la tarea
     */
    public void pintaNotificacion(JSONObject jsonObject){
        try{
            //Recursos que siempre van a tener todas las tareas
            String id = jsonObject.getString(Auxiliar.id);
            String tipoRespuesta = jsonObject.getString(Auxiliar.tipoRespuesta);
            tipoRespuesta = Auxiliar.ultimaParte(tipoRespuesta);
            String recursoAsociadoTexto = jsonObject.getString(Auxiliar.recursoAsociadoTexto);
            String recursoAsociadoImagen = null, recursoAsociadoImagenBaja = null, respuestaEsperada = null;
            try{
                recursoAsociadoImagen = jsonObject.getString(Auxiliar.recursoImagen);
                recursoAsociadoImagenBaja = jsonObject.getString(Auxiliar.recursoImagenBaja);
            } catch (Exception e){
                //Falta alguno de los dos recursos
            }

            try{
                respuestaEsperada = jsonObject.getString(Auxiliar.respuestaEsperada);
            } catch (Exception e){
                //No tiene respuestaEsperada
            }
            //GrupoTareas tarea = new GrupoTareas(id, tipoRespuesta, EstadoTarea.NOTIFICADA);
            try {
                JSONObject json = PersistenciaDatos.generaJSON(id, tipoRespuesta, EstadoTarea.NOTIFICADA);
                if(!PersistenciaDatos.guardaJSON(getApplication(), PersistenciaDatos.ficheroRespuestas, json, Context.MODE_PRIVATE))
                    throw new Exception();
                Intent intent = new Intent(context, Tarea.class);
                intent.putExtra(Auxiliar.id, id);
                intent.putExtra(Auxiliar.recursoAsociadoTexto, recursoAsociadoTexto);
                intent.putExtra(Auxiliar.recursoImagen, recursoAsociadoImagen);
                intent.putExtra(Auxiliar.recursoImagenBaja, recursoAsociadoImagenBaja);
                intent.putExtra(Auxiliar.respuestaEsperada, respuestaEsperada);

                NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId)
                        .setSmallIcon(R.drawable.ic_launcher_foreground)
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setContentTitle(tipoRespuesta)
                        .setContentText(recursoAsociadoTexto);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                PendingIntent pendingIntent = PendingIntent.getActivity(context, incr, intent, PendingIntent.FLAG_CANCEL_CURRENT);

                builder.setContentIntent(pendingIntent);
                builder.setAutoCancel(true);
                //builder.setTimeoutAfter(tiempoNotificacion); No es necesario en este tipo de notificaciones

                //Botones extra
                Intent intentBoton = new Intent(context, RecepcionNotificaciones.class);
                intentBoton.setAction("AHORA_NO");
                intentBoton.putExtra("id", id);
                intentBoton.putExtra("idNotificacion", incr);
                PendingIntent ahoraNoPending = PendingIntent.getBroadcast(context, incr + 999, intentBoton, PendingIntent.FLAG_UPDATE_CURRENT);
                builder.addAction(R.drawable.ic_thumb_down_black_24dp, getString(R.string.ahoraNo), ahoraNoPending);

                intentBoton.setAction("NUNCA_MAS");
                PendingIntent nuncaMasP = PendingIntent.getBroadcast(context, incr + 1000, intentBoton, PendingIntent.FLAG_UPDATE_CURRENT);
                builder.addAction(R.drawable.ic_delete_black_24dp, getString(R.string.nuncaMas), nuncaMasP);
                notificationManager.notify(incr, builder.build()); //Notificación lanzada

                instanteUltimaNotif = new Date().getTime(); //Actualizamos el instante
                ++incr; //Para que no tengan dos notificaciones el mismo valor
            }catch (Exception e){
                e.printStackTrace();
            }
            //db.grupoTareasDao().insertTarea(tarea);//Guardo en la base de datos
        }catch (JSONException je){
            //Si alguno de los campos que siempre deberían existir no existen
        }
    }

    /**
     * Inicializador del bucle que obtiene la posición
     */
    private void startLocation(){
        fusedLocationProviderClient
                .requestLocationUpdates(locationRequest, locationCallback,null);
    }

    /**
     * Detiene la tarea de recogida de posición
     */
    private void stopLocation(){
        fusedLocationProviderClient.removeLocationUpdates(locationCallback);
    }

    /**
     * Método para crear el servicio en primer plano
     */
    private void mantenServicio(){
        //Intent intent = new Intent(this, Proceso.class);
        Intent intent = new Intent(context, Maps.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){ //Se diferencia las versiones de android
            Notification notification = new Notification.Builder(context, channelPersisId)
                    .setContentTitle(getString(R.string.app_name))
                    .setContentText(getString(R.string.textoNotificacionPersistente))
                    .setSmallIcon(R.drawable.ic_launcher_foreground)
                    .setContentIntent(pendingIntent)
                    .build();
            startForeground(100, notification);
        }
        else
            startService(intent);
    }

    /**
     * Método de actuación cuando se detecta un cambio en una de las preferencias
     * @param sharedPreferences Preferencias
     * @param key Preferencia seleccionada
     */
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        switch (key){
            case Ajustes.INTERVALO_pref:
                intervalo = sharedPreferences.getInt(key, 1);
                break;
            case Ajustes.NO_MOLESTAR_pref:
                noMolestar = sharedPreferences.getBoolean(key, false);
                if(noMolestar){
                    stopLocation();
                    terminaServicio();
                }
                break;
        }
    }

    /**
     * Método utilizado para terminar con el servicio. Se hace distinción entre las versiones de android que
     * soportan xForeground de las que no
     */
    public void terminaServicio(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            stopForeground(Service.STOP_FOREGROUND_REMOVE);
            stopSelf();
        }
        else
            stopSelf();
    }
}
