package com.brhanumehari.traceroute.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.brhanumehari.traceroute.model.DnsResult
import com.brhanumehari.traceroute.model.NetworkHop
import com.brhanumehari.traceroute.model.PingResult
import com.brhanumehari.traceroute.model.TracerouteResult
import com.brhanumehari.traceroute.network.NetworkRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

/**
 * UI state for network diagnostics operations
 */
data class DiagnosticsUiState(
    val hostname: String = "",
    val isLoading: Boolean = false,
    val tracerouteResult: TracerouteResult? = null,
    val pingResult: PingResult? = null,
    val dnsResult: DnsResult? = null,
    val error: String? = null,
    val operationInProgress: OperationType? = null,
    val isDarkMode: Boolean = false
)

enum class OperationType {
    PING,
    TRACEROUTE,
    DNS_LOOKUP
}

@HiltViewModel
class DiagnosticsViewModel @Inject constructor(
    private val networkRepository: NetworkRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DiagnosticsUiState())
    val uiState: StateFlow<DiagnosticsUiState> = _uiState.asStateFlow()

    fun setHostname(hostname: String) {
        _uiState.value = _uiState.value.copy(hostname = hostname)
    }

    fun toggleDarkMode() {
        _uiState.value = _uiState.value.copy(
            isDarkMode = !_uiState.value.isDarkMode
        )
    }

    fun clearResults() {
        _uiState.value = _uiState.value.copy(
            tracerouteResult = null,
            pingResult = null,
            dnsResult = null,
            error = null
        )
    }

    fun executePing() {
        val hostname = _uiState.value.hostname.trim()
        if (hostname.isEmpty()) {
            _uiState.value = _uiState.value.copy(error = "Please enter a hostname or IP address")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                operationInProgress = OperationType.PING,
                error = null
            )

            try {
                val result = networkRepository.ping(hostname) { hop ->
                    // Update UI as hops are received
                    val currentResult = _uiState.value.pingResult
                    updatePingProgress(hop)
                }

                _uiState.value = _uiState.value.copy(
                    pingResult = result,
                    isLoading = false,
                    operationInProgress = null,
                    error = result.error
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    operationInProgress = null,
                    error = e.message ?: "Ping failed"
                )
            }
        }
    }

    fun executeTraceroute() {
        val hostname = _uiState.value.hostname.trim()
        if (hostname.isEmpty()) {
            _uiState.value = _uiState.value.copy(error = "Please enter a hostname or IP address")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                operationInProgress = OperationType.TRACEROUTE,
                error = null,
                tracerouteResult = TracerouteResult(
                    targetHostname = hostname,
                    targetIp = hostname,
                    hops = emptyList()
                )
            )

            try {
                val result = networkRepository.traceroute(hostname) { hop ->
                    updateTracerouteProgress(hop)
                }

                _uiState.value = _uiState.value.copy(
                    tracerouteResult = result,
                    isLoading = false,
                    operationInProgress = null,
                    error = result.error
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    operationInProgress = null,
                    error = e.message ?: "Traceroute failed"
                )
            }
        }
    }

    fun executeDnsLookup() {
        val hostname = _uiState.value.hostname.trim()
        if (hostname.isEmpty()) {
            _uiState.value = _uiState.value.copy(error = "Please enter a hostname")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                operationInProgress = OperationType.DNS_LOOKUP,
                error = null
            )

            try {
                val result = networkRepository.lookupDns(hostname)

                _uiState.value = _uiState.value.copy(
                    dnsResult = result,
                    isLoading = false,
                    operationInProgress = null,
                    error = result.error
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    operationInProgress = null,
                    error = e.message ?: "DNS lookup failed"
                )
            }
        }
    }

    fun stopOperation() {
        _uiState.value = _uiState.value.copy(
            isLoading = false,
            operationInProgress = null
        )
    }

    fun exportTracerouteResults(): String {
        val result = _uiState.value.tracerouteResult ?: return ""
        return buildString {
            appendLine("=== Traceroute Report ===")
            appendLine("Target: ${result.targetHostname} (${result.targetIp})")
            appendLine("Date: ${SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())}")
            appendLine("Total Time: ${result.totalTime}ms")
            appendLine()
            appendLine("Hops:")
            result.hops.forEach { hop ->
                appendLine(
                    "Hop ${hop.hopNumber}: ${hop.ipAddress} " +
                            "(${hop.hostname}) - ${hop.latencyMs}ms - ${hop.status}"
                )
            }
            if (result.error != null) {
                appendLine()
                appendLine("Error: ${result.error}")
            }
        }
    }

    private fun updateTracerouteProgress(hop: NetworkHop) {
        val currentResult = _uiState.value.tracerouteResult ?: return
        val updatedHops = currentResult.hops.toMutableList()
        val existingIndex = updatedHops.indexOfFirst { it.hopNumber == hop.hopNumber }

        if (existingIndex >= 0) {
            updatedHops[existingIndex] = hop
        } else {
            updatedHops.add(hop)
        }

        _uiState.value = _uiState.value.copy(
            tracerouteResult = currentResult.copy(hops = updatedHops.sortedBy { it.hopNumber })
        )
    }

    private fun updatePingProgress(hop: NetworkHop) {
        // Can be used for real-time ping updates if needed
    }
}
