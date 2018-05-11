package me.rotatingticket.yajd.repository;

import android.app.Application;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;

import me.rotatingticket.yajd.R;
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

    private FullTextTranslateRepository(Application application, BingTranslatorWebservice webservice) {
        this.webservice = webservice;
        this.networkNotification = new MutableLiveData<>();
        this.application = application;
    }

    public static synchronized FullTextTranslateRepository getInstance(Application application) {
        if (instance == null) {
            BingTranslatorWebservice webservice = WebserviceManager.getBingTranslatorWebservice();
            instance = new FullTextTranslateRepository(application, webservice);
        }
        return instance;
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
}
