package ca.bitwit.postcard;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.webkit.WebView;

public class PostcardWebView extends WebView {

    public PostcardWebView(Context context) {
        super(context);
    }

    public PostcardWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PostcardWebView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        if(keyCode == KeyEvent.KEYCODE_BACK && canGoBack())
        {
            Log.d("PostcardWebView", "Should go back");
            loadUrl("javascript:window.history.back();");
            return true;
        }
        Log.d("PostcardWebView", "Can't go back any further");
        return super.onKeyDown(keyCode, event);
    }
}
