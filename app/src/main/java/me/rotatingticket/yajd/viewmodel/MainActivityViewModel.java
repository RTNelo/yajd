package me.rotatingticket.yajd.viewmodel;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.MutableLiveData;
import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.NonNull;

import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import me.rotatingticket.yajd.R;
import me.rotatingticket.yajd.dict.Dict;
import me.rotatingticket.yajd.dict.core.WordEntry;
import me.rotatingticket.yajd.dict.implementation.BasicDict;
import me.rotatingticket.yajd.dict.implementation.core.sqlite.SQLiteDictCore;
import me.rotatingticket.yajd.dict.implementation.core.sqlite.SQLiteDictDatabase;
import me.rotatingticket.yajd.util.SpellCheckerManager;
import me.rotatingticket.yajd.util.TokenizerManager;
import me.rotatingticket.yajd.util.zinnia.Character;
import me.rotatingticket.yajd.util.zinnia.Recognizer;
import me.rotatingticket.yajd.util.zinnia.ResultSet;

/**
 * ViewModel for MainActivity.
 * Only support custom dict search suggestion now.
 */
public class MainActivityViewModel extends AndroidViewModel {
    /**
     * Return at most CANDIDATE_LIMIT items when trigger a query.
     */
    public static final int CANDIDATE_LIMIT = 16;
    public static final int HANDWRITING_CANDIDATE_LIMIT = 20;
    private static final String RECOGNIZER_MODEL_FILE_NAME = "handwriting.model";

    private Dict dict;
    private Recognizer recognizer;
    private MutableLiveData<List<? extends WordEntry>> candidates;
    private MutableLiveData<Boolean> handwritingToggled;
    private MutableLiveData<List<String>> handwritingCandidates;

    public MainActivityViewModel(@NonNull Application application) throws IOException {
        super(application);
        dict = prepareDict(application);
        recognizer = Recognizer.getInstance(prepareRecognizerModel(application));
    }

    /**
     * Prepare dict instance.
     * @param context The app context.
     * @return The dict instance.
     */
    public static Dict prepareDict(@NonNull Context context) {
        SQLiteDictCore dictCore = SQLiteDictDatabase.getInstance(context).getSQLiteDictCore();
        return new BasicDict(dictCore,
              SpellCheckerManager.getInstance(context),
              TokenizerManager.getInstance());
    }

    /**
     * Copy the recognizer model file from the resources to the local storage.
     * Skip the copy operation if there is already one file.
     * @param application Application instance for resource accessing.
     * @return The file path in the local storage.
     * @throws IOException Throwed at IO Error.
     */
    private String prepareRecognizerModel(@NonNull Application application) throws IOException {
        File file = application.getFileStreamPath(RECOGNIZER_MODEL_FILE_NAME);
        if (file.exists()) {
            return file.getAbsolutePath();
        }
        return initializeRecognizerModel(application);
    }

    /**
     * Copy the recognizer model file from the resources to the local storage.
     * @param application Application instance for resource accessing.
     * @return The file path in the local storage.
     * @throws IOException Throwed at IO Error.
     */
    private String initializeRecognizerModel(@NonNull Application application) throws IOException {
        InputStream inputStream = application.getResources().openRawResource(R.raw.handwriting);
        File internalFile = new File(application.getFilesDir(), RECOGNIZER_MODEL_FILE_NAME);
        OutputStream outputStream = new FileOutputStream(internalFile);
        IOUtils.copy(inputStream, outputStream);
        return internalFile.getAbsolutePath();
    }

    /**
     * Get candidate word entries for search suggestion.
     * @return A live data for a list of candidate WordEntry.
     */
    public MutableLiveData<List<? extends WordEntry>> getCandidates() {
        if (candidates == null) {
            candidates = new MutableLiveData<>();
        }
        return candidates;
    }

    /**
     * Trigger a query in the background thread.
     * The result will post to the result of getCandidates.
     * @param input User's query string. At most CANDIDATE_LIMIT items.
     */
    public void query(String input) {
        AsyncTask.execute(() -> {
            candidates.postValue(dict.userQuerySuggestion(input, CANDIDATE_LIMIT));
        });
    }

    /**
     * Get if the handwriting pannel is opened.
     * @return The target live data having a false default value.
     */
    public MutableLiveData<Boolean> getHandwritingToggled() {
        if (handwritingToggled == null) {
            handwritingToggled = new MutableLiveData<>();
            handwritingToggled.setValue(false);
        }
        return handwritingToggled;
    }

    /**
     * Get the toggle value directly.
     * Default is false.
     * @return If the handwriting panel is opened.
     */
    public boolean getHandWritingToggledRealValue() {
        Boolean result = getHandwritingToggled().getValue();
        return result != null && result;
    }

    /**
     * Toggle the handwriting panel.
     * @return Whether the panel is opened after the toggle exception.
     */
    public boolean toggleHandwriting() {
        if (handwritingToggled.getValue() == null || !handwritingToggled.getValue()) {
            setHandWritingState(true);
            return true;
        } else {
            setHandWritingState(false);
            return false;
        }
    }

    /**
     * Set whether the handwriting panel is opened directly.
     * @param state opened
     */
    public void setHandWritingState(boolean state) {
        handwritingToggled.setValue(state);
    }


    /**
     * Get the handwriting candidates in a list of string.
     * @return The LiveData for handwriting candidates.
     */
    public MutableLiveData<List<String>> getHandwritingCandidates() {
        if (handwritingCandidates == null) {
            handwritingCandidates = new MutableLiveData<>();
        }
        return handwritingCandidates;
    }

    /**
     * Recognize a Character in background.
     * @param ch The target Character.
     */
    public void recognizeCharacter(Character ch) {
        AsyncTask.execute(() -> {
            try (ResultSet resultSet = recognizer.classify(ch,
                  HANDWRITING_CANDIDATE_LIMIT)) {
                if (resultSet != null) {
                    handwritingCandidates.postValue(resultSet.toList());
                }
            }
        });
    }
}
