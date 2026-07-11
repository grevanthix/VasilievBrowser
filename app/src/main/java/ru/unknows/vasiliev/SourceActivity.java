package ru.unknows.vasiliev;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.widget.TextView;

public class SourceActivity extends Activity {

    public static String htmlCode = "";

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleManager.setLocale(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_source);
        BgUtils.applyBlackBg(this);

        TextView t = (TextView) findViewById(R.id.tvSource);
        t.setText(htmlCode);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        htmlCode = ""; /* чистка чтобы не жрало память */
    }
}
