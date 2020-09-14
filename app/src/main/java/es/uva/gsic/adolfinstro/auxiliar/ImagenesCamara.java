package es.uva.gsic.adolfinstro.auxiliar;

import android.net.Uri;

/**
 * Clase que genera la estructura para mostrar la lista de elementos multimedia. Se puede utilizar
 * tanto para fotografías como para vídeos.
 *
 * @author Pablo
 * @version 20200914
 */
public class ImagenesCamara {
    /** Identificador único del objeto */
    private Uri direccion;
    /** Visibilidad de la imagen de borrado */
    private int visible;

    /**
     * Contructor de la clase.
     *
     * @param direccion Identificador único del recurso
     * @param visible Visibilidad del borrado del recurso
     */
    public ImagenesCamara(Uri direccion, int visible){
        this.direccion = direccion;
        this.visible = visible;
    }

    /**
     * Constructor de la clase.
     * @param direccion Identificador único del recurso
     * @param visible Visibilidad del borrado del recurso
     */
    public ImagenesCamara(String direccion, int visible){
        this(Uri.parse(direccion), visible);
    }

    /**
     * Método para obtener el identificador único del recurso
     *
     * @return Identificador único del recurso
     */
    public Uri getDireccion(){
        return direccion;
    }

    /**
     * Método para saber si el borrado del recurso es visible o no
     *
     * @return Estado de la visibilidad del recurso
     */
    public int getVisible(){
        return visible;
    }

    /**
     * Método para establecer la visibilidad del recurso.
     *
     * @param codigo Nueva visibilidad
     */
    public void setVisible(int codigo){
        visible = codigo;
    }
}
