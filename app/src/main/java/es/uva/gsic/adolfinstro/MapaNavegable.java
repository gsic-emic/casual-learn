package es.uva.gsic.adolfinstro;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.preference.PreferenceManager;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.View;
import android.view.ViewTreeObserver;

import org.jetbrains.annotations.NotNull;
import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.CustomZoomButtonsController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.util.ArrayList;
import java.util.Objects;

import es.uva.gsic.adolfinstro.auxiliar.Auxiliar;

/**
 * Clase para gestionar el mapa navegable. En la actualidad únicamente muestra la posición del mapa
 * y la ubicación de la tarea. Se muestra un botón con el que se saca el usuario a Google Maps para
 * las indicaciones.
 *
 * @author Pablo
 * @version 20200607
 */
public class MapaNavegable extends AppCompatActivity
        implements ActivityCompat.OnRequestPermissionsResultCallback {

    /** Contexto */
    Context context;
    /** Mapa */
    MapView map;
    /** Objeto para seguir la posición del usuario */
    MyLocationNewOverlay myLocationNewOverlay;

    /** Latitud del marcador*/
    double latitudMarker;
    /** Longitud del marcador*/
    double longitudMarker;
    /** Latitud del usuario*/
    double latitudUser;
    /** Longitud del usuario*/
    double longitudUser;

    /**
     * Se recogen referencias y se representa el mapa y los marcadores
     * @param savedInstanceState bundle con los extras cuando se reinicie la actividad
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        context = getApplicationContext(); //contexto de la aplicación
        Configuration.getInstance().load(context, PreferenceManager.getDefaultSharedPreferences(context));
        Configuration.getInstance().setUserAgentValue(BuildConfig.APPLICATION_ID);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mapa_navegable);

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        latitudMarker = Objects.requireNonNull(getIntent().getExtras()).getDouble(Auxiliar.latitud + "task");
        longitudMarker = getIntent().getExtras().getDouble(Auxiliar.longitud + "task");

        latitudUser = getIntent().getExtras().getDouble(Auxiliar.latitud + "user");
        longitudUser = getIntent().getExtras().getDouble(Auxiliar.longitud + "user");

        map = findViewById(R.id.mvMapaNavegable);
        map.setTileSource(TileSourceFactory.MAPNIK);
        IMapController mapController = map.getController();
        map.setMultiTouchControls(true);
        map.getZoomController().setVisibility(CustomZoomButtonsController.Visibility.NEVER);

        /*GpsMyLocationProvider gpsMyLocationProvider = new GpsMyLocationProvider(context);
        myLocationNewOverlay = new MyLocationNewOverlay(gpsMyLocationProvider, map);
        myLocationNewOverlay.enableMyLocation();
        myLocationNewOverlay.setDirectionArrow(BitmapFactory.decodeResource(getResources(), R.drawable.person),
                BitmapFactory.decodeResource(getResources(), R.drawable.person));
        map.getOverlays().add(myLocationNewOverlay);*/

        map.setMaxZoomLevel(19.5);
        map.setMinZoomLevel(3.0);
        mapController.setZoom(19.5);

        GeoPoint posMarker = new GeoPoint(latitudMarker, longitudMarker);
        mapController.setCenter(posMarker);

        Marker marker = new Marker(map);
        marker.setPosition(posMarker);
        marker.setIcon(getResources().getDrawable(R.drawable.ic_11_tareas));
        marker.setInfoWindow(null);

        map.getOverlays().add(marker);

        map.setTilesScaledToDpi(true);

    }

    /**
     * Se restaura el mapa y se ajusta la vista a la posición del usuario y del marcador.
     */
    @Override
    protected void onResume(){
        super.onResume();
        if(map != null) {
            map.onResume();
            checkPermissions();
        }
    }

    public void checkPermissions(){
        ArrayList<String> permisos = Auxiliar.preQueryPermisos(this);
        if(permisos.isEmpty()) {
            if(myLocationNewOverlay == null) {
                GpsMyLocationProvider gpsMyLocationProvider = new GpsMyLocationProvider(context);
                gpsMyLocationProvider.addLocationSource(LocationManager.GPS_PROVIDER);
                gpsMyLocationProvider.addLocationSource(LocationManager.NETWORK_PROVIDER); //Utiliza red y GPS
                myLocationNewOverlay = new MyLocationNewOverlay(gpsMyLocationProvider, map);
                myLocationNewOverlay.enableMyLocation();
                myLocationNewOverlay.setDirectionArrow(BitmapFactory.decodeResource(getResources(), R.drawable.person),
                        BitmapFactory.decodeResource(getResources(), R.drawable.ic_flecha_roja));
                map.getOverlays().add(myLocationNewOverlay);
            }
            if (myLocationNewOverlay != null && myLocationNewOverlay.getMyLocation() != null) {
                latitudUser = myLocationNewOverlay.getMyLocation().getLatitude();
                longitudUser = myLocationNewOverlay.getMyLocation().getLongitude();
            }
            zumRecuadro(Auxiliar.colocaMapa(latitudMarker, longitudMarker, latitudUser, longitudUser));
        }
        else {
            ActivityCompat.requestPermissions(this, permisos.toArray(new String[permisos.size()]), 1000);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NotNull String[] permissions, int[] grantResults) {
        //Se comprueba uno a uno si alguno de los permisos no se había aceptado
        boolean falta = false;
        for (int i : grantResults) {
            if (i == -1) {
                falta = true;
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
        if(!falta){
            if(myLocationNewOverlay == null) {
                GpsMyLocationProvider gpsMyLocationProvider = new GpsMyLocationProvider(context);
                myLocationNewOverlay = new MyLocationNewOverlay(gpsMyLocationProvider, map);
                myLocationNewOverlay.enableMyLocation();
                myLocationNewOverlay.setDirectionArrow(BitmapFactory.decodeResource(getResources(), R.drawable.person),
                        BitmapFactory.decodeResource(getResources(), R.drawable.ic_flecha_roja));
                map.getOverlays().add(myLocationNewOverlay);
            }
            if (myLocationNewOverlay != null && myLocationNewOverlay.getMyLocation() != null) {
                latitudUser = myLocationNewOverlay.getMyLocation().getLatitude();
                longitudUser = myLocationNewOverlay.getMyLocation().getLongitude();
            }
            zumRecuadro(Auxiliar.colocaMapa(latitudMarker, longitudMarker, latitudUser, longitudUser));
        }
    }

    /**
     * Método para ajustar la posición y el zum del mapa a la posición del usuario y a la ubicación
     * del marcador
     * @param boundingBox Recuadro formado por la ubicación del usuario y el marcador
     */
    private void zumRecuadro(final BoundingBox boundingBox) {
        ViewTreeObserver viewTreeObserver = map.getViewTreeObserver();
        //Solamente se representa cuando el layout de representación del mapa esté disponible
        viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                map.zoomToBoundingBox(boundingBox, false);
                map.getController().zoomToSpan(
                        boundingBox.getLatitudeSpan(),
                        boundingBox.getLongitudeSpanWithDateLine());
                map.getController().setCenter(boundingBox.getCenterWithDateLine());
                boundingBox.getDiagonalLengthInMeters();
                ViewTreeObserver v = map.getViewTreeObserver();
                v.removeOnGlobalLayoutListener(this);
                map.setVisibility(View.VISIBLE);
                /*double zum = map.getZoomLevelDouble();
                if(zum - 2 >= 5)
                    map.getController().setZoom(zum-2);*/
            }
        });
    }

    /**
     * Metódo para pausar el mapa tal y como se indica en la documentación
     */
    @Override
    protected void onPause(){
        super.onPause();
        if(map != null)
            map.onPause();
    }

    /**
     * Flecha de la barra de tareas. Hace la misma acción que pulsar back
     * @return Se devuelve falso ya que no se finaliza la tarea.
     */
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return false;
    }

    /**
     * Método de la acción back. Finaliza la actividad
     */
    @Override
    public void onBackPressed() {
        finish();
    }

    /**
     * Método que es llamado cuando se pulsa uno de los botones de la interfaz gráfica.
     * @param view Vista
     */
    public void boton(View view) {
        if(view.getId() == R.id.btNavegar){
            try {
                if (myLocationNewOverlay.getMyLocation() != null) {
                    latitudUser = myLocationNewOverlay.getMyLocation().getLatitude();
                    longitudUser = myLocationNewOverlay.getMyLocation().getLongitude();
                }
                Uri uri;
                /*if (Auxiliar.calculaDistanciaDosPuntos(
                        latitudMarker, longitudMarker,
                        latitudUser, longitudUser)
                        <= 2) {*/
                uri = Uri.parse("google.navigation:q="
                        + latitudMarker + "," + longitudMarker +
                        "&mode=r");
                /*} else {
                    uri = Uri.parse("google.navigation:q="
                            + latitudMarker + "," + longitudMarker +
                            "&mode=r");
                }*/
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                //intent.setPackage("com.google.android.apps.maps");
                startActivity(Intent.createChooser(intent, ""));
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }
}
