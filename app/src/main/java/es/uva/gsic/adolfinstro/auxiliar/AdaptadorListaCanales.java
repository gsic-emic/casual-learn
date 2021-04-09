package es.uva.gsic.adolfinstro.auxiliar;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import es.uva.gsic.adolfinstro.R;

/**
 * Adaptador para la selección de canales
 *
 * @author Pablo
 * @version 20210216
 */
public class AdaptadorListaCanales extends RecyclerView.Adapter<AdaptadorListaCanales.ViewHolderCanales> {

    public static class ViewHolderCanales extends RecyclerView.ViewHolder implements View.OnClickListener {
        CheckBox cbMarcado;
        TextView tvTitulo, tvDescripcion, tvAutorCanal;

        ViewHolderCanales(@NonNull View view){
            super(view);
            cbMarcado = view.findViewById(R.id.cbItemCanal);
            tvTitulo = view.findViewById(R.id.tituloItemCanal);
            tvDescripcion = view.findViewById(R.id.descripcionItemCanal);
            tvAutorCanal = view.findViewById(R.id.tvAutorCanal);
            cbMarcado.setOnClickListener(this);
        }

        @Override
        public void onClick(View view){
            if (view.getId() == R.id.cbItemCanal) {
                if (itemClickCbCanal != null)
                    itemClickCbCanal.onItemClickCb(view, getAdapterPosition());
            }
        }
    }

    private Context context;
    private List<Canal> listaCanales;
    private LayoutInflater layoutInflater;
    private static ItemClickCbCanal itemClickCbCanal;

    public AdaptadorListaCanales(Context context, List<Canal> listaCanales){
        this.context = context;
        layoutInflater = LayoutInflater.from(context);
        this.listaCanales = listaCanales;
    }

    @NonNull
    @Override
    public AdaptadorListaCanales.ViewHolderCanales onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType){
        return new ViewHolderCanales(layoutInflater.inflate(R.layout.item_lista_canales, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolderCanales holder, int position) {
        Canal canal = listaCanales.get(position);
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
        holder.cbMarcado.setChecked(canal.isMarcado());
        holder.cbMarcado.setEnabled(!canal.getTipo().equals(Canal.obligatorio));
        if(Auxiliar.stringVacio(canal.getDetallesAutor()))
            holder.tvAutorCanal.setVisibility(View.GONE);
        else{
            holder.tvAutorCanal.setText(canal.getDetallesAutor());
            holder.tvAutorCanal.setVisibility(View.VISIBLE);
        }
        holder.tvTitulo.setText(canal.getTitulo());
        holder.tvDescripcion.setText(canal.getDescripcion());
    }

    @Override
    public int getItemCount() { return listaCanales.size(); }

    public void actualizarPosicionLista(List<Canal> listaCanales, int position){
        this.listaCanales = listaCanales;
        notifyItemChanged(position);
    }

    public void actualizaLista(List<Canal> listaCanales){
        this.listaCanales = listaCanales;
        notifyDataSetChanged();
    }

    public String getId(int position) { return listaCanales.get(position).getId(); }

    public void setItemClickCbCanal(ItemClickCbCanal iCbCanal){ itemClickCbCanal = iCbCanal; }

    public interface ItemClickCbCanal { void onItemClickCb(View view, int position);}
}
