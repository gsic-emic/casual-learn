package es.uva.gsic.adolfinstro;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.preference.PreferenceManager;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.analytics.FirebaseAnalytics;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import es.uva.gsic.adolfinstro.auxiliar.Auxiliar;
import es.uva.gsic.adolfinstro.auxiliar.ColaConexiones;

/**
 * Clase que permite a los usuarios identificarse frente al sistema.
 *
 * @author GSIC
 */
public class Login extends Activity implements SharedPreferences.OnSharedPreferenceChangeListener, View.OnClickListener{

    /** Código de identificación para la solicitud de los permisos de la app */
    private static final int requestCodePermissions = 1000;

    /** Código con el que se lanza la actividad de identificación del usuario con cuenta Google */
    private static final int requestAuth = 101010;

    /** Instancia para auntenticación con la cuenta Google */
    private GoogleSignInClient googleSignInClient;

    private SharedPreferences sharedPreferences;

    /** Análisis comportamiento usuario */
    private FirebaseAnalytics firebaseAnalytics;

    @Override
    public void onCreate(Bundle sI){
        super.onCreate(sI);
        //Análisis
        firebaseAnalytics = FirebaseAnalytics.getInstance(this);

        //Aquí irá la comprobación de si el usuario ya se ha autenticado previamente
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        onSharedPreferenceChanged(sharedPreferences, Ajustes.TOKEN_pref);
        onSharedPreferenceChanged(sharedPreferences, Ajustes.LISTABLANCA_pref);

        setContentView(R.layout.activity_login);

        //https://developers.google.com/identity/sign-in/android/sign-in
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail().build();

        googleSignInClient = GoogleSignIn.getClient(this, gso);

        SignInButton btGoogle = findViewById(R.id.btGoogle);
        //Se tiene que registrar las pulsaciones del botón desde el código ya que no es un botón estándar
        //Se podría evitar tener que implementar el OnClickListener utilizando un botón estándar con una imagen
        btGoogle.setOnClickListener(this);
        //pruebaGet();
        //pruebaPut();
    }

    /**
     * Método que es llamada cuando se pulsa un objeto clicklable
     * @param v Objeto pulsado
     */
    @Override
    public void onClick(View v){
        switch (v.getId()){
            case R.id.btGoogle:
                //Se lanza la actividad de identificación de Google y se espera al resultado
                Intent intent = googleSignInClient.getSignInIntent();
                startActivityForResult(intent, requestAuth);
                break;
            default:
        }
    }

    /**
     * Método para almacenar el token en las preferencias de la aplicación
     * @param token Identificador único del usuario
     */
    private void actualizaToken(String token) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(Ajustes.TOKEN_pref, token);
        //Tiene que ser un commit, con aply() no funciona
        editor.commit();
    }

    /**
     * Método para comprobar si el usuario ha otorgado a la aplicación los permisos necesarios.
     * En la actualidad, solicita permisos de localización y cámara.
     */
    public void checkPermissions(){
        if(!getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY))
            System.exit(-1);
        ArrayList<String> permisos = new ArrayList<>();
        Auxiliar.preQueryPermisos(this, permisos);
        if (permisos.size()>0) //Evitamos hacer una petición con un array nulo
            ActivityCompat.requestPermissions(this, permisos.toArray(new String[permisos.size()]), requestCodePermissions);
    }

    /**
     * Método que devuelve el resultado de la solicitud de permisos.
     * @param requestCode Código de la petición de permismos.
     * @param permissions Permisos que se han solicitado.
     * @param grantResults Valor otorgado por el usuario al permiso.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NotNull String[] permissions, int[] grantResults) {
        //Se comprueba uno a uno si alguno de los permisos no se había aceptado
        for (int i : grantResults) {
            if (i == -1) {
                AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
                alertBuilder.setTitle(getString(R.string.permi));
                alertBuilder.setMessage(getString(R.string.permiM));
                alertBuilder.setPositiveButton(getString(R.string.accept), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //Se comprueba todos los permisos que necesite la app de nuevo, por este
                        // motivo se puede salir del for directamente
                        checkPermissions();
                    }
                });
                alertBuilder.setNegativeButton(getString(R.string.exi), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //Si el usuario no quiere conceder los permisos que necesita la aplicación se
                        //cierra
                        System.exit(0);
                    }
                });
                alertBuilder.show();
                break;
            }
        }
    }

    /**
     * Método al que se le llamará cuando se vuelva de otra actividad.
     * @param requestCode Código con el que se ha lanzado el intent
     * @param result Valor que devuelve la actividad lanzada
     * @param data datos
     */
    @Override
    public void onActivityResult(int requestCode, int result, Intent data){
        switch (requestCode){
            case requestAuth:
                //No es necesario comprobar el resultado de la petición según la ayuda oficial
                Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                tareaCuenta(task);
            break;
            default:

        }
    }

    /**
     * Método que recupera el identificador único de la cuenta de Google con el que el usuario se
     * quiere identificar. Para probar el comportamiento de Firebase se manda un evento con el
     * nuevo usuario identificado
     * @param task Tarea
     */
    private void tareaCuenta(Task<GoogleSignInAccount> task) {
        try{
            GoogleSignInAccount account = task.getResult(ApiException.class);
            assert account != null;
            String idCuenta = account.getId();

            actualizaToken(idCuenta);

            Bundle bundle = new Bundle();
            final String nuevaIdentificacion = "nuevaIdentificacion";
            bundle.putString("ID", nuevaIdentificacion);
            bundle.putString("Cuenta", idCuenta);
            firebaseAnalytics.logEvent(nuevaIdentificacion, bundle);
            bundle = null;

            enviaUsuario(account);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void enviaUsuario(GoogleSignInAccount cuenta){
        String url = "http://192.168.1.14:8080/usuarios";
        JSONObject json = new JSONObject();
        try {
            json.put("id", cuenta.getId());
            json.put("nombre", cuenta.getDisplayName());
            json.put("email", cuenta.getEmail());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        final Intent intent = new Intent(this, Maps.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        JsonObjectRequest jsonObj = new JsonObjectRequest(Request.Method.PUT, url, json, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                //TODO PROCESADO DEL JSONARRAY
                startActivity(intent);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                System.err.println("Error en el listen");
            }
        });
        ColaConexiones.getInstance(getApplicationContext()).getRequestQueue().add(jsonObj);
    }


    /**
     * Método para atender al cambio de una preferencia
     * @param sharedPreferences preferencia
     * @param key clave modificada
     */
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        switch (key){
            case Ajustes.LISTABLANCA_pref:
                if(sharedPreferences.getBoolean(key, true))
                    Auxiliar.dialogoAyudaListaBlanca(this, sharedPreferences);
                break;
            case Ajustes.TOKEN_pref:
                //Si existe un token no se identifica al usuario y se salta directamente a la
                //actividad del mapa
                if(!sharedPreferences.getString(key, " ").equals(" ")){
                    Intent intent = new Intent (getApplicationContext(), Maps.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                }else {
                    checkPermissions(); //Compruebo los permisos antes de seguir
                }
            default:
        }
    }
}
