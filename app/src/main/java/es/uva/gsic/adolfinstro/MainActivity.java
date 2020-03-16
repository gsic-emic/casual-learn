package es.uva.gsic.adolfinstro;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import org.jetbrains.annotations.NotNull;

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
        tv.setText("Desarrollo");
        tv2 = findViewById(R.id.tvAjustes);
        tv2.setText("Desarrollo");
    }

    /**
     * Método de escucha de los botones de prueba
     * @param view
     */
    public void boton(@NotNull View view){
        Intent intent = new Intent(this, Tarea.class);
        intent.putExtra("recursoAsociadoTexto", "El Castillo de Calatañazor, también conocido como Castillo de los Padilla es un fortaleza medieval ubicada en la localidad española de igual nombre, en la provincia de Soria.");
        intent.putExtra("recursoAsociadoImagen", "https://upload.wikimedia.org/wikipedia/commons/6/69/Salamanca_Parroquia_Arrabal.jpg");
        intent.putExtra("recursoAsociadoImagen300px", "https://upload.wikimedia.org/wikipedia/commons/thumb/6/69/Salamanca_Parroquia_Arrabal.jpg/300px-Salamanca_Parroquia_Arrabal.jpg");
        //intent.putExtra("recursoAsociadoImagen", "https://upload.wikimedia.org/wikipedia/commons/5/53/Calata%C3%B1azor-Castillo.jpg");
        //intent.putExtra("recursoAsociadoImagen300px", "https://upload.wikimedia.org/wikipedia/commons/thumb/5/53/Calata%C3%B1azor-Castillo.jpg/300px-Calata%C3%B1azor-Castillo.jpg");
        switch (view.getId()){
            case R.id.btSinRespuesta:
                intent.putExtra("id", "https://casssualearn.gsic.uva.es/resource/Castillo_de_Calatañazor/informacion0");
                intent.putExtra("tipoRespuesta", TiposTareas.SIN_RESPUESTA.getValue());
                break;
            case R.id.btTexto:
                intent.putExtra("id", "https://casssualearn.gsic.uva.es/resource/Castillo_de_Calatañazor/informacion1");
                intent.putExtra("tipoRespuesta", TiposTareas.PREGUNTA_CORTA.getValue());
                break;
            case R.id.btTextoLargo:
                intent.putExtra("id", "https://casssualearn.gsic.uva.es/resource/Castillo_de_Calatañazor/informacion2");
                intent.putExtra("tipoRespuesta", TiposTareas.PREGUNTA_LARGA.getValue());
                break;
            case R.id.btUnaFoto:
                intent.putExtra("id", "https://casssualearn.gsic.uva.es/resource/Castillo_de_Calatañazor/informacion4");
                intent.putExtra("tipoRespuesta", TiposTareas.IMAGEN.getValue());
                break;
            case R.id.btVariasFotos:
                intent.putExtra("id", "https://casssualearn.gsic.uva.es/resource/Castillo_de_Calatañazor/informacion5");
                intent.putExtra("tipoRespuesta", TiposTareas.IMAGEN_MULTIPLE.getValue());
                break;
            case R.id.btVideo:
                intent.putExtra("id", "https://casssualearn.gsic.uva.es/resource/Castillo_de_Calatañazor/informacion6");
                intent.putExtra("tipoRespuesta", TiposTareas.VIDEO.getValue());
                break;
            case R.id.btTextoFoto:
                intent.putExtra("id", "https://casssualearn.gsic.uva.es/resource/Castillo_de_Calatañazor/informacion3");
                intent.putExtra("tipoRespuesta", TiposTareas.PREGUNTA_IMAGEN.getValue());
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
