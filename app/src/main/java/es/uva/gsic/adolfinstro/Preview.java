package es.uva.gsic.adolfinstro;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.preference.PreferenceManager;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.BitmapFactory;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.text.Layout;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.CustomZoomButtonsController;
import org.osmdroid.views.MapController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;
import org.osmdroid.views.overlay.simplefastpoint.LabelledGeoPoint;

import java.util.Locale;
import java.util.Objects;

import es.uva.gsic.adolfinstro.auxiliar.Auxiliar;

public class Preview extends AppCompatActivity {

    /** Contexto */
    private Context context;
    /** Vista donde se incluye la vista del mapa */
    private MapView map;
    /** Objeto para adecuar la vista del mapa */
    private MyLocationNewOverlay myLocationNewOverlay;
    /** Receptor de notificaciones */
    private RecepcionNotificaciones recepcionNotificaciones;

    private double latitud, longitud;

    /**
     * Se crea la vista de la interfaz de usuario.
     *
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

        setContentView(R.layout.activity_preview);
        Bundle extras = getIntent().getExtras();

        ImageView imageView = findViewById(R.id.imagenPreview);

        if(extras.getString(Auxiliar.recursoImagenBaja) != null) {
            Picasso.get()
                    .load(extras.getString(Auxiliar.recursoImagenBaja))
                    .placeholder(R.drawable.ic_cloud_download_blue_80dp)
                    .tag(Auxiliar.cargaImagenPreview)
                    .into(imageView);
            imageView.setVisibility(View.VISIBLE);
        }else{
            if(extras.getString(Auxiliar.recursoImagen) != null) {
                Picasso.get()
                        .load(extras.getString(Auxiliar.recursoImagen))
                        .placeholder(R.drawable.ic_cloud_download_blue_80dp)
                        .tag(Auxiliar.cargaImagenPreview)
                        .into(imageView);
                imageView.setVisibility(View.VISIBLE);
            }
        }
        map = findViewById(R.id.mapPreview);
        map.setTileSource(TileSourceFactory.MAPNIK);
        IMapController mapController = map.getController();

        latitud = extras.getDouble(Auxiliar.latitud);
        longitud = extras.getDouble(Auxiliar.longitud);
        GeoPoint posicionTarea = new GeoPoint( latitud, longitud);

        mapController.setCenter(posicionTarea);
        mapController.setZoom(16.0);
        map.setMaxZoomLevel(16.0);
        map.setMinZoomLevel(16.0);
        map.getZoomController().setVisibility(CustomZoomButtonsController.Visibility.NEVER);
        GpsMyLocationProvider gpsMyLocationProvider = new GpsMyLocationProvider(context);
        myLocationNewOverlay = new MyLocationNewOverlay(gpsMyLocationProvider, map);
        myLocationNewOverlay.enableMyLocation();
        myLocationNewOverlay.setDirectionArrow(BitmapFactory.decodeResource(getResources(), R.drawable.person),
                BitmapFactory.decodeResource(getResources(), R.drawable.person));
        map.getOverlays().add(myLocationNewOverlay);

        TextView titulo = findViewById(R.id.tituloPreview);
        titulo.setText(extras.getString(Auxiliar.titulo));
        TextView descripcion = findViewById(R.id.textoPreview);
        descripcion.setText(extras.getString(Auxiliar.recursoAsociadoTexto));
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            descripcion.setJustificationMode(Layout.JUSTIFICATION_MODE_INTER_WORD);
        }
        ImageView tipoTarea = findViewById(R.id.ivTipoTareaPreview);
        String tipo = extras.getString(Auxiliar.tipoRespuesta);
        tipoTarea.setContentDescription(String.format("%s%s", getString(R.string.tipoDeTarea), tipo));
        tipoTarea.setImageResource(Auxiliar.iconoTipoTarea(tipo));

        Marker marker = new Marker(map);
        marker.setPosition(new GeoPoint(extras.getDouble(Auxiliar.latitud), extras.getDouble(Auxiliar.longitud)));
        marker.setIcon(getResources().getDrawable(R.drawable.ic_11_tareas));
        marker.setTitle(extras.getString(Auxiliar.titulo));

        marker.setOnMarkerClickListener(new Marker.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker, MapView mapView) {
                double distancia;
                String msg = "";
                try {
                    distancia = Auxiliar.calculaDistanciaDosPuntos(
                            myLocationNewOverlay.getMyLocation().getLatitude(),
                            myLocationNewOverlay.getMyLocation().getLongitude(),
                            marker.getPosition().getLatitude(),
                            marker.getPosition().getLongitude());
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

    /**
     * Método que se ejecuta cuando el usuario presiona el botón de atras de su teléfono. Se pasa la
     * tarea a pospuesta y se muestra un toast antes de volver al mapa.
     */
    @Override
    public void onBackPressed(){
        Picasso.get().cancelTag(Auxiliar.cargaImagenPreview);
        Intent intent = new Intent();
        intent.setAction(Auxiliar.ahora_no);
        intent.putExtra(Auxiliar.id, Objects.requireNonNull(getIntent().getExtras()).getString(Auxiliar.id));
        sendBroadcast(intent);
        Toast.makeText(context, getString(R.string.tareaPospuesta), Toast.LENGTH_SHORT).show();
        Auxiliar.returnMain(context);
    }

