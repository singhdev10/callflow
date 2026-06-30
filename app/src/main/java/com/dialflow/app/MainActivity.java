package com.dialflow.app;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity {

    private static final String APP_URL = "https://singhdev10.github.io/smartcalling/";

    private WebView webView;
    private String pendingCallNumber = null;
    private static final int CALL_PERMISSION_CODE = 101;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        webView = new WebView(this);
        setContentView(webView);

        WebSettings s = webView.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setDatabaseEnabled(true);
        s.setLoadWithOverviewMode(true);
        s.setUseWideViewPort(true);
        s.setCacheMode(WebSettings.LOAD_DEFAULT);
        s.setMediaPlaybackRequiresUserGesture(false);

        webView.setWebChromeClient(new WebChromeClient());

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (url.startsWith("tel:")) {
                    String number = Uri.decode(url.replace("tel:", "").trim());
                    placeDirectCall(number);
                    return true;
                }
                if (url.startsWith("https://wa.me") || url.startsWith("whatsapp:")
                        || url.startsWith("sms:") || url.startsWith("mailto:")
                        || url.startsWith("upi:") || url.startsWith("intent:")) {
                    try {
                        Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                        startActivity(i);
                    } catch (Exception e) {
                        Toast.makeText(MainActivity.this, "App nahi mili", Toast.LENGTH_SHORT).show();
                    }
                    return true;
                }
                return false;
            }
        });

        webView.loadUrl(APP_URL);
    }

    private void placeDirectCall(String number) {
        pendingCallNumber = number;
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE)
                == PackageManager.PERMISSION_GRANTED) {
            doCall(number);
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CALL_PHONE}, CALL_PERMISSION_CODE);
        }
    }

    private void doCall(String number) {
        try {
            Intent callIntent = new Intent(Intent.ACTION_CALL);
            callIntent.setData(Uri.parse("tel:" + number));
            startActivity(callIntent);
        } catch (SecurityException e) {
            Toast.makeText(this, "Call permission chahiye", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Intent dial = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + number));
            startActivity(dial);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CALL_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (pendingCallNumber != null) doCall(pendingCallNumber);
            } else {
                if (pendingCallNumber != null) {
                    Intent dial = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + pendingCallNumber));
                    startActivity(dial);
                }
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }
}
