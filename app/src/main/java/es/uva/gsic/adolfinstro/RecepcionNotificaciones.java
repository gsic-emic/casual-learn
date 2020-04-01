package es.uva.gsic.adolfinstro;

import android.app.Application;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import org.json.JSONObject;

import es.uva.gsic.adolfinstro.persistencia.PersistenciaDatos;

/**
 * Clase que se encarga de recibir las notificaciones destinadas a la aplicación.
 *
 * @author GSIC
 */
public class RecepcionNotificaciones extends BroadcastReceiver {

    /**
     * Método que actua dependiendo de la acción que lo inicie. Para las notificaciones internas de
     * la aplicación como "NUNCA_MAS" y "AHORA_NO" modifica los ficheros de las tareas. Con
     * Intent.ACTION_BOOT_COMPLETED se realizan las acciones para reiniciar la aplicación cuando el
     * dispositivo se enciende
     *
     * @param context Contexto
     * @param intent Intent
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        String idTarea="", accion = intent.getAction();
        try {
            idTarea = intent.getExtras().getString("id");
            NotificationManager notificationManager = (NotificationManager) context.
                    getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.cancel(intent.getExtras().getInt("idNotificacion"));
        }catch (Exception ef){
            //Saltará en los reincios, pero es lo esperado
        }
            switch (accion) {
                case "NUNCA_MAS":
                    //Sacamos la tarea del fichero de tareas pendientes para pasarla a la lista negra
                    try{
                        PersistenciaDatos.obtenTarea((Application) context.getApplicationContext(),
                                PersistenciaDatos.ficheroRespuestas, idTarea);
                        PersistenciaDatos.guardaJSON((Application) context.getApplicationContext(),
                                PersistenciaDatos.ficheroTareasRechazadas,
                                new JSONObject().put("id", idTarea),
                                Context.MODE_PRIVATE);
                    }catch (Exception e){
                        Log.e("NUNCA_MAS", "El proceso ha lanzado una excepción");
                    }
                    break;
                case "AHORA_NO":
                    try{
                        JSONObject tarea = PersistenciaDatos.recuperaTarea(
                                (Application) context.getApplicationContext(),
                                PersistenciaDatos.ficheroRespuestas,
                                idTarea);
                        PersistenciaDatos.guardaJSON((Application) context.getApplicationContext(),
                                PersistenciaDatos.ficheroTareas,
                                tarea,
                                Context.MODE_PRIVATE);
                    } catch (Exception e){
                        Log.e("AHORA_NO", "El proceso ha lanzado una excepción");
                    }
                    break;
                case Intent.ACTION_BOOT_COMPLETED:
                    Intent servicioPermanente = new Intent(context, Proceso.class);
                    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                        context.startForegroundService(servicioPermanente);
                    else
                        context.startService(servicioPermanente);
                    break;
                default:
            }
    }
}
