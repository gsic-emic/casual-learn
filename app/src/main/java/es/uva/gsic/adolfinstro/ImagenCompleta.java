package es.uva.gsic.adolfinstro;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.github.chrisbanes.photoview.PhotoView;
import com.squareup.picasso.Picasso;

import java.util.Objects;

import es.uva.gsic.adolfinstro.auxiliar.Auxiliar;

/**
 * Clase diseñada para mostrar una imagen y que el usuario pueda ampliarla.
 * @author Pablo
 * @version 20200612
 */
public class ImagenCompleta extends AppCompatActivity {
    /**
     * Método de carga inicial de los objetos. Se inicia la descarga de la fotografía o la carga de
     * la memoria interna
     * @param savedInstanceState Bundle
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_imagen_completa);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        PhotoView photoView = findViewById(R.id.photoView);
        try {
            Picasso.get()
                    .load(Objects.requireNonNull(getIntent().getExtras()).getString("IMAGENCOMPLETA"))
                    .tag(Auxiliar.cargaImagenDetalles)
                    .placeholder(R.drawable.ic_cloud_download_blue_80dp)
                    .into(photoView);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * Método llamado cuando se pulsa en el botón atrás de la barra de título
     * @return Falso porque no finaliza la tarea
     */
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return false;
    }

    /**
     * Se pulsa al botón atrás físico. Cancela la carga de la imagen y finaliza la actividad.
     */
    @Override
    public  void onBackPressed(){
        Picasso.get().cancelTag(Auxiliar.cargaImagenDetalles);
        finish();
    }
}
