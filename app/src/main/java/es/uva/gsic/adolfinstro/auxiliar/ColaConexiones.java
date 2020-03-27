package es.uva.gsic.adolfinstro.auxiliar;

import android.content.Context;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

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
