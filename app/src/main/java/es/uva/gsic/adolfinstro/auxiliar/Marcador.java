package es.uva.gsic.adolfinstro.auxiliar;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Clase auxiliar para generar la estructura con la información de los marcadores que se representan
 * en el mapa.
 *
 * @author Pablo
 * @version 20201105
 */
public class Marcador {
    /** Título del marcador*/
    private String titulo;
    /** Latitud y longitud donde se coloca al marcador */
    private double latitud, longitud;
    /** Tareas que contendrá el marcador*/
    private JSONArray tareasMarcador;
    /** Número de tareas que están en el interior del marcador */
    private int numeroTareas;

    /**
     * Constructor de la subclase. Establece los valores iniciales e inicia la lista.
     */
    public Marcador(){
        titulo = null;
        latitud = 0;
        longitud = 0;
        tareasMarcador = new JSONArray();
    }

    /**
     * Método para establecer un título al marcador
     * @param titulo Título del marcador
     */
    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    /**
     * Método para recuperar un título del marcador
     * @return Título del marcador. Si no se ha establecido es null
     */
    public String getTitulo(){
        return titulo;
    }

    /**
     * Método para establecer la posición del marcador en el mapa
     * @param latitud Latitud
     * @param longitud Longitud
     */
    public void setPosicionMarcador(double latitud, double longitud) {
        this.latitud = latitud;
        this.longitud = longitud;
    }

    /**
     * Método para recuperar la latitud del marcador
     * @return Latitud
     */
    public double getLatitud() {
        return latitud;
    }

    /**
     * Método para recuperar la longitud del marcador
     * @return Longitud
     */
    public double getLongitud() {
        return longitud;
    }

    /**
     * Método para agregar una nueva tarea al marcador
     * @param tarea Tarea que se desea agregar
     */
    public void agregaTareaAlMarcador(JSONObject tarea){
        tareasMarcador.put(tarea);
        incrementaTareas();
    }

    /**
     * Método para recuperar la lista de tareas del marcador
     * @return Lista de tareas del marcador
     */
    public JSONArray getTareasMarcador(){
        return tareasMarcador;
    }

    /**
     * Método para incrementar el número de tareas del marcador
     */
    private void incrementaTareas(){
        ++numeroTareas;
    }

    /**
     * Método para obtener el número de tareas del marcador
     * @return Número de tareas
     */
    public int getNumeroTareas() {
        return numeroTareas;
    }
}
