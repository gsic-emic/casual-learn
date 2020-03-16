package es.uva.gsic.adolfinstro;

public enum TiposTareas {
    SIN_RESPUESTA("sinRespuesta"),
    PREGUNTA_CORTA("preguntaCorta"),
    PREGUNTA_LARGA("preguntaLarga"),
    PREGUNTA_IMAGEN("preguntaImagen"),
    IMAGEN("imagen"),
    IMAGEN_MULTIPLE("imagenMultiple"),
    VIDEO("video");

    private final String v;
    TiposTareas(String v){
        this.v = v;
    }

    public String getValue(){
        return v;
    }
}
