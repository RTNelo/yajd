package me.rotatingticket.yajd.viewmodel;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.support.annotation.NonNull;

import me.rotatingticket.yajd.repository.FullTextTranslateRepository;

public class FullTextTranslateActivityViewModel extends AndroidViewModel {
    private FullTextTranslateRepository fullTextTranslateRepository;
    private MutableLiveData<Integer> level;

    public FullTextTranslateActivityViewModel(@NonNull Application application) {
        super(application);
        fullTextTranslateRepository = FullTextTranslateRepository.getInstance(application);
        level = new MutableLiveData<>();
    }

    public LiveData<String> translate(String text) {
        return fullTextTranslateRepository.translate(text);
    }

    public LiveData<String> getNetworkNotification() {
        return fullTextTranslateRepository.getNetworkNotification();
    }

    public LiveData<String> translateByOccurency(String text, int frequency) {
        return fullTextTranslateRepository.getTranslateByOccurency(text, frequency);
    }

    public void setLevel(int level) {
        this.level.setValue(level);
    }

    public MutableLiveData<Integer> getLevel() {
        return level;
    }
}
