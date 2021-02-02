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
 * Clase para lista de contextos que se le han notificado al usuario
 *
 * @author Pablo
 * @version 20210202
 */
public class AdaptadorListaContextos extends RecyclerView.Adapter<AdaptadorListaContextos.ViewHolder> {
    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView label, fecha;
        ViewHolder(@NonNull View itemView) {
            super(itemView);
            label = itemView.findViewById(R.id.tvTituloListaContextos);
            fecha = itemView.findViewById(R.id.tvFechaListaContextos);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (itemClickListener != null) {
                itemClickListener.onItemClick(view, getAdapterPosition());
            }
        }
    }

    private List<ContextoLista> contextos;
    private Context context;
    private static ItemClickListenerContexto itemClickListener;
    private LayoutInflater layoutInflater;


    public AdaptadorListaContextos(Context context, List<ContextoLista> contextos) {
        this.contextos = contextos;
        layoutInflater = LayoutInflater.from(context);
        this.context = context;
    }

    @NonNull
    @Override
    public AdaptadorListaContextos.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = layoutInflater.inflate(R.layout.item_lista_contextos, parent, false);
        return new AdaptadorListaContextos.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ContextoLista contexto = contextos.get(position);
        holder.label.setText(contexto.getLabel());
        holder.fecha.setText(String.format("%s %s", context.getResources().getString(R.string.instanteNotificacion), contexto.getFecha()));
    }

    @Override
    public int getItemCount() {
        return contextos.size();
    }

    public String getIdContexto(int position){
        return contextos.get(position).getIdContexto();
    }

    public interface ItemClickListenerContexto {
        void onItemClick(View view, int position);
    }

    public void setClickListener(ItemClickListenerContexto icl){
        itemClickListener = icl;
    }

}
