package com.example.studynotes.api;

import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface OpenRouterService {

    @Headers("Content-Type: application/json")
    @POST("api/v1/chat/completions")
    Call<RequestBody> generateNotes(
            @Query("key") String apiKey,
            @Body RequestBody body
    );
}