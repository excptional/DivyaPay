package com.example.divyapay.auth

import android.os.Bundle
import android.view.Gravity
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import com.example.divyapay.R
import com.google.android.material.textfield.TextInputEditText

class SignUp : Fragment(), AdapterView.OnItemSelectedListener {

    private var banks = arrayListOf(
        "Select your bank",
        "Bank of Baroda",
        "Bank of India",
        "Bank of Maharashtra",
        "Canara Bank",
        "Central Bank of India",
        "Indian Bank",
        "Indian Overseas Bank",
        "Punjab & Sind Bank",
        "Punjab National Bank",
        "State Bank of India",
        "HDFC Bank",
        "ICICI Bank",
        "UCO Bank",
        "Union Bank of India",
        "Axis Bank",
        "Bandhan Bank",
        "CSB Bank",
        "City Union Bank",
        "DCB Bank",
        "Federal Bank",
        "Induslnd Bank",
        "IDFC First Bank",
        "Kotak Mahindra Bank",
        "Nainital Bank",
        "RBL Bank",
        "YES Bank",
        "IDBI Bank"
    )

    private var languages = arrayListOf(
        "Select language for communication",
        "English",
        "Hindi",
        "Bengali"
    )

    private lateinit var numberEditText: TextInputEditText
    private lateinit var accNoEditText: TextInputEditText
    private lateinit var ifscEditText: TextInputEditText
    private lateinit var bankSpinner: Spinner
    private lateinit var langSpinner: Spinner
    private lateinit var selectedBank: String
    private lateinit var selectedLanguage: String
    private lateinit var nextBtn: CardView
    private lateinit var signInText: TextView
    private val validPhoneNumberPattern by lazy { "^(\\+\\d{1,3}[- ]?)?\\d{10}\$" }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_sign_up, container, false)

        numberEditText = view.findViewById(R.id.signUpPhone)
        accNoEditText = view.findViewById(R.id.signUpAccountNo)
        ifscEditText = view.findViewById(R.id.signUpIFSC)
        bankSpinner = view.findViewById(R.id.bankSpinner)
        langSpinner = view.findViewById(R.id.languageSpinner)
        nextBtn = view.findViewById(R.id.nextBtn)
        signInText = view.findViewById(R.id.signInText)

        val bankAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            banks
        )
        bankAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        with(bankSpinner)
        {
            adapter = bankAdapter
            setSelection(0, true)
            onItemSelectedListener = this@SignUp
            gravity = Gravity.CENTER
            setPopupBackgroundResource(R.color.white)
        }

        val langAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            languages
        )
        langAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        with(langSpinner)
        {
            adapter = langAdapter
            setSelection(0, true)
            onItemSelectedListener = this@SignUp
            gravity = Gravity.CENTER
            setPopupBackgroundResource(R.color.white)
        }

        nextBtn.setOnClickListener {
            moveNext(view)
        }

        signInText.setOnClickListener {
            requireFragmentManager().beginTransaction().replace(R.id.authFrameLayout, SignIn())
                .commit()
        }

        return view
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        if (parent!!.id == R.id.bankSpinner) {
            selectedBank = banks[position]
            if (position == 0) {
                selectedBank = ""
                Toast.makeText(
                    requireContext(),
                    "Nothing selected, select to continue",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                Toast.makeText(requireContext(), "${banks[position]} selected", Toast.LENGTH_SHORT)
                    .show()
            }
        } else {
            selectedLanguage = languages[position]
            if (position == 0) {
                selectedLanguage = ""
                Toast.makeText(
                    requireContext(),
                    "Nothing selected, select to continue",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                Toast.makeText(
                    requireContext(),
                    "${languages[position]} selected",
                    Toast.LENGTH_SHORT
                )
                    .show()
            }
        }

    }

    override fun onNothingSelected(parent: AdapterView<*>?) {
        Toast.makeText(requireContext(), "Nothing selected, select to continue", Toast.LENGTH_SHORT)
            .show()
    }

    private fun moveNext(view: View) {
        val number = numberEditText.text.toString()
        val accNo = accNoEditText.text.toString()
        val ifsc = ifscEditText.text.toString()
        var allRight = true

        if (number.isEmpty()) {
            numberEditText.error = "Enter your registered mobile number"
            allRight = false
        }
        if (validPhoneNumberPattern.matches(number.toRegex())) {
            numberEditText.error = "Enter valid mobile number"
            allRight = false
        }
        if (accNo.isEmpty()) {
            accNoEditText.error = "Enter your bank account number"
            allRight = false
        }
        if (ifsc.isEmpty()) {
            ifscEditText.error = "Enter IFSC code"
            allRight = false
        }
        if (selectedBank.isEmpty()) {
            Toast.makeText(requireContext(), "Select your bank", Toast.LENGTH_SHORT).show()
            allRight = false
        }
        if (selectedLanguage.isEmpty()) {
            Toast.makeText(requireContext(), "Select your language", Toast.LENGTH_SHORT).show()
            allRight = false
        }

        if (!allRight) {
            Toast.makeText(requireContext(), "Enter valid details", Toast.LENGTH_SHORT).show()
        } else {
            val bundle = Bundle()
            bundle.putString("number", number)
            bundle.putString("accNo", accNo)
            bundle.putString("ifsc", ifsc)
            bundle.putString("bank", selectedBank)
            bundle.putString("lang", selectedLanguage)
            val fragment: Fragment = SignUp2()
            fragment.arguments = bundle
            requireFragmentManager().beginTransaction().replace(R.id.authFrameLayout, fragment)
                .commit()
        }
    }

}