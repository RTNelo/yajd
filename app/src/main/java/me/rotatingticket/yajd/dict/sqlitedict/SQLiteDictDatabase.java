package me.rotatingticket.yajd.dict.sqlitedict;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;

/**
 * The database of SQLiteDictCore.
 */
@Database(version = 1, entities = {SQLiteWordRecord.class, SQLiteWordRomaji.class})
public abstract class SQLiteDictDatabase extends RoomDatabase {
    abstract public SQLiteDictCore getSQLiteCoreDict();
}
