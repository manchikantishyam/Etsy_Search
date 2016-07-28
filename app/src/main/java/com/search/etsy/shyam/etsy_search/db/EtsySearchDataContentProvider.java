package com.search.etsy.shyam.etsy_search.db;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.provider.BaseColumns;
import android.provider.UserDictionary;
import android.support.annotation.Nullable;

import com.search.etsy.shyam.etsy_search.network.EstySearchIntentService;
import com.search.etsy.shyam.etsy_search.network.action.SearchEstyListingsApiAction;

/**
 * Created by Shyam on 7/17/16.
 */
public class EtsySearchDataContentProvider extends ContentProvider{


    public static final class SearchDataTable implements BaseColumns {

        public static final String TABLE_NAME = "EtsySearchDataTable";

        public static final String COLUMN_TITLE = "title";

        public static final String COLUMN_IMAGE_URL = "imageURL";

        public static final String COLUMN_PRICE = "price";

        public static final String COLUMN_CURRENCY = "currency";


    }
    public static final String SEARCH_DATA_REQUEST =SearchDataTable._ID+"=?";

    private static final String DATABASE_NAME = "EtsySearchData";

    private static final int DATABASE_VERSION = 1;


    public static final int SEARCHDATA = 1;
    public static final int SEARCHDATA_ID = 2;

    public static final String AUTHORITY = "com.searchetsy.data";

    public static final String URL = "content://" + AUTHORITY + "/" + SearchDataTable.TABLE_NAME;

    public static final Uri CONTENT_URI = Uri.parse(URL);

    private static final String CREATE_DB_SCRIPT =
            " CREATE TABLE " + SearchDataTable.TABLE_NAME +
                    " (" + SearchDataTable._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + SearchDataTable.COLUMN_TITLE + " TEXT NOT NULL, "
                    + SearchDataTable.COLUMN_IMAGE_URL + " TEXT NOT NULL, "
                    + SearchDataTable.COLUMN_PRICE + " REAL, "
                    + SearchDataTable.COLUMN_CURRENCY + " TEXT)";


    private static final UriMatcher sUriMatcher;

    static {
        sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        sUriMatcher.addURI(AUTHORITY, SearchDataTable.TABLE_NAME, SEARCHDATA);
    }

    private static class DatabaseHelper extends SQLiteOpenHelper {
        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(CREATE_DB_SCRIPT);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS " + SearchDataTable.TABLE_NAME);
            onCreate(db);
        }
    }
    private DatabaseHelper mDbHelper;


    public EtsySearchDataContentProvider() {
    }

    @Override
    public boolean onCreate() {
        Context context = getContext();
        mDbHelper = new DatabaseHelper(context);
        return true;
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Cursor cursor = null;
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        switch (sUriMatcher.match(uri)) {
            case SEARCHDATA: {
                cursor = db.query(SearchDataTable.TABLE_NAME, projection, selection, null, sortOrder, null, null);
                break;
            }
        }
        return cursor;
    }
    @Nullable
    @Override
    public String getType(Uri uri) {
        switch (sUriMatcher.match(uri)) {
            case SEARCHDATA:
                return "vnd.android.cursor.dir/vnd.etsysearch.data";
            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        long id = -1;
        switch (sUriMatcher.match(uri)) {
            case SEARCHDATA: {
                id = db.insertWithOnConflict(
                        SearchDataTable.TABLE_NAME,
                        null,
                        values, SQLiteDatabase.CONFLICT_REPLACE);
                break;
            }
        }
        return getUriForId(id, uri);
    }


    private Uri getUriForId(long id, Uri uri) {
        if (id > 0) {
            Uri itemUri = ContentUris.withAppendedId(uri, id);
            getContext().
                    getContentResolver().
                    notifyChange(itemUri, null);
            return itemUri;
        }
        return null;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        int id = -1;
        switch (sUriMatcher.match(uri)) {
            case SEARCHDATA: {
                id = db.delete(
                        SearchDataTable.TABLE_NAME,
                        null,
                        selectionArgs);
                break;
            }
        }
        if(id>0){
            getContext().
                    getContentResolver().
                    notifyChange(uri, null);
        }
        return id;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        // TODO: Implement this to handle requests to update one or more rows.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
