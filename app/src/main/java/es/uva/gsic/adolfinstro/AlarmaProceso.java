package es.uva.gsic.adolfinstro;

import android.Manifest;
import android.app.AlarmManager;
import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
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
import android.os.Build;
import android.os.Bundle;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.preference.PreferenceManager;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;

import es.uva.gsic.adolfinstro.auxiliar.Auxiliar;
import es.uva.gsic.adolfinstro.auxiliar.ColaConexiones;
import es.uva.gsic.adolfinstro.persistencia.PersistenciaDatos;

/**
 * Clase encargada de gestionar la comprobación de la posición del usuario y de proponerle tareas si
 * se cumplen una serie de circustancias.
 *
 * @author Pablo
 * @version 20200911
 */
public class AlarmaProceso extends BroadcastReceiver implements SharedPreferences.OnSharedPreferenceChangeListener {
    /** Contexto */
    Context context;

    Application application;

    private NotificationChannel channel;
    private NotificationManager notificationManager;
    private int intervalo;
    private boolean noMolestar;
    private String idInstanteGET = "instanteGET";
    private String idUltimaPosicion = "ultimaPosicionAlarma";
    private String idInstanteNotAuto = "instanteNotAuto";

    private final int intervaloComprobacion = 120000;

    public static boolean tareasActualizadas = false;

    LocationManager locationManager;

    LocationListener locationListener;


