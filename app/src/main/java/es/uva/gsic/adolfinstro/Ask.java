package es.uva.gsic.adolfinstro;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import static es.uva.gsic.adolfinstro.Auxiliar.returnMain;

public class Ask extends AppCompatActivity {

    /** Instancia del objeto donde el usuario introduce la respuesta*/
    EditText et;

    /**
     * Se crea el layout y se recoge la referencia al EditText
     * @param savedInstanceState
     */
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
                    //SALVAR LA RESPUESTA EN BBDD ¿ENVIARLA?
                    returnMain(this);
                }
                break;
            case R.id.btSalir:
                returnMain(this);
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
            Toast.makeText(this, getString(R.string.respuestaVacia), Toast.LENGTH_SHORT).show();
        }
        else{
            //Creación del Bundle
            Toast.makeText(this, getString(R.string.respuestaG), Toast.LENGTH_SHORT).show();
            salida = true;
        }
        return salida;
    }

    /*
    @Override
    protected void onSaveInstanceState(Bundle bundle){
        super.onSaveInstanceState(bundle);
    }

    @Override
    protected void onRestoreInstanceState(Bundle bundle){
        super.onRestoreInstanceState(bundle);
    }*/
}
