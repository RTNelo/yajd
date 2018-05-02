package me.rotatingticket.yajd.dict.implementation.core.sqlite;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.VisibleForTesting;

import java.util.Objects;

/**
 * The entity of main record of a word in the SQLite database.
 */
@Entity(tableName = "WordRecord")
public class SQLiteWordRecord {

    @PrimaryKey(autoGenerate = true)
    private long id;

    @ColumnInfo(index = true)
    private String word;
    @ColumnInfo(typeAffinity = ColumnInfo.TEXT)
    private String description;

    public SQLiteWordRecord(String word, String description) {
        this.word = word;
        this.description = description;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Compare to another SQLiteWordRecord by field word and description.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SQLiteWordRecord)) return false;
        SQLiteWordRecord that = (SQLiteWordRecord) o;
        return Objects.equals(word, that.word) &&
              Objects.equals(description, that.description);
    }

    @Override
    public int hashCode() {
        return Objects.hash(word, description);
    }

    @Override
    @VisibleForTesting
    public String toString() {
        return "SQLiteWordRecord{" +
              "id=" + id +
              ", word='" + word + '\'' +
              ", description='" + description + '\'' +
              '}';
    }
}
