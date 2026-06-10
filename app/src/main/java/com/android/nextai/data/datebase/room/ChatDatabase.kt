package com.android.nextai.data.datebase.room

import androidx.room.Database
import androidx.room.RoomDatabase
import com.android.nextai.data.datebase.room.dao.MessageDao
import com.android.nextai.data.datebase.room.dao.SessionDao
import com.android.nextai.data.datebase.room.entity.MessageEntity
import com.android.nextai.data.datebase.room.entity.SessionEntity

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