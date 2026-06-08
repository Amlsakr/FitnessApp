package com.aml_sakr.fitlife.core.data.sync

import android.content.Context
import dagger.hilt.android.EntryPointAccessors
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

class SyncWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val entryPoint = EntryPointAccessors.fromApplication(
            applicationContext,
            SyncEntryPoint::class.java
        )
        val dao = entryPoint.syncTestDao()
        val remoteClient = entryPoint.remoteSyncClient()
        val connectivityMonitor = entryPoint.connectivityMonitor()

        val coordinator = OfflineSyncCoordinator(dao, remoteClient, connectivityMonitor)
        val syncResult = coordinator.sync()

        return if (syncResult.success) {
            Result.success()
        } else {
            if (syncResult.error == "No connectivity") {
                Result.retry()
            } else {
                Result.failure()
            }
        }
    }
}
