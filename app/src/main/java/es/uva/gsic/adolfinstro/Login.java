package es.uva.gsic.adolfinstro;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.preference.PreferenceManager;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import es.uva.gsic.adolfinstro.auxiliar.Auxiliar;
import es.uva.gsic.adolfinstro.persistencia.PersistenciaDatos;

/**
 * Clase que permite a los usuarios identificarse frente al sistema.
 *
 * @author Pablo
 * @version 20200520
 */
public class Login extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener, View.OnClickListener{

    /** Código de identificación para la solicitud de los permisos de la app */
    private static final int requestCodePermissions = 1000;

    /** Código con el que se lanza la actividad de identificación del usuario con cuenta Google */
    public static final int requestAuth = 1010;

    /** Instancia para auntenticación con la cuenta Google */
    public static GoogleSignInClient googleSignInClient;

    private SharedPreferences sharedPreferences;

    /** Análisis comportamiento usuario */
    public static FirebaseAnalytics firebaseAnalytics;

    /** Firebase autenticación */
    public static FirebaseAuth firebaseAuth;

    /** Google signIn */
    public static GoogleSignInOptions gso;

    private AlertDialog.Builder dialogoAccesoSinId;

    private Boolean dialogoAccesoSinIdVisible;

    @Override
    public void onCreate(Bundle sI){
        super.onCreate(sI);
        //Análisis
        firebaseAnalytics = FirebaseAnalytics.getInstance(this);

        //Aquí irá la comprobación de si el usuario ya se ha autenticado previamente
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        onSharedPreferenceChanged(sharedPreferences, Ajustes.LISTABLANCA_pref);

        setContentView(R.layout.activity_login);

        Auxiliar.enlaceLicencia(this, (ImageView) findViewById(R.id.ivInfoFotoLogin), "https://commons.wikimedia.org/wiki/File:Ampudia_-_Castillo_1.jpg");

        //https://developers.google.com/identity/sign-in/android/sign-in
        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail().build();

        googleSignInClient = GoogleSignIn.getClient(this, gso);

        SignInButton btGoogle = findViewById(R.id.btGoogle);
        //Se tiene que registrar las pulsaciones del botón desde el código ya que no es un botón estándar
        //Se podría evitar tener que implementar el OnClickListener utilizando un botón estándar con una imagen
        btGoogle.setOnClickListener(this);

        firebaseAuth = FirebaseAuth.getInstance();

        dialogoAccesoSinId = new AlertDialog.Builder(this);
        dialogoAccesoSinId.setMessage(R.string.textoInicio);
        dialogoAccesoSinId.setPositiveButton(R.string.autenticarseMasTarde, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialogoAccesoSinIdVisible = false;
                saltaMapa(null);
            }
        });
        dialogoAccesoSinId.setNegativeButton(R.string.autenticarseAhora, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialogoAccesoSinIdVisible = false;
                lanzaGoogle();
            }
        });
        dialogoAccesoSinId.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                dialogoAccesoSinIdVisible = false;
            }
        });

        dialogoAccesoSinIdVisible = false;
        if(sI != null && sI.getBoolean("DIALOGOSINID", false)){
            dialogoAccesoSinIdVisible = true;
            dialogoAccesoSinId.show();
        }

    }

    @Override
    protected void onSaveInstanceState(@NotNull Bundle bundle){
        bundle.putBoolean("DIALOGOSINID", dialogoAccesoSinIdVisible);
        super.onSaveInstanceState(bundle);
    }

    @Override
    public void onStart(){
        super.onStart();
        checkPermissions();
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        updateUI(firebaseUser, false);
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
                lanzaGoogle();
                break;
            default:
        }
    }

    private void lanzaGoogle(){
        Intent intent = googleSignInClient.getSignInIntent();
        startActivityForResult(intent, requestAuth);
    }

    /**
     * Método para comprobar si el usuario ha otorgado a la aplicación los permisos necesarios.
     * En la actualidad, solicita permisos de localización y cámara.
     */
    public void checkPermissions(){
        if(!getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY))
            System.exit(-1);
        ArrayList<String> permisos = Auxiliar.preQueryPermisos(this);
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
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        //Se comprueba uno a uno si alguno de los permisos no se había aceptado
        for (int i : grantResults) {
            if (i == -1) {
                AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
                alertBuilder.setTitle(getString(R.string.permi));
                alertBuilder.setMessage(getString(R.string.permiM));
                alertBuilder.setPositiveButton(getString(R.string.volverSolicitar), new DialogInterface.OnClickListener() {
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
                alertBuilder.setCancelable(false);
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
    public void onActivityResult(int requestCode, int result, Intent data) {
        super.onActivityResult(requestCode, result, data);
        switch (requestCode) {
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
            firebaseAuthWithGoogle(account);
        }catch (Exception e){
            e.printStackTrace();
            updateUI(null, true);
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount googleSignInAccount){
        AuthCredential authCredential = GoogleAuthProvider.getCredential(googleSignInAccount.getIdToken(), null);
        firebaseAuth.signInWithCredential(authCredential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>(){
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
                    updateUI(firebaseUser, true);
                }else{
                    Toast.makeText(Login.this, getString(R.string.errorAutentica), Toast.LENGTH_SHORT).show();
                    updateUI(null, true);
                }
            }
        });
    }

    public void updateUI(FirebaseUser firebaseUser, boolean registro){
        if(firebaseUser != null){
            firebaseAnalytics.setUserId(firebaseUser.getUid());
            Bundle bundle = new Bundle();
            bundle.putString(Auxiliar.uid, firebaseUser.getUid());
            if(registro)
                firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SIGN_UP, bundle);
            else
                firebaseAnalytics.logEvent(FirebaseAnalytics.Event.LOGIN, bundle);
            try {
                JSONObject usuario = new JSONObject();
                usuario.put(Auxiliar.id, Auxiliar.id);
                usuario.put(Auxiliar.uid, firebaseUser.getUid());
                PersistenciaDatos.reemplazaJSON(getApplication(), PersistenciaDatos.ficheroUsuario, usuario);
            }catch (JSONException e){
                e.printStackTrace();
            }
            //Toast.makeText(this, String.format("%s%s", getString(R.string.hola), firebaseUser.getDisplayName()), Toast.LENGTH_SHORT).show();
            saltaMapa(firebaseUser.getDisplayName());
        }
    }

    private void saltaMapa(String firebaseUserName){
        Intent intent = new Intent (getApplicationContext(), Maps.class);
        if(firebaseUserName != null)
            intent.putExtra(Auxiliar.textoParaElMapa, String.format("%s%s", getString(R.string.hola), firebaseUserName));
        else
            intent.putExtra(Auxiliar.textoParaElMapa, "");
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        finishAffinity();
        startActivity(intent);
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
            default:
        }
    }

    public void boton(View view) {
        if(view.getId() == R.id.btInicioSinIdentificacion){
            dialogoAccesoSinIdVisible = true;
            dialogoAccesoSinId.show();
        }
    }
}
