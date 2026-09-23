package com.devpulse.core.data.cache

class CacheFreshnessPolicy(
    val ttlMillis: Long = DEFAULT_TTL_MILLIS,
) {
    fun isFresh(
        refreshedAtEpochMillis: Long?,
        nowEpochMillis: Long,
    ): Boolean =
        refreshedAtEpochMillis != null &&
            refreshedAtEpochMillis <= nowEpochMillis &&
            nowEpochMillis - refreshedAtEpochMillis <= ttlMillis

    companion object {
        const val DEFAULT_TTL_MILLIS = 5 * 60 * 1_000L
    }
}
