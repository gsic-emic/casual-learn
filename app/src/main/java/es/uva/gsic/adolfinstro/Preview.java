package es.uva.gsic.adolfinstro;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.preference.PreferenceManager;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;
import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
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

import es.uva.gsic.adolfinstro.auxiliar.Auxiliar;
import es.uva.gsic.adolfinstro.persistencia.PersistenciaDatos;

/**
 * Método para mostrar toda la información relacionada con la tarea que el usuario podría llegar a
 * realizar.
 *
 * @author Pablo
 * @version 20201203
 */
public class Preview extends AppCompatActivity implements LocationListener {

    /**
     * Contexto
     */
    private Context context;
    /**
     * Vista donde se incluye la vista del mapa
     */
    private MapView map;
    /**
     * Receptor de notificaciones
     */
    private RecepcionNotificaciones recepcionNotificaciones;

    /**
     * Objeto que tiene toda la información de la tarea
     */
    private JSONObject tarea;

    /**
     * Objeto donde se coloca la explicación del por qué no puede realizar la tarea
     */
    private TextView explicacionDistancia;
    /**
     * Instancia donde se coloca la distancia a la tarea
     */
    private TextView textoDistancia;
    /**
     * Botones de la vista
     */
    private Button btRechazar, btPosponer, btAceptar;

    /**
     * Objeto con el que se hace el seguimiento de la posición del usuario
     */
    private LocationManager locationManager;

    /**
     * Instancia de la posición
     */
    private Location location;

    /**
     * URL de la licencia
     */
    private String urlLicencia;

    /**
     * Objeto para almacenar el identificador del usuario
     */
    private String idUsuario;

    /**
     * Objeto para saber si una tarea se ha completado
     */
    private boolean completada;

    private ImageView ivSpeaker;

    private TextToSpeech textToSpeech;

    private String textoParaSpeaker;

