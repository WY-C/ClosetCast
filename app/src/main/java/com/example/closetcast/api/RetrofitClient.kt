package com.example.closetcast.api

import android.util.Log
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {
    private const val BASE_URL = "http://3.39.165.91:3000/"
    private var currentToken: String? = null  // 현재 토큰 저장

    private val loggingInterceptor = HttpLoggingInterceptor { message ->
        Log.d("RetrofitClient", message)
    }.apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    // Authorization 헤더에 토큰 자동 추가
    private val authInterceptor = okhttp3.Interceptor { chain ->
        val originalRequest = chain.request()
        val requestBuilder = originalRequest.newBuilder()

        if (currentToken != null) {
            Log.d("RetrofitClient", "토큰 추가: Bearer $currentToken")
            requestBuilder.header("Authorization", "Bearer $currentToken")
        }

        chain.proceed(requestBuilder.build())
    }

    private val httpClient = OkHttpClient.Builder()
        .addInterceptor(authInterceptor)
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .build()

    private val gson = GsonBuilder()
        .setLenient()
        .serializeNulls()
        .create()

    val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(httpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    val authApiService: AuthApiService by lazy {
        retrofit.create(AuthApiService::class.java)
    }

    val apiService: WeatherApiService by lazy {
        retrofit.create(WeatherApiService::class.java)
    }

    // 토큰 설정 (로그인 후 호출)
    fun setToken(token: String) {
        currentToken = token
        Log.d("RetrofitClient", "토큰 설정됨: $token")
    }
}
