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

import es.uva.gsic.adolfinstro.EstadoTarea;
import es.uva.gsic.adolfinstro.auxiliar.Auxiliar;

/**
 * Clase que gestiona las llamadas a los ficheros para la persistencia de datos
 *
 * @author Pablo
 * @version 20200722
 */
public class PersistenciaDatos {
    /** Fichero donde se almacenan las tareas recibidas desde el servidor que el usuario puede iniciar*/
    public static final String ficheroTareasUsuario = "tareasUsuario";
    /** Fichero donde se encuentras las tareas notificadas al alumno */
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

    public static final String ficheroPrimeraCuadricula = "primeraCuadricula";
    public static final String ficheroPosicionesCuadriculas = "posicionesCuadriculas";


    /**
     * Método para obtener el contenido de un fichero que se sabe que está estructurado en forma de JSON
     * @param app Aplicación
     * @param fichero Nombre del fichero
     * @return JSONArray con el contenido del fichero o un JSONArray inicializado pero vacío si el
     *      fichero estaba vacío o se ha producido algún error.
     */
    public static synchronized JSONArray leeFichero(Application app, String fichero){
        JSONArray array;
        File f = new File(app.getFilesDir(), fichero);
        if (f.exists()) { //Lectura del fichero existente
            try {
                InputStreamReader inputStreamReader = new InputStreamReader(app.openFileInput(fichero));
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                StringBuffer stringBuffer = new StringBuffer();
                String inter;
                while ((inter = bufferedReader.readLine()) != null) {
                    stringBuffer.append(inter);
                }
                array = new JSONArray(stringBuffer.toString());
            } catch (IOException | JSONException e){ //Si se produce un error se va a devolver el array inicializado
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
    public static synchronized boolean guardaFichero(Application app, String fichero, JSONArray array, int modo){
        try{
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(app.openFileOutput(fichero, modo));
            outputStreamWriter.write(array.toString());
            outputStreamWriter.close();
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
    public static synchronized boolean borraFichero(Application app, String fichero){
        try{
            File file = new File(app.getFilesDir(), fichero);
            app.deleteFile(fichero);
            //Por seguridad, compruebo de nuevo si el fichero existe
            if(file.exists())
                return file.delete();
            else
                return true;
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
                    if(PersistenciaDatos.borraFichero(app, PersistenciaDatos.ficheroTareasUsuario))
                        if(PersistenciaDatos.borraFichero(app, PersistenciaDatos.ficheroCompletadas))
                            if(PersistenciaDatos.borraFichero(app,
                                    PersistenciaDatos.ficheroPrimeraCuadricula))
                                if(PersistenciaDatos.borraFichero(app,
                                        PersistenciaDatos.ficheroInstantes))
                                    if(PersistenciaDatos.borraFichero(app,
                                            PersistenciaDatos.ficheroDenunciadas))
                                        if(PersistenciaDatos.borraFichero(app,
                                                PersistenciaDatos.ficheroTareasPospuestas))
                                            if(PersistenciaDatos.borraFichero(app,
                                                    PersistenciaDatos.ficheroTareasRechazadas))
                                                if(PersistenciaDatos.borraFichero(app,
                                                        PersistenciaDatos.ficheroNotificadas)) {
                                                    File file = new File(app.getFilesDir(),
                                                            PersistenciaDatos.ficheroPosicionesCuadriculas);
                                                    if(file.exists()) {
                                                        JSONArray cuadriculas =
                                                                PersistenciaDatos.leeFichero(app,
                                                                        PersistenciaDatos.ficheroPosicionesCuadriculas);
                                                        JSONObject cuadricula;
                                                        for (int i = 0; i < cuadriculas.length(); i++) {
                                                            cuadricula = cuadriculas.getJSONObject(i);
                                                            PersistenciaDatos.borraFichero(app,
                                                                    cuadricula.getString(Auxiliar.id));
                                                        }
                                                        return PersistenciaDatos.borraFichero(app,
                                                                PersistenciaDatos.ficheroPosicionesCuadriculas);
                                                    }else{
                                                        return true;
                                                    }
                                                }
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
    public static synchronized boolean creaFichero(Application app, String fichero, JSONObject jsonObject, int modo){
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
    public static synchronized boolean guardaJSON(Application app, String fichero, JSONObject jsonObject, int modo){
        JSONArray array = leeFichero(app, fichero);
        array.put(jsonObject);
        return guardaFichero(app, fichero, array, modo);
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
    public static synchronized boolean guardaTareaRespuesta(Application app,
                                                            String fichero,
                                                            JSONObject jsonObject,
                                                            String respuesta,
                                                            String tipo,
                                                            int modo){
        try {
            JSONObject tarea = obtenTarea(app, fichero, jsonObject.getString(Auxiliar.id));
            JSONArray vectorRespuestas = null;
            try{
                vectorRespuestas = tarea.getJSONArray(Auxiliar.respuestas);
            }catch (Exception e){
                vectorRespuestas = new JSONArray();
            }
            JSONObject nuevaRespuesta = new JSONObject();
            nuevaRespuesta.put(Auxiliar.posicionRespuesta, vectorRespuestas.length());
            nuevaRespuesta.put(Auxiliar.respuestaRespuesta, respuesta);
            nuevaRespuesta.put(Auxiliar.tipoRespuesta, tipo);
            vectorRespuestas.put(nuevaRespuesta);
            nuevaRespuesta = null;
            tarea.put(Auxiliar.respuestas, vectorRespuestas);
            vectorRespuestas = null;
            JSONArray array = leeFichero(app, fichero);
            array.put(tarea); tarea = null;
            return guardaFichero(app, fichero, array, modo);
        } catch (Exception e){
            return false;
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
    public static synchronized boolean reemplazaJSON(Application app, String fichero, JSONObject jsonObject){
        try {
            int modo = Context.MODE_PRIVATE;
            JSONArray array = leeFichero(app, fichero);
            String id = jsonObject.getString(Auxiliar.id);
            JSONObject base;
            boolean encontrado = false;
            int i;
            for (i = 0; i < array.length(); i++) {
                base = array.getJSONObject(i);
                if(base.getString(Auxiliar.id).equals(id)){
                    encontrado = true;
                    break;
                }
            }
            if(encontrado){
                array.remove(i);
            }
            array.put(jsonObject);
            return guardaFichero(app, fichero, array, modo);
        }catch (Exception e){
            return false;
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
     * Método para recuparar un JSONObject de un fichero. Modifia el fichero ya que elmina el JSONObject
     * del JSONArray y sobrescribe el fichero
     *
     * @param app Aplicación
     * @param fichero Nombre del fichero
     * @param idTarea Identificador de la tarea
     * @return JSONObject que corresponde con el identificador y el fichero
     * @throws Exception Se lanza una excepción cuando el identificador no esté en el registro
     */
    public static synchronized JSONObject obtenTarea(Application app,
                                                     String fichero,
                                                     String idTarea)
            throws Exception {
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
        if(encontrado){
            jsonArray.remove(i);
            guardaFichero(app, fichero, jsonArray, Context.MODE_PRIVATE);
            return jsonObject;
        }else{
            return null;
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
     * Método para comprobar si un fichero tiene objetos almacenados
     *
     * @param app Aplicación
     * @param fichero Nombre del fichero
     * @return Verdadero si el fichero tiene objetos
     */
    public static boolean tieneObjetos(Application app, String fichero){
        return leeFichero(app, fichero).length() > 0;
    }

    /**
     * Creación del JSON de una tarea
     * @param idTarea identificador de la tarea
     * @param tipo Tipo de tarea
     * @param estadoTarea Estado en el que va a estar la tarea
     * @return Objeto preparado para ser insertado en el fichero
     */
    public static JSONObject generaJSON(String idTarea, String tipo, EstadoTarea estadoTarea) {
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put(Auxiliar.id, idTarea);
            jsonObject.put(Auxiliar.tipoRespuesta, tipo);
            jsonObject.put(Auxiliar.estadoTarea, estadoTarea.getValue());
            return jsonObject;
        }catch (Exception w){
            return null;
        }
    }

    public static JSONArray tareasPosicion(Application app, String fichero, double latitud, double longitud){
        JSONArray tareas = new JSONArray();
        JSONArray todas = leeFichero(app, fichero);
        JSONObject tarea;
        for(int i = 0; i < todas.length(); i++){
            try {
                tarea = todas.getJSONObject(i);
                if((tarea.getDouble(Auxiliar.latitud) == latitud) &&
                        (tarea.getDouble(Auxiliar.longitud) == longitud)){
                    tareas.put(tarea);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return tareas;
    }
}
