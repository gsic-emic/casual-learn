package es.uva.gsic.adolfinstro;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.preference.PreferenceManager;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
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
import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.CustomZoomButtonsController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.MapEventsOverlay;
import org.osmdroid.views.overlay.Marker;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import es.uva.gsic.adolfinstro.auxiliar.Auxiliar;
import es.uva.gsic.adolfinstro.persistencia.PersistenciaDatos;

/**
 * Método para mostrar toda la información relacionada con la tarea que el usuario podría llegar a
 * realizar.
 *
 * @author Pablo
 * @version 20201005
 */
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

    /** Instancia de la posición */
    private Location location;

    /** URL de la licencia */
    private String urlLicencia;

    /** Objeto para almacenar el identificador del usuario */
    private String idUsuario;

    /** Objeto para saber si una tarea se ha completado */
    private boolean completada;

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
        context = getApplicationContext(); //contexto de la aplicación
        Configuration.getInstance().load(context, PreferenceManager.getDefaultSharedPreferences(context));
        Configuration.getInstance().setUserAgentValue(BuildConfig.APPLICATION_ID);
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_preview);

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        ImageView imageView = findViewById(R.id.imagenPreview);
        String idTarea = Objects.requireNonNull(getIntent().getExtras()).getString(Auxiliar.id);
        try{
            idUsuario = PersistenciaDatos.recuperaTarea(
                    getApplication(),
                    PersistenciaDatos.ficheroUsuario,
                    Auxiliar.id
            ).getString(Auxiliar.uid);
        }catch (Exception e){
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
        }catch (Exception e){
            tarea = null;
        }

        //Compruebo si la tarea ya ha sido completada
        JSONObject tareaCompletada;
        try{
            tareaCompletada = PersistenciaDatos.recuperaTarea(
                    getApplication(),
                    PersistenciaDatos.ficheroCompletadas,
                    idTarea,
                    idUsuario);
        } catch (Exception e){
            tareaCompletada = null;
        }

        completada = tareaCompletada != null;

        if(completada){
            ImageView icono = findViewById(R.id.ivCompletadaPrevie);
            icono.setImageDrawable(getResources().getDrawable(R.drawable.ic_check_completada));
        }

        if(tarea != null) {
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
                        Picasso.get()
                                .load(urlImagen)
                                .placeholder(R.drawable.ic_cloud_download_blue_80dp)
                                .tag(Auxiliar.cargaImagenPreview)
                                .into(imageView);
                        imageView.setVisibility(View.VISIBLE);

                    } else {
                        if (tarea.has(Auxiliar.recursoImagen) && !tarea.getString(Auxiliar.recursoImagen).equals("")) {
                            urlImagen = tarea.getString(Auxiliar.recursoImagenBaja);
                            Picasso.get()
                                    .load(urlImagen)
                                    .placeholder(R.drawable.ic_cloud_download_blue_80dp)
                                    .tag(Auxiliar.cargaImagenPreview)
                                    .into(imageView);
                            imageView.setVisibility(View.VISIBLE);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                if (imageView.getVisibility() == View.VISIBLE) {
                    urlLicencia = Auxiliar.enlaceLicencia(context, (ImageView) findViewById(R.id.ivLicenciaPreview), urlImagen);
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

                map.setMultiTouchControls(false);

                map.setTilesScaledToDpi(true);

                map.setClickable(false);
                map.setEnabled(false);


                TextView titulo = findViewById(R.id.tituloPreview);
                titulo.setText(tarea.getString(Auxiliar.titulo));

                TextView descripcion = findViewById(R.id.textoPreview);
                descripcion.setText(Auxiliar.creaEnlaces(this, tarea.getString(Auxiliar.recursoAsociadoTexto), false));
                descripcion.setMovementMethod(LinkMovementMethod.getInstance());

                Marker marker = new Marker(map);
                marker.setPosition(new GeoPoint(tarea.getDouble(Auxiliar.latitud), tarea.getDouble(Auxiliar.longitud)));
                marker.setIcon(getResources().getDrawable(R.drawable.ic_11_tareas));
                //marker.setTitle(extras.getString(Auxiliar.titulo));
                marker.setInfoWindow(null);

                map.getOverlays().add(new MapEventsOverlay(new MapEventsReceiver() {
                    @SuppressLint("MissingPermission")
                    @Override
                    public boolean singleTapConfirmedHelper(GeoPoint p) {
                        saltaNavegacion();
                        return false;
                    }

                    @Override
                    public boolean longPressHelper(GeoPoint p) {
                        saltaNavegacion();
                        return false;
                    }
                }));
                map.getOverlays().add(marker);

                if (!getIntent().getExtras().getString(Auxiliar.previa).equals(Auxiliar.notificacion)) {
                    botonesVisibles(false);
                } else {
                    botonesVisibles(true);
                }

                //Identifiación usuario. Si existe el fichero con el identificador no muestro la barra
                if (idUsuario == null) {
                    snackBarLogin(R.id.clIdentificatePreview);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }else{//Por si le salta una notificación, sale de la sesión y pulsa en la notficación
            Intent intent = new Intent(this, Login.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finishAffinity();
        }
    }

    private void saltaNavegacion(){
        try {//Se salta a la tarea de navegación cuando el usuario pulse sobre el mapa
            if(location != null){
                Intent intent = new Intent(context, MapaNavegable.class);
                intent.putExtra(Auxiliar.latitud + "user", location.getLatitude());
                intent.putExtra(Auxiliar.longitud + "user", location.getLongitude());
                intent.putExtra(Auxiliar.latitud + "task", tarea.getDouble(Auxiliar.latitud));
                intent.putExtra(Auxiliar.longitud + "task", tarea.getDouble(Auxiliar.longitud));
                startActivity(intent);
            }else{
                try{
                    Intent intent = new Intent(context, MapaNavegable.class);
                    intent.putExtra(Auxiliar.latitud + "user", getIntent().getExtras().getDouble(Auxiliar.posUsuarioLat));
                    intent.putExtra(Auxiliar.longitud + "user", getIntent().getExtras().getDouble(Auxiliar.posUsuarioLon));
                    intent.putExtra(Auxiliar.latitud + "task", tarea.getDouble(Auxiliar.latitud));
                    intent.putExtra(Auxiliar.longitud + "task", tarea.getDouble(Auxiliar.longitud));
                    startActivity(intent);
                }catch (Exception e){
                    pintaSnackBar(context.getString(R.string.recuperandoPosicion));
                }
            }
        }catch (JSONException e){
            e.printStackTrace();
        }
    }

    private void snackBarLogin(int snack){
        Snackbar snackbar = Snackbar.make(findViewById(snack), R.string.textoInicioBreve, Snackbar.LENGTH_INDEFINITE);
        snackbar.setTextColor(getResources().getColor(R.color.colorSecondaryText));
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
        snackbar.setActionTextColor(getResources().getColor(R.color.colorSecondary50));
        snackbar.getView().setBackground(getResources().getDrawable(R.drawable.snack));
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
            idUsuario = firebaseUser.getUid();
            Login.firebaseAnalytics.setUserId(idUsuario);
            Bundle bundle = new Bundle();
            bundle.putString(Auxiliar.uid, idUsuario);
            if(registro)
                Login.firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SIGN_UP, bundle);
            else
                Login.firebaseAnalytics.logEvent(FirebaseAnalytics.Event.LOGIN, bundle);
            try {
                JSONObject usuario = new JSONObject();
                usuario.put(Auxiliar.id, Auxiliar.id);
                usuario.put(Auxiliar.uid, idUsuario);
                PersistenciaDatos.reemplazaJSON(getApplication(), PersistenciaDatos.ficheroUsuario, usuario);
            }catch (JSONException e){
                e.printStackTrace();
            }
            try{
                tarea.put(Auxiliar.fechaInicio, Auxiliar.horaFechaActual());
                tarea.put(Auxiliar.idUsuario, idUsuario);
                PersistenciaDatos.reemplazaJSON(
                        getApplication(),
                        PersistenciaDatos.ficheroNotificadas,
                        tarea,
                        null);
            }catch (Exception e){
                tarea = null;
            }


            //Compruebo si la tarea ya ha sido completada
            JSONObject tareaCompletada;
            try{
                tareaCompletada = PersistenciaDatos.recuperaTarea(
                        getApplication(),
                        PersistenciaDatos.ficheroCompletadas,
                        tarea.getString(Auxiliar.id),
                        idUsuario);
            } catch (Exception e){
                tareaCompletada = null;
            }

            completada = tareaCompletada != null;

            if(completada){
                ImageView icono = findViewById(R.id.ivCompletadaPrevie);
                icono.setImageDrawable(getResources().getDrawable(R.drawable.ic_check_completada));
                botonesVisibles(false);
            }


            pintaSnackBar(String.format("%s%s", getString(R.string.hola), firebaseUser.getDisplayName()));
            permisos = new ArrayList<>();
            String textoPermisos = getString(R.string.necesidad_permisos);
            //Compruebo permisos de localización en primer y segundo plano
            if(!(ActivityCompat.checkSelfPermission(
                    context, Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED)) {
                permisos.add(Manifest.permission.ACCESS_FINE_LOCATION);
                textoPermisos = String.format("%s%s", textoPermisos, getString(R.string.ubicacion_primer));
            }
            //Comprobación para saber si el usuario se ha identificado
            if(idUsuario != null) {
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
                    if(!(ActivityCompat.checkSelfPermission(
                            context, Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                            == PackageManager.PERMISSION_GRANTED)) {
                        permisos.add(Manifest.permission.ACCESS_BACKGROUND_LOCATION);
                        textoPermisos = String.format("%s%s", textoPermisos, getString(R.string.ubicacion_segundo));
                    }
            }
            if(permisos.isEmpty())
                new AlarmaProceso().activaAlarmaProceso(getApplicationContext());
            else
                solicitaPermisoUbicacion(textoPermisos);
        }
    }

    /**
     * Método que habilita o deshabilita los botones dependiendo del argumento de entrada
     * @param visibles Si es true se muestran los botones para interactuar frente a la tarea. False
     *                 paramostrar la información de la distancia que falta a la tarea
     */
    private void botonesVisibles(boolean visibles){
        if(!completada) {
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
        }else{
            btRechazar.setVisibility(View.GONE);
            btPosponer.setVisibility(View.GONE);
            btAceptar.setVisibility(View.GONE);
            explicacionDistancia.setVisibility(View.GONE);
            textoDistancia.setVisibility(View.GONE);
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
                    if(idUsuario != null) {
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
        }catch (Exception e){
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
            switch (idBoton){
                case R.id.ivWikipediaPreview:
                    Auxiliar.navegadorInterno(this, getString(R.string.enlaceWiki)+(tarea.getString(Auxiliar.titulo).replace(' ', '_')));
                    break;
                case R.id.ivLicenciaPreview:
                    if(urlLicencia != null)
                        Auxiliar.navegadorInterno(this, urlLicencia);
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
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu_preview, menu);
        MenuItem menuItem = menu.findItem(R.id.iPreview);
        try {
            menuItem.setIcon(Auxiliar.iconoTipoTareaLista(tarea.getString(Auxiliar.tipoRespuesta)));
        }catch (Exception e){
            menuItem.setIcon(R.drawable.ic_11_tareas);
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NotNull MenuItem item){
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
    protected void onResume(){
        super.onResume();
        recepcionNotificaciones = new RecepcionNotificaciones();
        registerReceiver(recepcionNotificaciones, Auxiliar.intentFilter());
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        permisos = new ArrayList<>();
        try {
            String textoPermisos = getString(R.string.necesidad_permisos);

            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                botonesVisibles(false);
                permisos.add(Manifest.permission.ACCESS_FINE_LOCATION);
                textoPermisos = String.format("%s%s", textoPermisos, getString(R.string.ubicacion_primer));
            }

            if(idUsuario != null) {
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
                    if(!(ActivityCompat.checkSelfPermission(
                            context, Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                            == PackageManager.PERMISSION_GRANTED)) {
                        permisos.add(Manifest.permission.ACCESS_BACKGROUND_LOCATION);
                        textoPermisos = String.format("%s%s", textoPermisos, getString(R.string.ubicacion_segundo));
                    }
            }else{
                new AlarmaProceso().activaAlarmaProceso(getApplicationContext());
            }

            if(!permisos.isEmpty()){
                solicitaPermisoUbicacion(textoPermisos);
            } else {
                locationManager.requestLocationUpdates(
                        LocationManager.GPS_PROVIDER, 1000, 2, this);
                if(locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER) != null)
                    onLocationChanged(
                            locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER));
                else{
                    try {
                        textoInformativoDistancia(tarea.getDouble(Auxiliar.latitud),
                                tarea.getDouble(Auxiliar.longitud),
                                getIntent().getExtras().getDouble(Auxiliar.posUsuarioLat),
                                getIntent().getExtras().getDouble(Auxiliar.posUsuarioLon));
                    }catch (Exception e){
                        explicacionDistancia.setText(getResources().getString(R.string.recuperandoPosicion));
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if(map != null)
            map.onResume();
    }

    List<String> permisos;

    private void solicitaPermisoUbicacion(String textoDialogo) {
        AlertDialog.Builder alertaExplicativa = new AlertDialog.Builder(this);
        alertaExplicativa.setTitle(getString(R.string.permi));
        alertaExplicativa.setMessage(Html.fromHtml(textoDialogo));
        alertaExplicativa.setPositiveButton(getString(R.string.solicitar), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //Se comprueba todos los permisos que necesite la app de nuevo, por este
                // motivo se puede salir del for directamente
                ActivityCompat.requestPermissions(
                        Preview.this,
                        permisos.toArray(new String[permisos.size()]),
                        1002);
            }
        });
        alertaExplicativa.setNegativeButton(getString(R.string.volver), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                onBackPressed();
            }
        });
        alertaExplicativa.setCancelable(false);
        alertaExplicativa.show();
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

    /**
     * Método para modificar la distancia que existe, en línea recta, entre la tarea y el usuario
     * @param tareaLat Latitud de la tarea
     * @param tareaLon Longitud de la tarea
     * @param usuarioLat Latitud del usuario
     * @param usuarioLon Longitud del usuario
     */
    private void textoInformativoDistancia(double tareaLat, double tareaLon, double usuarioLat, double usuarioLon){
        double distancia = Auxiliar.calculaDistanciaDosPuntos(
                tareaLat,
                tareaLon,
                usuarioLat,
                usuarioLon);
        //TODO cambiar a 0.15
        if(distancia <= 0.15){
            botonesVisibles(true);
        }else{
            botonesVisibles(false);
            if(distancia>=1)
                textoDistancia.setText(String.format("%.2f km", distancia));
            else
                textoDistancia.setText(String.format("%.2f m", distancia*1000));
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

    private void pintaSnackBar(String texto){
        Snackbar snackbar = Snackbar.make(findViewById(R.id.clPreview), R.string.app_name, Snackbar.LENGTH_SHORT);
        snackbar.setTextColor(getResources().getColor(R.color.colorSecondaryText));
        snackbar.getView().setBackground(getResources().getDrawable(R.drawable.snack));
        snackbar.setText(texto);
        snackbar.show();
    }
}
