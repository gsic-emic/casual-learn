package es.uva.gsic.adolfinstro;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class Ask extends AppCompatActivity {

    EditText et;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ask);

        et = findViewById(R.id.etRespuesta);
    }

    /**
     * Método para gestionar las pulsaciones de los botones
     * @param view
     */
    public void boton(View view) {
        switch (view.getId()){
            case R.id.btAceptar:
                if(guardaRespuesta()) {
                    Intent intent = new Intent(this, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                }
                break;
            case R.id.btSalir:
                Intent intent = new Intent(this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                break;
        }
    }

    /**
     * Método para almacenar la respuesta del usuario
     * @return Devolverá true cuando la respuestas esté correctamente almacenada y false en cualquier otro caso
     */
    private boolean guardaRespuesta(){
        String respuesta = et.getText().toString();
        respuesta = respuesta.trim();
        boolean salida = false;
        if(respuesta.isEmpty()){
            Toast.makeText(this, "La respuesta no puede estar vacía", Toast.LENGTH_SHORT).show();
        }
        else{
            //Creación del Bundle
            Toast.makeText(this, "Respuesta guardada", Toast.LENGTH_SHORT).show();
            salida = true;
        }
        return salida;
    }
}
