package ca.bitwit.postcard.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class MySQLiteHelper extends SQLiteOpenHelper {

    public static final String COLUMN_ID = "_id";

    private static final String DATABASE_NAME = "storage";
    private static final int DATABASE_VERSION = 1;

    // TODO: complete these creates
    private static final String NETWORK_ACCOUNT_TABLE_CREATE = "create table networkAccounts ("
            + COLUMN_ID + " integer primary key autoincrement, "
            + "isEnabled integer DEFAULT 1, "
            + "isHost integer DEFAULT 0, "
            + "lastActivated DATETIME DEFAULT CURRENT_TIMESTAMP, "
            + "lastDeactivated DATETIME DEFAULT CURRENT_TIMESTAMP, "
            + "networkId text not null, "
            + "token text not null, "
            + "tokenSecret text DEFAULT '', "
            + "title text not null);";

    private static final String PERSON_TABLE_CREATE = "create table persons ("
            + COLUMN_ID + " integer primary key autoincrement, "
            + "lastUsed DATETIME, "
            + "avatarUrl text not null, "
            + "username text not null, "
            + "fullName text not null, "
            + "networkId text not null, "
            + "userId text not null);";

    private static final String TAG_TABLE_CREATE = "create table tags ("
            + COLUMN_ID + " integer primary key autoincrement, "
            + "lastUsed DATETIME DEFAULT CURRENT_TIMESTAMP, "
            + "value text not null);";

    public MySQLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        database.execSQL(NETWORK_ACCOUNT_TABLE_CREATE);
        database.execSQL(PERSON_TABLE_CREATE);
        database.execSQL(TAG_TABLE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(MySQLiteHelper.class.getName(),
                "Upgrading database from version " + oldVersion + " to "
                        + newVersion + ", which will destroy all old data");
        db.execSQL("DROP TABLE IF EXISTS comments");
        onCreate(db);
    }

}