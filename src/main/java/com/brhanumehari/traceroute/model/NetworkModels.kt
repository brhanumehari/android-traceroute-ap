package com.brhanumehari.traceroute.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Represents a single network hop in the traceroute path
 */
@Parcelize
data class NetworkHop(
    val hopNumber: Int,
    val ipAddress: String,
    val latencyMs: Long,
    val hostname: String = "",
    val status: HopStatus = HopStatus.IN_PROGRESS
) : Parcelable

/**
 * Status of a network hop
 */
enum class HopStatus {
    IN_PROGRESS,
    SUCCESS,
    TIMEOUT,
    ERROR
}

/**
 * Results of a traceroute operation
 */
@Parcelize
data class TracerouteResult(
    val targetHostname: String,
    val targetIp: String,
    val hops: List<NetworkHop> = emptyList(),
    val totalTime: Long = 0,
    val isComplete: Boolean = false,
    val error: String? = null
) : Parcelable

/**
 * Results of a ping operation
 */
@Parcelize
data class PingResult(
    val targetHostname: String,
    val targetIp: String,
    val packetsTransmitted: Int = 0,
    val packetsReceived: Int = 0,
    val minLatencyMs: Long = 0,
    val maxLatencyMs: Long = 0,
    val avgLatencyMs: Long = 0,
    val error: String? = null
) : Parcelable

/**
 * Results of a DNS lookup
 */
@Parcelize
data class DnsResult(
    val hostname: String,
    val addresses: List<String> = emptyList(),
    val error: String? = null,
    val lookupTimeMs: Long = 0
) : Parcelable