    /**
     * Se crea la vista de la interfaz de usuario.
     *
     * @param savedInstanceState Bundle
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        String urlImagen = null;

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        context = this; //contexto de la aplicación
        Configuration.getInstance().load(context, PreferenceManager.getDefaultSharedPreferences(context));
        Configuration.getInstance().setUserAgentValue(BuildConfig.APPLICATION_ID);
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);

        setContentView(R.layout.activity_preview);

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        ImageView imageView = findViewById(R.id.imagenPreview);
        String idTarea = Objects.requireNonNull(getIntent().getExtras()).getString(Auxiliar.id);
        try {
            idUsuario = PersistenciaDatos.recuperaTarea(
                    getApplication(),
                    PersistenciaDatos.ficheroUsuario,
                    Auxiliar.id
            ).getString(Auxiliar.uid);
        } catch (Exception e) {
            idUsuario = null;
        }
        try {
            tarea = PersistenciaDatos.recuperaTarea(
                    getApplication(),
                    PersistenciaDatos.ficheroNotificadas,
                    idTarea,
                    idUsuario);
            tarea.put(Auxiliar.fechaInicio, Auxiliar.horaFechaActual());
            PersistenciaDatos.reemplazaJSON(
                    getApplication(),
                    PersistenciaDatos.ficheroNotificadas,
                    tarea,
                    idUsuario);
        } catch (Exception e) {
            tarea = null;
        }

        //Compruebo si la tarea ya ha sido completada
        JSONObject tareaCompletada;
        try {
            tareaCompletada = PersistenciaDatos.recuperaTarea(
                    getApplication(),
                    PersistenciaDatos.ficheroCompletadas,
                    idTarea,
                    idUsuario);
        } catch (Exception e) {
            tareaCompletada = null;
        }

        completada = tareaCompletada != null;

        if (completada) {
            Button bt = findViewById(R.id.btIrACompletada);
            bt.setVisibility(View.VISIBLE);
        }

        if (tarea.has(Auxiliar.enlaceWiki)) {
            ImageView imageWikiPedia = findViewById(R.id.ivWikiPreview);
            imageWikiPedia.setVisibility(View.VISIBLE);
            try {
                final String enlaceWiki = tarea.getString(Auxiliar.enlaceWiki);
                imageWikiPedia.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Auxiliar.navegadorInterno(Preview.this, enlaceWiki);
                    }
                });
            } catch (Exception e) {
                imageWikiPedia.setVisibility(View.INVISIBLE);
            }
        } else {
            ImageView imageWikiPedia = findViewById(R.id.ivWikiPreview);
            imageWikiPedia.setVisibility(View.INVISIBLE);
        }

        if (tarea != null) {
            try {
                switch (tarea.getString(Auxiliar.tipoRespuesta)) {
                    case Auxiliar.tipoSinRespuesta:
                        setTitle(R.string.previewVisita);
                        break;
                    case Auxiliar.tipoPreguntaCorta:
                        setTitle(R.string.previewRespuestaCorta);
                        break;
                    case Auxiliar.tipoPreguntaLarga:
                        setTitle(R.string.previewRespuestaLarga);
                        break;
                    case Auxiliar.tipoPreguntaImagen:
                        setTitle(R.string.previewRespuestaImagen);
                        break;
                    case Auxiliar.tipoPreguntaImagenes:
                        setTitle(R.string.previewRespuestaImagenes);
                        break;
                    case Auxiliar.tipoImagen:
                        setTitle(R.string.previewFoto);
                        break;
                    case Auxiliar.tipoImagenMultiple:
                        setTitle(R.string.previewMultiplesFotos);
                        break;
                    case Auxiliar.tipoVideo:
                        setTitle(R.string.previewVideo);
                        break;
                    case Auxiliar.tipoTrueFalse:
                        setTitle(R.string.trueFalse);
                        break;
                    case Auxiliar.tipoMcq:
                        setTitle(R.string.eligeCorrecta);
                        break;
                    default:
                        break;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            try {
                try {

                    assert tarea != null;
                    if (tarea.has(Auxiliar.recursoImagenBaja) &&
                            !tarea.getString(Auxiliar.recursoImagenBaja).equals("") &&
                            !tarea.getString(Auxiliar.recursoImagenBaja).equals("?width=300")) {
                        urlImagen = tarea.getString(Auxiliar.recursoImagenBaja);
                    } else {
                        if (tarea.has(Auxiliar.recursoImagen) && !tarea.getString(Auxiliar.recursoImagen).equals("")) {
                            urlImagen = tarea.getString(Auxiliar.recursoImagen);
                        }
                    }
                    if (urlImagen != null) {
                        Picasso.get()
                                .load(urlImagen)
                                .placeholder(R.drawable.ic_cloud_download_blue_80dp)
                                .tag(Auxiliar.cargaImagenPreview)
                                .into(imageView);
                        imageView.setVisibility(View.VISIBLE);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                TextView licenciaPreview = findViewById(R.id.tvLicenciaPreview);
                if (imageView.getVisibility() == View.VISIBLE) {
                    if (tarea.has(Auxiliar.textoLicencia)) {
                        licenciaPreview.setText(tarea.getString(Auxiliar.textoLicencia));
                    }
                    urlLicencia = Auxiliar.enlaceLicencia(context, licenciaPreview, urlImagen);
                }

                btAceptar = findViewById(R.id.botonAceptarPreview);
                btPosponer = findViewById(R.id.botonAhoraNoPreview);
                btRechazar = findViewById(R.id.botonRechazarPreview);
                explicacionDistancia = findViewById(R.id.tvExplicacionDistancia);
                textoDistancia = findViewById(R.id.tvDistancia);
                map = findViewById(R.id.mapPreview);
                map.setTileSource(TileSourceFactory.MAPNIK);
                IMapController mapController = map.getController();

                double latitud = tarea.getDouble(Auxiliar.latitud);
                double longitud = tarea.getDouble(Auxiliar.longitud);
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

                TextView texto = findViewById(R.id.tituloPreview);
                texto.setText(tarea.getString(Auxiliar.titulo));

                texto = findViewById(R.id.textoPreview);
                texto.setText(Auxiliar.creaEnlaces(this, tarea.getString(Auxiliar.recursoAsociadoTexto), false));
                texto.setMovementMethod(LinkMovementMethod.getInstance());
                textoParaSpeaker = String.format(
                        "%s\n%s",
                        tarea.getString(Auxiliar.titulo),
                        Auxiliar.quitaEnlaces(tarea.getString(Auxiliar.recursoAsociadoTexto)));

                Marker marker = new Marker(map);
                marker.setPosition(new GeoPoint(tarea.getDouble(Auxiliar.latitud), tarea.getDouble(Auxiliar.longitud)));
                marker.setIcon(ResourcesCompat.getDrawable(this.getResources(), R.drawable.ic_marcador_uno, null));
                //marker.setTitle(extras.getString(Auxiliar.titulo));
                marker.setInfoWindow(null);

                /*map.getOverlays().add(new MapEventsOverlay(new MapEventsReceiver() {
                    @SuppressLint("MissingPermission")
                    @Override
                    public boolean singleTapConfirmedHelper(GeoPoint p) {
                        //saltaNavegacion();
                        return false;
                    }

                    @Override
                    public boolean longPressHelper(GeoPoint p) {
                        //saltaNavegacion();
                        return false;
                    }
                }));*/

