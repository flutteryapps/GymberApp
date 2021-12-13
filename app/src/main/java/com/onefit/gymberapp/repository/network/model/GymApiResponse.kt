package com.onefit.gymberapp.repository.network.model

data class GymApiResponse(
    val data: List<GymRemote>?
)

data class GymRemote(
    val id: Int,
    val name: String,
    val slug: String,
    val review_rating: Float?,
    val review_count: Int?,
    val header_image: Map<String, String>,
    val locations: List<GymAddress>
)

data class GymAddress(
    val latitude: Double,
    val longitude: Double
)
