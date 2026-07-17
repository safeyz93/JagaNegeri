package com.jaganegeri.ui.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jaganegeri.data.model.CorruptionCase
import com.jaganegeri.data.repository.CaseRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.Calendar

data class CalendarUiState(
    val isLoading: Boolean = false,
    val currentMonth: Int = Calendar.getInstance().get(Calendar.MONTH),
    val currentYear: Int = Calendar.getInstance().get(Calendar.YEAR),
    val datesWithEvents: Set<String> = emptySet(),
    val selectedDate: String = "",  // yyyy-MM-dd
    val eventsOnSelectedDate: List<CorruptionCase> = emptyList()
)

class CalendarViewModel(
    private val userId: String,
    private val caseRepository: CaseRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CalendarUiState())
    val uiState: StateFlow<CalendarUiState> = _uiState

    init {
        // Set selected date ke hari ini
        val cal = Calendar.getInstance()
        val today = String.format("%04d-%02d-%02d",
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH) + 1,
            cal.get(Calendar.DAY_OF_MONTH)
        )
        _uiState.value = _uiState.value.copy(selectedDate = today)
        loadMonth()
    }

    fun loadMonth() {
        val state = _uiState.value
        viewModelScope.launch {
            _uiState.value = state.copy(isLoading = true)
            val dates = caseRepository.getDatesWithEvents(userId, state.currentYear, state.currentMonth + 1)
            val events = if (state.selectedDate.isNotEmpty()) {
                caseRepository.getCasesByDate(userId, state.selectedDate)
            } else emptyList()

            _uiState.value = _uiState.value.copy(
                isLoading = false,
                datesWithEvents = dates,
                eventsOnSelectedDate = events
            )
        }
    }

    fun setMonthYear(month: Int, year: Int) {
        _uiState.value = _uiState.value.copy(currentMonth = month, currentYear = year)
        loadMonth()
    }

    fun selectDate(date: String) {
        _uiState.value = _uiState.value.copy(selectedDate = date)
        viewModelScope.launch {
            val events = caseRepository.getCasesByDate(userId, date)
            _uiState.value = _uiState.value.copy(eventsOnSelectedDate = events)
        }
    }

    fun previousMonth() {
        val state = _uiState.value
        var m = state.currentMonth - 1
        var y = state.currentYear
        if (m < 0) { m = 11; y-- }
        _uiState.value = state.copy(currentMonth = m, currentYear = y)
        loadMonth()
    }

    fun nextMonth() {
        val state = _uiState.value
        var m = state.currentMonth + 1
        var y = state.currentYear
        if (m > 11) { m = 0; y++ }
        _uiState.value = state.copy(currentMonth = m, currentYear = y)
        loadMonth()
    }
}
