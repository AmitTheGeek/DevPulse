package com.devpulse.core.data.cache

interface DevPulseClock {
    fun nowEpochMillis(): Long
}

object SystemDevPulseClock : DevPulseClock {
    override fun nowEpochMillis(): Long = System.currentTimeMillis()
}

