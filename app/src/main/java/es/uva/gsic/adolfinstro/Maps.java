package es.uva.gsic.adolfinstro;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.Guideline;
import androidx.core.app.ActivityCompat;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Application;
import android.app.Dialog;
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
import android.os.Bundle;
import android.os.StrictMode;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import androidx.appcompat.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonArrayRequest;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

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
import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.CustomZoomButtonsController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.ScaleBarOverlay;
import org.osmdroid.views.overlay.infowindow.InfoWindow;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import es.uva.gsic.adolfinstro.auxiliar.AdaptadorListaCoincidencia;
import es.uva.gsic.adolfinstro.auxiliar.AdaptadorListaMapa;
import es.uva.gsic.adolfinstro.auxiliar.Auxiliar;
import es.uva.gsic.adolfinstro.auxiliar.Bocadillo;
import es.uva.gsic.adolfinstro.auxiliar.ColaConexiones;
import es.uva.gsic.adolfinstro.persistencia.PersistenciaDatos;

/**
 * Clase que gestiona la actividad principal de la aplicación.
 * @author Pablo
 * @version 20200831
 */
public class  Maps extends AppCompatActivity implements
        SharedPreferences.OnSharedPreferenceChangeListener,
        AdaptadorListaMapa.ItemClickListener,
        ActivityCompat.OnRequestPermissionsResultCallback,
        AdaptadorListaCoincidencia.ItemClickListenerDialogo {
    /** Objeto que permite mostrar el mapa*/
    private MapView map;

    /** Instancia del texto informativo en el que el usuario todavía no ha pulsado ningún marcador */
    private TextView sinPulsarTarea;
    /** Objeto donde se expone toda la lista de tareas del marcador */
    private RecyclerView contenedor, contenedorBusqMapa;

    /** Objeto que almacenará, entre otras cosas, la última posición conocida del usuario*/
    private MyLocationNewOverlay myLocationNewOverlay;
    /** Objeto tuilizado para centrar el mapa en un punto específico*/
    private IMapController mapController;
    /** Posición inicial del punto conocido */
    private double latitudeOrigen , longitudeOrigen;
    /** Referencia a si la opción "no Molestar" está activada o no*/
    private boolean noMolestar;
    /** Preferencias de la aplicación */
    private SharedPreferences sharedPreferences;
    /** Regla sbore el mapa*/
    private ScaleBarOverlay scaleBarOverlay;
    /** Código de identificación para la solicitud de los permisos de la app */
    private final int requestCodePermissions = 1001;

    /** Contexto */
    private Context context;

    /** Nivel de zum mínimo permitido */
    private final double nivelMin = 6.5;
    /** Nivel de zum máximo permitido */
    private final double nivelMax = 19.5;

    /** Clave para obtener la última posición y el zum del mapa */
    private final String idPosicionZoom = "posicionZum";

    private Animation animation;

    private static AdaptadorListaMapa adaptadorListaMapa;

    /** Identificador de la primera cuadrícula */
    final String idPrimeraCuadricula = "1C";

    private AlertDialog.Builder dialogoSalirApp;

    Boolean dialogoSalirAppActivo = false;

    private AlertDialog.Builder dialogoCerrarSesion;

    Boolean dialogoCerrarSesionActivo = false;

    Guideline guiaMapaH, guiaMapaV;

    Dialog dialogoCoincidecncias;

    private AdaptadorListaCoincidencia adaptadorListaCoincidencia;

    private RecyclerView.LayoutManager layoutManager;



    /**
     * Método con el que se pinta la actividad. Lo primero que comprueba es si está activada el modo no
     * molestar para saber si se tiene que mostar el mapa o no
     * @param savedInstanceState Bundle
     */
    @Override
    protected void onCreate(final Bundle savedInstanceState) {

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        context = getApplicationContext(); //contexto de la aplicación
        Configuration.getInstance().load(context, PreferenceManager.getDefaultSharedPreferences(context));
        Configuration.getInstance().setUserAgentValue(BuildConfig.APPLICATION_ID);
        super.onCreate(savedInstanceState);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        //sharedPreferences.registerOnSharedPreferenceChangeListener(this);

        onSharedPreferenceChanged(sharedPreferences, Ajustes.NO_MOLESTAR_pref);
        onSharedPreferenceChanged(sharedPreferences, Ajustes.LISTABLANCA_pref);

        dialogoSalirApp = new AlertDialog.Builder(this);
        dialogoSalirApp.setTitle(getString(R.string.exitT));
        dialogoSalirApp.setMessage(getString(R.string.exit));
        dialogoSalirApp.setPositiveButton(getString(R.string.salir), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //finishAffinity();//Se cierra la app. //El proceso puede seguir activo
                finish();
            }
        });
        dialogoSalirApp.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialogoSalirAppActivo = false;
            }
        });
        dialogoSalirApp.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                dialogoSalirAppActivo = false;
            }
        });

        dialogoSalirAppActivo = false;
        if(savedInstanceState != null && savedInstanceState.getBoolean("DIALOGOSALIR", false)) {
            dialogoSalirAppActivo = true;
            dialogoSalirApp.show();
        }


        final Application app = getApplication();
        dialogoCerrarSesion = new AlertDialog.Builder(this);
        dialogoCerrarSesion.setTitle(getString(R.string.cerrarSesion));
        dialogoCerrarSesion.setMessage(getString(R.string.cerrarSesionMensaje));
        dialogoCerrarSesion.setPositiveButton(getString(R.string.cerrarSesion), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                try {
                    if(PersistenciaDatos.borraTodosFicheros(app)) {
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putBoolean(Ajustes.NO_MOLESTAR_pref, false);
                        editor.commit();
                        editor.putString(Ajustes.HASHTAG_pref, getString(R.string.hashtag));
                        editor.commit();
                        editor.putInt(Ajustes.INTERVALO_pref, 4);
                        editor.commit();
                        Login.firebaseAuth.signOut();
                        Login.googleSignInClient.signOut().addOnCompleteListener(Maps.this, new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                vuelveLogin();
                            }
                        });
                    }else{
                        Toast.makeText(app, context.getResources().getString(R.string.errorOpera), Toast.LENGTH_SHORT).show();
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        });
        dialogoCerrarSesion.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialogoCerrarSesionActivo = false;
            }
        });
        dialogoCerrarSesion.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                dialogoCerrarSesionActivo = false;
            }
        });

        dialogoCerrarSesionActivo = false;
        if(savedInstanceState != null && savedInstanceState.getBoolean("DIALOGOCERRARSESION", false)) {
            dialogoCerrarSesionActivo = true;
            dialogoCerrarSesion.show();
        }

        dialogoCoincidecncias = new Dialog(this);
        dialogoCoincidecncias.setContentView(R.layout.dialogo_coincidencias);
        dialogoCoincidecncias.setCancelable(true);
        /*dialogoDesarrolladores.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                dialogoDesarrolladoresActivo = false;
            }
        });*/

        //Se decide si se muestra el mapa
        if (noMolestar) {
            setContentView(R.layout.no_molestar);
        } else {
            setContentView(R.layout.activity_maps);
            map = findViewById(R.id.map);
            sinPulsarTarea = findViewById(R.id.tvTareasMapa);
            contenedor = findViewById(R.id.rvTareasMapa);
            contenedorBusqMapa = findViewById(R.id.rvBusquedaMapa);

            guiaMapaH = findViewById(R.id.guiaMapa);
            guiaMapaV = findViewById(R.id.guiaMapaV);

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
            //https://github.com/osmdroid/osmdroid/blob/master/osmdroid-android/src/main/java/org/osmdroid/tileprovider/tilesource/TileSourceFactory.java
            /*final OnlineTileSourceBase WIKIMEDIA = new XYTileSource("Wikimedia",
                    1, 19, 256, ".png", new String[] {
                    "https://maps.wikimedia.org/osm-intl/" },
                    "Wikimedia maps | Map data © OpenStreetMap contributors",
                    new TileSourcePolicy(1,
                            TileSourcePolicy.FLAG_NO_BULK
                                    | TileSourcePolicy.FLAG_NO_PREVENTIVE
                                    | TileSourcePolicy.FLAG_USER_AGENT_MEANINGFUL
                                    | TileSourcePolicy.FLAG_USER_AGENT_NORMALIZED
                    ));
            map.setTileSource(WIKIMEDIA);*/

            //map.setTileSource(TileSourceFactory.OpenTopo);//Blanco y negro

            map.setMultiTouchControls(true); //Habilitada la posibilidad de hacer zoom con dos dedos
            mapController = map.getController();
            map.getZoomController().setVisibility(CustomZoomButtonsController.Visibility.NEVER);

            final FloatingActionButton btCentrar = findViewById(R.id.btCentrar);
            if(PersistenciaDatos.existeTarea(getApplication(), PersistenciaDatos.ficheroPosicion, idPosicionZoom)) {
                JSONObject posicion = PersistenciaDatos.recuperaTarea(getApplication(), PersistenciaDatos.ficheroPosicion, idPosicionZoom);
                try {
                    mapController.setCenter(new GeoPoint(posicion.getDouble(Auxiliar.latitud), posicion.getDouble(Auxiliar.longitud)));
                    mapController.setZoom(posicion.getDouble(Auxiliar.zum));
                } catch (JSONException e) {
                    centraPrimeraVez();
                }
            }else {
                centraPrimeraVez();
                //mapController.setCenter(telecoPoint); //Centramos la posición en algún lugar conocido
                //mapController.setZoom(8.0);
            }
            //RotationGestureOverlay rotationGestureOverlay = new RotationGestureOverlay(map);
            //rotationGestureOverlay.setEnabled(true);
            //map.getOverlays().add(rotationGestureOverlay);
            map.setMinZoomLevel(nivelMin);
            map.setMaxZoomLevel(nivelMax);

            final DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
            scaleBarOverlay = new ScaleBarOverlay(map);
            scaleBarOverlay.setCentred(true); //La barra de escala se queda en el centro

            map.setTilesScaledToDpi(true);

            //Se agrega la brújula
            //compassOverlay = new CompassOverlay(context, new InternalCompassOrientationProvider(context), map);
            //compassOverlay.enableCompass();

            int ancho = displayMetrics.widthPixels;
            int alto = displayMetrics.heightPixels;
            if(getResources().getConfiguration().orientation == android.content.res.Configuration.ORIENTATION_PORTRAIT) {
                scaleBarOverlay.drawLongitudeScale(true);
                scaleBarOverlay.drawLatitudeScale(false);
                scaleBarOverlay.setScaleBarOffset((int)(ancho * 0.05), (int) (alto * 0.4)); //posición en el el display
            }else{
                scaleBarOverlay.drawLongitudeScale(true);
                scaleBarOverlay.drawLatitudeScale(false);
                scaleBarOverlay.setScaleBarOffset((int)(ancho*0.02), (int) (alto * 0.4));
            }

            pintaItemsfijos();

            //checkPermissions();
            map.addMapListener(new DelayedMapListener(new MapListener() {
                @Override
                public boolean onScroll(ScrollEvent event) { //Movimientos y zoom con dedos
                    if(map.getMapCenter() != null){
                        compruebaZona();
                    }
                    return false;
                }

                @Override
                public boolean onZoom(ZoomEvent event) {//Zoom con botones
                    if(map.getMapCenter() != null){
                        compruebaZona();
                    }
                    return false;
                }
            }, 400));

            //Para cerrar la lista de tareas y el bocadillo
            map.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    InfoWindow.closeAllInfoWindowsOn(map);
                    //sinPulsarTarea.setVisibility(View.VISIBLE);
                    //guiaMapa.setGuidelinePercent(1f);
                    contenedor.setBackgroundColor(getResources().getColor(R.color.transparente));
                    contenedor.setAdapter(null);
                    contenedor.setLayoutManager(null);
                    contenedor.setVisibility(View.GONE);
                    if(guiaMapaH != null)
                        guiaMapaH.setGuidelinePercent(1f);
                    else
                        guiaMapaV.setGuidelinePercent(1f);
                    return false;
                }
            });


            final SearchView searchView = findViewById(R.id.svMapa);
            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    /*JSONArray lugares = Auxiliar.buscaMunicipio(context, query.trim().toLowerCase());
                    if(lugares.length() > 0){
                        if(lugares.length() == 1){
                            try {
                                centraMapa(lugares.getJSONObject(0).getDouble("a"),lugares.getJSONObject(0).getDouble("o"));
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }else{
                            dialogoCoincidencias(lugares);
                        //}
                    }else{
                        pintaSnackBar(getString(R.string.municipioNoEncontrado));
                    }*/
                    return false;
                }

                @Override
                public boolean onQueryTextChange(String newText) {
                    //Aqui debería ir la sugerencia según escriba
                    if(newText.trim().length() > 0) {
                        JSONArray municipios = Auxiliar.buscaMunicipio(context, newText.trim().toLowerCase());
                        if(municipios.length() > 0) {
                            if(btCentrar.isShown())
                                btCentrar.hide();
                            contenedorBusqMapa.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));
                            contenedorBusqMapa.setBackgroundColor(getResources().getColor(R.color.transparente));
                            contenedorBusqMapa.setVisibility(View.VISIBLE);
                            contenedorBusqMapa.setHasFixedSize(true);
                            List<ListaCoincidencias> lista = new ArrayList<>();
                            JSONObject lugar;
                            try{
                                for(int i = 0; i < municipios.length(); i++){
                                    lugar = municipios.getJSONObject(i);
                                    if(lista.isEmpty())
                                        lista.add(new ListaCoincidencias(lugar));
                                    else{
                                        ListaCoincidencias coincidencia;
                                        boolean agregado = false;
                                        for(int j = 0; j < lista.size(); j++){
                                            coincidencia = lista.get(j);
                                            if(coincidencia.getPoblacion() < lugar.getInt("g")) {
                                                lista.add(j, new ListaCoincidencias(lugar));
                                                agregado = true;
                                                break;
                                            }
                                        }
                                        if(!agregado)
                                            lista.add(new ListaCoincidencias(lugar));
                                    }
                                }
                                adaptadorListaCoincidencia = new AdaptadorListaCoincidencia(context, lista);
                                adaptadorListaCoincidencia.setClickListenerDialogo(Maps.this);
                                contenedorBusqMapa.setAdapter(adaptadorListaCoincidencia);
                            }catch (Exception e){
                                e.printStackTrace();
                            }
                        }else{
                            ocultaContenedorBusqMapa();
                            if(!btCentrar.isShown())
                                btCentrar.show();
                        }

                    }else{
                        ocultaContenedorBusqMapa();
                        if(!btCentrar.isShown())
                            btCentrar.show();
                    }

                    return false;
                }
            });
        }

        try{
            String contenido = getIntent().getExtras().getString(Auxiliar.textoParaElMapa);
            if(!contenido.equals(""))
                pintaSnackBar(contenido);
            else {
                JSONObject idUsuario = PersistenciaDatos.recuperaTarea(getApplication(), PersistenciaDatos.ficheroUsuario, Auxiliar.id);
                if(idUsuario == null) {
                    Snackbar snackbar = Snackbar.make(findViewById(R.id.clIdentificateMapa), R.string.textoInicioBreve, Snackbar.LENGTH_INDEFINITE);
                    snackbar.setTextColor(getResources().getColor(R.color.colorSecondaryText));
                    snackbar.setAction(R.string.autenticarse, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            JSONObject idUser = PersistenciaDatos.recuperaTarea(getApplication(), PersistenciaDatos.ficheroUsuario, Auxiliar.id);
                            if(idUser == null) {
                                Login.gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                                        .requestIdToken(getString(R.string.default_web_client_id))
                                        .requestEmail().build();
                                Login.googleSignInClient = GoogleSignIn.getClient(context, Login.gso);
                                Intent intent = Login.googleSignInClient.getSignInIntent();
                                startActivityForResult(intent, Login.requestAuth + 1);
                            }
                        }
                    });
                    snackbar.setActionTextColor(getResources().getColor(R.color.colorSecondary100));
                    snackbar.getView().setBackground(getResources().getDrawable(R.drawable.snack));
                    snackbar.show();
                }
            }
        }catch (Exception e){
            //No hay nada que mostrar
        }
    }

    /**
     * Método para ocultar la lista de coincidencias de la búsqueda
     */
    private void ocultaContenedorBusqMapa() {
        contenedorBusqMapa.setBackgroundColor(getResources().getColor(R.color.transparente));
        contenedorBusqMapa.setAdapter(null);
        contenedorBusqMapa.setLayoutManager(null);
        contenedorBusqMapa.setVisibility(View.GONE);
    }

    /**
     * Método para modificar el dialogo de coincidencias de la búsqueda (agregar la lista de municipios
     * que coincidan
     * @param lugares Lista de municipios que puede haber buscado el usuario
     */
    private void dialogoCoincidencias(JSONArray lugares) {
        List<ListaCoincidencias> lista = new ArrayList<>();
        JSONObject lugar;
        try {
            for(int i = 0; i < lugares.length(); i++){
                lugar = lugares.getJSONObject(i);
                if(lista.isEmpty())
                    lista.add(new ListaCoincidencias(lugar));
                else{
                    ListaCoincidencias coincidencia;
                    boolean agregado = false;
                    for(int j = 0; j < lista.size(); j++){
                        coincidencia = lista.get(j);
                        if(coincidencia.getPoblacion() < lugar.getInt("g")) {
                            lista.add(j, new ListaCoincidencias(lugar));
                            agregado = true;
                            break;
                        }
                    }
                    if(!agregado)
                        lista.add(new ListaCoincidencias(lugar));
                }
            }
            RecyclerView recyclerView = dialogoCoincidecncias.findViewById(R.id.rvListaCoincidencias);
            recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));

            adaptadorListaCoincidencia = new AdaptadorListaCoincidencia(this, lista);
            adaptadorListaCoincidencia.setClickListenerDialogo(this);
            recyclerView.setAdapter(adaptadorListaCoincidencia);
            dialogoCoincidecncias.show();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void centraMapa(double latitud, double longitud) {
        mapController.setZoom(nivelMax - 2);
        mapController.setCenter(new GeoPoint(latitud, longitud));
    }

    private void pintaSnackBar(String texto){
        Snackbar snackbar = Snackbar.make(findViewById(R.id.clMapa), R.string.gracias, Snackbar.LENGTH_SHORT);
        snackbar.setTextColor(getResources().getColor(R.color.colorSecondaryText));
        snackbar.getView().setBackground(getResources().getDrawable(R.drawable.snack));
        snackbar.setText(texto);
        snackbar.show();
    }

    @Override
    public void onActivityResult(int requestCode, int result, Intent data) {
        super.onActivityResult(requestCode, result, data);
        switch (requestCode) {
            case (Login.requestAuth+1):
                //No es necesario comprobar el resultado de la petición según la ayuda oficial
                Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                GoogleSignInAccount account = null;
                try {
                    account = task.getResult(ApiException.class);
                    firebaseAuthWithGoogle(account);
                } catch (ApiException e) {
                    e.printStackTrace();
                }
                break;
            default:
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount googleSignInAccount){
        try {
            AuthCredential authCredential = GoogleAuthProvider.getCredential(googleSignInAccount.getIdToken(), null);
            Login.firebaseAuth.signInWithCredential(authCredential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = Login.firebaseAuth.getCurrentUser();
                        updateUI(firebaseUser, true);
                    } else {
                        updateUI(null, true);
                    }
                }
            });
        }catch (Exception e){
            updateUI(null, true);
        }
    }

    public void updateUI(FirebaseUser firebaseUser, boolean registro){
        if(firebaseUser != null){
            Login.firebaseAnalytics.setUserId(firebaseUser.getUid());
            Bundle bundle = new Bundle();
            bundle.putString(Auxiliar.uid, firebaseUser.getUid());
            if(registro)
                Login.firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SIGN_UP, bundle);
            else
                Login.firebaseAnalytics.logEvent(FirebaseAnalytics.Event.LOGIN, bundle);
            try {
                JSONObject usuario = new JSONObject();
                usuario.put(Auxiliar.id, Auxiliar.id);
                usuario.put(Auxiliar.uid, firebaseUser.getUid());
                PersistenciaDatos.reemplazaJSON(getApplication(), PersistenciaDatos.ficheroUsuario, usuario);
            }catch (JSONException e){
                e.printStackTrace();
            }
            pintaSnackBar(String.format("%s%s", getString(R.string.hola), firebaseUser.getDisplayName()));
            invalidateOptionsMenu();
        }
    }


    /**
     * Método para centrar el mapa en Castilla y León
     */
    private void centraPrimeraVez() {
        final BoundingBox bb = new BoundingBox(
                43.238770,
                -1.783297,
                40.096489,
                -7.021449);
        ViewTreeObserver viewTreeObserver = map.getViewTreeObserver();
        viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                map.zoomToBoundingBox(bb, false);
                map.getController().zoomToSpan(
                        bb.getLatitudeSpan(),
                        bb.getLongitudeSpanWithDateLine());
                map.getController().setCenter(bb.getCenterWithDateLine());
                bb.getDiagonalLengthInMeters();
                ViewTreeObserver v = map.getViewTreeObserver();
                v.removeOnGlobalLayoutListener(this);
                map.setVisibility(View.VISIBLE);
            }
        });
    }

    /**
     * Método para comprobar si el usuario ha otorgado a la aplicación los permisos necesarios.
     * En la actualidad, solicita permisos de localización y cámara.
     */
    public void checkPermissions() {
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY))
            System.exit(-1);
        ArrayList<String> permisos = Auxiliar.preQueryPermisos(this);
        if (permisos.size() > 0) //Evitamos hacer una petición con un array nulo
            ActivityCompat.requestPermissions(this, permisos.toArray(new String[permisos.size()]), requestCodePermissions);
        else{
            if(myLocationNewOverlay == null || myLocationNewOverlay.getMyLocation() == null) {
                GpsMyLocationProvider gpsMyLocationProvider = new GpsMyLocationProvider(context);
                gpsMyLocationProvider.setLocationUpdateMinDistance(5);
                gpsMyLocationProvider.setLocationUpdateMinTime(5000);
                gpsMyLocationProvider.addLocationSource(LocationManager.GPS_PROVIDER);
                gpsMyLocationProvider.addLocationSource(LocationManager.NETWORK_PROVIDER); //Utiliza red y GPS
                myLocationNewOverlay = new MyLocationNewOverlay(gpsMyLocationProvider, map);
                myLocationNewOverlay.enableMyLocation();
                myLocationNewOverlay.setDirectionArrow(BitmapFactory.decodeResource(getResources(), R.drawable.person),
                        BitmapFactory.decodeResource(getResources(), R.drawable.ic_flecha_roja));
                //myLocationNewOverlay.enableFollowLocation(); //Se activa que se aproxime a la posición del usuario
                myLocationNewOverlay.setEnableAutoStop(true);
            }
        }
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
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        boolean falta = false;
        for (int i : grantResults) {
            if (i == -1) {
                falta = true;
                AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
                alertBuilder.setTitle(getString(R.string.permi));
                alertBuilder.setMessage(getString(R.string.permiM));
                alertBuilder.setPositiveButton(getString(R.string.volverSolicitar), new DialogInterface.OnClickListener() {
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
                alertBuilder.setCancelable(false);
                alertBuilder.show();
                break;
            }
        }
        if (!falta) {
            GpsMyLocationProvider gpsMyLocationProvider = new GpsMyLocationProvider(context);
            gpsMyLocationProvider.setLocationUpdateMinDistance(5);
            gpsMyLocationProvider.setLocationUpdateMinTime(5000);
            gpsMyLocationProvider.addLocationSource(LocationManager.GPS_PROVIDER);
            gpsMyLocationProvider.addLocationSource(LocationManager.NETWORK_PROVIDER); //Utiliza red y GPS
            myLocationNewOverlay = new MyLocationNewOverlay(gpsMyLocationProvider, map);
            myLocationNewOverlay.enableMyLocation();
            myLocationNewOverlay.setDirectionArrow(BitmapFactory.decodeResource(getResources(), R.drawable.person),
                    BitmapFactory.decodeResource(getResources(), R.drawable.ic_flecha_roja));
            //myLocationNewOverlay.enableFollowLocation(); //Se activa que se aproxime a la posición del usuario
            myLocationNewOverlay.setEnableAutoStop(true);

            pintaItemsfijos();
        }
    }

    /**
     * Método para pintar los Overlays que siempre se desea que estén en el mapa
     */
    public void pintaItemsfijos() {
        map.getOverlays().clear();
        if(myLocationNewOverlay != null)
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

    /**
     * Método para representar un marcador en el mapa
     * @param marcador Marcador a representar
     */
    public void pintaLista(Marcador marcador){
        JSONArray tareas = marcador.getTareasMarcador();
        if(tareas.length() > 0){
            sinPulsarTarea.setVisibility(View.GONE);
            contenedor.setLayoutManager(layoutManager);
            contenedor.setBackgroundColor(getResources().getColor(R.color.blackTransparente25));
            if(guiaMapaH != null)
                guiaMapaH.setGuidelinePercent(0.8f);
            else
                guiaMapaV.setGuidelinePercent(0.8f);
            contenedor.setVisibility(View.VISIBLE);
            contenedor.setHasFixedSize(true);

            List<TareasMapaLista> tareasPunto = new ArrayList<>();
            JSONObject jo;
            String uriFondo;
            for(int i = 0; i < tareas.length(); i++){
                try {//agrego al marcador sus tareas. Dentro está el JSON completo para cuando el usuario decida realizar una de ellas
                    jo = tareas.getJSONObject(i);
                    try{
                        uriFondo = jo.getString(Auxiliar.recursoImagenBaja);
                    }catch (Exception e){
                        uriFondo = null;
                    }
                    tareasPunto.add(new TareasMapaLista(
                            jo.getString(Auxiliar.id),
                            jo.getString(Auxiliar.titulo),
                            Auxiliar.ultimaParte(jo.getString(Auxiliar.tipoRespuesta)),
                            uriFondo,
                            jo));
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

    /**
     * Método para atender las pulsaciones que se realicen sobre uno de los elementos de la lista de
     * tareas. Se salta a la pantalla de información previa.
     * @param view Vista
     * @param posicion Posicón
     */
    @Override
    public void onItemClick(View view, int posicion){
        try {
            GeoPoint miPosicion;
            if(myLocationNewOverlay != null && (miPosicion = myLocationNewOverlay.getMyLocation()) != null) {
                JSONObject tarea = adaptadorListaMapa.getTarea(posicion);
                tarea.put(Auxiliar.origen, tarea.getString(Auxiliar.ficheroOrigen));
                Intent intent = new Intent(this, Preview.class);
                intent.putExtra(Auxiliar.previa, Auxiliar.mapa);
                intent.putExtra(Auxiliar.id, tarea.getString(Auxiliar.id));
                intent.putExtra(Auxiliar.posUsuarioLat, miPosicion.getLatitude());
                intent.putExtra(Auxiliar.posUsuarioLon, miPosicion.getLongitude());
                startActivity(intent);
                tarea.put(Auxiliar.fechaUltimaModificacion, Auxiliar.horaFechaActual());
                tarea.put(Auxiliar.tipoRespuesta, Auxiliar.ultimaParte(tarea.getString(Auxiliar.tipoRespuesta)));
                PersistenciaDatos.guardaJSON(getApplication(), PersistenciaDatos.ficheroNotificadas, tarea, Context.MODE_PRIVATE);
            }else{
                //Toast.makeText(context, getString(R.string.recuperandoPosicion), Toast.LENGTH_SHORT).show();
                if(myLocationNewOverlay != null){
                    LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                    if(!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) && !locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)){
                        pintaSnackBar(getString(R.string.activaUbicacion));
                        //checkPermissions();
                    }
                }else
                    pintaSnackBar(getString(R.string.recuperandoPosicion));
            }
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    /**
     * Método para generar la parte gráfica del marcador. Dentro de un elemento fijo se dibujará el
     * número de tareas.
     * @param size Número de tareas que representa el marcador en un interior
     * @return Representación gráfica del marcador
     */
    private Bitmap generaBitmapMarkerNumero(int size) {
        Paint paint = new Paint();
        Drawable drawable;
        if(size > 60)
            paint.setARGB(255, 255, 255, 255);
        else
            paint.setARGB(255, 0, 0, 0);
        if(size <= 20)
            drawable = context.getResources().getDrawable(R.drawable.ic_marcador100);
        else
            if(size <= 40)
                drawable = context.getResources().getDrawable(R.drawable.ic_marcador300);
            else
                if(size <= 60)
                    drawable = context.getResources().getDrawable(R.drawable.ic_marcador500);
                else
                    if(size <= 80)
                        drawable = context.getResources().getDrawable(R.drawable.ic_marcador700);
                    else
                        drawable = context.getResources().getDrawable(R.drawable.ic_marcador900);



        //Drawable drawable = context.getResources().getDrawable(R.drawable.ic_marker);
        Bitmap bitmap = Bitmap.createBitmap(
                drawable.getIntrinsicWidth(),
                drawable.getIntrinsicHeight(),
                Bitmap.Config.ARGB_8888);
        int dimen = drawable.getIntrinsicWidth();
        float mitad = (float)dimen/2;
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        String texto;
        if(size>99) {
            texto = "99+";
        } else
            texto = String.valueOf(size);
        paint.setStyle(Paint.Style.FILL);
        int textSize = (int) (mitad + 1);
        paint.setTextSize(textSize);
        paint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText(texto, mitad, mitad + (float)(textSize/2), paint);
        //paint.setARGB(200 - 198 + 2*size, 136, 73, 248);
        //canvas.drawCircle(mitad, canvas.getHeight() - mitad/3, mitad/8, paint);
        //canvas.drawCircle(mitad, mitad, mitad/2, paint);
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
                    default:
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
     */
    private double calculaDistanciaDosPuntos(GeoPoint punto1, IGeoPoint punto2) {
        return Auxiliar.calculaDistanciaDosPuntos(punto1.getLatitude(), punto1.getLongitude(),
                punto2.getLatitude(), punto2.getLongitude());
    }

    //private LocationManager locationManager;

    /**
     * Se restaura el mapa tal y como se indica en la guía. Se restaura la lista de tareas si es que
     * el usuario había picado en un marcador con anterioridad
     */
    @Override
    public void onResume() {
        super.onResume();
        if(!noMolestar) {
            checkPermissions();
            if (map != null)
                map.onResume();
            if(estadoContenedor != null && contenedor.getLayoutManager() != null){
                contenedor.setVisibility(View.VISIBLE);
                sinPulsarTarea.setVisibility(View.GONE);
                contenedor.getLayoutManager().onRestoreInstanceState(estadoContenedor.getParcelable("CONTENEDOR"));
                contenedor.setAdapter(adaptadorListaMapa);
            }
        }
    }

    /**
     * Método para comprobar si en la parte del mapa que se muestra al usuario hay tareas que representar.
     * Si no se ha descargado del servidor las tareas se solicita la descarga. Si las tareas están desfasdas se
     * solicita al servidor nuevamente la zona.
     */
    private void compruebaZona() {
        boolean petServer = solicitarAlServidor(map.getBoundingBox());

        try {
            GeoPoint puntoPartida, puntoVariable;
            //Se recupera la primera cuadrícula a comprobar
            puntoPartida = posicionPrimeraComprobacionPantalla(
                    map.getBoundingBox().getLatNorth(),
                    map.getBoundingBox().getLonWest());
            if (puntoPartida == null) {//La primera cuadrícula vendrá dada por el punto más al norte y al oeste
                puntoPartida = establecePimeraCuadricula(map.getBoundingBox());
            }
            //Número de cuadrículas verticales y horizontales en la vista actual
            int cuadriculasVerticales = numeroCuadriculas(puntoPartida.getLatitude(), map.getBoundingBox().getLatSouth());
            int cuadriculasHorizontales = numeroCuadriculas(map.getBoundingBox().getLonEast(), puntoPartida.getLongitude());
            puntoVariable = new GeoPoint(puntoPartida);
            BoundingBox bb;
            boolean nuevoCuadrado;
            JSONObject jsonObject;
            List<String> ficherosPintar = new ArrayList<>();
            JSONArray posicionesCuadriculas = PersistenciaDatos.leeFichero(getApplication(), PersistenciaDatos.ficheroPosicionesCuadriculas);
            //Se recorren las cuadrículas
            if(map.getBoundingBox().getDiagonalLengthInMeters()/2000 < 2) {
                for (int i = 0; i < cuadriculasHorizontales; i++) {
                    puntoVariable.setLongitude(puntoPartida.getLongitude() + i * incremento);
                    for (int j = 0; j < cuadriculasVerticales; j++) {
                        puntoVariable.setLatitude(puntoPartida.getLatitude() - j * incremento);
                        nuevoCuadrado = true;
                        //Se comprueba si existe la cuadrícula
                        for (int k = 0; k < posicionesCuadriculas.length(); k++) {
                            jsonObject = posicionesCuadriculas.getJSONObject(k);
                            String id = jsonObject.getString(Auxiliar.id);
                            if (petServer) {
                                bb = new BoundingBox(
                                        jsonObject.getDouble(Auxiliar.latN),
                                        jsonObject.getDouble(Auxiliar.lonE),
                                        jsonObject.getDouble(Auxiliar.latS),
                                        jsonObject.getDouble(Auxiliar.lonO));
                                if (bb.contains(
                                        puntoVariable.getLatitude() - 0.00001,
                                        puntoVariable.getLongitude() + 0.00001)) {
                                    if(!ficherosPintar.contains(id))
                                        ficherosPintar.add(id);
                                    if (System.currentTimeMillis() -
                                            jsonObject.getLong(Auxiliar.fechaUltimaModificacion)
                                            > 86400000) {//Un día
                                        File file = new File(getFilesDir(), jsonObject.getString(Auxiliar.id));
                                        if (file.exists()) {
                                            //file.delete();
                                            long instante = System.currentTimeMillis();
                                            jsonObject.put(Auxiliar.fechaUltimaModificacion, instante);
                                            posicionesCuadriculas.put(k, jsonObject);
                                            PersistenciaDatos.reemplazaJSON(
                                                    getApplication(),
                                                    PersistenciaDatos.ficheroPosicionesCuadriculas,
                                                    jsonObject);
                                            peticionTareasServidor(bb, jsonObject.getString(Auxiliar.id));
                                            break;
                                        }
                                    }
                                    nuevoCuadrado = false;
                                    break;
                                }
                            }
                        }
                        if (petServer && nuevoCuadrado) {
                            bb = new BoundingBox(puntoVariable.getLatitude(),
                                    puntoVariable.getLongitude() + incremento,
                                    puntoVariable.getLatitude() - incremento,
                                    puntoVariable.getLongitude());
                            JSONObject cuadricula = new JSONObject();
                            long instante = System.currentTimeMillis();
                            cuadricula.put(Auxiliar.latN, bb.getLatNorth());
                            cuadricula.put(Auxiliar.latS, bb.getLatSouth());
                            cuadricula.put(Auxiliar.lonE, bb.getLonEast());
                            cuadricula.put(Auxiliar.lonO, bb.getLonWest());
                            cuadricula.put(Auxiliar.id, Long.toString(instante));
                            cuadricula.put(Auxiliar.fechaUltimaModificacion, instante);
                            PersistenciaDatos.guardaJSON(getApplication(),
                                    PersistenciaDatos.ficheroPosicionesCuadriculas,
                                    cuadricula,
                                    Context.MODE_PRIVATE);
                            posicionesCuadriculas.put(cuadricula);
                            //Solicitud al servidor
                            peticionTareasServidor(bb, Long.toString(instante));
                        }
                    }
                }
            }
            pintaItemsfijos();
            pintaZona(ficherosPintar);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Se representan los marcadores existentes en las cuadrículas que se le muestran al usuario
     * @param ficherosPintar Nombre de los ficheros donde se encuentran las tareas a representar
     */
    private void pintaZona(List<String> ficherosPintar){
        //Distancia a la que se van a agrupar las tareas
        double nivelZum = 0.05 * nivelMax - 0.05 * map.getZoomLevelDouble();
        //Evito los marcadores duplicados
        nivelZum = Math.max(nivelZum, 0.01);//10m;

        JSONArray todasTareas = new JSONArray();
        JSONObject cuadricula;
        JSONArray tareas;
        JSONObject tarea;
        for(String nombreFichero : ficherosPintar){
            cuadricula = PersistenciaDatos.recuperaTarea(getApplication(), nombreFichero, nombreFichero);
            if(cuadricula != null){
                try {
                    tareas = cuadricula.getJSONArray(Auxiliar.tareas);
                    for(int i = 0; i < tareas.length(); i++) {
                        tarea = tareas.getJSONObject(i);
                        tarea.put(Auxiliar.ficheroOrigen, nombreFichero);
                        todasTareas.put(tarea);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
        List<Marcador> listaMarcadores = new ArrayList<>();
        Marcador marcador;
        double latitud, longitud;
        //Map<Integer, Double> latitudes, longitudes;
        boolean anterior = false/*, anterior2 = false*/;
        try {
            while (todasTareas.length() > 0) {//Barro todas las tareas disponibles en el fichero
                tarea = (JSONObject)todasTareas.remove(0);
                latitud = tarea.getDouble(Auxiliar.latitud);
                longitud = tarea.getDouble(Auxiliar.longitud);

                if (listaMarcadores.isEmpty()) {
                    marcador = new Marcador();
                    marcador.setTitulo(getResources().getString(R.string.tareaIndividual));
                    marcador.setPosicionMarcador(tarea.getDouble(Auxiliar.latitud), tarea.getDouble(Auxiliar.longitud));
                    marcador.agregaTareaAlMarcador(tarea);
                    //marcador.incrementaTareas();
                    listaMarcadores.add(marcador);
                } else {
                    for (int i = 0; i < listaMarcadores.size(); i++) {
                        anterior = false;
                        marcador = listaMarcadores.get(i);
                        if (latitud == marcador.getLatitud() &&
                                longitud == marcador.getLongitud()) { //La tarea es de la misma posición
                            //marcador.incrementaTareas();
                            marcador.agregaTareaAlMarcador(tarea);
                            listaMarcadores.set(i, marcador);
                            anterior = true;
                            break;
                        } else {//Se comprueba la distancia a la tarea del marcador
                            if (Auxiliar.calculaDistanciaDosPuntos(marcador.latitud, marcador.longitud,
                                    latitud, longitud)
                                    <= nivelZum) { //Se agrega al marcador ya que se debe agrupar
                                marcador.setTitulo(getString(R.string.agrupacionTareas));
                                //marcador.incrementaTareas();
                                marcador.agregaTareaAlMarcador(tarea);

                                listaMarcadores.set(i, marcador);
                                anterior = true;
                                break;
                            }
                        }
                    }
                    if (!anterior) {//Hay que agregar un nuevo marcador
                        marcador = new Marcador();
                        marcador.setTitulo(getResources().getString(R.string.tareaIndividual));
                        marcador.setPosicionMarcador(tarea.getDouble(Auxiliar.latitud), tarea.getDouble(Auxiliar.longitud));
                        //marcador.incrementaTareas();
                        marcador.agregaTareaAlMarcador(tarea);
                        listaMarcadores.add(marcador);
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
     * Se pausa el mapa tal y como indica la guía. Se almacena el punto en el que se encuentra la
     * lista de tareas.
     */
    private static Bundle estadoContenedor;
    @Override
    public void onPause(){
        super.onPause();
        if(map != null) {
            try {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put(Auxiliar.id, idPosicionZoom);
                jsonObject.put(Auxiliar.latitud, map.getMapCenter().getLatitude());
                jsonObject.put(Auxiliar.longitud, map.getMapCenter().getLongitude());
                jsonObject.put(Auxiliar.zum, map.getZoomLevelDouble());
                PersistenciaDatos.reemplazaJSON(getApplication(), PersistenciaDatos.ficheroPosicion, jsonObject);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            map.onPause();
        }
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
                    //establecePimeraCuadricula(map.getBoundingBox());
                    mapController.setZoom(nivelMax);
                    mapController.setCenter(myLocationNewOverlay.getMyLocation());
                    //onLocationChanged(myLocationNewOverlay.getMyLocationProvider().getLastKnownLocation());
                }else{ //Si aún no se conoce se muestra un mensaje
                    //Toast.makeText(this, getString(R.string.recuperandoPosicion), Toast.LENGTH_SHORT).show();
                    pintaSnackBar(getString(R.string.recuperandoPosicion));
                }
                break;
            case R.id.switchNoMolestar: //Switch para activar el mapa deshabilitando el modo no molestar
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean(Ajustes.NO_MOLESTAR_pref, false);
                editor.commit();
                Intent intent = new Intent (getApplicationContext(), Maps.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                finishAffinity();
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

        bundle.putDouble("LATITUDE", latitudeOrigen);
        bundle.putDouble("LONGITUDE", longitudeOrigen);
        bundle.putBoolean("DIALOGOSALIR", dialogoSalirAppActivo);
        bundle.putBoolean("DIALOGOCERRARSESION", dialogoCerrarSesionActivo);
        //bundle.putLong("ULTIMANOTIFICACION", ultimaNotificacion);
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
            e.printStackTrace();
        }

        latitudeOrigen = bundle.getDouble("LATITUDE");
        longitudeOrigen = bundle.getDouble("LONGITUDE");

        if(latitudeOrigen != 0 && longitudeOrigen != 0) {
            GeoPoint lastCenter = new GeoPoint(latitudeOrigen, longitudeOrigen);
            mapController.setCenter(lastCenter);
        }
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
     * Método para lanzar el servicio en segundo plano para hacer las llamadas a una alarma de
     * manera periódica.
     */
    private void lanzaServicioPosicionamiento(){
        new AlarmaProceso().activaAlarmaProceso(getApplicationContext());
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

    @Override
    public boolean onPrepareOptionsMenu(Menu menu){
        JSONObject idUsuario = PersistenciaDatos.recuperaTarea(getApplication(), PersistenciaDatos.ficheroUsuario, Auxiliar.id);
        MenuItem menuItem = menu.findItem(R.id.cerrarSesion);
        if(idUsuario == null) {
            menuItem.setTitle(getString(R.string.iniciarSesion));
        }
        return super.onPrepareOptionsMenu(menu);
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
            case R.id.cerrarSesion:
                JSONObject idUsuario = PersistenciaDatos.recuperaTarea(getApplication(), PersistenciaDatos.ficheroUsuario, Auxiliar.id);
                if(idUsuario == null) {
                    Login.gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                            .requestIdToken(getString(R.string.default_web_client_id))
                            .requestEmail().build();
                    Login.googleSignInClient = GoogleSignIn.getClient(context, Login.gso);
                    startActivityForResult(Login.googleSignInClient.getSignInIntent(), Login.requestAuth + 1);
                }
                else {
                    dialogoCerrarSesionActivo = true;
                    dialogoCerrarSesion.show();
                    /*if (PersistenciaDatos.borraTodosFicheros(getApplication())) {
                        Login.firebaseAuth.signOut();
                        Login.googleSignInClient.signOut().addOnCompleteListener(this, new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                vuelveLogin();
                            }
                        });
                    }*/
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void vuelveLogin(){
        Intent intent = new Intent(this, Login.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finishAffinity();
    }

    /**
     * Método para indicar al usuario que va a salir de la aplicación
     */
    @Override
    public void onBackPressed(){
        dialogoSalirAppActivo = true;
        dialogoSalirApp.show();
    }

    /**
     * Método para establecer la primera cuadrícula. <del>Se coge el punto extremo superior más al oeste y
     * se construye dicha cuadrícula.</del> Se establece el punto en Teleco para que tenga sentido hacer una
     * caché en la pasarela
     *
     * @param boundingBox Caja del mapa mostrado
     * @return Punto más al norte y más al oeste
     */
    public GeoPoint establecePimeraCuadricula(BoundingBox boundingBox){
        //double latN = boundingBox.getLatNorth();
        //double lonO = boundingBox.getLonWest();
        double latN = 41.66247;
        double lonO = -4.70605;
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put(Auxiliar.id, idPrimeraCuadricula);
            jsonObject.put(Auxiliar.lat0, latN);
            jsonObject.put(Auxiliar.lon0, lonO);
            jsonObject.put(Auxiliar.fechaUltimaModificacion, Auxiliar.horaFechaActual());
            PersistenciaDatos.guardaJSON(
                    getApplication(),
                    PersistenciaDatos.ficheroPrimeraCuadricula,
                    jsonObject,
                    Context.MODE_PRIVATE);
            return new GeoPoint(latN, lonO);
        }catch (JSONException e){
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Método para comprobar cual va a ser la primera cuadrícula a comprobar. Se tiene en cuenta la
     * primera cuadrícula almacenada por la aplicación para que el mapa se vaya autocompletando sin
     * dejar huecos ni solapar cuadrículas
     * @param latP Latitud más al norte de la parte del mapa que se está mostrando
     * @param lonP Longitud más al oeste de la parte del mapa que se está mostrando
     * @return Punto del que se tiene que partir para crear la cuadrícula
     */
    public GeoPoint posicionPrimeraComprobacionPantalla(double latP, double lonP){
        JSONObject primeraCuadricula = PersistenciaDatos.recuperaTarea(
                getApplication(),
                PersistenciaDatos.ficheroPrimeraCuadricula,
                idPrimeraCuadricula);
        if(primeraCuadricula != null){
            try {
                double lat0 = primeraCuadricula.getDouble(Auxiliar.lat0);
                double lon0 = primeraCuadricula.getDouble(Auxiliar.lon0);
                double latPrima = lat0-incremento*Math.floor((lat0-latP)/incremento);
                latPrima=(latPrima>90)?90:latPrima;
                latPrima=(latPrima<-90)?-90:latPrima;
                double lonPrima = lon0-incremento*Math.ceil((lon0-lonP)/incremento);
                lonPrima=(lonPrima>180)?180:lonPrima;
                lonPrima=(lonPrima<-180)?-180:lonPrima;
                return new GeoPoint(latPrima, lonPrima);
            } catch (JSONException e) {
                e.printStackTrace();
                return null;
            }
        }else
            return null;
    }

    /**
     * Número de cuadrículas que se necesitan para cubrir la totalidad del área mostrada. Se puede
     * utilizar tanto para cuadrículas verticales como horizontales.
     * @param latNolonE Latitud más al norte (verticales) o longitud al este (horizontales)
     * @param latSoLonO Latitud más al sur (verticales) o longitud más al oeste (horizontales)
     * @return Número de cuadrículas que se tiene que componen el mapa mostrado
     */
    public int numeroCuadriculas(double latNolonE, double latSoLonO){
        double intermedio2 = (latNolonE-latSoLonO)/incremento;
        double intermedio = Math.ceil(intermedio2);
        return (int)intermedio;
    }

    /**
     * Método para saber si es neceario solicitar al servidor las tareas. Se utiliza para que cuando
     * el zum sea bajo no se pidan muchas zonas al servidor.
     * @param boundingBox Caja que representa al mapa actual
     * @return Verdadero si se puede solictar o falso si no se debe
     */
    public boolean solicitarAlServidor(BoundingBox boundingBox){
        return !(boundingBox.getDiagonalLengthInMeters() / 2000 > 1.5);
    }

    /** Número de grados con los que se forma el lado de la cuadrícula */
    private final double incremento = 0.002;

    /**
     * Método para solicitar al servidor la solicitud de las tareas de una cuadrícula
     * @param caja Cuadrícula solicitada
     * @param nombre Nombre del fichero donde se tiene que almacenar las tareas
     */
    private void peticionTareasServidor(final BoundingBox caja, final String nombre){
        String url = Auxiliar.direccionIP +
                "tareas?norte=" + caja.getLatNorth() +
                "&este=" + caja.getLonEast() +
                "&sur=" + caja.getLatSouth() +
                "&oeste=" + caja.getLonWest();
        JsonArrayRequest jsonObjectRequest = new JsonArrayRequest(Request.Method.GET, url, null, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                //Tengo que guardar el fichero específico de la tarea y además modificar el existente de posiciones
                try {
                    long instante = System.currentTimeMillis();
                    JSONObject cuadricula = new JSONObject();
                    cuadricula.put(Auxiliar.id, nombre);
                    /*JSONArray tareasValidas = new JSONArray();
                    JSONObject tarea;
                    double norte = caja.getLatNorth();
                    double sur = caja.getLatSouth();
                    double oeste = caja.getLonWest();
                    double este = caja.getLonEast();
                    for(int i = 0; i < response.length(); i++){
                        tarea = response.getJSONObject(i);
                        if(puntoContenido(
                                tarea.getDouble(Auxiliar.latitud), tarea.getDouble(Auxiliar.longitud),
                                norte, sur, este, oeste))
                            tareasValidas.put(tarea);
                    }*/
                    if (response.length() > 0) {
                        cuadricula.put(Auxiliar.tareas, response);
                        cuadricula.put(Auxiliar.fechaUltimaModificacion, instante);
                        PersistenciaDatos.creaFichero(getApplication(), nombre, cuadricula, Context.MODE_PRIVATE);
                        compruebaZona();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, null);

        ColaConexiones.getInstance(getApplicationContext()).getRequestQueue().add(jsonObjectRequest);
    }

    /*private boolean puntoContenido(double latP, double lonP, double norte, double sur, double este, double oeste){
        return latP < norte && latP > sur && lonP > oeste && lonP < este;
    }*/


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
                //InfoWindow.closeAllInfoWindowsOn(mapView);
                double distancia;
                String msg = getString(R.string.recuperandoPosicion);
                try {
                    if(myLocationNewOverlay != null){
                        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                        if(!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) && !locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                            msg = getString(R.string.activaUbicacion);
                        }else{
                            distancia = calculaDistanciaDosPuntos(myLocationNewOverlay.getMyLocation(), marker.getPosition());
                            msg = String.format(Locale.getDefault(), " %.3f km", distancia);
                        }
                    }
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

    @Override
    public void onItemClickDialogo(View view, int position) {
        if(dialogoCoincidecncias != null && dialogoCoincidecncias.isShowing())
            dialogoCoincidecncias.cancel();

        if(contenedorBusqMapa != null && contenedorBusqMapa.getVisibility() == View.VISIBLE){
            ocultaContenedorBusqMapa();
        }

        centraMapa(adaptadorListaCoincidencia.getLatitud(position),
                adaptadorListaCoincidencia.getLongitud(position));
    }

    //private String idInstanteGETZONA = "instanteGETZONA";

    /**
     * Estrucutra de la lista de Tareas. Se va a utilizar en los infladores
     */
    public static class TareasMapaLista {
        public String id, titulo, tipoTarea, uriFondo;
        public JSONObject tarea;
        TareasMapaLista(String id, String titulo, String tipoTarea, String uriFondo, JSONObject tarea){
            this.id = id;
            this.titulo = titulo;
            this.tipoTarea = tipoTarea;
            this.tarea = tarea;
            this.uriFondo = uriFondo;
        }
    }

    /**
     * Subclase con la que se representa al marcador del mapa
     */
    private static class Marcador{
        /** Título del marcador*/
        private String titulo;
        /** Latitud y longitud donde se coloca al marcador */
        private double latitud, longitud;
        /** Tareas que contendrá el marcador*/
        private JSONArray tareasMarcador;
        /** Número de tareas que están en el interior del marcador */
        private int numeroTareas;

        /**
         * Constructor de la subclase. Establece los valores iniciales e inicia la lista.
         */
        Marcador(){
            titulo = null;
            latitud = 0;
            longitud = 0;
            tareasMarcador = new JSONArray();
        }

        /**
         * Método para establecer un título al marcador
         * @param titulo Título del marcador
         */
        void setTitulo(String titulo) {
            this.titulo = titulo;
        }

        /**
         * Método para recuperar un título del marcador
         * @return Título del marcador. Si no se ha establecido es null
         */
        String getTitulo(){
            return titulo;
        }

        /**
         * Método para establecer la posición del marcador en el mapa
         * @param latitud Latitud
         * @param longitud Longitud
         */
        void setPosicionMarcador(double latitud, double longitud) {
            this.latitud = latitud;
            this.longitud = longitud;
        }

        /**
         * Método para recuperar la latitud del marcador
         * @return Latitud
         */
        double getLatitud() {
            return latitud;
        }

        /**
         * Método para recuperar la longitud del marcador
         * @return Longitud
         */
        double getLongitud() {
            return longitud;
        }

        /**
         * Método para agregar una nueva tarea al marcador
         * @param tarea Tarea que se desea agregar
         */
        void agregaTareaAlMarcador(JSONObject tarea){
            tareasMarcador.put(tarea);
            incrementaTareas();
        }

        /**
         * Método para establecer la lista de tareas del marcador
         * @param tareas Tareas del marcador
         */
        void setTareasMarcador(JSONArray tareas){
            tareasMarcador = tareas;
        }

        /**
         * Método para recuperar la lista de tareas del marcador
         * @return Lista de tareas del marcador
         */
        JSONArray getTareasMarcador(){
            return tareasMarcador;
        }

        /**
         * Método para incrementar el número de tareas del marcador
         */
        void incrementaTareas(){
            ++numeroTareas;
        }

        /**
         * Método para establecer el número de tareas que tiene el marcador
         * @param numeroTareas Número de tareas del marcador
         */
        void setNumeroTareas(int numeroTareas){
            this.numeroTareas = numeroTareas;
        }

        /**
         * Método para obtener el número de tareas del marcador
         * @return Número de tareas
         */
        int getNumeroTareas() {
            return numeroTareas;
        }
    }

    public static class ListaCoincidencias{
        String municipio, provincia;
        List<Double> posicion;
        int poblacion;

        ListaCoincidencias(JSONObject jsonObject) throws JSONException {
            municipio = jsonObject.getString("m");
            provincia = jsonObject.getString("p");
            posicion = new ArrayList<>();
            posicion.add(jsonObject.getDouble("a"));
            posicion.add(jsonObject.getDouble("o"));
            poblacion = jsonObject.getInt("g");
        }

        public String getMunicipio() {
            return municipio;
        }

        public String getProvincia() {
            return provincia;
        }

        public double getLatitud(){
            return posicion.get(0);
        }

        public double getLongitud(){
            return posicion.get(1);
        }

        public int getPoblacion() {
            return poblacion;
        }
    }


}
