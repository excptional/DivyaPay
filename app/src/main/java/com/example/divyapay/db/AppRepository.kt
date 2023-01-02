package com.example.divyapay.db

import android.annotation.SuppressLint
import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.*
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class AppRepository(private val application: Application) {

    private val userLiveData = MutableLiveData<FirebaseUser?>()
    val userData: LiveData<FirebaseUser?>
        get() = userLiveData
    private val responseLiveData = MutableLiveData<Response<String>>()
    val response: LiveData<Response<String>>
        get() = responseLiveData
    private val paymentStatusLivedata = MutableLiveData<String>()
    val paymentStatusData: LiveData<String>
        get() = paymentStatusLivedata
    private val accDetailsLiveData = MutableLiveData<ArrayList<String>>()
    val accDetails: LiveData<ArrayList<String>>
        get() = accDetailsLiveData
    private val transactionDetailsLiveData = MutableLiveData<ArrayList<DocumentSnapshot>>()
    val transactionDetails: LiveData<ArrayList<DocumentSnapshot>>
        get() = transactionDetailsLiveData

    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firebaseDB: FirebaseFirestore = FirebaseFirestore.getInstance()

    fun login(email: String?, password: String?) {
        firebaseAuth.signInWithEmailAndPassword(email!!, password!!)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    responseLiveData.postValue(Response.Success())
                    userLiveData.postValue(firebaseAuth.currentUser)
                } else {
                    responseLiveData.postValue(Response.Failure(getErrorMassage(task.exception!!)))
                }
            }
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
        val data = mapOf(
            "email" to email,
            "password" to password,
            "mobile number" to number,
            "name" to name,
            "account no" to accountNo,
            "bank name" to bank,
            "ifsc code" to ifsc,
            "language" to language,
            "acc balance" to "10000",
            "uid" to ""
        )
        firebaseAuth.createUserWithEmailAndPassword(email, password).addOnSuccessListener {
            val doc = firebaseDB.collection("users").document(firebaseAuth.currentUser!!.uid)
            doc.set(data)
            doc.get().addOnSuccessListener {
                doc.update("uid", firebaseAuth.currentUser!!.uid)
            }
            responseLiveData.postValue(Response.Success())
            userLiveData.postValue(firebaseAuth.currentUser)
        }
            .addOnFailureListener {
                responseLiveData.postValue(Response.Failure(getErrorMassage(it)))
            }
    }

    fun fetchAccountDetails(user: FirebaseUser) {
        firebaseDB.collection("users").document(user.uid).get()
            .addOnSuccessListener {
                val list = ArrayList<String>()
                list.add(it.getString("name").toString())
                list.add(it.getString("mobile number").toString())
                list.add(it.getString("account no").toString())
                list.add(it.getString("acc balance").toString())
                list.add(it.getString("bank name").toString())
                list.add(it.getString("ifsc code").toString())
                list.add(it.getString("language").toString())

                responseLiveData.postValue(Response.Success())
                accDetailsLiveData.postValue(list)
            }
            .addOnFailureListener {
                responseLiveData.postValue(Response.Failure(getErrorMassage(it)))
            }
    }

    @SuppressLint("SuspiciousIndentation")
    fun makePayment(amount: String, phone: String) {
        val amountInt = Integer.parseInt(amount)
        val doc = firebaseDB.collection("users").document(firebaseAuth.currentUser!!.uid)

        val doc2 = firebaseDB.collection("users")
        doc2.get().addOnSuccessListener {
            for (i in it) {
                if (phone == i.getString("mobile number")) {
                    doc.get().addOnSuccessListener {
                        val userBalance1 = Integer.parseInt(it.getString("acc balance")!!)
                        val newBalance1 = userBalance1 - amountInt
                        doc.update("acc balance", newBalance1.toString())
                    }
                    val userBalance2 = Integer.parseInt(i.getString("acc balance")!!)
                    val newBalance2 = userBalance2 + amountInt
                    val doc3 = firebaseDB.collection("users").document(i.getString("uid")!!)
                    doc3.get().addOnSuccessListener {
                        doc3.update("acc balance", newBalance2.toString())
                        addTransaction(phone, amount)
                        paymentStatusLivedata.postValue("success")
                    }
                        .addOnFailureListener {
                            paymentStatusLivedata.postValue("failure")
                        }
                } else {
                    paymentStatusLivedata.postValue("failure")
                }
            }
        }
    }

    private fun addTransaction(
        receiverPhone: String,
        amount: String
    ) {

        val dateAndTime = SimpleDateFormat("MMM dd, yyyy 'at' HH:mm aa", Locale.getDefault()).format(Date())
        val date = SimpleDateFormat("yyyy_MM_dd_hh:mm:ss", Locale.getDefault()).format(Date())

        val creditData = mutableMapOf(
            "name" to "",
            "acc no" to "",
            "phone no" to "",
            "uid" to firebaseAuth.currentUser!!.uid,
            "amount" to amount,
            "status" to "credited",
            "date" to dateAndTime,
            "doc id" to date
        )
        val debitData = mutableMapOf(
            "name" to "",
            "acc no" to "",
            "phone no" to receiverPhone,
            "uid" to "",
            "amount" to amount,
            "status" to "debited",
            "date" to dateAndTime,
            "doc id" to date
        )

        val doc = firebaseDB.collection("users")

        doc.document(firebaseAuth.currentUser!!.uid).get().addOnSuccessListener {
            creditData["name"] = it.getString("name").toString()
            creditData["acc no"] = it.getString("account no").toString()
            creditData["phone no"] = it.getString("mobile number").toString()
            doc.get().addOnSuccessListener {
                for (i in it) {
                    if (receiverPhone == i.getString("mobile number")) {
                        debitData["uid"] = i.getString("uid").toString()
                        debitData["name"] = i.getString("name").toString()
                        debitData["acc no"] = i.getString("account no").toString()
                        debitData["name"] = i.getString("name").toString()
                        firebaseDB.collection("transactions").document("transactions")
                            .collection(i.getString("uid")!!).document(date).set(creditData)
                        firebaseDB.collection("transactions").document("transactions")
                            .collection(firebaseAuth.currentUser!!.uid).document(date)
                            .set(debitData)
                    }
                }
            }
        }
    }

    fun fetchTransactionDetails(uid: String) {
        firebaseDB.collection("transactions").document("transactions")
            .collection(uid).orderBy("doc id", Query.Direction.DESCENDING).get().addOnSuccessListener { documents ->
                val list = arrayListOf<DocumentSnapshot>()
                for (document in documents) {
                    list.add(document)
                }
                transactionDetailsLiveData.postValue(list)
            }
    }

    private fun getErrorMassage(e: Exception): String {
        val colonIndex = e.toString().indexOf(":")
        return e.toString().substring(colonIndex + 2)
    }

    init {
        if (firebaseAuth.currentUser != null) {
            userLiveData.postValue(firebaseAuth.currentUser)
        } else {
            userLiveData.postValue(null)
        }
    }

}

