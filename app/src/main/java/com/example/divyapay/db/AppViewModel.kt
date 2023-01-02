package com.example.divyapay.db

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentSnapshot

class AppViewModel(application: Application) :
    AndroidViewModel(application) {

    private val appRepository: AppRepository = AppRepository(application)
    val userdata: LiveData<FirebaseUser?>
        get() = appRepository.userData
    val response: LiveData<Response<String>>
        get() = appRepository.response
    val paymentStatusData: LiveData<String>
        get() = appRepository.paymentStatusData
    val accDetails: LiveData<ArrayList<String>>
        get() = appRepository.accDetails
    val transactionDetails: LiveData<ArrayList<DocumentSnapshot>>
        get() = appRepository.transactionDetails

    fun login(email: String?, password: String?) {
        appRepository.login(email, password)
    }

    fun signUp(
        email: String,
        password: String,
        name: String,
        number: String,
        bank: String,
        accountNo: String,
        ifsc: String,
        language: String
    ) {
     appRepository.signUp(email, password, name, number, bank, accountNo, ifsc, language)
    }

    fun fetchAccountDetails(user: FirebaseUser){
        appRepository.fetchAccountDetails(user)
    }

    fun makePayment(amount: String, phone: String){
        appRepository.makePayment(amount, phone)
    }

    fun fetchTransactions(uid: String) {
        appRepository.fetchTransactionDetails(uid)
    }
}