package com.udc.apptfg.viewmodel.inventory

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class CategoryViewModel : ViewModel() {

    val categories = MutableLiveData<ArrayList<String>>()
    private var list: ArrayList<String> = ArrayList()
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    fun getAllCategories() {
        if (auth.currentUser != null) {
            val email = auth.currentUser?.email.toString()
            db.collection("/users").document(email)
                .collection("/categories").get().addOnSuccessListener { categoriesRes ->
                    list = ArrayList()
                    for (category in categoriesRes) {
                        list.add(category.getString("name").toString())
                    }
                    list.sort()
                    categories.postValue(list)
                }
        }
    }

    fun addCategory(name: String) {
        if (auth.currentUser != null) {
            val email = auth.currentUser?.email.toString()
            if (!list.contains(name)) {
                val data = hashMapOf(
                    "name" to name
                )
                val categoryRef = db.collection("/users").document(email)
                    .collection("/categories").document()
                categoryRef.set(data).addOnSuccessListener {
                    list.add(name)
                    list.sort()
                    categories.postValue(list)
                }
            }
        }
    }

    fun delCategory(name: String) {
        if (auth.currentUser != null) {
            if (list.contains(name)) {
                val email = auth.currentUser?.email.toString()
                list.remove(name)
                categories.postValue(list)
                val categoryRef = db.collection("/users").document(email)
                    .collection("/categories")
                categoryRef.whereEqualTo("name", name).get().addOnSuccessListener { category ->
                    val id = category.documents[0].id
                    categoryRef.document(id).delete()
                }
            }
        }
    }

}