package com.aml_sakr.fitlife.core.data.sync

import androidx.room.TypeConverter

class SyncTypeConverters {
    @TypeConverter
    fun toSyncStatus(value: String): SyncStatus = SyncStatus.valueOf(value)

    @TypeConverter
    fun fromSyncStatus(status: SyncStatus): String = status.name
}
