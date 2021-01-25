package es.uva.gsic.adolfinstro;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import es.uva.gsic.adolfinstro.auxiliar.AdaptadorLista;
import es.uva.gsic.adolfinstro.auxiliar.AdaptadorListaContextos;
import es.uva.gsic.adolfinstro.auxiliar.Auxiliar;
import es.uva.gsic.adolfinstro.auxiliar.ContextoLista;
import es.uva.gsic.adolfinstro.persistencia.PersistenciaDatos;

public class ListaContextos extends AppCompatActivity
    implements AdaptadorListaContextos.ItemClickListenerContexto {

    private TextView sinContextos;
    private RecyclerView contenedor;
    private int posicionPulsada;
    private String idUsuario;

    private AdaptadorListaContextos adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lista_contextos);

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        sinContextos = findViewById(R.id.tvSinContextosLista);
        contenedor = findViewById(R.id.rvContextosLista);

        setTitle(getResources().getString(R.string.lugares_notificados));

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
    }

    @Override
    public void onResume() {
        super.onResume();
        cargaContextos();
        if(posicionPulsada != -1)
            contenedor.scrollToPosition(posicionPulsada);
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return false;
    }

    @Override
    public void onSaveInstanceState(@NotNull Bundle b) {
        super.onSaveInstanceState(b);
        b.putInt("POSICION", posicionPulsada);
    }

    private void cargaContextos(){
        if(idUsuario != null) {
            JSONArray contextosNotificados = PersistenciaDatos.leeTareasUsuario(
                    getApplication(),
                    PersistenciaDatos.ficheroContextosNotificados,
                    idUsuario);

            if (contextosNotificados.length() > 0) {
                contenedorVisible(true);
                RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
                contenedor.setLayoutManager(layoutManager);
                JSONObject contexto;
                List<ContextoLista> contextos = new ArrayList<>();
                try {
                    for (int i = 0; i < contextosNotificados.length(); i++) {
                        contexto = contextosNotificados.getJSONObject(i);
                        contextos.add(new ContextoLista(
                                contexto.getString(Auxiliar.contexto),
                                contexto.getString(Auxiliar.label),
                                contexto.getString(Auxiliar.fechaNotificiacion)
                        ));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (!contextos.isEmpty()) {
                    adapter = new AdaptadorListaContextos(this, contextos);
                    adapter.setClickListener(this);
                    contenedor.setAdapter(adapter);
                }
            } else {
                contenedorVisible(false);
            }
        } else{
            contenedorVisible(false);
        }
    }

    private void contenedorVisible(boolean opcion){
        if(opcion){
            contenedor.setVisibility(View.VISIBLE);
            contenedor.setHasFixedSize(true);
            sinContextos.setVisibility(View.GONE);
        } else {
            contenedor.setVisibility(View.GONE);
            sinContextos.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onItemClick(View view, int position) {
        String idContexto = adapter.getIdContexto(position);
        Intent intent = new Intent(this, PuntoInteres.class);
        intent.putExtra(Auxiliar.contexto, idContexto);
        intent.putExtra(Auxiliar.previa, Auxiliar.mapa);
        startActivity(intent);
    }
}