                map.getOverlays().add(marker);

                botonesVisibles(getIntent().getExtras().getString(Auxiliar.previa).equals(Auxiliar.notificacion));

                //Identifiación usuario. Si existe el fichero con el identificador no muestro la barra
                if (idUsuario == null) {
                    snackBarLogin(R.id.clIdentificatePreview);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {//Por si le salta una notificación, sale de la sesión y pulsa en la notficación
            Intent intent = new Intent(this, Login.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finishAffinity();
        }

        ivSpeaker = findViewById(R.id.ivSpeakerPreview);
        ivSpeaker.setImageDrawable(ResourcesCompat.getDrawable(context.getResources(), R.drawable.ic_speaker, null));
    }

    private void saltaNavegacion() {
        try {//Se salta a la tarea de navegación cuando el usuario pulse sobre el mapa
            if (location != null) {
                Intent intent = new Intent(context, MapaNavegable.class);
                intent.putExtra(Auxiliar.latitud + "user", location.getLatitude());
                intent.putExtra(Auxiliar.longitud + "user", location.getLongitude());
                intent.putExtra(Auxiliar.latitud + "task", tarea.getDouble(Auxiliar.latitud));
                intent.putExtra(Auxiliar.longitud + "task", tarea.getDouble(Auxiliar.longitud));
                startActivity(intent);
            } else {
                try {
                    Intent intent = new Intent(context, MapaNavegable.class);
                    intent.putExtra(Auxiliar.latitud + "user", getIntent().getExtras().getDouble(Auxiliar.posUsuarioLat));
                    intent.putExtra(Auxiliar.longitud + "user", getIntent().getExtras().getDouble(Auxiliar.posUsuarioLon));
                    intent.putExtra(Auxiliar.latitud + "task", tarea.getDouble(Auxiliar.latitud));
                    intent.putExtra(Auxiliar.longitud + "task", tarea.getDouble(Auxiliar.longitud));
                    startActivity(intent);
                } catch (Exception e) {
                    pintaSnackBar(context.getString(R.string.recuperandoPosicion));
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void snackBarLogin(int snack) {
        Snackbar snackbar = Snackbar.make(findViewById(snack), R.string.textoInicioBreve, Snackbar.LENGTH_INDEFINITE);
        snackbar.setTextColor(ResourcesCompat.getColor(context.getResources(), R.color.colorSecondaryText, null));
        snackbar.setAction(R.string.autenticarse, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Login.gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestIdToken(getString(R.string.default_web_client_id))
                        .requestEmail().build();
                Login.googleSignInClient = GoogleSignIn.getClient(context, Login.gso);
                Intent intent = Login.googleSignInClient.getSignInIntent();
                startActivityForResult(intent, Login.requestAuth + 2);
            }
        });
        snackbar.setActionTextColor(ResourcesCompat.getColor(context.getResources(), R.color.colorSecondary50, null));
        snackbar.getView().setBackground(ResourcesCompat.getDrawable(context.getResources(), R.drawable.snack, null));
        snackbar.show();
    }

    @Override
    public void onActivityResult(int requestCode, int result, Intent data) {
        super.onActivityResult(requestCode, result, data);
        switch (requestCode) {
            case (Login.requestAuth + 2):
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
                break;
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
            idUsuario = firebaseUser.getUid();
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
                    usuario.put(Auxiliar.uid, idUsuario);
                    PersistenciaDatos.reemplazaJSON(getApplication(), PersistenciaDatos.ficheroUsuario, usuario);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            try {
                tarea.put(Auxiliar.fechaInicio, Auxiliar.horaFechaActual());
                tarea.put(Auxiliar.idUsuario, idUsuario);
                PersistenciaDatos.reemplazaJSON(
                        getApplication(),
                        PersistenciaDatos.ficheroNotificadas,
                        tarea,
                        null);
            } catch (Exception e) {
                tarea = null;
            }


            //Compruebo si la tarea ya ha sido completada
            JSONObject tareaCompletada;
            try {
                tareaCompletada = PersistenciaDatos.recuperaTarea(
                        getApplication(),
                        PersistenciaDatos.ficheroCompletadas,
                        tarea.getString(Auxiliar.id),
                        idUsuario);
            } catch (Exception e) {
                tareaCompletada = null;
            }

            completada = tareaCompletada != null;

            if (completada) {
                botonesVisibles(false);
            }

            pintaSnackBar(String.format("%s%s", getString(R.string.hola), firebaseUser.getDisplayName()));
            permisos = new ArrayList<>();
            //String textoPermisos = getString(R.string.necesidad_permisos);
            if (!(ActivityCompat.checkSelfPermission(
                    context, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED)) {
                permisos.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            }
            //Compruebo permisos de localización en primer y segundo plano
            if (!(ActivityCompat.checkSelfPermission(
                    context, Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED)) {
                permisos.add(Manifest.permission.ACCESS_FINE_LOCATION);
            }
            //Comprobación para saber si el usuario se ha identificado
            if (idUsuario != null) {
                if (Build.VERSION.SDK_INT == Build.VERSION_CODES.Q) {
                    if (!(ActivityCompat.checkSelfPermission(
                            context, Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                            == PackageManager.PERMISSION_GRANTED)) {
                        permisos.add(Manifest.permission.ACCESS_BACKGROUND_LOCATION);
                        //textoPermisos = String.format("%s%s", textoPermisos, getString(R.string.ubicacion_segundo));
                    }
                } else {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && !permisos.contains(Manifest.permission.ACCESS_FINE_LOCATION)) {
                        if (!(ActivityCompat.checkSelfPermission(
                                context, Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                                == PackageManager.PERMISSION_GRANTED)) {
                            permisos.add(Manifest.permission.ACCESS_BACKGROUND_LOCATION);
                        }
                    }
                }
            }

            if (permisos.contains(Manifest.permission.ACCESS_BACKGROUND_LOCATION)) {
                permisos.remove(Manifest.permission.ACCESS_FINE_LOCATION);
            }

            if (permisos.isEmpty()) {
                if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean(Ajustes.NO_MOLESTAR_pref, false))
                    new AlarmaProceso().activaAlarmaProceso(getApplicationContext());
            } else {
                solicitaPermisoUbicacion();
            }
        }
    }

    /**
     * Método que habilita o deshabilita los botones dependiendo del argumento de entrada
     *
     * @param visibles Si es true se muestran los botones para interactuar frente a la tarea. False
     *                 paramostrar la información de la distancia que falta a la tarea
     */
    private void botonesVisibles(boolean visibles) {
        if (!completada) {
            if (visibles) {
                btRechazar.setVisibility(View.VISIBLE);
                btPosponer.setVisibility(View.VISIBLE);
                btAceptar.setVisibility(View.VISIBLE);
                explicacionDistancia.setVisibility(View.GONE);
                textoDistancia.setVisibility(View.GONE);
            } else {
                btRechazar.setVisibility(View.GONE);
                btPosponer.setVisibility(View.GONE);
                btAceptar.setVisibility(View.GONE);
                explicacionDistancia.setVisibility(View.VISIBLE);
                textoDistancia.setVisibility(View.VISIBLE);
            }
        } else {
            btRechazar.setVisibility(View.GONE);
            btPosponer.setVisibility(View.GONE);
            btAceptar.setVisibility(View.GONE);
            explicacionDistancia.setVisibility(View.GONE);
            textoDistancia.setVisibility(View.GONE);
        }
    }

    /**
     * Al pulsar el botón de la barra título se realiza la misma acción que al pulsar atrás
     *
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
    public void onBackPressed() {
        Picasso.get().cancelTag(Auxiliar.cargaImagenPreview);
        try {
            switch (Objects.requireNonNull(Objects.requireNonNull(getIntent()
                    .getExtras()).getString(Auxiliar.previa))) {
                case Auxiliar.notificacion:
                    if (idUsuario != null) {
                        Intent intent = new Intent();
                        intent.setAction(Auxiliar.ahora_no);
                        intent.putExtra(Auxiliar.id, Objects.requireNonNull(getIntent()
                                .getExtras()).getString(Auxiliar.id));
                        sendBroadcast(intent);
                    }
                    //Toast.makeText(context, getString(R.string.tareaPospuesta), Toast.LENGTH_SHORT).show();
                    Intent intent2 = new Intent(context, Maps.class);
                    intent2.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    intent2.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent2.putExtra(Auxiliar.textoParaElMapa, getString(R.string.tareaPospuesta));
                    context.startActivity(intent2);
                    finish();
                    break;
                case Auxiliar.mapa:
                    PersistenciaDatos.obtenTarea(
                            getApplication(),
                            PersistenciaDatos.ficheroNotificadas,
                            tarea.getString(Auxiliar.id),
                            idUsuario);
                    finish();
                    break;
                case Auxiliar.tareasPospuestas:
                    PersistenciaDatos.guardaJSON(
                            getApplication(),
                            PersistenciaDatos.ficheroTareasPospuestas,
                            PersistenciaDatos.obtenTarea(
                                    getApplication(),
                                    PersistenciaDatos.ficheroNotificadas,
                                    tarea.getString(Auxiliar.id),
                                    idUsuario),
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
                                    tarea.getString(Auxiliar.id),
                                    idUsuario),
                            Context.MODE_PRIVATE);
                    finish();
                    break;
                default:
                    break;
            }
        } catch (Exception e) {
            pintaSnackBar(getString(R.string.errorOpera));
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
            //Para mostrar la información de donde se han obtenido los datos
            int idBoton = view.getId();
            switch (idBoton) {
                case R.id.ivSpeakerPreview:
                    if (textToSpeech != null) {
                        if (textToSpeech.isSpeaking()) {
                            textToSpeech.stop();
                            ivSpeaker.setImageDrawable(ResourcesCompat.getDrawable(context.getResources(), R.drawable.ic_speaker, null));
                        } else {
                            if (textoParaSpeaker != null) {
                                HashMap<String, String> map = new HashMap<>();
                                map.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "speakerPreview");
                                textToSpeech.speak(
                                        textoParaSpeaker,
                                        TextToSpeech.QUEUE_FLUSH,
                                        map);
                            }
                        }
                    }
                    break;
                case R.id.tvLicenciaPreview:
                    if (urlLicencia != null)
                        Auxiliar.navegadorInterno(this, urlLicencia);
                    break;
                case R.id.btIrACompletada:
                    intent = new Intent(context, Completadas.class);
                    intent.putExtra(
                            Auxiliar.id,
                            Objects.requireNonNull(getIntent().getExtras()).getString(Auxiliar.id));
                    startActivity(intent);
                    break;
                case R.id.btAmpliarMapa:
                    saltaNavegacion();
                    break;
                default:
                    if (idUsuario == null) {
                        snackBarLogin(R.id.clPreview);
                    } else {
                        switch (idBoton) {
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
                                finish();
                                break;
                            case R.id.botonRechazarPreview:
                                intent = new Intent();
                                intent.setAction(Auxiliar.nunca_mas);
                                intent.putExtra(
                                        Auxiliar.id,
                                        Objects.requireNonNull(getIntent().getExtras()).getString(Auxiliar.id));
                                sendBroadcast(intent);
                                finish();
                                break;
                            default:
                                break;
                        }
                    }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu_preview, menu);
        MenuItem menuItem = menu.findItem(R.id.iPreview);
        try {
            menuItem.setIcon(Auxiliar.iconoTipoTareaLista(tarea.getString(Auxiliar.tipoRespuesta)));
        } catch (Exception e) {
            menuItem.setIcon(R.drawable.ic_marcador_uno);
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NotNull MenuItem item) {
        if (item.getItemId() == R.id.iPreview) {
            try {
                Toast.makeText(
                        context,
                        Auxiliar.textoTipoTarea(
                                this,
                                tarea.getString(Auxiliar.tipoRespuesta)),
                        Toast.LENGTH_SHORT).show();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Se sobrescribe le método onResume para activar la recepción de notificaciones y restaurar el mapa.
     * Se activa el seguimiento del usuario para mostrar la distancia a la tarea según se desplace
     */
    @Override
    protected void onResume() {
        super.onResume();
        recepcionNotificaciones = new RecepcionNotificaciones();
        registerReceiver(recepcionNotificaciones, Auxiliar.intentFilter());
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        permisos = new ArrayList<>();
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        try {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                botonesVisibles(false);
                permisos.add(Manifest.permission.ACCESS_FINE_LOCATION);
            }

            if (idUsuario != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
                    if (!sharedPreferences.getBoolean(Ajustes.NO_MOLESTAR_pref, false) &&
                            !(ActivityCompat.checkSelfPermission(
                                    context, Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                                    == PackageManager.PERMISSION_GRANTED)) {
                        permisos.add(Manifest.permission.ACCESS_BACKGROUND_LOCATION);
                    }
            } else {
                new AlarmaProceso().activaAlarmaProceso(getApplicationContext());
            }

            if (permisos.contains(Manifest.permission.ACCESS_BACKGROUND_LOCATION)) {
                permisos.remove(Manifest.permission.ACCESS_FINE_LOCATION);
            }

            if (permisos != null && !permisos.isEmpty()) {
                solicitaPermisoUbicacion();
            } else {
                locationManager.requestLocationUpdates(
                        LocationManager.GPS_PROVIDER, 1000, 2, this);
                if (locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER) != null)
                    onLocationChanged(
                            locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER));
                else {
                    try {
                        textoInformativoDistancia(tarea.getDouble(Auxiliar.latitud),
                                tarea.getDouble(Auxiliar.longitud),
                                getIntent().getExtras().getDouble(Auxiliar.posUsuarioLat),
                                getIntent().getExtras().getDouble(Auxiliar.posUsuarioLon));
                    } catch (Exception e) {
                        explicacionDistancia.setText(getResources().getString(R.string.recuperandoPosicion));
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (map != null)
            map.onResume();

        textToSpeech = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status >= 0) {
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
        if (textToSpeech.getEngines() == null)
            ivSpeaker.setVisibility(View.INVISIBLE);
        else if (textToSpeech.getEngines().size() <= 0)
            ivSpeaker.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (textToSpeech != null)
            textToSpeech.shutdown();
    }

    List<String> permisos;

    private void solicitaPermisoUbicacion() {
        final Dialog dialogoPermisos = new Dialog(context);
        dialogoPermisos.setContentView(R.layout.dialogo_permisos_ubicacion);
        dialogoPermisos.setCancelable(false);

        String textoPermisos = context.getString(R.string.necesidad_permisos);

        for (String s : permisos) {
            switch (s) {
                case Manifest.permission.ACCESS_FINE_LOCATION:
                    textoPermisos = String.format("%s%s", textoPermisos, context.getString(R.string.ubicacion_primer));
                    break;
                case Manifest.permission.WRITE_EXTERNAL_STORAGE:
                    textoPermisos = String.format("%s%s", textoPermisos, context.getString(R.string.permiso_almacenamiento));
                    break;
                default:
                    break;
            }
        }
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        if (permisos.size() > 1 && !sharedPreferences.getBoolean(Ajustes.NO_MOLESTAR_pref, false) && permisos.contains(Manifest.permission.ACCESS_BACKGROUND_LOCATION)) {//Se necesitan mostrar dos dialogos
            final TextView tituloPermisos = (TextView) dialogoPermisos.findViewById(R.id.tvTituloPermisos);
            tituloPermisos.setVisibility(View.GONE);
            final TextView textoPermiso = (TextView) dialogoPermisos.findViewById(R.id.tvTextoPermisos);
            textoPermiso.setText(Html.fromHtml(textoPermisos));
            Button salir = (Button) dialogoPermisos.findViewById(R.id.btSalirPermisos);
            salir.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //finishAffinity();
                    SharedPreferences.Editor prefs = PreferenceManager.getDefaultSharedPreferences(context).edit();
                    prefs.putBoolean(Ajustes.NO_MOLESTAR_pref, true);
                    prefs.commit();
                    boolean encontrado = false;
                    int i, tama = permisos.size();
                    for (i = 0; i < tama; i++) {
                        if (permisos.get(i).equals(Manifest.permission.ACCESS_BACKGROUND_LOCATION)) {
                            encontrado = true;
                            break;
                        }
                    }
                    if (encontrado)
                        permisos.remove(i);
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
                            if (dialogoPermisos.isShowing())
                                dialogoPermisos.cancel();
                            permisos.add(Manifest.permission.ACCESS_FINE_LOCATION);
                            ActivityCompat.requestPermissions(
                                    Preview.this,
                                    permisos.toArray(new String[permisos.size()]),
                                    1002);
                        }
                    });
                }
            });
        } else {
            TextView textView = (TextView) dialogoPermisos.findViewById(R.id.tvTituloPermisos);
            if (permisos.contains(Manifest.permission.ACCESS_BACKGROUND_LOCATION)) {//Solo muestro el de ubicación siempre
                textView.setVisibility(View.VISIBLE);
                Button salir = (Button) dialogoPermisos.findViewById(R.id.btSalirPermisos);
                salir.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //finishAffinity();
                        SharedPreferences.Editor prefs = PreferenceManager.getDefaultSharedPreferences(context).edit();
                        prefs.putBoolean(Ajustes.NO_MOLESTAR_pref, true);
                        prefs.commit();
                        boolean encontrado = false;
                        int i, tama = permisos.size();
                        for (i = 0; i < tama; i++) {
                            if (permisos.get(i).equals(Manifest.permission.ACCESS_BACKGROUND_LOCATION)) {
                                encontrado = true;
                                break;
                            }
                        }
                        if (encontrado)
                            permisos.remove(i);
                        if (permisos.isEmpty() && dialogoPermisos.isShowing()) {
                            dialogoPermisos.cancel();
                        }
                    }
                });
                Button siguiente = (Button) dialogoPermisos.findViewById(R.id.btSiguientePermisos);
                siguiente.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (dialogoPermisos.isShowing())
                            dialogoPermisos.cancel();
                        permisos.add(Manifest.permission.ACCESS_FINE_LOCATION);
                        ActivityCompat.requestPermissions(
                                Preview.this,
                                permisos.toArray(new String[permisos.size()]),
                                1002);
                    }
                });
            } else {//Solo muestro el normal
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
                        if (dialogoPermisos.isShowing())
                            dialogoPermisos.cancel();
                        ActivityCompat.requestPermissions(
                                Preview.this,
                                permisos.toArray(new String[permisos.size()]),
                                1002);
                    }
                });
            }
        }
        dialogoPermisos.show();
    }

    /**
     * Se sobrescribe el método onPause para desactivar la recepción de notificaciones y pausar el mapa.
     * Se detiene el seguimiento al usuario
     */
    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(recepcionNotificaciones);
        if (map != null)
            map.onPause();
        if (locationManager != null)
            locationManager.removeUpdates(this);
        if (textToSpeech != null) {
            textToSpeech.stop();
        }
        if (ivSpeaker != null)
            ivSpeaker.setImageDrawable(ResourcesCompat.getDrawable(context.getResources(), R.drawable.ic_speaker, null));
    }

    /**
     * Método para modificar la distancia que existe, en línea recta, entre la tarea y el usuario
     *
     * @param tareaLat   Latitud de la tarea
     * @param tareaLon   Longitud de la tarea
     * @param usuarioLat Latitud del usuario
     * @param usuarioLon Longitud del usuario
     */
    private void textoInformativoDistancia(double tareaLat, double tareaLon, double usuarioLat, double usuarioLon) {
        double distancia = Auxiliar.calculaDistanciaDosPuntos(
                tareaLat,
                tareaLon,
                usuarioLat,
                usuarioLon);
        //TODO cambiar a 0.15
        if (distancia <= 0.15) {
            botonesVisibles(true);
        } else {
            botonesVisibles(false);
            if (distancia >= 1)
                textoDistancia.setText(String.format("%.2fkm", distancia));
            else
                textoDistancia.setText(String.format("%.2fm", distancia * 1000));
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        this.location = location;
        try {
            textoInformativoDistancia(tarea.getDouble(Auxiliar.latitud),
                    tarea.getDouble(Auxiliar.longitud),
                    location.getLatitude(),
                    location.getLongitude());
        } catch (Exception e) {
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

    private void pintaSnackBar(String texto) {
        Snackbar snackbar = Snackbar.make(findViewById(R.id.clPreview), R.string.app_name, Snackbar.LENGTH_SHORT);
        snackbar.setTextColor(ResourcesCompat.getColor(context.getResources(), R.color.colorSecondaryText, null));
        snackbar.getView().setBackground(ResourcesCompat.getDrawable(context.getResources(), R.drawable.snack, null));
        snackbar.setText(texto);
        snackbar.show();
    }
}
