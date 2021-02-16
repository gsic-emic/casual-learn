package es.uva.gsic.adolfinstro.auxiliar;

/**
 * Objeto que representa a un canal en Casual Learn.
 *
 * @author Pablo
 * @version 20210216
 */
public class Canal {
    public static String obligatorio = "obligatorio";
    public static String opcional = "opcional";

    private String id;
    private String titulo;
    private String descripcion;
    private String tipo;
    private boolean marcado;
    private int marcador;

    public Canal(String id, String titulo, String descripcion, String tipo) {
        this(id, titulo, descripcion, tipo, tipo.equals(obligatorio));
    }

    public Canal(String id, String titulo, String descripcion, String tipo, boolean marcado) {
        this(id, titulo, descripcion, tipo, marcado, -1);
    }

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
        this.titulo = titulo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
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
