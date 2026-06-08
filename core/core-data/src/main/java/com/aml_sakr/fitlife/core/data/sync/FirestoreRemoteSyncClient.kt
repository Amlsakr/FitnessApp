package com.aml_sakr.fitlife.core.data.sync

import com.google.android.gms.tasks.Task
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.suspendCancellableCoroutine

class FirestoreRemoteSyncClient(
    private val firestore: FirebaseFirestore
) : RemoteSyncClient {

    private val collection = firestore.collection("sync_test_records")

    override suspend fun getRecord(id: String): SyncTestEntity? {
        val snapshot = collection.document(id).get().await()
        return if (snapshot.exists()) {
            val payload = snapshot.getString("payload") ?: ""
            val lastModified = snapshot.getLong("lastModified")
                ?: snapshot.getTimestampMillis("lastModified")
                ?: snapshot.getTimestampMillis("serverUpdatedAt")
                ?: 0L
            SyncTestEntity(id, payload, lastModified, SyncStatus.SYNCED)
        } else {
            null
        }
    }

    override suspend fun saveRecord(record: SyncTestEntity): Boolean {
        val data = mapOf(
            "id" to record.id,
            "payload" to record.payload,
            "lastModified" to record.lastModified,
            "syncStatus" to SyncStatus.SYNCED.name,
            "serverUpdatedAt" to FieldValue.serverTimestamp()
        )
        collection.document(record.id).set(data).await()
        return true
    }

    private fun com.google.firebase.firestore.DocumentSnapshot.getTimestampMillis(
        field: String
    ): Long? = when (val value = get(field)) {
        is Timestamp -> value.toDate().time
        is java.util.Date -> value.time
        else -> null
    }

    private suspend fun <T> Task<T>.await(): T = suspendCancellableCoroutine { continuation ->
        addOnCompleteListener { task ->
            if (!continuation.isActive) {
                return@addOnCompleteListener
            }
            if (task.isSuccessful) {
                continuation.resume(task.result)
            } else {
                continuation.resumeWithException(task.exception ?: RuntimeException("Task failed"))
            }
        }
    }
}
