package es.uva.gsic.adolfinstro;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

class Auxiliar {

    private static final String[] listaFabricantes = {"huawei", "xiaomi"};

    /**
     * Creación del fichero donde se almacena la foto o el vídeo
     * @param type 1 foto, 2 vídeo
     * @return fichero donde se almacena la foto/vídeo
     * @throws IOException Se lanza una excepción cuando se produzca un error al crear el fichero vacío
     */
    static File createFile(int type, Context context) throws IOException{
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmssSSS").format(new Date());
        File mediaFile = null;
        switch (type){
            case 0:
            case 1:
            case 2:
                mediaFile = File.createTempFile("JPG_"+timeStamp,".jpg", context.getExternalFilesDir(Environment.DIRECTORY_PICTURES));
                break;
            case 3:
                mediaFile = File.createTempFile("VID_"+timeStamp, ".mp4", context.getExternalFilesDir(Environment.DIRECTORY_MOVIES));
                break;
            default:
        }
        return mediaFile;
    }

    /**
     * Método utilizado para volver a la actividad principal
     */
    static void returnMain(Context context){
        Intent intent = new Intent(context, Maps.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        context.startActivity(intent);
    }

    /**
     * Impreme un toast para indicar al usuario que ha sucedido algún problema
     */
    static void errorToast(Context context) {
        Toast.makeText(context, context.getString(R.string.errorOpera), Toast.LENGTH_SHORT).show();
    }

    /**
     * Método para preparar la petición de los permisos necesarios al usuario
     * @param context Contexto
     * @param permisos Vector donde agregar los permisos a los que el usuario aún no haya dado permiso
     */
    static void preQueryPermisos(Context context, ArrayList<String> permisos){
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

    /**
     * Método que muestra el mensaje de la lista blanca
     * @param context contexto
     * @param sharedPreferences preferencias
     */
    static void dialogoAyudaListaBlanca(Context context, final SharedPreferences sharedPreferences){
        List<String> fabricantesProblemas = Arrays.asList(listaFabricantes);
        if (fabricantesProblemas.contains(Build.MANUFACTURER.toLowerCase())) {
            AlertDialog.Builder brandBuilder = new AlertDialog.Builder(context);
            brandBuilder.setTitle(context.getString(R.string.tituloErrorMarca) + Build.MANUFACTURER.toLowerCase())
                    .setMessage(context.getString(R.string.mensajeSolucionMarca))
                    .setPositiveButton(context.getString(R.string.acept), new Dialog.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            actualizaListaBlanca(false, sharedPreferences);
                        }
                    })
                    .setNegativeButton(context.getString(R.string.cancel), new Dialog.OnClickListener(){
                        @Override
                        public void onClick(DialogInterface dialog, int which) { }
                    });
            brandBuilder.show();
        }
        else{
            actualizaListaBlanca(false, sharedPreferences);
        }
    }

    /**
     * Método para actualizar el valor de la preferencia listaBlanca
     * @param listaBlanca valor que va a tomar la preferencia
     * @param sharedPreferences preferencias
     */
    private static void actualizaListaBlanca(boolean listaBlanca, SharedPreferences sharedPreferences) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(Ajustes.LISTABLANCA_pref, listaBlanca);
        editor.commit();
    }

}
