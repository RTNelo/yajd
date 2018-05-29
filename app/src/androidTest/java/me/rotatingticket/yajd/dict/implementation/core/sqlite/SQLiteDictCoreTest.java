package me.rotatingticket.yajd.dict.implementation.core.sqlite;

import android.arch.persistence.room.Room;
import android.content.Context;
import android.support.test.InstrumentationRegistry;

import org.apache.commons.collections4.CollectionUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

public class SQLiteDictCoreTest {
    private SQLiteDictCore dictCore;
    private SQLiteDictDatabase db;

    private void createDb() {
        Context context = InstrumentationRegistry.getTargetContext();
        db = Room.inMemoryDatabaseBuilder(context, SQLiteDictDatabase.class).build();
        dictCore = db.getSQLiteDictCore();
    }

    private void closeDb() {
        db.close();
    }

    @Before
    public void setUp() throws Exception {
        createDb();
    }

    @After
    public void tearDown() throws Exception {
        closeDb();
    }

    @Test
    public void getWordEntryByWord() {
        SQLiteWordEntry entry = SQLiteWordEntry.construct("今日は", 1, "konnichiha", "説明");
        dictCore.insert(entry);
        SQLiteWordEntry actual = dictCore.getWordEntryByWord("今日は");
        assertEquals(entry, actual);

        ArrayList<String> romajis = new ArrayList<>(1);
        romajis.add("konnichiha");
        assertEquals(romajis, actual.getRomajis());

        // DictCore doesn't support spell correction
        assertNull(dictCore.getWordEntryByWord("こんにちわ"));

        // DictCore doesn't support translating kana to romaji
        assertNull(dictCore.getWordEntryByWord("こんにちは"));

        // Test multiple romajis
        SQLiteWordEntry entry2 = SQLiteWordEntry.construct(
              "今日",
              2,
              new String[]{"kyo", "konjitsu"},
              "説明",
              new String[]{"今日", "kyo", "konjitsu"}
        );
        dictCore.insert(entry2);
        SQLiteWordEntry actual2 = dictCore.getWordEntryByWord("今日");
        assertEquals(entry2, actual2);

        ArrayList<String> romajis2 = new ArrayList<>(2);
        romajis2.add("kyo");
        romajis2.add("konjitsu");
        assertEquals(romajis2, actual2.getRomajis());

        assertEquals(entry2, dictCore.getWordEntryByWordAndFrequency("今日", 2));
        assertNull(dictCore.getWordEntryByWordAndFrequency("今日", 1));
    }

    @Test
    public void getWordEntryByRomaji() {
        ArrayList<SQLiteWordEntry> entries =  new ArrayList<>();
        entries.add(SQLiteWordEntry.construct("日", "hi", "「日」の説明"));
        entries.add(SQLiteWordEntry.construct("ひ", "hi", "「ひ」の説明"));
        entries.add(SQLiteWordEntry.construct("おはよう", "ohayou", "「おはよう」の説明"));
        entries.add(SQLiteWordEntry.construct("こんばんは", "konbanha", "「こんばんは」の説明"));
        dictCore.insertAll(entries);
        assertCollectionEquals(entries.subList(0, 2), dictCore.getWordEntriesByRomaji("hi"));

        // DictCore doesn't support spell correction
        assertEquals(0, dictCore.getWordEntriesByRomaji("konnichiwa").size());

        // or romaji normalization
        assertEquals(0, dictCore.getWordEntriesByRomaji("konniqiha").size());
    }

    @Test
    public void getWordEntriesByWords() {
        ArrayList<SQLiteWordEntry> entries = new ArrayList<>();
        entries.add(SQLiteWordEntry.construct("今日は", "konnichiha", "「今日は」の説明"));
        entries.add(SQLiteWordEntry.construct("おはよう", "ohayou", "「おはよう」の説明"));
        entries.add(SQLiteWordEntry.construct("こんばんは", "konbanha", "「こんばんは」の説明"));

        dictCore.insertAll(entries);

        ArrayList<String> words = new ArrayList<>();
        words.add("今日は");
        words.add("おはよう");
        words.add("見つけない");

        Map<String, SQLiteWordEntry> actual = dictCore.getWordEntriesByWords(words);
        assertEquals(2, actual.size());
        assertEquals(entries.get(0), actual.get(words.get(0)));
        assertEquals(entries.get(1), actual.get(words.get(1)));
    }
    @Test
    public void getWordEntriesByRomajis() {
        ArrayList<SQLiteWordEntry> entries = new ArrayList<>();
        entries.add(SQLiteWordEntry.construct("日", "hi", "「日」の説明"));
        entries.add(SQLiteWordEntry.construct("ひ", "hi", "「ひ」の説明"));
        entries.add(SQLiteWordEntry.construct("おはよう", "ohayou", "「おはよう」の説明"));
        entries.add(SQLiteWordEntry.construct("こんばんは", "konbanha", "「こんばんは」の説明"));

        dictCore.insertAll(entries);

        ArrayList<String> romajis = new ArrayList<>();
        romajis.add("hi");
        romajis.add("ohayou");
        romajis.add("mitsukenai");

        Map<String, List<SQLiteWordEntry>> actual = dictCore.getWordEntriesByRomajis(romajis);
        assertEquals(3,actual.size());
        assertEquals(2, actual.get(romajis.get(0)).size());
        assertEquals(1, actual.get(romajis.get(1)).size());
        assertEquals(entries.get(0), actual.get(romajis.get(0)).get(0));
        assertEquals(entries.get(1), actual.get(romajis.get(0)).get(1));
        assertEquals(entries.get(2), actual.get(romajis.get(1)).get(0));
        assertEquals(0, actual.get(romajis.get(2)).size());
    }

