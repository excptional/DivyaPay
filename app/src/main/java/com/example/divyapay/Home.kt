package com.example.divyapay

import android.content.Context
import android.os.Bundle
import android.speech.tts.TextToSpeech
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import java.util.*

class Home : Fragment() {

    private lateinit var tts : TextToSpeech

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true){
            override fun handleOnBackPressed() {
                requireActivity().finish()
            }
        })

        textToSpeech(requireActivity().applicationContext, "Welcome to home page        ."
                + "swipe up to get your account details        ."
                + "swipe down to get your latest transaction details         ."
                + "swipe right to make payment         ."
                + "double tap to use voice assistance         ."
                + "swipe left to repeat the instructions")

        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    private fun textToSpeech(context: Context, speech: String) {
        tts = TextToSpeech(
            context
        ) {
            if (it == TextToSpeech.SUCCESS) {
                tts.language = Locale.ENGLISH
                tts.setSpeechRate(0.6f)
                tts.speak(speech, TextToSpeech.QUEUE_ADD, null)
            }
        }
    }

    override fun onDestroy() {
        if(tts.isSpeaking){
            tts.stop()
            tts.shutdown()
        }
        super.onDestroy()
    }

}