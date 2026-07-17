package com.jaganegeri.ui.detail

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jaganegeri.data.repository.CaseRepository
import com.jaganegeri.ui.theme.Green700
import com.jaganegeri.ui.theme.Orange700

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailCaseScreen(
    caseId: String,
    caseRepository: CaseRepository,
    onBack: () -> Unit
) {
    val viewModel = remember { DetailViewModel(caseId, caseRepository) }
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detail Kasus") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Kembali")
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
        } else if (uiState.error != null) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("Error: ${uiState.error}", color = MaterialTheme.colorScheme.error)
            }
        } else {
            val case = uiState.case
            if (case != null) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp)
                ) {
                    // Nama
                    Text(
                        text = case.namaKoruptor,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    // Detail info
                    DetailRow("Jabatan", case.jabatan)
                    DetailRow("Wilayah", case.wilayah)
                    DetailRow("Status Hukum", case.statusHukum.replaceFirstChar { it.uppercase() })
                    DetailRow("Tanggal", case.tanggalPengumuman)

                    if (case.sumberBerita.isNotEmpty()) {
                        DetailRow("Sumber Berita", case.sumberBerita)
                    }
                    if (case.deskripsi.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Deskripsi:", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(case.deskripsi, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(16.dp))

                    // Status Verifikasi
                    Text("Status Verifikasi", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(8.dp))

                    val (statusLabel, statusColor) = when (case.statusVerifikasi) {
                        "terverifikasi" -> "✅ Terverifikasi" to Green700
                        "ditolak" -> "❌ Ditolak" to MaterialTheme.colorScheme.error
                        else -> "⏳ Menunggu Validasi" to Orange700
                    }
                    Surface(
                        color = statusColor.copy(alpha = 0.15f),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Text(
                            text = statusLabel,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            color = statusColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }

                    if (case.statusVerifikasi == "menunggu") {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Menunggu validasi dari 10 user lain",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Dibuat: ${case.createdAt.take(10)}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(label, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, fontSize = 15.sp)
    }
}
