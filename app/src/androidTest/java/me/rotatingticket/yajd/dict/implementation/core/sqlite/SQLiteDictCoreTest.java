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
    private SQLiteDictCore coreDict;
    private SQLiteDictDatabase db;

    private void createDb() {
        Context context = InstrumentationRegistry.getTargetContext();
        db = Room.inMemoryDatabaseBuilder(context, SQLiteDictDatabase.class).build();
        coreDict = db.getSQLiteCoreDict();
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
        SQLiteWordEntry entry = SQLiteWordEntry.construct("今日は", "konnichiha", "説明");
        coreDict.insert(entry);
        SQLiteWordEntry actual = coreDict.getWordEntryByWord("今日は");
        assertEquals(entry, actual);

        ArrayList<String> romajis = new ArrayList<>(1);
        romajis.add("konnichiha");
        assertEquals(romajis, actual.getRomajis());

        // DictCore doesn't support spell correction
        assertNull(coreDict.getWordEntryByWord("こんにちわ"));

        // DictCore doesn't support translating kana to romaji
        assertNull(coreDict.getWordEntryByWord("こんにちは"));

        // Test multiple romajis
        SQLiteWordEntry entry2 = SQLiteWordEntry.construct("今日", new String[]{"kyo", "konjitsu"}, "説明");
        coreDict.insert(entry2);
        SQLiteWordEntry actual2 = coreDict.getWordEntryByWord("今日");
        assertEquals(entry2, actual2);

        ArrayList<String> romajis2 = new ArrayList<>(2);
        romajis2.add("kyo");
        romajis2.add("konjitsu");
        assertEquals(romajis2, actual2.getRomajis());
    }

    @Test
    public void getWordEntryByRomaji() {
        ArrayList<SQLiteWordEntry> entries =  new ArrayList<>();
        entries.add(SQLiteWordEntry.construct("日", "hi", "「日」の説明"));
        entries.add(SQLiteWordEntry.construct("ひ", "hi", "「ひ」の説明"));
        entries.add(SQLiteWordEntry.construct("おはよう", "ohayou", "「おはよう」の説明"));
        entries.add(SQLiteWordEntry.construct("こんばんは", "konbanha", "「こんばんは」の説明"));
        coreDict.insertAll(entries);
        assertCollectionEquals(entries.subList(0, 2), coreDict.getWordEntriesByRomaji("hi"));

        // DictCore doesn't support spell correction
        assertEquals(0, coreDict.getWordEntriesByRomaji("konnichiwa").size());

        // or romaji normalization
        assertEquals(0, coreDict.getWordEntriesByRomaji("konniqiha").size());
    }

    @Test
    public void getWordEntriesByWords() {
        ArrayList<SQLiteWordEntry> entries = new ArrayList<>();
        entries.add(SQLiteWordEntry.construct("今日は", "konnichiha", "「今日は」の説明"));
        entries.add(SQLiteWordEntry.construct("おはよう", "ohayou", "「おはよう」の説明"));
        entries.add(SQLiteWordEntry.construct("こんばんは", "konbanha", "「こんばんは」の説明"));

        coreDict.insertAll(entries);

        ArrayList<String> words = new ArrayList<>();
        words.add("今日は");
        words.add("おはよう");
        words.add("見つけない");

        Map<String, SQLiteWordEntry> actual = coreDict.getWordEntriesByWords(words);
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

        coreDict.insertAll(entries);

        ArrayList<String> romajis = new ArrayList<>();
        romajis.add("hi");
        romajis.add("ohayou");
        romajis.add("mitsukenai");

        Map<String, List<SQLiteWordEntry>> actual = coreDict.getWordEntriesByRomajis(romajis);
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
        entries.add(SQLiteWordEntry.construct("今日", new String[] {"kyo", "konjitsu"}, "「今日」の説明"));
        entries.add(SQLiteWordEntry.construct("今日は", "konnichiha", "「今日は」の説明"));
        entries.add(SQLiteWordEntry.construct("おはよう", "ohayou", "「おはよう」の説明"));
        entries.add(SQLiteWordEntry.construct("こんばんは", "konbanha", "「こんばんは」の説明"));

        coreDict.insertAll(entries);

        // multi results
        assertEquals(entries.subList(0, 2), coreDict.getWordEntriesByWordPrefix("今", 2));
        assertEquals(entries.subList(0, 3), coreDict.getWordEntriesByWordPrefix("今", 3));
        assertEquals(entries.subList(0, 3), coreDict.getWordEntriesByWordPrefix("今", 5));
        assertEquals(entries.subList(1, 3), coreDict.getWordEntriesByWordPrefix("今日", 5));

        // only one result
        assertEquals(entries.subList(3, 4), coreDict.getWordEntriesByWordPrefix("お", 5));

        // no result
        assertEquals(new ArrayList<SQLiteWordEntry>(), coreDict.getWordEntriesByWordPrefix("あ", 1));
    }

    @Test
    public void getWordEntriesByRomajiPrefix() {
        ArrayList<SQLiteWordEntry> entries = new ArrayList<>();
        entries.add(SQLiteWordEntry.construct("い", "i", "「い」の説明"));
        entries.add(SQLiteWordEntry.construct("今", "ima", "「今」の説明"));
        entries.add(SQLiteWordEntry.construct("いままで", "imamade", "「今日」の説明"));
        entries.add(SQLiteWordEntry.construct("おはよう", "ohayou", "「おはよう」の説明"));
        entries.add(SQLiteWordEntry.construct("こんばんは", "konbanha", "「こんばんは」の説明"));

        coreDict.insertAll(entries);

        // multi results
        assertEquals(entries.subList(0, 2), coreDict.getWordEntriesByRomajiPrefix("i", 2));
        assertEquals(entries.subList(0, 3), coreDict.getWordEntriesByRomajiPrefix("i", 3));
        assertEquals(entries.subList(0, 3), coreDict.getWordEntriesByRomajiPrefix("i", 5));
        assertEquals(entries.subList(1, 3), coreDict.getWordEntriesByRomajiPrefix("ima", 5));

        // only one result
        assertEquals(entries.subList(3, 4), coreDict.getWordEntriesByRomajiPrefix("o", 5));

        // no result
        assertEquals(new ArrayList<SQLiteWordEntry>(), coreDict.getWordEntriesByRomajiPrefix("a", 1));
    }

    private void assertCollectionEquals(Collection<?> a, Collection<?> b) {
        assertTrue(CollectionUtils.isEqualCollection(a, b));
    }
}