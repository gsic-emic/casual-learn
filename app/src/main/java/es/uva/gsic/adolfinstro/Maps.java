package es.uva.gsic.adolfinstro;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Toast;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.ItemizedOverlayWithFocus;
import org.osmdroid.views.overlay.OverlayItem;
import org.osmdroid.views.overlay.ScaleBarOverlay;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.util.ArrayList;

//https://github.com/osmdroid/osmdroid/wiki/How-to-use-the-osmdroid-library
public class Maps extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener{
    MapView map;
    MyLocationNewOverlay myLocationNewOverlay;
    IMapController mapController;
    double latitude=41.662357, longitude=-4.706005;
    final GeoPoint telecoPoint= new GeoPoint(latitude, longitude);
    boolean noMolestar;
    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        final Context context = getBaseContext();
        super.onCreate(savedInstanceState);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        onSharedPreferenceChanged(sharedPreferences, Ajustes.NO_MOLESTAR_pref);

        if(noMolestar){
            setContentView(R.layout.no_molestar);
        }
        else{
            setContentView(R.layout.activity_maps);
            map = findViewById(R.id.map);
            map.setTileSource(TileSourceFactory.MAPNIK);
            map.setMultiTouchControls(true);
            mapController = map.getController();
            mapController.setCenter(telecoPoint);
            mapController.setZoom(8.0);
            GpsMyLocationProvider gpsMyLocationProvider = new GpsMyLocationProvider(context);
            gpsMyLocationProvider.setLocationUpdateMinTime(10000);
            gpsMyLocationProvider.addLocationSource(LocationManager.GPS_PROVIDER);
            gpsMyLocationProvider.addLocationSource(LocationManager.NETWORK_PROVIDER);
            myLocationNewOverlay = new MyLocationNewOverlay( gpsMyLocationProvider, map);
            myLocationNewOverlay.enableMyLocation();
            myLocationNewOverlay.enableFollowLocation();
            myLocationNewOverlay.setEnableAutoStop(true);
            map.getOverlays().add(myLocationNewOverlay);


            final DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
            ScaleBarOverlay scaleBarOverlay = new ScaleBarOverlay(map);
            scaleBarOverlay.setCentred(true);
            scaleBarOverlay.setScaleBarOffset(displayMetrics.widthPixels / 2, 10);
            map.getOverlays().add(scaleBarOverlay);
            putItems();
        }
    }

    ArrayList<OverlayItem> items = new ArrayList<>();
    ArrayList<String> idItems = new ArrayList<>();

    public void putItems(){
        String id = "idPrueba";
        OverlayItem overlayItem = new OverlayItem( id,"PruebaTitulo", "PruebaDescripcion", new GeoPoint(42.662357, -4.706005));
        idItems.add(id);
        overlayItem.setMarker(getDrawable(R.drawable.marker_default));
        items.add(overlayItem);
        id = "idPlazaMayor";
        overlayItem = new OverlayItem(id, "Plaza Mayor", "Plaza Mayor Valladolid", new GeoPoint(41.6520, -4.7286));
        overlayItem.setMarker(getDrawable(R.drawable.marker_default));
        idItems.add(id);
        items.add(overlayItem);
        id = "idLaAntigua";
        overlayItem = new OverlayItem(id,"La Antigua", "La Antigua Valladolid", new GeoPoint(41.6547, -4.7231));
        overlayItem.setMarker(getDrawable(R.drawable.marker_default));
        items.add(overlayItem);
        pushItems();
    }

    public void putItem(OverlayItem overlayItem){
        final Context context = getApplicationContext();
        if(!items.contains(overlayItem.getUid())){
            items.add(overlayItem);
            pushItems();
        }
    }

    public void pushItems(){
        final Context context = getApplicationContext();
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
        if(map != null)
            map.onResume();
    }

    @Override
    public void onPause(){
        super.onPause();
        if(map != null)
            map.onPause();
    }


    public void boton(View view) {
        switch (view.getId()){
            case R.id.btCentrar:
                if(myLocationNewOverlay.getMyLocation() != null) {
                    mapController.setZoom(18.0);
                    mapController.setCenter(myLocationNewOverlay.getMyLocation());
                }
                break;
            case R.id.switchNoMolestar:
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean(Ajustes.NO_MOLESTAR_pref, false);
                editor.commit();
                Intent intent = new Intent (getApplicationContext(), Maps.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                break;
        }

    }

    @Override
    protected void onSaveInstanceState(Bundle bundle){
        if(myLocationNewOverlay!=null && myLocationNewOverlay.getMyLocation()!=null){
            latitude = myLocationNewOverlay.getMyLocation().getLatitude();
            longitude = myLocationNewOverlay.getMyLocation().getLongitude();
        }
        bundle.putDouble("LATITUDE", latitude);
        bundle.putDouble("LONGITUDE", longitude);
        super.onSaveInstanceState(bundle);
    }

    @Override
    protected void onRestoreInstanceState(Bundle bundle){
        super.onRestoreInstanceState(bundle);
        GeoPoint lastCenter = new GeoPoint(bundle.getDouble("LATITUDE"), bundle.getDouble("LONGITUDE"));
        latitude=bundle.getDouble("LATITUDE");
        longitude=bundle.getDouble("LONGITUDE");
        mapController.setCenter(lastCenter);
        putItem(new OverlayItem("otroPunto", "OtroPunto", "otroPunto", new GeoPoint(41.7520, -4.6286)));
    }

    /**
     * Método de actuación cuando se detecta un cambio en una de las preferencias
     * @param sharedPreferences
     * @param key
     */
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        switch (key){
            case Ajustes.NO_MOLESTAR_pref:
                noMolestar = sharedPreferences.getBoolean(key, false);
                break;
            default:
        }
    }
}
