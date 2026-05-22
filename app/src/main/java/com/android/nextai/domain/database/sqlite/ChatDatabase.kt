package com.android.nextai.domain.database.sqlite

import androidx.room.Database
import androidx.room.RoomDatabase
import com.android.nextai.domain.database.sqlite.dao.MessageDao
import com.android.nextai.domain.database.sqlite.dao.SessionDao
import com.android.nextai.domain.database.sqlite.entity.MessageEntity
import com.android.nextai.domain.database.sqlite.entity.SessionEntity

@Database(
    entities = [
        SessionEntity::class,
        MessageEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class ChatDatabase : RoomDatabase() {
    abstract fun sessionDao(): SessionDao
    abstract fun messageDao(): MessageDao
}