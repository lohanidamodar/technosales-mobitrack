package com.technosales.mobitrack;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;

import java.util.Arrays;
import java.util.HashSet;

public class MobitrackContentProvider extends ContentProvider {
    // used for the UriMacher
    private static final int SCHEDULES = 30;
    private static final int SCHEDULE_ID = 40;
    private static final String AUTHORITY = "com.tachnosales.mobitrack.contentprovider";
    private static final String SCHEDULES_PATH = "schedule";
    public static final Uri SCHEDULES_URI = Uri.parse("content://" + AUTHORITY
            + "/" + SCHEDULES_PATH);
    public static final String SCHEDULES_CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
            + "/" + SCHEDULES_PATH;
    private static final UriMatcher sURIMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        sURIMatcher.addURI(AUTHORITY, SCHEDULES_PATH, SCHEDULES);
        sURIMatcher.addURI(AUTHORITY, SCHEDULES_PATH + "/#", SCHEDULE_ID);
    }

    // database
    private DatabaseHelper database;

    public MobitrackContentProvider() {
    }

    @Override
    public boolean onCreate() {
        database = new DatabaseHelper(getContext());
        return false;
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        int uriType = sURIMatcher.match(uri);
        SQLiteDatabase sqlDB = database.getWritableDatabase();
        int rowsDeleted = 0;
        String returnPath;
        long id = 0;
        switch (uriType) {
            case SCHEDULES:
                id = sqlDB.insert(SCHEDULES_PATH, null, values);
                returnPath = SCHEDULES_PATH;
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return Uri.parse(returnPath + "/" + id);
    }


    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        // Uisng SQLiteQueryBuilder instead of query() method
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
        // check if the caller has requested a column which does not exists


        int uriType = sURIMatcher.match(uri);
        switch (uriType) {
            case SCHEDULES:
                queryBuilder.setTables(SCHEDULES_PATH);
                break;
            case SCHEDULE_ID:
                checkCategoriesColumns(projection);
                queryBuilder.setTables(SCHEDULES_PATH);
                queryBuilder.appendWhere("id="
                        + uri.getLastPathSegment());
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        SQLiteDatabase db = database.getWritableDatabase();
        Cursor cursor = queryBuilder.query(db, projection, selection,
                selectionArgs, null, null, sortOrder);
// make sure that potential listeners are getting notified
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        int uriType = sURIMatcher.match(uri);
        SQLiteDatabase sqlDB = database.getWritableDatabase();
        int rowsUpdated = 0;
        String id;
        switch (uriType) {
            case SCHEDULES:
                rowsUpdated = sqlDB.update(SCHEDULES_PATH,
                        values,
                        selection,
                        selectionArgs);
                break;
            case SCHEDULE_ID:
                id = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)) {
                    rowsUpdated = sqlDB.update(SCHEDULES_PATH,
                            values,
                            "_id=" + id,
                            null);
                } else {
                    rowsUpdated = sqlDB.update(SCHEDULES_PATH,
                            values,
                            "_id=" + id
                                    + " and "
                                    + selection,
                            selectionArgs);
                }
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return rowsUpdated;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int uriType = sURIMatcher.match(uri);
        SQLiteDatabase sqlDB = database.getWritableDatabase();
        int rowsDeleted = 0;
        String id;
        switch (uriType) {
            case SCHEDULES:
                rowsDeleted = sqlDB.delete(SCHEDULES_PATH, selection,
                        selectionArgs);
                break;
            case SCHEDULE_ID:
                id = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)) {
                    rowsDeleted = sqlDB.delete(SCHEDULES_PATH,
                            "_id=" + id,
                            null);
                } else {
                    rowsDeleted = sqlDB.delete(SCHEDULES_PATH,
                            "_id=" + id
                                    + " and " + selection,
                            selectionArgs);
                }
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return rowsDeleted;
    }

    private void checkCategoriesColumns(String[] projection) {
        String[] available = {"id", "day", "startHour", "startMinute", "stopHour", "stopMinute"};
        if (projection != null) {
            HashSet<String> requestedColumns = new HashSet<String>(Arrays.asList(projection));
            HashSet<String> availableColumns = new HashSet<String>(Arrays.asList(available));
// check if all columns which are requested are available
            if (!availableColumns.containsAll(requestedColumns)) {
                throw new IllegalArgumentException("Unknown columns in projection");
            }
        }
    }
}
