package es.uva.gsic.adolfinstro;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.github.chrisbanes.photoview.PhotoView;
import com.squareup.picasso.Picasso;

import java.util.Objects;

import es.uva.gsic.adolfinstro.auxiliar.Auxiliar;

public class ImagenCompleta extends AppCompatActivity {

    PhotoView photoView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_imagen_completa);
        photoView = findViewById(R.id.photoView);
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
}
