package com.onefit.gymberapp.view

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.location.Location
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.constraintlayout.motion.widget.TransitionAdapter
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.onefit.gymberapp.R
import com.onefit.gymberapp.databinding.ActivityFindGymBinding
import com.onefit.gymberapp.databinding.ItemGymInfoBinding
import com.onefit.gymberapp.location.LocationListener
import com.onefit.gymberapp.model.GymInfo
import com.onefit.gymberapp.utils.LocationUtility
import com.onefit.gymberapp.utils.Response
import com.onefit.gymberapp.utils.gone
import com.onefit.gymberapp.utils.setTextValueOrHideView
import com.onefit.gymberapp.utils.visible
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class FindGymActivity : AppCompatActivity() {

    private lateinit var viewModel: FindGymViewModel
    private lateinit var binding: ActivityFindGymBinding
    private lateinit var locationListener: LocationListener

    @Inject
    lateinit var locationUtility: LocationUtility

    private val locationPermissionRequest =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
            if (locationUtility.checkLocationPermissionsGranted()) {
                enableLocationIfNotEnabled()
            } else if (locationUtility.shouldShowRequestPermissionRationale(
                    this,
                    LOCATION_PERMISSIONS
                )
            ) {
                showLocationRationaleDialog()
            } else {
                fetchGymsWithoutLocation()
                trackUserBehaviorWrtLocation("User denied location permission! Fetching gyms without distance.")
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFindGymBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initListeners()

        viewModel = ViewModelProvider(this)[FindGymViewModel::class.java]
        viewModel.gymInfoListLiveData.observe(this, Observer { showGymsInStack(it) })
        viewModel.matchedGymInfoLiveData.observe(this, Observer { showMatchedGymInfo(it) })
    }

    private fun showMatchedGymInfo(gymInfo: GymInfo) {
        MatchedGymInfoDialog.newInstance(gymInfo)
            .show(supportFragmentManager, MatchedGymInfoDialog.TAG)
    }

    private fun initListeners() {
        binding.likeButton.setOnClickListener {
            if (binding.gymStackLayout.childCount > 1) {
                binding.gymStackLayout.getChildAt(binding.gymStackLayout.childCount - 1)
                    .findViewById<MotionLayout>(R.id.motion_layout)
                    .transitionToState(R.id.endLike)
            }
        }
        binding.unlikeButton.setOnClickListener {
            if (binding.gymStackLayout.childCount > 1) {
                binding.gymStackLayout.getChildAt(binding.gymStackLayout.childCount - 1)
                    .findViewById<MotionLayout>(R.id.motion_layout)
                    .transitionToState(R.id.endUnlike)
            }
        }

        locationListener = LocationListener(this, lifecycle) { location: Location? ->
            fetchGymsWithLocation(location)
        }
    }

    override fun onStart() {
        super.onStart()
        checkLocationPermissionGranted()
    }

    private fun showGymsInStack(response: Response<List<GymInfo>>) {
        when (response) {
            is Response.Success -> {
                hideLoading()
                showGyms(response.data)
            }

            is Response.Failure -> {
                hideLoading()
                showError(response.errorMessage)
            }

            is Response.Loading -> {
                showLoading()
            }

            is Response.InternetNotAvailable -> {
                hideLoading()
                showError(getString(R.string.lbl_internet_connection_not_available))
            }
        }
    }

    private fun showError(errorMessage: String?) {

        binding.gymStackLayout.gone()
        binding.likeButton.gone()
        binding.unlikeButton.gone()

        binding.errorTextView.text = if (TextUtils.isEmpty(errorMessage)) {
            getString(R.string.msg_sever_error_try_again)
        } else {
            errorMessage
        }
        binding.retryButton.setOnClickListener {
            fetchGymsWithoutLocation()
        }

        binding.errorLayout.visible()
    }

    private fun showLoading() {
        binding.progressBar.visible()
        binding.gymStackLayout.gone()
        binding.likeButton.gone()
        binding.unlikeButton.gone()
        binding.errorLayout.gone()
    }

    private fun hideLoading() {
        binding.progressBar.gone()
    }

    private fun showGyms(gymInfoList: List<GymInfo>) {
        binding.gymStackLayout.visible()
        binding.likeButton.visible()
        binding.unlikeButton.visible()
        gymInfoList.forEach { gymInfo ->
            addGymInfoToStack(gymInfo)
        }
    }

    private fun addGymInfoToStack(gymInfo: GymInfo) {
        val gymInfoBinding = ItemGymInfoBinding.inflate(layoutInflater)

        gymInfoBinding.gymNameTextView.text = gymInfo.name
        gymInfo.reviewCount?.let {
            gymInfoBinding.gymReviewCountTextView.text = "($it)"
        }

        gymInfo.reviewRating?.let {
            gymInfoBinding.gymRatingBar.rating = it
        }

        val distance = gymInfo.distance?.let { "$it kms" }
        gymInfoBinding.gymDistanceTextView.setTextValueOrHideView(distance)

        Glide.with(this@FindGymActivity)
            .load(gymInfo.imageUrl)
            .into(gymInfoBinding.gymImageView)

        gymInfoBinding.motionLayout.setTransitionListener(object : TransitionAdapter() {
            override fun onTransitionCompleted(motionLayout: MotionLayout, currentId: Int) {
                when (currentId) {
                    R.id.endLike,
                    R.id.endUnlike -> {
                        motionLayout.progress = 0f
                        motionLayout.transitionToStart()

                        binding.gymStackLayout.removeView(
                            binding.gymStackLayout.findViewWithTag(
                                gymInfo.id
                            )
                        )

                        viewModel.swipe(gymInfo, currentId == R.id.endLike)
                    }
                }
            }

            override fun onTransitionChange(
                motionLayout: MotionLayout?,
                startId: Int,
                endId: Int,
                progress: Float
            ) {
                when (endId) {
                    R.id.endLike -> {
                        gymInfoBinding.gymLikeTextView.alpha = progress * 10f
                        gymInfoBinding.gymNotLikeTextView.alpha = 0f
                    }
                    R.id.endUnlike -> {
                        gymInfoBinding.gymNotLikeTextView.alpha = progress * 10f
                        gymInfoBinding.gymLikeTextView.alpha = 0f
                    }

                    R.id.start -> {
                        gymInfoBinding.gymLikeTextView.alpha = progress
                        gymInfoBinding.gymNotLikeTextView.alpha = progress
                    }
                }

                if (progress == 0f) {
                    gymInfoBinding.gymLikeTextView.alpha = 0f
                    gymInfoBinding.gymNotLikeTextView.alpha = 0f
                }
            }
        })

        if (gymInfo.isLastItem) {
            gymInfoBinding.motionLayout.isInteractionEnabled = false
        }

        gymInfoBinding.root.tag = gymInfo.id
        binding.gymStackLayout.addView(gymInfoBinding.root, 0)

        // gynInfoBinding.motionLayout.progress = 1f
        // gynInfoBinding.motionLayout.setTransition(R.id.start, R.id.endUnlike)
    }

    private fun checkLocationPermissionGranted() {
        if (locationUtility.checkLocationPermissionsGranted()) {
            enableLocationIfNotEnabled()
        } else {
            locationPermissionRequest.launch(LOCATION_PERMISSIONS)
        }
    }

    private fun showLocationRationaleDialog() {

        AlertDialog.Builder(this)
            .setTitle(R.string.msg_location_permission_needed)
            .setMessage(R.string.msg_location_permission_rationale_retry)
            .setPositiveButton(getString(R.string.lbl_okay)) { _, _ ->
                locationPermissionRequest.launch(LOCATION_PERMISSIONS)
            }
            .create()
            .show()
    }

    private fun nudgeUserToEnableLocation() {
        locationUtility.enableLocation(this, LOCATION_SETTING_REQUEST) { locationEnabled ->
            if (locationEnabled) {
                locationListener.getLocation()
            } else {
                fetchGymsWithoutLocation()
                trackUserBehaviorWrtLocation("User didn't enable location! Fetching gyms without distance.")
            }
        }
    }

    private fun enableLocationIfNotEnabled() {
        if (locationUtility.checkLocationEnabled()) {
            locationListener.getLocation()
        } else {
            nudgeUserToEnableLocation()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            LOCATION_SETTING_REQUEST -> {
                if (resultCode == Activity.RESULT_OK) {
                    locationListener.getLocation()
                } else {
                    fetchGymsWithoutLocation()
                    trackUserBehaviorWrtLocation("User didn't enable location! Fetching gyms without distance.")
                }
            }
        }
    }

    private fun fetchGymsWithoutLocation() {
        fetchGyms()
    }

    private fun fetchGymsWithLocation(location: Location?) {
        viewModel.userLocation = location
        fetchGyms()
    }

    private fun fetchGyms() {
        if (binding.gymStackLayout.childCount == 0) {
            viewModel.getGyms()
        }
    }

    private fun trackUserBehaviorWrtLocation(message: String) {
        Log.d(TAG, message)
    }

    companion object {
        private const val TAG = "FindGymActivity"

        private const val LOCATION_SETTING_REQUEST = 100
        private val LOCATION_PERMISSIONS = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    }
}