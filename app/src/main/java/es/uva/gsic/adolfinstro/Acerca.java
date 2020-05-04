package es.uva.gsic.adolfinstro;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

public class Acerca extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_acerca);
    }

    public void boton(View view) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.addCategory(Intent.CATEGORY_BROWSABLE);
        switch (view.getId()){
            case R.id.imagenGsic:
                intent.setData(Uri.parse("https://www.gsic.uva.es"));
                break;
            case R.id.imagenUva:
                intent.setData(Uri.parse("https://www.uva.es"));
                break;
            case R.id.tvOpenStreetMap:
                intent.setData(Uri.parse("https://www.openstreetmap.org/copyright"));
                break;
            case R.id.tvOsmdroid:
                intent.setData(Uri.parse("https://github.com/osmdroid/osmdroid"));
                break;
            case R.id.tvPhotoView:
                intent.setData(Uri.parse("https://github.com/chrisbanes/PhotoView"));
                break;
            case R.id.tvFused:
                intent.setData(Uri.parse("https://developers.google.com/location-context/fused-location-provider"));
                break;
            default:
                return;
        }
        startActivity(intent);
    }
}
