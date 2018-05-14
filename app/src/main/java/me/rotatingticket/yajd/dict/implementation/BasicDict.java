package me.rotatingticket.yajd.dict.implementation;

import com.swabunga.spell.engine.Word;
import com.swabunga.spell.event.SpellChecker;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

import me.rotatingticket.yajd.dict.Dict;
import me.rotatingticket.yajd.dict.core.DictCore;
import me.rotatingticket.yajd.dict.core.WordEntry;

/**
 * A basic Dict implementation to build up the app.
 */
public class BasicDict implements Dict {
    private DictCore dictCore;
    private SpellChecker spellChecker;

    public BasicDict(DictCore dictCore, SpellChecker spellChecker) {
        this.dictCore = dictCore;
        this.spellChecker = spellChecker;
    }

    /**
     * Look up in the dictCore for query suggestions.
     * First, Get word by prefix, or spell checker suggestion.
     * @param input The user input string, may romajis, kanas or kanjis.
     * @param limit The result list size limitation.
     * @return The words corresponding to the user's input.
     */
    @Override
    public List<? extends WordEntry> userQuerySuggestion(String input, int limit) {
        List<? extends WordEntry> result = dictCore.queryWordEntriesByPrefix(input, limit);
        if (result.size() == 0 && mayBeRomaji(input)) {
            List<Word> suggestions = (List<Word>) spellChecker.getSuggestions(input, 1);
            if (suggestions.size() != 0) {
                return dictCore.queryWordEntriesByPrefix(suggestions.get(0).getWord(), limit);
            }
        }
        return result;
    }

    /**
     * Look up in the dictCore.
     * If the input is romaji (see mayBeRomaji), get words by romaji.
     * Otherwise, get words by word. In this case, the result list size is at most 1.
     * @param input The user input string, may romajis, kanas or kanjis.
     * @return The words corresponding to the user's input.
     */
    @Override
    public List<? extends WordEntry> userQuery(String input) {
        if (!mayBeRomaji(input)) {
            ArrayList<WordEntry> results = new ArrayList<>(1);
            WordEntry result = dictCore.getWordEntryByWord(input);
            if (result != null) {
                results.add(result);
            }
            return results;
        } else {
            return dictCore.getWordEntriesByRomaji(input);
        }
    }

    /**
     * Look up the target word directly for user view.
     * @param word The target word.
     * @return The result word, or null if word not found.
     */
    @Override
    public WordEntry userView(String word) {
        return dictCore.getWordEntryByWord(word);
    }

    /**
     * Look up the target word directly for user view and the word frequency should be lower than the ubound.
     * @param word The target word.
     * @param frequencyUpBound The frequency up bound.
     * @return The target word, or null if word not found or word frequency is higher than up bound.
     */
    @Override
    public WordEntry userViewByFrequency(String word, int frequencyUpBound) {
        return dictCore.getWordEntryByWordAndFrequency(word, frequencyUpBound);
    }

    /**
     * Determine whether the input may be romajis.
     *
     * @param input The user input.
     * @return true if the stripped input contains only letters or ASCII dashes ("-"), else false.
     */
    private boolean mayBeRomaji(String input) {
        return StringUtils.containsOnly(
              StringUtils.strip(input).toLowerCase(),
              "abcdefghijklmnopqrstuvwxyz-");
    }
}
