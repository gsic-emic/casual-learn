package es.uva.gsic.adolfinstro;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
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
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;

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
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.ScaleBarOverlay;
import org.osmdroid.views.overlay.infowindow.InfoWindow;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import es.uva.gsic.adolfinstro.auxiliar.AdaptadorListaMapa;
import es.uva.gsic.adolfinstro.auxiliar.Auxiliar;
import es.uva.gsic.adolfinstro.auxiliar.Bocadillo;
import es.uva.gsic.adolfinstro.auxiliar.ColaConexiones;
import es.uva.gsic.adolfinstro.persistencia.PersistenciaDatos;

//https://github.com/osmdroid/osmdroid/wiki/How-to-use-the-osmdroid-library
public class Maps extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener, AdaptadorListaMapa.ItemClickListener {
    /** Objeto que permite mostrar el mapa*/
    private MapView map;

    private TextView sinPulsarTarea;
    private RecyclerView contenedor;
    /** Objeto que almacenará, entre otras cosas, la última posición conocida del usuario*/
    private MyLocationNewOverlay myLocationNewOverlay;
    /** Objeto tuilizado para centrar el mapa en un punto específico*/
    private IMapController mapController;
    /** Posición inicial del punto conocido */
    private double latitudeOrigen , longitudeOrigen;
    /** Punto del mapa en el que se centrará si no consigue recuperar la posición actual*/
    private final GeoPoint telecoPoint = new GeoPoint(41.662357, -4.706005);
    /** Referencia a si la opción "no Molestar" está activada o no*/
    private boolean noMolestar;
    /** Preferencias de la aplicación */
    private SharedPreferences sharedPreferences;
    /** Regla sbore el mapa*/
    private ScaleBarOverlay scaleBarOverlay;
    ///** Brújula del dispositivo*/
    //private CompassOverlay compassOverlay;
    /** Código de identificación para la solicitud de los permisos de la app */
    private final int requestCodePermissions = 1001;

    /** Canal utilizado para las notificaciones de las tareas */
    private NotificationChannel channel;
    /** Instancia del NotificationManager*/
    private NotificationManager notificationManager;

    private Context context;

    private long ultimaNotificacion;

    /*private long ultimaPosicionInstante;
    private double ultimaPosicionLatitud;
    private double ultimaPosicionLogintud;*/

    private final double nivelMin = 6.5;
    private final double nivelMax = 19.5;

    private Animation animation;

    private static AdaptadorListaMapa adaptadorListaMapa;


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
            setContentView(R.layout.activity_maps);
            map = findViewById(R.id.map);
            sinPulsarTarea = findViewById(R.id.tvTareasMapa);
            contenedor = findViewById(R.id.rvTareasMapa);
            RecyclerView.LayoutManager layoutManager;

