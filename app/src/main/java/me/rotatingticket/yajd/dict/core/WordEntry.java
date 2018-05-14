package me.rotatingticket.yajd.dict.core;

import org.apache.commons.lang3.StringUtils;

import java.util.List;

/**
 * The interface of a word in the dictionary.
 */
public abstract class WordEntry {
    public static final String DEFAULT_ROMAJIS_SEPARATOR = "; ";

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

    /**
     * Get the summary of the word.
     * The summary show as short as possible to be used in translation by frequency.
     * @return the summary string of the word.
     */
    public abstract String getSummary();


    /**
     * Get the romajis representing in one line;
     * @return The romajis in one line;
     */
    public String getRomajisInOneline() {
        return getRomajisInOneline(DEFAULT_ROMAJIS_SEPARATOR);
    };

    public String getRomajisInOneline(String separator) {
        return StringUtils.join(getRomajis(), separator);
    }
}
