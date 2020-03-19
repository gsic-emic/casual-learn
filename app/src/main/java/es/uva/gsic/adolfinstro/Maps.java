package es.uva.gsic.adolfinstro;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import android.app.ActivityOptions;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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

import org.osmdroid.api.IGeoPoint;
import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.ScaleBarOverlay;
import org.osmdroid.views.overlay.compass.CompassOverlay;
import org.osmdroid.views.overlay.compass.InternalCompassOrientationProvider;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;
import org.osmdroid.views.overlay.simplefastpoint.LabelledGeoPoint;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

//https://github.com/osmdroid/osmdroid/wiki/How-to-use-the-osmdroid-library
public class Maps extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener{
    /** Objeto que permite mostrar el mapa*/
    private MapView map;
    /** Objeto que almacenará, entre otras cosas, la última posición conocida del usuario*/
    private MyLocationNewOverlay myLocationNewOverlay;
    /** Objeto tuilizado para centrar el mapa en un punto específico*/
    private IMapController mapController;
    /** Posición inicial del punto conocido */
    private double latitude = 41.662357, longitude = -4.706005;
    /** Punto del mapa en el que se centrará si no consigue recuperar la posición actual*/
    private final GeoPoint telecoPoint= new GeoPoint( latitude, longitude);
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

    /**
     * Método con el que se pinta la actividad. Lo primero que comprueba es si está activada el modo no
     * molestar para saber si se tiene que mostar el mapa o no
     * @param savedInstanceState Bundle
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        final Context context = getApplicationContext(); //contexto de la app
        Configuration.getInstance().load(context, PreferenceManager.getDefaultSharedPreferences(context));
        Configuration.getInstance().setUserAgentValue(BuildConfig.APPLICATION_ID);
        super.onCreate(savedInstanceState);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        onSharedPreferenceChanged(sharedPreferences, Ajustes.NO_MOLESTAR_pref);
        onSharedPreferenceChanged(sharedPreferences, Ajustes.LISTABLANCA_pref);

        //Se decide si se muestra el mapa
        if(noMolestar){
            setContentView(R.layout.no_molestar);
        }
        else{
            setContentView(R.layout.activity_maps);
            map = findViewById(R.id.map);
            map.setTileSource(TileSourceFactory.MAPNIK);
            map.setMultiTouchControls(true); //Habilitada la posibilidad de hacer zoom con dos dedos
            mapController = map.getController();
            mapController.setCenter(telecoPoint); //Centramos la posición en algún lugar conocido
            mapController.setZoom(10.0);
            map.setMinZoomLevel(4.0);
            map.setMaxZoomLevel(20.0);
            GpsMyLocationProvider gpsMyLocationProvider = new GpsMyLocationProvider(context);
            gpsMyLocationProvider.setLocationUpdateMinTime(15000); //Recupera la posición cada 15 segundos
            gpsMyLocationProvider.addLocationSource(LocationManager.GPS_PROVIDER);
            gpsMyLocationProvider.addLocationSource(LocationManager.NETWORK_PROVIDER); //Utiliza red y GPS
            myLocationNewOverlay = new MyLocationNewOverlay( gpsMyLocationProvider, map);
            myLocationNewOverlay.enableMyLocation();
            myLocationNewOverlay.setDirectionArrow(BitmapFactory.decodeResource(getResources(), R.drawable.person),
                    BitmapFactory.decodeResource(getResources(), R.drawable.person));
            myLocationNewOverlay.enableFollowLocation(); //Se activa que se aproxime a la posición del usuario
            myLocationNewOverlay.setEnableAutoStop(true);
            map.getOverlays().add(myLocationNewOverlay); //Se centra en el usuario. Si no lo consigue porque la
                                                         //posición aún está a null siempre se tiene el pto conocido

            final DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
            ScaleBarOverlay scaleBarOverlay = new ScaleBarOverlay(map);
            scaleBarOverlay.setCentred(true); //La barra de escala se queda en el centro
            scaleBarOverlay.setScaleBarOffset(displayMetrics.widthPixels / 2, 25); //posición en el el display
            map.getOverlays().add(scaleBarOverlay);

            //Se agrega la brújula
            /** Objeto con el que se mostrará la brújula del mapa*/
            CompassOverlay compassOverlay = new CompassOverlay(context, new InternalCompassOrientationProvider(context), map);
            compassOverlay.enableCompass();
            map.getOverlays().add(compassOverlay);

