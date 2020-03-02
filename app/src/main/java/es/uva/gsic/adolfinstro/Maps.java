package es.uva.gsic.adolfinstro;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
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
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.ItemizedOverlayWithFocus;
import org.osmdroid.views.overlay.OverlayItem;
import org.osmdroid.views.overlay.ScaleBarOverlay;
import org.osmdroid.views.overlay.compass.CompassOverlay;
import org.osmdroid.views.overlay.compass.InternalCompassOrientationProvider;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.util.ArrayList;

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
    /** Aproximación del radio de la Tierra*/
    private final double radioTierra = 6371;
    /** Objeto con el que se mostrará la brújula del mapa*/
    private CompassOverlay compassOverlay;
    /** Vector donde se almacena los puntos representados en el mapa */
    private ArrayList<OverlayItem> items = new ArrayList<>();
    /** Vector con los id's de los puntos. Por ahora el id es el título del punto */
    private ArrayList<String> idItems = new ArrayList<>();

    /**
     * Método con el que se pinta la actividad. Lo primero que comprueba es si está activada el modo no
     * molestar para saber si se tiene que mostar el mapa o no
     * @param savedInstanceState Bundle
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        final Context context = getBaseContext(); //contexto de la app
        Configuration.getInstance().setUserAgentValue(BuildConfig.APPLICATION_ID);
        super.onCreate(savedInstanceState);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        onSharedPreferenceChanged(sharedPreferences, Ajustes.NO_MOLESTAR_pref);

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
            GpsMyLocationProvider gpsMyLocationProvider = new GpsMyLocationProvider(context);
            gpsMyLocationProvider.setLocationUpdateMinTime(15000); //Recupera la posición cada 15 segundos
            gpsMyLocationProvider.addLocationSource(LocationManager.GPS_PROVIDER);
            gpsMyLocationProvider.addLocationSource(LocationManager.NETWORK_PROVIDER); //Utiliza red y GPS
            myLocationNewOverlay = new MyLocationNewOverlay( gpsMyLocationProvider, map);
            myLocationNewOverlay.enableMyLocation();
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
            compassOverlay = new CompassOverlay(context, new InternalCompassOrientationProvider(context), map);
            compassOverlay.enableCompass();
            map.getOverlays().add(compassOverlay);

            putItems(); //Se agregan los puntos de prueba
        }
    }

    /**
     * Método para insertar los puntos de prueba
     */
    public void putItems(){
        String id = "idPrueba";
        OverlayItem overlayItem = new OverlayItem( id,"Punto", "Punto lejano", new GeoPoint(42.662357, -4.706005));
        idItems.add(id);
        overlayItem.setMarker(getDrawable(R.drawable.marker_default));
        items.add(overlayItem);
        id = "idPlazaMayor";
        overlayItem = new OverlayItem(id, "Plaza Mayor", "Plaza Mayor, Valladolid", new GeoPoint(41.6520, -4.7286));
        overlayItem.setMarker(getDrawable(R.drawable.marker_default));
        idItems.add(id);
        items.add(overlayItem);
        id = "idLaAntigua";
        overlayItem = new OverlayItem(id,"La Antigua", "La Antigua, Valladolid", new GeoPoint(41.6547, -4.7231));
        overlayItem.setMarker(getDrawable(R.drawable.marker_default));
        items.add(overlayItem);
        pushItems();
    }

    /**
     * Método para comprbar si un punto que nos llega estába siendo mostrado ya. Si estaba siendo mostrado no
     * se vuelve a agregar a los vectores.
     * @param overlayItem Punto en el que se incluye el id (título), descripción y GeoPoint.
     */
    public void putItem(OverlayItem overlayItem){
        if(!items.contains(overlayItem.getUid())){
            items.add(overlayItem);
            pushItems();
        }
    }

    /**
     * Método que dibuja los puntos en el mapa. Se establece la acción que tiene que suceder cuando se pulsa.
     */
    public void pushItems(){
        final Context context = getApplicationContext();
        ItemizedOverlayWithFocus<OverlayItem> overlay = new ItemizedOverlayWithFocus<OverlayItem>(items,
                new ItemizedIconOverlay.OnItemGestureListener<OverlayItem>() {
            @Override
            public boolean onItemSingleTapUp(int index, OverlayItem item) {
                double distancia;
                final double distanciaError = -72.47699963546;
                try{
                    distancia = calculaDistanciaDosPuntos(myLocationNewOverlay.getMyLocation(), item.getPoint());
                } catch (Exception e){
                    distancia = distanciaError;
                }
                Toast.makeText(context, item.getTitle()+"\r\n"+
                        ((distancia==distanciaError)?"null km":String.format("%.3f km",distancia)), Toast.LENGTH_SHORT).show();
                return false;
            }

            @Override
            public boolean onItemLongPress(int index, OverlayItem item) {
                if(item.getTitle() == "Plaza Mayor"){
                    putItem(new OverlayItem("otroPunto", "OtroPunto", "otroPunto",
                            new GeoPoint(41.7520, -4.6286)));
                }
                onItemSingleTapUp(index, item);
                return false;
            }
        }, context);
        map.getOverlays().add(overlay);
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
                    mapController.setCenter(myLocationNewOverlay.getMyLocation());
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
            case Ajustes.INTERVALO_pref:
                int intervalo = sharedPreferences.getInt(key, 1);
                break;
            case Ajustes.NO_MOLESTAR_pref:
                noMolestar = sharedPreferences.getBoolean(key, false);
                if(!noMolestar)
                    lanzaServicioPosicionamiento();
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
        switch (item.getItemId()){
            case R.id.ajustes:
                Intent intent = new Intent(this, Ajustes.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
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
                finishAffinity();//Se cierra la app. //El proceso puede seguir activox
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
