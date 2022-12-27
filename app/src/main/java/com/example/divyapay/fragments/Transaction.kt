package com.example.divyapay.fragments

import android.annotation.SuppressLint
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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.divyapay.R
import com.example.divyapay.TransHisItems
import com.example.divyapay.TransactionHistoryAdapter
import com.example.divyapay.db.AppViewModel
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.ml.naturallanguage.FirebaseNaturalLanguage
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslateLanguage
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslator
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslatorOptions
import java.util.*

class Transaction : Fragment() {

    private lateinit var transactionHistoryAdapter: TransactionHistoryAdapter
    private var thItemsArray = arrayListOf<TransHisItems>()
    private lateinit var thRecyclerView: RecyclerView
    private lateinit var shimmerContainerTransaction: ShimmerFrameLayout
    private lateinit var appViewModel: AppViewModel
    private var selectedLang: String = "English"
    private lateinit var phone: String
    private lateinit var translator: FirebaseTranslator
    private lateinit var tts: TextToSpeech
    private val list1 = arrayListOf<String>()
    private val list2 = arrayListOf<String>()
    private val list3 = arrayListOf<String>()

    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_transaction, container, false)
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    requireActivity().supportFragmentManager.beginTransaction()
                        .replace(R.id.frame_layout, Home()).commit()
                }
            })

        appViewModel = ViewModelProvider(this)[AppViewModel::class.java]
        thRecyclerView = view.findViewById(R.id.thRecyclerview)
        transactionHistoryAdapter = TransactionHistoryAdapter(thItemsArray)
        thRecyclerView.layoutManager = LinearLayoutManager(view.context)
        thRecyclerView.setHasFixedSize(true)
        thRecyclerView.setItemViewCacheSize(20)
        thRecyclerView.adapter = transactionHistoryAdapter
        shimmerContainerTransaction = view.findViewById(R.id.shimmer_view_transaction)
        shimmerContainerTransaction.startShimmer()

        appViewModel.userdata.observe(viewLifecycleOwner) {
            appViewModel.fetchTransactions(it!!.uid)
            appViewModel.transactionDetails.observe(viewLifecycleOwner) {
                fetchTransactions(it)
            }
            appViewModel.accDetails.observe(viewLifecycleOwner) {
                selectedLang = it[6]
                if(selectedLang != "English") {
                    prepareTranslateModel()
                }
            }
        }

        return view
    }

    private fun getMonth(str: String): String {
        return when(str) {
            "Jan" -> {"January"}
            "Feb" -> {"February"}
            "Mar" -> {"March"}
            "Apr" -> {"April"}
            "May" -> {"May"}
            "Jun" -> {"June"}
            "Jul" -> {"July"}
            "Aug" -> {"August"}
            "Sep" ->  {"September"}
            "Oct" -> {"October"}
            "Nov" -> {"November"}
            "Dec" -> {"December"}
            else -> {""}
        }
    }

    private fun getStatus(str: String): String {
        return when (str) {
            "credited" -> "to"
            "debited" -> "from"
            else -> {""}
        }
    }

            private fun fetchTransactions(list: MutableList<DocumentSnapshot>) {
        thItemsArray = arrayListOf()
        for (i in list) {
            val thItem = TransHisItems(
                i.getString("name"),
                i.getString("phone no"),
                i.getString("amount"),
                i.getString("date"),
                i.getString("status")
            )
            thItemsArray.add(thItem)
        }
        transactionHistoryAdapter.updateTH(thItemsArray)
        shimmerContainerTransaction.clearAnimation()
        shimmerContainerTransaction.visibility = View.GONE
        thRecyclerView.visibility = View.VISIBLE
    }

    private fun textToSpeech(context: Context, speech: String) {
        if (selectedLang == "English") {
            tts = TextToSpeech(
                context
            ) {
                if (it == TextToSpeech.SUCCESS) {
                    tts.language = Locale.ENGLISH
                    tts.setSpeechRate(0.6f)
                    tts.speak(it.toString(), TextToSpeech.QUEUE_ADD, null)
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