            putItems();//Puntos de prueba
        }
    }

    /**
     * Este método almacena los puntos que se tenga en una lista para su posterior manipulación
     */
    public void putItems(){
        //Valores de desarrollo. Estas posiciciones se obtendrán de manera dinámica
        newMarker(41.6520, -4.7286, "Plaza Mayor, Valladolid", 25);
        newMarker(41.6547, -4.7231, "La Antigua, Valladolid", 2);
        newMarker(41.6501, -4.7221, "Punto3", 6);
        newMarker(41.6576, -4.7267, "Punto4", 9);
    }

    /**
     * Método que se utiliza para agregar un marcador al mapa
     *
     * @param latitude Latitud del marcador
     * @param longitude Longitud del marcador
     * @param titule Título del marcador
     * @param nTask Número de tareas que encontrará el usuario en la posición indicada por el marcador
     */
    public void newMarker(double latitude, double longitude, String titule, final int nTask){
        LabelledGeoPoint labelledGeoPoint = new LabelledGeoPoint(latitude, longitude, titule);
        if(!items.contains(labelledGeoPoint)){
            items.add(labelledGeoPoint);
            Marker marker = new Marker(map);
            marker.setPosition(new GeoPoint(latitude, longitude));
            marker.setTitle(titule);
            if(nTask < 5)
                marker.setIcon(getResources().getDrawable(R.drawable.ic_3_tareas));
            else{
                if(nTask < 8)
                    marker.setIcon(getResources().getDrawable(R.drawable.ic_5_tareas));
                else{
                    if(nTask < 11)
                        marker.setIcon(getResources().getDrawable(R.drawable.ic_8_tareas));
                    else
                        marker.setIcon(getResources().getDrawable(R.drawable.ic_11_tareas));
                }
            }
            marker.setOnMarkerClickListener(new Marker.OnMarkerClickListener() {
                @Override
                public boolean onMarkerClick(Marker marker, MapView mapView) {
                    double distancia;
                    String msg = getResources().getString(R.string.task) + "s " + nTask + " || ";
                    try{
                        distancia = calculaDistanciaDosPuntos(myLocationNewOverlay.getMyLocation(), marker.getPosition());
                        msg += String.format(Locale.getDefault(), " %.3f km", distancia);
                    } catch (Exception e){
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

    /**
     * Método para calcular la distancia entre dos puntos
     * @param punto1 Localización actual del usuario
     * @param punto2 Localización a la que quiere ir
     * @return Distancia (en km) entre las dos localizaciones
     * @throws Exception Se lanzará una excepción cuando alguno de los argumentos de entrada sea null
     */
    private double calculaDistanciaDosPuntos(GeoPoint punto1, IGeoPoint punto2) throws Exception{
        return calculaDistanciaDosPuntos(punto1.getLatitude(), punto1.getLongitude(),
                    punto2.getLatitude(), punto2.getLongitude());
    }

    /**
     * Método para calcular la distancia entre dos puntos
     * @param lat1 Latitud del punto 1 (en grados)
     * @param lon1 Longitud del punto 1 (en grados)
     * @param lat2 Latitud del punto 2 (en grados)
     * @param lon2 Longitud del punto 2 (en grados)
     * @return Distancia (en km) entre los dos puntos
     */
    private double calculaDistanciaDosPuntos(double lat1, double lon1, double lat2, double lon2){
        /** Aproximación del radio de la Tierra*/
        double radioTierra = 6371;
        return 2 * radioTierra *
                Math.asin(Math.sqrt(
                        (1-Math.cos(Math.toRadians(lat2-lat1)))/2 +
                                Math.cos(Math.toRadians(lat1))*
                                        Math.cos(Math.toRadians(lat2))*
                                        ((1-Math.cos(Math.toRadians(lon2 - lon1)))/2)
                ));
    }

    /**
     * Se restaura el mapa tal y como se indica en la guía.
     */
    @Override
    public void onResume(){
        super.onResume();
        if(map != null)
            map.onResume();
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

    /**
     * Método que responde a la pulsación del alguno de los botones
     * @param view Instancia del botón pulsado que ha lanzado el método
     */
    public void boton(View view) {
        switch (view.getId()){
            case R.id.btCentrar: //Solo centra la posición si se ha conseguido recuperar
                if(myLocationNewOverlay.getMyLocation() != null) {
                    mapController.setZoom(18.0);
                    mapController.animateTo(myLocationNewOverlay.getMyLocation());
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
        alertBuilder.setPositiveButton(getString(R.string.acept), new DialogInterface.OnClickListener() {
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
}
