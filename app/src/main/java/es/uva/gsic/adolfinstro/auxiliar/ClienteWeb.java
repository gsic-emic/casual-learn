package es.uva.gsic.adolfinstro.auxiliar;

import android.net.Uri;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.util.Objects;

/**
 * Clase para controlar la interacción del usuario frente al navegador interno
 * @author pablo
 * @version 20210311
 */
public abstract class ClienteWeb extends WebViewClient {

    /**
     * Todos los enlaces https los abre dentro del navegador interno. Si tiene enlaces http se abre en
     * su navedor por defecto
     * @param view Vista
     * @param url Url
     * @return True fuera, false dentro
     */
    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        if(Objects.requireNonNull(Uri.parse(url).getScheme()).toLowerCase().equals("https")){
            if(url.toLowerCase().contains("youtube") || url.toLowerCase().contains("youtu.be")){
                navegadorExterno();
                return true;
            }else{
                return false;
            }
        }
        else {
            navegadorExterno();
            return true;
        }
    }

    /**
     * Método que hay que sobrescribir para que los enlaces que no sean http se abran en el navegador
     * externo
     */
    public abstract void navegadorExterno();
}
