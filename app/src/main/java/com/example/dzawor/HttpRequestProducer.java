package com.example.dzawor;

import okhttp3.*;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class HttpRequestProducer {
    private static final HttpRequestProducer INSTANCE = new HttpRequestProducer();
    private static final String BASE_PATH = "http://10.3.141.1:46001/";
    private static final String CALIBRATE = "calibrate";
    private final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build();

    public static HttpRequestProducer getInstance(){
        return INSTANCE;
    }

    public void sendCalibrate(Consumer<IOException> failureFunction,
                               Consumer<Response> responseFunction){
        RequestBody formBody = new FormBody.Builder()
                .add("message", "no value")
                .build();
        Request request = new Request.Builder()
                .url(BASE_PATH + CALIBRATE)
                .post(formBody)
                .build();

        registerRequest(failureFunction, responseFunction, request);
    }

    private void registerRequest(Consumer<IOException> failureFunction, Consumer<Response> responseFunction, Request request) {
        client.newCall(request)
                .enqueue(new Callback() {
                    @Override
                    public void onFailure(final Call call, IOException e) {
                        failureFunction.accept(e);
                    }

                    @Override
                    public void onResponse(Call call, final Response response) throws IOException {
                        responseFunction.accept(response);
                    }
                });
    }
}
