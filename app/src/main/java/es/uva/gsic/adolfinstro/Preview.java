package es.uva.gsic.adolfinstro;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.preference.PreferenceManager;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.text.Layout;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;
import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.CustomZoomButtonsController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.MapEventsOverlay;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.util.Objects;

import es.uva.gsic.adolfinstro.auxiliar.Auxiliar;
import es.uva.gsic.adolfinstro.persistencia.PersistenciaDatos;

public class Preview extends AppCompatActivity implements LocationListener {

    /** Contexto */
    private Context context;
    /** Vista donde se incluye la vista del mapa */
    private MapView map;
    /** Objeto para adecuar la vista del mapa */
    private MyLocationNewOverlay myLocationNewOverlay;
    /** Receptor de notificaciones */
    private RecepcionNotificaciones recepcionNotificaciones;

    //private RoadManager roadManager;

    private double latitud, longitud;
    private boolean grande = false, foto = false;

    private JSONObject tarea;

    private ImageView imageView;
    private TextView descripcion, titulo, explicacionDistancia, textoDistancia;
    private Button btRechazar, btPosponer, btAceptar;
    private int alturaOriginal;

    private LocationManager locationManager;

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

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        imageView = findViewById(R.id.imagenPreview);
        String idTarea = getIntent().getExtras().getString(Auxiliar.id);
        tarea = PersistenciaDatos.recuperaTarea(getApplication(), PersistenciaDatos.ficheroNotificadas, idTarea);
        try {
            try{
            if (!tarea.getString(Auxiliar.recursoImagenBaja).equals("")) {
                Picasso.get()
                        .load(tarea.getString(Auxiliar.recursoImagenBaja))
                        .placeholder(R.drawable.ic_cloud_download_blue_80dp)
                        .tag(Auxiliar.cargaImagenPreview)
                        .into(imageView);
                imageView.setVisibility(View.VISIBLE);
                foto = true;
            } else {
                if (!tarea.getString(Auxiliar.recursoImagen).equals("")) {
                    Picasso.get()
                            .load(tarea.getString(Auxiliar.recursoImagen))
                            .placeholder(R.drawable.ic_cloud_download_blue_80dp)
                            .tag(Auxiliar.cargaImagenPreview)
                            .into(imageView);
                    imageView.setVisibility(View.VISIBLE);
                    foto = true;
                }
            }}
            catch (Exception e){
                e.printStackTrace();
            }

            btAceptar = findViewById(R.id.botonAceptarPreview);
            btPosponer = findViewById(R.id.botonAhoraNoPreview);
            btRechazar = findViewById(R.id.botonRechazarPreview);
            explicacionDistancia = findViewById(R.id.tvExplicacionDistancia);
            textoDistancia = findViewById(R.id.tvDistancia);
            map = findViewById(R.id.mapPreview);
            map.setTileSource(TileSourceFactory.MAPNIK);
            IMapController mapController = map.getController();
            //roadManager = new OSRMRoadManager(this);

            latitud = tarea.getDouble(Auxiliar.latitud);
            longitud = tarea.getDouble(Auxiliar.longitud);
            GeoPoint posicionTarea = new GeoPoint(latitud, longitud);

            mapController.setCenter(posicionTarea);
            mapController.setZoom(17.5);
            map.setMaxZoomLevel(17.5);
            map.setMinZoomLevel(17.5);
            map.getZoomController().setVisibility(CustomZoomButtonsController.Visibility.NEVER);
            GpsMyLocationProvider gpsMyLocationProvider = new GpsMyLocationProvider(context);
            myLocationNewOverlay = new MyLocationNewOverlay(gpsMyLocationProvider, map);
            myLocationNewOverlay.enableMyLocation();
            myLocationNewOverlay.setDirectionArrow(BitmapFactory.decodeResource(getResources(), R.drawable.person),
                    BitmapFactory.decodeResource(getResources(), R.drawable.person));
            map.getOverlays().add(myLocationNewOverlay);
//
//            map.setMultiTouchControls(true);
            titulo = findViewById(R.id.tituloPreview);
            titulo.setText(tarea.getString(Auxiliar.titulo));
            descripcion = findViewById(R.id.textoPreview);
            descripcion.setText(tarea.getString(Auxiliar.recursoAsociadoTexto));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                descripcion.setJustificationMode(Layout.JUSTIFICATION_MODE_INTER_WORD);
            }
            ImageView tipoTarea = findViewById(R.id.ivTipoTareaPreview);
            String tipo = tarea.getString(Auxiliar.tipoRespuesta);
            tipoTarea.setContentDescription(String.format("%s%s", getString(R.string.tipoDeTarea), tipo));
            tipoTarea.setImageResource(Auxiliar.iconoTipoTarea(tipo));

            Marker marker = new Marker(map);
            marker.setPosition(new GeoPoint(tarea.getDouble(Auxiliar.latitud), tarea.getDouble(Auxiliar.longitud)));
            marker.setIcon(getResources().getDrawable(R.drawable.ic_11_tareas));
            //marker.setTitle(extras.getString(Auxiliar.titulo));
            marker.setInfoWindow(null);

