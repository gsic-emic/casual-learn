package es.uva.gsic.adolfinstro;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.preference.PreferenceManager;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.text.Layout;
import android.view.View;
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
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.CustomZoomButtonsController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.MapEventsOverlay;
import org.osmdroid.views.overlay.Marker;

import java.util.Objects;

import es.uva.gsic.adolfinstro.auxiliar.Auxiliar;
import es.uva.gsic.adolfinstro.persistencia.PersistenciaDatos;

public class Preview extends AppCompatActivity implements LocationListener {

    /** Contexto */
    private Context context;
    /** Vista donde se incluye la vista del mapa */
    private MapView map;
    /** Receptor de notificaciones */
    private RecepcionNotificaciones recepcionNotificaciones;

    /** Objeto que tiene toda la información de la tarea */
    private JSONObject tarea;

    /** Objeto donde se coloca la explicación del por qué no puede realizar la tarea */
    private TextView explicacionDistancia;
    /** Instancia donde se coloca la distancia a la tarea */
    private TextView textoDistancia;
    /** Botones de la vista */
    private Button btRechazar, btPosponer, btAceptar;

    /** Objeto con el que se hace el seguimiento de la posición del usuario */
    private LocationManager locationManager;

    private Location location;

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

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        ImageView imageView = findViewById(R.id.imagenPreview);
        String idTarea = Objects.requireNonNull(getIntent().getExtras()).getString(Auxiliar.id);
        tarea = PersistenciaDatos.recuperaTarea(getApplication(), PersistenciaDatos.ficheroNotificadas, idTarea);
        try {
            try{
                assert tarea != null;
                if (tarea.has(Auxiliar.recursoImagenBaja) && !tarea.getString(Auxiliar.recursoImagenBaja).equals("")) {
                Picasso.get()
                        .load(tarea.getString(Auxiliar.recursoImagenBaja))
                        .placeholder(R.drawable.ic_cloud_download_blue_80dp)
                        .tag(Auxiliar.cargaImagenPreview)
                        .into(imageView);
                imageView.setVisibility(View.VISIBLE);
            } else {
                if (tarea.has(Auxiliar.recursoImagen) && !tarea.getString(Auxiliar.recursoImagen).equals("")) {
                    Picasso.get()
                            .load(tarea.getString(Auxiliar.recursoImagen))
                            .placeholder(R.drawable.ic_cloud_download_blue_80dp)
                            .tag(Auxiliar.cargaImagenPreview)
                            .into(imageView);
                    imageView.setVisibility(View.VISIBLE);
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

            double latitud = tarea.getDouble(Auxiliar.latitud);
            double longitud = tarea.getDouble(Auxiliar.longitud);
            GeoPoint posicionTarea = new GeoPoint(latitud, longitud);

            mapController.setCenter(posicionTarea);
            mapController.setZoom(17.5);
            map.setMaxZoomLevel(17.5);
            map.setMinZoomLevel(17.5);
            map.getZoomController().setVisibility(CustomZoomButtonsController.Visibility.NEVER);

            map.setMultiTouchControls(true);

            TextView titulo = findViewById(R.id.tituloPreview);
            titulo.setText(tarea.getString(Auxiliar.titulo));
            TextView descripcion = findViewById(R.id.textoPreview);
            descripcion.setText(tarea.getString(Auxiliar.recursoAsociadoTexto));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                descripcion.setJustificationMode(Layout.JUSTIFICATION_MODE_INTER_WORD);
            }
            //ImageView tipoTarea = findViewById(R.id.ivTipoTareaPreview);
            String tipo = tarea.getString(Auxiliar.tipoRespuesta);
            titulo.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, Auxiliar.iconoTipoTarea(tipo), 0);
            //tipoTarea.setContentDescription(String.format("%s%s", getString(R.string.tipoDeTarea), tipo));
            //tipoTarea.setImageResource(Auxiliar.iconoTipoTarea(tipo));

            Marker marker = new Marker(map);
            marker.setPosition(new GeoPoint(tarea.getDouble(Auxiliar.latitud), tarea.getDouble(Auxiliar.longitud)));
            marker.setIcon(getResources().getDrawable(R.drawable.ic_11_tareas));
            //marker.setTitle(extras.getString(Auxiliar.titulo));
            marker.setInfoWindow(null);

            map.getOverlays().add(new MapEventsOverlay(new MapEventsReceiver() {
                @SuppressLint("MissingPermission")
                @Override
                public boolean singleTapConfirmedHelper(GeoPoint p) {
                    try {//Se salta a la tarea de navegación cuando el usuario pulse sobre el mapa
                        if(location != null){
                            Intent intent = new Intent(context, MapaNavegable.class);
                            intent.putExtra(Auxiliar.latitud + "user", location.getLatitude());
                            intent.putExtra(Auxiliar.longitud + "user", location.getLongitude());
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
                    singleTapConfirmedHelper(p);
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

    /**
     * Método que habilita o deshabilita los botones dependiendo del argumento de entrada
     * @param visibles Si es true se muestran los botones para interactuar frente a la tarea. False
     *                 paramostrar la información de la distancia que falta a la tarea
     */
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

    /**
     * Al pulsar el botón de la barra título se realiza la misma acción que al pulsar atrás
     * @return false ya que no se finaliza la actividad
     */
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return false;
    }

    /**
     * Método que se ejecuta cuando el usuario presiona el botón de atras de su teléfono. Se pasa la
     * tarea a pospuesta si procede (tarea de notificación) y se muestra un toast antes de volver al mapa.
     */
    @Override
    public void onBackPressed(){
        Picasso.get().cancelTag(Auxiliar.cargaImagenPreview);
        try {
            switch (Objects.requireNonNull(Objects.requireNonNull(getIntent()
                        .getExtras()).getString(Auxiliar.previa))){
                case Auxiliar.notificacion:
                    Intent intent = new Intent();
                    intent.setAction(Auxiliar.ahora_no);
                    intent.putExtra(Auxiliar.id, Objects.requireNonNull(getIntent()
                            .getExtras()).getString(Auxiliar.id));
                    sendBroadcast(intent);
                    Toast.makeText(context, getString(R.string.tareaPospuesta), Toast.LENGTH_SHORT).show();
                    Auxiliar.returnMain(this);
                    break;
                case Auxiliar.mapa:
                    PersistenciaDatos.obtenTarea(
                            getApplication(),
                            PersistenciaDatos.ficheroNotificadas,
                            tarea.getString(Auxiliar.id));
                    finish();
                    break;
                case Auxiliar.tareasPospuestas:
                    PersistenciaDatos.guardaJSON(
                            getApplication(),
                            PersistenciaDatos.ficheroTareasPospuestas,
                            PersistenciaDatos.obtenTarea(
                                    getApplication(),
                                    PersistenciaDatos.ficheroNotificadas,
                                    tarea.getString(Auxiliar.id)),
                            Context.MODE_PRIVATE);
                    finish();
                    break;
                case Auxiliar.tareasRechazadas:
                    PersistenciaDatos.guardaJSON(
                            getApplication(),
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
                        intent.putExtra(
                                Auxiliar.id,
                                Objects.requireNonNull(getIntent().getExtras()).getString(Auxiliar.id));
                        startActivity(intent);
                        break;
                    case R.id.botonAhoraNoPreview:
                        intent = new Intent();
                        intent.setAction(Auxiliar.ahora_no);
                        intent.putExtra(
                                Auxiliar.id,
                                Objects.requireNonNull(getIntent().getExtras()).getString(Auxiliar.id));
                        sendBroadcast(intent);
                        Auxiliar.returnMain(context);
                        break;
                    case R.id.botonRechazarPreview:
                        intent = new Intent();
                        intent.setAction(Auxiliar.nunca_mas);
                        intent.putExtra(
                                Auxiliar.id,
                                Objects.requireNonNull(getIntent().getExtras()).getString(Auxiliar.id));
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
     * Se activa el seguimiento del usuario para mostrar la distancia a la tarea según se desplace
     */
    @Override
    protected void onResume(){
        super.onResume();
        recepcionNotificaciones = new RecepcionNotificaciones();
        registerReceiver(recepcionNotificaciones, Auxiliar.intentFilter());
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        try {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED  &&
                    ActivityCompat.checkSelfPermission(
                            this,
                            Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                botonesVisibles(false);
            } else {
                locationManager.requestLocationUpdates(
                        LocationManager.GPS_PROVIDER, 5000, 10, this);
                if(locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER) != null)
                    onLocationChanged(
                            locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if(map != null)
            map.onResume();
    }

    /**
     * Se sobrescribe el método onPause para desactivar la recepción de notificaciones y pausar el mapa.
     * Se detiene el seguimiento al usuario
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
        this.location = location;
        try {
            double distancia = Auxiliar.calculaDistanciaDosPuntos(
                    tarea.getDouble(Auxiliar.latitud),
                    tarea.getDouble(Auxiliar.longitud),
                    location.getLatitude(),
                    location.getLongitude());
            if(distancia <= 0.15){
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
