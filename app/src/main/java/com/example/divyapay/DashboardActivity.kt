package com.example.divyapay

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.provider.Settings
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import androidx.biometric.BiometricPrompt
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.airbnb.lottie.LottieAnimationView
import com.example.divyapay.auth.AuthenticationActivity
import com.example.divyapay.databinding.ActivityDashboardBinding
import com.example.divyapay.db.AppViewModel
import com.example.divyapay.db.Response
import com.example.divyapay.fragments.*
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.ml.naturallanguage.FirebaseNaturalLanguage
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslateLanguage
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslator
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslatorOptions
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
    private val validPhoneNumberPattern by lazy { "^(\\+\\d{1,3}[- ]?)?\\d{10}\$" }
    private val validNumericPattern by lazy { "^(0|[1-9][0-9]*)\$" }
    private var isLoggedIn = false
    private var selectedLang: String = "English"
    private lateinit var bankName: String
    private lateinit var phone: String
    private lateinit var currentAmount: String
    private lateinit var translator: FirebaseTranslator
    private lateinit var appViewModel: AppViewModel
    private val username: TextView
        get() = findViewById(R.id.name_dialog)
    private val payerPhone: TextView
        get() = findViewById(R.id.phone_dialog)
    private val bank: TextView
        get() = findViewById(R.id.bank_dialog)
    private val payAmount: TextView
        get() = findViewById(R.id.amount_dialog)
    private val dialogLayout: RelativeLayout
        get() = findViewById(R.id.finalPayment_dialog)
    private val paymentWhiteLayout: LinearLayout
        get() = findViewById(R.id.paymentWhiteLayout)
    private val paymentProgressbar: LottieAnimationView
        get() = findViewById(R.id.paymentProgressbar)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        this.onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (!flag) {
                    binding.voiceInputLayout.visibility = View.GONE
                    binding.frameLayout.visibility = View.VISIBLE
                    flag = true
                }
            }
        })


        appViewModel = ViewModelProvider(this)[AppViewModel::class.java]
        appViewModel.userdata.observe(this) {
            if (it == null) {
                startActivity(Intent(this, AuthenticationActivity::class.java))
            } else {
                appViewModel.fetchAccountDetails(it)
                appViewModel.accDetails.observe(this) {
                    bankName = it[4]
                    selectedLang = it[6]
                    phone = it[1]
                    currentAmount = it[3]
                    if (selectedLang != "English") {
                        prepareTranslateModel()
                    }
                }
                checkDeviceHasBiometric()
                textToSpeech(
                    this@DashboardActivity,
                    "Use fingerprint or default phone unlock to logged in"
                )
                biometricPrompt.authenticate(promptInfo)
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }
        window.statusBarColor = Color.WHITE

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
                    }, 10000)
                }

                override fun onAuthenticationSucceeded(
                    result: BiometricPrompt.AuthenticationResult
                ) {
                    super.onAuthenticationSucceeded(result)
                    if (!isLoggedIn) {
                        isLoggedIn = true
                        textToSpeech(
                            this@DashboardActivity,
                            "Logged in successfully"
                        )
                        Toast.makeText(
                            this@DashboardActivity,
                            "Authentication succeeded!", Toast.LENGTH_SHORT
                        ).show()
                        replaceFragments(Home())
                    } else {
                        appViewModel.makePayment(amount, phoneNumber)
                        var st = false
                        appViewModel.paymentStatusData.observe(this@DashboardActivity) {
                            st = when (it) {
                                "success" -> true
                                "failure" -> false
                                else -> return@observe
                            }
                            paymentWhiteLayout.visibility = View.VISIBLE
                            paymentProgressbar.visibility = View.VISIBLE
                        }
                        Handler().postDelayed({
                            paymentWhiteLayout.visibility = View.GONE
                            paymentProgressbar.visibility = View.GONE
                            binding.voiceInputLayout.visibility = View.GONE
                            binding.frameLayout.visibility = View.VISIBLE
                            flag = true
                            binding.nameVoiceInput.text = ""
                            binding.amountVoiceInput.text = ""
                            binding.phoneNumberVoiceInput.text = ""
                            binding.descriptionVoiceInput.text = ""
                            count = 0
                            if (!st) {
                                replaceFragments(Failure())
                                textToSpeech(
                                    this@DashboardActivity,
                                    "Payment Fail,       Given Phone number not registered,         returning to the payment page"
                                )
                                Toast.makeText(this@DashboardActivity, "Payment fail, given phone number not registered", Toast.LENGTH_LONG).show()
                                Handler().postDelayed({
                                    replaceFragments(Payment())
                                }, 9000)
                            } else {
                                replaceFragments(Success())
                                textToSpeech(
                                    this@DashboardActivity,
                                    "Payment Successful,    returning to the home page"
                                )
                                Toast.makeText(this@DashboardActivity, "Payment Successful", Toast.LENGTH_LONG).show()
                                Handler().postDelayed({
                                    replaceFragments(Home())
                                }, 7000)
                            }
                        }, 5000)
                    }
                }
                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    textToSpeech(this@DashboardActivity, "Log in failed")
                    Toast.makeText(
                        this@DashboardActivity, "Authentication failed",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })

        promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Unlock Divya Pay")
            .setSubtitle("Enter phone screen lock pattern, PIN, password or fingerprint")
            .setDeviceCredentialAllowed(true)
            .setConfirmationRequired(true)
            .build()

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
        when (biometricManager.canAuthenticate(BIOMETRIC_STRONG or DEVICE_CREDENTIAL)) {
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
                                BIOMETRIC_STRONG or DEVICE_CREDENTIAL
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
        if (tts.isSpeaking) {
            tts.stop()
            tts.shutdown()
        }
        val currentFragment = supportFragmentManager.findFragmentById(R.id.frame_layout)
        when (gesture) {
            GestureDetector.Gesture.TAP ->
                return true
            GestureDetector.Gesture.SWIPE_RIGHT -> {
                if (!flag) {
                    if (tts.isSpeaking) {
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
                    } else if ((count == 1) && (Integer.parseInt(amount) > 10000)) {
                        textToSpeech(
                            this,
                            "You crossed maximum transaction limit, your maximum transaction amount is 10000 rupees, double tap and say valid amount"
                        )
                    } else if ((count == 1) && (Integer.parseInt(amount) > Integer.parseInt(
                            currentAmount
                        ))
                    ) {
                        textToSpeech(
                            this,
                            "You don't have sufficient balance in your account"
                        )
                        Toast.makeText(
                            this,
                            "You don't have sufficient balance in your account",
                            Toast.LENGTH_LONG
                        ).show()
                    } else if ((count == 1) && (Integer.parseInt(currentAmount) == 0)) {
                        textToSpeech(
                            this,
                            "You don't have sufficient balance in your account"
                        )
                        Toast.makeText(
                            this,
                            "You don't have sufficient balance in your account",
                            Toast.LENGTH_LONG
                        ).show()
                    } else if ((count == 1) && (Integer.parseInt(amount) <= 0)) {
                        textToSpeech(
                            this,
                            "Enter valid amount"
                        )
                        Toast.makeText(
                            this,
                            "Enter valid amount",
                            Toast.LENGTH_LONG
                        ).show()
                    } else if ((count == 2) && (!phoneNumber.matches(validPhoneNumberPattern.toRegex()) || phoneNumber.isEmpty())) {
                        textToSpeech(
                            this,
                            "Say valid phone number, double tap to try again "
                        )
                        Toast.makeText(
                            this,
                            "Say valid phone number, double tap to try again",
                            Toast.LENGTH_LONG
                        ).show()
                    } else if ((count == 2) && (phoneNumber == phone)) {
                        textToSpeech(
                            this,
                            "You can't send money to your own number, double tap and say another number"
                        )
                        Toast.makeText(
                            this,
                            "Say valid phone number, double tap to try again",
                            Toast.LENGTH_LONG
                        ).show()
                    } else if (count == 3) {
                        binding.makePayText.text = "Swipe right to make payment"
                        showDialog()
                        dialogLayout.visibility = View.VISIBLE
                        textToSpeech(
                            this,
                            "Receiver name is $name, selected amount is $amount rupees, receiver mobile number is ${
                                spaceGiving(
                                    phoneNumber
                                )
                            } and note is $description, now do swipe right to make payment and swipe left to cancel the payment"
                        )
                        count++
                    } else if (count == 4) {
                        dialogLayout.visibility = View.GONE
                        voicePFBefore()
                        biometricPrompt.authenticate(promptInfo)
                    } else {
                        count++
                        voicePFBefore()
                    }

                } else {
                    when (currentFragment) {
                        is Home -> replaceFragments(Payment())
                        is Payment -> {
                            flag = false
                            supportFragmentManager.findFragmentById(R.id.frame_layout)?.onDestroy()
                            binding.voiceInputLayout.visibility = View.VISIBLE
                            binding.frameLayout.visibility = View.GONE
                            voicePFBefore()
                        }
                        is Account -> replaceFragments(Account())
//                    is PaymentForm -> {
//                        flag = false
//                        supportFragmentManager.findFragmentById(R.id.frame_layout)?.onDestroy()
//                        binding.voiceInputLayout.visibility = View.VISIBLE
//                        binding.frameLayout.visibility = View.GONE
//                        voicePFBefore()
//                    }
                    }
                }
                return true
            }
            GestureDetector.Gesture.SWIPE_LEFT -> {
                if (!flag) {
                    textToSpeech(this, "Payment cancel")
                    flag = true
                    count = 0
                    binding.voiceInputLayout.visibility = View.GONE
                    binding.frameLayout.visibility = View.VISIBLE
                    binding.nameVoiceInput.text = ""
                    binding.amountVoiceInput.text = ""
                    binding.phoneNumberVoiceInput.text = ""
                    binding.descriptionVoiceInput.text = ""
                    dialogLayout.visibility = View.GONE
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

    @SuppressLint("SetTextI18n")
    fun showDialog() {
        username.text = name
        payerPhone.text = phoneNumber
        payAmount.text = "₹$amount"
        bank.text = bankName
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

    private fun fixNumber(s: String): String {
        var str = s
        if (str.contains(" ")) str = str.replace(" ", "")
        if (str.contains(",")) str = str.replace(",", "")
        if (str.contains("suno")) str = str.replace("suno", "0")
        if (str.contains("sunno")) str = str.replace("suno", "0")
        return str
    }

    private fun fixAmount(s: String): String {
        var str = s
        if (str.contains(" ")) str = str.replace(" ", "")
        if (str.contains(",")) str = str.replace(",", "")
        if (str.contains("rupees")) str = str.replace("rupees", "")
        if (str.contains("rupee")) str = str.replace("rupee", "")
        if (str.contains("rupay")) str = str.replace("rupay", "")
        if (str.contains("rupaye")) str = str.replace("rupaye", "")
        if (str.contains("rupya")) str = str.replace("rupya", "")
        if (str.contains("taka")) str = str.replace("taka", "")
        if (str.contains("₹")) str = str.replace("₹", "")
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
                "Double tap and say the phone number where you want to send the money"
            )
            3 -> textToSpeech(this, "Double tap and say the note you want to add")
            4 -> textToSpeech(this, "Use fingerprint for final payment")
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
                        this@DashboardActivity, "Your selected amount is $amount",
                        Toast.LENGTH_SHORT
                    )
                    .show()
            }
            2 -> {
                binding.phoneNumberVoiceInput.text = phoneNumber
                textToSpeech(
                    this,
                    "Your given phone number is ${spaceGiving(phoneNumber)}, swipe right to confirm, double tap to say again  "
                )
                Toast
                    .makeText(
                        this@DashboardActivity, "Your given phone number is $phoneNumber",
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
                    this@DashboardActivity,
                    "Language models downloading, please wait",
                    Toast.LENGTH_SHORT
                )
                    .show()
            }
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
                        if (selectedLang == "Hindi") tts.language = Locale.forLanguageTag("hin")
                        else tts.language = Locale.forLanguageTag("bn_IN")
                        tts.setSpeechRate(0.6f)
                        tts.speak(it, TextToSpeech.QUEUE_ADD, null)
                    }
                        .addOnFailureListener {
                            Toast.makeText(
                                this@DashboardActivity,
                                it.toString(),
                                Toast.LENGTH_SHORT
                            )
                                .show()
                        }
                }
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
                        1 -> {
                            amount = fixAmount(str)
                        }
                        2 -> {
                            phoneNumber = fixNumber(str)
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
        } else if (str.contains("super logout")) {
            startActivity(Intent(this@DashboardActivity, AuthenticationActivity::class.java))
            finish()
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

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (supportFragmentManager.findFragmentById(R.id.frame_layout) == Home()) {
            finishAffinity()
            finish()
        }
        if (!flag) {
            if (dialogLayout.isVisible) {
                dialogLayout.visibility = View.GONE
                count--
                if (tts.isSpeaking) {
                    tts.stop()
                    tts.shutdown()
                }
            } else {
                flag = true
                count = 0
                binding.voiceInputLayout.visibility = View.GONE
                binding.frameLayout.visibility = View.VISIBLE
                binding.nameVoiceInput.text = null
                binding.phoneNumberVoiceInput.text = null
                binding.amountVoiceInput.text = null
                binding.descriptionVoiceInput.text = null
            }
        } else {
            replaceFragments(Home())
        }

    }
}