package es.uva.gsic.adolfinstro;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity{

    Button bt;
    TextView tv;
    HashMap<String, Integer> permisos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bt = findViewById(R.id.btObtener);
        tv = findViewById(R.id.tvTexto);

        permisos = new HashMap<>();
        checkPermissions();
    }

    /*@Override
    public void onResume(){
        checkPermissions();
        super.onResume();
    }*/

    private void checkPermissions(){
        ArrayList<String> permisos = new ArrayList<>();
        if(!(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED))
            permisos.add(Manifest.permission.ACCESS_COARSE_LOCATION);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
            if(!(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED))
                permisos.add(Manifest.permission.ACCESS_BACKGROUND_LOCATION);
        if(!(ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED))
            permisos.add(Manifest.permission.CAMERA);
        ActivityCompat.requestPermissions(this, permisos.toArray(new String[permisos.size()]), 1000);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults){
        for(int i : grantResults){
            if(i == -1){
                AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
                alertBuilder.setTitle("Faltan permisos");
                alertBuilder.setMessage("Se necesitan todos los permisos solicitados para el correcto funcionamiento de la app");
                alertBuilder.setPositiveButton("Continuar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        checkPermissions();
                    }
                });
                alertBuilder.setNegativeButton("Salir", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        System.exit(0);
                    }
                });
                alertBuilder.show();
            }
        }
    }
    public void btgetLocation(View view) {
        checkPermissions();

    }


    protected String createLocationRequest() {

        return "null";
    }
}
