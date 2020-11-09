package es.uva.gsic.adolfinstro.persistencia;

import android.app.Application;
import android.content.Context;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import es.uva.gsic.adolfinstro.auxiliar.Auxiliar;

/**
 * Clase que gestiona las llamadas a los ficheros para la persistencia de datos
 *
 * @author Pablo
 * @version 20200924
 */
public class PersistenciaDatos {
    /** Fichero donde se almacenan las tareas recibidas desde el servidor que el usuario puede iniciar*/
    public static final String ficheroTareasUsuario = "tareasUsuario";
    /** Fichero donde se encuentras las tareas notificadas al ususario */
    public static final String ficheroNotificadas = "notificadas";
    /** Fichero con las tareas rechazadas */
    public static final String ficheroTareasRechazadas = "rechazadas";
    /** Fichero con las tareas pospuestas */
    public static final String ficheroTareasPospuestas = "pospuestas";
    /** Fichero con los datos del usuario */
    public static final String ficheroUsuario = "usario";
    /** Fichero con fechas de instantes de eventos */
    public static final String ficheroInstantes = "eventos";
    /** Fichero con las tareas completadas */
    public static final String ficheroCompletadas = "completadas";
    /** Fichero con las tareas denunciadas por el usuario */
    public static final String ficheroDenunciadas = "denunciadas";
    /** Fichero con la última posición del usuario */
    public static final String ficheroPosicion = "posicion";
    /** Fichero con las respuestas que aun no se han podido enviar a la pasarela */
    public static final String ficheroSinEnviar = "respuestas";

    public static final String ficheroPrimeraCuadricula = "primeraCuadricula";
    public static final String ficheroPosicionesCuadriculas = "posicionesCuadriculas";

    public static final Object bloqueo = new Object();
    public static final String ficheroNuevasCuadriculas = "ficheroNuevasCuadriculas";


    /**
     * Método para obtener el contenido de un fichero que se sabe que está estructurado en forma de JSON
     * @param app Aplicación
     * @param fichero Nombre del fichero
     * @return JSONArray con el contenido del fichero o un JSONArray inicializado pero vacío si el
     *      fichero estaba vacío o se ha producido algún error.
     */
    public static JSONArray leeFichero(Application app, String fichero){
            JSONArray array;
            File f = new File(app.getFilesDir(), fichero);
            if (f.exists()) { //Lectura del fichero existente
                try {
                    synchronized (PersistenciaDatos.bloqueo) {
                        InputStreamReader inputStreamReader = new InputStreamReader(app.openFileInput(fichero));
                        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                        StringBuffer stringBuffer = new StringBuffer();
                        String inter;
                        while ((inter = bufferedReader.readLine()) != null) {
                            stringBuffer.append(inter);
                        }
                        array = new JSONArray(stringBuffer.toString());
                    }
                } catch (IOException | JSONException e) { //Si se produce un error se va a devolver el array inicializado
                    array = new JSONArray();
                }
            } else {
                array = new JSONArray();
            }
            return array;
    }

    /**
     * Método para almacenar un JSONArray en el fichero que se expecifique
     *
     * @param app Aplicación
     * @param fichero Nombre del fichero
     * @param array Estructura a almacenar
     * @param modo Modo en el que se va a modificar/sobrescribir el fichero
     * @return Verdadero si el método ha finalizado de una manera esperada o falso si se ha ocasionado
     *      una excepción
     */
    public static boolean guardaFichero(Application app, String fichero, JSONArray array, int modo){
        try{
            synchronized (PersistenciaDatos.bloqueo) {
                OutputStreamWriter outputStreamWriter = new OutputStreamWriter(app.openFileOutput(fichero, modo));
                outputStreamWriter.write(array.toString());
                outputStreamWriter.close();
            }
        } catch (IOException e){
            return false;
        }
        return true;
    }

