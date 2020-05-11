package es.uva.gsic.adolfinstro.auxiliar;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.List;

import es.uva.gsic.adolfinstro.Completadas;
import es.uva.gsic.adolfinstro.R;

public class AdaptadorImagenesCompletadas extends RecyclerView.Adapter<AdaptadorImagenesCompletadas.ViewHolder> {
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        ImageView imageView, aspas;
        ViewHolder(final View item){
            super(item);
            imageView = item.findViewById(R.id.ivItemListaImagen);
            aspas = item.findViewById(R.id.ivBorrarMedia);
            imageView.setOnClickListener(this);
            aspas.setOnClickListener(this);
        }
        @Override
        public void onClick(View v) {
            if(itemClick != null){
                itemClick.onItemClick(v, getAdapterPosition());
            }
        }
    }

    private List<Completadas.ImagenesCamara> lista;
    private LayoutInflater layoutInflater;
    private static ItemClickListener itemClick;

    public AdaptadorImagenesCompletadas(Context context, List<Completadas.ImagenesCamara> uris){
        lista = uris;
        /*for(String uri : uris){
            try {
                lista.add(new URI(uri));
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
        }*/
        layoutInflater = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public AdaptadorImagenesCompletadas.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = layoutInflater.inflate(R.layout.item_lista_imagenes, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AdaptadorImagenesCompletadas.ViewHolder holder, int position) {
        Picasso.get().load(lista.get(position).getDireccion()).into(holder.imageView);
        if(lista.get(position).getVisible() == View.VISIBLE){
            holder.aspas.setVisibility(View.VISIBLE);
            holder.imageView.setClickable(false);
        }else {
            holder.aspas.setVisibility(View.GONE);
            holder.imageView.setClickable(true);
        }
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

    //https://stackoverflow.com/questions/40584424/simple-android-recyclerview-example
    public void setClickListener(ItemClickListener itemClickListener){
        AdaptadorImagenesCompletadas.itemClick = itemClickListener;
    }

    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }

}
