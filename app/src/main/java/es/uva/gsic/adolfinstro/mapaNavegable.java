package es.uva.gsic.adolfinstro;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.mtp.MtpConstants;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.ViewTreeObserver;

import org.osmdroid.api.IGeoPoint;
import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.util.TileSystem;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import es.uva.gsic.adolfinstro.auxiliar.Auxiliar;

public class mapaNavegable extends AppCompatActivity {

    Context context;
    MapView map;
    MyLocationNewOverlay myLocationNewOverlay;
    double latitudMarker, longitudMarker, latitudUser, longitudUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        context = getApplicationContext(); //contexto de la aplicación
        Configuration.getInstance().load(context, PreferenceManager.getDefaultSharedPreferences(context));
        Configuration.getInstance().setUserAgentValue(BuildConfig.APPLICATION_ID);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mapa_navegable);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        latitudMarker = getIntent().getExtras().getDouble(Auxiliar.latitud + "task");
        longitudMarker = getIntent().getExtras().getDouble(Auxiliar.longitud + "task");

        latitudUser = getIntent().getExtras().getDouble(Auxiliar.latitud + "user");
        longitudUser = getIntent().getExtras().getDouble(Auxiliar.longitud + "user");


        map = findViewById(R.id.mvMapaNavegable);
        map.setTileSource(TileSourceFactory.MAPNIK);
        IMapController mapController = map.getController();
        map.setMultiTouchControls(true);

        GpsMyLocationProvider gpsMyLocationProvider = new GpsMyLocationProvider(context);
        myLocationNewOverlay = new MyLocationNewOverlay(gpsMyLocationProvider, map);
        myLocationNewOverlay.enableMyLocation();
        myLocationNewOverlay.setDirectionArrow(BitmapFactory.decodeResource(getResources(), R.drawable.person),
                BitmapFactory.decodeResource(getResources(), R.drawable.person));
        map.getOverlays().add(myLocationNewOverlay);

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
    }

    @Override
    protected void onResume(){
        super.onResume();
        if(map != null) {
            map.onResume();
            if(myLocationNewOverlay.getMyLocation() != null){
                latitudUser = myLocationNewOverlay.getMyLocation().getLatitude();
                longitudUser = myLocationNewOverlay.getMyLocation().getLongitude();
            }
            zumRecuadro(Auxiliar.colocaMapa(latitudMarker, longitudMarker, latitudUser, longitudUser));
        }
    }

    private void zumRecuadro(final BoundingBox boundingBox) {
        if(map.getHeight() > 0){
            map.zoomToBoundingBox(boundingBox, false);
        }else {
            ViewTreeObserver viewTreeObserver = map.getViewTreeObserver();
            viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    map.zoomToBoundingBox(boundingBox, false);
                    map.getController().zoomToSpan(boundingBox.getLatitudeSpan(), boundingBox.getLongitudeSpanWithDateLine());
                    map.getController().setCenter(boundingBox.getCenterWithDateLine());
                    boundingBox.getDiagonalLengthInMeters();
                    ViewTreeObserver v = map.getViewTreeObserver();
                    v.removeOnGlobalLayoutListener(this);
                    /*double zum = map.getZoomLevelDouble();
                    if(zum - 2 >= 5)
                        map.getController().setZoom(zum-2);*/
                }
            });
        }
    }

    @Override
    protected void onPause(){
        super.onPause();
        if(map != null)
            map.onPause();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return false;
    }

    @Override
    public void onBackPressed() {
        finish();
    }



}
