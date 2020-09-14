package es.uva.gsic.adolfinstro.auxiliar;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import es.uva.gsic.adolfinstro.R;

/**
 * Clase para ver la lista de tareas completadas, pendientes o realizadas dentro de un contenedor.
 *
 * @author pablo
 * @version 20200914
 */
public class AdaptadorLista extends RecyclerView.Adapter<AdaptadorLista.ViewHolder> {

    /**
     * Subclase para establecer el formato de cada uno de los objetos.
     */
    public static class ViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener, View.OnLongClickListener{
        TextView tvTitulo, tvFecha;
        ImageView ivTipoTarea;
        RatingBar ratingBar;

        /**
         * Constructor de la subclase. Fija cada uno de las referencias dle layout.
         * @param itemView ItemView
         */
        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitulo = itemView.findViewById(R.id.tvListaTitulo);
            ivTipoTarea = itemView.findViewById(R.id.ivListaTipoTarea);
            tvFecha = itemView.findViewById(R.id.tvListaFechaTarea);
            ratingBar = itemView.findViewById(R.id.rbLista);
            itemView.setOnClickListener(this);
            ivTipoTarea.setOnClickListener(this);
            ivTipoTarea.setOnLongClickListener(this);
        }

        /**
         * Método que establece lo que sucede cuando se pulsa sobre el objeto
         * @param v Vista
         */
        @Override
        public void onClick(View v) {
            if(itemClickListener != null)
                itemClickListener.onItemClick(v, getAdapterPosition());
        }

        /**
         * Método que establece lo que sucede cuando se pulsa sobre el objeto con una pulsación
         * larga
         * @param v vista
         * @return Verdadero
         */
        @Override
        public boolean onLongClick(View v){
            if(itemLongClickLister != null)
                itemLongClickLister.onItemLongClick(v, getAdapterPosition());
            return true;
        }
    }

    private List<TareasLista> lista;
    private LayoutInflater layoutInflater;
    private static ItemClickListener itemClickListener;
    private static ItemLongClickLister itemLongClickLister;

    /**
     * Constructor de la clase
     * @param c Contexto
     * @param l Lista de objetos con la información necesaria de la cada una de las tareas
     */
    public AdaptadorLista(Context c, List<TareasLista> l){
        lista = l;
        layoutInflater = LayoutInflater.from(c);
    }

    /**
     * Inflador
     * @param parent Parent
     * @param viewType ViewType
     * @return Objeto que forma la lista
     */
    @NonNull
    @Override
    public AdaptadorLista.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = layoutInflater.inflate(R.layout.item_lista, parent, false);
        return new ViewHolder(view);
    }

    /**
     * Método para establecer dentro del contenedor el contendido de cada objeto
     * @param holder Holder
     * @param position Posición
     */
    @Override
    public void onBindViewHolder(@NonNull AdaptadorLista.ViewHolder holder, int position) {
        holder.tvTitulo.setText(lista.get(position).getTitulo());
        holder.ivTipoTarea.setImageResource(Auxiliar.iconoTipoTarea(lista.get(position).getTipoTarea()));
        holder.tvFecha.setText(lista.get(position).getFecha());
        float puntuacion = lista.get(position).getPuntuacion();
        if(puntuacion >= 0){
            holder.ratingBar.setRating(puntuacion);
            holder.ratingBar.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Método para obtener el número items que forman la lista
     * @return Número de items que forman la lista
     */
    @Override
    public int getItemCount() {
        return lista.size();
    }

    /**
     * Método para obtener el identificador del marcador
     * @param posicion Posición que ocupa el objeto
     * @return Identificador del marcador
     */
    public String getId(int posicion){
        return lista.get(posicion).getId();
    }

    /**
     * Método para obtener el tipo del marcador
     * @param posicion Posición que ocupa el objeto
     * @return Tipo de respuesta esperada del marcador
     */
    public String getTipo(int posicion){
        return lista.get(posicion).getTipoTarea();
    }

    /**
     * Mëtodo que establece el click simple
     * @param itemClickListener ItemClickListener
     */
    public void setClickListener(ItemClickListener itemClickListener){
        AdaptadorLista.itemClickListener = itemClickListener;
    }

    /**
     * Método que esteablece la pulsación larga
     * @param itemLongClickLister ItemLongClickLister
     */
    public void setLongClickLister(ItemLongClickLister itemLongClickLister){
        AdaptadorLista.itemLongClickLister = itemLongClickLister;
    }

    /**
     * Interfaz para establecer el click simple
     */
    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }

    /**
     * Interfaz para establecer el click largo
     */
    public interface ItemLongClickLister {
        void onItemLongClick(View v, int position);
    }

}
