package com.udc.apptfg.viewmodel.employees

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.udc.apptfg.model.employees.EmployeeModel
import com.udc.apptfg.model.inventory.ItemModel
import java.io.File

class EmployeesViewModel: ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()
    private var employeeList = ArrayList<EmployeeModel>()
    val employees = MutableLiveData<ArrayList<EmployeeModel>>()

    fun getAll(){
        if(auth.currentUser!=null){
            employeeList = ArrayList<EmployeeModel>()
            val email = auth.currentUser!!.email.toString()
            db.collection("users").whereEqualTo("mgr_email", email)
                .get().addOnSuccessListener { employeesRes ->
                    for (employee in employeesRes){
                        employeeList.add(
                            EmployeeModel(
                                employee.id,
                                employee.getString("name").toString(),
                                employee.getString("surname").toString()
                            )
                        )
                    }
                    getPhotos()
                    employees.postValue(employeeList)
            }
        }
    }

    private fun getPhotos(){
        for (employee in employeeList){
                val localfile = File.createTempFile(employee.email, "jpg")
                val path = employee.email + "/" + employee.email
                val reference = storage.reference.child(path)
                reference.getFile(localfile).addOnSuccessListener {
                    val index = employeeList.indexOf(employee)
                    employeeList[index] = EmployeeModel(
                        employee.email,
                        employee.name,
                        employee.lastName,
                        Bitmap.createScaledBitmap(
                            BitmapFactory.decodeFile(localfile.absolutePath),
                            200,
                            200,
                            false
                        )
                    )
                    employees.postValue(employeeList)
                }
        }
    }
}