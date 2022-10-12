package com.example.divyapay

import android.content.Context
import android.os.Bundle
import android.speech.tts.TextToSpeech
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import java.util.*


class PaymentForm : Fragment() {

//    private lateinit var binding: FragmentPaymentFormBinding

    private lateinit var tts: TextToSpeech
    private lateinit var name: String
    private lateinit var amount: String
    private lateinit var transactionID: String
    private lateinit var description: String
    private val validUpiIDPattern = "^[\\w.-]+@[\\w.-]+\$"
    private val validNumericPattern = "^(0|[1-9][0-9]*)\$"


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

//        binding = FragmentPaymentFormBinding.inflate(layoutInflater)

        val view: View = inflater.inflate(R.layout.fragment_payment_form, container, false)
        val payBtn: LinearLayout = view.findViewById(R.id.payBtn)
        val nameEditText: EditText = view.findViewById(R.id.nameEditText)
        val amountEditText: EditText = view.findViewById(R.id.amountEditText)
        val transactionIDEditText: EditText = view.findViewById(R.id.transactionIDEditText)
        val descriptionEditText: EditText = view.findViewById(R.id.descriptionEditText)

        payBtn.setOnClickListener {
            name = nameEditText.text.toString()
            amount = amountEditText.text.toString()
            transactionID = transactionIDEditText.text.toString()
            description = descriptionEditText.text.toString()

            if (name.isNotEmpty() && amount.isNotEmpty() && amount.matches(validNumericPattern.toRegex()) && transactionID.isNotEmpty() && transactionID.matches(
                    validUpiIDPattern.toRegex()
                )
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
                if (transactionID.isEmpty())
                    transactionIDEditText.error = "Enter transaction id here"
                if (!transactionID.matches(validUpiIDPattern.toRegex()))
                    transactionIDEditText.error = "Enter valid transaction id"
                Toast.makeText(
                    requireActivity().applicationContext,
                    "Fill details properly",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
        textToSpeech(
            requireActivity().applicationContext,
            "Here you have to fill name, amount, transaction i d and description          ."
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