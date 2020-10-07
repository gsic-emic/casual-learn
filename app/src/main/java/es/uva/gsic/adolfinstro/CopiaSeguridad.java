package es.uva.gsic.adolfinstro;

import android.app.backup.BackupAgentHelper;
import android.app.backup.BackupDataInput;
import android.app.backup.BackupDataOutput;
import android.app.backup.FileBackupHelper;
import android.content.Context;
import android.os.ParcelFileDescriptor;
import android.util.Log;

import java.io.IOException;

import es.uva.gsic.adolfinstro.persistencia.PersistenciaDatos;

/**
 * Clase para realizar la copia de seguridad de los ficheros deseados
 *
 * @author Pablo
 * @version 20201005
 *
 * https://developer.android.com/guide/topics/data/keyvaluebackup
 */
public class CopiaSeguridad extends BackupAgentHelper {

    static final String ficheros = "FICHEROS";

    @Override
    public void onCreate() {
        super.onCreate();
        FileBackupHelper fileBackupHelper = new FileBackupHelper(
                this,
                PersistenciaDatos.ficheroCompletadas,
                PersistenciaDatos.ficheroTareasRechazadas,
                PersistenciaDatos.ficheroTareasPospuestas);

        addHelper(ficheros, fileBackupHelper);
    }

    // Se usa la sincronización en el guardado y la restauración
    @Override
    public void onBackup(ParcelFileDescriptor antiguo,
                         BackupDataOutput datos,
                         ParcelFileDescriptor nuevo)
        throws IOException{
        Log.d("COPIA", "entro en onBackup");
        synchronized (PersistenciaDatos.bloqueo){
            super.onBackup(antiguo, datos, nuevo);
        }
        Log.d("COPIA", "salgo onBackup");
    }

    @Override
    public void onRestore(BackupDataInput datos,
                          int codigoV,
                          ParcelFileDescriptor nuevo)
        throws IOException{
        synchronized (PersistenciaDatos.bloqueo){
            super.onRestore(datos, codigoV, nuevo);
        }
    }
}
