package me.rotatingticket.yajd.dict.implementation.core.sqlite;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.PrimaryKey;

import java.util.Objects;

/**
 * The entity of one of romaji records of a word in the SQLite database.
 */
@Entity(tableName = "WordRomaji",
      foreignKeys = @ForeignKey(entity = SQLiteWordRecord.class,
            parentColumns = "id",
            childColumns = "wordRecordId",
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
      ))
public class SQLiteWordRomaji {
    @PrimaryKey(autoGenerate = true)
    private long id;
    private String romaji;
    @ColumnInfo(index = true)
    private long wordRecordId;

    public SQLiteWordRomaji(String romaji) {
        this.romaji = romaji;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getRomaji() {
        return romaji;
    }

    public void setRomaji(String romaji) {
        this.romaji = romaji;
    }

    public long getWordRecordId() {
        return wordRecordId;
    }

    public void setWordRecordId(long wordRecordId) {
        this.wordRecordId = wordRecordId;
    }

    @Override
    public String toString() {
        return "SQLiteWordRomaji{" +
              ", romaji='" + romaji + '\'' +
              '}';
    }

    /**
     * Compare to another SQLiteWordRomaji by romaji.
     * @return
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SQLiteWordRomaji)) return false;
        SQLiteWordRomaji that = (SQLiteWordRomaji) o;
        return Objects.equals(romaji, that.romaji);
    }

    @Override
    public int hashCode() {
        return Objects.hash(romaji);
    }
}
