package com.udc.apptfg.viewmodel.orders

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.udc.apptfg.model.orders.OrderModel
import com.udc.apptfg.util.Util
import kotlin.collections.ArrayList

class OrdersViewModel: ViewModel() {

    val orders = MutableLiveData<ArrayList<OrderModel>>()
    private var ordersArray = ArrayList<OrderModel>()
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    fun newOrder(){
        if (auth.currentUser != null) {
            val email = auth.currentUser!!.email.toString()
            val userRef = db.collection("users").document(email)
            val ordersRef = userRef.collection("orders")
            val date = Util.getTodayDate()
            val data = hashMapOf(
                "date" to date,
                "completed" to false
            )
            ordersRef.add(data).addOnSuccessListener { order ->
                ordersArray.add(
                    OrderModel(order.id, date, false)
                )
                orders.postValue(ordersArray)
            }
        }
    }

    fun getAll() {
        if (auth.currentUser != null) {
            val email = auth.currentUser!!.email.toString()
            val userRef = db.collection("users").document(email)
            val ordersRef = userRef.collection("orders")
            ordersArray = ArrayList()
            ordersRef.get().addOnSuccessListener { ordersRes ->
                for (order in ordersRes) {
                    ordersArray.add(
                        OrderModel(
                            order.id,
                            order.get("date") as String,
                            order.get("completed") as Boolean
                        )
                    )
                }
                orders.postValue(ordersArray)
            }
        }
    }

}