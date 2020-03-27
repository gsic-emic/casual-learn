package es.uva.gsic.adolfinstro;

import android.app.Application;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import org.json.JSONObject;

import es.uva.gsic.adolfinstro.auxiliar.Auxiliar;
import es.uva.gsic.adolfinstro.persistencia.PersistenciaDatos;

public class RecepcionNotificaciones extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String idTarea = intent.getExtras().getString("id");
        String accion = intent.getAction();
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(intent.getExtras().getInt("idNotificacion"));
        JSONObject tarea;
        try {
            switch (accion) {
                case "NUNCA_MAS":{
                    tarea = PersistenciaDatos.recuperaTarea((Application) context.getApplicationContext(), PersistenciaDatos.ficheroRespuestas, idTarea);
                    tarea = PersistenciaDatos.generaJSON(idTarea, tarea.getString("tipoTarea"), EstadoTarea.RECHAZADA);
                    PersistenciaDatos.guardaJSON((Application) context.getApplicationContext(), PersistenciaDatos.ficheroRespuestas, tarea, Context.MODE_PRIVATE);
                    break;}
                case "AHORA_NO":{
                    tarea = PersistenciaDatos.recuperaTarea((Application) context.getApplicationContext(), PersistenciaDatos.ficheroRespuestas, idTarea);
                    tarea = PersistenciaDatos.generaJSON(idTarea, tarea.getString("tipoTarea"), EstadoTarea.RETRASA);
                    PersistenciaDatos.guardaJSON((Application) context.getApplicationContext(), PersistenciaDatos.ficheroRespuestas, tarea, Context.MODE_PRIVATE);
                    break;}
                default:
                    Toast.makeText(context, accion, Toast.LENGTH_SHORT).show();
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
