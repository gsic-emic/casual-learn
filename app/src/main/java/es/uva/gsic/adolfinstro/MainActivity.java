package es.uva.gsic.adolfinstro;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.ArrayList;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity{

    private Button bt;
    private TextView tv;
    private static final int requestCodePermissions = 1000;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationCallback locationCallback;
    private LocationRequest locationRequest;
    private int contador = 0;
    private NotificationChannel channel;
    NotificationManager notificationmanager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bt = findViewById(R.id.btObtener);
        tv = findViewById(R.id.tvTexto);
        checkPermissions();
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        locationRequest = new LocationRequest().create().setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY).setInterval(60000).setFastestInterval(10000);
        //NotificationChannel channel;
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            CharSequence name = "100";
            String description = "100";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            channel = new NotificationChannel("100", name, importance);
            channel.setDescription(description);
            notificationmanager = getSystemService(NotificationManager.class);
            notificationmanager.createNotificationChannel(channel);
        }
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult){
                if(locationResult == null){
                    return;
                }
                for(Location location :locationResult.getLocations()){

                    /*NotificationCompat.Builder builder = new NotificationCompat.Builder(getBaseContext(), "100")
                            .setContentTitle(String.format("%i",contador))
                            .setContentText(String.format("%d\nLongitud: %f\nLatitud: %f\nPrecisión: %f",contador, location.getLongitude(), location.getLatitude(), location.getAccuracy()))
                            .setPriority(NotificationCompat.PRIORITY_HIGH);*/
                    tv.setText(String.format("%d\nLongitud: %f\nLatitud: %f\nPrecisión: %f",contador, location.getLongitude(), location.getLatitude(), location.getAccuracy()));
                    ++contador;
                    pintaNotificacion(location);

                }
            }
        };
    }

    private void pintaNotificacion(Location location){
        if(location!=null){
            String l = "Longitud: "+location.getLongitude()+ " Latitud: "+location.getLatitude();
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "100")
                .setContentTitle("Hola2")
                .setContentText("Hola")
                .setPriority(NotificationCompat.PRIORITY_HIGH);
        notificationmanager.notify(100, builder.build());}
    }
    @Override
    protected void onResume(){
        super.onResume();
        startLocationUpdates();
    }

    @Override
    protected void onPause(){
        super.onPause();
        //stopLocationUpdates();
    }

    @Override
    protected void onStop(){
        super.onStop();
        //stopLocationUpdates();
    }

    private void startLocationUpdates(){
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, null);
    }

    private void stopLocationUpdates(){
        fusedLocationProviderClient.removeLocationUpdates(locationCallback);
    }

    /**
     * Método para comprobar si el usuario ha otorgado a la aplicación los permisos necesarios.
     * En la actualidad, solicita permisos de localización y cámara.
     */
    private void checkPermissions(){
        ArrayList<String> permisos = new ArrayList<>();
        if(!(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED))
            permisos.add(Manifest.permission.ACCESS_FINE_LOCATION);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
            if(!(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED))
                permisos.add(Manifest.permission.ACCESS_BACKGROUND_LOCATION);
        if(!(ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED))
            permisos.add(Manifest.permission.CAMERA);
        if (permisos.size()>0) //Evitamos hacer una petición con un array nulo
            ActivityCompat.requestPermissions(this, permisos.toArray(new String[permisos.size()]), requestCodePermissions);
    }

    /**
     * Método que devuelve el resultado de la solicitud de permisos.
     * @param requestCode Código de la petición de permismos.
     * @param permissions Permisos que se han solicitado.
     * @param grantResults Valor otorgado por el usuario al permiso.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults){
        for(int i : grantResults){
            if(i == -1){
                AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
                alertBuilder.setTitle(getString(R.string.permi));
                alertBuilder.setMessage(getString(R.string.permiM));
                alertBuilder.setPositiveButton(getString(R.string.acept), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        checkPermissions();
                    }
                });
                alertBuilder.setNegativeButton(getString(R.string.exi), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        System.exit(0);
                    }
                });
                alertBuilder.show();
                break;
            }
        }
    }
    static Location loca;
    public void btgetLocation(View view) {
        //Location loca;
        fusedLocationProviderClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if(location != null){
                    loca = location;
                }
            }
        });
        pintaNotificacion(loca);
    }


}
