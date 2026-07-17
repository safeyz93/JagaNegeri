package com.jaganegeri.ui.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jaganegeri.data.repository.CaseRepository
import com.jaganegeri.data.repository.ValidationRepository
import com.jaganegeri.ui.theme.Green700
import com.jaganegeri.ui.theme.Orange700
import com.jaganegeri.ui.theme.Red700

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    userId: String,
    username: String,
    caseRepository: CaseRepository,
    validationRepository: ValidationRepository,
    onBack: () -> Unit,
    onValidationQueueClick: () -> Unit,
    onLogout: () -> Unit
) {
    val viewModel = remember { ProfileViewModel(userId, caseRepository, validationRepository) }
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profil") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { padding ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                // Avatar placeholder
                Card(
                    modifier = Modifier
                        .size(80.dp)
                        .align(Alignment.CenterHorizontally),
                    shape = MaterialTheme.shapes.extraLarge,
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        Text(
                            text = username.take(2).uppercase(),
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "@$username",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )

                Spacer(modifier = Modifier.height(24.dp))

                // === Statistik Input ===
                Text(
                    text = "Statistik Input Data",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.height(8.dp))

                StatCard(
                    label = "Total kasus diinput",
                    value = "${uiState.totalInput}",
                    color = MaterialTheme.colorScheme.primary
                )
                StatCard(
                    label = "✅ Terverifikasi",
                    value = "${uiState.terverifikasi}",
                    color = Green700
                )
                StatCard(
                    label = "⏳ Menunggu validasi",
                    value = "${uiState.menunggu}",
                    color = Orange700
                )
                StatCard(
                    label = "❌ Ditolak",
                    value = "${uiState.ditolak}",
                    color = MaterialTheme.colorScheme.error
                )

                Spacer(modifier = Modifier.height(20.dp))

                // === Statistik Validasi ===
                Text(
                    text = "Validasi Saya",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.height(8.dp))

                StatCard(
                    label = "Total vote diberikan",
                    value = "${uiState.totalVote}",
                    color = MaterialTheme.colorScheme.primary
                )
                StatCard(
                    label = "✅ Approve",
                    value = "${uiState.approveVote}",
                    color = Green700
                )
                StatCard(
                    label = "❌ Tolak",
                    value = "${uiState.tolakVote}",
                    color = MaterialTheme.colorScheme.error
                )

                Spacer(modifier = Modifier.height(20.dp))

                // === Tombol Validasi Queue ===
                OutlinedCard(
                    onClick = onValidationQueueClick,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Validasi Data Masuk",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                            Text(
                                text = "${uiState.queueCount} data menunggu",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        if (uiState.queueCount > 0) {
                            Badge(
                                containerColor = Red700,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            ) {
                                Text("${uiState.queueCount}")
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        Icon(
                            Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // === Tombol Logout ===
                OutlinedButton(
                    onClick = onLogout,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("KELUAR", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun StatCard(label: String, value: String, color: androidx.compose.ui.graphics.Color) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.08f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(label, fontSize = 14.sp)
            Text(
                text = value,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = color
            )
        }
    }
}
