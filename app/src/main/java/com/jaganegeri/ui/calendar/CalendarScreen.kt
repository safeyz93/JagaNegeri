package com.jaganegeri.ui.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jaganegeri.data.model.CorruptionCase
import com.jaganegeri.data.repository.DateDetail
import com.jaganegeri.ui.theme.Red700
import com.jaganegeri.ui.theme.Red50
import com.jaganegeri.ui.theme.Orange700
import com.jaganegeri.ui.theme.Green700
import com.jaganegeri.ui.theme.Green50
import java.util.Calendar

private val dayHeaders = listOf("Min", "Sen", "Sel", "Rab", "Kam", "Jum", "Sab")
private val monthNames = listOf(
    "Januari", "Februari", "Maret", "April", "Mei", "Juni",
    "Juli", "Agustus", "September", "Oktober", "November", "Desember"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    userId: String,
    initialMonth: Int,   // 0-11
    initialYear: Int,
    caseRepository: com.jaganegeri.data.repository.CaseRepository,
    onBack: () -> Unit,
    onAddCase: (String) -> Unit,  // selected date yyyy-MM-dd
    onCaseClick: (String) -> Unit  // case id
) {
    val viewModel = remember { CalendarViewModel(userId, caseRepository) }
    LaunchedEffect(initialMonth, initialYear) {
        viewModel.setMonthYear(initialMonth, initialYear)
    }

    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("${monthNames[uiState.currentMonth]} ${uiState.currentYear}") },
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
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onAddCase(uiState.selectedDate) },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Tambah Kasus")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // === Navigasi bulan ===
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = { viewModel.previousMonth() }) {
                    Icon(Icons.Default.ChevronLeft, contentDescription = "Sebelumnya")
                    Text("Sebelumnya")
                }
                TextButton(onClick = { viewModel.nextMonth() }) {
                    Text("Berikutnya")
                    Icon(Icons.Default.ChevronRight, contentDescription = "Berikutnya")
                }
            }

            // === Hari dalam seminggu ===
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
            ) {
                dayHeaders.forEach { day ->
                    Text(
                        text = day,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        fontSize = 12.sp,
                        fontWeight = if (day == "Min" || day == "Sab") FontWeight.Bold else FontWeight.Normal,
                        color = if (day == "Min" || day == "Sab")
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // === Grid tanggal ===
            val cal = remember(uiState.currentMonth, uiState.currentYear) {
                Calendar.getInstance().apply {
                    set(uiState.currentYear, uiState.currentMonth, 1)
                }
            }
            val daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
            val firstDayOfWeek = (cal.get(Calendar.DAY_OF_WEEK) - 1 + 7) % 7  // 0=Min, 1=Sen,...

            val today = remember {
                val c = Calendar.getInstance()
                String.format("%04d-%02d-%02d", c.get(Calendar.YEAR), c.get(Calendar.MONTH) + 1, c.get(Calendar.DAY_OF_MONTH))
            }

            val dayItems = remember(uiState.currentMonth, uiState.currentYear) {
                buildList {
                    for (i in 0 until firstDayOfWeek) add(null)
                    for (d in 1..daysInMonth) add(d)
                }
            }

            LazyVerticalGrid(
                columns = GridCells.Fixed(7),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 320.dp)
                    .padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(2.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp),
                userScrollEnabled = false
            ) {
                items(dayItems) { day ->
                    if (day == null) {
                        Box(modifier = Modifier.aspectRatio(1f))
                    } else {
                        val dateStr = String.format("%04d-%02d-%02d", uiState.currentYear, uiState.currentMonth + 1, day)
                        val isSelected = dateStr == uiState.selectedDate
                        val isToday = dateStr == today
                        val hasEvent = uiState.datesWithEvents.contains(dateStr)
                        val detail = uiState.dateDetails[dateStr]

                        Box(
                            modifier = Modifier
                                .aspectRatio(1f)
                                .then(
                                    if (isSelected) Modifier
                                        .background(MaterialTheme.colorScheme.primary, MaterialTheme.shapes.small)
                                    else Modifier
                                )
                                .clickable { viewModel.selectDate(dateStr) },
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = day.toString(),
                                    fontSize = 14.sp,
                                    fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                                    color = when {
                                        isSelected -> MaterialTheme.colorScheme.onPrimary
                                        isToday -> MaterialTheme.colorScheme.primary
                                        else -> MaterialTheme.colorScheme.onSurface
                                    }
                                )
                                if (detail != null) {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        if (detail.terverifikasi > 0) {
                                            Box(
                                                modifier = Modifier
                                                    .size(5.dp)
                                                    .background(Green700, MaterialTheme.shapes.extraSmall)
                                            )
                                        }
                                        if (detail.menunggu > 0) {
                                            Box(
                                                modifier = Modifier
                                                    .size(5.dp)
                                                    .background(Orange700, MaterialTheme.shapes.extraSmall)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            // === Selected date label ===
            Text(
                text = uiState.selectedDate.ifEmpty { "Pilih tanggal" },
                modifier = Modifier.padding(horizontal = 16.dp),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )

            // === Detail verifikasi tanggal terpilih ===
            val selectedDetail = uiState.dateDetails[uiState.selectedDate]
            if (selectedDetail != null) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 2.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "✅ Terverifikasi: ${selectedDetail.terverifikasi}",
                        fontSize = 13.sp,
                        color = Green700,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "⏳ Menunggu: ${selectedDetail.menunggu}",
                        fontSize = 13.sp,
                        color = Orange700,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // === List kasus ===
            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (uiState.eventsOnSelectedDate.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Belum ada kasus di tanggal ini.\nTap + untuk tambah",
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(uiState.eventsOnSelectedDate) { case ->
                        CaseCard(
                            case = case,
                            onClick = { onCaseClick(case.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CaseCard(case: CorruptionCase, onClick: () -> Unit) {
    val statusColor = when (case.statusVerifikasi) {
        "terverifikasi" -> Green700
        "ditolak" -> MaterialTheme.colorScheme.onSurfaceVariant
        else -> Orange700
    }
    val statusLabel = case.statusVerifikasi.replace("menunggu", "Menunggu")
        .replace("terverifikasi", "Terverifikasi")
        .replace("ditolak", "Ditolak")

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = case.namaKoruptor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${case.jabatan} • ${case.wilayah}",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Status: ${case.statusHukum}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            // Badge status verifikasi
            Surface(
                color = statusColor.copy(alpha = 0.15f),
                shape = MaterialTheme.shapes.small
            ) {
                Text(
                    text = statusLabel,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                    color = statusColor,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
