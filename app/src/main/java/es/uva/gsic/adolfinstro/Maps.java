package es.uva.gsic.adolfinstro;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.preference.PreferenceManager;

import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.ItemizedOverlay;
import org.osmdroid.views.overlay.ItemizedOverlayWithFocus;
import org.osmdroid.views.overlay.MinimapOverlay;
import org.osmdroid.views.overlay.OverlayItem;
import org.osmdroid.views.overlay.ScaleBarOverlay;
import org.osmdroid.views.overlay.compass.CompassOverlay;
import org.osmdroid.views.overlay.compass.InternalCompassOrientationProvider;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.IMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.util.ArrayList;

//https://github.com/osmdroid/osmdroid/wiki/How-to-use-the-osmdroid-library
public class Maps extends AppCompatActivity {
    MapView map;
    MyLocationNewOverlay myLocationNewOverlay;
    CompassOverlay compassOverlay;
    IMapController mapController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        final Context context = getApplicationContext();
        super.onCreate(savedInstanceState);
        Configuration.getInstance().load(context, PreferenceManager.getDefaultSharedPreferences(context));
        setContentView(R.layout.activity_maps);


        map = findViewById(R.id.map);
        map.setTileSource(TileSourceFactory.MAPNIK);
        map.setMultiTouchControls(true);
        mapController = map.getController();
        mapController.setZoom(18.0);
        myLocationNewOverlay = new MyLocationNewOverlay(new GpsMyLocationProvider(context), map);
        myLocationNewOverlay.enableMyLocation();
        myLocationNewOverlay.setEnableAutoStop(true);
        //GeoPoint startPoint = new GeoPoint(41.662357, -4.706005);
        map.getOverlays().add(myLocationNewOverlay);

        final DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        ScaleBarOverlay scaleBarOverlay = new ScaleBarOverlay(map);
        scaleBarOverlay.setCentred(true);
        scaleBarOverlay.setScaleBarOffset(displayMetrics.widthPixels / 2, 10);
        map.getOverlays().add(scaleBarOverlay);
        putItems();
    }

    ArrayList<OverlayItem> items = new ArrayList<>();

    public void putItems(){
        final Context context = getApplicationContext();
        OverlayItem overlayItem = new OverlayItem("PruebaTitulo", "PruebaDescripcion", new GeoPoint(42.662357, -4.706005));
        overlayItem.setMarker(getDrawable(R.drawable.zoom_in));
        items.add(overlayItem);
        overlayItem = new OverlayItem("Plaza Mayor", "Plaza Mayor Valladolid", new GeoPoint(41.6520, -4.7286));
        overlayItem.setMarker(getDrawable(R.drawable.moreinfo_arrow));
        items.add(overlayItem);
        overlayItem = new OverlayItem("La Antigua", "La Antigua Valladolid", new GeoPoint(41.6547, -4.7231));
        overlayItem.setMarker(getDrawable(R.drawable.marker_default));
        items.add(overlayItem);

        ItemizedOverlayWithFocus<OverlayItem> overlay = new ItemizedOverlayWithFocus<OverlayItem>(items, new ItemizedIconOverlay.OnItemGestureListener<OverlayItem>() {
            @Override
            public boolean onItemSingleTapUp(int index, OverlayItem item) {
                Toast.makeText(context, "Un toque"+item.getTitle(), Toast.LENGTH_SHORT).show();
                return false;
            }

            @Override
            public boolean onItemLongPress(int index, OverlayItem item) {
                Toast.makeText(context, "Toque largo "+item.getSnippet(), Toast.LENGTH_LONG).show();
                return false;
            }
        }, context);
        map.getOverlays().add(overlay);
    }

    @Override
    public void onResume(){
        super.onResume();
        map.onResume();
    }

    @Override
    public void onPause(){
        super.onPause();
        map.onPause();
    }



    public void boton(View view) {
        switch (view.getId()){
            case R.id.btCentrar:
                mapController.setZoom(18.0);
                mapController.setCenter(myLocationNewOverlay.getMyLocation());
                break;
        }

    }

    @Override
    protected void onSaveInstanceState(Bundle bundle){
        bundle.putDouble("LATITUDE", myLocationNewOverlay.getMyLocation().getLatitude());
        bundle.putDouble("LONGITUDE", myLocationNewOverlay.getMyLocation().getLongitude());
        super.onSaveInstanceState(bundle);
    }

    @Override
    protected void onRestoreInstanceState(Bundle bundle){
        super.onRestoreInstanceState(bundle);
        /*if(mapController==null)
            mapController=map.getController();
        if(myLocationNewOverlay==null) {
            final Context context = getApplicationContext();
            myLocationNewOverlay = new MyLocationNewOverlay(new GpsMyLocationProvider(context), map);
            myLocationNewOverlay.enableMyLocation();
            myLocationNewOverlay.setEnableAutoStop(true);
        }else{
            if(myLocationNewOverlay.getMyLocation()==null){
                final Context context = getApplicationContext();
                myLocationNewOverlay = new MyLocationNewOverlay(new GpsMyLocationProvider(context), map);
                myLocationNewOverlay.enableMyLocation();
                myLocationNewOverlay.setEnableAutoStop(true);
            }
        }*/
        GeoPoint lastCenter = new GeoPoint(bundle.getDouble("LATITUDE"), bundle.getDouble("LONGITUDE"));
        mapController.setCenter(lastCenter);
    }
}
