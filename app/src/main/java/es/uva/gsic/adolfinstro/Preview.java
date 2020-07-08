package es.uva.gsic.adolfinstro;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.preference.PreferenceManager;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.text.LineBreaker;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

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

/**
 * Método para mostrar toda la información relacionada con la tarea que el usuario podría llegar a
 * realizar.
 *
 * @author Pablo
 * @version 20200703
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
        try {
            tarea = PersistenciaDatos.recuperaTarea(getApplication(), PersistenciaDatos.ficheroNotificadas, idTarea);
            tarea.put(Auxiliar.fechaInicio, Auxiliar.horaFechaActual());
            PersistenciaDatos.reemplazaJSON(getApplication(), PersistenciaDatos.ficheroNotificadas, tarea);
        }catch (Exception e){
            tarea = null;
        }

        try{
            switch (tarea.getString(Auxiliar.tipoRespuesta)){
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
        }catch (JSONException e){
            e.printStackTrace();
        }

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

            map.setMultiTouchControls(false);

            map.setTilesScaledToDpi(true);

            map.setClickable(false);
            map.setEnabled(false);


            TextView titulo = findViewById(R.id.tituloPreview);
            titulo.setText(tarea.getString(Auxiliar.titulo));
            TextView descripcion = findViewById(R.id.textoPreview);
            descripcion.setText(tarea.getString(Auxiliar.recursoAsociadoTexto));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                descripcion.setJustificationMode(LineBreaker.JUSTIFICATION_MODE_INTER_WORD);
            }
            //ImageView tipoTarea = findViewById(R.id.ivTipoTareaPreview);
            //String tipo = tarea.getString(Auxiliar.tipoRespuesta);
            //titulo.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, Auxiliar.iconoTipoTarea(tipo), 0);

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
                            try{
                                Intent intent = new Intent(context, MapaNavegable.class);
                                intent.putExtra(Auxiliar.latitud + "user", getIntent().getExtras().getDouble(Auxiliar.posUsuarioLat));
                                intent.putExtra(Auxiliar.longitud + "user", getIntent().getExtras().getDouble(Auxiliar.posUsuarioLon));
                                intent.putExtra(Auxiliar.latitud + "task", tarea.getDouble(Auxiliar.latitud));
                                intent.putExtra(Auxiliar.longitud + "task", tarea.getDouble(Auxiliar.longitud));
                                startActivity(intent);
                            }catch (Exception e){
                                //Toast.makeText(context,  context.getString(R.string.recuperandoPosicion), Toast.LENGTH_SHORT).show();
                                pintaSnackBar(context.getString(R.string.recuperandoPosicion));
                            }
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

            JSONObject idUsuario = PersistenciaDatos.recuperaTarea(getApplication(), PersistenciaDatos.ficheroUsuario, Auxiliar.id);
            if(Login.firebaseAuth == null || idUsuario == null) {
                snackBarLogin(R.id.clIdentificatePreview);
            }

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void snackBarLogin(int snack){
        Snackbar snackbar = Snackbar.make(findViewById(snack), R.string.textoInicioBreve, Snackbar.LENGTH_INDEFINITE);
        snackbar.setTextColor(getResources().getColor(R.color.white));
        snackbar.setAction(R.string.autenticarse, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Login.gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestIdToken(getString(R.string.default_web_client_id))
                        .requestEmail().build();
                Login.googleSignInClient = GoogleSignIn.getClient(context, Login.gso);
                Intent intent = Login.googleSignInClient.getSignInIntent();
                startActivityForResult(intent, Login.requestAuth);
            }
        });
        snackbar.setActionTextColor(getResources().getColor(R.color.texto));
        snackbar.getView().setBackground(getResources().getDrawable(R.drawable.snack));
        snackbar.show();
    }

    @Override
    public void onActivityResult(int requestCode, int result, Intent data) {
        super.onActivityResult(requestCode, result, data);
        switch (requestCode) {
            case Login.requestAuth:
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
        AuthCredential authCredential = GoogleAuthProvider.getCredential(googleSignInAccount.getIdToken(), null);
        Login.firebaseAuth.signInWithCredential(authCredential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>(){
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    FirebaseUser firebaseUser = Login.firebaseAuth.getCurrentUser();
                    updateUI(firebaseUser, true);
                }else{
                    updateUI(null, true);
                }
            }
        });
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
                    JSONObject idUsuario = PersistenciaDatos.recuperaTarea(getApplication(), PersistenciaDatos.ficheroUsuario, Auxiliar.id);
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
                    //Auxiliar.returnMain(this);
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
            //Toast.makeText(context, getString(R.string.errorOpera), Toast.LENGTH_SHORT).show();
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
            if (Auxiliar.tareaRegistrada(getApplication(), tarea.getString(Auxiliar.id))) {
                //Toast.makeText(context, getString(R.string.tareaRegistrada), Toast.LENGTH_LONG).show();
                pintaSnackBar(getString(R.string.tareaRegistrada));
            } else {
                JSONObject idUsuario = PersistenciaDatos.recuperaTarea(getApplication(), PersistenciaDatos.ficheroUsuario, Auxiliar.id);
                if(Login.firebaseAuth == null || idUsuario == null) {
                    snackBarLogin(R.id.clPreview);
                }else{
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
        //TODO reducir la distancia a 0.15
        if(distancia <= 5.5){
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
        snackbar.setTextColor(getResources().getColor(R.color.white));
        snackbar.getView().setBackground(getResources().getDrawable(R.drawable.snack));
        snackbar.setText(texto);
        snackbar.show();
    }
}
