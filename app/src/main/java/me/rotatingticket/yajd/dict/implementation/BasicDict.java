package me.rotatingticket.yajd.dict.implementation;

import com.swabunga.spell.engine.Word;
import com.swabunga.spell.event.SpellChecker;

import org.apache.commons.lang3.StringUtils;
import org.atilika.kuromoji.Token;
import org.atilika.kuromoji.Tokenizer;

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
    private Tokenizer tokenizer;

    public BasicDict(DictCore dictCore, SpellChecker spellChecker, Tokenizer tokenizer) {
        this.dictCore = dictCore;
        this.spellChecker = spellChecker;
        this.tokenizer = tokenizer;
    }

    /**
     * Look up in the dictCore for query suggestions.
     * First, Get word by prefix, spell checker suggestion or word base form.
     * @param input The user input string, may romajis, kanas or kanjis.
     * @param limit The result list size limitation.
     * @return The words corresponding to the user's input.
     */
    @Override
    public List<? extends WordEntry> userQuerySuggestion(String input, int limit) {
        List<? extends WordEntry> result = dictCore.queryWordEntriesByPrefix(input, limit);
        // only run extra logic on empty result
        if (result.size() == 0) {
            if (mayBeRomaji(input)) {
                // try use spell checker if it is all romajis.
                List<Word> suggestions = (List<Word>) spellChecker.getSuggestions(input, 1);
                if (suggestions.size() != 0) {
                    return dictCore.queryWordEntriesByPrefix(suggestions.get(0).getWord(), limit);
                }
            } else {
                // try use base form if it is not all romajis.
                List<Token> tokens = tokenizer.tokenize(input);
                if (tokens.size() != 0
                      && tokens.get(0).getBaseForm() != null
                      && !input.equals(tokens.get(0).getBaseForm())) {
                    result = dictCore.queryWordEntriesByPrefix(tokens.get(0).getBaseForm(), limit);
                }
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