    /**
     * Acciones que se realizarán cuando se recibe la notificación de la alarma
     * @param context Contexto
     * @param intent Intent
     */
    @Override
    public void onReceive(Context context, Intent intent){
        this.context = context;
        application = (Application) context.getApplicationContext();

        ArrayList<String> permisos = Auxiliar.preQueryPermisos(context);
        if (permisos.size() > 0) { // Si se le han revocado permisos a la aplicación se mata el proceso
            cancelaAlarmaProceso(context);
        }

        //Se necesita un canal para API 26 y superior
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            channel = new NotificationChannel(Auxiliar.channelId,
                    context.getString(R.string.canalTareas),
                    NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription(context.getString(R.string.canalTareas));
            notificationManager = context.getSystemService(NotificationManager.class);
            assert notificationManager != null;
            notificationManager.createNotificationChannel(channel);
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                notificationManager = context.getSystemService(NotificationManager.class);
            else {//API 22
                notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            }
        }

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);
        onSharedPreferenceChanged(sharedPreferences, Ajustes.INTERVALO_pref);
        onSharedPreferenceChanged(sharedPreferences, Ajustes.NO_MOLESTAR_pref);
        posicionamiento();
    }

    /**
     * Método para activar la alarma que se repite coda intervaloComprobacion milisegundos.
     * @param context Contexto
     */
    public void activaAlarmaProceso(Context context){
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if(alarmManager != null)
            alarmManager.setRepeating(
                    AlarmManager.ELAPSED_REALTIME_WAKEUP,
                    10000,
                    intervaloComprobacion,
                    PendingIntent.getBroadcast(
                                        context,
                            9995,
                            new Intent(context, AlarmaProceso.class),
                    0));
    }

    /**
     * Método para cancelar la alarma con repeticón.
     *
     * @param context Contexto
     */
    public void cancelaAlarmaProceso(Context context){
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if(alarmManager != null)
            alarmManager.cancel(
                    PendingIntent.getBroadcast(context,
                    9995,
                    new Intent(context, AlarmaProceso.class),
                    0));
    }

    /**
     * Método para recuperar las prefrencias del usuario.
     *
     * @param sharedPreferences Preferencia
     * @param key Llave
     */
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        switch (key){
            case Ajustes.INTERVALO_pref:
                intervalo = sharedPreferences.getInt(key, 5);
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
     * Método para obtener la posción del usuario.
     */
    private void posicionamiento() {
        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        JSONObject idUsuario = PersistenciaDatos.
                recuperaTarea(application, PersistenciaDatos.ficheroUsuario, Auxiliar.id);
        if (
                ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED
                &&
                ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED
        ) {
            cancelaAlarmaProceso(context);
        }else {
            if (idUsuario != null) { //Compruebo la posición únicamente si el usuario está identificado
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                        0, 0, locationListener = new LocationListener() {
                            @Override
                            public void onLocationChanged(Location location) {
                                //Fundamental eliminar la actualización antes de continuar para que
                                // no entre más de una vez en la comprobación
                                locationManager.removeUpdates(locationListener);
                                compruebaTareas(location);
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
            }
            else //Si el usuario no está identificado cancelo la alarma
                cancelaAlarmaProceso(context);
        }
    }

    /**
     * Método para comprobar si existe alguna tarea válida para notificar al usuario
     *
     * @param location Localización actual del usuario
     */
    private void compruebaTareas(Location location) {
        //Latitiud desde donde se han recuperado las tareas del servidor
        double latitudGet = 0;
        //Longitud desde donde se han recuperado las tareas del servidor
        double longitudGet = 0;
        long momento = 0;
        double latitud = location.getLatitude();
        double longitud = location.getLongitude();
        //Se comprueba si existe el fichero y el objeto
        if(PersistenciaDatos.existeTarea(
                application,
                PersistenciaDatos.ficheroInstantes,
                idInstanteGET)){
            try {
                JSONObject instante = PersistenciaDatos.recuperaTarea(
                        application,
                        PersistenciaDatos.ficheroInstantes,
                        idInstanteGET);
                latitudGet = instante.getDouble(Auxiliar.latitud);
                longitudGet = instante.getDouble(Auxiliar.longitud);
                momento = instante.getLong(Auxiliar.instante);
            }catch (Exception e){
                e.printStackTrace();
            }
        }

        //Inicio del servicio, se tiene que recuperar la tarea del servidor
        //Validez de un día para las tareas
        JSONObject idUsuario = PersistenciaDatos.recuperaTarea(
                application, PersistenciaDatos.ficheroUsuario, Auxiliar.id);
        if(idUsuario != null) {
            if (latitudGet == 0 || longitudGet == 0 || ((new Date().getTime() - momento) > 7200000)) {
                peticionTareasServidor(location);
            } else {
                double distanciaOrigen = Auxiliar.calculaDistanciaDosPuntos(
                        latitud, longitud,
                        latitudGet, longitudGet);
                if (distanciaOrigen >= 0.5) {
                    //Las tareas en local están obsoletas, hay que pedir unas nuevas al servidor
                    peticionTareasServidor(location);
                } else {//El fichero sigue siendo válido
                    compruebaLocalizacion(location);
                }
            }
        }
    }

    /**
     * Método para realizar la petición de tareas al servidor
     * @param location Punto del que se extraerá la latitud y la longitud
     */
    private void peticionTareasServidor(final Location location){
        String idUsuario = null;
        try{
            JSONObject usuario = PersistenciaDatos.recuperaTarea(
                    application, PersistenciaDatos.ficheroUsuario, Auxiliar.id);
            idUsuario = usuario.getString(Auxiliar.uid);
        }catch (Exception e){
            e.printStackTrace();
        }
        String url = Auxiliar.direccionIP +
                "tareas?norte=" + (location.getLatitude() + 0.00325) +
                "&este=" + (location.getLongitude() + 0.00325) +
                "&sur=" + (location.getLatitude() - 0.00325) +
                "&oeste=" + (location.getLongitude() - 0.00325)
                +((idUsuario == null)?"":"&id=" + idUsuario);
        JsonArrayRequest jsonObjectRequest = new JsonArrayRequest(Request.Method.GET, url,
                null, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                JSONArray nuevasTaras = new JSONArray();
                JSONObject jsonObject;
                boolean guarda;
                for(int i = 0; i < response.length(); i++){
                    try {
                        jsonObject = response.getJSONObject(i);
                        if(Auxiliar.tareaRegistrada(
                                application,
                                jsonObject.getString(Auxiliar.id))){
                            if(PersistenciaDatos.existeTarea(
                                    application,
                                    PersistenciaDatos.ficheroNotificadas,
                                    jsonObject.getString(Auxiliar.id))){
                                try {
                                    PersistenciaDatos.obtenTarea(
                                            application,
                                            PersistenciaDatos.ficheroNotificadas,
                                            jsonObject.getString(Auxiliar.id));
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
                            nuevasTaras.put(jsonObject);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                PersistenciaDatos.guardaFichero(
                        application,
                        PersistenciaDatos.ficheroTareasUsuario,
                        nuevasTaras,
                        Context.MODE_PRIVATE);
                try {
                    jsonObject = new JSONObject();
                    jsonObject.put(Auxiliar.id, idInstanteGET);
                    jsonObject.put(Auxiliar.latitud, location.getLatitude());
                    jsonObject.put(Auxiliar.longitud, location.getLongitude());
                    jsonObject.put(Auxiliar.instante, new Date().getTime());
                    PersistenciaDatos.reemplazaJSON(
                            application,
                            PersistenciaDatos.ficheroInstantes,
                            jsonObject);
                }catch (JSONException e){
                    e.printStackTrace();
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
        ColaConexiones.getInstance(application).getRequestQueue().add(jsonObjectRequest);
    }

    /**
     * Método para comprobar si el usuario puede realizar alguna tarea de la zona en la que se
     * encuentre
     *
     * @param location Localización del usuario
     */
    private void compruebaLocalizacion(Location location) {
        double distanciaAndada=1200, latitud=0, longitud=0,latitudAnt, longitudAnt;
        boolean datosValidos = false;

        if(PersistenciaDatos.existeTarea(
                application,
                PersistenciaDatos.ficheroPosicion,
                idUltimaPosicion)){
            try{
                JSONObject jsonObject = PersistenciaDatos.obtenTarea(
                        application,
                        PersistenciaDatos.ficheroPosicion,
                        idUltimaPosicion);
                latitudAnt = jsonObject.getDouble(Auxiliar.latitud);
                longitudAnt = jsonObject.getDouble(Auxiliar.longitud);
                datosValidos = true;
            }catch (Exception e){
                latitudAnt = location.getLatitude();
                longitudAnt = location.getLongitude();
            }
            latitud = location.getLatitude();
            longitud = location.getLongitude();
            distanciaAndada = Auxiliar.calculaDistanciaDosPuntos(
                    latitudAnt, longitudAnt,
                    latitud, longitud);
        }
        try{
            //Última posición. Se guarda en un fichero porque el proceso se va a destruir
            JSONObject jsonObject = new JSONObject();
            jsonObject.put(Auxiliar.latitud, latitud);
            jsonObject.put(Auxiliar.longitud, longitud);
            jsonObject.put(Auxiliar.id, idUltimaPosicion);
            PersistenciaDatos.reemplazaJSON(
                    application,
                    PersistenciaDatos.ficheroPosicion,
                    jsonObject);
        }catch (JSONException e){
            e.printStackTrace();
        }


        long instanteUltimaNotif;

        try {
            JSONObject instante = PersistenciaDatos.recuperaTarea(
                    application,
                    PersistenciaDatos.ficheroInstantes,
                    idInstanteNotAuto);
            instanteUltimaNotif = instante.getLong(Auxiliar.instante);
        }catch (Exception e){
            instanteUltimaNotif = 0;
        }

        boolean comprueba = (new Date().getTime()) >= instanteUltimaNotif + (
                (Auxiliar.intervaloMinutos(intervalo) > 0)?
                        Auxiliar.intervaloMinutos(intervalo)*60*1000:
                        20000);
        JSONObject idUsuario = PersistenciaDatos.recuperaTarea(
                application, PersistenciaDatos.ficheroUsuario, Auxiliar.id);
        if(idUsuario != null) {
            if (comprueba) {//Se comprueba cuando se ha lanzado la última notificación
                if (datosValidos) {//Se comprueba si los datos son válidos (inicio proceso)
                    /* Distancia máxima que podría andar en el intervalo de comprobación*/
                    double maxAndado = (5 * ((double) intervaloComprobacion / 1000) / 3600);
                    if (distanciaAndada <= maxAndado) {//Se comprueba si el usuario está caminando
                        //Comprobación de la ubucación actual a las tareas almacenadas
                        JSONObject tarea = Auxiliar.tareaMasCercana(application, latitud, longitud);
                        //Se obtiene la distancia más baja a la tarea
                        if (tarea != null) {
                            double distancia;
                            try {
                                distancia = Auxiliar.calculaDistanciaDosPuntos(
                                        latitud, longitud,
                                        tarea.getDouble(Auxiliar.latitud), tarea.getDouble(Auxiliar.longitud));
                            } catch (JSONException je) {
                                distancia = 10;
                            }
                            if (distancia < 0.15) {//Si el usuario está lo suficientemente cerca, se le envía una notificación
                                try {
                                    //Se extrae la tarea para que no se le vuelva a ofrecer
                                    PersistenciaDatos.obtenTarea(
                                            application,
                                            PersistenciaDatos.ficheroTareasUsuario,
                                            tarea.getString(Auxiliar.id));
                                    pintaNotificacion(tarea);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Método para notificar al usuario mediante una notificación del sistema
     *
     * @param jsonObject Tarea que se va a notificar
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
                if(!PersistenciaDatos.guardaJSON(
                        application,
                        PersistenciaDatos.ficheroNotificadas,
                        jsonObject,
                        Context.MODE_PRIVATE))
                    throw new Exception();
                Intent intent = new Intent(context, Preview.class);
                intent.putExtra(Auxiliar.id, id);
                intent.putExtra(Auxiliar.previa, Auxiliar.notificacion);
                NotificationCompat.Builder builder;
                int iconoTarea;
                if((iconoTarea = Auxiliar.iconoTipoTarea(tipoRespuesta)) == 0)
                    iconoTarea = R.drawable.ic_11_tareas;
                //Elimino los enlaces
                String textoTarea = jsonObject.getString(Auxiliar.recursoAsociadoTexto)
                        .replaceAll("</a>", "")
                        .replaceAll("<a.*?>","");

                String titulo = String.format("%s %s!", context.getString(R.string.nuevaTarea), jsonObject.getString(Auxiliar.titulo));

                builder = new NotificationCompat.Builder(context, Auxiliar.channelId)
                        .setSmallIcon(R.drawable.ic_walk_white)
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setContentTitle(titulo)
                        .setStyle(new NotificationCompat.BigTextStyle().bigText(textoTarea))
                        .setContentText(textoTarea)
                        .setLargeIcon(iconoGrandeNotificacion(context.getResources().getDrawable(iconoTarea)));

                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                PendingIntent pendingIntent = PendingIntent.getActivity(
                        context,
                        Auxiliar.incr,
                        intent,
                        PendingIntent.FLAG_CANCEL_CURRENT);

                builder.setContentIntent(pendingIntent);
                builder.setAutoCancel(true);
                //builder.setTimeoutAfter(tiempoNotificacion); No es necesario en este tipo de notificaciones

                //Acción de descartar la notificación
                Intent intentBoton = new Intent(context, RecepcionNotificaciones.class);
                intentBoton.setAction(Auxiliar.ahora_no);
                intentBoton.putExtra(Auxiliar.id, id);
                intentBoton.putExtra(Auxiliar.idNotificacion, Auxiliar.incr);
                PendingIntent ahoraNoPending = PendingIntent.getBroadcast(
                        context,
                        Auxiliar.incr + 999,
                        intentBoton,
                        PendingIntent.FLAG_UPDATE_CURRENT);
                //builder.addAction(R.drawable.ic_thumb_down_black_24dp, getString(R.string.ahoraNo), ahoraNoPending);
                builder.setDeleteIntent(ahoraNoPending);

                /*intentBoton.setAction("NUNCA_MAS");
                PendingIntent nuncaMasP = PendingIntent.getBroadcast(context, incr + 1000, intentBoton, PendingIntent.FLAG_UPDATE_CURRENT);
                builder.addAction(R.drawable.ic_delete_black_24dp, getString(R.string.nuncaMas), nuncaMasP);*/
                notificationManager.notify(Auxiliar.incr, builder.build()); //Notificación lanzada

                long instanteUltimaNotif = new Date().getTime(); //Actualizamos el instante
                JSONObject j = new JSONObject();
                j.put(Auxiliar.id, idInstanteNotAuto);
                j.put(Auxiliar.instante, instanteUltimaNotif);
                PersistenciaDatos.reemplazaJSON(application, PersistenciaDatos.ficheroInstantes, j);
                ++Auxiliar.incr; //Para que no tengan dos notificaciones el mismo valor
            }catch (Exception e){
                e.printStackTrace();
            }
            //db.grupoTareasDao().insertTarea(tarea);//Guardo en la base de datos
        }catch (JSONException je){
            //Si alguno de los campos que siempre deberían existir no existen
        }
    }

    /**
     * Método para obtener un Bitmap a partir de un drawable. Se utiliza para colocar un icono en la
     * notificación de la tarea.
     *
     * @param drawable Icono en formato no compatible con la notificación
     * @return Icono compatible con la notificación
     */
    private Bitmap iconoGrandeNotificacion(Drawable drawable) {
        Bitmap bitmap = Bitmap.createBitmap(
                drawable.getIntrinsicWidth(),
                drawable.getIntrinsicHeight(),
                Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }
}
