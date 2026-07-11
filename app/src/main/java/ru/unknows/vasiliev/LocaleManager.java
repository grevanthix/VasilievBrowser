package ru.unknows.vasiliev;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import java.util.Locale;

public class LocaleManager {
    public static Context setLocale(Context ctx) {
        SharedPreferences s = ctx.getSharedPreferences("vasiliev_prefs", Context.MODE_PRIVATE);
        String l = s.getString("pref_language", "system");
        
        if (l.equals("system") == true) {
            l = Resources.getSystem().getConfiguration().locale.getLanguage();
        }
        return updateResources(ctx, l);
    }

    private static Context updateResources(Context ctx, String l) {
        Locale loc = new Locale(l);
        Locale.setDefault(loc);
        
        Resources res = ctx.getResources();
        Configuration conf = new Configuration(res.getConfiguration());
        
        if (android.os.Build.VERSION.SDK_INT >= 17) {
            conf.setLocale(loc);
            ctx = ctx.createConfigurationContext(conf);
        } else {
            conf.locale = loc;
            res.updateConfiguration(conf, res.getDisplayMetrics());
        }
        return ctx;
    }
}
