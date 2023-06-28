package com.udc.apptfg.viewmodel.inventory

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.udc.apptfg.model.ErrorModel
import com.udc.apptfg.model.inventory.ItemModel
import java.io.ByteArrayOutputStream
import java.io.File

class EditItemViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val storage = FirebaseStorage.getInstance()
    val item = MutableLiveData<ItemModel>()
    val finish = MutableLiveData<Boolean>()
    val error = MutableLiveData<ErrorModel>()
    var categories = MutableLiveData<ArrayList<String>>()

    fun initActivity(id: String) {
        if (auth.currentUser != null) {
            val email: String = auth.currentUser!!.email.toString()
            db.collection("users").document(email)
                .collection("items").document(id).get().addOnSuccessListener { data ->
                    val name = data.getString("name").toString()
                    val cost = data.getDouble("cost")!!.toDouble()
                    val price = data.getDouble("price")!!.toDouble()
                    val category = data.getString("category").toString()
                    val units = data.getLong("units")!!.toInt()
                    val limit: Int? = (data.get("units_limit") as Long?)?.toInt()

                    // image
                    val localfile = File.createTempFile(name, "jpg")
                    val path = auth.currentUser!!.email + "/items/" + id
                    val reference = storage.reference.child(path)
                    reference.getFile(localfile).addOnSuccessListener {
                        val bitmap = BitmapFactory.decodeFile(localfile.absolutePath)
                        item.postValue(
                            ItemModel(
                                id,
                                name,
                                cost,
                                price,
                                category,
                                units,
                                limit,
                                bitmap
                            )
                        )
                    }.addOnFailureListener {
                        item.postValue(
                            ItemModel(
                                id,
                                name,
                                cost,
                                price,
                                category,
                                units,
                                limit
                            )
                        )
                    }
                }.addOnFailureListener {
                    finish.postValue(true)
                }
        } else finish.postValue(true)
    }

    fun edit(item: ItemModel) {
        if (auth.currentUser != null) {
            val email: String = auth.currentUser!!.email.toString()
            if (item.name.isBlank()) error.postValue(ErrorModel("name_blank"))
            else {
                try {
                    if (item.cost <= 0) error.postValue(ErrorModel("invalid_cost"))
                    else if (item.price <= 0) error.postValue(ErrorModel("invalid_price"))
                    else if (item.units <= 0) error.postValue(ErrorModel("invalid_units"))
                    else {
                        db.collection("users").document(email)
                            .collection("items").document(item.id).set(
                                hashMapOf(
                                    "name" to item.name,
                                    "cost" to item.cost,
                                    "price" to item.price,
                                    "category" to item.category,
                                    "units" to item.units,
                                    "units_limit" to item.unitsLimit
                                )
                            ).addOnSuccessListener {
                                if (item.image != null) {
                                    val storageRef = storage.reference
                                    val pathRef =
                                        storageRef.child(auth.currentUser!!.email + "/items")
                                    val itemRef = pathRef.child(item.id)
                                    itemRef.delete()
                                    val stream = ByteArrayOutputStream()
                                    item.image?.compress(Bitmap.CompressFormat.JPEG, 10, stream)
                                    itemRef.putBytes(stream.toByteArray())
                                }
                                finish.postValue(true)
                            }
                            .addOnFailureListener { error.postValue(ErrorModel("unexpected")) }
                    }

                } catch (e: java.lang.NumberFormatException) {
                    error.postValue(ErrorModel("input_validation"))
                }
            }
        }
    }

    fun getAllCategories() {
        if (auth.currentUser != null) {
            val list = ArrayList<String>()
            val email: String = auth.currentUser!!.email.toString()
            db.collection("/users").document(email)
                .collection("/categories").get().addOnSuccessListener { categoriesRes ->
                    for (category in categoriesRes) {
                        category.getString("name")?.let { list.add(it) }
                    }
                    categories.postValue(list)
                }
        }
    }

}