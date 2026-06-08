package com.aml_sakr.fitlife.core.data.sync

import android.content.Context
import androidx.room.Room
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object SyncModule {
    @Provides
    @Singleton
    fun provideSyncTestDatabase(
        @ApplicationContext context: Context
    ): SyncTestDatabase = Room.databaseBuilder(
        context,
        SyncTestDatabase::class.java,
        "sync_test.db"
    ).build()

    @Provides
    fun provideSyncTestDao(database: SyncTestDatabase): SyncTestDao = database.syncTestDao()

    @Provides
    @Singleton
    fun provideFirestore(): FirebaseFirestore = FirebaseFirestore.getInstance().also { firestore ->
        val shouldUseEmulator = System.getProperty("fitlife.firestore.useEmulator")?.toBoolean()
            ?: false
        if (shouldUseEmulator) {
            firestore.useEmulator("10.0.2.2", 8080)
        }
    }

    @Provides
    @Singleton
    fun provideRemoteSyncClient(
        firestore: FirebaseFirestore
    ): RemoteSyncClient = FirestoreRemoteSyncClient(firestore)
}
