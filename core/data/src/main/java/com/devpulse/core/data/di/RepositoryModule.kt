package com.devpulse.core.data.di

import com.devpulse.core.data.repository.DefaultDeveloperRepository
import com.devpulse.core.data.repository.DefaultRepositoryCatalog
import com.devpulse.core.data.repository.DeveloperRepository
import com.devpulse.core.data.repository.RepositoryCatalog
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    @Singleton
    abstract fun bindDeveloperRepository(
        repository: DefaultDeveloperRepository,
    ): DeveloperRepository

    @Binds
    @Singleton
    abstract fun bindRepositoryCatalog(
        repository: DefaultRepositoryCatalog,
    ): RepositoryCatalog
}
