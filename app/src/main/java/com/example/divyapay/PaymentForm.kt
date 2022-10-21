package com.example.divyapay

import android.content.Context
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import java.util.*


class PaymentForm : Fragment() {

    private lateinit var tts: TextToSpeech
    private lateinit var name: String
    private lateinit var amount: String
    private lateinit var phoneNumber: String
    private lateinit var description: String
    private val validNumericPattern by lazy { "^(0|[1-9][0-9]*)\$" }


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

        payBtn.setOnClickListener {
            name = nameEditText.text.toString()
            amount = amountEditText.text.toString()
            phoneNumber = phoneEditText.text.toString()
            description = descriptionEditText.text.toString()

            if (name.isNotEmpty() && amount.isNotEmpty() && amount.matches(validNumericPattern.toRegex()) && phoneNumber.isNotEmpty() && phoneNumber.matches(
                    validNumericPattern.toRegex()
                ) && (phoneNumber.length == 10)
            ) {
                Toast.makeText(
                    requireActivity().applicationContext,
                    "Transaction successful",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                if (name.isEmpty())
                    nameEditText.error = "Enter name here"
                if (amount.isEmpty())
                    amountEditText.error = "Enter amount here"
                if (!amount.matches(validNumericPattern.toRegex()))
                    amountEditText.error = "Enter valid amount"
                if (phoneNumber.isEmpty())
                    phoneEditText.error = "Enter phone number here"
                if (!phoneNumber.matches(validNumericPattern.toRegex()) || (phoneNumber.length == 10))
                    phoneEditText.error = "Enter valid phone number"
                Toast.makeText(
                    requireActivity().applicationContext,
                    "Fill details properly",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
        textToSpeech(
            requireActivity().applicationContext,
            "Here you have to fill name, amount, phone number and description          ."
                    + "fill all the details and click on the pay button for final payment       ."
                    + "swipe left to return back        ."
                    + "if you are want to fill this details using our voice assistance then swipe right        ."
        )
        return view
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
        if (tts.isSpeaking) {
            tts.stop()
            tts.shutdown()
        }
        super.onDestroy()
    }

}