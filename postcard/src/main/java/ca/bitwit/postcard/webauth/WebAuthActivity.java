package ca.bitwit.postcard.webauth;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.graphics.Color;
import android.view.Display;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.webkit.ValueCallback;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.Boolean;
import java.lang.Override;

public class WebAuthActivity extends Activity {

    protected LinearLayout root;
    private int backgroundColor = Color.WHITE;
    private WebView webView;

    private Handler handler;

    String authUrl;
    JSONObject parameters;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        handler = new Handler();
        root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(this.backgroundColor);
        root.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT, 0.0F));

        webView = new WebView(this);
        webView.getSettings().setJavaScriptEnabled(true);
        Bundle b = getIntent().getExtras();

        try{
            parameters = new JSONObject(b.getString("parameters"));
            Log.d("CordovaWebAuth", "params" + parameters.toString());
            authUrl = parameters.getString("auth_url");
            openWebView();
        } catch(JSONException ex){
            ex.printStackTrace();
            Log.d("CordovaWebAuth", "failed to load parameters");
        }
    }

    protected void openWebView(){
        webView.setWebViewClient(new WebAuthClient(parameters, this));
        webView.loadUrl(authUrl);
        this.root.addView(webView);
        setContentView(this.root);
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        Log.d("CordovaWebAuth", "WebAuthActivity Destroy " + hashCode());
    }

    public void onWebAuthRequestCompleted(final Boolean success, final String token){
        if(success){
            Log.d("CordovaWebAuth", "Activity :: Token request completed -> " + token);
        } else {
            Log.d("CordovaWebAuth", "Activity :: Token request failed");
        }

        handler.post(new Runnable() {
            public void run() {
                Intent returnIntent = new Intent();
                if (success) {
                    returnIntent.putExtra("token", token);
                    setResult(RESULT_OK, returnIntent);
                } else {
                    returnIntent.putExtra("message", "Not completed successfully");
                    setResult(RESULT_CANCELED, returnIntent);
                }
                Log.d("CordovaWebAuth", "Finishing Activity");
                finish();
            }
        });
    }

}