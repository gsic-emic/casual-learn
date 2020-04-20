package es.uva.gsic.adolfinstro;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import org.jetbrains.annotations.NotNull;

import es.uva.gsic.adolfinstro.auxiliar.Auxiliar;
import es.uva.gsic.adolfinstro.persistencia.PersistenciaDatos;

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
        tv.setText(getString(R.string.debug));
        tv2 = findViewById(R.id.tvAjustes);
        tv2.setText(getString(R.string.debug));
    }

    /**
     * Método de escucha de los botones de prueba
     * @param view Objeto que ha provocado que se llame al método
     */
    public void boton(@NotNull View view){
        Intent intent = new Intent(this, Tarea.class);
        String recursoAsociadoTexto = "Fotografía la portada que da acceso a la Ermita de San Juan Bautista. Luego puedes acercarte a la Iglesia de San Francisco. Fotografía también su portada y compara ambas";
        intent.putExtra("recursoAsociadoTexto", recursoAsociadoTexto);
        //intent.putExtra("recursoAsociadoTexto", "Tras haber visitado la Iglesia de San Juan de Barbalos, quizá quieras visitar otros lugares de interés cultural. Por ejemplo, te invitamos a visitar la Iglesia Nueva del Arrabal. ¡Está muy cerca de ti!");
        //intent.putExtra(Tarea.recursoImagen, "https://upload.wikimedia.org/wikipedia/commons/6/69/Salamanca_Parroquia_Arrabal.jpg");
        //intent.putExtra(Tarea.recursoImagenBaja, "https://upload.wikimedia.org/wikipedia/commons/thumb/6/69/Salamanca_Parroquia_Arrabal.jpg/300px-Salamanca_Parroquia_Arrabal.jpg");
        //intent.putExtra("recursoAsociadoImagen", "https://upload.wikimedia.org/wikipedia/commons/5/53/Calata%C3%B1azor-Castillo.jpg");
        //intent.putExtra("recursoAsociadoImagen300px", "https://upload.wikimedia.org/wikipedia/commons/thumb/5/53/Calata%C3%B1azor-Castillo.jpg/300px-Calata%C3%B1azor-Castillo.jpg");
        intent.putExtra(Auxiliar.recursoImagen, "https://upload.wikimedia.org/wikipedia/commons/1/13/Iglesia_de_San_Francisco_%28Palencia%29._Fachada.jpg");
        intent.putExtra(Auxiliar.recursoImagen, "https://upload.wikimedia.org/wikipedia/commons/thumb/1/13/Iglesia_de_San_Francisco_%28Palencia%29._Fachada.jpg/300px-Iglesia_de_San_Francisco_%28Palencia%29._Fachada.jpg");
        String id = "https://casssualearn.gsic.uva.es/resource/Ermita_de_San_Juan_Bautista_(Palencia)/compararPortadaRomanicoGotico/10"+System.nanoTime();
        //GrupoTareas tarea = null;
        switch (view.getId()){
            case R.id.btSinRespuesta:
                intent.putExtra("id", id);
                intent.putExtra("tipoRespuesta", TiposTareas.SIN_RESPUESTA.getValue());

                //tarea = new GrupoTareas(id, TiposTareas.SIN_RESPUESTA.getValue(), EstadoTarea.NO_COMPLETADA);
                break;
            case R.id.btTexto:
                intent.putExtra("id", id);
                intent.putExtra("tipoRespuesta", TiposTareas.PREGUNTA_CORTA.getValue());
                //tarea = new GrupoTareas(id, TiposTareas.PREGUNTA_CORTA.getValue(), EstadoTarea.NO_COMPLETADA);
                break;
            case R.id.btTextoLargo:
                intent.putExtra("id", id);
                intent.putExtra("tipoRespuesta", TiposTareas.PREGUNTA_LARGA.getValue());
                //tarea = new GrupoTareas(id, TiposTareas.PREGUNTA_LARGA.getValue(), EstadoTarea.NO_COMPLETADA);
                break;
            case R.id.btUnaFoto:
                intent.putExtra("id", id);
                intent.putExtra("tipoRespuesta", TiposTareas.IMAGEN.getValue());
                //tarea = new GrupoTareas(id, TiposTareas.IMAGEN.getValue(), EstadoTarea.NO_COMPLETADA);
                break;
            case R.id.btVariasFotos:
                intent.putExtra("id", id);
                intent.putExtra("tipoRespuesta", TiposTareas.IMAGEN_MULTIPLE.getValue());
                //tarea = new GrupoTareas(id, TiposTareas.IMAGEN_MULTIPLE.getValue(), EstadoTarea.NO_COMPLETADA);
                break;
            case R.id.btVideo:
                intent.putExtra("id", id);
                intent.putExtra("tipoRespuesta", TiposTareas.VIDEO.getValue());
                //tarea = new GrupoTareas(id, TiposTareas.VIDEO.getValue(), EstadoTarea.NO_COMPLETADA);
                break;
            case R.id.btTextoFoto:
                intent.putExtra("id", id);
                intent.putExtra("tipoRespuesta", TiposTareas.PREGUNTA_IMAGEN.getValue());
                //tarea = new GrupoTareas(id, TiposTareas.PREGUNTA_IMAGEN.getValue(), EstadoTarea.NO_COMPLETADA);
                break;
            case R.id.btMapa:
                intent = new Intent(this, Maps.class);
                break;
            case R.id.btLogin:
                intent = new Intent( this, Login.class);
                break;
            case R.id.btPuntua:
                intent = new Intent(this, ListaTareas.class);
                intent.putExtra("peticion", PersistenciaDatos.ficheroTareasRechazadas);
                break;
            default:
                System.exit(-2);
                intent = null;
        }
        //if(tarea!=null){
            //db.grupoTareasDao().insertTarea(tarea);
        //}
        startActivity(intent);
    }
}
