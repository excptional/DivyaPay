package com.example.divyapay.fragments

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.speech.tts.TextToSpeech
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.divyapay.R
import com.example.divyapay.auth.SignIn
import com.example.divyapay.db.AppViewModel
import com.google.firebase.ml.naturallanguage.FirebaseNaturalLanguage
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslateLanguage
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslator
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslatorOptions
import java.util.*


class PaymentForm : Fragment() {

    private lateinit var tts: TextToSpeech
    private lateinit var name: String
    private lateinit var amount: String
    private lateinit var phoneNumber: String
    private lateinit var description: String
    private val validNumericPattern by lazy { "^(0|[1-9][0-9]*)\$" }
    private val validPhoneNumberPattern by lazy { "^(\\+\\d{1,3}[- ]?)?\\d{10}\$" }
    private lateinit var appViewModel: AppViewModel
    private lateinit var selectedLang: String
    private lateinit var phone: String
    private lateinit var translator: FirebaseTranslator

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    requireActivity().supportFragmentManager.beginTransaction()
                        .replace(R.id.frame_layout, Payment()).commit()
                }
            })

        val view: View = inflater.inflate(R.layout.fragment_payment_form, container, false)
        val payBtn: LinearLayout = view.findViewById(R.id.payBtn)
        val nameEditText: EditText = view.findViewById(R.id.nameEditText)
        val amountEditText: EditText = view.findViewById(R.id.amountEditText)
        val phoneEditText: EditText = view.findViewById(R.id.phoneEditText)
        val descriptionEditText: EditText = view.findViewById(R.id.descriptionEditText)

        appViewModel = ViewModelProvider(requireActivity())[AppViewModel::class.java]
        appViewModel.userdata.observe(viewLifecycleOwner) {
            appViewModel.accDetails.observe(viewLifecycleOwner) {
                phone = it[1]
                selectedLang = it[6]
            }
            if (selectedLang != "English") {
                prepareTranslateModel()
            }
            textToSpeech(
                requireActivity().applicationContext,
                "Here you have to fill name, amount, phone number and description          ."
                        + "fill all the details and click on the pay button for final payment       ."
                        + "swipe left to return back        ."
                        + "if you are want to fill this details using our voice assistance then swipe right        ."
            )
        }

        payBtn.setOnClickListener {
            name = nameEditText.text.toString()
            amount = amountEditText.text.toString()
            phoneNumber = phoneEditText.text.toString()
            description = descriptionEditText.text.toString()

            if (name.isNotEmpty() && amount.isNotEmpty() && amount.matches(validNumericPattern.toRegex()) && phoneNumber.isNotEmpty() && phoneNumber.matches(
                    validPhoneNumberPattern.toRegex()
                ) && (phoneNumber != phone) && (Integer.parseInt(amount) > 10000)
            ) {
                appViewModel.makePayment(amount, phoneNumber)
                Toast.makeText(
                    requireActivity().applicationContext,
                    "Payment successful",
                    Toast.LENGTH_SHORT
                ).show()
                textToSpeech(requireContext(), "Payment successful")
                Handler().postDelayed({
                    requireFragmentManager().beginTransaction().replace(R.id.authFrameLayout, Home())
                        .commit()
                },2000)
            } else {
                if (name.isEmpty())
                    nameEditText.error = "Enter name here"
                if (amount.isEmpty())
                    amountEditText.error = "Enter amount here"
                if (!amount.matches(validNumericPattern.toRegex()))
                    amountEditText.error = "Enter valid amount"
                if (Integer.parseInt(amount) > 10000)
                    amountEditText.error = "Cross maximum transaction limit, maximum transaction amount is 10000 INR"
                if (phoneNumber.isEmpty())
                    phoneEditText.error = "Enter phone number here"
                if (!phoneNumber.matches(validPhoneNumberPattern.toRegex()))
                    phoneEditText.error = "Enter valid phone number"
                if(phone == phoneNumber)
                    phoneEditText.error = "Can't send money on your own number"
                Toast.makeText(
                    requireActivity().applicationContext,
                    "Fill details properly",
                    Toast.LENGTH_SHORT
                ).show()
            }
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
    }

    override fun onDestroy() {
        if (tts.isSpeaking) {
            tts.stop()
            tts.shutdown()
        }
        super.onDestroy()
    }

}