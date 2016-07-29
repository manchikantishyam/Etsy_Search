package com.search.etsy.shyam.etsy_search.db;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.provider.BaseColumns;
import android.support.annotation.Nullable;
import android.util.Log;

/**
 * Created by Shyam on 7/17/16.
 */
public class EtsySearchDataContentProvider extends ContentProvider {

    public static final class SearchDataTable implements BaseColumns {
        // Initializing the Table to store search data
        public static final String TABLE_NAME = "EtsySearchDataTable";
        public static final String COLUMN_PAGE = "page";
        public static final String COLUMN_TITLE = "title";
        public static final String COLUMN_IMAGE_URL = "imageURL";
        public static final String COLUMN_PRICE = "price";
        public static final String COLUMN_CURRENCY = "currency";
    }
    public static final class RecentSearchDataTable implements BaseColumns{
        public static final String TABLE_NAME = "EtsyRecentSearchDataTable";
        public static final String COLUMN_SEARCH_KEY = "searchkey";
    }

    public static final String SEARCH_DATA_REQUEST =SearchDataTable.COLUMN_PAGE+"=?";

    private static final String DATABASE_NAME = "EtsySearchData";
    private static final int DATABASE_VERSION = 1;
    public static final int SEARCHDATA = 1;
    public static final int RECENTSEARCH = 2;

    public static final String AUTHORITY = "com.searchetsy.data";
    public static final String SEARCH_DATA_URL = "content://" + AUTHORITY + "/" + SearchDataTable.TABLE_NAME;
    public static final Uri CONTENT_SEARCH_URI = Uri.parse(SEARCH_DATA_URL);
    public static final String RECENT_SEARCH_URL = "content://" + AUTHORITY + "/" + RecentSearchDataTable.TABLE_NAME;
    public static final Uri CONTENT_RECENT_URI = Uri.parse(RECENT_SEARCH_URL);

    private static final String CREATE_DB_SEARCH_DATA =
            " CREATE TABLE " + SearchDataTable.TABLE_NAME +
                    " (" + SearchDataTable._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + SearchDataTable.COLUMN_PAGE + " REAL, "
                    + SearchDataTable.COLUMN_TITLE + " TEXT NOT NULL, "
                    + SearchDataTable.COLUMN_IMAGE_URL + " TEXT NOT NULL, "
                    + SearchDataTable.COLUMN_PRICE + " REAL, "
                    + SearchDataTable.COLUMN_CURRENCY + " TEXT NOT NULL)";

    private static final String CREATE_DB_RECENT_SEARCH =
            " CREATE TABLE " + RecentSearchDataTable.TABLE_NAME +
                    " (" + RecentSearchDataTable._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + RecentSearchDataTable.COLUMN_SEARCH_KEY + " TEXT unique)";


    private static final UriMatcher sUriMatcher;

    static {
        sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        sUriMatcher.addURI(AUTHORITY, SearchDataTable.TABLE_NAME, SEARCHDATA);
        sUriMatcher.addURI(AUTHORITY, RecentSearchDataTable.TABLE_NAME, RECENTSEARCH);
    }

    private static class DatabaseHelper extends SQLiteOpenHelper {
        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(CREATE_DB_SEARCH_DATA);
            db.execSQL(CREATE_DB_RECENT_SEARCH);
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
                cursor = db.query(SearchDataTable.TABLE_NAME, projection, selection, selectionArgs, sortOrder, null, null);
                break;
            }
            case RECENTSEARCH: {
                cursor = db.query(RecentSearchDataTable.TABLE_NAME, projection, selection, selectionArgs, sortOrder, null, null);
                break;
            }
        }
        return cursor;
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        switch (sUriMatcher.match(uri)) {
            //returning the MIME type
            case SEARCHDATA:
                return "vnd.android.cursor.dir/vnd.etsysearch.data";
            case RECENTSEARCH:
                return "vnd.android.cursor.dir/vnd.etsyrecent.data";
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
            case RECENTSEARCH: {
                id = db.insertWithOnConflict(
                        RecentSearchDataTable.TABLE_NAME,
                        null,
                        values, SQLiteDatabase.CONFLICT_REPLACE);
                Log.d("Database table",getTableAsString(db,RecentSearchDataTable.TABLE_NAME));
                break;
            }
        }
        return getUriForId(id, uri);
    }


    private Uri getUriForId(long id, Uri uri) {
        if (id > 0) {
            Uri itemUri = ContentUris.withAppendedId(uri, id);
            getContext().getContentResolver().notifyChange(itemUri, null);
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
        if (id > 0) {
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
    public String getTableAsString(SQLiteDatabase db, String tableName) {
        Log.d("DataBase", "getTableAsString called");
        String tableString = String.format("Table %s:\n", tableName);
        Cursor allRows  = db.rawQuery("SELECT * FROM " + tableName, null);
        if (allRows.moveToFirst() ){
            String[] columnNames = allRows.getColumnNames();
            do {
                for (String name: columnNames) {
                    tableString += String.format("%s: %s\n", name,
                            allRows.getString(allRows.getColumnIndex(name)));
                }
                tableString += "\n";

            } while (allRows.moveToNext());
        }

        return tableString;
    }
}
