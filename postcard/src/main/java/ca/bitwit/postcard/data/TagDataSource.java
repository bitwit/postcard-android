package ca.bitwit.postcard.data;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class TagDataSource {

    // Database fields
    private SQLiteDatabase database;
    private MySQLiteHelper dbHelper;
    private String[] allColumns = {
            MySQLiteHelper.COLUMN_ID,
            "lastUsed",
            "value"
    };

    public TagDataSource(Context context) {
        dbHelper = new MySQLiteHelper(context);
    }

    public void open() throws SQLException {
        database = dbHelper.getWritableDatabase();
    }

    public void close() {
        dbHelper.close();
    }

    public JSONObject createTag(JSONObject tag) {
        ContentValues values = new ContentValues();

        try {
            values.put("value", tag.getString("value"));
        } catch (JSONException ex) {
            ex.printStackTrace();
            return null;
        }

        long insertId = database.insert("tags", null,
                values);

        Cursor cursor = database.query("tags",
                allColumns, MySQLiteHelper.COLUMN_ID + " = " + insertId, null,
                null, null, null);

        cursor.moveToFirst();
        JSONObject newTag = cursorToTagJSON(cursor);
        cursor.close();
        return newTag;
    }

    public void deleteTag(JSONObject tag) {
        try {
            long id = tag.getLong("id");
            System.out.println("Tag deleted with id: " + id);
            database.delete("tags", MySQLiteHelper.COLUMN_ID
                    + " = " + id, null);
        } catch (JSONException ex) {
            ex.printStackTrace();
        }
    }

    public JSONArray getAllTags() {
        JSONArray tags = new JSONArray();

        Cursor cursor = database.query("tags",
                allColumns, null, null, null, null, null);

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            JSONObject tag = cursorToTagJSON(cursor);
            tags.put(tag);
            cursor.moveToNext();
        }
        // make sure to close the cursor
        cursor.close();

        return tags;
    }

    private JSONObject cursorToTagJSON(Cursor cursor) {
        try {
            JSONObject tag = new JSONObject();
            tag.put("id", cursor.getLong(0));
            tag.put("lastUsed", cursor.getString(1));
            tag.put("value", cursor.getString(2));
            return tag;
        } catch (JSONException ex) {
            ex.printStackTrace();
            return null;
        }
    }
}