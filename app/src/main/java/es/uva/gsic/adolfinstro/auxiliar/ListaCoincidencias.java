package es.uva.gsic.adolfinstro.auxiliar;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Clase auxiliar para la lógica de la aplicación. Con ella se crea el objeto que luego representa
 * cada una de las coincidencias de la búsqueda de municipios que realice el usuario.
 *
 * @author Pablo
 * @version 20200914
 */
public class ListaCoincidencias {
        /** Nombre del municipio. */
        String municipio;
        /** Provincia del municipio. */
        String provincia;
        /** Situación del municipio. El primer dato es la latitud y el segundo la longitud. */
        List<Double> posicion;
        /** Población del municipio */
        int poblacion;

        /**
         * Constructor del municipio que coincide con la búsqueda del usuario
         * @param jsonObject Información del municipio
         * @throws JSONException Excepción al trabajar con JSON
         */
        public ListaCoincidencias(JSONObject jsonObject) throws JSONException {
            municipio = jsonObject.getString("m");
            provincia = jsonObject.getString("p");
            posicion = new ArrayList<>();
            posicion.add(jsonObject.getDouble("a"));// Latitud
            posicion.add(jsonObject.getDouble("o"));// Longitud
            poblacion = jsonObject.getInt("g");
        }

        /** Método para obtener el nombrel del municipio */
        public String getMunicipio() {
            return municipio;
        }

        /** Método para obtener la provincia del municipio */
        public String getProvincia() {
            return provincia;
        }

        /** Método para obtener la latitud del municipio */
        public double getLatitud(){
            return posicion.get(0);
        }

        /** Método para obtener la longitud del municipio */
        public double getLongitud(){
            return posicion.get(1);
        }

        /** Método para obtener la población del muncipio */
        public int getPoblacion() {
            return poblacion;
        }
    }
