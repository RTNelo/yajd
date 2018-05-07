package me.rotatingticket.yajd.webservice;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Query;

public interface BingTranslatorWebservice {
    String TRANSLATE_KEY = "4613cc5935f24fc8b52ff2d13948481c";

    @Headers("Ocp-Apim-Subscription-Key: " + TRANSLATE_KEY)
    @GET("V2/Http.svc/Translate")
    Call<String> shortTranslate(@Query("text") String text,
                                @Query("from") String from,
                                @Query("to") String to);
}
