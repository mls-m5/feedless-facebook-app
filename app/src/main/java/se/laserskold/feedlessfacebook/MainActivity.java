package se.laserskold.feedlessfacebook;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.*;
import android.webkit.*;

public class MainActivity extends AppCompatActivity {
    WebView webview = null;
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // Check if the key event was the Back button and if there's history
        if ((keyCode == KeyEvent.KEYCODE_BACK) && webview.canGoBack()) {
            webview.goBack();
            return true;
        }
        // If it wasn't the Back key or there's no web page history, bubble up to the default
        // system behavior (probably exit the activity)
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Remove title bar
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
//        setContentView(R.layout.activity_main);
//        webview = (WebView) findViewById(R.id.webview);

//        Log.d(getPackageName(), "Does web view exist? = " + Boolean.toString(webview != null));

//        if (webview == null) {
            webview = new WebView(getApplicationContext());
            webview.setVisibility(View.INVISIBLE);

            WebSettings settings = webview.getSettings();
            settings.setJavaScriptEnabled(true);

            final String script = "javascript: ( function x () { function y() {root = document.getElementById('m_newsfeed_stream'); root.style.display = 'none'}; y(); setInterval(y, 1000)}) ()";


            webview.setWebViewClient(new WebViewClient() {
                @Override
                public void onPageFinished(WebView view, String url) {
    //                view.loadUrl(script);
                    injectCSS();
                    webview.setVisibility(View.VISIBLE);
                    super.onPageFinished(view, url);
                }

                @Override
                public void onPageStarted(WebView view, String url, Bitmap favicon) {
                    super.onPageStarted(view, url, favicon);
                }

                //This function opens another application if you press a link to something
                @Override
                public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                    String host = request.getUrl().getHost();
                    // If WebViewClient is provided, return true means the host application handles the url,
                    // while return false means the current WebView handles the url.
//                    Log.d("feedlessfacebook", host);
//                    Log.d("feedlessfacebook", request.getUrl().toString());
                    if (host.contains("facebook")) {
                        return false;
                    }
                    else {
                        try {
                            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(request.getUrl().toString()));
                            startActivity(intent);
                        } catch (ActivityNotFoundException e) {
                            //This probably happends because facebook tries to open messenger
                            try {
                                Intent intent = getPackageManager().getLaunchIntentForPackage("com.facebook.orca");
                                startActivity(intent);
                            } catch (Exception e2) {
                                Log.d("feedlessfacebook", "could not start activity");
                            }
                        }
                        return true;
                    }
    //                return !host.contains("facebook");
    //                return super.shouldOverrideUrlLoading(view, request);
                }
            });

            webview.loadUrl("https://www.facebook.com");
//        }
        setContentView(webview);

    }

    // Inject CSS method: read style.css from assets folder
// Append stylesheet to document head
    private void injectCSS() {
        try {
            String code = "#m_newsfeed_stream {display:none;}";
            byte [] buffer = code.getBytes();
            String encoded = Base64.encodeToString(buffer, Base64.NO_WRAP);
            webview.loadUrl("javascript:(function() {" +
                    "var parent = document.getElementsByTagName('head').item(0);" +
                    "var style = document.createElement('style');" +
                    "style.type = 'text/css';" +
                    // Tell the browser to BASE64-decode the string into your script !!!
                    "style.innerHTML = window.atob('" + encoded + "');" +
                    "parent.appendChild(style)" +
                    "})()");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
