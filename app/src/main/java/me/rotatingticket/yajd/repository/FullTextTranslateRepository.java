package me.rotatingticket.yajd.repository;

import android.app.Application;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.NonNull;

import org.atilika.kuromoji.Token;
import org.atilika.kuromoji.Tokenizer;

import java.util.List;

import me.rotatingticket.yajd.R;
import me.rotatingticket.yajd.dict.Dict;
import me.rotatingticket.yajd.dict.core.WordEntry;
import me.rotatingticket.yajd.dict.implementation.BasicDict;
import me.rotatingticket.yajd.dict.implementation.core.sqlite.SQLiteDictCore;
import me.rotatingticket.yajd.dict.implementation.core.sqlite.SQLiteDictDatabase;
import me.rotatingticket.yajd.util.SpellCheckerManager;
import me.rotatingticket.yajd.util.TokenizerManager;
import me.rotatingticket.yajd.webservice.BingTranslatorWebservice;
import me.rotatingticket.yajd.webservice.WebserviceManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FullTextTranslateRepository {
    private static FullTextTranslateRepository instance;
    private static final String LANG_SRC = "ja";
    private static final String LANG_DEST = "zh-cn";
    private BingTranslatorWebservice webservice;
    private MutableLiveData<String> networkNotification;
    private Application application;
    private Tokenizer tokenizer;
    private Dict dict;

    private FullTextTranslateRepository(
          Application application,
          BingTranslatorWebservice webservice,
          Tokenizer tokenizer,
          Dict dict) {
        this.webservice = webservice;
        this.networkNotification = new MutableLiveData<>();
        this.application = application;
        this.tokenizer = tokenizer;
        this.dict = dict;
    }

    public static synchronized FullTextTranslateRepository getInstance(Application application) {
        if (instance == null) {
            BingTranslatorWebservice webservice = WebserviceManager.getBingTranslatorWebservice();
            instance = new FullTextTranslateRepository(
                  application,
                  webservice,
                  TokenizerManager.getInstance(),
                  prepareDict(application)
            );
        }
        return instance;
    }

    /**
     * Prepare dict instance.
     * @param context The app context.
     * @return The dict instance.
     */
    private static Dict prepareDict(@NonNull Context context) {
        SQLiteDictCore dictCore = SQLiteDictDatabase.getInstance(context).getSQLiteCoreDict();
        return new BasicDict(dictCore,
              SpellCheckerManager.getInstance(context),
              TokenizerManager.getInstance());
    }

    public BingTranslatorWebservice getWebservice() {
        return webservice;
    }

    public LiveData<String> translate(String source) {
        MutableLiveData<String> result = new MutableLiveData<>();
        webservice.shortTranslate(source, LANG_SRC, LANG_DEST).enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                result.setValue(response.body());
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                String notification = application.getString(R.string.full_text_translate_netword_error);
                networkNotification.setValue(notification);
                result.setValue(null);
            }
        });
        return result;
    }

    public MutableLiveData<String> getNetworkNotification() {
        return networkNotification;
    }

    public MutableLiveData<String> getTranslateByOccurency(String text,
                                                           int frequency) {
        MutableLiveData<String> result = new MutableLiveData<>();
        AsyncTask.execute(() -> {
            result.postValue(translateByOccurency(text, frequency, tokenizer, dict));
        });
        return result;
    }

    private String translateByOccurency(String text,
                                        int frequency,
                                        Tokenizer tokenizer,
                                        Dict dict) {
        String[] lines = text.split("\n");
        StringBuilder resultBuilder = new StringBuilder();
        for (String line : lines) {
            try {
                if (line.length() == 0) {
                    continue;
                }
                List<Token> tokens = tokenizer.tokenize(text);
                for (Token token : tokens) {
                    switch (token.getAllFeaturesArray()[0]) {
                        case TokenizerManager.TOKEN_TYPE_PARTICLE:
                        case TokenizerManager.TOKEN_TYPE_PUNCTUATION:
                            resultBuilder.append(token.getSurfaceForm());
                            break;
                        default:
                            WordEntry queryResult = dict.userViewByFrequency(token.getBaseForm(), frequency);
                            resultBuilder.append(token.getSurfaceForm());
                            if (queryResult == null) {
                                break;
                            } else {
                                resultBuilder.append("(");
                                String surfaceForm = token.getSurfaceForm();
                                String baseForm = token.getBaseForm();
                                if (!surfaceForm.equals(baseForm)) {
                                    resultBuilder.append(baseForm);
                                    resultBuilder.append(": ");
                                }
                                resultBuilder.append(queryResult.getRomajisInOneline());
                                resultBuilder.append(" | ");
                                resultBuilder.append(queryResult.getSummary());
                                resultBuilder.append(")");
                            }
                            break;
                    }
                }
            } finally {
                resultBuilder.append("\n");
            }
        }
        return resultBuilder.toString();
    }
}
