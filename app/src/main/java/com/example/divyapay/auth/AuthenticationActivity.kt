package com.example.divyapay.auth

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.divyapay.R

class AuthenticationActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_authentication)

//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
//        }

        this.supportFragmentManager.beginTransaction().replace(R.id.authFrameLayout, SignIn()).commit()
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (supportFragmentManager.findFragmentById(R.id.authFrameLayout) == SignIn()) {
            finishAffinity()
            finish()
        }else{
            this.supportFragmentManager.beginTransaction().replace(R.id.authFrameLayout, SignIn()).commit()
        }
    }
}