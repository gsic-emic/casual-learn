package es.uva.gsic.adolfinstro;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonArrayRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.osmdroid.api.IMapController;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.CustomZoomButtonsController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import es.uva.gsic.adolfinstro.auxiliar.AdaptadorListaMapa;
import es.uva.gsic.adolfinstro.auxiliar.Auxiliar;
import es.uva.gsic.adolfinstro.auxiliar.ColaConexiones;
import es.uva.gsic.adolfinstro.auxiliar.TareasMapaLista;
import es.uva.gsic.adolfinstro.persistencia.PersistenciaDatos;

/**
 * Clase para mostrar toda la información disponible de un contexto. Carga dinámica de las tareas del
 * contexto. El orden viene determinado por lo que indique el sistema de recomendación.
 *
 * @author Pablo
 * @version 20210202
 */
public class PuntoInteres extends AppCompatActivity implements LocationListener, AdaptadorListaMapa.ItemClickListener {

    private Context context;
    private JSONObject lugar;
    private MapView map;
    private String textoSpeaker;
    private ImageView ivSpeaker;
    private List<String> permisos;
    private LocationManager locationManager;
    private TextView distanciaTexto, textoLugar, textoLugarReducido;
    private TextToSpeech textToSpeech;
    private AdaptadorListaMapa adaptadorListaMapa;
    private RecyclerView contenedorTareas;
    private Location posicion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
        setContentView(R.layout.activity_punto_interes);

