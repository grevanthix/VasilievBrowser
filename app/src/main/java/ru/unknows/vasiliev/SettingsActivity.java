package ru.unknows.vasiliev;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.os.Bundle;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.WebView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

public class SettingsActivity extends Activity {

    private Switch swJs;
    private Switch swImg;
    private Switch swRestore;
    private Spinner spinLang;
    private Spinner spinSearch;
    private Spinner spinUa;
    private RadioGroup homeRadGrp;
    private EditText homeEdit;
    private SharedPreferences myPrefs;
    
    private final String[] searchArr = {"Google", "DuckDuckGo", "SearX"};
    private final String[] uaArr = {"Android", "iPhone", "Desktop"};
    private final String[] langArr = {"system", "ru", "en"};

    public static final String[] searchUrls = {
            "https://www.google.com/search?q=",
            "https://duckduckgo.com/?q=",
            "https://searx.be/search?q="
    };

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleManager.setLocale(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        BgUtils.applyBlackBg(this);
        BgUtils.applyBars(this);

        myPrefs = getSharedPreferences("vasiliev_prefs", MODE_PRIVATE);

        swJs = (Switch) findViewById(R.id.switchJs);
        swImg = (Switch) findViewById(R.id.switchImages);
        swRestore = (Switch) findViewById(R.id.switchRestoreTabs);
        spinLang = (Spinner) findViewById(R.id.spinnerLanguage);
        spinSearch = (Spinner) findViewById(R.id.spinnerSearch);
        spinUa = (Spinner) findViewById(R.id.spinnerUa);
        homeRadGrp = (RadioGroup) findViewById(R.id.radioGroupHomepageType);
        homeEdit = (EditText) findViewById(R.id.editHomepage);
        
        TextView vers = (TextView) findViewById(R.id.tvVersion);
        Button clearBtn = (Button) findViewById(R.id.btnClearData);
        LinearLayout customBtn = (LinearLayout) findViewById(R.id.btnCustomization);

        String[] langNames = {getString(R.string.lang_system), getString(R.string.lang_ru), getString(R.string.lang_en)};
        ArrayAdapter<String> la = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, langNames);
        la.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinLang.setAdapter(la);

        ArrayAdapter<String> sa = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, searchArr);
        sa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinSearch.setAdapter(sa);

        ArrayAdapter<String> uaa = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, uaArr);
        uaa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinUa.setAdapter(uaa);

        String l = myPrefs.getString("pref_language", "system");
        int idx = 0;
        if (l.equals("ru") == true) idx = 1;
        else if (l.equals("en") == true) idx = 2;
        
        spinLang.setSelection(idx);

        swJs.setChecked(myPrefs.getBoolean("pref_js", true));
        swImg.setChecked(myPrefs.getBoolean("pref_images", true));
        swRestore.setChecked(myPrefs.getBoolean("pref_restore_tabs", true));
        spinSearch.setSelection(myPrefs.getInt("pref_search_engine_index", 0));
        spinUa.setSelection(myPrefs.getInt("pref_user_agent", 0));

        boolean cust = myPrefs.getBoolean("pref_homepage_custom", false);
        if (cust == true) {
            homeRadGrp.check(R.id.radioHomepageCustom);
            homeEdit.setVisibility(View.VISIBLE);
        } else {
            homeRadGrp.check(R.id.radioHomepageBuiltin);
            homeEdit.setVisibility(View.GONE);
        }

        homeRadGrp.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup g, int id) {
                if (id == R.id.radioHomepageCustom) {
                    homeEdit.setVisibility(View.VISIBLE);
                } else {
                    homeEdit.setVisibility(View.GONE);
                }
            }
        });
        
        homeEdit.setText(myPrefs.getString("pref_homepage", "https://google.com"));

        try {
            PackageInfo p = getPackageManager().getPackageInfo(getPackageName(), 0);
            vers.setText(String.format(getString(R.string.version), p.versionName));
        } catch (Exception e) {
            vers.setText(String.format(getString(R.string.version), "1.0.0"));
        }

        customBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SettingsActivity.this, CustomizationActivity.class));
            }
        });

        TextView tvTg = (TextView) findViewById(R.id.tvTelegram);
        if (myPrefs.getBoolean("pref_tg_hidden", false) == true) {
            tvTg.setVisibility(View.GONE);
        }
        tvTg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String channel = "your_channel";
                try {
                    Intent tgIntent = new Intent(Intent.ACTION_VIEW,
                        Uri.parse("tg://resolve?domain=" + channel));
                    startActivity(tgIntent);
                } catch (Exception e) {
                    Intent webIntent = new Intent(Intent.ACTION_VIEW,
                        Uri.parse("https://t.me/" + channel));
                    startActivity(webIntent);
                }
            }
        });

        tvTg.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                SharedPreferences.Editor ed = myPrefs.edit();
                ed.putBoolean("pref_tg_hidden", true);
                ed.commit();
                v.setVisibility(View.GONE);
                return true;
            }
        });
        
        clearBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                WebView w = new WebView(SettingsActivity.this);
                w.clearCache(true);
                CookieManager.getInstance().removeAllCookies(null);
                Toast.makeText(SettingsActivity.this, getString(R.string.cache_cleared), Toast.LENGTH_SHORT).show();
                w.destroy();
            }
        });
    }

    private void saveMyStuff() {
        SharedPreferences.Editor ed = myPrefs.edit();
        
        String curL = myPrefs.getString("pref_language", "system");
        String newL = langArr[spinLang.getSelectedItemPosition()];
        boolean changedL = false;
        if (newL.equals(curL) == false) {
            changedL = true;
        }

        ed.putString("pref_language", newL);
        ed.putBoolean("pref_js", swJs.isChecked());
        ed.putBoolean("pref_images", swImg.isChecked());
        ed.putBoolean("pref_restore_tabs", swRestore.isChecked());
        ed.putInt("pref_search_engine_index", spinSearch.getSelectedItemPosition());
        ed.putInt("pref_user_agent", spinUa.getSelectedItemPosition());

        boolean cust = false;
        if (homeRadGrp.getCheckedRadioButtonId() == R.id.radioHomepageCustom) {
            cust = true;
        }
        ed.putBoolean("pref_homepage_custom", cust);
        
        String h = homeEdit.getText().toString().trim();
        if (h.isEmpty() == true) {
            h = "https://google.com";
        }
        ed.putString("pref_homepage", h);

        ed.commit();

        if (changedL == true) {
            Intent i = new Intent(this, MainActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(i);
            Runtime.getRuntime().exit(0);
        } else {
            finish();
        }
    }

    @Override
    public void onBackPressed() {
        saveMyStuff();
    }
}
