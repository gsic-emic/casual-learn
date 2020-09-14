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

import es.uva.gsic.adolfinstro.R;

/**
 * Clase para ofrecer la visión al usuario de los vídeos que grabe
 *
 * @author pablo
 * @version 20200626
 */
public class AdaptadorVideosCompletados extends RecyclerView.Adapter<AdaptadorVideosCompletados.ViewHolder> {
    /**
     * Subclase para establecer la referencia a cada objeto del layout
     */
    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        VideoView videoView;
        ImageView aspas;

        /**
         * Constructor de la subclase.
         * @param item Vista
         */
        ViewHolder(final View item){
            super(item);
            videoView = item.findViewById(R.id.vvVideoLista);
            aspas = item.findViewById(R.id.ivBorrarMediaVideo);
            videoView.setOnClickListener(this);
            aspas.setOnClickListener(this);
        }

        /**
         * Método con el que se establece la acción al pulsar sobre la vista
         * @param v Vista
         */
        @Override
        public void onClick(View v) {
            if(itemClickVideo != null){
                itemClickVideo.onItemClickVideo(v, getAdapterPosition());
            }
        }
    }

    private List<ImagenesCamara> lista;
    private LayoutInflater layoutInflater;
    private static ItemClickListenerVideo itemClickVideo;

    /**
     * Constructor de la clase.
     * @param context Contexto
     * @param uris Lista con toda la información necesaria de los vídeos.
     */
    public AdaptadorVideosCompletados(Context context, List<ImagenesCamara> uris){
        lista = uris;
        layoutInflater = LayoutInflater.from(context);
    }

    /**
     * Inflador del layout
     * @param parent Parent
     * @param viewType ViewType
     * @return Objeto de la subclase
     */
    @NonNull
    @Override
    public AdaptadorVideosCompletados.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = layoutInflater.inflate(R.layout.item_lista_videos, parent, false);
        return new ViewHolder(view);
    }

    /**
     * Método para introducir la información necesaria a cada objeto
     * @param holder Holder
     * @param position Posición
     */
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
     * Método para obtener el número de items que componen la lista
     * @return Número de objetos que componen la lista
     */
    @Override
    public int getItemCount() {
        return lista.size();
    }

    /**
     * Se establece la acción al pulsar en un objeto de la lista
     * @param itemClickListener ItemClickListenerVideo
     */
    //https://stackoverflow.com/questions/40584424/simple-android-recyclerview-example
    public void setClickListenerVideo(ItemClickListenerVideo itemClickListener){
        AdaptadorVideosCompletados.itemClickVideo = itemClickListener;
    }

    /**
     * Interfaz para establecer la pulsación simple
     */
    public interface ItemClickListenerVideo {
        void onItemClickVideo(View view, int position);
    }

}
