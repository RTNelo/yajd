package me.rotatingticket.yajd.viewmodel;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.os.AsyncTask;
import android.support.annotation.NonNull;

import java.util.List;

import me.rotatingticket.yajd.dict.Dict;
import me.rotatingticket.yajd.dict.core.DictCore;
import me.rotatingticket.yajd.dict.core.WordEntry;
import me.rotatingticket.yajd.dict.implementation.BasicDict;
import me.rotatingticket.yajd.dict.implementation.core.sqlite.SQLiteDictCore;
import me.rotatingticket.yajd.dict.implementation.core.sqlite.SQLiteDictDatabase;
import me.rotatingticket.yajd.dict.implementation.core.sqlite.SQLiteWordEntry;

/**
 * ViewModel for MainActivity.
 * Only support custom dict search suggestion now.
 */
public class MainActivityViewModel extends AndroidViewModel {
    /**
     * Return at most CANDIDATE_LIMIT items when trigger a query.
     */
    public static final int CANDIDATE_LIMIT = 16;

    private Dict dict;
    private MutableLiveData<List<? extends WordEntry>> candidates;

    public MainActivityViewModel(@NonNull Application application) {
        super(application);
        SQLiteDictCore dictCore = SQLiteDictDatabase.getInstance(application).getSQLiteCoreDict();
        dict = new BasicDict(dictCore);
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
            candidates.postValue(dict.userQuery(input, CANDIDATE_LIMIT));
        });
    }
}
