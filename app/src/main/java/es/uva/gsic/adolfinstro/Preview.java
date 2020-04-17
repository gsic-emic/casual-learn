package es.uva.gsic.adolfinstro;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.preference.PreferenceManager;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.BitmapFactory;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.StrictMode;
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

import es.uva.gsic.adolfinstro.auxiliar.Auxiliar;

public class Preview extends AppCompatActivity {

    private Context context;
    private MapView map;
    private MyLocationNewOverlay myLocationNewOverlay;

    private RecepcionNotificaciones recepcionNotificaciones;
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
                    .tag("imagen")
                    .into(imageView);
            map = findViewById(R.id.mapPreview);
            imageView.setVisibility(View.VISIBLE);
        }else{
            if(extras.getString(Auxiliar.recursoImagen) != null) {
                Picasso.get()
                        .load(extras.getString(Auxiliar.recursoImagen))
                        .placeholder(R.drawable.ic_cloud_download_blue_80dp)
                        .tag("imagen")
                        .into(imageView);
                map = findViewById(R.id.mapPreview);
                imageView.setVisibility(View.VISIBLE);
            } else{
                //imageView.setImageResource(R.drawable.ic_camera_roll_black_24dp);
                map = findViewById(R.id.mapPreviewC);
            }
        }
        map.setVisibility(View.VISIBLE);
        map.setTileSource(TileSourceFactory.MAPNIK);
        IMapController mapController = map.getController();

        GeoPoint posicionTarea = new GeoPoint(extras.getDouble(Auxiliar.latitud), extras.getDouble(Auxiliar.longitud));

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
        TextView tipoTarea = findViewById(R.id.tipoPreview);
        tipoTarea.setText(String.format("%s%s", getString(R.string.tipoDeTarea), extras.getString(Auxiliar.tipoRespuesta)));

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

    @Override
    public void onBackPressed(){
        Picasso.get().cancelTag(Auxiliar.cargaImagenPreview);
        Toast.makeText(context, "Pospuesta", Toast.LENGTH_SHORT).show();
        startActivity(new Intent(this, MainActivity.class));
    }


    public void boton(View view) {
        Intent intent;
        switch (view.getId()){
            case R.id.botonAceptarPreview:
                intent = new Intent(context, Tarea.class);
                intent.putExtras(getIntent().getExtras());
                startActivity(intent);
                break;
            case R.id.botonAhoraNoPreview:
                intent = new Intent();
                intent.setAction(Auxiliar.ahora_no);
                intent.putExtra(Auxiliar.id, getIntent().getExtras().getString(Auxiliar.id));
                sendBroadcast(intent);
                Auxiliar.returnMain(context);
                break;
            case R.id.botonRechazarPreview:
                intent = new Intent();
                intent.setAction(Auxiliar.nunca_mas);
                intent.putExtra(Auxiliar.id, getIntent().getExtras().getString(Auxiliar.id));
                sendBroadcast(intent);
                Auxiliar.returnMain(context);
                break;
        }

    }

    @Override
    protected void onResume(){
        super.onResume();
        recepcionNotificaciones = new RecepcionNotificaciones();
        registerReceiver(recepcionNotificaciones, Auxiliar.intentFilter());
        if(map != null)
            map.onResume();
    }
    @Override
    protected void onPause(){
        super.onPause();
        unregisterReceiver(recepcionNotificaciones);
        if(map != null)
            map.onPause();
    }
}
