package me.rotatingticket.yajd.viewmodel;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.MutableLiveData;
import android.os.AsyncTask;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import me.rotatingticket.yajd.dict.Dict;
import me.rotatingticket.yajd.dict.core.WordEntry;
import me.rotatingticket.yajd.dict.implementation.BasicDict;
import me.rotatingticket.yajd.dict.implementation.core.sqlite.SQLiteDictCore;
import me.rotatingticket.yajd.dict.implementation.core.sqlite.SQLiteDictDatabase;
import me.rotatingticket.yajd.util.SpellCheckerManager;
import me.rotatingticket.yajd.util.TokenizerManager;

/**
 * View model for look up result activity.
 * Even though this activity will not change the result value.
 */
public class LookUpResultActivityViewModel extends AndroidViewModel {
    private Dict dict;
    private MutableLiveData<List<? extends WordEntry>> lookUpResults;

    public LookUpResultActivityViewModel(@NonNull Application application) {
        super(application);
        SQLiteDictCore dictCore = SQLiteDictDatabase.getInstance(application).getSQLiteCoreDict();
        dict = new BasicDict(dictCore,
              SpellCheckerManager.getInstance(application),
              TokenizerManager.getInstance());
    }

    public MutableLiveData<List<? extends WordEntry>> getLookUpResults() {
        if (lookUpResults == null) {
            lookUpResults = new MutableLiveData<>();
        }
        return lookUpResults;
    }

    public void handleQuery(String query) {
        AsyncTask.execute(() -> {
            lookUpResults.postValue(dict.userQuery(query));
        });
    }

    public void handleView(String word) {
        AsyncTask.execute(() -> {
            WordEntry wordEntry = dict.userView(word);
            ArrayList<WordEntry> wordEntries = new ArrayList<>(1);
            if (wordEntry != null) {
                wordEntries.add(wordEntry);
            }
            lookUpResults.postValue(wordEntries);
        });
    }
}
