package es.uva.gsic.adolfinstro.auxiliar;

public class ContextoLista {

    private String idContexto, label, fecha;

    public ContextoLista(
            String idContexto,
            String label,
            String fecha){
        this.idContexto = idContexto;
        this.label = label;
        this.fecha = fecha;
    }

    public String getLabel(){
        return label;
    }

    public String getIdContexto(){
        return idContexto;
    }

    public String getFecha(){
        return fecha;
    }
}