        context = this;

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        String idContexto = Objects.requireNonNull(getIntent().getExtras()).getString(Auxiliar.contexto);
        try{
            String idUsuario = PersistenciaDatos.recuperaTarea(
                    getApplication(),
                    PersistenciaDatos.ficheroUsuario,
                    Auxiliar.id
            ).getString(Auxiliar.uid);
            lugar = PersistenciaDatos.recuperaObjeto(
                    getApplication(),
                    PersistenciaDatos.ficheroContextosNotificados,
                    Auxiliar.contexto,
                    idContexto,
                    idUsuario);
            if(lugar.has(Auxiliar.enlaceWiki)){
                ImageView imageWikiPedia = findViewById(R.id.ivWikiInfoPunto);
                imageWikiPedia.setVisibility(View.VISIBLE);
                try {
                    final String enlaceWiki = lugar.getString(Auxiliar.enlaceWiki);
                    imageWikiPedia.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Auxiliar.navegadorInterno(PuntoInteres.this, enlaceWiki);
                        }
                    });
                } catch (Exception e) {
                    imageWikiPedia.setVisibility(View.INVISIBLE);
                }
            }

            map = findViewById(R.id.mapInfoPunto);
            map.setTileSource(TileSourceFactory.MAPNIK);

            IMapController mapController = map.getController();

            double latitud = lugar.getDouble(Auxiliar.latitud);
            double longitud = lugar.getDouble(Auxiliar.longitud);

            GeoPoint posicionTarea = new GeoPoint(latitud, longitud);

            mapController.setZoom(17.5);
            mapController.setCenter(posicionTarea);
            map.setMaxZoomLevel(17.5);
            map.setMinZoomLevel(17.5);
            map.getZoomController().setVisibility(CustomZoomButtonsController.Visibility.NEVER);
            map.setMultiTouchControls(false);
            map.setTilesScaledToDpi(true);
            map.setClickable(false);
            map.setEnabled(false);
            map.invalidate();

            ((TextView) findViewById(R.id.tvTituloInfoPunto)).setText(lugar.getString(Auxiliar.label));


            distanciaTexto = findViewById(R.id.tvDistanciaInfoPunto);

            textoLugar = findViewById(R.id.textoInfoPunto);
            textoLugar.setText(Auxiliar.creaEnlaces(context, lugar.getString(Auxiliar.comment), false));
            textoLugar.setMovementMethod(LinkMovementMethod.getInstance());

            textoLugarReducido = findViewById(R.id.textoReducidoInfoPunto);
            textoLugarReducido.setText(Auxiliar.quitaEnlaces(lugar.getString(Auxiliar.comment)));

            textoLugar.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    if (textoLugar.getLineCount() > 0) {
                        textoLugar.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        if(textoLugar.getLineCount() > 5){
                            textoLugar.setVisibility(View.GONE);
                            textoLugarReducido.setVisibility(View.VISIBLE);
                        }
                    }
                }
            });

            textoSpeaker = String.format(
                    "%s\n%s", lugar.getString(Auxiliar.label),
                    Auxiliar.quitaEnlaces(lugar.getString(Auxiliar.comment)));

            Marker marker = new Marker(map);
            marker.setPosition(posicionTarea);
            marker.setIcon(ResourcesCompat.getDrawable(this.getResources(), R.drawable.ic_marcador_uno, null));
            marker.setInfoWindow(null);
            map.getOverlays().add(marker);
            map.invalidate();
            ivSpeaker = findViewById(R.id.ivSpeakerInfoPunto);
            ivSpeaker.setImageDrawable(ResourcesCompat.getDrawable(context.getResources(), R.drawable.ic_speaker, null));

            contenedorTareas = findViewById(R.id.rvTareasInfoPunto);

            contenedorTareas.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));
        }catch (Exception e){
            Intent intent = new Intent(context, Maps.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finishAffinity();
        }
    }

    private void peticionTareasPersonalizadas(@Nullable final String enlaceWiki) {
        List<String> keys = new ArrayList<>();
        List<Object> objects = new ArrayList<>();
        try {
            String idUsuario = PersistenciaDatos.recuperaTarea(
                    getApplication(), PersistenciaDatos.ficheroUsuario, Auxiliar.id).getString(Auxiliar.uid);
            keys.add(Auxiliar.contextos); objects.add(lugar.getString(Auxiliar.contexto));
            keys.add(Auxiliar.id); objects.add(idUsuario);
            String url = Auxiliar.creaQuery(Auxiliar.rutaTareas, keys, objects);

            JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(
                    Request.Method.GET,
                    url,
                    null,
                    new Response.Listener<JSONArray>() {
                        @Override
                        public void onResponse(JSONArray response) {
                            if(response != null){
                                try {
                                    JSONObject tarea;
                                    JSONArray tareasGuardar = new JSONArray();
                                    for (int i = 0; i < response.length(); i++) {
                                        tarea = response.getJSONObject(i);
                                        if (!tarea.has(Auxiliar.latitud) || !tarea.has(Auxiliar.longitud)) {
                                            tarea.put(Auxiliar.latitud, lugar.getDouble(Auxiliar.latitud));
                                            tarea.put(Auxiliar.longitud, lugar.getDouble(Auxiliar.longitud));
                                        }
                                        if (enlaceWiki != null)
                                            tarea.put(Auxiliar.enlaceWiki, enlaceWiki);
                                        if (!tarea.has(Auxiliar.comment) || tarea.getString(Auxiliar.comment).equals(""))
                                            tarea.put(Auxiliar.comment, lugar.getString(Auxiliar.label));
                                        tareasGuardar.put(tarea);
                                    }
                                    boolean actualiza = false;
                                    if(tareasGuardar.length() > 0)
                                            actualiza = PersistenciaDatos.guardaFichero(
                                                    getApplication(),
                                                    PersistenciaDatos.ficheroTareasPersonalizadas,
                                                    tareasGuardar,
                                                    Context.MODE_PRIVATE
                                            );
                                    if(actualiza){
                                        pintaTareas(tareasGuardar);
                                    }
                                }catch (Exception e){
                                    e.printStackTrace();
                                }
                            }
                        }
                    },
                    null);
            jsonArrayRequest.setRetryPolicy(new DefaultRetryPolicy(
                    18000,
                    3,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            ColaConexiones.getInstance(getApplicationContext()).getRequestQueue().add(jsonArrayRequest);
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume(){
        super.onResume();
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        permisos = new ArrayList<>();
        try {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                permisos.add(Manifest.permission.ACCESS_FINE_LOCATION);
            }
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
                if(!(ActivityCompat.checkSelfPermission(
                        context, Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                        == PackageManager.PERMISSION_GRANTED)) {
                    permisos.add(Manifest.permission.ACCESS_BACKGROUND_LOCATION);
                }

            if(permisos.contains(Manifest.permission.ACCESS_BACKGROUND_LOCATION)){
                permisos.remove(Manifest.permission.ACCESS_FINE_LOCATION);
            }

            if(!permisos.isEmpty()){
                solicitaPermisoUbicacion();
            } else {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 5, this);
                if(locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER) != null)
                    onLocationChanged(
                            locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER));
                else
                    distanciaTexto.setText(context.getResources().getString(R.string.recuperandoPosicion));

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if(map != null)
            map.onResume();

        textToSpeech = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status >= 0){
                    textToSpeech.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                        @Override
                        public void onStart(String utteranceId) {
                            ivSpeaker.setImageDrawable(ResourcesCompat.getDrawable(context.getResources(), R.drawable.ic_stop_24, null));
                        }

                        @Override
                        public void onDone(String utteranceId) {
                            ivSpeaker.setImageDrawable(ResourcesCompat.getDrawable(context.getResources(), R.drawable.ic_speaker, null));
                        }

                        @Override
                        public void onError(String utteranceId) {
                            ivSpeaker.setImageDrawable(ResourcesCompat.getDrawable(context.getResources(), R.drawable.ic_speaker, null));
                        }
                    });
                }
            }
        });
        if(textToSpeech.getEngines() == null)
            ivSpeaker.setVisibility(View.INVISIBLE);
        else if(textToSpeech.getEngines().size() <= 0)
            ivSpeaker.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onStart() {
        JSONArray tareasPersonalizadas = PersistenciaDatos.leeFichero(getApplication(), PersistenciaDatos.ficheroTareasPersonalizadas);
        try {
            if (tareasPersonalizadas == null || tareasPersonalizadas.length() == 0  || !tareasPersonalizadas.getJSONObject(0).getString(Auxiliar.contexto).equals(lugar.getString(Auxiliar.contexto))) {
                if(lugar.has(Auxiliar.enlaceWiki))
                    peticionTareasPersonalizadas(lugar.getString(Auxiliar.enlaceWiki));
                else
                    peticionTareasPersonalizadas(null);
            }
            else
                pintaTareas(tareasPersonalizadas);
        }catch (Exception e){
            e.printStackTrace();
        }
        super.onStart();
    }

    private void solicitaPermisoUbicacion() {
        final Dialog dialogoPermisos = new Dialog(context);
        dialogoPermisos.setContentView(R.layout.dialogo_permisos_ubicacion);
        dialogoPermisos.setCancelable(false);

        String textoPermisos = context.getString(R.string.necesidad_permisos);

        for(String s : permisos){
            switch (s){
                case Manifest.permission.ACCESS_FINE_LOCATION:
                    textoPermisos = String.format("%s%s",textoPermisos,context.getString(R.string.ubicacion_primer));
                    break;
                case Manifest.permission.WRITE_EXTERNAL_STORAGE:
                    textoPermisos = String.format("%s%s",textoPermisos,context.getString(R.string.permiso_almacenamiento));
                    break;
                default:
                    break;
            }
        }

        if(permisos.size() > 1 && permisos.contains(Manifest.permission.ACCESS_BACKGROUND_LOCATION)) {//Se necesitan mostrar dos dialogos
            final TextView tituloPermisos = dialogoPermisos.findViewById(R.id.tvTituloPermisos);
            tituloPermisos.setVisibility(View.GONE);
            final TextView textoPermiso = dialogoPermisos.findViewById(R.id.tvTextoPermisos);
            textoPermiso.setText(Html.fromHtml(textoPermisos));
            Button salir = dialogoPermisos.findViewById(R.id.btSalirPermisos);
            salir.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finishAffinity();
                }
            });
            final Button siguiente = dialogoPermisos.findViewById(R.id.btSiguientePermisos);
            siguiente.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    tituloPermisos.setVisibility(View.VISIBLE);
                    textoPermiso.setText(context.getString(R.string.texto_peticion_ubicacion_siempre));
                    siguiente.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if(dialogoPermisos.isShowing())
                                dialogoPermisos.cancel();
                            permisos.add(Manifest.permission.ACCESS_FINE_LOCATION);
                            ActivityCompat.requestPermissions(
                                    PuntoInteres.this,
                                    permisos.toArray(new String[permisos.size()]),
                                    1006);
                        }
                    });
                }
            });
        } else{
            TextView textView = dialogoPermisos.findViewById(R.id.tvTituloPermisos);
            if(permisos.contains(Manifest.permission.ACCESS_BACKGROUND_LOCATION)){//Solo muestro el de ubicación siempre
                textView.setVisibility(View.VISIBLE);
                Button salir = dialogoPermisos.findViewById(R.id.btSalirPermisos);
                salir.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        finishAffinity();
                    }
                });
                Button siguiente = dialogoPermisos.findViewById(R.id.btSiguientePermisos);
                siguiente.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(dialogoPermisos.isShowing())
                            dialogoPermisos.cancel();
                        permisos.add(Manifest.permission.ACCESS_FINE_LOCATION);
                        ActivityCompat.requestPermissions(
                                PuntoInteres.this,
                                permisos.toArray(new String[permisos.size()]),
                                1006);
                    }
                });
            }else {//Solo muestro el normal
                textView.setVisibility(View.GONE);
                textView = dialogoPermisos.findViewById(R.id.tvTextoPermisos);
                textView.setText(Html.fromHtml(textoPermisos));
                Button salir = dialogoPermisos.findViewById(R.id.btSalirPermisos);
                salir.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        finishAffinity();
                    }
                });
                Button siguiente = dialogoPermisos.findViewById(R.id.btSiguientePermisos);
                siguiente.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(dialogoPermisos.isShowing())
                            dialogoPermisos.cancel();
                        ActivityCompat.requestPermissions(
                                PuntoInteres.this,
                                permisos.toArray(new String[permisos.size()]),
                                1006);
                    }
                });
            }
        }
        dialogoPermisos.show();
    }

    private void textoInformativoDistancia(double tareaLat, double tareaLon, double usuarioLat, double usuarioLon){
        double distancia = Auxiliar.calculaDistanciaDosPuntos(
                tareaLat,
                tareaLon,
                usuarioLat,
                usuarioLon);
        if(distancia>=1)
            distanciaTexto.setText(String.format("%s %.2fkm", context.getResources().getString(R.string.distancia_linea_recta),distancia));
        else
            distanciaTexto.setText(String.format("%s %.2fm", context.getResources().getString(R.string.distancia_linea_recta),distancia*1000));
    }

    @Override
    protected void onPause(){
        super.onPause();
        if(map != null)
            map.onPause();
        if(locationManager != null)
            locationManager.removeUpdates(this);
        if(textToSpeech != null){
            textToSpeech.stop();
        }
        if(ivSpeaker != null)
            ivSpeaker.setImageDrawable(ResourcesCompat.getDrawable(context.getResources(), R.drawable.ic_speaker, null));
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        if(textToSpeech != null)
            textToSpeech.shutdown();
    }

    public void pintaTareas(JSONArray tareas) {
        try {
            if (tareas != null && tareas.length() > 0 && tareas.getJSONObject(0).getString(Auxiliar.contexto).equals(lugar.getString(Auxiliar.contexto))) {
                String idUsuario;
                try{
                    idUsuario = PersistenciaDatos.recuperaTarea(
                            getApplication(), PersistenciaDatos.ficheroUsuario, Auxiliar.id).getString(Auxiliar.uid);
                }catch (Exception e){
                    idUsuario = null;
                }
                List<String> listaId = Auxiliar.getListaTareasCompletadas(getApplication(), idUsuario);
                List<TareasMapaLista> tareasPunto = new ArrayList<>();
                JSONObject jo;
                String uriFondo;
                String id;
                for (int i = 0; i < tareas.length(); i++) {
                    try {
                        jo = tareas.getJSONObject(i);
                        try {
                            uriFondo = jo.getString(Auxiliar.recursoImagenBaja);
                        } catch (Exception e) {
                            uriFondo = null;
                        }
                        //Agrego el fichero de donde extraer la tarea
                        jo.put(Auxiliar.ficheroOrigen, PersistenciaDatos.ficheroTareasPersonalizadas);
                        id = jo.getString(Auxiliar.id);
                        if (listaId.contains(id))
                            tareasPunto.add(new TareasMapaLista(
                                    id,
                                    Auxiliar.quitaEnlaces(jo.getString(Auxiliar.recursoAsociadoTexto)).replace("<br>", ""),
                                    Auxiliar.ultimaParte(jo.getString(Auxiliar.tipoRespuesta)),
                                    uriFondo,
                                    jo,
                                    true));
                        else
                            tareasPunto.add(new TareasMapaLista(
                                    id,
                                    Auxiliar.quitaEnlaces(jo.getString(Auxiliar.recursoAsociadoTexto)).replace("<br>", ""),
                                    Auxiliar.ultimaParte(jo.getString(Auxiliar.tipoRespuesta)),
                                    uriFondo,
                                    jo,
                                    false));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                adaptadorListaMapa = new AdaptadorListaMapa(context, tareasPunto);
                adaptadorListaMapa.setClickListener(this);
                contenedorTareas.setAdapter(adaptadorListaMapa);
                contenedorTareas.setHasFixedSize(true);
                contenedorTareas.setVisibility(View.VISIBLE);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void boton(View view) {
        switch (view.getId()){
            case R.id.ivSpeakerInfoPunto:
                if(textToSpeech != null) {
                    if(textToSpeech.isSpeaking()) {
                        textToSpeech.stop();
                        ivSpeaker.setImageDrawable(ResourcesCompat.getDrawable(context.getResources(), R.drawable.ic_speaker, null));
                    }else{
                        if(textoSpeaker != null) {
                            HashMap<String, String> map = new HashMap<>();
                            map.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "speakerPreview");
                            textToSpeech.speak(
                                    textoSpeaker,
                                    TextToSpeech.QUEUE_FLUSH,
                                    map);
                        }
                    }
                }
                break;
            case R.id.btAmpliarInfoPunto:
                try {//Se salta a la tarea de navegación cuando el usuario pulse sobre el mapa
                    if(posicion != null){
                        Intent intent = new Intent(context, MapaNavegable.class);
                        intent.putExtra(Auxiliar.latitud + "user", posicion.getLatitude());
                        intent.putExtra(Auxiliar.longitud + "user", posicion.getLongitude());
                        intent.putExtra(Auxiliar.latitud + "task", lugar.getDouble(Auxiliar.latitud));
                        intent.putExtra(Auxiliar.longitud + "task", lugar.getDouble(Auxiliar.longitud));
                        startActivity(intent);
                    }else{
                        Toast.makeText(context, R.string.recuperandoPosicion, Toast.LENGTH_SHORT).show();
                    }
                }catch (JSONException e){
                    e.printStackTrace();
                }
                break;
            case R.id.textoReducidoInfoPunto:
                textoLugarReducido.setVisibility(View.GONE);
                textoLugar.setVisibility(View.VISIBLE);
                break;
            default:

                break;
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return false;
    }

    @Override
    public void onBackPressed(){
        Bundle extras = getIntent().getExtras();
        if (extras != null && extras.containsKey(Auxiliar.previa)) {
            switch (extras.getString(Auxiliar.previa)){
                case Auxiliar.mapa:
                    finish();
                    break;
                case Auxiliar.notificacion:
                default:
                    aMapas();
                    break;
            }
        }else{
            aMapas();
        }
    }

    private void aMapas(){
        Intent intent = new Intent(context, Maps.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(Auxiliar.textoParaElMapa, "");
        context.startActivity(intent);
        finish();
    }

    @Override
    public void onLocationChanged(Location location) {
        posicion = location;
        try {
            textoInformativoDistancia(
                    lugar.getDouble(Auxiliar.latitud), lugar.getDouble(Auxiliar.longitud),
                    location.getLatitude(), location.getLongitude());
        }catch (Exception e){
            distanciaTexto.setText(context.getResources().getString(R.string.recuperandoPosicion));
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

    @Override
    public void onItemClick(View view, int position) {
        try {
            if (posicion != null) {
                JSONObject tarea = adaptadorListaMapa.getTarea(position);
                tarea.put(Auxiliar.origen, tarea.getString(Auxiliar.ficheroOrigen));
                Intent intent = new Intent(context, Preview.class);
                String idUsuario;
                String idTarea = tarea.getString(Auxiliar.id);
                intent.putExtra(Auxiliar.id, idTarea);
                intent.putExtra(Auxiliar.posUsuarioLat, posicion.getLatitude());
                intent.putExtra(Auxiliar.posUsuarioLon, posicion.getLongitude());
                try {
                    idUsuario = Objects.requireNonNull(PersistenciaDatos.recuperaTarea(
                            getApplication(), PersistenciaDatos.ficheroUsuario, Auxiliar.id))
                            .getString(Auxiliar.uid);
                } catch (Exception e) {
                    idUsuario = null;
                }
                    String[] ficheros = {
                            PersistenciaDatos.ficheroTareasPospuestas,
                            PersistenciaDatos.ficheroTareasRechazadas,
                            PersistenciaDatos.ficheroDenunciadas};
                    JSONObject tareaAnterior = null;
                    String fichero = null;
                    for (String f : ficheros) {
                        tareaAnterior = PersistenciaDatos.obtenTarea(
                                getApplication(),
                                f,
                                idTarea,
                                idUsuario);
                        if (tareaAnterior != null) {
                            fichero = f;
                            break;
                        }
                    }
                    if (tareaAnterior != null) {//Se ha encontrado la tarea en uno de los ficheros
                        if (fichero.equals(ficheros[2])) {
                            PersistenciaDatos.guardaJSON(
                                    getApplication(),
                                    PersistenciaDatos.ficheroTareasRechazadas,
                                    tarea,
                                    Context.MODE_PRIVATE);
                            Toast.makeText(context, R.string.tareaDenunciadaAntes, Toast.LENGTH_SHORT).show();
                        } else {
                            if (fichero.equals(ficheros[0]))
                                intent.putExtra(Auxiliar.previa, Auxiliar.tareasPospuestas);
                            else
                                intent.putExtra(Auxiliar.previa, Auxiliar.tareasRechazadas);
                            PersistenciaDatos.guardaJSON(
                                    getApplication(),
                                    PersistenciaDatos.ficheroNotificadas,
                                    tareaAnterior,
                                    Context.MODE_PRIVATE);
                            startActivity(intent);
                        }
                    } else {
                        intent.putExtra(Auxiliar.previa, Auxiliar.mapa);
                        tarea.put(Auxiliar.idUsuario, idUsuario);
                        tarea.put(Auxiliar.fechaUltimaModificacion, Auxiliar.horaFechaActual());
                        tarea.put(Auxiliar.tipoRespuesta, Auxiliar.ultimaParte(tarea.getString(Auxiliar.tipoRespuesta)));
                        PersistenciaDatos.guardaJSON(
                                getApplication(),
                                PersistenciaDatos.ficheroNotificadas,
                                tarea,
                                Context.MODE_PRIVATE);
                        startActivity(intent);
                    }

            } else {
                Toast.makeText(context, R.string.recuperandoPosicion, Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}