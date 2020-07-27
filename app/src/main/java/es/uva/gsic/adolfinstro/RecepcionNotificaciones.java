package es.uva.gsic.adolfinstro;

import android.app.Application;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import org.json.JSONObject;

import java.util.Objects;

import es.uva.gsic.adolfinstro.auxiliar.Auxiliar;
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
        String idTarea = "", accion = intent.getAction();
        try {
            idTarea = Objects.requireNonNull(intent.getExtras()).getString(Auxiliar.id);
            NotificationManager notificationManager = (NotificationManager) context.
                    getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.cancel(intent.getExtras().getInt("idNotificacion"));
        }catch (Exception ef){
            //Saltará en los reincios, pero es lo esperado
        }
        assert accion != null;
        switch (accion) {
            case Auxiliar.nunca_mas:
                //Sacamos la tarea del fichero de tareas pendientes para pasarla a la lista negra
                try{
                    JSONObject tarea = PersistenciaDatos.obtenTarea((Application) context.getApplicationContext(),
                            PersistenciaDatos.ficheroNotificadas, idTarea);
                    assert tarea != null;
                    tarea.put(Auxiliar.fechaUltimaModificacion, Auxiliar.horaFechaActual());
                    PersistenciaDatos.guardaJSON((Application) context.getApplicationContext(),
                            PersistenciaDatos.ficheroTareasRechazadas,
                            tarea,
                            Context.MODE_PRIVATE);
                    tareaFirebase((Application) context.getApplicationContext(), "tareaRechazada", idTarea);
                }catch (Exception e){
                    e.printStackTrace();
                }
                break;
            case Auxiliar.ahora_no:
                try{
                    JSONObject tarea = PersistenciaDatos.obtenTarea(
                            (Application) context.getApplicationContext(),
                            PersistenciaDatos.ficheroNotificadas,
                            idTarea);
                    tarea.put(Auxiliar.fechaUltimaModificacion, Auxiliar.horaFechaActual());
                    PersistenciaDatos.guardaJSON((Application) context.getApplicationContext(),
                            PersistenciaDatos.ficheroTareasPospuestas,
                            tarea,
                            Context.MODE_PRIVATE);
                    tareaFirebase((Application) context.getApplicationContext(), "tareaPospuesta", idTarea);
                } catch (Exception e){
                    e.printStackTrace();
                }
                break;
            case Intent.ACTION_BOOT_COMPLETED:
                /*Intent servicioPermanente = new Intent(context, Proceso.class);
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                    context.startForegroundService(servicioPermanente);
                else
                    context.startService(servicioPermanente);*/
                new AlarmaProceso().activaAlarmaProceso(context);
                break;
            default:
                break;
            }

    }

    private void tareaFirebase(Application app, String evento, String idTarea){
        Bundle bundle;
        try {
            bundle = new Bundle();
            bundle.putString("idTarea", Auxiliar.idReducida(idTarea));
            bundle.putString("idUsuario", Login.firebaseAuth.getUid());
            Login.firebaseAnalytics.logEvent(evento, bundle);
        } catch (Exception e) {
            try{
                bundle = new Bundle();
                bundle.putString("idTarea", Auxiliar.idReducida(idTarea));
                JSONObject usuario = PersistenciaDatos.
                        recuperaTarea(app, PersistenciaDatos.ficheroUsuario, Auxiliar.id);
                assert usuario != null;
                bundle.putString("idUsuario", usuario.getString(Auxiliar.uid));
                Login.firebaseAnalytics.logEvent(evento, bundle);
            }catch (Exception e1){
                e.printStackTrace();
            }
        }
    }
}
