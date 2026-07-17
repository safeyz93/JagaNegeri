package com.jaganegeri.ui.riwayat

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jaganegeri.data.model.CorruptionCase
import com.jaganegeri.data.repository.CaseRepository
import com.jaganegeri.ui.theme.Green700
import com.jaganegeri.ui.theme.Orange700
import com.jaganegeri.ui.theme.Red700

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RiwayatScreen(
    caseRepository: CaseRepository,
    onBack: () -> Unit,
    onCaseClick: (String) -> Unit
) {
    val viewModel = remember { RiwayatViewModel(caseRepository) }
    val uiState by viewModel.uiState.collectAsState()
    var searchText by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Riwayat Korupsi") },
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            // Search bar
            OutlinedTextField(
                value = searchText,
                onValueChange = { searchText = it },
                label = { Text("Cari nama koruptor...") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                trailingIcon = {
                    IconButton(onClick = { viewModel.search(searchText) }) {
                        Icon(Icons.Default.Search, contentDescription = "Cari")
                    }
                },
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                    imeAction = androidx.compose.ui.text.input.ImeAction.Search
                ),
                keyboardActions = androidx.compose.foundation.text.KeyboardActions(
                    onSearch = { viewModel.search(searchText) }
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Tombol cari
            Button(
                onClick = { viewModel.search(searchText) },
                modifier = Modifier.fillMaxWidth(),
                enabled = searchText.isNotBlank() && !uiState.isLoading
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(Icons.Default.Search, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("CARI", fontWeight = FontWeight.Bold)
                }
            }

            if (uiState.error != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = uiState.error!!,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 13.sp
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Hasil
            if (!uiState.hasSearched) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "🔍\nKetik nama koruptor\nuntuk mencari riwayat",
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 15.sp
                    )
                }
            } else if (uiState.results.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Tidak ditemukan data untuk \"${uiState.query}\"",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    uiState.groupedResults.forEach { (nama, cases) ->
                        item {
                            Text(
                                text = "$nama (${cases.size} kasus)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        }
                        items(cases) { case ->
                            RiwayatCaseCard(
                                case = case,
                                onClick = { onCaseClick(case.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RiwayatCaseCard(case: CorruptionCase, onClick: () -> Unit) {
    val statusColor = when (case.statusVerifikasi) {
        "terverifikasi" -> Green700
        "ditolak" -> MaterialTheme.colorScheme.onSurfaceVariant
        else -> Orange700
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${case.jabatan} • ${case.wilayah}",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f)
                )
                Surface(
                    color = statusColor.copy(alpha = 0.15f),
                    shape = MaterialTheme.shapes.extraSmall
                ) {
                    Text(
                        text = when (case.statusVerifikasi) {
                            "terverifikasi" -> "✅ Terverifikasi"
                            "ditolak" -> "Ditolak"
                            else -> "⏳ Menunggu"
                        },
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        color = statusColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
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
        }
    }
}
