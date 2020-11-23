package es.uva.gsic.adolfinstro.auxiliar;

import org.json.JSONObject;

public class PuntoSingular {

    private String titulo;
    private double distancia;
    private JSONObject punto;

    public PuntoSingular(String titulo, JSONObject punto){
        this(titulo, -1, punto);
    }

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
