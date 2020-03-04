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
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.preference.PreferenceManager;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;


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
    private NotificationChannel channel, channelPersis;
    private static String channelId = "notiTareas";
    private static String channelPersisId = "notiPersistencia";
    private NotificationManager notificationManager;

    private NotificationCompat.Builder builder;
    private int intervalo;
    private int incr = 0;
    private boolean noMolestar;
    //private long ultimaMedida;
    //private Date date;
    private HashMap<Integer, Integer> identificadorRealizada;//0 no contesta, 1 realizada, 2 rechazada

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
        identificadorRealizada = new HashMap<>();

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){ //Se necesita un canal para API 26 y superior
            channel = new NotificationChannel(channelId, getString(R.string.canalTareas), NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription(getString(R.string.canalTareas));
            channelPersis = new NotificationChannel(channelPersisId, getString(R.string.canalPersistente), NotificationManager.IMPORTANCE_LOW);
            channelPersis.setDescription(getString(R.string.canalPersistente));
            notificationManager = this.getSystemService(NotificationManager.class);
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
        //Se crea la notificación  SE NECESITA CAMBIAR PARA TENER MÁS DE UNA NOTIFICACIÓN DE LA APP
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
        Log.i("Intervalo: ", ""+(inter));
        Log.i("Inter. locationRequest", ""+locationRequest.getInterval());
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult){
                if(locationResult == null){
                    return;
                }
                for(Location location :locationResult.getLocations()){
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
        String l = getString(R.string.latitud) + ": " + latitud + " || " +
                getString(R.string.longitud) + ": " + longitud;
        if(!noMolestar){
            Boolean comprueba = true;
            /*if(ultimaMedida == 0){
                date = new Date();
            }
            else{
                date = new Date();
                Log.i("Resta: ", String.format("%d",(date.getTime() - ultimaMedida)));
                if(date.getTime() - ultimaMedida < intervalo * 60 * 1000){
                    comprueba = false;
                }
            }*/
            if(comprueba){
                //ultimaMedida = date.getTime();
                int idTarea = -1;
                //AQUÍ SE REALIZARÁ LA PETECIÓN SPARQL --> TIENE QUE HACERSE EN UN SERVICIO
                if(plazaMayorLat == latitud && plazaMayorLong == longitud){ //Respuesta texto
                    idTarea = 0; //La tomará de la respuesta a la consulta
                    setIdentificadorTarea(idTarea);
                }else{
                    if(laAntiguaLat == latitud && laAntiguaLong == longitud){ // Una foto
                        idTarea = 1;
                        setIdentificadorTarea(idTarea);
                    }else{
                        if(castilloPLat == latitud && castilloPLong == longitud){ //Varias fotos
                            idTarea = 2;
                            setIdentificadorTarea(idTarea);
                        }else{
                            if(plazaMayorSLat == latitud && plazaMayorSLong == longitud) { // Un vídeo
                                idTarea = 3;
                                setIdentificadorTarea(idTarea);
                            }else{
                                if(cigalesLat == latitud && cigalesLong == longitud) {
                                    idTarea = 4;
                                    setIdentificadorTarea(4);
                                }
                                else
                                    return;
                            }
                        }
                    }
                }
                if(idTarea != -1){
                    if(getEstado(idTarea)==0){
                        pintaNotificacion(location, idTarea);
                    }
                }
            }
        }
    }
    private void setIdentificadorTarea(int idTarea){
        synchronized (identificadorRealizada){
            if(identificadorRealizada.get(idTarea) == null){
                identificadorRealizada.put(idTarea, 0);
            }
        }
    }

    private int getEstado(int idTarea){
        int salida;
        synchronized (identificadorRealizada){
            salida = identificadorRealizada.get(idTarea);
        }
        return salida;
    }

    /**
     * Método que lanza la notificación y actualiza el valor del TextView
     * @param location Última posición obtenida
     * @param idTarea Identificador de la tarea asociada a la notificación
     */
    private void pintaNotificacion(Location location, int idTarea) {
        if (location != null) {
            String l = getString(R.string.latitud) + ": " + location.getLatitude() + " || " +
                    getString(R.string.longitud) + ": " + location.getLongitude();
            String titu = "";
            Intent intent = null;
            switch (idTarea){
                case 0:
                    titu = "Tarea de pregunta";
                    intent = new Intent(this, Ask.class);
                    break;
                case 1:
                    titu = "Tarea de una foto";
                    intent = new Intent(this, TaskCamera.class);
                    intent.putExtra("TIPO", R.id.btUnaFoto);
                    break;
                case 2:
                    titu = "Tarea de varias fotos";
                    intent = new Intent(this, TaskCamera.class);
                    intent.putExtra("TIPO", R.id.btVariasFotos);
                    break;
                case 3:
                    titu = "Tarea de un vídeo";
                    intent = new Intent(this, TaskCamera.class);
                    intent.putExtra("TIPO", R.id.btVideo);
                    break;
                case 4:
                    titu = "Tarea de pregunta y foto";
                    intent = new Intent(this, Ask_camera.class);
                    break;
            }
            builder.setContentTitle(titu).setContentText(l);
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
        Intent intent = new Intent(this, Proceso.class);
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
