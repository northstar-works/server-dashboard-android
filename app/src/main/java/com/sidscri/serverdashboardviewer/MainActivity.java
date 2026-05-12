package com.sidscri.serverdashboardviewer;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
import android.content.res.Configuration;
import android.text.TextUtils;
import android.view.Window;
import android.view.Gravity;
import android.view.View;
import android.webkit.ConsoleMessage;
import android.webkit.CookieManager;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Locale;

public class MainActivity extends Activity {
    private static final String DASHBOARD_URL = "http://home.sidneyshelton.com:8029/";
    private WebView webView;
    private TextView statusText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Window window = getWindow();
        window.setStatusBarColor(Color.rgb(9, 13, 20));
        window.setNavigationBarColor(Color.rgb(9, 13, 20));
        applyImmersiveMode();
        buildUi();
        configureWebView();
        if (savedInstanceState == null) {
            webView.loadUrl(DASHBOARD_URL);
        } else {
            webView.restoreState(savedInstanceState);
        }
    }

    private void buildUi() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.rgb(9, 13, 20));
        // Do not reserve a fake navigation-bar gutter here. On Samsung/Android landscape
        // that caused the WebView to lose a large strip of usable width. We hide system
        // bars in immersive mode instead, so the WebView can use the full screen.
        root.setPadding(0, 0, 0, 0);

        LinearLayout bar = new LinearLayout(this);
        bar.setOrientation(LinearLayout.HORIZONTAL);
        bar.setGravity(Gravity.CENTER_VERTICAL);
        bar.setPadding(dp(4), dp(4), dp(4), dp(4));
        bar.setBackgroundColor(Color.rgb(13, 18, 28));

        Button back = makeButton("‹");
        back.setOnClickListener(v -> {
            if (webView.canGoBack()) webView.goBack();
            else Toast.makeText(this, "No back history", Toast.LENGTH_SHORT).show();
        });

        Button home = makeButton("Home");
        home.setOnClickListener(v -> webView.loadUrl(DASHBOARD_URL));

        Button refresh = makeButton("Refresh");
        refresh.setOnClickListener(v -> webView.reload());

        Button external = makeButton("Open");
        external.setOnClickListener(v -> openExternal(webView.getUrl()));

        statusText = new TextView(this);
        statusText.setText("Server Dashboard");
        statusText.setTextColor(Color.rgb(205, 214, 226));
        statusText.setTextSize(11);
        statusText.setSingleLine(true);
        statusText.setEllipsize(TextUtils.TruncateAt.MIDDLE);
        statusText.setPadding(dp(6), 0, dp(6), 0);
        LinearLayout.LayoutParams statusParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);

        bar.addView(back);
        bar.addView(home);
        bar.addView(refresh);
        bar.addView(statusText, statusParams);
        bar.addView(external);

        webView = new WebView(this);
        LinearLayout.LayoutParams webParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1f
        );

        root.addView(bar, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        root.addView(webView, webParams);
        setContentView(root);
    }

    private Button makeButton(String text) {
        Button b = new Button(this);
        b.setText(text);
        b.setAllCaps(false);
        b.setTextSize(11);
        b.setTextColor(Color.rgb(223, 232, 245));
        b.setBackgroundColor(Color.rgb(21, 29, 43));
        b.setPadding(dp(6), 0, dp(6), 0);
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                dp(34)
        );
        p.setMargins(dp(1), 0, dp(1), 0);
        b.setLayoutParams(p);
        return b;
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void configureWebView() {
        WebSettings s = webView.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setDatabaseEnabled(true);
        s.setMediaPlaybackRequiresUserGesture(false);
        s.setLoadWithOverviewMode(false);
        s.setUseWideViewPort(true);
        s.setTextZoom(100);
        s.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.NORMAL);
        s.setBuiltInZoomControls(true);
        s.setDisplayZoomControls(false);
        s.setSupportMultipleWindows(true);
        s.setJavaScriptCanOpenWindowsAutomatically(true);
        s.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        s.setUserAgentString(s.getUserAgentString() + " ServerDashboardAndroidViewer/1.0.6");
        webView.setInitialScale(100);
        webView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);

        CookieManager.getInstance().setAcceptCookie(true);
        CookieManager.getInstance().setAcceptThirdPartyCookies(webView, true);

        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                String url = view.getUrl() == null ? DASHBOARD_URL : view.getUrl();
                statusText.setText(newProgress < 100 ? "Loading " + newProgress + "%" : shortUrl(url));
            }

            @Override
            public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
                return true;
            }

            @Override
            public boolean onCreateWindow(WebView view, boolean isDialog, boolean isUserGesture, Message resultMsg) {
                WebView popup = new WebView(MainActivity.this);
                popup.setWebViewClient(new WebViewClient() {
                    @Override
                    public boolean shouldOverrideUrlLoading(WebView popupView, WebResourceRequest request) {
                        webView.loadUrl(request.getUrl().toString());
                        return true;
                    }

                    @Override
                    public boolean shouldOverrideUrlLoading(WebView popupView, String url) {
                        webView.loadUrl(url);
                        return true;
                    }
                });
                WebView.WebViewTransport transport = (WebView.WebViewTransport) resultMsg.obj;
                transport.setWebView(popup);
                resultMsg.sendToTarget();
                return true;
            }
        });

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                return handleNavigation(request.getUrl());
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return handleNavigation(Uri.parse(url));
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                statusText.setText(shortUrl(url));
                injectDashboardMobileFixes(view, url);
                super.onPageFinished(view, url);
            }
        });
    }

    private void injectDashboardMobileFixes(WebView view, String url) {
        if (url == null) return;
        Uri uri = Uri.parse(url);
        String host = uri.getHost() == null ? "" : uri.getHost().toLowerCase(Locale.US);
        if (!host.equals("home.sidneyshelton.com") && !host.endsWith(".sidneyshelton.com")) return;

        String js = "(function(){" +
                "var id='sdav-mobile-tile-fix';" +
                "var old=document.getElementById(id); if(old) old.remove();" +
                "var st=document.createElement('style'); st.id=id;" +
                "st.textContent=`" +
                "html,body{ -webkit-text-size-adjust:100% !important; text-size-adjust:100% !important; overflow-x:hidden !important; }" +
                ".container{max-width:none !important; width:100% !important; box-sizing:border-box !important;}" +
                ".sgrid{grid-template-columns:repeat(auto-fill,minmax(min(520px,100%),1fr)) !important;}" +
                ".svc{box-sizing:border-box !important; width:100% !important; max-width:100% !important; height:auto !important; min-height:0 !important; max-height:none !important;} .res-cpu,.cat-cpu{color:#f59e0b!important}.res-ram,.cat-ram{color:#a855f7!important}.res-disk,.cat-disk,.sdisk{color:#06b6d4!important} .cat-resources{display:inline-flex!important;gap:6px!important;flex-wrap:wrap!important}" +
                ".srow,.sinfo,.smeta,.sctrl,.saux-row{min-width:0 !important; max-width:100% !important;}" +
                ".sname,.sdesc{overflow:hidden !important; text-overflow:ellipsis !important;}" +
                "@media(max-width:820px){" +
                ".container{padding-left:10px !important; padding-right:10px !important;}" +
                ".sgrid,.asa-category .sgrid.asa-children-grid{display:grid !important; grid-template-columns:1fr !important; gap:10px !important; padding-left:0 !important; margin-left:0 !important;}" +
                ".svc{display:grid !important; grid-template-columns:10px minmax(0,1fr) !important; grid-template-areas:'dot row' '. meta' '. ctrl' '. aux' !important; align-items:center !important; align-content:start !important; gap:7px 10px !important; padding:12px 12px 12px 14px !important; overflow:hidden !important;}" +
                ".svc-child{display:grid !important; grid-template-columns:10px minmax(0,1fr) !important; min-height:72px !important; padding:12px 12px 12px 14px !important;}" +
                ".sdot{grid-area:dot !important; align-self:center !important; justify-self:center !important;}" +
                ".srow{grid-area:row !important; display:flex !important; align-items:center !important; gap:10px !important; width:100% !important;}" +
                ".svc-icon,.svc-icon-emoji{width:36px !important; height:36px !important; min-width:36px !important; flex:0 0 36px !important;}" +
                ".sinfo{flex:1 1 auto !important; min-width:0 !important; width:auto !important;}" +
                ".sname{font-size:14px !important; line-height:1.2 !important; white-space:nowrap !important; word-break:normal !important;}" +
                ".sdesc{font-size:11px !important; line-height:1.25 !important; white-space:nowrap !important; word-break:normal !important;}" +
                ".smeta{grid-area:meta !important; display:flex !important; flex-wrap:wrap !important; justify-content:flex-start !important; gap:6px !important; width:100% !important;}" +
                ".sctrl{grid-area:ctrl !important; display:flex !important; flex-wrap:wrap !important; justify-content:flex-start !important; gap:5px !important; width:100% !important;}" +
                ".cbtn{width:30px !important; height:30px !important; min-width:30px !important; flex:0 0 30px !important;}" +
                ".saux-row{grid-area:aux !important; display:block !important; width:100% !important; margin-top:2px !important; padding-left:0 !important; white-space:nowrap !important; overflow:hidden !important; text-overflow:ellipsis !important;}" +
                ".ark-summary-text,.mpd-now{white-space:nowrap !important; overflow:hidden !important; text-overflow:ellipsis !important; display:block !important; max-width:100% !important;}" +
                ".svc *{overflow-wrap:normal !important;}" +
                "}" +
                "@media(max-width:420px){" +
                ".container{padding-left:8px !important; padding-right:8px !important;}" +
                ".svc{grid-template-columns:8px minmax(0,1fr) !important; gap:6px 8px !important; padding:10px 10px 10px 12px !important;}" +
                ".sname{font-size:13px !important;} .sdesc{font-size:10px !important;}" +
                ".cbtn{width:28px !important; height:28px !important; min-width:28px !important; flex-basis:28px !important;}" +
                "}" +
                "`; document.head.appendChild(st);" +
                "window.dispatchEvent(new Event('resize'));" +
                "})();";
        view.evaluateJavascript(js, null);
    }

    private boolean handleNavigation(Uri uri) {
        String scheme = uri.getScheme() == null ? "" : uri.getScheme().toLowerCase(Locale.US);
        if (scheme.equals("http") || scheme.equals("https")) {
            // Keep dashboard View/Open links inside the app. The native Open button is the only
            // action that intentionally launches the external Android browser.
            return false;
        }
        openExternal(uri.toString());
        return true;
    }

    private boolean isAllowedHost(String host) {
        if (host == null) return false;
        String h = host.toLowerCase(Locale.US);
        if (h.equals("home.sidneyshelton.com") || h.equals("sidneyshelton.com") || h.endsWith(".sidneyshelton.com")) {
            return true;
        }
        return h.startsWith("192.168.0.") || h.equals("127.0.0.1") || h.equals("localhost");
    }

    private void openExternal(String url) {
        if (url == null || url.trim().isEmpty()) return;
        try {
            Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(i);
        } catch (ActivityNotFoundException ignored) {
            Toast.makeText(this, "No app can open this link", Toast.LENGTH_SHORT).show();
        }
    }

    private String shortUrl(String url) {
        if (url == null) return "Server Dashboard";
        try {
            Uri uri = Uri.parse(url);
            String host = uri.getHost();
            String path = uri.getPath();
            if (host == null) return "Server Dashboard";
            if (path == null || path.equals("/")) return host;
            return host + path;
        } catch (Exception e) {
            return "Server Dashboard";
        }
    }

    private int getStatusBarHeight() {
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            return getResources().getDimensionPixelSize(resourceId);
        }
        return 0;
    }

    private int getNavigationBarWidth() {
        int resourceId = getResources().getIdentifier("navigation_bar_width", "dimen", "android");
        if (resourceId > 0) {
            return getResources().getDimensionPixelSize(resourceId);
        }
        return 0;
    }

    private int getNavigationBarHeight() {
        int resourceId = getResources().getIdentifier("navigation_bar_height", "dimen", "android");
        if (resourceId > 0) {
            return getResources().getDimensionPixelSize(resourceId);
        }
        return 0;
    }

    private void applyImmersiveMode() {
        Window window = getWindow();
        View decor = window.getDecorView();
        decor.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        );
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            applyImmersiveMode();
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        applyImmersiveMode();
        if (webView != null) {
            webView.post(() -> {
                webView.requestLayout();
                webView.evaluateJavascript("window.dispatchEvent(new Event('resize'));", null);
            });
        }
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        webView.saveState(outState);
    }

    @Override
    public void onBackPressed() {
        if (webView != null && webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }
}
