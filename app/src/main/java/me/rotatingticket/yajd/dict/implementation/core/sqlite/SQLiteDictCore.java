package me.rotatingticket.yajd.dict.implementation.core.sqlite;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Transaction;
import android.support.annotation.VisibleForTesting;
import android.util.LongSparseArray;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import me.rotatingticket.yajd.dict.core.DictCore;

/**
 * The implementation of DictCore based on Room.
 */
@Dao
public abstract class SQLiteDictCore implements DictCore {

    @Override
    @Transaction
    @Query("SELECT * FROM WordRecord WHERE word = :word")
    public abstract SQLiteWordEntry getWordEntryByWord(String word);

    @Transaction
    @Query("SELECT * FROM WordRecord WHERE id IN (:ids)")
    protected abstract List<SQLiteWordEntry> getWordEntriesByIds(List<Integer> ids);

    @Transaction
    @Query("SELECT DISTINCT wordRecordId FROM WordRomaji WHERE romaji = :romaji")
    protected abstract List<Integer> getWordEntryIdsByRomaji(String romaji);

    @Override
    @Transaction
    public List<SQLiteWordEntry> getWordEntriesByRomaji(String romaji) {
        List<Integer> wordEntryIds = getWordEntryIdsByRomaji(romaji);
        return getWordEntriesByIds(wordEntryIds);
    }

    @Transaction
    @Query("SELECT * FROM WordRecord WHERE word IN (:words)")
    protected abstract List<SQLiteWordEntry> getByWords(List<String> words);

    @Override
    public HashMap<String, SQLiteWordEntry> getWordEntriesByWords(List<String> words) {
        HashMap<String, SQLiteWordEntry> result = new HashMap<>();
        for (SQLiteWordEntry entry : getByWords(words)) {
            result.put(entry.getWord(), entry);
        }
        return result;
    }

    @Override
    public Map<String, List<SQLiteWordEntry>> getWordEntriesByRomajis(List<String> romajis) {
        HashMap<String, List<SQLiteWordEntry>> result = new HashMap<>();
        for (String romaji : romajis) {
            result.put(romaji, getWordEntriesByRomaji(romaji));
        }
        return result;
    }

    @Transaction
    @Query("SELECT DISTINCT wordRecordId FROM WordRomaji WHERE romaji LIKE :romaji" +
          " ORDER BY romaji LIMIT :limit")
    protected abstract List<Integer> getWordRecordIdByLikeRomaji(String romaji, int limit);

    @Transaction
    @Query("SELECT * FROM wordrecord WHERE word LIKE :word ORDER BY word LIMIT :limit")
    protected abstract List<SQLiteWordEntry> getByLikeWord(String word, int limit);

    private String replaceLikePattern(String raw) {
        return raw
              .replace("_", "\\_")
              .replace("%", "\\%");
    }

    public Iterable<SQLiteWordEntry> getWordEntriesByWordPrefix(String wordPrefix, int limit) {
        return getByLikeWord(replaceLikePattern(wordPrefix) + "%", limit);
    }

    @Transaction
    public Iterable<SQLiteWordEntry> getWordEntriesByRomajiPrefix(String romajiPrefix, int limit) {
        String template = replaceLikePattern(romajiPrefix) + "%";
        List<Integer> wordEntryIds = getWordRecordIdByLikeRomaji(template, limit);
        List<SQLiteWordEntry> wordEntries = getWordEntriesByIds(wordEntryIds);

        LongSparseArray<Integer> reverseMapper = new LongSparseArray<>(wordEntryIds.size());
        for (int i = 0; i != wordEntryIds.size(); ++i) {
            reverseMapper.put(wordEntryIds.get(i), i);
        }

        wordEntries.sort(Comparator.comparingLong(o -> reverseMapper.get(o.getWordRecordId())));
        return wordEntries;
    }

    @VisibleForTesting
    @Insert
    public abstract long insertWordRecord(SQLiteWordRecord records);

    @VisibleForTesting
    @Insert
    public abstract void insertWordRecord(List<SQLiteWordRecord> records);

    @VisibleForTesting
    @Insert
    public abstract long insertAllWordRomajis(SQLiteWordRomaji romajis);

    @VisibleForTesting
    @Insert
    public abstract void insertAllWordRomajis(List<SQLiteWordRomaji> romajis);

    @VisibleForTesting
    public void insertAll(List<SQLiteWordEntry> entries) {
        for (SQLiteWordEntry entry : entries) {
            long wordRecordId = insertWordRecord(entry.wordRecord);
            for (SQLiteWordRomaji romaji : entry.wordRomajis) {
                romaji.setWordRecordId(wordRecordId);
            }
            insertAllWordRomajis(entry.wordRomajis);
        }
    }

    @VisibleForTesting
    public void insert(SQLiteWordEntry entries) {
        insertAll(Arrays.asList(entries));
    }
}
