package com.example.divyapay.auth

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.lifecycle.ViewModelProvider
import com.airbnb.lottie.LottieAnimationView
import com.example.divyapay.DashboardActivity
import com.example.divyapay.R
import com.example.divyapay.db.AppViewModel
import com.example.divyapay.db.Response
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class SignIn : Fragment() {

    private lateinit var emailEditText: TextInputEditText
    private lateinit var passwordEditText: TextInputEditText
    private lateinit var passwordLayout: TextInputLayout
    private lateinit var signInBtn: CardView
    private lateinit var signUpText: TextView
    private lateinit var whiteLayout: LinearLayout
    private lateinit var progressbar: LottieAnimationView
    private val emailPattern by lazy { "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+" }
    private lateinit var appViewModel: AppViewModel

    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_sign_in, container, false)

        appViewModel = ViewModelProvider(requireActivity())[AppViewModel::class.java]

        emailEditText = view.findViewById(R.id.signInEmail)
        passwordEditText = view.findViewById(R.id.signInPassword)
        passwordLayout = view.findViewById(R.id.signInPasswordLayout)
        signInBtn = view.findViewById(R.id.signInBtn)
        signUpText = view.findViewById(R.id.signUpText)
        whiteLayout = view.findViewById(R.id.signInWhiteLayout)
        progressbar = view.findViewById(R.id.signInProgressbar)

        signUpText.setOnClickListener {
            requireFragmentManager().beginTransaction().replace(R.id.authFrameLayout, SignUp()).commit()
        }

        signInBtn.setOnClickListener {
            signIn()
        }

        return view
    }

    private fun signIn() {

        whiteLayout.visibility = View.VISIBLE
        progressbar.visibility = View.VISIBLE

        val email = emailEditText.text.toString()
        val password = passwordEditText.text.toString()
        var allRight = true

        passwordLayout.isPasswordVisibilityToggleEnabled = true

        if(email.isEmpty()) {
            emailEditText.error = "Enter your email address"
            allRight = false
        }
        if(emailPattern.matches(email.toRegex())) {
            emailEditText.error = "Enter valid email address"
            allRight = false
        }
        if(password.isEmpty()) {
            passwordLayout.isPasswordVisibilityToggleEnabled = false
            passwordEditText.error = "Enter your password"
            allRight = false
        }
        if(!allRight) {
            Toast.makeText(requireContext(), "Enter valid details", Toast.LENGTH_SHORT).show()
            whiteLayout.visibility = View.GONE
            progressbar.visibility = View.GONE
        }else{
            appViewModel.login(email, password)
            appViewModel.response.observe(viewLifecycleOwner) {
                when(it) {
                    is Response.Success -> {
                        startActivity(Intent(requireContext(), DashboardActivity::class.java))
                        this.onDestroy()
                    }
                    is Response.Failure -> {
                        Toast.makeText(requireContext(), it.errorMassage, Toast.LENGTH_SHORT).show()
                        whiteLayout.visibility = View.GONE
                        progressbar.visibility = View.GONE
                    }
                }
            }
        }
    }

}