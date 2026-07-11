package ru.unknows.vasiliev;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;

public class BgUtils {
    public static void applyBlackBg(Activity act) {
        SharedPreferences p = act.getSharedPreferences("vasiliev_prefs", Activity.MODE_PRIVATE);
        boolean on = p.getBoolean("pref_black_bg", false);
        if (on == true) {
            ViewGroup cont = (ViewGroup) act.findViewById(android.R.id.content);
            if (cont != null && cont.getChildCount() > 0) {
                View troot = cont.getChildAt(0);
                if (troot != null) {
                    troot.setBackgroundColor(Color.BLACK);
                }
            }
        }
    }
}
