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
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.osmdroid.api.IGeoPoint;
import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.events.DelayedMapListener;
import org.osmdroid.events.MapListener;
import org.osmdroid.events.ScrollEvent;
import org.osmdroid.events.ZoomEvent;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.CopyrightOverlay;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.ScaleBarOverlay;
import org.osmdroid.views.overlay.compass.CompassOverlay;
import org.osmdroid.views.overlay.compass.InternalCompassOrientationProvider;
import org.osmdroid.views.overlay.gestures.RotationGestureOverlay;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;
import org.osmdroid.views.overlay.simplefastpoint.LabelledGeoPoint;


import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import es.uva.gsic.adolfinstro.auxiliar.Auxiliar;
import es.uva.gsic.adolfinstro.persistencia.PersistenciaDatos;

//https://github.com/osmdroid/osmdroid/wiki/How-to-use-the-osmdroid-library
public class Maps extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener, LocationListener {
    /** Objeto que permite mostrar el mapa*/
    private MapView map;
    /** Objeto que almacenará, entre otras cosas, la última posición conocida del usuario*/
    private MyLocationNewOverlay myLocationNewOverlay;
    /** Objeto tuilizado para centrar el mapa en un punto específico*/
    private IMapController mapController;
    /** Posición inicial del punto conocido */
    private double latitude = 41.662357, longitude = -4.706005;
    /** Punto del mapa en el que se centrará si no consigue recuperar la posición actual*/
    private final GeoPoint telecoPoint = new GeoPoint(latitude, longitude);
    /** Referencia a si la opción "no Molestar" está activada o no*/
    private boolean noMolestar;
    /** Preferencias de la aplicación */
    private SharedPreferences sharedPreferences;
    /** Vector donde se almacena los puntos representados en el mapa */
    //private ArrayList<OverlayItem> items = new ArrayList<>();
    private List<IGeoPoint> items = new ArrayList<>();
    private HashMap<String, Integer> itemsTask = new HashMap<>();
    /** Vector con los id's de los puntos. Por ahora el id es el título del punto */
    private ArrayList<String> idItems = new ArrayList<>();
    /** Regla sbore el mapa*/
    private ScaleBarOverlay scaleBarOverlay;
    /** Brújula del dispositivo*/
    private CompassOverlay compassOverlay;
    /** Código de identificación para la solicitud de los permisos de la app */
    private final int requestCodePermissions = 1001;
    /** Distancia (en km) por la que se recarga el fichero con tareas */
    private final double radioMaxTareas = 0.75;

    /** Canal utilizado para las notificaciones de las tareas */
    private NotificationChannel channel;
    /** Instancia del NotificationManager*/
    private NotificationManager notificationManager;

    private int incr = 2000;

    private Context context;

    private long ultimaNotificacion;

    private boolean primerosPuntos;

    private String idUltimaPosicionMapa = "ultimaPosicionMapa";
    private String idinstanteUltimaNoti = "ultimaNoti";

    /*private long ultimaPosicionInstante;
    private double ultimaPosicionLatitud;
    private double ultimaPosicionLogintud;*/



