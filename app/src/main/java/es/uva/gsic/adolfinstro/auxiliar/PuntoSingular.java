package es.uva.gsic.adolfinstro.auxiliar;

import org.json.JSONObject;

/**
 * Clase para la creación de los puntos de interés
 *
 * @author Pablo
 * @version 20201211
 */
public class PuntoSingular {

    private String titulo;
    private double distancia;
    private JSONObject punto;

    /**
     * Constructor del punto de interés.
     *
     * @param titulo Nombre del lugar de interés.
     * @param distancia Distancia hasta el punto
     * @param punto Toda la información del sitio de interés.
     */
    public PuntoSingular(String titulo, double distancia, JSONObject punto){
        setTitulo(titulo);
        setDistancia(distancia);
        setPunto(punto);
    }

    public double getDistancia() {
        return distancia;
    }

    public void setDistancia(double distancia) {
        this.distancia = distancia;
    }

    public String getTitulo() {
        return titulo;
    }

    private void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public JSONObject getPunto() {
        return punto;
    }

    public void setPunto(JSONObject punto) {
        this.punto = punto;
    }
}
