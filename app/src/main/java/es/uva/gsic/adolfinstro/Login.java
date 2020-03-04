package es.uva.gsic.adolfinstro;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.preference.PreferenceManager;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class Login extends Activity implements SharedPreferences.OnSharedPreferenceChangeListener{

    /** Objetos donde estará la respuesta del usuario*/
    private EditText etUser, etPassword;
    /** Objeto para inidicar que ha ocurrido un error al realizar la autenticación */
    private TextView tvErrorLogin;
    /** Código de identificación para la solicitud de los permisos de la app */
    private static final int requestCodePermissions = 1000;

    private SharedPreferences sharedPreferences;

    @Override
    public void onCreate(Bundle sI){
        super.onCreate(sI);
        checkPermissions(); //Compruebo los permisos antes de seguir
        //Aquí irá la comprobación de si el usuario ya se ha autenticado previamente
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        onSharedPreferenceChanged(sharedPreferences, Ajustes.TOKEN_pref);
        onSharedPreferenceChanged(sharedPreferences, Ajustes.LISTABLANCA_pref);

        setContentView(R.layout.activity_login);
        etUser = findViewById(R.id.etUsuario);
        etPassword = findViewById(R.id.etContrase);

        tvErrorLogin = findViewById(R.id.tvAcessoIncorrecto);

        try {
            if (getIntent().getExtras().getBoolean("ERRORACCESO")) {
                tvErrorLogin.setText(getString(R.string.incorrectLogin));
            }
        }catch (NullPointerException e){
            //No se ha pasado como parámetro "ERRORACCESO"
        }

    }

    private void actualizaToken(String token) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(Ajustes.TOKEN_pref, token);
        editor.commit();
    }

    /**
     * Método que devuelve el resultado de la solicitud de permisos.
     * @param requestCode Código de la petición de permismos.
     * @param permissions Permisos que se han solicitado.
     * @param grantResults Valor otorgado por el usuario al permiso.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NotNull String[] permissions, int[] grantResults){
        for(int i : grantResults){
            if(i == -1){
                AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
                alertBuilder.setTitle(getString(R.string.permi));
                alertBuilder.setMessage(getString(R.string.permiM));
                alertBuilder.setPositiveButton(getString(R.string.acept), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        checkPermissions();
                    }
                });
                alertBuilder.setNegativeButton(getString(R.string.exi), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        System.exit(0);
                    }
                });
                alertBuilder.show();
                break;
            }
        }
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
     * Método que realiza las operaciones necesarias para comprobar si el par usuario contraseña está registrado
     * en el sistema
     * @param usuario Nombre del usuario
     * @param contra Contraseña del usuario
     * @return Devolverá un true si el nombre de usuario y la contraseña son correctas
     */
    private boolean comprueba(String usuario, String contra){
        boolean salida = true;
        if(usuario.isEmpty()){
            etUser.setError(getString(R.string.emptyLoginUser));
            salida = false;
        }
        if(contra.isEmpty()){
            etPassword.setError(getString(R.string.emptyLoginPass));
            salida = false;
        }
        return salida;
    }

    /**
     * Método para gestionar la acción de los botones
     * @param view Instancia del objeto
     */
    public void boton(View view) {
        switch (view.getId()){
            case R.id.btLog:
                String usuario=etUser.getText().toString().trim(), contra=etPassword.getText().toString().trim();

                if(comprueba(usuario, contra)) {
                    Intent intent = new Intent(this, Maps.class);
                    intent.putExtra("USER", usuario);
                    intent.putExtra("CONTRA", contra);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    actualizaToken("Token de prueba");
                    startActivity(intent);
                }
                break;
            case R.id.btRegistro:
                Toast.makeText(this, "Por implementar", Toast.LENGTH_SHORT).show();
                break;
        }
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
                if(sharedPreferences.getString(key, " ") != " "){
                    Intent intent = new Intent (getApplicationContext(), Maps.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                }
            default:
        }
    }
}
