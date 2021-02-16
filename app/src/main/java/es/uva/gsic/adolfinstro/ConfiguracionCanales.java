package es.uva.gsic.adolfinstro;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.CheckBox;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SwitchCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import es.uva.gsic.adolfinstro.auxiliar.AdaptadorListaCanales;
import es.uva.gsic.adolfinstro.auxiliar.Canal;

/**
 * Clase para modtrar al usuario la lista de canales del sistema y permitirle configurar sus suscripciones.
 * También podrá elegir qué marcador asigna a cada canal.
 *
 * @author Pablo
 * @version 20210216
 */
public class ConfiguracionCanales extends AppCompatActivity implements
        View.OnClickListener,
        AdaptadorListaCanales.ItemClickCbCanal,
        AdaptadorListaCanales.ItemClickMarcadorCanal {

    private SwitchCompat scActivado;
    private RecyclerView contenedorLista;
    private Context context;
    private AdaptadorListaCanales adaptador;
    private Dialog dialogoMarcadores;
    private int posicion;
    private int[] listaMarcadores;

    @Override
    public void onCreate (Bundle savedInstance) {
        super.onCreate(savedInstance);
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
        setContentView(R.layout.conf_canales);
        scActivado = findViewById(R.id.scActivarCanales);
        scActivado.setOnClickListener(this);
        contenedorLista = findViewById(R.id.rvCanales);
        context = getApplicationContext();
        dialogoMarcadores = new Dialog(ConfiguracionCanales.this);
        dialogoMarcadores.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialogoMarcadores.setContentView(R.layout.dialogo_selector_marcador);
        dialogoMarcadores.setCancelable(true);
        listaMarcadores = new int[]{R.id.ivNormalSC, R.id.ivEspecial0, R.id.ivEspecial1,
                R.id.ivEspecial2,  R.id.ivEspecial3,  R.id.ivEspecial4};
    }

    private List<Canal> listaCanales;
    @Override
    public void onResume() {
        super.onResume();
        listaCanales = new ArrayList<>();
        listaCanales.add(new Canal("1", "Uno", "Descripción de 1", Canal.obligatorio, false));
        listaCanales.add(new Canal("2", "Dos", "Descripción de 2", Canal.opcional, false));
        listaCanales.add(new Canal("3", "Tres", "Descripción de 3", Canal.opcional, true, 2));
        listaCanales.add(new Canal("1", "Uno", "Descripción de 1", Canal.obligatorio, false));
        listaCanales.add(new Canal("2", "Dos", "Descripción de 2", Canal.opcional, false));
        listaCanales.add(new Canal("3", "Tres", "Descripción de 3", Canal.opcional, true, 2));
        listaCanales.add(new Canal("1", "Uno", "Descripción de 1", Canal.obligatorio, false));
        listaCanales.add(new Canal("2", "Dos", "Descripción de 2", Canal.opcional, false));
        listaCanales.add(new Canal("3", "Tres", "Descripción de 3", Canal.opcional, true, 2));
        listaCanales.add(new Canal("1", "Uno", "Descripción de 1", Canal.obligatorio, false));
        listaCanales.add(new Canal("2", "Dos", "Descripción de 2", Canal.opcional, false));
        listaCanales.add(new Canal("3", "Tres", "Descripción de 3", Canal.opcional, true, 2));
        listaCanales.add(new Canal("1", "Uno", "Descripción de 1", Canal.obligatorio, false));
        listaCanales.add(new Canal("2", "Dos", "Descripción de 2", Canal.opcional, false));
        listaCanales.add(new Canal("3", "Tres", "Descripción de 3", Canal.opcional, true, 2));
        contenedorLista.setHasFixedSize(true);
        contenedorLista.setLayoutManager(new LinearLayoutManager(context));
        adaptador = new AdaptadorListaCanales(context, listaCanales);
        adaptador.setItemClickCbCanal(this);
        adaptador.setItemClickMarcadorCanal(this);
        contenedorLista.setAdapter(adaptador);
        if (scActivado.isChecked()) {//Descarga de canales
            contenedorLista.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.scActivarCanales:
                //Todo Tiene que hacer más cosas como comunicarse con el servidor
                if (scActivado.isChecked())
                    contenedorLista.setVisibility(View.VISIBLE);
                else
                    contenedorLista.setVisibility(View.GONE);
                break;
            case R.id.ivNormalSC:
            case R.id.ivEspecial0:
            case R.id.ivEspecial1:
            case R.id.ivEspecial2:
            case R.id.ivEspecial3:
            case R.id.ivEspecial4:
                dialogoMarcadores.cancel();
                Canal canal = listaCanales.remove(posicion);
                for(int i = 0; i < listaMarcadores.length; i++){
                    if(listaMarcadores[i] == v.getId()){
                        canal.setMarcador(i - 1);
                        break;
                    }
                }
                listaCanales.add(posicion, canal);
                adaptador.actualizarPosicionLista(listaCanales, posicion);
                break;
            default:
                break;
        }
    }

    @Override
    public void onItemClickCb(View view, int position) {
        Canal canal = listaCanales.remove(position);
        canal.setMarcado(((CheckBox) view).isChecked());
        listaCanales.add(position, canal);
        adaptador.actualizarPosicionLista(listaCanales, position);
    }

    @Override
    public void onItemClickMarcador(View view, int position) {
        posicion = position;
        dialogoMarcadores.show();
    }
}
