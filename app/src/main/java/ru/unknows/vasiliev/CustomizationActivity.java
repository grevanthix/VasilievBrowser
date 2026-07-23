package ru.unknows.vasiliev;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

public class CustomizationActivity extends Activity {

    private LinearLayout myPreviewBox;
    private View theToolbar;
    private View theWeb;
    private ImageButton btnFwd;
    private ImageButton btnBack;

    private RadioGroup radioGrp;
    private Switch swFwd;
    private Switch swBack;
    private Switch swBlackBg;

    private SeekBar radiusBar;
    private TextView radiusText;

    private SharedPreferences mySettings;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleManager.setLocale(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customization);
        BgUtils.applyBlackBg(this);
        BgUtils.applyBars(this);

        mySettings = getSharedPreferences("vasiliev_prefs", MODE_PRIVATE);

        int rad = mySettings.getInt("pref_corner_radius", 12);
        CornerUtils.applyRadius(this, rad);

        myPreviewBox = (LinearLayout) findViewById(R.id.previewContainer);
        theToolbar = (View) findViewById(R.id.previewToolbarLayout);
        theWeb = (View) findViewById(R.id.previewWebView);
        btnFwd = (ImageButton) findViewById(R.id.previewBtnForward);
        btnBack = (ImageButton) findViewById(R.id.previewBtnBack);

        radioGrp = (RadioGroup) findViewById(R.id.radioGroupToolbar);
        swFwd = (Switch) findViewById(R.id.switchForwardBtn);
        swBack = (Switch) findViewById(R.id.switchBackBtn);
        swBlackBg = (Switch) findViewById(R.id.switchBlackBg);
        
        radiusBar = (SeekBar) findViewById(R.id.seekCornerRadius);
        radiusText = (TextView) findViewById(R.id.tvCornerRadiusValue);

        boolean bottom = mySettings.getBoolean("pref_toolbar_bottom", false);
        if (bottom == true) {
            radioGrp.check(R.id.radioBottom);
        } else {
            radioGrp.check(R.id.radioTop);
        }

        swFwd.setChecked(mySettings.getBoolean("pref_show_forward_btn", true));
        swBack.setChecked(mySettings.getBoolean("pref_show_back_btn", true));
        swBlackBg.setChecked(mySettings.getBoolean("pref_black_bg", false));

        refreshLook();

        radiusBar.setMax(25);
        radiusBar.setProgress(rad);
        radiusText.setText(rad + "dp");

        radiusBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar s, int p, boolean user) {
                radiusText.setText(p + "dp");
                CornerUtils.applyRadius(CustomizationActivity.this, p);
                redrawThings();
            }
            @Override
            public void onStartTrackingTouch(SeekBar s) {}
            @Override
            public void onStopTrackingTouch(SeekBar s) {
                saveMyStuff();
            }
        });

        RadioGroup.OnCheckedChangeListener rListen = new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup g, int id) {
                refreshLook();
            }
        };
        radioGrp.setOnCheckedChangeListener(rListen);

        CompoundButton.OnCheckedChangeListener swListen = new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton btn, boolean check) {
                refreshLook();
            }
        };
        
        swFwd.setOnCheckedChangeListener(swListen);
        swBack.setOnCheckedChangeListener(swListen);

        swBlackBg.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton btn, boolean check) {
                Toast.makeText(CustomizationActivity.this, getString(R.string.restart_needed), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void refreshLook() {
        myPreviewBox.removeAllViews();
        
        boolean isBtm = false;
        if (radioGrp.getCheckedRadioButtonId() == R.id.radioBottom) {
            isBtm = true;
        }
        
        boolean f = swFwd.isChecked();
        boolean bk = swBack.isChecked();

        if (f == true) {
            btnFwd.setVisibility(View.VISIBLE);
        } else {
            btnFwd.setVisibility(View.GONE);
        }

        if (bk == true) {
            btnBack.setVisibility(View.VISIBLE);
        } else {
            btnBack.setVisibility(View.GONE);
        }

        if (isBtm == true) {
            myPreviewBox.addView(theWeb);
            myPreviewBox.addView(theToolbar);
        } else {
            myPreviewBox.addView(theToolbar);
            myPreviewBox.addView(theWeb);
        }
    }

    private void redrawThings() {
        View inp = theToolbar.findViewById(R.id.previewInputContainer);
        if (inp != null) {
            inp.setBackgroundResource(R.drawable.bg_input);
        }

        myPreviewBox.invalidate();
    }

    private void saveMyStuff() {
        SharedPreferences.Editor ed = mySettings.edit();
        
        boolean btm = false;
        if (radioGrp.getCheckedRadioButtonId() == R.id.radioBottom) {
            btm = true;
        }
        
        ed.putBoolean("pref_toolbar_bottom", btm);
        ed.putBoolean("pref_show_forward_btn", swFwd.isChecked());
        ed.putBoolean("pref_show_back_btn", swBack.isChecked());
        ed.putBoolean("pref_black_bg", swBlackBg.isChecked());
        ed.putInt("pref_corner_radius", radiusBar.getProgress());
        ed.apply();
    }

    @Override
    public void onBackPressed() {
        saveMyStuff();
        super.onBackPressed();
    }
}
