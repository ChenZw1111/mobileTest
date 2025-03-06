package com.org.zrek.accenturetest.service

import android.content.Context
import com.google.gson.Gson
import com.org.zrek.accenturetest.model.BookingResponse
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import javax.inject.Inject

interface BookingService {
    suspend fun getBookingDetails(): BookingResponse
}

class MockBookingService @Inject constructor(
    @ApplicationContext private val context: Context
) : BookingService {
    private val gson = Gson()
    
    override suspend fun getBookingDetails(): BookingResponse {
        // 模拟网络延迟
        delay(1000)
        
        return withContext(Dispatchers.IO) {
            val jsonString = readJsonFromAssets()
            gson.fromJson(jsonString, BookingResponse::class.java)
        }
    }
    
    private fun readJsonFromAssets(): String {
        return context.assets.open("booking.json").bufferedReader().use { it.readText() }
    }
} 