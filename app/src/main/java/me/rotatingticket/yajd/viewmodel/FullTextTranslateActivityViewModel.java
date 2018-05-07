package me.rotatingticket.yajd.viewmodel;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.support.annotation.NonNull;

import me.rotatingticket.yajd.repository.FullTextTranslateRepository;

public class FullTextTranslateActivityViewModel extends AndroidViewModel {
    private FullTextTranslateRepository fullTextTranslateRepository;

    public FullTextTranslateActivityViewModel(@NonNull Application application) {
        super(application);
        fullTextTranslateRepository = FullTextTranslateRepository.getInstance(application);
    }

    public LiveData<String> translate(String text) {
        return fullTextTranslateRepository.translate(text);
    }

    public LiveData<String> getNetworkNotification() {
        return fullTextTranslateRepository.getNetworkNotification();
    }
}
