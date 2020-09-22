package es.uva.gsic.adolfinstro.auxiliar;

/**
 * Clase para generar la estructura utilizada en la pantalla de tareas pendientes, rechazadas y
 * completadas.
 *
 * @author Pablo
 * @version 20200914
 */
public class TareasLista {
    private String id, titulo, tipoTarea, fecha;
    private float puntuacion;

    /**
     * Constructor de la clase. Necesita la información que se muestra posteriormente en la lista.
     * @param id Identificador único de la tarea
     * @param titulo Título de la tarea. Actualemnte es el nombre del monumento
     * @param tipoTarea Tipo de respuesta esperada que va a dar el usuario
     * @param fecha Fecha de la última modificación
     * @param puntuacion Puntuación que el usuario da a la tarea. Si no es una tarea completada se
     *                   tiene que introducir un valor negativo para que no se muestren las estrellas
     *                   en la lista
     */
    public TareasLista(String id,
                String titulo,
                String tipoTarea,
                String fecha,
                float puntuacion){
        setId(id);
        setTitulo(titulo);
        setTipoTarea(tipoTarea);
        setFecha(fecha);
        setPuntuacion(puntuacion);
    }

    /**
     * Método para obtener el identificador único de la tarea.
     * @return Identificador único de la tarea.
     */
    public String getId() {
        return id;
    }

    /**
     * Establece el identificador único de la tarea
     * @param id Identificador único de la tarea
     */
    private void setId(String id) {
        this.id = id;
    }

    /**
     * Método para obtener el nombre del monumento
     * @return Título de la tarea
     */
    public String getTitulo() {
        return titulo;
    }

    /**
     * Método para establecer el nombre de la tarea
     * @param titulo Nombre de la tarea
     */
    private void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    /**
     * Método para obtener el tipo de respuesta que el usuario deberá dar
     * @return Tipo de tarea
     */
    public String getTipoTarea() {
        return tipoTarea;
    }

    /**
     * Método para establecer el tipo de tarea.
     * @param tipoTarea Tipo de tarea.
     */
    private void setTipoTarea(String tipoTarea) {
        this.tipoTarea = tipoTarea;
    }

    /**
     * Método con el que se obtiene la fecha de modificación de la tarea
     * @return Última modificación de la tarea
     */
    public String getFecha() {
        return fecha;
    }

    /**
     * Método para establecer la fecha de última modificación de la tarea que se muestra en la lista.
     * @param fecha Fecha de última modificación.
     */
    private void setFecha(String fecha) {
        this.fecha = fecha;
    }

    /**
     * Método para obtener la puntuación de la tarea. Si es negativa es que no se ha completado.
     * @return Puntuación dada a la tarea.
     */
    public float getPuntuacion() {
        return puntuacion;
    }

    /**
     * Método para establecer la puntuación de la tarea.
     * @param puntuacion Puntuación de la tarea
     */
    private void setPuntuacion(float puntuacion) {
        this.puntuacion = puntuacion;
    }
}
