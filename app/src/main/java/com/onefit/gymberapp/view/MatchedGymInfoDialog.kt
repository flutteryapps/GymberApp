package com.onefit.gymberapp.view

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.RelativeLayout
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import com.bumptech.glide.Glide
import com.onefit.gymberapp.R
import com.onefit.gymberapp.databinding.DialogMatchedGymInfoBinding
import com.onefit.gymberapp.model.GymInfo

class MatchedGymInfoDialog : DialogFragment() {

    companion object {

        const val TAG = "MatchedGymInfoDialog"

        private const val GYM_NAME = "gym_name"
        private const val GYM_IMAGE = "gym_image"

        @JvmStatic
        fun newInstance(gymInfo: GymInfo) = MatchedGymInfoDialog().apply {
            arguments = Bundle().apply {
                putString(GYM_NAME, gymInfo.name)
                putString(GYM_IMAGE, gymInfo.imageUrl)
            }
        }
    }

    private lateinit var binding: DialogMatchedGymInfoBinding

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val root = RelativeLayout(activity)
        root.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )

        val dialog = Dialog(requireActivity(), R.style.CustomDialog)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(root)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )

        return dialog
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DialogMatchedGymInfoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        arguments?.let {
            val gymName = it.getString(GYM_NAME)
            val gymImageUrl = it.getString(GYM_IMAGE)

            binding.matchSubtitleTextView.text = "It's hard to beat a person who never gives up."
            Glide.with(requireContext()).load(gymImageUrl).into(binding.gymImageView)

        } ?: run {
            dismissAllowingStateLoss()
        }


        binding.keepSwipingButton.setOnClickListener {
            dismissAllowingStateLoss()
        }
    }

    override fun show(manager: FragmentManager, tag: String?) {
        manager
            .beginTransaction()
            .add(this, tag)
            .commitAllowingStateLoss()
    }
}
