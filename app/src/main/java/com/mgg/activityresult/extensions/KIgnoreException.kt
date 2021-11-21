package com.mgg.activityresult.extensions

import android.util.Log

sealed class Try<out T> {
    companion object {
        operator fun <T> invoke(body: () -> T): Try<T> {
            return try {
                Success(body())
            } catch (e: Exception) {
                Failure(e)
            }
        }
    }

    abstract fun isSuccess(): Boolean
    abstract fun isFailure(): Boolean
}

data class Success<out T>(val value: T) : Try<T>() {
    override fun isSuccess(): Boolean = true
    override fun isFailure(): Boolean = false
}

data class Failure<out T>(val e: Throwable) : Try<T>() {
    override fun isSuccess(): Boolean = false
    override fun isFailure(): Boolean = true
}

/*
sealed class Try<out T> {
    abstract fun get(): T
    abstract fun getOrElse(default: @UnsafeVariance T): T
    abstract fun orElse(default: Try<@UnsafeVariance T>): Try<T>
}

data class Success<out T>(val value: T) : Try<T>() {
    override fun getOrElse(default: @UnsafeVariance T): T = value
    override fun get() = value
    override fun orElse(default: Try<@UnsafeVariance T>): Try<T> = this
}

data class Failure<out T>(val e: Throwable) : Try<T>() {
    override fun getOrElse(default: @UnsafeVariance T): T = default
    override fun get(): T = throw e
    override fun orElse(default: Try<@UnsafeVariance T>): Try<T> = default
}
*/


fun <T> ignoreException(body: () -> T): T? {
    return try {
        body()
    } catch (e: Exception) {
        e.message?.let { Log.e("ignoreException", it) }
        return null
    }
}