package com.aml_sakr.fitlife.core.data.sync

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [SyncTestEntity::class], version = 1, exportSchema = false)
@TypeConverters(SyncTypeConverters::class)
abstract class SyncTestDatabase : RoomDatabase() {
    abstract fun syncTestDao(): SyncTestDao
}
