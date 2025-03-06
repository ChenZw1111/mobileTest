package com.org.zrek.accenturetest.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.org.zrek.accenturetest.data.BookingDataManager
import com.org.zrek.accenturetest.model.Segment
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ListViewModel @Inject constructor(
    private val bookingDataManager: BookingDataManager
) : ViewModel() {
    
    private val _segments = MutableStateFlow<List<Segment>>(emptyList())
    val segments: StateFlow<List<Segment>> = _segments.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun loadData() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val segmentList = bookingDataManager.getBookingList()
                _segments.value = segmentList
                println("Loaded segments: $segmentList") // 打印加载的航段数据
            } catch (e: Exception) {
                println("Error loading data: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }
} 