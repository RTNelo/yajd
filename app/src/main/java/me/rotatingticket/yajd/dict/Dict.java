package me.rotatingticket.yajd.dict;

import java.util.List;

import me.rotatingticket.yajd.dict.core.WordEntry;

/**
 * Interface of dictionary.
 */
public interface Dict {
    /**
     * Look up word in the dictionary.
     * Implementations may apply extra operations on the query, such like romaji normalization,
     * spell correction, etc.
     * @param input The user input string, may romajis, kanas or kanjis.
     * @param limit The result list size limitation.
     * @return The words corresponding to the user's input.
     */
    List<? extends WordEntry> userQuery(String input, int limit);
}
