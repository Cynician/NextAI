package com.android.nextai.data.datasource.datebase.room

import androidx.room.Database
import androidx.room.RoomDatabase
import com.android.nextai.data.datasource.datebase.room.dao.MessageDao
import com.android.nextai.data.datasource.datebase.room.dao.ModelParamsDao
import com.android.nextai.data.datasource.datebase.room.dao.SessionDao
import com.android.nextai.data.datasource.datebase.room.entity.MessageEntity
import com.android.nextai.data.datasource.datebase.room.entity.ModelParamsEntity
import com.android.nextai.data.datasource.datebase.room.entity.SessionEntity


@Database(
    entities = [
        SessionEntity::class,
        MessageEntity::class,
        ModelParamsEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class ChatDatabase : RoomDatabase() {
    abstract fun sessionDao(): SessionDao
    abstract fun messageDao(): MessageDao
    abstract fun modelParamsDao(): ModelParamsDao
}