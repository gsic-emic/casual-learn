package es.uva.gsic.adolfinstro;

public enum estadoTarea {
    RECHAZADA(0), IGNORADA(1), NO_COMPLETADA(2), COMPLETADA(3);

    private final int v;
    private estadoTarea(int v){
        this.v = v;
    }

    public int getValue(){
        return v;
    }
}
