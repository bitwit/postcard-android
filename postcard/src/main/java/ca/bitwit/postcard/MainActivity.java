package ca.bitwit.postcard;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.*;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Toast;
import ca.bitwit.postcard.camera.CameraHandler;
import ca.bitwit.postcard.webauth.OAuthAccessTokenTask;

import java.io.File;
import java.io.FileOutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

public class MainActivity extends Activity {

    private PostcardAdaptor adaptor;
    private PostcardWebView myWebView;

    public float m_downY;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        myWebView = (PostcardWebView) findViewById(R.id.webView);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            myWebView.setWebContentsDebuggingEnabled(true);
        }
        // disable scroll on touch
        myWebView.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN: {
                        // save the x
                        m_downY = event.getY();
                    }
                    break;

                    case MotionEvent.ACTION_MOVE:
                    case MotionEvent.ACTION_CANCEL:
                    case MotionEvent.ACTION_UP: {
                        // set y so that it doesn't move
                        event.setLocation(event.getX(), m_downY);
                    }
                    break;

                }

                return false;
            }
        });
        this.adaptor = new PostcardAdaptor(this, myWebView);


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case R.id.action_send:
                Log.d("Postcard", "Send()!");
                send();
                return true;
            case android.R.id.home:
                // app icon in action bar clicked; go home
                home();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void home(){
        myWebView.post(new Runnable() {
            @Override
            public void run() {
                myWebView.loadUrl("javascript:window.PostcardUI.home();");
            }
        });
    }

    private void send(){
        myWebView.post(new Runnable() {
            @Override
            public void run() {
                myWebView.loadUrl("javascript:window.PostcardUI.send();");
            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();
        adaptor.networkAccountDataSource.open();
        adaptor.personDataSource.open();
        adaptor.tagDataSource.open();
    }

    @Override
    public void onResume() {
        super.onResume();
        adaptor.networkAccountDataSource.close();
        adaptor.personDataSource.close();
        adaptor.tagDataSource.close();
    }

    public void onNewIntent(Intent intent) {
        // super.onNewIntent(intent);

        this.adaptor.networkAccountDataSource.open();
        this.adaptor.personDataSource.open();
        this.adaptor.tagDataSource.open();

        Uri uri = intent.getData();
        if (uri != null) {

            String token = uri.getQueryParameter("oauth_token");
            String verifier = uri.getQueryParameter("oauth_verifier");

            Log.d("CordovaWebAuth", "onNewIntent Token:" + token);
            Log.d("CordovaWebAuth", "onNewIntent Verifier:" + verifier);

            try {
                OAuthAccessTokenTask task = new OAuthAccessTokenTask();

                if (adaptor.oAuthService == null) {
                    Log.d("CordovaWebAuth", "OAuth Service is null");
                }

                if (adaptor.requestToken == null) {
                    Log.d("CordovaWebAuth", "RequestToken is null");
                }

                task.prepare(adaptor.oAuthService, adaptor, adaptor.requestToken, URLDecoder.decode(verifier, "UTF-8"));
                task.execute();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d("CordovaWebAuth", "Activity ResultCode: " + resultCode + " RequestCode: " + requestCode);
        if (requestCode == 100) {
            if (resultCode == Activity.RESULT_OK) {
                String verifierToken = data.getStringExtra("token");
                Log.d("CordovaWebAuth", "Plugin :: Successful return to first activity with token " + verifierToken);

                try {
                    OAuthAccessTokenTask task = new OAuthAccessTokenTask();
                    task.prepare(adaptor.oAuthService, adaptor, adaptor.requestToken, URLDecoder.decode(verifierToken, "UTF-8"));
                    task.execute();
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }

                //this.lastCallbackContext.success(token);
                //return;
            } else if (resultCode == Activity.RESULT_CANCELED) {
                String token = data.getStringExtra("message");
                Log.d("CordovaWebAuth", "Plugin :: Failed or Cancelled");
            }
            //this.lastCallbackContext.error("Web Auth Did Not Complete");
        }

        if (requestCode == CameraHandler.CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {

                int scaleSize = 720;
                int thumbScaleSize = 144;
                Bitmap photo = BitmapFactory.decodeFile(adaptor.camera.fileUri.toString().replace("file://", ""));
                saveImageToFile(scaledPhotoToLargestDimension(photo, scaleSize), adaptor.camera.fileUriMedium);
                saveImageToFile(scaledPhotoToLargestDimension(photo, thumbScaleSize), adaptor.camera.fileUriThumbnail);

                adaptor.mediaSaved(adaptor.camera.fileUriMedium, "image");
                Toast.makeText(this, "Image saved to " + adaptor.camera.fileUriMedium, Toast.LENGTH_LONG).show();
            } else if (resultCode == RESULT_CANCELED) {
                // User cancelled the image capture
            } else {
                // Image capture failed, advise user
                Toast.makeText(this, "Error capturing photo", Toast.LENGTH_LONG).show();
            }
        }

        if (requestCode == CameraHandler.CAPTURE_VIDEO_ACTIVITY_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                // Video captured and saved to fileUri specified in the Intent
                Toast.makeText(this, "Video saved", Toast.LENGTH_LONG).show();
                //Toast.makeText(this, "Video saved to:\n" + data.getData(), Toast.LENGTH_LONG).show();
                adaptor.mediaSaved(adaptor.camera.fileUri, "video");
            } else if (resultCode == RESULT_CANCELED) {
                // User cancelled the video capture
            } else {
                // Video capture failed, advise user
                Toast.makeText(this, "Error capturing video", Toast.LENGTH_LONG).show();
            }
        }

    }

    private Bitmap scaledPhotoToLargestDimension(Bitmap photo, int scale){
        if (photo.getWidth() >= photo.getHeight()) {
            double ratio = photo.getHeight() / photo.getWidth();
            return Bitmap.createScaledBitmap(photo, scale, (int)Math.round(scale * ratio), true);
        } else {
            double ratio = (double)photo.getWidth() / (double)photo.getHeight();
            return Bitmap.createScaledBitmap(photo, (int)Math.round(scale * ratio), scale, true);
        }
    }

    private void saveImageToFile(Bitmap bitmap, Uri fileUri){
        File file = new File(fileUri.toString().replace("file://", ""));
        if (file.exists()) file.delete();
        try {
            FileOutputStream out = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
