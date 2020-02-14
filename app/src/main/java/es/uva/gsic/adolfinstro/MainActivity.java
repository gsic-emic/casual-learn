package es.uva.gsic.adolfinstro;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.preference.PreferenceManager;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener {

    private TextView tv, tv2;
    private static final int requestCodePermissions = 1000;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationRequest locationRequest;
    private NotificationChannel channel;
    private NotificationManager notificationManager;
    private LocationCallback locationCallback;
    private NotificationCompat.Builder builder;
    private int intervalo;
    private boolean noMolestar;
    private long ultimaMedida;
    private Date date;
    private HashMap<Integer, Integer> identificadorRealizada;//0 no contesta, 1 realizada, 2 rechazada


    /**
     * Método de creación. Se recogen las referencias a los objectos del layout y se inicializan alguno de los objetos
     * que estarán activos durante toda la sesión
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (identificadorRealizada==null)
            identificadorRealizada = new HashMap<>();

        checkPermissions(); //Compruebo los permisos antes de seguir
        tv = findViewById(R.id.tvLatitudLongitud);
        tv2 = findViewById(R.id.tvAjustes);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){ //Se necesita un canal para API 26 y superior
            channel = new NotificationChannel("100", "100", NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription("100");
            notificationManager = this.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
        //Se crea la notificación  SE NECESITA CAMBIAR PARA TENER MÁS DE UNA NOTIFICACIÓN DE LA APP
        builder = new NotificationCompat.Builder(this, "100")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setPriority(NotificationCompat.PRIORITY_HIGH);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);
        onSharedPreferenceChanged(sharedPreferences, Ajustes.INTERVALO_pref);
        onSharedPreferenceChanged(sharedPreferences, Ajustes.NO_MOLESTAR_pref);
    }

    /**
     * Método para comprobar si el usuario ha otorgado a la aplicación los permisos necesarios.
     * En la actualidad, solicita permisos de localización y cámara.
     */
    private void checkPermissions(){
        if(!getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA))
            System.exit(-1);
        ArrayList<String> permisos = new ArrayList<>();
        if(!(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED))
            permisos.add(Manifest.permission.ACCESS_FINE_LOCATION);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
            if(!(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED))
                permisos.add(Manifest.permission.ACCESS_BACKGROUND_LOCATION);
        if(!(ActivityCompat.checkSelfPermission(this, Manifest.permission.INTERNET) == PackageManager.PERMISSION_GRANTED))
            permisos.add(Manifest.permission.INTERNET);
        if(!(ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED))
            permisos.add(Manifest.permission.CAMERA);
        if(!(ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED))
            permisos.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if(!(ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED))
            permisos.add(Manifest.permission.RECORD_AUDIO);
        if (permisos.size()>0) //Evitamos hacer una petición con un array nulo
            ActivityCompat.requestPermissions(this, permisos.toArray(new String[permisos.size()]), requestCodePermissions);
        else{
            posicionamiento();
        }
    }

    /**
     * Método que devuelve el resultado de la solicitud de permisos.
     * @param requestCode Código de la petición de permismos.
     * @param permissions Permisos que se han solicitado.
     * @param grantResults Valor otorgado por el usuario al permiso.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NotNull String[] permissions, int[] grantResults){
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
        posicionamiento();
    }

    /**
     * Inicia los objetos necesarios para llevar a cabo el seguimiento de la posición
     */
    private void posicionamiento(){
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        locationRequest = new LocationRequest().create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(10000).setFastestInterval(2000);
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult){
                if(locationResult == null){
                    return;
                }
                for(Location location :locationResult.getLocations()){
                    compruebaLocalizacion(location);
                }
            }
        };
        startLocation();
    }

    final double plazaMayorLat = 41.6520;
    final double plazaMayorLong = -4.7286;
    final double laAntiguaLat = 41.6547;
    final double laAntiguaLong = -4.7231;
    final double castilloPLat = 41.5966;
    final double castilloPLong = -4.1144;
    final double plazaMayorSLat = 40.9650;
    final double plazaMayorSLong = -5.6641;

    private void compruebaLocalizacion(Location location) {
        double latitud = Math.round(location.getLatitude()*10000d)/10000d;
        double longitud = Math.round(location.getLongitude()*10000d)/10000d;
        String l = getString(R.string.latitud) + ": " + latitud + " || " +
                getString(R.string.longitud) + ": " + longitud;
        tv.setText(l);
        if(!noMolestar){
            Boolean comprueba = true;
            if(ultimaMedida == 0){
                date = new Date();
            }
            else{
                date = new Date();
                if(date.getTime() - ultimaMedida < intervalo * 20 * 1000){
                    comprueba = false;
                }
            }
            if(comprueba){
                ultimaMedida = date.getTime();
                int idTarea = -1;
                //AQUÍ SE REALIZARÁ LA PETECIÓN SPARQL --> TIENE QUE HACERSE EN UN SERVICIO
                if(plazaMayorLat == latitud && plazaMayorLong == longitud){ //Respuesta texto
                    idTarea = 0; //La tomará de la respuesta a la consulta
                    setIdentificadorTarea(idTarea);
                }else{
                    if(laAntiguaLat == latitud && laAntiguaLong == longitud){ // Una foto
                        idTarea = 1;
                        setIdentificadorTarea(idTarea);
                    }else{
                        if(castilloPLat == latitud && castilloPLong == longitud){ //Varias fotos
                            idTarea = 2;
                            setIdentificadorTarea(idTarea);
                        }else{
                            if(plazaMayorSLat == latitud && plazaMayorSLong == longitud) { // Un vídeo
                            idTarea = 3;
                            setIdentificadorTarea(idTarea);
                            }else{
                                return;
                            }
                        }
                    }
                }
                if(idTarea != -1){
                    if(getEstado(idTarea)==0){
                        pintaNotificacion(location, idTarea);
                    }
                }
            }
        }
    }

    private void setIdentificadorTarea(int idTarea){
        synchronized (identificadorRealizada){
            if(identificadorRealizada.get(idTarea) == null){
                identificadorRealizada.put(idTarea, 0);
            }
        }
    }

    private int getEstado(int idTarea){
        int salida;
        synchronized (identificadorRealizada){
            salida = identificadorRealizada.get(idTarea);
        }
        return salida;
    }

    /**
     * Inicializador del bucle que obtiene la posición
     */
    private void startLocation(){
        fusedLocationProviderClient
                .requestLocationUpdates(locationRequest, locationCallback,null);
    }

    /**
     * Detiene la actulización de la posición
     */
    private void stopLocation(){
        fusedLocationProviderClient.removeLocationUpdates(locationCallback);
    }

    /**
     * Método que lanza la notificación y actualiza el valor del TextView
     * @param location Última posición obtenida
     * @param idTarea Identificador de la tarea asociada a la notificación
     */
    private void pintaNotificacion(Location location, int idTarea) {
        if (location != null) {
            Log.i("pintaNoficacion", "Pinto notificación de la tarea" + idTarea);
            String l = getString(R.string.latitud) + ": " + location.getLatitude() + " || " +
                    getString(R.string.longitud) + ": " + location.getLongitude();
            String titu = "";
            Intent intent = null;
            switch (idTarea){
                case 0:
                    titu = "Tarea de pregunta";
                    intent = new Intent(this, Ask.class);
                    break;
                case 1:
                    titu = "Tarea de una foto";
                    intent = new Intent(this, TaskCamera.class);
                    intent.putExtra("TIPO", R.id.btUnaFoto);
                    break;
                case 2:
                    titu = "Tarea de varias fotos";
                    intent = new Intent(this, TaskCamera.class);
                    intent.putExtra("TIPO", R.id.btVariasFotos);
                    break;
                case 3:
                    titu = "Tarea de un vídeo";
                    intent = new Intent(this, TaskCamera.class);
                    intent.putExtra("TIPO", R.id.btVideo);
                    break;
            }
            builder.setContentTitle(titu).setContentText(l);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);
            builder.setContentIntent(pendingIntent);
            builder.setAutoCancel(true);
            notificationManager.notify( idTarea, builder.build());
        }
    }

    @Override
    protected void onResume(){
        super.onResume();
        startLocation();
    }

    @Override
    protected void onPause(){
        super.onPause();
        stopLocation();
    }

    @Override
    protected void onStop(){
        super.onStop();
        stopLocation();
    }

    /**
     * Método de escucha de los botones de prueba
     * @param view
     */
    public void boton(View view){
        Intent intent;
        switch (view.getId()){
            case R.id.btTexto:
                intent = new Intent(this, Ask.class);
                break;
            case R.id.btUnaFoto:
            case R.id.btVariasFotos:
            case R.id.btVideo:
                intent = new Intent(this, TaskCamera.class);
                intent.putExtra("TIPO", view.getId());
                break;
            default:
                System.exit(-2);
                intent = null;
        }
        startActivity(intent);
    }

    /**
     * Creación del menú en el layout
     * @param menu
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    /**
     * Método que reacciona a la pulsación de alguno de los items del menú
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()){
            case R.id.ajustes:
                startActivity(new Intent(this, Ajustes.class));
                return true;
            case R.id.acerca:
                Toast.makeText(this, getString(R.string.gsic), Toast.LENGTH_LONG).show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Called when a shared preference is changed, added, or removed. This
     * may be called even if a preference is set to its existing value.
     * @param sharedPreferences
     * @param key
     */
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        switch (key){
            case Ajustes.INTERVALO_pref:
                intervalo = Integer.parseInt(sharedPreferences.getString(key, "3"));
                break;
            case Ajustes.NO_MOLESTAR_pref:
                noMolestar = sharedPreferences.getBoolean(key, false);
                break;
        }
        String m = "Intervalo: " + intervalo + " No Molestar: " + (noMolestar?"ON":"OFF");
        tv2.setText(m);
    }
}
