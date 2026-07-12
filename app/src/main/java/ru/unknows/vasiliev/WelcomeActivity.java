package ru.unknows.vasiliev;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Switch;

public class WelcomeActivity extends Activity {

    private Spinner searchSpin;
    private RadioGroup homeGrp;
    private EditText homeTxt;
    private Switch jsSw;
    private Switch imgSw;

    private final String[] engines = {"Google", "DuckDuckGo", "SearX"};

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleManager.setLocale(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        BgUtils.applyBlackBg(this);
        BgUtils.applyBars(this);

        searchSpin = (Spinner) findViewById(R.id.welcomeSpinnerSearch);
        homeGrp = (RadioGroup) findViewById(R.id.welcomeRadioGroupHomepageType);
        homeTxt = (EditText) findViewById(R.id.welcomeEditHomepage);
        jsSw = (Switch) findViewById(R.id.welcomeSwitchJs);
        imgSw = (Switch) findViewById(R.id.welcomeSwitchImages);
        Button sBtn = (Button) findViewById(R.id.welcomeBtnSave);

        ArrayAdapter<String> a = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, engines);
        a.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        searchSpin.setAdapter(a);

        homeGrp.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup grp, int id) {
                if (id == R.id.welcomeRadioHomepageCustom) {
                    homeTxt.setVisibility(View.VISIBLE);
                } else {
                    homeTxt.setVisibility(View.GONE);
                }
            }
        });
        
        sBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences myP = getSharedPreferences("vasiliev_prefs", MODE_PRIVATE);
                SharedPreferences.Editor ed = myP.edit();

                ed.putInt("pref_search_engine_index", searchSpin.getSelectedItemPosition());

                boolean isC = false;
                if (homeGrp.getCheckedRadioButtonId() == R.id.welcomeRadioHomepageCustom) {
                    isC = true;
                }
                ed.putBoolean("pref_homepage_custom", isC);

                String h = homeTxt.getText().toString().trim();
                if (h.isEmpty() == true) {
                    h = "https://google.com";
                }
                ed.putString("pref_homepage", h);

                ed.putBoolean("pref_js", jsSw.isChecked());
                ed.putBoolean("pref_images", imgSw.isChecked());
                ed.putBoolean("is_first_run", false);
                ed.apply();
                
                setResult(RESULT_OK);
                finish();
            }
        });
    }

    @Override
    public void onBackPressed() {
        // не даёт выйти назад
    }
}
