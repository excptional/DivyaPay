package com.example.divyapay

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.provider.Settings
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.divyapay.databinding.ActivityDashboardBinding
import java.util.*
import java.util.concurrent.Executor
import kotlin.system.exitProcess

class DashboardActivity : AppCompatActivity(), GestureDetector.OnGestureListener {

    private lateinit var binding: ActivityDashboardBinding
    private lateinit var executor: Executor
    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: BiometricPrompt.PromptInfo
    private lateinit var gestureDetector: GestureDetector
    private lateinit var tts: TextToSpeech
    private var name: String = ""
    private var amount: String = ""
    private var phoneNumber: String = ""
    private var description: String = ""
    private var flag = false
    private var count = 0
    private val validNumericPattern by lazy { "^(0|[1-9][0-9]*)\$" }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        this.onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true){
            override fun handleOnBackPressed() {
                if(!flag){
                    binding.voiceInputLayout.visibility = View.GONE
                    binding.frameLayout.visibility = View.VISIBLE
                    flag = true
                }
            }
        })

        checkDeviceHasBiometric()
        window.statusBarColor = Color.WHITE
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }

        textToSpeech(this, "Use fingerprint  or default phone unlock to logged in")
        gestureDetector = GestureDetector(this, this)

        executor = ContextCompat.getMainExecutor(this)
        biometricPrompt = BiometricPrompt(this, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(
                    errorCode: Int,
                    errString: CharSequence
                ) {
                    super.onAuthenticationError(errorCode, errString)
                    textToSpeech(this@DashboardActivity, "Application is closed")
                    Handler().postDelayed({
                        moveTaskToBack(true)
                        exitProcess(-1)
                    }, 3000)
                }

                override fun onAuthenticationSucceeded(
                    result: BiometricPrompt.AuthenticationResult
                ) {
                    super.onAuthenticationSucceeded(result)
                    textToSpeech(
                        this@DashboardActivity,
                        "Logged in successfully"
                    )
                    Toast.makeText(
                        applicationContext,
                        "Authentication succeeded!", Toast.LENGTH_SHORT
                    )
                        .show()
                    replaceFragments(Home())
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    textToSpeech(this@DashboardActivity, "Log in failed")
                    Toast.makeText(
                        applicationContext, "Authentication failed",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })

        promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Unlock Divya Pay")
            .setSubtitle("Enter phone screen lock pattern, PIN, password or fingerprint")
            .setDeviceCredentialAllowed(true)
            .setConfirmationRequired(false)
            .build()

        biometricPrompt.authenticate(promptInfo)
    }

    private fun replaceFragments(fragment: Fragment) {
        if (tts.isSpeaking) {
            tts.stop()
            tts.shutdown()
        }
        flag = true
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.frame_layout, fragment)
        fragmentTransaction.commit()
        navTransition(fragment)
    }

    @SuppressLint("SwitchIntDef")
    private fun checkDeviceHasBiometric() {
        val biometricManager = BiometricManager.from(this)
        when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL)) {
            BiometricManager.BIOMETRIC_SUCCESS ->
                Toast.makeText(this, "App can authenticate using biometrics.", Toast.LENGTH_SHORT)
                    .show()
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE ->
                Toast.makeText(
                    this,
                    "No biometric features available on this device.",
                    Toast.LENGTH_SHORT
                ).show()
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> {}
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                val enrollIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    Intent(Settings.ACTION_BIOMETRIC_ENROLL).apply {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                            putExtra(
                                Settings.EXTRA_BIOMETRIC_AUTHENTICATORS_ALLOWED,
                                BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL
                            )
                        }
                    }
                } else {
                    TODO("VERSION.SDK_INT < R")
                }
                startActivityForResult(enrollIntent, 100)
            }
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onGesture(gesture: GestureDetector.Gesture): Boolean {
        val currentFragment = supportFragmentManager.findFragmentById(R.id.frame_layout)
        when (gesture) {
            GestureDetector.Gesture.TAP ->
                return true
            GestureDetector.Gesture.SWIPE_RIGHT -> {
                if (!flag) {
                    if(tts.isSpeaking){
                        tts.stop()
                        tts.shutdown()
                    }
                    if (name.isEmpty()) {
                        textToSpeech(
                            this,
                            "Say name first, double tap to try again "
                        )
                        Toast.makeText(
                            this,
                            "Say name first, double tap to try again",
                            Toast.LENGTH_LONG
                        ).show()
                    } else if ((count == 1) && (!amount.matches(validNumericPattern.toRegex()) || amount.isEmpty())) {
                        textToSpeech(
                            this,
                            "Say valid amount in numbers only, double tap to try again "
                        )
                        Toast.makeText(
                            this,
                            "Say valid amount in numbers only, double tap to try again",
                            Toast.LENGTH_LONG
                        ).show()
                    } else if ((count == 2) && (!phoneNumber.matches(validNumericPattern.toRegex()) || phoneNumber.isEmpty())) {
                        textToSpeech(
                            this,
                            "Say valid transaction i d, double tap to try again "
                        )
                        Toast.makeText(
                            this,
                            "Say valid transaction id, double tap to try again",
                            Toast.LENGTH_LONG
                        ).show()
                    } else if (count == 3) {
                        binding.makePayText.text = "Swipe right to make payment"
                        textToSpeech(
                            this,
                            "your name is $name, selected amount is $amount rupees, transaction i d is ${
                                spaceGiving(
                                    phoneNumber
                                )
                            } and note is $description, now swipe right to pay the money and swipe left to cancel the payment"
                        )
                    } else {
                        count++
                        voicePFBefore()
                    }

                } else {
                    when (currentFragment) {
                        is Home -> replaceFragments(Payment())
                        is Payment -> replaceFragments(PaymentForm())
                        is Account -> replaceFragments(Account())
                        is PaymentForm -> {
                            flag = false
                            supportFragmentManager.findFragmentById(R.id.frame_layout)?.onDestroy()
                            binding.voiceInputLayout.visibility = View.VISIBLE
                            binding.frameLayout.visibility = View.GONE
                            voicePFBefore()
                        }
                    }
                }
                return true
            }
            GestureDetector.Gesture.SWIPE_LEFT -> {
                if (!flag) {
                    flag = true
                    count = 0
                    binding.voiceInputLayout.visibility = View.GONE
                    binding.frameLayout.visibility = View.VISIBLE
                    return true
                }
                when (currentFragment) {
                    is Home -> replaceFragments(Home())
                    is Account -> replaceFragments(Home())
                    is Payment -> replaceFragments(Home())
                    is Transaction -> replaceFragments(Home())
                    is PaymentForm -> replaceFragments(Payment())
                }
                return true
            }
            GestureDetector.Gesture.SWIPE_UP -> {
                if (!flag) return false
                when (currentFragment) {
                    is Home -> replaceFragments(Account())
                }
                return true
            }
            GestureDetector.Gesture.SWIPE_DOWN -> {
                if (!flag) return false
                when (currentFragment) {
                    is Home -> replaceFragments(Transaction())
                }
                return true
            }
            GestureDetector.Gesture.DOUBLE_TAP -> {
                return if (currentFragment !is PaymentForm || !flag) {
                    speechToText()
                    true
                } else false
            }
            else -> return false
        }
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        return if (gestureDetector.onTouchEvent(ev)) {
            true
        } else super.dispatchTouchEvent(ev)
    }

    private fun navTransition(fragment: Fragment) {
        when (fragment) {
            is Home -> {
                binding.homeIcon.setImageResource(R.drawable.home_blue)
                binding.paymentIcon.setImageResource(R.drawable.pay_icon)
                binding.accountIcon.setImageResource(R.drawable.account_details_icon)
                binding.transactionIcon.setImageResource(R.drawable.history_icon)
                binding.homeText.visibility = View.VISIBLE
                binding.paymentText.visibility = View.GONE
                binding.accountText.visibility = View.GONE
                binding.transactionText.visibility = View.GONE
            }
            is Account -> {
                binding.accountIcon.setImageResource(R.drawable.account_blue)
                binding.paymentIcon.setImageResource(R.drawable.pay_icon)
                binding.homeIcon.setImageResource(R.drawable.home_icon)
                binding.transactionIcon.setImageResource(R.drawable.history_icon)
                binding.accountText.visibility = View.VISIBLE
                binding.paymentText.visibility = View.GONE
                binding.homeText.visibility = View.GONE
                binding.transactionText.visibility = View.GONE
            }
            is Payment -> {
                binding.paymentIcon.setImageResource(R.drawable.pay_blue)
                binding.homeIcon.setImageResource(R.drawable.home_icon)
                binding.accountIcon.setImageResource(R.drawable.account_details_icon)
                binding.transactionIcon.setImageResource(R.drawable.history_icon)
                binding.paymentText.visibility = View.VISIBLE
                binding.accountText.visibility = View.GONE
                binding.homeText.visibility = View.GONE
                binding.transactionText.visibility = View.GONE
            }
            is Transaction -> {
                binding.transactionIcon.setImageResource(R.drawable.history_blue)
                binding.paymentIcon.setImageResource(R.drawable.pay_icon)
                binding.homeIcon.setImageResource(R.drawable.home_icon)
                binding.accountIcon.setImageResource(R.drawable.account_details_icon)
                binding.transactionText.visibility = View.VISIBLE
                binding.paymentText.visibility = View.GONE
                binding.accountText.visibility = View.GONE
                binding.homeText.visibility = View.GONE
            }
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

    private fun fixNumber(s: String) : String{
        var str = s
        if(str.contains(" ")) str = str.replace( " ", "")
        if(str.contains(",")) str = str.replace(",", "")
        return str
    }

    private fun voicePFBefore() {
        when (count) {
            0 -> textToSpeech(this, "Double tap and say the name ")
            1 -> textToSpeech(
                this, "Double tap and say the amount you want to pay"
            )
            2 -> textToSpeech(
                this,
                "Double tap and say the transaction upi i d where you want to send the money"
            )
            3 -> textToSpeech(this, "Double tap and say the note you want to add ")
        }

    }

    private fun voicePFAfter() {
        when (count) {
            0 -> {
                binding.nameVoiceInput.text = name
                textToSpeech(
                    this,
                    "Your given name is $name, swipe right to confirm, double tap to say again"
                )
                Toast
                    .makeText(
                        this@DashboardActivity, "Your given name is $name",
                        Toast.LENGTH_SHORT
                    )
                    .show()
            }
            1 -> {
                binding.amountVoiceInput.text = amount
                textToSpeech(
                    this,
                    "Your selected amount is $amount rupees, swipe right to confirm, double tap to say again "
                )
                Toast
                    .makeText(
                        this@DashboardActivity, "Your given name is $name",
                        Toast.LENGTH_SHORT
                    )
                    .show()
            }
            2 -> {
                binding.transactionIDVoiceInput.text = phoneNumber
                textToSpeech(
                    this,
                    "Your given transaction i d is ${spaceGiving(phoneNumber)}, swipe right to confirm, double tap to say again  "
                )
                Toast
                    .makeText(
                        this@DashboardActivity, "Your given transaction i d is $phoneNumber",
                        Toast.LENGTH_SHORT
                    )
                    .show()
            }
            3 -> {
                binding.descriptionVoiceInput.text = description
                textToSpeech(
                    this,
                    "Your given description is $description, swipe right to confirm, double tap to say again "
                )
                Toast
                    .makeText(
                        this@DashboardActivity, "Your given description is $description",
                        Toast.LENGTH_SHORT
                    )
                    .show()
            }
        }
    }

    private fun textToSpeech(context: Context, speech: String) {
        tts = TextToSpeech(
            context
        ) {
            if (it == TextToSpeech.SUCCESS) {
                tts.language = Locale.ENGLISH
                tts.setSpeechRate(0.8f)
                tts.speak(speech, TextToSpeech.QUEUE_ADD, null)
            }
        }
    }

    private fun speechToText() {
        if (tts.isSpeaking) {
            tts.stop()
            tts.shutdown()
        }
        supportFragmentManager.findFragmentById(R.id.frame_layout)?.onDestroy()
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)

        intent.putExtra(
            RecognizerIntent.EXTRA_LANGUAGE_MODEL,
            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
        )

        intent.putExtra(
            RecognizerIntent.EXTRA_LANGUAGE,
            Locale.getDefault()
        )

        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak to text")

        try {
            startActivityForResult(intent, 100)
        } catch (e: Exception) {
            Toast
                .makeText(
                    this@DashboardActivity, " " + e.message,
                    Toast.LENGTH_SHORT
                )
                .show()
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 100) {
            if (resultCode == RESULT_OK && data != null) {

                val searchString: List<String> =
                    data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS) as List<String>

                var str: String = searchString.joinToString()

                if (flag) {
                    searchActivity(str)
                } else {
                    when (count) {
                        0 -> name = str
                        1 -> amount = str
                        2 -> {
                            str = fixNumber(str)
                            phoneNumber = str
                        }
                        3 -> description = str
                    }
                    voicePFAfter()
                }
            }
        }
    }

    private fun searchActivity(str: String) {
        if (str.contains("home")) {
            replaceFragments(Home())
        } else if (str.contains("account")) {
            replaceFragments(Account())
        } else if (str.contains("pay")) {
            replaceFragments(Payment())
        } else if (str.contains("transaction")) {
            replaceFragments(Transaction())
        } else {
            textToSpeech(this, "Result is nothing,    try again")
            Toast.makeText(this, "Result is nothing", Toast.LENGTH_SHORT).show()
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