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

import es.uva.gsic.adolfinstro.Maps;
import es.uva.gsic.adolfinstro.R;

public class AdaptadorListaMapa extends RecyclerView.Adapter<AdaptadorListaMapa.ViewHolderMapa> {

    public static class ViewHolderMapa extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView tvTitulo;
        ImageView ivTipoTarea;
        ViewHolderMapa(@NonNull View itemView){
            super(itemView);
            tvTitulo = itemView.findViewById(R.id.tvItemListaMapa);
            ivTipoTarea = itemView.findViewById(R.id.ivItemListaMapa);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v){
            if(itemClickListenerMapa != null)
                itemClickListenerMapa.onItemClick(v, getAdapterPosition());
        }
    }

    private List<Maps.TareasMapaLista> lista;
    private LayoutInflater layoutInflater;
    private static ItemClickListener itemClickListenerMapa;

    public AdaptadorListaMapa(Context context, List<Maps.TareasMapaLista> lista){
        this.lista = lista;
        layoutInflater = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public AdaptadorListaMapa.ViewHolderMapa onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = layoutInflater.inflate(R.layout.item_lista_mapa, parent, false);
        return new ViewHolderMapa(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AdaptadorListaMapa.ViewHolderMapa holder, int position){
        holder.tvTitulo.setText(lista.get(position).titulo);
        holder.ivTipoTarea.setImageResource(Auxiliar.iconoTipoTarea(lista.get(position).tipoTarea));
    }

    @Override
    public int getItemCount(){ return lista.size();}

    public String getId(int posicion) { return lista.get(posicion).id; }

    public void setClickListener(ItemClickListener itemClickListener){
        itemClickListenerMapa = itemClickListener;
    }

    public interface ItemClickListener{
        void onItemClick(View view, int position);
    }
}
