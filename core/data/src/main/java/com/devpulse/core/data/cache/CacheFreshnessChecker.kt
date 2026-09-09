package com.devpulse.core.data.cache

import com.devpulse.core.database.dao.SyncMetadataDao
import javax.inject.Inject

class CacheFreshnessChecker @Inject constructor(
    private val syncMetadataDao: SyncMetadataDao,
    private val clock: DevPulseClock,
    private val cacheFreshnessPolicy: CacheFreshnessPolicy = CacheFreshnessPolicy(),
) {
    suspend fun isFresh(syncKey: String): Boolean {
        val refreshedAt = syncMetadataDao.getSyncMetadata(syncKey)?.refreshedAtEpochMillis
        return cacheFreshnessPolicy.isFresh(
            refreshedAtEpochMillis = refreshedAt,
            nowEpochMillis = clock.nowEpochMillis(),
        )
    }
}
