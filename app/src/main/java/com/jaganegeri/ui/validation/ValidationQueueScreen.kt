package com.jaganegeri.ui.validation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jaganegeri.data.model.CorruptionCase
import com.jaganegeri.data.repository.ValidationRepository
import com.jaganegeri.ui.theme.Green700
import com.jaganegeri.ui.theme.Red700

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ValidationQueueScreen(
    userId: String,
    validationRepository: ValidationRepository,
    onBack: () -> Unit,
    onCaseClick: (String) -> Unit
) {
    val viewModel = remember { ValidationQueueViewModel(userId, validationRepository) }
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Validasi Data Masuk") },
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Success/Error snackbar
            if (uiState.successMessage != null || uiState.error != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (uiState.error != null)
                            MaterialTheme.colorScheme.errorContainer
                        else
                            MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Text(
                        text = uiState.successMessage ?: uiState.error ?: "",
                        modifier = Modifier.padding(12.dp),
                        fontSize = 13.sp,
                        color = if (uiState.error != null)
                            MaterialTheme.colorScheme.onErrorContainer
                        else
                            MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
            }

            if (uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (uiState.queue.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "✅",
                            fontSize = 40.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Tidak ada data yang perlu divalidasi",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Semua data sudah diverifikasi user lain",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                // Header info
                Text(
                    text = "${uiState.queue.size} data menunggu validasi Anda",
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(uiState.queue) { case ->
                        ValidationCard(
                            case = case,
                            isLoading = uiState.loadingCaseId == case.id,
                            onApprove = { viewModel.approve(case.id) },
                            onTolak = { viewModel.tolak(case.id) },
                            onClick = { onCaseClick(case.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ValidationCard(
    case: CorruptionCase,
    isLoading: Boolean,
    onApprove: () -> Unit,
    onTolak: () -> Unit,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Info kasus
            Text(
                text = case.namaKoruptor,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${case.jabatan} • ${case.wilayah}",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Status: ${case.statusHukum.replaceFirstChar { it.uppercase() }}",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = case.tanggalPengumuman,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (case.sumberBerita.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Sumber: ${case.sumberBerita}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Tombol approve/tolak
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Approve
                Button(
                    onClick = onApprove,
                    modifier = Modifier.weight(1f),
                    enabled = !isLoading,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Green700
                    )
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("SETUJU", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // Tolak
                OutlinedButton(
                    onClick = onTolak,
                    modifier = Modifier.weight(1f),
                    enabled = !isLoading,
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Red700
                    )
                ) {
                    Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("TOLAK", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
