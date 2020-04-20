package es.uva.gsic.adolfinstro;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;


public class Ajustes extends AppCompatActivity{

    public static final String NO_MOLESTAR_pref = "noMolestar";
    public static final String INTERVALODIA_pref = "intervaloD";
    public static final String INTERVALOHORA_pref = "intervalo";
    public static final String INTERVALOMIN_pref = "intervaloM";
    public static final String FINDES_pref = "tareasFindes";
    public static final String LISTABLANCA_pref = "listaBlanca";
    public static final String TOKEN_pref = "token";
    public static SharedPreferences sharedPreferences;

    @Override
    public void onCreate(Bundle sIS){
        super.onCreate(sIS);
        setContentView(R.layout.activity_ajustes);
        getSupportFragmentManager().beginTransaction().replace(R.id.container_ajustes, new AjustesFragment()).commit();
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onBackPressed(){
        super.onBackPressed();
        Intent intent = new Intent(this, Maps.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        this.finish();

    }
}