    @Test
    public void getWordEntriesByWordPrefix() {
        ArrayList<SQLiteWordEntry> entries = new ArrayList<>();
        entries.add(SQLiteWordEntry.construct("今", "ima", "「今」の説明"));
        entries.add(SQLiteWordEntry.construct(
              "今日",
              new String[] {"kyo", "konjitsu"},
              "「今日」の説明",
              new String[] {"今日", "kyo", "konjitsu"}
        ));
        entries.add(SQLiteWordEntry.construct("今日は", "konnichiha", "「今日は」の説明"));
        entries.add(SQLiteWordEntry.construct("おはよう", "ohayou", "「おはよう」の説明"));
        entries.add(SQLiteWordEntry.construct("こんばんは", "konbanha", "「こんばんは」の説明"));

        dictCore.insertAll(entries);

        // multi results
        assertEquals(entries.subList(0, 2), dictCore.getWordEntriesByWordPrefix("今", 2));
        assertEquals(entries.subList(0, 3), dictCore.getWordEntriesByWordPrefix("今", 3));
        assertEquals(entries.subList(0, 3), dictCore.getWordEntriesByWordPrefix("今", 5));
        assertEquals(entries.subList(1, 3), dictCore.getWordEntriesByWordPrefix("今日", 5));

        // only one result
        assertEquals(entries.subList(3, 4), dictCore.getWordEntriesByWordPrefix("お", 5));

        // no result
        assertEquals(new ArrayList<SQLiteWordEntry>(), dictCore.getWordEntriesByWordPrefix("あ", 1));
    }

    @Test
    public void getWordEntriesByRomajiPrefix() {
        ArrayList<SQLiteWordEntry> entries = new ArrayList<>();
        entries.add(SQLiteWordEntry.construct("い", "i", "「い」の説明"));
        entries.add(SQLiteWordEntry.construct("今", "ima", "「今」の説明"));
        entries.add(SQLiteWordEntry.construct("いままで", "imamade", "「今日」の説明"));
        entries.add(SQLiteWordEntry.construct("おはよう", "ohayou", "「おはよう」の説明"));
        entries.add(SQLiteWordEntry.construct("こんばんは", "konbanha", "「こんばんは」の説明"));

        dictCore.insertAll(entries);

        // multi results
        assertEquals(entries.subList(0, 2), dictCore.getWordEntriesByRomajiPrefix("i", 2));
        assertEquals(entries.subList(0, 3), dictCore.getWordEntriesByRomajiPrefix("i", 3));
        assertEquals(entries.subList(0, 3), dictCore.getWordEntriesByRomajiPrefix("i", 5));
        assertEquals(entries.subList(1, 3), dictCore.getWordEntriesByRomajiPrefix("ima", 5));

        // only one result
        assertEquals(entries.subList(3, 4), dictCore.getWordEntriesByRomajiPrefix("o", 5));

        // no result
        assertEquals(new ArrayList<SQLiteWordEntry>(), dictCore.getWordEntriesByRomajiPrefix("a", 1));
    }

    @Test
    public void queryWordEntriesAndPrefix() {
        ArrayList<SQLiteWordEntry> entries = new ArrayList<>();
        entries.add(SQLiteWordEntry.construct("今", "ima", "「今」の説明"));
        entries.add(SQLiteWordEntry.construct(
              "今日",
              new String[] {"kyo", "konjitsu"},
              "「今日」の説明",
              new String[] {"今日", "kyo", "konjitsu"}
        ));
        entries.add(SQLiteWordEntry.construct(
              "打ち上げ花火",
              new String[] {"uchiagehanabi"},
              "「打ち上げ花火」の説明",
              new String[] {"打ち上げ花火", "uchiagehanabi", "打上花火"}
        ));
        dictCore.insertAll(entries);

        assertEquals(entries.subList(1, 2), dictCore.queryWordEntries("今日", 10));
        assertEquals(entries.subList(1, 2), dictCore.queryWordEntries("kyo", 10));
        assertEquals(entries.subList(1, 2), dictCore.queryWordEntries("konjitsu", 10));
        assertEquals(entries.subList(0, 1), dictCore.queryWordEntries("ima", 10));
        assertEquals(entries.subList(2, 3), dictCore.queryWordEntries("打上花火", 10));

        assertEquals(entries.subList(0, 2), dictCore.queryWordEntriesByPrefix("今", 10));
        assertEquals(entries.subList(1, 2), dictCore.queryWordEntriesByPrefix("k", 10));
        assertEquals(entries.subList(2, 3), dictCore.queryWordEntriesByPrefix("打", 10));
    }

    private void assertCollectionEquals(Collection<?> a, Collection<?> b) {
        assertTrue(CollectionUtils.isEqualCollection(a, b));
    }
}