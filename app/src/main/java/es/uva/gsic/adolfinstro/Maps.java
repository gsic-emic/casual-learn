package es.uva.gsic.adolfinstro;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.Guideline;
import androidx.core.app.ActivityCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.ComponentName;
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
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.text.Html;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuInflater;
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
import com.android.volley.toolbox.JsonArrayRequest;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
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
 * @version 20201119
 */
public class Maps extends AppCompatActivity implements
        SharedPreferences.OnSharedPreferenceChangeListener,
        AdaptadorListaMapa.ItemClickListener,
        AdaptadorListaCoincidencia.ItemClickListenerDialogo,
        AdaptadorListaPuntos.ItemClickListenerDialogoVariosPuntos,
        LocationListener {
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
    Boolean dialogoSalirAppActivo = false;

    /** Dialogo para mostrar los distintos puntos que agrupa un marcador */
    Dialog dialogoVariosPuntos;

    /** Guía de la vista vertical */
    Guideline guiaMapaH;
    /** Guía de la vista apaisada*/
    Guideline guiaMapaV;

    /** Adaptador para la lista de municipios cuando se realiza una búsqueda*/
    private AdaptadorListaCoincidencia adaptadorListaCoincidencia;

    private boolean marcadorPulsado = false;

    /** Objeto para indicar el nombre del punto de interés*/
    TextView tituloPunto;
    /** Objeto para indicar la descripción del punto de interés*/
    TextView textoPunto;
    /** Objeto para indicar la distancia al punto de interés */
    TextView distanciaPunto;
    /** Objeto para el texto reducido del punto de interés*/
    TextView textoPuntoReducido;
    /** Objeto para mostrar la información completa del punto de interés*/
    Button masInfo;

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

    private Dialog dialogoSegundoPlano;
    private boolean dialogoSegundoPlanoVisible;

    /**
     * Método con el que se pinta la actividad. Lo primero que comprueba es si está activada el modo no
     * molestar para saber si se tiene que mostar el mapa o no
     * @param savedInstanceState Bundle
     */
    @Override
    protected void onCreate(final Bundle savedInstanceState) {

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        //context = getApplicationContext(); //contexto de la aplicación
        context = this;
        Configuration.getInstance().load(context, PreferenceManager.getDefaultSharedPreferences(context));
        Configuration.getInstance().setUserAgentValue(BuildConfig.APPLICATION_ID);
        super.onCreate(savedInstanceState);
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

        //Se decide si se muestra el mapa
        if (noMolestar) {
            setContentView(R.layout.no_molestar);
        } else {
            setContentView(R.layout.activity_maps);
            map = findViewById(R.id.map);
            contenedor = findViewById(R.id.rvTareasMapa);
            contenedorBusqMapa = findViewById(R.id.rvBusquedaMapa);

            guiaMapaH = findViewById(R.id.guiaMapa);
            guiaMapaV = findViewById(R.id.guiaMapaV);

            contenedor.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

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

            searchView = findViewById(R.id.svMapa);
            searchView.setOnSearchClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(marcadorPulsado)
                        ocultaInfoPuntoInteres();
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
                            if (btCentrar.isShown())
                                btCentrar.hide();
                            contenedorBusqMapa.setLayoutManager(new LinearLayoutManager(
                                    context, LinearLayoutManager.VERTICAL, false));
                            contenedorBusqMapa.setBackgroundColor(getResources().
                                    getColor(R.color.transparente));
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
                            if (!btCentrar.isShown())
                                btCentrar.show();
                        }

                    } else {
                        ocultaContenedorBusqMapa();
                        if (!btCentrar.isShown())
                            btCentrar.show();
                    }
                    return false;
                }
            });
        }

        tituloPunto = findViewById(R.id.tvPuntoTitulo);
        textoPunto = findViewById(R.id.tvPuntoTexto);
        distanciaPunto = findViewById(R.id.tvPuntoDistancia);
        textoPuntoReducido = findViewById(R.id.tvPuntoTextoReducido);
        masInfo = findViewById(R.id.btMasInfoPunto);

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

        try {
            String contenido = Objects.requireNonNull(getIntent().getExtras()).getString(Auxiliar.textoParaElMapa);
            if (contenido != null && !contenido.equals("")) {
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
                    snackbar.setTextColor(getResources().getColor(R.color.colorSecondaryText));
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
                    snackbar.setActionTextColor(getResources().getColor(R.color.colorSecondary50));
                    snackbar.getView().setBackground(getResources().getDrawable(R.drawable.snack));
                    snackbar.show();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

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
        } catch (JSONException e){
            e.printStackTrace();
        }
    }

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
    }

    private void ocultaReducido() {
        textoPuntoReducido.setVisibility(View.GONE);
        masInfo.setVisibility(View.GONE);
        textoPunto.setVisibility(View.VISIBLE);
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
        snackbar.setTextColor(getResources().getColor(R.color.colorSecondaryText));
        snackbar.getView().setBackground(getResources().getDrawable(R.drawable.snack));
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
            invalidateOptionsMenu();
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
        //Compruebo permisos de localización en primer y segundo plano
        if (!(ActivityCompat.checkSelfPermission(
                context, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED)) {
            permisos.add(Manifest.permission.ACCESS_FINE_LOCATION);
            //textoPermisos = String.format("%s%s", textoPermisos, getString(R.string.ubicacion_primer));
        }
        //Comprobación para saber si el usuario se ha identificado
        JSONObject idUsuario = PersistenciaDatos.recuperaTarea(getApplication(), PersistenciaDatos.ficheroUsuario, Auxiliar.id);
        if (idUsuario != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
                if (!(ActivityCompat.checkSelfPermission(
                        context, Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                        == PackageManager.PERMISSION_GRANTED)) {
                    permisos.add(Manifest.permission.ACCESS_BACKGROUND_LOCATION);
                    //textoPermisos = String.format("%s%s", textoPermisos, getString(R.string.ubicacion_segundo));
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
            if (idUsuario != null)
                lanzaServicioPosicionamiento();
            if (myLocationNewOverlay == null || myLocationNewOverlay.getMyLocation() == null) {
                activaPosicionMapa();
            }

            if (idUsuario != null) {
                JSONArray ficheroSegundoPlano = PersistenciaDatos.leeFichero(
                        getApplication(),
                        PersistenciaDatos.ficheroSegundoPlano);
                if (ficheroSegundoPlano.length() == 0) {//El fichero no existe. Muestro el diálogo si es necesario
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
                        noVolverAPreguntar();
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

                ivSpeaker.setImageDrawable(ResourcesCompat.getDrawable(context.getResources(), R.drawable.ic_speaker, null));

                svPunto.fullScroll(ScrollView.FOCUS_UP);

                tituloPunto.setText(puntoInteres.getString(Auxiliar.label));
                textoPunto.setText(
                        (puntoInteres.getString(Auxiliar.comment).equals("") ?
                                getResources().getString(R.string.puntoSinTexto) :
                                puntoInteres.getString(Auxiliar.comment)));
                int tama = textoPunto.getLineCount();
                textoPunto.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        if (textoPunto.getLineCount() > 0) {
                            textoPunto.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                            if(textoPunto.getLineCount() > 7){
                                textoPuntoReducido.setText(textoPunto.getText());
                                textoPunto.setVisibility(View.GONE);
                                textoPuntoReducido.setVisibility(View.VISIBLE);
                                masInfo.setVisibility(View.VISIBLE);
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
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    public void pintaTareas(String ficheroTareas) {
        JSONArray tareas = PersistenciaDatos.leeFichero(getApplication(), ficheroTareas);
        List<TareasMapaLista> tareasPunto = new ArrayList<>();
        JSONObject jo;
        String uriFondo;
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
                tareasPunto.add(new TareasMapaLista(
                        jo.getString(Auxiliar.id),
                        Auxiliar.quitaEnlaces(jo.getString(Auxiliar.recursoAsociadoTexto)).replace("<br>", ""),
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
                //Toast.makeText(context, getString(R.string.recuperandoPosicion), Toast.LENGTH_SHORT).show();
                if (myLocationNewOverlay != null) {
                    LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                    if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                            && !locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                        pintaSnackBar(getString(R.string.activaUbicacion));
                        //checkPermissions();
                    }
                } else
                    pintaSnackBar(getString(R.string.recuperandoPosicion));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * Método para generar la parte gráfica del marcador. Dentro de un elemento fijo se dibujará el
     * número de tareas.
     * @param size Número de tareas que representa el marcador en un interior
     * @return Representación gráfica del marcador
     */
    private Bitmap generaBitmapMarkerNumero(int size, boolean especial) {
        Paint paint = new Paint();
        Drawable drawable;
        if (size > 40)
            paint.setARGB(255, 255, 255, 255);
        else
            paint.setARGB(255, 0, 0, 0);
        if(especial){
            if (size < 0)
                drawable = ResourcesCompat.getDrawable(context.getResources(), R.drawable.ic_marcador_pulsado_especial, null);
            else if (size <= 10)
                drawable = ResourcesCompat.getDrawable(context.getResources(), R.drawable.ic_marcador100_especial, null);
            else if (size <= 20)
                drawable = ResourcesCompat.getDrawable(context.getResources(), R.drawable.ic_marcador300_especial, null);
            else if (size <= 40)
                drawable = ResourcesCompat.getDrawable(context.getResources(), R.drawable.ic_marcador500_especial, null);
            else if (size <= 70)
                drawable = ResourcesCompat.getDrawable(context.getResources(), R.drawable.ic_marcador700_especial, null);
            else
                drawable = ResourcesCompat.getDrawable(context.getResources(), R.drawable.ic_marcador900_especial, null);
        }else {
            if (size < 0)
                drawable = ResourcesCompat.getDrawable(context.getResources(), R.drawable.ic_marcador_pulsado, null);
            else if (size <= 10)
                drawable = ResourcesCompat.getDrawable(context.getResources(), R.drawable.ic_marcador100, null);
            else if (size <= 20)
                drawable = ResourcesCompat.getDrawable(context.getResources(), R.drawable.ic_marcador300, null);
            else if (size <= 40)
                drawable = ResourcesCompat.getDrawable(context.getResources(), R.drawable.ic_marcador500, null);
            else if (size <= 70)
                drawable = ResourcesCompat.getDrawable(context.getResources(), R.drawable.ic_marcador700, null);
            else
                drawable = ResourcesCompat.getDrawable(context.getResources(), R.drawable.ic_marcador900, null);
        }

        if (size<0)
            size*=-1;

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
        if (!noMolestar) {
            checkPermissions();
            locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
            if (map != null)
                map.onResume();
            if (!idZona.equals("") && locationManager != null) {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                        && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED)
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 10, this);
            }

            textToSpeech = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
                @Override
                public void onInit(int status) {
                    if(status < 0)
                        Toast.makeText(context, "Error", Toast.LENGTH_SHORT).show();
                    else{
                        textToSpeech.setLanguage(new Locale("spa", "ESP"));

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
        }
        invalidateOptionsMenu();
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
     * Se representan los marcadores existentes en las cuadrículas que se le muestran al usuario
     * @param ficherosPintar Nombre de los ficheros donde se encuentran las tareas a representar
     */
    private void pintaZona(List<String> ficherosPintar, double diagonal){
        //Distancia a la que se van a agrupar los marcadores
        //Evito los marcadores duplicados
        double nivelZum = Math.max(diagonal / 20000, 0.01);//10m;

        JSONArray todasTareas = new JSONArray();
        JSONArray puntosEspeciales = new JSONArray();

        JSONObject puntoInteres;
        JSONArray ficheroPuntosInteres;
        for(String nombreFichero : ficherosPintar){
            ficheroPuntosInteres = PersistenciaDatos.leeFichero(getApplication(), nombreFichero);
            try {
                if (ficheroPuntosInteres != null) {
                    for (int i = 0; i < ficheroPuntosInteres.length(); i++) {
                        puntoInteres = ficheroPuntosInteres.getJSONObject(i);
                        puntoInteres.put(Auxiliar.ficheroZona, nombreFichero);
                        if(puntoInteres.has(Auxiliar.creadoPor) && !puntoInteres.getString(Auxiliar.creadoPor).equals(Auxiliar.creadorInvestigadores)) {
                            puntosEspeciales.put(puntoInteres);
                        }else{
                            todasTareas.put(puntoInteres);
                        }
                    }
                }
            } catch (JSONException e){
                e.printStackTrace();
            }
        }

        List<Marcador> listaMarcadores = new ArrayList<>();
        Marcador marcador;
        double latitud, longitud;

        boolean anterior = false;
        try {
            while (todasTareas.length() > 0) {//Barro todas las tareas disponibles en el fichero
                puntoInteres = (JSONObject)todasTareas.remove(0);
                latitud = puntoInteres.getDouble(Auxiliar.latitud);
                longitud = puntoInteres.getDouble(Auxiliar.longitud);

                if (listaMarcadores.isEmpty()) {
                    marcador = new Marcador();
                    marcador.setTitulo(puntoInteres.getString(Auxiliar.label));
                    marcador.setPosicionMarcador(puntoInteres.getDouble(Auxiliar.latitud), puntoInteres.getDouble(Auxiliar.longitud));
                    marcador.agregaTareaAlMarcador(puntoInteres, puntoInteres.getInt(Auxiliar.nTareas));
                    listaMarcadores.add(marcador);
                } else {
                    for (int i = 0; i < listaMarcadores.size(); i++) {
                        anterior = false;
                        marcador = listaMarcadores.get(i);
                        if (latitud == marcador.getLatitud() &&
                                longitud == marcador.getLongitud()) { //La tarea es de la misma posición
                            marcador.agregaTareaAlMarcador(puntoInteres, puntoInteres.getInt(Auxiliar.nTareas));
                            listaMarcadores.set(i, marcador);
                            anterior = true;
                            break;
                        } else {//Se comprueba la distancia a la tarea del marcador
                            if (Auxiliar.calculaDistanciaDosPuntos(
                                    marcador.getLatitud(), marcador.getLongitud(),
                                    latitud, longitud)
                                    <= nivelZum) { //Se agrega al marcador ya que se debe agrupar
                                marcador.setTitulo(getString(R.string.agrupacionPuntos));
                                marcador.agregaTareaAlMarcador(puntoInteres, puntoInteres.getInt(Auxiliar.nTareas));

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
                        marcador.agregaTareaAlMarcador(puntoInteres, puntoInteres.getInt(Auxiliar.nTareas));
                        listaMarcadores.add(marcador);
                    }
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }

        if(!listaMarcadores.isEmpty()){
            for(Marcador m : listaMarcadores){
                newMarker(m, false);
            }
        }

        listaMarcadores = new ArrayList<>();
        try {
            while (puntosEspeciales.length() > 0) {
                puntoInteres = (JSONObject)puntosEspeciales.remove(0);
                marcador = new Marcador();
                marcador.setTitulo(puntoInteres.getString(Auxiliar.label));
                marcador.setPosicionMarcador(puntoInteres.getDouble(Auxiliar.latitud), puntoInteres.getDouble(Auxiliar.longitud));
                marcador.agregaTareaAlMarcador(puntoInteres, puntoInteres.getInt(Auxiliar.nTareas));
                listaMarcadores.add(marcador);
            }
        }catch (Exception e){
            e.printStackTrace();
        }

        if(!listaMarcadores.isEmpty()){
            for(Marcador m : listaMarcadores){
                newMarker(m, true);
            }
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
                ivSpeaker.setImageDrawable(ResourcesCompat.getDrawable(context.getResources(), R.drawable.ic_speaker, null));
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
                        ivSpeaker.setImageDrawable(ResourcesCompat.getDrawable(context.getResources(), R.drawable.ic_speaker, null));
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
            case R.id.btMasInfoPunto:
                ocultaReducido();
                break;
            default:
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
        bundle.putBoolean("DIALOGOSEGUNDOPLANO", dialogoSegundoPlanoVisible);
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
                /*if(!noMolestar)
                    lanzaServicioPosicionamiento();*/
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
        if(idUsuario == null) {//Si el usuario no se ha identificado se cambia la etiqueta a mostrar
            menuItem.setTitle(getString(R.string.iniciarSesion));
        }else{
            menuItem.setVisible(false);
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
            default:
                return super.onOptionsItemSelected(item);
        }
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
     * @return Punto más al norte y más al oeste
     */
    public GeoPoint establecePimeraCuadricula(){
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
        keys.add(Auxiliar.peticion); objects.add(Auxiliar.peticionPuntos);
        keys.add(Auxiliar.norte); objects.add(caja.getLatNorth());
        keys.add(Auxiliar.este); objects.add(caja.getLonEast());
        keys.add(Auxiliar.sur); objects.add(caja.getLatSouth());
        keys.add(Auxiliar.oeste); objects.add(caja.getLonWest());

        String url = Auxiliar.creaQuery(Auxiliar.rutaTareas, keys, objects);

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
                    if(puntosInteres != null){
                        JSONArray ficheroAntiguo = PersistenciaDatos.leeFichero(
                                getApplication(),
                                nombre);
                        //Si el fichero está vacío no va a tener tareas relacionadas
                        if(ficheroAntiguo == null || ficheroAntiguo.length() == 0) {
                            JSONObject puntoInteres;
                            for(int i = 0; i < puntosInteres.length(); i++) {
                                try {
                                    puntoInteres = puntosInteres.getJSONObject(i);
                                    puntoInteres.put(Auxiliar.id, String.format("%d%d", System.currentTimeMillis(), System.nanoTime()));
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
                            for(int i = 0; i < puntosInteres.length(); i++){
                                try {
                                    nuevoPunto = puntosInteres.getJSONObject(i);
                                    for(int j = 0; j < ficheroAntiguo.length(); j++) {
                                        puntoAntiguo = ficheroAntiguo.getJSONObject(j);
                                        //Si hay conincidencia guardo el instante y el nombre del fichero si lo tuviera
                                        if(nuevoPunto.getString(Auxiliar.contexto).equals(puntoAntiguo.getString(Auxiliar.contexto))){
                                            if(puntoAntiguo.has(Auxiliar.caducidad) && puntoAntiguo.has(Auxiliar.id)) {
                                                nuevoPunto.put(Auxiliar.caducidad, puntoAntiguo.getLong(Auxiliar.caducidad));
                                                nuevoPunto.put(Auxiliar.id, puntoAntiguo.getString(Auxiliar.id));
                                            }
                                            break;
                                        }
                                    }
                                    if(!nuevoPunto.has(Auxiliar.id))
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
                        /*cuadricula.put(Auxiliar.caducidad,
                                System.currentTimeMillis() + 604800000);*///Caduca a la semana
                        cuadricula.put(Auxiliar.caducidad,
                                System.currentTimeMillis() + 86400000);//Caduca al día
                        PersistenciaDatos.reemplazaJSON(
                                getApplication(),
                                PersistenciaDatos.ficheroNuevasCuadriculas,
                                cuadricula);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    synchronized ((Object)numeroCuadriculasPendientes) {
                        if(numeroCuadriculasPendientes > 0) {
                            --numeroCuadriculasPendientes;
                            if (numeroCuadriculasPendientes == 0)
                                compruebaZona(false);
                        }
                    }
                }
            },
            null
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
        keys.add(Auxiliar.peticion); objects.add(Auxiliar.peticionTareas);
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

        jsonArrayRequest.setRetryPolicy(new DefaultRetryPolicy(
                18000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        ColaConexiones.getInstance(getApplicationContext()).getRequestQueue().add(jsonArrayRequest);
    }

    /**
     * Método que se utiliza para agregar y representar un marcador al mapa. Al pulsar
     * sobre el marcador se muestra la lista de tareas que contiene
     * @param marcador Información que representa al marcador
     */
    void newMarker(final Marcador marcador, final Boolean especial) {
        Marker marker = new Marker(map);
        marker.setPosition(new GeoPoint(marcador.getLatitud(), marcador.getLongitud()));
        BitmapDrawable d = new BitmapDrawable(context.getResources(), generaBitmapMarkerNumero(marcador.getNumeroTareas(), especial));
        marker.setIcon(d);

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
                marker.setIcon(new BitmapDrawable(context.getResources(), generaBitmapMarkerNumero(marcador.getNumeroTareas()*-1 , especial)));
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
            rvPuntosInteres.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));

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
