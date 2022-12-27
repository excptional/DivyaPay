package com.example.divyapay.fragments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.tts.TextToSpeech
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.lifecycle.ViewModelProvider
import com.example.divyapay.R
import com.example.divyapay.auth.AuthenticationActivity
import com.example.divyapay.db.AppViewModel
import com.example.divyapay.db.Response
import com.google.firebase.ml.naturallanguage.FirebaseNaturalLanguage
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslateLanguage
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslator
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslatorOptions
import java.util.*

class Home : Fragment() {

    private lateinit var tts: TextToSpeech
    private lateinit var translator: FirebaseTranslator
    private lateinit var appViewModel: AppViewModel
    private var selectedLang: String = "English"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    requireActivity().finish()
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
            textToSpeech(
                requireContext(), "Welcome to home page             ."
            )
            textToSpeech(
                requireContext(), "To get your account details Swipe up            ."
            )
            textToSpeech(
                requireContext(), "To get your latest transaction details Swipe down           ."
            )
            textToSpeech(
                requireContext(), "To make payment Swipe right          ."
            )
            textToSpeech(
                requireContext(), "To use voice assistance  Double tap              ."
            )
            textToSpeech(
                requireContext(), "To repeat this instructions swipe left"
            )
        }

        return inflater.inflate(R.layout.fragment_home, container, false)
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
                        tts.speak(it, TextToSpeech.QUEUE_ADD, null)
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
        translator = if (selectedLang == "Hindi") {
            val options = FirebaseTranslatorOptions.Builder()
                .setSourceLanguage(FirebaseTranslateLanguage.EN)
                .setTargetLanguage(FirebaseTranslateLanguage.HI)
                .build()
            FirebaseNaturalLanguage.getInstance().getTranslator(options)
        } else {
            val options = FirebaseTranslatorOptions.Builder()
                .setSourceLanguage(FirebaseTranslateLanguage.EN)
                .setTargetLanguage(FirebaseTranslateLanguage.BN)
                .build()
            FirebaseNaturalLanguage.getInstance().getTranslator(options)
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
        tts.stop()
        tts.shutdown()
        super.onDestroy()
    }

}