package com.onefit.gymberapp.utils

sealed class Response<out T> {
    object Loading: Response<Nothing>()
    object InternetNotAvailable: Response<Nothing>()
    data class Success<out T>(val data: T) : Response<T>()
    data class Failure<out T>(val errorMessage: String? = null) : Response<T>()
}