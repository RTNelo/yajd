package me.rotatingticket.yajd.webservice;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Response;

import static org.junit.jupiter.api.Assertions.*;

class BingTranslatorWebserviceTest {

    private BingTranslatorWebservice webservice;

    @BeforeEach
    void setUp() {
        webservice = WebserviceManager.getBingTranslatorWebservice();
    }

    @Test
    void shortTranslate() throws IOException {
        Call<String> call = webservice.shortTranslate("テスト", "ja", "zh-cn");
        Response<String> response = call.execute();
        assertEquals(200, response.code());
        assertEquals("测试", response.body());
    }
}