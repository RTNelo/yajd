package me.rotatingticket.yajd.dict.implementation.core.sqlite;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.PrimaryKey;

import java.util.Objects;

/**
 * Word feature record.
 * SQLiteDictCore may use the records to perform universal word query.
 */
@Entity(tableName = "WordFeature",
    foreignKeys = @ForeignKey(entity = SQLiteWordRecord.class,
          parentColumns = "id",
          childColumns = "wordRecordId",
          onDelete = ForeignKey.CASCADE,
          onUpdate = ForeignKey.CASCADE
    ))
public class SQLiteWordFeature {
    @PrimaryKey(autoGenerate = true)
    private long id;
    @ColumnInfo(index = true)
    private String feature;
    @ColumnInfo(index = true)
    private long wordRecordId;

    public SQLiteWordFeature(String feature) {
        this.feature = feature;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getFeature() {
        return feature;
    }

    public void setFeature(String feature) {
        this.feature = feature;
    }

    public long getWordRecordId() {
        return wordRecordId;
    }

    public void setWordRecordId(long wordRecordId) {
        this.wordRecordId = wordRecordId;
    }

    @Override
    public String toString() {
        return "SQLiteWordFeature{" +
              ", feature='" + feature + '\'' +
              '}';
    }

    /**
     * Compare to another SQLiteWordFeature by feature.
     * @return
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SQLiteWordFeature)) return false;
        SQLiteWordFeature that = (SQLiteWordFeature) o;
        return Objects.equals(feature, that.feature);
    }

    @Override
    public int hashCode() {
        return Objects.hash(feature);
    }
}
