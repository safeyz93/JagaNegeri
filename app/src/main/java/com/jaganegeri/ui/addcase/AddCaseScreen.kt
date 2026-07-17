package com.jaganegeri.ui.addcase

import android.app.DatePickerDialog
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCaseScreen(
    userId: String,
    selectedDate: String,
    caseRepository: com.jaganegeri.data.repository.CaseRepository,
    onBack: () -> Unit,
    onSaved: () -> Unit
) {
    val viewModel = remember { AddCaseViewModel(userId, selectedDate, caseRepository) }
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    var namaKoruptor by remember { mutableStateOf("") }
    var jabatan by remember { mutableStateOf("") }
    var wilayah by remember { mutableStateOf("") }
    var statusHukum by remember { mutableStateOf("tersangka") }
    var tanggal by remember { mutableStateOf(selectedDate) }
    var sumberBerita by remember { mutableStateOf("") }
    var deskripsi by remember { mutableStateOf("") }

    var statusDropdownExpanded by remember { mutableStateOf(false) }
    val statusOptions = listOf("tersangka", "terdakwa", "terpidana")

    LaunchedEffect(uiState.success) {
        if (uiState.success) onSaved()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tambah Kasus Korupsi") },
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
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            if (uiState.error != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text(
                        text = uiState.error!!,
                        modifier = Modifier.padding(12.dp),
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        fontSize = 13.sp
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Nama Koruptor
            OutlinedTextField(
                value = namaKoruptor,
                onValueChange = { namaKoruptor = it; viewModel.clearError() },
                label = { Text("Nama Koruptor *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(12.dp))

            // Jabatan
            OutlinedTextField(
                value = jabatan,
                onValueChange = { jabatan = it; viewModel.clearError() },
                label = { Text("Jabatan *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(12.dp))

            // Wilayah
            OutlinedTextField(
                value = wilayah,
                onValueChange = { wilayah = it; viewModel.clearError() },
                label = { Text("Wilayah *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(12.dp))

            // Status Hukum Dropdown
            ExposedDropdownMenuBox(
                expanded = statusDropdownExpanded,
                onExpandedChange = { statusDropdownExpanded = it }
            ) {
                OutlinedTextField(
                    value = statusHukum.replaceFirstChar { it.uppercase() },
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Status Hukum *") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = statusDropdownExpanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    singleLine = true
                )
                ExposedDropdownMenu(
                    expanded = statusDropdownExpanded,
                    onDismissRequest = { statusDropdownExpanded = false }
                ) {
                    statusOptions.forEach { status ->
                        DropdownMenuItem(
                            text = { Text(status.replaceFirstChar { it.uppercase() }) },
                            onClick = {
                                statusHukum = status
                                statusDropdownExpanded = false
                            }
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))

            // Tanggal - Button DatePicker
            Text(
                text = "Tanggal Pengumuman *",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            OutlinedButton(
                onClick = {
                    val cal = Calendar.getInstance()
                    DatePickerDialog(
                        context,
                        { _, year, month, day ->
                            tanggal = String.format("%04d-%02d-%02d", year, month + 1, day)
                        },
                        cal.get(Calendar.YEAR),
                        cal.get(Calendar.MONTH),
                        cal.get(Calendar.DAY_OF_MONTH)
                    ).show()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = if (tanggal.isNotEmpty()) "📅 $tanggal" else "📅 Pilih Tanggal",
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Spacer(modifier = Modifier.height(12.dp))

            // Sumber Berita (WAJIB)
            OutlinedTextField(
                value = sumberBerita,
                onValueChange = { sumberBerita = it; viewModel.clearError() },
                label = { Text("Sumber Berita * (URL)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri)
            )
            Spacer(modifier = Modifier.height(12.dp))

            // Deskripsi
            OutlinedTextField(
                value = deskripsi,
                onValueChange = { deskripsi = it },
                label = { Text("Deskripsi (opsional)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 80.dp),
                maxLines = 5
            )
            Spacer(modifier = Modifier.height(24.dp))

            // Tombol Simpan
            Button(
                onClick = {
                    viewModel.save(
                        namaKoruptor, jabatan, wilayah,
                        statusHukum, tanggal, sumberBerita, deskripsi
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                enabled = !uiState.isLoading
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("SIMPAN", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "* = wajib diisi",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
