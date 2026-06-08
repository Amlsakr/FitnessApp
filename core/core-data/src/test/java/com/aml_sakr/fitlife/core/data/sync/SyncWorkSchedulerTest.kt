package com.aml_sakr.fitlife.core.data.sync

import androidx.work.NetworkType
import org.junit.Assert.assertEquals
import org.junit.Test

class SyncWorkSchedulerTest {
    @Test
    fun buildRequest_requiresConnectedNetwork() {
        val constraints = SyncWorkScheduler.buildConstraints()

        assertEquals(NetworkType.CONNECTED, constraints.requiredNetworkType)
    }
}
