package ca.bitwit.postcard.data;

import java.lang.Integer;
import java.util.ArrayList;
import java.util.List;

import android.util.Log;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;


public class NetworkAccountDataSource {

    private SQLiteDatabase database;
    private MySQLiteHelper dbHelper;
    private String[] allColumns = {
            MySQLiteHelper.COLUMN_ID,
            "isEnabled",
            "isHost",
            "lastActivated",
            "lastDeactivated",
            "networkId",
            "token",
            "tokenSecret",
            "title"
    };

    public NetworkAccountDataSource(Context context) {
        dbHelper = new MySQLiteHelper(context);
    }

    public void open() throws SQLException {
        database = dbHelper.getWritableDatabase();
    }

    public void close() {
        dbHelper.close();
    }

    public JSONObject createNetworkAccount(JSONObject networkAccount) {
        ContentValues values = new ContentValues();

        try {
            values.put("networkId", networkAccount.getString("networkId"));
            values.put("title", networkAccount.getString("title"));
            values.put("token", networkAccount.getString("token"));
            if(networkAccount.getString("tokenSecret") != null){
                values.put("tokenSecret", networkAccount.getString("tokenSecret"));
            }
        } catch (JSONException ex) {
            ex.printStackTrace();
            Log.d("CordovaPostcard", "JSON getString error");
            Log.d("CordovaPostcard", ex.getMessage());
            return null;
        }

        if(database == null || !database.isOpen()){
            open();
        }

        long insertId = database.insert("networkAccounts", null,
                values);

        Cursor cursor = database.query("networkAccounts",
                allColumns, MySQLiteHelper.COLUMN_ID + " = " + insertId, null,
                null, null, null);

        cursor.moveToFirst();
        JSONObject newNetworkAccount = cursorToNetworkAccountJSON(cursor);
        cursor.close();
        return newNetworkAccount;
    }

    public Boolean updateNetworkAccount(JSONObject networkAccount){
        Log.d("Postcard", "Updating network account");
        ContentValues values = new ContentValues();

        int networkAccountId;
        try {
            networkAccountId = networkAccount.getInt("id");


            values.put("networkId", networkAccount.getString("networkId"));
            values.put("title", networkAccount.getString("title"));
            values.put("token", networkAccount.getString("token"));
            values.put("isEnabled", networkAccount.getBoolean("isEnabled") ? 1 : 0);
            values.put("isHost", networkAccount.getBoolean("isHost") ? 1 : 0);
            values.put("lastActivated", networkAccount.getString("lastActivated"));
            values.put("lastDeactivated", networkAccount.getString("lastDeactivated"));

            if(networkAccount.getString("tokenSecret") != null){
                values.put("tokenSecret", networkAccount.getString("tokenSecret"));
            }
        } catch (JSONException ex) {
            ex.printStackTrace();
            Log.d("CordovaPostcard", "JSON getString error");
            Log.d("CordovaPostcard", ex.getMessage());
            return false;
        }

        if(database == null || !database.isOpen()){
            open();
        }

        Log.d(this.getClass().getName(), "Entering new values into SQLite");
        database.update("networkAccounts", values, MySQLiteHelper.COLUMN_ID + "=" + networkAccountId, null);

        return true;
    }

    public void deleteNetworkAccount(JSONObject networkAccount) {
        if(database == null || !database.isOpen()){
            open();
        }
        try {
            long id = networkAccount.getLong("id");
            System.out.println("NetworkAccount deleted with id: " + id);
            database.delete("networkAccounts", MySQLiteHelper.COLUMN_ID
                    + " = " + id, null);
        } catch (JSONException ex) {
            ex.printStackTrace();
        }
    }

    public JSONArray getAllNetworkAccounts() {
        if(database == null || !database.isOpen()){
            open();
        }

        JSONArray networkAccounts = new JSONArray();

        Cursor cursor = database.query("networkAccounts",
                allColumns, null, null, null, null, null);

        cursor.moveToFirst();
        Integer index = 0;
        while (!cursor.isAfterLast()) {
            JSONObject networkAccount = cursorToNetworkAccountJSON(cursor);
            try {
                networkAccount.put("order", index);
            } catch (JSONException ex) {
                ex.printStackTrace();
                return null;
            }
            networkAccounts.put(networkAccount);
            cursor.moveToNext();
            index++;
        }
        // make sure to close the cursor
        cursor.close();
        return networkAccounts;
    }

    private JSONObject cursorToNetworkAccountJSON(Cursor cursor) {
        try {
            JSONObject networkAccount = new JSONObject();
            networkAccount.put("id", cursor.getLong(0));
            networkAccount.put("isEnabled", (cursor.getLong(1) != 0));
            networkAccount.put("isHost", (cursor.getLong(2) != 0));
            networkAccount.put("lastActivated", cursor.getString(3));
            networkAccount.put("lastDeactivated", cursor.getString(4));
            networkAccount.put("networkId", cursor.getString(5));
            networkAccount.put("token", cursor.getString(6));
            networkAccount.put("tokenSecret", cursor.getString(7));
            networkAccount.put("title", cursor.getString(8));
            return networkAccount;
        } catch (JSONException ex) {
            ex.printStackTrace();
            return null;
        }
    }
}