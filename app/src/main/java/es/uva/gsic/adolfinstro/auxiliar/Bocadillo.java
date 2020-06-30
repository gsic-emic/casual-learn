package es.uva.gsic.adolfinstro.auxiliar;

import android.view.View;
import android.widget.TextView;

import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.infowindow.InfoWindow;

import es.uva.gsic.adolfinstro.R;

/**
 * Clase para representar el bocadillo de información de cada marcador cuando el usuario lo pulse.
 *
 * @author pablo
 * @version 20200626
 */
public class Bocadillo extends InfoWindow {

    /**
     * Constructor de la clase
     * @param id Identificador único del layout que se va a utilizar para representar al bocadillo
     * @param map Mapa
     */
    public Bocadillo(int id, MapView map){
        super(id, map);
    }

    /**
     * Método para inflar al bocadillo con la información específica del marcador
     * @param item Marcador (Object, hay que realizar un cast)
     */
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

    /**
     * Método de cierre del bocadillo
     */
    @Override
    public void onClose() {
    }
}
