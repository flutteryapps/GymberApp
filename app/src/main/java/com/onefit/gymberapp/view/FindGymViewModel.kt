package com.onefit.gymberapp.view

import android.location.Location
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.onefit.gymberapp.model.GymInfo
import com.onefit.gymberapp.usecase.GetGymsUseCase
import com.onefit.gymberapp.utils.DispatcherProvider
import com.onefit.gymberapp.utils.Response
import com.onefit.gymberapp.utils.toDecimalFormat
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.UnknownHostException
import javax.inject.Inject

@HiltViewModel
class FindGymViewModel @Inject constructor(
    private val getGymsUseCase: GetGymsUseCase,
    private val dispatcherProvider: DispatcherProvider
) : ViewModel() {

    private var currentIndex = 0
    private val gymInfoList = mutableListOf<GymInfo>()
    private val _gymInfoListLiveData = MutableLiveData<Response<List<GymInfo>>>()

    val gymInfoListLiveData: LiveData<Response<List<GymInfo>>>
        get() = _gymInfoListLiveData

    private val _matchedGymInfoLiveData = MutableLiveData<GymInfo>()
    val matchedGymInfoLiveData: LiveData<GymInfo>
        get() = _matchedGymInfoLiveData

    var userLocation: Location? = null
        set(value) {
            if (value != null) {
                field = value
            }
        }

    fun getGyms() {
        currentIndex = 0
        _gymInfoListLiveData.value = Response.Loading

        viewModelScope.launch(dispatcherProvider.io()) {
            try {
                when (val response = getGymsUseCase.getGyms(CITY_NAME)) {
                    is Response.Success -> {
                        gymInfoList.clear()
                        gymInfoList.addAll(response.data)
                        withContext(dispatcherProvider.main()) {
                            updateCards(5)
                        }
                    }

                    is Response.Failure -> {
                        _gymInfoListLiveData.postValue(Response.Failure(response.errorMessage))
                        Log.e(TAG, response.errorMessage ?: "Error")
                    }
                }
            } catch (e: UnknownHostException) {
                _gymInfoListLiveData.postValue(Response.InternetNotAvailable)
            } catch (e: Exception) {
                _gymInfoListLiveData.postValue(Response.Failure(e.localizedMessage))
            }
        }
    }

    fun swipe(gymInfo: GymInfo, swipeRight: Boolean) {
        updateCards(1)
        if (swipeRight && gymInfo.id % 20 == 0) {
            _matchedGymInfoLiveData.value = gymInfo
        }
    }

    private fun updateCards(size: Int) {
        if (gymInfoList.size == currentIndex) return
        val mutableList = mutableListOf<GymInfo>()
        for (i in 0 until size) {

            if (gymInfoList.size == currentIndex) break

            mutableList.add(gymInfoList[currentIndex++].apply {
                distance = calculateDistanceBetweenGymAndUser(this)
                isLastItem = gymInfoList.size == currentIndex
            })
        }

        _gymInfoListLiveData.value = Response.Success(mutableList)
    }

    /**
     * We have used Google's default API to calculate distance between two locations
     * which might not be accurate due to unavailability of path
     *
     * TODO: Use Google Places API for accurate distance
     */
    private fun calculateDistanceBetweenGymAndUser(gymInfo: GymInfo): Float? {
        if (gymInfo.latitude == null || gymInfo.longitude == null || userLocation == null) return null

        var distanceInMeters: Float? = null
        userLocation?.let { userLocation ->

            val gymLocation = Location(gymInfo.name)
            gymLocation.latitude = gymInfo.latitude
            gymLocation.longitude = gymInfo.longitude

            distanceInMeters = userLocation.distanceTo(gymLocation)
        }

        return distanceInMeters?.let {
            (it / 1000) // Converting into kilometer
                .toDecimalFormat()
        }
    }

    companion object {
        private const val TAG = "FindGymViewModel"

        const val CITY_NAME = "AMS"
    }
}