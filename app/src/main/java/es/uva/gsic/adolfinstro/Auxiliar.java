package es.uva.gsic.adolfinstro;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class Auxiliar {

    /**
     * Creación del fichero donde se almacena la foto o el vídeo
     * @param type 1 foto, 2 vídeo
     * @return fichero donde se almacena la foto/vídeo
     * @throws IOException Se lanza una excepción cuando se produzca un error al crear el fichero vacío
     */
    public static File createFile(int type, Context context) throws IOException{
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmssSSS").format(new Date());
        File mediaFile = null;
        switch (type){
            case 1:
                mediaFile = File.createTempFile("JPG_"+timeStamp,".jpg", context.getExternalFilesDir(Environment.DIRECTORY_PICTURES));
                break;
            case 2:
                mediaFile = File.createTempFile("VID_"+timeStamp, ".mp4", context.getExternalFilesDir(Environment.DIRECTORY_MOVIES));
                break;
            default:
        }
        return mediaFile;
    }

    /**
     * Método utilizado para volver a la actividad principal
     */
    public static void returnMain(Context context){
        Intent intent = new Intent(context, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        context.startActivity(intent);
    }

    /**
     * Impreme un toast para indicar al usuario que ha sucedido algún problema
     */
    public static void errorToast(Context context) {
        Toast.makeText(context, context.getString(R.string.errorOpera), Toast.LENGTH_SHORT).show();
    }

    public static void preQueryPermisos(Context context, ArrayList<String> permisos){
        if(!(ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED))
            permisos.add(Manifest.permission.ACCESS_FINE_LOCATION);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
            if(!(ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED))
                permisos.add(Manifest.permission.ACCESS_BACKGROUND_LOCATION);
        if(!(ActivityCompat.checkSelfPermission(context, Manifest.permission.INTERNET) == PackageManager.PERMISSION_GRANTED))
            permisos.add(Manifest.permission.INTERNET);
        if(!(ActivityCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED))
            permisos.add(Manifest.permission.CAMERA);
        if(!(ActivityCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED))
            permisos.add(Manifest.permission.RECORD_AUDIO);
        if(!(ActivityCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED))
            permisos.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
    }

}
