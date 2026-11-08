package id.terraserenada.andeserenada;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.print.PrintAttributes;
import android.print.PrintJob;
import android.print.PrintManager;
import android.util.Base64;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends Activity {
    private static final int REQUEST_OPEN_FILE = 4101;
    private static final int REQUEST_CREATE_FILE = 4102;

    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final List<WebView> activePrintWebViews = new ArrayList<>();
    private final Map<WebView, Runnable> printMonitorTasks = new HashMap<>();

    private WebView webView;
    private ValueCallback<Uri[]> pendingFileCallback;
    private String pendingSaveContent;
    private byte[] pendingSaveBytes;
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
        settings.setAllowFileAccessFromFileURLs(false);
        settings.setAllowUniversalAccessFromFileURLs(false);
        settings.setJavaScriptCanOpenWindowsAutomatically(false);
        settings.setSupportMultipleWindows(false);
        settings.setDefaultTextEncodingName("UTF-8");
        settings.setMixedContentMode(WebSettings.MIXED_CONTENT_NEVER_ALLOW);

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, android.webkit.WebResourceRequest request) {
                Uri uri = request.getUrl();
                String scheme = uri.getScheme() == null ? "" : uri.getScheme();
                String path = uri.getPath() == null ? "" : uri.getPath();
                if ("file".equalsIgnoreCase(scheme) && path.startsWith("/android_asset/")) {
                    return false;
                }
                if ("https".equalsIgnoreCase(scheme) || "http".equalsIgnoreCase(scheme)) {
                    try {
                        startActivity(new Intent(Intent.ACTION_VIEW, uri));
                    } catch (ActivityNotFoundException ex) {
                        showToast("Tidak ada aplikasi untuk membuka tautan ini.", Toast.LENGTH_LONG);
                    }
                }
                // Do not allow non-app pages to load inside the WebView that exposes AndroidBridge.
                return true;
            }
        });

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
                    showToast("Tidak ada aplikasi pemilih file yang tersedia.", Toast.LENGTH_LONG);
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
            runOnUiThread(() -> beginSaveText(fileName, content, mimeType));
        }

        @JavascriptInterface
        public void saveBase64File(String fileName, String base64Content, String mimeType) {
            try {
                if (base64Content == null) {
                    throw new IllegalArgumentException("Isi file kosong.");
                }
                byte[] decoded = Base64.decode(base64Content, Base64.DEFAULT);
                runOnUiThread(() -> beginSaveBytes(fileName, decoded, mimeType));
            } catch (IllegalArgumentException ex) {
                runOnUiThread(() -> showToast("Data JPG/PDF tidak valid: " + ex.getMessage(), Toast.LENGTH_LONG));
            }
        }

        @JavascriptInterface
        public void printReceiptHTML(String html, String jobName) {
            if (html == null || html.trim().isEmpty()) {
                runOnUiThread(() -> showToast("Isi struk kosong; pencetakan dibatalkan.", Toast.LENGTH_LONG));
                return;
            }
            runOnUiThread(() -> startReceiptPrint(html, jobName));
        }
    }

    private void beginSaveText(String fileName, String content, String mimeType) {
        pendingSaveContent = content == null ? "" : content;
        pendingSaveBytes = null;
        launchSavePicker(fileName, mimeType);
    }

    private void beginSaveBytes(String fileName, byte[] content, String mimeType) {
        pendingSaveContent = null;
        pendingSaveBytes = content == null ? new byte[0] : content;
        launchSavePicker(fileName, mimeType);
    }

    private void launchSavePicker(String fileName, String mimeType) {
        pendingSaveName = sanitizeFileName(fileName);
        pendingSaveMime = (mimeType == null || mimeType.trim().isEmpty())
                ? "application/octet-stream" : mimeType;

        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType(pendingSaveMime);
        intent.putExtra(Intent.EXTRA_TITLE, pendingSaveName);
        intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
        try {
            startActivityForResult(intent, REQUEST_CREATE_FILE);
        } catch (ActivityNotFoundException ex) {
            clearPendingSave();
            showToast("Tidak ada aplikasi penyimpanan file yang tersedia.", Toast.LENGTH_LONG);
        }
    }

    private String sanitizeFileName(String name) {
        if (name == null || name.trim().isEmpty()) return "ande-serenada-data.txt";
        String clean = name.trim().replaceAll("[\\\\/:*?\"<>|]", "_");
        return clean.isEmpty() ? "ande-serenada-data.txt" : clean;
    }

    private String sanitizePrintJobName(String name) {
        if (name == null || name.trim().isEmpty()) return "Ande Serenada - Struk";
        String clean = name.trim().replaceAll("[\\\\/:*?\"<>|]", "_");
        return clean.length() > 80 ? clean.substring(0, 80) : clean;
    }

    private void startReceiptPrint(String html, String requestedJobName) {
        if (android.os.Build.VERSION.SDK_INT < 19) {
            showToast("Fitur cetak Android tidak didukung pada versi perangkat ini.", Toast.LENGTH_LONG);
            return;
        }

        PrintManager printManager = (PrintManager) getSystemService(Context.PRINT_SERVICE);
        if (printManager == null) {
            showToast("Layanan cetak Android tidak tersedia.", Toast.LENGTH_LONG);
            return;
        }

        final String jobName = sanitizePrintJobName(requestedJobName);
        final WebView printWebView = new WebView(this);
        WebSettings printSettings = printWebView.getSettings();
        printSettings.setJavaScriptEnabled(false);
        printSettings.setDomStorageEnabled(false);
        printSettings.setAllowFileAccess(false);
        printSettings.setAllowContentAccess(false);
        printSettings.setDefaultTextEncodingName("UTF-8");
        printWebView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        activePrintWebViews.add(printWebView);

        printWebView.setWebViewClient(new WebViewClient() {
            private boolean printStarted;

            @Override
            public void onPageFinished(WebView view, String url) {
                if (printStarted) return;
                printStarted = true;
                view.postDelayed(() -> {
                    if (isFinishing() || isDestroyed()) {
                        releasePrintWebView(printWebView);
                        return;
                    }
                    try {
                        android.print.PrintDocumentAdapter adapter =
                                printWebView.createPrintDocumentAdapter(jobName);
                        PrintAttributes attributes = new PrintAttributes.Builder()
                                .setMediaSize(PrintAttributes.MediaSize.UNKNOWN_PORTRAIT)
                                .setColorMode(PrintAttributes.COLOR_MODE_COLOR)
                                .build();
                        PrintJob job = printManager.print(jobName, adapter, attributes);
                        if (job == null) {
                            releasePrintWebView(printWebView);
                        } else {
                            monitorPrintJob(job, printWebView);
                        }
                    } catch (RuntimeException ex) {
                        releasePrintWebView(printWebView);
                        showToast("Gagal membuka dialog cetak: " + ex.getMessage(), Toast.LENGTH_LONG);
                    }
                }, 300);
            }
        });

        printWebView.loadDataWithBaseURL(
                "https://ande-serenada.invalid/",
                html,
                "text/html",
                "UTF-8",
                null
        );
    }

    private void monitorPrintJob(PrintJob job, WebView printWebView) {
        Runnable previous = printMonitorTasks.remove(printWebView);
        if (previous != null) mainHandler.removeCallbacks(previous);
        Runnable monitor = new Runnable() {
            @Override
            public void run() {
                if (job.isCompleted() || job.isCancelled() || job.isFailed()) {
                    releasePrintWebView(printWebView);
                } else {
                    mainHandler.postDelayed(this, 1000);
                }
            }
        };
        printMonitorTasks.put(printWebView, monitor);
        mainHandler.postDelayed(monitor, 1000);
    }

    private void releasePrintWebView(WebView printWebView) {
        if (printWebView == null) return;
        Runnable monitor = printMonitorTasks.remove(printWebView);
        if (monitor != null) mainHandler.removeCallbacks(monitor);
        activePrintWebViews.remove(printWebView);
        try {
            printWebView.stopLoading();
            printWebView.setWebViewClient(null);
            printWebView.destroy();
        } catch (Exception ignored) {
            // Best-effort cleanup after the Android print job has ended.
        }
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
                    if (pendingSaveBytes != null) {
                        output.write(pendingSaveBytes);
                    } else {
                        output.write((pendingSaveContent == null ? "" : pendingSaveContent)
                                .getBytes(StandardCharsets.UTF_8));
                    }
                    output.flush();
                    showToast("File berhasil disimpan.", Toast.LENGTH_SHORT);
                } catch (Exception ex) {
                    showToast("Gagal menyimpan file: " + ex.getMessage(), Toast.LENGTH_LONG);
                }
            }
            clearPendingSave();
        }
    }

    private void clearPendingSave() {
        pendingSaveContent = null;
        pendingSaveBytes = null;
        pendingSaveMime = null;
        pendingSaveName = null;
    }

    private void showToast(String message, int duration) {
        Toast.makeText(this, message, duration).show();
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
        mainHandler.removeCallbacksAndMessages(null);
        for (WebView printWebView : new ArrayList<>(activePrintWebViews)) {
            try {
                printWebView.stopLoading();
                printWebView.setWebViewClient(null);
                printWebView.destroy();
            } catch (Exception ignored) {
                // Best-effort cleanup during Activity teardown.
            }
        }
        activePrintWebViews.clear();
        printMonitorTasks.clear();

        if (pendingFileCallback != null) {
            pendingFileCallback.onReceiveValue(null);
            pendingFileCallback = null;
        }
        if (webView != null) {
            webView.removeJavascriptInterface("AndroidBridge");
            webView.destroy();
            webView = null;
        }
        super.onDestroy();
    }
}
