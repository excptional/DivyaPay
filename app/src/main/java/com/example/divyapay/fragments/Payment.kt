package com.example.divyapay.fragments

import android.content.Context
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.divyapay.R
import com.example.divyapay.db.AppViewModel
import com.google.firebase.ml.naturallanguage.FirebaseNaturalLanguage
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslateLanguage
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslator
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslatorOptions
import java.util.*

class Payment : Fragment() {

    private lateinit var tts: TextToSpeech
    private lateinit var translator: FirebaseTranslator
    private lateinit var selectedLang: String
    private lateinit var appViewModel: AppViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    requireActivity().supportFragmentManager.beginTransaction()
                        .replace(R.id.frame_layout, Home()).commit()
                }
            })

        appViewModel = ViewModelProvider(requireActivity())[AppViewModel::class.java]
        appViewModel.userdata.observe(viewLifecycleOwner) {
            appViewModel.fetchAccountDetails(it!!)
            appViewModel.accDetails.observe(viewLifecycleOwner) {
                selectedLang = it[6]
            }
            if (selectedLang != "English") {
                prepareTranslateModel()
            }

            if (selectedLang == "English") {
                textToSpeech(
                    requireContext(), "Welcome to payment page        ."
                            + "Here you have to provide the name of the receiver , the amount you want to pay,          ."
                            + "that phone number where you want to send the money and a description( what is not compulsory)        ."
                            + "To make payment swipe right              ."
                            + "To use the voice assistance double tap to the screen             ."
                            + "To return at the home page  swipe left           ."
                )
            } else {
                textToSpeech(
                    requireContext(), "Welcome to payment page        ."
                )
                textToSpeech(
                    requireContext(),
                    "Here you have to provide the name of the receiver , have to provide the amount you want to pay,          ."
                )
                textToSpeech(
                    requireContext(),
                    " have to provide that phone number where you want to send the money and a description which is not compulsory        ."
                )
                textToSpeech(
                    requireContext(), "swipe right  To make payment       ."
                )
                textToSpeech(
                    requireContext(), "To use voice assistance double tap to the screen      ."
                )
                textToSpeech(
                    requireContext(), "To return at the home page  swipe left    ."
                )
            }

        }


        return inflater.inflate(R.layout.fragment_payment, container, false)
    }

    private fun textToSpeech(context: Context, speech: String) {
        if (selectedLang == "English") {
            tts = TextToSpeech(
                context
            ) {
                if (it == TextToSpeech.SUCCESS) {
                    tts.language = Locale.ENGLISH
                    tts.setSpeechRate(0.6f)
                    tts.speak(speech, TextToSpeech.QUEUE_ADD, null)
                }
            }
        } else {
            tts = TextToSpeech(
                context
            ) {
                if (it == TextToSpeech.SUCCESS) {
                    translator.translate(speech).addOnSuccessListener {
                        if (selectedLang == "Hindi")
                            tts.language = Locale.forLanguageTag("hin")
                        else tts.language = Locale.forLanguageTag("bn_IN")
                        tts.setSpeechRate(0.6f)
                        tts.speak(it.toString(), TextToSpeech.QUEUE_ADD, null)
                    }
                        .addOnFailureListener {
                            Toast.makeText(requireContext(), it.toString(), Toast.LENGTH_SHORT)
                                .show()
                        }
                }
            }
        }
    }

    private fun prepareTranslateModel() {
        if (selectedLang == "Hindi") {
            val options = FirebaseTranslatorOptions.Builder()
                .setSourceLanguage(FirebaseTranslateLanguage.EN)
                .setTargetLanguage(FirebaseTranslateLanguage.HI)
                .build()
            translator = FirebaseNaturalLanguage.getInstance().getTranslator(options)
        } else {
            val options = FirebaseTranslatorOptions.Builder()
                .setSourceLanguage(FirebaseTranslateLanguage.EN)
                .setTargetLanguage(FirebaseTranslateLanguage.BN)
                .build()
            translator = FirebaseNaturalLanguage.getInstance().getTranslator(options)
        }


        translator.downloadModelIfNeeded()
            .addOnSuccessListener {
            }
            .addOnFailureListener {
                Toast.makeText(
                    requireContext(),
                    "Language models downloading, please wait",
                    Toast.LENGTH_SHORT
                )
                    .show()
            }
    }

    override fun onDestroy() {
        if (tts.isSpeaking) {
            tts.stop()
            tts.shutdown()
        }
        super.onDestroy()
    }
}