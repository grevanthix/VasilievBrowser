package ru.unknows.vasiliev;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

public class HistoryActivity extends Activity {

    public static final String EXTRA_URL = "selected_url";

    private HistoryDbHelper myDb;
    private ListView theList;
    private MyAdapter myAdapter;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleManager.setLocale(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        BgUtils.applyBlackBg(this);
        BgUtils.applyBars(this);

        myDb = new HistoryDbHelper(this);
        theList = findViewById(R.id.historyListView);
        Button clearBtn = findViewById(R.id.btnClearHistory);

        loadItems();
        
        clearBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myDb.clearHistory();
                loadItems();
            }
        });
    }

    private void loadItems() {
        Cursor curs = myDb.getAllRecords();
        if (myAdapter == null) {
            myAdapter = new MyAdapter(this, curs);
            theList.setAdapter(myAdapter);
        } else {
            Cursor oldCurs = myAdapter.getCursor();
            myAdapter.changeCursor(curs);
            if (oldCurs != null) {
                oldCurs.close();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (myAdapter != null) {
            if (myAdapter.getCursor() != null) {
                myAdapter.getCursor().close();
            }
        }
        myDb.close();
    }

    private class MyAdapter extends CursorAdapter {

        MyAdapter(Context c, Cursor curs) {
            super(c, curs, 0);
        }

        @Override
        public View newView(Context c, Cursor curs, ViewGroup p) {
            return getLayoutInflater().inflate(R.layout.item_history, p, false);
        }

        @Override
        public void bindView(View v, Context c, Cursor curs) {
            TextView titleTxt = v.findViewById(R.id.historyTitle);
            TextView urlTxt = v.findViewById(R.id.historyUrl);
            ImageButton goBtn = v.findViewById(R.id.btnGo);
            ImageButton starBtn = v.findViewById(R.id.btnBookmark);

            String t = curs.getString(curs.getColumnIndexOrThrow(HistoryDbHelper.COLUMN_TITLE));
            final String u = curs.getString(curs.getColumnIndexOrThrow(HistoryDbHelper.COLUMN_URL));
            int mark = curs.getInt(curs.getColumnIndexOrThrow(HistoryDbHelper.COLUMN_BOOKMARKED));
            final long id = curs.getLong(curs.getColumnIndexOrThrow(HistoryDbHelper.COLUMN_ID));
            
            if (t == null || t.isEmpty()) {
                titleTxt.setText(getString(R.string.unnamed));
            } else {
                titleTxt.setText(t);
            }
            
            urlTxt.setText(u);
            
            if (mark == 1) {
                starBtn.setImageResource(R.drawable.ic_star_filled);
            } else {
                starBtn.setImageResource(R.drawable.ic_star_outline);
            }
            
            goBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent();
                    i.putExtra(EXTRA_URL, u);
                    setResult(RESULT_OK, i);
                    finish();
                }
            });
            
            starBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int s = myDb.toggleBookmark(id);
                    ImageButton b = (ImageButton) view;
                    if (s == 1) {
                        b.setImageResource(R.drawable.ic_star_filled);
                    } else { /* иконки звёздочек рисовал ИИ */
                        b.setImageResource(R.drawable.ic_star_outline);
                    }
                    loadItems();
                }
            });
        }
    }
}
