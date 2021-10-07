package es.uva.gsic.adolfinstro;

import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.preference.PreferenceManager;

import org.json.JSONArray;
import org.json.JSONObject;

import es.uva.gsic.adolfinstro.auxiliar.Auxiliar;
import es.uva.gsic.adolfinstro.persistencia.PersistenciaDatos;

/**
 * Clase que comprueba si existen respuestas del usuario sin enviar. Si existen, y las preferencias
 * del usuario lo permiten, se envían al servidor.
 *
 * @author Pablo
 * @version 20210111
 */
public class CompruebaEnvios extends BroadcastReceiver {

    /** Objeto donde se almacena la preferencia del usuario */
    private boolean enviaWifi;

    /** Método para inicializar los objetos necesarios para comprobar si existen respuestas del usuario
     * sin enviar.
     * @param context Contexto
     * @param intent Intent
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        Application application = (Application) context.getApplicationContext();
        enviaWifi = PreferenceManager.getDefaultSharedPreferences(context).getBoolean(Ajustes.WIFI_pref, false);
        compruebaRespuestasSinEnviar(application, context);
    }

    /**
     * Método para comprobar si existen respuestas del usuario sin enviar. Si existen se envían. Si no
     * existen se elimina el proceso de comprobación.
     * @param application Aplicación
     * @param context Contexto
     */
    private void compruebaRespuestasSinEnviar(Application application, Context context) {
        int tipoConectividad = Auxiliar.tipoConectividad(context);

        JSONArray respuestasPendientes = PersistenciaDatos.leeFichero(application, PersistenciaDatos.ficheroSinEnviar);

        if(respuestasPendientes.length() != 0) {
            if (tipoConectividad == 0 || (tipoConectividad == 1 && !enviaWifi)) {
                if (respuestasPendientes.length() > 0) {
                    JSONObject tareaPendiente;
                    for (int i = 0; i < respuestasPendientes.length(); i++) {
                        try {
                            tareaPendiente = respuestasPendientes.getJSONObject(i);
                            Auxiliar.enviaResultados(application, context, tareaPendiente.getString(Auxiliar.id));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    respuestasPendientes = new JSONArray();
                    PersistenciaDatos.guardaFichero(application,
                            PersistenciaDatos.ficheroSinEnviar,
                            respuestasPendientes,
                            Context.MODE_PRIVATE);
                }
            }
        } else
            cancelaComrpuebaEnvios(context);
    }

    /**
     * Método para activar la comprobación de la existencia de respuestas sin enviar.
     * @param context Contexto
     */
    public void activaCompruebaEnvios(Context context){
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if(alarmManager != null) {
            cancelaComrpuebaEnvios(context);
            //10 minutos entre cada comprobación
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                alarmManager.setRepeating(
                        AlarmManager.ELAPSED_REALTIME_WAKEUP,
                        5000,
                        600000,
                        PendingIntent.getBroadcast(
                                context,
                                9985,
                                new Intent(context, CompruebaEnvios.class),
                                PendingIntent.FLAG_IMMUTABLE));
            } else {
                alarmManager.setRepeating(
                        AlarmManager.ELAPSED_REALTIME_WAKEUP,
                        5000,
                        600000,
                        PendingIntent.getBroadcast(
                                context,
                                9985,
                                new Intent(context, CompruebaEnvios.class),
                                0));
            }
        }
    }

    /**
     * Método para cancelar la comprobación de la existencia de respuestas sin enviar.
     *
     * @param context Contexto
     */
    public void cancelaComrpuebaEnvios(Context context){
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if(alarmManager != null)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                alarmManager.cancel(
                        PendingIntent.getBroadcast(context,
                                9985,
                                new Intent(context, CompruebaEnvios.class),
                                PendingIntent.FLAG_IMMUTABLE));
            } else {
                alarmManager.cancel(
                        PendingIntent.getBroadcast(context,
                                9985,
                                new Intent(context, CompruebaEnvios.class),
                                0));
            }
    }
}
