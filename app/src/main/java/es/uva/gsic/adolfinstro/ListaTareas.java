package es.uva.gsic.adolfinstro;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import es.uva.gsic.adolfinstro.auxiliar.AdaptadorLista;
import es.uva.gsic.adolfinstro.auxiliar.Auxiliar;
import es.uva.gsic.adolfinstro.persistencia.PersistenciaDatos;

public class ListaTareas extends AppCompatActivity implements AdaptadorLista.ItemClickListener{

    public class TareasLista{
        public String id, titulo, tipoTarea, fecha;
        TareasLista(String id, String titulo, String tipoTarea, String fecha){
            this.id = id;
            this.titulo = titulo;
            this.tipoTarea = tipoTarea;
            this.fecha = fecha;
        }
    }

    private AdaptadorLista adapter;
    private String peticion;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lista_tareas);
        peticion = getIntent().getExtras().getString("peticion");


        TextView sinTareas = findViewById(R.id.tvSinTareasLista);
        switch (peticion){
            case PersistenciaDatos.ficheroTareasPospuestas:
                setTitle(getString(R.string.tareasPospuestas));
                break;
            case PersistenciaDatos.ficheroTareasRechazadas:
                setTitle(getString(R.string.tareasRechazadas));
                break;
            case PersistenciaDatos.ficheroCompletadas:
                setTitle(getString(R.string.tareasCompletadas));
                break;
        }
        JSONArray arrayTareas = PersistenciaDatos.leeFichero(getApplication(), peticion);

        if(arrayTareas.length() > 0) {
            RecyclerView contenedor = findViewById(R.id.rvTareas);
            contenedor.setVisibility(View.VISIBLE);
            contenedor.setHasFixedSize(true);

            RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
            contenedor.setLayoutManager(layoutManager);
            JSONObject jsonObject;
            List<TareasLista> tareas = new ArrayList<>();
            try {
                for (int i = 0; i < arrayTareas.length(); i++) {
                    jsonObject = arrayTareas.getJSONObject(i);
                    tareas.add(new TareasLista(
                            jsonObject.getString(Auxiliar.id),
                            jsonObject.getString(Auxiliar.titulo),
                            jsonObject.getString(Auxiliar.tipoRespuesta),
                            jsonObject.getString(Auxiliar.fechaUltimaModificacion)));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            adapter = new AdaptadorLista(this, tareas);
            adapter.setClickListener(this);
            contenedor.setAdapter(adapter);
        }else{
            sinTareas.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onItemClick(View view, int position) {
        //Toast.makeText(this, adapter.getItem(position), Toast.LENGTH_SHORT).show();
        String idTarea = adapter.getItem(position);
        JSONObject jTarea;
        switch (peticion) {
            case PersistenciaDatos.ficheroTareasPospuestas:
                try {
                    jTarea = PersistenciaDatos.obtenTarea(getApplication(), PersistenciaDatos.ficheroTareasPospuestas, idTarea);
                    Intent intent = new Intent(this, Preview.class);
                    intent.putExtra(Auxiliar.id, idTarea);
                    intent.putExtra(Auxiliar.tipoRespuesta, jTarea.getString(Auxiliar.tipoRespuesta));
                    intent.putExtra(Auxiliar.recursoAsociadoTexto, jTarea.getString(Auxiliar.recursoAsociadoTexto));
                    String intermedio = null;
                    try{
                        intermedio = jTarea.getString(Auxiliar.recursoImagen);
                    }catch (Exception e){
                        //
                    }
                    intent.putExtra(Auxiliar.recursoImagen, (intermedio.equals("")?null:intermedio));
                    intermedio = null;
                    try{
                        intermedio = jTarea.getString(Auxiliar.recursoImagenBaja);
                    }catch (Exception e){
                        //
                    }
                    intent.putExtra(Auxiliar.recursoImagenBaja, (intermedio.equals("")?null:intermedio));
                        try{
                        intermedio = jTarea.getString(Auxiliar.respuestaEsperada);
                    }catch (Exception e){
                        //
                    }
                    intent.putExtra(Auxiliar.respuestaEsperada, (intermedio.equals("")?null:intermedio));
                    intent.putExtra(Auxiliar.latitud, jTarea.getDouble(Auxiliar.latitud));
                    intent.putExtra(Auxiliar.longitud, jTarea.getDouble(Auxiliar.longitud));
                    intent.putExtra(Auxiliar.titulo, jTarea.getString(Auxiliar.titulo));
                    startActivity(intent);
                    jTarea.put(Auxiliar.fechaUltimaModificacion, Auxiliar.horaFechaActual());
                    PersistenciaDatos.guardaJSON(getApplication(), PersistenciaDatos.ficheroNotificadas, jTarea, Context.MODE_PRIVATE);
                }catch (Exception e){
                    //
                }
                break;
            case PersistenciaDatos.ficheroTareasRechazadas:
                //TODO
                break;
            case PersistenciaDatos.ficheroCompletadas:
                //TODO
                break;
        }

    }
}
