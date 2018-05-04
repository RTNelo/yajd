package me.rotatingticket.yajd.dict.implementation;

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

    public BasicDict(DictCore dictCore) {
        this.dictCore = dictCore;
    }

    /**
     * Look up in the dictCore for query suggestions.
     * If the input is romaji (see mayBeRomaji), get words by romaji prefix.
     * Otherwise, get words by word prefix.
     * @param input The user input string, may romajis, kanas or kanjis.
     * @param limit The result list size limitation.
     * @return The words corresponding to the user's input.
     */
    @Override
    public List<? extends WordEntry> userQuerySuggestion(String input, int limit) {
        if (mayBeRomaji(input)) {
            return dictCore.getWordEntriesByRomajiPrefix(input, limit);
        } else {
            return dictCore.getWordEntriesByWordPrefix(input, limit);
        }
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
