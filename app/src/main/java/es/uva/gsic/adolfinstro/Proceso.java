package es.uva.gsic.adolfinstro;

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

import java.util.Date;
import java.util.HashMap;


/************************************ NO FUNCIONA *************************************************/
public class Proceso extends Service implements SharedPreferences.OnSharedPreferenceChangeListener {

    private static final int requestCodePermissions = 1000;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationRequest locationRequest;
    private NotificationChannel channel;
    private NotificationManager notificationManager;
    private LocationCallback locationCallback;
    private NotificationCompat.Builder builder;
    private int intervalo;
    private boolean noMolestar;
    private long ultimaMedida;
    private Date date;
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

    @Override
    public void onCreate(){
        Log.i("Dentro del proceso", "Proceso creado");
        identificadorRealizada = new HashMap<>();

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){ //Se necesita un canal para API 26 y superior
            channel = new NotificationChannel("100", "100", NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription("100");
            notificationManager = this.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
        //Se crea la notificación  SE NECESITA CAMBIAR PARA TENER MÁS DE UNA NOTIFICACIÓN DE LA APP
        builder = new NotificationCompat.Builder(this, "100")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setPriority(NotificationCompat.PRIORITY_HIGH);
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);
        onSharedPreferenceChanged(sharedPreferences, Ajustes.INTERVALO_pref);
        onSharedPreferenceChanged(sharedPreferences, Ajustes.NO_MOLESTAR_pref);

        posicionamiento();
    }

    /**
     * Inicia los objetos necesarios para llevar a cabo el seguimiento de la posición
     */
    private void posicionamiento(){
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        locationRequest = new LocationRequest().create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(10000).setFastestInterval(10000);
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

    private void compruebaLocalizacion(Location location) {
        double latitud = Math.round(location.getLatitude()*10000d)/10000d;
        double longitud = Math.round(location.getLongitude()*10000d)/10000d;
        String l = getString(R.string.latitud) + ": " + latitud + " || " +
                getString(R.string.longitud) + ": " + longitud;
        if(!noMolestar){
            Boolean comprueba = true;
            if(ultimaMedida == 0){
                date = new Date();
            }
            else{
                date = new Date();
                Log.i("Resta: ", String.format("%d",(date.getTime() - ultimaMedida)));
                if(date.getTime() - ultimaMedida < intervalo * 60 * 1000){
                    comprueba = false;
                }
            }
            if(comprueba){
                ultimaMedida = date.getTime();
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
            Log.i("pintaNoficacion", "Pinto notificación de la tarea" + idTarea);
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
            }
            builder.setContentTitle(titu).setContentText(l);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);
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

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        switch (key){
            case Ajustes.INTERVALO_pref:
                intervalo = Integer.parseInt(sharedPreferences.getString(key, "3"));
                break;
            case Ajustes.NO_MOLESTAR_pref:
                noMolestar = sharedPreferences.getBoolean(key, false);
                break;
        }
        String m = "Intervalo: " + intervalo + " No Molestar: " + (noMolestar?"ON":"OFF");
    }
}
