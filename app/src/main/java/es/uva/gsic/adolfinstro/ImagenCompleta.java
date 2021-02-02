package es.uva.gsic.adolfinstro;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.github.chrisbanes.photoview.PhotoView;
import com.squareup.picasso.Picasso;

import java.util.Objects;

import es.uva.gsic.adolfinstro.auxiliar.Auxiliar;

/**
 * Clase diseñada para mostrar una imagen y que el usuario pueda ampliarla.
 * @author Pablo
 * @version 20210202
 */
public class ImagenCompleta extends AppCompatActivity {

    String urlLicencia;

    /**
     * Método de carga inicial de los objetos. Se inicia la descarga de la fotografía o la carga de
     * la memoria interna
     * @param savedInstanceState Bundle
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_imagen_completa);
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        PhotoView photoView = findViewById(R.id.photoView);
        try {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                Picasso.get()
                    .load(Objects.requireNonNull(getIntent().getExtras()).getString("IMAGENCOMPLETA"))
                    .tag(Auxiliar.cargaImagenDetalles)
                    .placeholder(R.drawable.ic_cloud_download_blue_80dp)
                    .into(photoView);
            else
                Picasso.get()
                        .load(Objects.requireNonNull(getIntent().getExtras()).getString("IMAGENCOMPLETA"))
                        .tag(Auxiliar.cargaImagenDetalles)
                        .into(photoView);
        }catch (Exception e){
            e.printStackTrace();
        }

        if(Objects.requireNonNull(getIntent().getExtras()).getBoolean("MUESTRAC")) {
            urlLicencia = Auxiliar.enlaceLicencia(
                    this,
                    (ImageView) findViewById(R.id.ivInfoFotoCompleta),
                    Objects.requireNonNull(getIntent().getExtras()).getString("IMAGENCOMPLETA"));
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

    public void boton(View view) {
        if(view.getId() == R.id.ivInfoFotoCompleta){
            if(urlLicencia != null)
                Auxiliar.navegadorInterno(this, urlLicencia);
        }
    }
}
