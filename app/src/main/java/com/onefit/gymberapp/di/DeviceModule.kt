package com.onefit.gymberapp.di

import android.content.Context
import com.onefit.gymberapp.utils.DeviceUtility
import com.onefit.gymberapp.utils.DeviceUtilityImpl
import com.onefit.gymberapp.utils.LocationUtility
import com.onefit.gymberapp.utils.LocationUtilityImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class DeviceModule {

    @Provides
    @Singleton
    fun provideDeviceUtility(@ApplicationContext context: Context): DeviceUtility {
        return DeviceUtilityImpl(context)
    }

    @Provides
    @Singleton
    fun provideLocationUtility(@ApplicationContext context: Context): LocationUtility {
        return LocationUtilityImpl(context)
    }
}