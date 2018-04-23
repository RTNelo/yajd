package me.rotatingticket.yajd.dict;

import java.util.List;

/**
 * The interface of a word in the dictionary.
 */
public abstract class WordEntry {
    /**
     * Get the String of the kanji or kana of the word.
     * If the word is こんにちは, this method should return String "こんにちは".
     * @return the String of the kanji or kana.
     */
    public abstract String getWord();

    /**
     * Get a list of the romaji of the word.
     * If the word is こんにちは, this method should return String "konnichiha".
     * @return a list of the romaji string.
     */
    public abstract List<String> getRomajis();

    /**
     * Get the description of the word.
     * The description should in HTML format.
     * @return the description string of the word.
     */
    public abstract String getDescription();
}
