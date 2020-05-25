package es.uva.gsic.adolfinstro;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import java.util.Objects;

/**
 * Clase diseñada para mostrar la información de la aplicación
 *
 * @author Pablo
 * @version 20200520
 */
public class Acerca extends AppCompatActivity {

    /** Instancia del cuadro del textview donde se coloca la versión*/
    TextView version;

    /**
     * Método para completar la interfaz gráfica del usuario. Se pinta la versión de la app.
     *
     * @param savedInstanceState bundle con la información almacenada antes de destruir la
     *                           actividad
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_acerca);

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        version = findViewById(R.id.tvVersion);

        if(savedInstanceState == null){
            try {
                version.setText(String.format("%s: %s", getString(R.string.version),
                        this.getPackageManager().getPackageInfo(getPackageName(), 0).versionName));
            } catch (PackageManager.NameNotFoundException e) {
                version.setText(String.format("%s: %d", getString(R.string.version), 0));

            }
        }else{
            version.setText(savedInstanceState.getString("TEXTOVERSION"));
        }
    }

    /**
     * Acciones que se toman al tocar los distintos botones de la pantalla.
     * @param view Vista
     */
    public void boton(View view) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.addCategory(Intent.CATEGORY_BROWSABLE);
        switch (view.getId()){
            case R.id.imagenGsic:
                intent.setData(Uri.parse("https://www.gsic.uva.es"));
                break;
            case R.id.imagenUva:
                intent.setData(Uri.parse("https://www.uva.es"));
                break;
            case R.id.tvOpenStreepMap:
                intent.setData(Uri.parse("https://www.openstreetmap.org/copyright"));
                break;
            case R.id.tvOsmdroid:
                intent.setData(Uri.parse("https://github.com/osmdroid/osmdroid"));
                break;
            case R.id.tvPhotoView:
                intent.setData(Uri.parse("https://github.com/chrisbanes/PhotoView"));
                break;
            case R.id.tvFused:
                intent.setData(Uri.parse("https://developers.google.com/location-context/fused-location-provider"));
                break;
            case R.id.tvPicasso:
                intent.setData(Uri.parse("https://square.github.io/picasso/"));
                break;
            default:
                return;
        }
        startActivity(intent);
    }

    /**
     * Método que recoge la pulsación del botón atrás de la barra.
     * @return False ya que este método no finaliza la actividad
     */
    @Override
    public boolean onSupportNavigateUp(){
        onBackPressed();
        return false;
    }

    /**
     * Acción que se toma al accionar el botón atrás
     */
    @Override
    public void onBackPressed(){
        finish();
    }

    /**
     * Se almacnea el texto de la versión para no tener que recargarlo con
     * cada cierre.
     * @param b bundle donde se almacena el estado
     */
    @Override
    public void onSaveInstanceState(Bundle b) {
        super.onSaveInstanceState(b);
        b.putString("TEXTOVERSION", version.getText().toString());
    }
}
