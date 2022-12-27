package com.example.divyapay.fragments

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.speech.tts.TextToSpeech
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.lifecycle.ViewModelProvider
import com.airbnb.lottie.LottieAnimationView
import com.example.divyapay.R
import com.example.divyapay.db.AppViewModel
import com.google.firebase.ml.naturallanguage.FirebaseNaturalLanguage
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslateLanguage
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslator
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslatorOptions
import java.util.*

class Account : Fragment() {

    private lateinit var tts: TextToSpeech
    private lateinit var appViewModel: AppViewModel
    private lateinit var nameAccount: TextView
    private lateinit var phoneAccount: TextView
    private lateinit var accNoAccount: TextView
    private lateinit var bankAccount: TextView
    private lateinit var ifscAccount: TextView
    private lateinit var accBalanceAccount: TextView
    private lateinit var progressbar: LottieAnimationView
    private lateinit var mainLayout: LinearLayout
    private lateinit var selectedLang: String
    private lateinit var translator: FirebaseTranslator

    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_account, container, false)

        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    requireActivity().supportFragmentManager.beginTransaction()
                        .replace(R.id.frame_layout, Home()).commit()
                }
            })

        nameAccount = view.findViewById(R.id.nameAccount)
        phoneAccount = view.findViewById(R.id.numberAccount)
        accNoAccount = view.findViewById(R.id.accNoAccount)
        accBalanceAccount = view.findViewById(R.id.accBalanceAccount)
        ifscAccount = view.findViewById(R.id.ifscAccount)
        bankAccount = view.findViewById(R.id.bankAccount)
        mainLayout = view.findViewById(R.id.mainLayout_account)
        progressbar = view.findViewById(R.id.accountProgressbar)

        appViewModel = ViewModelProvider(requireActivity())[AppViewModel::class.java]
        appViewModel = ViewModelProvider(requireActivity())[AppViewModel::class.java]
        appViewModel.userdata.observe(viewLifecycleOwner) {
            appViewModel.fetchAccountDetails(it!!)
            appViewModel.accDetails.observe(viewLifecycleOwner) {
                nameAccount.text = it[0]
                phoneAccount.text = it[1]
                accNoAccount.text = it[2]
                accBalanceAccount.text = "₹${it[3]} INR"
                bankAccount.text = it[4]
                ifscAccount.text = it[5]
                selectedLang = it[6]

                progressbar.visibility = View.GONE
                mainLayout.visibility = View.VISIBLE
            }
            if (selectedLang != "English") {
                prepareTranslateModel()
            }

            textToSpeech(
                requireContext(), "Welcome to account page"
            )
            textToSpeech(
                requireContext(), "Bank  name is         ${bankAccount.text}               ."
            )
            textToSpeech(
                requireContext(),
                "Holder name is           ${nameAccount.text}                                ."
            )
            textToSpeech(
                requireContext(),
                "Registered mobile number is          ${phoneAccount.text}                          ."
            )
            textToSpeech(
                requireContext(),
                "Account number is           ${accNoAccount.text}                 ."
            )
            textToSpeech(
                requireContext(),
                "Current account balance is       ${accBalanceAccount.text.toString().replace("INR", "")} rupees                        ."
            )
            textToSpeech(
                requireContext(), "double tap to use voice assistance                           ."
            )
            textToSpeech(
                requireContext(), "swipe left to return at the home page                        ."
            )
            textToSpeech(
                requireContext(), "swipe right to repeat your account details                   ."
            )
        }

        return view
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
        tts.stop()
        tts.shutdown()
        super.onDestroy()
    }
}