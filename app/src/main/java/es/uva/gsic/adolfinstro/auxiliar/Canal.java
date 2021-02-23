package es.uva.gsic.adolfinstro.auxiliar;

/**
 * Objeto que representa a un canal en Casual Learn.
 *
 * @author Pablo
 * @version 20210223
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
    /** Indica el marcador del canal */
    private int marcador;

    /**
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
     *
     * @param id Identificador del canal
     * @param titulo Título del canal
     * @param descripcion Descripción del canal
     * @param tipo Opcional o  obligatorio
     * @param marcado Indica si el canal está seleccionado
     */
    public Canal(String id, String titulo, String descripcion, String tipo, boolean marcado) {
        this(id, titulo, descripcion, tipo, marcado, -1);
    }

    /**
     *
     * @param id Identificador del canal
     * @param titulo Título del canal
     * @param descripcion Descripción del canal
     * @param tipo Opcional o  obligatorio
     * @param marcado Indica si el canal está seleccionado
     * @param marcador Marcador que se va a utilizar
     */
    public Canal(String id, String titulo, String descripcion, String tipo, boolean marcado, int marcador) {
        setId(id);
        setTitulo(titulo);
        setDescripcion(descripcion);
        setTipo(tipo);
        setMarcado(marcado);
        setMarcador(marcador);
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
        this.titulo = (titulo.length() < 50) ? titulo : titulo.substring(0, 49);
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = (descripcion.length() < 300) ? descripcion : descripcion.substring(0, 299);
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

    public int getMarcador() {
        return marcador;
    }

    public void setMarcador(int marcador) {
        this.marcador = marcador;
    }
}
