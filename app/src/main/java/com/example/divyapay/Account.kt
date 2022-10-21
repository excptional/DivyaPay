package com.example.divyapay

import android.content.Context
import android.os.Bundle
import android.speech.tts.TextToSpeech
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import com.example.divyapay.databinding.FragmentAccountBinding
import java.util.*

class Account : Fragment() {

    private lateinit var tts: TextToSpeech
    private lateinit var binding: FragmentAccountBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentAccountBinding.inflate(layoutInflater)

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true){
            override fun handleOnBackPressed() {
                requireActivity().supportFragmentManager.beginTransaction().replace(R.id.frame_layout, Home()).commit()
            }
        })

        val name: String = binding.nameAccount.text.toString()
        val number: String = spaceGiving(binding.numberAccount.text.toString())
        val accBalance: String = binding.accBalanceAccount.text.toString()

        textToSpeech(
            requireActivity().applicationContext, "Welcome to account page        "
                    + "Account holder name is $name        ."
                    + "Registered mobile number is $number        ."
                    + "Current account balance is $accBalance        ."
                    + "double tap to use voice assistance        ."
                    + "swipe left to return at the home page        ."
                    + "swipe right to repeat your account details        ."
        )

        return inflater.inflate(R.layout.fragment_account, container, false)
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

    private fun spaceGiving(s: String): String {
        var temp = ""
        for (i in s.indices) {
            temp += if (s[i] == '0') {
                "zero "
            } else {
                s[i] + " "
            }
        }
        return temp
    }

    override fun onDestroy() {
        if (tts.isSpeaking) {
            tts.stop()
            tts.shutdown()
        }
        super.onDestroy()
    }
}