package es.uva.gsic.adolfinstro;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
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

public class Preview extends AppCompatActivity {

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
    private TextView descripcion, titulo;
    private Button btRechazar, btPosponer, btAceptar;
    private int alturaOriginal;

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

//            map.getOverlays().add(new MapEventsOverlay(new MapEventsReceiver() {
//                @Override
//                public boolean singleTapConfirmedHelper(GeoPoint p) {
//                    if (!grande) {
//                        /*if(myLocationNewOverlay.mMyLocationProvider != null && myLocationNewOverlay.mMyLocationProvider.getLastKnownLocation() != null) {
//                            try{
//                                Location location = myLocationNewOverlay.mMyLocationProvider.getLastKnownLocation();
//                                ArrayList<GeoPoint> listaPuntos = new ArrayList<>();
//                                listaPuntos.add(new GeoPoint(location.getLatitude(), location.getLongitude()));
//                                listaPuntos.add(new GeoPoint(tarea.getDouble(Auxiliar.latitud), tarea.getDouble(Auxiliar.longitud)));
//                                Road road = roadManager.getRoad(listaPuntos);
//                                Polyline roadOverlay = RoadManager.buildRoadOverlay(road);
//                                map.getOverlays().add(roadOverlay);
//                                map.invalidate();
//                            }catch (JSONException e){
//                                e.printStackTrace();
//                            }
//                        }*/
//                        ViewGroup.LayoutParams params = map.getLayoutParams();
//                        //params.height += getResources().getDisplayMetrics().heightPixels / 2;
//                        alturaOriginal = params.height;
//                        params.height = ((getResources().getDisplayMetrics().heightPixels)/4);
//                        params.height = params.height*3;
//                        map.setMaxZoomLevel(17.5);
//                        map.setMinZoomLevel(13.5);
//                        map.getZoomController().setVisibility(CustomZoomButtonsController.Visibility.ALWAYS);
//                        map.setMultiTouchControls(true);
//                        map.invalidate();
//                        grande = true;
//                        map.setLayoutParams(params);
//                        if(foto)
//                            imageView.setVisibility(View.GONE);
//                        titulo.setVisibility(View.GONE);
//                        descripcion.setVisibility(View.GONE);
//                        btAceptar.setVisibility(View.GONE);
//                        btPosponer.setVisibility(View.GONE);
//                        btRechazar.setVisibility(View.GONE);
//                    }
//                    return false;
//                }
//
//                @Override
//                public boolean longPressHelper(GeoPoint p) {
//                    if (grande) {
//                        ViewGroup.LayoutParams params = map.getLayoutParams();
//                        //params.height -= getResources().getDisplayMetrics().heightPixels / 2;
//                        params.height = alturaOriginal;
//                        map.setMaxZoomLevel(17.5);
//                        map.setMinZoomLevel(17.5);
//                        map.getZoomController().setVisibility(CustomZoomButtonsController.Visibility.NEVER);
//                        map.setMultiTouchControls(false);
//                        map.getController().setCenter(new GeoPoint(latitud, longitud));
//                        map.invalidate();
//                        grande = false;
//                        map.setLayoutParams(params);
//                        if(foto)
//                            imageView.setVisibility(View.VISIBLE);
//                        titulo.setVisibility(View.VISIBLE);
//                        descripcion.setVisibility(View.VISIBLE);
//                        btAceptar.setVisibility(View.VISIBLE);
//                        btPosponer.setVisibility(View.VISIBLE);
//                        btRechazar.setVisibility(View.VISIBLE);
//                    }
//                    return false;
//                }
//            }));

       /* marker.setOnMarkerClickListener(new Marker.OnMarkerClickListener() {
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
        });*/
            map.getOverlays().add(marker);
        }catch (Exception e){
            e.printStackTrace();
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
                        if (myLocationNewOverlay.getMyLocation() != null) {
                            double distancia = Auxiliar.calculaDistanciaDosPuntos(myLocationNewOverlay.getMyLocation().getLatitude(),
                                    myLocationNewOverlay.getMyLocation().getLongitude(),
                                    latitud,
                                    longitud);
                            //TODO volver a los 150 metros!!
                            if (distancia < 15) {
                                intent = new Intent(context, Tarea.class);
                                intent.putExtra(Auxiliar.id, Objects.requireNonNull(getIntent().getExtras()).getString(Auxiliar.id));
                                startActivity(intent);
                            } else {
                                Toast.makeText(context, getString(R.string.acercate), Toast.LENGTH_SHORT).show();
                            }
                        } else {
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
