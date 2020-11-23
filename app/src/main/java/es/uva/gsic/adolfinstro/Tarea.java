package es.uva.gsic.adolfinstro;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Html;
import android.text.InputFilter;
import android.text.InputType;
import android.text.method.LinkMovementMethod;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import es.uva.gsic.adolfinstro.auxiliar.AdaptadorImagenesCompletadas;
import es.uva.gsic.adolfinstro.auxiliar.AdaptadorVideosCompletados;
import es.uva.gsic.adolfinstro.auxiliar.Auxiliar;
import es.uva.gsic.adolfinstro.auxiliar.ImagenesCamara;
import es.uva.gsic.adolfinstro.persistencia.PersistenciaDatos;

import static java.util.Objects.requireNonNull;

/**
 * Clase encargada de mostrar la tarea al usuario. A medida que complete la actividad se irá almacenando
 * la respuesta en una base de datos.
 *
 * @author Pablo
 * @version 20201028
 */
public class Tarea extends AppCompatActivity implements
        AdaptadorVideosCompletados.ItemClickListenerVideo,
        AdaptadorImagenesCompletadas.ItemClickListener{

    /** Instancia del campo de texto donde introduce el usuario la respuesta*/
    private EditText etRespuestaTextual;
    /** Instancia del botón de la cámara*/
    private Button btCamara;
    /** Instancia del botón para finalizar la toma de imágenes */
    private Button btTerminar;
    /** Instancia donde se almacena el tipo de tarea*/
    private String tipo;
    /** Instancia donde se almacena el identificador de la tarea */
    private String idTarea;
    /** Imagen descriptiva en baja resolución */
    private String recursoAsociadoImagen300px;
    /** Imagen descriptiva en alta resolución */
    private String recursoAsociadoImagen;
    /** Texto que se espera que tenga la respuesta del alumno*/
    private String respuestaEsperada;

    private RecyclerView recyclerView;
    private AdaptadorImagenesCompletadas adaptadorImagenes;
    private AdaptadorVideosCompletados adaptadorVideos;

    /** URI de la imagen que se acaba de tomar */
    private Uri photoURI;
    /** URI del vídeo que se acaba de tomar */
    private Uri videoURI;

    /** Estado del botón de la cámara */
    private boolean estadoBtCamara;
    /** Estado del botón para volver a la actividad principal*/
    private boolean estadoBtCancelar;

    private boolean estadoBtTerminar;

    RecepcionNotificaciones recepcionNotificaciones;

    private List<ImagenesCamara> imagenesCamaraList;

    private int posicion;

    /** URL de la licencia de la imagen si la tuviera */
    private String urlLicencia;

    /** Identificador del usuario */
    private String idUsuario;

    /** Indica si la tarea realizado se tiene que publicar o no */
    private boolean publicarRespuesta;

    /**
     * Método que se lanza al inicio de la vida de la actividad. Se encarga de dibujar la interfaz
     * gráfica sobre la que va a trabajar el cliente. Establece la conexión con la base de datos
     * para que se consiga la persistencia.
     *
     * @param savedInstanceState Bundle
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tarea);

        requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        try{
            idUsuario = PersistenciaDatos.recuperaTarea(
                    getApplication(), PersistenciaDatos.ficheroUsuario, Auxiliar.id).getString(Auxiliar.uid);
        }catch (Exception e){
            e.printStackTrace();
        }
        idTarea = getIntent().getExtras().getString(Auxiliar.id);
        if(!PersistenciaDatos.existeTarea(
                getApplication(),
                PersistenciaDatos.ficheroNotificadas,
                idTarea,
                idUsuario))
            mensajeError();
        else {
            try {
                if(savedInstanceState == null)
                    posicion = 0;
                else
                    posicion = savedInstanceState.getInt("POSICION");

                // Instancia donde se colocará la imagen descriptiva de la tarea
                ImageView ivImagenDescripcion = findViewById(R.id.ivImagenDescripcion);

                JSONObject tarea = PersistenciaDatos.recuperaTarea(
                        getApplication(),
                        PersistenciaDatos.ficheroNotificadas,
                        idTarea,
                        idUsuario);
                try {
                    recursoAsociadoImagen300px = tarea.getString(Auxiliar.recursoImagenBaja);
                    if(recursoAsociadoImagen300px.equals("") || recursoAsociadoImagen300px.equals("?width=300"))
                        recursoAsociadoImagen300px = null;
                }catch (Exception e){
                    recursoAsociadoImagen300px = null;
                }
                try{
                    recursoAsociadoImagen = tarea.getString(Auxiliar.recursoImagen);
                    if(recursoAsociadoImagen.equals(""))
                        recursoAsociadoImagen = null;
                }catch (Exception e){
                    recursoAsociadoImagen = null;
                }
                String urlImagen = null;
                if(recursoAsociadoImagen != null){
                    if(recursoAsociadoImagen300px != null){ //Se muestra la imagen en baja resolución
                        urlImagen = recursoAsociadoImagen300px;
                        Picasso.get()
                                .load(recursoAsociadoImagen300px)
                                .placeholder(R.drawable.ic_cloud_download_blue_80dp)
                                .tag(Auxiliar.cargaImagenTarea)
                                .into(ivImagenDescripcion);
                    }else{ //Solo tiene imagen en alta resolución
                        urlImagen = recursoAsociadoImagen;
                        Picasso.get()
                                .load(recursoAsociadoImagen)
                                .placeholder(R.drawable.ic_cloud_download_blue_80dp)
                                .tag(Auxiliar.cargaImagenTarea)
                                .into(ivImagenDescripcion);
                    }
                    ivImagenDescripcion.setVisibility(View.VISIBLE);
                }

                if(ivImagenDescripcion.getVisibility() == View.VISIBLE) {
                    TextView licenciaImagen = findViewById(R.id.tvInfoFotoTarea);
                    if(tarea.has(Auxiliar.textoLicencia)){
                        licenciaImagen.setText(tarea.getString(Auxiliar.textoLicencia));
                    }
                    urlLicencia = Auxiliar.enlaceLicencia(
                            this,
                            licenciaImagen,
                            urlImagen);
                }

                tipo = tarea.getString(Auxiliar.tipoRespuesta);
                try{
                    respuestaEsperada = tarea.getString(Auxiliar.respuestaEsperada);
                    if(respuestaEsperada.equals(""))
                        respuestaEsperada = null;
                }catch (Exception e){
                    respuestaEsperada = null;
                }

                TextView tvDescripcion = findViewById(R.id.tvDescripcion);
                etRespuestaTextual = findViewById(R.id.etRespuestaTextual);
                if(tarea.has(Auxiliar.respuestas)){
                    JSONArray respuestas = tarea.getJSONArray(Auxiliar.respuestas);
                    JSONObject respuesta;
                    for(int t = 0; t < respuestas.length(); t++){
                        respuesta = respuestas.getJSONObject(t);
                        if(respuesta.getString(Auxiliar.tipoRespuesta).equals(Auxiliar.texto)){
                            etRespuestaTextual.setText(respuesta.getString(Auxiliar.tipoRespuesta));
                            switch (tipo){
                                case Auxiliar.tipoSinRespuesta:
                                case Auxiliar.tipoPreguntaCorta:
                                case Auxiliar.tipoPreguntaLarga:
                                    activaBtTerminar();
                                    break;
                                default:
                                    break;
                            }
                            break;
                        }
                    }
                }
                Button btAceptar = findViewById(R.id.btAceptar);
                btCamara = findViewById(R.id.btCamara);
                btTerminar = findViewById(R.id.btTerminar);

                tvDescripcion.setText(Auxiliar.creaEnlaces(this, tarea.getString(Auxiliar.recursoAsociadoTexto), false));
                tvDescripcion.setMovementMethod(LinkMovementMethod.getInstance());

                recyclerView = findViewById(R.id.rvRealizaTarea);
                recyclerView.setHasFixedSize(true);
                RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this,
                        LinearLayoutManager.HORIZONTAL, false);
                recyclerView.setLayoutManager(layoutManager);
                imagenesCamaraList = new ArrayList<>();

                if(tarea.has(Auxiliar.respuestas)){
                    JSONArray respuetas = tarea.getJSONArray(Auxiliar.respuestas);
                    JSONObject respuesta;
                    for(int t = 0; t < respuetas.length(); t++){
                        respuesta = respuetas.getJSONObject(t);
                        if(respuesta.getString(Auxiliar.tipoRespuesta).equals(Auxiliar.uri)){
                            imagenesCamaraList.add(new ImagenesCamara(respuesta.getString(Auxiliar.respuestaRespuesta), View.VISIBLE));
                        }
                    }
                    switch (tipo){
                        case Auxiliar.tipoPreguntaImagen:
                        case Auxiliar.tipoPreguntaImagenes:
                        case Auxiliar.tipoImagen:
                        case Auxiliar.tipoImagenMultiple:
                            recyclerView.setVisibility(View.VISIBLE);
                            adaptadorImagenes = new AdaptadorImagenesCompletadas(this, imagenesCamaraList);
                            adaptadorImagenes.setClickListener(this);
                            recyclerView.setAdapter(adaptadorImagenes);
                            recyclerView.scrollToPosition(posicion);
                            if(!imagenesCamaraList.isEmpty())
                                activaBtTerminar();
                            break;
                        case Auxiliar.tipoVideo:
                            recyclerView.setVisibility(View.VISIBLE);
                            adaptadorVideos = new AdaptadorVideosCompletados(this, imagenesCamaraList);
                            adaptadorVideos.setClickListenerVideo(this);
                            recyclerView.setAdapter(adaptadorVideos);
                            recyclerView.scrollToPosition(posicion);
                            if(!imagenesCamaraList.isEmpty())
                                activaBtTerminar();
                            break;
                        default:
                            break;
                    }
                }

                switch (tipo) {
                    case Auxiliar.tipoSinRespuesta:
                        btAceptar.setText(R.string.voy);
                        etRespuestaTextual.setHint(getString(R.string.textoOpcional));
                        btAceptar.setVisibility(View.VISIBLE);
                        break;
                    case Auxiliar.tipoPreguntaCorta:
                        etRespuestaTextual.setInputType(InputType.TYPE_CLASS_TEXT);
                        etRespuestaTextual.setFilters(new InputFilter[]{new InputFilter.LengthFilter(40)});
                        btAceptar.setVisibility(View.VISIBLE);
                        break;
                    case Auxiliar.tipoPreguntaLarga:
                        btAceptar.setVisibility(View.VISIBLE);
                        break;
                    case Auxiliar.tipoPreguntaImagen:
                    case Auxiliar.tipoPreguntaImagenes:
                        btCamara.setVisibility(View.VISIBLE);
                        break;
                    case Auxiliar.tipoImagen:
                    case Auxiliar.tipoVideo:
                    case Auxiliar.tipoImagenMultiple:
                        etRespuestaTextual.setHint(getString(R.string.textoOpcional));
                        if(tipo.equals(Auxiliar.tipoVideo))
                            btCamara.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_videocam_white_24dp, 0, 0, 0);
                        btCamara.setVisibility(View.VISIBLE);
                        break;
                    default:
                        break;
                }

                estadoBtCamara = true;
                estadoBtCancelar = true;

                //checkPermissions();

                tarea.put(Auxiliar.estadoTarea, EstadoTarea.NO_COMPLETADA.getValue());
                tarea.put(Auxiliar.fechaUltimaModificacion, Auxiliar.horaFechaActual());
                PersistenciaDatos.reemplazaJSON(
                        getApplication(),
                        PersistenciaDatos.ficheroNotificadas,
                        tarea);
            }catch (Exception e){
                e.printStackTrace();
            }

            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
            publicarRespuesta = sharedPreferences.getBoolean(Ajustes.PORTAFOLIO_pref, false);
        }

        try {
            version = this.getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    String version;

    private void mensajeError(){
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
        alertBuilder.setTitle(getString(R.string.tituErrorBBDD));
        alertBuilder.setMessage(getString(R.string.ErrorBBDD));
        alertBuilder.setPositiveButton(getString(R.string.accept), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Auxiliar.returnMain(getBaseContext());
            }
        });
        alertBuilder.setCancelable(false);
        alertBuilder.show();
    }

    List<String> permisos;
    /**
     * Método que atiende a las pulsaciones en los botones
     * @param view Referencia al lanzador del evento
     */
    public void boton(View view) {
        final Intent intent;
        switch (view.getId()){
            case R.id.btAceptar:
                if(tipo.equals(Auxiliar.tipoSinRespuesta)){
                    try {
                        JSONObject respuesta = PersistenciaDatos.obtenTarea(
                                getApplication(),
                                PersistenciaDatos.ficheroNotificadas,
                                idTarea,
                                idUsuario);
                        respuesta.put(Auxiliar.estadoTarea, EstadoTarea.COMPLETADA.getValue());
                        respuesta.put(Auxiliar.fechaFinalizacion, Auxiliar.horaFechaActual());
                        respuesta.put(Auxiliar.fechaUltimaModificacion, Auxiliar.horaFechaActual());
                        respuesta.put(Auxiliar.publico, publicarRespuesta);
                        PersistenciaDatos.guardaJSON(getApplication(),
                                PersistenciaDatos.ficheroCompletadas,
                                respuesta,
                                Context.MODE_PRIVATE);
                        if(!etRespuestaTextual.getText().toString().isEmpty()){
                            PersistenciaDatos.guardaTareaRespuesta(getApplication(),
                                    PersistenciaDatos.ficheroCompletadas,
                                    respuesta,
                                    etRespuestaTextual.getText().toString(),
                                    Auxiliar.texto,
                                    Context.MODE_PRIVATE);
                        }
                    }catch (Exception e){
                        mensajeError();
                    }
                    tareaCompletadaFirebase();
                    Auxiliar.puntuaTarea(this, idTarea);
                } else {
                    guardaRespuestaPregunta();
                }
                break;
            case R.id.btCamara:
                permisos = new ArrayList<>();

                String textoPermisos = getString(R.string.necesidad_permisos);

                if(!(ActivityCompat.checkSelfPermission(
                        this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)) {
                    permisos.add(Manifest.permission.CAMERA);
                    textoPermisos = String.format("%s%s", textoPermisos, getString(R.string.permiso_camara));
                }
                /*if(!(ActivityCompat.checkSelfPermission(
                        this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)) {
                    permisos.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
                    textoPermisos = String.format("%s%s", textoPermisos, getString(R.string.permiso_almacenamiento));
                }*/
                if(tipo.equals(Auxiliar.tipoVideo) && !(ActivityCompat.checkSelfPermission(
                        this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED)) {
                    permisos.add(Manifest.permission.RECORD_AUDIO);
                    textoPermisos = String.format("%s%s", textoPermisos, getString(R.string.permiso_micro));
                }

                if(permisos.isEmpty()) {
                    bloqueaBotones();
                    switch (tipo) {
                        case Auxiliar.tipoPreguntaImagen:
                            realizaCaptura(0);
                            break;
                        case Auxiliar.tipoImagen:
                            realizaCaptura(1);
                            break;
                        case Auxiliar.tipoImagenMultiple:
                        case Auxiliar.tipoPreguntaImagenes:
                            realizaCaptura(2);
                            break;
                        case Auxiliar.tipoVideo:
                            realizaVideo();
                            break;
                    }
                }else{
                    AlertDialog.Builder alertaExplicativa = new AlertDialog.Builder(this);
                    alertaExplicativa.setTitle(getString(R.string.permi));
                    alertaExplicativa.setMessage(Html.fromHtml(textoPermisos));
                    alertaExplicativa.setPositiveButton(getString(R.string.solicitar), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //Se comprueba todos los permisos que necesite la app de nuevo, por este
                            // motivo se puede salir del for directamente
                            ActivityCompat.requestPermissions(
                                    Tarea.this,
                                    permisos.toArray(new String[permisos.size()]),
                                    1004);
                        }
                    });
                    alertaExplicativa.setCancelable(true);
                    alertaExplicativa.show();
                }
                break;
            case R.id.ivImagenDescripcion:
                if(recursoAsociadoImagen != null) {//Si la imagen en alta resolución existe se salta simpre a ella para la vista en detalle
                    intent = new Intent(this, ImagenCompleta.class);
                    if(recursoAsociadoImagen != null)
                        intent.putExtra("IMAGENCOMPLETA", recursoAsociadoImagen);
                    intent.putExtra("MUESTRAC", true);
                    startActivity(intent);
                }else{//Ya está visible la imagen de resolución baja y no hay una alta asociada
                    if(recursoAsociadoImagen300px != null){
                        intent = new Intent(this, ImagenCompleta.class);
                        intent.putExtra("IMAGENCOMPLETA", recursoAsociadoImagen);
                        intent.putExtra("MUESTRAC", true);
                        startActivity(intent);
                    }
                    else{//No hay ninguna imagen. Este mensaje no debería aparecer nunca
                        muestraSnackBar(getString(R.string.recursoMaximaResolucion));
                    }
                }
                break;
            case R.id.tvInfoFotoTarea:
                if(urlLicencia != null)
                    Auxiliar.navegadorInterno(this, urlLicencia);
                break;
            case R.id.btTerminar:
                try {
                    if(tipo.equals(Auxiliar.tipoPreguntaImagen) || tipo.equals(Auxiliar.tipoPreguntaImagenes)){
                        if(etRespuestaTextual.getText().toString().isEmpty()){
                            etRespuestaTextual.setError(getString(R.string.respuestaVacia));
                        }else{
                            guardaRespuesta(etRespuestaTextual.getText().toString());
                            Toast.makeText(this, getString(R.string.imagenesG), Toast.LENGTH_SHORT).show();
                        }
                    }else {
                        guardaRespuesta(etRespuestaTextual.getText().toString());
                    }
                }catch (Exception e){
                    mensajeError();
                }
                break;
            case R.id.ivDenunciarPregunta:
                final Dialog dialogo = new Dialog(this);
                dialogo.setContentView(R.layout.dialogo_denuncia);
                dialogo.setCancelable(true);
                //https://stackoverflow.com/questions/10211338/view-inside-scrollview-doesnt-take-all-place
                //Que el dialogo esté centrado y se vea como un popup normal
                Window window = dialogo.getWindow();
                window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                window.setGravity(Gravity.CENTER);
                Button botonEnviar = dialogo.findViewById(R.id.btAceptarReportar);
                final CheckBox cb1 = dialogo.findViewById(R.id.cbNoEntiendoTarea);
                final CheckBox cb2 = dialogo.findViewById(R.id.cbTareaFallos);
                final CheckBox cb3 = dialogo.findViewById(R.id.cbNoPuedoRealizar);
                final CheckBox cb4 = dialogo.findViewById(R.id.cbTareaErronea);
                final CheckBox cb5 = dialogo.findViewById(R.id.cbTareaNoPertinente);
                final EditText editText = dialogo.findViewById(R.id.etDenuncia);
                botonEnviar.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        boolean envia = false;

                        String contenido;
                        if(idUsuario != null)
                            contenido = String.format("%s\n%s\n%s\n%s\n%s\n", idTarea, idUsuario, Build.MANUFACTURER, Build.MODEL, version);
                        else
                            contenido = String.format("%s\n%s\n%s\n%s\n", idTarea, Build.MANUFACTURER, Build.MODEL, version);

                        String textoEdit = editText.getText().toString();
                        String cb = "";
                        if(cb1.isChecked() || cb2.isChecked() || cb3.isChecked() || cb4.isChecked() || cb5.isChecked()){//Alguno de los checkbox está activado, no tiene porque tener texto
                            envia = true;
                            if(cb1.isChecked()) {
                                contenido = contenido.concat(String.format("%s\n", getString(R.string.noEntiendo)));
                                cb  = cb.concat("1");
                            }
                            if(cb2.isChecked()) {
                                contenido = contenido.concat(String.format("%s\n", getString(R.string.tareaConFallos)));
                                cb  = cb.concat("2");
                            }
                            if(cb3.isChecked()) {
                                contenido = contenido.concat(String.format("%s\n", getString(R.string.noSePuedeRealizar)));
                                cb  = cb.concat("3");
                            }
                            if(cb4.isChecked()) {
                                contenido = contenido.concat(String.format("%s\n", getString(R.string.tarea_erronea)));
                                cb  = cb.concat("4");
                            }
                            if(cb5.isChecked()) {
                                contenido = contenido.concat(String.format("%s\n", getString(R.string.tarea_no_pertinente)));
                                cb  = cb.concat("5");
                            }
                            if(!textoEdit.isEmpty() || !textoEdit.equals("")) {
                                contenido = contenido.concat(String.format("%s\n", textoEdit));
                                cb = cb.concat(String.format(" %s", textoEdit.substring(0, (Math.min(textoEdit.length(), 19)))));
                            }
                        }else {//Necesita texto
                            if(!textoEdit.isEmpty() || !textoEdit.equals("")) {
                                envia = true;
                                contenido = contenido.concat(String.format("%s\n", textoEdit));
                                cb = cb.concat(String.format(" %s", textoEdit.substring(0, (Math.min(textoEdit.length(), 19)))));
                            }
                            else
                                editText.setError(getString(R.string.errorTextoDenuncia));
                        }
                        if(envia) {
                            Intent mail = new Intent(Intent.ACTION_SEND);
                            mail.setType("message/rfc822");
                            String[] direcciones = {getString(R.string.emailCasualLearn)};
                            mail.putExtra(Intent.EXTRA_EMAIL, direcciones);
                            mail.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.errorTarea));
                            mail.putExtra(Intent.EXTRA_TEXT, contenido);
                            if (mail.resolveActivity(getPackageManager()) != null) {
                                startActivity(mail);
                                dialogo.cancel();
                            }else{
                                dialogo.cancel();
                                muestraSnackBar(getString(R.string.noEmail));
                            }
                            enviaDenunciaFirebase(cb);
                        }
                    }
                });
                dialogo.show();
                break;
        }
    }

    /**
     * Método para guardar la respuesta de texto que haya dado el usuario. Agrega esta respueta al
     * vector de las respuestas.
     * @param respuestaTextual Frase o palabras dadas por el usuario.
     * @throws Exception Posibles excepciones al manipular el JSON
     */
    private void guardaRespuesta(String respuestaTextual) throws Exception {
        JSONObject json;
        json = PersistenciaDatos.obtenTarea(
                getApplication(),
                PersistenciaDatos.ficheroNotificadas,
                idTarea,
                idUsuario);
        json.put(Auxiliar.estadoTarea, EstadoTarea.COMPLETADA.getValue());
        json.put(Auxiliar.fechaUltimaModificacion, Auxiliar.horaFechaActual());
        json.put(Auxiliar.fechaFinalizacion, Auxiliar.horaFechaActual());
        json.put(Auxiliar.publico, publicarRespuesta);
        PersistenciaDatos.guardaJSON(
                getApplication(),
                PersistenciaDatos.ficheroCompletadas,
                json,
                Context.MODE_PRIVATE);
        if(!respuestaTextual.isEmpty()){
            PersistenciaDatos.guardaTareaRespuesta(
                    getApplication(),
                    PersistenciaDatos.ficheroCompletadas,
                    json,
                    respuestaTextual,
                    Auxiliar.texto,
                    Context.MODE_PRIVATE);
        }
        tareaCompletadaFirebase();
        Auxiliar.puntuaTarea(this, idTarea);
        finish();
    }

    /**
     * Método para envíar una denuncia de la tarea. Esta denuncia se recogerá mediante un evento de
     * FIREBASE
     */
    public void enviaDenunciaFirebase(String cb){
        Intent intent = new Intent();
        intent.setAction(Auxiliar.nunca_mas);
        intent.putExtra(Auxiliar.id, idTarea);
        sendBroadcast(intent);
        Bundle bundle = new Bundle();
        bundle.putString("idTarea", Auxiliar.idReducida(idTarea));
        bundle.putString("idUsuario", idUsuario);
        bundle.putString("motivo", cb);
        Login.firebaseAnalytics.logEvent("tareaDenunciada", bundle);
    }

    /**
     * Método que bloquea el botón de la cámara y el de cancelar
     */
    private void bloqueaBotones(){
        estadoBtCamara = false;
        estadoBtCancelar = false;
        setBotones();
    }

    /**
     * Método que establece si los botones son clicables o no dependiendo del estado de las variables
     * estadoBtCancelar y estadoBtCamara
     */
    private void setBotones(){
        btCamara.setClickable(estadoBtCamara);
        if(btTerminar.getVisibility() == View.VISIBLE)
            btTerminar.setClickable(estadoBtCamara);
    }

    /**
     * Método para hacer visible el botón para finalizar la tarea
     */
    private void activaBtTerminar(){
        btTerminar.setVisibility(View.VISIBLE);
        btTerminar.setClickable(true);
        estadoBtTerminar = true;
    }

    /**
     * Agrupa las sentencias necesarias para la realización de una foto
     * @param tipo Tipo de operación desde donde se invoca al método. Se utiliza para diferenciarlas
     *             cuando se vuelve de la actividad de la cámara
     */
    private void realizaCaptura(int tipo){
        final Context context = getBaseContext();
        Intent takePicture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if(takePicture.resolveActivity(getPackageManager())!=null){
            File photoFile = null;
            try{
                photoFile = Auxiliar.createFile(tipo,this);
            }catch (IOException e){
                //
            }
            if(photoFile != null){
                photoURI = FileProvider.getUriForFile(context, getString(R.string.fileProvider), photoFile);
                //photoURI = Uri.fromFile(photoFile);
                takePicture.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePicture, tipo);//Los requestCode solo pueden ser de 16 bits
            }
            else{
                muestraSnackBar(getString(R.string.errorOpera));
                desbloqueaBt();
            }
        } else{
            muestraSnackBar(getString(R.string.errorOpera));
            desbloqueaBt();
        }
    }

    /**
     * Método encargado de realizar las tareas necesarias para que el usuario pueda grabar un vídeo
     * con la aplicación. Realiza una llamada a la cámara del sistema. Para ahorrar espacio, los
     * vídeos se realizan en baja calidad.
     */
    private void realizaVideo(){
        Intent takeVideo = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);

        File videoFile = null;
        try {
            videoFile = Auxiliar.createFile(3, this);
        }catch (IOException e){
            //
        }
        if(videoFile != null){
            videoURI = FileProvider.getUriForFile(getBaseContext(), getString(R.string.fileProvider), videoFile);
            takeVideo.putExtra(MediaStore.EXTRA_OUTPUT, videoURI);
            if(takeVideo.resolveActivity(getPackageManager()) != null){
                //CALIDAD MMS
                //https://developer.android.com/reference/android/provider/MediaStore#EXTRA_VIDEO_QUALITY
                takeVideo.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 0);
                startActivityForResult(takeVideo, 3);
            }else{
                muestraSnackBar(getString(R.string.errorOpera));
                desbloqueaBt();
            }
        }
        else{
            muestraSnackBar(getString(R.string.errorOpera));
            desbloqueaBt();
        }
    }

    /**
     * Método que desbloquea el botón de cancelar
     */
    private void desbloqueaBt(){
        estadoBtCancelar = true;
        estadoBtCamara = true;
        setBotones();
    }

    /**
     * Método que se lanza para resolver el resultado de otra actividad. En nuestro caso se activa
     * cuando el usuario realice la captura o el vídeo
     * @param requestCode Código que identifica si ha sido una foto, un vídeo...
     * @param resultCode Código que devuelve la operación
     * @param data Intent
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        JSONObject respuesta = null;
        switch (requestCode){
            case 0: //Pregunta + imagen
            case 1: //Imagen
                switch (resultCode){
                    case RESULT_OK:
                        try {
                            respuesta = PersistenciaDatos.obtenTarea(
                                    getApplication(),
                                    PersistenciaDatos.ficheroNotificadas,
                                    idTarea,
                                    idUsuario);
                            respuesta.put(Auxiliar.estadoTarea, EstadoTarea.COMPLETADA.getValue());
                            respuesta.put(Auxiliar.fechaUltimaModificacion, Auxiliar.horaFechaActual());
                            PersistenciaDatos.guardaJSON(getApplication(), PersistenciaDatos.ficheroNotificadas,
                                    respuesta, Context.MODE_PRIVATE);
                            if (!PersistenciaDatos.guardaTareaRespuesta(
                                    getApplication(),
                                    PersistenciaDatos.ficheroNotificadas,
                                    respuesta,
                                    photoURI.toString(),
                                    Auxiliar.uri,
                                    Context.MODE_PRIVATE))
                                mensajeError();
                            else {
                                imagenesCamaraList.add( new ImagenesCamara(photoURI, View.VISIBLE));
                                actualizaContenedorImagenes(-1);
                                muestraSnackBar(getString(R.string.imagenG));
                            }
                            //Auxiliar.publicaGaleria(this, photoURI);
                            activaBtTerminar();
                            //Auxiliar.puntuaTarea(this, idTarea);
                        }catch (Exception e){
                            mensajeError();
                            if(respuesta != null)
                                PersistenciaDatos.reemplazaJSON(getApplication(), PersistenciaDatos.ficheroNotificadas, respuesta);
                        }
                        break;
                    case RESULT_CANCELED:
                        desbloqueaBt();
                        break;
                    default:
                        mensajeError();
                        muestraSnackBar(getString(R.string.errorOpera));
                }
                break;
            case 2://imagen multiple || preguntaImágenes
                switch (resultCode){
                    case RESULT_OK:
                        respuesta = PersistenciaDatos.recuperaTarea(
                                getApplication(),
                                PersistenciaDatos.ficheroNotificadas,
                                idTarea,
                                idUsuario);
                        if(!PersistenciaDatos.guardaTareaRespuesta(getApplication(), PersistenciaDatos.ficheroNotificadas,
                                respuesta, photoURI.toString(), Auxiliar.uri,
                                Context.MODE_PRIVATE))
                            mensajeError();
                        else {
                            activaBtTerminar();
                            desbloqueaBt();
                            imagenesCamaraList.add(new ImagenesCamara(photoURI, View.VISIBLE));
                            actualizaContenedorImagenes(-1);
                            muestraSnackBar(getString(R.string.imagenGN));
                        }
                        break;
                    case RESULT_CANCELED:
                        desbloqueaBt();
                        break;
                    default:
                        mensajeError();
                        Auxiliar.errorToast(this);
                }
                break;
            case 3://video
                switch (resultCode){
                    case RESULT_OK:
                        try {
                            respuesta = PersistenciaDatos.obtenTarea(
                                    getApplication(),
                                    PersistenciaDatos.ficheroNotificadas,
                                    idTarea,
                                    idUsuario);
                            respuesta.put(Auxiliar.estadoTarea, EstadoTarea.COMPLETADA.getValue());
                            respuesta.put(Auxiliar.fechaUltimaModificacion, Auxiliar.horaFechaActual());
                            PersistenciaDatos.guardaJSON(getApplication(), PersistenciaDatos.ficheroNotificadas, respuesta, Context.MODE_PRIVATE);
                            if (!PersistenciaDatos.guardaTareaRespuesta(getApplication(),
                                    PersistenciaDatos.ficheroNotificadas,
                                    respuesta,
                                    videoURI.toString(),
                                    Auxiliar.uri,
                                    Context.MODE_PRIVATE))
                                mensajeError();
                            else {
                                muestraSnackBar(getString(R.string.videoG));
                                imagenesCamaraList.add(new ImagenesCamara(videoURI, View.VISIBLE));
                                actualizaContenedorVideos(-1);
                                activaBtTerminar();
                            }
                        }catch (Exception e){
                            mensajeError();
                            if(respuesta != null)
                                PersistenciaDatos.reemplazaJSON(getApplication(), PersistenciaDatos.ficheroNotificadas, respuesta);
                        }
                        break;
                    case RESULT_CANCELED:
                        desbloqueaBt();
                        break;
                    default:
                        mensajeError();
                        muestraSnackBar(getString(R.string.errorOpera));
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public void actualizaContenedorImagenes(int pos){
        recyclerView.setVisibility(View.VISIBLE);
        adaptadorImagenes = new AdaptadorImagenesCompletadas( this, imagenesCamaraList);
        adaptadorImagenes.setClickListener(this);
        recyclerView.invalidate();
        recyclerView.setAdapter(adaptadorImagenes);
        if(pos == -1) {
            posicion = imagenesCamaraList.size() - 1;
        }
        else {
            posicion = ((pos == 0) ? pos - 1 : pos);
        }
        recyclerView.scrollToPosition(posicion);
    }

    public void actualizaContenedorVideos(int pos){
        recyclerView.setVisibility(View.VISIBLE);
        adaptadorVideos = new AdaptadorVideosCompletados( this, imagenesCamaraList);
        adaptadorVideos.setClickListenerVideo(this);
        recyclerView.invalidate();
        recyclerView.setAdapter(adaptadorVideos);
        if(pos == -1)
            recyclerView.scrollToPosition(imagenesCamaraList.size() - 1);
        else
            recyclerView.scrollToPosition(((pos == 0) ? pos - 1 : pos));
    }

    /**
     * Método para almacenar la respuesta del usuario
     */
    private void guardaRespuestaPregunta(){
        String respuesta = etRespuestaTextual.getText().toString();
        respuesta = respuesta.trim();
        if(respuesta.isEmpty()){
            etRespuestaTextual.setError(getString(R.string.respuestaVacia));
        }
        else{
            JSONObject tarea;
            if(tipo.equals(Auxiliar.tipoPreguntaCorta) || tipo.equals(Auxiliar.tipoPreguntaLarga)) {
                try {
                    tarea = PersistenciaDatos.recuperaTarea(
                            getApplication(),
                            PersistenciaDatos.ficheroNotificadas,
                            idTarea,
                            idUsuario);
                    assert tarea != null;
                    tarea.put(Auxiliar.estadoTarea, EstadoTarea.COMPLETADA.getValue());
                    tarea.put(Auxiliar.fechaUltimaModificacion, Auxiliar.horaFechaActual());
                    tarea.put(Auxiliar.publico, publicarRespuesta);
                    PersistenciaDatos.guardaJSON(getApplication(), PersistenciaDatos.ficheroCompletadas, tarea, Context.MODE_PRIVATE);
                    if (!PersistenciaDatos.guardaTareaRespuesta(getApplication(),
                            PersistenciaDatos.ficheroCompletadas,
                            tarea,
                            respuesta,
                            Auxiliar.texto,
                            Context.MODE_PRIVATE))
                        mensajeError();
                }catch (Exception e){
                    mensajeError();
                }
                if(respuestaEsperada!=null){
                    if (respuesta.toLowerCase().contains(respuestaEsperada.toLowerCase())) {
                        mensajeRespuestaEsperada(this,true);
                    } else {
                        mensajeRespuestaEsperada(this,false);
                    }
                }else {
                    Toast.makeText(this, getString(R.string.respuestaG), Toast.LENGTH_SHORT).show();
                    tareaCompletadaFirebase();
                    Auxiliar.puntuaTarea(this, idTarea);
                }
            }
        }
    }

    /**
     * Método para indicar al usuario si la respuesta que ha dado es correcta o no
     * @param context Contexto
     * @param acierto Indica si el usuario ha acertado o no
     */
    private void mensajeRespuestaEsperada(final Context context, boolean acierto){
        final AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
        alertBuilder.setTitle(getString(R.string.respuestaEspeTitulo));
        if(acierto){
            alertBuilder.setIcon(R.drawable.ic_check_green_24dp);
            alertBuilder.setMessage(R.string.respuestaEspeCorrecta);
        }else{
            alertBuilder.setIcon(R.drawable.ic_thumb_down_red_24dp);
            alertBuilder.setMessage(getString(R.string.respuestaEspeIncrrecta)+"\r\n"+respuestaEsperada);
        }

        alertBuilder.setPositiveButton(getString(R.string.continuar), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                tareaCompletadaFirebase();
                Auxiliar.puntuaTarea(context, idTarea);
                finish();
            }
        });
        alertBuilder.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                tareaCompletadaFirebase();
                Auxiliar.puntuaTarea(context, idTarea);
                finish();
            }
        });
        alertBuilder.setCancelable(false);
        alertBuilder.show();
    }

    /**
     * Método que se utiliza para almacenar el valor de algunas variables de la clase
     * @param bundle Objeto donde se almacenan las variables de la clase
     */
    @Override
    protected void onSaveInstanceState(@NotNull Bundle bundle){
        super.onSaveInstanceState(bundle);
        bundle.putBoolean("ESTADOCAMARA", estadoBtCamara);
        bundle.putBoolean("ESTADOCANCELAR", estadoBtCancelar);
        bundle.putBoolean("ESTADOTERMINAR", estadoBtTerminar);
        bundle.putString("IDTAREA", idTarea);
        bundle.putString("TIPOTAREA", tipo);
        bundle.putString("RECURSOIMAGEN300", recursoAsociadoImagen300px);
        bundle.putString("RECURSOIMAGEN", recursoAsociadoImagen);
        bundle.putInt("POSICION", posicion);
    }

    /**
     * Método utilizado para restablecer el valor de algunas variables de la clase
     * @param bundle Objeto donde están almacenadas las variables de la clase entre otras cosas
     */
    @Override
    protected void onRestoreInstanceState(@NotNull Bundle bundle){
        super.onRestoreInstanceState(bundle);
        //restantes = bundle.getInt("RESTANTES");
        estadoBtCamara = bundle.getBoolean("ESTADOCAMARA");
        estadoBtCancelar = bundle.getBoolean("ESTADOCANCELAR");
        estadoBtTerminar = bundle.getBoolean("ESTADOTERMINAR");
        idTarea = bundle.getString("IDTAREA");
        tipo = bundle.getString("TIPOTAREA");
        recursoAsociadoImagen300px = bundle.getString("RECURSOIMAGEN300");
        recursoAsociadoImagen = bundle.getString("RECURSOIMAGEN");
        if(estadoBtTerminar){
            activaBtTerminar();
        }
        setBotones();
    }

    /**
     * Método que establece la conexión con la base de datos si no existiera
     */
    public void onResume() {
        super.onResume();
        recepcionNotificaciones = new RecepcionNotificaciones();
        registerReceiver(recepcionNotificaciones, Auxiliar.intentFilter());
    }

    /**
     * Método para controlar la llamada del usuario al botón atrás
     */
    @Override
    public void onBackPressed(){
        super.onBackPressed();
        finish();
    }

    @Override
    protected void onPause(){
        super.onPause();
        unregisterReceiver(recepcionNotificaciones);
    }

    @Override
    public void onItemClick(View view, int position) {
        eliminaFotoVideo(position);
    }

    @Override
    public void onItemClickVideo(View view, int position) {
        eliminaFotoVideo(position);
    }

    private void eliminaFotoVideo(int position){
        Uri uri = imagenesCamaraList.get(position).getDireccion();
        try{
            JSONObject tarea = PersistenciaDatos.obtenTarea(
                    getApplication(),
                    PersistenciaDatos.ficheroNotificadas,
                    idTarea,
                    idUsuario);
            if(tarea == null){
                tarea = PersistenciaDatos.obtenTarea(
                        getApplication(),
                        PersistenciaDatos.ficheroCompletadas,
                        idTarea,
                        idUsuario);
            }
            assert tarea != null;
            JSONArray respuestas = tarea.getJSONArray(Auxiliar.respuestas);
            JSONObject respuesta;
            for(int i = 0; i < respuestas.length(); i++){
                respuesta = respuestas.getJSONObject(i);
                if(respuesta.getString(Auxiliar.tipoRespuesta).equals(Auxiliar.uri) &&
                        respuesta.getString(Auxiliar.respuestaRespuesta).equals(uri.toString())){
                    respuestas.remove(i);
                    tarea.put(Auxiliar.respuestas, respuestas);
                    this.getContentResolver().delete(uri, null, null);
                    imagenesCamaraList.remove(position);
                    switch (tipo){
                        case Auxiliar.tipoVideo:
                            desbloqueaBt();
                            actualizaContenedorVideos(position);
                            btTerminar.setVisibility(View.GONE);
                            break;
                        case Auxiliar.tipoImagenMultiple:
                        case Auxiliar.tipoPreguntaImagenes:
                            desbloqueaBt();
                            actualizaContenedorImagenes(position);
                            if(imagenesCamaraList.size() == 0){
                                btTerminar.setVisibility(View.GONE);
                            }
                            break;
                        case Auxiliar.tipoImagen:
                        case Auxiliar.tipoPreguntaImagen:
                            desbloqueaBt();
                            actualizaContenedorImagenes(position);
                            btTerminar.setVisibility(View.GONE);
                            break;
                        default:
                            break;
                    }
                    tarea.put(Auxiliar.fechaUltimaModificacion, Auxiliar.horaFechaActual());
                    PersistenciaDatos.guardaJSON(
                            getApplication(),
                            PersistenciaDatos.ficheroNotificadas,
                            tarea,
                            Context.MODE_PRIVATE);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return false;
    }

    /**
     * Método para mostrar algún aviso al usuario mendiante una snackBar
     * @param texto Texto que se desea mostrar al usuario
     */
    private void muestraSnackBar(String texto){
        Snackbar snackbar = Snackbar.make(findViewById(R.id.clTarea), R.string.app_name, Snackbar.LENGTH_SHORT);
        snackbar.setTextColor(getResources().getColor(R.color.colorSecondaryText));
        snackbar.getView().setBackground(getResources().getDrawable(R.drawable.snack));
        snackbar.setText(texto);
        snackbar.show();
    }

    /**
     * Método para indicar dejar registro en Firebase de una tarea completada
     */
    private void tareaCompletadaFirebase(){
        try {
            Bundle bundle = new Bundle();
            bundle.putString("idTarea", Auxiliar.idReducida(idTarea));
            bundle.putString("idUsuario", idUsuario);
            Login.firebaseAnalytics.logEvent("tareaCompletada", bundle);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
