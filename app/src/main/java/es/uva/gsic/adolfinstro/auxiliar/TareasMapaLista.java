package es.uva.gsic.adolfinstro.auxiliar;

import org.json.JSONObject;

/**
 * Clase con la que se genera la estructura de las tareas que se utiliza en la lista del mapa.
 *
 * @author Pablo
 * @version 20200914
 */
public class TareasMapaLista {
    private String id, titulo, tipoTarea, uriFondo;
    private JSONObject tarea;
    private boolean completada;

    /**
     * Constructor de la tarea que luego se utilizará en la lista de tareas del marcador.
     * @param id Identificador de la tarea
     * @param titulo Título del lugar (nombre)
     * @param tipoTarea Tipo de la tarea
     * @param uriFondo URI de la imagen en baja resolución que se utiliza para el fondo
     * @param tarea JSON con toda la información de la tarea
     */
    public TareasMapaLista(String id,
                           String titulo,
                           String tipoTarea,
                           String uriFondo,
                           JSONObject tarea,
                           boolean completada){
        setId(id);
        setTitulo(titulo);
        setTipoTarea(tipoTarea);
        setUriFondo(uriFondo);
        setTarea(tarea);
        this.completada = completada;
    }

    /**
     * Método para obtener el identificador único de la tarea.
     * @return Identificador de la tarea.
     */
    public String getId() {
        return id;
    }

    /**
     * Método con el que se establece el identificador único de la tarea.
     * @param id Identificador único de la tarea
     */
    private void setId(String id) {
        this.id = id;
    }

    /**
     * Método para obtener el título de la tarea. Actualmente, el título es el nombre del monumento.
     * @return Nombre del monumento.
     */
    public String getTitulo() {
        return titulo;
    }

    /**
     * Método con el que se establece el nombre del monumento.
     * @param titulo Nombre del monumento
     */
    private void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    /**
     * Método para obtener el tipo de tarea.
     * @return Tipo de respuesta esperada que tendrá que hacer el usuario.
     */
    public String getTipoTarea() {
        return tipoTarea;
    }

    /**
     * Método para establecer el tipo de respuesta que deberá realizar el usuario
     * @param tipoTarea Tipo de tarea
     */
    private void setTipoTarea(String tipoTarea) {
        this.tipoTarea = tipoTarea;
    }

    /**
     * Método para obtener el URI de la imagen que se usará de fondo de la lista
     * @return URI de la imagen que se utiliza de fondo en la tarjeta
     */
    public String getUriFondo() {
        return uriFondo;
    }

    /**
     * Método para establecer el URI de la imagen de la tarjeta
     * @param uriFondo URI del fondo de la tarjeta
     */
    public void setUriFondo(String uriFondo) {
        this.uriFondo = uriFondo;
    }

    /**
     * Método para obtener el JSON con toda la información de la tarea
     * @return JSON con toda la información de la tarea
     */
    public JSONObject getTarea() {
        return tarea;
    }

    /**
     * Método para establecer el JSON de la tarea.
     * @param tarea JSON con la información de la tarea.
     */
    private void setTarea(JSONObject tarea) {
        this.tarea = tarea;
    }

    public boolean getCompletada() {
        return completada;
    }
}
