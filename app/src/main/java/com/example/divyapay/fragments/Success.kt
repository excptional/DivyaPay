package com.example.divyapay.fragments

import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.airbnb.lottie.LottieAnimationView
import com.example.divyapay.R


class Success : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_success, container, false)

            val mp = MediaPlayer.create(requireContext(), R.raw.success)
            mp.start()

        return view
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}