    /**
     * Método ejecutado cuando se pulsa sobre un botón de la vista preview
     *
     * @param view Vista que se ha pulsado
     */
    public void boton(View view) {
        Intent intent;
        switch (view.getId()){
            case R.id.botonAceptarPreview:
                if(myLocationNewOverlay.getMyLocation() != null){
                    double distancia = Auxiliar.calculaDistanciaDosPuntos(myLocationNewOverlay.getMyLocation().getLatitude(),
                            myLocationNewOverlay.getMyLocation().getLongitude(),
                            latitud,
                            longitud);
                    if(distancia<0.15) {
                        intent = new Intent(context, Tarea.class);
                        intent.putExtras(Objects.requireNonNull(getIntent().getExtras()));
                        startActivity(intent);
                    }else {
                        Toast.makeText(context, getString(R.string.acercate), Toast.LENGTH_SHORT).show();
                    }
                }else {
                    Toast.makeText(context, getString(R.string.recuperandoPosicion), Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.botonAhoraNoPreview:
                intent = new Intent();
                intent.setAction(Auxiliar.ahora_no);
                intent.putExtra(Auxiliar.id, Objects.requireNonNull(getIntent().getExtras()).getString(Auxiliar.id));
                sendBroadcast(intent);
                Auxiliar.returnMain(context);
                break;
            case R.id.botonRechazarPreview:
                intent = new Intent();
                intent.setAction(Auxiliar.nunca_mas);
                intent.putExtra(Auxiliar.id, Objects.requireNonNull(getIntent().getExtras()).getString(Auxiliar.id));
                sendBroadcast(intent);
                Auxiliar.returnMain(context);
                break;
        }
    }

    /**
     * Se sobrescribe le método onResume para activar la recepción de notificaciones y restaurar el mapa.
     * Se realiza la llamada al método que se sobrescribe
     */
    @Override
    protected void onResume(){
        super.onResume();
        recepcionNotificaciones = new RecepcionNotificaciones();
        registerReceiver(recepcionNotificaciones, Auxiliar.intentFilter());
        if(map != null)
            map.onResume();
    }

    /**
     * Se sobrescribe el método onPause para desactivar la recepción de notificaciones y pausar el mapa.
     * Se realiza la llamada al método que se sobrescribe.
     */
    @Override
    protected void onPause(){
        super.onPause();
        unregisterReceiver(recepcionNotificaciones);
        if(map != null)
            map.onPause();
    }
}
