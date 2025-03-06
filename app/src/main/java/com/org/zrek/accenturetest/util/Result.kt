package com.org.zrek.accenturetest.util

sealed class Result<out T> {
    data class Success<T>(val value: T) : Result<T>()
    data class Failure(val exception: Throwable) : Result<Nothing>()
} 