package es.uva.gsic.adolfinstro.auxiliar;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import es.uva.gsic.adolfinstro.ListaTareas;
import es.uva.gsic.adolfinstro.R;

public class AdaptadorLista extends RecyclerView.Adapter<AdaptadorLista.ViewHolder> {

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView tvTitulo;
        ImageView ivTipoTarea;
        TextView tvFecha;
        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitulo = itemView.findViewById(R.id.tvListaTitulo);
            ivTipoTarea = itemView.findViewById(R.id.ivListaTipoTarea);
            tvFecha = itemView.findViewById(R.id.tvListaFechaTarea);
            tvTitulo.setOnClickListener(this);
        }

        /**
         * Called when a view has been clicked.
         *
         * @param v The view that was clicked.
         */
        @Override
        public void onClick(View v) {
            if(itemClickListener != null)
                itemClickListener.onItemClick(v, getAdapterPosition());
        }
    }

    private List<ListaTareas.TareasLista> lista;
    private LayoutInflater layoutInflater;
    private static ItemClickListener itemClickListener;

    public AdaptadorLista(Context c, List<ListaTareas.TareasLista> l){
        lista = l;
        layoutInflater = LayoutInflater.from(c);
    }

    @NonNull
    @Override
    public AdaptadorLista.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = layoutInflater.inflate(R.layout.item_lista, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AdaptadorLista.ViewHolder holder, int position) {
        holder.tvTitulo.setText(lista.get(position).titulo);
        holder.ivTipoTarea.setImageResource(Auxiliar.iconoTipoTarea(lista.get(position).tipoTarea));
        holder.tvFecha.setText(lista.get(position).fecha);
    }

    /**
     * Returns the total number of items in the data set held by the adapter.
     *
     * @return The total number of items in this adapter.
     */
    @Override
    public int getItemCount() {
        return lista.size();
    }

    public String getItem(int id){
        return lista.get(id).id;
    }

    //https://stackoverflow.com/questions/40584424/simple-android-recyclerview-example
    public void setClickListener(ItemClickListener itemClickListener){
        AdaptadorLista.itemClickListener = itemClickListener;
    }

    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }

}
