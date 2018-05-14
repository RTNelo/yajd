package me.rotatingticket.yajd.dict.implementation;

import com.swabunga.spell.event.SpellChecker;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.util.ArrayList;
import java.util.List;

import me.rotatingticket.yajd.MockitoTestCase;
import me.rotatingticket.yajd.dict.core.DictCore;
import me.rotatingticket.yajd.dict.core.WordEntry;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;

class BasicDictTest extends MockitoTestCase {
    @Mock
    private DictCore dictCore;
    @Mock
    private SpellChecker spellChecker;
    private BasicDict dict;

    @BeforeEach
    void setupDict() {
        dict = new BasicDict(dictCore, spellChecker);
    }

    @Test
    void testWordUserQuery() {
        String query = "今日";

        assertEquals(new ArrayList<>(0), dict.userQuery(query));
        verify(dictCore).getWordEntryByWord(query);
    }

    @Test
    void testRomajiUserQuery() {
        String query = "kyo";

        // TODO(rtnelo@yeah.net): prettier approach?
        List<? extends WordEntry> stub = dictCore.getWordEntriesByRomaji(query);
        reset(dictCore);


        assertEquals(stub, dict.userQuery(query));
        verify(dictCore).getWordEntriesByRomaji(query);
    }

    @Test
    void userQuerySuggestion() {
        String query = "kyo";
        int limit = 10;

        List<? extends WordEntry> stub = dictCore.queryWordEntriesByPrefix(query, limit);
        reset(dictCore);

        assertEquals(stub, dict.userQuerySuggestion(query, limit));
        verify(dictCore).queryWordEntriesByPrefix(query, limit);
    }
}