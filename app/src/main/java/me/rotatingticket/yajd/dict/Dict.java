package me.rotatingticket.yajd.dict;

import java.util.List;

import me.rotatingticket.yajd.dict.core.WordEntry;

/**
 * Interface of dictionary.
 */
public interface Dict {
    /**
     * Look up word in the dictionary for query suggestions.
     * Implementations may apply extra operations on the query, such like romaji normalization,
     * spell correction, etc.
     * @param input The user input string, may romajis, kanas or kanjis.
     * @param limit The result list size limitation.
     * @return The words corresponding to the user's input.
     */
    List<? extends WordEntry> userQuerySuggestion(String input, int limit);

    /**
     * Look up word in the dictionary for word view.
     * Implementations may apply extra operations on the query, such like romaji normalization,
     * spell correction, etc.
     * @param input The user input string, may romajis, kanas or kanjis.
     * @return The words corresponding to the user's input.
     */
    List<? extends  WordEntry> userQuery(String input);

    /**
     * Look up the target word directly for user view.
     * @param word The target word.
     * @return The result word, or null if word not found.
     */
    WordEntry userView(String word);

    /**
     * Look up the target word directly for user view and the word frequency should be lower than the ubound.
     * @param word The target word.
     * @param frequencyUpBound The frequency up bound.
     * @return The target word, or null if word not found or word frequency is higher than up bound.
     */
    WordEntry userViewByFrequency(String word, int frequencyUpBound);
}
