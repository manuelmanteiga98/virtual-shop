package com.udc.apptfg.viewmodel.statistics

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.udc.apptfg.model.statistics.StatisticsModel

class StatisticsViewModel : ViewModel() {

    val statistics = MutableLiveData<StatisticsModel>()
    private val statisticsModel = StatisticsModel()
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private var email = ""

    fun getStats() {
        if (auth.currentUser != null) {
            email = auth.currentUser!!.email.toString()
            getRegistrationDate()
            getEmployees()
            getSalesStats()
            getItemsStats()
        }
    }

    private fun getItemsStats() {
        getMaxCost()
        getMinCost()
        getMaxPrice()
        getMinPrice()
        getMaxCategory()
    }

    private fun getSalesStats() {
        getMaxSale()
        getMinSale()
        getMaxMinSalesDay()
    }

    private fun getRegistrationDate() {
        val userRef = db.collection("users").document(email)
        userRef.get().addOnSuccessListener { user ->
            statisticsModel.registrationDate = user.getString("registration_date")
            statistics.postValue(statisticsModel)
        }
    }

    private fun getMaxMinSalesDay() {
        val userRef = db.collection("users").document(email)
        val salesRef = userRef.collection("sales")

        salesRef.get().addOnSuccessListener { sales ->
            val days = HashMap<String, Int>()

            for (sale in sales) {
                // We're removing the HH:mm:ss part
                val day = sale.getString("date").toString().split(" ")[1]

                // We search the date on the hashmap and then we increment the counter
                if (days.containsKey(day)) {
                    days[day] = days[day]!!.toInt() + 1
                } else {
                    days[day] = 1
                }
            }

            if (days.isNotEmpty()) {
                val maxSalesDay = days.maxByOrNull { it.value }?.key
                val minSalesDay = days.minByOrNull { it.value }?.key

                statisticsModel.maxSalesDay = maxSalesDay
                statisticsModel.minSalesDay = minSalesDay

                statistics.postValue(statisticsModel)
            }
        }
    }


    private fun getEmployees() {
        val usersRef = db.collection("users")
        usersRef.whereEqualTo("mgr_email", email).get()
            .addOnSuccessListener { employees ->
                statisticsModel.employees = employees.size()
                statistics.postValue(statisticsModel)
            }
    }

    private fun getMaxSale() {
        val userRef = db.collection("users").document(email)
        val salesRef = userRef.collection("sales")
        salesRef.orderBy("amount", Query.Direction.DESCENDING).whereEqualTo("completed", true)
            .limit(1).get().addOnSuccessListener { sale ->
                if (!sale.isEmpty) {
                    statisticsModel.maxSale = sale.documents[0].getString("date")
                    statistics.postValue(statisticsModel)
                }
            }
    }

    private fun getMinSale() {
        val userRef = db.collection("users").document(email)
        val salesRef = userRef.collection("sales")
        salesRef.orderBy("amount").whereEqualTo("completed", true).limit(1).get()
            .addOnSuccessListener { sale ->
                if (!sale.isEmpty) {
                    statisticsModel.minSale = sale.documents[0].getString("date")
                    statistics.postValue(statisticsModel)
                }
            }
    }

    private fun getMaxCost() {
        val userRef = db.collection("users").document(email)
        val itemsRef = userRef.collection("items")
        itemsRef.orderBy("cost", Query.Direction.DESCENDING).limit(1).get()
            .addOnSuccessListener { item ->
                if (!item.isEmpty) {
                    statisticsModel.maxCost = item.documents[0].getString("name")
                    statistics.postValue(statisticsModel)
                }
            }
    }

    private fun getMinCost() {
        val userRef = db.collection("users").document(email)
        val itemsRef = userRef.collection("items")
        itemsRef.orderBy("cost").limit(1).get().addOnSuccessListener { item ->
            if (!item.isEmpty) {
                statisticsModel.minCost = item.documents[0].getString("name")
                statistics.postValue(statisticsModel)
            }
        }
    }

    private fun getMaxPrice() {
        val userRef = db.collection("users").document(email)
        val itemsRef = userRef.collection("items")
        itemsRef.orderBy("price", Query.Direction.DESCENDING).limit(1).get()
            .addOnSuccessListener { item ->
                if (!item.isEmpty) {
                    statisticsModel.maxPrice = item.documents[0].getString("name")
                    statistics.postValue(statisticsModel)
                }
            }
    }

    private fun getMinPrice() {
        val userRef = db.collection("users").document(email)
        val itemsRef = userRef.collection("items")
        itemsRef.orderBy("price").limit(1).get().addOnSuccessListener { item ->
            if (!item.isEmpty) {
                statisticsModel.minPrice = item.documents[0].getString("name")
                statistics.postValue(statisticsModel)
            }
        }
    }

    private fun getMaxCategory() {
        val userRef = db.collection("users").document(email)
        val itemsRef = userRef.collection("items")
        itemsRef.get().addOnSuccessListener { items ->
            val categories = HashMap<String, Int>()
            for (item in items) {
                val category = item.getString("category").toString()
                if (categories.containsKey(category)) categories[category] =
                    categories[category]!!.toInt() + 1
                else {
                    categories[category] = 1
                }
            }
            if (categories.size > 0) {
                statisticsModel.biggestCategory =
                    categories.maxWith { x, y -> x.value.compareTo(y.value) }.key
                statistics.postValue(statisticsModel)
            }
        }
    }

}