package es.uva.gsic.adolfinstro.auxiliar;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import es.uva.gsic.adolfinstro.R;

/**
 * Clase para mostrar las coincidencias de la búsqueda que realice el usuario
 *
 * @author Pablo
 * @version 20200914
 */
public class AdaptadorListaCoincidencia extends RecyclerView.Adapter<AdaptadorListaCoincidencia.ViewHolder> {

    /**
     * Subclase para establecer el formato de cada uno de los objetos.
     */
    public static class ViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener{
        TextView tvContenidoM, tvContenidoP;

        /**
         * Constructor de la subclase. Fija las referencias.
         * @param itemView ItemView
         */
        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvContenidoM = itemView.findViewById(R.id.tvcontenidoCoincidenciaMunicipio);
            tvContenidoP = itemView.findViewById(R.id.tvcontenidoCoincidenciaProvincia);
            itemView.setOnClickListener(this);
        }

        /**
         * Método que establece lo que sucede cuando se pulsa sobre el objeto
         * @param v Vista
         */
        @Override
        public void onClick(View v) {
            if(itemClickListenerDialogo != null)
                itemClickListenerDialogo.onItemClickDialogo(v, getAdapterPosition());
        }
    }

    private List<ListaCoincidencias> lista;
    private LayoutInflater layoutInflater;
    private static ItemClickListenerDialogo itemClickListenerDialogo;

    /**
     * Constructor de la clase
     * @param c Contexto
     * @param l Lista de objetos con la información necesaria de la cada una de las tareas
     */
    public AdaptadorListaCoincidencia(Context c, List<ListaCoincidencias> l){
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
    public AdaptadorListaCoincidencia.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = layoutInflater.inflate(R.layout.item_lista_coincidencias, parent, false);
        return new ViewHolder(view);
    }

    /**
     * Método para establecer dentro del contenedor el contendido de cada objeto
     * @param holder Holder
     * @param position Posición
     */
    @Override
    public void onBindViewHolder(@NonNull AdaptadorListaCoincidencia.ViewHolder holder, int position) {
        holder.tvContenidoM.setText(lista.get(position).getMunicipio());
        holder.tvContenidoP.setText(lista.get(position).getProvincia());
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
    public double getLatitud(int posicion){
        return lista.get(posicion).getLatitud();
    }

    /**
     * Método para obtener la longitud del item
     * @param posicion Posición que ocupa el item
     * @return Longitud del item
     */
    public double getLongitud(int posicion){
        return lista.get(posicion).getLongitud();
    }

    /**
     * Método que establece el click simple
     * @param itemClickListenerDialogo ItemClickListenerDialogo
     */
    public void setClickListenerDialogo(ItemClickListenerDialogo itemClickListenerDialogo){
        AdaptadorListaCoincidencia.itemClickListenerDialogo = itemClickListenerDialogo;
    }

    /**
     * Interfaz para establecer el click simple
     */
    public interface ItemClickListenerDialogo {
        void onItemClickDialogo(View view, int position);
    }

}
