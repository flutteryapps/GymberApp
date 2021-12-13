package com.onefit.gymberapp.usecase

import com.onefit.gymberapp.model.GymInfo
import com.onefit.gymberapp.repository.network.GymberApi
import com.onefit.gymberapp.utils.DeviceUtility
import com.onefit.gymberapp.utils.Response
import com.onefit.gymberapp.utils.toDecimalFormat
import javax.inject.Inject

class GetGymsUseCase @Inject constructor(
    private val gymberApi: GymberApi,
    private val deviceUtility: DeviceUtility
) {

    suspend fun getGyms(cityName: String): Response<List<GymInfo>> {

        val response = gymberApi.getGyms(cityName)
        val gymApiResponse = response.body()
        if (response.isSuccessful && gymApiResponse != null) {

            val gymInfoList = gymApiResponse.data?.map {
                val gymAddress = it.locations.firstOrNull()
                GymInfo(it.id, cityName, it.name, it.slug, it.review_rating?.toDecimalFormat(), it.review_count,
                    getGymImageUrl(it.header_image), gymAddress?.latitude, gymAddress?.longitude)
            } ?: emptyList()

            return Response.Success(gymInfoList)
        } else {
            return Response.Failure(response.message())
        }
    }

    private fun getGymImageUrl(imageUrls: Map<String, String>): String? {
        val deviceDensity = deviceUtility.getDeviceDensity()
        return if (deviceDensity != null) {
            imageUrls[deviceDensity.density]
        } else {
            imageUrls["orig"]
        }
    }
}