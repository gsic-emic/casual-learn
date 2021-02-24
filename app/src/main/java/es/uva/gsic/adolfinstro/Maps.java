package es.uva.gsic.adolfinstro;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.Guideline;
import androidx.core.app.ActivityCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.Activity;
import android.app.Application;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
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
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.text.Html;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;

import androidx.appcompat.widget.SearchView;

import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.play.core.review.ReviewInfo;
import com.google.android.play.core.review.ReviewManager;
import com.google.android.play.core.review.ReviewManagerFactory;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import org.apache.commons.lang3.StringUtils;
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
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import es.uva.gsic.adolfinstro.auxiliar.AdaptadorListaCoincidencia;
import es.uva.gsic.adolfinstro.auxiliar.AdaptadorListaMapa;
import es.uva.gsic.adolfinstro.auxiliar.AdaptadorListaPuntos;
import es.uva.gsic.adolfinstro.auxiliar.Auxiliar;
import es.uva.gsic.adolfinstro.auxiliar.Bocadillo;
import es.uva.gsic.adolfinstro.auxiliar.ColaConexiones;
import es.uva.gsic.adolfinstro.auxiliar.ListaCoincidencias;
import es.uva.gsic.adolfinstro.auxiliar.Marcador;
import es.uva.gsic.adolfinstro.auxiliar.PuntoSingular;
import es.uva.gsic.adolfinstro.auxiliar.TareasMapaLista;
import es.uva.gsic.adolfinstro.persistencia.PersistenciaDatos;

/**
 * Clase que gestiona la actividad principal de la aplicación.
 * @author Pablo
 * @version 20210202
 */
