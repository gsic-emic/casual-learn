package es.uva.gsic.adolfinstro;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

public class AlarmaProceso extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent){
        Intent servicioPermanente = new Intent(context, Proceso.class);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            context.startForegroundService(servicioPermanente);
        else
            context.startService(servicioPermanente);
    }
}
