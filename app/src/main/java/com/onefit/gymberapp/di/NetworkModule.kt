package com.onefit.gymberapp.di

import com.onefit.gymberapp.BuildConfig
import com.onefit.gymberapp.repository.network.GymberApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

private const val BASE_API_URL: String = BuildConfig.API_HOST
private const val TIMEOUT_DURATION = 20L //In Seconds

@Module
@InstallIn(SingletonComponent::class)
class NetworkModule {

    private val headerInterceptor = Interceptor { chain: Interceptor.Chain ->

        val requestBuilder = chain.request().newBuilder()
        requestBuilder.addHeader("User-Agent", "OneFit/1.19.0")
        chain.proceed(requestBuilder.build())
    }


    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {

        val httpClient = OkHttpClient.Builder().apply {
            connectTimeout(TIMEOUT_DURATION, TimeUnit.SECONDS)
            writeTimeout(TIMEOUT_DURATION, TimeUnit.SECONDS)
            readTimeout(TIMEOUT_DURATION, TimeUnit.SECONDS)

            addInterceptor(headerInterceptor)
        }

        return httpClient.build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {

        return Retrofit.Builder().baseUrl(BASE_API_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient)
            .build()
    }

    @Provides
    @Singleton
    fun getApiClientService(retrofit: Retrofit): GymberApi {
        return retrofit.create(GymberApi::class.java)
    }
}