package com.org.zrek.accenturetest.data

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import android.util.Log
import com.org.zrek.accenturetest.data.local.BookingCache
import com.org.zrek.accenturetest.model.BookingResponse
import com.org.zrek.accenturetest.model.Segment
import com.org.zrek.accenturetest.service.BookingService
import com.org.zrek.accenturetest.util.Result
import javax.inject.Singleton
import com.org.zrek.accenturetest.util.Logger

@Singleton
class BookingDataManager @Inject constructor(
    private val bookingService: BookingService,
    private val bookingCache: BookingCache
) {
    private val _bookingFlow = MutableStateFlow<Result<BookingResponse>?>(null)
    val bookingFlow: StateFlow<Result<BookingResponse>?> = _bookingFlow.asStateFlow()

    suspend fun loadFromCache() {
        try {
            bookingCache.getBooking()?.let { cachedBooking ->
                if (!bookingCache.isDataExpired()) {
                    _bookingFlow.emit(Result.Success(cachedBooking))
                }
            }
        } catch (e: Exception) {
            Log.e("BookingDataManager", "Cache error", e)
        }
    }

    suspend fun fetchBookingDetails(forceRefresh: Boolean = false) {
        Logger.d("fetchBookingDetails called with forceRefresh: $forceRefresh")
        try {
            // 先发射 null 表示加载状态
            if (forceRefresh) {
                _bookingFlow.emit(null)
            }

            if (!forceRefresh) {
                val isExpired = bookingCache.isDataExpired()
                if (!isExpired) {
                    bookingCache.getBooking()?.let { cachedBooking ->
                        Logger.d("Using cache data")
                        _bookingFlow.emit(Result.Success(cachedBooking))
                        return
                    }
                }
            }

            Logger.d("Fetching new data from service")
            val response = bookingService.getBookingDetails()
            Logger.d("Fetched new data: $response")
            
            bookingCache.saveBooking(response)
            
            _bookingFlow.emit(Result.Success(response))
            Logger.d("Emitted success result")
            
        } catch (e: Exception) {
            Logger.e("Error fetching data", e)
            _bookingFlow.emit(Result.Failure(e))
        }
    }

    suspend fun clearCache() {
        bookingCache.clearCache()
    }

    // 获取剩余有效时间（秒）
    suspend fun getRemainingValidTime(): Long? {
        val booking = bookingCache.getBooking() ?: return null
        val expiryTime = booking.expiryTime.toLongOrNull() ?: return null
        val currentTime = System.currentTimeMillis() / 1000
        return (expiryTime - currentTime).coerceAtLeast(0)
    }

    // 修改获取列表数据的方法
    suspend fun getBookingList(): List<Segment> {
        return try {
            val response = bookingService.getBookingDetails()
            Logger.d("Fetched booking data: $response") // 打印完整响应到控制台

            // 从 response 中提取 segments 列表
            response.segments.also { segments ->
                Logger.d("Extracted segments: $segments") // 打印提取的航段信息
            }
        } catch (e: Exception) {
            Logger.e("Error fetching booking data: ${e.message}") // 打印错误信息
            emptyList()
        }
    }
} 