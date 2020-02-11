package es.uva.gsic.adolfinstro;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.location.Location;
import android.os.Build;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;


/************************************ NO FUNCIONA *************************************************/
public class Proceso extends Worker {

    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationCallback locationCallback;
    private LocationRequest locationRequest;
    private int contador = 0;
    private NotificationChannel channel;
    NotificationManager notificationmanager;
    private Context context;

    public Proceso(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
        this.context = context;
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context);
        locationRequest = new LocationRequest().create().setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY).setNumUpdates(1).setInterval(1);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            CharSequence name = "100";
            String description = "100";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            channel = new NotificationChannel("100", name, importance);
            channel.setDescription(description);
            notificationmanager = context.getSystemService(NotificationManager.class);
            notificationmanager.createNotificationChannel(channel);
        }
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult){
                if(locationResult == null){
                    return;
                }
                for(Location location :locationResult.getLocations()){
                    ++contador;
                    pintaNotificacion(location);

                }
            }
        };
    }

    private void startLocationUpdates(){
        Looper.prepare();
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, null);
    }

    private void stopLocationUpdates(){
        fusedLocationProviderClient.removeLocationUpdates(locationCallback);
    }

    private void pintaNotificacion(Location location) {
        Log.i("Pinta", "Inicio");
        if (location != null) {
            Log.i("location", "!= null");
            String l = "Latitud: " + location.getLatitude() + " Longitud: " + location.getLongitude();
            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "100")
                    .setSmallIcon(R.drawable.ic_launcher_foreground)
                    .setContentTitle(String.format("%d",contador))
                    .setContentText(l)
                    .setPriority(NotificationCompat.PRIORITY_HIGH);
            notificationmanager.notify(100, builder.build());
        }
        Log.i("Pinta", "Fin");
        startLocationUpdates();
    }

    @Override
    public Result doWork(){
        //startLocationUpdates();
        while(!(fusedLocationProviderClient.getLastLocation().isSuccessful() && fusedLocationProviderClient.getLastLocation().getResult() != null));
        pintaNotificacion(fusedLocationProviderClient.getLastLocation().getResult());
        //stopLocationUpdates();
        return Result.success();
    }

}
