package com.udc.apptfg.viewmodel.orders

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.udc.apptfg.model.ErrorModel
import java.io.File

class AddItemOrderViewModel : ViewModel() {

    val error = MutableLiveData<ErrorModel>()
    val photo = MutableLiveData<Bitmap>()
    val finish = MutableLiveData<Boolean>()
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()

    fun getInfo(idItem: String) {
        if (auth.currentUser != null) {
            getItemImage(idItem)
            exists(idItem)
        }
    }

    private fun exists(idItem:String){
        val email = auth.currentUser!!.email.toString()
        db.collection("users").document(email)
            .collection("items").document(idItem).get()
            .addOnCompleteListener { res ->
                if(!res.isSuccessful || !res.result.exists()){
                    error.postValue(
                        ErrorModel("item_not_found")
                    )
                }
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

    fun addItemToOrder(idOrder: String, idItem: String, units: Int) {
        if (units <= 0)
            error.postValue(ErrorModel("units_zero"))
        else
            if (auth.currentUser != null) {
                val email = auth.currentUser!!.email.toString()
                db.collection("users").document(email)
                    .collection("orders").document(idOrder)
                    .collection("items").document(idItem).set(
                        hashMapOf(
                            "units" to units
                        )
                    ).addOnSuccessListener {
                        finish.postValue(true)
                    }.addOnFailureListener {
                        error.postValue(ErrorModel("item_not_found"))
                    }
            } else error.postValue(ErrorModel())
    }
}