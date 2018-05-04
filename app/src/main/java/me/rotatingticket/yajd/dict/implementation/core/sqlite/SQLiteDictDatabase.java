package me.rotatingticket.yajd.dict.implementation.core.sqlite;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;

/**
 * The database of SQLiteDictCore.
 */
@Database(version = 1, entities = {SQLiteWordRecord.class, SQLiteWordRomaji.class})
public abstract class SQLiteDictDatabase extends RoomDatabase {
    private static SQLiteDictDatabase instance;
    private static final String DB_NAME = "DictCore.db";

    abstract public SQLiteDictCore getSQLiteCoreDict();

    public static synchronized SQLiteDictDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context, SQLiteDictDatabase.class, DB_NAME).build();
        }
        return instance;
    }
}
