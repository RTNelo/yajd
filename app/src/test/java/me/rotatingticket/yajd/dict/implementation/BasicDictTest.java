package me.rotatingticket.yajd.dict.implementation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

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
    private BasicDict dict;

    @BeforeEach
    void setupDict() {
        dict = new BasicDict(dictCore);
    }

    @Test
    void testWordUserQuery() {
        String query = "今日";
        int limit = 10;

        // TODO(rtnelo@yeah.net): prettier approach?
        List<? extends WordEntry> stub = dictCore.getWordEntriesByWordPrefix(query, limit);
        reset(dictCore);


        assertEquals(stub, dict.userQuerySuggestion(query, limit));
        verify(dictCore).getWordEntriesByWordPrefix(query, limit);
    }

    @Test
    void testRomajiUserQuery() {
        String query = "kyo";
        int limit = 10;

        // TODO(rtnelo@yeah.net): prettier approach?
        List<? extends WordEntry> stub = dictCore.getWordEntriesByRomajiPrefix(query, limit);
        reset(dictCore);


        assertEquals(stub, dict.userQuerySuggestion(query, limit));
        verify(dictCore).getWordEntriesByRomajiPrefix(query, limit);
    }
}