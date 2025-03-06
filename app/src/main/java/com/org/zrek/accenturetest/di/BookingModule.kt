package com.org.zrek.accenturetest.di


import android.content.Context
import com.org.zrek.accenturetest.data.BookingDataManager
import com.org.zrek.accenturetest.data.local.BookingCache
import com.org.zrek.accenturetest.data.local.BookingCacheImpl
import com.org.zrek.accenturetest.service.BookingService
import com.org.zrek.accenturetest.service.MockBookingService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object BookingModule {
    @Provides
    @Singleton
    fun provideBookingService(@ApplicationContext context: Context): BookingService {
        return MockBookingService(context)
    }

    @Provides
    @Singleton
    fun provideBookingCache(@ApplicationContext context: Context): BookingCache {
        return BookingCacheImpl(context)
    }

    @Provides
    @Singleton
    fun provideBookingDataManager(
        bookingService: BookingService,
        bookingCache: BookingCache
    ): BookingDataManager {
        return BookingDataManager(bookingService, bookingCache)
    }
} 