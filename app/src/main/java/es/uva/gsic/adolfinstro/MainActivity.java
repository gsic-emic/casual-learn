package es.uva.gsic.adolfinstro;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private TextView tv, tv2;


    /**
     * Método de creación. Se recogen las referencias a los objectos del layout y se inicializan alguno de los objetos
     * que estarán activos durante toda la sesión
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tv = findViewById(R.id.tvLatitudLongitud);
        tv2 = findViewById(R.id.tvAjustes);
    }

    /**
     * Método de escucha de los botones de prueba
     * @param view
     */
    public void boton(View view){
        Intent intent = new Intent(this, Tarea.class);
        intent.putExtra("id", "https://casssualearn.gsic.uva.es/resource/Castillo_de_Calatañazor/informacion");
        intent.putExtra("recursoAsociadoTexto", "El Castillo de Calatañazor, también conocido como Castillo de los Padilla es un fortaleza medieval ubicada en la localidad española de igual nombre, en la provincia de Soria.");
        intent.putExtra("recursoAsociadoImagen", "https://upload.wikimedia.org/wikipedia/commons/6/69/Salamanca_Parroquia_Arrabal.jpg");
        switch (view.getId()){
            case R.id.btSinRespuesta:
                intent.putExtra("tipoRespuesta", "sinRespuesta");
                break;
            case R.id.btTexto:
                intent.putExtra("tipoRespuesta", "preguntaCorta");
                break;
            case R.id.btTextoLargo:
                intent.putExtra("tipoRespuesta", "preguntaLarga");
                break;
            case R.id.btUnaFoto:
                intent.putExtra("tipoRespuesta", "imagen");
                break;
            case R.id.btVariasFotos:
                intent.putExtra("tipoRespuesta", "imagenMultiple");
                break;
            case R.id.btVideo:
                intent.putExtra("tipoRespuesta", "video");
                break;
            case R.id.btTextoFoto:
                intent.putExtra("tipoRespuesta", "preguntaImagen");
                break;
            case R.id.btMapa:
                intent = new Intent(this, Maps.class);
                break;
            case R.id.btLogin:
                intent = new Intent( this, Login.class);
                intent.putExtra("ERRORACCESO", true);
                break;
            default:
                System.exit(-2);
                intent = null;
        }
        startActivity(intent);
    }
}
