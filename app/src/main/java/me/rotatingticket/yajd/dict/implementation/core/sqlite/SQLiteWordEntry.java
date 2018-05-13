package me.rotatingticket.yajd.dict.implementation.core.sqlite;

import android.arch.persistence.room.Embedded;
import android.arch.persistence.room.Relation;
import android.support.annotation.VisibleForTesting;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import me.rotatingticket.yajd.dict.core.WordEntry;

/**
 * The implementation of WordEntry also providing some utilities for testing.
 * A POJO composed by two room entities: SQLiteWordRecord and SQLiteWordRomaji.
 */
public class SQLiteWordEntry extends WordEntry {
    private static final int DEFAULT_FREQUENCY = 5;

    /**
     * The entity of word record in the SQLite database.
     */
    @Embedded
    public SQLiteWordRecord wordRecord;

    /**
     * A list of entities of word romaji records in the SQLite database.
     */
    @Relation(parentColumn = "id", entityColumn = "wordRecordId", entity = SQLiteWordRomaji.class)
    public List<SQLiteWordRomaji> wordRomajis;

    /**
     * A list of entities of word feature records in the SQLite database.
     */
    @Relation(parentColumn = "id", entityColumn = "wordRecordId", entity = SQLiteWordFeature.class)
    public List<SQLiteWordFeature> wordFeatures;

    public SQLiteWordEntry() {
    }

    public SQLiteWordEntry(SQLiteWordRecord wordRecord,
                           List<SQLiteWordRomaji> wordRomajis,
                           List<SQLiteWordFeature> wordFeatures) {
        this.wordRecord = wordRecord;
        this.wordRomajis = wordRomajis;
        this.wordFeatures = wordFeatures;
    }

    public static SQLiteWordEntry construct(String word, int frequency, String romaji, String description) {
        ArrayList<SQLiteWordRomaji> wordRomajis = new ArrayList<>(1);
        wordRomajis.add(new SQLiteWordRomaji(romaji));

        ArrayList<SQLiteWordFeature> wordFeatures = new ArrayList<>(2);
        wordFeatures.add(new SQLiteWordFeature(word));
        wordFeatures.add(new SQLiteWordFeature(romaji));

        String firstline = StringUtils.substringBefore(description, "\n");
        return new SQLiteWordEntry(
              new SQLiteWordRecord(word, frequency, firstline, description),
              wordRomajis,
              wordFeatures);
    }

    public static SQLiteWordEntry construct(String word, String romaji, String description) {
        return construct(word, DEFAULT_FREQUENCY, romaji, description);
    }

    public static SQLiteWordEntry construct(String word,
                                            int frequency,
                                            String[] romajis,
                                            String description,
                                            String[] features) {
        ArrayList<SQLiteWordRomaji> wordRomajis = new ArrayList<>(romajis.length);
        for (String romaji : romajis) {
            wordRomajis.add(new SQLiteWordRomaji(romaji));
        }

        ArrayList<SQLiteWordFeature> wordFeatures = new ArrayList<>(features.length);
        for (String feature : features) {
            wordFeatures.add(new SQLiteWordFeature(feature));
        }

        String firstline = StringUtils.substringBefore(description, "\n");
        return new SQLiteWordEntry(
              new SQLiteWordRecord(word, frequency, firstline, description),
              wordRomajis,
              wordFeatures
        );
    }

    public static SQLiteWordEntry construct(String word,
                                            String[] romajis,
                                            String description,
                                            String[] features) {
        return construct(word, DEFAULT_FREQUENCY, romajis, description, features);
    }

    @Override
    public String getWord() {
        return this.wordRecord.getWord();
    }

    @Override
    public ArrayList<String> getRomajis() {
        ArrayList<String> romajis = new ArrayList<>(wordRomajis.size());
        for (SQLiteWordRomaji romaji : wordRomajis) {
            romajis.add(romaji.getRomaji());
        }
        return romajis;
    }

    @Override
    public String getDescription() {
        return this.wordRecord.getDescription();
    }

    @Override
    public String getSummary() {
        return this.wordRecord.getSummary();
    }

    public long getWordRecordId() {
        return wordRecord.getId();
    }

    /**
     * Compare to another WordEntry by wordRecord and wordRomajis.
     */
    @Override
    @VisibleForTesting
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SQLiteWordEntry)) return false;
        SQLiteWordEntry that = (SQLiteWordEntry) o;
        return Objects.equals(wordRecord, that.wordRecord) &&
              Objects.equals(wordRomajis, that.wordRomajis);
    }

    @Override
    @VisibleForTesting
    public int hashCode() {
        return Objects.hash(wordRecord, wordRomajis);
    }

    @Override
    public String toString() {
        return "SQLiteWordEntry{" +
              "wordRecord=" + wordRecord +
              ", wordRomaji=" + wordRomajis +
              '}';
    }
}
