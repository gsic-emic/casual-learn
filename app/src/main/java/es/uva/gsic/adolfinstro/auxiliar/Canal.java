package es.uva.gsic.adolfinstro.auxiliar;

/**
 * Objeto que representa a un canal en Casual Learn.
 *
 * @author Pablo
 * @version 20210409
 */
public class Canal {
    /** El canal es obligatorio. El usuario no podrá darse de baja */
    public static String obligatorio = "obligatorio";
    /** El canal es opcional. El usuario podrá darse de alta y de baja */
    public static String opcional = "opcional";

    /** Identificador del canal */
    private String id;
    /** Título del canal. No podrá tener más de 50 caracteres */
    private String titulo;
    /** Descripción del canal. No podrá tener más de 300 caracteres */
    private String descripcion;
    /** Tipo del canal (opcional u obligatorio) */
    private String tipo;
    /** Indica si el usuario está suscrito al canal o no */
    private boolean marcado;
    /** Objeto para almacenar el nombre del autor que se va a mostrar en la lista de la configuración
     * de los canales */
    private String detallesAutor;

    /**
     * Constructor del canal
     *
     * @param id Identificador del canal
     * @param titulo Título del canal
     * @param descripcion Descripción del canal
     * @param tipo Opcional o  obligatorio
     */
    public Canal(String id, String titulo, String descripcion, String tipo) {
        this(id, titulo, descripcion, tipo, tipo.equals(obligatorio));
    }

    /**
     * Constructor del canal
     *
     * @param id Identificador del canal
     * @param titulo Título del canal
     * @param descripcion Descripción del canal
     * @param tipo Opcional o  obligatorio
     * @param marcado Indica si el canal está seleccionado
     */
    public Canal(String id, String titulo, String descripcion, String tipo, boolean marcado) {
        this(id, titulo, descripcion, tipo, marcado, null);
    }

    /**
     * Constructor del canal
     *
     * @param id Identificador del canal
     * @param titulo Título del canal
     * @param descripcion Descripción del canal
     * @param tipo Opcional o  obligatorio
     * @param marcado Indica si el canal está seleccionado
     * @param detallesAutor Nombre del autor del canal
     */
    public Canal(String id, String titulo, String descripcion, String tipo, boolean marcado, String detallesAutor){
        setId(id);
        setTitulo(titulo);
        setDescripcion(descripcion);
        setTipo(tipo);
        setMarcado(marcado);
        setDetallesAutor(detallesAutor);
    }


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = (titulo.length() < 100) ? titulo : titulo.substring(0, 98) + "…";
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = (descripcion.length() < 400) ? descripcion : descripcion.substring(0, 398) + "…";
    }

    public String getTipo() {
        return (tipo == null)?opcional:tipo;
    }

    public void setTipo(String tipo) {
        if(!tipo.equals(obligatorio) && !tipo.equals(opcional))
            this.tipo = opcional;
        else
            this.tipo = tipo;
    }

    public boolean isMarcado() {
        return marcado;
    }

    public void setMarcado(boolean marcado) {
        this.marcado = (getTipo().equals(obligatorio)) || marcado;
    }

    public String getDetallesAutor() {
        return detallesAutor;
    }

    public void setDetallesAutor(String detallesAutor){
        this.detallesAutor = detallesAutor;
    }
}
