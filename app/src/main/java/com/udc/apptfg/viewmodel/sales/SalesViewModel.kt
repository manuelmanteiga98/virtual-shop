package com.udc.apptfg.viewmodel.sales

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.udc.apptfg.model.ErrorModel
import com.udc.apptfg.model.sales.SaleModel
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.time.Duration.Companion.hours

class SalesViewModel : ViewModel() {

    val error = MutableLiveData<ErrorModel>()
    val sales = MutableLiveData<ArrayList<SaleModel>>()
    private var saleArray = ArrayList<SaleModel>()
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    fun newSale() {
        if (auth.currentUser != null) {
            val email = auth.currentUser!!.email.toString()
            val userRef = db.collection("users").document(email)
            val salesRef = userRef.collection("sales")
            val date = getTodayDate()
            val data = hashMapOf(
                "date" to date,
                "completed" to false,
                "amount" to 0.0
            )
            salesRef.add(data).addOnSuccessListener { sale ->
                saleArray.add(
                    SaleModel(sale.id, date, false, 0.0)
                )
                sales.postValue(saleArray)
            }
        }
    }

    fun getAll() {
        if (auth.currentUser != null) {
            val email = auth.currentUser!!.email.toString()
            val userRef = db.collection("users").document(email)
            val salesRef = userRef.collection("sales")
            saleArray = ArrayList()
            salesRef.get().addOnSuccessListener { salesRes ->
                for (sale in salesRes) {
                    saleArray.add(
                        SaleModel(
                            sale.id,
                            sale.get("date") as String,
                            sale.get("completed") as Boolean,
                            sale.get("amount") as Double
                        )
                    )
                }
                sales.postValue(saleArray)
            }
        }
    }

    private fun getTodayDate(): String {
        val currentDate = Date()
        val formatter = SimpleDateFormat("HH:mm:ss dd-MM-yyyy", Locale.getDefault())
        return formatter.format(currentDate)
    }

}