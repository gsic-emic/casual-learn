package es.uva.gsic.adolfinstro;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;

import androidx.core.app.NotificationCompat;
import androidx.preference.PreferenceManager;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import java.util.ArrayList;

import es.uva.gsic.adolfinstro.persistencia.GrupoTareas;
import es.uva.gsic.adolfinstro.persistencia.GrupoTareasDatabase;

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
    private NotificationManager notificationManager;

    private NotificationCompat.Builder builder;
    private int intervalo;
    private int incr = 0;
    private boolean noMolestar;

    /** Instancia de la base de datos*/
    private GrupoTareasDatabase db;

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
        ArrayList<String> permisos = new ArrayList<>();
        Auxiliar.preQueryPermisos(this, permisos);
        db = GrupoTareasDatabase.getInstance(getBaseContext());
        if(permisos.size()>0){ // Si se le han revocado permisos a la aplicación se mata el proceso
            terminaServicio();
        }
        //SUBIDA CON WIFI
        /*ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        for(Network network : connectivityManager.getAllNetworks()){
            NetworkInfo networkInfo = connectivityManager.getNetworkInfo(network);
            if(networkInfo.getType() == ConnectivityManager.TYPE_WIFI){
                Log.i("Wifi", "Conectado");
            }
            else{
                Log.i("Wifi", "no es wifi");
            }
        }*/

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){ //Se necesita un canal para API 26 y superior
            channel = new NotificationChannel(channelId, getString(R.string.canalTareas), NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription(getString(R.string.canalTareas));
            channelPersis = new NotificationChannel(channelPersisId, getString(R.string.canalPersistente), NotificationManager.IMPORTANCE_LOW);
            channelPersis.setDescription(getString(R.string.canalPersistente));
            notificationManager = this.getSystemService(NotificationManager.class);
            assert notificationManager != null;
            notificationManager.createNotificationChannel(channel);
            notificationManager.createNotificationChannel(channelPersis);
        }
        else{
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                notificationManager = this.getSystemService(NotificationManager.class);
            else{//API 22
                notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            }
        }
        //Se crea la notificación
        builder = new NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setPriority(NotificationCompat.PRIORITY_HIGH);
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
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
        long inter = 30000;
        locationRequest = new LocationRequest().create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(inter)
                .setFastestInterval(inter);
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult){
                if(locationResult == null){
                    return;
                }
                for(Location location : locationResult.getLocations()){
                    compruebaLocalizacion(location);
                }
            }
        };
        startLocation();
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

    /**
     * Método de pruebas
     * @param location Posición
     */
    private void compruebaLocalizacion(Location location) {
        double latitud = Math.round(location.getLatitude()*10000d)/10000d;
        double longitud = Math.round(location.getLongitude()*10000d)/10000d;

        if(!noMolestar){
            if(plazaMayorLat == latitud && plazaMayorLong == longitud){ //Actividad aleatoria
                pintaNotificacion(location, ((int) (Math.random()*6)));
            }
        }
    }

    /**
     * Método que lanza la notificación y actualiza el valor del TextView
     * @param location Última posición obtenida
     * @param idTarea Identificador de la tarea asociada a la notificación
     */
    private void pintaNotificacion(Location location, int idTarea) {
        if (location != null) {
            String id = "https://casssualearn.gsic.uva.es/resource/Castillo_de_Calatañazor/informacion";
            //todo
            //todo
            //todo
            //todo
            //todo
            //todo
            //todo
            //todo
            //¡¡¡¡¡¡¡¡¡¡¡¡¡¡¡CUANDO NO SE ESTÉ DEPURANDO PARA QUE EL IDENTIFICADOR DE LA TAREA SEA ÚNICO!!!!!!!!!!!!!!!!!!!!!!
            //todo
            //todo
            //todo
            //todo
            //todo
            id = id + System.nanoTime();
            GrupoTareas tarea;
            String titu = "";
            Intent intent = new Intent(this, Tarea.class);
            String recursoAsociadoTexto = "El Castillo de Calatañazor, también conocido como Castillo de los Padilla es un fortaleza medieval ubicada en la localidad española de igual nombre, en la provincia de Soria.";
            intent.putExtra("id", id);
            intent.putExtra("recursoAsociadoTexto", recursoAsociadoTexto);
            //intent.putExtra(Tarea.recursoImagen, "https://upload.wikimedia.org/wikipedia/commons/6/69/Salamanca_Parroquia_Arrabal.jpg");
            intent.putExtra(Tarea.recursoImagen, "https://commons.wikimedia.org/wiki/Special:FilePath/Calatañazor-Castillo.jpg");
            intent.putExtra(Tarea.recursoImagenBaja, "https://upload.wikimedia.org/wikipedia/commons/thumb/5/53/Calata%C3%B1azor-Castillo.jpg/300px-Calata%C3%B1azor-Castillo.jpg");
            switch (idTarea){
                case 0:
                    titu = "sinRespuesta";
                    intent.putExtra("tipoRespuesta", TiposTareas.SIN_RESPUESTA.getValue());
                    tarea = new GrupoTareas(id, TiposTareas.SIN_RESPUESTA.getValue(), EstadoTarea.NO_COMPLETADA);
                    break;
                case 1:
                    titu = "preguntaCorta";
                    intent.putExtra("tipoRespuesta", TiposTareas.PREGUNTA_CORTA.getValue());
                    tarea = new GrupoTareas(id, TiposTareas.PREGUNTA_CORTA.getValue(), EstadoTarea.NO_COMPLETADA);
                    break;
                case 2:
                    titu = "preguntaLarga";
                    intent.putExtra("tipoRespuesta", TiposTareas.PREGUNTA_LARGA.getValue());
                    tarea = new GrupoTareas(id, TiposTareas.PREGUNTA_LARGA.getValue(), EstadoTarea.NO_COMPLETADA);
                    break;
                case 3:
                    titu = "preguntaImagen";
                    intent.putExtra("tipoRespuesta", TiposTareas.PREGUNTA_IMAGEN.getValue());
                    tarea = new GrupoTareas(id, TiposTareas.PREGUNTA_IMAGEN.getValue(), EstadoTarea.NO_COMPLETADA);
                    break;
                case 4:
                    titu = "imagen";
                    intent.putExtra("tipoRespuesta", TiposTareas.IMAGEN.getValue());
                    tarea = new GrupoTareas(id, TiposTareas.IMAGEN.getValue(), EstadoTarea.NO_COMPLETADA);
                    break;
                case 5:
                    titu = "imagenMultiple";
                    intent.putExtra("tipoRespuesta", TiposTareas.IMAGEN_MULTIPLE.getValue());
                    tarea = new GrupoTareas(id, TiposTareas.IMAGEN_MULTIPLE.getValue(), EstadoTarea.NO_COMPLETADA);
                    break;
                case 6:
                    titu = "video";
                    intent.putExtra("tipoRespuesta", TiposTareas.VIDEO.getValue());
                    tarea = new GrupoTareas(id, TiposTareas.VIDEO.getValue(), EstadoTarea.NO_COMPLETADA);
                    break;
                default:
                    return;
            }
            db.grupoTareasDao().insertTarea(tarea);
            builder.setContentTitle(titu).setContentText(recursoAsociadoTexto);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), incr, intent, PendingIntent.FLAG_CANCEL_CURRENT);
            ++incr;
            builder.setContentIntent(pendingIntent);
            builder.setAutoCancel(true);
            notificationManager.notify( idTarea, builder.build());
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
        Intent intent = new Intent(this, Maps.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){ //Se diferencia las versiones de android
            Notification notification = new Notification.Builder(this, channelPersisId)
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
