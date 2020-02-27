package es.uva.gsic.adolfinstro;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;


public class Ajustes extends AppCompatActivity{

    public static final String NO_MOLESTAR_pref = "noMolestar";
    public static final String INTERVALO_pref = "intervalo";
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
        Log.i("Destruyo ajustes", "Destruyo ajustes");
        super.onDestroy();
    }
}

