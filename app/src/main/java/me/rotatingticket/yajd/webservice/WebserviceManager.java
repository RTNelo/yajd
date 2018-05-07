package me.rotatingticket.yajd.webservice;

import retrofit2.Retrofit;
import retrofit2.converter.simplexml.SimpleXmlConverterFactory;

public class WebserviceManager {
    private static final String BING_TRANSLATOR_BASE_URL = "https://api.microsofttranslator.com/";
    private static BingTranslatorWebservice bingTranslatorWebservice;

    public static synchronized BingTranslatorWebservice getBingTranslatorWebservice() {
        if (bingTranslatorWebservice== null){
            Retrofit retrofit = new Retrofit.Builder()
                  .baseUrl(BING_TRANSLATOR_BASE_URL)
                  .addConverterFactory(SimpleXmlConverterFactory.create())
                  .build();
            bingTranslatorWebservice = retrofit.create(BingTranslatorWebservice.class);
        }
        return bingTranslatorWebservice;
    }
}
