package es.uva.gsic.adolfinstro;

public enum EstadoTarea {
    RECHAZADA(0), IGNORADA(1), NO_COMPLETADA(2), COMPLETADA(3);

    private final int v;
    private EstadoTarea(int v){
        this.v = v;
    }

    public int getValue(){
        return v;
    }
}
