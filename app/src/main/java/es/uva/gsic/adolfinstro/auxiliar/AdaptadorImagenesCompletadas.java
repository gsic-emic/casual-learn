package es.uva.gsic.adolfinstro.auxiliar;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import es.uva.gsic.adolfinstro.R;

/**
 * Clase para adaptar las imágenes y vídeos que ha realizado el usuario y mostrarla.
 *
 * @author pablo
 * @version 20200914
 */
public class AdaptadorImagenesCompletadas
        extends RecyclerView.Adapter<AdaptadorImagenesCompletadas.ViewHolder> {

    /**
     * Subclase con la estructura de los objetos donde se muestran las imágenes
     */
    public static class ViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener{
        ImageView imageView, aspas;

        /**
         * Constructor de la estructura
         * @param item Layout donde están las referencias
         */
        ViewHolder(final View item){
            super(item);
            imageView = item.findViewById(R.id.ivItemListaImagen);
            aspas = item.findViewById(R.id.ivBorrarMedia);
            imageView.setOnClickListener(this);
            aspas.setOnClickListener(this);
        }

        /**
         * Escucha de las pulsaciones en los objetos
         * @param v vista
         */
        @Override
        public void onClick(View v) {
            if(itemClick != null){
                itemClick.onItemClick(v, getAdapterPosition());
            }
        }
    }

    private List<ImagenesCamara> lista;
    private LayoutInflater layoutInflater;
    private static ItemClickListener itemClick;

    /**
     * Constructur de la clase
     * @param context Contexto
     * @param uris Lista de objetos donde se incluye el uri y el estado de visión de la papelera
     */
    public AdaptadorImagenesCompletadas(Context context, List<ImagenesCamara> uris){
        lista = uris;
        layoutInflater = LayoutInflater.from(context);
    }

    /**
     * Método para inflar el layout. Hace la llamada a la subclase para crear el objeto
     * @param parent Parent
     * @param viewType ViewType
     * @return Objeto de la subclase
     */
    @NonNull
    @Override
    public AdaptadorImagenesCompletadas.ViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent, int viewType) {
        View view = layoutInflater.inflate(R.layout.item_lista_imagenes, parent, false);
        return new ViewHolder(view);
    }

    /**
     * Método para establecer la imagen concreta en cada objeto. Establece la visibilidad de la
     * papelera
     * @param holder Objeto
     * @param position Posición en la lista
     */
    @Override
    public void onBindViewHolder(@NonNull AdaptadorImagenesCompletadas.ViewHolder holder, int position) {
        holder.imageView.setImageURI(lista.get(position).getDireccion());
        if(lista.get(position).getVisible() == View.VISIBLE){
            holder.aspas.setVisibility(View.VISIBLE);
            holder.imageView.setClickable(false);
        }else {
            holder.aspas.setVisibility(View.GONE);
            holder.imageView.setClickable(true);
        }
    }

    /**
     * Número de items de la lista
     * @return El número de lista
     */
    @Override
    public int getItemCount() {
        return lista.size();
    }

    /**
     * Método para establecer la pulsación sobre cada item de la lista
     * @param itemClickListener ItemClickListener
     */
    //https://stackoverflow.com/questions/40584424/simple-android-recyclerview-example
    public void setClickListener(ItemClickListener itemClickListener){
        AdaptadorImagenesCompletadas.itemClick = itemClickListener;
    }

    /**
     * Interfaz que establece el onclick de cada objeto de la lista
     */
    public interface ItemClickListener {
        /**
         * Establece vista y posición en la subclase
         * @param view vista
         * @param position posición
         */
        void onItemClick(View view, int position);
    }

}
