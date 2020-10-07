package es.uva.gsic.adolfinstro;

import android.app.Application;
import android.app.NotificationManager;
import android.app.backup.BackupManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import org.json.JSONObject;

import java.util.Objects;

import es.uva.gsic.adolfinstro.auxiliar.Auxiliar;
import es.uva.gsic.adolfinstro.persistencia.PersistenciaDatos;

/**
 * Clase que se encarga de recibir las notificaciones destinadas a la aplicación. Con ella se
 * gestina cuando el usuario pospone una tarea o la rechaza. También está encargada de activar
 * el servicio en segundo plano cuando el dispositivo se reinicia.
 *
 * @author Pablo
 * @version 20201006
 */
public class RecepcionNotificaciones extends BroadcastReceiver {

    /** Objeto donde se almacena el identificador único del usuario*/
    private String idUsuario;

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
        String idTarea = "", accion = intent.getAction();
        try {
            idTarea = Objects.requireNonNull(intent.getExtras()).getString(Auxiliar.id);
            NotificationManager notificationManager = (NotificationManager) context.
                    getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.cancel(intent.getExtras().getInt("idNotificacion"));
        }catch (Exception ef){
            //Saltará en los reincios, pero es lo esperado
        }
        try{
            idUsuario = Objects.requireNonNull(PersistenciaDatos.recuperaTarea(
                    (Application) context.getApplicationContext(),
                    PersistenciaDatos.ficheroUsuario,
                    Auxiliar.id)).getString(Auxiliar.uid);
        }catch (Exception e){
            idUsuario = null;
        }
        assert accion != null;
        switch (accion) {
            case Auxiliar.nunca_mas:
                //Sacamos la tarea del fichero de tareas pendientes para pasarla a la lista negra
                try{
                    JSONObject tarea = PersistenciaDatos.obtenTarea(
                            (Application) context.getApplicationContext(),
                            PersistenciaDatos.ficheroNotificadas,
                            idTarea,
                            idUsuario);
                    assert tarea != null;
                    tarea.put(Auxiliar.fechaUltimaModificacion, Auxiliar.horaFechaActual());
                    PersistenciaDatos.guardaJSON((Application) context.getApplicationContext(),
                            PersistenciaDatos.ficheroTareasRechazadas,
                            tarea,
                            Context.MODE_PRIVATE);
                    new BackupManager(context).dataChanged();
                    tareaFirebase("tareaRechazada", idTarea);
                }catch (Exception e){
                    e.printStackTrace();
                }
                break;
            case Auxiliar.ahora_no:
                try{
                    JSONObject tarea = PersistenciaDatos.obtenTarea(
                            (Application) context.getApplicationContext(),
                            PersistenciaDatos.ficheroNotificadas,
                            idTarea,
                            idUsuario);
                    assert tarea != null;
                    tarea.put(Auxiliar.fechaUltimaModificacion, Auxiliar.horaFechaActual());
                    PersistenciaDatos.guardaJSON((Application) context.getApplicationContext(),
                            PersistenciaDatos.ficheroTareasPospuestas,
                            tarea,
                            Context.MODE_PRIVATE);
                    new BackupManager(context).dataChanged();
                    tareaFirebase("tareaPospuesta", idTarea);
                } catch (Exception e){
                    e.printStackTrace();
                }
                break;
            case Intent.ACTION_BOOT_COMPLETED:
                new AlarmaProceso().activaAlarmaProceso(context);
                break;
            default:
                break;
            }

    }

    /**
     * Método para registrar cuando una tarea es pospuesta o rechazada
     * @param evento Llave donde se indica si la tarea se ha rechazada o pospuesto
     * @param idTarea Identificador de la tarea
     */
    private void tareaFirebase(String evento, String idTarea){
        Bundle bundle;
        if(idUsuario != null) {
            try {
                bundle = new Bundle();
                bundle.putString("idTarea", Auxiliar.idReducida(idTarea));
                bundle.putString("idUsuario", idUsuario);
                Login.firebaseAnalytics.logEvent(evento, bundle);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
