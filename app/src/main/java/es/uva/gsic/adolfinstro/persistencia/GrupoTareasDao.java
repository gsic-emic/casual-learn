package es.uva.gsic.adolfinstro.persistencia;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

import io.reactivex.Completable;

@Dao
public
interface GrupoTareasDao {
    @Query(value = "SELECT * FROM grupoTareas")
    List<GrupoTareas> getGrupoTareas();

    @Query(value = "SELECT * FROM grupoTareas WHERE idTarea = :id")
    GrupoTareas getTarea(final String id);

    @Insert(onConflict = OnConflictStrategy.REPLACE, entity = GrupoTareas.class)
    void insertTarea(GrupoTareas tarea);

    @Query(value = "DELETE FROM grupoTareas")
    int deleteGrupoTareas();

    @Query(value = "DELETE FROM grupoTareas WHERE uid = :Uid")
    int deleteTarea(long Uid);
}
