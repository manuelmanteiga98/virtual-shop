package com.udc.apptfg.viewmodel.orders

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.udc.apptfg.model.ErrorModel
import com.udc.apptfg.model.orders.ItemOrderModel
import java.io.File

class OrderViewModel : ViewModel() {

    val orderItems = MutableLiveData<ArrayList<ItemOrderModel>>()
    val error = MutableLiveData<ErrorModel>()
    val complete = MutableLiveData<Boolean>()
    private var itemList = ArrayList<ItemOrderModel>()
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()

    fun getAll(id: String) {
        if (auth.currentUser != null) {
            val email = auth.currentUser!!.email.toString()
            val userRef = db.collection("users").document(email)
            val orderRef = userRef.collection("orders").document(id)
            val itemsRef = orderRef.collection("items")
            itemsRef.get().addOnSuccessListener { items ->
                itemList = ArrayList()
                for (item in items) {
                    itemList.add(
                        ItemOrderModel(
                            item.id,
                            item.getLong("units")!!.toInt()
                        )
                    )
                }
                orderItems.postValue(itemList)
                getItemsImage()
                getItemsName()
            }
        } else {
            error.postValue(ErrorModel())
        }
    }

    private fun getItemsImage() {
        for (item in itemList) {
            val localFile = File.createTempFile(item.id, "jpg")
            val path = auth.currentUser!!.email + "/items/" + item.id
            val reference = storage.reference.child(path)
            reference.getFile(localFile).addOnSuccessListener {
                // Updates the photo
                itemList.find { it.id == item.id }?.image = Bitmap.createScaledBitmap(
                    BitmapFactory.decodeFile(localFile.absolutePath),
                    200,
                    200,
                    false
                )
                orderItems.postValue(itemList)
            }
        }
    }

    private fun getItemsName() {
        for (item in itemList) {
            db.collection("users").document(auth.currentUser!!.email.toString())
                .collection("items").document(item.id).get()
                .addOnSuccessListener { response ->
                    itemList.find { it.id == item.id }?.name = response.getString("name").toString()
                    orderItems.postValue(itemList)
                }
        }
    }

    fun increase(idOrder: String, idItem: String) {
        if (auth.currentUser != null) {
            val email = auth.currentUser!!.email.toString()
            val userRef = db.collection("users").document(email)
            val saleRef = userRef.collection("orders").document(idOrder)
            val itemSaleRef = saleRef.collection("items").document(idItem)
            val itemOrder = itemList.find { it.id == idItem }
            if (itemOrder != null) {
                itemSaleRef.set(
                    hashMapOf(
                        "units" to itemOrder.units + 1
                    )
                )
                itemOrder.units += 1
                orderItems.postValue(itemList)
            }
        }
        else {
            error.postValue(ErrorModel())
        }
    }

    fun decrease(idSale: String, idItem: String) {
        if (auth.currentUser != null) {
            val email = auth.currentUser!!.email.toString()
            val userRef = db.collection("users").document(email)
            val saleRef = userRef.collection("orders").document(idSale)
            val itemSaleRef = saleRef.collection("items").document(idItem)
            val item = itemList.find { it.id == idItem }
            if(item!=null) {
                if (item.units - 1 > 0) {
                    itemSaleRef.set(
                        hashMapOf(
                            "units" to item.units - 1
                        )
                    ).addOnSuccessListener {
                        itemList.find { it.id == idItem }?.units = item.units - 1
                        orderItems.postValue(itemList)
                    }
                }
            } else error.postValue(ErrorModel())
        } else {
            error.postValue(ErrorModel())
        }
    }

    fun deleteItem(idOrder: String, idItem: String) {
        if (auth.currentUser != null) {
            val email = auth.currentUser!!.email.toString()
            val userRef = db.collection("users").document(email)
            val saleRef = userRef.collection("orders").document(idOrder)
            val itemSaleRef = saleRef.collection("items").document(idItem)

            itemSaleRef.delete().addOnSuccessListener {
                itemList.remove(itemList.find { it.id == idItem })
                orderItems.postValue(itemList)
            }
        } else {
            error.postValue(ErrorModel())
        }
    }

    fun deleteOrder(idOrder: String) {
        if (auth.currentUser != null) {
            val email = auth.currentUser!!.email.toString()
            val userRef = db.collection("users").document(email)
            val orderRef = userRef.collection("orders").document(idOrder)
            val itemsOrderRef = orderRef.collection("items")
            itemsOrderRef.get().addOnCompleteListener {
                if (it.isSuccessful) {
                    orderRef.delete().addOnCompleteListener { result ->
                        if (result.isSuccessful) {
                            complete.postValue(true)
                        } else {
                            error.postValue(ErrorModel())
                        }
                    }
                } else {
                    error.postValue(ErrorModel())
                }
            }
        } else {
            error.postValue(ErrorModel())
        }
    }

    fun complete(idOrder: String) {
        if(itemList.isNotEmpty()) {
            if (auth.currentUser != null) {
                val userRef = db.collection("users").document(auth.currentUser!!.email.toString())
                val itemsRef = userRef.collection("items")

                for (item in itemList) {
                    itemsRef.document(item.id).get().addOnSuccessListener { actual ->
                        itemsRef.document(item.id).update(
                            "units", (actual.get("units") as Long).toInt() + item.units
                        )
                    }
                }
                orderCompleted(idOrder)
            }
        } else{
            error.postValue(ErrorModel("empty_order"))
        }
    }

    private fun orderCompleted(idOrder: String) {
        val email = auth.currentUser!!.email.toString()
        val userRef = db.collection("users").document(email)
        val orderRef = userRef.collection("orders").document(idOrder)

        orderRef.get()
            .addOnSuccessListener { order ->
                orderRef.set(
                    hashMapOf(
                        "completed" to true,
                        "date" to order.getString("date")
                    )
                ).addOnCompleteListener {
                    if (it.isSuccessful) {
                        complete.postValue(true)
                    } else {
                        error.postValue(ErrorModel())
                    }
                }
            }
            .addOnFailureListener {
                error.postValue(ErrorModel())
            }
    }

}