    /**
     * Método para eliminar un fichero de la aplicación. Si se borra correctamente, o el fichero no
     * existe, devuelve true.
     *
     * @param app Aplicación
     * @param fichero Nombre del fichero que se desea eliminar
     * @return True si se ha eliminado (o no existía) o false si no se ha conseguido eliminar
     */
    public static boolean borraFichero(Application app, String fichero){
        try{
            File file = new File(app.getFilesDir(), fichero);
            synchronized (PersistenciaDatos.bloqueo) {
                app.deleteFile(fichero);
                //Por seguridad, compruebo de nuevo si el fichero existe
            }
            return !file.exists();
        }catch (Exception e){
            return false;
        }
    }

    /**
     * Método para eliminar todos los fichero de la aplicación antes de cerrar sesión
     * @param app Aplicación
     * @return True si se ha conseguido eliminar todos los ficheros de la aplicación.
     */
    public static boolean borraTodosFicheros(Application app){
        try {
            if (PersistenciaDatos.borraFichero(app, PersistenciaDatos.ficheroUsuario))
                if(PersistenciaDatos.borraFichero(app, PersistenciaDatos.ficheroPosicion))
                    if(PersistenciaDatos.borraFichero(app,
                            PersistenciaDatos.ficheroInstantes))
                        if(PersistenciaDatos.borraFichero(app,
                                PersistenciaDatos.ficheroDenunciadas))
                            if(PersistenciaDatos.borraFichero(app,
                                    PersistenciaDatos.ficheroTareasUsuario))
                                return PersistenciaDatos.borraFichero(app,
                                        PersistenciaDatos.ficheroSinEnviar);

            return false;
        }catch (Exception e){
            return false;
        }
    }

    /**
     * Método para guardar un array al final de un fichero
     * @param app Aplicación
     * @param fichero Identificador del fichero
     * @param array Array que se va a almacenar
     * @return True si se consigue almacenar, false en caso contrario
     */
    public static boolean guardaArray(Application app, String fichero, JSONArray array){
        JSONArray antiguo = leeFichero(app, fichero);
        JSONObject j;
        try {
            for (int i = 0; i < array.length(); i++) {
                j = array.getJSONObject(i);
                antiguo.put(j);
            }
            return guardaFichero(app, fichero, antiguo, Context.MODE_PRIVATE);
        }catch (Exception e){
            return false;
        }

    }

    /**
     * Método para guardar un fichero con un JSONbject. Para reutilizar la mayor cantidad de código
     * posible, el JSONObject está contenido en un JSONArray (Context.MODE_PRIVATE)
     *
     * @param app App
     * @param fichero Nombre del fichero que se va a crear
     * @param jsonObject Objeto que se va a almacenar en el fichero
     * @param modo Modo de estritura del fichero
     * @return Devolverá true si el fichero se ha almacenado correctamente
     */
    public static boolean creaFichero(Application app, String fichero, JSONObject jsonObject, int modo){
        JSONArray array = new JSONArray();
        array.put(jsonObject);
        return guardaFichero(app, fichero, array, modo);
    }
    /**
     * Método para almacenar un JSONObject al final de un fichero (dentro de un JSONArray)
     *
     * @param app Aplicación
     * @param fichero Nombre del fichero
     * @param jsonObject JSONObject que se guarda al final del JSONArray que exista en el fichero
     * @param modo Modo en el que se va a realizar la escritura del fichero (Context.MODE_PRIVATE -> sobrescritura)
     * @return Verdadero si la operación se ha realizado correctamente
     */
    public static boolean guardaJSON(Application app, String fichero, JSONObject jsonObject, int modo){
        synchronized (PersistenciaDatos.bloqueo) {
            JSONArray array = leeFichero(app, fichero);
            array.put(jsonObject);
            return guardaFichero(app, fichero, array, modo);
        }
    }

