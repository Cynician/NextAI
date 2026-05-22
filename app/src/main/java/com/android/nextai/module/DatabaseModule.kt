package com.android.nextai.module

import android.content.Context
import androidx.room.Room
import com.android.nextai.domain.database.sqlite.ChatDatabase
import com.android.nextai.domain.database.sqlite.dao.MessageDao
import com.android.nextai.domain.database.sqlite.dao.SessionDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context,
    ): ChatDatabase {
        return Room.databaseBuilder(
            context,
            ChatDatabase::class.java,
            "chat.db"
        ).fallbackToDestructiveMigration(true) // develop stage
            .build()
    }

    @Provides
    fun provideSessionDao(db: ChatDatabase): SessionDao {
        return db.sessionDao()
    }

    @Provides
    fun provideMessageDao(db: ChatDatabase): MessageDao {
        return db.messageDao()
    }
}