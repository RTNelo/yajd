package me.rotatingticket.yajd.util;

import org.atilika.kuromoji.Tokenizer;

public class TokenizerManager {
    private static Tokenizer instance;

    public static final String TOKEN_TYPE_PUNCTUATION = "記号";
    public static final String TOKEN_TYPE_PARTICLE = "助詞";

    public static synchronized Tokenizer getInstance() {
        if (instance == null) {
            instance = Tokenizer.builder().build();
        }
        return instance;
    }
}