    /**
     * Método para almacenar un JSONObject al final de un fichero (dentro de un JSONArray)
     *
     * @param app Aplicación
     * @param fichero Nombre del fichero
     * @param jsonObject JSONObject donde se enecuntra el identificador de la tarea (se comprueba su
     *                   existencia en la base de datos (tiene que existir para que el método devuelva
     *                   true
     * @param respuesta Respuesta que se va a agregar al JSON antes de almacenarlo
     * @param tipo Tipo de respuesta
     * @param modo Modo de escritura con el que se va a guardar el fichero (Context.MODE_PRIVATE -> sobrescritura)
     * @return True si todas las operaciones se han llevado a cabo de manera correcta
     */
    public static boolean guardaTareaRespuesta(Application app,
                                                            String fichero,
                                                            JSONObject jsonObject,
                                                            String respuesta,
                                                            String tipo,
                                                            int modo){
        synchronized (PersistenciaDatos.bloqueo) {
            try {
                JSONObject tarea = obtenTarea(
                        app,
                        fichero,
                        jsonObject.getString(Auxiliar.id),
                        jsonObject.getString(Auxiliar.idUsuario));
                JSONArray vectorRespuestas = null;
                try {
                    vectorRespuestas = tarea.getJSONArray(Auxiliar.respuestas);
                } catch (Exception e) {
                    vectorRespuestas = new JSONArray();
                }
                JSONObject nuevaRespuesta = new JSONObject();
                nuevaRespuesta.put(Auxiliar.posicionRespuesta, vectorRespuestas.length());
                nuevaRespuesta.put(Auxiliar.respuestaRespuesta, respuesta);
                nuevaRespuesta.put(Auxiliar.tipoRespuesta, tipo);
                vectorRespuestas.put(nuevaRespuesta);
                tarea.put(Auxiliar.respuestas, vectorRespuestas);
                JSONArray array = leeFichero(app, fichero);
                array.put(tarea);
                return guardaFichero(app, fichero, array, modo);
            } catch (Exception e) {
                return false;
            }
        }
    }

    /**
     * Método para reemplazar un objeto JSON que exista dentro de un fichero por otro. Si el objeto no
     * existe en el fichero se almacena sin sustituir nada
     *
     * @param app Application
     * @param fichero Nombre del fichero
     * @param jsonObject Fichero a reemplazar por el existento en el fichero. Tiene que tener un
     *                   String con el identificador ("id")
     * @return Devolverá true si se ha conseguido almacenar el JSON en el fichero
     */
    public static boolean reemplazaJSON(Application app, String fichero, JSONObject jsonObject){
        synchronized (PersistenciaDatos.bloqueo) {
            try {
                int modo = Context.MODE_PRIVATE;
                JSONArray array = leeFichero(app, fichero);
                String id = jsonObject.getString(Auxiliar.id);
                JSONObject base;
                boolean encontrado = false;
                int i;
                for (i = 0; i < array.length(); i++) {
                    base = array.getJSONObject(i);
                    if (base.getString(Auxiliar.id).equals(id)) {
                        encontrado = true;
                        break;
                    }
                }
                if (encontrado) {
                    array.remove(i);
                }
                array.put(jsonObject);
                return guardaFichero(app, fichero, array, modo);
            } catch (Exception e) {
                return false;
            }
        }
    }

    /**
     * Método para reemplazar una tarea que exista dentro de un fichero por otra. Si la tarea no
     * existe en el fichero se almacena sin sustituir nada
     *
     * @param app Application
     * @param fichero Nombre del fichero
     * @param jsonObject Tarea con la que se va a reemplazar la existente en el fichero
     * @param idUsuario Identificador del usuario
     * @return Devolverá true si se ha conseguido almacenar el JSON en el fichero
     */
    public static boolean reemplazaJSON(Application app, String fichero, JSONObject jsonObject, String idUsuario){
        synchronized (PersistenciaDatos.bloqueo) {
            try {
                int modo = Context.MODE_PRIVATE;
                JSONArray array = leeFichero(app, fichero);
                String id = jsonObject.getString(Auxiliar.id);
                JSONObject base;
                boolean encontrado = false;
                int i;
                for (i = 0; i < array.length(); i++) {
                    base = array.getJSONObject(i);
                    if (idUsuario != null) {
                        if (base.getString(Auxiliar.id).equals(id)
                                && base.getString(Auxiliar.idUsuario).equals(idUsuario)) {
                            encontrado = true;
                            break;
                        }
                    } else {
                        if (base.getString(Auxiliar.id).equals(id)
                                && !base.has(Auxiliar.idUsuario)) {
                            encontrado = true;
                            break;
                        }
                    }
                }
                if (encontrado) {
                    array.remove(i);
                }
                array.put(jsonObject);
                return guardaFichero(app, fichero, array, modo);
            } catch (Exception e) {
                return false;
            }
        }
    }

