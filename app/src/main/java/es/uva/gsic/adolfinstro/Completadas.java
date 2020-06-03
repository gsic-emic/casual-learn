package es.uva.gsic.adolfinstro;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Layout;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import es.uva.gsic.adolfinstro.auxiliar.AdaptadorImagenesCompletadas;
import es.uva.gsic.adolfinstro.auxiliar.AdaptadorVideosCompletados;
import es.uva.gsic.adolfinstro.auxiliar.Auxiliar;
import es.uva.gsic.adolfinstro.persistencia.PersistenciaDatos;

/**
 * Clase para ofrecer al usuario la información de las respuestas que ha dado. También permite
 * modificar la respuesta dada o complementarla.
 *
 * @author Pablo García Zarza
 * @version 20200515
 */
public class Completadas extends AppCompatActivity implements
        AdaptadorImagenesCompletadas.ItemClickListener,
        View.OnClickListener,
        AdaptadorVideosCompletados.ItemClickListenerVideo{

    /** TextView donde se coloca la pregunta de la tarea */
    TextView enunciado;
    /** EditText donde se incluye la respuesta textual del usuario */
    EditText textoUsuario;
    /** Puntuación que el usuario tiene asignado a la tarea*/
    RatingBar ratingBar;
    /** Contenedor donde se colocará las imágenes o vídeos de la tarea realiacidos por el usuario */
    RecyclerView recyclerView;
    /** Botón con el que el usuario podrá agregar contenido multimedia*/
    Button btAgregar;
    /** Estado en el que se encuentra la edición */
    boolean editando=false;
    /** Tarea que ha completado el usuario */
    JSONObject tarea;
    /** Lista de identificadores únicos del contendido multimedia */
    List<String> listaURI;
    /** Lista de recursos a eliminar cuando el usuario guarde */
    List<Uri> uriEliminar;
    /** Lista de uris guardadadas por el si el usuario sale de la actividad sin guardar*/
    List<Uri> uriGuardadas;
    /** Lista de recursos multimedia representados en el contenedor */
    List<ImagenesCamara> imagenesCamaras;
    /** Adaptador del contenedor para las imágenes */
    AdaptadorImagenesCompletadas adaptadorImagenesCompletadas;
    /** Adaptador del contenedor para los vídeos */
    AdaptadorVideosCompletados adaptadorVideosCompletados;
    /** Posicion al que se desplaza el scroll */
    int posicion = 0;

    /**
     * Método de creación de la actividad. Pinta la interfaz gráfica y establece las referencias
     * para la lógica de la aplicación.
     *
     * @param savedInstanceState Bundle con el estado con el que se entra a la actividad
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_completadas);
        //Se activa la flecha para volver a la actividad anterior
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //Se recupera la tarea de la base de datos. La tarea permanence en el fichero de completadas por
        //si el usuario se sale sin guardar
        tarea = PersistenciaDatos.recuperaTarea(getApplication(), PersistenciaDatos.ficheroCompletadas,
                getIntent().getExtras().getString(Auxiliar.id));

        //Referencias a objetos
        enunciado = findViewById(R.id.tvDescripcionCompletada);
        ratingBar = findViewById(R.id.rbPuntuacionCompletada);
        textoUsuario = findViewById(R.id.etRespuestaTextualCompletada);


        btAgregar = findViewById(R.id.btAgregarCompletada);

        btAgregar.setOnClickListener(this);

        uriEliminar = new ArrayList<>();
        uriGuardadas = new ArrayList<>();

        if(savedInstanceState != null){
            posicion = savedInstanceState.getInt("POSICION");
        }

        try {
            enunciado.setText(tarea.getString(Auxiliar.recursoAsociadoTexto));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                enunciado.setJustificationMode(Layout.JUSTIFICATION_MODE_INTER_WORD);
            }

            try {
                ratingBar.setRating((float) tarea.getDouble(Auxiliar.rating));
            }catch (Exception e){
                e.printStackTrace();
            }

            if(tarea.has(Auxiliar.respuestas)) {
                JSONArray respuestas = tarea.getJSONArray(Auxiliar.respuestas);
                JSONObject respuesta;
                listaURI = new ArrayList<>();
                for (int i = 0; i < respuestas.length(); i++) {
                    respuesta = respuestas.getJSONObject(i);
                    if (respuesta.getString(Auxiliar.tipoRespuesta).equals(Auxiliar.texto)) {
                        if (!respuesta.getString(Auxiliar.respuestaRespuesta).equals("")) {
                            textoUsuario.setText(respuesta.getString(Auxiliar.respuestaRespuesta));
                            textoUsuario.setVisibility(View.VISIBLE);
                        }
                    } else {//URI de video o fotos
                        listaURI.add(respuesta.getString(Auxiliar.respuestaRespuesta));
                    }
                }
            }else{
                listaURI = new ArrayList<>();
            }

            recyclerView = findViewById(R.id.rvImagenesAlumno);
            recyclerView.setHasFixedSize(true);
            RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this,
                    LinearLayoutManager.HORIZONTAL, false);
            recyclerView.setLayoutManager(layoutManager);
            imagenesCamaras = new ArrayList<>();


            switch (tarea.getString(Auxiliar.tipoRespuesta)) {
                case Auxiliar.tipoImagen:
                case Auxiliar.tipoImagenMultiple:
                case Auxiliar.tipoPreguntaImagen:
                case Auxiliar.tipoVideo:
                    recyclerView.setVisibility(View.VISIBLE);
                    for (String s : listaURI) {
                        imagenesCamaras.add(new ImagenesCamara(s, View.GONE));
                    }
                    switch (tarea.getString(Auxiliar.tipoRespuesta)) {
                        case Auxiliar.tipoImagen:
                        case Auxiliar.tipoImagenMultiple:
                        case Auxiliar.tipoPreguntaImagen:
                            adaptadorImagenesCompletadas = new AdaptadorImagenesCompletadas(this,
                                    imagenesCamaras);
                            adaptadorImagenesCompletadas.setClickListener(this);
                            recyclerView.setAdapter(adaptadorImagenesCompletadas);
                            break;
                        case Auxiliar.tipoVideo:
                            adaptadorVideosCompletados = new AdaptadorVideosCompletados(this,
                                    imagenesCamaras);
                            adaptadorVideosCompletados.setClickListenerVideo(this);
                            recyclerView.setAdapter(adaptadorVideosCompletados);
                            break;
                        default:
                            break;
                    }
                    recyclerView.scrollToPosition(posicion);
                    break;
                default:
                    break;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        if(savedInstanceState != null) {
            editando = savedInstanceState.getBoolean("EDITANDO");
            onOptionsItemSelected((MenuItem) findViewById(R.id.editarCompletada));
        }
    }

    /**
     * Método para controlar la pulsación del botón atrás de la barra de tareas.
     *
     * @return True or False
     */
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }

    /**
     * Método para controlar la pulsación atrás del botón físico
     */
    @Override
    public void onBackPressed(){
        //Si el usuario a agregado una imágen o un vídeo a la aplicación y luego no guarda se elimina
        if(editando && !uriGuardadas.isEmpty()){
            for(Uri uri : uriGuardadas){
                this.getContentResolver().delete(uri, null, null);
            }
        }
        finish();
    }

    /**
     * Creación del menú.
     *
     * @param menu Menú a rellenar
     * @return Verdadero si se va a mostrar el menú
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu_completada, menu);
        return super.onCreateOptionsMenu(menu);
    }

    /**
     * Método para controlar la pulsación de los items del menú
     *
     * @param item Opción pulsada en el menú
     * @return True si la opción está registrada
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        int caso;
        //Si es null es que se ha llamado desde el onCreate para que mantenga la edición
        if(item == null) {
            caso = R.id.editarCompletada;
            editando = !editando;
        }
        else
            caso = item.getItemId();
        switch (caso){
            case R.id.editarCompletada:
                if(editando){
                    try {
                        String tipoRespuesta = tarea.getString(Auxiliar.tipoRespuesta);
                        //Se comprueba si la respuesta está vacía
                        if (textoUsuario.getVisibility() != View.GONE &&
                                (tipoRespuesta.equals(Auxiliar.tipoPreguntaCorta)
                                        || tipoRespuesta.equals(Auxiliar.tipoPreguntaLarga)
                                        || tipoRespuesta.equals(Auxiliar.tipoPreguntaImagen)
                                ) && textoUsuario.getText().toString().isEmpty()) {
                            textoUsuario.setError(getString(R.string.respuestaVacia));
                        } else {
                            //Se comprueba si se tiene algún recurso multimedia
                            if((tipoRespuesta.equals(Auxiliar.tipoPreguntaImagen)
                                    || tipoRespuesta.equals(Auxiliar.tipoImagen)
                                    || tipoRespuesta.equals(Auxiliar.tipoImagenMultiple)
                                    || tipoRespuesta.equals(Auxiliar.tipoVideo))
                                    && (listaURI.size() == 0)){
                                Toast.makeText(this, getString(R.string.agregarContenido),
                                        Toast.LENGTH_SHORT).show();
                            }else {
                                editando = false;
                                if(item!=null)
                                    item.setIcon(R.drawable.ic_edit_black_24dp);
                                bloqueaYGuarda();
                            }
                        }
                    }catch (JSONException e){
                        e.printStackTrace();
                    }
                }
                else{
                    editando = true;
                    if(item!=null)
                        item.setIcon(R.drawable.ic_save_white_24dp);
                    desbloqueaCampos();
                }
                return true;
            case R.id.publicarCompletada:
                Toast.makeText(this, Login.firebaseAuth.getUid(), Toast.LENGTH_SHORT).show();
                mandaTweet();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void mandaTweet(){
        try {
            //Compruebo que tiene instalado el cliente oficial de twitter antes de seguir
            getPackageManager().getPackageInfo("com.twitter.android", PackageManager.GET_ACTIVITIES);
            Intent intent;
            switch (tarea.getString(Auxiliar.tipoRespuesta)){
                case Auxiliar.tipoPreguntaCorta:
                case Auxiliar.tipoPreguntaLarga:
                case Auxiliar.tipoSinRespuesta:
                    intent = new Intent(Intent.ACTION_SEND);
                    intent.putExtra(Intent.EXTRA_TEXT, contenidoTextoTweet());
                    intent.setType("text/plain");
                    break;
                case Auxiliar.tipoPreguntaImagen:
                case Auxiliar.tipoImagen:
                case Auxiliar.tipoImagenMultiple:
                    if(listaURI != null && listaURI.size() > 0)
                        intent = new Intent(Intent.ACTION_SEND_MULTIPLE);
                    else
                        intent = new Intent(Intent.ACTION_SEND);
                    intent.putExtra(Intent.EXTRA_TEXT, contenidoTextoTweet());
                    intent.setType("text/plain");
                    if(tarea.getString(Auxiliar.tipoRespuesta).equals(Auxiliar.tipoVideo)){
                        if(listaURI != null && !listaURI.isEmpty()){
                            ArrayList<Uri> uris = new ArrayList<>();
                            for(String s : listaURI){
                                uris.add(Uri.parse(s));
                            }
                            intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
                            intent.setType("video/*");
                        }
                    }else{
                        if(listaURI != null && !listaURI.isEmpty()){
                            ArrayList<Uri> uris = new ArrayList<>();
                            for(String s : listaURI){
                                uris.add(Uri.parse(s));
                            }
                            intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
                            intent.setType("image/*");
                        }
                    }
                    break;
                case Auxiliar.tipoVideo:
                    intent = new Intent(Intent.ACTION_SEND);
                    intent.putExtra(Intent.EXTRA_TEXT, contenidoTextoTweet());
                    intent.setType("text/plain");
                    if(listaURI != null && !listaURI.isEmpty()){
                        intent.putExtra(Intent.EXTRA_STREAM, Uri.parse(listaURI.get(0)));
                        intent.setType("video/*");
                    }
                    break;
                default:
                    intent = null;
                    break;
            }

            if(intent != null) {
                intent.setPackage("com.twitter.android");
                startActivity(intent);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (PackageManager.NameNotFoundException e) {
            Toast.makeText(this, getString(R.string.instalaTwitter), Toast.LENGTH_SHORT).show();
        }
    }

    private String contenidoTextoTweet(){
        String texto = null;

        try {
            texto = tarea.getString(Auxiliar.titulo);
            if(!textoUsuario.getText().toString().equals("")){
                texto = texto.concat(String.format("\n%s", textoUsuario.getText().toString()));
                texto = (texto.length() > 262)?texto.substring(0, 259)+"...":texto;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        texto=(texto==null)?String.format("%s", getString(R.string.hashtag)):texto.concat(String.format("\n%s", getString(R.string.hashtag)));
        return texto;
    }

    /**
     * Método para bloquar la interfaz gráfica y almacenar las modificaciones realizadas por el
     * usuario.
     */
    private void bloqueaYGuarda() {

        JSONArray respuestas;
        try {
            respuestas = tarea.getJSONArray(Auxiliar.respuestas);
        }catch (Exception e) {
            respuestas = new JSONArray();
        }
        try{
            JSONObject respuesta;
            boolean teniaRespuesta = false;
            int i;
            for (i = 0; i < respuestas.length(); i++) {
                respuesta = respuestas.getJSONObject(i);
                if (respuesta.getString(Auxiliar.tipoRespuesta).equals(Auxiliar.texto)) {
                    respuesta.put(Auxiliar.respuestaRespuesta, textoUsuario.getText().toString());
                    respuestas.put(i, respuesta);
                    teniaRespuesta = true;
                    break;
                }
            }
            if (!teniaRespuesta) {
                respuesta = new JSONObject();
                respuesta.put(Auxiliar.tipoRespuesta, Auxiliar.texto);
                respuesta.put(Auxiliar.respuestaRespuesta, textoUsuario.getText().toString());
                respuestas.put(respuesta);
            }
            tarea.put(Auxiliar.respuestas, respuestas);
        }catch (Exception e){
            e.printStackTrace();
        }
        if(textoUsuario.getText().toString().isEmpty()){
            textoUsuario.setVisibility(View.GONE);
        }
        if(textoUsuario.getVisibility() != View.GONE){
            textoUsuario.setEnabled(false);
        }
        try {
            tarea.put(Auxiliar.rating, ratingBar.getRating());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        ratingBar.setIsIndicator(true);
        try {
            tarea.put(Auxiliar.fechaUltimaModificacion, Auxiliar.horaFechaActual());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        btAgregar.setVisibility(View.GONE);

        if(listaURI.size() > 0){//Lista de imágenes o vídeos
            try{
                ImagenesCamara ic;
                List<ImagenesCamara> lista = new ArrayList<>();
                switch (tarea.getString(Auxiliar.tipoRespuesta)){
                    case Auxiliar.tipoImagen:
                    case Auxiliar.tipoImagenMultiple:
                    case Auxiliar.tipoPreguntaImagen:
                        for(int i = 0; i< imagenesCamaras.size(); i++){
                            ic = imagenesCamaras.get(i);
                            ic.setVisible(View.GONE);
                            lista.add(ic);
                        }
                        updateRV(lista);
                        break;
                    case Auxiliar.tipoVideo:
                        for(int i = 0; i< imagenesCamaras.size(); i++){
                            ic = imagenesCamaras.get(i);
                            ic.setVisible(View.GONE);
                            lista.add(ic);
                        }
                        updateRVVideo(lista);
                        break;
                    default:
                        break;
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }

        if(!uriEliminar.isEmpty()){
            for (Uri uri : uriEliminar){
                this.getContentResolver().delete(uri, null, null);
            }
        }
        try {
            tarea.put(Auxiliar.fechaUltimaModificacion, Auxiliar.horaFechaActual());
            PersistenciaDatos.reemplazaJSON(
                    getApplication(),
                    PersistenciaDatos.ficheroCompletadas,
                    tarea);
        }catch (JSONException e){
            e.printStackTrace();
        }
        try {
            Bundle bundle = new Bundle();
            bundle.putString("user", Login.firebaseAuth.getUid());
            bundle.putString("idTarea", tarea.getString(Auxiliar.id));
            Login.firebaseAnalytics.logEvent("tareaModificada", bundle);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * Método para desbloquear la interfaz gráfica para que el usuario pueda llevar a cabo las
     * modificaciones que considere.
     */
    private void desbloqueaCampos(){
        if(textoUsuario.getVisibility() == View.GONE){
            textoUsuario.setVisibility(View.VISIBLE);
        }

        textoUsuario.setEnabled(true);

        ratingBar.setIsIndicator(false);

        try {
            String tipo = tarea.getString(Auxiliar.tipoRespuesta);
            switch (tipo){
                case Auxiliar.tipoImagen:
                case Auxiliar.tipoImagenMultiple:
                case Auxiliar.tipoPreguntaImagen:
                case Auxiliar.tipoVideo:
                    btAgregar.setVisibility(View.VISIBLE);
                    break;
                default:
                    break;
            }

            if (listaURI.size() >= 1) {//Lista de imágenes o vídeos
                ImagenesCamara ic;
                List<ImagenesCamara> lista = new ArrayList<>();
                for (int i = 0; i < imagenesCamaras.size(); i++) {
                    ic = imagenesCamaras.get(i);
                    ic.setVisible(View.VISIBLE);
                    lista.add(ic);
                }
                    switch (tipo) {
                        case Auxiliar.tipoImagen:
                        case Auxiliar.tipoImagenMultiple:
                        case Auxiliar.tipoPreguntaImagen:
                            updateRV(lista);
                            break;
                        case Auxiliar.tipoVideo:
                            updateRVVideo(lista);
                            break;
                        default:
                            break;
                    }
            }
        }catch(JSONException e){
            e.printStackTrace();
        }
    }

    /**
     * Método para actualizar la lista de imágenes del contenedor.
     *
     * @param lista Nueva lista que reemplazará la antigua
     */
    public void updateRV(final List<ImagenesCamara> lista){
        imagenesCamaras = lista;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                adaptadorImagenesCompletadas.notifyDataSetChanged();
            }
        });
    }

    /**
     * Método para actualizar la lista de vídeos del contenedor.
     *
     * @param lista Nueva lista de vídeos que reemplazaran a los anteriores.
     */
    public void updateRVVideo(final List<ImagenesCamara> lista){
        imagenesCamaras = lista;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                adaptadorVideosCompletados.notifyDataSetChanged();
            }
        });
    }


    /**
     * Método que será llamado cuando se pulse a un elemento del contendor cuando esté conteniendo
     * imágenes
     * @param view Vista pulsada
     * @param position Posición que ocupa en el contenedor
     */
    @Override
    public void onItemClick(View view, final int position) {
        if(editando) {
            if(listaURI.size() <= 1){ //El usuario no puede eliminar todos los recursos gráficos
                Toast.makeText(this,
                        getString(R.string.agregaImagenAntesBorrar), Toast.LENGTH_SHORT).show();
            }else{
                Uri uri = imagenesCamaras.get(position).getDireccion();
                uriGuardadas.remove(uri);
                try{
                    JSONArray respuestas = tarea.getJSONArray(Auxiliar.respuestas);
                    JSONObject respuesta;
                    for(int i = 0; i < respuestas.length(); i++){
                        respuesta = respuestas.getJSONObject(i);
                        if(respuesta.getString(Auxiliar.tipoRespuesta).equals(Auxiliar.uri)
                                && respuesta.getString(Auxiliar.respuestaRespuesta).equals(uri.toString())){
                            uriEliminar.add(uri);
                            respuestas.remove(i);
                            tarea.put(Auxiliar.respuestas, respuestas);
                            listaURI.remove(position);
                            imagenesCamaras.remove(position);

                            adaptadorImagenesCompletadas = new AdaptadorImagenesCompletadas(
                                    this, imagenesCamaras);
                            adaptadorImagenesCompletadas.setClickListener(this);
                            recyclerView.invalidate();
                            recyclerView.setAdapter(adaptadorImagenesCompletadas);
                            recyclerView.scrollToPosition(((position == 0)?position-1:position));
                            break;
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }else {//Se salta a la siguiente actividad para ver la imágen en pantalla completa
            Intent intent = new Intent(this, ImagenCompleta.class);
            intent.putExtra("IMAGENCOMPLETA", listaURI.get(position));
            posicion = position;
            startActivity(intent);
        }
    }

    /**
     * Método que será llamado cuando se pulsa un elemento del contendor cuando almacena vídeos
     *
     * @param view Vista del ítem pulsado
     * @param position Posición que ocupa en el contenedor
     */
    @Override
    public void onItemClickVideo(View view, final int position){
        if(editando) {
            if(listaURI.size() <= 1){
                Toast.makeText(this, getString(R.string.agregaVideoAntesBorrar),
                        Toast.LENGTH_SHORT).show();
            }else{
                Uri uri = imagenesCamaras.get(position).getDireccion();
                uriGuardadas.remove(uri);
                try{
                    JSONArray respuestas = tarea.getJSONArray(Auxiliar.respuestas);
                    JSONObject respuesta;
                    for(int i = 0; i < respuestas.length(); i++){
                        respuesta = respuestas.getJSONObject(i);
                        if(respuesta.getString(Auxiliar.tipoRespuesta).equals(Auxiliar.uri) &&
                                respuesta.getString(Auxiliar.respuestaRespuesta).equals(uri.toString())){
                            uriEliminar.add(uri);
                            respuestas.remove(i);
                            tarea.put(Auxiliar.respuestas, respuestas);
                            listaURI.remove(position);
                            imagenesCamaras.remove(position);
                            adaptadorVideosCompletados = new AdaptadorVideosCompletados(
                                    this, imagenesCamaras);
                            adaptadorVideosCompletados.setClickListenerVideo(this);
                            recyclerView.invalidate();
                            recyclerView.setAdapter(adaptadorVideosCompletados);
                            recyclerView.scrollToPosition(((position == 0)?position-1:position));
                            break;
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }else {
            if (((VideoView) view).isPlaying()) {
                ((VideoView) view).pause();
            } else {
                ((VideoView) view).start();
            }
        }
    }


    /**
     * Método utilizado para antender las pulsaciones del usuario en otros objetos que no sean del
     * contenedor
     * @param v Vista pulsada
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btAgregarCompletada:
                try {
                    switch (tarea.getString(Auxiliar.tipoRespuesta)){
                        case Auxiliar.tipoImagen:
                        case Auxiliar.tipoImagenMultiple:
                        case Auxiliar.tipoPreguntaImagen:
                        case Auxiliar.tipoVideo:
                            Intent intent = new Intent();
                            intent.setAction(Intent.ACTION_GET_CONTENT);
                            if(tarea.getString(Auxiliar.tipoRespuesta).equals(Auxiliar.tipoVideo)){
                                intent.setType("video/*");
                            }else{
                                intent.setType("image/*");
                            }
                            startActivityForResult(
                                    Intent.createChooser(intent, ""),
                                    5000);
                            break;
                        default:
                            break;
                    }
                }catch (JSONException e){
                    e.printStackTrace();
                }
                break;
            default:
                break;
        }
    }

    /**
     * Método para responder cuando se vuelve de una actividad. En esta clase se utiliza para
     * atender a los eventos de vuelta de la galería.
     *
     * @param codigo Código del evento
     * @param resultado Resultado del evento
     * @param datos Datos que devuelve el evento
     */
    @Override
    public void onActivityResult(int codigo, int resultado, Intent datos) {
        super.onActivityResult(codigo, resultado, datos);
        switch (codigo) {
            case 5000://IMAGEN o VIDEO
                if(resultado == -1){
                    try {//-1 si que tiene contenido; //0 no tiene contenido
                        String tipo = tarea.getString(Auxiliar.tipoRespuesta);
                        File copia;
                        if(tipo.equals(Auxiliar.tipoVideo))
                            copia = Auxiliar.createFile(3, this);
                        else
                            copia = Auxiliar.createFile(0, this);

                        Uri uri = FileProvider.getUriForFile(this,
                                getString(R.string.fileProvider), copia);
                        uriGuardadas.add(uri);
                        copiar(getContentResolver().openInputStream(Objects.requireNonNull(datos.getData())),
                                new FileOutputStream(copia));
                        listaURI.add(uri.toString());
                        JSONArray respuestas = tarea.getJSONArray(Auxiliar.respuestas);
                        JSONObject nuevaRespuesta = new JSONObject();
                        nuevaRespuesta.put(Auxiliar.posicionRespuesta, respuestas.length());
                        nuevaRespuesta.put(Auxiliar.respuestaRespuesta, uri.toString());
                        nuevaRespuesta.put(Auxiliar.tipoRespuesta, Auxiliar.uri);
                        respuestas.put(nuevaRespuesta);
                        tarea.put(Auxiliar.respuestas, respuestas);
                        imagenesCamaras.add(new ImagenesCamara(uri, View.VISIBLE));
                        if(recyclerView.getVisibility() == View.GONE)
                            recyclerView.setVisibility(View.VISIBLE);
                        if(tipo.equals(Auxiliar.tipoVideo)){
                            adaptadorVideosCompletados = new AdaptadorVideosCompletados(
                                    this, imagenesCamaras);
                            adaptadorVideosCompletados.setClickListenerVideo(this);
                            recyclerView.invalidate();
                            recyclerView.setAdapter(adaptadorVideosCompletados);
                        }else{
                            adaptadorImagenesCompletadas = new AdaptadorImagenesCompletadas(
                                    this, imagenesCamaras);
                            adaptadorImagenesCompletadas.setClickListener(this);
                            recyclerView.invalidate();
                            recyclerView.setAdapter(adaptadorImagenesCompletadas);
                        }
                        recyclerView.scrollToPosition(imagenesCamaras.size() - 1);
                    }catch (JSONException | IOException e){
                        e.printStackTrace();
                    }
                }
                break;
            default:
                break;
        }
    }

    /**
     * Método para copiar un elemento desde una ubicación pública a la interna de la aplicación.
     *
     * @param original Elemento en el directorio público
     * @param copia Nuevo elemento
     * @throws IOException Lanzará una exceción cuando alguno de los elementos no exista o sea nulo.
     */
    private void copiar(InputStream original, OutputStream copia) throws IOException{
        byte[] bufer = new byte[1024];
        int lee;
        while((lee=original.read(bufer)) != -1){
            copia.write(bufer, 0, lee);
        }
        original.close();
        copia.close();
    }

    /**
     * Subclase para controlar los items del contenedor
     */
    public static class ImagenesCamara {
        /** Identificador único del objeto */
        private Uri direccion;
        /** Visibilidad de la imagen de borrado */
        private int visible;

        /**
         * Contructor de la clase.
         *
         * @param direccion Identificador único del recurso
         * @param visible Visibilidad del borrado del recurso
         */
        ImagenesCamara(Uri direccion, int visible){
            this.direccion = direccion;
            this.visible = visible;
        }

        /**
         * Constructor de la clase.
         * @param direccion Identificador único del recurso
         * @param visible Visibilidad del borrado del recurso
         */
        ImagenesCamara(String direccion, int visible){
            this(Uri.parse(direccion), visible);
        }

        /**
         * Método para obtener el identificador único del recurso
         *
         * @return Identificador único del recurso
         */
        public Uri getDireccion(){
            return direccion;
        }

        /**
         * Método para saber si el borrado del recurso es visible o no
         *
         * @return Estado de la visibilidad del recurso
         */
        public int getVisible(){
            return visible;
        }

        /**
         * Método para establecer la visibilidad del recurso.
         *
         * @param codigo Nueva visibilidad
         */
        public void setVisible(int codigo){
            visible = codigo;
        }
    }

    /**
     * Se almacena el estado cuando se produce una rotación del terminal
     * @param b Bundle donde se almacena el estado
     */
    @Override
    public void onSaveInstanceState(Bundle b) {
        super.onSaveInstanceState(b);
        b.putBoolean("EDITANDO", editando);
        b.putInt("POSICION", posicion);
    }
}
