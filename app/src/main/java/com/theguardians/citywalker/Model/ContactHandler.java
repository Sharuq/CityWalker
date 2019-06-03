package com.theguardians.citywalker.Model;
/**
 * This class is utilised for database storage of contact details
 * @Author Sharuq
 * @Version 2.0
 */
import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class ContactHandler extends SQLiteOpenHelper {

    // All variables about DB
    // Database name
    private static final String DATABASE_NAME = "contactBook";

    // Database version
    private static final int DATABASE_VERSION = 1;

    // Contacts table name
    private static final String TABLE_CONTACT = "contacts";

    // Table Column names
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_NAME = "name";
    private static final String COLUMN_PHONE = "phone";

    private String[] columns= {COLUMN_ID, COLUMN_NAME, COLUMN_PHONE};

    // Create database
    public ContactHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }


    // Create table
    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_TABLE = "CREATE TABLE " + TABLE_CONTACT + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY,"
                + COLUMN_NAME + " TEXT,"
                + COLUMN_PHONE + " TEXT"
                + ")";
        db.execSQL(CREATE_TABLE);
    }

    // Drop table if older version exist
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CONTACT);
        onCreate(db);
    }

    /*
     * Handling Contact table using sql queries.
     * */


    // Add Contact
    public boolean addContactDetails(UserContact contact){
        // Get db writable
        SQLiteDatabase db = this.getWritableDatabase();

        // Get the values to insert
        ContentValues vals = new ContentValues();
        vals.put(COLUMN_NAME, contact.getName());
        vals.put(COLUMN_PHONE, contact.getPhoneNumber());

        // Insert values into table
        long i = db.insert(TABLE_CONTACT, null, vals);
        // Close database
        db.close();

        if(i != 0){
            return true;
        }else{
            return false;
        }
    }


    // Reading all contacts
    public List<UserContact> readAllContacts(){
        // Get db writable
        SQLiteDatabase db = this.getWritableDatabase();

        // Define contacts list
        List<UserContact> contacts = new ArrayList<UserContact>();

        Cursor cursor = db.query(TABLE_CONTACT, columns, null, null, null, null, null);

        cursor.moveToFirst();

        while (!cursor.isAfterLast()) {
            UserContact contact = new UserContact();
            contact.setId(Integer.parseInt(cursor.getString(0)));
            contact.setName(cursor.getString(1));
            contact.setPhoneNumber(cursor.getString(2));
            contacts.add(contact);
            cursor.moveToNext();
        }

        // Make sure to close the cursor
        cursor.close();
        return contacts;
    }

    // Update contact contact
    public boolean editContact(UserContact contact){
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues vals = new ContentValues();
        vals.put(COLUMN_NAME, contact.getName());
        vals.put(COLUMN_PHONE, contact.getPhoneNumber());

        // updating row
        int i = db.update(TABLE_CONTACT, vals, COLUMN_ID + " = ?",  new String[] { String.valueOf(contact.getId()) });

        db.close();

        if(i != 0){
            return true;
        }else{
            return false;
        }

    }

    // Deleting contact
    public boolean deleteContact(int id){
        SQLiteDatabase db = this.getWritableDatabase();

        int i = db.delete(TABLE_CONTACT, COLUMN_ID + " = ?",  new String[] { String.valueOf(id) });

        db.close();

        if(i != 0){
            return true;
        }else{
            return false;
        }
    }

}