            if(getResources().getConfiguration().orientation == android.content.res.Configuration.ORIENTATION_PORTRAIT) {
                layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
                animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.desplaza_horizontal);
            }
            else {
                layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
                animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.desplaza_vertical);
            }

            contenedor.setLayoutManager(layoutManager);

            map.setTileSource(TileSourceFactory.MAPNIK);
            map.setMultiTouchControls(true); //Habilitada la posibilidad de hacer zoom con dos dedos
            mapController = map.getController();
            if(PersistenciaDatos.existeTarea(getApplication(), PersistenciaDatos.ficheroInstantes, idInstanteGETZONA)) {
                JSONObject instante = PersistenciaDatos.recuperaTarea(getApplication(), PersistenciaDatos.ficheroInstantes, idInstanteGETZONA);
                try {
                    mapController.setCenter(new GeoPoint(instante.getDouble(Auxiliar.latitud), instante.getDouble(Auxiliar.longitud)));
                } catch (JSONException e) {
                    mapController.setCenter(telecoPoint);
                }
            }else {
                mapController.setCenter(telecoPoint); //Centramos la posición en algún lugar conocido
            }
            mapController.setZoom(nivelMax - 5);
            //RotationGestureOverlay rotationGestureOverlay = new RotationGestureOverlay(map);
            //rotationGestureOverlay.setEnabled(true);
            //map.getOverlays().add(rotationGestureOverlay);
            map.setMinZoomLevel(nivelMin);
            map.setMaxZoomLevel(nivelMax);
            GpsMyLocationProvider gpsMyLocationProvider = new GpsMyLocationProvider(context);
            gpsMyLocationProvider.setLocationUpdateMinDistance(5);
            gpsMyLocationProvider.setLocationUpdateMinTime(5000);
            gpsMyLocationProvider.addLocationSource(LocationManager.GPS_PROVIDER);
            gpsMyLocationProvider.addLocationSource(LocationManager.NETWORK_PROVIDER); //Utiliza red y GPS
            myLocationNewOverlay = new MyLocationNewOverlay(gpsMyLocationProvider, map);
            myLocationNewOverlay.enableMyLocation();
            myLocationNewOverlay.setDirectionArrow(BitmapFactory.decodeResource(getResources(), R.drawable.person),
                    BitmapFactory.decodeResource(getResources(), R.drawable.person));
            //myLocationNewOverlay.enableFollowLocation(); //Se activa que se aproxime a la posición del usuario
            myLocationNewOverlay.setEnableAutoStop(true);
            //map.getOverlays().add(myLocationNewOverlay); //Se centra en el usuario. Si no lo consigue porque la
            //posición aún está a null siempre se tiene el pto conocido

            final DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
            scaleBarOverlay = new ScaleBarOverlay(map);
            scaleBarOverlay.setCentred(true); //La barra de escala se queda en el centro
            //map.getOverlays().add(scaleBarOverlay);

            //Se agrega la brújula
            //compassOverlay = new CompassOverlay(context, new InternalCompassOrientationProvider(context), map);
            //compassOverlay.enableCompass();

            int ancho = displayMetrics.widthPixels;
            int alto = displayMetrics.heightPixels;
            if(getResources().getConfiguration().orientation == android.content.res.Configuration.ORIENTATION_PORTRAIT) {
                scaleBarOverlay.setScaleBarOffset((int)(ancho / 2), (int) (alto * 0.05)); //posición en el el display
                //compassOverlay.setCompassCenter(35, (int) (displayMetrics.heightPixels * 0.2 + 50)); //posicón de la brújula
            }else{
                scaleBarOverlay.setScaleBarOffset((int)(ancho*0.4), (int) (alto * 0.05));

                //compassOverlay.setCompassCenter(200, (int) (displayMetrics.heightPixels * 0.1 + 25)); //posicón de la brújula
            }


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
                    if(map.getMapCenter() != null){
                        IGeoPoint centro = map.getMapCenter();
                        compruebaZona(centro.getLatitude(), centro.getLongitude());
                    }
                    return false;
                }

                @Override
                public boolean onZoom(ZoomEvent event) {//Zoom con botones
                    if(map.getMapCenter() != null){
                        IGeoPoint centro = map.getMapCenter();
                        compruebaZona(centro.getLatitude(), centro.getLongitude());
                    }
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
        //map.getOverlays().add(compassOverlay);
    }

    /*
    public void putItems() {
        newMarker(42.0076, -4.52449, "Ermita de San Juan Bautista, Palencia", 8);
        newMarker(42.0081, -4.5210, "San Marco, Palencia", 2);
        newMarker(42.0160, -4.5275, "Parroquia Reina Inmaculada", 1);
        newMarker(42.0094, -4.5296, "Parroquia de San Lazaro, Palencia", 5);
        newMarker(42.0114, -4.5321, "Iglesia de San Francisco, Palencia", 12);
    }*/

    public void pintaLista(Marcador marcador){
        JSONArray tareas = PersistenciaDatos.tareasPosicion(getApplication(), PersistenciaDatos.ficheroTareasZona, marcador.latitud, marcador.longitud);
        if(marcador.getLatitudes().size() > 0){//Es una agrupacion
            try {
                Map<Integer, Double> latitudes, longitudes;
                JSONArray masTareas;
                latitudes = marcador.getLatitudes();
                longitudes = marcador.getLongitudes();
                for(int j = 0; j< latitudes.size(); j++){
                    masTareas = PersistenciaDatos.tareasPosicion(getApplication(), PersistenciaDatos.ficheroTareasZona, latitudes.get(j), longitudes.get(j));
                    for(int t = 0; t < masTareas.length(); t++){
                            tareas.put(masTareas.get(t));
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        if(tareas.length() > 0){
            sinPulsarTarea.setVisibility(View.GONE);
            contenedor.setVisibility(View.VISIBLE);
            contenedor.setHasFixedSize(true);

            List<TareasMapaLista> tareasPunto = new ArrayList<>();
            JSONObject jo;
            for(int i = 0; i < tareas.length(); i++){
                try {
                    jo = tareas.getJSONObject(i);
                    tareasPunto.add( new TareasMapaLista(jo.getString(Auxiliar.id), jo.getString(Auxiliar.titulo), Auxiliar.ultimaParte(jo.getString(Auxiliar.tipoRespuesta))));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            adaptadorListaMapa = new AdaptadorListaMapa(this, tareasPunto);
            adaptadorListaMapa.setClickListener(this);
            contenedor.setAdapter(adaptadorListaMapa);
            contenedor.startAnimation(animation);

        }else {
            sinPulsarTarea.setVisibility(View.VISIBLE);
            contenedor.setVisibility(View.GONE);
        }
    }

    @Override
    public void onItemClick(View view, int posicion){
        try {
            JSONObject tarea = PersistenciaDatos.recuperaTarea(getApplication(), PersistenciaDatos.ficheroTareasZona, adaptadorListaMapa.getId(posicion));
            tarea.put(Auxiliar.origen, PersistenciaDatos.ficheroTareasZona);
            Intent intent = new Intent(this, Preview.class);
            intent.putExtra(Auxiliar.previa, Auxiliar.mapa);
            intent.putExtra(Auxiliar.id, tarea.getString(Auxiliar.id));
            startActivity(intent);
            tarea.put(Auxiliar.fechaUltimaModificacion, Auxiliar.horaFechaActual());
            tarea.put(Auxiliar.tipoRespuesta, Auxiliar.ultimaParte(tarea.getString(Auxiliar.tipoRespuesta)));
            PersistenciaDatos.guardaJSON(getApplication(), PersistenciaDatos.ficheroNotificadas, tarea, Context.MODE_PRIVATE);
        }catch (Exception e){
            //
        }

    }


    private Bitmap generaBitmapMarkerNumero(int size) {
        int dimen = 72;
        float mitad = (float)dimen/2;
        Drawable drawable = context.getResources().getDrawable(R.drawable.ic_marker);
        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        //Bitmap bitmap = Bitmap.createBitmap(dimen, dimen, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        Paint paint = new Paint();
        /*paint.setARGB(255, 0, 0, 0);
        canvas.drawCircle(mitad, mitad, mitad, paint);
        float radio = mitad - 5;
        paint.setARGB(255, 255, 255, 255);
        canvas.drawCircle(mitad, mitad, radio, paint);*/

        paint.setARGB(255, 0, 0, 0);


        paint.setStyle(Paint.Style.FILL);
        int textSize = (int) (mitad+1);
        paint.setTextSize(textSize);
        paint.setTextAlign(Paint.Align.CENTER);
        String texto;
        if(size>99)
            texto = "99+";
        else
            texto = String.valueOf(size);
        canvas.drawText(texto, mitad, mitad + (float)textSize/3, paint);
        return bitmap;
    }

    /*/**
     * Método para representar las tareas de dentro de un marcador mediante quesitos.
     *
     * @deprecated Sustituido por generaBitmapMarkerNumer(int numeroTareas)
     * @param listaTareas tipo de tareas que están presentes en el marcador
     * @return Bitmap a representar
     */
    /*private Bitmap generaBitmapMarker(List<String> listaTareas){
        Bitmap bitmap = Bitmap.createBitmap(50, 50, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint();
        float xy = (float)bitmap.getWidth()/2;
        float radio = xy/2;
        List<String> tipoTarea = new ArrayList<>();
        List<Integer> numeroTipoTarea = new ArrayList<>();

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
        int casos = 7;
        float angulo = (float)360/casos;
        float anguloInicio;
        anguloInicio = 0;
        RectF rectF = new RectF(0, 0, bitmap.getWidth(), bitmap.getHeight());
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
            canvas.drawArc(rectF, anguloInicio, angulo, true, paint);
            anguloInicio += angulo;
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
    }**/

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

    //private LocationManager locationManager;

    /**
     * Se restaura el mapa tal y como se indica en la guía.
     */
    @Override
    public void onResume() {
        super.onResume();
        if(!noMolestar) {
            //locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
            try {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                        && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    checkPermissions();
                } //else {
                    //locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 15000, 0, this);
                    //Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    //if(location!=null){
                        //mapController.setCenter(new GeoPoint(location.getLatitude(), location.getLongitude()));
                        //compruebaZona(location.getLatitude(), location.getLongitude());
                    //}
                //}
            } catch (Exception e) {
                //
            }
            if (map != null)
                map.onResume();
            if(estadoContenedor != null){
                contenedor.setVisibility(View.VISIBLE);
                sinPulsarTarea.setVisibility(View.GONE);
                contenedor.getLayoutManager().onRestoreInstanceState(estadoContenedor.getParcelable("CONTENEDOR"));
                contenedor.setAdapter(adaptadorListaMapa);
            }
        }
        //else
            //locationManager = null;
    }

    private void compruebaZona(double latitud, double longitud) {
        //Latitiud desde donde se han recuperado las tareas del servidor
        double latitudGet = 0;
        //Longitud desde donde se han recuperado las tareas del servidor
        double longitudGet = 0;
        //Se comprueba si existe el fichero y el objeto
        if(PersistenciaDatos.existeTarea(getApplication(), PersistenciaDatos.ficheroInstantes, idInstanteGETZONA)){
            try {
                JSONObject instante = PersistenciaDatos.recuperaTarea(getApplication(), PersistenciaDatos.ficheroInstantes, idInstanteGETZONA);
                latitudGet = instante.getDouble(Auxiliar.latitud);
                longitudGet = instante.getDouble(Auxiliar.longitud);
            }catch (Exception e){
                //
            }
        }
        double radio = Auxiliar.calculaDistanciaDosPuntos(latitud, longitud,
                map.getBoundingBox().getLatSouth(), map.getBoundingBox().getLonWest());
        if(latitudGet==0 || longitudGet==0){//Inicio del servicio, se tiene que recuperar la tarea del servidor
            peticionTareasServidor(latitud, longitud, Math.min(2 * radio + 0.1, 1.5));
        }else{
            if(map.getZoomLevelDouble() >= (nivelMax - 4) ){
            //if(radio<=1){
                double distanciaOrigen = Auxiliar.calculaDistanciaDosPuntos(latitud, longitud, latitudGet, longitudGet);
                if(distanciaOrigen >= radio){//Las tareas en local están obsoletas, hay que pedir unas nuevas al servidor
                    peticionTareasServidor(latitud, longitud, Math.min(2 * radio + 0.1, 1.5));
                }else{
                    map.getOverlays().clear();
                    pintaItemsfijos();
                    pintaZona(latitud, longitud);
                }
            }else {//El fichero sigue siendo válido
                map.getOverlays().clear();
                pintaItemsfijos();
                pintaZona(latitud, longitud);
            }
        }
    }

    final double[] distanciaAgrupacion = {0, 0.05, 0.1, 0.2, 0.4, 0.8, 1.6, 3.2};
    private void pintaZona(double latitude, double longitude){
        List<GeoPoint> puntos = new ArrayList<>();
        puntos.add(new GeoPoint(latitude, longitude, map.getZoomLevelDouble()));
        puntos.add(new GeoPoint(map.getBoundingBox().getLatSouth(), map.getBoundingBox().getLonWest()));

        double nivelZum = puntos.get(0).getAltitude();
        int caso;
        if(nivelZum > nivelMax - 2){
            caso = 0;
        }else{
            if(nivelZum > nivelMax - 2.5){
                caso = 1;
            }else {
                if (nivelZum > nivelMax - 3) {
                    caso = 2;
                } else {
                    if (nivelZum > nivelMax - 4) {
                        caso = 3;
                    } else {
                        if (nivelZum > nivelMax - 5) {
                            caso = 4;
                        } else {
                            if (nivelZum > nivelMax - 6) {
                                caso = 5;
                            } else {
                                if(nivelZum > nivelMax - 7)
                                    caso = 6;
                                else{
                                    caso = 7;
                                }
                            }
                        }
                    }
                }
            }
        }

        double distanciaMax = Auxiliar.calculaDistanciaDosPuntos(latitude, longitude,
                puntos.get(1).getLatitude(), puntos.get(1).getLongitude());

        nivelZum = distanciaAgrupacion[caso];
        JSONArray todasTareas = PersistenciaDatos.leeFichero(getApplication(), PersistenciaDatos.ficheroTareasZona);
        JSONObject tarea;
        List<Marcador> listaMarcadores = new ArrayList<>();
        Marcador marcador;
        double latitud, longitud;
        Map<Integer, Double> latitudes, longitudes;
        boolean anterior = false, anterior2 = false;
        try {
            while (todasTareas.length() > 0) {//Barro todas las tareas disponibles en el fichero
                tarea = (JSONObject)todasTareas.remove(0);
                latitud = tarea.getDouble(Auxiliar.latitud);
                longitud = tarea.getDouble(Auxiliar.longitud);
                //Comprobación de la zona
                if(Auxiliar.calculaDistanciaDosPuntos(latitude, longitude, latitud, longitud) <= distanciaMax) {
                    //Si no hay ningún punto guardado se guarda directamente
                    if (listaMarcadores.isEmpty()) {
                        marcador = new Marcador();
                        marcador.setTitulo(getResources().getString(R.string.tareaIndividual));
                        marcador.setPosicionMarcador(tarea.getDouble(Auxiliar.latitud), tarea.getDouble(Auxiliar.longitud));
                        marcador.incrementaTareas();
                        listaMarcadores.add(marcador);
                    } else {
                        for (int i = 0; i < listaMarcadores.size(); i++) {
                            anterior = false;
                            marcador = listaMarcadores.get(i);
                            if (latitud == marcador.getLatitud() &&
                                    longitud == marcador.getLongitud()) { //La tarea es de la misma posición
                                marcador.incrementaTareas();
                                listaMarcadores.set(i, marcador);
                                anterior = true;
                                break;
                            } else {//Se comprueba la distancia a la tarea del marcador
                                if (Auxiliar.calculaDistanciaDosPuntos(marcador.latitud, marcador.longitud,
                                        latitud, longitud)
                                        <= nivelZum) { //Se agrega al marcador ya que se debe agrupar
                                    marcador.setTitulo(getString(R.string.agrupacionTareas));
                                    marcador.incrementaTareas();
                                    if (marcador.getLatitudes().isEmpty()) {
                                        marcador.agregaPosicion(latitud, longitud);
                                    } else {
                                        latitudes = marcador.getLatitudes();
                                        longitudes = marcador.getLongitudes();
                                        for (int j = 0; j < latitudes.size(); j++) {
                                            anterior2 = false;
                                            if (latitudes.get(j) == latitud && longitudes.get(j) == longitud) {
                                                anterior2 = true;
                                                break;
                                            }
                                        }
                                        if (!anterior2) {//No existía la posición en la tarea
                                            marcador.agregaPosicion(latitud, longitud);
                                        }
                                    }
                                    listaMarcadores.set(i, marcador);
                                    anterior = true;
                                    break;
                                }
                            }
                        }
                        if (!anterior) {//Hay que agregar un nuevo marcador
                            marcador = new Marcador();
                            //marcador.setTitulo(tarea.getString(Auxiliar.titulo));
                            marcador.setTitulo(getResources().getString(R.string.tareaIndividual));
                            marcador.setPosicionMarcador(tarea.getDouble(Auxiliar.latitud), tarea.getDouble(Auxiliar.longitud));
                            marcador.incrementaTareas();
                            listaMarcadores.add(marcador);
                        }
                    }
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }

        if(!listaMarcadores.isEmpty()){
            for(Marcador m : listaMarcadores){
                newMarker(m);
            }
        }
    }

    /**
     * Se pausa el mapa tal y como indica la guía
     */
    private static Bundle estadoContenedor;
    @Override
    public void onPause(){
        super.onPause();
        if(map != null)
            map.onPause();
        if(contenedor != null && contenedor.getVisibility() == View.VISIBLE){
            estadoContenedor = new Bundle();
            estadoContenedor.putParcelable("CONTENEDOR", contenedor.getLayoutManager().onSaveInstanceState());
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        /*if(locationManager != null) {
            locationManager.removeUpdates(this);
        }*/
    }

    /**
     * Método que responde a la pulsación del alguno de los botones
     * @param view Instancia del botón pulsado que ha lanzado el método
     */
    public void boton(View view) {
        switch (view.getId()){
            case R.id.btCentrar: //Solo centra la posición si se ha conseguido recuperar
                if(myLocationNewOverlay.getMyLocation() != null) {
                    mapController.setZoom(nivelMax);
                    mapController.setCenter(myLocationNewOverlay.getMyLocation());
                    //onLocationChanged(myLocationNewOverlay.getMyLocationProvider().getLastKnownLocation());
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
    protected void onSaveInstanceState(@NotNull Bundle bundle){
        if(map != null && map.getMapCenter() != null) {
            IGeoPoint puntoCentral = map.getMapCenter();
            if (puntoCentral != null) {
                latitudeOrigen = puntoCentral.getLatitude();
                longitudeOrigen = puntoCentral.getLongitude();
            }
        }
        if(map!=null)
            bundle.putDouble("ZUM", map.getZoomLevelDouble());

        /*if(contenedor.getVisibility() == View.VISIBLE){
            bundle.putParcelable("CONTENEDOR", contenedor.getLayoutManager().onSaveInstanceState());
        }*/

        bundle.putDouble("LATITUDE", latitudeOrigen);
        bundle.putDouble("LONGITUDE", longitudeOrigen);
        bundle.putLong("ULTIMANOTIFICACION", ultimaNotificacion);
        super.onSaveInstanceState(bundle);
    }

    /**
     * Método que se llamará al restaurar la actividad
     * @param bundle Bundle
     */
    @Override
    protected void onRestoreInstanceState(@NotNull Bundle bundle){
        super.onRestoreInstanceState(bundle);
        try {
            mapController.setZoom(bundle.getDouble("ZUM"));
        }catch (Exception e){
            //
        }

        latitudeOrigen = bundle.getDouble("LATITUDE");
        longitudeOrigen = bundle.getDouble("LONGITUDE");
        ultimaNotificacion = bundle.getLong("ULTIMANOTIFICACION");
        GeoPoint lastCenter = new GeoPoint(latitudeOrigen, longitudeOrigen);
        mapController.setCenter(lastCenter);
        /*try{
            adaptadorListaMapa = (AdaptadorListaMapa) bundle.getSerializable("ADAPTADOR");
            contenedor.setAdapter(adaptadorListaMapa);
            contenedor.getLayoutManager().onRestoreInstanceState(bundle.getParcelable("CONTENEDOR"));
            contenedor.setVisibility(View.VISIBLE);
            sinPulsarTarea.setVisibility(View.GONE);
        }catch (Exception e){
            e.printStackTrace();
        }*/
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
                break;
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

    /*@Override
    public void onLocationChanged(Location location){
        if(PersistenciaDatos.tieneObjetos(getApplication(), PersistenciaDatos.ficheroTareasZona)){//El fichero tiene tareas que representar
            if(Proceso.tareasActualizadas || primerosPuntos){
                Proceso.tareasActualizadas = false;
                map.getOverlays().clear();
                try {
                    JSONArray arrayTareas = PersistenciaDatos.leeFichero(getApplication(), PersistenciaDatos.ficheroTareasZona);
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
                        newMarker(j.getDouble(Auxiliar.latitud), j.getDouble(Auxiliar.longitud), j.getString(Auxiliar.titulo), posiciones.get(j).size());
                    }
                    primerosPuntos = false;
                    map.invalidate();
                }catch (Exception e){
                    //
                }
                pintaItemsfijos();
            }
        }
        else{
            map.getOverlays().clear();
            pintaItemsfijos();
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

    }*/

    /**
     * Método que recupera del servidor las tareas que se encuentral cerca del usuario para lanzar las notificiaciones
     * cuando sea preciso.
     * @param latitud Latitud
     * @param longitud Longitud
     * @param radio Radio de la consulta
     */
    private void peticionTareasServidor(final double latitud, final double longitud, double radio){
        String url = "http://192.168.1.14:8080/tareas?latitude="+latitud
                +"&longitude="+longitud
                +"&radio="+radio;
        JsonArrayRequest jsonObjectRequest = new JsonArrayRequest(Request.Method.GET, url, null, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                JSONArray nuevasTareas = new JSONArray();
                JSONObject j;
                boolean guarda;
                for(int i = 0; i < response.length(); i++){
                    try {
                        j = response.getJSONObject(i);
                        if(Auxiliar.tareaRegistrada(getApplication(), j.getString(Auxiliar.id))){
                            if(PersistenciaDatos.existeTarea(getApplication(), PersistenciaDatos.ficheroNotificadas, j.getString(Auxiliar.id))){
                                try {
                                    PersistenciaDatos.obtenTarea(getApplication(), PersistenciaDatos.ficheroNotificadas, j.getString(Auxiliar.id));
                                    guarda = !PersistenciaDatos.existeTarea(getApplication(), PersistenciaDatos.ficheroTareasZona, j.getString(Auxiliar.id));
                                } catch (Exception e) {
                                    guarda = false;
                                }
                            } else {
                                guarda = false;
                            }
                        }else{
                            guarda = !PersistenciaDatos.existeTarea(getApplication(), PersistenciaDatos.ficheroTareasZona, j.getString(Auxiliar.id));
                        }
                        if(guarda)
                            nuevasTareas.put(j);
                    } catch (JSONException e) {
                        //Error al extrar el json
                    }
                }
                if(nuevasTareas.length()>0)
                    PersistenciaDatos.guardaArray(getApplication(), PersistenciaDatos.ficheroTareasZona, nuevasTareas );
                try {
                    j = new JSONObject();
                    j.put(Auxiliar.id, idInstanteGETZONA);
                    j.put(Auxiliar.latitud, latitud);
                    j.put(Auxiliar.longitud, longitud);
                    j.put(Auxiliar.instante, new Date().getTime());
                    PersistenciaDatos.reemplazaJSON(getApplication(), PersistenciaDatos.ficheroInstantes, j);
                }catch (JSONException e){
                    //No se ha guardado el get en el registro, volverá a pedirlo en la siguiente iteración
                }
                pintaZona(latitud, longitud);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                pintaZona(latitud, longitud);
            }

        });
        ColaConexiones.getInstance(getApplicationContext()).getRequestQueue().add(jsonObjectRequest);
    }

    /**
     * Método que se utiliza para agregar un marcador al mapa
     * @param marcador Contiene toda la información necesaria para agregar el marcador al mapa y cuando se pulse mostrar la lista de tareas
     */
    void newMarker(final Marcador marcador) {
        Marker marker = new Marker(map);
        marker.setPosition(new GeoPoint(marcador.getLatitud(), marcador.getLongitud()));
        BitmapDrawable d = new BitmapDrawable(getResources(), generaBitmapMarkerNumero(marcador.getNumeroTareas()));
        marker.setIcon(d);
        marker.setInfoWindow(new Bocadillo(R.layout.bocadillo, map));

        marker.setTitle(marcador.getTitulo());
        marker.setOnMarkerClickListener(new Marker.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker, MapView mapView) {
                InfoWindow.closeAllInfoWindowsOn(mapView);
                double distancia;
                String msg;
                try {
                    distancia = calculaDistanciaDosPuntos(myLocationNewOverlay.getMyLocation(), marker.getPosition());
                    msg = String.format(Locale.getDefault(), " %.3f km", distancia);
                } catch (Exception e) {
                    msg = getString(R.string.recuperandoPosicion);
                }
                marker.setSubDescription(msg);
                marker.showInfoWindow();
                pintaLista(marcador);
                return false;
            }
        });
        map.getOverlays().add(marker);
        map.invalidate();
    }

    private String idInstanteGETZONA = "instanteGETZONA";

    /**
     * Estrucutra de la lista de Tareas. Se va a utilizar en los infladores
     */
    public static class TareasMapaLista {
        public String id, titulo, tipoTarea;
        TareasMapaLista(String id, String titulo, String tipoTarea){
            this.id = id;
            this.titulo = titulo;
            this.tipoTarea = tipoTarea;
        }
    }

    private static class Marcador{
        private String titulo;
        private double latitud, longitud;
        private Map<Integer, Double> latitudes, longitudes;
        private int numeroTareas;
        private int posiciones;
        Marcador(){
            titulo = null;
            latitud = 0;
            longitud = 0;
            resto();
        }

        Marcador(String titulo, double latitud, double longitud){
            this.titulo = titulo;
            this.latitud = latitud;
            this.longitud = longitud;
            resto();
        }

        private void resto(){
            latitudes = new HashMap<>();
            longitudes = new HashMap<>();
            posiciones = 0;
        }

        void setTitulo(String titulo) {
            this.titulo = titulo;
        }

        String getTitulo(){
            return titulo;
        }

        void setPosicionMarcador(double latitud, double longitud) {
            this.latitud = latitud;
            this.longitud = longitud;
        }

        double getLatitud() {
            return latitud;
        }

        double getLongitud() {
            return longitud;
        }

        void agregaPosicion(double latitud, double longitud){
            latitudes.put(posiciones, latitud);
            longitudes.put(posiciones, longitud);
            ++posiciones;
        }

        Map<Integer, Double> getLatitudes(){
            return latitudes;
        }

        Map<Integer, Double> getLongitudes() {
            return longitudes;
        }

        void incrementaTareas(){
            ++numeroTareas;
        }

        public int getNumeroTareas() {
            return numeroTareas;
        }
    }

    private class PintaMarcadores extends AsyncTask<Object, Void, Void> {

        @Override
        protected Void doInBackground(Object... objects) {
            return null;
        }
    }


}
