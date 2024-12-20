package es.uva.gsic.adolfinstro;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import es.uva.gsic.adolfinstro.auxiliar.AdaptadorLista;
import es.uva.gsic.adolfinstro.auxiliar.Auxiliar;
import es.uva.gsic.adolfinstro.auxiliar.TareasLista;
import es.uva.gsic.adolfinstro.persistencia.PersistenciaDatos;

/**
 * Clase que presenta al usuario los distintos tipos de listas de la aplicación.
 *
 * @author Pablo
 * @version 20201005
 */
public class ListaTareas extends AppCompatActivity
        implements AdaptadorLista.ItemClickListener, AdaptadorLista.ItemLongClickLister{

    /** Objeto donde se introducen las tareas de la lista con el formato adecuado */
    private AdaptadorLista adapter;
    /** Tipo de lista que va a representar la clase */
    private String peticion;

    private TextView sinTareas;
    private RecyclerView contenedor;

    private int posicionPulsada;

    private String idUsuario;

    /**
     * Método para crear la vista del usuario. Se obtiene la lista de tareas de la petición y se
     * representa.
     *
     * @param savedInstanceState Bundle
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);

        setContentView(R.layout.activity_lista_tareas);

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        sinTareas = findViewById(R.id.tvSinTareasLista);
        peticion = Objects.requireNonNull(getIntent().getExtras()).getString(Auxiliar.peticion);
        contenedor = findViewById(R.id.rvTareas);

        //Se muestra el título de la lista que se va a mostrar al usuario
        assert peticion != null;
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
            default:
                break;
        }

        try{
            idUsuario = PersistenciaDatos.recuperaTarea(
                    getApplication(), PersistenciaDatos.ficheroUsuario, Auxiliar.id)
                    .getString(Auxiliar.uid);
        }catch (Exception e){
            idUsuario = null;
        }

        if(savedInstanceState != null)
            posicionPulsada = savedInstanceState.getInt("PULSACION");
        else
            posicionPulsada = -1;

        OnBackPressedCallback onBackPressedCallback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                finish();
            }
        };
        getOnBackPressedDispatcher().addCallback(this,onBackPressedCallback);
    }

    /**
     * Método para cargar las tareas dentro del contenedor.
     * Si no existen tarea se le notifica al usuario mediante un mensaje
     */
    private void cargaTareas(){
        if(idUsuario != null){
            JSONArray arrayTareas = PersistenciaDatos.leeTareasUsuario(getApplication(), peticion, idUsuario);
            if(arrayTareas.length() > 0) {
                sinTareas.setVisibility(View.GONE);
                contenedor.setVisibility(View.VISIBLE);
                contenedor.setHasFixedSize(true);

                RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
                contenedor.setLayoutManager(layoutManager);
                JSONObject jsonObject;
                List<TareasLista> tareas = new ArrayList<>();
                try {
                    for (int i = 0; i < arrayTareas.length(); i++) {
                        jsonObject = arrayTareas.getJSONObject(i);
                        float puntuacion;
                        try{
                            puntuacion = jsonObject.getInt(Auxiliar.rating);
                        }catch (Exception e){
                            puntuacion = -1;
                        }
                        tareas.add(new TareasLista(
                                jsonObject.getString(Auxiliar.id),
                                jsonObject.getString(Auxiliar.titulo),
                                Auxiliar.ultimaParte(jsonObject.getString(Auxiliar.tipoRespuesta)),
                                jsonObject.getString(Auxiliar.fechaUltimaModificacion),
                                puntuacion));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                adapter = new AdaptadorLista(this, tareas);
                adapter.setClickListener(this);
                adapter.setLongClickLister(this);
                contenedor.setAdapter(adapter);
            }else{
                //Mensaje si no se extrae ninguna tarea
                contenedor.setVisibility(View.GONE);
                sinTareas.setVisibility(View.VISIBLE);
            }
        }else{
            //Mensaje si no se extrae ninguna tarea
            contenedor.setVisibility(View.GONE);
            sinTareas.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onItemClick(View view, int position) {
        String idTarea = adapter.getId(position);
        JSONObject jTarea;
        switch (peticion) {
            case PersistenciaDatos.ficheroTareasPospuestas:
            case PersistenciaDatos.ficheroTareasRechazadas:
                try {
                    if(peticion.equals(PersistenciaDatos.ficheroTareasPospuestas))
                        jTarea = PersistenciaDatos.obtenTarea(
                                getApplication(),
                                PersistenciaDatos.ficheroTareasPospuestas,
                                idTarea,
                                idUsuario);
                    else
                        jTarea = PersistenciaDatos.obtenTarea(
                                getApplication(),
                                PersistenciaDatos.ficheroTareasRechazadas,
                                idTarea,
                                idUsuario);
                    Intent intent = new Intent(this, Preview.class);
                    intent.putExtra(Auxiliar.id, idTarea);
                    if(peticion.equals(PersistenciaDatos.ficheroTareasPospuestas)){
                        intent.putExtra(Auxiliar.previa, Auxiliar.tareasPospuestas);
                    }else{
                        intent.putExtra(Auxiliar.previa, Auxiliar.tareasRechazadas);
                    }
                    startActivity(intent);
                    PersistenciaDatos.guardaJSON(
                            getApplication(),
                            PersistenciaDatos.ficheroNotificadas,
                            jTarea,
                            Context.MODE_PRIVATE);
                }catch (Exception e){
                    e.printStackTrace();
                }
                break;
            case PersistenciaDatos.ficheroCompletadas:
                Intent intent = new Intent(this, Completadas.class);
                posicionPulsada = position;
                intent.putExtra(Auxiliar.id, idTarea);
                startActivity(intent);
                break;
        }
    }

    @Override
    public void onItemLongClick(View v, int position){
        String mensaje = Auxiliar.textoTipoTarea(this, adapter.getTipo(position));
        if(mensaje != null)
            Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onSupportNavigateUp() {
        getOnBackPressedDispatcher().onBackPressed();
        return false;
    }

    @Override
    public void onResume() {
        super.onResume();
        cargaTareas();
        if(posicionPulsada != -1)
            contenedor.scrollToPosition(posicionPulsada);
    }

    @Override
    public void onSaveInstanceState(@NotNull Bundle b) {
        super.onSaveInstanceState(b);
        b.putInt("POSICION", posicionPulsada);
    }
}