public class Maps extends AppCompatActivity implements
        SharedPreferences.OnSharedPreferenceChangeListener,
        AdaptadorListaMapa.ItemClickListener,
        AdaptadorListaCoincidencia.ItemClickListenerDialogo,
        AdaptadorListaPuntos.ItemClickListenerDialogoVariosPuntos,
        LocationListener,
        NavigationView.OnNavigationItemSelectedListener {
    /** Objeto que permite mostrar el mapa*/
    private MapView map;

    /** Objeto donde se expone toda la lista de tareas del marcador */
    private RecyclerView contenedor;
    /** Objeto sobre el que se carga la lista de municipios que indique el usuario*/
    private RecyclerView contenedorBusqMapa;

    /** Objeto que almacenará, entre otras cosas, la última posición conocida del usuario*/
    private MyLocationNewOverlay myLocationNewOverlay;
    /** Objeto tuilizado para centrar el mapa en un punto específico*/
    private IMapController mapController;
    /** Posición inicial del punto conocido */
    private double latitudeOrigen, longitudeOrigen;
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

    /** Nivel de zum máximo permitido */
    private final double nivelMax = 19.5;

    /** Clave para obtener la última posición y el zum del mapa */
    private final String idPosicionZoom = "posicionZum";

    /** Adaptador para la lista de tareas del marcador */
    private static AdaptadorListaMapa adaptadorListaMapa;

    /** Identificador de la primera cuadrícula */
    final String idPrimeraCuadricula = "1C";

    /** Diálogo para salir de la apliación */
    private AlertDialog.Builder dialogoSalirApp;
    /** Boolean que determian si el dialogo de salir de la aplicación está activo */
    private boolean dialogoSalirAppActivo = false;

    /** Dialogo para mostrar los distintos puntos que agrupa un marcador */
    private Dialog dialogoVariosPuntos;

    private Dialog dialogoConfiguracionPorfolio;
    private boolean dialogoConfiguracionPorfolioVisible;


    /** Guía de la vista vertical */
    Guideline guiaMapaH;
    /** Guía de la vista apaisada*/
    Guideline guiaMapaV;

    /** Adaptador para la lista de municipios cuando se realiza una búsqueda*/
    private AdaptadorListaCoincidencia adaptadorListaCoincidencia;

    /** Objeto con el que se indica si hay un marcador pulsado */
    private boolean marcadorPulsado = false;

    /** Objeto para indicar el nombre del punto de interés*/
    TextView tituloPunto;
    /** Objeto para indicar la descripción del punto de interés*/
    TextView textoPunto;
    /** Objeto para indicar la distancia al punto de interés */
    TextView distanciaPunto;
    /** Objeto para el texto reducido del punto de interés*/
    TextView textoPuntoReducido;

    /** Vista de los puntos de interés */
    ScrollView svPunto;

    /** Número de cuadrículas pendientes de obtener del servidor */
    private int numeroCuadriculasPendientes = 0;

    /** Lista de permisos que la aplicación necesita solicitar al usuario*/
    private List<String> permisos;

    /** Objeto para la búsqueda de municipios */
    SearchView searchView;

    /** Número de grados con los que se forma el lado de la cuadrícula */
    private final double incremento = 0.0254;

    /** Objeto que controlará la lectura de la información del punto de interés */
    private TextToSpeech textToSpeech;

    /** Texto que se va a leer sobre el punto de interés*/
    private String textoParaAltavoz;

    /** Objeto para controlar la posibilidad de ver el artículo de la wikipedia */
    private ImageView ivWiki;

    /** Objeto que controla la imagen del texto que se lee*/
    private ImageView ivSpeaker;

    /** Enlace para mostrar el artículo de la wikipedia en el navegador interno */
    private String enlaceWiki;

    /** Diálogo para mostrar a los usuarios los posibles problemas que puede tener su dispositivo */
    private Dialog dialogoSegundoPlano;
    /** Objeto para saber si el diálogo sobre la gestión de energía está activo */
    private boolean dialogoSegundoPlanoVisible;

    private DrawerLayout drawerLayout;

    private NavigationView navigationView;

    private Uri rutaAlPunto;

    private FloatingActionButton btCentrar;

    private FloatingActionButton btNavegar;

    //private FloatingActionButton btModos;

    //private Button btModos1, btModos2, btModos3, btModos4;

    //private Button[] btsModo;

    //private ConstraintLayout modos;

    /**
     * Método con el que se pinta la actividad. Lo primero que comprueba es si está activada el modo no
     * molestar para saber si se tiene que mostar el mapa o no
     * @param savedInstanceState Bundle
     */
    @Override
    protected void onCreate(final Bundle savedInstanceState) {

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        context = this;
        Configuration.getInstance().load(context, PreferenceManager.getDefaultSharedPreferences(context));
        Configuration.getInstance().setUserAgentValue(BuildConfig.APPLICATION_ID);
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

        onSharedPreferenceChanged(sharedPreferences, Ajustes.NO_MOLESTAR_pref);

        dialogoSalirApp = new AlertDialog.Builder(this);
        dialogoSalirApp.setTitle(getString(R.string.exitT));
        dialogoSalirApp.setMessage(getString(R.string.exit));
        dialogoSalirApp.setPositiveButton(getString(R.string.salir), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
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
        if (savedInstanceState != null && savedInstanceState.getBoolean("DIALOGOSALIR", false)) {
            dialogoSalirAppActivo = true;
            dialogoSalirApp.show();
        }

        setContentView(R.layout.activity_maps);
        Toolbar toolbar = findViewById(R.id.tbMaps);
        setSupportActionBar((Toolbar) findViewById(R.id.tbMaps));
        toolbar.setTitleTextColor(dameColor(R.color.white));

        drawerLayout = findViewById(R.id.dlMapa);
        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.app_name, R.string.app_name);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();

        navigationView = findViewById(R.id.nvMapa);

        navigationView.setNavigationItemSelectedListener(this);
        navigationView.setOnCreateContextMenuListener(this);

        ocultaOpcionInicioSesionSiIdentificado(navigationView.getMenu());

        map = findViewById(R.id.map);
        contenedor = findViewById(R.id.rvTareasMapa);
        contenedorBusqMapa = findViewById(R.id.rvBusquedaMapa);

        guiaMapaH = findViewById(R.id.guiaMapa);
        guiaMapaV = findViewById(R.id.guiaMapaV);

        contenedor.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        map.setTileSource(TileSourceFactory.MAPNIK);

        map.setMultiTouchControls(true); //Habilitada la posibilidad de hacer zum con dos dedos
        mapController = map.getController();
        map.getZoomController().setVisibility(CustomZoomButtonsController.Visibility.NEVER);

        btCentrar = findViewById(R.id.btCentrar);
        btNavegar = findViewById(R.id.btNavegarMaps);
        if (PersistenciaDatos.existeTarea(getApplication(), PersistenciaDatos.ficheroPosicion, idPosicionZoom)) {
            JSONObject posicion = PersistenciaDatos.recuperaTarea(getApplication(), PersistenciaDatos.ficheroPosicion, idPosicionZoom);
            try {
                assert posicion != null;
                mapController.setCenter(new GeoPoint(posicion.getDouble(Auxiliar.latitud), posicion.getDouble(Auxiliar.longitud)));
                mapController.setZoom(posicion.getDouble(Auxiliar.zum));
            } catch (JSONException e) {
                centraPrimeraVez();
            }
        } else {
            centraPrimeraVez();
        }

        // Nivel de zum mínimo permitido
        double nivelMin = 6.5;
        map.setMinZoomLevel(nivelMin);
        map.setMaxZoomLevel(nivelMax);

        final DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        scaleBarOverlay = new ScaleBarOverlay(map);
        scaleBarOverlay.setCentred(true); //La barra de escala se queda en el centro

        map.setTilesScaledToDpi(true);

        int ancho = displayMetrics.widthPixels;
        int alto = displayMetrics.heightPixels;
        if (getResources().getConfiguration().orientation == android.content.res.Configuration.ORIENTATION_PORTRAIT) {
            scaleBarOverlay.drawLongitudeScale(true);
            scaleBarOverlay.drawLatitudeScale(false);
            scaleBarOverlay.setScaleBarOffset((int) (ancho * 0.05), (int) (alto * 0.4)); //posición en el el display
        } else {
            scaleBarOverlay.drawLongitudeScale(true);
            scaleBarOverlay.drawLatitudeScale(false);
            scaleBarOverlay.setScaleBarOffset((int) (ancho * 0.02), (int) (alto * 0.4));
        }

        pintaItemsfijos();

        map.addMapListener(new DelayedMapListener(new MapListener() {
            @Override
            public boolean onScroll(ScrollEvent event) { //Movimientos y zoom con dedos
                if (map != null) {
                    compruebaZona(true);
                }
                return false;
            }

            @Override
            public boolean onZoom(ZoomEvent event) {//Zoom con botones
                if (map != null) {
                    compruebaZona(true);
                }
                return false;
            }
        }, 250));

        //Para cerrar la lista de tareas y el bocadillo
        map.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                ocultaInfoPuntoInteres();
                return false;
            }
        });

        //Búsqueda de municipios
        searchView = findViewById(R.id.svMapa);
        searchView.setOnSearchClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(marcadorPulsado)
                    ocultaInfoPuntoInteres();
                if (btCentrar.isShown())
                    btCentrar.hide();
                //ocultaTodoModos();
            }
        });
        searchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                //ocultaModos();
                if(!btCentrar.isShown())
                    btCentrar.show();
                return false;
            }
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                //Aqui debería ir la sugerencia según escriba
                if (newText.trim().length() > 0) {
                    JSONArray municipios = Auxiliar.buscaMunicipio(
                            context, StringUtils.stripAccents(newText.trim().toLowerCase()));
                    if (municipios.length() > 0) {
                        contenedorBusqMapa.setLayoutManager(new LinearLayoutManager(
                                context, LinearLayoutManager.VERTICAL, false));
                        contenedorBusqMapa.setBackgroundColor(dameColor(R.color.transparente));
                        contenedorBusqMapa.setVisibility(View.VISIBLE);
                        contenedorBusqMapa.setHasFixedSize(true);
                        List<ListaCoincidencias> lista = new ArrayList<>();
                        JSONObject lugar;
                        try {
                            for (int i = 0; i < municipios.length(); i++) {
                                lugar = municipios.getJSONObject(i);
                                if (lista.isEmpty())
                                    lista.add(new ListaCoincidencias(lugar));
                                else {
                                    ListaCoincidencias coincidencia;
                                    boolean agregado = false;
                                    for (int j = 0; j < lista.size(); j++) {
                                        coincidencia = lista.get(j);
                                        if (coincidencia.getPoblacion() < lugar.getInt("g")) {
                                            lista.add(j, new ListaCoincidencias(lugar));
                                            agregado = true;
                                            break;
                                        }
                                    }
                                    if (!agregado)
                                        lista.add(new ListaCoincidencias(lugar));
                                }
                            }
                            adaptadorListaCoincidencia = new AdaptadorListaCoincidencia(context, lista);
                            adaptadorListaCoincidencia.setClickListenerDialogo(Maps.this);
                            contenedorBusqMapa.setAdapter(adaptadorListaCoincidencia);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        ocultaContenedorBusqMapa();
                        /*if (!btCentrar.isShown())
                            btCentrar.show();*/
                    }

                } else {
                    ocultaContenedorBusqMapa();
                    /*if (!btCentrar.isShown())
                        btCentrar.show();*/
                }
                return false;
            }
        });

        tituloPunto = findViewById(R.id.tvPuntoTitulo);
        textoPunto = findViewById(R.id.tvPuntoTexto);
        distanciaPunto = findViewById(R.id.tvPuntoDistancia);
        textoPuntoReducido = findViewById(R.id.tvPuntoTextoReducido);

        ivWiki = findViewById(R.id.ivWikipediaMapa);
        ivSpeaker = findViewById(R.id.ivSpeaker);

        svPunto = findViewById(R.id.svPunto);
        dialogoVariosPuntos = new Dialog(this);
        dialogoVariosPuntos.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialogoVariosPuntos.setContentView(R.layout.dialogo_varios_puntos);
        dialogoVariosPuntos.setCancelable(true);


        dialogoSegundoPlano = new Dialog(this);
        dialogoSegundoPlano.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialogoSegundoPlano.setContentView(R.layout.dialogo_segundo_plano);
        dialogoSegundoPlano.setCancelable(false);
        dialogoSegundoPlano.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                dialogoSegundoPlanoVisible = false;
            }
        });
        TextView textoSegundoPlano = dialogoSegundoPlano.findViewById(R.id.tvTextoSegundoPlano);
        textoSegundoPlano.setText(Html.fromHtml(context.getString(R.string.texto_segundo_plano)));
        Button vamos = dialogoSegundoPlano.findViewById(R.id.btVamosSegundoPlano);
        vamos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogoSegundoPlano.cancel();
                dialogoSegundoPlanoVisible = false;
                noVuelvasAMostrarDialogoSegundoPlano();
                boolean muestraToast = true;
                for(Intent intent : Auxiliar.intentProblematicos()){
                    if(getPackageManager().resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY) != null){
                        muestraToast = false;
                        startActivity(intent);
                        break;
                    }
                }
                if(muestraToast)
                    Toast.makeText(context, context.getString(R.string.no_app_gestion), Toast.LENGTH_LONG).show();
            }
        });

        Button omitir = dialogoSegundoPlano.findViewById(R.id.btOmitirSegundoPlano);
        omitir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogoSegundoPlanoVisible = false;
                noVuelvasAMostrarDialogoSegundoPlano();
                dialogoSegundoPlano.cancel();
            }
        });

        dialogoSegundoPlanoVisible = false;
        if (savedInstanceState != null && savedInstanceState.getBoolean("DIALOGOSEGUNDOPLANO", false)) {
            dialogoSegundoPlanoVisible = true;
            dialogoSegundoPlano.show();
        }

        dialogoConfiguracionPorfolio = new Dialog(this);
        dialogoConfiguracionPorfolio.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialogoConfiguracionPorfolio.setContentView(R.layout.dialogo_conf_porfolio);
        dialogoConfiguracionPorfolio.setCancelable(true);

        final SwitchCompat swActivarPorfolio = dialogoConfiguracionPorfolio.findViewById(R.id.swActivarPor);
        final SwitchCompat swRetardarPorfolio = dialogoConfiguracionPorfolio.findViewById(R.id.swRetardarPor);
        //final TextView textoTiempo = dialogoConfiguracionPorfolio.findViewById(R.id.tvIntervaloPor);
        //textoTiempo.setText(String.format("%s\n(3 %s)", getResources().getString(R.string.intervalo), getResources().getString(R.string.horas)));
        //SeekBar seekBar = dialogoConfiguracionPorfolio.findViewById(R.id.sbIntervalo);
        final Button btOmitir = dialogoConfiguracionPorfolio.findViewById(R.id.btOmitirPor);

        dialogoConfiguracionPorfolio.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                boolean estadoPorfolio, retardo;
                estadoPorfolio = swActivarPorfolio.isChecked();
                if(swRetardarPorfolio.isEnabled()){
                    retardo = !swRetardarPorfolio.isChecked();
                } else{
                    retardo = true;
                }
                enviaConfiguracionPorfolio(estadoPorfolio, retardo);
                dialogoConfiguracionPorfolioVisible = false;
                dialogoConfiguracionPorfolio.cancel();
            }
        });

        swActivarPorfolio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean estado = ((SwitchCompat) v).isChecked();
                swRetardarPorfolio.setEnabled(estado);
                if(estado) {
                    swRetardarPorfolio.setEnabled(true);
                    btOmitir.setText(context.getString(R.string.cerrar));
                } else {
                    swRetardarPorfolio.setChecked(false);
                    swRetardarPorfolio.setEnabled(false);
                }
            }
        });

        btOmitir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogoConfiguracionPorfolio.cancel();
            }
        });

        dialogoConfiguracionPorfolioVisible = false;
        if (savedInstanceState != null && savedInstanceState.getBoolean("DIALOGOCONFPOR", false)) {
            dialogoConfiguracionPorfolioVisible = true;
            dialogoConfiguracionPorfolio.show();
        }

        try {
            String contenido = Objects.requireNonNull(getIntent().getExtras()).getString(Auxiliar.textoParaElMapa);
            if (contenido != null && !contenido.equals("")) {
                getIntent().removeExtra(Auxiliar.textoParaElMapa);
                pintaSnackBar(contenido);
                if(!contenido.contains(getString(R.string.hola)))
                    llamadaAPlayStore();
            }
            else {
                JSONObject idUsuario = PersistenciaDatos.recuperaTarea(
                        getApplication(), PersistenciaDatos.ficheroUsuario, Auxiliar.id);
                if (idUsuario == null) {
                    Snackbar snackbar = Snackbar.make(
                            findViewById(R.id.clIdentificateMapa),
                            R.string.textoInicioBreve,
                            Snackbar.LENGTH_INDEFINITE);
                    snackbar.setTextColor(dameColor(R.color.colorSecondaryText));
                    snackbar.setAction(R.string.autenticarse, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            JSONObject idUser = PersistenciaDatos.recuperaTarea(
                                    getApplication(), PersistenciaDatos.ficheroUsuario, Auxiliar.id);
                            if (idUser == null) {
                                Login.gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                                        .requestIdToken(getString(R.string.default_web_client_id))
                                        .requestEmail().build();
                                Login.googleSignInClient = GoogleSignIn.getClient(context, Login.gso);
                                Intent intent = Login.googleSignInClient.getSignInIntent();
                                startActivityForResult(intent, Login.requestAuth + 1);
                            }
                        }
                    });
                    snackbar.setActionTextColor(dameColor(R.color.colorSecondary50));
                    snackbar.getView().setBackground(dameDrawable(R.drawable.snack));
                    snackbar.show();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        //btModos = findViewById(R.id.btModo);
        //modos = findViewById(R.id.modos);
        //btModos1 = findViewById(R.id.modo1);
        //btModos2 = findViewById(R.id.modo2);
        //btModos3 = findViewById(R.id.modo3);
        //btModos4 = findViewById(R.id.modo4);
        //btsModo = new Button[]{btModos1, btModos2, btModos3, btModos4};
    }

    private void enviaConfiguracionPorfolio(final boolean publico, final boolean retardado) {
        final JSONObject idUsuario = PersistenciaDatos.recuperaTarea(getApplication(), PersistenciaDatos.ficheroUsuario, Auxiliar.id);
        if (idUsuario == null || idUsuario.has(Auxiliar.uid)) {
            try {
                JSONObject infoUsuario = new JSONObject();
                JsonObjectRequest jsonObjectRequest;
                infoUsuario.put(Auxiliar.publico, publico);
                infoUsuario.put(Auxiliar.retardado, retardado);
                if (idUsuario != null && idUsuario.has(Auxiliar.idPortafolio)) {//Es una actualización
                    jsonObjectRequest = new JsonObjectRequest(
                            Request.Method.PUT,
                            Auxiliar.rutaPortafolio + idUsuario.getString(Auxiliar.idPortafolio),
                            infoUsuario,
                            new Response.Listener<JSONObject>() {
                                @Override
                                public void onResponse(JSONObject response) {
                                    try {
                                        SharedPreferences.Editor editor = sharedPreferences.edit();
                                        editor.putBoolean(Ajustes.PORTAFOLIO_pref, publico);
                                        editor.putBoolean(Ajustes.RETARDOPORTA_pref, retardado);
                                        editor.commit();
                                    } catch (Exception e) {
                                        Log.d("porfolio", "Creación del porfolio desde el dialogo");
                                    }
                                }
                            },
                            new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError error) {
                                    idUsuario.remove(Auxiliar.idPortafolio);
                                    PersistenciaDatos.reemplazaJSON(
                                            (Application) context.getApplicationContext(),
                                            PersistenciaDatos.ficheroUsuario,
                                            idUsuario);
                                    Toast.makeText(context, context.getString(R.string.errorCambioEstado), Toast.LENGTH_SHORT).show();
                                }
                            }
                    );
                } else {//Es una creación
                    infoUsuario.put(Auxiliar.idUsuario, idUsuario.getString(Auxiliar.uid));
                    jsonObjectRequest = new JsonObjectRequest(
                            Request.Method.POST,
                            Auxiliar.direccionIP + "portafolio",
                            infoUsuario,
                            new Response.Listener<JSONObject>() {
                                @Override
                                public void onResponse(JSONObject docUsuario) {
                                    if (docUsuario != null) {
                                        try {
                                            idUsuario.put(Auxiliar.idPortafolio, docUsuario.getString(Auxiliar.idPortafolio));
                                            PersistenciaDatos.reemplazaJSON(
                                                    (Application) context.getApplicationContext(),
                                                    PersistenciaDatos.ficheroUsuario,
                                                    idUsuario);
                                            try {
                                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                                editor.putBoolean(Ajustes.PORTAFOLIO_pref, publico);
                                                editor.putBoolean(Ajustes.RETARDOPORTA_pref, retardado);
                                                editor.commit();
                                            } catch (Exception e) {
                                                Log.d("porfolio", "Creación del porfolio desde el dialogo");
                                            }
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }
                            },
                            new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError error) {
                                    Toast.makeText(context, context.getString(R.string.errorOpera), Toast.LENGTH_SHORT).show();
                                }
                            }
                    );
                }

                ColaConexiones.getInstance(context).getRequestQueue().add(jsonObjectRequest);
            }
            catch (Exception e){
                Toast.makeText(context, getString(R.string.errorOpera), Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(context, getString(R.string.errorOpera), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Método para ocultar la opción de inicio de sesión en el menú lateral
     * @param menu Menú lateral
     */
    private void ocultaOpcionInicioSesionSiIdentificado(Menu menu) {
        if(PersistenciaDatos.recuperaTarea(
                getApplication(), PersistenciaDatos.ficheroUsuario, Auxiliar.id) != null){
            MenuItem sesion = menu.findItem(R.id.cerrarSesion);
            if(sesion.isVisible())
                sesion.setVisible(false);
        }
    }

    /**
     * Método para que el usuario no vuelva a ver el diálogo sobre los problemas que puede tener su
     * dispositivo debido a la aplicación de gestión de energía.
     */
    private void noVuelvasAMostrarDialogoSegundoPlano() {
        try {
            JSONArray ficheroSegundoPlano = PersistenciaDatos.leeFichero(
                    getApplication(),
                    PersistenciaDatos.ficheroSegundoPlano);
            if(ficheroSegundoPlano.length() == 0) {
                JSONObject noVuelvasMostrar = new JSONObject();
                noVuelvasMostrar.put(Auxiliar.id, PersistenciaDatos.ficheroSegundoPlano);
                noVuelvasMostrar.put(Auxiliar.instante, System.currentTimeMillis());

                ficheroSegundoPlano.put(noVuelvasMostrar);
                PersistenciaDatos.guardaFichero(
                        getApplication(),
                        PersistenciaDatos.ficheroSegundoPlano,
                        ficheroSegundoPlano,
                        Context.MODE_PRIVATE);
            }
            //En este momento indico al usuario la posibilidad de utilizar el porfolio
            if(!dialogoConfiguracionPorfolio.isShowing())
                dialogoConfiguracionPorfolio.show();
            dialogoConfiguracionPorfolioVisible = true;
        } catch (JSONException e){
            e.printStackTrace();
        }
    }

    /**
     * Método para ocultar toda la información del punto de interés.
     */
    private void ocultaInfoPuntoInteres() {
        InfoWindow.closeAllInfoWindowsOn(map);
        idZona = "";
        if(marcadorPulsado) {
            marcadorPulsado = false;
            compruebaZona(false);
        }
        if(locationManager != null)
            locationManager.removeUpdates(Maps.this);
        if (contenedor.getVisibility() != View.GONE)
            contenedor.setVisibility(View.GONE);
        if (guiaMapaH != null)
            guiaMapaH.setGuidelinePercent(1f);
        else
            guiaMapaV.setGuidelinePercent(1f);
        if (textToSpeech != null && textToSpeech.isSpeaking())
            textToSpeech.stop();
        if(textoPuntoReducido.getVisibility() == View.VISIBLE){
            ocultaReducido();
        }
        btCentrar.setVisibility(View.VISIBLE);
        btNavegar.setVisibility(View.GONE);
        //ocultaModos();
    }

    /*private void ocultaModos(){
        if(modos.isShown()){
            modos.setVisibility(View.GONE);
        }
        if(!btModos.isShown())
            btModos.show();
    }

    private void ocultaTodoModos(){
        if(modos.isShown()){
            modos.setVisibility(View.GONE);
        }
        if(btModos.isShown())
            btModos.hide();
    }*/

    /**
     * Método para pasar la vista de la descripción del lugar de reducida a completa
     */
    private void ocultaReducido() {
        textoPuntoReducido.setVisibility(View.GONE);
        //masInfo.setVisibility(View.GONE);
        textoPunto.setVisibility(View.VISIBLE);
    }

    /**
     * Método para ocultar la lista de coincidencias de la búsqueda
     */
    private void ocultaContenedorBusqMapa() {
        contenedorBusqMapa.setBackgroundColor(dameColor(R.color.transparente));
        contenedorBusqMapa.setAdapter(null);
        contenedorBusqMapa.setLayoutManager(null);
        contenedorBusqMapa.setVisibility(View.GONE);
    }

    /**
     * Método para centrar el mapa en una ubicación. El nivel de zum se ajusta a dos puntos menos que
     * el nivel de zum máximo. Si el número de búsquedas es superior a 10 vez se le solicita al usuario
     * que valore la aplicación.
     * @param latitud Latitud de la ubicación
     * @param longitud Longitud de la ubicación
     */
    private void centraMapa(double latitud, double longitud) {
        mapController.setZoom(nivelMax - 2);
        mapController.setCenter(new GeoPoint(latitud, longitud));
    }

    /**
     * Método para pintar la snackBar con el texto que se desee mostrar al usuario
     * @param texto Texto que se desea mostrar al usuarios
     */
    private void pintaSnackBar(String texto) {
        Snackbar snackbar = Snackbar.make(findViewById(R.id.clMapa), R.string.gracias, Snackbar.LENGTH_SHORT);
        snackbar.setTextColor(dameColor(R.color.colorSecondaryText));
        snackbar.getView().setBackground(dameDrawable(R.drawable.snack));
        snackbar.setText(texto);
        snackbar.show();
    }

    @Override
    public void onActivityResult(int requestCode, int result, Intent data) {
        super.onActivityResult(requestCode, result, data);
        if (requestCode == Login.requestAuth + 1) {//No es necesario comprobar el resultado de la petición según la ayuda oficial
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            GoogleSignInAccount account = null;
            try {
                account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                e.printStackTrace();
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount googleSignInAccount) {
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
        } catch (Exception e) {
            updateUI(null, true);
        }
    }

    public void updateUI(FirebaseUser firebaseUser, boolean registro) {
        if (firebaseUser != null) {
            String idUsuario = firebaseUser.getUid();
            Login.firebaseAnalytics.setUserId(idUsuario);
            Bundle bundle = new Bundle();
            bundle.putString(Auxiliar.uid, idUsuario);
            if (registro)
                Login.firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SIGN_UP, bundle);
            else
                Login.firebaseAnalytics.logEvent(FirebaseAnalytics.Event.LOGIN, bundle);
            try {
                JSONObject jsonObject = PersistenciaDatos.recuperaTarea(getApplication(), PersistenciaDatos.ficheroUsuario, Auxiliar.id);
                if (jsonObject == null || !jsonObject.getString(Auxiliar.uid).equals(idUsuario)) {
                    JSONObject usuario = new JSONObject();
                    usuario.put(Auxiliar.id, Auxiliar.id);
                    usuario.put(Auxiliar.uid, firebaseUser.getUid());
                    PersistenciaDatos.reemplazaJSON(getApplication(), PersistenciaDatos.ficheroUsuario, usuario);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            pintaSnackBar(String.format("%s%s", getString(R.string.hola), firebaseUser.getDisplayName()));
            //invalidateOptionsMenu();
            ocultaOpcionInicioSesionSiIdentificado((navigationView.getMenu()));
            checkPermissions();
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
     * Solicita el permiso de localización.
     */
    public void checkPermissions() {
        permisos = new ArrayList<>();
        String textoPermisos = getString(R.string.necesidad_permisos);
        if (!(ActivityCompat.checkSelfPermission(
                context, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED)) {
            permisos.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            textoPermisos = String.format("%s%s", textoPermisos, getString(R.string.permiso_almacenamiento));
        }
        //Compruebo permisos de localización en primer plano
        if (!(ActivityCompat.checkSelfPermission(
                context, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED)) {
            permisos.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }

        //Comprobación para saber si el usuario se ha identificado
        JSONObject idUsuario = PersistenciaDatos.recuperaTarea(getApplication(), PersistenciaDatos.ficheroUsuario, Auxiliar.id);
        if (idUsuario != null) {
            if (Build.VERSION.SDK_INT == Build.VERSION_CODES.Q) {
                if (!(ActivityCompat.checkSelfPermission(
                        context, Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                        == PackageManager.PERMISSION_GRANTED)) {
                    permisos.add(Manifest.permission.ACCESS_BACKGROUND_LOCATION);
                }
            }
            else {
                if (Build.VERSION.SDK_INT >= 30 && !permisos.contains(Manifest.permission.ACCESS_FINE_LOCATION)){
                    if (!(ActivityCompat.checkSelfPermission(
                            context, Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                            == PackageManager.PERMISSION_GRANTED)) {
                        permisos.add(Manifest.permission.ACCESS_BACKGROUND_LOCATION);
                    }
                }
            }
        }
        if(!permisos.contains(Manifest.permission.ACCESS_BACKGROUND_LOCATION)){
            if(permisos.contains(Manifest.permission.ACCESS_FINE_LOCATION)){
                textoPermisos = String.format("%s%s", textoPermisos, getString(R.string.ubicacion_primer));
            }
        }else{
            permisos.remove(Manifest.permission.ACCESS_FINE_LOCATION);
        }

        //Si no falta ningún servicio se activa el servicio en segundo plano (si el usuario se ha identificado).
        //Muestra la posición del usuario en el mapa
        if (!permisos.isEmpty()) {
            final Dialog dialogoPermisos = new Dialog(context);
            dialogoPermisos.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialogoPermisos.setContentView(R.layout.dialogo_permisos_ubicacion);
            dialogoPermisos.setCancelable(false);
            if(permisos.size() > 1 && permisos.contains(Manifest.permission.ACCESS_BACKGROUND_LOCATION)) {//Se necesitan mostrar dos dialogos
                final TextView tituloPermisos = (TextView) dialogoPermisos.findViewById(R.id.tvTituloPermisos);
                tituloPermisos.setVisibility(View.GONE);
                final TextView textoPermiso = (TextView) dialogoPermisos.findViewById(R.id.tvTextoPermisos);
                textoPermiso.setText(Html.fromHtml(textoPermisos));
                Button salir = (Button) dialogoPermisos.findViewById(R.id.btSalirPermisos);
                salir.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        finishAffinity();
                    }
                });
                final Button siguiente = (Button) dialogoPermisos.findViewById(R.id.btSiguientePermisos);
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
                                        Maps.this,
                                        permisos.toArray(new String[permisos.size()]),
                                        requestCodePermissions);
                            }
                        });
                    }
                });
            } else{
                TextView textView = (TextView) dialogoPermisos.findViewById(R.id.tvTituloPermisos);
                if(permisos.contains(Manifest.permission.ACCESS_BACKGROUND_LOCATION)){//Solo muestro el de ubicación siempre
                    textView.setVisibility(View.VISIBLE);
                    Button salir = (Button) dialogoPermisos.findViewById(R.id.btSalirPermisos);
                    salir.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            finishAffinity();
                        }
                    });
                    Button siguiente = (Button) dialogoPermisos.findViewById(R.id.btSiguientePermisos);
                    siguiente.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if(dialogoPermisos.isShowing())
                                dialogoPermisos.cancel();
                            permisos.add(Manifest.permission.ACCESS_FINE_LOCATION);
                            ActivityCompat.requestPermissions(
                                    Maps.this,
                                    permisos.toArray(new String[permisos.size()]),
                                    requestCodePermissions);
                        }
                    });
                }else {//Solo muestro el normal
                    textView.setVisibility(View.GONE);
                    textView = (TextView) dialogoPermisos.findViewById(R.id.tvTextoPermisos);
                    textView.setText(Html.fromHtml(textoPermisos));
                    Button salir = (Button) dialogoPermisos.findViewById(R.id.btSalirPermisos);
                    salir.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            finishAffinity();
                        }
                    });
                    Button siguiente = (Button) dialogoPermisos.findViewById(R.id.btSiguientePermisos);
                    siguiente.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if(dialogoPermisos.isShowing())
                                dialogoPermisos.cancel();
                            ActivityCompat.requestPermissions(
                                    Maps.this,
                                    permisos.toArray(new String[permisos.size()]),
                                    requestCodePermissions);
                        }
                    });
                }
            }
            dialogoPermisos.show();
        } else {
            if (idUsuario != null &&  !noMolestar)
                lanzaServicioPosicionamiento();
            if (myLocationNewOverlay == null || myLocationNewOverlay.getMyLocation() == null) {
                activaPosicionMapa();
            }

            if (idUsuario != null) {
                if (PersistenciaDatos.leeFichero(
                        getApplication(),
                        PersistenciaDatos.ficheroSegundoPlano).length() == 0) {//El fichero no existe. Muestro el diálogo si es necesario
                    boolean dispositivoConProblemas = false;
                    for (Intent intent : Auxiliar.intentProblematicos()) {
                        if (getPackageManager().resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY) != null) {
                            dialogoSegundoPlanoVisible = true;
                            dialogoSegundoPlano.show();
                            dispositivoConProblemas = true;
                            break;
                        }
                    }
                    if(!dispositivoConProblemas)
                        noVuelvasAMostrarDialogoSegundoPlano();
                }
            }
        }
    }

    /**
     * Método que, una vez que se han obtenido los permisos necesarios, se muestra en el mapa la
     * posición del usuario. Si se detecta el usuario, se cambia el monigote por una flecha.
     */
    private void activaPosicionMapa() {
        GpsMyLocationProvider gpsMyLocationProvider = new GpsMyLocationProvider(context);
        gpsMyLocationProvider.setLocationUpdateMinDistance(5);
        gpsMyLocationProvider.setLocationUpdateMinTime(5000);
        gpsMyLocationProvider.addLocationSource(LocationManager.GPS_PROVIDER);
        gpsMyLocationProvider.addLocationSource(LocationManager.NETWORK_PROVIDER); //Utiliza red y GPS
        myLocationNewOverlay = new MyLocationNewOverlay(gpsMyLocationProvider, map);
        myLocationNewOverlay.enableMyLocation();
        myLocationNewOverlay.setDirectionArrow(BitmapFactory.decodeResource(getResources(), R.drawable.person),
                BitmapFactory.decodeResource(getResources(), R.drawable.ic_flecha_roja));

        myLocationNewOverlay.setEnableAutoStop(true);
        pintaItemsfijos();
    }

    /**
     * Método para pintar los Overlays que siempre se desea que estén en el mapa
     */
    public void pintaItemsfijos() {
        map.getOverlays().clear();
        if (myLocationNewOverlay != null)
            map.getOverlays().add(myLocationNewOverlay);
        map.getOverlays().add(scaleBarOverlay);
    }

    private String idZona = "";

    private LocationManager locationManager;
    private GeoPoint geoPuntoInteres;

    /**
     * Método para mostrar la lista de tareas que contiene el punto de interés.
     * @param puntoInteres Punto de interés con toda la información
     */
    public void muestraPuntoInteres(JSONObject puntoInteres) {
        try {
            //ocultaTodoModos();
            //Con el siguiente if evito que se hagan dos peticiones al servidor
            if (!idZona.equals(puntoInteres.getString(Auxiliar.ficheroZona))) {
                idZona = puntoInteres.getString(Auxiliar.ficheroZona);

                if (puntoInteres.has(Auxiliar.enlaceWiki)) {
                    ivWiki.setVisibility(View.VISIBLE);
                    enlaceWiki = puntoInteres.getString(Auxiliar.enlaceWiki);
                } else {
                    ivWiki.setVisibility(View.INVISIBLE);
                    enlaceWiki = null;
                }

                if (!puntoInteres.has(Auxiliar.caducidad)
                        || System.currentTimeMillis() > puntoInteres.getLong(Auxiliar.caducidad)) {
                    //Tengo que pedir al servidor las tareas
                    peticionTareas(
                            puntoInteres.getString(Auxiliar.contexto),
                            enlaceWiki,
                            puntoInteres.getString(Auxiliar.id),
                            puntoInteres.getString(Auxiliar.ficheroZona),
                            puntoInteres.getDouble(Auxiliar.latitud),
                            puntoInteres.getDouble(Auxiliar.longitud),
                            puntoInteres.getString(Auxiliar.label)
                    );

                } else {
                    //El fichero existe y es válido, por lo que muestro las tareas.
                    pintaTareas(puntoInteres.getString(Auxiliar.id));
                }

                rutaAlPunto = Uri.parse("https://www.google.com/maps/dir/?api=1&destination="
                        + puntoInteres.getDouble(Auxiliar.latitud) + "," + puntoInteres.getDouble(Auxiliar.longitud)
                        + "&travelmode=transit");

                ivSpeaker.setImageDrawable(dameDrawable(R.drawable.ic_speaker));

                svPunto.fullScroll(ScrollView.FOCUS_UP);

                tituloPunto.setText(puntoInteres.getString(Auxiliar.label));
                textoPunto.setText(
                        (puntoInteres.getString(Auxiliar.comment).equals("") ?
                                getResources().getString(R.string.puntoSinTexto) :
                                puntoInteres.getString(Auxiliar.comment)));
                textoPunto.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        if (textoPunto.getLineCount() > 0) {
                            textoPunto.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                            if(textoPunto.getLineCount() > 7){
                                textoPuntoReducido.setText(textoPunto.getText());
                                textoPunto.setVisibility(View.GONE);
                                textoPuntoReducido.setVisibility(View.VISIBLE);
                            }
                        }
                    }
                });

                textoParaAltavoz = String.format(
                        "%s\n%s",
                        puntoInteres.getString(Auxiliar.label),
                        Auxiliar.quitaEnlaces(puntoInteres.getString(Auxiliar.comment)));
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                        && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 10, this);
                    geoPuntoInteres = new GeoPoint(puntoInteres.getDouble(Auxiliar.latitud), puntoInteres.getDouble(Auxiliar.longitud));
                }

                if (myLocationNewOverlay != null && myLocationNewOverlay.getMyLocation() != null) {
                    double distanciaDospuntos = calculaDistanciaDosPuntos(
                            myLocationNewOverlay.getMyLocation(),
                            new GeoPoint(
                                    puntoInteres.getDouble(Auxiliar.latitud),
                                    puntoInteres.getDouble(Auxiliar.longitud)
                            ));
                    if(distanciaDospuntos < 1)
                        distanciaPunto.setText(
                                String.format(
                                        Locale.getDefault(),
                                        "%s %.0fm",
                                        getString(R.string.distancia), distanciaDospuntos*1000));
                    else
                        distanciaPunto.setText(
                                String.format(
                                        Locale.getDefault(),
                                        "%s %.3fkm",
                                        getString(R.string.distancia), distanciaDospuntos));
                }
                else
                    distanciaPunto.setText("");

                if (guiaMapaH != null)
                    guiaMapaH.setGuidelinePercent(0.5f);
                else
                    guiaMapaV.setGuidelinePercent(0.5f);
                btCentrar.setVisibility(View.GONE);
                btNavegar.setVisibility(View.VISIBLE);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * Método para representar las tareas del punto de interés en una lista en la parte inferior de
     * la información.
     *
     * @param ficheroTareas Fichero de donde están las tareas del punto de interés
     */
    public void pintaTareas(String ficheroTareas) {
        JSONArray tareas = PersistenciaDatos.leeFichero(getApplication(), ficheroTareas);
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
            try {//agrego al marcador sus tareas. Dentro está el JSON completo para cuando el usuario decida realizar una de ellas
                jo = tareas.getJSONObject(i);
                try {
                    uriFondo = jo.getString(Auxiliar.recursoImagenBaja);
                } catch (Exception e) {
                    uriFondo = null;
                }
                //Agrego el fichero de donde extraer la tarea
                jo.put(Auxiliar.ficheroOrigen, ficheroTareas);
                id = jo.getString(Auxiliar.id);
                tareasPunto.add(new TareasMapaLista(
                        id,
                        Auxiliar.quitaEnlaces(jo.getString(Auxiliar.recursoAsociadoTexto)).replace("<br>", ""),
                        Auxiliar.ultimaParte(jo.getString(Auxiliar.tipoRespuesta)),
                        uriFondo,
                        jo,
                        listaId.contains(id)));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        adaptadorListaMapa = new AdaptadorListaMapa(this, tareasPunto);
        adaptadorListaMapa.setClickListener(this);
        contenedor.setAdapter(adaptadorListaMapa);
        contenedor.setHasFixedSize(true);
        contenedor.setVisibility(View.VISIBLE);
    }

    /**
     * Método para atender las pulsaciones que se realicen sobre uno de los elementos de la lista de
     * tareas. Se salta a la pantalla de información previa.
     * @param view Vista
     * @param posicion Posicón
     */
    @Override
    public void onItemClick(View view, int posicion) {
        try {
            GeoPoint miPosicion;
            if (myLocationNewOverlay != null && (miPosicion = myLocationNewOverlay.getMyLocation()) != null) {
                JSONObject tarea = adaptadorListaMapa.getTarea(posicion);
                tarea.put(Auxiliar.origen, tarea.getString(Auxiliar.ficheroOrigen));
                Intent intent = new Intent(this, Preview.class);
                String idUsuario;
                String idTarea = tarea.getString(Auxiliar.id);
                intent.putExtra(Auxiliar.id, idTarea);
                intent.putExtra(Auxiliar.posUsuarioLat, miPosicion.getLatitude());
                intent.putExtra(Auxiliar.posUsuarioLon, miPosicion.getLongitude());
                try {
                    idUsuario = Objects.requireNonNull(PersistenciaDatos.recuperaTarea(
                            getApplication(), PersistenciaDatos.ficheroUsuario, Auxiliar.id))
                            .getString(Auxiliar.uid);
                } catch (Exception e) {
                    idUsuario = null;
                }
                if (idUsuario == null) {
                    intent.putExtra(Auxiliar.previa, Auxiliar.mapa);
                    tarea.put(Auxiliar.fechaUltimaModificacion, Auxiliar.horaFechaActual());
                    tarea.put(Auxiliar.tipoRespuesta, Auxiliar.ultimaParte(tarea.getString(Auxiliar.tipoRespuesta)));
                    PersistenciaDatos.guardaJSON(
                            getApplication(),
                            PersistenciaDatos.ficheroNotificadas,
                            tarea,
                            Context.MODE_PRIVATE);
                    startActivity(intent);
                } else { //La tarea puede estar en el fichero de rechazadas, pospuestas o denunciadas ya que el usuario está identificado
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
                            pintaSnackBar(getString(R.string.tareaDenunciadaAntes));
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
                }
            } else {
                if (myLocationNewOverlay != null) {
                    LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                    if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                            && !locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                        pintaSnackBar(getString(R.string.activaUbicacion));
                    }
                } else
                    pintaSnackBar(getString(R.string.recuperandoPosicion));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Método para obtener un objeto que se pueda dibujar compatible con la versión más antigua
     * soportada por la app (API 19). Especialmente util para los xml.
     * @param id Identificador del recurso a representar.
     * @return Objeto que se puede representar
     */
    private Drawable dameDrawable(int id){
        return ResourcesCompat.getDrawable(context.getResources(), id, null);
    }

    private int dameColor(int id){
        return ResourcesCompat.getColor(context.getResources(), id, null);
    }

    /**
     * Método para generar la parte gráfica del marcador. Dentro de un elemento fijo se dibujará el
     * número de tareas.
     * @param size Número de tareas que representa el marcador en un interior
     * @return Representación gráfica del marcador
     */
    private Bitmap generaBitmapMarkerNumero(int size, int tipo) {
        Drawable drawable;

        switch (tipo){
            case 0:
                if (size < 0)
                    drawable = dameDrawable(R.drawable.ic_marcador_pulsado_especial);
                else if (size == 0)
                    drawable = dameDrawable(R.drawable.ic_marcador_check);
                else if (size <= 10)
                    drawable = dameDrawable(R.drawable.ic_marcador100_especial);
                else if (size <= 20)
                    drawable = dameDrawable(R.drawable.ic_marcador300_especial);
                else if (size <= 40)
                    drawable = dameDrawable(R.drawable.ic_marcador500_especial);
                else if (size <= 70)
                    drawable = dameDrawable(R.drawable.ic_marcador700_especial);
                else
                    drawable = dameDrawable(R.drawable.ic_marcador900_especial);
                break;
            case 1://R1
                if (size < 0)
                    drawable = dameDrawable(R.drawable.ic_marcador_pulsado_especial1);
                else if (size == 0)
                    drawable = dameDrawable(R.drawable.ic_marcador_check);
                else if (size <= 10)
                    drawable = dameDrawable(R.drawable.ic_marcador100_especial1);
                else if (size <= 20)
                    drawable = dameDrawable(R.drawable.ic_marcador300_especial);
                else if (size <= 40)
                    drawable = dameDrawable(R.drawable.ic_marcador500_especial1);
                else if (size <= 70)
                    drawable = dameDrawable(R.drawable.ic_marcador700_especial1);
                else
                    drawable = dameDrawable(R.drawable.ic_marcador900_especial1);
                break;
            case 2://R2
                if (size < 0)
                    drawable = dameDrawable(R.drawable.ic_marcador_pulsado_especial2);
                else if (size == 0)
                    drawable = dameDrawable(R.drawable.ic_marcador_check);
                else if (size <= 10)
                    drawable = dameDrawable(R.drawable.ic_marcador100_especial2);
                else if (size <= 20)
                    drawable = dameDrawable(R.drawable.ic_marcador300_especial2);
                else if (size <= 40)
                    drawable = dameDrawable(R.drawable.ic_marcador500_especial2);
                else if (size <= 70)
                    drawable = dameDrawable(R.drawable.ic_marcador700_especial2);
                else
                    drawable = dameDrawable(R.drawable.ic_marcador900_especial2);
                break;
            case 3://R3
                if (size < 0)
                    drawable = dameDrawable(R.drawable.ic_marcador_pulsado_especial3);
                else if (size == 0)
                    drawable = dameDrawable(R.drawable.ic_marcador_check);
                else if (size <= 10)
                    drawable = dameDrawable(R.drawable.ic_marcador100_especial3);
                else if (size <= 20)
                    drawable = dameDrawable(R.drawable.ic_marcador300_especial3);
                else if (size <= 40)
                    drawable = dameDrawable(R.drawable.ic_marcador500_especial3);
                else if (size <= 70)
                    drawable = dameDrawable(R.drawable.ic_marcador700_especial3);
                else
                    drawable = dameDrawable(R.drawable.ic_marcador900_especial3);
                break;
            case 4://R4
                if (size < 0)
                    drawable = dameDrawable(R.drawable.ic_marcador_pulsado_especial4);
                else if (size == 0)
                    drawable = dameDrawable(R.drawable.ic_marcador_check);
                else if (size <= 10)
                    drawable = dameDrawable(R.drawable.ic_marcador100_especial4);
                else if (size <= 20)
                    drawable = dameDrawable(R.drawable.ic_marcador300_especial4);
                else if (size <= 40)
                    drawable = dameDrawable(R.drawable.ic_marcador500_especial4);
                else if (size <= 70)
                    drawable = dameDrawable(R.drawable.ic_marcador700_especial4);
                else
                    drawable = dameDrawable(R.drawable.ic_marcador900_especial4);
                break;
            default:
                if (size < 0)
                    drawable = dameDrawable(R.drawable.ic_marcador_pulsado);
                else if (size == 0)
                    drawable = dameDrawable(R.drawable.ic_marcador_check);
                else if (size <= 10)
                    drawable = dameDrawable(R.drawable.ic_marcador100);
                else if (size <= 20)
                    drawable = dameDrawable(R.drawable.ic_marcador300);
                else if (size <= 40)
                    drawable = dameDrawable(R.drawable.ic_marcador500);
                else if (size <= 70)
                    drawable = dameDrawable(R.drawable.ic_marcador700);
                else
                    drawable = dameDrawable(R.drawable.ic_marcador900);
                break;
        }

        size = Math.abs(size);

        Paint paint = new Paint();
        if (size > 40)
            paint.setARGB(255, 255, 255, 255);
        else
            paint.setARGB(255, 0, 0, 0);

        assert drawable != null;
        Bitmap bitmap = Bitmap.createBitmap(
                drawable.getIntrinsicWidth(),
                drawable.getIntrinsicHeight(),
                Bitmap.Config.ARGB_8888);
        int dimen = drawable.getIntrinsicWidth();
        float mitad = (float) dimen / 2;
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        if (size > 0) {
            String texto;
            int textSize;
            if (size > 99) {
                texto = "99+";
                textSize = (int) (mitad - 15);
            } else {
                texto = String.valueOf(size);
                textSize = (int) (mitad + 1);
            }
            paint.setStyle(Paint.Style.FILL);
            paint.setTextSize(textSize);
            paint.setTextAlign(Paint.Align.CENTER);
            canvas.drawText(texto, mitad, mitad + (float) (textSize / 2), paint);
        }
        return bitmap;
    }

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

    /**
     * Se restaura el mapa tal y como se indica en la guía. Se restaura la lista de tareas si es que
     * el usuario había picado en un marcador con anterioridad
     */
    @Override
    public void onResume() {
        super.onResume();

        checkPermissions();
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        if (map != null)
            map.onResume();
        if (!idZona.equals("") && locationManager != null) {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED)
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 10, this);
        }

        textToSpeech = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status < 0)
                    pintaSnackBar(context.getResources().getString(R.string.noTTS));
                else{
                    textToSpeech.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                        @Override
                        public void onStart(String utteranceId) {
                            ivSpeaker.setImageDrawable(dameDrawable(R.drawable.ic_stop_24));
                        }

                        @Override
                        public void onDone(String utteranceId) {
                            ivSpeaker.setImageDrawable(dameDrawable(R.drawable.ic_speaker));
                        }

                        @Override
                        public void onError(String utteranceId) {
                            ivSpeaker.setImageDrawable(dameDrawable(R.drawable.ic_speaker));
                        }
                    });
                }
            }
        });

        if(textToSpeech.getEngines() == null)
            ivSpeaker.setVisibility(View.INVISIBLE);
        else if(textToSpeech.getEngines().size() <= 0)
            ivSpeaker.setVisibility(View.INVISIBLE);

        if(navigationView == null){
            navigationView = findViewById(R.id.nvMapa);
            navigationView.setNavigationItemSelectedListener(this);
            navigationView.setOnCreateContextMenuListener(this);
        }
        ocultaOpcionInicioSesionSiIdentificado((navigationView.getMenu()));
    }

    /**
     * Método para comprobar si en la parte del mapa que se muestra al usuario hay tareas que representar.
     * Si no se ha descargado del servidor las tareas se solicita la descarga. Si las tareas están desfasdas se
     * solicita al servidor nuevamente la zona.
     *
     * @param descarga Indica si tiene que solicitar al servidor la descarga de una cuadrícula que no tenga
     *                 en la base de datos.
     */
    private void compruebaZona(boolean descarga) {
        if(!marcadorPulsado) {
            BoundingBox pantallaActual = map.getBoundingBox();

            boolean petServer = solicitarAlServidor(pantallaActual);

            try {
                GeoPoint puntoPartida, puntoVariable;
                //Se recupera la primera cuadrícula a comprobar
                puntoPartida = posicionPrimeraComprobacionPantalla(
                        pantallaActual.getLatNorth(),
                        pantallaActual.getLonWest());
                if (puntoPartida == null) {//La primera cuadrícula vendrá dada por el punto más al norte y al oeste
                    puntoPartida = establecePimeraCuadricula();
                }

                BoundingBox bb;
                boolean nuevoCuadrado;
                JSONObject jsonObject;
                List<String> ficherosPintar = new ArrayList<>();

                if (petServer) {
                    //Se recorren las cuadrículas
                    synchronized ((Object) numeroCuadriculasPendientes) {
                        numeroCuadriculasPendientes = 0;
                    }
                    //Número de cuadrículas verticales y horizontales en la vista actual
                    int cuadriculasVerticales = numeroCuadriculas(puntoPartida.getLatitude(), pantallaActual.getLatSouth());
                    int cuadriculasHorizontales = numeroCuadriculas(pantallaActual.getLonEast(), puntoPartida.getLongitude());
                    puntoVariable = new GeoPoint(puntoPartida);
                    JSONArray posicionesCuadriculas = PersistenciaDatos.leeFichero(
                            getApplication(),
                            PersistenciaDatos.ficheroNuevasCuadriculas);
                    for (int i = 0; i < cuadriculasHorizontales; i++) {
                        puntoVariable.setLongitude(puntoPartida.getLongitude() + i * incremento);
                        for (int j = 0; j < cuadriculasVerticales; j++) {
                            puntoVariable.setLatitude(puntoPartida.getLatitude() - j * incremento);
                            nuevoCuadrado = true;
                            //Se comprueba si existe la cuadrícula
                            for (int k = 0; k < posicionesCuadriculas.length(); k++) {
                                jsonObject = posicionesCuadriculas.getJSONObject(k);
                                String id = jsonObject.getString(Auxiliar.id);
                                bb = new BoundingBox(
                                        jsonObject.getDouble(Auxiliar.latN),
                                        jsonObject.getDouble(Auxiliar.lonE),
                                        jsonObject.getDouble(Auxiliar.latS),
                                        jsonObject.getDouble(Auxiliar.lonO));
                                if (bb.contains(
                                        puntoVariable.getLatitude() - 0.00001,
                                        puntoVariable.getLongitude() + 0.00001)) {
                                    if (!jsonObject.has(Auxiliar.caducidad)
                                            || System.currentTimeMillis() > jsonObject.getLong(Auxiliar.caducidad)) {
                                        if (descarga) {
                                            File file = new File(getFilesDir(), jsonObject.getString(Auxiliar.id));
                                            if (file.exists()) {
                                                peticionPuntosInteres(bb, jsonObject.getString(Auxiliar.id));
                                                nuevoCuadrado = false;
                                                break;
                                            }
                                        } else {
                                            break;
                                        }
                                    } else {
                                        if (!ficherosPintar.contains(id))
                                            ficherosPintar.add(id);
                                    }
                                    nuevoCuadrado = false;
                                    break;
                                }
                            }
                            if (descarga && nuevoCuadrado) {
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
                                PersistenciaDatos.guardaJSON(getApplication(),
                                        PersistenciaDatos.ficheroNuevasCuadriculas,
                                        cuadricula,
                                        Context.MODE_PRIVATE);
                                posicionesCuadriculas.put(cuadricula);
                                //Solicitud al servidor
                                peticionPuntosInteres(bb, Long.toString(instante));
                            }
                        }
                    }
                }
                pintaItemsfijos();
                pintaZona(ficherosPintar, pantallaActual.getDiagonalLengthInMeters());

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Método que recupera la información de los lugares que posteriormente se representan a través
     * de los marcadores. Establece la distancia mín. entre dos puntos a partir de la cual se agruparán.
     *
     * @param ficherosPintar Nombre de los ficheros donde se encuentran los puntos a representar
     */
    private void pintaZona(List<String> ficherosPintar, double diagonal){
        //Distancia a la que se van a agrupar los marcadores
        //Evito los marcadores duplicados
        double nivelZum = Math.max(diagonal / 20000, 0.01);//10m;

        JSONArray todasTareas = new JSONArray(),
                puntosEspeciales = new JSONArray(),
                puntosEpecialesR1 = new JSONArray(),
                puntosEpecialesR2 = new JSONArray(),
                puntosEpecialesR3 = new JSONArray(),
                puntosEpecialesR4 = new JSONArray();

        JSONObject puntoInteres;
        JSONArray ficheroPuntosInteres;
        for(String nombreFichero : ficherosPintar){
            ficheroPuntosInteres = PersistenciaDatos.leeFichero(getApplication(), nombreFichero);
            try {
                if (ficheroPuntosInteres != null) {
                    for (int i = 0; i < ficheroPuntosInteres.length(); i++) {
                        puntoInteres = ficheroPuntosInteres.getJSONObject(i);
                        puntoInteres.put(Auxiliar.ficheroZona, nombreFichero);
                        if(puntoInteres.has(Auxiliar.creadoPor)){
                            switch (puntoInteres.getString(Auxiliar.creadoPor)){
                                case Auxiliar.r1:
                                    puntosEpecialesR1.put(puntoInteres);
                                    break;
                                case Auxiliar.r2:
                                    puntosEpecialesR2.put(puntoInteres);
                                    break;
                                case Auxiliar.r3:
                                    puntosEpecialesR3.put(puntoInteres);
                                    break;
                                case Auxiliar.r4:
                                    puntosEpecialesR4.put(puntoInteres);
                                    break;
                                case Auxiliar.creadorInvestigadores:
                                    todasTareas.put(puntoInteres);
                                    break;
                                default:
                                    puntosEspeciales.put(puntoInteres);
                                    break;
                            }
                        }
                        else
                            todasTareas.put(puntoInteres);

                    }
                }
            } catch (JSONException e){
                e.printStackTrace();
            }
        }

        JSONArray[] tareas = {todasTareas, puntosEspeciales, puntosEpecialesR1, puntosEpecialesR2, puntosEpecialesR3, puntosEpecialesR3};

        List<Marcador> listaMarcadores;
        for(int i = 0; i < tareas.length; i++){
            if(tareas[i].length() > 0){
                listaMarcadores = creaAgrupaciones(tareas[i], nivelZum);
                for(Marcador m : listaMarcadores)
                    newMarker(m, i - 1);
            }
        }
    }

    /**
     * Método que crea los marcadores a partir de una distancia mínima.
     *
     * @param todasTareas Lista con la información de todos los puntos a representar.
     * @param nivelZum Distancia mínima entre dos puntos. Si la distancia es menor se agrupan en
     *                 un marcador
     * @return Lista de marcadores que se tienen que representar en el mapa
     */
    private List<Marcador> creaAgrupaciones(JSONArray todasTareas, double nivelZum){
        List<Marcador> listaMarcadores = new ArrayList<>();
        JSONObject puntoInteres;
        Marcador marcador;
        double latitud, longitud;

        JSONArray tareasCompletadas = PersistenciaDatos.leeFichero(getApplication(), PersistenciaDatos.ficheroCompletadas);
        String nombreTareaCompletada, nombrePuntoInteres;
        boolean anterior = false;
        try {
            int nTareasCompletadas;
            while (todasTareas.length() > 0) {//Barro todas las tareas disponibles en el fichero
                puntoInteres = (JSONObject)todasTareas.remove(0);
                latitud = puntoInteres.getDouble(Auxiliar.latitud);
                longitud = puntoInteres.getDouble(Auxiliar.longitud);
                nombrePuntoInteres = puntoInteres.getString(Auxiliar.contexto);
                nTareasCompletadas = 0;
                for(int i = 0; i < tareasCompletadas.length(); i++){
                    nombreTareaCompletada = tareasCompletadas.getJSONObject(i).getString(Auxiliar.contexto);
                    if(nombreTareaCompletada.equals(nombrePuntoInteres))
                        ++nTareasCompletadas;
                }

                if (listaMarcadores.isEmpty()) {
                    marcador = new Marcador();
                    marcador.setTitulo(puntoInteres.getString(Auxiliar.label));
                    marcador.setPosicionMarcador(puntoInteres.getDouble(Auxiliar.latitud), puntoInteres.getDouble(Auxiliar.longitud));
                    marcador.agregaTareaAlMarcador(puntoInteres, puntoInteres.getInt(Auxiliar.nTareas) - nTareasCompletadas);
                    listaMarcadores.add(marcador);
                } else {
                    for (int i = 0; i < listaMarcadores.size(); i++) {
                        anterior = false;
                        marcador = listaMarcadores.get(i);
                        if (latitud == marcador.getLatitud() &&
                                longitud == marcador.getLongitud()) { //La tarea es de la misma posición
                            marcador.agregaTareaAlMarcador(puntoInteres, puntoInteres.getInt(Auxiliar.nTareas) - nTareasCompletadas);
                            listaMarcadores.set(i, marcador);
                            anterior = true;
                            break;
                        } else {//Se comprueba la distancia a la tarea del marcador
                            if (Auxiliar.calculaDistanciaDosPuntos(
                                    marcador.getLatitud(), marcador.getLongitud(),
                                    latitud, longitud)
                                    <= nivelZum) { //Se agrega al marcador ya que se debe agrupar
                                marcador.setTitulo(getString(R.string.agrupacionPuntos));
                                marcador.agregaTareaAlMarcador(puntoInteres, puntoInteres.getInt(Auxiliar.nTareas) - nTareasCompletadas);

                                listaMarcadores.set(i, marcador);
                                anterior = true;
                                break;
                            }
                        }
                    }
                    if (!anterior) {//Hay que agregar un nuevo marcador
                        marcador = new Marcador();
                        marcador.setTitulo(puntoInteres.getString(Auxiliar.label));
                        marcador.setPosicionMarcador(puntoInteres.getDouble(Auxiliar.latitud), puntoInteres.getDouble(Auxiliar.longitud));
                        marcador.agregaTareaAlMarcador(puntoInteres, puntoInteres.getInt(Auxiliar.nTareas) - nTareasCompletadas);
                        listaMarcadores.add(marcador);
                    }
                }
            }
            return  listaMarcadores;
        }catch (Exception e){
            return new ArrayList<>();
        }
    }

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
            if(textToSpeech != null){
                textToSpeech.stop();
            }
            if(ivSpeaker != null)
                ivSpeaker.setImageDrawable(dameDrawable(R.drawable.ic_speaker));
            if(locationManager != null)
                locationManager.removeUpdates(this);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if(locationManager != null) locationManager.removeUpdates(this);
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        if(textToSpeech != null)
            textToSpeech.shutdown();
    }

    /**
     * Método que responde a la pulsación del alguno de los botones
     * @param view Instancia del botón pulsado que ha lanzado el método
     */
    public void boton(View view) {
        switch (view.getId()){
            case R.id.btCentrar: //Solo centra la posición si se ha conseguido recuperar
                //ocultaModos();
                if(myLocationNewOverlay.getMyLocation() != null) {
                    mapController.setZoom(nivelMax);
                    mapController.setCenter(myLocationNewOverlay.getMyLocation());
                }else{ //Si aún no se conoce se muestra un mensaje
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
            case R.id.ivSpeaker:
                if(textToSpeech != null) {
                    if(textToSpeech.isSpeaking()) {
                        textToSpeech.stop();
                        ivSpeaker.setImageDrawable(dameDrawable(R.drawable.ic_speaker));
                    }else{
                        if (textoParaAltavoz != null && !textoParaAltavoz.equals("")) {
                            HashMap<String, String> map = new HashMap<>();
                            map.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "speakerMapa");
                            textToSpeech.speak(textoParaAltavoz, TextToSpeech.QUEUE_FLUSH, map);
                        }
                    }
                }
                break;
            case R.id.ivWikipediaMapa:
                Auxiliar.navegadorInterno(this, enlaceWiki);
                break;
            case R.id.tvPuntoTextoReducido:
                ocultaReducido();
                break;
            case R.id.btNavegarMaps:
                try {
                    Intent intentRuta = new Intent(Intent.ACTION_VIEW, rutaAlPunto);
                    startActivity(Intent.createChooser(intentRuta, ""));
                }catch (Exception e){
                    e.printStackTrace();
                }
                break;
            /*case R.id.btModo:
                if(!modos.isShown()) {
                    btModos.hide();
                    configuraModos();
                }
                break;
            case R.id.modo1:
                btModos.setImageDrawable(dameDrawable(R.drawable.ic_uno));
                numero = 1;
                modos.setVisibility(View.GONE);
                btModos.show();
                break;
            case R.id.modo2:
                btModos.setImageDrawable(dameDrawable(R.drawable.ic_dos));
                numero = 2;
                modos.setVisibility(View.GONE);
                btModos.show();
                break;
            case R.id.modo3:
                btModos.setImageDrawable(dameDrawable(R.drawable.ic_tres));
                numero = 3;
                modos.setVisibility(View.GONE);
                btModos.show();
                break;
            case R.id.modo4:
                btModos.setImageDrawable(dameDrawable(R.drawable.ic_cuatro));
                numero = 4;
                modos.setVisibility(View.GONE);
                btModos.show();
                break;*/
            default:
                break;
        }
    }

    /*private void configuraModos() {
        for(Button b : btsModo){
            b.setBackground(dameDrawable(R.drawable.boton_rojo));
        }
        switch (numero){
            case 1:
                btModos1.setBackground(dameDrawable(R.drawable.boton_secundario));
                break;
            case 2:
                btModos2.setBackground(dameDrawable(R.drawable.boton_secundario));
                break;
            case 3:
                btModos3.setBackground(dameDrawable(R.drawable.boton_secundario));
                break;
            default:
                btModos4.setBackground(dameDrawable(R.drawable.boton_secundario));
                break;
        }
        modos.setVisibility(View.VISIBLE);
    }*/

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
        bundle.putBoolean("DIALOGOSEGUNDOPLANO", dialogoSegundoPlanoVisible);
        bundle.putBoolean("DIALOGOCONFPOR", dialogoConfiguracionPorfolioVisible);
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
        if (Ajustes.NO_MOLESTAR_pref.equals(key)) {
            noMolestar = sharedPreferences.getBoolean(key, false);
        }
    }

    /**
     * Método para lanzar el servicio en segundo plano para hacer las llamadas a una alarma de
     * manera periódica.
     */
    private void lanzaServicioPosicionamiento(){
        new AlarmaProceso().activaAlarmaProceso(getApplicationContext());
    }

    /*/**
     * Creación del menú en el layout
     * @param menu Menú a rellenar
     * @return Verdadero si se va a mostrar el menú
     */
    /*@Override
    public boolean onCreateOptionsMenu(Menu menu){
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }*/

    /*@Override
    public boolean onPrepareOptionsMenu(Menu menu){
        JSONObject idUsuario = PersistenciaDatos.recuperaTarea(getApplication(), PersistenciaDatos.ficheroUsuario, Auxiliar.id);
        MenuItem menuItem = menu.findItem(R.id.cerrarSesion);
        if(idUsuario == null) {//Si el usuario no se ha identificado se cambia la etiqueta a mostrar
            menuItem.setTitle(getString(R.string.iniciarSesion));
        }else{
            menuItem.setVisible(false);
        }
        return super.onPrepareOptionsMenu(menu);
    }*/

    @Override
    public boolean onNavigationItemSelected(MenuItem menuItem){
        onBackPressed();
        return onOptionsItemSelected(menuItem);
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
            case R.id.menuLugaresNotificados:
                intent = new Intent(this, ListaContextos.class);
                startActivity(intent);
                return true;
            case R.id.cerrarSesion://Puede ser el de inicio de sesión si el usuario aún no se ha identificado
                JSONObject idUsuario = PersistenciaDatos.recuperaTarea(getApplication(), PersistenciaDatos.ficheroUsuario, Auxiliar.id);
                if(idUsuario == null) {
                    Login.gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                            .requestIdToken(getString(R.string.default_web_client_id))
                            .requestEmail().build();
                    Login.googleSignInClient = GoogleSignIn.getClient(context, Login.gso);
                    startActivityForResult(Login.googleSignInClient.getSignInIntent(), Login.requestAuth + 1);
                }
                return true;
            case R.id.valora:
                try {
                    intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse(
                            "https://play.google.com/store/apps/details?id=" + getPackageName()));
                    intent.setPackage("com.android.vending");
                    startActivity(intent);
                }catch (ActivityNotFoundException e){
                    Toast.makeText(context, getResources().getString(R.string.noGooglePlay), Toast.LENGTH_SHORT).show();
                }
                return true;
            case R.id.comparte:
                intent = new Intent(Intent.ACTION_SEND);
                intent.putExtra(Intent.EXTRA_TEXT, context.getResources().getString(R.string.urlLanding));
                intent.setType("text/plain");
                startActivity(Intent.createChooser(intent, context.getResources().getString(R.string.app_name)));
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
        if(drawerLayout.isDrawerOpen(GravityCompat.START))
            drawerLayout.closeDrawer(GravityCompat.START);
        else {
            if (marcadorPulsado) {
                ocultaInfoPuntoInteres();
            } else {
                dialogoSalirAppActivo = true;
                dialogoSalirApp.show();
            }
        }
    }

    /**
     * Método para establecer la primera cuadrícula. <del>Se coge el punto extremo superior más al oeste y
     * se construye dicha cuadrícula.</del> Se establece el punto en Teleco para que tenga sentido hacer una
     * caché en la pasarela
     *
     * @return Punto más al norte y más al oeste
     */
    public GeoPoint establecePimeraCuadricula(){
        //Teleco
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
        return (int)Math.ceil((latNolonE-latSoLonO)/incremento);
    }

    /**
     * Método para saber si es neceario solicitar al servidor las tareas. Se utiliza para que cuando
     * el zum sea bajo no se pidan muchas zonas al servidor.
     * @param boundingBox Caja que representa al mapa actual
     * @return Verdadero si se puede solictar o falso si no se debe
     */
    public boolean solicitarAlServidor(BoundingBox boundingBox){
        return !(boundingBox.getDiagonalLengthInMeters() / 2000 > 5);
    }

    /**
     * Método para obtener del servidor los puntos de interés de un área determinada. Almacena los
     * resultados en un fichero con el nombre que se indique. Actualiza el valor de la fecha de caducidad
     * de la cuadrícula
     *
     * @param caja Área del que se solicitan los puntos de interés
     * @param nombre Nombre del fichero donde se van a almacenera los puntos de interés
     */
    private void peticionPuntosInteres(final BoundingBox caja, final String nombre){
        List<String> keys = new ArrayList<>();
        List<Object> objects = new ArrayList<>();
        keys.add(Auxiliar.norte); objects.add(caja.getLatNorth());
        keys.add(Auxiliar.este); objects.add(caja.getLonEast());
        keys.add(Auxiliar.sur); objects.add(caja.getLatSouth());
        keys.add(Auxiliar.oeste); objects.add(caja.getLonWest());
        try{
            /*Envío el ID del usuario si lo tuviera. Por ahora no se está haciendo nada con este dato,
            pero en un futuro podrá utilizarse para personalizar los contextos que se muestran. */
            JSONObject usuario = PersistenciaDatos.recuperaTarea(
                    getApplication(), PersistenciaDatos.ficheroUsuario, Auxiliar.id);
            if(usuario != null) {
                String idUsuario = usuario.getString(Auxiliar.uid);
                if(!idUsuario.trim().equals("")) {
                    keys.add(Auxiliar.id);
                    objects.add(idUsuario);
                }
            }
        } catch (Exception e){
            e.printStackTrace();
        }

        String url = Auxiliar.creaQuery(Auxiliar.rutaContextos, keys, objects);

        synchronized ((Object)numeroCuadriculasPendientes) {
            ++numeroCuadriculasPendientes;
        }

        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(
                Request.Method.GET,
                url,
                null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray puntosInteres) {
                        //Tengo que guardar el fichero específico de la cuadrícula con los puntos de interés
                        if (puntosInteres != null) {
                            JSONArray ficheroAntiguo = PersistenciaDatos.leeFichero(
                                    getApplication(),
                                    nombre);
                            //Si el fichero está vacío no va a tener tareas relacionadas
                            if (ficheroAntiguo == null || ficheroAntiguo.length() == 0) {
                                JSONObject puntoInteres;
                                for (int i = 0; i < puntosInteres.length(); i++) {
                                    try {
                                        puntoInteres = puntosInteres.getJSONObject(i);
                                        puntoInteres.put(Auxiliar.id,
                                                String.format("%d%d", System.currentTimeMillis(), System.nanoTime()));
                                        puntosInteres.put(i, puntoInteres);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                                PersistenciaDatos.guardaFichero(
                                        getApplication(),
                                        nombre,
                                        puntosInteres,
                                        Context.MODE_PRIVATE);
                            } else {
                                JSONArray vectorFinal = new JSONArray();
                                JSONObject nuevoPunto, puntoAntiguo;
                                //Me quedo con los datos del servidor más actualizados
                                for (int i = 0; i < puntosInteres.length(); i++) {
                                    try {
                                        nuevoPunto = puntosInteres.getJSONObject(i);
                                        for (int j = 0; j < ficheroAntiguo.length(); j++) {
                                            puntoAntiguo = ficheroAntiguo.getJSONObject(j);
                                            //Si hay conincidencia guardo el instante y el nombre del fichero si lo tuviera
                                            if (nuevoPunto.getString(Auxiliar.contexto).equals(puntoAntiguo.getString(Auxiliar.contexto))) {
                                                if (puntoAntiguo.has(Auxiliar.caducidad) && puntoAntiguo.has(Auxiliar.id)) {
                                                    nuevoPunto.put(Auxiliar.caducidad, puntoAntiguo.getLong(Auxiliar.caducidad));
                                                    nuevoPunto.put(Auxiliar.id, puntoAntiguo.getString(Auxiliar.id));
                                                }
                                                break;
                                            }
                                        }
                                        if (!nuevoPunto.has(Auxiliar.id))
                                            nuevoPunto.put(Auxiliar.id, String.format("%d%d", System.currentTimeMillis(), System.nanoTime()));
                                        vectorFinal.put(nuevoPunto);
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                                PersistenciaDatos.guardaFichero(
                                        getApplication(),
                                        nombre,
                                        vectorFinal,
                                        Context.MODE_PRIVATE);
                            }
                        }

                        try {//Actualización del fichero de cuadrículas
                            JSONObject cuadricula = PersistenciaDatos.recuperaTarea(
                                    getApplication(),
                                    PersistenciaDatos.ficheroNuevasCuadriculas,
                                    nombre);
                            cuadricula.put(Auxiliar.caducidad,
                                    System.currentTimeMillis() + 86400000);//Caduca al día
                            PersistenciaDatos.reemplazaJSON(
                                    getApplication(),
                                    PersistenciaDatos.ficheroNuevasCuadriculas,
                                    cuadricula);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        synchronized ((Object) numeroCuadriculasPendientes) {
                            if (numeroCuadriculasPendientes > 0) {
                                --numeroCuadriculasPendientes;
                                if (numeroCuadriculasPendientes == 0)
                                    compruebaZona(false);
                            }
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        synchronized ((Object) numeroCuadriculasPendientes) {
                            if (numeroCuadriculasPendientes > 0) {
                                --numeroCuadriculasPendientes;
                                if (numeroCuadriculasPendientes == 0)
                                    compruebaZona(false);
                            }
                        }
                    }
                }
        );

        ColaConexiones.getInstance(getApplicationContext()).getRequestQueue().add(jsonArrayRequest);
    }

    /**
     * Método para recuperar las tareas pertenecientes a un contexto. Almacena las tareas en el fichero que
     * se le indique y actualiza la fecha de caducidad en el fichero donde esté almacenado el punto
     * de interés
     *
     * @param contexto Identificador del punto de interés
     * @param nombreFicheroTareas Nombre que se desea poner al fichero donde se almacenan las tareas
     * @param nombreFicheroZona Nombre del fichero donde está almacenado el punto de interés
     */
    private void peticionTareas(
            final String contexto,
            final String enlaceWikipedia,
            final String nombreFicheroTareas,
            final String nombreFicheroZona,
            final double latitud,
            final double longitud,
            final String nombre){//Seguro que necesito algo más como el id del lugar donde lo voy a colocar
        List<String> keys = new ArrayList<>();
        List<Object> objects = new ArrayList<>();
        keys.add(Auxiliar.contextos); objects.add(contexto);
        String url = Auxiliar.creaQuery(Auxiliar.rutaTareas, keys, objects);

        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(
                Request.Method.GET,
                url,
                null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray tareas) {
                        if(tareas != null){
                            JSONObject tarea;
                            JSONArray tareasG = new JSONArray();
                            for(int i = 0; i < tareas.length(); i++){
                                try {
                                    tarea = tareas.getJSONObject(i);
                                    tarea.put(Auxiliar.latitud, latitud);
                                    tarea.put(Auxiliar.longitud, longitud);
                                    if(enlaceWikipedia != null)
                                        tarea.put(Auxiliar.enlaceWiki, enlaceWikipedia);
                                    if(!tarea.has(Auxiliar.comment) || tarea.getString(Auxiliar.comment).equals(""))
                                        tarea.put(Auxiliar.comment, nombre);
                                    tareasG.put(tarea);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                            boolean actualiza = true;
                            if(tareas.length() > 0)
                                actualiza = PersistenciaDatos.guardaFichero(
                                        getApplication(),
                                        nombreFicheroTareas,
                                        tareasG,
                                        Context.MODE_PRIVATE
                                );
                            if(actualiza){
                                try {
                                    JSONObject puntoInteres = PersistenciaDatos.recuperaTarea(
                                            getApplication(),
                                            nombreFicheroZona,
                                            nombreFicheroTareas
                                    );
                                    puntoInteres.put(Auxiliar.caducidad,
                                            System.currentTimeMillis() + 86400000);//Caduca al día
                                    PersistenciaDatos.reemplazaJSON(
                                            getApplication(),
                                            nombreFicheroZona,
                                            puntoInteres
                                    );
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }

                            compruebaZona(false);

                            if(nombreFicheroZona.equals(idZona)){
                                pintaTareas(nombreFicheroTareas);
                            }
                        }
                    }
                },
                null
        );

        //Aumento el tiempo ya que el servidor tiene que recuperar la frase y autor de las fotografías
        jsonArrayRequest.setRetryPolicy(new DefaultRetryPolicy(
                18000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        ColaConexiones.getInstance(getApplicationContext()).getRequestQueue().add(jsonArrayRequest);
    }

    /**
     * Método que se utiliza para representar un marcador en el mapa. Al pulsar obre el marcador se
     * muestra la información del lugar y se descargar, si fuera necesario, la lista de tareas que
     * contiene. También se muestra esta lista al final de la información.
     *
     * @param marcador Información que representa al marcador
     */
    void newMarker(final Marcador marcador, final int tipo) {
        Marker marker = new Marker(map);
        marker.setPosition(new GeoPoint(marcador.getLatitud(), marcador.getLongitud()));
        BitmapDrawable d = new BitmapDrawable(
                context.getResources(),
                generaBitmapMarkerNumero(marcador.getNumeroTareas(), tipo));
        marker.setIcon(d);

        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT)
            marker.setInfoWindow(new Bocadillo(R.layout.bocadillo, map));

        marker.setTitle(marcador.getTitulo());
        final GeoPoint geoPoint;
        if(getResources().getConfiguration().orientation == android.content.res.Configuration.ORIENTATION_PORTRAIT)
            geoPoint = new GeoPoint(marcador.getLatitud() - (map.getLatitudeSpanDouble()/5),
                    marcador.getLongitud());
        else
            geoPoint = new GeoPoint(marcador.getLatitud(),
                    marcador.getLongitud() + (map.getLongitudeSpanDouble()/5));
        marker.setOnMarkerClickListener(new Marker.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker, MapView mapView) {
                mapController.animateTo(geoPoint);
                marker.setIcon(new BitmapDrawable(
                        context.getResources(),
                        generaBitmapMarkerNumero(marcador.getNumeroTareas()*-1 , tipo)));
                marcadorPulsado = true;
                String msg = getString(R.string.recuperandoPosicion);
                try {
                    if(myLocationNewOverlay != null){
                        LocationManager lM = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                        if(!lM.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                            msg = getString(R.string.activaUbicacion);
                            marker.setSubDescription(msg);
                            marker.showInfoWindow();
                        }else{
                            if(marcador.getNumeroPuntos() == 1) {
                                muestraPuntoInteres(marcador.getTareasMarcador().getJSONObject(0));
                            } else{
                                muestraDialogo(marcador);
                            }
                        }
                    }
                } catch (Exception e) {
                    msg = getString(R.string.recuperandoPosicion);
                    marker.setSubDescription(msg);
                    marker.showInfoWindow();
                }


                if(marcador.getNumeroPuntos() == 1) {
                    try {
                        muestraPuntoInteres(marcador.getTareasMarcador().getJSONObject(0));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                return false;
            }
        });
        map.getOverlays().add(marker);
        map.invalidate();
    }
    private AdaptadorListaPuntos adaptadorListaPuntos;

    /**
     * Método para mostrar el diálogo con los puntos de interés agrupados en un único marcador
     *
     * @param marcador Marcador donde están agrupados los puntos de interés
     */
    public void muestraDialogo(Marcador marcador){
        try {
            JSONArray puntos = marcador.getTareasMarcador();
            JSONObject punto;
            PuntoSingular puntoSingular, puntoCompara;
            List<PuntoSingular> lista = new ArrayList<>();
            boolean guardado;
            GeoPoint geoPoint = myLocationNewOverlay.getMyLocation();
            for (int i = 0; i < puntos.length(); i++) {
                punto = puntos.getJSONObject(i);
                puntoSingular = new PuntoSingular(
                        punto.getString(Auxiliar.label),
                        calculaDistanciaDosPuntos(
                                geoPoint,
                                new GeoPoint(
                                        punto.getDouble(Auxiliar.latitud),
                                        punto.getDouble(Auxiliar.longitud)
                                ))
                        , punto
                );
                guardado = false;
                for(int j = 0; j < lista.size(); j++){
                    puntoCompara = lista.get(j);
                    if(puntoSingular.getDistancia() < puntoCompara.getDistancia()){
                        lista.add(j, puntoSingular);
                        guardado = true;
                        break;
                    }
                }
                if(!guardado)
                    lista.add(puntoSingular);
            }
            RecyclerView rvPuntosInteres = dialogoVariosPuntos.findViewById(R.id.rvPuntosInteres);
            rvPuntosInteres.setLayoutManager(new LinearLayoutManager(
                    this, LinearLayoutManager.VERTICAL, false));

            adaptadorListaPuntos = new AdaptadorListaPuntos(this, lista);
            adaptadorListaPuntos.setClickListenerDialogo(this);
            rvPuntosInteres.setAdapter(adaptadorListaPuntos);
            dialogoVariosPuntos.show();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * Método para controlar la pulsación sobre un item de la lista de municipios
     *
     * @param view Vista pulsada
     * @param position Posición que ocupa en la lista
     */
    @Override
    public void onItemClickDialogo(View view, int position) {
        searchView.setIconified(true);
        JSONObject busquedas = null;
        try {
            busquedas = PersistenciaDatos.obtenTarea(
                    getApplication(),
                    PersistenciaDatos.ficheroPrimeraApertura,
                    PersistenciaDatos.ficheroPrimeraApertura);
            busquedas.put(Auxiliar.busquedasMunicipio, busquedas.getInt(Auxiliar.busquedasMunicipio)+1);
            PersistenciaDatos.guardaJSON(
                    getApplication(),
                    PersistenciaDatos.ficheroPrimeraApertura,
                    busquedas,
                    Context.MODE_PRIVATE);
        }catch (Exception e){
            if(busquedas != null){
                PersistenciaDatos.guardaJSON(
                        getApplication(),
                        PersistenciaDatos.ficheroPrimeraApertura,
                        busquedas,
                        Context.MODE_PRIVATE);
            }
        }

        if(contenedorBusqMapa != null && contenedorBusqMapa.getVisibility() == View.VISIBLE){
            ocultaContenedorBusqMapa();
        }

        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if(this.getCurrentFocus() != null && inputMethodManager.isActive(this.getCurrentFocus()))
            inputMethodManager.hideSoftInputFromWindow(this.getCurrentFocus().getWindowToken(), 0);
        centraMapa(adaptadorListaCoincidencia.getLatitud(position),
                adaptadorListaCoincidencia.getLongitud(position));
        if(!searchView.isIconified())
            searchView.setIconified(true);

        //ocultaModos();
        llamadaAPlayStore();
    }

    /**
     * Método para controlar la pulsación sobre un item del diálogo de la agrupación de puntos de
     * interés
     * @param view Vista
     * @param position Posición que ocupa en la lista
     */
    @Override
    public void onItemClickDialogoVariosPuntos(View view, int position) {
        //Se ha pulsado sobre un punto de interés de la agrupación
        dialogoVariosPuntos.cancel();
        JSONObject puntoInteres = adaptadorListaPuntos.getPunto(position);
        muestraPuntoInteres(puntoInteres);
    }

    /**
     * Método que se utiliza para actualizar la distancia del usuario al punto de interés en la
     * información del punto.
     * @param location Ubicación del usuario
     */
    @Override
    public void onLocationChanged(Location location) {
        if(!idZona.equals("")) {
            if (distanciaPunto != null) {
                double distanciaDospuntos = calculaDistanciaDosPuntos(
                        new GeoPoint(location),
                        geoPuntoInteres);

                if(distanciaDospuntos < 1)
                    distanciaPunto.setText(
                            String.format(
                                    Locale.getDefault(),
                                    "%s %.0fm",
                                    getString(R.string.distancia), distanciaDospuntos*1000));
                else
                    distanciaPunto.setText(
                            String.format(
                                    Locale.getDefault(),
                                    "%s %.3fkm",
                                    getString(R.string.distancia), distanciaDospuntos));
            }
        }else{
            if(locationManager != null)
                locationManager.removeUpdates(this);
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

    /**
     * Método para que se le muestre al usuario la sugerencia de valorar la aplicación en Google Play
     */
    public void llamadaAPlayStore(){
        final Activity activity = this;
        JSONObject primeraApertura = PersistenciaDatos.recuperaTarea(
                getApplication(),
                PersistenciaDatos.ficheroPrimeraApertura,
                PersistenciaDatos.ficheroPrimeraApertura);
        if(primeraApertura != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                try {
                    if (
                            primeraApertura.getLong(Auxiliar.instante) > System.currentTimeMillis()
                                    && (
                                    primeraApertura.getInt(Auxiliar.tareas) > 2 ||
                                            primeraApertura.getInt(Auxiliar.busquedasMunicipio) > 9
                            )) {
                        final ReviewManager reviewManager = ReviewManagerFactory.create(this);
                        com.google.android.play.core.tasks.Task<ReviewInfo> peticion = reviewManager.requestReviewFlow();
                        peticion.addOnCompleteListener(new com.google.android.play.core.tasks.OnCompleteListener<ReviewInfo>() {
                            @Override
                            public void onComplete(com.google.android.play.core.tasks.Task<ReviewInfo> task) {
                                if (task.isSuccessful()) {
                                    ReviewInfo reviewInfo = task.getResult();
                                    com.google.android.play.core.tasks.Task<Void> flow =
                                            reviewManager.launchReviewFlow(activity, reviewInfo);
                                    flow.addOnCompleteListener(new com.google.android.play.core.tasks.OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(com.google.android.play.core.tasks.Task<Void> peticionFlujo) {
                                            if (peticionFlujo.isSuccessful()) {
                                                noVolverAPreguntar();
                                            }
                                        }
                                    });
                                } else {
                                    noVolverAPreguntar();
                                }
                            }
                        });
                    }
                } catch (Exception e){
                    e.printStackTrace();
                    noVolverAPreguntar();
                }
            } else {//No es compatible con la api de petición de revisiones online
                noVolverAPreguntar();
            }
        }
    }

    /**
     * Método para no volver a mostrar la sugerencia de valoración de la aplicación en Google Play
     */
    private void noVolverAPreguntar(){
        try {
            JSONObject primeraApertura = PersistenciaDatos.obtenTarea(
                    getApplication(),
                    PersistenciaDatos.ficheroPrimeraApertura,
                    PersistenciaDatos.ficheroPrimeraApertura);
            //Al guardar el identificador con otro nombre no se volverá a solicitar al usuario. Tampoco se regenera en la pantalla de login
            primeraApertura.put(
                    Auxiliar.id,
                    PersistenciaDatos.ficheroPrimeraApertura + PersistenciaDatos.ficheroPrimeraApertura);
            PersistenciaDatos.guardaJSON(
                    getApplication(),
                    PersistenciaDatos.ficheroPrimeraApertura,
                    primeraApertura,
                    Context.MODE_PRIVATE);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