    /**
     * Método que se puede comprobar para determinar si una tarea existe en el fichero
     * @param app Aplicación
     * @param fichero Nombre del fichero
     * @param idTarea Identificador de la tarea
     * @return True si el identificador está presente en la ase de datos
     */
    public static Boolean existeTarea(Application app, String fichero, String idTarea){
        try {
            JSONArray jsonArray = leeFichero(app, fichero);
            JSONObject jsonObject;
            for (int i = 0; i < jsonArray.length(); i++) {
                jsonObject = jsonArray.getJSONObject(i);
                if (jsonObject.get(Auxiliar.id).equals(idTarea)) {
                    return true;
                }
            }
        }catch (Exception e) {
            return false;
        }
        return false;
    }

    /**
     * Método que se puede comprobar para determinar si una tarea existe en el fichero
     * @param app Aplicación
     * @param fichero Nombre del fichero
     * @param idTarea Identificador de la tarea
     * @param idUser Identificador único del usuario
     * @return True si el identificador está presente en la ase de datos
     */
    public static Boolean existeTarea(Application app, String fichero, String idTarea, String idUser){
        try {
            JSONArray jsonArray = leeFichero(app, fichero);
            JSONObject jsonObject;
            for (int i = 0; i < jsonArray.length(); i++) {
                jsonObject = jsonArray.getJSONObject(i);
                if (jsonObject.get(Auxiliar.id).equals(idTarea)
                        && jsonObject.get(Auxiliar.idUsuario).equals(idUser)) {
                    return true;
                }
            }
        }catch (Exception e) {
            return false;
        }
        return false;
    }

    /**
     * Método para recuparar un JSONObject de un fichero. Modifia el fichero ya que elmina el JSONObject
     * del JSONArray y sobrescribe el fichero
     *
     * @param app Aplicación
     * @param fichero Nombre del fichero
     * @param idTarea Identificador de la tarea
     * @return JSONObject que corresponde con el identificador y el fichero
     * @throws Exception Se lanza una excepción cuando el identificador no esté en el registro
     */
    public static JSONObject obtenTarea(Application app,
                                                     String fichero,
                                                     String idTarea)
            throws Exception {
        synchronized (PersistenciaDatos.bloqueo) {
            JSONArray jsonArray = leeFichero(app, fichero);
            JSONObject jsonObject = null;
            boolean encontrado = false;
            int i;
            for (i = 0; i < jsonArray.length(); i++) {
                jsonObject = jsonArray.getJSONObject(i);
                if (jsonObject.get(Auxiliar.id).equals(idTarea)) {
                    encontrado = true;
                    break;
                }
            }
            if (encontrado) {
                jsonArray.remove(i);
                guardaFichero(app, fichero, jsonArray, Context.MODE_PRIVATE);
                return jsonObject;
            } else {
                return null;
            }
        }
    }

