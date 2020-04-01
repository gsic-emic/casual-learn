/*package es.uva.gsic.adolfinstro.persistencia;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import es.uva.gsic.adolfinstro.EstadoTarea;

@Entity(tableName = "grupoTareas")
public class GrupoTareas {
    @NonNull
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "uid")
    private long uid;

    @ColumnInfo(name = "idTarea")
    private String idTarea;

    @ColumnInfo(name = "tipoTarea")
    private String tipoTarea;

    @ColumnInfo(name = "EstadoTarea")
    private int estadoTarea;

    @ColumnInfo(name = "respuestaTarea")
    private String respuestaTarea;

    @Ignore
    public GrupoTareas(@NonNull String idTarea, @NonNull String tipoTarea){
        this.idTarea = idTarea;
        this.tipoTarea = tipoTarea;
    }

    @Ignore
    public GrupoTareas(@NonNull String idTarea, @NonNull String tipoTarea, EstadoTarea estadoTarea){
        this.idTarea = idTarea;
        this.tipoTarea = tipoTarea;
        this.estadoTarea = estadoTarea.getValue();
    }

    @Ignore
    public GrupoTareas(@NonNull String idTarea, @NonNull String tipoTarea, EstadoTarea estadoTarea, @NonNull String respuestaTarea) {
        this.idTarea = idTarea;
        this.tipoTarea = tipoTarea;
        this.estadoTarea = estadoTarea.getValue();
        this.respuestaTarea = respuestaTarea;
    }

    public GrupoTareas(){
        this("a","b", EstadoTarea.NO_COMPLETADA,"d");
    }

    public Long getUid(){
        return uid;
    }

    protected void setUid(long uid){
        this.uid = uid;
    }

    public String getIdTarea(){
        return idTarea;
    }

    public void setIdTarea(String idTarea){
        this.idTarea = idTarea;
    }

    public String getTipoTarea(){
        return tipoTarea;
    }

    public void setTipoTarea(String tipoTarea){
        this.tipoTarea = tipoTarea;
    }

    public int getEstadoTarea(){
        return estadoTarea;
    }

    public void setEstadoTarea(EstadoTarea estadoTarea){
        setEstadoTarea(estadoTarea.getValue());
    }

    protected void setEstadoTarea(int estadoTarea){
        this.estadoTarea = estadoTarea;
    }

    public String getRespuestaTarea(){
        return respuestaTarea;
    }

    public void setRespuestaTarea(String respuestaTarea){
        this.respuestaTarea = respuestaTarea;
    }
}*/
