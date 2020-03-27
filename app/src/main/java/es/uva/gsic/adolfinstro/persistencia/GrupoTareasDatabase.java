/*package es.uva.gsic.adolfinstro.persistencia;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {GrupoTareas.class}, version = 1)
public abstract class GrupoTareasDatabase extends RoomDatabase {

    private static volatile GrupoTareasDatabase BBDD;

    public abstract GrupoTareasDao grupoTareasDao();

    public static GrupoTareasDatabase getInstance(Context context){
        if(BBDD == null){
            synchronized (GrupoTareasDatabase.class){
                if(BBDD == null){
                    BBDD = Room.databaseBuilder(
                            context.getApplicationContext(),
                            GrupoTareasDatabase.class,
                            "grupoTareasBD.db"
                    ).enableMultiInstanceInvalidation().allowMainThreadQueries()
                            .build();
                }
            }
        }
        return BBDD;
    }
}*/
