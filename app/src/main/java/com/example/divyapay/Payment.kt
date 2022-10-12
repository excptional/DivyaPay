package com.example.divyapay

import android.content.Context
import android.os.Bundle
import android.speech.tts.TextToSpeech
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import java.util.*

class Payment : Fragment() {

    private lateinit var tts : TextToSpeech

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        textToSpeech(requireActivity().applicationContext, "Welcome to payment page        ."
                + "Here you have to provide the name of the receiver , the amount you want to pay,          ."
                + " that transaction i d where you want to send the money and a description( what is not compulsory)        ."
                + "swipe right to make payment        ."
                + "double tap to use voice assistance        ."
                + "swipe left to return at the home page      .")

        return inflater.inflate(R.layout.fragment_payment, container, false)
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