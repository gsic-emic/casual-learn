package es.uva.gsic.adolfinstro;

import android.Manifest;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.SystemClock;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.preference.PreferenceManager;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import es.uva.gsic.adolfinstro.auxiliar.Auxiliar;
import es.uva.gsic.adolfinstro.auxiliar.ColaConexiones;
import es.uva.gsic.adolfinstro.persistencia.PersistenciaDatos;

/**
 * Clase encargada de mantener el proceso en memoria y lanzar las notificaciones de tareas cuando sea
 * necesario. Recupera la posición del usuario periódicamente.
 *
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
    private final double maxAndado = (5 * ((double) intervaloComprobacion / 1000) / 3600);

    public static boolean tareasActualizadas = false;

    /** Última latitud obtenida */
    private double latitudAnt = 0;
    /** Última longitud obtenida */
    private double longitudAnt = 0;

    private String idInstanteGET = "instanteGET";
    private String idInstanteNotAuto = "instanteNotAuto";

    private boolean servicioIniciado = false;

    // Métodos necesarios para heredar de Service
    /**public class ProcesoBinder extends Binder {
        Proceso getService() {
            return Proceso.this;
        }
    }

    private final IBinder iBinder = new ProcesoBinder();**/

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /**
     * Método de creación del proceso. Se comprueba si los servicios siguen siendo los que necesita
     * la app
     */
    @Override
    public void onCreate() {
        context = this;
        ArrayList<String> permisos = new ArrayList<>();
        Auxiliar.preQueryPermisos(context, permisos);
        if (permisos.size() > 0) { // Si se le han revocado permisos a la aplicación se mata el proceso
            terminaServicio();
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) { //Se necesita un canal para API 26 y superior
            channel = new NotificationChannel(Auxiliar.channelId, getString(R.string.canalTareas), NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription(getString(R.string.canalTareas));
            channelPersis = new NotificationChannel(channelPersisId, getString(R.string.canalPersistente), NotificationManager.IMPORTANCE_LOW);
            channelPersis.setDescription(getString(R.string.canalPersistente));
            notificationManager = context.getSystemService(NotificationManager.class);
            assert notificationManager != null;
            notificationManager.createNotificationChannel(channel);
            notificationManager.createNotificationChannel(channelPersis);
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                notificationManager = context.getSystemService(NotificationManager.class);
            else {//API 22
                notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            }
        }

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);
        posicionamiento();
        onSharedPreferenceChanged(sharedPreferences, Ajustes.INTERVALO_pref);
        onSharedPreferenceChanged(sharedPreferences, Ajustes.NO_MOLESTAR_pref);

        //mantenServicio();
        /*AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(getApplicationContext(), AlarmaProceso.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 9999, intent, 0);
        alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() + 5*1000, pendingIntent);*/
    }

    /**
     * Inicia los objetos necesarios para llevar a cabo el seguimiento de la posición
     */
    LocationManager locationManager;
    LocationListener locationListener;

    private void posicionamiento() {
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                compruebaTareas(location);
                locationManager.removeUpdates(locationListener);
                //terminaServicio();
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        });
        /*fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
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
        startLocation();*/
    }

    /**
     * Método donde se comprueba si es necesario solicitar al servidor nuevas tareas ya que el
     * alumno se ha desplazado lo suficiente
     * @param location Objeto que contiene la ubicación del alumno
     */
    private void compruebaTareas(Location location) {
        //Latitiud desde donde se han recuperado las tareas del servidor
        double latitudGet = 0;
        //Longitud desde donde se han recuperado las tareas del servidor
        double longitudGet = 0;
        double latitud = location.getLatitude();
        double longitud = location.getLongitude();
        //Se comprueba si existe el fichero y el objeto
        if(PersistenciaDatos.existeTarea(getApplication(), PersistenciaDatos.ficheroInstantes, idInstanteGET)){
            try {
                JSONObject instante = PersistenciaDatos.recuperaTarea(getApplication(), PersistenciaDatos.ficheroInstantes, idInstanteGET);
                latitudGet = instante.getDouble(Auxiliar.latitud);
                longitudGet = instante.getDouble(Auxiliar.longitud);
            }catch (Exception e){
                //
            }
        }
        if(latitudGet==0 || longitudGet==0){//Inicio del servicio, se tiene que recuperar la tarea del servidor
            peticionTareasServidor(location, 1.25);
        }else{
            double distanciaOrigen = Auxiliar.calculaDistanciaDosPuntos(latitud, longitud, latitudGet, longitudGet);
            if(distanciaOrigen >= 0.75){//Las tareas en local están obsoletas, hay que pedir unas nuevas al servidor
                peticionTareasServidor(location, 1.25);
            }else {//El fichero sigue siendo válido
                compruebaLocalizacion(location);
            }
        }
    }

    /**
     * Método que recupera del servidor las tareas que se encuentral cerca del usuario para lanzar las notificiaciones
     * cuando sea preciso.
     * @param location Posición del usuario
     * @param radio Radio de la consulta
     */
    private void peticionTareasServidor(final Location location, double radio){
        String url = "http://192.168.1.14:10001/tareas?latitude="+location.getLatitude()
                +"&longitude="+location.getLongitude()
                +"&radio="+radio;
        JsonArrayRequest jsonObjectRequest = new JsonArrayRequest(Request.Method.GET, url, null, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                JSONArray nuevasTaras = new JSONArray();
                JSONObject j;
                boolean guarda;
                for(int i = 0; i < response.length(); i++){
                    try {
                        j = response.getJSONObject(i);
                        if(Auxiliar.tareaRegistrada(getApplication(), j.getString(Auxiliar.id))){
                            if(PersistenciaDatos.existeTarea(getApplication(), PersistenciaDatos.ficheroNotificadas, j.getString(Auxiliar.id))){
                                try {
                                    PersistenciaDatos.obtenTarea(getApplication(), PersistenciaDatos.ficheroNotificadas, j.getString(Auxiliar.id));
                                    guarda = true;
                                } catch (Exception e) {
                                    guarda = false;
                                }
                            } else {
                                guarda = false;
                            }
                        }else{
                            guarda = true;
                        }
                        if(guarda)
                            nuevasTaras.put(j);
                    } catch (JSONException e) {
                        //Error al extrar el json
                    }
                }
                PersistenciaDatos.guardaFichero(getApplication(), PersistenciaDatos.ficheroTareasUsuario, nuevasTaras, Context.MODE_PRIVATE);
                try {
                    j = new JSONObject();
                    j.put(Auxiliar.id, idInstanteGET);
                    j.put(Auxiliar.latitud, location.getLatitude());
                    j.put(Auxiliar.longitud, location.getLongitude());
                    j.put(Auxiliar.instante, new Date().getTime());
                    PersistenciaDatos.reemplazaJSON(getApplication(), PersistenciaDatos.ficheroInstantes, j);
                }catch (JSONException e){
                    //No se ha guardado el get en el registro, volverá a pedirlo en la siguiente iteración
                }
                tareasActualizadas = true;
                compruebaLocalizacion(location);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                compruebaLocalizacion(location);
            }

        });
        ColaConexiones.getInstance(getApplicationContext()).getRequestQueue().add(jsonObjectRequest);
    }


    /* PRUEBAS
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
    final double tareaLong = -4.52449;*/
    /**
     * Método que a partir de la localización del usuario y una serie de condiciones decide si se le
     * debe avisar al usuario de la realización de una nueva tarea.
     * @param location Posición
     */
    private void compruebaLocalizacion(Location location) {
        double distanciaAndada=1200, latitud=0, longitud=0;
        boolean datosValidos = false;

        if(latitudAnt == 0 && longitudAnt == 0){//Se acaba de iniciar el servicio
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

        long instanteUltimaNotif;

        try {
            JSONObject instante = PersistenciaDatos.recuperaTarea(getApplication(), PersistenciaDatos.ficheroInstantes, idInstanteNotAuto);
            instanteUltimaNotif = instante.getLong(Auxiliar.instante);
        }catch (Exception e){
            instanteUltimaNotif = 0;
        }

        boolean comprueba = (new Date().getTime()) >= instanteUltimaNotif + ((Auxiliar.intervaloMinutos(intervalo) > 0)?Auxiliar.intervaloMinutos(intervalo)*60*1000:20000);
        if(comprueba){//Se comprueba cuando se ha lanzado la última notificación
            if(datosValidos){//Se comprueba si los datos son válidos (inicio proceso)
                if(distanciaAndada <= maxAndado){//Se comprueba si el usuario está caminando
                    //Comprobación de la ubucación actual a las tareas almacenadas
                    JSONObject tarea = Auxiliar.tareaMasCercana(getApplication(), latitud, longitud);
                    //Se obtiene la distancia más baja a la tarea
                    if(tarea != null) {
                        double distancia;
                        try {
                            distancia = Auxiliar.calculaDistanciaDosPuntos(latitud,
                                    longitud,
                                    tarea.getDouble(Auxiliar.latitud),
                                    tarea.getDouble(Auxiliar.longitud));
                        } catch (JSONException je) {
                            distancia = 10;
                        }
                        if (distancia < 0.15) {//Si el usuario está lo suficientemente cerca, se le envía una notificación
                            //pintaNotificacion(String.format("%d",(int) (Math.random()*6)));
                            try {
                                PersistenciaDatos.obtenTarea(getApplication(), PersistenciaDatos.ficheroTareasUsuario, tarea.getString(Auxiliar.id));
                                pintaNotificacion(tarea); //Si no se ha eliminado la tarea del otro fichero no se lanza la notificación
                            }catch (Exception e){
                                //
                            }
                        }
                    }
                }
            }
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
            try {
                jsonObject.put(Auxiliar.tipoRespuesta, tipoRespuesta);
                jsonObject.put(Auxiliar.estadoTarea, EstadoTarea.NOTIFICADA.getValue());
                jsonObject.put(Auxiliar.origen, PersistenciaDatos.ficheroTareasUsuario);
                jsonObject.put(Auxiliar.fechaNotificiacion, Auxiliar.horaFechaActual());
                if(!PersistenciaDatos.guardaJSON(getApplication(), PersistenciaDatos.ficheroNotificadas, jsonObject, Context.MODE_PRIVATE))
                    throw new Exception();
                Intent intent = new Intent(context, Preview.class);
                intent.putExtra(Auxiliar.id, id);
                intent.putExtra(Auxiliar.previa, Auxiliar.notificacion);
                NotificationCompat.Builder builder;
                int iconoTarea;
                if((iconoTarea = Auxiliar.iconoTipoTarea(tipoRespuesta)) == 0)
                    iconoTarea = R.drawable.ic_11_tareas;
                //Bitmap iconoGrande = BitmapFactory.decodeResource(getApplicationContext().getResources(), R.drawable.ic_brush_black_128_dp);
                builder = new NotificationCompat.Builder(context, Auxiliar.channelId)
                        .setSmallIcon(R.drawable.ic_launcher_foreground)
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setContentTitle(getString(R.string.nuevaTarea))
                        .setStyle(new NotificationCompat.BigTextStyle().bigText(jsonObject.getString(Auxiliar.recursoAsociadoTexto)))
                        .setContentText(jsonObject.getString(Auxiliar.recursoAsociadoTexto))
                        .setLargeIcon(iconoGrandeNotificacion(context.getResources().getDrawable(iconoTarea)));

                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                PendingIntent pendingIntent = PendingIntent.getActivity(context, incr, intent, PendingIntent.FLAG_CANCEL_CURRENT);

                builder.setContentIntent(pendingIntent);
                builder.setAutoCancel(true);
                //builder.setTimeoutAfter(tiempoNotificacion); No es necesario en este tipo de notificaciones

                //Acción de descartar la notificación
                Intent intentBoton = new Intent(context, RecepcionNotificaciones.class);
                intentBoton.setAction(Auxiliar.ahora_no);
                intentBoton.putExtra(Auxiliar.id, id);
                intentBoton.putExtra(Auxiliar.idNotificacion, incr);
                PendingIntent ahoraNoPending = PendingIntent.getBroadcast(context, incr + 999, intentBoton, PendingIntent.FLAG_UPDATE_CURRENT);
                //builder.addAction(R.drawable.ic_thumb_down_black_24dp, getString(R.string.ahoraNo), ahoraNoPending);
                builder.setDeleteIntent(ahoraNoPending);

                /*intentBoton.setAction("NUNCA_MAS");
                PendingIntent nuncaMasP = PendingIntent.getBroadcast(context, incr + 1000, intentBoton, PendingIntent.FLAG_UPDATE_CURRENT);
                builder.addAction(R.drawable.ic_delete_black_24dp, getString(R.string.nuncaMas), nuncaMasP);*/
                notificationManager.notify(incr, builder.build()); //Notificación lanzada

                long instanteUltimaNotif = new Date().getTime(); //Actualizamos el instante
                JSONObject j = new JSONObject();
                j.put(Auxiliar.id, idInstanteNotAuto);
                j.put(Auxiliar.instante, instanteUltimaNotif);
                PersistenciaDatos.reemplazaJSON(getApplication(), PersistenciaDatos.ficheroInstantes, j);
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
     * Método para transformar un xml en un bitmap y poder representarlo en la notificación. Código
     * obtenido de:
     * https://stackoverflow.com/questions/24389043/bitmapfactory-decoderesource-returns-null-for-shape-defined-in-xml-drawable
     *
     * @param drawable Recurso a representar
     * @return Recurso representable
     */
    private Bitmap iconoGrandeNotificacion(Drawable drawable) {
        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }

    /**
     * Inicializador del bucle que obtiene la posición
     */
    private void startLocation(){
        servicioIniciado = true;
        fusedLocationProviderClient
                .requestLocationUpdates(locationRequest, locationCallback,null);
    }

    /**
     * Detiene la tarea de recogida de posición
     */
    private void stopLocation(){
        servicioIniciado = false;
        fusedLocationProviderClient.removeLocationUpdates(locationCallback);
    }

    /**
     * Método para crear el servicio en primer plano
     */
    private void mantenServicio(){
        Intent intent = new Intent(this, Proceso.class);
        //Intent intent = new Intent(context, Maps.class);
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
                intervalo = sharedPreferences.getInt(key, 0);
                break;
            case Ajustes.NO_MOLESTAR_pref:
                noMolestar = sharedPreferences.getBoolean(key, false);
                if(noMolestar){
                    //stopLocation();
                    //terminaServicio();
                    new AlarmaProceso().cancelaAlarmaProceso(context);
                }
                break;
            default:
                break;
        }
    }

    /**
     * Método para saber si es fin de semana o si no lo es.
     *
     * @return Devuelve verdadero si es sábado o domingo y falso si es un día de diario
     */
    private boolean esFinde() {
        int dia = Calendar.getInstance().get(Calendar.DAY_OF_WEEK);
        return (dia == Calendar.SATURDAY || dia == Calendar.SUNDAY);
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
