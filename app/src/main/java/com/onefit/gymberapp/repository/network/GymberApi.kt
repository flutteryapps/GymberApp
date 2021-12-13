package com.onefit.gymberapp.repository.network

import com.onefit.gymberapp.repository.network.model.GymApiResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface GymberApi {

    @GET("v2/en-nl/partners/city/{cityName}")
    suspend fun getGyms(@Path("cityName") cityName: String): Response<GymApiResponse>
}