package com.brhanumehari.traceroute.ui.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Brightness4
import androidx.compose.material.icons.filled.Brightness7
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.brhanumehari.traceroute.model.NetworkHop
import com.brhanumehari.traceroute.viewmodel.DiagnosticsViewModel
import com.brhanumehari.traceroute.viewmodel.OperationType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiagnosticsScreen(viewModel: DiagnosticsViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var selectedTab by remember { mutableIntStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top App Bar
        TopAppBar(
            title = {
                Text(
                    "Network Diagnostics",
                    color = MaterialTheme.colorScheme.onPrimary
                )
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.primary
            ),
            actions = {
                IconButton(onClick = { viewModel.toggleDarkMode() }) {
                    Icon(
                        imageVector = if (uiState.isDarkMode) Icons.Filled.Brightness7 else Icons.Filled.Brightness4,
                        contentDescription = "Toggle Dark Mode",
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Hostname Input
            TextField(
                value = uiState.hostname,
                onValueChange = { viewModel.setHostname(it) },
                label = { Text("Hostname or IP Address") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Tab Navigation
            TabRow(selectedTabIndex = selectedTab) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Ping") }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Traceroute") }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text("DNS") }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Progress Indicator
            if (uiState.isLoading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Error Message
            uiState.error?.let { error ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = error,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = { viewModel.clearResults() },
                            modifier = Modifier.width(24.dp)
                        ) {
                            Icon(
                                Icons.Filled.Close,
                                contentDescription = "Dismiss",
                                tint = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Tab Content
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                when (selectedTab) {
                    0 -> PingTab(uiState, viewModel)
                    1 -> TracerouteTab(uiState, viewModel)
                    2 -> DnsTab(uiState, viewModel)
                }
            }
        }
    }
}

@Composable
private fun PingTab(uiState: com.brhanumehari.traceroute.viewmodel.DiagnosticsUiState, viewModel: DiagnosticsViewModel) {
    val isPingOperation = uiState.operationInProgress == OperationType.PING

    item {
        Button(
            onClick = { viewModel.executePing() },
            enabled = !uiState.isLoading,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (isPingOperation) "Pinging..." else "Start Ping")
        }
    }

    uiState.pingResult?.let { result ->
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        "${result.hostname} (${result.ipAddress})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    StatRow("Packets Sent:", "${result.packetsTransmitted}")
                    StatRow("Packets Received:", "${result.packetsReceived}")
                    StatRow("Packet Loss:", "%.1f%%".format(result.packetLoss))
                    StatRow("Min Latency:", "${result.minLatencyMs}ms")
                    StatRow("Max Latency:", "${result.maxLatencyMs}ms")
                    StatRow("Avg Latency:", "${result.avgLatencyMs}ms")
                    StatRow("Total Time:", "${result.totalTime}ms")
                }
            }
        }
    }
}

@Composable
private fun TracerouteTab(
    uiState: com.brhanumehari.traceroute.viewmodel.DiagnosticsUiState,
    viewModel: DiagnosticsViewModel
) {
    val isTracerouteOperation = uiState.operationInProgress == OperationType.TRACEROUTE

    item {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { viewModel.executeTraceroute() },
                enabled = !uiState.isLoading,
                modifier = Modifier.weight(1f)
            ) {
                Text(if (isTracerouteOperation) "Tracing..." else "Start Traceroute")
            }

            if (isTracerouteOperation) {
                Button(
                    onClick = { viewModel.stopOperation() },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Stop")
                }
            }
        }
    }

    uiState.tracerouteResult?.let { result ->
        item {
            if (result.hops.isNotEmpty()) {
                Text(
                    "Hops (${result.hops.size}): ${result.targetHostname} (${result.targetIp})",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(4.dp)
                )
            }
        }

        items(result.hops) { hop ->
            HopCard(hop)
        }

        item {
            if (result.hops.isNotEmpty()) {
                Button(
                    onClick = { 
                        val report = viewModel.exportTracerouteResults()
                        // In a real app, this would open a share dialog or save to file
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Export Results")
                }
            }
        }
    }
}

@Composable
private fun DnsTab(uiState: com.brhanumehari.traceroute.viewmodel.DiagnosticsUiState, viewModel: DiagnosticsViewModel) {
    val isDnsOperation = uiState.operationInProgress == OperationType.DNS_LOOKUP

    item {
        Button(
            onClick = { viewModel.executeDnsLookup() },
            enabled = !uiState.isLoading,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (isDnsOperation) "Looking up..." else "Start DNS Lookup")
        }
    }

    uiState.dnsResult?.let { result ->
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        result.hostname,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    if (result.canonicalHostname.isNotEmpty()) {
                        StatRow("Canonical:", result.canonicalHostname)
                    }
                    StatRow("Lookup Time:", "${result.lookupTime}ms")
                    Text(
                        "Addresses (${result.addresses.size}):",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                    result.addresses.forEach { address ->
                        Text(
                            "• $address",
                            style = MaterialTheme.typography.bodySmall,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HopCard(hop: NetworkHop) {
    val statusColor = when (hop.status) {
        "Success" -> Color(0xFF4CAF50)
        "Timeout" -> Color(0xFFFFC107)
        "Error" -> Color(0xFFF44336)
        else -> Color(0xFF2196F3)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Hop Number
            Box(
                modifier = Modifier
                    .background(
                        color = statusColor,
                        shape = MaterialTheme.shapes.small
                    )
                    .padding(8.dp)
                    .width(40.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    hop.hopNumber.toString(),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    hop.ipAddress,
                    style = MaterialTheme.typography.bodyMedium,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
                if (hop.hostname != "?" && hop.hostname.isNotEmpty()) {
                    Text(
                        hop.hostname,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Latency
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                if (hop.status != "Timeout") {
                    Text(
                        "${hop.latencyMs}ms",
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Text(
                    hop.status,
                    style = MaterialTheme.typography.labelSmall,
                    color = statusColor
                )
            }
        }
    }
}

@Composable
private fun StatRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            value,
            style = MaterialTheme.typography.bodySmall,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.SemiBold
        )
    }
}
