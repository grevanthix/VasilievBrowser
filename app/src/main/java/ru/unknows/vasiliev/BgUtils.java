package ru.unknows.vasiliev;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;

public class BgUtils {
    public static boolean isBlackBgOn(Activity act) {
        SharedPreferences p = act.getSharedPreferences("vasiliev_prefs", Activity.MODE_PRIVATE);
        return p.getBoolean("pref_black_bg", false);
    }

    public static void applyBlackBg(Activity act) {
        if (isBlackBgOn(act) == true) {
            ViewGroup cont = (ViewGroup) act.findViewById(android.R.id.content);
            if (cont != null && cont.getChildCount() > 0) {
                View troot = cont.getChildAt(0);
                if (troot != null) {
                    troot.setBackgroundColor(Color.BLACK);
                }
            }
        }
    }

    public static void applyBars(Activity act) {
        if (android.os.Build.VERSION.SDK_INT >= 21) {
            int c = act.getResources().getColor(R.color.background_dark);
            if (isBlackBgOn(act) == true) {
                c = Color.BLACK;
            }
            act.getWindow().setStatusBarColor(c);
            act.getWindow().setNavigationBarColor(c);
        }
    }
}
