package es.uva.gsic.adolfinstro;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.appcompat.widget.SwitchCompat;
import androidx.constraintlayout.widget.Guideline;
import androidx.core.app.ActivityCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import es.uva.gsic.adolfinstro.auxiliar.AdaptadorListaCanales;
import es.uva.gsic.adolfinstro.auxiliar.Auxiliar;
import es.uva.gsic.adolfinstro.auxiliar.Canal;
import es.uva.gsic.adolfinstro.auxiliar.ColaConexiones;
import es.uva.gsic.adolfinstro.persistencia.PersistenciaDatos;

/**
 * Clase para modtrar al usuario la lista de canales del sistema y permitirle configurar sus suscripciones.
 * También podrá elegir qué marcador asigna a cada canal.
 *
 * @author Pablo
 * @version 20210223
 */
public class ConfiguracionCanales extends AppCompatActivity implements
        /*View.OnClickListener,*/
        AdaptadorListaCanales.ItemClickCbCanal/*,
        AdaptadorListaCanales.ItemClickMarcadorCanal*/ {

    /** Switch para activar o desactivar la característica de canales */
    private SwitchCompat scActivado;
    /** Contenedor donde se agregará la información de cada uno de los canales */
    private RecyclerView contenedorLista;

    private TextView tvObligatorios, tvOpcionales;
    private ImageView ivObligatorios, ivOpcionales;
    /** Contexto */
    private Context context;
    /** Adaptador para controlar el contenido del contenedor */
    private AdaptadorListaCanales adaptador;
    /** Diálogo para seleccionar el marcador del canal */
    private Dialog dialogoMarcadores;
    private int posicion;
    private int[] listaMarcadores;
    /** Idetnificador del usuario */
    private String idUsuario;
    /** Lista con la totalidad de los canales que tiene la aplicación */
    private List<Canal> listaCanales;
    /** Lista con los canales mostrados al buscar por título */
    private List<Canal> listaVariable;
    /** Objeto sobre el que se realizarán las búsquedas de canales por su título */
    private SearchView searchView;

    @Override
    public void onCreate (Bundle savedInstance) {
        super.onCreate(savedInstance);
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
        setContentView(R.layout.conf_canales);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        scActivado = findViewById(R.id.scActivarCanales);
        //scActivado.setOnClickListener(this);
        tvObligatorios = findViewById(R.id.tvObligatoriosCanales);
        tvOpcionales = findViewById(R.id.tvOpcionalesCanales);
        ivObligatorios = findViewById(R.id.ivObligatorioConfCanales);
        ivOpcionales = findViewById(R.id.ivOpcionalConfCanales);
        final Guideline guia = findViewById(R.id.guiaConfCanalesH);
        contenedorLista = findViewById(R.id.rvCanales);
        if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            contenedorLista.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                    switch (newState) {
                        case RecyclerView.SCROLL_STATE_IDLE:
                            if (!recyclerView.canScrollVertically(-1))
                                guia.setGuidelinePercent(0.3f);
                            break;
                        case RecyclerView.SCROLL_STATE_DRAGGING:
                            if (recyclerView.canScrollVertically(1))
                                guia.setGuidelinePercent(0.15f);
                            break;
                        default:
                            break;
                    }
                    super.onScrollStateChanged(recyclerView, newState);
                }
            });
        }
        context = getApplicationContext();
        dialogoMarcadores = new Dialog(ConfiguracionCanales.this);
        dialogoMarcadores.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialogoMarcadores.setContentView(R.layout.dialogo_selector_marcador);
        dialogoMarcadores.setCancelable(true);
        //Los marcadores siempre se van a mostrar en el mismo orden para facilitar su selección
        listaMarcadores = new int[]{R.id.ivNormalSC, R.id.ivEspecial0, R.id.ivEspecial1,
                R.id.ivEspecial2,  R.id.ivEspecial3,  R.id.ivEspecial4};
        //Se obtiene el identificador del usuario ya que se va a utilizar continuamente
        try {
            idUsuario = PersistenciaDatos.recuperaTarea(
                    getApplication(),
                    PersistenciaDatos.ficheroUsuario,
                    Auxiliar.id
            ).getString(Auxiliar.uid);
        }catch (Exception e){
            idUsuario = null;
        }
        if(idUsuario != null) {//No debería pasar ya que solamente se puede acceder a esta pantalla si
            //el usuario está identificado
            try {
                //Recupero la configuración actual para saber si se tiene que activar o no el swich.
                //También se comprueba si la caducidad de caché ha finalizado.
                //Si está activado el sw y la lista de canales no está caducada se muestra directamente.
                JSONObject configuracionActual = PersistenciaDatos.recuperaObjeto(
                        getApplication(),
                        PersistenciaDatos.ficheroListaCanales,
                        Auxiliar.canal,
                        Auxiliar.configuracionActual,
                        idUsuario);
                if (configuracionActual != null
                        && configuracionActual.has(Auxiliar.caracteristica)
                        && configuracionActual.getBoolean(Auxiliar.caracteristica)) {
                    //scActivado.setChecked(true);
                    muestraConfMarcadores();
                    if(configuracionActual.has(Auxiliar.instante)
                            && configuracionActual.getLong(Auxiliar.instante) < System.currentTimeMillis())
                        obtenerListaCanales();
                    else {
                        listaCanales = cargaListaCanales();
                        muestraLista();
                    }
                }
            } catch (Exception e){
                e.printStackTrace();
            }
        } else {
            scActivado.setVisibility(View.GONE);
        }
    }

    /**
     * Método para cargar la lista de canales en el contendor. La lista de canales (del objeto
     * listaCanales) no  puede ser null.
     */
    public void muestraLista(){
        if(listaCanales != null) {
            contenedorLista.setHasFixedSize(true);
            contenedorLista.setLayoutManager(new LinearLayoutManager(context));
            adaptador = new AdaptadorListaCanales(context, listaCanales);
            adaptador.setItemClickCbCanal(this);
            //adaptador.setItemClickMarcadorCanal(this);
            contenedorLista.setAdapter(adaptador);
            contenedorLista.setVisibility(View.VISIBLE);
            invalidateOptionsMenu();
        }
    }

    public void muestraConfMarcadores(){
        scActivado.setVisibility(View.GONE);
        JSONObject configuracionActual = PersistenciaDatos.recuperaObjeto(
                getApplication(),
                PersistenciaDatos.ficheroListaCanales,
                Auxiliar.canal,
                Auxiliar.configuracionActual,
                idUsuario);
        if(configuracionActual != null){
            try {
                if (configuracionActual.has(Canal.obligatorio))
                    ivObligatorios.setImageDrawable(ResourcesCompat.getDrawable(context.getResources(), Auxiliar.obtenIdMarcadores()[configuracionActual.getInt(Canal.obligatorio)], null));
            } catch (Exception e){
                e.printStackTrace();
            }
            try {
                if (configuracionActual.has(Canal.opcional))
                    ivOpcionales.setImageDrawable(ResourcesCompat.getDrawable(context.getResources(), Auxiliar.obtenIdMarcadores()[configuracionActual.getInt(Canal.opcional)], null));
            } catch (Exception e){
                e.printStackTrace();
            }
        }
        tvObligatorios.setVisibility(View.VISIBLE);
        tvOpcionales.setVisibility(View.VISIBLE);
        ivObligatorios.setVisibility(View.VISIBLE);
        ivOpcionales.setVisibility(View.VISIBLE);
    }

    public void ocultaConfMarcadores(){
        scActivado.setVisibility(View.VISIBLE);
        tvObligatorios.setVisibility(View.GONE);
        tvOpcionales.setVisibility(View.GONE);
        ivObligatorios.setVisibility(View.GONE);
        ivOpcionales.setVisibility(View.GONE);
    }

    /**
     * Método para establecer el menú de esta pantalla. Estará compuesto por la barra de búsqueda y
     * la solicitud de descarga de la lista de canales. Solamente se activará si el conmutador de la
     * característica de canales está activo.
     *
     * @param menu Menú
     * @return Devolverá true si hay un menú que pintar y false en caso contrario.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        if(scActivado.getVisibility() == View.GONE) {
            final MenuInflater menuInflater = getMenuInflater();
            menuInflater.inflate(R.menu.menu_canales, menu);
            final MenuItem menuItem = menu.findItem(R.id.busquedaCanal);
            final SearchView searchView = (SearchView) menuItem.getActionView();
            this.searchView = searchView;
            //Se expanda el icono de la búsqueda por nombre al pulsar sobre la lupa. Mismo comportamiento
            //que otras aplicaciones populares como whatsapp
            menuItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    item.expandActionView();
                    searchView.onActionViewExpanded();
                    return true;
                }
            });
            searchView.setQueryHint(context.getResources().getString(R.string.buscaCanales));
            //Pulsar el salto de carro no tiene efecto. Solo se atiende a cambios del contenido textual del
            //cuadro de búsqueda
            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) { return false; }

                @Override
                public boolean onQueryTextChange(String newText) {
                    //Mismo procesado en las dos cadenas de texto
                    newText = StringUtils.stripAccents(newText.trim().toLowerCase());
                    if(newText.length() > 0) {
                        listaVariable = new ArrayList<>(listaCanales);
                        for(Canal canal : listaCanales) {
                            if(!StringUtils.stripAccents(
                                    canal.getTitulo().trim().toLowerCase()).contains(newText)){
                                listaVariable.remove(canal);
                            }
                        }
                        adaptador.actualizaLista(listaVariable);
                    }else{
                        adaptador.actualizaLista(listaCanales);
                        listaVariable = null;
                    }
                    return false;
                }
            });

            //Métodos para que al colapsar y extender la búsqueda se tenga el comportamiento esperado
            menuItem.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
                @Override
                public boolean onMenuItemActionExpand(MenuItem item) {
                    if(searchView.isIconified())
                        searchView.setIconified(false);
                    return true;
                }

                @Override
                public boolean onMenuItemActionCollapse(MenuItem item) {
                    InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    if(getCurrentFocus() != null && inputMethodManager.isActive(getCurrentFocus()))
                        inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
                    if(!searchView.isIconified())
                        searchView.setIconified(true);
                    searchView.clearFocus();//Evito que se vuelva a mostrar el teclado
                    return true;
                }
            });
        }
        return (scActivado.getVisibility() == View.GONE);
    }

    /**
     * Método para atencder a las pulsaciones en ítems del menú
     *
     * @param menuItem Ítem pulsado
     * @return Verdadero si se ha pulsado una opción esperada
     */
    @Override
    public boolean onOptionsItemSelected(@NotNull MenuItem menuItem){
        // Los métodos del icono de búsqueda se
        //configuran en el onCreteOptionsMenu(Menu menu)
        switch (menuItem.getItemId()) {
            case R.id.actualizarCanales:
                obtenerListaCanales();
                return true;
            case R.id.descactivaCanales:
                desactivarCanales();
                contenedorLista.setVisibility(View.GONE);
                ocultaConfMarcadores();
                invalidateOptionsMenu();
                return true;
            default:
                return super.onOptionsItemSelected(menuItem);

        }
    }

    /**
     * Pulsación del botón atrás de la barra de título. Se llama al método onBackPressed()
     *
     * @return Falso
     */
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return false;
    }

    /**
     * Método llamado al pulsar sobre el botón atrás físico del teclado (o equivalente). Si se estaba
     * mostrando la búsqueda se oculta. Si no se estaba realizando una búsqueda se vuelve a la
     * actividad anterior.
     */
    @Override
    public void onBackPressed() {
        if (searchView != null && !searchView.isIconified()) {
            searchView.setIconified(true);
        } else {
            super.onBackPressed();
        }
    }

    private void actualizaElemento(int posicion, int marcador, boolean busquedaCerrada){
        Canal canal;
        if (busquedaCerrada) { //No se está buscando
            //Elimino el canal para luego volver a agregarlo modificado
            canal = listaCanales.remove(posicion);
            //canal.setMarcador(marcador);
            listaCanales.add(posicion, canal);
            adaptador.actualizarPosicionLista(listaCanales, posicion);
        } else{
            canal = listaVariable.remove(posicion);
            int posicionFijo;
            for (posicionFijo = 0; posicionFijo < listaCanales.size(); posicionFijo++)
                if (canal.getId().equals(listaCanales.get(posicionFijo).getId()))
                    break;
            //canal.setMarcador(marcador);
            listaVariable.add(posicion, canal);
            listaCanales.remove(posicionFijo);
            listaCanales.add(posicionFijo, canal);
            adaptador.actualizarPosicionLista(listaVariable, posicion);
        }
        //Almaceno en el fichero el marcador seleccionado para la persistencia de los datos
        try {
            JSONObject jCanal = PersistenciaDatos.recuperaObjeto(
                    getApplication(),
                    PersistenciaDatos.ficheroListaCanales,
                    Auxiliar.canal,
                    canal.getId(),
                    idUsuario);
            jCanal.put(Auxiliar.marcador, marcador);
            PersistenciaDatos.reemplazaJSON(
                    getApplication(),
                    PersistenciaDatos.ficheroListaCanales,
                    Auxiliar.canal,
                    jCanal,
                    idUsuario);
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * Método para obtener la lista de canales del fichero donde se almacenan. Se encarga del correcto
     * formateado de los datos.
     *
     * @return Lista con los canales que se pueden mostrar en el contenedor de esta pantalla.
     */
    private List<Canal> cargaListaCanales() {
        List<Canal> salida = new ArrayList<>();
        JSONArray fichero = PersistenciaDatos.leeFichero(
                getApplication(),
                PersistenciaDatos.ficheroListaCanales);
        JSONObject canal;
        for(int i = 0; i < fichero.length(); i++){
            try {
                canal = fichero.getJSONObject(i);
                if(canal.getString(Auxiliar.idUsuario).equals(idUsuario)) {
                    //No tengo en cuenta el canal con la configuración porque no se le va a mostrar al usuario
                    if (!canal.getString(Auxiliar.canal).equals(Auxiliar.configuracionActual)) {
                        boolean marcado = canal.has(Auxiliar.marcado) && canal.getBoolean(Auxiliar.marcado);

                        salida.add(new Canal(
                                canal.getString(Auxiliar.canal),
                                canal.getString(Auxiliar.label),
                                canal.getString(Auxiliar.comment),
                                canal.getString(Auxiliar.tipo),
                                marcado));
                    }
                }
            } catch (Exception e){
                e.printStackTrace();
            }
        }
        return salida;
    }

    /**
     * Método llamado al pulsar sobre un checkbox de un ítem de la lista de canales.
     * @param view Vista que se ha pulsado
     * @param position Posición pulsada dentro de la lista. Hay que tener en cuenta si se está mostrando
     *                 la lista completa o una reducida si se está realizando una búsqueda.
     */
    @Override
    public void onItemClickCb(View view, int position) {
        Canal canal;
        boolean marcado;
        if(listaVariable == null) {
            canal = listaCanales.remove(position);
            marcado = ((CheckBox) view).isChecked();
            canal.setMarcado(marcado);
            listaCanales.add(position, canal);
            adaptador.actualizarPosicionLista(listaCanales, position);
        } else {
            canal = listaVariable.remove(position);
            int posicionFija;
            for(posicionFija = 0; posicionFija < listaCanales.size(); posicionFija++) {
                if(canal.getId().equals(listaCanales.get(posicionFija).getId()))
                    break;
            }
            marcado = ((CheckBox) view).isChecked();
            canal.setMarcado(marcado);
            listaVariable.add(position, canal);
            listaCanales.remove(posicionFija);
            listaCanales.add(posicionFija, canal);
            adaptador.actualizarPosicionLista(listaVariable, position);
        }
        //enviaSuscripcion(canal.getId(), marcado);
        PersistenciaDatos.borraFichero(getApplication(), PersistenciaDatos.ficheroNuevasCuadriculas);
        try {
            JSONObject jsonCanal = PersistenciaDatos.recuperaObjeto(
                    getApplication(),
                    PersistenciaDatos.ficheroListaCanales,
                    Auxiliar.canal,
                    canal.getId(),
                    idUsuario);
            jsonCanal.put(Auxiliar.marcado, marcado);
            PersistenciaDatos.reemplazaJSON(
                    getApplication(),
                    PersistenciaDatos.ficheroListaCanales,
                    Auxiliar.canal,
                    jsonCanal,
                    idUsuario);
        } catch (Exception e){
          e.printStackTrace();
        }
    }

    /*/**
     * Método llamado cuando se pulsa sobre el icono de un elemento de la lista de canales. Únicamente
     * almacena qué item se ha seleccionado de la lista y se muestra el diálogo para que el usuario
     * seleccione el marcador que prefiera.
     *
     * @param view Vista pulsada
     * @param position Posición dentro de la lista mostrada
     */
    /*@Override
    public void onItemClickMarcador(View view, int position) {
        posicion = position;
        dialogoMarcadores.show();
    }*/

    /**
     * Método para obtener la lista de canales del servidor. Al recuperarla se almacena en un fichero
     * indicando cuándo será el instante en el que la lista dejará de tener validez. El cacheado es
     * de una semana.
     */
    public void obtenerListaCanales() {
        if(idUsuario != null){
            List<String> keys = new ArrayList<>();
            final List<Object> objects = new ArrayList<>();
            keys.add(Auxiliar.idUsuario); objects.add(idUsuario);
            String url = Auxiliar.creaQuery(Auxiliar.rutaCanales, keys, objects);
            JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(
                    Request.Method.GET,
                    url,
                    null,
                    new Response.Listener<JSONArray>() {
                        @Override
                        public void onResponse(JSONArray response) {
                            if(response != null) {
                                JSONArray listaCanalesAntiguos = PersistenciaDatos.leeFichero(
                                        getApplication(),
                                        PersistenciaDatos.ficheroListaCanales);
                                //Doy por válidos los canales que vienen del servidor. Los del
                                //dispositivo están "siempre" obsoletos
                                JSONObject canalServidor, canalCacheado;
                                //Respeto la configuración actual del usuario
                                boolean encontrado;
                                for(int i = 0; i < response.length(); i++){
                                    try {
                                        encontrado = false;
                                        canalServidor = response.getJSONObject(i);
                                        for (int j = 0; j < listaCanalesAntiguos.length(); j++) {
                                            canalCacheado = listaCanalesAntiguos.getJSONObject(j);
                                            if (canalServidor.getString(Auxiliar.canal).equals(
                                                    canalCacheado.getString(Auxiliar.canal))) {
                                                if (canalCacheado.has(Auxiliar.marcado))
                                                    canalServidor.put(Auxiliar.marcado,
                                                            canalCacheado.getBoolean(Auxiliar.marcado));
                                                else
                                                    canalServidor.put(Auxiliar.marcado, false);
                                                /*if (canalCacheado.has(Auxiliar.marcador))
                                                    canalServidor.put(Auxiliar.marcador,
                                                            canalCacheado.getInt(Auxiliar.marcador));
                                                else
                                                    canalServidor.put(Auxiliar.marcador, -1);*/
                                                encontrado = true;
                                                break;
                                            }
                                        }
                                        if(!encontrado){
                                            canalServidor.put(Auxiliar.marcado, false);
                                            //canalServidor.put(Auxiliar.marcador, -1);
                                        }
                                        canalServidor.put(Auxiliar.idUsuario, idUsuario);
                                        response.put(i, canalServidor);
                                    }catch (Exception e){
                                        e.printStackTrace();
                                    }
                                }
                                //Guardo la info del servidor
                                try {
                                    JSONObject configuracionActual = PersistenciaDatos.recuperaObjeto(
                                            getApplication(),
                                            PersistenciaDatos.ficheroListaCanales,
                                            Auxiliar.canal,
                                            Auxiliar.configuracionActual,
                                            idUsuario);
                                    if(configuracionActual == null){
                                        configuracionActual = new JSONObject();
                                        configuracionActual.put(Auxiliar.canal, Auxiliar.configuracionActual);
                                        configuracionActual.put(Auxiliar.idUsuario, idUsuario);
                                        configuracionActual.put(Canal.obligatorio, 0);
                                        configuracionActual.put(Canal.opcional, 4);

                                    }
                                    configuracionActual.put(Auxiliar.caracteristica, true);
                                    configuracionActual.put(Auxiliar.instante,
                                            (System.currentTimeMillis() + 604800000));

                                    response.put(configuracionActual);
                                    PersistenciaDatos.guardaFichero(
                                            getApplication(),
                                            PersistenciaDatos.ficheroListaCanales,
                                            response,
                                            Context.MODE_PRIVATE);
                                } catch (Exception e){
                                    e.printStackTrace();
                                }
                                listaCanales = cargaListaCanales();
                                if(listaCanales.size() > 0)
                                    muestraLista();
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            scActivado.setChecked(false);
                        }
                    }
            );
            jsonArrayRequest.setRetryPolicy(new DefaultRetryPolicy(
                    3000,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            ColaConexiones.getInstance(getApplicationContext()).getRequestQueue().add(jsonArrayRequest);
        }
    }

    /**
     * Método para enviar la suscripción (o la baja) del usuario a un canal.
     * @param idCanal Identificador del canal
     * @param marcado Indica si es un alta (true) o una baja (false) de la suscripción.
     */
    public void enviaSuscripcion(final String idCanal, final boolean marcado){
        try {
            JSONObject peticion = new JSONObject();
            peticion.put(Auxiliar.canal, idCanal);
            peticion.put(Auxiliar.idUsuario, idUsuario);
            peticion.put(Auxiliar.marcado, marcado);
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                    Request.Method.PUT,
                    Auxiliar.rutaCanales + "/"  + Auxiliar.ultimaParte(idCanal),
                    peticion,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            try {
                                JSONObject canal = PersistenciaDatos.recuperaObjeto(
                                        getApplication(),
                                        PersistenciaDatos.ficheroListaCanales,
                                        Auxiliar.canal,
                                        idCanal,
                                        idUsuario);
                                canal.put(Auxiliar.marcado, marcado);
                                PersistenciaDatos.reemplazaJSON(
                                        getApplication(),
                                        PersistenciaDatos.ficheroListaCanales,
                                        Auxiliar.canal,
                                        canal,
                                        idUsuario);
                            }catch (Exception e){
                                e.printStackTrace();
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            //Por ahora no hago nada si ha ocurrido un error. De esta forma, cuando el
                            //usuario vuelva a acceder a la pantalla del canal verá que no se ha
                            //producido el cambio
                            System.err.println(error.toString());
                        }
                    }
            );
            jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(
                    2500,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            ColaConexiones.getInstance(getApplicationContext()).getRequestQueue().add(jsonObjectRequest);
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * Método llamado para desactivar la característica de canales. Modifica el fichero con la lista
     * de canales y envía al servidor la desactivación.
     */
    public void desactivarCanales(){
        try {
            JSONObject configuracionActual = PersistenciaDatos.recuperaObjeto(
                    getApplication(),
                    PersistenciaDatos.ficheroListaCanales,
                    Auxiliar.canal,
                    Auxiliar.configuracionActual,
                    idUsuario);
            if(configuracionActual == null) {
                configuracionActual.put(Auxiliar.id, Auxiliar.canal);
                configuracionActual.put(Auxiliar.instante, 0);
                configuracionActual.put(Auxiliar.idUsuario, idUsuario);
            }
            configuracionActual.put(Auxiliar.caracteristica, false);
            PersistenciaDatos.reemplazaJSON(
                    getApplication(),
                    PersistenciaDatos.ficheroListaCanales,
                    Auxiliar.canal,
                    configuracionActual,
                    idUsuario);
            //enviaConfiguracion(false);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private boolean tipoCanalObligatorio;

    public void boton(View view) {
        switch (view.getId()){
            case R.id.scActivarCanales:
                scActivado.setChecked(false);
                muestraConfMarcadores();
                //Borro el fichero de los contextos para obligar a que desde la pantalla de mapas se
                // descarguen los puntos elegidos por el usuario
                PersistenciaDatos.borraFichero(getApplication(), PersistenciaDatos.ficheroNuevasCuadriculas);

                    if(idUsuario != null) {
                        try {
                            JSONObject configuracionActual = PersistenciaDatos.recuperaObjeto(
                                    getApplication(),
                                    PersistenciaDatos.ficheroListaCanales,
                                    Auxiliar.canal,
                                    Auxiliar.configuracionActual,
                                    idUsuario);
                            if (configuracionActual != null
                                    && configuracionActual.has(Auxiliar.instante)
                                    && configuracionActual.getLong(Auxiliar.instante)
                                    > System.currentTimeMillis()) {
                                //Los canales actuales siguen siendo válidos.
                                configuracionActual.put(Auxiliar.caracteristica, true);
                                PersistenciaDatos.reemplazaJSON(
                                        getApplication(),
                                        PersistenciaDatos.ficheroListaCanales,
                                        Auxiliar.canal,
                                        configuracionActual,
                                        idUsuario);
                                listaCanales = cargaListaCanales();
                                muestraLista();
                                //enviaConfiguracion(true);
                            } else {
                                //enviaConfiguracion(true);
                                obtenerListaCanales();
                            }
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }

                /*else { TODO descativar los canales
                    desactivarCanales();
                    contenedorLista.setVisibility(View.GONE);
                    invalidateOptionsMenu();
                }*/

                break;
            case R.id.tvObligatoriosCanales:
            case R.id.ivObligatorioConfCanales:
                tipoCanalObligatorio = true;
                dialogoMarcadores.show();
                break;
            case R.id.tvOpcionalesCanales:
            case R.id.ivOpcionalConfCanales:
                tipoCanalObligatorio = false;
                dialogoMarcadores.show();
                break;
            //Pulsación sobre uno de los iconos del diálogo
            case R.id.ivNormalSC:
            case R.id.ivEspecial0:
            case R.id.ivEspecial1:
            case R.id.ivEspecial2:
            case R.id.ivEspecial3:
            case R.id.ivEspecial4:
                dialogoMarcadores.cancel();
                try {
                    int marcador = -5;
                    for (int i = 0; i < listaMarcadores.length; i++) {
                        if (listaMarcadores[i] == view.getId()) {
                            marcador = i;
                            break;
                        }
                    }
                    if(marcador > -5) {
                        JSONObject jCanal = PersistenciaDatos.recuperaObjeto(
                                getApplication(),
                                PersistenciaDatos.ficheroListaCanales,
                                Auxiliar.canal,
                                Auxiliar.configuracionActual,
                                idUsuario);
                        if (tipoCanalObligatorio) {
                            jCanal.put(Canal.obligatorio, marcador);
                            ivObligatorios.setImageDrawable(ResourcesCompat.getDrawable(context.getResources(), Auxiliar.obtenIdMarcadores()[marcador], null));
                        } else {
                            jCanal.put(Canal.opcional, marcador);
                            ivOpcionales.setImageDrawable(ResourcesCompat.getDrawable(context.getResources(), Auxiliar.obtenIdMarcadores()[marcador], null));
                        }
                        PersistenciaDatos.reemplazaJSON(
                                getApplication(),
                                PersistenciaDatos.ficheroListaCanales,
                                Auxiliar.canal,
                                jCanal,
                                idUsuario);
                    }
                }  catch (Exception e){
                    e.printStackTrace();
                }
                break;
            default:
                break;
        }
    }

    /*/**
     * Método para activar o desactivar la característica de los canales en el servidor.
     *
     * @param canalesActivos True si se desea activar o false si se desea darse de baja.
     */
    /*private void enviaConfiguracion(boolean canalesActivos) {
        try {
            JSONObject peticion = new JSONObject();
            peticion.put(Auxiliar.idUsuario, idUsuario);
            peticion.put(Auxiliar.marcado, canalesActivos);
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                    Request.Method.PUT,
                    Auxiliar.rutaCanales + "/users/" + idUsuario,
                    peticion,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            //Por ahora no hago nada
                            //System.err.println(response.toString());
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            //Por ahora no hago nada si ha ocurrido un error. De esta forma, cuando el
                            //usuario vuelva a acceder a la pantalla del canal verá que no se ha
                            //producido el cambio
                            //System.err.println(error.toString());
                        }
                    }
            );
            jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(
                    2500,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            ColaConexiones.getInstance(getApplicationContext()).getRequestQueue().add(jsonObjectRequest);
        } catch (Exception e){
            e.printStackTrace();
        }
    }*/
}
