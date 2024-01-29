package com.udc.apptfg.viewmodel.inventory

import android.content.DialogInterface
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.udc.apptfg.model.ErrorModel
import com.udc.apptfg.model.inventory.ItemModel
import com.udc.apptfg.repositories.inventory.InventoryDataRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import java.io.ByteArrayOutputStream
import java.io.File
import javax.inject.Inject

@HiltViewModel
class InventoryViewModel @Inject constructor(
    private val inventoryDataRepository: InventoryDataRepository
): ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()
    private val auth = FirebaseAuth.getInstance()
    var loaded = MutableLiveData<Boolean>()
    val finish = MutableLiveData<Boolean>()
    val error = MutableLiveData<ErrorModel>()
    val items = MutableLiveData<ArrayList<ItemModel>>()
    private var itemsList = ArrayList<ItemModel>()
    var categories = MutableLiveData<ArrayList<String>>()

    fun addItem(item: ItemModel, builder: AlertDialog.Builder, texts: Map<String, String>) {
        inventoryDataRepository.addItem(item){}
//        if (auth.currentUser != null) {
//            val email: String = auth.currentUser!!.email.toString()
//            val userRef = db.collection("users").document(email)
//            userRef.collection("items").document(item.id).get().addOnSuccessListener { result ->
//                if (result.exists()) {
//                    builder.setMessage(texts["msg"])
//                    builder.setPositiveButton(texts["accept"]) { _: DialogInterface, _: Int ->
//                        addItemAux(email, item)
//                    }
//                    builder.setNegativeButton(texts["cancel"], null)
//                    val dialog: AlertDialog = builder.create()
//                    dialog.show()
//                } else {
//                    addItemAux(email, item)
//                }
//            }.addOnFailureListener {
//                finish.postValue(true)
//            }
//        } else finish.postValue(true)
    }

    private fun addItemAux(email: String, item: ItemModel) {
        if (item.name.isBlank()) error.postValue(ErrorModel("name_blank"))
        else if (item.id.isBlank()) error.postValue(ErrorModel("code_blank"))
        else if (item.price <= 0) error.postValue(ErrorModel("invalid_price"))
        else if (item.cost <= 0) error.postValue(ErrorModel("invalid_cost"))
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
                        val pathRef = storageRef.child(auth.currentUser!!.email + "/items")
                        val itemRef = pathRef.child(item.id)
                        val stream = ByteArrayOutputStream()
                        item.image?.compress(Bitmap.CompressFormat.JPEG, 10, stream)
                        itemRef.putBytes(stream.toByteArray())
                    }
                    finish.postValue(true)
                }
                .addOnFailureListener {
                    error.postValue(ErrorModel("unexpected"))
                }
        }
    }

    fun deleteItem(id: String) {
        if (auth.currentUser != null) {
            val email: String = auth.currentUser!!.email.toString()
            db.collection("users").document(email)
                .collection("items").document(id).delete().addOnSuccessListener {
                    val storageRef = storage.reference
                    val pathRef = storageRef.child(auth.currentUser!!.email + "/items")
                    val itemRef = pathRef.child(id)
                    itemRef.delete()
                    for (item in itemsList) {
                        if (item.id == id) {
                            itemsList.remove(item)
                            items.postValue(itemsList)
                            break
                        }
                    }
                }
        }
    }

    fun getAll() {
        if (auth.currentUser != null) {
            if (itemsList.isNotEmpty()) itemsList = ArrayList()
            val email: String = auth.currentUser!!.email.toString()
            db.collection("users").document(email)
                .collection("items").get().addOnSuccessListener { documents ->
                    if(documents.isEmpty){
                        loaded.postValue(true)
                    }
                    for ((i, document) in documents.withIndex()) {
                        val code = document.id
                        val name = document.get("name").toString()
                        val cost = document.getDouble("cost")!!.toDouble()
                        val price = document.getDouble("price")!!.toDouble()
                        val category = document.getString("category").toString()
                        val units = document.getLong("units")!!.toInt()
                        val limit = document.get("units_limit") as Long?
                        if (limit != null) {
                            itemsList.add(
                                ItemModel(
                                    code,
                                    name,
                                    cost,
                                    price,
                                    category,
                                    units,
                                    limit.toInt()
                                )
                            )
                        } else {
                            itemsList.add(ItemModel(code, name, cost, price, category, units, null))
                        }
                        items.postValue(itemsList)
                        getImage(i)
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

    private fun getImage(index: Int) {
        val item = itemsList[index]
        val name = item.name
        val localfile = File.createTempFile(name, "jpg")
        val path = auth.currentUser!!.email + "/items/" + item.id
        val reference = storage.reference.child(path)
        reference.getFile(localfile).addOnSuccessListener {
            val fullItem = ItemModel(
                item.id,
                item.name,
                item.cost,
                item.price,
                item.category,
                item.units,
                item.unitsLimit,
                Bitmap.createScaledBitmap(
                    BitmapFactory.decodeFile(localfile.absolutePath),
                    200,
                    200,
                    false
                )

            )
            itemsList[index] = fullItem
            items.postValue(itemsList)
        }
    }

}