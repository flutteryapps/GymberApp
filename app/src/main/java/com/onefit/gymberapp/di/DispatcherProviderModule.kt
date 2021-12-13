package com.onefit.gymberapp.di

import com.onefit.gymberapp.utils.DefaultDispatcherProvider
import com.onefit.gymberapp.utils.DispatcherProvider
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class DispatcherProviderModule {

    @Provides
    @Singleton
    fun getDispatcherProvider(): DispatcherProvider {
        return DefaultDispatcherProvider()
    }
}