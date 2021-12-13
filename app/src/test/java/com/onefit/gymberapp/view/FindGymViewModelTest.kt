package com.onefit.gymberapp.view

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import com.androidbytes.githubclient.utils.CoroutineTestRule
import com.onefit.gymberapp.model.GymInfo
import com.onefit.gymberapp.usecase.GetGymsUseCase
import com.onefit.gymberapp.utils.Response
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.junit.MockitoJUnitRunner
import java.net.UnknownHostException

@ExperimentalCoroutinesApi
@RunWith(MockitoJUnitRunner::class)
class FindGymViewModelTest {

    @get:Rule
    var coroutinesTestRule = CoroutineTestRule()

    @get:Rule
    val testInstantTaskExecutorRule: TestRule = InstantTaskExecutorRule()

    @Mock
    lateinit var getGymsUseCase: GetGymsUseCase

    @Mock
    lateinit var gymInfoListObserver: Observer<Response<List<GymInfo>>>

    @Mock
    lateinit var matchedGymInfoObserver: Observer<GymInfo>

    private lateinit var viewModel: FindGymViewModel

    @Before
    fun setUp() {
        viewModel = FindGymViewModel(getGymsUseCase, coroutinesTestRule.testDispatcherProvider)
    }

    @Test
    fun testFetchData_withInternetNotAvailable_shouldShowError() =
        coroutinesTestRule.testDispatcher.runBlockingTest {

            `when`(getGymsUseCase.getGyms(FindGymViewModel.CITY_NAME))
                .thenAnswer {
                    throw UnknownHostException()
                }

            viewModel.gymInfoListLiveData.observeForever(gymInfoListObserver)
            viewModel.getGyms()

            verify(gymInfoListObserver).onChanged(Response.InternetNotAvailable)
        }

    @Test
    fun testFetchData_withInternetAvailable_shouldShowData() =
        coroutinesTestRule.testDispatcher.runBlockingTest {

            val cityName = FindGymViewModel.CITY_NAME
            `when`(getGymsUseCase.getGyms(cityName))
                .then {
                    Response.Success(getGymInfoList())
                }

            viewModel.gymInfoListLiveData.observeForever(gymInfoListObserver)
            viewModel.getGyms()

            Mockito.inOrder(getGymsUseCase, gymInfoListObserver).apply {
                verify(gymInfoListObserver).onChanged(Response.Loading)
                verify(getGymsUseCase, Mockito.times(1)).getGyms(cityName)
                verify(gymInfoListObserver).onChanged(Response.Success(getGymInfoList()))
            }
        }

    @Test
    fun testSwipeRight_gymIsInTreatmentGroup_shouldShowMatchedInfo() =
        coroutinesTestRule.testDispatcher.runBlockingTest {

            val gymInfo = getGymInfoList().first()
            val cityName = FindGymViewModel.CITY_NAME
            `when`(getGymsUseCase.getGyms(cityName))
                .then {
                    Response.Success(getGymInfoList())
                }

            viewModel.gymInfoListLiveData.observeForever(gymInfoListObserver)
            viewModel.matchedGymInfoLiveData.observeForever(matchedGymInfoObserver)
            viewModel.getGyms()

            viewModel.swipe(gymInfo, true)
            verify(matchedGymInfoObserver).onChanged(gymInfo)
        }

    @Test
    fun testSwipeLeft_gymIsInTreatmentGroup_shouldNotShowMatchedInfo() =
        coroutinesTestRule.testDispatcher.runBlockingTest {

            val gymInfo = getGymInfoList().first()
            val cityName = FindGymViewModel.CITY_NAME
            `when`(getGymsUseCase.getGyms(cityName))
                .then {
                    Response.Success(getGymInfoList())
                }

            viewModel.gymInfoListLiveData.observeForever(gymInfoListObserver)
            viewModel.matchedGymInfoLiveData.observeForever(matchedGymInfoObserver)
            viewModel.getGyms()

            viewModel.swipe(gymInfo, false)
            verify(matchedGymInfoObserver, times(0)).onChanged(gymInfo)
        }

    private fun getGymInfoList() = listOf<GymInfo>(
        GymInfo(
            id = 16880,
            cityName = "AMS",
            name = "24Seven",
            slug = "24seven-amsterdam",
            reviewRating = 5.0f,
            reviewCount = 2,
            imageUrl = "https://edge.one.fit/image/partner/image/16849/e4fc0d74-ab03-43b4-aec1-873f444f7a3f.jpg",
            latitude = 52.348547,
            longitude = 4.97432,
            distance = null,
            isLastItem = true
        )
    )
}