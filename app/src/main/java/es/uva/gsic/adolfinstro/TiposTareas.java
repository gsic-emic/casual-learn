package es.uva.gsic.adolfinstro;

import es.uva.gsic.adolfinstro.auxiliar.Auxiliar;

public enum TiposTareas {
    SIN_RESPUESTA(Auxiliar.tipoSinRespuesta),
    PREGUNTA_CORTA(Auxiliar.tipoPreguntaCorta),
    PREGUNTA_LARGA(Auxiliar.tipoPreguntaLarga),
    PREGUNTA_IMAGEN(Auxiliar.tipoPreguntaImagen),
    IMAGEN(Auxiliar.tipoImagen),
    IMAGEN_MULTIPLE(Auxiliar.tipoImagenMultiple),
    VIDEO(Auxiliar.tipoVideo);

    private final String v;
    TiposTareas(String v){
        this.v = v;
    }

    public String getValue(){
        return v;
    }
}
