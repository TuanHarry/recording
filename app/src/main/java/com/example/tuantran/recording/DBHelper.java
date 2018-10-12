package com.example.tuantran.recording;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.support.annotation.NonNull;
import android.util.Log;

import com.example.tuantran.recording.listeners.OnDatabaseChangedListener;

import java.util.Comparator;

/**
 * Created by Tuan Tran on 3/24/2018.
 */

public class DBHelper extends SQLiteOpenHelper{
    private Context mContext;

    private static final String LOG_TAG ="DBHelper";

    private static OnDatabaseChangedListener mOnDatabaseChangedListener;
    private static final String DATABASE_NAME = "save_recording.db";
    private static final int DATABASE_VERSION = 1;

    // using recording item
    public static abstract class DBHelperItem implements BaseColumns{
        public static final String TABLE_NAME = "save_recordings";
        public static final String COLUMN_NAME_RECORDING_NAME = "recording_name";
        public static final String COLUMN_NAME_RECORDING_FILE_PATH = "file_path";
        public static final String COLUMN_NAME_RECORDING_LENGTH = "length";
        public static final String COLUMN_NAME_TIME_ADDED = "time_added";
    }
    private static final String INTEGER_TYPE = " INTEGER ";
    private static final String TEXT_TYPE = " TEXT ";
    private static final String COMMA_SEP = " , ";
    // create table
    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE "+ DBHelperItem.TABLE_NAME + " ("+
                    DBHelperItem._ID + " INTEGER PRIMARY KEY" + COMMA_SEP +
                    DBHelperItem.COLUMN_NAME_RECORDING_NAME + TEXT_TYPE + COMMA_SEP +
                    DBHelperItem.COLUMN_NAME_RECORDING_FILE_PATH + TEXT_TYPE + COMMA_SEP +
                    DBHelperItem.COLUMN_NAME_RECORDING_LENGTH + INTEGER_TYPE + COMMA_SEP +
                    DBHelperItem.COLUMN_NAME_TIME_ADDED + INTEGER_TYPE +
                    "  )";

    // drop table
    private static final String SQL_DELETE_ENTRIES = "DROP TABLE IF EXIT "+ DBHelperItem.TABLE_NAME;



    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.mContext = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRIES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
    // event set on database changed
    public static void setOnDatabaseChangedListener(OnDatabaseChangedListener listener){
        mOnDatabaseChangedListener = listener;
    }

    // get 1 item recording by position
    public RecordingItem getItemAt(int position){
        SQLiteDatabase sqLiteDatabase = this.getReadableDatabase();
        String[] arrayItem = {
                DBHelperItem._ID,
                DBHelperItem.COLUMN_NAME_RECORDING_NAME,
                DBHelperItem.COLUMN_NAME_RECORDING_FILE_PATH,
                DBHelperItem.COLUMN_NAME_RECORDING_LENGTH,
                DBHelperItem.COLUMN_NAME_TIME_ADDED
        };

        // query
        Cursor cursor = sqLiteDatabase.query(DBHelperItem.TABLE_NAME,arrayItem,null,null,null,null,null);

        if (cursor.moveToPosition(position)){
            RecordingItem item = new RecordingItem();
            item.setId(cursor.getInt(cursor.getColumnIndex(DBHelperItem._ID)));
            item.setName(cursor.getString(cursor.getColumnIndex(DBHelperItem.COLUMN_NAME_RECORDING_NAME)));
            item.setFilePath(cursor.getString(cursor.getColumnIndex(DBHelperItem.COLUMN_NAME_RECORDING_FILE_PATH)));
            item.setLength(cursor.getInt(cursor.getColumnIndex(DBHelperItem.COLUMN_NAME_RECORDING_LENGTH)));
            item.setTime(cursor.getLong(cursor.getColumnIndex(DBHelperItem.COLUMN_NAME_TIME_ADDED)));
            cursor.close();
            return item;
        }
        return null;
    }

    // delete item with id
    public void removeItemWithId(int id){
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        String[] whereAgrs = {String.valueOf(id)};
        sqLiteDatabase.delete(DBHelperItem.TABLE_NAME,"_ID =?",whereAgrs);
    }

    // get count list item
    public int getCount(){
        SQLiteDatabase sqLiteDatabase = this.getReadableDatabase();
        String[] arrayItem = {DBHelperItem._ID};
        Cursor cursor = sqLiteDatabase.query(DBHelperItem.TABLE_NAME,arrayItem,null,null,null,null,null);
        int count = cursor.getCount();
        cursor.close();
        return  count;
    }

    // comparator list item recording
    public class RecordingComparator implements Comparator<RecordingItem> {
        @Override
        public int compare(RecordingItem o1, RecordingItem o2) {
            Long obj1 = o1.getTime();
            Long obj2 = o2.getTime();
            return obj2.compareTo(obj1);
        }
    }

    // add item to list
    /*
    * Put values to data
    * -Name
    * -Path
    * -Lenght
    * -Time = system.currentTimeMillis
    * */
    public long addRecording(String recordingName , String recordingPath, long length){
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DBHelperItem.COLUMN_NAME_RECORDING_NAME,recordingName);
        values.put(DBHelperItem.COLUMN_NAME_RECORDING_FILE_PATH,recordingPath);
        values.put(DBHelperItem.COLUMN_NAME_RECORDING_LENGTH,length);
        values.put(DBHelperItem.COLUMN_NAME_TIME_ADDED,System.currentTimeMillis());

        long rowId = sqLiteDatabase.insert(DBHelperItem.TABLE_NAME,null,values);

        // check data changed
        if (mOnDatabaseChangedListener != null){
            mOnDatabaseChangedListener.onNewDatabaseEntryAdded();
        }
        return rowId;
    }

    /*
    * Rename file
    * values is name and file path
    * set rename at id file
    * */
    public void renameItem(RecordingItem item, String recordingName , String recordingPath){
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DBHelperItem.COLUMN_NAME_RECORDING_NAME,recordingName);
        values.put(DBHelperItem.COLUMN_NAME_RECORDING_FILE_PATH,recordingPath);
        sqLiteDatabase.update(DBHelperItem.TABLE_NAME,values,DBHelperItem._ID + "="+item.getId(),null);
        if (mOnDatabaseChangedListener !=null){
            mOnDatabaseChangedListener.onDatabaseEntryRename();
        }
    }

    /*
    * Restore file
    *
    * */
    public long restoreRecording(RecordingItem item){
        SQLiteDatabase database = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DBHelperItem.COLUMN_NAME_RECORDING_NAME, item.getName());
        values.put(DBHelperItem.COLUMN_NAME_RECORDING_FILE_PATH, item.getFilePath());
        values.put(DBHelperItem.COLUMN_NAME_RECORDING_LENGTH, item.getLength());
        values.put(DBHelperItem.COLUMN_NAME_TIME_ADDED,item.getTime());
        values.put(DBHelperItem._ID,item.getId());
        long rowId = database.insert(DBHelperItem.TABLE_NAME,null,values);
        return rowId;
    }
}
