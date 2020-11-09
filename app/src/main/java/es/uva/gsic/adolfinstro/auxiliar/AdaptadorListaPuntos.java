package es.uva.gsic.adolfinstro.auxiliar;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONObject;

import java.util.List;

import es.uva.gsic.adolfinstro.R;

/**
 * Clase para mostrar las coincidencias de la búsqueda que realice el usuario
 *
 * @author Pablo
 * @version 20200914
 */
public class AdaptadorListaPuntos extends RecyclerView.Adapter<AdaptadorListaPuntos.ViewHolder> {

    /**
     * Subclase para establecer el formato de cada uno de los objetos.
     */
    public static class ViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener{
        TextView tvTitulo, tvDistancia;

        /**
         * Constructor de la subclase. Fija las referencias.
         * @param itemView ItemView
         */
        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitulo = itemView.findViewById(R.id.tvVariosPuntosTitulo);
            tvDistancia = itemView.findViewById(R.id.tvVariosPuntosDistancia);
            itemView.setOnClickListener(this);
        }

        /**
         * Método que establece lo que sucede cuando se pulsa sobre el objeto
         * @param v Vista
         */
        @Override
        public void onClick(View v) {
            if(item != null)
                item.onItemClickDialogoVariosPuntos(v, getAdapterPosition());
        }
    }

    private List<PuntoSingular> lista;
    private LayoutInflater layoutInflater;
    private static ItemClickListenerDialogoVariosPuntos item;

    /**
     * Constructor de la clase
     * @param c Contexto
     * @param l Lista de objetos con la información necesaria de la cada una de las tareas
     */
    public AdaptadorListaPuntos(Context c, List<PuntoSingular> l){
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
    public AdaptadorListaPuntos.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = layoutInflater.inflate(R.layout.item_lista_varios_puntos_interes, parent, false);
        return new ViewHolder(view);
    }

    /**
     * Método para establecer dentro del contenedor el contendido de cada objeto
     * @param holder Holder
     * @param position Posición
     */
    @Override
    public void onBindViewHolder(@NonNull AdaptadorListaPuntos.ViewHolder holder, int position) {
        holder.tvTitulo.setText(lista.get(position).getTitulo());
        holder.tvDistancia.setText(String.format("%.3f km",lista.get(position).getDistancia()));
    }

    /**
     * Método para obtener el número de items
     * @return Número de items que forman la lista
     */
    @Override
    public int getItemCount() {
        return lista.size();
    }

    /**
     * Método para obtener la latitud del item
     * @param posicion posición que ocupa el item en la lista
     * @return Latitud del item
     */
    public JSONObject getPunto(int posicion){
        return lista.get(posicion).getPunto();
    }

    /**
     * Método que establece el click simple
     * @param item Item click
     */
    public void setClickListenerDialogo(ItemClickListenerDialogoVariosPuntos item){
        AdaptadorListaPuntos.item = item;
    }

    /**
     * Interfaz para establecer el click simple
     */
    public interface ItemClickListenerDialogoVariosPuntos {
        void onItemClickDialogoVariosPuntos(View view, int position);
    }

}
