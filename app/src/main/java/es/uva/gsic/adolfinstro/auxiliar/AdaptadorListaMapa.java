package es.uva.gsic.adolfinstro.auxiliar;

import android.content.Context;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import org.json.JSONObject;

import java.util.List;

import es.uva.gsic.adolfinstro.R;

/**
 * Clase para establecer la lista de tareas que se muestra cuando un usuario pulsa en un marcador
 * @author pablo
 * @version 20210202
 */
public class AdaptadorListaMapa extends RecyclerView.Adapter<AdaptadorListaMapa.ViewHolderMapa>  {

    /**
     * Subclase con el que establezco los items que van a componer cada objeto
     */
    public static class ViewHolderMapa extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView tvTitulo;
        ImageView ivTipoTarea, ivFondo, ivCompletada, ivMarcadorCanal;
        ConstraintLayout constraintLayout;

        /**
         * Constructor de la subclase
         * @param itemView Vista
         */
        ViewHolderMapa(@NonNull View itemView){
            super(itemView);
            tvTitulo = itemView.findViewById(R.id.tvItemListaMapa);
            ivMarcadorCanal = itemView.findViewById(R.id.ivItemMarcadorListaMapa);
            ivTipoTarea = itemView.findViewById(R.id.ivItemListaMapa);
            ivCompletada = itemView.findViewById(R.id.ivItemListaCompletada);
            ivFondo = itemView.findViewById(R.id.ivFondoListaMapa);
            constraintLayout = itemView.findViewById(R.id.clTarjetaTareasMapa);
            itemView.setOnClickListener(this);
        }

        /**
         * Método para responder a la pulsación de un usuario
         * @param v vista
         */
        @Override
        public void onClick(View v){
            if(itemClickListenerMapa != null)
                itemClickListenerMapa.onItemClick(v, getAdapterPosition());
        }
    }

    private List<TareasMapaLista> lista;
    private LayoutInflater layoutInflater;
    private static ItemClickListener itemClickListenerMapa;
    private Context context;

    /**
     * Constructor de la clase
     * @param context Contexto
     * @param lista Lista con la información necesaria de cada objeto
     */
    public AdaptadorListaMapa(Context context, List<TareasMapaLista> lista){
        this.lista = lista;
        layoutInflater = LayoutInflater.from(context);
        this.context = context;
    }

    /**
     * Método para inflar el layout
     * @param parent Parent
     * @param viewType ViewType
     * @return Objeto de la subclase
     */
    @NonNull
    @Override
    public AdaptadorListaMapa.ViewHolderMapa onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = layoutInflater.inflate(R.layout.item_lista_mapa, parent, false);
        return new ViewHolderMapa(view);
    }

    /**
     * Método para introducir la información en la tarjeta
     * @param holder Holder
     * @param position Posición
     */
    @Override
    public void onBindViewHolder(@NonNull AdaptadorListaMapa.ViewHolderMapa holder, int position){
        holder.tvTitulo.setText(lista.get(position).getTitulo());
        String uriFondo = lista.get(position).getUriFondo();
        if(uriFondo != null && !uriFondo.equals("") && !uriFondo.equals("?width=300")) {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                Picasso.get()
                        .load(uriFondo)
                        .placeholder(R.drawable.ic_cloud_download_blue_80dp)
                        .into(holder.ivFondo);
            else
                Picasso.get()
                        .load(uriFondo)
                        .into(holder.ivFondo);
            holder.ivTipoTarea.setImageDrawable(ResourcesCompat.getDrawable(
                    context.getResources(), Auxiliar.iconoTipoTarea(lista.get(position).getTipoTarea()), null));
            holder.ivTipoTarea.setVisibility(View.VISIBLE);
        } else {
            holder.ivFondo.setImageDrawable(ResourcesCompat.getDrawable(
                    context.getResources(), Auxiliar.iconoTipoTarea(lista.get(position).getTipoTarea()), null));
            holder.ivTipoTarea.setVisibility(View.GONE);
        }
        if(lista.get(position).getCompletada()){
            holder.ivCompletada.setVisibility(View.VISIBLE);
        }else{
            holder.ivCompletada.setVisibility(View.GONE);
        }
        try{
            holder.constraintLayout.setBackground(ResourcesCompat.getDrawable(
                    context.getResources(), R.drawable.boton_secundario, null));
            if(lista.get(position).getOpcional() != null){
                holder.ivMarcadorCanal.setImageDrawable(ResourcesCompat.getDrawable(
                        context.getResources(), lista.get(position).getOpcional(), null));
                holder.ivMarcadorCanal.setVisibility(View.VISIBLE);
            }else{
                holder.ivMarcadorCanal.setVisibility(View.GONE);
            }

            //Aqui lo de los canales
            /*if(lista.get(position).getCanales() != null && !lista.get(position).getCanales().trim().equals("")){
                holder.tvCanales.setText(lista.get(position).getCanales());
                holder.tvCanales.setVisibility(View.VISIBLE);
            } else {
                holder.tvCanales.setVisibility(View.GONE);
            }*/
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * Método para recuperar el tamaño de la lista
     * @return Tamaño de la lista
     */
    @Override
    public int getItemCount(){ return lista.size();}

    /**
     * Método para obtener el identificador del objeto que se pulse
     * @param posicion Posición pulsada
     * @return Identificador del objeto pulsado
     */
    public String getId(int posicion) { return lista.get(posicion).getId(); }

    /**
     * Método para obtener toda la información del marcador que se pulse
     * @param position Posición pulsada
     * @return JSONObject con toda la información de la tarea
     */
    public JSONObject getTarea(int position) { return  lista.get(position).getTarea(); }

    /**
     * Método para establecer la pulsación simple
     * @param itemClickListener ItemClickListener
     */
    public void setClickListener(ItemClickListener itemClickListener){
        itemClickListenerMapa = itemClickListener;
    }

    /**
     * Interfaz para establecer la pulsación
     */
    public interface ItemClickListener{
        void onItemClick(View view, int position);
    }
}
