package es.uva.gsic.adolfinstro;

public enum EstadoTarea {
    NOTIFICADA(0), RECHAZADA(1), NO_COMPLETADA(2), COMPLETADA(3), RETRASA(4);

    private final int v;
    private EstadoTarea(int v){
        this.v = v;
    }

    public int getValue(){
        return v;
    }
}
