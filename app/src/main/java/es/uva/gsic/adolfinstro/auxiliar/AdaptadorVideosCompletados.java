package es.uva.gsic.adolfinstro.auxiliar;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import es.uva.gsic.adolfinstro.Completadas;
import es.uva.gsic.adolfinstro.R;

public class AdaptadorVideosCompletados extends RecyclerView.Adapter<AdaptadorVideosCompletados.ViewHolder> {
    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        VideoView videoView;
        ImageView aspas;
        ViewHolder(final View item){
            super(item);
            videoView = item.findViewById(R.id.vvVideoLista);
            aspas = item.findViewById(R.id.ivBorrarMediaVideo);
            videoView.setOnClickListener(this);
            aspas.setOnClickListener(this);
        }
        @Override
        public void onClick(View v) {
            if(itemClickVideo != null){
                itemClickVideo.onItemClickVideo(v, getAdapterPosition());
            }
        }
    }

    private List<Completadas.ImagenesCamara> lista;
    private LayoutInflater layoutInflater;
    private static ItemClickListenerVideo itemClickVideo;

    public AdaptadorVideosCompletados(Context context, List<Completadas.ImagenesCamara> uris){
        lista = uris;
        layoutInflater = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public AdaptadorVideosCompletados.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = layoutInflater.inflate(R.layout.item_lista_videos, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AdaptadorVideosCompletados.ViewHolder holder, int position) {
        holder.videoView.setMinimumHeight(300);
        holder.videoView.setVisibility(View.VISIBLE);
        holder.videoView.setVideoURI(lista.get(position).getDireccion());
        if(holder.videoView.isPlaying()){
            holder.videoView.stopPlayback();
            holder.videoView.resume();
        }
        if(lista.get(position).getVisible() == View.VISIBLE){
            holder.aspas.setVisibility(View.VISIBLE);
            holder.videoView.setClickable(false);
        }else {
            holder.aspas.setVisibility(View.GONE);
            holder.videoView.setClickable(true);
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
    public void setClickListenerVideo(ItemClickListenerVideo itemClickListener){
        AdaptadorVideosCompletados.itemClickVideo = itemClickListener;
    }

    public interface ItemClickListenerVideo {
        void onItemClickVideo(View view, int position);
    }

}
