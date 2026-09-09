package com.devpulse.core.data.di

import com.devpulse.core.data.cache.CacheFreshnessPolicy
import com.devpulse.core.data.cache.DevPulseClock
import com.devpulse.core.data.cache.SystemDevPulseClock
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object CacheModule {
    @Provides
    @Singleton
    fun provideDevPulseClock(): DevPulseClock = SystemDevPulseClock

    @Provides
    @Singleton
    fun provideCacheFreshnessPolicy(): CacheFreshnessPolicy =
        CacheFreshnessPolicy()
}
