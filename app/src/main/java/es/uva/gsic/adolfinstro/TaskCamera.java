package es.uva.gsic.adolfinstro;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.FileProvider;

import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class TaskCamera extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_camera);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        final Context context = getBaseContext();
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent takePicture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if(takePicture.resolveActivity(getPackageManager())!=null){
                    File photoFile = null;
                    try{
                        photoFile = createImageFile();
                    }catch (IOException e){

                    }
                    if(photoFile != null){
                        photoURI = FileProvider.getUriForFile(context, "es.uva.gsic.adolfinstro.fileprovider", photoFile);

                        takePicture.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                        startActivityForResult(takePicture, 1);
                    }
                }
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    Uri photoURI;

    String currentPhotoPath;

    private File createImageFile() throws IOException{
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmssSSS").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDire = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(imageFileName, ".jpg", storageDire);
        currentPhotoPath = image.getAbsolutePath();
        //tv.setText(currentPhotoPath);
        return image;
    }



}
