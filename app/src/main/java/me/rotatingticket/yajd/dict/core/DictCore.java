package me.rotatingticket.yajd.dict.core;

import android.support.annotation.Nullable;

import java.util.List;
import java.util.Map;

/**
 * Interface of Dictionary Core providing basic word query API.
 */
public interface DictCore {
    /**
     * Get the word entry of which the word matches param word precisely.
     * @param word target word.
     * @return the word entry of target word or null if there is no word reach the condition.
     */
    @Nullable WordEntry getWordEntryByWord(String word);

    /**
     * Get a list of word entry which have a romaji matching the param precisely.
     * @param romaji target romaji.
     * @return list of the target word entry, ordered by matching romaji.
     */
    List<? extends WordEntry> getWordEntriesByRomaji(String romaji);

    /**
     * Multi-get version of getWordEntryByWord.
     * Implementation should provide an algorithm which
     * is not slower than Multi-call of getWordEntryByWord
     * @param words a list of target words.
     * @return Mapping from word to corresponding word entry.
     */
    Map<String, ? extends WordEntry> getWordEntriesByWords(List<String> words);

    /**
     * Multi-get version of getWordEntriesByRomaji.
     * Implementation should provide an algorithm which
     * is not slower than Multi-call of getWordEntriesByRomaji.
     * @param Romajis a list of target romajis.
     * @return Mapping from romaji to corresponding word entry list.
     */
    Map<String, ? extends List<? extends WordEntry>> getWordEntriesByRomajis(List<String> Romajis);

    /**
     * Get a list of word entries of which the word have the prefix equals to wordPrefix.
     * @param wordPrefix the target prefix of word.
     * @param limit should return at most limit result
     * @return a list of result word entries, ordered by matching word. .size() is lte to limit.
     */
    List<? extends WordEntry> getWordEntriesByWordPrefix(String wordPrefix, int limit);

    /**
     * Get a list of word entries of which the romaji have the prefix equals to wordPrefix.
     * @param romajiPrefix the target prefix of romaji.
     * @param limit should return at most limit result
     * @return a list of result word entries, ordered by matching romaji. .size() is lte to limit.
     */
    List<? extends WordEntry> getWordEntriesByRomajiPrefix(String romajiPrefix, int limit);

    /**
     * Get a list of word entries by query feature prefix.
     * @param prefix The prefix to query.
     * @param limit The result size limit.
     * @return A list of WordEntry.
     */
    List<? extends WordEntry> queryWordEntriesByPrefix(String prefix, int limit);

    /**
     * Get a list of word entries by query feature.
     * @param query The target query.
     * @param limit The result size limit.
     * @return A list of WordEntry.
     */
    List<? extends WordEntry> queryWordEntries(String query, int limit);
}
