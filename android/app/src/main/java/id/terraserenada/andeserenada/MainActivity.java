package id.terraserenada.andeserenada;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.webkit.JavascriptInterface;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class MainActivity extends Activity {
    private static final int REQUEST_OPEN_FILE = 4101;
    private static final int REQUEST_CREATE_FILE = 4102;

    private WebView webView;
    private ValueCallback<Uri[]> pendingFileCallback;
    private String pendingSaveContent;
    private String pendingSaveMime;
    private String pendingSaveName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        webView = new WebView(this);
        setContentView(webView);

        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setDatabaseEnabled(true);
        settings.setAllowFileAccess(true);
        settings.setAllowContentAccess(true);
        settings.setJavaScriptCanOpenWindowsAutomatically(false);
        settings.setSupportMultipleWindows(false);
        settings.setDefaultTextEncodingName("UTF-8");
        settings.setMixedContentMode(WebSettings.MIXED_CONTENT_NEVER_ALLOW);

        webView.setWebViewClient(new WebViewClient());
        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onShowFileChooser(WebView view, ValueCallback<Uri[]> filePathCallback,
                                             FileChooserParams fileChooserParams) {
                if (pendingFileCallback != null) {
                    pendingFileCallback.onReceiveValue(null);
                }
                pendingFileCallback = filePathCallback;
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("application/json");
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                try {
                    startActivityForResult(intent, REQUEST_OPEN_FILE);
                    return true;
                } catch (ActivityNotFoundException ex) {
                    pendingFileCallback = null;
                    Toast.makeText(MainActivity.this,
                            "Tidak ada aplikasi pemilih file yang tersedia.", Toast.LENGTH_LONG).show();
                    return false;
                }
            }
        });

        webView.addJavascriptInterface(new AndroidBridge(), "AndroidBridge");
        if (savedInstanceState == null) {
            webView.loadUrl("file:///android_asset/index.html");
        } else {
            webView.restoreState(savedInstanceState);
        }
    }

    private class AndroidBridge {
        @JavascriptInterface
        public void saveTextFile(String fileName, String content, String mimeType) {
            runOnUiThread(() -> beginSave(fileName, content, mimeType));
        }
    }

    private void beginSave(String fileName, String content, String mimeType) {
        pendingSaveName = sanitizeFileName(fileName);
        pendingSaveContent = content == null ? "" : content;
        pendingSaveMime = (mimeType == null || mimeType.trim().isEmpty())
                ? "application/octet-stream" : mimeType;

        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType(pendingSaveMime);
        intent.putExtra(Intent.EXTRA_TITLE, pendingSaveName);
        try {
            startActivityForResult(intent, REQUEST_CREATE_FILE);
        } catch (ActivityNotFoundException ex) {
            clearPendingSave();
            Toast.makeText(this, "Tidak ada aplikasi penyimpanan file yang tersedia.", Toast.LENGTH_LONG).show();
        }
    }

    private String sanitizeFileName(String name) {
        if (name == null || name.trim().isEmpty()) return "ande-serenada-data.txt";
        return name.replaceAll("[\\\\/:*?\"<>|]", "_");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_OPEN_FILE) {
            if (pendingFileCallback != null) {
                Uri[] result = (resultCode == RESULT_OK && data != null && data.getData() != null)
                        ? new Uri[]{data.getData()} : null;
                pendingFileCallback.onReceiveValue(result);
                pendingFileCallback = null;
            }
            return;
        }

        if (requestCode == REQUEST_CREATE_FILE) {
            if (resultCode == RESULT_OK && data != null && data.getData() != null) {
                try (OutputStream output = getContentResolver().openOutputStream(data.getData())) {
                    if (output == null) throw new IllegalStateException("Output stream tidak tersedia");
                    output.write((pendingSaveContent == null ? "" : pendingSaveContent)
                            .getBytes(StandardCharsets.UTF_8));
                    Toast.makeText(this, "File berhasil disimpan.", Toast.LENGTH_SHORT).show();
                } catch (Exception ex) {
                    Toast.makeText(this, "Gagal menyimpan file: " + ex.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
            clearPendingSave();
        }
    }

    private void clearPendingSave() {
        pendingSaveContent = null;
        pendingSaveMime = null;
        pendingSaveName = null;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (webView != null) webView.saveState(outState);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onBackPressed() {
        if (webView != null && webView.canGoBack()) webView.goBack();
        else super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        if (webView != null) {
            webView.removeJavascriptInterface("AndroidBridge");
            webView.destroy();
            webView = null;
        }
        super.onDestroy();
    }
}
