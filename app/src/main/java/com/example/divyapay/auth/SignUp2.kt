package com.example.divyapay.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.airbnb.lottie.LottieAnimationView
import com.example.divyapay.R
import com.example.divyapay.db.AppViewModel
import com.example.divyapay.db.Response
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class SignUp2 : Fragment() {

    private lateinit var nameEditText: TextInputEditText
    private lateinit var emailEditText: TextInputEditText
    private lateinit var passwordEditText: TextInputEditText
    private lateinit var cPasswordEditText: TextInputEditText
    private lateinit var passwordEditTextLayout: TextInputLayout
    private lateinit var cPasswordEditTextLayout: TextInputLayout
    private lateinit var signInText: TextView
    private lateinit var prevBtn: CardView
    private lateinit var signUpBtn: CardView
    private lateinit var whiteLayout: LinearLayout
    private lateinit var progressbar: LottieAnimationView
    private val emailPattern by lazy { "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+" }
    private lateinit var number: String
    private lateinit var accNo: String
    private lateinit var ifsc: String
    private lateinit var bank: String
    private lateinit var lang: String
    private lateinit var appViewModel: AppViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_sign_up2, container, false)

        nameEditText = view.findViewById(R.id.signUpName)
        emailEditText = view.findViewById(R.id.signUpEmail)
        passwordEditText = view.findViewById(R.id.signUpPassword)
        cPasswordEditText = view.findViewById(R.id.signUpCPassword)
        passwordEditTextLayout = view.findViewById(R.id.signUpPasswordLayout)
        cPasswordEditTextLayout = view.findViewById(R.id.signUpCPasswordLayout)
        prevBtn = view.findViewById(R.id.prevBtn)
        signUpBtn = view.findViewById(R.id.signUpBtn)
        whiteLayout = view.findViewById(R.id.signUpWhiteLayout)
        progressbar = view.findViewById(R.id.signUpProgressbar)
        signInText = view.findViewById(R.id.signInText)

        appViewModel = ViewModelProvider(requireActivity())[AppViewModel::class.java]

        number = requireArguments().getString("number").toString()
        accNo = requireArguments().getString("accNo").toString()
        ifsc = requireArguments().getString("ifsc").toString()
        bank = requireArguments().getString("bank").toString()
        lang = requireArguments().getString("lang").toString()

        signInText.setOnClickListener {
            requireFragmentManager().beginTransaction().replace(R.id.authFrameLayout, SignIn()).commit()
        }

        prevBtn.setOnClickListener {
            requireFragmentManager().beginTransaction().replace(R.id.authFrameLayout, SignUp()).commit()
        }

        signUpBtn.setOnClickListener {
            signUp()
        }

        return view
    }

    private fun signUp() {

        whiteLayout.visibility = View.VISIBLE
        progressbar.visibility = View.VISIBLE

        val name = nameEditText.text.toString()
        val email = emailEditText.text.toString()
        val password = passwordEditText.text.toString()
        val cPassword = cPasswordEditText.text.toString()
        var allRight = true

        passwordEditTextLayout.isPasswordVisibilityToggleEnabled = true
        cPasswordEditTextLayout.isPasswordVisibilityToggleEnabled = true

        if(name.isEmpty()) {
            nameEditText.error = "Enter your name"
            allRight = false
        }
        if(email.isEmpty()) {
            emailEditText.error = "Enter your email address"
            allRight = false
        }
        if(emailPattern.matches(email.toRegex())) {
            emailEditText.error = "Enter valid email address"
            allRight = false
        }
        if(password.isEmpty()) {
            passwordEditTextLayout.isPasswordVisibilityToggleEnabled = false
            passwordEditText.error = "Enter your password"
            allRight = false
        }
        if(cPassword.isEmpty()) {
            cPasswordEditTextLayout.isPasswordVisibilityToggleEnabled = false
            cPasswordEditText.error = "Enter your password again"
            allRight = false
        }
        if(password != cPassword) {
            cPasswordEditTextLayout.isPasswordVisibilityToggleEnabled = false
            cPasswordEditText.error = "Password not matched"
            allRight = false
        }
        if(!allRight) {
            Toast.makeText(requireContext(), "Enter valid details", Toast.LENGTH_SHORT).show()
            whiteLayout.visibility = View.GONE
            progressbar.visibility = View.GONE
        }else{
            appViewModel.signUp(email, password, name, number, bank, accNo, ifsc, lang)
            appViewModel.response.observe(viewLifecycleOwner) {
                when(it) {
                    is Response.Success -> {
                        requireFragmentManager().beginTransaction().replace(R.id.authFrameLayout, SignIn()).commit()
                        this.onDestroy()
                    }
                    is Response.Failure -> {
                        Toast.makeText(requireContext(), it.errorMassage, Toast.LENGTH_SHORT).show()
                        whiteLayout.visibility = View.GONE
                        progressbar.visibility = View.GONE
                    }
                    else -> {}
                }
            }
        }
    }

}