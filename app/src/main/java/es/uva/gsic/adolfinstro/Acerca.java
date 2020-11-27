package es.uva.gsic.adolfinstro;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.TextView;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import es.uva.gsic.adolfinstro.auxiliar.Auxiliar;

/**
 * Clase diseñada para mostrar la información de la aplicación
 *
 * @author Pablo
 * @version 20200701
 */
public class Acerca extends AppCompatActivity {

    /** Instancia del cuadro del textview donde se coloca la versión*/
    TextView version;

    /** Dialogo para mostrar a los desarrolladores */
    Dialog dialogoDesarrolladores;

    /** Variable para saber si se está mostrando el dialogo de desarrolladores o no */
    Boolean dialogoDesarrolladoresActivo;

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

        TextView proyectos = findViewById(R.id.tvFondosEuropeos);
        String texto = getString(R.string.proyectosSistema);
        proyectos.setText(Auxiliar.creaEnlaces(this, texto, true));
        proyectos.setMovementMethod(LinkMovementMethod.getInstance());

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

        dialogoDesarrolladores = new Dialog(this);
        dialogoDesarrolladores.setContentView(R.layout.dialogo_desarrolladores);
        dialogoDesarrolladores.setCancelable(true);
        dialogoDesarrolladores.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                dialogoDesarrolladoresActivo = false;
            }
        });
        TextView textView = dialogoDesarrolladores.findViewById(R.id.tvEspacioDesarrolladores);
        textView.setText(Html.fromHtml(getResources().getString(R.string.desarrolladores_nombres)));

        dialogoDesarrolladoresActivo = false;

        if(savedInstanceState != null && savedInstanceState.getBoolean("DIALOGODESARROLLADORES", false)){
            dialogoDesarrolladoresActivo = true;
            dialogoDesarrolladores.show();
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
                intent.setData(Uri.parse(getString(R.string.urlgsic)));
                startActivity(intent);
                break;
            case R.id.imagenUva:
                intent.setData(Uri.parse(getString(R.string.urluva)));
                startActivity(intent);
                break;
            case R.id.tvDesarrolladores:
                dialogoDesarrolladoresActivo = true;
                dialogoDesarrolladores.show();
                break;
            case R.id.ivJunta:
                intent.setData(Uri.parse(getString(R.string.urlJunta)));
                startActivity(intent);
                break;
            case R.id.ivDbPiedia:
                intent.setData(Uri.parse(getString(R.string.urlDbpedia)));
                startActivity(intent);
                break;
            case R.id.ivWikidata:
                intent.setData(Uri.parse(getString(R.string.urlWikidata)));
                startActivity(intent);
                break;
            case R.id.tvOpenStreepMap:
                intent.setData(Uri.parse(getString(R.string.urlopenStreetMap)));
                startActivity(intent);
                break;
            case R.id.tvOsmdroid:
                intent.setData(Uri.parse(getString(R.string.urlOsmdroid)));
                startActivity(intent);
                break;
            case R.id.tvPhotoView:
                intent.setData(Uri.parse(getString(R.string.urlPhotoView)));
                startActivity(intent);
                break;
            case R.id.tvPicasso:
                intent.setData(Uri.parse(getString(R.string.urlPicasso)));
                startActivity(intent);
                break;
            case R.id.tvLicencia:
                intent.setData(Uri.parse(getString(R.string.urlLicencia)));
                startActivity(intent);
                break;
            default:
                break;
        }
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
        b.putBoolean("DIALOGODESARROLLADORES", dialogoDesarrolladoresActivo);
    }
}
