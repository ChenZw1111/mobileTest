package com.org.zrek.accenturetest.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.org.zrek.accenturetest.data.BookingDataManager
import com.org.zrek.accenturetest.model.BookingResponse
import com.org.zrek.accenturetest.util.Result
import com.org.zrek.accenturetest.util.Logger
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BookingViewModel @Inject constructor(
    private val bookingDataManager: BookingDataManager
) : ViewModel() {
    
    private val _uiState = MutableStateFlow<BookingUiState>(BookingUiState.Loading)
    val uiState: StateFlow<BookingUiState> = _uiState.asStateFlow()
    
    private val _remainingTime = MutableStateFlow<Long?>(null)
    val remainingTime: StateFlow<Long?> = _remainingTime.asStateFlow()
    
    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    init {
        viewModelScope.launch {
            bookingDataManager.bookingFlow.collect { result ->
                Logger.d("Received new result: $result")
                
                // 更新 UI 状态
                _uiState.value = when (result) {
                    is Result.Success -> {
                        Logger.d("Success state")
                        BookingUiState.Success(result.value)
                    }
                    is Result.Failure -> {
                        Logger.e("Error state: ${result.exception.message}", result.exception)
                        BookingUiState.Error(result.exception)
                    }
                    null -> {
                        Logger.d("Loading state")
                        BookingUiState.Loading
                    }
                }

                // 只有在非加载状态时才重置刷新状态
                if (result != null) {
                    _isRefreshing.value = false
                    Logger.d("isRefreshing set to false")
                }
            }
        }
        
        loadInitialData()
        
        // 启动过期时间检查
//        startExpiryTimeCheck()
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            try {
                bookingDataManager.loadFromCache()
                
                if (bookingDataManager.bookingFlow.value == null) {
                    Logger.d("No cache data, fetching from server")
                    bookingDataManager.fetchBookingDetails(forceRefresh = false)
                }
            } catch (e: Exception) {
                Logger.e("Error loading initial data", e)
                _uiState.value = BookingUiState.Error(e)
            }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            Logger.d("Starting refresh...")
            _isRefreshing.value = true
            try {
                bookingDataManager.fetchBookingDetails(forceRefresh = true)
                Logger.d("Refresh request sent")
            } catch (e: Exception) {
                Logger.e("Refresh failed", e)
                _uiState.value = BookingUiState.Error(e)
                _isRefreshing.value = false
            }
        }
    }

    fun retry() {
        viewModelScope.launch {
            _uiState.value = BookingUiState.Loading
            fetchBooking(forceRefresh = true)
        }
    }

    private fun startExpiryTimeCheck() {
        viewModelScope.launch {
            while (isActive) {
                _remainingTime.value = bookingDataManager.getRemainingValidTime()
                if (_remainingTime.value == 0L) {
                    refresh()
                }
                delay(1000)
            }
        }
    }

    private fun fetchBooking(forceRefresh: Boolean = false) {
        viewModelScope.launch {
            try {
                bookingDataManager.fetchBookingDetails(forceRefresh)
            } catch (e: Exception) {
                _uiState.value = BookingUiState.Error(e)
            }
        }
    }
}

sealed class BookingUiState {
    object Loading : BookingUiState()
    data class Success(val booking: BookingResponse) : BookingUiState()
    data class Error(val exception: Throwable) : BookingUiState()
} 