    /**
     * Método para recuparar un JSONObject de un fichero. Modifica el fichero ya que elmina el JSONObject
     * del JSONArray y sobrescribe el fichero
     *
     * @param app Aplicación
     * @param fichero Nombre del fichero
     * @param idTarea Identificador de la tarea
     * @return JSONObject que corresponde con el identificador y el fichero
     * @throws Exception Se lanza una excepción cuando el identificador no esté en el registro
     */
    public static JSONObject obtenTarea(Application app,
                                                     String fichero,
                                                     String idTarea,
                                                     String idUser)
            throws Exception {
        synchronized (PersistenciaDatos.bloqueo) {
            JSONArray jsonArray = leeFichero(app, fichero);
            JSONObject jsonObject = null;
            boolean encontrado = false;
            int i;
            for (i = 0; i < jsonArray.length(); i++) {
                jsonObject = jsonArray.getJSONObject(i);
                if (idUser != null) {
                    if (jsonObject.get(Auxiliar.id).equals(idTarea) && idUser.equals(jsonObject.get(Auxiliar.idUsuario))) {
                        encontrado = true;
                        break;
                    }
                } else {
                    if (jsonObject.get(Auxiliar.id).equals(idTarea) && !jsonObject.has(Auxiliar.idUsuario)) {
                        encontrado = true;
                        break;
                    }
                }
            }
            if (encontrado) {
                jsonArray.remove(i);
                guardaFichero(app, fichero, jsonArray, Context.MODE_PRIVATE);
                return jsonObject;
            } else {
                return null;
            }
        }
    }

    /**
     * Método utilizado para recupar un JSONObject del fichero. Esta operación no modfica el registro.
     *
     * @param app Aplicación
     * @param fichero Nombre del fichero
     * @param idTarea Identificador de la tarea
     * @return JSONObject si el identificador se correspondía con alguno de los que están en el fichero
     * o null si no existía
     */
    public static JSONObject recuperaTarea(Application app, String fichero, String idTarea) {
        JSONArray jsonArray = leeFichero(app, fichero);
        JSONObject jsonObject;
        try{
            for (int i = 0; i < jsonArray.length(); i++) {
                jsonObject = jsonArray.getJSONObject(i);
                if (jsonObject.get(Auxiliar.id).equals(idTarea)) {
                    return jsonObject;
                }
            }
        }
        catch (Exception e){
            return null;
        }
        return null;
    }

    /**
     * Método utilizado para recupar un JSONObject del fichero. Esta operación no modfica el registro.
     *
     * @param app Aplicación
     * @param fichero Nombre del fichero
     * @param idTarea Identificador de la tarea
     * @return JSONObject si el identificador se correspondía con alguno de los que están en el fichero
     * o null si no existía
     */
    public static JSONObject recuperaTarea(Application app, String fichero, String idTarea, String idUser) {
        JSONArray jsonArray = leeFichero(app, fichero);
        JSONObject jsonObject;
        try{
            for (int i = 0; i < jsonArray.length(); i++) {
                jsonObject = jsonArray.getJSONObject(i);
                if(idUser != null) {
                    if (jsonObject.get(Auxiliar.id).equals(idTarea)
                            && idUser.equals(jsonObject.get(Auxiliar.idUsuario))) {
                        return jsonObject;
                    }
                } else{ //El usuario no se ha identificado aún
                    if (!jsonObject.has(Auxiliar.idUsuario)
                            && jsonObject.get(Auxiliar.id).equals(idTarea))
                        return jsonObject;
                }
            }
        }
        catch (Exception e){
            return null;
        }
        return null;
    }

    /**
     * Método para obtener las tareas de un usuario que están almacenadas en un fichero
     *
     * @param app Aplicación
     * @param fichero Fichero donde están almacenadas las tareas
     * @param idUsuario Identificador único del usuario
     * @return Lista de tareas del usuario. Puede estar vacío.
     */
    public static JSONArray leeTareasUsuario(Application app, String fichero, String idUsuario){
        JSONArray tareas = leeFichero(app, fichero);
        JSONArray tareasUsuario = new JSONArray();
        JSONObject tarea;
        try {
            for (int i = 0; i < tareas.length(); i++) {
                tarea = tareas.getJSONObject(i);
                if(tarea.getString(Auxiliar.idUsuario).equals(idUsuario))
                    tareasUsuario.put(tarea);
            }
            return tareasUsuario;
        }catch (Exception e){
            return tareasUsuario;
        }
    }
}
