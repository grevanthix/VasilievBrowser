package ru.unknows.vasiliev;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import java.io.File;
import java.io.FileNotFoundException;

public class PicProvider extends ContentProvider {

    @Override
    public boolean onCreate() {
        return true;
    }

    @Override
    public ParcelFileDescriptor openFile(Uri uri, String mode) throws FileNotFoundException {
        File f = new File(getContext().getCacheDir(), "pic_copy.png");
        return ParcelFileDescriptor.open(f, ParcelFileDescriptor.MODE_READ_ONLY);
    }

    @Override
    public String getType(Uri uri) {
        return "image/png";
    }

    @Override
    public Cursor query(Uri uri, String[] proj, String sel, String[] selArgs, String sort) {
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues vals) {
        return null;
    }

    @Override
    public int delete(Uri uri, String sel, String[] selArgs) {
        return 0;
    }

    @Override
    public int update(Uri uri, ContentValues vals, String sel, String[] selArgs) {
        return 0;
    }
}
