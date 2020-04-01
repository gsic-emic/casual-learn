/*package es.uva.gsic.adolfinstro.persistencia;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public
interface GrupoTareasDao {
    @Query(value = "SELECT * FROM grupoTareas")
    List<GrupoTareas> listTareas();

    @Query(value = "SELECT * FROM grupoTareas WHERE idTarea = :id")
    GrupoTareas getTarea(final String id);

    @Insert(onConflict = OnConflictStrategy.REPLACE, entity = GrupoTareas.class)
    void insertTarea(GrupoTareas tarea);

    @Query(value = "DELETE FROM grupoTareas")
    int deleteTodasLasTareas();

    @Query(value = "DELETE FROM grupoTareas WHERE uid = :Uid")
    int deleteTarea(long Uid);

    @Update(entity = GrupoTareas.class)
    int updateTarea(GrupoTareas tarea);

}*/
