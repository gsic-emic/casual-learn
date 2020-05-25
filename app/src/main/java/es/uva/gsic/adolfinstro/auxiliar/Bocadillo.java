package es.uva.gsic.adolfinstro.auxiliar;

import android.view.View;
import android.widget.TextView;

import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.infowindow.InfoWindow;

import es.uva.gsic.adolfinstro.R;


public class Bocadillo extends InfoWindow {

    public Bocadillo(int id, MapView map){
        super(id, map);
    }

    @Override
    public void onOpen(final Object item) {
        Marker marker = (Marker)item;
        TextView titulo = mView.findViewById(R.id.tvTituloBocadillo);
        titulo.setText(marker.getTitle());
        titulo = mView.findViewById(R.id.tvDistanciaBocadillo);
        titulo.setText(marker.getSubDescription());
        mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((Marker)item).closeInfoWindow();
            }
        });
    }

    @Override
    public void onClose() {
    }
}
