package com.aml_sakr.fitlife.core.data.sync

import com.aml_sakr.fitlife.core.data.connectivity.ConnectivityMonitor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class OfflineSyncCoordinator(
    private val dao: SyncTestDao,
    private val remoteClient: RemoteSyncClient,
    private val connectivityMonitor: ConnectivityMonitor
) {
    suspend fun sync(): SyncResult = withContext(Dispatchers.IO) {
        if (!connectivityMonitor.isConnected()) {
            return@withContext SyncResult(success = false, error = "No connectivity")
        }

        var successCount = 0
        var failureCount = 0
        var conflictResolvedCount = 0

        try {
            val unsynced = dao.getUnsyncedRecords()
            for (staleLocal in unsynced) {
                val local = dao.getById(staleLocal.id) ?: continue
                if (local.syncStatus == SyncStatus.SYNCED) {
                    continue
                }

                val remote = remoteClient.getRecord(local.id)
                if (remote == null) {
                    val uploaded = remoteClient.saveRecord(local)
                    if (uploaded) {
                        dao.update(local.copy(syncStatus = SyncStatus.SYNCED))
                        successCount++
                    } else {
                        failureCount++
                    }
                    continue
                }

                when {
                    local.lastModified > remote.lastModified -> {
                        val uploaded = remoteClient.saveRecord(local)
                        if (uploaded) {
                            dao.update(local.copy(syncStatus = SyncStatus.SYNCED))
                            successCount++
                        } else {
                            failureCount++
                        }
                    }
                    local.lastModified < remote.lastModified -> {
                        dao.update(remote.copy(syncStatus = SyncStatus.SYNCED))
                        conflictResolvedCount++
                        successCount++
                    }
                    else -> {
                        val uploaded = remoteClient.saveRecord(local)
                        if (uploaded) {
                            dao.update(local.copy(syncStatus = SyncStatus.SYNCED))
                            conflictResolvedCount++
                            successCount++
                        } else {
                            failureCount++
                        }
                    }
                }
            }
            val overallSuccess = failureCount == 0
            SyncResult(
                success = overallSuccess,
                successCount = successCount,
                failureCount = failureCount,
                conflictResolvedCount = conflictResolvedCount,
                error = if (overallSuccess) null else "Partial sync failure"
            )
        } catch (e: Exception) {
            SyncResult(success = false, error = e.message ?: "Unknown error")
        }
    }
}

data class SyncResult(
    val success: Boolean,
    val successCount: Int = 0,
    val failureCount: Int = 0,
    val conflictResolvedCount: Int = 0,
    val error: String? = null
)
