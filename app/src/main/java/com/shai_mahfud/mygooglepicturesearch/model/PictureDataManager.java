/**
 * All rights reserved to Shai Mahfud.
 */

package com.shai_mahfud.mygooglepicturesearch.model;

//import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
//import android.database.Cursor;
//import android.database.sqlite.SQLiteDatabase;
//import android.database.sqlite.SQLiteOpenHelper;
//import android.provider.BaseColumns;

import com.activeandroid.ActiveAndroid;
import com.activeandroid.query.Select;
import com.activeandroid.util.SQLiteUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Responsible for managing the data of the pictures which is retrieved from the API and exposes
 * methods for getting and manipulating the stored data in a thread-safe manner.
 *
 * @author Shai Mahfud
 */
public class PictureDataManager {//extends SQLiteOpenHelper {
    // Nested classes:
    /*
     * Holds the column names of the database table of the pictures data.

    private static class FeedEntry implements BaseColumns {
        private static final String COLUMN_NAME_TITLE = "title";
        private static final String COLUMN_NAME_LINK = "link";
    }*/


    // Constants:
    /* The name of the database */
    //private static final String DATABASE_NAME = "pictures_data.db";
    /** The name of the table in which the pictures data is stored */
    static final String TABLE_NAME = "'pictures_data'";


    // Fields:
    /*
     * The accumulated data of all the pictures fetched and displayed (excluding the pictures
     * themselves, which are very heavy and shouldn't be stored in a data structure.
     */
    private List<PictureData> picturesData = new ArrayList<>();
    /* A lock with which to synchronize operations on the pictures data */
    private final Object lock = new Object();


    // Constructors:
    /**
     * Instantiates this manager
     *
     * @param ctx The context in which this constructor is called
     */
    public PictureDataManager(Context ctx) {
        //super(ctx, DATABASE_NAME, null, 1);
        initDataFromDatabase();
    }


    // Methods:
    /*
    @Override
    public void onCreate(SQLiteDatabase db) {
        createTable(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Discard the data and start over:
        deleteTable(db);
        createTable(db);
    }*/

    /**
     * Adds data of newly retrieved pictures to the data structure.
     *
     * @param newResults The new results from the server in JSON
     *
     * @return The number of pictures retrieved
     */
    public int addNewResults(PicturesDataResponseJson newResults) {
        if (newResults == null) {
            return 0;
        }
        if (newResults.getItems() == null) {
            // The search yielded no results. If there were any previous results, these must be
            // removed:
            synchronized (lock) {
                picturesData.clear();
                SQLiteDatabase db = ActiveAndroid.getDatabase();
                db.delete(PictureDataManager.TABLE_NAME, null, null);
                db.close();
                return 0;
            }
        }

        List<PictureData> newPicturesData = Arrays.asList(newResults.getItems());
        // Note for the reader: using synchronized in the method signature is possible but
        // experience taught me it's better to synchronize inside the body of a method rather than
        // in its signature, because if a deadlock occurs, it's much easier to find the cause that
        // way, for example by printing before each synchronized block, right after capturing the
        // lock and right after releasing the lock. This is much more difficult to do with
        // synchronized methods - you'll have to print before each method call.
        if (!(newPicturesData.isEmpty())) {
            synchronized (lock) {
                picturesData.addAll(newPicturesData);

                // Store the data to the database using ActiveAndroid library:
                ActiveAndroid.beginTransaction();   // Using transactions to speed up actions on
                                                    // the database
                try {
                    for (PictureData newData : newPicturesData) {
                        PictureDataTableModel pdtm = new PictureDataTableModel();
                        pdtm.title = newData.getTitle();
                        pdtm.link = newData.getPictureLink();
                        pdtm.save();
                    }
                    ActiveAndroid.setTransactionSuccessful();
                }
                finally {
                    ActiveAndroid.endTransaction();
                }
            }
        }

        return newPicturesData.size();
    }

    /**
     *
     * @return The number of accumulated pictures
     */
    public int getCount() {
        int count;
        synchronized (lock) {
            count = picturesData.size();
        }
        return count;
    }

    /**
     * Fetches the data of a specific picture whose index is given as argument.
     *
     * @param position The zero-relative position of the picture within the accumulated pictures
     *                 data structure
     *
     * @return The item stored in the position given as argument
     */
    public PictureData getItem(int position) {
        PictureData item;
        synchronized (lock) {
            item = picturesData.get(position);
        }
        return item;
    }

    /**
     * Removes all the data currently stored
     */
    public void clear() {
        synchronized (lock) {
            picturesData.clear();
        }
        clearTable();
    }

    /*
     * Inserts new data to the SQLite database
     *
     * @param pictureData The data to insert

    private void insertToTable(PictureData pictureData) {
        // Get the data repository in write mode:
        SQLiteDatabase db = getWritableDatabase();

        // Create a new map of values, where column names are the keys:
        ContentValues values = new ContentValues();
        values.put(FeedEntry.COLUMN_NAME_TITLE, pictureData.getTitle());
        values.put(FeedEntry.COLUMN_NAME_LINK, pictureData.getPictureLink());

        // Insert the new row:
        db.insert(TABLE_NAME, null, values);
    }*/

    /*
     * Clears the table's contents
     */
    private void clearTable() {
        //SQLiteDatabase db = getWritableDatabase();
        //db.execSQL("DELETE FROM " + TABLE_NAME);
        SQLiteUtils.execSql("DELETE FROM " + TABLE_NAME);
    }

    /*
     * Creates the pictures data table in the database
     *
     * @param db An instance of the database

    private void createTable(SQLiteDatabase db) {
        String TEXT_TYPE = " TEXT";
        String COMMA_SEP = ",";
        String SQL_CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + " (" +
                        FeedEntry._ID + " INTEGER PRIMARY KEY," +
                        FeedEntry.COLUMN_NAME_TITLE + TEXT_TYPE + COMMA_SEP +
                        FeedEntry.COLUMN_NAME_LINK + TEXT_TYPE + " )";
        db.execSQL(SQL_CREATE_TABLE);
    }*/

    /*
     * Deletes the pictures data table
     *
     * @param db An instance of the database

    private void deleteTable(SQLiteDatabase db) {
        String SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS " + TABLE_NAME;
        db.execSQL(SQL_DELETE_ENTRIES);
    }*/

    /*
     * Reads the data from the SQLite database to the local database.
     */
    private void initDataFromDatabase() {
        /*
        SQLiteDatabase db = getReadableDatabase();
        String[] projection = {FeedEntry.COLUMN_NAME_TITLE, FeedEntry.COLUMN_NAME_LINK};
        Cursor cursor = db.query(TABLE_NAME, projection, null, null, null, null, null);
        synchronized (lock) {
            picturesData.clear();
            cursor.moveToFirst();
            while (cursor.moveToNext()) {
                String title = cursor.getString(cursor.getColumnIndexOrThrow(
                        FeedEntry.COLUMN_NAME_TITLE));
                String link = cursor.getString(cursor.getColumnIndexOrThrow(
                        FeedEntry.COLUMN_NAME_LINK));
                PictureData pd = new PictureData();
                pd.setTitle(title);
                pd.setLink(link);
                picturesData.add(pd);
            }
        }
        cursor.close();
        */

        List<PictureDataTableModel> storedData = new Select()
                .from(PictureDataTableModel.class)
                .execute();
        synchronized (lock) {
            picturesData.clear();
            for (PictureDataTableModel pdtm : storedData) {
                PictureData pd = new PictureData();
                pd.setTitle(pdtm.title);
                pd.setLink(pdtm.link);
                picturesData.add(pd);
            }
        }
    }
}
