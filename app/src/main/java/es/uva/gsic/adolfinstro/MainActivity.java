package es.uva.gsic.adolfinstro;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.VideoView;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    private Button bt;
    private Button bt2;
    private TextView tv;
    private ImageView iv;
    private VideoView vv;
    private static final int requestCodePermissions = 1000;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bt = findViewById(R.id.btObtener);
        bt2 = findViewById(R.id.bt);
        tv = findViewById(R.id.tvTexto);
        iv = findViewById(R.id.iv);
        vv = findViewById(R.id.vv);
        checkPermissions();
    }

    @Override
    protected void onResume(){
        super.onResume();
        //startLocationUpdates();
    }

    @Override
    protected void onPause(){
        super.onPause();
        //stopLocationUpdates();
    }

    @Override
    protected void onStop(){
        super.onStop();
        //stopLocationUpdates();
    }

    /**
     * Método para comprobar si el usuario ha otorgado a la aplicación los permisos necesarios.
     * En la actualidad, solicita permisos de localización y cámara.
     */
    private void checkPermissions(){
        if(!getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA))
            System.exit(-1);
        ArrayList<String> permisos = new ArrayList<>();
        if(!(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED))
            permisos.add(Manifest.permission.ACCESS_FINE_LOCATION);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
            if(!(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED))
                permisos.add(Manifest.permission.ACCESS_BACKGROUND_LOCATION);
        if(!(ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED))
            permisos.add(Manifest.permission.CAMERA);
        if(!(ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED))
            permisos.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if(!(ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED))
            permisos.add(Manifest.permission.RECORD_AUDIO);
        if (permisos.size()>0) //Evitamos hacer una petición con un array nulo
            ActivityCompat.requestPermissions(this, permisos.toArray(new String[permisos.size()]), requestCodePermissions);
    }

    /**
     * Método que devuelve el resultado de la solicitud de permisos.
     * @param requestCode Código de la petición de permismos.
     * @param permissions Permisos que se han solicitado.
     * @param grantResults Valor otorgado por el usuario al permiso.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults){
        for(int i : grantResults){
            if(i == -1){
                AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
                alertBuilder.setTitle(getString(R.string.permi));
                alertBuilder.setMessage(getString(R.string.permiM));
                alertBuilder.setPositiveButton(getString(R.string.acept), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        checkPermissions();
                    }
                });
                alertBuilder.setNegativeButton(getString(R.string.exi), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        System.exit(0);
                    }
                });
                alertBuilder.show();
                break;
            }
        }
    }
    //static Location loca;
    public void btgetLocation(View view) {
        //Location loca;
        /*fusedLocationProviderClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if(location != null){
                    loca = location;
                }
            }
        });
        pintaNotificacion(loca);*/
        /*OneTimeWorkRequest uploadWorkRequest = new OneTimeWorkRequest.Builder(Proceso.class).setInitialDelay(1, TimeUnit.SECONDS).build();
        WorkManager.getInstance(this).enqueue(uploadWorkRequest);*/
        //OneTimeWorkRequest periodicWorkRequest = new OneTimeWorkRequest.Builder(Proceso.class).build();
        //WorkManager.getInstance().enqueueUniqueWork("Location", ExistingWorkPolicy.REPLACE, periodicWorkRequest);
        Intent takePicture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if(takePicture.resolveActivity(getPackageManager())!=null){
            File photoFile = null;
            try{
                photoFile = createImageFile();
            }catch (IOException e){

            }
            if(photoFile != null){
                photoURI = FileProvider.getUriForFile(this, "es.uva.gsic.adolfinstro.fileprovider", photoFile);

                takePicture.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePicture, 1);
            }
        }
    }
    Uri photoURI;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        if(requestCode == 1 && resultCode == RESULT_OK){
            tv.setText("FOTO");
            iv.setImageURI(photoURI);
        }
        if(requestCode == 2 && resultCode == RESULT_OK){
            tv.setText("VIDEO");
            //Uri videoUri = data.getData();
            //vv.setVideoURI(videoUri);
            //vv.resume();

        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    String currentPhotoPath;

    private File createImageFile() throws IOException{
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmssSSS").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDire = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(imageFileName, ".jpg", storageDire);
        currentPhotoPath = image.getAbsolutePath();
        tv.setText(currentPhotoPath);
        return image;
    }


    public void btVideo(View view) {
        Intent takeVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        if(takeVideoIntent.resolveActivity(getPackageManager()) != null){
            startActivityForResult(takeVideoIntent, 2);
        }
    }

    public void btSalto(View view) {
        Intent intent = new Intent(this, TaskCamera.class);
        startActivity(intent);
    }
}
