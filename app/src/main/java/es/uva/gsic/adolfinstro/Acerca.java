package es.uva.gsic.adolfinstro;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

import es.uva.gsic.adolfinstro.auxiliar.Auxiliar;

/**
 * Clase diseñada para mostrar la información de la aplicación
 *
 * @author Pablo
 * @version 20201201
 */
public class Acerca extends AppCompatActivity {

    /** Instancia del cuadro del textview donde se coloca la versión*/
    private TextView version;

    /** Diálogo para mostrar a los desarrolladores */
    private Dialog dialogoDesarrolladores;

    /** Diálogo para mostrar los recursos de la Junta*/
    private Dialog dialogoJuntaCyL;

    /** Variable para saber si se está mostrando el dialogo de desarrolladores o no */
    private Boolean dialogoDesarrolladoresActivo;

    /** Objeto para saber si se está mostrando el dialogo de recursos de la Junta */
    private Boolean dialogoJuntaCyLActivo;

    /** Objeto con el contexto */
    private Context contexto;

    /**
     * Método para completar la interfaz gráfica del usuario. Se pinta la versión de la app.
     *
     * @param savedInstanceState bundle con la información almacenada antes de destruir la
     *                           actividad
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);

        setContentView(R.layout.activity_acerca);

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        contexto = this;

        version = findViewById(R.id.tvVersion);

        TextView proyectos = findViewById(R.id.tvFondosEuropeos);
        String texto = contexto.getResources().getString(R.string.proyectosSistema);
        proyectos.setText(Auxiliar.creaEnlaces(contexto, texto, true));
        proyectos.setMovementMethod(LinkMovementMethod.getInstance());

        if(savedInstanceState == null){
            try {
                version.setText(String.format("%s %s", contexto.getResources().getString(R.string.version),
                        contexto.getPackageManager().getPackageInfo(getPackageName(), 0).versionName));
            } catch (PackageManager.NameNotFoundException e) {
                version.setText(String.format("%s %d", contexto.getResources().getString(R.string.version), 0));

            }
        }else{
            version.setText(savedInstanceState.getString("TEXTOVERSION"));
        }

        dialogoDesarrolladores = new Dialog(contexto);
        dialogoDesarrolladores.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialogoDesarrolladores.setContentView(R.layout.dialogo_desarrolladores);
        dialogoDesarrolladores.setCancelable(true);
        dialogoDesarrolladores.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                dialogoDesarrolladoresActivo = false;
            }
        });
        TextView textView = dialogoDesarrolladores.findViewById(R.id.tvEspacioDesarrolladores);
        textView.setText(Html.fromHtml(contexto.getResources().getString(R.string.desarrolladores_nombres)));

        dialogoDesarrolladoresActivo = false;

        dialogoJuntaCyL = new Dialog(contexto);
        dialogoJuntaCyL.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialogoJuntaCyL.setContentView(R.layout.dialogo_recursos_junta);
        dialogoJuntaCyL.setCancelable(true);
        dialogoJuntaCyL.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                dialogoJuntaCyLActivo = false;
            }
        });

        dialogoJuntaCyLActivo = false;

        if(savedInstanceState != null){
            if(savedInstanceState.getBoolean("DIALOGODESARROLLADORES", false)) {
                dialogoDesarrolladoresActivo = true;
                dialogoDesarrolladores.show();
            }
            else{
                if(savedInstanceState.getBoolean("DIALOGOJUNTA", false)) {
                    dialogoJuntaCyLActivo = true;
                    dialogoJuntaCyL.show();
                }
            }
        }

        OnBackPressedCallback onBackPressedCallback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                finish();
            }
        };
        getOnBackPressedDispatcher().addCallback(this,onBackPressedCallback);
    }

    /**
     * Acciones que se toman al tocar los distintos botones de la pantalla.
     * @param view Vista
     */
    public void boton(View view) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.addCategory(Intent.CATEGORY_BROWSABLE);
        int id = view.getId();
        if (id == R.id.imagenGsic) {
            intent.setData(Uri.parse(contexto.getResources().getString(R.string.urlgsic)));
            startActivity(intent);
        } else if (id == R.id.imagenUva) {
            intent.setData(Uri.parse(contexto.getResources().getString(R.string.urluva)));
            startActivity(intent);
        } else if (id == R.id.tvDesarrolladores) {
            dialogoDesarrolladoresActivo = true;
            dialogoDesarrolladores.show();
        } else if (id == R.id.ivJunta) {
            dialogoJuntaCyLActivo = true;
            dialogoJuntaCyL.show();
        } else if (id == R.id.tvBICJunta) {
            intent.setData(Uri.parse(contexto.getResources().getString(R.string.urlBienesJunta)));
            startActivity(intent);
        } else if (id == R.id.tvMunicipiosJunta) {
            intent.setData(Uri.parse(contexto.getResources().getString(R.string.urlMunicipiosJunta)));
            startActivity(intent);
        } else if (id == R.id.ivDbPiedia) {
            intent.setData(Uri.parse(contexto.getResources().getString(R.string.urlDbpedia)));
            startActivity(intent);
        } else if (id == R.id.ivWikidata) {
            intent.setData(Uri.parse(contexto.getResources().getString(R.string.urlWikidata)));
            startActivity(intent);
        } else if (id == R.id.tvOpenStreepMap) {
            intent.setData(Uri.parse(contexto.getResources().getString(R.string.urlopenStreetMap)));
            startActivity(intent);
        } else if (id == R.id.tvOsmdroid) {
            intent.setData(Uri.parse(contexto.getResources().getString(R.string.urlOsmdroid)));
            startActivity(intent);
        } else if (id == R.id.tvPhotoView) {
            intent.setData(Uri.parse(contexto.getResources().getString(R.string.urlPhotoView)));
            startActivity(intent);
        } else if (id == R.id.tvPicasso) {
            intent.setData(Uri.parse(contexto.getResources().getString(R.string.urlPicasso)));
            startActivity(intent);
        } else if (id == R.id.tvLicencia) {
            intent.setData(Uri.parse(contexto.getResources().getString(R.string.urlLicencia)));
            startActivity(intent);
        } else if (id == R.id.tvPoliticaPrivacidad) {
            intent.setData(Uri.parse(contexto.getResources().getString(R.string.politica_url)));
            startActivity(intent);
        } else if (id == R.id.tvContacto) {
            intent = new Intent(Intent.ACTION_SEND);
            intent.setType("message/rfc822");
            String[] direcciones = {getString(R.string.emailCasualLearn)};
            intent.putExtra(Intent.EXTRA_EMAIL, direcciones);
            if (intent.resolveActivity(getPackageManager()) != null) {
                startActivity(intent);
            } else {
                Toast.makeText(contexto, getString(R.string.noEmail), Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * Método que recoge la pulsación del botón atrás de la barra.
     * @return False ya que este método no finaliza la actividad
     */
    @Override
    public boolean onSupportNavigateUp(){
        getOnBackPressedDispatcher().onBackPressed();
        return false;
    }

    /**
     * Se almacena el texto de la versión para no tener que recargarlo con
     * cada cierre.
     * @param b bundle donde se almacena el estado
     */
    @Override
    public void onSaveInstanceState(@NotNull Bundle b) {
        super.onSaveInstanceState(b);
        b.putString("TEXTOVERSION", version.getText().toString());
        b.putBoolean("DIALOGODESARROLLADORES", dialogoDesarrolladoresActivo);
        b.putBoolean("DIALOGOJUNTA", dialogoJuntaCyLActivo);
    }
}
