package es.uva.gsic.adolfinstro;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class Login extends Activity {

    EditText etUser, etPassword;
    @Override
    public void onCreate(Bundle sI){
        super.onCreate(sI);
        setContentView(R.layout.activity_login);
        etUser = findViewById(R.id.etUsuario);
        etPassword = findViewById(R.id.etContrase);
    }

    private boolean comprueba(String usuario, String contra){
        boolean salida = true;
        if(usuario.isEmpty()){
            etUser.setError("Introduce el nombre de usuario");
            salida = false;
        }
        if(contra.isEmpty()){
            etPassword.setError("Introduce la contraseña");
            salida = false;
        }
        return salida;
    }
    public void boton(View view) {
        switch (view.getId()){
            case R.id.btLog:
                String usuario=etUser.getText().toString().trim(), contra=etPassword.getText().toString().trim();

                if(comprueba(usuario, contra)) {
                    Intent intent = new Intent(this, Maps.class);
                    intent.putExtra("USER", usuario);
                    intent.putExtra("CONTRA", contra);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                }
                break;
            case R.id.btRegistro:
                Toast.makeText(this, "Por implementar", Toast.LENGTH_SHORT).show();
                break;
        }
    }
}