    /**
     * Método con el que se pinta la actividad. Lo primero que comprueba es si está activada el modo no
     * molestar para saber si se tiene que mostar el mapa o no
     * @param savedInstanceState Bundle
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        context = getApplicationContext(); //contexto de la aplicación
        Configuration.getInstance().load(context, PreferenceManager.getDefaultSharedPreferences(context));
        Configuration.getInstance().setUserAgentValue(BuildConfig.APPLICATION_ID);
        super.onCreate(savedInstanceState);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        onSharedPreferenceChanged(sharedPreferences, Ajustes.NO_MOLESTAR_pref);
        onSharedPreferenceChanged(sharedPreferences, Ajustes.LISTABLANCA_pref);
        checkPermissions();

        //Se decide si se muestra el mapa
        if (noMolestar) {
            setContentView(R.layout.no_molestar);
        } else {
            primerosPuntos = true;
            setContentView(R.layout.activity_maps);
            map = findViewById(R.id.map);
            map.setTileSource(TileSourceFactory.MAPNIK);
            map.setMultiTouchControls(true); //Habilitada la posibilidad de hacer zoom con dos dedos
            mapController = map.getController();
            mapController.setCenter(telecoPoint); //Centramos la posición en algún lugar conocido
            mapController.setZoom(10.0);
            RotationGestureOverlay rotationGestureOverlay = new RotationGestureOverlay(map);
            rotationGestureOverlay.setEnabled(true);
            map.setMinZoomLevel(4.0);
            map.setMaxZoomLevel(16.9);
            GpsMyLocationProvider gpsMyLocationProvider = new GpsMyLocationProvider(context);
            //gpsMyLocationProvider.setLocationUpdateMinTime(15000); //Recupera la posición cada 15 segundos
            gpsMyLocationProvider.setLocationUpdateMinDistance(5);
            gpsMyLocationProvider.setLocationUpdateMinTime(5000);
            gpsMyLocationProvider.addLocationSource(LocationManager.GPS_PROVIDER);
            gpsMyLocationProvider.addLocationSource(LocationManager.NETWORK_PROVIDER); //Utiliza red y GPS
            myLocationNewOverlay = new MyLocationNewOverlay(gpsMyLocationProvider, map);
            myLocationNewOverlay.enableMyLocation();
            myLocationNewOverlay.setDirectionArrow(BitmapFactory.decodeResource(getResources(), R.drawable.person),
                    BitmapFactory.decodeResource(getResources(), R.drawable.person));
            myLocationNewOverlay.enableFollowLocation(); //Se activa que se aproxime a la posición del usuario
            myLocationNewOverlay.setEnableAutoStop(true);
            //map.getOverlays().add(myLocationNewOverlay); //Se centra en el usuario. Si no lo consigue porque la
            //posición aún está a null siempre se tiene el pto conocido

            final DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
            scaleBarOverlay = new ScaleBarOverlay(map);
            scaleBarOverlay.setCentred(true); //La barra de escala se queda en el centro
            scaleBarOverlay.setScaleBarOffset(displayMetrics.widthPixels / 2, 25); //posición en el el display
            //map.getOverlays().add(scaleBarOverlay);

            //Se agrega la brújula
            compassOverlay = new CompassOverlay(context, new InternalCompassOrientationProvider(context), map);
            compassOverlay.enableCompass();

            pintaItemsfijos();
            //putItems();//Puntos de prueba

            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){ //Se necesita un canal para API 26 y superior
                channel = new NotificationChannel(Auxiliar.channelId, getString(R.string.canalTareas), NotificationManager.IMPORTANCE_HIGH);
                channel.setDescription(getString(R.string.canalTareas));
                notificationManager = context.getSystemService(NotificationManager.class);
                assert notificationManager != null;
                notificationManager.createNotificationChannel(channel);
            }
            else{
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                    notificationManager = context.getSystemService(NotificationManager.class);
                else{//API 22
                    notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                }
            }
            map.addMapListener(new DelayedMapListener(new MapListener() {
                @Override
                public boolean onScroll(ScrollEvent event) { //Movimientos y zoom con dedos
                    //Toast.makeText(context, "Scroll "+map.getMapCenter().getLatitude()+" "+map.getMapCenter().getLongitude(), Toast.LENGTH_SHORT).show();
                    return false;
                }

                @Override
                public boolean onZoom(ZoomEvent event) {//Zoom con botones
                    Toast.makeText(context, "zoom "+map.getZoomLevelDouble(), Toast.LENGTH_SHORT).show();
                    return false;
                }
            }, 200));
        }
    }

    /**
     * Método para comprobar si el usuario ha otorgado a la aplicación los permisos necesarios.
     * En la actualidad, solicita permisos de localización y cámara.
     */
    public void checkPermissions() {
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY))
            System.exit(-1);
        ArrayList<String> permisos = new ArrayList<>();
        Auxiliar.preQueryPermisos(this, permisos);
        if (permisos.size() > 0) //Evitamos hacer una petición con un array nulo
            ActivityCompat.requestPermissions(this, permisos.toArray(new String[permisos.size()]), requestCodePermissions);
    }

    /**
     * Método que devuelve el resultado de la solicitud de permisos.
     * @param requestCode Código de la petición de permismos.
     * @param permissions Permisos que se han solicitado.
     * @param grantResults Valor otorgado por el usuario al permiso.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NotNull String[] permissions, int[] grantResults) {
        //Se comprueba uno a uno si alguno de los permisos no se había aceptado
        for (int i : grantResults) {
            if (i == -1) {
                AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
                alertBuilder.setTitle(getString(R.string.permi));
                alertBuilder.setMessage(getString(R.string.permiM));
                alertBuilder.setPositiveButton(getString(R.string.accept), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //Se comprueba todos los permisos que necesite la app de nuevo, por este
                        // motivo se puede salir del for directamente
                        checkPermissions();
                    }
                });
                alertBuilder.setNegativeButton(getString(R.string.exi), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //Si el usuario no quiere conceder los permisos que necesita la aplicación se
                        //cierra
                        System.exit(0);
                    }
                });
                alertBuilder.show();
                break;
            }
        }
    }

    public void pintaItemsfijos() {
        map.getOverlays().add(myLocationNewOverlay);
        map.getOverlays().add(scaleBarOverlay);
        map.getOverlays().add(compassOverlay);
    }

    /*
    public void putItems() {
        newMarker(42.0076, -4.52449, "Ermita de San Juan Bautista, Palencia", 8);
        newMarker(42.0081, -4.5210, "San Marco, Palencia", 2);
        newMarker(42.0160, -4.5275, "Parroquia Reina Inmaculada", 1);
        newMarker(42.0094, -4.5296, "Parroquia de San Lazaro, Palencia", 5);
        newMarker(42.0114, -4.5321, "Iglesia de San Francisco, Palencia", 12);
    }*/

    /**
     * Método que se utiliza para agregar un marcador al mapa
     *  @param latitude Latitud del marcador
     * @param longitude Longitud del marcador
     * @param titule Título del marcador
     * @param listaTareas Tipo de tareas que encontrará el usuario en la posición indicada por el marcador
     */
    public void newMarker(double latitude, double longitude, String titule, List<String> listaTareas) {
        LabelledGeoPoint labelledGeoPoint = new LabelledGeoPoint(latitude, longitude, titule);
        if (!items.contains(labelledGeoPoint)) {
            items.add(labelledGeoPoint);
            Marker marker = new Marker(map);
            marker.setPosition(new GeoPoint(latitude, longitude));
            marker.setTitle(titule);
            final int nTask = listaTareas.size();
            BitmapDrawable d = new BitmapDrawable(getResources(), generaBitmapMarker(listaTareas));
            marker.setIcon(d);
            //makerShape.setBounds(0, 0, bitmap.getWidth(), bitmap.getHeight());
            /*if (nTask < 2) {
                marker.setIcon(getResources().getDrawable(R.drawable.ic_3_tareas));
            }
            else {
                if (nTask < 4)
                    marker.setIcon(d);
                else {
                    if (nTask < 6)
                        marker.setIcon(getResources().getDrawable(R.drawable.ic_8_tareas));
                    else
                        marker.setIcon(getResources().getDrawable(R.drawable.ic_11_tareas));
                }
            }*/
            marker.setOnMarkerClickListener(new Marker.OnMarkerClickListener() {
                @Override
                public boolean onMarkerClick(Marker marker, MapView mapView) {
                    double distancia;
                    String msg = getResources().getString(R.string.task) + "s " + nTask + " || ";
                    try {
                        distancia = calculaDistanciaDosPuntos(myLocationNewOverlay.getMyLocation(), marker.getPosition());
                        msg += String.format(Locale.getDefault(), " %.3f km", distancia);
                    } catch (Exception e) {
                        msg += getString(R.string.recuperandoPosicion);
                    }
                    marker.setSubDescription(msg);
                    marker.showInfoWindow();
                    return false;
                }
            });
            map.getOverlays().add(marker);
        }
    }

    private Bitmap generaBitmapMarker(List<String> listaTareas){
        Bitmap bitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint();
        float xy = (float)bitmap.getWidth()/2;
        float radio = xy/2;
        List<String> tipoTarea = new ArrayList<>();
        List<Integer> numeroTipoTarea = new ArrayList<>();
        int intermedio = 0;
        /*if((intermedio = compruebaVecesTipo(listaTareas, Auxiliar.tipoSinRespuesta)) > 0) {
            tipoTarea.add(Auxiliar.tipoSinRespuesta);
            numeroTipoTarea.add(intermedio);
        }
        if((intermedio = compruebaVecesTipo(listaTareas, Auxiliar.tipoPreguntaCorta)) > 0){
            tipoTarea.add(Auxiliar.tipoPreguntaCorta);
            numeroTipoTarea.add(intermedio);
        }
        if((intermedio = compruebaVecesTipo(listaTareas, Auxiliar.tipoPreguntaLarga)) > 0){
            tipoTarea.add(Auxiliar.tipoPreguntaLarga);
            numeroTipoTarea.add(intermedio);
        }
        if((intermedio = compruebaVecesTipo(listaTareas, Auxiliar.tipoPreguntaImagen)) > 0){
            tipoTarea.add(Auxiliar.tipoPreguntaImagen);
            numeroTipoTarea.add(intermedio);
        }
        if((intermedio = compruebaVecesTipo(listaTareas, Auxiliar.tipoImagen)) > 0){
            tipoTarea.add(Auxiliar.tipoImagen);
            numeroTipoTarea.add(intermedio);
        }
        if((intermedio = compruebaVecesTipo(listaTareas, Auxiliar.tipoImagenMultiple)) > 0){
            tipoTarea.add(Auxiliar.tipoImagenMultiple);
            numeroTipoTarea.add(intermedio);
        }
        if((intermedio = compruebaVecesTipo(listaTareas, Auxiliar.tipoVideo)) > 0){
            tipoTarea.add(Auxiliar.tipoVideo);
            numeroTipoTarea.add(intermedio);
        }*/


            tipoTarea.add(Auxiliar.tipoSinRespuesta);
            numeroTipoTarea.add(compruebaVecesTipo(listaTareas, Auxiliar.tipoSinRespuesta));


            tipoTarea.add(Auxiliar.tipoPreguntaCorta);
            numeroTipoTarea.add(compruebaVecesTipo(listaTareas, Auxiliar.tipoPreguntaCorta));

            tipoTarea.add(Auxiliar.tipoPreguntaLarga);
            numeroTipoTarea.add(compruebaVecesTipo(listaTareas, Auxiliar.tipoPreguntaLarga));

            tipoTarea.add(Auxiliar.tipoPreguntaImagen);
            numeroTipoTarea.add(compruebaVecesTipo(listaTareas, Auxiliar.tipoPreguntaImagen));

            tipoTarea.add(Auxiliar.tipoImagen);
            numeroTipoTarea.add(compruebaVecesTipo(listaTareas, Auxiliar.tipoImagen));


            tipoTarea.add(Auxiliar.tipoImagenMultiple);
            numeroTipoTarea.add(compruebaVecesTipo(listaTareas, Auxiliar.tipoImagenMultiple));

            tipoTarea.add(Auxiliar.tipoVideo);
            numeroTipoTarea.add(compruebaVecesTipo(listaTareas, Auxiliar.tipoVideo));

        float resta = radio/tipoTarea.size();
        for(int i = 0; i< tipoTarea.size(); i++){
            if(numeroTipoTarea.get(i)>0) {
                switch (tipoTarea.get(i)) {
                    case Auxiliar.tipoSinRespuesta:
                        paint.setARGB(255, 255, 51, 51);
                        break;
                    case Auxiliar.tipoPreguntaCorta:
                        paint.setARGB(255, 160, 160, 160);
                        break;
                    case Auxiliar.tipoPreguntaLarga:
                        paint.setARGB(255, 51, 255, 255);
                        break;
                    case Auxiliar.tipoPreguntaImagen:
                        paint.setARGB(255, 255, 255, 51);
                        break;
                    case Auxiliar.tipoImagen:
                        paint.setARGB(255, 32, 32, 32);
                        break;
                    case Auxiliar.tipoImagenMultiple:
                        paint.setARGB(255, 51, 255, 51);
                        break;
                    case Auxiliar.tipoVideo:
                        paint.setARGB(255, 51, 51, 255);
                        break;
                }
            }else{
                paint.setARGB(255, 255, 255, 255);
            }
            canvas.drawCircle(xy, xy, (int)radio, paint);
            radio -= resta;
        }

        return bitmap;
    }

    private int compruebaVecesTipo(List<String> listaTareas, String tipoRespuesta){
        if(listaTareas.contains(tipoRespuesta)){
            int i = 0;
            for(String s :listaTareas){
                if(s.equals(tipoRespuesta))
                    i++;
            }
            return i;
        }
        return 0;
    }

    /**
     * Método para calcular la distancia entre dos puntos
     * @param punto1 Localización actual del usuario
     * @param punto2 Localización a la que quiere ir
     * @return Distancia (en km) entre las dos localizaciones
     * @throws Exception Se lanzará una excepción cuando alguno de los argumentos de entrada sea null
     */
    private double calculaDistanciaDosPuntos(GeoPoint punto1, IGeoPoint punto2) throws Exception {
        return Auxiliar.calculaDistanciaDosPuntos(punto1.getLatitude(), punto1.getLongitude(),
                punto2.getLatitude(), punto2.getLongitude());
    }

    private LocationManager locationManager;
    private LocationListener locationListenerGPS;
    private LocationListener locationListenerNETWORK;

    /**
     * Se restaura el mapa tal y como se indica en la guía.
     */
    @Override
    public void onResume() {
        super.onResume();
        if(!noMolestar) {
            locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
            try {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                        && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    checkPermissions();
                } else {
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10000, 0, this);
                    //locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 10000, 0, this);
                }
            } catch (Exception e) {
                //TODO
            }
            if (map != null)
                map.onResume();
        }
        else
            locationManager = null;
    }

    /**
     * Se pausa el mapa tal y como indica la guía
     */
    @Override
    public void onPause(){
        super.onPause();
        if(map != null)
            map.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        if(locationManager != null) {
            locationManager.removeUpdates(this);
        }
    }

    /**
     * Método que responde a la pulsación del alguno de los botones
     * @param view Instancia del botón pulsado que ha lanzado el método
     */
    public void boton(View view) {
        switch (view.getId()){
            case R.id.btCentrar: //Solo centra la posición si se ha conseguido recuperar
                if(myLocationNewOverlay.getMyLocation() != null) {
                    mapController.setZoom(16.9);
                    mapController.animateTo(myLocationNewOverlay.getMyLocation());
                    onLocationChanged(myLocationNewOverlay.getMyLocationProvider().getLastKnownLocation());
                }else{ //Si aún no se conoce se muestra un mensaje
                    Toast.makeText(this, getString(R.string.recuperandoPosicion), Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.switchNoMolestar: //Switch para activar el mapa deshabilitando el modo no molestar
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean(Ajustes.NO_MOLESTAR_pref, false);
                editor.commit();
                Intent intent = new Intent (getApplicationContext(), Maps.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                break;
        }
    }

    /**
     * Método que se llamará antes de destruir temporalmente la actividad para almacenar la posición
     * @param bundle Bundle
     */
    @Override
    protected void onSaveInstanceState(Bundle bundle){
        if(myLocationNewOverlay!=null && myLocationNewOverlay.getMyLocation()!=null){
            latitude = myLocationNewOverlay.getMyLocation().getLatitude();
            longitude = myLocationNewOverlay.getMyLocation().getLongitude();
        }
        if(map!=null)
            bundle.putDouble("ZOOM", map.getZoomLevelDouble());
        bundle.putDouble("LATITUDE", latitude);
        bundle.putDouble("LONGITUDE", longitude);
        bundle.putLong("ULTIMANOTIFICACION", ultimaNotificacion);
        bundle.putBoolean("PRIMEROSPUNTOS", primerosPuntos);
        super.onSaveInstanceState(bundle);
    }

    /**
     * Método que se llamará al restaurar la actividad
     * @param bundle Bundle
     */
    @Override
    protected void onRestoreInstanceState(Bundle bundle){
        super.onRestoreInstanceState(bundle);
        try {
            mapController.setZoom(bundle.getDouble("ZOOM"));
        }catch (Exception e){

        }

        latitude = bundle.getDouble("LATITUDE");
        longitude = bundle.getDouble("LONGITUDE");
        ultimaNotificacion = bundle.getLong("ULTIMANOTIFICACION");
        //primerosPuntos = bundle.getBoolean("PRIMEROSPUNTOS");
        GeoPoint lastCenter = new GeoPoint(latitude, longitude);
        mapController.setCenter(lastCenter);
    }

    /**
     * Método de actuación cuando se detecta un cambio en una de las preferencias
     * @param sharedPreferences Preferencias modificadas
     * @param key Preferencia que se ha modificado
     */
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        switch (key){
            case Ajustes.NO_MOLESTAR_pref:
                noMolestar = sharedPreferences.getBoolean(key, false);
                if(!noMolestar)
                    lanzaServicioPosicionamiento();
                break;
            case Ajustes.LISTABLANCA_pref:
                if(sharedPreferences.getBoolean(key, true))
                    Auxiliar.dialogoAyudaListaBlanca(this, sharedPreferences);
                break;
            default:
        }
    }

    /**
     * Método para lanzar el servicio que se quedará constantemente buscando la posición.
     * Se tiene en cuenta la versión de Android utilizada
     */
    private void lanzaServicioPosicionamiento(){
        Intent intent = new Intent(this, Proceso.class);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            startForegroundService(intent);
        else
            startService(intent);
    }

    /**
     * Creación del menú en el layout
     * @param menu Menú a rellenar
     * @return Verdadero si se va a mostrar el menú
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    /**
     * Método que reacciona a la pulsación de alguno de los items del menú
     * @param item Opción seleccionada
     * @return Verdadero si la opción estaba registrada en el menú
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        Intent intent;
        switch (item.getItemId()){
            case R.id.ajustes:
                intent = new Intent(this, Ajustes.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                return true;
            case R.id.acerca:
                intent = new Intent(this, Acerca.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                return true;
            case R.id.menuTareasPospuestas:
                intent = new Intent(this, ListaTareas.class);
                intent.putExtra(Auxiliar.peticion, PersistenciaDatos.ficheroTareasPospuestas);
                startActivity(intent);
                return true;
            case R.id.menuTareasRechazadas:
                intent = new Intent(this, ListaTareas.class);
                intent.putExtra(Auxiliar.peticion, PersistenciaDatos.ficheroTareasRechazadas);
                startActivity(intent);
                return true;
            case R.id.menuTareasCompletadas:
                intent = new Intent(this, ListaTareas.class);
                intent.putExtra(Auxiliar.peticion, PersistenciaDatos.ficheroCompletadas);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Método para indicar al usuario que va a salir de la aplicación
     */
    @Override
    public void onBackPressed(){
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
        alertBuilder.setTitle(getString(R.string.exitT));
        alertBuilder.setMessage(getString(R.string.exit));
        alertBuilder.setPositiveButton(getString(R.string.accept), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finishAffinity();//Se cierra la app. //El proceso puede seguir activo
            }
        });
        alertBuilder.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        alertBuilder.show();
    }

    @Override
    public void onLocationChanged(Location location){
        double latitud = location.getLatitude();
        double longitud = location.getLongitude();
        long instante = new Date().getTime();

        if(PersistenciaDatos.tieneObjetos(getApplication(), PersistenciaDatos.ficheroTareasUsuario)){//El fichero tiene tareas que representar
            if(Proceso.tareasActualizadas || primerosPuntos){
                Proceso.tareasActualizadas = false;
                map.getOverlays().clear();
                pintaItemsfijos();
                try {
                    JSONArray arrayTareas = PersistenciaDatos.leeFichero(getApplication(), PersistenciaDatos.ficheroTareasUsuario);
                    Map<JSONObject, List<String>> posiciones = new HashMap<>();
                    List<String> lista;
                    JSONObject json;
                    boolean existia;
                    for (int i = 0; i < arrayTareas.length(); i++) {
                        if(posiciones.isEmpty()){
                            lista = new ArrayList<>();
                            lista.add(Auxiliar.ultimaParte(arrayTareas.getJSONObject(i).getString(Auxiliar.tipoRespuesta)));
                            posiciones.put(arrayTareas.getJSONObject(i), lista);
                        }
                        else{
                            json = arrayTareas.getJSONObject(i);
                            existia = false;
                            for(JSONObject j : posiciones.keySet()){
                                if(json.getString(Auxiliar.latitud).equals(j.getString(Auxiliar.latitud)) &&
                                json.getString(Auxiliar.longitud).equals(j.getString(Auxiliar.longitud))){
                                    lista = posiciones.get(j);
                                    lista.add(Auxiliar.ultimaParte(json.getString(Auxiliar.tipoRespuesta)));
                                    posiciones.put(j, lista);
                                    existia = true;
                                    break;
                                }
                            }
                            if(!existia){
                                lista = new ArrayList<>();
                                lista.add(Auxiliar.ultimaParte(json.getString(Auxiliar.tipoRespuesta)));
                                posiciones.put(json, lista);
                            }
                        }
                    }
                    for(JSONObject j : posiciones.keySet()){
                        newMarker(j.getDouble(Auxiliar.latitud), j.getDouble(Auxiliar.longitud), j.getString(Auxiliar.titulo), posiciones.get(j));
                    }
                    primerosPuntos = false;
                    map.invalidate();
                }catch (Exception e){
                    //TODO
                }
            }

            //Si el usuario está cerca de una tarea se le notifica
            try{
                JSONObject tarea = Auxiliar.tareaMasCercana(getApplication(), latitud, longitud); //TODO LA TAREA PUEDE SER NULL!!!
                double distancia = Auxiliar.calculaDistanciaDosPuntos(latitud, longitud, tarea.getDouble(Auxiliar.latitud), tarea.getDouble(Auxiliar.longitud));
                //Con esta distancia se notifia al usuario si está lo suficientemente cerca
                long ultimaPosicionInstante;
                double ultimaPosicionLatitud, ultimaPosicionLogintud;
                if(PersistenciaDatos.existeTarea(getApplication(), PersistenciaDatos.ficheroInstantes, idUltimaPosicionMapa)){
                    try {
                        JSONObject json = PersistenciaDatos.recuperaTarea(getApplication(), PersistenciaDatos.ficheroInstantes, idUltimaPosicionMapa);
                        ultimaPosicionInstante = json.getLong(Auxiliar.instante);
                        ultimaPosicionLatitud = json.getDouble(Auxiliar.latitud);
                        ultimaPosicionLogintud = json.getDouble(Auxiliar.longitud);
                        json = null;
                    }catch (Exception e){
                        ultimaPosicionInstante = 0;
                        ultimaPosicionLatitud = 0;
                        ultimaPosicionLogintud = 0;
                    }
                }
                else{
                    ultimaPosicionInstante = 0;
                    ultimaPosicionLatitud = 0;
                    ultimaPosicionLogintud = 0;
                }
                if(distancia < 0.075){
                    //Se comprueba si va andando:
                    if(ultimaPosicionInstante != 0){//No es la primera iteración
                        long tiempo = instante - ultimaPosicionInstante;
                        double maxAndada = tiempo * ((double) 5/3600000);
                        distancia = Auxiliar.calculaDistanciaDosPuntos(latitud, longitud, ultimaPosicionLatitud, ultimaPosicionLogintud);
                        if(distancia <= maxAndada) {
                            long ultimaNotificacion;
                            try {
                                JSONObject json = PersistenciaDatos.recuperaTarea(getApplication(), PersistenciaDatos.ficheroInstantes, idinstanteUltimaNoti);
                                ultimaNotificacion = json.getLong(Auxiliar.instante);
                                json = null;
                            }catch (Exception e){
                                ultimaNotificacion = 0;
                            }
                            if (new Date().getTime() > (ultimaNotificacion + 60000)) {
                                try {
                                    //PersistenciaDatos.obtenTarea(getApplication(), PersistenciaDatos.ficheroTareas, tarea.getString(Auxiliar.id));
                                    //pintaNotificacion(tarea);
                                } catch (Exception e) {
                                    //NO se ha extraido de las tareas
                                }
                            }
                        }
                    }
                }
                JSONObject j = new JSONObject();
                j.put("id", idUltimaPosicionMapa);
                j.put(Auxiliar.latitud, location.getLatitude());
                j.put(Auxiliar.longitud, location.getLongitude());
                j.put(Auxiliar.instante, new Date().getTime());
                PersistenciaDatos.reemplazaJSON(getApplication(), PersistenciaDatos.ficheroInstantes, j);
            }catch (Exception e){
                //TODO
            }
        }
        else{
            map.getOverlays().clear();
            pintaItemsfijos();
        }
    }

    /**
     * Método encargado de lanzar la notificación automática
     * @param jsonObject Objeto JSON con los datos de la tarea
     */
    public void pintaNotificacion(JSONObject jsonObject){
        try{
            //Recursos que siempre van a tener todas las tareas
            String id = jsonObject.getString(Auxiliar.id);
            String tipoRespuesta = jsonObject.getString(Auxiliar.tipoRespuesta);
            tipoRespuesta = Auxiliar.ultimaParte(tipoRespuesta);
            String recursoAsociadoTexto = jsonObject.getString(Auxiliar.recursoAsociadoTexto);
            String recursoAsociadoImagen = null, recursoAsociadoImagenBaja = null, respuestaEsperada = null;
            try{
                recursoAsociadoImagen = jsonObject.getString(Auxiliar.recursoImagen);
                recursoAsociadoImagenBaja = jsonObject.getString(Auxiliar.recursoImagenBaja);
            } catch (Exception e){
                //Falta alguno de los dos recursos
            }

            try{
                respuestaEsperada = jsonObject.getString(Auxiliar.respuestaEsperada);
            } catch (Exception e){
                //No tiene respuestaEsperada
            }
            try {
                jsonObject.put(Auxiliar.tipoRespuesta, tipoRespuesta);
                jsonObject.put(Auxiliar.estadoTarea, EstadoTarea.NOTIFICADA.getValue());
                if(!PersistenciaDatos.guardaJSON(getApplication(), PersistenciaDatos.ficheroNotificadas, jsonObject, Context.MODE_PRIVATE))
                    throw new Exception();
                Intent intent = new Intent(context, Tarea.class);
                intent.putExtra(Auxiliar.id, id);
                intent.putExtra(Auxiliar.tipoRespuesta, tipoRespuesta);
                intent.putExtra(Auxiliar.recursoAsociadoTexto, recursoAsociadoTexto);
                intent.putExtra(Auxiliar.recursoImagen, recursoAsociadoImagen);
                intent.putExtra(Auxiliar.recursoImagenBaja, recursoAsociadoImagenBaja);
                intent.putExtra(Auxiliar.respuestaEsperada, respuestaEsperada);

                NotificationCompat.Builder builder = new NotificationCompat.Builder(context, Auxiliar.channelId)
                        .setSmallIcon(R.drawable.ic_launcher_foreground)
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setContentTitle(getString(R.string.nuevaTarea))
                        .setContentText(jsonObject.getString(Auxiliar.titulo))
                        .setTimeoutAfter(180000);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                PendingIntent pendingIntent = PendingIntent.getActivity(context, incr, intent, PendingIntent.FLAG_CANCEL_CURRENT);

                builder.setContentIntent(pendingIntent);
                builder.setAutoCancel(true);
                //builder.setTimeoutAfter(tiempoNotificacion); No es necesario en este tipo de notificaciones

                //Botones extra
                Intent intentBoton = new Intent(context, RecepcionNotificaciones.class);
                intentBoton.setAction("AHORA_NO");
                intentBoton.putExtra(Auxiliar.id, id);
                intentBoton.putExtra("idNotificacion", incr);
                PendingIntent ahoraNoPending = PendingIntent.getBroadcast(context, incr + 999, intentBoton, PendingIntent.FLAG_UPDATE_CURRENT);
                builder.addAction(R.drawable.ic_thumb_down_black_24dp, getString(R.string.ahoraNo), ahoraNoPending);
                builder.setDeleteIntent(ahoraNoPending);

                intentBoton.setAction("NUNCA_MAS");
                PendingIntent nuncaMasP = PendingIntent.getBroadcast(context, incr + 1000, intentBoton, PendingIntent.FLAG_UPDATE_CURRENT);
                builder.addAction(R.drawable.ic_delete_black_24dp, getString(R.string.nuncaMas), nuncaMasP);

                notificationManager.notify(incr, builder.build()); //Notificación lanzada
                JSONObject j = new JSONObject();
                j.put(Auxiliar.id, idinstanteUltimaNoti);
                j.put(Auxiliar.instante, new Date().getTime());
                PersistenciaDatos.reemplazaJSON(getApplication(), PersistenciaDatos.ficheroInstantes, j);
                ++incr; //Para que no tengan dos notificaciones el mismo valor
            }catch (Exception e){
                e.printStackTrace();
            }
        }catch (JSONException je){
            //Si alguno de los campos que siempre deberían existir no existen
        }
    }

    /**
     * This callback will never be invoked and providers can be considers as always in the
     * LocationProvider#AVAILABLE state.
     *
     * @param provider
     * @param status
     * @param extras
     * @deprecated This callback will never be invoked.
     */
    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    /**
     * Called when the provider is enabled by the user.
     *
     * @param provider the name of the location provider associated with this
     *                 update.
     */
    @Override
    public void onProviderEnabled(String provider) {

    }

    /**
     * Called when the provider is disabled by the user. If requestLocationUpdates
     * is called on an already disabled provider, this method is called
     * immediately.
     *
     * @param provider the name of the location provider associated with this
     *                 update.
     */
    @Override
    public void onProviderDisabled(String provider) {

    }

}
