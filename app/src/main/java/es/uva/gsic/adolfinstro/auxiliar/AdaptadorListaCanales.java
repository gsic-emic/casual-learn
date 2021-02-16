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
        TextView tvTitulo, tvDescripcion;
        ImageView ivMarcador;

        ViewHolderCanales(@NonNull View view){
            super(view);
            cbMarcado = view.findViewById(R.id.cbItemCanal);
            tvTitulo = view.findViewById(R.id.tituloItemCanal);
            tvDescripcion = view.findViewById(R.id.descripcionItemCanal);
            ivMarcador = view.findViewById(R.id.marcadorItemCanal);
            cbMarcado.setOnClickListener(this);
            ivMarcador.setOnClickListener(this);
        }

        @Override
        public void onClick(View view){
            switch (view.getId()){
                case R.id.cbItemCanal:
                    if(itemClickCbCanal != null)
                        itemClickCbCanal.onItemClickCb(view, getAdapterPosition());
                    break;
                case R.id.marcadorItemCanal:
                    if(itemClickMarcadorCanal != null)
                        itemClickMarcadorCanal.onItemClickMarcador(view, getAdapterPosition());
                    break;
                default:
                    break;
            }
        }
    }

    private Context context;
    private List<Canal> listaCanales;
    private LayoutInflater layoutInflater;
    private static ItemClickCbCanal itemClickCbCanal;
    private static ItemClickMarcadorCanal itemClickMarcadorCanal;

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
        holder.tvTitulo.setText(canal.getTitulo());
        holder.tvDescripcion.setText(canal.getDescripcion());
        int icono;
        switch (canal.getMarcador()){
            case 0:
                icono = R.drawable.ic_marcador100_especial;
                break;
            case 1:
                icono = R.drawable.ic_marcador100_especial1;
                break;
            case 2:
                icono = R.drawable.ic_marcador100_especial2;
                break;
            case 3:
                icono = R.drawable.ic_marcador100_especial3;
                break;
            case 4:
                icono = R.drawable.ic_marcador100_especial4;
                break;
            default:
                icono = R.drawable.ic_marcador100;
        }
        holder.ivMarcador.setImageDrawable(AppCompatResources.getDrawable(context, icono));
        if(canal.isMarcado())
            holder.ivMarcador.setVisibility(View.VISIBLE);
        else
            holder.ivMarcador.setVisibility(View.GONE);
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

    public void setItemClickMarcadorCanal(ItemClickMarcadorCanal iMarcadorCanal){
        itemClickMarcadorCanal = iMarcadorCanal;
    }

    public interface ItemClickCbCanal { void onItemClickCb(View view, int position);}

    public interface  ItemClickMarcadorCanal { void onItemClickMarcador(View view, int position); }
}
