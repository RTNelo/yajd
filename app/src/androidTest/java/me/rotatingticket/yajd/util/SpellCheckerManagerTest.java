package me.rotatingticket.yajd.util;

import android.content.Context;
import android.support.test.InstrumentationRegistry;

import com.swabunga.spell.engine.Word;
import com.swabunga.spell.event.SpellChecker;

import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;

public class SpellCheckerManagerTest {

    @Test
    public void getInstance() {
        Context context = InstrumentationRegistry.getTargetContext();
        SpellChecker spellChecker = SpellCheckerManager.getInstance(context);
        List<Word> suggestions = spellChecker.getSuggestions("simimasen", 1);
        assertThat(suggestions.size(), greaterThanOrEqualTo(1));
        assertThat(suggestions.get(0).getWord(), equalTo("sumimasenn"));
    }
}