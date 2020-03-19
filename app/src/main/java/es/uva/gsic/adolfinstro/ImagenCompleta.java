package es.uva.gsic.adolfinstro;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;

import com.github.chrisbanes.photoview.PhotoView;

import java.io.IOException;
import java.net.URL;
import java.util.Objects;

public class ImagenCompleta extends AppCompatActivity {

    PhotoView photoView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_imagen_completa);
        photoView = findViewById(R.id.photoView);
        try {
            new DownloadImages().execute(new URL(Objects.requireNonNull(
                    getIntent().getExtras()).getString("IMAGENCOMPLETA")));
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * Clase que se encarga de obtener la imagen del servidor
     */
    private class DownloadImages extends AsyncTask<URL, Void, Bitmap> {

        /**
         * Método encargado de descargar las imágenes de las URLs que indiquen
         * @param urls URLs a descargar. Según está implementado, solamente descarga la primera URL
         * @return Imagen descargada o null si no se pudo descargar
         */
        protected Bitmap doInBackground(URL... urls) {
            try {
                photoView.setImageDrawable(getResources().getDrawable(R.drawable.ic_cloud_download_blue_80dp));
                RotateAnimation rotateAnimation = new RotateAnimation(0f, 359f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                rotateAnimation.setInterpolator(new LinearInterpolator());
                rotateAnimation.setRepeatCount(Animation.INFINITE);
                rotateAnimation.setDuration(1200);
                photoView.startAnimation(rotateAnimation);
                return BitmapFactory.decodeStream(urls[0].openConnection().getInputStream());
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }

        /**
         * Método que carga la imagen descargada en la interfaz gráfica de la aplicación
         * @param bitmap Imagen que se va a mostrar
         */
        @Override
        protected void onPostExecute(Bitmap bitmap){
            if (bitmap != null) {
                photoView.setAnimation(null);
                photoView.setImageBitmap(bitmap);
            } else {
                photoView.setAnimation(null);
                photoView.setImageDrawable(getResources().getDrawable(R.drawable.ic_close_red_80dp));
            }
        }
    }
}
