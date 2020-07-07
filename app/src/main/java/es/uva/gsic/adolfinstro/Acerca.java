package es.uva.gsic.adolfinstro;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.text.LineBreaker;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Random;

/**
 * Clase diseñada para mostrar la información de la aplicación
 *
 * @author Pablo
 * @version 20200701
 */
public class Acerca extends AppCompatActivity {

    /** Instancia del cuadro del textview donde se coloca la versión*/
    TextView version;
    //List<TextView> desarrolladores;
    //Boolean ensena = false;


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

        /*desarrolladores = new ArrayList<>();
        desarrolladores.add((TextView) findViewById(R.id.tvPablo));
        desarrolladores.add((TextView) findViewById(R.id.tvAdolfo));*/

        TextView proyectos = findViewById(R.id.tvFondosEuropeos);
        proyectos.setMovementMethod(LinkMovementMethod.getInstance());
        proyectos.setPadding(18, 0, 8, 0);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            proyectos.setJustificationMode(LineBreaker.JUSTIFICATION_MODE_INTER_WORD);
        }

        if(savedInstanceState == null){
            try {
                version.setText(String.format("%s %s", getString(R.string.version),
                        this.getPackageManager().getPackageInfo(getPackageName(), 0).versionName));
            } catch (PackageManager.NameNotFoundException e) {
                version.setText(String.format("%s %d", getString(R.string.version), 0));

            }
        }else{
            version.setText(savedInstanceState.getString("TEXTOVERSION"));
        }


        /*if(savedInstanceState != null){
            ensena = savedInstanceState.getBoolean("ENSENA");
            desarrolladores();
        }*/
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
                intent.setData(Uri.parse(getString(R.string.urlgsic)));
                break;
            case R.id.imagenUva:
                intent.setData(Uri.parse(getString(R.string.urluva)));
                break;
            case R.id.tvDesarrolladores:
                Dialog dialogo = new Dialog(this);
                dialogo.setContentView(R.layout.dialogo_desarrolladores);
                dialogo.setCancelable(true);
                TextView textView = dialogo.findViewById(R.id.tvEspacioDesarrolladores);
                textView.setText(desarrolladores());
                dialogo.show();
                break;
            case R.id.ivJunta:
                intent.setData(Uri.parse(getString(R.string.urlJunta)));
                break;
            case R.id.ivDbPiedia:
                intent.setData(Uri.parse(getString(R.string.urlDbpedia)));
                break;
            case R.id.ivWikidata:
                intent.setData(Uri.parse(getString(R.string.urlWikidata)));
                break;
            case R.id.tvOpenStreepMap:
                intent.setData(Uri.parse(getString(R.string.urlopenStreetMap)));
                break;
            case R.id.tvOsmdroid:
                intent.setData(Uri.parse(getString(R.string.urlOsmdroid)));
                break;
            case R.id.tvPhotoView:
                intent.setData(Uri.parse(getString(R.string.urlPhotoView)));
                break;
            case R.id.tvPicasso:
                intent.setData(Uri.parse(getString(R.string.urlPicasso)));
                break;
            case R.id.tvLicencia:
                intent.setData(Uri.parse(getString(R.string.urlLicencia)));
                break;
            default:
                return;
        }
        if(view.getId() != R.id.tvDesarrolladores)
            startActivity(intent);
    }

    /**
     * Método para recuperar a las personas implicadas en el proyecto.
     */
    private String desarrolladores() {
        List<String> nombres = new ArrayList<>();
        String salida = "";
        nombres.add(getString(R.string.pablo)+"\n");
        nombres.add(getString(R.string.adolfo)+"\n");
        int pos;
        Random random = new Random();
        while(nombres.size() > 0) {
            pos = random.nextInt(nombres.size());
            salida = salida.concat(nombres.remove(pos));
        }
        return salida;
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
    public void onSaveInstanceState(@NotNull Bundle b) {
        super.onSaveInstanceState(b);
        b.putString("TEXTOVERSION", version.getText().toString());
        //b.putBoolean("ENSENA", ensena);
    }
}
