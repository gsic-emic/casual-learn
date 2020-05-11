package es.uva.gsic.adolfinstro;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.FileUtils;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import es.uva.gsic.adolfinstro.auxiliar.AdaptadorImagenesCompletadas;
import es.uva.gsic.adolfinstro.auxiliar.Auxiliar;
import es.uva.gsic.adolfinstro.persistencia.PersistenciaDatos;

public class Completadas extends AppCompatActivity implements AdaptadorImagenesCompletadas.ItemClickListener, View.OnClickListener{

    String idTarea;

    TextView enunciado;
    EditText textoUsuario;
    RatingBar ratingBar;
    RecyclerView recyclerView;
    ImageView imageView, borraImagen, borraVideo;
    VideoView videoView;
    boolean editando=false;
    JSONObject tarea;
    List<String> listaURI;
    List<ImagenesCamara> imagenesCamaras;
    AdaptadorImagenesCompletadas adaptadorImagenesCompletadas;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_completadas);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        idTarea = getIntent().getExtras().getString(Auxiliar.id);
        tarea = PersistenciaDatos.recuperaTarea(getApplication(), PersistenciaDatos.ficheroCompletadas, idTarea);

        enunciado = findViewById(R.id.tvDescripcionCompletada);
        ratingBar = findViewById(R.id.rbPuntuacionCompletada);
        textoUsuario = findViewById(R.id.etRespuestaTextualCompletada);

        borraImagen = findViewById(R.id.ivBorrarImagen);
        borraVideo = findViewById(R.id.ivBorrarVideo);

        try {
            enunciado.setText(tarea.getString(Auxiliar.recursoAsociadoTexto));

            try {
                ratingBar.setRating((float) tarea.getDouble(Auxiliar.rating));
            }catch (Exception e){
                e.printStackTrace();
            }

            JSONArray respuestas = tarea.getJSONArray(Auxiliar.respuestas);
            JSONObject respuesta;
            listaURI = new ArrayList<>();
            for(int i = 0; i < respuestas.length(); i++){
                respuesta = respuestas.getJSONObject(i);
                if(respuesta.getString(Auxiliar.tipoRespuesta).equals(Auxiliar.texto)){
                    if(!respuesta.getString(Auxiliar.respuestaRespuesta).equals("")) {
                        textoUsuario.setText(respuesta.getString(Auxiliar.respuestaRespuesta));
                        textoUsuario.setVisibility(View.VISIBLE);
                    }
                }
                else{//URI de video o fotos
                    listaURI.add(respuesta.getString(Auxiliar.respuestaRespuesta));
                }
            }

            if(!listaURI.isEmpty()){//Hay que mostrar fotos o vídeos
                switch (tarea.getString(Auxiliar.tipoRespuesta)){
                    case Auxiliar.tipoImagen:
                    case Auxiliar.tipoImagenMultiple:
                    case Auxiliar.tipoPreguntaImagen:
                        if(listaURI.size()>1){
                            recyclerView = findViewById(R.id.rvImagenesAlumno);
                            recyclerView.setVisibility(View.VISIBLE);
                            recyclerView.setHasFixedSize(true);
                            RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
                            recyclerView.setLayoutManager(layoutManager);
                            imagenesCamaras = new ArrayList<>();
                            for(String s: listaURI){
                                imagenesCamaras.add(new ImagenesCamara(s, View.GONE));
                            }
                            adaptadorImagenesCompletadas = new AdaptadorImagenesCompletadas(this, imagenesCamaras);
                            adaptadorImagenesCompletadas.setClickListener(this);
                            recyclerView.setAdapter(adaptadorImagenesCompletadas);
                        }else{
                            imageView = findViewById(R.id.ivImagenAlumno);
                            imageView.setVisibility(View.VISIBLE);
                            Picasso.get().load(listaURI.get(0)).into(imageView);
                            imageView.setAdjustViewBounds(true);
                            imageView.setOnClickListener(this);
                        }
                        break;
                    case Auxiliar.tipoVideo:
                        if(listaURI.size()==1){
                            videoView = findViewById(R.id.vvVideoAlumno);
                            videoView.setMinimumHeight(300);
                            videoView.setVisibility(View.VISIBLE);
                            videoView.setVideoURI(Uri.parse(listaURI.get(0)));
                            videoView.setOnClickListener(this);
                        }
                        break;
                    default:
                        break;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }

    @Override
    public void onBackPressed(){
        super.onBackPressed();
        finish();
    }

    /**
     * Creación del menú en el layout
     * @param menu Menú a rellenar
     * @return Verdadero si se va a mostrar el menú
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu_completada, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()){
            case R.id.editarCompletada:
                if(editando){
                    try {
                        if (textoUsuario.getVisibility() != View.GONE &&
                                (tarea.getString(Auxiliar.tipoRespuesta).equals(Auxiliar.tipoPreguntaCorta)
                                        || tarea.getString(Auxiliar.tipoRespuesta).equals(Auxiliar.tipoPreguntaLarga)
                                        || tarea.getString(Auxiliar.tipoRespuesta).equals(Auxiliar.tipoPreguntaImagen)
                                ) && textoUsuario.getText().toString().isEmpty()) {
                            textoUsuario.setError(getString(R.string.respuestaVacia));
                        } else {
                            editando = false;
                            item.setIcon(R.drawable.ic_edit_black_24dp);
                            bloqueaYGuarda();
                        }
                    }catch (JSONException e){
                        e.printStackTrace();
                    }
                }
                else{
                    editando = true;
                    item.setIcon(R.drawable.ic_save_white_24dp);
                    desbloqueaCampos();
                }
                return true;
            case R.id.publicarCompletada:

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void bloqueaYGuarda() {

        JSONArray respuestas;
        borraVideo.setVisibility(View.GONE);
        borraImagen.setVisibility(View.GONE);

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
        if(listaURI.size() > 0){//Lista de imágenes o vídeos
            try{
                switch (tarea.getString(Auxiliar.tipoRespuesta)){
                    case Auxiliar.tipoImagen:
                    case Auxiliar.tipoImagenMultiple:
                    case Auxiliar.tipoPreguntaImagen:
                        ImagenesCamara ic;
                        List<ImagenesCamara> lista = new ArrayList<>();
                        for(int i = 0; i< imagenesCamaras.size(); i++){
                            ic = imagenesCamaras.get(i);
                            ic.setVisible(View.GONE);
                            lista.add(ic);
                        }
                        updateRV(lista);
                        break;
                    case Auxiliar.tipoVideo:

                        break;
                    default:
                        break;
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }else{
            try{
                switch (tarea.getString(Auxiliar.tipoRespuesta)){
                    case Auxiliar.tipoImagen:
                    case Auxiliar.tipoImagenMultiple:
                    case Auxiliar.tipoPreguntaImagen:
                        borraImagen.setVisibility(View.GONE);
                        borraImagen.setOnClickListener(this);
                        break;
                    case Auxiliar.tipoVideo:
                        borraVideo.setVisibility(View.GONE);
                        borraVideo.setOnClickListener(this);
                        break;
                    default:
                        break;
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        PersistenciaDatos.reemplazaJSON(getApplication(), PersistenciaDatos.ficheroCompletadas, tarea);
    }

    private void desbloqueaCampos(){
        if(textoUsuario.getVisibility() == View.GONE){
            textoUsuario.setVisibility(View.VISIBLE);
        }
        if(videoView != null && videoView.getVisibility() == View.VISIBLE){
            if(videoView.isPlaying()){
                videoView.stopPlayback();
                videoView.resume();
            }
        }
        textoUsuario.setEnabled(true);

        ratingBar.setIsIndicator(false);

        if(listaURI.size() > 1){//Lista de imágenes o vídeos
            try{
                switch (tarea.getString(Auxiliar.tipoRespuesta)){
                    case Auxiliar.tipoImagen:
                    case Auxiliar.tipoImagenMultiple:
                    case Auxiliar.tipoPreguntaImagen:
                        ImagenesCamara ic;
                        List<ImagenesCamara> lista = new ArrayList<>();
                        for(int i = 0; i< imagenesCamaras.size(); i++){
                            ic = imagenesCamaras.get(i);
                            ic.setVisible(View.VISIBLE);
                            lista.add(ic);
                        }
                        updateRV(lista);
                        break;
                    case Auxiliar.tipoVideo:

                        break;
                    default:
                        break;
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }else{
            try{
                switch (tarea.getString(Auxiliar.tipoRespuesta)){
                    case Auxiliar.tipoImagen:
                    case Auxiliar.tipoImagenMultiple:
                    case Auxiliar.tipoPreguntaImagen:
                        borraImagen.setVisibility(View.VISIBLE);
                        borraImagen.setOnClickListener(this);
                        break;
                    case Auxiliar.tipoVideo:
                        borraVideo.setVisibility(View.VISIBLE);
                        borraVideo.setOnClickListener(this);
                        break;
                    default:
                        break;
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    public void updateRV(final List<ImagenesCamara> lista){
        imagenesCamaras = lista;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                adaptadorImagenesCompletadas.notifyItemRangeChanged(0, lista.size());
            }
        });
    }


    @Override
    public void onItemClick(View view, int position) {
        if(editando) {
            Toast.makeText(this, "Has tocado la opción de borrar", Toast.LENGTH_SHORT).show();
        }else {
            Intent intent = new Intent(this, ImagenCompleta.class);
            intent.putExtra("IMAGENCOMPLETA", listaURI.get(position));
            startActivity(intent);
        }
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.ivImagenAlumno:
                onItemClick(v, 0);
                break;

            case R.id.vvVideoAlumno:
                if(!editando) {
                    if (videoView.isPlaying()) {
                        videoView.pause();
                    } else {
                        videoView.start();
                    }
                }
                break;
            case R.id.ivBorrarImagen:
            case R.id.ivBorrarMedia:
                try {
                    JSONArray respuestas = tarea.getJSONArray(Auxiliar.respuestas);
                    JSONObject respuesta;
                    for(int i = 0; i < respuestas.length(); i++){
                        respuesta = respuestas.getJSONObject(i);
                        if(!respuesta.getString(Auxiliar.tipoRespuesta).equals(Auxiliar.texto)) {
                            if(getContentResolver().delete(Uri.parse(respuesta.getString(Auxiliar.respuestaRespuesta)), null, null)>0){
                                respuestas.remove(i);
                                if(v.getId() == R.id.ivBorrarImagen)
                                    imageView.setVisibility(View.GONE);
                                else
                                    videoView.setVisibility(View.GONE);
                            }
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                break;
            default:
                break;
        }
    }

    public class ImagenesCamara {
        Uri direccion;
        int visible;
        public ImagenesCamara(Uri direccion, int visible){
            this.direccion = direccion;
            this.visible = visible;
        }

        public ImagenesCamara(String direccion, int visible){
            this.direccion = Uri.parse(direccion);
            this.visible = visible;
        }

        public Uri getDireccion(){
            return direccion;
        }
        public int getVisible(){
            return visible;
        }
        public void setVisible(int codigo){
            visible = codigo;
        }
    }
}
