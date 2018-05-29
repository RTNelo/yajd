package me.rotatingticket.yajd.dict.implementation.core.sqlite;

import android.arch.persistence.db.SupportSQLiteDatabase;
import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.NonNull;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import me.rotatingticket.yajd.R;

/**
 * The database of SQLiteDictCore.
 */
@Database(version = 1,
      entities = {SQLiteWordRecord.class, SQLiteWordRomaji.class, SQLiteWordFeature.class})
public abstract class SQLiteDictDatabase extends RoomDatabase {
    private static SQLiteDictDatabase instance;
    private static final String DB_NAME = "DictCore.db";

    abstract public SQLiteDictCore getSQLiteDictCore();

    public static synchronized SQLiteDictDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context, SQLiteDictDatabase.class, DB_NAME)
                  .addCallback(new Callback() {
                      @Override
                      public void onCreate(@NonNull SupportSQLiteDatabase db) {
                          AsyncTask.execute(() -> {
                              InputStream sqlInputStream = context
                                    .getResources()
                                    .openRawResource(R.raw.dict);
                              BufferedReader br = new BufferedReader(new InputStreamReader(sqlInputStream));
                              String line;

                              // prepopulate sqlite dict data

                              try {
                                  while ((line = br.readLine()) != null) {
                                      db.execSQL(line);
                                  }
                              } catch (IOException e) {
                                  // FIXME(rtnelo@yeah.net) add feedback to user and rollback action.
                              }
                          });
                      }
                  })
                  .build();
        }
        return instance;
    }
}
