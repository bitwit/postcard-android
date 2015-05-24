package ca.bitwit.postcard.data;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;

public class PersonDataSource {

    // Database fields
    private SQLiteDatabase database;
    private MySQLiteHelper dbHelper;
    private String[] allColumns = {
            MySQLiteHelper.COLUMN_ID,
            "lastUsed",
            "avatarUrl",
            "username",
            "fullName",
            "networkId",
            "userId"
    };

    public PersonDataSource(Context context) {
        dbHelper = new MySQLiteHelper(context);
    }

    public void open() throws SQLException {
        database = dbHelper.getWritableDatabase();
    }

    public void close() {
        dbHelper.close();
    }

    public JSONObject createPerson(JSONObject person) {
        ContentValues values = new ContentValues();

        try {
            values.put("avatarUrl", person.getString("avatarUrl"));
            values.put("username", person.getString("username"));
            values.put("fullName", person.getString("fullName"));
            values.put("networkId", person.getString("networkId"));
            values.put("userId", person.getString("userId"));
        } catch (JSONException ex) {
            ex.printStackTrace();
            return null;
        }

        long insertId = database.insert("persons", null,
                values);

        Cursor cursor = database.query("persons",
                allColumns, MySQLiteHelper.COLUMN_ID + " = " + insertId, null,
                null, null, null);

        cursor.moveToFirst();
        JSONObject newPerson = cursorToPersonJSON(cursor);
        cursor.close();
        return newPerson;
    }

    public void deletePerson(JSONObject person) {
        try {
            long id = person.getLong("id");
            System.out.println("Person deleted with id: " + id);
            database.delete("persons", MySQLiteHelper.COLUMN_ID
                    + " = " + id, null);
        } catch (JSONException ex) {
            ex.printStackTrace();
        }
    }

    public JSONArray getAllpersons() {
        JSONArray persons = new JSONArray();

        Cursor cursor = database.query("persons",
                allColumns, null, null, null, null, null);

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            JSONObject person = cursorToPersonJSON(cursor);
            persons.put(person);
            cursor.moveToNext();
        }
        // make sure to close the cursor
        cursor.close();

        return persons;
    }

    private JSONObject cursorToPersonJSON(Cursor cursor) {
        try {
            JSONObject person = new JSONObject();
            person.put("id", cursor.getLong(0));
            person.put("lastUsed", cursor.getString(1));
            person.put("avatarUrl", cursor.getString(2));
            person.put("username", cursor.getString(3));
            person.put("fullName", cursor.getString(4));
            person.put("networkId", cursor.getString(5));
            person.put("userId", cursor.getString(6));
            return person;
        } catch (JSONException ex) {
            ex.printStackTrace();
            return null;
        }
    }
}