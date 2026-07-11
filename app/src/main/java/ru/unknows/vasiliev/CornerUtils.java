package ru.unknows.vasiliev;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;

public class CornerUtils {
    public static void applyRadius(Context myContext, int myRadius) {
        float r = myRadius * myContext.getResources().getDisplayMetrics().density;
        
        int[] allDrawables = new int[7];
        allDrawables[0] = R.drawable.bg_dialog;
        allDrawables[1] = R.drawable.bg_input;
        allDrawables[2] = R.drawable.bg_input_incognito;
        allDrawables[3] = R.drawable.bg_tab_active;
        allDrawables[4] = R.drawable.bg_tab_active_incognito;
        allDrawables[5] = R.drawable.bg_tab_inactive;
        allDrawables[6] = R.drawable.bg_tab_inactive_incognito;

        for (int i = 0; i < allDrawables.length; i++) {
            int currentId = allDrawables[i];
            try {
                Drawable draw = myContext.getResources().getDrawable(currentId);
                if (draw instanceof GradientDrawable) {
                    GradientDrawable grad = (GradientDrawable) draw;
                    grad.setCornerRadius(r);
                }
            } catch (Exception e) {
            }
        }
    }
}
