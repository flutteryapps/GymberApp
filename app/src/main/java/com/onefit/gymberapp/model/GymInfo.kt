package com.onefit.gymberapp.model

data class GymInfo(
    val id: Int,
    val cityName: String,
    val name: String,
    val slug: String,
    val reviewRating: Float?,
    val reviewCount: Int?,
    val imageUrl: String?,
    val latitude: Double?,
    val longitude: Double?,
    var distance: Float? = null,
    var isLastItem: Boolean = false
)
