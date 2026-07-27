package ru.unknows.vasiliev;

import android.content.Context;
import android.net.Uri;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class SiteRegistryManager {

    public enum DangerLevel {
        WARNING,
        BLOCKED
    }

    private static class BadSite {
        String dom;
        DangerLevel lvl;

        BadSite(String d, DangerLevel l) {
            this.dom = d;
            this.lvl = l;
        }
    }

    private static SiteRegistryManager myInst;
    private final List<BadSite> badList = new ArrayList<BadSite>();

    public static SiteRegistryManager getInstance(Context ctx) {
        if (myInst == null) {
            myInst = new SiteRegistryManager(ctx.getApplicationContext());
        }
        return myInst;
    }

    private SiteRegistryManager(Context ctx) {
        try {
            InputStream str = ctx.getResources().openRawResource(R.raw.blocklist);
            BufferedReader rd = new BufferedReader(new InputStreamReader(str, "UTF-8"));
            String textLine;
            
            while ((textLine = rd.readLine()) != null) {
                textLine = textLine.trim();
                if (textLine.isEmpty() == true || textLine.startsWith("#") == true) {
                    continue;
                }
                
                int cPos = textLine.indexOf(':');
                if (cPos < 0) {
                    continue;
                }
                
                String lStr = textLine.substring(0, cPos).trim().toUpperCase();
                String dStr = textLine.substring(cPos + 1).trim().toLowerCase();
                
                if (dStr.startsWith("www.") == true) {
                    dStr = dStr.substring(4);
                }
                if (dStr.isEmpty() == true) {
                    continue;
                }
                
                DangerLevel lvl;
                if ("BLOCKED".equals(lStr) == true) {
                    lvl = DangerLevel.BLOCKED;
                } else if ("WARNING".equals(lStr) == true) {
                    lvl = DangerLevel.WARNING;
                } else {
                    continue;
                }
                
                badList.add(new BadSite(dStr, lvl));
            }
            rd.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public DangerLevel checkUrl(String u) {
        if (u == null) {
            return null;
        }
        
        String h;
        try {
            h = Uri.parse(u).getHost();
        } catch (Exception e) {
            return null;
        }
        
        if (h == null) {
            return null;
        }
        
        h = h.toLowerCase();
        if (h.startsWith("www.") == true) {
            h = h.substring(4);
        }
        
        for (int i = 0; i < badList.size(); i++) {
            BadSite bs = badList.get(i);
            if (h.equals(bs.dom) || h.endsWith("." + bs.dom)) {
                return bs.lvl;
            }
        }
        return null;
    }
}
