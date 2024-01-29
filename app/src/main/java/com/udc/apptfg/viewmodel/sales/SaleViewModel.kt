package com.udc.apptfg.viewmodel.sales

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.udc.apptfg.model.ErrorModel
import com.udc.apptfg.model.sales.ItemSaleModel
import kotlinx.coroutines.tasks.await
import java.io.File
import kotlin.collections.ArrayList
import kotlin.math.roundToInt

class SaleViewModel : ViewModel() {
    val saleItems = MutableLiveData<ArrayList<ItemSaleModel>>()
    val error = MutableLiveData<ErrorModel>()
    val complete = MutableLiveData<Boolean>()
    private var itemList = ArrayList<ItemSaleModel>()
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()

    fun getAll(id: String) {
        if (auth.currentUser != null) {
            val email = auth.currentUser!!.email.toString()
            val userRef = db.collection("users").document(email)
            val saleRef = userRef.collection("sales").document(id)
            val itemsRef = saleRef.collection("items")
            itemsRef.get().addOnSuccessListener { items ->
                itemList = ArrayList()
                for (item in items) {
                    itemList.add(
                        ItemSaleModel(
                            item.id,
                            item.getLong("units")!!.toInt(),
                            item.getDouble("total_price")!!
                        )
                    )
                }
                saleItems.postValue(itemList)
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
                saleItems.postValue(itemList)
            }
        }
    }

    private fun getItemsName() {
        for (item in itemList) {
            db.collection("users").document(auth.currentUser!!.email.toString())
                .collection("items").document(item.id).get()
                .addOnSuccessListener { response ->
                    itemList.find { it.id == item.id }?.name = response.getString("name").toString()
                    saleItems.postValue(itemList)
                }
        }
    }

    fun increase(idSale: String, idItem: String) {
        if (auth.currentUser != null) {
            val email = auth.currentUser!!.email.toString()
            val userRef = db.collection("users").document(email)
            val itemRef = userRef.collection("items").document(idItem)
            val saleRef = userRef.collection("sales").document(idSale)
            val itemSaleRef = saleRef.collection("items").document(idItem)
            itemRef.get().addOnSuccessListener { item ->
                val itemSale = itemList.find { it.id == idItem }
                if (itemSale != null) {
                    if (item.getLong("units")!!.toInt() > itemSale.units) {
                        val newPrice =
                            (((itemSale.units + 1) * (item.get("price") as Double)) * 100.0).roundToInt() / 100.0
                        itemSaleRef.set(
                            hashMapOf(
                                "units" to itemSale.units + 1,
                                "total_price" to newPrice
                            )
                        )
                        itemList.find { it.id == idItem }?.units = itemSale.units + 1
                        itemList.find { it.id == idItem }?.price = newPrice
                        saleItems.postValue(itemList)
                    }
                }
            }
        } else {
            error.postValue(ErrorModel())
        }
    }

    fun decrease(idSale: String, idItem: String) {
        if (auth.currentUser != null) {
            val email = auth.currentUser!!.email.toString()
            val userRef = db.collection("users").document(email)
            val itemRef = userRef.collection("items").document(idItem)
            val saleRef = userRef.collection("sales").document(idSale)
            val itemSaleRef = saleRef.collection("items").document(idItem)
            itemRef.get().addOnSuccessListener { item ->
                val itemSale = itemList.find { it.id == idItem }
                if (itemSale != null) {
                    if (itemSale.units - 1 > 0) {
                        val newPrice =
                            (((itemSale.units - 1) * (item.get("price") as Double)) * 100.0).roundToInt() / 100.0
                        itemSaleRef.set(
                            hashMapOf(
                                "units" to itemSale.units - 1,
                                "total_price" to newPrice
                            )
                        ).addOnSuccessListener {
                            itemList.find { it.id == idItem }?.units = itemSale.units - 1
                            itemList.find { it.id == idItem }?.price = newPrice
                            saleItems.postValue(itemList)
                        }
                    }
                }
            }
        } else {
            error.postValue(ErrorModel())
        }
    }

    fun deleteItem(idSale: String, idItem: String) {
        if (auth.currentUser != null) {
            val email = auth.currentUser!!.email.toString()
            val userRef = db.collection("users").document(email)
            val saleRef = userRef.collection("sales").document(idSale)
            val itemSaleRef = saleRef.collection("items").document(idItem)

            itemSaleRef.delete().addOnSuccessListener {
                itemList.remove(itemList.find { it.id == idItem })
                saleItems.postValue(itemList)
            }
        } else {
            error.postValue(ErrorModel())
        }
    }

    suspend fun deleteSale(id: String) {
        if (auth.currentUser != null) {
            val email = auth.currentUser!!.email.toString()
            val userRef = db.collection("users").document(email)
            val saleRef = userRef.collection("sales").document(id)
            val itemsSaleRef = saleRef.collection("items")
            itemsSaleRef.get().addOnCompleteListener {
                if (it.isSuccessful) {
                    saleRef.delete().addOnCompleteListener { result ->
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

    suspend fun complete(idSale: String) {
        var ok = true
        if(itemList.isNotEmpty()) {
            if (auth.currentUser != null) {
                val userRef = db.collection("users").document(auth.currentUser!!.email.toString())
                val itemsRef = userRef.collection("items")

                for (item in itemList) {
                    val currentItem = itemsRef.document(item.id).get().await()
                    if (currentItem.exists()) {
                        if (currentItem.getLong("units")?.toInt()!! < item.units) {
                            ok = false
                            error.postValue(ErrorModel("not_enough_units", item.name))
                        }
                    } else {
                        ok = false
                        error.postValue(ErrorModel("item_not_found", item.name))
                    }
                }

                // Updates units
                if (ok) {
                    for (item in itemList) {
                        itemsRef.document(item.id).get().addOnSuccessListener { actual ->
                            val newUnits = (actual.get("units") as Long).toInt() - item.units
                            itemsRef.document(item.id).update(
                                "units", newUnits
                            )
                            val limit = actual.getLong("units_limit")

                            // Alert that units are under the limit
                            if ((limit != null) && (newUnits < limit)) {
                                val notification = hashMapOf(
                                    "subject" to "units_limit",
                                    "content" to newUnits.toString() + "," + item.name,
                                    "read" to false
                                )
                                userRef.collection("notifications").add(notification)
                            }
                        }
                    }
                    saleCompleted(idSale)
                }
            }
        } else{
            error.postValue(ErrorModel("empty_sale"))
        }
    }

    private fun saleCompleted(idSale: String) {
        val email = auth.currentUser!!.email.toString()
        var amount = 0.0
        val userRef = db.collection("users").document(email)
        val saleRef = userRef.collection("sales").document(idSale)

        for (item in itemList) {
            amount += item.price
        }
        saleRef.get()
            .addOnSuccessListener { sale ->
                saleRef.set(
                    hashMapOf(
                        "amount" to amount,
                        "completed" to true,
                        "date" to sale.getString("date")
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