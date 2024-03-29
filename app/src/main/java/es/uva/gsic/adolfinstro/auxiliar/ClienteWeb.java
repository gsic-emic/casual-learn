package es.uva.gsic.adolfinstro.auxiliar;

import android.webkit.WebView;
import android.webkit.WebViewClient;

/**
 * Clase para controlar la interacción del usuario frente al navegador interno
 *
 * @author pablo
 * @version 20200727
 */
public abstract class ClienteWeb extends WebViewClient {

    /**
     * Todos los enlaces https los abre dentro del navegador interno. Si tiene enlaces http se abre en
     * su navedor por defecto
     *
     * @param view Vista
     * @param url  URL
     * @return True fuera, false dentro
     */
    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        if (url.toLowerCase().contains("youtube") || url.toLowerCase().contains("youtu")) {
            navegadorExterno();
            return true;
        } else {
            if (url.toLowerCase().contains("https://")) {
                return false;
            } else {
                navegadorExterno();
                return true;
            }
        }
    }

    /**
     * Método que hay que sobrescribir para que los enlaces que no sean http se abran en el navegador
     * externo
     */
    public abstract void navegadorExterno();
}
