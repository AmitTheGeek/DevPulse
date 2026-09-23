package com.devpulse.core.database.di

import android.content.Context
import androidx.room.Room
import com.devpulse.core.database.DevPulseDatabase
import com.devpulse.core.database.DevPulseDatabaseMigrations
import com.devpulse.core.database.dao.DeveloperDao
import com.devpulse.core.database.dao.RepositoryDao
import com.devpulse.core.database.dao.SavedRepositoryDao
import com.devpulse.core.database.dao.SyncMetadataDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    private const val DATABASE_NAME = "devpulse.db"

    @Provides
    @Singleton
    fun provideDevPulseDatabase(
        @ApplicationContext context: Context,
    ): DevPulseDatabase =
        Room.databaseBuilder(
            context,
            DevPulseDatabase::class.java,
            DATABASE_NAME,
        )
            .addMigrations(DevPulseDatabaseMigrations.MIGRATION_1_2)
            .build()

    @Provides
    fun provideDeveloperDao(database: DevPulseDatabase): DeveloperDao =
        database.developerDao()

    @Provides
    fun provideRepositoryDao(database: DevPulseDatabase): RepositoryDao =
        database.repositoryDao()

    @Provides
    fun provideSavedRepositoryDao(database: DevPulseDatabase): SavedRepositoryDao =
        database.savedRepositoryDao()

    @Provides
    fun provideSyncMetadataDao(database: DevPulseDatabase): SyncMetadataDao =
        database.syncMetadataDao()
}
