package com.aml_sakr.fitlife.core.data.sync

import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface SyncEntryPoint {
    fun syncTestDao(): SyncTestDao
    fun remoteSyncClient(): RemoteSyncClient
    fun connectivityMonitor(): com.aml_sakr.fitlife.core.data.connectivity.ConnectivityMonitor
}
