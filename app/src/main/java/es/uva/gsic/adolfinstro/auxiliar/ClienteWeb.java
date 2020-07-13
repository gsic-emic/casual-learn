package es.uva.gsic.adolfinstro.auxiliar;

import android.net.Uri;
import android.webkit.WebView;
import android.webkit.WebViewClient;

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
        if(Uri.parse(url).getScheme().toLowerCase().equals("https"))
            return false;
        else {
            navegadorExterno();
            return true;
        }
    }

    public abstract void navegadorExterno();
}
