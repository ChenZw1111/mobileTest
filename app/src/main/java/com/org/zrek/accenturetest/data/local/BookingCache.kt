package com.org.zrek.accenturetest.data.local

import android.content.Context
import com.google.gson.Gson
import com.org.zrek.accenturetest.model.BookingResponse
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

interface BookingCache {
    suspend fun saveBooking(booking: BookingResponse)
    suspend fun getBooking(): BookingResponse?
    suspend fun clearCache()
    suspend fun isDataExpired(): Boolean
}

class BookingCacheImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : BookingCache {
    private val sharedPreferences = context.getSharedPreferences("booking_cache", Context.MODE_PRIVATE)
    private val gson = Gson()
    private val BOOKING_KEY = "booking_data"

    override suspend fun saveBooking(booking: BookingResponse) {
        withContext(Dispatchers.IO) {
            val jsonString = gson.toJson(booking)
            sharedPreferences.edit()
                .putString(BOOKING_KEY, jsonString)
                .apply()
        }
    }

    override suspend fun getBooking(): BookingResponse? {
        return withContext(Dispatchers.IO) {
            val jsonString = sharedPreferences.getString(BOOKING_KEY, null)
            jsonString?.let {
                gson.fromJson(it, BookingResponse::class.java)
            }
        }
    }

    override suspend fun isDataExpired(): Boolean {
        return withContext(Dispatchers.IO) {
            val booking = getBooking() ?: return@withContext true
            
            val expiryTime = booking.expiryTime.toLongOrNull() ?: return@withContext true
            val currentTime = System.currentTimeMillis() / 1000 // 转换为秒
            
            currentTime >= expiryTime
        }
    }

    override suspend fun clearCache() {
        withContext(Dispatchers.IO) {
            sharedPreferences.edit().remove(BOOKING_KEY).apply()
        }
    }
} 