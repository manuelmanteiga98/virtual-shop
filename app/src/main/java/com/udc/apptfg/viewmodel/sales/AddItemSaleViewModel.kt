package com.udc.apptfg.viewmodel.sales

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.udc.apptfg.model.ErrorModel
import java.io.File
import kotlin.math.roundToInt

class AddItemSaleViewModel : ViewModel() {

    val error = MutableLiveData<ErrorModel>()
    val photo = MutableLiveData<Bitmap>()
    val finish = MutableLiveData<Boolean>()
    val maxUnits = MutableLiveData<Int>()
    val unitPrice = MutableLiveData<Double>()
    private var price: Double? = null
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()

    fun getInfo(idItem: String) {
        if (auth.currentUser != null) {
            getItemImage(idItem)
            getItemFields(idItem)
        }
    }

    private fun getItemImage(id: String) {
        val localFile = File.createTempFile(id, "jpg")
        val path = auth.currentUser!!.email + "/items/" + id
        val reference = storage.reference.child(path)
        reference.getFile(localFile).addOnSuccessListener {
            photo.postValue(
                Bitmap.createScaledBitmap(
                    BitmapFactory.decodeFile(localFile.absolutePath),
                    200,
                    200,
                    false
                )
            )
        }
    }

    private fun getItemFields(id: String) {
        val email = auth.currentUser!!.email.toString()
        db.collection("users").document(email)
            .collection("items").document(id).get()
            .addOnCompleteListener { res ->
                if(res.isSuccessful && res.result.exists()){
                    val item = res.result
                    maxUnits.postValue(item.getLong("units")!!.toInt())
                    unitPrice.postValue(item.getDouble("price"))
                    price = item.getDouble("price")
                } else{
                    error.postValue(
                        ErrorModel("item_not_found")
                    )
                }
            }
    }

    fun addItemToSale(idSale: String, idItem: String, units: Int) {
        if ((maxUnits.value == null) || (price == null)) {
            // max units or price is not available
            error.postValue(
                ErrorModel()
            )
        } else {
            if (units == 0) error.postValue(ErrorModel("units_zero"))
            else if (units > maxUnits.value!!) error.postValue(ErrorModel("not_enough_units"))
            else if (auth.currentUser != null) {
                val email = auth.currentUser!!.email.toString()
                val totalPrice = price!! * units
                db.collection("users").document(email)
                    .collection("sales").document(idSale)
                    .collection("items").document(idItem).set(
                        hashMapOf(
                            "units" to units,
                            "total_price" to (totalPrice * 100.0).roundToInt() / 100.0
                        )
                    ).addOnSuccessListener {
                        finish.postValue(true)
                    }.addOnFailureListener {
                        error.postValue(ErrorModel("item_not_found"))
                    }
            }
        }
    }

}