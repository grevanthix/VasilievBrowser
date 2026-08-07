/*
   Copyright 2026 grevanthix

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at                                                  
       http://www.apache.org/licenses/LICENSE-2.0                                           
   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
package ru.unknows.vasiliev;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.webkit.CookieManager;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.WebResourceRequest;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.TextView;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MainActivity extends Activity {

    private static final int FILE_CHOOSER_RESULT_CODE = 10000;
    private static final int REQUEST_HISTORY = 200;
    private ValueCallback<Uri[]> myUploadMsg;
    private LinearLayout layoutMain;
    private View theToolbar;
    private EditText theUrlInput;
    private ProgressBar loadBar;
    private FrameLayout webBox;
    
    private View findBarLayout;
    private View findInputContainer;
    private EditText findInput;
    private TextView findCounter;
    private ImageButton btnFindPrev;
    private ImageButton btnFindNext;
    private ImageButton btnFindClose;

    private List<BrowserTab> allTabs = new ArrayList<BrowserTab>();
    private BrowserTab activeTab = null;

    private ImageButton btnFwd;
    private ImageButton btnBack;
    private ImageButton btnGrid;
    private SharedPreferences myPrefs;
    private HistoryDbHelper myDb;

    private boolean jsOn;
    private boolean imgsOn;
    private int searchIdx;
    private boolean toolbarBtm;
    private boolean homeCustom;
    private boolean showFwd;
    private boolean showBack;
    private boolean restoreTabsOn;
    private boolean blackBgOn;
    private String homeUrl;
    private int uaIdx;
    private View gridOverlay;
    private GridView gridTabs;
    private ImageButton gridMenuBtn;
    private ImageButton gridAddBtn;
    private final Set<String> allowedUrls = new HashSet<String>();
    
    /* === ДАЛЬШЕ БОГА НЕТ === */
     
    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleManager.setLocale(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        myDb = new HistoryDbHelper(this);
        myPrefs = getSharedPreferences("vasiliev_prefs", MODE_PRIVATE);

        layoutMain = (LinearLayout) findViewById(R.id.mainLayout);
        theToolbar = findViewById(R.id.toolbarLayout);
        theUrlInput = (EditText) findViewById(R.id.urlInput);
        loadBar = (ProgressBar) findViewById(R.id.progressBar);
        webBox = (FrameLayout) findViewById(R.id.webViewContainer);
        
        findBarLayout = findViewById(R.id.findBarLayout);
        findInputContainer = findViewById(R.id.findInputContainer);
        findInput = (EditText) findViewById(R.id.findInput);
        findCounter = (TextView) findViewById(R.id.findCounter);
        btnFindPrev = (ImageButton) findViewById(R.id.btnFindPrev);
        btnFindNext = (ImageButton) findViewById(R.id.btnFindNext);
        btnFindClose = (ImageButton) findViewById(R.id.btnFindClose);

        btnFwd = (ImageButton) findViewById(R.id.btnForward);
        btnBack = (ImageButton) findViewById(R.id.btnBack);
        btnGrid = (ImageButton) findViewById(R.id.btnGridToolbar);
        ImageButton btnGo = (ImageButton) findViewById(R.id.btnGo);
        ImageButton btnMenu = (ImageButton) findViewById(R.id.btnMenu);
        View over = getLayoutInflater().inflate(R.layout.overlay_tab_grid, null);
        addContentView(over, new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));
        
        gridOverlay = over.findViewById(R.id.tabGridOverlay);
        gridTabs = (GridView) over.findViewById(R.id.tabsGridView);
        gridMenuBtn = (ImageButton) over.findViewById(R.id.btnGridMenu);
        gridAddBtn = (ImageButton) over.findViewById(R.id.btnGridAddTab);
        
        gridMenuBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu p = new PopupMenu(MainActivity.this, v);
                p.getMenu().add(Menu.NONE, 1, 1, R.string.menu_close_all_tabs);
                p.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
        
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        if (item.getItemId() == 1) {
                            allTabs.clear();
        
                            finish();
                            return true;
                        }
                        return false;
  
                    }
                });
                p.show();
            }
        });
        gridAddBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gridOverlay.setVisibility(View.GONE);
                makeNewTab(false, null);
            }
        });
        
        findInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (activeTab != null && activeTab.myWeb != null) {
                    activeTab.myWeb.findAllAsync(s.toString());
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        btnFindNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (activeTab != null && activeTab.myWeb != null) {
                    activeTab.myWeb.findNext(true);
                }
            }
        });

        btnFindPrev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (activeTab != null && activeTab.myWeb != null) {
                    activeTab.myWeb.findNext(false);
                }
            }
        });

        btnFindClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeFindBar();
            }
        });

        if (myPrefs.getBoolean("is_first_run", true) == true) {
            Intent i = new Intent(MainActivity.this, WelcomeActivity.class);
            startActivityForResult(i, 101);
        } else {
            startBrowser();
        }

        btnFwd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (activeTab != null && activeTab.myWeb != null) {
                    if (activeTab.myWeb.canGoForward()) {
                
                        activeTab.myWeb.goForward();
                    }
                }
            }
        });
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (activeTab != null && activeTab.myWeb != null) {
                    if (activeTab.myWeb.canGoBack()) {

                        activeTab.myWeb.goBack();
                    }
                }
            }
        });
        btnGo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goFromInput();
            }
        });
        theUrlInput.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int aId, KeyEvent ev) {
                if (aId == EditorInfo.IME_ACTION_GO || (ev != null && ev.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                    goFromInput();
                  
                    return true;
                }
                return false;
            }
        });
        btnGrid.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openGrid();
            }
        });
        btnMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openMenu(v);
            }
        });
    }

    private void closeFindBar() {
        findBarLayout.setVisibility(View.GONE);
        findInput.setText("");
        if (activeTab != null && activeTab.myWeb != null) {
            activeTab.myWeb.clearMatches();
        }
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(findInput.getWindowToken(), 0);
        }
    }

    private void startBrowser() {
        getSettings();
        buildLayouts();
        if (allTabs.isEmpty() == true) {
            getSavedTabs();
        }
        checkIntent(getIntent());
    }

    @Override
    protected void onNewIntent(Intent i) {
        super.onNewIntent(i);
        setIntent(i);
        checkIntent(i);
    }

    private void checkIntent(Intent i) {
        if (i != null && Intent.ACTION_VIEW.equals(i.getAction())) {
            if (i.getDataString() != null) {
                String u = i.getDataString();
                makeNewTab(false, u);
            }
        }
    }

    private void getSavedTabs() {
        if (restoreTabsOn == false) {
            makeNewTab(false, null);
            return;
        }
        String s = myPrefs.getString("saved_tabs", "[]");
        try {
            org.json.JSONArray arr = new org.json.JSONArray(s);
            if (arr.length() == 0) {
                makeNewTab(false, null);
            } else {
                for (int x = 0; x < arr.length(); x++) {
                    String u = arr.getString(x);
                    if (u.equals("vasiliev://newtab")) {
                        makeNewTab(false, null);
                    } else {
                        makeNewTab(false, u);
                    }
                    loadPreviewFile(allTabs.get(allTabs.size() - 1), x);
                }
            }
        } catch (Exception e) {
            makeNewTab(false, null);
        }
    }

    private void saveMyTabs() {
        try {
            org.json.JSONArray arr = new org.json.JSONArray();
            for (int x = 0; x < allTabs.size(); x++) {
                BrowserTab t = allTabs.get(x);
                if (t.incognito == false) {
                    if (t.myNtp.getVisibility() == View.VISIBLE) {
                        arr.put("vasiliev://newtab");
                    } else {
                        String u = t.myWeb.getUrl();
                        if (u != null && u.equals("about:blank") == false) {
                            arr.put(u);
                        }
                    }
                    if (restoreTabsOn == true) {
                        savePreviewFile(t, x);
                    }
                }
            }
            myPrefs.edit().putString("saved_tabs", arr.toString()).apply();
        } catch (Exception pizdec) {}
    }

    private void savePreviewFile(BrowserTab t, int idx) {
        if (t.img == null) return;
        try {
            int smallW = 200;
            int smallH = (int) (t.img.getHeight() * (200.0 / t.img.getWidth()));
            if (smallH <= 0) smallH = 1;
            Bitmap small = Bitmap.createScaledBitmap(t.img, smallW, smallH, true);
            File f = new File(getCacheDir(), "tab_preview_" + idx + ".jpg");
            FileOutputStream fos = new FileOutputStream(f);
            small.compress(Bitmap.CompressFormat.JPEG, 70, fos);
            fos.close();
            small.recycle();
        } catch (Exception pizdec) {}
    }

    private void loadPreviewFile(BrowserTab t, int idx) {
        try {
            File f = new File(getCacheDir(), "tab_preview_" + idx + ".jpg");
            if (f.exists()) {
                Bitmap b = android.graphics.BitmapFactory.decodeFile(f.getAbsolutePath());
                if (b != null) {
                    t.img = b;
                }
            }
        } catch (Exception e) {}
    }

    private void deletePreviewFile(BrowserTab t) {
        int idx = allTabs.indexOf(t);
        File f = new File(getCacheDir(), "tab_preview_" + idx + ".jpg");
        if (f.exists()) {
            f.delete();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        saveMyTabs();
    }

    @Override
    protected void onActivityResult(int reqCode, int resCode, Intent d) {
        if (reqCode == 101 && resCode == RESULT_OK) {
            startBrowser();
        } else if (reqCode == REQUEST_HISTORY && resCode == RESULT_OK && d != null) {
            String u = d.getStringExtra(HistoryActivity.EXTRA_URL);
            if (u != null && activeTab != null) {
                makeNewTab(false, u);
            }
        } else if (reqCode == FILE_CHOOSER_RESULT_CODE) {
            if (myUploadMsg == null) return;
            Uri[] res = null;
            if (resCode == RESULT_OK && d != null) {
                String s = d.getDataString();
                if (s != null) {
                    res = new Uri[1];
                    res[0] = Uri.parse(s);
                } else if (d.getClipData() != null) {
                    int cnt = d.getClipData().getItemCount();
                    res = new Uri[cnt];
                    for (int x = 0; x < cnt; x++) {
                        res[x] = d.getClipData().getItemAt(x).getUri();
                    }
                }
            }
            myUploadMsg.onReceiveValue(res);
            myUploadMsg = null;
        } else {
            super.onActivityResult(reqCode, resCode, d);
        }
    }

    private void getSettings() {
        jsOn = myPrefs.getBoolean("pref_js", true);
        imgsOn = myPrefs.getBoolean("pref_images", true);
        searchIdx = myPrefs.getInt("pref_search_engine_index", 0);
        toolbarBtm = myPrefs.getBoolean("pref_toolbar_bottom", false);
        homeCustom = myPrefs.getBoolean("pref_homepage_custom", false);
        homeUrl = myPrefs.getString("pref_homepage", "https://google.com");
        uaIdx = myPrefs.getInt("pref_user_agent", 0);
        showFwd = myPrefs.getBoolean("pref_show_forward_btn", true);
        showBack = myPrefs.getBoolean("pref_show_back_btn", true);
        restoreTabsOn = myPrefs.getBoolean("pref_restore_tabs", true);
        blackBgOn = myPrefs.getBoolean("pref_black_bg", false);

        int rad = myPrefs.getInt("pref_corner_radius", 12);
        CornerUtils.applyRadius(this, rad);
    }

    private void buildLayouts() {
        layoutMain.removeView(theToolbar);
        layoutMain.removeView(findBarLayout);
        layoutMain.removeView(loadBar);
        layoutMain.removeView(webBox);
        
        if (btnFwd != null) {
            if (showFwd == true) {
                btnFwd.setVisibility(View.VISIBLE);
            } else {
                btnFwd.setVisibility(View.GONE);
            }
        }

        if (btnBack != null) {
            if (showBack == true) {
                btnBack.setVisibility(View.VISIBLE);
            } else {
                btnBack.setVisibility(View.GONE);
            }
        }

        if (toolbarBtm == true) {
            layoutMain.addView(loadBar);
            layoutMain.addView(webBox);
            layoutMain.addView(findBarLayout);
            layoutMain.addView(theToolbar);
        } else {
            layoutMain.addView(theToolbar);
            layoutMain.addView(findBarLayout);
            layoutMain.addView(loadBar);
            layoutMain.addView(webBox);
        }
    }

    private void setupWebViews() {
        String uas = "";
        if (uaIdx == 1) {
            uas = "Mozilla/5.0 (iPhone; CPU iPhone OS 17_4 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/17.4 Mobile/15E148 Safari/604.1";
        } else if (uaIdx == 2) {
            uas = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36";
        } else {
            uas = "Mozilla/5.0 (Linux; Android 10; K) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Mobile Safari/537.36";
        }

        for (int i = 0; i < allTabs.size(); i++) {
            BrowserTab t = allTabs.get(i);
            WebSettings ws = t.myWeb.getSettings();
            ws.setJavaScriptEnabled(jsOn);
            ws.setLoadsImagesAutomatically(imgsOn);
            ws.setUserAgentString(uas);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (myPrefs.getBoolean("is_first_run", true) == false) {
            getSettings();
            buildLayouts();
            setupWebViews();
            if (activeTab != null) {
                changeTab(activeTab);
            }
            
            for (int i = 0; i < allTabs.size(); i++) {
                BrowserTab tab = allTabs.get(i);
                if (tab.myNtp != null) {
                    View c = tab.myNtp.findViewById(R.id.ntpInputContainer);
                    if (c != null) {
                        if (tab.incognito) {
                            c.setBackgroundResource(R.drawable.bg_input_incognito);
                        } else {
                            c.setBackgroundResource(R.drawable.bg_input);
                        }
                    }
                }
            }
        }
    }

    private void runUrl(BrowserTab t, String s) {
        String finalU = "";
        if (s.regionMatches(true, 0, "javascript:", 0, 11)) {
            finalU = s;
        } else if (!s.contains(".") || s.contains(" ")) {
            String eng = SettingsActivity.searchUrls[searchIdx];
            finalU = eng + Uri.encode(s);
        } else {
            if (s.startsWith("http://") == false && s.startsWith("https://") == false) {
                finalU = "https://" + s;
            } else {
                finalU = s;
            }
        }
        
        checkSafeAndLoad(t, finalU);
    }

    private void checkSafeAndLoad(final BrowserTab t, final String u) {
        if (!u.startsWith("http://") && !u.startsWith("https://")) {
            t.myNtp.setVisibility(View.GONE);
            t.myWeb.setVisibility(View.VISIBLE);
            t.myWeb.loadUrl(u);
            return;
        }
        
        SiteRegistryManager.DangerLevel lvl = SiteRegistryManager.getInstance(this).checkUrl(u);
        if (lvl == null) {
            t.myNtp.setVisibility(View.GONE);
            t.myWeb.setVisibility(View.VISIBLE);
            t.myWeb.loadUrl(u);
            return;
        }
        
        if (lvl == SiteRegistryManager.DangerLevel.BLOCKED) {
            android.app.AlertDialog.Builder b = new android.app.AlertDialog.Builder(this);
            b.setTitle(getString(R.string.registry_blocked_title));
            b.setMessage(getString(R.string.registry_blocked_msg));
            b.setPositiveButton(getString(R.string.registry_close), null);
            b.setCancelable(true);
            b.show();
        } else {
            android.app.AlertDialog.Builder b = new android.app.AlertDialog.Builder(this);
            b.setTitle(getString(R.string.registry_warning_title));
            b.setMessage(getString(R.string.registry_warning_msg));
            b.setPositiveButton(getString(R.string.registry_open), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface d, int w) {
                    allowedUrls.add(u);
                    t.myNtp.setVisibility(View.GONE);
                    t.myWeb.setVisibility(View.VISIBLE);
             
                    t.myWeb.loadUrl(u);
                }
            });
            b.setNegativeButton(getString(R.string.registry_cancel), null);
            b.setCancelable(true);
            b.show();
        }
    }

    private void goFromInput() {
        if (activeTab == null) return;
        String s = theUrlInput.getText().toString().trim();
        if (s.isEmpty() == false) {
            runUrl(activeTab, s);
        }
    }

    private int bgColor(int c) {
        if (blackBgOn == true) {
            return android.graphics.Color.BLACK;
        }
        return c;
    }

    private void fixForwardBtn() {
        if (btnFwd == null) return;
        boolean canGo = false;
        if (activeTab != null && activeTab.myWeb != null) {
            canGo = activeTab.myWeb.canGoForward();
        }
        
        int c = 0;
        if (canGo == true) {
            c = getResources().getColor(R.color.text_high_emphasis);
        } else {
            c = getResources().getColor(R.color.text_medium_emphasis);
        }
        
        if (btnFwd.getDrawable() != null) {
            btnFwd.getDrawable().setColorFilter(c, android.graphics.PorterDuff.Mode.SRC_IN);
        }
        btnFwd.setEnabled(canGo);
    }

    private void fixBackBtn() {
        if (btnBack == null) return;
        boolean canGo = false;
        if (activeTab != null && activeTab.myWeb != null) {
            canGo = activeTab.myWeb.canGoBack();
        }

        int c = 0;
        if (canGo == true) {
            c = getResources().getColor(R.color.text_high_emphasis);
        } else {
            c = getResources().getColor(R.color.text_medium_emphasis);
        }

        if (btnBack.getDrawable() != null) {
            btnBack.getDrawable().setColorFilter(c, android.graphics.PorterDuff.Mode.SRC_IN);
        }
        btnBack.setEnabled(canGo);
    }

    private boolean isFileDownload(String u) {
        if (u == null) return false;
        String low = u.toLowerCase();
        int pos = low.indexOf('?');
        if (pos != -1) {
            low = low.substring(0, pos);
        }
        if (low.endsWith(".apk") || low.endsWith(".zip") || low.endsWith(".rar") ||
            low.endsWith(".pdf") || low.endsWith(".mp3") || low.endsWith(".mp4") ||
            low.endsWith(".7z") || low.endsWith(".tar") || low.endsWith(".gz") ||
            low.endsWith(".exe") || low.endsWith(".msi")) {
            return true;
        }
        
        String ext = android.webkit.MimeTypeMap.getFileExtensionFromUrl(u);
        if (ext != null && !ext.isEmpty()) {
            String mime = android.webkit.MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext.toLowerCase());
            if (mime != null && (mime.startsWith("application/") || mime.startsWith("audio/") || mime.startsWith("video/"))) {
                return true;
            }
        }
        return false;
    }

    private void startDl(String u, String ua, String disp, String mime) {
        try {
            String name = null;
            if (disp != null) {
                int utfI = disp.toLowerCase().indexOf("filename*=utf-8''");
                if (utfI != -1) {
                    try {
                        String enc = disp.substring(utfI + 17).trim();
                        int semi = enc.indexOf(";");
                        if (semi != -1) {
                            enc = enc.substring(0, semi).trim();
                        }
                        name = Uri.decode(enc);
                    } catch (Exception e) {}
                }
                if (name == null || name.isEmpty()) {
                    int id = disp.toLowerCase().indexOf("filename=");
                    if (id != -1) {
                        name = disp.substring(id + 9).trim();
                        if (name.startsWith("\"") && name.endsWith("\"") && name.length() > 1) {
                            name = name.substring(1, name.length() - 1);
                        } else if (name.startsWith("'") && name.endsWith("'") && name.length() > 1) {
                            name = name.substring(1, name.length() - 1);
                        }
                        int semi = name.indexOf(";");
                        if (semi != -1) {
                            name = name.substring(0, semi).trim();
                        }
                    }
                }
            }
            if (name == null || name.isEmpty() || name.equalsIgnoreCase("download.bin")) {
                name = android.webkit.URLUtil.guessFileName(u, disp, mime);
            }
            if (name == null || name.isEmpty() || name.equalsIgnoreCase("download.bin")) {
                String last = android.net.Uri.parse(u).getLastPathSegment();
                if (last != null && last.isEmpty() == false) {
                    name = last;
                } else {
                    name = "download_" + System.currentTimeMillis();
                }
            }
            if (name != null) {
                name = name.replaceAll("[\\\\/:*?\"<>|]", "_");
            }
            android.app.DownloadManager.Request req = new android.app.DownloadManager.Request(android.net.Uri.parse(u));
            req.setMimeType(mime);
       
             String cook = android.webkit.CookieManager.getInstance().getCookie(u);
            req.addRequestHeader("Cookie", cook);
            req.addRequestHeader("User-Agent", ua);
            req.setDescription(name);
            req.setTitle(name);
            req.allowScanningByMediaScanner();
            req.setNotificationVisibility(android.app.DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            req.setDestinationInExternalPublicDir(android.os.Environment.DIRECTORY_DOWNLOADS, name);

     
             android.app.DownloadManager mgr = (android.app.DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
            if (mgr != null) {
                mgr.enqueue(req);
            }
            android.widget.Toast.makeText(MainActivity.this, getString(R.string.download_started), android.widget.Toast.LENGTH_SHORT).show();
        } catch (Throwable err) {
            err.printStackTrace();
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void makeNewTab(boolean isIncog, String url) {
        FrameLayout myFrame = new FrameLayout(this);
        myFrame.setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));
        
        WebView w = new WebView(this);
        w.setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));
        
        WebSettings set = w.getSettings();
        set.setDomStorageEnabled(true);
        set.setSupportZoom(true);
        set.setBuiltInZoomControls(true);
        set.setDisplayZoomControls(false);
        set.setDatabaseEnabled(true);
        set.setJavaScriptCanOpenWindowsAutomatically(true);
        
        if (android.os.Build.VERSION.SDK_INT >= 21) {
            android.webkit.CookieManager.getInstance().setAcceptThirdPartyCookies(w, true);
            set.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        }

        final View myPage = getLayoutInflater().inflate(R.layout.layout_new_tab, myFrame, false);
        myFrame.addView(w);
        myFrame.addView(myPage);
        TextView titleT = new TextView(this);
        titleT.setText(R.string.new_tab);
        final BrowserTab newT = new BrowserTab(myFrame, w, myPage, titleT, isIncog);
        allTabs.add(newT);
        if (isIncog == true) {
            set.setCacheMode(WebSettings.LOAD_NO_CACHE);
            w.clearHistory();
            w.clearCache(true);
        } else {
            set.setCacheMode(WebSettings.LOAD_DEFAULT);
        }

        w.setWebViewClient(new MyWebClient(newT));
        w.setWebChromeClient(new MyChromeClient(newT));
        w.addJavascriptInterface(new HtmlShow(), "HtmlViewer");
        
        w.setFindListener(new WebView.FindListener() {
            @Override
            public void onFindResultReceived(int activeMatchOrdinal, int numberOfMatches, boolean isDoneCounting) {
                if (isDoneCounting && findBarLayout.getVisibility() == View.VISIBLE) {
                    if (numberOfMatches == 0) {
                        findCounter.setText("0/0");
                    } else {
                        findCounter.setText((activeMatchOrdinal + 1) + "/" + numberOfMatches);
                    }
                }
            }
        });

        w.setDownloadListener(new android.webkit.DownloadListener() {
            @Override
            public void onDownloadStart(String u, String ua, String disp, String mime, long size) {
                startDl(u, ua, disp, mime);
            }
        });
        w.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                WebView.HitTestResult h = ((WebView) v).getHitTestResult();
                if (h.getType() == WebView.HitTestResult.SRC_ANCHOR_TYPE || h.getType() == WebView.HitTestResult.SRC_IMAGE_ANCHOR_TYPE) {
                    final String lUrl = h.getExtra();
  
                    if (lUrl != null) {
                        final android.app.Dialog d = new android.app.Dialog(MainActivity.this);
                        d.requestWindowFeature(android.view.Window.FEATURE_NO_TITLE);
                        d.setContentView(R.layout.dialog_link_menu);

 
                        if (d.getWindow() != null) {
                            d.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));
                            d.getWindow().setLayout(android.view.ViewGroup.LayoutParams.WRAP_CONTENT, android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
                        }

                        TextView txtTitle = (TextView) d.findViewById(R.id.menuTitle);
                        txtTitle.setText(lUrl);

                        d.findViewById(R.id.menuCopyText).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                android.content.ClipboardManager cb = (android.content.ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
 
                                android.content.ClipData c = android.content.ClipData.newPlainText("URL", lUrl);
                                cb.setPrimaryClip(c);
                                android.widget.Toast.makeText(MainActivity.this, 
getString(R.string.text_copied), android.widget.Toast.LENGTH_SHORT).show();
                                d.dismiss();
                            }
                        });
                        d.findViewById(R.id.menuCopyUrl).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                android.content.ClipboardManager cb = (android.content.ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
  
                                android.content.ClipData c = android.content.ClipData.newPlainText("URL", lUrl);
                                cb.setPrimaryClip(c);
                                android.widget.Toast.makeText(MainActivity.this, getString(R.string.link_copied), 
android.widget.Toast.LENGTH_SHORT).show();
                                d.dismiss();
                            }
                        });
                        d.findViewById(R.id.menuOpenNewTab).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                makeNewTab(false, lUrl);
     
                                d.dismiss();
                            }
                        });
                        d.findViewById(R.id.menuOpenIncognito).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                makeNewTab(true, lUrl);
     
                                d.dismiss();
                            }
                        });
                        d.show();
                        return true;
                    }
                } else if (h.getType() == WebView.HitTestResult.IMAGE_TYPE) {
                    final String imgUrl = h.getExtra();
  
                    if (imgUrl != null) {
                        final android.app.Dialog d2 = new android.app.Dialog(MainActivity.this);
                        d2.requestWindowFeature(android.view.Window.FEATURE_NO_TITLE);
                        d2.setContentView(R.layout.dialog_image_menu);

 
                        if (d2.getWindow() != null) {
                            d2.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));
                            d2.getWindow().setLayout(android.view.ViewGroup.LayoutParams.WRAP_CONTENT, android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
                        }

                        TextView txtTitle2 = (TextView) d2.findViewById(R.id.menuTitle);
                        txtTitle2.setText(imgUrl);

                        d2.findViewById(R.id.menuCopyPicture).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                String picUa1 = null;
                                if (activeTab != null && activeTab.myWeb != null) {
                                    picUa1 = activeTab.myWeb.getSettings().getUserAgentString();
                                }
                                final String picUa2 = picUa1;
                                new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        try {
                                            java.net.URL u2 = new java.net.URL(imgUrl);
                                            java.net.HttpURLConnection conn = (java.net.HttpURLConnection) u2.openConnection();
                                            if (picUa2 != null) {
                                                conn.setRequestProperty("User-Agent", picUa2);
                                            }
                                            conn.connect();
                                            java.io.InputStream str1 = conn.getInputStream();
                                            java.io.ByteArrayOutputStream bo = new java.io.ByteArrayOutputStream();
                                            byte[] buf = new byte[4096];
                                            int rn = 0;
                                            while (true) {
                                                rn = str1.read(buf);
                                                if (rn == -1) {
                                                    break;
                                                }
                                                bo.write(buf, 0, rn);
                                            }
                                            str1.close();
                                            byte[] allBytes = bo.toByteArray();
                                            final android.graphics.Bitmap bmp = android.graphics.BitmapFactory.decodeByteArray(allBytes, 0, allBytes.length);
                                            if (bmp != null) {
                                                java.io.File picFile = new java.io.File(getCacheDir(), "pic_copy.png");
                                                java.io.FileOutputStream fos = new java.io.FileOutputStream(picFile);
                                                bmp.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, fos);
                                                fos.close();
                                                final android.net.Uri uri2 = android.net.Uri.parse("content://ru.unknows.vasiliev.picprovider/pic_copy.png");
                                                runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        android.content.ClipboardManager cb = (android.content.ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                                                        android.content.ClipData c = android.content.ClipData.newUri(getContentResolver(), "Image", uri2);
                                                        cb.setPrimaryClip(c);
                                                        android.widget.Toast.makeText(MainActivity.this, 
getString(R.string.picture_copied), android.widget.Toast.LENGTH_SHORT).show();
                                                    }
                                                });
                                            }
                                        } catch (Throwable err) {
                                            err.printStackTrace();
                                        }
                                    }
                                }).start();
                                d2.dismiss();
                            }
                        });
                        d2.findViewById(R.id.menuDownloadPicture).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                String ext = android.webkit.MimeTypeMap.getFileExtensionFromUrl(imgUrl);
                                String mime = null;
                                if (ext != null && ext.isEmpty() == false) {
                                    mime = android.webkit.MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext.toLowerCase());
                                }
                                if (mime == null) {
                                    mime = "image/*";
                                }
                                String ua2 = null;
                                if (activeTab != null && activeTab.myWeb != null) {
                                    ua2 = activeTab.myWeb.getSettings().getUserAgentString();
                                }
                                startDl(imgUrl, ua2, null, mime);
     
                                d2.dismiss();
                            }
                        });
                        d2.show();
                        return true;
                    }
                }
                return false;
            }
        });

        webBox.addView(myFrame);

        final EditText txtInp = (EditText) myPage.findViewById(R.id.ntpInput);
        ImageButton goB = (ImageButton) myPage.findViewById(R.id.ntpGo);
        CheckBox chkIncog = (CheckBox) myPage.findViewById(R.id.ntpIncognito);
        final LinearLayout boxInp = (LinearLayout) myPage.findViewById(R.id.ntpInputContainer);
        final TextView txtT = (TextView) myPage.findViewById(R.id.ntpTitle);
        final ImageView imgI = (ImageView) myPage.findViewById(R.id.ntpIcon);
        if (getString(R.string.app_name).equals("Vasiliev") == true) {
            imgI.setVisibility(View.VISIBLE);
            txtT.setText(R.string.app_name_suffix);
        } else {
            imgI.setVisibility(View.GONE);
            txtT.setText(R.string.app_name);
        }

        final Runnable colorUpdater = new Runnable() {
            @Override
            public void run() {
                boolean i = newT.incognito;
                int bg;
                int acc;
                if (i) {
                    bg = getResources().getColor(R.color.incognito_bg);
                    acc = getResources().getColor(R.color.incognito_accent);
                } else {
                    bg = getResources().getColor(R.color.background_dark);
                    acc = getResources().getColor(R.color.accent_primary);
                }
                bg = bgColor(bg);
                myPage.setBackgroundColor(bg);
                if (txtT != null) txtT.setTextColor(acc);
                if (imgI != null) imgI.setColorFilter(acc, PorterDuff.Mode.SRC_IN);
                if (boxInp != null) {
                    if (i) {
                        boxInp.setBackgroundResource(R.drawable.bg_input_incognito);
                    } else {
                        boxInp.setBackgroundResource(R.drawable.bg_input);
                    }
                }
            }
        };
        chkIncog.setChecked(isIncog);
        colorUpdater.run();
        
        chkIncog.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton b, boolean checked) {
                newT.incognito = checked;
                colorUpdater.run();

                if (checked == true) {
            
                    newT.myWeb.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
                    newT.myWeb.clearHistory();
                    newT.myWeb.clearCache(true);
                } else {
                    newT.myWeb.getSettings().setCacheMode(WebSettings.LOAD_DEFAULT);
              
                }

                if (activeTab == newT) {
                    changeTab(newT);
                    CookieManager.getInstance().setAcceptCookie(!checked);
                }
            }
        });
        goB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String s = txtInp.getText().toString().trim();
                if (s.isEmpty() == false) {
                    runUrl(newT, s);
          
                }
            }
        });
        txtInp.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actId, KeyEvent ev) {
                if (actId == EditorInfo.IME_ACTION_GO || (ev != null && ev.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                    String s = txtInp.getText().toString().trim();
               
                    if (s.isEmpty() == false) {
                        runUrl(newT, s);
                    }
                    return true;
                }
         
                return false;
            }
        });
        setupWebViews();
        changeTab(newT);

        if (url != null) {
            runUrl(newT, url);
        } else {
            if (homeCustom == true) {
                runUrl(newT, homeUrl);
            } else {
                newT.myNtp.setVisibility(View.VISIBLE);
                newT.myWeb.setVisibility(View.GONE);
            }
        }
    }

    private void captureImage() {
        if (activeTab != null && activeTab.myFrame != null) {
            int w = activeTab.myFrame.getWidth();
            int h = activeTab.myFrame.getHeight();
            if (w > 0 && h > 0) {
                try {
                    Bitmap bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
                    Canvas can = new Canvas(bmp);
                    activeTab.myFrame.draw(can);
                    activeTab.img = bmp;
                } catch (Exception e) {}
            }
        }
    }

    private void changeTab(BrowserTab t) {
        closeFindBar();
        captureImage();
        activeTab = t;
        boolean inc = t.incognito;

        int bgCol = 0;
        if (inc) {
            bgCol = getResources().getColor(R.color.incognito_bg);
        } else {
            bgCol = getResources().getColor(R.color.background_dark);
        }
        bgCol = bgColor(bgCol);
        
        layoutMain.setBackgroundColor(bgCol);
        theToolbar.setBackgroundColor(bgCol);
        if (findBarLayout != null) findBarLayout.setBackgroundColor(bgCol);
        webBox.setBackgroundColor(bgCol);
        if (android.os.Build.VERSION.SDK_INT >= 21) {
            getWindow().setStatusBarColor(bgCol);
            getWindow().setNavigationBarColor(bgCol);
        }

        View uBox = findViewById(R.id.urlInputContainer);
        if (uBox != null) {
            if (inc) {
                uBox.setBackgroundResource(R.drawable.bg_input_incognito);
            } else {
                uBox.setBackgroundResource(R.drawable.bg_input);
            }
        }
        
        if (findInputContainer != null) {
            if (inc) {
                findInputContainer.setBackgroundResource(R.drawable.bg_input_incognito);
            } else {
                findInputContainer.setBackgroundResource(R.drawable.bg_input);
            }
        }

        int accCol = 0;
        if (inc) {
            accCol = getResources().getColor(R.color.incognito_accent);
        } else {
            accCol = getResources().getColor(R.color.accent_primary);
        }
        
        if (android.os.Build.VERSION.SDK_INT >= 21) {
            loadBar.setProgressTintList(ColorStateList.valueOf(accCol));
        } else {
            if (loadBar.getProgressDrawable() != null) {
                loadBar.getProgressDrawable().setColorFilter(accCol, PorterDuff.Mode.SRC_IN);
            }
        }

        for (int i = 0; i < allTabs.size(); i++) {
            BrowserTab cur = allTabs.get(i);
            if (cur == t) {
                cur.myFrame.setVisibility(View.VISIBLE);
            } else {
                cur.myFrame.setVisibility(View.GONE);
            }
        }

        if (t.myNtp.getVisibility() == View.VISIBLE && t.myWeb.getVisibility() == View.GONE) {
            theUrlInput.setText("");
        } else {
            theUrlInput.setText(t.myWeb.getUrl());
        }

        CookieManager.getInstance().setAcceptCookie(!t.incognito);
        fixForwardBtn();
        fixBackBtn();
    }

    private void removeTab(BrowserTab t) {
        webBox.removeView(t.myFrame);
        t.myWeb.destroy();
        allTabs.remove(t);
        deletePreviewFile(t);
        if (allTabs.isEmpty() == true) {
            finish();
        } else if (activeTab == t) {
            changeTab(allTabs.get(allTabs.size() - 1));
        }
    }

    private void openGrid() {
        captureImage();
        gridOverlay.setVisibility(View.VISIBLE);

        if (activeTab != null) {
            if (activeTab.incognito) {
                gridOverlay.setBackgroundColor(bgColor(getResources().getColor(R.color.incognito_bg)));
            } else {
                gridOverlay.setBackgroundColor(bgColor(getResources().getColor(R.color.background_dark)));
            }
        }

        BaseAdapter adp = new BaseAdapter() {
            @Override
            public int getCount() {
                return allTabs.size();
            }
            @Override
            public Object getItem(int p) {
                return allTabs.get(p);
            }
            @Override
            public long getItemId(int p) {
                return p;
            }
            @Override
            public View getView(final int pos, View v, ViewGroup prnt) {
                if (v == null) {
                    v = getLayoutInflater().inflate(R.layout.item_tab_grid, prnt, false);
                }
                TextView tTitle = (TextView) v.findViewById(R.id.gridTabTitle);
                ImageView tImg = (ImageView) v.findViewById(R.id.gridTabPreview);
                ImageButton btnC = (ImageButton) v.findViewById(R.id.btnGridCloseTab);

                int gridColumns = 2;
                int spacePx = (int) (12 * getResources().getDisplayMetrics().density);
                int gridW = gridTabs.getWidth();
                if (gridW <= 0) {
                    gridW = prnt.getWidth();
                }
                int colW = (gridW - spacePx * (gridColumns + 1)) / gridColumns;
                if (colW > 0) {
                    ViewGroup.LayoutParams imgLp = tImg.getLayoutParams();
                    imgLp.height = (int) (colW * 0.85);
                    tImg.setLayoutParams(imgLp);
                }

                final BrowserTab bt = allTabs.get(pos);
                String txt = bt.myTitle.getText().toString();
                if (txt.isEmpty()) {
                    tTitle.setText(getString(R.string.new_tab));
                } else {
                    tTitle.setText(txt);
                }

                if (bt.img != null) {
                    tImg.setImageBitmap(bt.img);
                } else {
                    tImg.setImageResource(android.R.drawable.ic_menu_gallery);
                }

                if (bt == activeTab) {
                    if (bt.incognito) v.setBackgroundResource(R.drawable.bg_tab_active_incognito);
                    else v.setBackgroundResource(R.drawable.bg_tab_active);
                } else {
                    if (bt.incognito) v.setBackgroundResource(R.drawable.bg_tab_inactive_incognito);
                    else v.setBackgroundResource(R.drawable.bg_tab_inactive);
                }

                v.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v2) {
                        changeTab(bt);
            
                        gridOverlay.setVisibility(View.GONE);
                    }
                });
                btnC.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v2) {
                        removeTab(bt);
                        if (allTabs.isEmpty() == false) {
  
                            openGrid();
                        } else {
                            gridOverlay.setVisibility(View.GONE);
                    
                        }
                    }
                });
                return v;
            }
        };
        gridTabs.setAdapter(adp);
    }

    private void openMenu(View v) {
        PopupMenu p = new PopupMenu(this, v);
        p.getMenu().add(Menu.NONE, 1, 1, getString(R.string.menu_create_tab));

        SubMenu tools = p.getMenu().addSubMenu(Menu.NONE, Menu.NONE, 2, getString(R.string.menu_tools));
        tools.add(Menu.NONE, 4, 1, getString(R.string.menu_history));
        tools.add(Menu.NONE, 7, 2, getString(R.string.menu_find));
        tools.add(Menu.NONE, 6, 3, getString(R.string.menu_source));
        p.getMenu().add(Menu.NONE, 5, 3, getString(R.string.menu_settings));

        p.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem i) {
                int code = i.getItemId();
                if (code == 1) {
                    makeNewTab(false, null);
      
                    return true;
                } else if (code == 4) {
                    Intent hist = new Intent(MainActivity.this, HistoryActivity.class);
                    startActivityForResult(hist, REQUEST_HISTORY);
                 
                    return true;
                } else if (code == 5) {
                    Intent set = new Intent(MainActivity.this, SettingsActivity.class);
                    startActivity(set);
                    return true;
        
                } else if (code == 6) {
                    if (activeTab != null && activeTab.myWeb != null) {
                        activeTab.myWeb.loadUrl("javascript:window.HtmlViewer.showHTML(document.documentElement.outerHTML);");
                    }
              
                    return true;
                } else if (code == 7) {
                    findBarLayout.setVisibility(View.VISIBLE);
                    findInput.requestFocus();
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    if (imm != null) {
                        imm.showSoftInput(findInput, InputMethodManager.SHOW_IMPLICIT);
                    }
                    return true;
                }
                return false;
            }
        });
        p.show();
    }

    @Override
    public void onBackPressed() {
        if (gridOverlay != null && gridOverlay.getVisibility() == View.VISIBLE) {
            gridOverlay.setVisibility(View.GONE);
        } else if (findBarLayout != null && findBarLayout.getVisibility() == View.VISIBLE) {
            closeFindBar();
        } else if (activeTab != null && activeTab.myNtp.getVisibility() == View.GONE && activeTab.myWeb.canGoBack()) {
            activeTab.myWeb.goBack();
        } else {
            super.onBackPressed();
        }
    }

    private static class BrowserTab {
        FrameLayout myFrame;
        WebView myWeb;
        View myNtp;
        TextView myTitle;
        boolean incognito;
        Bitmap img;
        BrowserTab(FrameLayout f, WebView w, View n, TextView txt, boolean i) {
            this.myFrame = f;
            this.myWeb = w;
            this.myNtp = n;
            this.myTitle = txt;
            this.incognito = i;
            this.img = null;
        }
    }

    private class MyWebClient extends WebViewClient {
        private BrowserTab tInfo;
        MyWebClient(BrowserTab t) { 
            this.tInfo = t;
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView w, WebResourceRequest req) {
            String u = req.getUrl().toString();
            if (u.startsWith("http://") || u.startsWith("https://")) {
                if (isFileDownload(u)) {
                    String ua = w.getSettings().getUserAgentString();
                    startDl(u, ua, null, null);
                    return true;
                }
                if (allowedUrls.remove(u) == false) {
                    SiteRegistryManager.DangerLevel lvl = SiteRegistryManager.getInstance(MainActivity.this).checkUrl(u);
                    if (lvl != null) {
                        checkSafeAndLoad(tInfo, u);
                        return true;
                    }
                }
                return false;
            }
            try {
                if (u.startsWith("intent://")) {
                    Intent i = Intent.parseUri(u, Intent.URI_INTENT_SCHEME);
                    if (i != null) {
                        if (w.getContext().getPackageManager().resolveActivity(i, 0) != null) {
                            w.getContext().startActivity(i);
                            return true;
                        }
                        String fall = i.getStringExtra("browser_fallback_url");
                        if (fall != null) {
                            w.loadUrl(fall);
                            return true;
                        }
                    }
                } else {
                    Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(u));
                    w.getContext().startActivity(i);
                    return true;
                }
            } catch (Exception e) {}
            return true;
        }

        @Override
        public void onPageStarted(WebView w, String u, Bitmap fav) {
            super.onPageStarted(w, u, fav);
            if (activeTab == tInfo && tInfo.myNtp.getVisibility() == View.GONE) {
                theUrlInput.setText(u);
                loadBar.setVisibility(View.VISIBLE);
                loadBar.setProgress(0);
            }
            if (activeTab == tInfo) {
                fixForwardBtn();
                fixBackBtn();
            }
        }

        @Override
        public void onPageFinished(WebView w, String u) {
            super.onPageFinished(w, u);
            
            if (activeTab == tInfo) {
                theUrlInput.setText(u);
                
                loadBar.setVisibility(View.GONE);
            }
            
            boolean incognito = tInfo.incognito;
            if (incognito == false) {
                String pageTitle = w.getTitle();
                
                if (pageTitle != null) {
                    if (pageTitle.isEmpty() == false) {
                        myDb.addRecord(pageTitle, u);
                    } else {
                        myDb.addRecord("Untitled", u);
                    }
                } else {
                    myDb.addRecord("Untitled", u);
                }
            }
        }
    }

    private class MyChromeClient extends WebChromeClient {
        private BrowserTab tInfo;
        MyChromeClient(BrowserTab t) { 
            this.tInfo = t;
        }

        @Override
        public void onProgressChanged(WebView w, int p) {
            if (activeTab == tInfo) {
                loadBar.setProgress(p);
                if (p == 100) {
                    loadBar.setVisibility(View.GONE);
                } else {
                    loadBar.setVisibility(View.VISIBLE);
                }
            }
        }

        @Override
        public void onReceivedTitle(WebView w, String title) {
            if (title != null && title.isEmpty() == false) {
                tInfo.myTitle.setText(title);
            } else {
                tInfo.myTitle.setText(R.string.new_tab);
            }
        }

        @Override
        public boolean onShowFileChooser(WebView w, ValueCallback<Uri[]> call, FileChooserParams params) {
            if (myUploadMsg != null) {
                myUploadMsg.onReceiveValue(null);
            }
            myUploadMsg = call;
            Intent i = params.createIntent();
            try {
                startActivityForResult(i, FILE_CHOOSER_RESULT_CODE);
            } catch (Exception e) {
                myUploadMsg = null;
                return false;
            }
            return true;
        }
    }

    private class HtmlShow {
        @android.webkit.JavascriptInterface
        public void showHTML(String h) {
            SourceActivity.htmlCode = h;
            startActivity(new Intent(MainActivity.this, SourceActivity.class));
        }
    }
}