            map.getOverlays().add(new MapEventsOverlay(new MapEventsReceiver() {
                @Override
                public boolean singleTapConfirmedHelper(GeoPoint p) {
                    try {
                        if(myLocationNewOverlay.getMyLocation() != null){
                            Intent intent = new Intent(context, mapaNavegable.class);
                            intent.putExtra(Auxiliar.latitud + "user", myLocationNewOverlay.getMyLocation().getLatitude());
                            intent.putExtra(Auxiliar.longitud + "user", myLocationNewOverlay.getMyLocation().getLongitude());
                            intent.putExtra(Auxiliar.latitud + "task", tarea.getDouble(Auxiliar.latitud));
                            intent.putExtra(Auxiliar.longitud + "task", tarea.getDouble(Auxiliar.longitud));
                            startActivity(intent);
                        }else{
                            Toast.makeText(context,  context.getString(R.string.recuperandoPosicion), Toast.LENGTH_SHORT).show();
                        }
                    }catch (JSONException e){
                        e.printStackTrace();
                    }
                    return false;
                }

                @Override
                public boolean longPressHelper(GeoPoint p) {
                    return false;
                }
            }));
            map.getOverlays().add(marker);

            if(!getIntent().getExtras().getString(Auxiliar.previa).equals(Auxiliar.notificacion)){
                botonesVisibles(false);
            }else{
                botonesVisibles(true);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void botonesVisibles(boolean visibles){
        if(visibles) {
            btRechazar.setVisibility(View.VISIBLE);
            btPosponer.setVisibility(View.VISIBLE);
            btAceptar.setVisibility(View.VISIBLE);
            explicacionDistancia.setVisibility(View.GONE);
            textoDistancia.setVisibility(View.GONE);
        }else{
            btRechazar.setVisibility(View.GONE);
            btPosponer.setVisibility(View.GONE);
            btAceptar.setVisibility(View.GONE);
            explicacionDistancia.setVisibility(View.VISIBLE);
            textoDistancia.setVisibility(View.VISIBLE);

        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return false;
    }

    /**
     * Método que se ejecuta cuando el usuario presiona el botón de atras de su teléfono. Se pasa la
     * tarea a pospuesta y se muestra un toast antes de volver al mapa.
     */
    @Override
    public void onBackPressed(){
        Picasso.get().cancelTag(Auxiliar.cargaImagenPreview);
        try {
            switch (getIntent().getExtras().getString(Auxiliar.previa)){
                case Auxiliar.notificacion:
                    Intent intent = new Intent();
                    intent.setAction(Auxiliar.ahora_no);
                    intent.putExtra(Auxiliar.id, Objects.requireNonNull(getIntent().getExtras()).getString(Auxiliar.id));
                    sendBroadcast(intent);
                    Toast.makeText(context, getString(R.string.tareaPospuesta), Toast.LENGTH_SHORT).show();
                    Auxiliar.returnMain(this);
                    break;
                case Auxiliar.mapa:
                    PersistenciaDatos.obtenTarea(getApplication(), PersistenciaDatos.ficheroNotificadas, tarea.getString(Auxiliar.id));
                    finish();
                    break;
                case Auxiliar.tareasPospuestas:
                    PersistenciaDatos.guardaJSON(getApplication(),
                            PersistenciaDatos.ficheroTareasPospuestas,
                            PersistenciaDatos.obtenTarea(
                                    getApplication(),
                                    PersistenciaDatos.ficheroNotificadas,
                                    tarea.getString(Auxiliar.id)),
                            Context.MODE_PRIVATE);
                    finish();
                    break;
                case Auxiliar.tareasRechazadas:
                    PersistenciaDatos.guardaJSON(getApplication(),
                            PersistenciaDatos.ficheroTareasRechazadas,
                            PersistenciaDatos.obtenTarea(
                                    getApplication(),
                                    PersistenciaDatos.ficheroNotificadas,
                                    tarea.getString(Auxiliar.id)),
                            Context.MODE_PRIVATE);
                    finish();
                    break;
                default:
                    break;
            }
        }catch (Exception e){
            Toast.makeText(context, getString(R.string.errorOpera), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Método ejecutado cuando se pulsa sobre un botón de la vista preview
     *
     * @param view Vista que se ha pulsado
     */
    public void boton(View view) {
        try {
            Intent intent;
            if (Auxiliar.tareaRegistrada(getApplication(), tarea.getString(Auxiliar.id))) {
                Toast.makeText(context, getString(R.string.tareaRegistrada), Toast.LENGTH_LONG).show();
            } else {
                switch (view.getId()) {
                    case R.id.botonAceptarPreview:
                            intent = new Intent(context, Tarea.class);
                            intent.putExtra(Auxiliar.id, Objects.requireNonNull(getIntent().getExtras()).getString(Auxiliar.id));
                            startActivity(intent);

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
        }catch (Exception e){
            e.printStackTrace();
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
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        try {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                botonesVisibles(false);
            } else {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 10, this);
                if(locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER) != null)
                    onLocationChanged(locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
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
        if(locationManager != null)
            locationManager.removeUpdates(this);
    }

    @Override
    public void onLocationChanged(Location location) {
        try {
            double distancia = Auxiliar.calculaDistanciaDosPuntos(tarea.getDouble(Auxiliar.latitud), tarea.getDouble(Auxiliar.longitud), location.getLatitude(), location.getLongitude());
            //TODO VOLVER A PONER A 0.15
            if(distancia <= 1){
                botonesVisibles(true);
            }else{
                botonesVisibles(false);
                if(distancia>=1)
                    textoDistancia.setText(String.format("%.2f km", distancia));
                else
                    textoDistancia.setText(String.format("%.2f m", distancia*1000));
            }
        }catch (Exception e){
            explicacionDistancia.setText(getResources().getString(R.string.recuperandoPosicion));
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }


    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }
}
