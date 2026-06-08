package com.aml_sakr.fitlife.core.data.sync

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager

object SyncWorkScheduler {
    const val UNIQUE_WORK_NAME = "fitlife_sync_work"

    internal fun buildConstraints() =
        Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

    internal fun buildRequest() =
        OneTimeWorkRequestBuilder<SyncWorker>()
            .setConstraints(buildConstraints())
            .build()

    fun schedule(context: Context) {
        WorkManager.getInstance(context).enqueueUniqueWork(
            UNIQUE_WORK_NAME,
            ExistingWorkPolicy.KEEP,
            buildRequest()
        )
    }
}
