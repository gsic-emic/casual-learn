package es.uva.gsic.adolfinstro.auxiliar;

import android.content.Context;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

/**
 * Clase que gestiona la cola de peticiones que se realizan desde la aplicación.
 * @author pablo
 * @version 20200626
 *
 * Basado en https://developer.android.com/training/volley/requestqueue#java
 */
public class ColaConexiones {
    private static ColaConexiones instancia;
    private RequestQueue requestQueue;
    private static Context contexto;

    private ColaConexiones(Context context){
        contexto = context;
        requestQueue = getRequestQueue();
    }

    public static synchronized ColaConexiones getInstance(Context context){
        if(instancia == null){
            instancia = new ColaConexiones(context);
        }
        return instancia;
    }

    public RequestQueue getRequestQueue(){
        if(requestQueue == null){
            requestQueue = Volley.newRequestQueue(contexto.getApplicationContext());
        }
        return requestQueue;
    }
}
